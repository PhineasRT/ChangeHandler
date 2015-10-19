package com.jk.changehandler.change.processor;

import com.jk.changehandler.channels.model.DynamoDbChannel;
import org.junit.Test;
import sun.jvm.hotspot.utilities.Assert;

public class DynamoDBChangeProcessorTest {

    @Test
    public void basicConstructionTest() {
        DynamoDbChannel chan = DynamoDbChannel.builder().channelName("Users::getByCity::[Delhi]").build();
        Assert.that(chan.getTableName().equals("Users"), "table name matches");
        Assert.that(chan.getSubscriptionName().equals("getByCity"), "subscription name matches");
        Assert.that(chan.getArgs().size() == 1, "args size matches");
    }

}