package org.pentaho.di.engine.api.remote;

/**
 * Close Message
 * <p>
 * Since the CloseReason is not serializable collect the necessary parts and reconstruct the object when needed.
 * <p>
 * NOTE: There was an attempt to extend CloseReason and implement Message to make it serializable; but this did not work.
 * <p>
 * TODO Add CloseReason if it is ok to add references to the javax.websocket-api depencency
 * Created by ccaspanello on 7/25/17.
 */
public class StopMessage implements Message {

    private static final long serialVersionUID = 8842623444691045346L;
    //private CloseReason.CloseCode closeCode;
    private String reasonPhrase;

    public StopMessage(
            //CloseReason.CloseCode closeCode,
            String reasonPhrase) {
        //this.closeCode = closeCode;
        this.reasonPhrase = reasonPhrase;
    }

//    public CloseReason.CloseCode getCloseCode() {
//        return closeCode;
//    }

//    public void setCloseCode(CloseReason.CloseCode closeCode) {
//        this.closeCode = closeCode;
//    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public void setReasonPhrase(String reasonPhrase) {
        this.reasonPhrase = reasonPhrase;
    }

//    public CloseReason toCloseReason() {
//        return new CloseReason(closeCode, reasonPhrase);
//    }
}
