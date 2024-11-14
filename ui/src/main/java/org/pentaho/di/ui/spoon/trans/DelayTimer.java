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


package org.pentaho.di.ui.spoon.trans;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * A timer where you can attach a timer to. Once the time is up, the listeners are fired off.
 *
 * @author matt
 *
 */
public class DelayTimer implements Runnable {

  private int delayInMiliseconds;
  private boolean stopped;

  private List<DelayListener> delayListeners;
  /**
   * Default prolonger should not prolong  delay.
   */
  private Callable<Boolean> prolonger = new Callable<Boolean>() {
    @Override
    public Boolean call() throws Exception {
      return false;
    }
  };

  private long start;

  public DelayTimer( int delayInMiliseconds ) {
    this.delayInMiliseconds = delayInMiliseconds;
    this.delayListeners = new ArrayList<DelayListener>();

    stopped = false;
  }

  public DelayTimer( int delayInMilliseconds, DelayListener delayListener ) {
    this( delayInMilliseconds );
    addDelayListener( delayListener );
  }

  public DelayTimer( int delayInMilliseconds, DelayListener delayListener, Callable<Boolean> prolonger ) {
    this( delayInMilliseconds, delayListener );
    this.prolonger = prolonger;
  }

  public void reset() {
    start = System.currentTimeMillis();
  }

  public void run() {
    reset();
    while ( ( delayNotExpired() || needProlong() ) && !stopped ) {
      try {
        Thread.sleep( 25 );
      } catch ( InterruptedException e ) {
        // Simply break out of the loop, nothing else
        //
        break;
      }
    }
    // Fire the listeners...
    //
    for ( DelayListener delayListener : delayListeners ) {
      delayListener.expired();
    }
  }

  private boolean delayNotExpired() {
    return ( System.currentTimeMillis() - start ) < delayInMiliseconds;
  }

  private boolean needProlong() {
    try {
      return Boolean.valueOf( prolonger.call() );
    } catch ( Exception e ) {
      throw new RuntimeException( "Prolonger call finished with error", e );
    }
  }

  public void stop() {
    stopped = true;
  }

  public void addDelayListener( DelayListener delayListener ) {
    delayListeners.add( delayListener );
  }

  /**
   * @return the delay in milliseconds
   */
  public int getDelayInMilliseconds() {
    return delayInMiliseconds;
  }

  /**
   * @param delayInMilliseconds
   *          the delay in milliseconds to set
   */
  public void setDelayInSeconds( int delayInMilliseconds ) {
    this.delayInMiliseconds = delayInMilliseconds;
  }

}
