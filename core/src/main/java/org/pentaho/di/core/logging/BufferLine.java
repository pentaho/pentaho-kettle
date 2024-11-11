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


package org.pentaho.di.core.logging;

import java.util.concurrent.atomic.AtomicInteger;

public class BufferLine {
  private static AtomicInteger sequence = new AtomicInteger( 0 );

  private int nr;
  private KettleLoggingEvent event;

  public BufferLine( KettleLoggingEvent event ) {
    this.event = event;
    this.nr = sequence.incrementAndGet();
  }

  public int getNr() {
    return nr;
  }

  public KettleLoggingEvent getEvent() {
    return event;
  }

  @Override
  public String toString() {
    return event.toString();
  }
}
