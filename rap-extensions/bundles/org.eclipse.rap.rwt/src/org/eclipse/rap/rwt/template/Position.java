/*******************************************************************************
 * Copyright (c) 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.template;

import org.eclipse.rap.json.JsonArray;


class Position {

  final float percentage;
  final int offset;

  Position( float percentage, int offset ) {
    this.percentage = percentage;
    this.offset = offset;
  }

  JsonArray toJson() {
    return new JsonArray().add( percentage ).add( offset );
  }

  @Override
  public String toString() {
    return "Position{ " + percentage + "%, " + offset + "px }";
  }

  @Override
  public boolean equals( Object object ) {
    if( this == object ) {
      return true;
    }
    if( object instanceof Position ) {
      Position other = ( Position )object;
      return other.offset == offset
          && Float.floatToIntBits( other.percentage ) == Float.floatToIntBits( percentage );
    }
    return false;
  }

  @Override
  public int hashCode() {
    return offset ^ Float.floatToIntBits( percentage );
  }

}
