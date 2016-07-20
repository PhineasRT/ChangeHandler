package com.jk.changehandler.change.model;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.*;
import com.jk.changehandler.config.dynamodb.DynamodbConfigurations;
import com.jk.changehandler.transform.GetItemRequestJsonUnmarshaller;
import com.jk.changehandler.transform.QueryJsonUnmarshaller;
import lombok.NonNull;
import org.apache.commons.lang.Validate;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Model to encapsulate dynamodb query/get item request
 */
public class DynamoDBQuery implements IQuery<Map<String, AttributeValue>> {
    private DynamoDBOperationType operationType;
    private JSONObject query;

    private AmazonDynamoDBClient localDynamoDb;

    public DynamoDBQuery(JSONObject q) {
        if(q.getString("operation").equalsIgnoreCase("Query")) {
            this.operationType = DynamoDBOperationType.QUERY;
        } else if(q.getString("operation").equalsIgnoreCase("GetItemRequest")) {
            this.operationType = DynamoDBOperationType.GET_ITEM_REQUEST;
        }

        this.query = q.getJSONObject("params"); // json query to be executed
    }

    public DynamoDBQuery withDynamodbClient(AmazonDynamoDBClient client) {
        this.localDynamoDb = client;
        return this;
    }

    @Override
    public List<Map<String, AttributeValue>> execute() throws Exception {
        Validate.notNull(localDynamoDb, "dynamodb client can't be null");

        List<Map<String, AttributeValue>> result = new ArrayList<>();

        switch (this.operationType) {
        case GET_ITEM_REQUEST:
            GetItemRequest getReq = new GetItemRequestJsonUnmarshaller().unmarshall(this.query);
            GetItemResult getResult = localDynamoDb.getItem(getReq);
            result.add(getResult.getItem());
            break;
        case QUERY:
            QueryRequest queryReq = new QueryJsonUnmarshaller().unmarshall(this.query);
            QueryResult queryResponse = localDynamoDb.query(queryReq);
            result = queryResponse.getItems();
            break;
        }

        return result;
    }
}
