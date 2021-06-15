/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.swt.internal.widgets;

import org.eclipse.rap.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;


@SuppressWarnings("restriction")
public class LayoutUtil {

  private final static int HORIZONTAL_DIALOG_UNIT_PER_CHAR = 4;
  private final static int BUTTON_WIDTH = 61;

  public static GridLayout createGridLayout( int columns, int margin, int spacing ) {
    GridLayout layout = new GridLayout( columns, false );
    layout.marginWidth = margin;
    layout.marginHeight = margin;
    layout.horizontalSpacing = spacing;
    layout.verticalSpacing = spacing;
    return layout;
  }

  public static GridData createHorizontalFillData() {
    return new GridData( SWT.FILL, GridData.CENTER, true, false );
  }

  public static GridData createFillData() {
    return new GridData( SWT.FILL, SWT.FILL, true, true );
  }

  public static GridData createButtonLayoutData( Control control ) {
    GridData layoutData = new GridData( GridData.HORIZONTAL_ALIGN_FILL );
    int minWidth = getButtonMinWidth( control.getFont() );
    Point preferedSize = control.computeSize( SWT.DEFAULT, SWT.DEFAULT, true );
    layoutData.widthHint = Math.max( minWidth, preferedSize.x );
    return layoutData;
  }

  private static int getButtonMinWidth( Font font ) {
    float charWidth = TextSizeUtil.getAvgCharWidth( font );
    float width = charWidth * BUTTON_WIDTH + HORIZONTAL_DIALOG_UNIT_PER_CHAR / 2;
    return ( int )( width / HORIZONTAL_DIALOG_UNIT_PER_CHAR );
  }

  private LayoutUtil() {
    // prevent instantiation
  }

}
