package com.jk.changehandler.transform;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.transform.AttributeValueJsonUnmarshaller;
import com.amazonaws.transform.JsonUnmarshallerContext;
import com.amazonaws.transform.JsonUnmarshallerContextImpl;
import com.amazonaws.transform.Unmarshaller;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.svenson.JSONParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Request syntax: http://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_GetItem.html
 *
 */
public class GetItemRequestJsonUnmarshaller implements Unmarshaller<GetItemRequest, JSONObject> {
    private static final Logger log = LogManager.getLogger(GetItemRequestJsonUnmarshaller.class);


    /**
     * String representation of json request
     *
     * @param jsonRequest JSONobject representing the getItem request
     * @return instance of GetItemRequest
     */
    public GetItemRequest unmarshall(final JSONObject jsonRequest) throws IOException {
        GetItemRequest req = new GetItemRequest();

        try {
            // parse "Key" attribute
            Map<String, AttributeValue> keyMap = new HashMap<>();
            JSONObject keyJSONObj = jsonRequest.getJSONObject("Key");
            for (final String key : keyJSONObj.keySet()) {
                JsonParser keyParser = new JsonFactory().createParser(keyJSONObj.get(key).toString());
                JsonUnmarshallerContext ctx = new JsonUnmarshallerContextImpl(keyParser);
                AttributeValue attrValue = AttributeValueJsonUnmarshaller.getInstance().unmarshall(ctx);
                keyMap.put(key, attrValue);
            }

            // now that "Key" is parsed, remove it
            jsonRequest.remove("Key");

            // convert first characters of rest of keys to lowercase
            JSONObject reducedJSONrequest = new JSONObject();
            for(final String key: jsonRequest.keySet()) {
                String lcKey = convertFirstCharToLowercase(key);
                reducedJSONrequest.put(lcKey, jsonRequest.get(key));
            }

            JSONParser parser = new JSONParser();
            parser.addTypeHint(".expressionAttributeNames", Map.class);
            parser.addTypeHint(".consistentRead", Boolean.class);
            parser.addTypeHint(".attributesToGet", List.class);
            req = parser.parse(GetItemRequest.class,  reducedJSONrequest.toString());
            req.setKey(keyMap);

            return req;
        } catch (IOException e) {
            log.error("IOException while parsing: {}", jsonRequest.toString(), e);
            throw e;
        } catch (Exception e) {
            log.error("Generic Exception while parsing: {}", jsonRequest.toString(), e);
            return req;
        }
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
