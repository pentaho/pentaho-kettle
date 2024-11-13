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

package org.pentaho.di.engine.model;

import com.google.common.base.Objects;
import org.pentaho.di.engine.api.model.Operation;

/**
 * Created by hudak on 1/18/17.
 */
public class Hop implements org.pentaho.di.engine.api.model.Hop {
  private static final long serialVersionUID = -1896736624164197955L;
  private final Operation from;
  private final Operation to;
  private final String type;

  public Hop( Operation from, Operation to, String type ) {
    this.from = from;
    this.to = to;
    this.type = type;
  }

  @Override public String getType() {
    return type;
  }

  @Override public Operation getFrom() {
    return from;
  }

  @Override public Operation getTo() {
    return to;
  }

  @Override public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }
    Hop hop = (Hop) o;
    return Objects.equal( from, hop.from ) && Objects.equal( to, hop.to ) && Objects.equal( type, hop.type );
  }

  @Override public int hashCode() {
    return Objects.hashCode( from, to, type );
  }

  @Override public String toString() {
    return "Hop{" + getId() + ", type='" + type + '\'' + '}';
  }
}
