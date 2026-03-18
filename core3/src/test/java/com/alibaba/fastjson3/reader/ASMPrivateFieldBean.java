package com.alibaba.fastjson3.reader;

/**
 * Test bean with private fields (like JJB User/Client classes).
 */
public class ASMPrivateFieldBean {
    private String id;
    private int age;
    private double balance;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
}
