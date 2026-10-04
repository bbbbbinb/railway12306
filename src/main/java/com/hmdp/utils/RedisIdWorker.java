package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static com.hmdp.utils.RedisConstants.ICR_KEY;

/**
 * <p>
 *  基于 Redis 自增的全局唯一 id 生成器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Component
public class RedisIdWorker {

    /**
     * 起始时间戳,2022-01-01 00:00:00,单位:秒
     */
    private static final long BEGIN_TIMESTAMP = 1640995200L;

    /**
     * 序列号占用的位数
     */
    private static final int COUNT_BITS = 32;

    /**
     * 序列号 key 中日期的格式,按天重置
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy:MM:dd");

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 生成全局唯一 id
     * @param keyPrefix 业务前缀,不同业务使用不同前缀,例如 order、voucher
     * @return 64 位长整型 id
     */
    public long nextId(String keyPrefix) {
        // 1.生成时间戳:当前时间与起始时间的差值,单位秒,占 31 位
        LocalDateTime now = LocalDateTime.now();
        long timestamp = now.toEpochSecond(ZoneOffset.UTC) - BEGIN_TIMESTAMP;

        // 2.生成序列号:以天为单位自增,占 32 位,key 形如 icr:order:2022:01:01
        String date = now.format(DATE_FORMATTER);
        long count = stringRedisTemplate.opsForValue().increment(ICR_KEY + keyPrefix + ":" + date);

        // 3.拼接:时间戳在高位,序列号在低位
        return timestamp << COUNT_BITS | count;
    }
}
