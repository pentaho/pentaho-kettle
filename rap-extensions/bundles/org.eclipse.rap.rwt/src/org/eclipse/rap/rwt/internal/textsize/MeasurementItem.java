/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.textsize;

import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.internal.SerializableCompatibility;


class MeasurementItem implements SerializableCompatibility {
  private final int wrapWidth;
  private final FontData fontData;
  private final String string;
  private final int mode;

  MeasurementItem( String textToMeasure, FontData fontData, int wrapWidth, int mode ) {
    ParamCheck.notNull( textToMeasure, "textToMeasure" );
    ParamCheck.notNull( fontData, "fontData" );
    this.wrapWidth = wrapWidth;
    this.fontData = fontData;
    this.string = textToMeasure;
    this.mode = mode;
  }

  FontData getFontData() {
    return fontData;
  }

  String getTextToMeasure() {
    return string;
  }

  int getWrapWidth() {
    return wrapWidth;
  }

  int getMode() {
    return mode;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + fontData.hashCode();
    result = prime * result + string.hashCode();
    result = prime * result + wrapWidth;
    result = prime * result + mode;
    return result;
  }

  @Override
  public boolean equals( Object object ) {
    boolean result = false;
    if( object != null && getClass() == object.getClass() ) {
      if( this == object ) {
        result = true;
      } else {
        MeasurementItem other = ( MeasurementItem )object;
        result =    fontData.equals( other.fontData )
                 && string.equals( other.string )
                 && wrapWidth == other.wrapWidth
                 && mode == other.mode;
      }
    }
    return result;
  }

}
