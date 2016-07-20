package com.jk.changehandler.channels.model;

import com.jk.changehandler.change.model.DynamoDBChange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DynamoDbChannelClient {

    private Jedis jedis;

    @Autowired
    DynamoDbChannelClient(Jedis jedis) {
        this.jedis = jedis;
    }

    /**
     * get all the dynamodb channels
     * @return
     */
    public List<DynamoDbChannel> getAllChannels() {
        if(System.getProperty("ENV") != null && System.getProperty("ENV").equals("dev"))
            return  testGetAllChannels();

        List<String> channels = jedis.pubsubChannels("*");

        List<DynamoDbChannel> dynamoDbChannels = channels.stream()
                .map(s -> DynamoDbChannel.builder().channelName(s).build())
                .collect(Collectors.toList());

        return dynamoDbChannels;

    }

    public List<DynamoDbChannel> testGetAllChannels() {
        List<String> channels = new ArrayList<>();
        channels.add("SecondaryIndexTest::usersByCity::['Delhi']");
        channels.add("SecondaryIndexTest::usersByCity::['Mumbai']");

        List<DynamoDbChannel> dynamoDbChannels = channels.stream()
                .map(s -> DynamoDbChannel.builder().channelName(s).build())
                .collect(Collectors.toList());

        return dynamoDbChannels;
    }
}
