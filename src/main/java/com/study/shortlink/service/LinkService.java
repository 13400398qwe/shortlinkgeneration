package com.study.shortlink.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.study.shortlink.mapper.TLinkMapper;
import com.study.shortlink.pojo.entity.TLink;
import com.study.shortlink.util.Base62;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final TLinkMapper linkRepository; // 假设这是JPA或MyBatis的Mapper
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String SHORT_URL_PREFIX = "http://localhost:8080/";

    @Transactional
    public String createShortLink(String longUrl) {
        // 1. 先在数据库中创建一个占位记录，以获取唯一的自增ID
        TLink placeholder = new TLink();
        placeholder.setLongUrl(longUrl);
        linkRepository.insert(placeholder); // 保存后，placeholder对象会获得数据库生成的ID

        // 2. 使用Base62算法将ID转换为短码
        String shortCode = Base62.fromBase10(placeholder.getId());

        // 3. 更新数据库记录，填上短码
        placeholder.setShortCode(shortCode);
        linkRepository.updateById(placeholder);

        // 4. 将映射关系写入Redis缓存，并设置一个过期时间（例如24小时）
        redisTemplate.opsForValue().set(shortCode, longUrl, 24, TimeUnit.HOURS);

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
}
