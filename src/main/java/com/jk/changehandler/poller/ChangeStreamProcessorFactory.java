package com.jk.changehandler.poller;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Used to create new record processors.
 */
@Component
public class ChangeStreamProcessorFactory implements IRecordProcessorFactory {

    @Autowired
    private ChangeStreamProcessor changeStreamProcessor;

    /**
     * {@inheritDoc}
     */
    @Override
    public IRecordProcessor createProcessor() {
        return changeStreamProcessor;
    }
}
