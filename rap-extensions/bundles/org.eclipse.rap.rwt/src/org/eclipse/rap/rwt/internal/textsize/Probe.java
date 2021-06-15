/*******************************************************************************
 * Copyright (c) 2011, 2012 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.textsize;

import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.internal.SerializableCompatibility;

class Probe implements SerializableCompatibility {

  final static String DEFAULT_PROBE_STRING;
  static {
    StringBuilder result = new StringBuilder();
    for( int i = 33; i < 97; i++ ) {
      if( i != 34 && i != 39 ) {
        result.append( ( char ) i );
      }
      // Create sequence "AzBy...YbZa" to workaround bug 374914
      if( Character.isLetter( ( char ) i ) ) {
        result.append( ( char ) ( 187 - i ) );
      }
    }
    DEFAULT_PROBE_STRING = result.toString();
  }

  private final String text;
  private final FontData fontData;

  Probe( FontData fontData ) {
    this( DEFAULT_PROBE_STRING, fontData );
  }

  Probe( String text, FontData fontData ) {
    ParamCheck.notNull( text, "text" );
    ParamCheck.notNull( fontData, "fontData" );
    this.text = text;
    this.fontData = fontData;
  }

  FontData getFontData() {
    return fontData;
  }

  String getText() {
    return text;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + fontData.hashCode();
    result = prime * result + text.hashCode();
    return result;
  }

  @Override
  public boolean equals( Object obj ) {
    boolean result = false;
    if( obj != null && getClass() == obj.getClass() ) {
      if( this == obj ) {
        result = true;
      } else {
        Probe other = ( Probe )obj;
        result =    fontData.equals( other.fontData )
                 && text.equals( other.text );
      }
    }
    return result;
  }
}