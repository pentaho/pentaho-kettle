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

package org.pentaho.di.core.lifecycle;

import java.util.HashSet;
import java.util.Set;

public class LifeEventInfo {
  public enum Hint {
    DISPLAY_MSG_BOX, DISPLAY_BROWSER;
  }

  public enum State {
    SUCCESS, FAIL, HALTED;
  }

  private String message;

  private String name;

  private Set<Hint> hints = new HashSet<Hint>();

  private State state;

  public void setHint( Hint hint ) {
    hints.add( hint );
  }

  public String getMessage() {
    return message;
  }

  public void setMessage( String message ) {
    this.message = message;
  }

  public boolean hasHint( Hint h ) {
    for ( Hint hint : hints ) {
      if ( hint == h ) {
        return true;
      }
    }

    return false;
  }

  public State getState() {
    return state;
  }

  public void setState( State state ) {
    this.state = state;
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }
}
