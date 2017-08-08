package org.pentaho.di.engine.api.remote;

/**
 * Spark Reqeust Message
 *
 * The purpose of this message is for the Spark Application to request an ExecutionRequest object based off the unique
 * requestId.
 *
 * Created by ccaspanello on 7/25/17.
 */
public class SparkRequestMessage implements Message {

    private static final long serialVersionUID = 3237149735948763557L;
    public String requestId;

    public SparkRequestMessage(String requestId){
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
