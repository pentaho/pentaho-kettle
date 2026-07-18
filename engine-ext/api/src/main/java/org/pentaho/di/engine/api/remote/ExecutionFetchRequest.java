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
 * Created by ccaspanello on 8/2/17.
 */
public class ExecutionFetchRequest implements Message {

  public final String requestId;

  public ExecutionFetchRequest( String requestId ) {
    this.requestId = requestId;
  }

  public String getRequestId() {
    return requestId;
  }
}
