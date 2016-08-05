package com.jk.changehandler.config.dynamodb;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBStreamsClient;
import com.jk.changehandler.change.processor.Dyno;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j2
@Configuration
public class DynamodbConfigurations {
    public final static String DYAMODB_LOCAL_CLIENT = "dynamoDbLocalClient";
    public final static String DYNAMODB_CLIENT = "dynamoDbClient";

    public static String TABLE_NAME;

    @Bean
    public AWSCredentialsProvider getCredentials() {
        return new ProfileCredentialsProvider("users-dynamodb");
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
        String endpoint = "http://dd-local:8000";

        // in k8s, changeHandler, and dd-local go to same pod
        if(System.getenv().containsKey("KUBERNETES_SERVICE_HOST")) {
            endpoint = "http://localhost:8000";
        }
        log.info("[CONFIG] Dynamodb local endpoint {}", endpoint);
        client.setEndpoint(endpoint);
        return client;
    }

}

