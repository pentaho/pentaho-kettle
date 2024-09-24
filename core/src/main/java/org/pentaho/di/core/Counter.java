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

package org.pentaho.di.core;

/**
 * Is used to keep the state of sequences / counters throughout a single session of a Transformation, but across Steps.
 *
 * @author Matt
 * @since 13-05-2003
 *
 */
public class Counter {
  private long counter;
  private long start;
  private long increment;
  private long maximum;
  private boolean loop;

  public Counter() {
    start = 1L;
    increment = 1L;
    maximum = 0L;
    loop = false;
    counter = start;
  }

  public Counter( long start ) {
    this();
    this.start = start;
    counter = start;
  }

  public Counter( long start, long increment ) {
    this( start );
    this.increment = increment;
  }

  public Counter( long start, long increment, long maximum ) {
    this( start, increment );
    this.loop = true;
    this.maximum = maximum;
  }

  /**
   * @return Returns the counter.
   */
  public long getCounter() {
    return counter;
  }

  /**
   * @return Returns the increment.
   */
  public long getIncrement() {
    return increment;
  }

  /**
   * @return Returns the maximum.
   */
  public long getMaximum() {
    return maximum;
  }

  /**
   * @return Returns the start.
   */
  public long getStart() {
    return start;
  }

  /**
   * @return Returns the loop.
   */
  public boolean isLoop() {
    return loop;
  }

  /**
   * @param counter
   *          The counter to set.
   */
  public void setCounter( long counter ) {
    this.counter = counter;
  }

  /**
   * @param increment
   *          The increment to set.
   */
  public void setIncrement( long increment ) {
    this.increment = increment;
  }

  /**
   * @param loop
   *          The loop to set.
   */
  public void setLoop( boolean loop ) {
    this.loop = loop;
  }

  /**
   * @param maximum
   *          The maximum to set.
   */
  public void setMaximum( long maximum ) {
    this.maximum = maximum;
  }

  public long next() {
    long retval = counter;

    counter += increment;
    if ( loop && counter > maximum ) {
      counter = start;
    }

    return retval;
  }
}
