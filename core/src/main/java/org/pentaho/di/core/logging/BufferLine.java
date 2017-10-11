/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
