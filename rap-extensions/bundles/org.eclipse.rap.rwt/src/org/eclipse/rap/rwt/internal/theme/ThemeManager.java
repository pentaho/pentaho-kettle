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
 *    Rüdiger Herrmann - bug 335112
 *    Frank Appel - replaced singletons and static fields (Bug 337787)
 *                - fixed Bug 355723
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.theme;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.lifecycle.LifeCycleAdapterUtil;
import org.eclipse.rap.rwt.internal.theme.css.CssFileReader;
import org.eclipse.rap.rwt.internal.theme.css.StyleSheet;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.service.ResourceLoader;
import org.eclipse.swt.widgets.Widget;


/**
 * The ThemeManager maintains information about the themeable widgets and the
 * installed themes.
 */
public class ThemeManager {

  public static final String FALLBACK_THEME_ID = "org.eclipse.rap.rwt.theme.Fallback";
  private static final String FALLBACK_THEME_NAME = "RAP Fallback Theme";
  private static final String DEFAULT_THEME_NAME = "RAP Default Theme";

  public static final ResourceLoader STANDARD_RESOURCE_LOADER = new ResourceLoader() {
    ClassLoader classLoader = getClass().getClassLoader();
    @Override
    public InputStream getResourceAsStream( String resourceName ) throws IOException {
      return classLoader.getResourceAsStream( resourceName );
    }
  };

  private static final String[] THEMEABLE_WIDGETS = {
    org.eclipse.swt.widgets.Widget.class.getName(),
    org.eclipse.swt.widgets.Control.class.getName(),
    org.eclipse.swt.widgets.Composite.class.getName(),
    org.eclipse.swt.widgets.Button.class.getName(),
    org.eclipse.swt.widgets.Combo.class.getName(),
    org.eclipse.swt.widgets.CoolBar.class.getName(),
    org.eclipse.swt.custom.CTabFolder.class.getName(),
    org.eclipse.swt.widgets.Group.class.getName(),
    org.eclipse.swt.widgets.Label.class.getName(),
    org.eclipse.swt.widgets.Link.class.getName(),
    org.eclipse.swt.widgets.List.class.getName(),
    org.eclipse.swt.widgets.Menu.class.getName(),
    org.eclipse.swt.widgets.ProgressBar.class.getName(),
    org.eclipse.swt.widgets.Shell.class.getName(),
    org.eclipse.swt.widgets.Spinner.class.getName(),
    org.eclipse.swt.widgets.TabFolder.class.getName(),
    org.eclipse.swt.widgets.Table.class.getName(),
    org.eclipse.swt.widgets.Text.class.getName(),
    org.eclipse.swt.widgets.ToolBar.class.getName(),
    org.eclipse.swt.widgets.Tree.class.getName(),
    org.eclipse.swt.widgets.Scale.class.getName(),
    org.eclipse.swt.widgets.DateTime.class.getName(),
    org.eclipse.swt.widgets.ExpandBar.class.getName(),
    org.eclipse.swt.widgets.Sash.class.getName(),
    org.eclipse.swt.widgets.Slider.class.getName(),
    org.eclipse.swt.widgets.ToolTip.class.getName(),
    org.eclipse.swt.custom.CCombo.class.getName(),
    org.eclipse.swt.custom.CLabel.class.getName(),
    org.eclipse.swt.browser.Browser.class.getName(),
    org.eclipse.swt.custom.ScrolledComposite.class.getName(),
    org.eclipse.swt.widgets.ScrollBar.class.getName(),
    org.eclipse.rap.rwt.widgets.FileUpload.class.getName(),
    org.eclipse.rap.rwt.widgets.DropDown.class.getName(),
    "org.eclipse.nebula.widgets.grid.Grid"
  };

  private final Map<String, Theme> themes;
  private final ThemeableWidgetHolder themeableWidgets;
  private final List<String> appearances;
  private final ThemeAdapterManager themeAdapterManager;
  private final Map<String, String> resolvedPackageNames; // only for performance improvements
  private Theme fallbackTheme;
  private boolean initialized;

  public ThemeManager() {
    themes = new HashMap<>();
    themeableWidgets = new ThemeableWidgetHolder();
    appearances = new ArrayList<>();
    themeAdapterManager = new ThemeAdapterManager();
    resolvedPackageNames = new HashMap<>();
    initialized = false;
    createAndAddFallbackTheme();
    addDefaultThemableWidgets();
  }

  public void initialize() {
    if( !hasTheme( RWT.DEFAULT_THEME_ID ) ) {
      StyleSheet defaultStyleSheet = ThemeUtil.readDefaultThemeStyleSheet();
      Theme defaultTheme = new Theme( RWT.DEFAULT_THEME_ID, DEFAULT_THEME_NAME, defaultStyleSheet );
      registerTheme( defaultTheme );
    }
  }

  public void activate() {
    if( !initialized ) {
      initializeThemeableWidgets();
      Collection<Theme> allThemes = themes.values();
      ThemeableWidget[] allThemeableWidgets = themeableWidgets.getAll();
      for( Theme theme : allThemes ) {
        theme.initialize( allThemeableWidgets );
      }
      initialized = true;
    }
  }

  public void deactivate() {
    themes.clear();
    themeableWidgets.reset();
    appearances.clear();
    themeAdapterManager.reset();
    resolvedPackageNames.clear();
    initialized = false;
    createAndAddFallbackTheme();
    addDefaultThemableWidgets();
  }

  /**
   * Adds a custom widget to the list of themeable widgets. Note that this
   * method must be called before <code>initialize</code>.
   *
   * @param className the themeable widget to add, must not be <code>null</code>
   * @param loader the resource loader used to load theme resources like theme
   *          definitions etc. The resources to load follow a naming convention
   *          and must be resolved by the class loader. This argument must not
   *          be <code>null</code>.
   * @throws IllegalStateException if the ThemeManager is already initialized
   * @throws NullPointerException if a parameter is null
   * @throws IllegalArgumentException if the given widget is not a subtype of
   *           {@link Widget}
   */
  public void addThemeableWidget( String className, ResourceLoader loader ) {
    checkNotInitialized();
    ParamCheck.notNull( className, "widget" );
    ParamCheck.notNull( loader, "loader" );
    themeableWidgets.add( new ThemeableWidget( className, loader ) );
  }

  public ThemeAdapterManager getThemeAdapterManager() {
    return themeAdapterManager;
  }

  /**
   * Registers a theme. Must be called before <code>initialize()</code>.
   *
   * @param theme the theme to register
   * @throws IllegalStateException if already initialized
   * @throws IllegalArgumentException if a theme with the same id is already
   *           registered
   */
  public void registerTheme( Theme theme ) {
    checkNotInitialized();
    String id = theme.getId();
    if( themes.containsKey( id ) ) {
      String pattern = "Theme with id ''{0}'' exists already";
      Object[] arguments = { id };
      String msg = MessageFormat.format( pattern, arguments );
      throw new IllegalArgumentException( msg );
    }
    themes.put( id, theme );
  }

  /**
   * Determines whether a theme with the specified id has been registered.
   *
   * @param themeId the id to check for
   * @return <code>true</code> if a theme has been registered with the given
   *         id
   */
  public boolean hasTheme( String themeId ) {
    return themes.containsKey( themeId );
  }

  /**
   * Returns the theme registered with the given id.
   *
   * @param themeId the id of the theme to retrieve
   * @return the theme registered with the given id or <code>null</code> if
   *         there is no theme registered with this id
   */
  public Theme getTheme( String themeId ) {
    Theme result = null;
    if( themes.containsKey( themeId ) ) {
      result = themes.get( themeId );
    }
    return result;
  }

  /**
   * Returns a list of all registered themes.
   *
   * @return an array that contains the ids of all registered themes, never
   *         <code>null</code>
   */
  public String[] getRegisteredThemeIds() {
    String[] result = new String[ themes.size() ];
    return themes.keySet().toArray( result );
  }

  public ThemeableWidget getThemeableWidget( Class<? extends Widget> widget ) {
    return themeableWidgets.get( widget.getName() );
  }

  public List<String> getAppearances() {
    return Collections.unmodifiableList( appearances );
  }

  ThemeableWidget[] getAllThemeableWidgets() {
    return themeableWidgets.getAll();
  }

  private void checkNotInitialized() {
    if( initialized ) {
      throw new IllegalStateException( "ThemeManager is already initialized" );
    }
  }

  private void createAndAddFallbackTheme() {
    fallbackTheme = new Theme( FALLBACK_THEME_ID, FALLBACK_THEME_NAME, null );
    themes.put( FALLBACK_THEME_ID, fallbackTheme );
  }

  private void initializeThemeableWidgets() {
    ThemeableWidget[] widgets = themeableWidgets.getAll();
    for( ThemeableWidget widget : widgets ) {
      loadThemeableWidgetResources( widget );
    }
  }

  private void addDefaultThemableWidgets() {
    for( String className : THEMEABLE_WIDGETS ) {
      addThemeableWidget( className, STANDARD_RESOURCE_LOADER );
    }
  }

  private void loadThemeableWidgetResources( ThemeableWidget themeWidget ) {
    String className = LifeCycleAdapterUtil.getSimpleClassName( themeWidget.className );
    String[] variants = LifeCycleAdapterUtil.getKitPackageVariants( themeWidget.className );
    boolean found = false;
    try {
      for( int i = 0; i < variants.length && !found ; i++ ) {
        found |= loadThemeDef( themeWidget, variants[ i ], className );
        found |= loadAppearanceJs( themeWidget, variants[ i ], className );
        found |= loadDefaultCss( themeWidget, variants[ i ], className );
      }
      if( themeWidget.defaultStyleSheet != null ) {
        fallbackTheme.addStyleSheet( themeWidget.defaultStyleSheet );
      }
    } catch( IOException e ) {
      String msg = "Failed to initialize themeable widget: " + themeWidget.className;
      throw new ThemeManagerException( msg, e );
    }
  }

  private boolean loadThemeDef( ThemeableWidget themeWidget, String pkgName, String className )
    throws IOException
  {
    boolean result = false;
    String resPkgName = resolvePackageName( pkgName );
    String fileName = resPkgName + "/" + className + ".theme.xml";
    InputStream inStream = themeWidget.loader.getResourceAsStream( fileName );
    if( inStream != null ) {
      result = true;
      try {
        ThemeDefinitionReader reader = new ThemeDefinitionReader( inStream, fileName );
        reader.read();
        themeWidget.elements = reader.getThemeCssElements();
      } catch( Exception e ) {
        String message = "Failed to parse theme definition file " + fileName;
        throw new ThemeManagerException( message, e );
      } finally {
        inStream.close();
      }
    }
    return result;
  }

  private boolean loadAppearanceJs( ThemeableWidget themeWidget, String pkgName, String className )
    throws IOException
  {
    boolean result = false;
    String resPkgName = resolvePackageName( pkgName );
    String fileName = resPkgName + "/" + className + ".appearances.js";
    InputStream inStream = themeWidget.loader.getResourceAsStream( fileName );
    if( inStream != null ) {
      try {
        String content = AppearancesUtil.readAppearanceFile( inStream );
        appearances.add( content );
        result = true;
      } finally {
        inStream.close();
      }
    }
    return result;
  }

  private boolean loadDefaultCss( ThemeableWidget themeWidget, String pkgName, String className )
    throws IOException
  {
    boolean result = false;
    String resPkgName = resolvePackageName( pkgName );
    String fileName = resPkgName + "/" + className + ".default.css";
    ResourceLoader resLoader = themeWidget.loader;
    InputStream inStream = resLoader.getResourceAsStream( fileName );
    if( inStream != null ) {
      try {
        // TODO [rst] Check for illegal element names in selector list
        themeWidget.defaultStyleSheet
          = CssFileReader.readStyleSheet( inStream, fileName, resLoader );
        result = true;
      } finally {
        inStream.close();
      }
    }
    return result;
  }

  private String resolvePackageName( String packageName ) {
    String result = resolvedPackageNames.get( packageName );
    if( result == null ) {
      result =  packageName.replace( '.', '/' );
      resolvedPackageNames.put( packageName, result );
    }
    return result;
  }

}
