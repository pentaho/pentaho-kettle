/*******************************************************************************
 * Copyright (c) 2009, 2010 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;


public interface IWidgetGraphicsAdapter {

  Color[] getBackgroundGradientColors();
  int[] getBackgroundGradientPercents();
  boolean isBackgroundGradientVertical();
  void setBackgroundGradient( Color[] gradientColors,
                              int[] percents,
                              boolean vertical );

  int getRoundedBorderWidth();
  Color getRoundedBorderColor();
  Rectangle getRoundedBorderRadius();
  void setRoundedBorder( int width,
                         Color color,
                         int topLeftRadius,
                         int topRightRadius,
                         int bottomRightRadius,
                         int bottomLeftRadius );

}
