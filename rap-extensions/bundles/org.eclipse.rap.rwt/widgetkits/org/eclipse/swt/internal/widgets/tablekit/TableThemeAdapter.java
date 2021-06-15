/*******************************************************************************
 * Copyright (c) 2007, 2015 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package org.eclipse.swt.internal.widgets.tablekit;

import org.eclipse.rap.rwt.internal.theme.Size;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.internal.widgets.controlkit.ControlThemeAdapterImpl;
import org.eclipse.swt.widgets.Control;


public class TableThemeAdapter extends ControlThemeAdapterImpl {

  public BoxDimensions getCheckBoxMargin( Control control ) {
    return getCssBoxDimensions( "Table-Checkbox", "margin", control ).dimensions;
  }

  public int getCheckBoxWidth( Control control ) {
    return getCssDimension( "Table-Checkbox", "width", control );
  }

  public Size getCheckBoxImageSize( Control control ) {
    return getCssImageSize( "Table-Checkbox", "background-image", control );
  }

  public BoxDimensions getCellPadding( Control control ) {
    return getCssBoxDimensions( "Table-Cell", "padding", control ).dimensions;
  }

  public int getCellSpacing( Control control ) {
    return Math.max( 0, getCssDimension( "Table-Cell", "spacing", control ) );
  }

  public int getHeaderBorderBottomWidth( Control control ) {
    return getCssBorderWidth( "TableColumn", "border-bottom", control );
  }

  public BoxDimensions getHeaderPadding( Control control ) {
    return getCssBoxDimensions( "TableColumn", "padding", control ).dimensions;
  }

  public Font getHeaderFont( Control control ) {
    return getCssFont( "TableColumn", "font", control );
  }

}
