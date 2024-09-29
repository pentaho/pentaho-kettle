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
