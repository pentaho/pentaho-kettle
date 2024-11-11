/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


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
