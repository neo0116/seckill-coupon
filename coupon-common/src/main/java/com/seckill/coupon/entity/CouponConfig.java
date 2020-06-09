package com.seckill.coupon.entity;

public class CouponConfig {

    private int num;

    private int total;

    public CouponConfig() {
    }

    public CouponConfig(int num, int total) {
        this.num = num;
        this.total = total;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "CouponConfig{" +
                "num=" + num +
                ", total=" + total +
                '}';
    }
}
