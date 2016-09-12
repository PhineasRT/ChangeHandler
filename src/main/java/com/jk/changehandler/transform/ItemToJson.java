package com.jk.changehandler.transform;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.transform.AttributeValueJsonMarshaller;
import com.amazonaws.util.json.JSONObject;
import com.amazonaws.util.json.JSONWriter;

import java.io.StringWriter;
import java.util.Map;

/**
 * Created by jstar on 13/09/16.
 */
public class ItemToJson {
    public static JSONObject convert(Map<String, AttributeValue> item) throws Exception {
        JSONObject json = new JSONObject();
        for(String key: item.keySet()) {
            StringWriter stringWriter = new StringWriter();
            JSONWriter jsonWriter = new JSONWriter(stringWriter);
            AttributeValueJsonMarshaller.getInstance().marshall(item.get(key), jsonWriter);
            String jsonString = stringWriter.toString();
            json.put(key, new JSONObject(jsonString));
        }

        return json;
    }
}
