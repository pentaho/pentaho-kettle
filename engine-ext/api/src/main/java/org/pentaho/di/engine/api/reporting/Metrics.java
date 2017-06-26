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

package org.pentaho.di.engine.api.reporting;

import java.io.Serializable;

/**
 * Created by hudak on 1/5/17.
 */
public class Metrics implements Serializable {
  private static final long serialVersionUID = -5354823227842967351L;
  private final long in, out, dropped, inFlight;

  public static Metrics empty() {
    return new Metrics( 0, 0, 0, 0 );
  }

  public Metrics( long in, long out, long dropped, long inFlight ) {
    this.in = in;
    this.out = out;
    this.dropped = dropped;
    this.inFlight = inFlight;
  }

  /**
   * Get number of {@link PDIEvent}s into this component
   *
   * @return
   */
  public long getIn() {
    return in;
  }

  /**
   * Get number of {@link PDIEvent}s out from this component
   *
   * @return
   */
  public long getOut() {
    return out;
  }

  /**
   * Get number of {@link PDIEvent}s dropped (errorred)
   *
   * @return
   */
  public long getDropped() {
    return dropped;
  }

  /**
   * Get number of {@link PDIEvent}s currently in-flight
   *
   * @return
   */
  public long getInFlight() {
    return inFlight;
  }

  @Override public String toString() {
    return String.format( "Metrics{in=%d, out=%d, dropped=%d, inFlight=%d}", in, out, dropped, inFlight );
  }

  public Metrics add( Metrics right ) {
    return new Metrics(
      getIn() + right.getIn(),
      getOut() + right.getOut(),
      getDropped() + right.getDropped(),
      getInFlight() + right.getInFlight()
    );
  }

  @Override public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( !( o instanceof Metrics ) ) {
      return false;
    }

    Metrics metrics = (Metrics) o;

    if ( in != metrics.in ) {
      return false;
    }
    if ( out != metrics.out ) {
      return false;
    }
    if ( dropped != metrics.dropped ) {
      return false;
    }
    return inFlight == metrics.inFlight;
  }

  @Override public int hashCode() {
    int result = (int) ( in ^ ( in >>> 32 ) );
    result = 31 * result + (int) ( out ^ ( out >>> 32 ) );
    result = 31 * result + (int) ( dropped ^ ( dropped >>> 32 ) );
    result = 31 * result + (int) ( inFlight ^ ( inFlight >>> 32 ) );
    return result;
  }
}
