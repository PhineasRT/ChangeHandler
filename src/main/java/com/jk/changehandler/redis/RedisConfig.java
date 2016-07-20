package com.jk.changehandler.redis;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

@Log4j2
@Configuration
public class RedisConfig {

    @Bean
    public Jedis getJedisClient() {
        final String HOST = "redis";
        log.info("[CONFIGURATION] [redis]. Host: {}", HOST);
        Jedis jedis = new Jedis(HOST);
        return jedis;
    }
}
