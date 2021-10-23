/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.core;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.ObjectUsageCount;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.gui.GUIOption;
import org.pentaho.di.core.gui.GUIPositionInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.plugins.LifecyclePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.laf.BasePropertyHandler;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * We use Props to store all kinds of user interactive information such as the selected colors, fonts, positions of
 * windows, etc.
 *
 * @author Matt
 * @since 15-12-2003
 */
public class PropsUI extends Props {

  private static String OS = System.getProperty( "os.name" ).toLowerCase();

  private static final String NO = "N";

  private static final String YES = "Y";

  private static Display display;

  protected List<LastUsedFile> lastUsedFiles;
  protected List<LastUsedFile> openTabFiles;
  protected Map<String, List<LastUsedFile>> lastUsedRepoFiles;

  protected String overriddenFileName;

  private Hashtable<String, WindowProperty> screens;

  private static final String STRING_SHOW_COPY_OR_DISTRIBUTE_WARNING = "ShowCopyOrDistributeWarning";

  private static final String STRING_SHOW_WELCOME_PAGE_ON_STARTUP = "ShowWelcomePageOnStartup";

  private static final String STRING_SHOW_BRANDING_GRAPHICS = "ShowBrandingGraphics";

  private static final String STRING_ONLY_SHOW_ACTIVE_FILE = "OnlyShowActiveFileInTree";

  private static final String SHOW_TOOL_TIPS = "ShowToolTips";

  private static final String SHOW_HELP_TOOL_TIPS = "ShowHelpToolTips";

  private static final String CANVAS_GRID_SIZE = "CanvasGridSize";

  private static final String LEGACY_PERSPECTIVE_MODE = "LegacyPerspectiveMode";

  private static final String DISABLE_BROWSER_ENVIRONMENT_CHECK = "DisableBrowserEnvironmentCheck";

  private static List<GUIOption<Object>> editables;

  /**
   * Initialize the properties: load from disk.
   *
   * @param d The Display
   * @param t The type of properties file.
   */
  public static void init( Display d, int t ) {
    if ( props == null || Const.isRunningOnWebspoonMode() ) {
      display = d;
      props = new PropsUI( t );

      // Also init the colors and fonts to use...
      GUIResource.getInstance();
    } else {
      throw new RuntimeException( "The Properties systems settings are already initialised!" );
    }
  }

  /**
   * Initialize the properties: load from disk.
   *
   * @param d        The Display
   * @param filename the filename to use
   */
  public static void init( Display d, String filename ) {
    if ( props == null ) {
      display = d;
      props = new PropsUI( filename );

      // Also init the colors and fonts to use...
      GUIResource.getInstance();
    } else {
      throw new RuntimeException( "The properties systems settings are already initialised!" );
    }
  }

  /**
   * Check to see whether the Kettle properties where loaded.
   *
   * @return true if the Kettle properties where loaded.
   */
  public static boolean isInitialized() {
    return props != null;
  }

  public static PropsUI getInstance() {
    if ( Const.isRunningOnWebspoonMode() ) {
      try {
        Class webSpoonUtils = Class.forName( "org.pentaho.di.webspoon.WebSpoonUtils" );
        Method getUISession = webSpoonUtils.getDeclaredMethod( "getUISession" );
        Class singletonUtil = Class.forName( "org.eclipse.rap.rwt.SingletonUtil" );
        Method getUniqueInstance = Arrays.stream( singletonUtil.getDeclaredMethods() )
                .filter( method -> method.getName().equals( "getUniqueInstance" ) && method.toGenericString().contains( "UISession" ) )
                .collect( Collectors.toList() ).get( 0 );
        return (PropsUI) getUniqueInstance.invoke( null, PropsUI.class, getUISession.invoke( null ) );
      } catch ( ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e ) {
        e.printStackTrace();
        return null;
      }
    }
    else if ( props != null ) {
      return (PropsUI) props;
    }

    throw new RuntimeException( "Properties, Kettle systems settings, not initialised!" );
  }

  private PropsUI( int t ) {
    super( t );
  }

  private PropsUI( String filename ) {
    super( filename );
  }

  @SuppressWarnings( "unchecked" )
  protected synchronized void init() {
    super.createLogChannel();
    properties = new Properties();
    pluginHistory = new ArrayList<ObjectUsageCount>();

    setDefault();
    loadProps();
    addDefaultEntries();

    loadPluginHistory();

    loadScreens();
    loadLastUsedFiles();
    loadLastUsedRepoFiles();
    loadOpenTabFiles();
    resetRecentSearches();

    PluginRegistry registry = PluginRegistry.getInstance();
    List<PluginInterface> plugins = registry.getPlugins( LifecyclePluginType.class );
    List<GUIOption<Object>> leditables = new ArrayList<GUIOption<Object>>();
    for ( PluginInterface plugin : plugins ) {
      if ( !plugin.getClassMap().keySet().contains( GUIOption.class ) ) {
        continue;
      }

      try {
        GUIOption<Object> loaded = registry.loadClass( plugin, GUIOption.class );
        if ( loaded != null ) {
          leditables.add( loaded );
        }
      } catch ( ClassCastException cce ) {
        // Not all Lifecycle plugins implement GUIOption, keep calm and carry on
        LogChannel.GENERAL.logDebug( "Plugin " + plugin.getIds()[ 0 ]
          + " does not implement GUIOption, it will not be editable" );
      } catch ( Exception e ) {
        LogChannel.GENERAL.logError( "Unexpected error loading class for plugin " + plugin.getName(), e );
      }
    }

    editables = Collections.unmodifiableList( leditables );

  }

  public void setDefault() {
    FontData fd;
    RGB col;

    lastUsedFiles = new ArrayList<LastUsedFile>();
    lastUsedRepoFiles = new LinkedHashMap<>();
    openTabFiles = new ArrayList<LastUsedFile>();
    screens = new Hashtable<String, WindowProperty>();

    properties.setProperty( STRING_LOG_LEVEL, getLogLevel() );
    properties.setProperty( STRING_LOG_FILTER, getLogFilter() );

    if ( ( !Const.isRunningOnWebspoonMode() && display != null ) || ( Const.isRunningOnWebspoonMode() && Display.getCurrent() != null ) ) {
      // Set Default Look for all dialogs and sizes.
      String prop =
        BasePropertyHandler.getProperty( "Default_UI_Properties_Resource", "org.pentaho.di.ui.core.default" );
      try {
        ResourceBundle bundle = PropertyResourceBundle.getBundle( prop );
        if ( bundle != null ) {
          Enumeration<String> enumer = bundle.getKeys();
          String theKey;
          while ( enumer.hasMoreElements() ) {
            theKey = enumer.nextElement();
            properties.setProperty( theKey, bundle.getString( theKey ) );
          }
        }
      } catch ( Exception ex ) {
        // don't throw an exception, but log it.
        ex.printStackTrace();
      }

      fd = getFixedFont();
      properties.setProperty( STRING_FONT_FIXED_NAME, fd.getName() );
      properties.setProperty( STRING_FONT_FIXED_SIZE, "" + fd.getHeight() );
      properties.setProperty( STRING_FONT_FIXED_STYLE, "" + fd.getStyle() );

      fd = getDefaultFont();
      properties.setProperty( STRING_FONT_DEFAULT_NAME, fd.getName() );
      properties.setProperty( STRING_FONT_DEFAULT_SIZE, "" + fd.getHeight() );
      properties.setProperty( STRING_FONT_DEFAULT_STYLE, "" + fd.getStyle() );

      fd = getDefaultFont();
      properties.setProperty( STRING_FONT_GRAPH_NAME, fd.getName() );
      properties.setProperty( STRING_FONT_GRAPH_SIZE, "" + fd.getHeight() );
      properties.setProperty( STRING_FONT_GRAPH_STYLE, "" + fd.getStyle() );

      fd = getDefaultFont();
      properties.setProperty( STRING_FONT_GRID_NAME, fd.getName() );
      properties.setProperty( STRING_FONT_GRID_SIZE, "" + fd.getHeight() );
      properties.setProperty( STRING_FONT_GRID_STYLE, "" + fd.getStyle() );

      fd = getDefaultFont();
      properties.setProperty( STRING_FONT_NOTE_NAME, fd.getName() );
      properties.setProperty( STRING_FONT_NOTE_SIZE, "" + fd.getHeight() );
      properties.setProperty( STRING_FONT_NOTE_STYLE, "" + fd.getStyle() );

      col = getBackgroundRGB();
      properties.setProperty( STRING_BACKGROUND_COLOR_R, "" + col.red );
      properties.setProperty( STRING_BACKGROUND_COLOR_G, "" + col.green );
      properties.setProperty( STRING_BACKGROUND_COLOR_B, "" + col.blue );

      col = getGraphColorRGB();
      properties.setProperty( STRING_GRAPH_COLOR_R, "" + col.red );
      properties.setProperty( STRING_GRAPH_COLOR_G, "" + col.green );
      properties.setProperty( STRING_GRAPH_COLOR_B, "" + col.blue );

      properties.setProperty( STRING_ICON_SIZE, "" + getIconSize() );
      properties.setProperty( STRING_LINE_WIDTH, "" + getLineWidth() );
      properties.setProperty( STRING_SHADOW_SIZE, "" + getShadowSize() );
      properties.setProperty( STRING_MAX_UNDO, "" + getMaxUndo() );

      setSashWeights( getSashWeights() );
    }
  }

  public void storeScreens() {
    // Add screens hash table to properties..
    //
    Enumeration<String> keys = screens.keys();
    int nr = 1;
    while ( keys.hasMoreElements() ) {
      String name = keys.nextElement();
      properties.setProperty( "ScreenName" + nr, name );

      WindowProperty winprop = screens.get( name );
      properties.setProperty( STRING_SIZE_MAX + nr, winprop.isMaximized() ? YES : NO );
      if ( winprop.getRectangle() != null ) {
        properties.setProperty( STRING_SIZE_X + nr, "" + winprop.getX() );
        properties.setProperty( STRING_SIZE_Y + nr, "" + winprop.getY() );
        properties.setProperty( STRING_SIZE_W + nr, "" + winprop.getWidth() );
        properties.setProperty( STRING_SIZE_H + nr, "" + winprop.getHeight() );
      }

      nr++;
    }
  }

  public void loadScreens() {
    screens = new Hashtable<String, WindowProperty>();

    int nr = 1;

    String name = properties.getProperty( "ScreenName" + nr );
    while ( name != null ) {
      boolean max = YES.equalsIgnoreCase( properties.getProperty( STRING_SIZE_MAX + nr ) );
      int x = Const.toInt( properties.getProperty( STRING_SIZE_X + nr ), 0 );
      int y = Const.toInt( properties.getProperty( STRING_SIZE_Y + nr ), 0 );
      int w = Const.toInt( properties.getProperty( STRING_SIZE_W + nr ), 320 );
      int h = Const.toInt( properties.getProperty( STRING_SIZE_H + nr ), 200 );

      WindowProperty winprop = new WindowProperty( name, max, x, y, w, h );
      screens.put( name, winprop );

      nr++;
      name = properties.getProperty( "ScreenName" + nr );
    }
  }

  public void saveProps() {
    storeScreens();
    setLastFiles();
    setLastUsedRepoFiles();
    setOpenTabFiles();

    super.saveProps();
  }

  public void setLastFiles() {
    properties.setProperty( "lastfiles", "" + lastUsedFiles.size() );
    for ( int i = 0; i < lastUsedFiles.size(); i++ ) {
      LastUsedFile lastUsedFile = lastUsedFiles.get( i );

      properties.setProperty( "filetype" + ( i + 1 ), Const.NVL( lastUsedFile.getFileType(),
        LastUsedFile.FILE_TYPE_TRANSFORMATION ) );
      properties.setProperty( "lastfile" + ( i + 1 ), Const.NVL( lastUsedFile.getFilename(), "" ) );
      properties.setProperty( "lastdir" + ( i + 1 ), Const.NVL( lastUsedFile.getDirectory(), "" ) );
      properties.setProperty( "lasttype" + ( i + 1 ), lastUsedFile.isSourceRepository() ? YES : NO );
      properties.setProperty( "lastrepo" + ( i + 1 ), Const.NVL( lastUsedFile.getRepositoryName(), "" ) );
      properties.setProperty( "lastuser" + ( i + 1 ), Const.NVL( lastUsedFile.getUsername(), "" ) );
      properties.setProperty( "lastconnection" + ( i + 1 ), Const.NVL( lastUsedFile.getConnection(), "" ) );
    }
  }

  public void setLastUsedRepoFiles() {
    int lastRepoFiles = 0;
    for ( String repoName : lastUsedRepoFiles.keySet() ) {
      lastRepoFiles += lastUsedRepoFiles.get( repoName ).size();
    }
    properties.setProperty( "lastrepofiles", "" + lastRepoFiles );
    int i = 0;
    for ( String repoName : lastUsedRepoFiles.keySet() ) {
      for ( LastUsedFile lastUsedFile : lastUsedRepoFiles.get( repoName ) ) {
        properties.setProperty( "repofiletype" + ( i + 1 ), Const.NVL( lastUsedFile.getFileType(),
          LastUsedFile.FILE_TYPE_TRANSFORMATION ) );
        properties.setProperty( "repolastfile" + ( i + 1 ), Const.NVL( lastUsedFile.getFilename(), "" ) );
        properties.setProperty( "repolastdir" + ( i + 1 ), Const.NVL( lastUsedFile.getDirectory(), "" ) );
        properties.setProperty( "repolasttype" + ( i + 1 ), lastUsedFile.isSourceRepository() ? YES : NO );
        properties.setProperty( "repolastrepo" + ( i + 1 ), Const.NVL( lastUsedFile.getRepositoryName(), "" ) );
        properties.setProperty( "repolastuser" + ( i + 1 ), Const.NVL( lastUsedFile.getUsername(), "" ) );
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        properties.setProperty( "repolastdate" + ( i + 1 ),
          Const.NVL( simpleDateFormat.format( lastUsedFile.getLastOpened() ), "" ) );
        i++;
      }
    }
  }

  public void setOpenTabFiles() {
    properties.setProperty( "tabfiles", "" + openTabFiles.size() );
    for ( int i = 0; i < openTabFiles.size(); i++ ) {
      LastUsedFile openTabFile = openTabFiles.get( i );

      properties.setProperty( "tabtype" + ( i + 1 ), Const.NVL( openTabFile.getFileType(),
        LastUsedFile.FILE_TYPE_TRANSFORMATION ) );
      properties.setProperty( "tabfile" + ( i + 1 ), Const.NVL( openTabFile.getFilename(), "" ) );
      properties.setProperty( "tabdir" + ( i + 1 ), Const.NVL( openTabFile.getDirectory(), "" ) );
      properties.setProperty( "tabrep" + ( i + 1 ), openTabFile.isSourceRepository() ? YES : NO );
      properties.setProperty( "tabrepname" + ( i + 1 ), Const.NVL( openTabFile.getRepositoryName(), "" ) );
      properties.setProperty( "tabopened" + ( i + 1 ), openTabFile.isOpened() ? YES : NO );
      properties.setProperty( "tabopentypes" + ( i + 1 ), "" + openTabFile.getOpenItemTypes() );
      properties.setProperty( "tabconnection" + ( i + 1 ), "" + openTabFile.getConnection() );
    }
  }

  /**
   * Add a last opened file to the top of the recently used list.
   *
   * @param fileType         the type of file to use @see LastUsedFile
   * @param filename         The name of the file or transformation
   * @param directory        The repository directory path, null in case lf is an XML file
   * @param sourceRepository True if the file was loaded from repository, false if ld is an XML file.
   * @param repositoryName   The name of the repository the file was loaded from or save to.
   */
  public void addLastFile( String fileType, String filename, String directory, boolean sourceRepository,
                           String repositoryName ) {
    addLastFile( fileType, filename, directory, sourceRepository, repositoryName, "", null, null );
  }

  /**
   * Add a last opened file to the top of the recently used list.
   *
   * @param fileType         the type of file to use @see LastUsedFile
   * @param filename         The name of the file or transformation
   * @param directory        The repository directory path, null in case lf is an XML file
   * @param sourceRepository True if the file was loaded from repository, false if ld is an XML file.
   * @param repositoryName   The name of the repository the file was loaded from or save to.
   */
  public void addLastFile( String fileType, String filename, String directory, boolean sourceRepository,
                           String repositoryName, String username, Date lastOpened, String connection ) {
    LastUsedFile lastUsedFile =
      new LastUsedFile( fileType, filename, directory, sourceRepository, repositoryName, username, false,
        LastUsedFile.OPENED_ITEM_TYPE_MASK_GRAPH, lastOpened, connection );

    int idx = lastUsedFiles.indexOf( lastUsedFile );
    if ( idx >= 0 ) {
      lastUsedFiles.remove( idx );
    }
    // Add it to position 0
    lastUsedFiles.add( 0, lastUsedFile );

    // If we have more than Const.MAX_FILE_HIST, top it off
    while ( lastUsedFiles.size() > Const.MAX_FILE_HIST ) {
      lastUsedFiles.remove( lastUsedFiles.size() - 1 );
    }

    addLastRepoFile( lastUsedFile );
  }

  private void addLastRepoFile( LastUsedFile lastUsedFile ) {
    String repositoryName = lastUsedFile.getRepositoryName();
    String username = lastUsedFile.getUsername() != null ? lastUsedFile.getUsername() : "";
    if ( !Utils.isEmpty( repositoryName ) ) {
      List<LastUsedFile> lastUsedFiles =
        lastUsedRepoFiles.getOrDefault( repositoryName + ":" + username, new ArrayList<>() );
      int idx = lastUsedFiles.indexOf( lastUsedFile );
      if ( idx >= 0 ) {
        lastUsedFiles.remove( idx );
      }

      lastUsedFiles.add( 0, lastUsedFile );
      lastUsedRepoFiles.put( repositoryName + ":" + username, lastUsedFiles );

      // If we have more than Const.MAX_FILE_HIST, top it off
      while ( lastUsedFiles.size() > 12 ) {
        lastUsedFiles.remove( lastUsedRepoFiles.get( repositoryName + ":" + username ).size() - 1 );
      }
    }
  }

  public void addOpenTabFile( String fileType, String filename, String directory, boolean sourceRepository,
                              String repositoryName, int openTypes, String connection ) {
    LastUsedFile lastUsedFile =
      new LastUsedFile( fileType, filename, directory, sourceRepository, repositoryName, null, true, openTypes, null,
        connection );
    openTabFiles.add( lastUsedFile );
  }

  /**
   * Add a last opened file to the top of the recently used list.
   *
   * @param fileType         the type of file to use @see LastUsedFile
   * @param filename         The name of the file or transformation
   * @param directory        The repository directory path, null in case lf is an XML file
   * @param sourceRepository True if the file was loaded from repository, false if ld is an XML file.
   * @param repositoryName   The name of the repository the file was loaded from or save to.
   */
  public void addOpenTabFile( String fileType, String filename, String directory, boolean sourceRepository,
                              String repositoryName, int openTypes ) {
    LastUsedFile lastUsedFile =
      new LastUsedFile( fileType, filename, directory, sourceRepository, repositoryName, true, openTypes );
    openTabFiles.add( lastUsedFile );
  }

  public void loadLastUsedFiles() {
    lastUsedFiles = new ArrayList<LastUsedFile>();
    int nr = Const.toInt( properties.getProperty( "lastfiles" ), 0 );
    for ( int i = 0; i < nr; i++ ) {
      String fileType = properties.getProperty( "filetype" + ( i + 1 ), LastUsedFile.FILE_TYPE_TRANSFORMATION );
      String filename = properties.getProperty( "lastfile" + ( i + 1 ), "" );
      String directory = properties.getProperty( "lastdir" + ( i + 1 ), "" );
      boolean sourceRepository = YES.equalsIgnoreCase( properties.getProperty( "lasttype" + ( i + 1 ), NO ) );
      String repositoryName = properties.getProperty( "lastrepo" + ( i + 1 ) );
      boolean isOpened = YES.equalsIgnoreCase( properties.getProperty( "lastopened" + ( i + 1 ), NO ) );
      int openItemTypes = Const.toInt( properties.getProperty( "lastopentypes" + ( i + 1 ), "0" ), 0 );

      lastUsedFiles.add(
        new LastUsedFile( fileType, filename, directory, sourceRepository, repositoryName, isOpened,
          openItemTypes ) );
    }
  }

  public void loadLastUsedRepoFiles() {
    lastUsedRepoFiles = new LinkedHashMap<>();
    int nr = Const.toInt( properties.getProperty( "lastrepofiles" ), 0 );
    for ( int i = 0; i < nr; i++ ) {
      String fileType = properties.getProperty( "repofiletype" + ( i + 1 ), LastUsedFile.FILE_TYPE_TRANSFORMATION );
      String filename = properties.getProperty( "repolastfile" + ( i + 1 ), "" );
      String directory = properties.getProperty( "repolastdir" + ( i + 1 ), "" );
      boolean sourceRepository = YES.equalsIgnoreCase( properties.getProperty( "repolasttype" + ( i + 1 ), NO ) );
      String repositoryName = properties.getProperty( "repolastrepo" + ( i + 1 ) );
      String username = properties.getProperty( "repolastuser" + ( i + 1 ) );
      boolean isOpened = YES.equalsIgnoreCase( properties.getProperty( "repolastopened" + ( i + 1 ), NO ) );
      int openItemTypes = Const.toInt( properties.getProperty( "repolastopentypes" + ( i + 1 ), "0" ), 0 );
      Date lastOpened = null;
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
      try {
        String repoLastDate = properties.getProperty( "repolastdate" + ( i + 1 ) );
        if ( repoLastDate != null ) {
          lastOpened = simpleDateFormat.parse( repoLastDate );
        }
      } catch ( ParseException pe ) {
        // Default of null is acceptable
      }

      List<LastUsedFile> lastUsedFiles =
        lastUsedRepoFiles.getOrDefault( repositoryName + ":" + username, new ArrayList<>() );
      lastUsedFiles.add(
        new LastUsedFile( fileType, filename, directory, sourceRepository, repositoryName, username, isOpened,
          openItemTypes, lastOpened, null ) );
      lastUsedRepoFiles.put( repositoryName + ":" + username, lastUsedFiles );
    }
  }

  public void loadOpenTabFiles() {
    openTabFiles = new ArrayList<LastUsedFile>();
    int nr = Const.toInt( properties.getProperty( "tabfiles" ), 0 );
    for ( int i = 0; i < nr; i++ ) {
      String fileType = properties.getProperty( "tabtype" + ( i + 1 ), LastUsedFile.FILE_TYPE_TRANSFORMATION );
      String filename = properties.getProperty( "tabfile" + ( i + 1 ), "" );
      String directory = properties.getProperty( "tabdir" + ( i + 1 ), "" );
      String connection = properties.getProperty( "tabconnection" + ( i + 1 ), "" );
      boolean sourceRepository = YES.equalsIgnoreCase( properties.getProperty( "tabrep" + ( i + 1 ), NO ) );
      String repositoryName = properties.getProperty( "tabrepname" + ( i + 1 ) );
      boolean isOpened = YES.equalsIgnoreCase( properties.getProperty( "tabopened" + ( i + 1 ), NO ) );
      int openItemTypes = Const.toInt( properties.getProperty( "tabopentypes" + ( i + 1 ), "0" ), 0 );

      openTabFiles.add(
        new LastUsedFile( fileType, filename, directory, sourceRepository, repositoryName, null, isOpened,
          openItemTypes, null, connection ) );
    }
  }

  public Map<String, List<LastUsedFile>> getLastUsedRepoFiles() {
    return lastUsedRepoFiles;
  }

  public List<LastUsedFile> getLastUsedFiles() {
    return lastUsedFiles;
  }

  public void setLastUsedFiles( List<LastUsedFile> lastUsedFiles ) {
    this.lastUsedFiles = lastUsedFiles;
  }

  public String[] getLastFileTypes() {
    String[] retval = new String[ lastUsedFiles.size() ];
    for ( int i = 0; i < retval.length; i++ ) {
      LastUsedFile lastUsedFile = lastUsedFiles.get( i );
      retval[ i ] = lastUsedFile.getFileType();
    }
    return retval;
  }

  public String[] getLastFiles() {
    String[] retval = new String[ lastUsedFiles.size() ];
    for ( int i = 0; i < retval.length; i++ ) {
      LastUsedFile lastUsedFile = lastUsedFiles.get( i );
      retval[ i ] = lastUsedFile.getFilename();
    }
    return retval;
  }

  public String getFilename() {
    if ( this.filename == null ) {
      String s = System.getProperty( "org.pentaho.di.ui.PropsUIFile" );
      if ( s != null ) {
        return s;
      } else {
        return super.getFilename();
      }
    } else {
      return this.filename;
    }
  }

  public String[] getLastDirs() {
    String[] retval = new String[ lastUsedFiles.size() ];
    for ( int i = 0; i < retval.length; i++ ) {
      LastUsedFile lastUsedFile = lastUsedFiles.get( i );
      retval[ i ] = lastUsedFile.getDirectory();
    }
    return retval;
  }

  public boolean[] getLastTypes() {
    boolean[] retval = new boolean[ lastUsedFiles.size() ];
    for ( int i = 0; i < retval.length; i++ ) {
      LastUsedFile lastUsedFile = lastUsedFiles.get( i );
      retval[ i ] = lastUsedFile.isSourceRepository();
    }
    return retval;
  }

  public String[] getLastRepositories() {
    String[] retval = new String[ lastUsedFiles.size() ];
    for ( int i = 0; i < retval.length; i++ ) {
      LastUsedFile lastUsedFile = lastUsedFiles.get( i );
      retval[ i ] = lastUsedFile.getRepositoryName();
    }
    return retval;
  }

  public void setFixedFont( FontData fd ) {
    properties.setProperty( STRING_FONT_FIXED_NAME, fd.getName() );
    properties.setProperty( STRING_FONT_FIXED_SIZE, "" + fd.getHeight() );
    properties.setProperty( STRING_FONT_FIXED_STYLE, "" + fd.getStyle() );
  }

  public FontData getFixedFont() {
    FontData def = getDefaultFontData();

    String name = properties.getProperty( STRING_FONT_FIXED_NAME );
    int size = Const.toInt( properties.getProperty( STRING_FONT_FIXED_SIZE ), def.getHeight() );
    int style = Const.toInt( properties.getProperty( STRING_FONT_FIXED_STYLE ), def.getStyle() );

    return new FontData( name, size, style );
  }

  public void setDefaultFont( FontData fd ) {
    if ( fd != null ) {
      properties.setProperty( STRING_FONT_DEFAULT_NAME, fd.getName() );
      properties.setProperty( STRING_FONT_DEFAULT_SIZE, "" + fd.getHeight() );
      properties.setProperty( STRING_FONT_DEFAULT_STYLE, "" + fd.getStyle() );
    }
  }

  public FontData getDefaultFont() {
    FontData def = getDefaultFontData();

    if ( isOSLookShown() ) {
      return def;
    }

    String name = properties.getProperty( STRING_FONT_DEFAULT_NAME, def.getName() );
    int size = Const.toInt( properties.getProperty( STRING_FONT_DEFAULT_SIZE ), def.getHeight() );
    int style = Const.toInt( properties.getProperty( STRING_FONT_DEFAULT_STYLE ), def.getStyle() );

    return new FontData( name, size, style );
  }

  public void setGraphFont( FontData fd ) {
    properties.setProperty( STRING_FONT_GRAPH_NAME, fd.getName() );
    properties.setProperty( STRING_FONT_GRAPH_SIZE, "" + fd.getHeight() );
    properties.setProperty( STRING_FONT_GRAPH_STYLE, "" + fd.getStyle() );
  }

  public FontData getGraphFont() {
    FontData def = getDefaultFontData();

    String name = properties.getProperty( STRING_FONT_GRAPH_NAME, def.getName() );

    int size = Const.toInt( properties.getProperty( STRING_FONT_GRAPH_SIZE ), def.getHeight() );
    int style = Const.toInt( properties.getProperty( STRING_FONT_GRAPH_STYLE ), def.getStyle() );

    return new FontData( name, size, style );
  }

  public void setGridFont( FontData fd ) {
    properties.setProperty( STRING_FONT_GRID_NAME, fd.getName() );
    properties.setProperty( STRING_FONT_GRID_SIZE, "" + fd.getHeight() );
    properties.setProperty( STRING_FONT_GRID_STYLE, "" + fd.getStyle() );
  }

  public FontData getGridFont() {
    FontData def = getDefaultFontData();

    String name = properties.getProperty( STRING_FONT_GRID_NAME, def.getName() );
    String ssize = properties.getProperty( STRING_FONT_GRID_SIZE );
    String sstyle = properties.getProperty( STRING_FONT_GRID_STYLE );

    int size = Const.toInt( ssize, def.getHeight() );
    int style = Const.toInt( sstyle, def.getStyle() );

    return new FontData( name, size, style );
  }

  public void setNoteFont( FontData fd ) {
    properties.setProperty( STRING_FONT_NOTE_NAME, fd.getName() );
    properties.setProperty( STRING_FONT_NOTE_SIZE, "" + fd.getHeight() );
    properties.setProperty( STRING_FONT_NOTE_STYLE, "" + fd.getStyle() );
  }

  public FontData getNoteFont() {
    FontData def = getDefaultFontData();

    String name = properties.getProperty( STRING_FONT_NOTE_NAME, def.getName() );
    String ssize = properties.getProperty( STRING_FONT_NOTE_SIZE );
    String sstyle = properties.getProperty( STRING_FONT_NOTE_STYLE );

    int size = Const.toInt( ssize, def.getHeight() );
    int style = Const.toInt( sstyle, def.getStyle() );

    return new FontData( name, size, style );
  }

  public void setBackgroundRGB( RGB c ) {
    properties.setProperty( STRING_BACKGROUND_COLOR_R, c != null ? "" + c.red : "" );
    properties.setProperty( STRING_BACKGROUND_COLOR_G, c != null ? "" + c.green : "" );
    properties.setProperty( STRING_BACKGROUND_COLOR_B, c != null ? "" + c.blue : "" );
  }

  public RGB getBackgroundRGB() {
    int r = Const.toInt( properties.getProperty( STRING_BACKGROUND_COLOR_R ), ConstUI.COLOR_BACKGROUND_RED ); // Defaut:
    int g = Const.toInt( properties.getProperty( STRING_BACKGROUND_COLOR_G ), ConstUI.COLOR_BACKGROUND_GREEN );
    int b = Const.toInt( properties.getProperty( STRING_BACKGROUND_COLOR_B ), ConstUI.COLOR_BACKGROUND_BLUE );

    return new RGB( r, g, b );
  }

  public void setGraphColorRGB( RGB c ) {
    properties.setProperty( STRING_GRAPH_COLOR_R, "" + c.red );
    properties.setProperty( STRING_GRAPH_COLOR_G, "" + c.green );
    properties.setProperty( STRING_GRAPH_COLOR_B, "" + c.blue );
  }

  public RGB getGraphColorRGB() {
    int r = Const.toInt( properties.getProperty( STRING_GRAPH_COLOR_R ), ConstUI.COLOR_GRAPH_RED ); // default White
    int g = Const.toInt( properties.getProperty( STRING_GRAPH_COLOR_G ), ConstUI.COLOR_GRAPH_GREEN );
    int b = Const.toInt( properties.getProperty( STRING_GRAPH_COLOR_B ), ConstUI.COLOR_GRAPH_BLUE );

    return new RGB( r, g, b );
  }

  public void setTabColorRGB( RGB c ) {
    properties.setProperty( STRING_TAB_COLOR_R, "" + c.red );
    properties.setProperty( STRING_TAB_COLOR_G, "" + c.green );
    properties.setProperty( STRING_TAB_COLOR_B, "" + c.blue );
  }

  public RGB getTabColorRGB() {
    int r = Const.toInt( properties.getProperty( STRING_TAB_COLOR_R ), ConstUI.COLOR_TAB_RED ); // default White
    int g = Const.toInt( properties.getProperty( STRING_TAB_COLOR_G ), ConstUI.COLOR_TAB_GREEN );
    int b = Const.toInt( properties.getProperty( STRING_TAB_COLOR_B ), ConstUI.COLOR_TAB_BLUE );

    return new RGB( r, g, b );
  }

  public boolean isSVGEnabled() {
    String enabled = properties.getProperty( STRING_SVG_ENABLED, YES );
    return YES.equalsIgnoreCase( enabled ); // Default: svg is enabled
  }

  public void setSVGEnabled( boolean svg ) {
    properties.setProperty( STRING_SVG_ENABLED, svg ? YES : NO );
  }

  public void setIconSize( int size ) {
    properties.setProperty( STRING_ICON_SIZE, "" + size );
  }

  public int getIconSize() {
    return Const.toInt( properties.getProperty( STRING_ICON_SIZE ), ConstUI.ICON_SIZE );
  }

  public void setLineWidth( int width ) {
    properties.setProperty( STRING_LINE_WIDTH, "" + width );
  }

  public int getLineWidth() {
    return Const.toInt( properties.getProperty( STRING_LINE_WIDTH ), ConstUI.LINE_WIDTH );
  }

  public void setShadowSize( int size ) {
    properties.setProperty( STRING_SHADOW_SIZE, "" + size );
  }

  public int getShadowSize() {
    return Const.toInt( properties.getProperty( STRING_SHADOW_SIZE ), Const.SHADOW_SIZE );
  }

  public void setLastTrans( String trans ) {
    properties.setProperty( STRING_LAST_PREVIEW_TRANS, trans );
  }

  public String getLastTrans() {
    return properties.getProperty( STRING_LAST_PREVIEW_TRANS, "" );
  }

  public void setLastPreview( String[] lastpreview, int[] stepsize ) {
    properties.setProperty( STRING_LAST_PREVIEW_STEP, "" + lastpreview.length );

    for ( int i = 0; i < lastpreview.length; i++ ) {
      properties.setProperty( STRING_LAST_PREVIEW_STEP + ( i + 1 ), lastpreview[ i ] );
      properties.setProperty( STRING_LAST_PREVIEW_SIZE + ( i + 1 ), "" + stepsize[ i ] );
    }
  }

  public String[] getLastPreview() {
    String snr = properties.getProperty( STRING_LAST_PREVIEW_STEP );
    int nr = Const.toInt( snr, 0 );
    String[] lp = new String[ nr ];
    for ( int i = 0; i < nr; i++ ) {
      lp[ i ] = properties.getProperty( STRING_LAST_PREVIEW_STEP + ( i + 1 ), "" );
    }
    return lp;
  }

  public int[] getLastPreviewSize() {
    String snr = properties.getProperty( STRING_LAST_PREVIEW_STEP );
    int nr = Const.toInt( snr, 0 );
    int[] si = new int[ nr ];
    for ( int i = 0; i < nr; i++ ) {
      si[ i ] = Const.toInt( properties.getProperty( STRING_LAST_PREVIEW_SIZE + ( i + 1 ), "" ), 0 );
    }
    return si;
  }

  public FontData getDefaultFontData() {
    if ( Const.isRunningOnWebspoonMode() ) {
      return Display.getCurrent().getSystemFont().getFontData()[ 0 ];
    } else {
      return display.getSystemFont().getFontData()[0];
    }
  }

  public void setMaxUndo( int max ) {
    properties.setProperty( STRING_MAX_UNDO, "" + max );
  }

  public int getMaxUndo() {
    return Const.toInt( properties.getProperty( STRING_MAX_UNDO ), Const.MAX_UNDO );
  }

  public void setMiddlePct( int pct ) {
    properties.setProperty( STRING_MIDDLE_PCT, "" + pct );
  }

  public int getMiddlePct() {
    return Const.toInt( properties.getProperty( STRING_MIDDLE_PCT ), Const.MIDDLE_PCT );
  }

  public void setScreen( WindowProperty winprop ) {
    screens.put( winprop.getName(), winprop );
  }

  public WindowProperty getScreen( String windowname ) {
    if ( windowname == null ) {
      return null;
    }
    return screens.get( windowname );
  }

  public void setSashWeights( int[] w ) {
    properties.setProperty( STRING_SASH_W1, "" + w[ 0 ] );
    properties.setProperty( STRING_SASH_W2, "" + w[ 1 ] );
  }

  public int[] getSashWeights() {
    int w1 = Const.toInt( properties.getProperty( STRING_SASH_W1 ), 25 );
    int w2 = Const.toInt( properties.getProperty( STRING_SASH_W2 ), 75 );

    return new int[] { w1, w2 };
  }

  public void setOpenLastFile( boolean open ) {
    properties.setProperty( STRING_OPEN_LAST_FILE, open ? YES : NO );
  }

  public boolean openLastFile() {
    String open = properties.getProperty( STRING_OPEN_LAST_FILE );
    return !NO.equalsIgnoreCase( open );
  }

  public void setAutoSave( boolean autosave ) {
    properties.setProperty( STRING_AUTO_SAVE, autosave ? YES : NO );
  }

  public boolean getAutoSave() {
    String autosave = properties.getProperty( STRING_AUTO_SAVE );
    return YES.equalsIgnoreCase( autosave ); // Default = OFF
  }

  public void setSaveConfirmation( boolean saveconf ) {
    properties.setProperty( STRING_SAVE_CONF, saveconf ? YES : NO );
  }

  public boolean getSaveConfirmation() {
    String saveconf = properties.getProperty( STRING_SAVE_CONF );
    return YES.equalsIgnoreCase( saveconf ); // Default = OFF
  }

  public void setAutoSplit( boolean autosplit ) {
    properties.setProperty( STRING_AUTO_SPLIT, autosplit ? YES : NO );
  }

  public boolean getAutoSplit() {
    String autosplit = properties.getProperty( STRING_AUTO_SPLIT );
    return YES.equalsIgnoreCase( autosplit ); // Default = OFF
  }

  public void setAutoCollapseCoreObjectsTree( boolean autoCollapse ) {
    properties.setProperty( STRING_AUTO_COLLAPSE_CORE_TREE, autoCollapse ? YES : NO );
  }

  public boolean getAutoCollapseCoreObjectsTree() {
    String autoCollapse = properties.getProperty( STRING_AUTO_COLLAPSE_CORE_TREE );
    return YES.equalsIgnoreCase( autoCollapse ); // Default = OFF
  }

  public boolean showRepositoriesDialogAtStartup() {
    String show = properties.getProperty( STRING_START_SHOW_REPOSITORIES, NO );
    return YES.equalsIgnoreCase( show ); // Default: show warning before tool exit.
  }

  public void setExitWarningShown( boolean show ) {
    properties.setProperty( STRING_SHOW_EXIT_WARNING, show ? YES : NO );
  }

  public boolean isAntiAliasingEnabled() {
    String anti = properties.getProperty( STRING_ANTI_ALIASING, YES );
    return YES.equalsIgnoreCase( anti ); // Default: don't do anti-aliasing
  }

  public void setAntiAliasingEnabled( boolean anti ) {
    properties.setProperty( STRING_ANTI_ALIASING, anti ? YES : NO );
  }

  public boolean isShowCanvasGridEnabled() {
    String showCanvas = properties.getProperty( STRING_SHOW_CANVAS_GRID, NO );
    return YES.equalsIgnoreCase( showCanvas ); // Default: don't show canvas grid
  }

  public void setShowCanvasGridEnabled( boolean anti ) {
    properties.setProperty( STRING_SHOW_CANVAS_GRID, anti ? YES : NO );
  }

  public boolean showExitWarning() {
    String show = properties.getProperty( STRING_SHOW_EXIT_WARNING, YES );
    return YES.equalsIgnoreCase( show ); // Default: show repositories dialog at startup
  }

  public void setRepositoriesDialogAtStartupShown( boolean show ) {
    properties.setProperty( STRING_START_SHOW_REPOSITORIES, show ? YES : NO );
  }

  public boolean isOSLookShown() {
    String show = properties.getProperty( STRING_SHOW_OS_LOOK, NO );
    return YES.equalsIgnoreCase( show ); // Default: don't show gray dialog boxes, show Kettle look.
  }

  public void setOSLookShown( boolean show ) {
    properties.setProperty( STRING_SHOW_OS_LOOK, show ? YES : NO );
  }

  public static void setGCFont( GC gc, Device device, FontData fontData ) {
    if ( Const.getOS().startsWith( "Windows" ) ) {
      Font font = new Font( device, fontData );
      gc.setFont( font );
      font.dispose();
    } else {
      gc.setFont( device.getSystemFont() );
    }
  }

  public void setLook( Control widget ) {
    setLook( widget, WIDGET_STYLE_DEFAULT );
    if ( widget instanceof Composite ) {
      for ( Control child : ( (Composite) widget ).getChildren() ) {
        setLook( child );
      }
    }
  }

  public void setLook( final Control control, int style ) {
    if ( this.isOSLookShown() && style != WIDGET_STYLE_FIXED ) {
      return;
    }

    final GUIResource gui = GUIResource.getInstance();
    Font font = null;
    Color background = null;

    switch ( style ) {
      case WIDGET_STYLE_DEFAULT:
        background = gui.getColorBackground();
        if ( control instanceof Group && OS.indexOf( "mac" ) > -1 ) {
          control.addPaintListener( new PaintListener() {
            @Override
            public void paintControl( PaintEvent paintEvent ) {
              paintEvent.gc.setBackground( gui.getColorBackground() );
              paintEvent.gc.fillRectangle( 2, 0, control.getBounds().width - 8, control.getBounds().height - 20 );
            }
          } );
        }
        font = null; // GUIResource.getInstance().getFontDefault();
        break;
      case WIDGET_STYLE_FIXED:
        if ( !this.isOSLookShown() ) {
          background = gui.getColorBackground();
        }
        font = gui.getFontFixed();
        break;
      case WIDGET_STYLE_TABLE:
        background = gui.getColorBackground();
        font = null; // gui.getFontGrid();
        break;
      case WIDGET_STYLE_NOTEPAD:
        background = gui.getColorBackground();
        font = gui.getFontNote();
        break;
      case WIDGET_STYLE_GRAPH:
        background = gui.getColorBackground();
        font = gui.getFontGraph();
        break;
      case WIDGET_STYLE_TOOLBAR:
        background = GUIResource.getInstance().getColorDemoGray();
        break;
      case WIDGET_STYLE_TAB:
        background = GUIResource.getInstance().getColorWhite();
        CTabFolder tabFolder = (CTabFolder) control;
        tabFolder.setSimple( false );
        tabFolder.setBorderVisible( true );
        // need to make a copy of the tab selection background color to get around PDI-13940
        Color c = GUIResource.getInstance().getColorTab();
        Color tabColor = new Color( c.getDevice(), c.getRed(), c.getGreen(), c.getBlue() );
        tabFolder.setSelectionBackground( tabColor );
        break;
      default:
        background = gui.getColorBackground();
        font = null; // gui.getFontDefault();
        break;
    }

    if ( font != null && !font.isDisposed() ) {
      control.setFont( font );
    }

    if ( background != null && !background.isDisposed() ) {
      if ( control instanceof Button ) {
        Button b = (Button) control;
        if ( ( b.getStyle() & SWT.PUSH ) != 0 ) {
          return;
        }
      }
      control.setBackground( background );
    }
  }

  public static void setTableItemLook( TableItem item, Display disp ) {
    if ( !Const.getOS().startsWith( "Windows" ) ) {
      return;
    }

    Color background = GUIResource.getInstance().getColorBackground();
    if ( background != null ) {
      item.setBackground( background );
    }
  }

  /**
   * @return Returns the display.
   */
  public static Display getDisplay() {
    if ( Const.isRunningOnWebspoonMode() ) {
      return Display.getCurrent();
    } else {
      return display;
    }
  }

  /**
   * @param d The display to set.
   */
  public static void setDisplay( Display d ) {
    display = d;
  }

  public void setDefaultPreviewSize( int size ) {
    properties.setProperty( STRING_DEFAULT_PREVIEW_SIZE, "" + size );
  }

  public int getDefaultPreviewSize() {
    return Const.toInt( properties.getProperty( STRING_DEFAULT_PREVIEW_SIZE ), 1000 );
  }

  public boolean showCopyOrDistributeWarning() {
    String show = properties.getProperty( STRING_SHOW_COPY_OR_DISTRIBUTE_WARNING, YES );
    return YES.equalsIgnoreCase( show );
  }

  public void setShowCopyOrDistributeWarning( boolean show ) {
    properties.setProperty( STRING_SHOW_COPY_OR_DISTRIBUTE_WARNING, show ? YES : NO );
  }

  public boolean showWelcomePageOnStartup() {
    String show = properties.getProperty( STRING_SHOW_WELCOME_PAGE_ON_STARTUP, YES );
    return YES.equalsIgnoreCase( show );
  }

  public void setShowWelcomePageOnStartup( boolean show ) {
    properties.setProperty( STRING_SHOW_WELCOME_PAGE_ON_STARTUP, show ? YES : NO );
  }

  public int getJobsDialogStyle() {
    String prop = properties.getProperty( "JobDialogStyle" );
    return parseStyle( prop );
  }

  public int getDialogStyle( String styleProperty ) {
    String prop = properties.getProperty( styleProperty );
    if ( Utils.isEmpty( prop ) ) {
      return SWT.NONE;
    }

    return parseStyle( prop );
  }

  private int parseStyle( String sStyle ) {
    int style = SWT.DIALOG_TRIM;
    String[] styles = sStyle.split( "," );
    for ( String style1 : styles ) {
      if ( "APPLICATION_MODAL".equals( style1 ) ) {
        style |= SWT.APPLICATION_MODAL | SWT.SHEET;
      } else if ( "RESIZE".equals( style1 ) ) {
        style |= SWT.RESIZE;
      } else if ( "MIN".equals( style1 ) ) {
        style |= SWT.MIN;
      } else if ( "MAX".equals( style1 ) ) {
        style |= SWT.MAX;
      }
    }

    return style;
  }

  public void setDialogSize( Shell shell, String styleProperty ) {
    String prop = properties.getProperty( styleProperty );
    if ( Utils.isEmpty( prop ) ) {
      return;
    }

    String[] xy = prop.split( "," );
    if ( xy.length != 2 ) {
      return;
    }

    shell.setSize( Integer.parseInt( xy[ 0 ] ), Integer.parseInt( xy[ 1 ] ) );
  }

  public boolean isBrandingActive() {
    String show = properties.getProperty( STRING_SHOW_BRANDING_GRAPHICS, NO );
    return YES.equalsIgnoreCase( show );
  }

  public void setBrandingActive( boolean active ) {
    properties.setProperty( STRING_SHOW_BRANDING_GRAPHICS, active ? YES : NO );
  }

  public boolean isOnlyActiveFileShownInTree() {
    String show = properties.getProperty( STRING_ONLY_SHOW_ACTIVE_FILE, YES );
    return YES.equalsIgnoreCase( show );
  }

  public void setOnlyActiveFileShownInTree( boolean show ) {
    properties.setProperty( STRING_ONLY_SHOW_ACTIVE_FILE, show ? YES : NO );
  }

  public boolean showToolTips() {
    return YES.equalsIgnoreCase( properties.getProperty( SHOW_TOOL_TIPS, YES ) );
  }

  public void setShowToolTips( boolean show ) {
    properties.setProperty( SHOW_TOOL_TIPS, show ? YES : NO );
  }

  public List<GUIOption<Object>> getRegisteredEditableComponents() {
    return editables;
  }

  public boolean isShowingHelpToolTips() {
    return YES.equalsIgnoreCase( properties.getProperty( SHOW_HELP_TOOL_TIPS, YES ) );
  }

  public void setShowingHelpToolTips( boolean show ) {
    properties.setProperty( SHOW_HELP_TOOL_TIPS, show ? YES : NO );
  }

  /**
   * @return the openTabFiles
   */
  public List<LastUsedFile> getOpenTabFiles() {
    return openTabFiles;
  }

  /**
   * @param openTabFiles the openTabFiles to set
   */
  public void setOpenTabFiles( List<LastUsedFile> openTabFiles ) {
    this.openTabFiles = openTabFiles;
  }

  public int getCanvasGridSize() {
    return Const.toInt( properties.getProperty( CANVAS_GRID_SIZE, "16" ), 16 );
  }

  public void setCanvasGridSize( int gridSize ) {
    properties.setProperty( CANVAS_GRID_SIZE, Integer.toString( gridSize ) );
  }

  /**
   * Gets the supported version of the requested software.
   *
   * @param property the key for the software version
   * @return an integer that represents the supported version for the software.
   */
  public int getSupportedVersion( String property ) {
    return Integer.parseInt( properties.getProperty( property ) );
  }

  /**
   * Ask if the browsing environment checks are disabled.
   *
   * @return 'true' if disabled 'false' otherwise.
   */
  public boolean isBrowserEnvironmentCheckDisabled() {
    return "Y".equalsIgnoreCase( properties.getProperty( DISABLE_BROWSER_ENVIRONMENT_CHECK, "N" ) );
  }

  public boolean isLegacyPerspectiveMode() {
    return "Y".equalsIgnoreCase( properties.getProperty( LEGACY_PERSPECTIVE_MODE, "N" ) );
  }

  public static void setLocation( GUIPositionInterface guiElement, int x, int y ) {
    if ( x < 0 ) {
      x = 0;
    }
    if ( y < 0 ) {
      y = 0;
    }
    guiElement.setLocation( calculateGridPosition( new Point( x, y ) ) );
  }

  public static Point calculateGridPosition( Point p ) {
    int gridSize = PropsUI.getInstance().getCanvasGridSize();
    if ( gridSize > 1 ) {
      // Snap to grid...
      //
      return new Point( gridSize * Math.round( p.x / gridSize ), gridSize * Math.round( p.y / gridSize ) );
    } else {
      // Normal draw
      //
      return p;
    }
  }

  public boolean isIndicateSlowTransStepsEnabled() {
    String indicate = properties.getProperty( STRING_INDICATE_SLOW_TRANS_STEPS, "Y" );
    return YES.equalsIgnoreCase( indicate );
  }

  public void setIndicateSlowTransStepsEnabled( boolean indicate ) {
    properties.setProperty( STRING_INDICATE_SLOW_TRANS_STEPS, indicate ? YES : NO );
  }

  private void resetRecentSearches() {
    if ( properties.containsKey( STRING_RECENT_SEARCHES ) ) {
      properties.remove( STRING_RECENT_SEARCHES );
    }
  }

  public void setRecentSearches( String recentSearches ) {
    properties.setProperty( STRING_RECENT_SEARCHES, recentSearches );
  }

  public String getRecentSearches() {
    return properties.getProperty( STRING_RECENT_SEARCHES );
  }
}
