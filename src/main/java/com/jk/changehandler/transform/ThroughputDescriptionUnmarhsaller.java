package com.jk.changehandler.transform;

import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndexDescription;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputDescription;
import com.amazonaws.transform.Unmarshaller;

/**
 * Created by jshridha on 10/11/15.
 */
public class ThroughputDescriptionUnmarhsaller implements Unmarshaller<ProvisionedThroughput, ProvisionedThroughputDescription> {

    @Override
    public ProvisionedThroughput unmarshall(ProvisionedThroughputDescription description) {
        ProvisionedThroughput throughput = new ProvisionedThroughput();
        throughput.setReadCapacityUnits(description.getReadCapacityUnits());
        throughput.setWriteCapacityUnits(description.getWriteCapacityUnits());
        return throughput;
    }
}
