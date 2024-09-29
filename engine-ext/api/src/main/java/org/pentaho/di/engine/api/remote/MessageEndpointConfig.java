/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */

package org.pentaho.di.engine.api.remote;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.Decoder;
import javax.websocket.Encoder;
import javax.websocket.Extension;
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
