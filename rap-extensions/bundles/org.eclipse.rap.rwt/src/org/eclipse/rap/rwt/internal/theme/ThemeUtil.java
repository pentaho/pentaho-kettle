/*******************************************************************************
 * Copyright (c) 2007, 2016 Innoopract Informationssysteme GmbH and others.
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

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.WebClient;
import org.eclipse.rap.rwt.internal.application.ApplicationContextImpl;
import org.eclipse.rap.rwt.internal.lifecycle.EntryPointManager;
import org.eclipse.rap.rwt.internal.lifecycle.EntryPointRegistration;
import org.eclipse.rap.rwt.internal.service.ContextProvider;
import org.eclipse.rap.rwt.internal.theme.css.ConditionalValue;
import org.eclipse.rap.rwt.internal.theme.css.CssFileReader;
import org.eclipse.rap.rwt.internal.theme.css.StyleSheet;
import org.eclipse.rap.rwt.service.ResourceLoader;
import org.eclipse.rap.rwt.service.UISession;
import org.eclipse.swt.widgets.Widget;


/**
 * Used to switch between themes at runtime.
 */
public final class ThemeUtil {

  private static final String DEFAULT_THEME_CSS = "resource/theme/default.css";

  public static final String CURR_THEME_ATTR = "org.eclipse.rap.theme.current";

  /**
   * Returns the ids of all themes that are currently registered.
   *
   * @return an array of the theme ids, never <code>null</code>
   */
  public static String[] getAvailableThemeIds() {
    return getApplicationContext().getThemeManager().getRegisteredThemeIds();
  }

  /**
   * Returns the id of the currently active theme.
   *
   * @return the id of the current theme, never <code>null</code>
   */
  public static String getCurrentThemeId() {
    UISession uiSession = ContextProvider.getUISession();
    return ( String )uiSession.getAttribute( CURR_THEME_ATTR );
  }

  /**
   * Sets the current theme to the theme identified by the given id.
   * @param uiSession TODO
   * @param themeId the id of the theme to activate
   *
   * @throws IllegalArgumentException if no theme with the given id is
   *             registered
   */
  public static void setCurrentThemeId( UISession uiSession, String themeId ) {
    uiSession.setAttribute( CURR_THEME_ATTR, themeId );
  }

  public static String getThemeIdFor( String servletPath ) {
    Map<String, String> properties = getEntryPointProperties( servletPath );
    String themeId = properties.get( WebClient.THEME_ID );
    if( themeId != null && themeId.length() > 0 ) {
      verifyThemeId( themeId );
      return themeId;
    }
    return RWT.DEFAULT_THEME_ID;
  }

  public static Theme getCurrentTheme() {
    return getApplicationContext().getThemeManager().getTheme( getCurrentThemeId() );
  }

  private static Theme getFallbackTheme() {
    ThemeManager themeManager = getApplicationContext().getThemeManager();
    return themeManager.getTheme( ThemeManager.FALLBACK_THEME_ID );
  }

  static StyleSheet readDefaultThemeStyleSheet() {
    StyleSheet result;
    try {
      ResourceLoader resLoader = ThemeManager.STANDARD_RESOURCE_LOADER;
      result = CssFileReader.readStyleSheet( DEFAULT_THEME_CSS, resLoader );
    } catch( IOException ioe ) {
      String msg = "Failed to load default theme: " + DEFAULT_THEME_CSS;
      throw new ThemeManagerException( msg, ioe );
    }
    return result;
  }

  private static void verifyThemeId( String themeId ) {
    ApplicationContextImpl applicationContext = getApplicationContext();
    if( !applicationContext.getThemeManager().hasTheme( themeId ) ) {
      throw new IllegalArgumentException( "Illegal theme id: " + themeId );
    }
  }

  private static Map<String, String> getEntryPointProperties( String servletPath ) {
    ApplicationContextImpl applicationContext = getApplicationContext();
    EntryPointManager entryPointManager = applicationContext.getEntryPointManager();
    EntryPointRegistration registration = entryPointManager.getRegistrationByPath( servletPath );
    if( registration != null ) {
      return registration.getProperties();
    }
    return Collections.emptyMap();
  }

  //////////////////////////////////////
  // Methods for accessing themed values

  public static CssValue getCssValue( String cssElement,
                                      String cssProperty,
                                      SimpleSelector selector )
  {
    return getCssValue( cssElement, cssProperty, selector, null );
  }

  public static CssValue getCssValue( String cssElement,
                                      String cssProperty,
                                      ValueSelector selector,
                                      Widget widget )
  {
    return getCssValue( getCurrentThemeId(), cssElement, cssProperty, selector, widget );
  }

  public static CssValue getCssValue( String themeId,
                                      String cssElement,
                                      String cssProperty,
                                      ValueSelector selector,
                                      Widget widget )
  {
    Theme theme =  getApplicationContext().getThemeManager().getTheme( themeId );
    CssValuesMap valuesMap = theme.getValuesMap();
    ConditionalValue[] values = valuesMap.getValues( cssElement, cssProperty );
    CssValue result = selector.select( widget, values );
    if( result == null ) {
      // resort to fallback theme
      theme = getFallbackTheme();
      valuesMap = theme.getValuesMap();
      values = valuesMap.getValues( cssElement, cssProperty );
      result = selector.select( widget, values );
    }
    return result;
  }

  private ThemeUtil() {
    // prevent instantiation
  }

}
