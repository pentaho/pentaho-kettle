/*******************************************************************************
 * Copyright (c) 2010, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme;


public class CssFloat implements CssValue {

  private static final CssFloat ZERO = new CssFloat( 0 );
  private static final CssFloat ONE = new CssFloat( 1 );

  public final float value;

  private CssFloat( float value ) {
    this.value = value;
  }

  public static CssFloat create( float value ) {
    if( value == 0 ) {
      return ZERO;
    }
    if( value == 1 ) {
      return ONE;
    }
    return new CssFloat( value );
  }

  public static CssFloat valueOf( String input ) {
    if( input == null ) {
      throw new NullPointerException( "input" );
    }
    return create( Float.parseFloat( input ) );
  }

  @Override
  public String toDefaultString() {
    return String.valueOf( value );
  }

  @Override
  public boolean equals( Object object ) {
    if( object == this ) {
      return true;
    }
    if( object instanceof CssFloat ) {
      CssFloat other = ( CssFloat )object;
      return Float.floatToIntBits( value ) == Float.floatToIntBits( other.value );
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Float.floatToIntBits( value );
  }

  @Override
  public String toString() {
    return "CssFloat{ " + String.valueOf( value ) + " }";
  }

}
