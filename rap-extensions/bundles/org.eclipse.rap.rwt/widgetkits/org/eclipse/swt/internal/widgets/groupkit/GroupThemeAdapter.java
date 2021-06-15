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
package org.eclipse.swt.internal.widgets.groupkit;

import org.eclipse.rap.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.internal.widgets.controlkit.ControlThemeAdapterImpl;
import org.eclipse.swt.widgets.Group;


public class GroupThemeAdapter extends ControlThemeAdapterImpl {

  public BoxDimensions getFramePadding( Group group ) {
    return getCssBoxDimensions( "Group-Frame", "padding", group ).dimensions;
  }

  public BoxDimensions getFrameMargin( Group group ) {
    return getCssBoxDimensions( "Group-Frame", "margin", group ).dimensions;
  }

  /**
   * Returns the size of the trimming of the given group control not including
   * the control's border size.
   */
  public BoxDimensions getTrimmingSize( Group group ) {
    BoxDimensions margin = getFrameMargin( group );
    BoxDimensions padding = getFramePadding( group );
    BoxDimensions frameWidth = getCssBorder( "Group-Frame", group );
    int top = margin.top + padding.top + frameWidth.top;
    top = Math.max( top, TextSizeUtil.getCharHeight( group.getFont() ) );
    int right = margin.right + padding.right + frameWidth.right;
    int bottom = margin.bottom + padding.bottom + frameWidth.bottom;
    int left = margin.left + padding.left + frameWidth.left;
    return new BoxDimensions( top, right, bottom, left );
  }

  public BoxDimensions getHeaderTrimmingSize( Group group ) {
    BoxDimensions margin = getCssBoxDimensions( "Group-Label", "margin", group ).dimensions;
    BoxDimensions padding = getCssBoxDimensions( "Group-Label", "padding", group ).dimensions;
    int top = margin.top + padding.top;
    int right = margin.right + padding.right;
    int bottom = margin.bottom + padding.bottom;
    int left = margin.left + padding.left;
    return new BoxDimensions( top, right, bottom, left );
  }

}
