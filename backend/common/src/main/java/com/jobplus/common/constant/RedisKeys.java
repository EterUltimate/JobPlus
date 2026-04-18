package com.jobplus.common.constant;

/** Redis Key 前缀常量 */
public final class RedisKeys {
    private RedisKeys() {}

    public static String token(Long userId) { return "token:user:" + userId; }
    public static String jobViewCount(Long jobId) { return "job:views:" + jobId; }
    public static String userSession(Long userId) { return "session:user:" + userId; }

    /** Kafka Topic */
    public static final String TOPIC_JOB_APPLY = "topic_job_apply";
}
