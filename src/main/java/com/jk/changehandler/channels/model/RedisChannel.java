package com.jk.changehandler.channels.model;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;

/**
 * Redis implementation of channel interface
 */
@Log4j2
public class RedisChannel implements Channel {

    @Getter
    protected String channelName;

    private Jedis redis;

    @Override
    public Integer publish(String message) {
        Validate.notNull(redis, "Redis client must be specified");
        Long publishCount = redis.publish(channelName, message);
        log.info("Publish count: {}", publishCount);
        return null;
    }

    public RedisChannel withRedisClient(Jedis jedis) {
        this.redis = jedis;
        return this;
    }
}
