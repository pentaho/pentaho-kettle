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
package org.eclipse.swt.internal.custom.ctabfolderkit;

import org.eclipse.rap.rwt.internal.theme.CssBoxDimensions;
import org.eclipse.rap.rwt.internal.theme.CssColor;
import org.eclipse.rap.rwt.internal.theme.CssDimension;
import org.eclipse.rap.rwt.internal.theme.CssFont;
import org.eclipse.rap.rwt.internal.theme.CssValue;
import org.eclipse.rap.rwt.internal.theme.SimpleSelector;
import org.eclipse.rap.rwt.internal.theme.ThemeUtil;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.internal.widgets.controlkit.ControlThemeAdapterImpl;
import org.eclipse.swt.widgets.Control;


public class CTabFolderThemeAdapter extends ControlThemeAdapterImpl {

  private static final BoxDimensions ZERO = new BoxDimensions( 0, 0, 0, 0 );

  /*
   * [if] CTabFolder border is not themeable. It overrides getBorderWidth to return 0.
   * Make getBorder to return zero rectangle as well.
   * See bug 445620: ViewPart toolbar overlap border
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=445620
   */
  @Override
  public BoxDimensions getBorder( Control control ) {
    return ZERO;
  }

  public Color getBackground( CTabFolder folder ) {
    return getCssColor( "CTabItem", "background-color", folder );
  }

  public Color getForeground( CTabFolder folder ) {
    return getCssColor( "CTabItem", "color", folder );
  }

  public Color getSelectedBackground() {
    CssValue cssValue = ThemeUtil.getCssValue( "CTabItem",
                                               "background-color",
                                               SimpleSelector.SELECTED );
    return CssColor.createColor( ( CssColor )cssValue );
  }

  public Color getSelectedForeground() {
    CssValue cssValue = ThemeUtil.getCssValue( "CTabItem", "color", SimpleSelector.SELECTED );
    return CssColor.createColor( ( CssColor )cssValue );
  }

  public BoxDimensions getItemPadding( boolean selected ) {
    SimpleSelector selector = selected ? SimpleSelector.SELECTED : SimpleSelector.DEFAULT;
    CssValue cssValue = ThemeUtil.getCssValue( "CTabItem", "padding", selector );
    return ( ( CssBoxDimensions )cssValue ).dimensions;
  }

  public int getItemSpacing( boolean selected ) {
    SimpleSelector selector = selected ? SimpleSelector.SELECTED : SimpleSelector.DEFAULT;
    CssValue cssValue = ThemeUtil.getCssValue( "CTabItem", "spacing", selector );
    return ( ( CssDimension )cssValue ).value;
  }

  public Font getItemFont( boolean selected ) {
    SimpleSelector selector = selected ? SimpleSelector.SELECTED : SimpleSelector.DEFAULT;
    CssValue cssValue = ThemeUtil.getCssValue( "CTabItem", "font", selector );
    return CssFont.createFont( ( CssFont )cssValue );
  }

}
