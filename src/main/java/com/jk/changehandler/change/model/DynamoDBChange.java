package com.jk.changehandler.change.model;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.Record;
import lombok.Builder;
import lombok.ToString;

import java.util.Map;

/**
 * Model representing a dynamodb change
 */
@Builder
@ToString
public class DynamoDBChange implements ChangeEvent<Map<String, AttributeValue>> {

    private final Record record;

    @Override
    public Map<String, AttributeValue> getOldItem() {
        return record.getDynamodb().getOldImage();
    }

    @Override
    public Map<String, AttributeValue> getNewItem() {
        return record.getDynamodb().getNewImage();
    }

    @Override
    public ChangeEventType getEventType() {
        return ChangeEventType.valueOf(record.getEventName());
    }
}
