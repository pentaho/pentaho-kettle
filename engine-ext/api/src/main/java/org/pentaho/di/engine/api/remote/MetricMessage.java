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
