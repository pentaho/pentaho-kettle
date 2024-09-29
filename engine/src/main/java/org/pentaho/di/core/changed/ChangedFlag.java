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


package org.pentaho.di.core.changed;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;

public class ChangedFlag implements ChangedFlagInterface {
  private Set<PDIObserver> obs = Collections.newSetFromMap( new ConcurrentHashMap<PDIObserver, Boolean>( ) );

  private AtomicBoolean changed = new AtomicBoolean();

  public void addObserver( PDIObserver o ) {
    if ( o == null ) {
      throw new NullPointerException();
    }

    validateAdd( o );
  }

  private synchronized void validateAdd( PDIObserver o ) {
    if ( !obs.contains( o ) ) {
      obs.add( o );
    }
  }

  public void deleteObserver( PDIObserver o ) {
    obs.remove( o );
  }

  public void notifyObservers( Object arg ) {

    PDIObserver[] lobs;
    if ( !changed.get() ) {
      return;
    }
    lobs = obs.toArray( new PDIObserver[obs.size()] );
    clearChanged();
    for ( int i = lobs.length - 1; i >= 0; i-- ) {
      lobs[i].update( this, arg );
    }
  }

  /**
   * Sets this as being changed.
   */
  public void setChanged() {
    changed.set( true );
  }

  /**
   * Sets whether or not this has changed.
   *
   * @param ch
   *          true if you want to mark this as changed, false otherwise
   */
  public void setChanged( boolean b ) {
    changed.set( b );
  }

  /**
   * Clears the changed flags.
   */
  public void clearChanged() {
    changed.set( false );
  }

  /**
   * Checks whether or not this has changed.
   *
   * @return true if the this has changed, false otherwise
   */
  public boolean hasChanged() {
    return changed.get();
  }

}
