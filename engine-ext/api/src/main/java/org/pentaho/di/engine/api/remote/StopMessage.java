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

/**
 * Close Message
 * <p>
 * Since the CloseReason is not serializable collect the necessary parts and reconstruct the object when needed.
 * <p>
 * NOTE: There was an attempt to extend CloseReason and implement Message to make it serializable; but this did not
 * work.
 * <p>
 * TODO Add CloseReason if it is ok to add references to the javax.websocket-api depencency Created by ccaspanello on
 * 7/25/17.
 */
public class StopMessage implements Message {

  private static final long serialVersionUID = 8842623444691045346L;
  //private CloseReason.CloseCode closeCode;
  private String reasonPhrase;

  public StopMessage(
    //CloseReason.CloseCode closeCode,
    String reasonPhrase ) {
    //this.closeCode = closeCode;
    this.reasonPhrase = reasonPhrase;
  }

  public String getReasonPhrase() {
    return reasonPhrase;
  }

  public void setReasonPhrase( String reasonPhrase ) {
    this.reasonPhrase = reasonPhrase;
  }

  //    public CloseReason toCloseReason() {
  //        return new CloseReason(closeCode, reasonPhrase);
  //    }
}
