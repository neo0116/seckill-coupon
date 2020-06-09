package com.seckill.coupon.consumer.consumer.impl;

import com.seckill.coupon.entity.CouponConfig;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

@DependsOn({"couponConsumer"})
@Service
@RocketMQMessageListener(topic = "seckill-coupon-config", consumerGroup = "my-consumer_seckill-coupon-config")
public class ConfigConsumer implements RocketMQListener<CouponConfig> {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    CouponConsumer couponConsumer;

    @Override
    public void onMessage(CouponConfig message) {
        int num = message.getNum();
        int total = message.getTotal();

        RMap<Integer, Integer> storeMap = redissonClient.getMap("coupon:store:");
        //后台设置编号
        for (int i = 1; i <= num; i++) {
            storeMap.put(i, total);
        }
    }
}
