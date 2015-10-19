package com.jk.changehandler.channels.model;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;

/**
 * Redis implementation of channel interface
 */
public class RedisChannel implements Channel {
    private static final Logger log = LogManager.getLogger(RedisChannel.class);

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
