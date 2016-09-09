package com.jk.changehandler.stores;

import com.jk.changehandler.channels.model.DynamoDbChannel;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to serve as temporary query store.
 * Should be replaced with Redis in the real scenaria.
 */
@Log4j2
public class QueryStore {
    private static Map<String, String> queries = new HashMap<>();
    private static final String REQUEST_PROCESSOR_IP = "http://rp:3001";

    private final static String query = "{\n" +
            "\"operation\" : \"Query\",\n" +
            "\"params\": {\n" +
            "    \"TableName\": \"SecondaryIndexTest\",\n" +
            "    \"IndexName\": \"city-index\",\n" +
            "    \"KeyConditionExpression\": \"city = :city\",\n" +
            "    \"ExpressionAttributeValues\": {\n" +
            "        \":city\": {\"S\": \"Delhi\"}\n" +
            "    }\n" +
            "}\n" +
            "};";

    private final static String queryMumbai = "{\n" +
            "\"operation\" : \"Query\",\n" +
            "\"params\": {\n" +
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


    public static String getQueryForChannel(DynamoDbChannel chan) {
        if(System.getProperty("ENV") != null && System.getProperty("ENV").equals("dev"))
            return getQuery(chan);

        String url = REQUEST_PROCESSOR_IP +  "/query/" + chan.getChannelName();

        try {
            HttpResponse<JsonNode> response = Unirest.get(url).asJson();
            int status = response.getStatus();
            log.info("Url: {}, Response Status: {}", url, status);
            JSONObject body =  response.getBody().getObject();
            return body.get("query").toString();
        } catch (UnirestException e) {
            log.error("Error while making request", e);
            return null;
        } catch (JSONException e) {
            log.error("Error parsing json in getQueryForChannel()", e);
            return null;
        }


    }
}
