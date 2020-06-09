package com.seckill.coupon.product.impl;

import com.alibaba.fastjson.JSON;
import com.seckill.coupon.entity.Coupon;
import com.seckill.coupon.entity.CouponConfig;
import com.seckill.coupon.product.Producer;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class CouponProducer implements Producer {


    @Autowired
    RocketMQTemplate rocketMQTemplate;

    @Override
    public void sendMsg(String content) {
        String topic = "seckill-coupon";
        String tags = "";
        String destination = StringUtils.isNotBlank(tags) ? topic + ":" + tags : topic;
        rocketMQTemplate.send(destination,
                MessageBuilder.withPayload(JSON.toJSONString(new Coupon(content))).build());
    }


    @Override
    public void sendConfigMsg(int num, int total) {
        String topic = "seckill-coupon-config";
        String tags = "";
        String destination = StringUtils.isNotBlank(tags) ? topic + ":" + tags : topic;
        rocketMQTemplate.send(destination,
                MessageBuilder.withPayload(JSON.toJSONString(new CouponConfig(num, total))).build());
    }
}
