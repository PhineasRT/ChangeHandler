package com.jk.changehandler.transform;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.transform.AttributeValueJsonUnmarshaller;
import com.amazonaws.transform.JsonUnmarshallerContext;
import com.amazonaws.transform.JsonUnmarshallerContextImpl;
import com.amazonaws.transform.Unmarshaller;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.svenson.JSONParser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jshridha on 10/11/15.
 */
public class QueryJsonUnmarshaller implements Unmarshaller<QueryRequest, JSONObject> {

    private final String EXPR_ATTR_VALUES = "ExpressionAttributeValues";
    private static final Logger log = LogManager.getLogger(QueryJsonUnmarshaller.class);

    @Override
    public QueryRequest unmarshall(JSONObject jsonObject) throws Exception {
        QueryRequest query = new QueryRequest();

        List<String> attributeValueKeys = Arrays.asList(
                EXPR_ATTR_VALUES
        );

        JSONObject reducedJSONrequest = new JSONObject();
        for(String key : jsonObject.keySet()) {
            if(! attributeValueKeys.contains(key)) {
                reducedJSONrequest.put(convertFirstCharToLowercase(key), jsonObject.get(key));
            }
        }

        JSONParser parser = new JSONParser();
        parser.addTypeHint(".tableName", String.class);
        parser.addTypeHint(".indexName", String.class);
        parser.addTypeHint(".keyConditionExpression", String.class);
        query = parser.parse(QueryRequest.class, reducedJSONrequest.toString());

        log.debug("jsonObject: {}", jsonObject.toString());
        // parse "ExpressionAttributeValues"
        if(jsonObject.has(EXPR_ATTR_VALUES)) {
            log.debug("has {} ?: YES", EXPR_ATTR_VALUES);
            Map<String, AttributeValue> expressionAttributeValuesMap = new HashMap<>();
            JSONObject expressionAttributeValueField = jsonObject.getJSONObject(EXPR_ATTR_VALUES);
            for (final String key : expressionAttributeValueField.keySet()) {
                JsonParser keyParser = new JsonFactory().createParser(expressionAttributeValueField.get(key).toString());
                JsonUnmarshallerContext ctx = new JsonUnmarshallerContextImpl(keyParser);
                AttributeValue attrValue = AttributeValueJsonUnmarshaller.getInstance().unmarshall(ctx);
                expressionAttributeValuesMap.put(key, attrValue);
            }
            query.setExpressionAttributeValues(expressionAttributeValuesMap);
        }

        return query;
    }

    /**
     * convert first character of string to lowercase
     * @param str string to convert
     * @return converted string
     */
    private String convertFirstCharToLowercase(final String str) {
        if(str.trim().isEmpty()) {
            return str;
        }
        char[] strArray = str.trim().toCharArray();
        strArray[0] = Character.toLowerCase(strArray[0]);
        return new String(strArray);
    }
}
