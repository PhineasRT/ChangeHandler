package com.jk.changehandler.config.dynamodb;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBStreamsClient;
import com.jk.changehandler.change.processor.Dyno;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DynamodbConfigurations {
    public final static String DYAMODB_LOCAL_CLIENT = "dynamoDbLocalClient";
    public final static String DYNAMODB_CLIENT = "dynamoDbClient";

    public final static String TABLE_NAME = "SecondaryIndexTest";
    public final static String STREAM_ARN = "arn:aws:dynamodb:us-east-1:467623578459:table/SecondaryIndexTest/stream/2015-08-26T20:12:03.858";


    @Bean
    public AWSCredentialsProvider getCredentials() {
        return new ProfileCredentialsProvider("shridhardotjatin");
    }

    @Bean(name = DYNAMODB_CLIENT)
    public AmazonDynamoDBClient getDynamoDbClient() {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient(getCredentials());
        return client;
    }

    @Bean
    public AmazonDynamoDBStreamsClient getDynamoDbStreamsClient() {
        AmazonDynamoDBStreamsClient client = new AmazonDynamoDBStreamsClient(getCredentials());
        return client;
    }

    @Bean(name = DYAMODB_LOCAL_CLIENT)
    public AmazonDynamoDBClient getDynamoDbLocalClient() {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient(getCredentials());
        client.setEndpoint("http://localhost:8000");
        return client;
    }

    @Bean
    public Dyno getDyno() {
        return Dyno.builder().dynamodb(getDynamoDbLocalClient()).tableName(DynamodbConfigurations.TABLE_NAME).build();
    }
}

