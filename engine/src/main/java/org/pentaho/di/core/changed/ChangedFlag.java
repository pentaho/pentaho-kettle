/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
