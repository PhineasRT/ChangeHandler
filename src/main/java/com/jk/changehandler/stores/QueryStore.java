package com.jk.changehandler.stores;

import com.jk.changehandler.channels.model.DynamoDbChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to serve as temporary query store.
 * Should be replaced with Redis in the real scenaria.
 */
public class QueryStore {
    private static Map<String, String> queries = new HashMap<>();

    private final static String query = "{\n" +
            "\"Operation\" : \"Query\",\n" +
            "\"Query\": {\n" +
            "    \"TableName\": \"SecondaryIndexTest\",\n" +
            "    \"IndexName\": \"city-index\",\n" +
            "    \"KeyConditionExpression\": \"city = :city\",\n" +
            "    \"ExpressionAttributeValues\": {\n" +
            "        \":city\": {\"S\": \"Delhi\"}\n" +
            "    }\n" +
            "}\n" +
            "};";

    private final static String queryMumbai = "{\n" +
            "\"Operation\" : \"Query\",\n" +
            "\"Query\": {\n" +
            "    \"TableName\": \"SecondaryIndexTest\",\n" +
            "    \"IndexName\": \"city-index\",\n" +
            "    \"KeyConditionExpression\": \"city = :city\",\n" +
            "    \"ExpressionAttributeValues\": {\n" +
            "        \":city\": {\"S\": \"Mumbai\"}\n" +
            "    }\n" +
            "}\n" +
            "};";

    static {
        queries.put("SecondaryIndexTest::usersByCity::['Delhi']", query);
        queries.put("SecondaryIndexTest::usersByCity::['Mumbai']", queryMumbai);
    }

    public static String getQuery(String queryName, String ...args) {
        String query = queries.get(queryName);
        return String.format(query, args[0]);
    }

    public static String getQuery(DynamoDbChannel chan) {
        String channelName = chan.getChannelName();
        return queries.get(channelName);
    }
}
