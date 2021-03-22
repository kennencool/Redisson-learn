package com.kennen.redis_high_concurrency_test.controller;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author: hejiyuan
 * @Date: 2021/3/22 11:45
 * @Description: 高并发库存减少测试
 */
@RestController
public class IndexController {
    @Resource private Redisson redisson;
    @Resource private RedisTemplate redisTemplate;

    /**
     * 使用 synchronized 关键字实现并发控制
     * 缺点：当有请求访问开始执行时，其他请求被阻塞，接口网络吞吐率很低
     * @return
     */
    @RequestMapping("/deduct_stock")
    public String deductStock(){
        // 使用Jmeter高并发测试，发现打印结果都为49，因为所有请求在并发访问时，并发读取的库存都是50
//        int stock = Integer.parseInt((String)redisTemplate.opsForValue().get("stock"));
//        if(stock > 0){
//            int newStock = stock - 1;
//            redisTemplate.opsForValue().set("stock",newStock+"");
//            System.out.println("扣减成功，剩余库存：" + newStock);
//        }else{
//            System.out.println("扣减失败，库存不足");
//        }
        
        // 通过加 synchronized 关键字进行并发控制，测试结果就是正常递减数据，但是并发数据的吞吐率大大下降
        synchronized (this){
            int stock = Integer.parseInt((String)redisTemplate.opsForValue().get("stock"));
            if(stock > 0){
                int newStock = stock - 1;
                redisTemplate.opsForValue().set("stock",newStock+"");
                System.out.println("扣减成功，剩余库存：" + newStock);
            }else{
                System.out.println("扣减失败，库存不足");
            }
        }
        return "end";
    }

    /**
     * 通过 setnx(setIfAbsent) 设置锁变量来实现并发控制
     * 缺点：当有请求在执行时，其他请求不会被阻塞，而是直接返回操作失败。
     * @return
     */
    @RequestMapping("/deduct_stock1")
    public String deductStock1(){
        // 这里的id是为了防止创建的锁名和其他线程的锁一样，导致被其他线程解开
        String lockKey = "lockKey";
        String id = UUID.randomUUID().toString();
        // 设置超时时间是防止因为服务器挂掉或出异常导致的锁没有被清除
        Boolean lock = redisTemplate.opsForValue().setIfAbsent(lockKey,id,10,TimeUnit.SECONDS);
        if(!lock){
            return "error_other_using";
        }
        int stock = Integer.parseInt((String)redisTemplate.opsForValue().get("stock"));
        if(stock > 0){
            int newStock = stock - 1;
            redisTemplate.opsForValue().set("stock",newStock+"");
            System.out.println("扣减成功，剩余库存：" + newStock);
        }else{
            System.out.println("扣减失败，库存不足");
        }
        if(id.equals(redisTemplate.opsForValue().get(lockKey))){
            redisTemplate.delete(lockKey);
        }
        
        return "end";
    }

    /**
     * 通过 redisson 提供的 RLock 锁来实现并发控制
     * 缺点：
     * @return
     */
    @RequestMapping("/deduct_stock2")
    public String deductStock2(){
        String lockKey = "lockKey";
        RLock redissonLock = redisson.getLock(lockKey);
        // 加锁
        redissonLock.lock();
        int stock = Integer.parseInt((String)redisTemplate.opsForValue().get("stock"));
        if(stock > 0){
            int newStock = stock - 1;
            redisTemplate.opsForValue().set("stock",newStock+"");
            System.out.println("扣减成功，剩余库存：" + newStock);
        }else{
            System.out.println("扣减失败，库存不足");
        }
        // 释放锁
        redissonLock.unlock();
        
        return "end";
    }
}
