/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.engine.model;


import java.io.Serializable;
import java.security.Principal;

/**
 * Created by jchilton on 3/15/2017.
 */
public class ActingPrincipal implements Principal, Serializable {
  private static final long serialVersionUID = -8326458100440326949L;
  private final boolean anonymous;
  private final String name;

  public static final ActingPrincipal ANONYMOUS = new ActingPrincipal();

  public ActingPrincipal( String name ) {
    this.name = name;
    anonymous = false;
  }

  private ActingPrincipal() {
    anonymous = true;
    name = null;
  }

  @Override
  public String getName() {
    return name;
  }

  public String toString() {
    return this.name;
  }

  @Override public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }

    ActingPrincipal that = (ActingPrincipal) o;

    if ( isAnonymous() != that.isAnonymous() ) {
      return false;
    }
    return getName() != null ? getName().equals( that.getName() ) : that.getName() == null;
  }

  @Override public int hashCode() {
    int result = ( isAnonymous() ? 1 : 0 );
    result = 31 * result + ( getName() != null ? getName().hashCode() : 0 );
    return result;
  }

  public boolean isAnonymous() {
    return anonymous;
  }
}
