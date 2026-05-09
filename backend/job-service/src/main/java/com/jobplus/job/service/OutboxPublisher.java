package com.jobplus.job.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jobplus.common.entity.OutboxEvent;
import com.jobplus.job.repository.OutboxEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbox 事件发布器
 * 定时扫描 outbox 表，将 NEW 状态的事件发送到 Kafka
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventMapper outboxEventMapper;
    private final ObjectProvider<KafkaTemplate<String, String>> kafkaTemplateProvider;

    @Scheduled(fixedDelay = 3000)
    public void publish() {
        KafkaTemplate<String, String> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();

        if (kafkaTemplate == null) {
            log.debug("Kafka not configured, skip outbox publishing");
            return;
        }

        List<OutboxEvent> events = outboxEventMapper.selectList(
                new LambdaQueryWrapper<OutboxEvent>()
                        .eq(OutboxEvent::getStatus, "NEW")
                        .le(OutboxEvent::getNextRetryTime, LocalDateTime.now())
                        .lt(OutboxEvent::getRetryCount, 10)
                        .last("LIMIT 50")
        );

        for (OutboxEvent event : events) {
            try {
                kafkaTemplate
                        .send(event.getTopic(), String.valueOf(event.getAggregateId()), event.getPayload())
                        .get();

                event.setStatus("SENT");
                event.setUpdateTime(LocalDateTime.now());
                outboxEventMapper.updateById(event);

            } catch (Exception ex) {
                int retry = event.getRetryCount() == null ? 1 : event.getRetryCount() + 1;

                event.setRetryCount(retry);
                event.setNextRetryTime(LocalDateTime.now().plusSeconds(Math.min(300, retry * 10L)));
                event.setUpdateTime(LocalDateTime.now());

                if (retry >= 10) {
                    event.setStatus("FAILED");
                }

                outboxEventMapper.updateById(event);

                log.error("Publish outbox event failed, id={}, retry={}", event.getId(), retry, ex);
            }
        }
    }
}
