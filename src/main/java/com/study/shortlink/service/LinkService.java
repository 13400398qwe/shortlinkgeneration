package com.study.shortlink.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.study.shortlink.mapper.TLinkMapper;
import com.study.shortlink.pojo.entity.TLink;
import com.study.shortlink.util.Base62;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final TLinkMapper linkRepository; // 假设这是JPA或MyBatis的Mapper
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String SHORT_URL_PREFIX = "http://localhost:8080/";

    @Transactional
    public String createShortLink(String longUrl, String customCode) {
        String shortCode;
        if (customCode != null && !customCode.isEmpty()) {
            shortCode = customCode.trim();
            // 1. 校验自定义短码的合法性 (例如：长度、字符等)
            if (customCode.length() > 10 || !customCode.matches("^[a-zA-Z0-9_-]+$")) {
                throw new IllegalArgumentException("自定义短码不合法！");
            }
            // 2. redis中检查短码
            if (redisTemplate.opsForValue().get(customCode) != null) {
                throw new IllegalArgumentException("自定义短码已存在");
            }
            // 3.如果用户指定了自定义短码，则检查是否已经存在
            TLink existingLink = linkRepository.selectOne(new QueryWrapper<TLink>().eq("short_code", customCode));
            if (existingLink != null) {
                throw new IllegalArgumentException("自定义短码已存在");
            }
            redisTemplate.opsForValue().set(shortCode, longUrl, 24, TimeUnit.HOURS);
            TLink link = new TLink();
            link.setShortCode(shortCode);
            link.setLongUrl(longUrl);
            linkRepository.insert(link);
        } else {
            // 1. 先在数据库中创建一个占位记录，以获取唯一的自增ID
            TLink placeholder = new TLink();
            placeholder.setLongUrl(longUrl);
            linkRepository.insert(placeholder); // 保存后，placeholder对象会获得数据库生成的ID

            // 2. 使用Base62算法将ID转换为短码
            shortCode = Base62.fromBase10(placeholder.getId());

            // 3. 更新数据库记录，填上短码
            placeholder.setShortCode(shortCode);
            linkRepository.updateById(placeholder);

            // 4. 将映射关系写入Redis缓存，并设置一个过期时间（例如24小时）
            redisTemplate.opsForValue().set(shortCode, longUrl, 24, TimeUnit.HOURS);
        }
        return SHORT_URL_PREFIX + shortCode;
    }

    public String getLongUrl(String shortCode) {
        // 1. 先查Redis缓存
        String longUrl = redisTemplate.opsForValue().get(shortCode);

        if (longUrl != null) {
            // 缓存命中！
            // 2. 异步发送统计消息
            sendAnalyticsEvent(shortCode);
            return longUrl;
        }

        // 3. 缓存未命中，查询数据库
        TLink link = linkRepository.selectOne(new QueryWrapper<TLink>().eq("short_code", shortCode));
        if (link != null) {
            longUrl = link.getLongUrl();
            // 4. 将结果回写到Redis缓存
            redisTemplate.opsForValue().set(shortCode, longUrl, 24, TimeUnit.HOURS);
            // 5. 异步发送统计消息
            sendAnalyticsEvent(shortCode);
            return longUrl;
        }

        // 数据库也找不到
        return null;
    }

    // 异步发送统计事件到Kafka
    private void sendAnalyticsEvent(String shortCode) {
        // 为了极致性能，这里的消息体可以做得更丰富，比如带上时间戳、IP等
        kafkaTemplate.send("link-access-topic", shortCode);
    }

    public byte[] generateQrCode(String shortCode) throws WriterException, IOException {
        // 1. 拼接完整的短链接URL
        String shortUrl = SHORT_URL_PREFIX + shortCode;

        // 2. 创建二维码写入器
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        // 3. 将URL编码为二维码的矩阵表示
        // 参数：内容, 格式, 宽度, 高度
        var bitMatrix = qrCodeWriter.encode(shortUrl, BarcodeFormat.QR_CODE, 200, 200);

        // 4. 将矩阵写入到一个字节输出流中 (PNG格式)
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

        // 5. 返回图片的字节数组
        return pngOutputStream.toByteArray();
    }
}
