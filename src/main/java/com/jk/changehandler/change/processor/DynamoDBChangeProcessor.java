package com.jk.changehandler.change.processor;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.*;
import com.jk.changehandler.change.model.ChangeEventType;
import com.jk.changehandler.change.model.DynamoDBChange;
import com.jk.changehandler.change.model.DynamoDBQuery;
import com.jk.changehandler.channels.model.DynamoDbChannel;
import com.jk.changehandler.channels.model.DynamoDbChannelClient;
import com.jk.changehandler.config.dynamodb.DynamodbConfigurations;
import com.jk.changehandler.stores.QueryStore;
import com.jk.changehandler.stores.TableExists;
import com.jk.changehandler.stores.TableInfo;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This is the class which processes a `change` event in the dynamoDB.
 *
 * {@inheritDoc}
 */
@Component
public class DynamoDBChangeProcessor implements IChangeProcessor<DynamoDBChange> {
    private static final Logger log = LogManager.getLogger(DynamoDBChangeProcessor.class);

    @Autowired
    @Qualifier(DynamodbConfigurations.DYAMODB_LOCAL_CLIENT)
    private AmazonDynamoDBClient localDynamoDb;

    @Autowired
    private TableInfo tableInfo;

    @Autowired
    private DynamoDbChannelClient channelClient;

    @Autowired
    private Dyno dyno;

    @Autowired
    private Jedis redis;

    /**
     * the function which gets the `change` and based on
     * whether it's INSERT/REMOVE/MODIFY takes appropriate action.
     *
     * @param change
     */
    @Override
    public void process(DynamoDBChange change) {
        createTableInNotPresent(DynamodbConfigurations.TABLE_NAME);

        switch (change.getEventType()) {
        case INSERT:
            handleInsertOrRemove(change);
            break;
        case REMOVE:
            handleInsertOrRemove(change);
            break;
        case MODIFY:
            // TODO
            break;
        default:
            log.fatal("Unknown event type. Ctx: {}", change.toString());
        }
    }

    private void createTableInNotPresent(String tableName) {
        if(! TableExists.exists(tableName)) {
            CreateTableRequest req = tableInfo.getInfo(tableName);
            try {
                final CreateTableResult table = localDynamoDb.createTable(req);
                log.info("Created Table: {}", tableName);
                TableExists.setExists(tableName);
            } catch (ResourceInUseException e) {
                log.error(e.getMessage());
                TableExists.setExists(tableName);
            }
        }
    }

    private void handleInsertOrRemove(DynamoDBChange change) {
        String tableName = DynamodbConfigurations.TABLE_NAME;

        Map<String, AttributeValue> item = new HashMap<>();

        if(change.getEventType() == ChangeEventType.INSERT) {
            item = change.getNewItem();
        } else if(change.getEventType() == ChangeEventType.REMOVE) {
            item = change.getOldItem();
        }

        final PutItemResult putResult = dyno.putItem(item);

        Validate.isTrue(
                localDynamoDb.describeTable(new DescribeTableRequest().withTableName(tableName)).getTable().getItemCount() == 1,
                "Count should be 1. Ctx: " + change.toString());

        List<DynamoDbChannel> channels = channelClient.getAllChannels();
        log.info("Channels: {}", channels);

        // get all the queries corresponding to channels
        List<String> queries = channels.stream()
                .map(chan -> QueryStore.getQuery(chan))
                .collect(Collectors.toList());

        log.info("Queries: {}", queries);

        // execute each query against the table
        List<Integer> returnCounts = queries.stream()
                .map(query -> new DynamoDBQuery(query))
                .map(dynamoDBQuery -> {
                    Integer size = 0;
                    try {
                        size = dynamoDBQuery.withDynamodbClient(localDynamoDb).execute().size();
                    } catch (Exception e) {
                        log.error("Error executing the query", e);
                    }
                    return size;
                })
                .collect(Collectors.toList());

        // get channels which satisfy the query
        List<DynamoDbChannel> satisfiedChannels = IntStream.range(0, channels.size())
                .filter(i -> returnCounts.get(i) != 0)
                .mapToObj(i -> channels.get(i))
                .collect(Collectors.toList());

        log.info("Channels satisfied by change {}: {}", change.toString(), satisfiedChannels);

        // Delete item inserted above
        dyno.deleteItem(item);

        // publish to channels
//        satisfiedChannels.stream()
//                .forEach(chan -> chan.withRedisClient(redis).publish(change.toString()));

        Validate.isTrue(
                dyno.getTableDescription().getItemCount() == 0,
                "Item deleted. Count should be 0. Ctx: " + change.toString());
    }

}
