package com.jk.changehandler;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.jk.changehandler.transform.GetItemRequestJsonUnmarshaller;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;


public class GetItemRequestJsonUnmarshallerTest {

    @Test
    public void testUnmarshalling() throws Exception {
        String json = "{ \"Key\": {    \"username\": {     \"S\": \"jatins\"   }, }, \"TableName\": \"Ext\",  \"AttributesToGet\": [    \"fullName\",   \"age\",   \"dob\" ]}";
        JSONObject obj = new JSONObject(json);
        System.out.println(obj.toString());

        GetItemRequestJsonUnmarshaller unmarshaller = new GetItemRequestJsonUnmarshaller();
        GetItemRequest req = unmarshaller.unmarshall(obj);

        Assert.assertTrue("should have username key", req.getKey().containsKey("username"));
        Assert.assertTrue("type should be AttributeValue", req.getKey().get("username") instanceof AttributeValue);
    }
}