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
package org.eclipse.rap.rwt.internal.theme;

import static org.eclipse.rap.rwt.internal.service.ContextProvider.getApplicationContext;

import org.eclipse.rap.rwt.theme.BoxDimensions;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Widget;


/**
 * Base class for theme adapters.
 */
public abstract class ThemeAdapter {

  private final WidgetMatcher matcher;

  public ThemeAdapter() {
    matcher = new WidgetMatcher();
    configureMatcher( matcher );
  }

  /**
   * Returns the name of the main CSS element for a given widget.
   */
  public static String getPrimaryElement( Widget widget ) {
    Class<?> widgetClass = widget.getClass();
    ThemeableWidget thWidget = findThemeableWidget( widget );
    if( thWidget == null || thWidget.elements == null ) {
      throw new RuntimeException( "No themeable widget found for " + widgetClass.getName() );
    }
    return thWidget.elements[ 0 ].getName();
  }

  /**
   * Configures the widget matcher to be able to match widgets. Subclasses need
   * to implement.
   */
  protected abstract void configureMatcher( WidgetMatcher matcher );

  ////////////////////
  // Delegator methods

  protected Color getCssColor( String cssElement, String cssProperty, Widget widget ) {
    CssValue cssValue = ThemeUtil.getCssValue( cssElement, cssProperty, matcher, widget );
    return CssColor.createColor( ( CssColor )cssValue );
  }

  protected Font getCssFont( String cssElement, String cssProperty, Widget widget ) {
    CssValue cssValue = ThemeUtil.getCssValue( cssElement, cssProperty, matcher, widget );
    return CssFont.createFont( ( CssFont )cssValue );
  }

  protected BoxDimensions getCssBorder( String cssElement, Widget widget ) {
    int top = getCssBorderWidth( cssElement, "border-top", widget );
    int right = getCssBorderWidth( cssElement, "border-right", widget );
    int bottom = getCssBorderWidth( cssElement, "border-bottom", widget );
    int left = getCssBorderWidth( cssElement, "border-left", widget );
    return new BoxDimensions( top, right, bottom, left );
  }

  protected int getCssBorderWidth( String cssElement, String cssProperty, Widget widget ) {
    CssValue cssValue = ThemeUtil.getCssValue( cssElement, cssProperty, matcher, widget );
    return ( ( CssBorder )cssValue ).width;
  }

  protected int getCssDimension( String cssElement, String cssProperty, Widget widget ) {
    CssValue cssValue = ThemeUtil.getCssValue( cssElement, cssProperty, matcher, widget );
    return ( ( CssDimension )cssValue ).value;
  }

  protected CssBoxDimensions getCssBoxDimensions( String cssElement,
                                                  String cssProperty,
                                                  Widget widget )
  {
    return ( CssBoxDimensions )ThemeUtil.getCssValue( cssElement, cssProperty, matcher, widget );
  }

  protected Size getCssImageSize( String cssElement, String cssProperty, Widget widget ) {
    CssImage image = ( CssImage ) ThemeUtil.getCssValue( cssElement, cssProperty, matcher, widget );
    return image.getSize();
  }

  @SuppressWarnings( "unchecked" )
  private static ThemeableWidget findThemeableWidget( Widget widget ) {
    ThemeableWidget result;
    Class<?> widgetClass = widget.getClass();
    ThemeManager manager = getApplicationContext().getThemeManager();
    result = manager.getThemeableWidget( ( Class<? extends Widget> )widgetClass );
    while( ( result == null || result.elements == null ) && widgetClass.getSuperclass() != null ) {
      widgetClass = widgetClass.getSuperclass();
      result = manager.getThemeableWidget( ( Class<? extends Widget> )widgetClass );
    }
    return result;
  }

}
