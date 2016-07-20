package com.jk.changehandler;

import com.jk.changehandler.config.dynamodb.DynamodbConfigurations;
import com.jk.changehandler.poller.ChangeStreamProcessor;
import com.jk.changehandler.poller.ChangeStreamProcessorWorker;
import com.jk.changehandler.poller.KinesisConfig;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;


/**
 * Entry point in the application.
 */
@Log4j2
public class App
{
    private static ChangeStreamProcessorWorker worker;

    private static void init(final String streamArn, final String tableName) {
        ApplicationContext ctx =
                new AnnotationConfigApplicationContext("com.jk.changehandler");

        worker = ctx.getBean(ChangeStreamProcessorWorker.class);
        worker.setSTREAM_ARN(streamArn); // HACK, pass via constructor
    }

    public static void main( String[] args ) throws IOException, ParseException {
        log.info("[START]");
        Options options = new Options();
        options.addOption(OptionBuilder.hasArg().withLongOpt("table").create("t"));
        options.addOption(OptionBuilder.hasArg().withLongOpt("streamArn").create("s"));
        options.addOption(OptionBuilder.withLongOpt("dev").create());

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args);



        if(!cmd.hasOption("t")) {
            log.error("No table name specified in args");
            return ;
        }
        if(!cmd.hasOption("s")) {
            log.error("No stream ARN specified in args");
            return ;
        }

        String TABLE = cmd.getOptionValue("t");
        String STREAM_ARN = cmd.getOptionValue("s");

        if(cmd.hasOption("dev")) {
            log.info("running in dev mode");
            System.setProperty("ENV", "dev");
            TABLE = "SecondaryIndexTest";
            STREAM_ARN = "arn:aws:dynamodb:us-east-1:467623578459:table/SecondaryIndexTest/stream/2015-08-26T20:12:03.858";
        }

        log.info("Table: {},Stream Arn: {}", TABLE, STREAM_ARN);

        // set application wide table name
        DynamodbConfigurations.TABLE_NAME = TABLE;

        init(STREAM_ARN, TABLE);
        worker.start();
    }
}

