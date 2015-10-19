package com.jk.changehandler.stores;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.*;
import com.jk.changehandler.config.dynamodb.DynamodbConfigurations;
import com.jk.changehandler.transform.GSIDescToGSIUnmarshaller;
import com.jk.changehandler.transform.ThroughputDescriptionUnmarhsaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class that provides information related to a table.
 */
@Component
public class TableInfo {

    @Autowired
    @Qualifier(DynamodbConfigurations.DYNAMODB_CLIENT)
    AmazonDynamoDBClient dynamoDBClient;

    private Map<String, TableDescription> tableDescriptionMap = new HashMap<>();

    /**
     * Given a tableName, gets its description by querying dynamdb
     * and from that description creates a CreateTableRequest and returns it.
     *
     * So, a new table with same parameters can be create in memory with this request.
     *
     * @param tableName
     * @return
     */
    public CreateTableRequest getInfo(String tableName) {
        DescribeTableResult describeTableResponse = dynamoDBClient.describeTable(tableName);
        TableDescription table = describeTableResponse.getTable();

        List<GlobalSecondaryIndex> gsis = table.getGlobalSecondaryIndexes().stream()
                .map(desc -> new GSIDescToGSIUnmarshaller().unmarshall(desc))
                .collect(Collectors.toList());

        CreateTableRequest createReq = new CreateTableRequest();
        createReq.setAttributeDefinitions(table.getAttributeDefinitions());
        createReq.setGlobalSecondaryIndexes(gsis);
        createReq.setKeySchema(table.getKeySchema());
        createReq.setTableName(table.getTableName());
        createReq.setProvisionedThroughput(new ThroughputDescriptionUnmarhsaller().unmarshall(table.getProvisionedThroughput()));

        // TODO: set lsi

        return createReq;
    }

    /**
     * Gets table description by querying dynamodb.
     * Wrapper over dynamodb's describe table api.
     *
     * @param tableName name of the table
     * @return
     */
    public TableDescription getTableDescription(String tableName) {
        //TODO: remove caching?
        if(tableDescriptionMap.containsKey(tableName))
            return tableDescriptionMap.get(tableName);

        DescribeTableResult describeTableResponse = dynamoDBClient.describeTable(tableName);
        TableDescription table = describeTableResponse.getTable();
        tableDescriptionMap.put(tableName, table);
        return table;
    }
}
