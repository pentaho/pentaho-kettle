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
