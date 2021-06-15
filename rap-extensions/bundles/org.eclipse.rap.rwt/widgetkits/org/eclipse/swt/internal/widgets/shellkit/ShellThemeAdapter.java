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
package org.eclipse.swt.internal.widgets.shellkit;

import org.eclipse.rap.rwt.internal.textsize.TextSizeUtil;
import org.eclipse.rap.rwt.internal.theme.CssBoxDimensions;
import org.eclipse.rap.rwt.internal.theme.CssValue;
import org.eclipse.rap.rwt.internal.theme.SimpleSelector;
import org.eclipse.rap.rwt.internal.theme.ThemeUtil;
import org.eclipse.rap.rwt.internal.theme.WidgetMatcher;
import org.eclipse.rap.rwt.internal.theme.WidgetMatcher.Constraint;
import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.internal.widgets.controlkit.ControlThemeAdapterImpl;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;


public class ShellThemeAdapter extends ControlThemeAdapterImpl {

  private static final int MENU_BAR_MIN_HEIGHT = 20;

  @Override
  protected void configureMatcher( WidgetMatcher matcher ) {
    super.configureMatcher( matcher );
    matcher.addStyle( "TITLE", SWT.TITLE );
    matcher.addStyle( "APPLICATION_MODAL", SWT.APPLICATION_MODAL );
    matcher.addStyle( "TOOL", SWT.TOOL );
    matcher.addStyle( "SHEET", SWT.SHEET );
    matcher.addState( "maximized", new Constraint() {
      @Override
      public boolean matches( Widget widget ) {
        return ( ( Shell )widget ).getMaximized();
      }
    } );
  }

  public BoxDimensions getTitleBarMargin( Shell shell ) {
    if( ( shell.getStyle() & SWT.TITLE ) != 0 ) {
      return getCssBoxDimensions( "Shell-Titlebar", "margin", shell ).dimensions;
    }
    return CssBoxDimensions.ZERO.dimensions;
  }

  public int getTitleBarHeight( Shell shell ) {
    int result = 0;
    if( ( shell.getStyle() & SWT.TITLE ) != 0 ) {
      result = getCssDimension( "Shell-Titlebar", "height", shell );
    }
    return result;
  }

  public int getMenuBarHeight( Shell shell ) {
    int result = 0;
    if( shell.getMenuBar() != null ) {
      Font font = getCssFont( "Shell", "font", shell );
      int fontHeight = TextSizeUtil.getCharHeight( font );
      BoxDimensions padding = getMenuBarItemPadding();
      result = Math.max( MENU_BAR_MIN_HEIGHT, fontHeight + padding.top + padding.bottom );
    }
    return result;
  }

  private static BoxDimensions getMenuBarItemPadding() {
    SimpleSelector selector = new SimpleSelector( new String[] { ":onMenuBar" } );
    CssValue cssValue = ThemeUtil.getCssValue( "MenuItem", "padding", selector );
    return ( ( CssBoxDimensions )cssValue ).dimensions;
  }

}
