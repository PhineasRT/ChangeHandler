package com.jk.changehandler.poller;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KinesisConfig {

    @Bean
    @Autowired
    public AmazonKinesisClient getKinesisClient(AWSCredentialsProvider credentials) {
        AmazonKinesisClient client = new AmazonKinesisClient(credentials);
        return client;
    }
}
