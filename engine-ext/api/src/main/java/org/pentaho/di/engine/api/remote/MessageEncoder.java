package org.pentaho.di.engine.api.remote;

import org.pentaho.di.engine.api.remote.Message;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.io.*;
import java.util.Base64;

/**
 * Created by ccaspanello on 7/20/17.
 */
public class MessageEncoder implements Encoder.Text<Message>  {

    @Override
    public String encode(Message object) throws EncodeException {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            oos.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error trying to encode object.", e);
        }
    }

    @Override
    public void init(EndpointConfig config) {
        // Do Nothing
    }

    @Override
    public void destroy() {
        // Do Nothing
    }
}
