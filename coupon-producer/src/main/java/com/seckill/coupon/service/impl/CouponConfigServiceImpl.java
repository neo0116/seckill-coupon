package com.seckill.coupon.service.impl;


import com.seckill.coupon.product.impl.CouponProducer;
import com.seckill.coupon.service.CouponConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CouponConfigServiceImpl implements CouponConfigService {

    @Autowired
    CouponProducer couponProducer;


    @Override
    public boolean doConfig(int num, int total) {
        couponProducer.sendConfigMsg(num, total);
        return true;
    }
}
