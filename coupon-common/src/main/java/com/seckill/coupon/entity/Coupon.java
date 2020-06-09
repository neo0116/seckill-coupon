package com.seckill.coupon.entity;


public class Coupon {

    private String userId;

    public Coupon() {
    }

    public Coupon(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Coupon{" +
                "userId='" + userId + '\'' +
                '}';
    }
}
