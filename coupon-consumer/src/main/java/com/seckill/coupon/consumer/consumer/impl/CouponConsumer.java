package com.seckill.coupon.consumer.consumer.impl;

import com.seckill.coupon.entity.Coupon;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RocketMQMessageListener(topic = "seckill-coupon", consumerGroup = "my-consumer_seckill-coupon")
public class CouponConsumer implements RocketMQListener<Coupon> {

    @Autowired
    RedissonClient redissonClient;

    RMap<Integer, Integer> storeMap;

    @PostConstruct
    public void init() {
        //优惠券库存
        storeMap = redissonClient.getMap("coupon:store:");
    }

    @Override
    public void onMessage(Coupon message) {
        for (;;){
            boolean flag = false;
            int size = storeMap.size();
            if (size == 0) {
                //记录此用户 抢券失败
                try {
                    System.out.println("没券了");
                    Thread.sleep(200L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
            //随机拿个库存出来
            List<Integer> keys = storeMap.entrySet().stream()
                    .map(Map.Entry::getKey).collect(Collectors.toList());
            //并发下 还是可能为空
            if (CollectionUtils.isEmpty(keys)) {
                //记录此用户 抢券失败
                try {
                    System.out.println("没券了");
                    Thread.sleep(200L);
                } catch (InterruptedException e) {
                }
                break;
            }
            //随机抽取仓库号加锁
            Integer num = keys.get(ThreadLocalRandom.current().nextInt(keys.size()));
            RLock lock = redissonClient.getLock("coupon:num:" + num);
            try {
                boolean tryLock = lock.tryLock(-1L, TimeUnit.SECONDS);
                //未拿到锁换个仓库号再去抢
                if (!tryLock) {
                    System.out.println("未拿到锁换个仓库号再去抢");
                    continue;
                } else {
                    flag = true;
                    if (!storeMap.containsKey(num)) {
                        System.out.println("当前库存已经被移除了，再去抢");
                        continue;
                    }
                    Integer total = storeMap.get(num);
                    if (total.intValue() == 0) {
                        System.out.println("当前已经没有库存了，再去抢");
                        continue;
                    }
                    //修改数据库 库存
                    Thread.sleep(100L);
                    //记录用户抢券的结果
                    System.out.println(message.toString());
                    Thread.sleep(100L);
                    //修改缓存 库存
                    total -= 1;
                    if (total == 0) {
                        storeMap.remove(num);
                    } else {
                        storeMap.put(num, total);
                    }
                    System.out.println("用户抢券成功");
                }
            } catch (InterruptedException e) {
            } finally {
                if (lock != null && flag) {
                    lock.unlock();
                }
            }
            break;
        }
    }
}
