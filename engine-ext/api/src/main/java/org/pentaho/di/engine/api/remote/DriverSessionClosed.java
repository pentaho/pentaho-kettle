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

/**
 * Message sent by the AEL Driver Session to inform that session is going to close.
 *
 * @author fcamara
 */
public class DriverSessionClosed implements Message {
  public final String userId;
  public final String reason;

  public DriverSessionClosed( String userId, String reason ) {
    this.userId = userId;
    this.reason = reason;
  }

  public String getUserId() {
    return userId;
  }

  public String getReason() {
    return reason;
  }
}
