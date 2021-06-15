/*******************************************************************************
 * Copyright (c) 2007, 2017 Innoopract Informationssysteme GmbH and others.
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CssDimension implements CssValue {

  public static final CssDimension ZERO = new CssDimension( 0 );
  public static final CssDimension AUTO = new CssDimension( Integer.MIN_VALUE );

  private static final Pattern LENGTH_PATTERN
    = Pattern.compile( "((\\+|-)?\\d+)(em|ex|px|pt|pc|in|cm|mm|%)?" );

  public final int value;

  private CssDimension( int value ) {
    this.value = value;
  }

  public static CssDimension create( int value ) {
    return value == 0 ? ZERO : new CssDimension( value );
  }

  public static CssDimension valueOf( String input ) {
    if( input == null ) {
      throw new NullPointerException( "null argument" );
    }
    if( "auto".equals( input ) ) {
      return AUTO;
    }
    Integer parsed = parseLength( input );
    if( parsed == null ) {
      throw new IllegalArgumentException( "Illegal dimension parameter: " + input );
    }
    return create( parsed.intValue() );
  }

  @Override
  public String toDefaultString() {
    return value == Integer.MIN_VALUE ? "auto" : value + "px";
  }

  @Override
  public boolean equals( Object object ) {
    if( object == this ) {
      return true;
    }
    if( object instanceof CssDimension ) {
      CssDimension other = ( CssDimension )object;
      return ( other.value == this.value );
    }
    return false;
  }

  @Override
  public int hashCode () {
    return value * 47;
  }

  @Override
  public String toString() {
    return "CssDimension{ " + value + " }";
  }

  /**
   * Tries to interpret a string as length parameter.
   *
   * @return the parsed length as integer, or <code>null</code> if the string
   *         could not be parsed.
   * @throws IllegalArgumentException if the string is valid CSS length
   *             parameter that is a percentage value or has an unsupported
   *             unit.
   */
  static Integer parseLength( String input ) {
    // TODO [rst] Also catch values with fractional digits
    Integer result = null;
    Matcher matcher = LENGTH_PATTERN.matcher( input );
    if( matcher.matches() ) {
      result = Integer.valueOf( matcher.group( 1 ) );
      String unit = matcher.group( 3 );
      if( unit != null && "%".equals( unit ) ) {
        throw new IllegalArgumentException( "Percentages not supported: " + input );
      }
      if( unit != null && !"px".equals( unit ) ) {
        throw new IllegalArgumentException( "Unit not supported: " + input );
      }
    }
    return result;
  }

}
