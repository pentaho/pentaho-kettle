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
