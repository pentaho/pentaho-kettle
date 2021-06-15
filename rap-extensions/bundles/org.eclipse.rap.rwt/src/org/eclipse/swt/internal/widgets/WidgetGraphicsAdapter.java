/*******************************************************************************
 * Copyright (c) 2009, 2011 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.SerializableCompatibility;

public final class WidgetGraphicsAdapter 
  implements IWidgetGraphicsAdapter, SerializableCompatibility 
{
  
  private int roundedBorderWidth;
  private Color roundedBorderColor;
  private Rectangle roundedBorderRadius;
  private Color[] backgroundGradientColors;
  private int[] backgroundGradientPercents;
  private boolean backgroundGradientVertical;

  public Color[] getBackgroundGradientColors() {
    Color[] result = null;
    if( backgroundGradientColors != null ) {
      result = backgroundGradientColors.clone();
    }
    return result;
  }

  public int[] getBackgroundGradientPercents() {
    int[] result = null;
    if( backgroundGradientPercents != null ) {
      result = backgroundGradientPercents.clone();
    }
    return result;
  }

  public boolean isBackgroundGradientVertical() {
    return backgroundGradientVertical;
  }

  public void setBackgroundGradient( Color[] gradientColors, int[] percents, boolean vertical ) {
    if( gradientColors != null && percents != null ) {
      if( gradientColors.length != percents.length ) {
        SWT.error( SWT.ERROR_INVALID_ARGUMENT );
      }
      for( int i = 0; i < gradientColors.length; i++ ) {
        if( gradientColors[ i ] == null ) {
          SWT.error( SWT.ERROR_INVALID_ARGUMENT );
        }
      }
    }
    backgroundGradientColors = null;
    if( gradientColors != null ) {
      backgroundGradientColors = gradientColors.clone();
    }
    backgroundGradientPercents = null;
    if( percents != null ) {
      backgroundGradientPercents = percents.clone();
    }
    backgroundGradientVertical = vertical;
  }

  public int getRoundedBorderWidth() {
    return roundedBorderWidth;
  }

  public Color getRoundedBorderColor() {
    return roundedBorderColor;
  }

  public Rectangle getRoundedBorderRadius() {
    Rectangle result;
    if( roundedBorderRadius != null ) {
      result = new Rectangle( roundedBorderRadius.x,
                              roundedBorderRadius.y,
                              roundedBorderRadius.width,
                              roundedBorderRadius.height );
    } else {
      result = new Rectangle( 0, 0, 0, 0 );
    }
    return result;
  }

  public void setRoundedBorder( int width,
                                Color color,
                                int topLeftRadius,
                                int topRightRadius,
                                int bottomRightRadius,
                                int bottomLeftRadius ) {
    roundedBorderWidth = width;
    roundedBorderColor = color;
    roundedBorderRadius = new Rectangle( topLeftRadius,
                                         topRightRadius,
                                         bottomRightRadius,
                                         bottomLeftRadius );
  }
}
