package com.jk.changehandler;

import com.jk.changehandler.config.dynamodb.DynamodbConfigurations;
import com.jk.changehandler.poller.ChangeStreamProcessorWorker;
import com.jk.changehandler.poller.KinesisConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;


/**
 * Entry point in the application.
 */
public class App
{
    private static ChangeStreamProcessorWorker worker;

    private static void init() {
        ApplicationContext ctx =
                new AnnotationConfigApplicationContext("com.jk.changehandler");
        worker = ctx.getBean(ChangeStreamProcessorWorker.class);
    }

    public static void main( String[] args ) throws IOException {
        init();
        worker.start();
    }
}

