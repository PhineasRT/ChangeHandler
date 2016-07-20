package com.jk.changehandler.channels.model;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a channel which represents a dynamodb subscription.
 * Publishing to the channel writes to all the clients subscribed to that channel.
 *
 * The channel is a hash of (tableName, subscriptionName, argsToSubscription)
 */

@Getter
@ToString
public class DynamoDbChannel extends RedisChannel {
    private String tableName;
    private String subscriptionName;
    private List<Object> args = new ArrayList<>();

    private static final String SEPARATOR = "::" ;

    @Builder
    private DynamoDbChannel(String channelName) {
        super();
        this.channelName = channelName;

        String[] parts = channelName.split(SEPARATOR);
        this.tableName = parts[0];
        this.subscriptionName = parts[1];

        JSONArray argsArray = new JSONArray(parts[2]);
        for(int i = 0; i < argsArray.length(); i++) {
            args.add(argsArray.get(i));
        }
    }
}
