package com.hmdp.mq;

import com.hmdp.entity.VoucherOrder;
import com.hmdp.service.IVoucherOrderService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.hmdp.config.RabbitMQConfig.SECKILL_ORDER_QUEUE;

@Slf4j
@Component
public class VoucherOrderConsumer {

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private IVoucherOrderService voucherOrderService;

    /**
     * 扣减库存1：获取锁
     * @param voucherOrder
     */
    @RabbitListener(queues = SECKILL_ORDER_QUEUE)
    public void handleVoucherOrder(VoucherOrder voucherOrder){
        //1.获取用户
        Long userId = voucherOrder.getUserId();
        // 2.创建锁对象
        RLock redisLock = redissonClient.getLock("lock:order:" + userId);
        // 3.尝试获取锁
        boolean isLock = redisLock.tryLock();
        // 4.判断是否获得锁成功
        if (!isLock) {
            // 获取锁失败，直接返回失败或者重试
            log.error("不允许重复下单！");
            return;
        }
        try {
            voucherOrderService.createVoucherOrder(voucherOrder);
        } finally {
            // 释放锁
            redisLock.unlock();
        }
    }
}
