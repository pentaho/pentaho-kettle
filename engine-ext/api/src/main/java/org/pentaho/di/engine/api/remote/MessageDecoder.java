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

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * A WebSocket decoder that converts gziped strings transported across the wire in WebSockets calls into AEL
 * Message objects.
 *
 * This class will be used by WebSockets endpoint when recieving webSocket messages.
 *
 * Created by ccaspanello on 7/20/17.
 */
public class MessageDecoder implements Decoder.Text<Message> {
  /**
   * Decode the given String into an AEL Message object.
   *
   * @param string string to be decoded.
   * @return the decoded message as an Message object.
   */
  @Override
  public Message decode( String string ) throws DecodeException {
    try {
      byte[] data = EncodeUtil.decodeBase64Zipped( string );
      InputStream is = new ByteArrayInputStream( data );
      ObjectInputStream ois = new ObjectInputStream( is );
      Object o = ois.readObject();
      ois.close();
      return (Message) o;
    } catch ( Exception e ) {
      throw new RuntimeException( "Unexpected error trying to decode object.", e );
    }
  }

  @Override
  public boolean willDecode( String s ) {
    return true;
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
