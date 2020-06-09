package com.seckill.coupon.controller;

import com.seckill.coupon.service.CouponConfigService;
import com.seckill.coupon.service.CouponService;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
public class CouponController {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    CouponService couponService;

    @Autowired
    CouponConfigService couponConfigService;

    /**
     * 并发抢券
     * @return
     */
    @GetMapping("coupon")
    public boolean get() {
        boolean b = couponService.enq(ThreadLocalRandom.current().nextInt(999999999));
        return b;
    }

    /**
     * 最终看看库存结果
     * @return
     */
    @GetMapping("remain")
    public RMap<Object, Object> remain() {
        RMap<Object, Object> map = redissonClient.getMap("coupon:store:");
        return map;
    }

    /**
     * 初始化库存
     * @param num
     * @param total
     * @return
     */
    @GetMapping("/ctl/{num}/{total}")
    public boolean ctl(
            @PathVariable("num") int num,
            @PathVariable("total") int total) {
        return couponConfigService.doConfig(num, total);
    }

}
