package org.pentaho.di.engine.api.remote;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Extension;
import java.util.*;

/**
 * Created by ccaspanello on 8/3/17.
 */
public class MessageEndpointConfig implements ClientEndpointConfig {

    @Override
    public List<String> getPreferredSubprotocols() {
        return new ArrayList<>();
    }

    @Override
    public List<Extension> getExtensions() {
        return new ArrayList<>();
    }

    @Override
    public Configurator getConfigurator() {
        return new Configurator();
    }

    @Override
    public List<Class<? extends Encoder>> getEncoders() {
        return Arrays.asList(MessageEncoder.class);
    }

    @Override
    public List<Class<? extends Decoder>> getDecoders() {
        return Arrays.asList(MessageDecoder.class);
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return new HashMap<>();
    }
}
