package com.jk.changehandler.poller;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.streamsadapter.AmazonDynamoDBStreamsAdapterClient;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.jk.changehandler.config.dynamodb.DynamodbConfigurations;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

@Component
public final class ChangeStreamProcessorWorker {
    private final Logger log = LogManager.getLogger(ChangeStreamProcessorWorker.class);

    public static String APPLICATION_NAME;

    @Setter
    private String STREAM_ARN;

    private AmazonDynamoDBStreamsAdapterClient adapterClient;

    @Autowired
    private AWSCredentialsProvider credentialsProvider;

    @Autowired
    @Qualifier(DynamodbConfigurations.DYNAMODB_CLIENT)
    private AmazonDynamoDBClient dynamoDBClient;

    @Autowired
    private ChangeStreamProcessorFactory changeStreamProcessorFactory;


    private void init(String tableName) {
        APPLICATION_NAME = tableName + "ChangeProcessor";

        log.info("Initializing...");
        // Ensure the JVM will refresh the cached IP values of AWS resources (e.g. service endpoints).
        java.security.Security.setProperty("networkaddress.cache.ttl", "60");

        adapterClient = new AmazonDynamoDBStreamsAdapterClient(credentialsProvider);

        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
                    + "Please make sure that your credentials file is at the correct "
                    + "location (~/.aws/credentials), and is in valid format.", e);
        }
    }

    public void start(String tableName) throws UnknownHostException {
        init(tableName);

        String workerId = null;
        try {
            workerId = InetAddress.getLocalHost().getCanonicalHostName() + ":" + UUID.randomUUID();
        } catch (UnknownHostException e) {
            log.error("Unknown host exception while starting worker", e);
            throw e;
        }

        KinesisClientLibConfiguration kinesisClientLibConfiguration =
                new KinesisClientLibConfiguration(APPLICATION_NAME, STREAM_ARN, credentialsProvider, workerId)
                    .withMaxRecords(1000)
                    .withIdleTimeBetweenReadsInMillis(500)
                    .withInitialPositionInStream(InitialPositionInStream.LATEST);

        IRecordProcessorFactory recordProcessorFactory = changeStreamProcessorFactory;


        log.info("Creating worker for stream: " + STREAM_ARN);
        Worker worker = new Worker.Builder()
                .recordProcessorFactory(recordProcessorFactory)
                .config(kinesisClientLibConfiguration)
                .dynamoDBClient(dynamoDBClient)
                .kinesisClient(adapterClient)
                .build();


        log.info("Running {} to process stream {} as worker {}...\n",
                APPLICATION_NAME,
                STREAM_ARN,
                workerId);

        try {
            log.info("Starting worker...");
            worker.run();
        } catch (Throwable t) {
            log.error("Caught throwable while processing data.", t);
            throw t;
        }
    }

}
