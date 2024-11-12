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
