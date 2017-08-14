/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

/**
 * Created by ccaspanello on 7/20/17.
 */
public class MessageEncoder implements Encoder.Text<Message> {

  @Override
  public String encode( Message object ) throws EncodeException {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream( baos );
      oos.writeObject( object );
      oos.close();
      return Base64.getEncoder().encodeToString( baos.toByteArray() );
    } catch ( Exception e ) {
      throw new RuntimeException( "Unexpected error trying to encode object.", e );
    }
  }

  @Override
  public void init( EndpointConfig config ) {
    // Do Nothing
  }

  @Override
  public void destroy() {
    // Do Nothing
  }
}
