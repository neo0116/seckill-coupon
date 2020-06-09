package com.seckill.coupon.service.impl;

import com.seckill.coupon.product.impl.CouponProducer;
import com.seckill.coupon.service.CouponService;
import org.redisson.api.RBucket;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class CouponServiceImpl implements CouponService, Runnable {

    private static volatile int off = 0;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    CouponProducer couponProducer;

    private ArrayBlockingQueue<Integer> queue;

    RMap<Integer, Integer> checkMap;
    RBucket<Integer> typeBucket;
    RBucket<Integer> offBucket;

    public CouponServiceImpl() {
        //不必写死容量，可用MQ去更改队列初始化容量
        queue = new ArrayBlockingQueue(1000);
    }

    /**
     * 初始化
     * 1、重复点击抢券行为
     * 2、库存编号
     */
    @PostConstruct
    public void init() {
        //重复提交检查
        checkMap = redissonClient.getMap("coupon:userId:");
        checkMap.expire(7L, TimeUnit.DAYS);
        //活动总开关 默认开启
        typeBucket = redissonClient.getBucket("coupon:type:");
        typeBucket.set(0);
        //活动计时开关 默认关闭
        offBucket = redissonClient.getBucket("coupon:off:");
        offBucket.set(0);

        Thread thread = new Thread(this);
        thread.start();

        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
        scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Integer flag = offBucket.get();
                if (flag == 1) {
                    System.out.println("活动开启");
                    off = 1;
                    scheduledThreadPool.shutdown();
                }
            }
        }, 1L, 1L, TimeUnit.SECONDS);
    }

    @Override
    public boolean enq(int userId) {
        if (off == 0) {
            System.out.println("未开始？" );
            return false;
        }
        //频繁点击检查
        RLock checkLock = null;
        try {
            checkLock = redissonClient.getLock("coupon:lock:" + userId);
            boolean lock = checkLock.tryLock(-1L, TimeUnit.SECONDS);
            if (!lock) {
                //不要频繁点击
                return false;
            }
            //是否尝试过入队抢券
            Integer checkValue = checkMap.get(userId);
            if (checkValue != null && checkValue.intValue() == 1) {
                //用户已经尝试过，提示 券已抢完
                return false;
            }
            checkMap.put(userId, 1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (Objects.nonNull(checkLock)) {
                checkLock.unlock();
            }
        }

        //入队
        boolean offer = queue.offer(userId);
        if (!offer) {
            System.out.println(userId + "没入队啊，不让你抢券========");
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        while (typeBucket.get() != 1) {
            try {
                Integer userId = queue.take();
                System.out.println("用户来了" + userId);
                couponProducer.sendMsg(String.valueOf(userId));
            } catch (InterruptedException e) {
            }
        }
    }
}
