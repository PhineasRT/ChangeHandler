package com.jk.changehandler.change.model;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * Notification to be sent to clients
 */
@AllArgsConstructor
@ToString
public class Notification {
    @NonNull private ChangeEventType eventType;
    private JSONObject oldItem;
    private JSONObject newItem;

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("eventType", eventType.toString());
        json.put("oldItem", oldItem);
        json.put("newItem", newItem);
        return json;
    }
}
