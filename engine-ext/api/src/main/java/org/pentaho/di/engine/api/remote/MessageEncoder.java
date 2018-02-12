/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

/**
 * A WebSocket encoder that converts AEL Message objects into gziped strings that can be transported across the wire
 * and used in WebSockets calls.
 *
 * This class will be used by WebSockets endpoint when sending webSocket messages.
 *
 * Created by ccaspanello on 7/20/17.
 */
public class MessageEncoder implements Encoder.Text<Message> {
  /**
   * Encode the given AEL Message object into a String.
   *
   * @param object the Message object being encoded.
   * @return the encoded object as a string.
   */
  @Override
  public String encode( Message object ) throws EncodeException {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream( baos );
      oos.writeObject( object );
      oos.close();
      return EncodeUtil.encodeBase64Zipped( baos.toByteArray() );
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
