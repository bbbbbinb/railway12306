package com.hmdp;

import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class RedisIdWorkerTest {

    @Resource
    private RedisIdWorker redisIdWorker;

    /**
     * 300 个线程各生成 100 个 id,验证并发下不会重复
     */
    @Test
    void testNextIdConcurrent() throws InterruptedException {
        int threadCount = 300;
        int perThread = 100;
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        long begin = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < perThread; j++) {
                        ids.add(redisIdWorker.nextId("order"));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        long cost = System.currentTimeMillis() - begin;
        executor.shutdown();

        System.out.println("生成 " + ids.size() + " 个 id,耗时 " + cost + " ms");
        assertEquals(threadCount * perThread, ids.size(), "并发下出现了重复的 id");
    }

    /**
     * 同一个业务前缀下,后生成的 id 必须大于先生成的 id
     */
    @Test
    void testNextIdIncrease() {
        long first = redisIdWorker.nextId("order");
        long second = redisIdWorker.nextId("order");
        System.out.println("first = " + first + " (十六进制 " + Long.toHexString(first) + ")");
        System.out.println("second = " + second + " (十六进制 " + Long.toHexString(second) + ")");
        System.out.println("时间戳部分 = " + (first >>> 32) + ",序列号部分 = " + (first & 0xFFFFFFFFL));
        assertTrue(second > first, "同一业务前缀下的 id 应该递增");
    }
}
