/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



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
