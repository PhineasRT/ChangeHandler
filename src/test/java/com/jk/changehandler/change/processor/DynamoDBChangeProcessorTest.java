package com.jk.changehandler.change.processor;

import com.jk.changehandler.channels.model.DynamoDbChannel;
import org.junit.Assert;
import org.junit.Test;

public class DynamoDBChangeProcessorTest {

    @Test
    public void basicConstructionTest() {
        DynamoDbChannel chan = DynamoDbChannel.builder().channelName("Users::getByCity::[Delhi]").build();
        Assert.assertTrue("table name matches", chan.getTableName().equals("Users"));
        Assert.assertTrue( "subscription name matches", chan.getSubscriptionName().equals("getByCity"));
        Assert.assertTrue("args size matches", chan.getArgs().size() == 1);
    }

}