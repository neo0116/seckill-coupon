package com.seckill.coupon.product;

public interface Producer {

    void sendMsg(String content);

    void sendConfigMsg(int num, int total);
}
