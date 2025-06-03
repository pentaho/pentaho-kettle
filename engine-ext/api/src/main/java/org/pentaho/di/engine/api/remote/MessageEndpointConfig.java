/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.engine.api.remote;

import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.Decoder;
import jakarta.websocket.Encoder;
import jakarta.websocket.Extension;
import javax.net.ssl.SSLContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  public SSLContext getSSLContext() {
    return null;
  }

  @Override
  public Configurator getConfigurator() {
    return new Configurator();
  }

  @Override
  public List<Class<? extends Encoder>> getEncoders() {
    return Arrays.asList( MessageEncoder.class );
  }

  @Override
  public List<Class<? extends Decoder>> getDecoders() {
    return Arrays.asList( MessageDecoder.class );
  }

  @Override
  public Map<String, Object> getUserProperties() {
    return new HashMap<>();
  }
}
