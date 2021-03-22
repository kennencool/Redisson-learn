package com.kennen.redis_high_concurrency_test;

import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class RedisHighConcurrencyTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedisHighConcurrencyTestApplication.class, args);
	}

	@Bean
	public Redisson redisson(){
		// 单机模式
		Config config = new Config();
		config.useSingleServer().setAddress("redis://localhost:6380").setDatabase(4);
		return (Redisson)Redisson.create(config);
	}
}
