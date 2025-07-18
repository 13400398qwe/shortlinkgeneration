package com.study.shortlink.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.study.shortlink.mapper.TLinkMapper;
import com.study.shortlink.pojo.entity.TLink;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final TLinkMapper linkRepository;
    @KafkaListener(topics = "link-access-topic", groupId = "analytics-group")
    public void consume(String shortCode) {
        // 在这里，你可以实现更复杂的逻辑，比如批量更新数据库来减少I/O
        // 为了简单演示，我们直接更新PV
        TLink link = linkRepository.selectOne(new QueryWrapper<TLink>().eq("short_code", shortCode));
        if (link != null) {
            link.setPv(link.getPv() + 1);
            linkRepository.updateById(link);
        }
        System.out.println("Updated PV for: " + shortCode);
    }
}
