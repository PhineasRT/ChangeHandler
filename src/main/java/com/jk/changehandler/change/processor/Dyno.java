package com.jk.changehandler.change.processor;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.*;
import com.jk.changehandler.config.dynamodb.DynamodbConfigurations;
import com.jk.changehandler.stores.TableInfo;
import lombok.Builder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * High level wrapper over dynamodb
 */
@Builder
public class Dyno {
    private static final Logger log = LogManager.getLogger(Dyno.class);

    private AmazonDynamoDBClient dynamodb;
    private String tableName;

    @Autowired
    TableInfo tableInfo;

    public PutItemResult putItem(Map<String, AttributeValue> item) {
        log.debug("Put item request for item: {} in table {}", item.toString(), tableName);
        return dynamodb.putItem(new PutItemRequest()
                .withTableName(this.tableName)
                .withItem(item));
    }

    public TableDescription getTableDescription() {
        DescribeTableResult describeTableResponse = dynamodb.describeTable(tableName);
        TableDescription table = describeTableResponse.getTable();
        return table;
    }

    public void deleteItem(Map<String, AttributeValue> item) {
        log.debug("Trying to delete item: {} from table {}", item.toString(), tableName);

        // TODO: replace with this.getTableDescription() ?
        TableDescription tableDescription = tableInfo.getTableDescription(tableName);
        List<KeySchemaElement> keySchemaEls = tableDescription.getKeySchema();

        Map<String, AttributeValue> keySchemaMap = new HashMap<>();
        for(KeySchemaElement el: keySchemaEls) {
            String attrName = el.getAttributeName();
            keySchemaMap.put(attrName, item.get(attrName));
        }

        DeleteItemRequest delReq = new DeleteItemRequest();
        delReq.setKey(keySchemaMap);
        delReq.setTableName(tableName);
        dynamodb.deleteItem(delReq);
        log.debug("Deleted item: {} from table {}", item.toString(), tableName);
    }

}

