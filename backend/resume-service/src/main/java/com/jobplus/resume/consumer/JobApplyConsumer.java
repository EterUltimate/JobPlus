package com.jobplus.resume.consumer;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Kafka consumer - handle job apply events (optional)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class JobApplyConsumer {

    /**
     * 消费投递申请消息
     * 消息格式：{type, deliveryId, jobId, jobTitle, userId, hrUserId, applyTime}
     */
    @KafkaListener(topics = "topic_job_apply", groupId = "notification-group")
    public void onJobApply(String message) {
        try {
            Map<String, Object> msg = JSON.parseObject(message, Map.class);
            String type = (String) msg.get("type");
            log.info("收到 Kafka 消息: type={}, deliveryId={}", type, msg.get("deliveryId"));

            if ("JOB_APPLY".equals(type)) {
                // 1️⃣ 通知 HR
                notifyHR(msg);
                // 2️⃣ 推荐相似职位给求职者
                recommendSimilarJobs(msg);
            } else if ("DELIVERY_STATUS_CHANGE".equals(type)) {
                // 3️⃣ 通知求职者投递状态变化
                notifySeeker(msg);
            }
        } catch (Exception e) {
            log.error("Kafka 消息消费失败: {}", message, e);
        }
    }

    /** 通知 HR 有新投递 */
    private void notifyHR(Map<String, Object> msg) {
        Long hrUserId = ((Number) msg.get("hrUserId")).longValue();
        String jobTitle = (String) msg.get("jobTitle");
        String applyTime = (String) msg.get("applyTime");
        log.info("📬 通知 HR(userId={}): 用户 {} 于 {} 投递了职位《{}》",
                hrUserId, msg.get("userId"), applyTime, jobTitle);
        // TODO: 集成邮件服务（JavaMailSender）或站内消息系统
        // emailService.send(hrUserId, "新简历投递", "...");
    }

    /** 基于投递行为更新推荐列表 */
    private void recommendSimilarJobs(Map<String, Object> msg) {
        Long userId = ((Number) msg.get("userId")).longValue();
        Long jobId = ((Number) msg.get("jobId")).longValue();
        log.info("🤖 推荐服务：用户 {} 投递了 jobId={}，正在计算相似职位...", userId, jobId);
        // TODO: 基于内容相似度（岗位描述/Tags）或协同过滤更新推荐
        // recommendService.updateUserPreferences(userId, jobId);
    }

    /** 通知求职者状态变化 */
    private void notifySeeker(Map<String, Object> msg) {
        Long userId = ((Number) msg.get("userId")).longValue();
        Integer status = (Integer) msg.get("status");
        String feedback = (String) msg.get("feedback");
        String statusText = switch (status) {
            case 2 -> "待查看";
            case 3 -> "笔试中";
            case 4 -> "已被录用";
            case 5 -> "未通过";
            default -> "未知";
        };
        log.info("📬 通知求职者(userId={}): 投递状态变为【{}】, 反馈: {}",
                userId, statusText, feedback != null ? feedback : "无");
        // TODO: 集成通知服务
    }
}
