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

/**
 * Generic MetricMessage to be sent across the wire.
 * <p>
 * TODO Revisit and rework this to utilize PDI events if that is the proper direction to go
 * <p>
 * Created by ccaspanello on 7/13/17.
 */
public class MetricMessage implements Message {

  private static final long serialVersionUID = 2609919768423885048L;
  public String requestId;
  public Type type;
  private String object;

  public MetricMessage( String requestId, Type type, String object ) {
    this.requestId = requestId;
    this.type = type;
    this.object = object;
  }

  public String getRequestId() {
    return requestId;
  }

  public void setRequestId( String requestId ) {
    this.requestId = requestId;
  }

  public Type getType() {
    return type;
  }

  public void setType( Type type ) {
    this.type = type;
  }

  public String getObject() {
    return object;
  }

  public void setObject( String object ) {
    this.object = object;
  }
}
