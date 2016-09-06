package com.jk.changehandler.change.processor;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import com.jk.changehandler.change.model.ChangeEventType;
import com.jk.changehandler.change.model.DynamoDBChange;
import com.jk.changehandler.change.model.DynamoDBQuery;
import com.jk.changehandler.change.model.Notification;
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

import java.util.*;
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
        dyno = new Dyno(localDynamoDb, DynamodbConfigurations.TABLE_NAME);

        switch (change.getEventType()) {
        case INSERT:
            handleInsertOrRemove(change, change.getNewItem());
            break;
        case REMOVE:
            handleInsertOrRemove(change, change.getOldItem());
            break;
        case MODIFY:
            handleModify(change);
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
                log.info("Local table does not exist. Created Table: {}", tableName);
                TableExists.setExists(tableName);
            } catch (ResourceInUseException e) {
                log.info("Table already exists");
                TableExists.setExists(tableName);
            }
        }
    }

    private void handleModify(DynamoDBChange change) {
        Map<String, AttributeValue> oldItem = change.getOldItem();
        Map<String, AttributeValue> newItem = change.getNewItem();

        dyno.putItem(oldItem);
        List<DynamoDbChannel> channels = channelClient.getAllChannels();

        List<DynamoDBQuery> queries = channels.stream()
            .map(QueryStore::getQueryForChannel)
            .map(org.json.JSONObject::new)
            .map(DynamoDBQuery::new)
            .collect(Collectors.toList());

        List<Integer> returnCountsOld = queries.stream()
            .map(this::getDocCount)
            .collect(Collectors.toList());

        dyno.deleteItem(oldItem);

        dyno.putItem(newItem);
        List<Integer> returnCountsNew = queries.stream()
            .map(this::getDocCount)
            .collect(Collectors.toList());

        dyno.deleteItem(newItem);



        for(Integer i = 0; i < channels.size(); i++) {
            if(returnCountsOld.get(i) == 0 && returnCountsNew.get(i) == 0)
                continue;;

            DynamoDbChannel chan = channels.get(i);

            // modify change
            change = modifyChange(change, queries.get(i));

            ChangeEventType eventType = getEventType(returnCountsOld.get(i), returnCountsNew.get(i));
            if(eventType == null) continue;
            String notification = getNotificationString(change, eventType);
            log.info("Chan: {}, notification: {}", chan.getChannelName(), notification);

            if(System.getProperty("ENV") == null || !System.getProperty("ENV").equals("dev"))
                chan.withRedisClient(redis).publish(notification.toString());

            log.info("Published notification");
        }

    }

    private DynamoDBChange modifyChange(DynamoDBChange change, DynamoDBQuery query) {
        Map<String, AttributeValue> oldItem = getItemForQuery(change.getOldItem(), query);
        Map<String, AttributeValue> newItem = getItemForQuery(change.getNewItem(), query);

        Record record = new Record();
        StreamRecord dd = new StreamRecord();
        dd.setNewImage(newItem);
        dd.setOldImage(oldItem);
        record.setDynamodb(dd);
        record.setEventName(change.getEventType().name());
        return new DynamoDBChange(record);
    }

    // puts 'item' in db, then runs the 'query' on db
    private Map<String, AttributeValue> getItemForQuery(Map<String, AttributeValue> item, DynamoDBQuery query) {
        if(item == null)
            return null;

        dyno.putItem(item);
        List<Map<String, AttributeValue>> list = getDocs(query);
        if(list.size() > 1) {
            log.error("list count must be 0/1", list);
        }

        Map<String, AttributeValue> doc = null;
        if(list.size() == 1)
            doc = list.get(0);

        dyno.deleteItem(item);
        return  doc;
    }

    private void handleInsertOrRemove(DynamoDBChange change, Map<String, AttributeValue> item) {
        String tableName = DynamodbConfigurations.TABLE_NAME;
        final PutItemResult putResult = dyno.putItem(item);

        Validate.isTrue(
                localDynamoDb.describeTable(new DescribeTableRequest().withTableName(tableName)).getTable().getItemCount() == 1,
                "Count should be 1. Ctx: " + change.toString());

        List<DynamoDbChannel> channels = channelClient.getAllChannels();
        log.info("Channels count: {}", channels.size());

        // get all the queries corresponding to channels
        List<DynamoDBQuery> queries = channels.stream()
                .map(chan -> QueryStore.getQueryForChannel(chan))
                .map(org.json.JSONObject::new)
                .map(DynamoDBQuery::new)
                .collect(Collectors.toList());

        log.info("Queries count: {}", queries.size());

        // execute each query against the local table
        List<Integer> returnCounts = queries.stream()
            .map(this::getDocCount)
            .collect(Collectors.toList());

        // Delete item inserted above
        dyno.deleteItem(item);

        // get channels which satisfy the query
        List<Integer> indices = IntStream.range(0, channels.size())
            .filter(i -> returnCounts.get(i) != 0)
            .mapToObj(Integer::new)
            .collect(Collectors.toList());

        log.info("Satisfied channel count: {}", indices.size());

        for(Integer i: indices) {
            DynamoDBQuery query =  queries.get(i);
            DynamoDbChannel chan = channels.get(i);
            change = modifyChange(change, query);
            String notification = getNotificationString(change, null);

            if(System.getProperty("ENV") == null || !System.getProperty("ENV").equals("dev"))
                chan.withRedisClient(redis).publish(notification.toString());
        }

        Validate.isTrue(
                dyno.getTableDescription().getItemCount() == 0,
                "Item deleted. Count should be 0. Ctx: " + change.toString());
    }

    private String getNotificationString(DynamoDBChange change, ChangeEventType eventType) {
        Map<String, AttributeValue> item = new HashMap<>();
        Notification notif = null;

        try {
            if(change.getEventType() == ChangeEventType.INSERT) {
                item = change.getNewItem();
                notif = new Notification(change.getEventType(), null, new JSONObject(item.toString()));
            } else if(change.getEventType() == ChangeEventType.REMOVE) {
                item = change.getOldItem();
                notif = new Notification(change.getEventType(), new JSONObject(item.toString()),null);
            } else if(change.getEventType() == ChangeEventType.MODIFY){
                // override event type in case of modify
                notif = new Notification(eventType,
                        new JSONObject(change.getOldItem().toString()),
                        new JSONObject(change.getNewItem().toString()));
            }

            if(notif == null) return null;

            String jsonNotification = null;
            jsonNotification = notif.toJSON().toString();
            return jsonNotification;
        } catch (JSONException e) {
            log.error("error converting notification to json", e);
            return null;
        }


    }

    private List<Map<String, AttributeValue>> getDocs(DynamoDBQuery dynamoDBQuery) {
        List<Map<String, AttributeValue>> list = new ArrayList<>();
        try {
            list = dynamoDBQuery.withDynamodbClient(localDynamoDb).execute();
            list.removeAll(Collections.singleton(null));
        } catch (Exception e) {
            log.error("Error executing the query", e);
        }
        return list;
    }

    private Integer getDocCount(DynamoDBQuery dynamoDBQuery) {
        return getDocs(dynamoDBQuery).size();
    }

    private ChangeEventType getEventType(Integer oldValue, Integer newValue) {
        ChangeEventType eventType = null;

        if(newValue == 1 && oldValue == 1)
            eventType = ChangeEventType.MODIFY;
        else if (newValue == 0 && oldValue == 1)
            eventType = ChangeEventType.REMOVE;
        else if (newValue == 1 && oldValue == 0)
            eventType = ChangeEventType.INSERT;

        return eventType;
    }
}
