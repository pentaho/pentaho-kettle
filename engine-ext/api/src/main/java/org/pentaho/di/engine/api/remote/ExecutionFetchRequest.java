package org.pentaho.di.engine.api.remote;

/**
 * Created by ccaspanello on 8/2/17.
 */
public class ExecutionFetchRequest implements Message {

    public final String requestId;

    public ExecutionFetchRequest(String requestId){
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }
}
