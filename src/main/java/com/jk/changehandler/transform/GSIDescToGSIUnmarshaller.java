package com.jk.changehandler.transform;

import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndexDescription;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.transform.Unmarshaller;
import org.json.JSONObject;

/**
 * Created by jshridha on 10/11/15.
 */
public class GSIDescToGSIUnmarshaller implements Unmarshaller<GlobalSecondaryIndex, GlobalSecondaryIndexDescription> {

    @Override
    public GlobalSecondaryIndex unmarshall(GlobalSecondaryIndexDescription description) {
        GlobalSecondaryIndex gsi = new GlobalSecondaryIndex();
        gsi.setKeySchema(description.getKeySchema());
        gsi.setIndexName(description.getIndexName());
        gsi.setProjection(description.getProjection());

        ProvisionedThroughput throughput = new ThroughputDescriptionUnmarhsaller().unmarshall(description.getProvisionedThroughput());
        gsi.setProvisionedThroughput(throughput);

        return gsi;
    }
}
