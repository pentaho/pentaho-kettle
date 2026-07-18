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


package org.pentaho.di.repository.pur;

public class ActiveCacheResult<Value> {
  private final Exception exception;

  private final Value value;

  private long timeLoaded;

  public ActiveCacheResult( Value value, Exception exception ) {
    this.value = value;
    this.exception = exception;
    this.timeLoaded = System.currentTimeMillis();
  }

  public Exception getException() {
    return exception;
  }

  public Value getValue() {
    return value;
  }

  public long getTimeLoaded() {
    return timeLoaded;
  }
}
