package org.pentaho.di.engine.api.remote;

import org.pentaho.di.engine.api.remote.Message;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.io.*;
import java.util.Base64;

/**
 * Created by ccaspanello on 7/20/17.
 */
public class MessageDecoder implements Decoder.Text<Message> {

    @Override
    public Message decode(String string) throws DecodeException {
        try {
            byte [] data = Base64.getDecoder().decode( string );
            InputStream is = new ByteArrayInputStream(  data );
            ObjectInputStream ois = new ObjectInputStream(is);
            Object o = ois.readObject();
            ois.close();
            return (Message) o;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error trying to decode object.", e);
        }
    }

    @Override
    public boolean willDecode(String s) {
        return true;
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
