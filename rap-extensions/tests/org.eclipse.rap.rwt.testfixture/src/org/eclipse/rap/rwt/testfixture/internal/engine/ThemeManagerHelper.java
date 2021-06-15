/*******************************************************************************
 * Copyright (c) 2011, 2014 Frank Appel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frank Appel - initial API and implementation
 *    EclipseSource - bug 348056: Eliminate compiler warnings
 ******************************************************************************/
package org.eclipse.rap.rwt.testfixture.internal.engine;

import java.util.Arrays;
import java.util.List;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.theme.Theme;
import org.eclipse.rap.rwt.internal.theme.ThemeManager;


public class ThemeManagerHelper {

  private static ThemeManager themeManager;

  public static void resetThemeManager() {
    if( isThemeManagerAvailable() ) {
      doThemeManagerReset();
    }
  }

  public static void resetThemeManagerIfNeeded() {
    if( isThemeManagerResetNeeded() ) {
      doThemeManagerReset();
    }
  }

  public static ThemeManager ensureThemeManager() {
    if( themeManager == null ) {
      themeManager = new TestThemeManager();
    }
    return themeManager;
  }

  private static void doThemeManagerReset() {
    ( ( TestThemeManager )themeManager ).resetInstanceInTestCases();
  }

  private static boolean isThemeManagerResetNeeded() {
    boolean result = isThemeManagerAvailable();
    if( result ) {
      List<String> registeredThemeIds = Arrays.asList( themeManager.getRegisteredThemeIds() );
      if( registeredThemeIds.size() == 2 ) {
        result =    !registeredThemeIds.contains( ThemeManager.FALLBACK_THEME_ID )
                 || !registeredThemeIds.contains( RWT.DEFAULT_THEME_ID );
      }
    }
    return result;
  }

  private static boolean isThemeManagerAvailable() {
    return themeManager != null;
  }

  public static class TestThemeManager extends ThemeManager {
    boolean initialized;
    boolean activated;
    boolean deactivated;

    @Override
    public void initialize() {
      if( !initialized ) {
        // Register empty default theme. Execute tests against fall-back theme.
        registerTheme( new Theme( RWT.DEFAULT_THEME_ID, "RAP Default Theme", null ) );
        initialized = true;
      }
    }

    @Override
    public void activate() {
      if( !activated ) {
        super.activate();
        activated = true;
      }
      deactivated = false;
    }

    @Override
    public void deactivate() {
      // ignore reset for test cases to improve performance
      deactivated = true;
    }

    @Override
    public String[] getRegisteredThemeIds() {
      String[] result = new String[ 0 ];
      if( !deactivated ) {
        result = super.getRegisteredThemeIds();
      }
      return result;
    }

    public void resetInstanceInTestCases() {
      initialized = false;
      activated = false;
      super.deactivate();
    }
  }

}
