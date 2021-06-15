/*******************************************************************************
 * Copyright (c) 2008, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme.css;

import java.util.Arrays;

import org.eclipse.rap.rwt.internal.theme.CssValue;


/**
 * A value that can only be applied to a widget that meets certain constraints.
 */
public class ConditionalValue {

  public final CssValue value;
  public final String[] constraints;

  public ConditionalValue( CssValue value, String... constraints ) {
    this.value = value;
    this.constraints = constraints;
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append( "ConditionalValue{ value: " );
    buffer.append( value );
    buffer.append( ", constraints: " );
    for( int i = 0; i < constraints.length; i++ ) {
      if( i > 0 ) {
        buffer.append( ", " );
      }
      buffer.append( constraints[ i ] );
    }
    buffer.append( " }" );
    return buffer.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode( constraints );
    result = prime * result + ( ( value == null ) ? 0 : value.hashCode() );
    return result;
  }

  @Override
  public boolean equals( Object obj ) {
    if( this == obj ) {
      return true;
    }
    if( obj == null ) {
      return false;
    }
    if( getClass() != obj.getClass() ) {
      return false;
    }
    ConditionalValue other = ( ConditionalValue )obj;
    if( !Arrays.equals( constraints, other.constraints ) ) {
      return false;
    }
    if( value == null ) {
      if( other.value != null ) {
        return false;
      }
    } else if( !value.equals( other.value ) ) {
      return false;
    }
    return true;
  }

}
