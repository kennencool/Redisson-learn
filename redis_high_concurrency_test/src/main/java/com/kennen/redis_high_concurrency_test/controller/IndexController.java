package com.kennen.redis_high_concurrency_test.controller;

import org.redisson.Redisson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Author: hejiyuan
 * @Date: 2021/3/22 11:45
 * @Description: 高并发库存减少测试
 */
@RestController
public class IndexController {
    @Resource private RedisTemplate redisTemplate;
    
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
}
