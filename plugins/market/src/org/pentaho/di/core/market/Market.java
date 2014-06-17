/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.market;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.market.entry.MarketEntry;
import org.pentaho.di.core.market.entry.MarketEntryType;
import org.pentaho.di.core.plugins.KettleURLClassLoader;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonLifecycleListener;
import org.pentaho.di.ui.spoon.SpoonPerspective;
import org.pentaho.di.ui.spoon.SpoonPlugin;
import org.pentaho.di.ui.spoon.SpoonPluginCategories;
import org.pentaho.di.ui.spoon.SpoonPluginInterface;
import org.pentaho.di.ui.spoon.dialog.MarketplaceController;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.xul.swt.tab.TabSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

@SpoonPlugin( id = "market", image = "" )
@SpoonPluginCategories( { "spoon" } )
public class Market implements SpoonPluginInterface {

  private static Class<?> PKG = Market.class;

  /**
   * Determines the location of the marketplaces file
   * 
   * @return the name of the marketplaces file
   */
  public static final String getMarketplacesFile() {
    return Const.getKettleDirectory() + Const.FILE_SEPARATOR + "marketplaces.xml";
  }

  /**
   * Applies the XUL overlay specified in the loadOverlay call.
   */
  @Override
  public void applyToContainer( String category, XulDomContainer container ) throws XulException {
    if ( category.equals( "spoon" ) ) {
      container.registerClassLoader( getClass().getClassLoader() );
      container.loadOverlay( "org/pentaho/di/core/market/spoon_overlays.xul" );
      container.addEventHandler( new MarketplaceController() );
    }
  }

  @Override
  public SpoonLifecycleListener getLifecycleListener() {
    return null;
  }

  @Override
  public SpoonPerspective getPerspective() {
    return null;
  }

  /**
   * Find the plugin object related to a pluginId.
   * 
   * @param pluginId
   *          id of plugin
   * @return plugin object
   */
  private static PluginInterface getPluginObject( String pluginId ) {
    for ( Class<? extends PluginTypeInterface> pluginType : PluginRegistry.getInstance().getPluginTypes() ) {
      if ( PluginRegistry.getInstance().findPluginWithId( pluginType, pluginId ) != null ) {
        return PluginRegistry.getInstance().findPluginWithId( pluginType, pluginId );
      }
    }
    return null;
  }

  /**
   * This method determines the installed version of a plugin. If the plugin doesn't define a version correctly, it
   * returns "Unknown". This method makes the assumption that the plugin id also equals the plugin folder name.
   * 
   * @param pluginId
   *          the plugin id related to the version.
   */
  public static String discoverInstalledVersion( MarketEntry marketEntry ) {

    String pluginFolder = buildPluginsFolderPath( marketEntry ) + File.separator + marketEntry.getId();
    File pluginFolderFile = new File( pluginFolder );
    if ( pluginFolderFile.exists() ) {
      marketEntry.setInstalled( true );

      String versionPath = pluginFolder + File.separator + "version.xml";
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      FileReader reader = null;
      try {
        File file = new File( versionPath );
        if ( !file.exists() ) {
          return "Unknown";
        }
        DocumentBuilder db = dbf.newDocumentBuilder();
        reader = new FileReader( versionPath );
        Document dom = db.parse( new InputSource( reader ) );
        NodeList versionElements = dom.getElementsByTagName( "version" );
        if ( versionElements.getLength() >= 1 ) {
          Element versionElement = (Element) versionElements.item( 0 );

          marketEntry.setInstalledBuildId( versionElement.getAttribute( "buildId" ) );
          marketEntry.setInstalledBranch( versionElement.getAttribute( "branch" ) );
          marketEntry.setInstalledVersion( versionElement.getTextContent() );

          return versionElement.getTextContent();
        }

      } catch ( Exception e ) {
        e.printStackTrace();
      } finally {
        try {
          if ( reader != null ) {
            reader.close();
          }
        } catch ( Exception e ) {
          e.printStackTrace();
        }
      }

    } else {
      marketEntry.setInstalled( false );
    }
    return "Unknown";
  }

  /**
   * Builds and returns the path to the plugins folder.
   * 
   * @param marketEntry
   * @return String the path to the plugins folder.
   */
  public static String buildPluginsFolderPath( final MarketEntry marketEntry ) {
    PluginInterface plugin = getPluginObject( marketEntry.getId() );
    if ( plugin != null && plugin.getPluginDirectory() != null ) {
      return new File( plugin.getPluginDirectory().getFile() ).getParent();
    } else {
      String subfolder = getInstallationSubfolder( marketEntry );

      // Use current directory (should be the Kettle distribution directory) as the root folder to install plugins
      // This is because plugin types are not guaranteed to search the ~/.kettle folder for plugins.
      return "plugins" + ( subfolder == null ? "" : Const.FILE_SEPARATOR + subfolder );
    }
  }

  /**
   * Installs or uninstalls the MarketEntry.
   * 
   * @param marketEntry
   *          The market entry to install/uninstall.
   * @param isInstalled
   *          Boolean indicating if the plugin is installed. If so then it is uninstalled. Otherwise it is installed.
   * @throws KettleException
   */
  public static void installUninstall( final MarketEntry marketEntry, boolean isInstalled,
      ProgressMonitorDialog monitorDialog ) throws KettleException {
    if ( isInstalled ) {
      Market.uninstall( marketEntry, monitorDialog, true );
    } else {
      Market.install( marketEntry, monitorDialog );
    }
  }

  /**
   * Installs the passed MarketEntry into the folder built by buildPluginsFolderPath(marketEntry).
   * 
   * A warning dialog box is displayed if the plugin folder already exists. If the user chooses to install then the
   * plugin folder is deleted and then recreated.
   * 
   * @param marketEntry
   * @throws KettleException
   */
  public static void install( final MarketEntry marketEntry, ProgressMonitorDialog monitorDialog )
    throws KettleException {
    String parentFolderName = buildPluginsFolderPath( marketEntry );

    // Until plugin dependencies are implemented, check that the pentaho-big-data-plugin directory exists
    // before installing anything of type HadoopShim
    if ( marketEntry.getType().equals( MarketEntryType.HadoopShim ) ) {

      File bdPluginFolder = new File( parentFolderName ).getParentFile();
      if ( bdPluginFolder == null || !bdPluginFolder.exists() ) {
        throw new KettleException( BaseMessages.getString( PKG, "Marketplaces.Dialog.PluginNotInstalled.message",
            "Pentaho Big Data Plugin" )
            + ". You must install the Pentaho Big Data plugin before you can install a Hadoop Shim" );
      }
    }

    File pluginFolder = new File( parentFolderName + File.separator + marketEntry.getId() );
    LogChannel.GENERAL.logBasic( "Installing plugin in folder: " + pluginFolder.getAbsolutePath() );
    if ( pluginFolder.exists() ) {
      monitorDialog.close();
      MessageBox mb = new MessageBox( Spoon.getInstance().getShell(), SWT.NO | SWT.YES | SWT.ICON_WARNING );
      mb.setMessage( BaseMessages.getString( PKG, "Marketplace.Dialog.PromptOverwritePlugin.Message", pluginFolder
          .getAbsolutePath() ) );
      mb.setText( BaseMessages.getString( PKG, "Marketplace.Dialog.PromptOverwritePlugin.Title" ) );
      int answer = SWT.NO;
      answer = mb.open();
      if ( answer == SWT.YES ) {
        monitorDialog.open();
        ClassLoader cl = PluginRegistry.getInstance().getClassLoader( getPluginObject( marketEntry.getId() ) );
        if ( cl instanceof KettleURLClassLoader ) {
          ( (KettleURLClassLoader) cl ).closeClassLoader();
        }
        deleteDirectory( pluginFolder );
        unzipMarketEntry( parentFolderName, marketEntry.getPackageUrl() );
        if ( Market.discoverInstalledVersion( marketEntry ).equalsIgnoreCase( "unknown" ) ) {
          createVersionXML( marketEntry );
        }
        refreshSpoon( monitorDialog );
      }
    } else {
      unzipMarketEntry( parentFolderName, marketEntry.getPackageUrl() );
      if ( Market.discoverInstalledVersion( marketEntry ).equalsIgnoreCase( "unknown" ) ) {
        createVersionXML( marketEntry );
      }
      refreshSpoon( monitorDialog );
    }
  }

  private static void createVersionXML( MarketEntry marketEntry ) throws KettleException {
    String pluginFolder = buildPluginsFolderPath( marketEntry ) + File.separator + marketEntry.getId();
    String versionPath = pluginFolder + File.separator + "version.xml";
    File parentFolder = new File( pluginFolder );
    File file = new File( versionPath );
    if ( file != null ) {
      BufferedWriter bufferedWriter = null;
      try {
        if ( !parentFolder.exists() ) {
          parentFolder.mkdirs();
        }

        FileWriter fw = new FileWriter( file.getAbsoluteFile() );
        bufferedWriter = new BufferedWriter( fw );
        bufferedWriter.write( "<version>" + marketEntry.getVersion() + "</version>" );
      } catch ( IOException ioe ) {
        throw new KettleException( ioe );
      } finally {
        if ( bufferedWriter != null ) {
          try {
            bufferedWriter.close();
          } catch ( IOException ioe ) {
            throw new KettleException( ioe );
          }
        }
      }
    }
  }

  /**
   * Unzips the plugin to the file system The passed MarkeyEntry has the URL of the zip file.
   * 
   * @param folderName
   * @param marketEntry
   * @throws KettleException
   */
  private static void unzipMarketEntry( String folderName, String packageUrl ) throws KettleException {

    // Copy the file locally first
    //
    File tmpFile = null;
    InputStream inputStream = null;
    ZipInputStream zis = null;

    try {
      tmpFile = File.createTempFile( "plugin", ".zip" );
      org.apache.commons.io.FileUtils.copyURLToFile( new URL( packageUrl ), tmpFile );

      // Read the package, extract in folder
      //
      inputStream = new FileInputStream( tmpFile );
      zis = new ZipInputStream( inputStream );
      ZipEntry zipEntry = null;
      try {
        zipEntry = zis.getNextEntry();
      } catch ( IOException ioe ) {
        throw new KettleException( ioe );
      }
      byte[] buffer = new byte[1024];
      int bytesRead = 0;
      FileOutputStream fos = null;

      while ( zipEntry != null ) {
        try {
          File file = new File( folderName + File.separator + zipEntry.getName() );

          if ( zipEntry.isDirectory() ) {
            file.mkdirs();
          } else {
            file.getParentFile().mkdirs();

            fos = new FileOutputStream( file );
            while ( ( bytesRead = zis.read( buffer ) ) != -1 ) {
              fos.write( buffer, 0, bytesRead );
            }
          }

          zipEntry = zis.getNextEntry();
        } catch ( FileNotFoundException fnfe ) {
          throw new KettleException( fnfe );
        } catch ( IOException ioe ) {
          throw new KettleException( ioe );
        } finally {
          if ( fos != null ) {
            try {
              fos.close();
            } catch ( IOException e ) {
              // Ignore.
            }
          }
        }
      }
    } catch ( IOException e ) {
      throw new KettleException( "Unable to unzip file " + packageUrl, e );
    } finally {
      if ( zis != null ) {
        tmpFile.delete();
        try {
          zis.close();
        } catch ( Exception e ) {
          throw new KettleException( "Unable to close zip file stream (corrupt file?) of file " + tmpFile, e );
        }
      }
    }

  }

  /**
   * Uninstalls the passed MarketEntry.
   * 
   * @param marketEntry
   * @throws KettleException
   */
  public static void uninstall( final MarketEntry marketEntry, final ProgressMonitorDialog monitorDialog,
      boolean refresh ) throws KettleException {

    String parentFolderName = buildPluginsFolderPath( marketEntry );
    File pluginFolder = new File( parentFolderName + File.separator + marketEntry.getId() );
    LogChannel.GENERAL.logBasic( "Uninstalling plugin in folder: " + pluginFolder.getAbsolutePath() );

    if ( !pluginFolder.exists() ) {
      throw new KettleException( "No plugin was found in the expected folder : " + pluginFolder.getAbsolutePath() );
    }

    try {
      for ( PluginInterface plugin : PluginRegistry.getInstance().findPluginsByFolder( pluginFolder.toURI().toURL() ) ) {
        // unload plugin
        ClassLoader cl = PluginRegistry.getInstance().getClassLoader( plugin );
        if ( cl instanceof KettleURLClassLoader ) {
          ( (KettleURLClassLoader) cl ).closeClassLoader();
        }

        // remove plugin from registry
        PluginRegistry.getInstance().removePlugin( plugin.getPluginType(), plugin );
      }
    } catch ( MalformedURLException e1 ) {
      LogChannel.GENERAL.logError( e1.getLocalizedMessage(), e1 );
    }

    // delete plugin folder
    deleteDirectory( pluginFolder );

    if ( refresh ) {
      if ( !Display.getDefault().getThread().equals( Thread.currentThread() ) ) {
        Spoon.getInstance().getDisplay().asyncExec( new Runnable() {
          public void run() {
            try {
              refreshSpoon( monitorDialog );
            } catch ( KettleException e ) {
              e.printStackTrace();
            }
          }
        } );
      } else {
        refreshSpoon( monitorDialog );
      }
    }
  }

  public static void uninstallMarketInSeparateClassLoader( final File path, final ProgressMonitorDialog monitorDialog )
    throws Exception {
    try {
      Spoon.getInstance().getMainSpoonContainer().removeOverlay( "org/pentaho/di/core/market/spoon_overlays.xul" );
      uninstall( new MarketEntry( "market", MarketEntryType.Mixed ), monitorDialog, false );
      Spoon.getInstance().getDisplay().asyncExec( new Runnable() {
        public void run() {
          try {
            refreshSpoon( monitorDialog );
          } catch ( Exception e ) {
            e.printStackTrace();
          } finally {
            if ( Market.class.getClassLoader() instanceof KettleURLClassLoader ) {
              ( (KettleURLClassLoader) Market.class.getClassLoader() ).closeClassLoader();
            }
            path.delete();
          }
        }
      } );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  public static void upgradeMarketInSeparateClassLoader( final File path, final String packageUrl,
      final ProgressMonitorDialog monitorDialog ) throws Exception {
    try {

      PluginInterface plugin = getPluginObject( "market" );
      if ( plugin == null ) {
        throw new KettleException( "No Plugin!" );
      }
      File pluginFolder = new File( plugin.getPluginDirectory().getFile() );
      String parentFolderName = pluginFolder.getParent();

      // unload plugin
      ClassLoader cl = PluginRegistry.getInstance().getClassLoader( plugin );
      if ( cl instanceof KettleURLClassLoader ) {
        ( (KettleURLClassLoader) cl ).closeClassLoader();
      }
      // remove plugin
      deleteDirectory( pluginFolder );

      // install new version
      unzipMarketEntry( parentFolderName, packageUrl );

      Spoon.getInstance().getDisplay().asyncExec( new Runnable() {
        public void run() {
          try {
            // refresh plugins
            refreshSpoon( monitorDialog );

            MessageBox box = new MessageBox( Spoon.getInstance().getShell(), SWT.ICON_WARNING | SWT.OK );
            box.setText( BaseMessages.getString( PKG, "MarketplacesDialog.RestartUpdate.Title" ) );
            box.setMessage( BaseMessages.getString( PKG, "MarketplacesDialog.RestartUpdate.Message" ) );
            box.open();

          } catch ( KettleException e ) {
            e.printStackTrace();
          } finally {
            if ( Market.class.getClassLoader() instanceof KettleURLClassLoader ) {
              ( (KettleURLClassLoader) Market.class.getClassLoader() ).closeClassLoader();
            }
            path.delete();
          }
        }
      } );

    } catch ( Exception e ) {
      e.printStackTrace();

      // clean up if there was an exception
      if ( Market.class.getClassLoader() instanceof KettleURLClassLoader ) {
        ( (KettleURLClassLoader) Market.class.getClassLoader() ).closeClassLoader();
      }
      path.delete();
    }
  }

  public static void upgradeMarket( final MarketEntry entry ) throws KettleException {
    try {
      PluginInterface plugin = getPluginObject( "market" );
      if ( plugin == null ) {
        throw new KettleException( "No Plugin!" );
      }

      String pluginFolderName = plugin.getPluginDirectory().getFile();
      File folder = new File( pluginFolderName );

      // do these two steps from the Dialog
      // prompt user if they are sure they want to uninstall (optional)
      // exit out of marketplace UI (do this from the UI)

      // find marketplace jar
      File[] files = folder.listFiles();
      File jar = null;
      for ( File f : files ) {
        if ( f.getName().endsWith( ".jar" ) ) {
          jar = f;
          break;
        }
      }
      // load a tmp jar

      final File finalJar = jar;

      Thread t = new Thread() {
        public void run() {
          KettleURLClassLoader classloader = null;
          try {
            File tmpJar = File.createTempFile( "kettle_marketplace_tmp", ".jar" );
            FileInputStream fis = new FileInputStream( finalJar );
            FileOutputStream fos = new FileOutputStream( tmpJar );
            IOUtils.copy( fis, fos );
            fis.close();
            fos.close();
            classloader =
                new KettleURLClassLoader( new URL[] { tmpJar.toURI().toURL() }, Spoon.getInstance().getClass()
                    .getClassLoader() );
            Class<?> clazz = classloader.loadClass( "org.pentaho.di.core.market.Market" );
            // remove the plugin, unload when done.
            Method m = clazz.getMethod( "upgradeMarketInSeparateClassLoader", File.class, String.class );
            m.invoke( null, tmpJar, entry.getPackageUrl() );
          } catch ( Throwable t ) {
            t.printStackTrace();
          } finally {
            if ( classloader != null ) {
              classloader.closeClassLoader();
            }
          }
        }
      };
      t.start();

    } catch ( Throwable t ) {
      t.printStackTrace();
      throw new KettleException( t );
    }
  }

  public static void uninstallMarket() throws KettleException {
    try {
      PluginInterface plugin = getPluginObject( "market" );
      if ( plugin == null ) {
        throw new KettleException( "No Plugin!" );
      }

      String pluginFolderName = plugin.getPluginDirectory().getFile();
      File folder = new File( pluginFolderName );

      // do these two steps from the Dialog
      // prompt user if they are sure they want to uninstall (optional)
      // exit out of marketplace UI (do this from the UI)

      // find marketplace jar
      File[] files = folder.listFiles();
      File jar = null;
      for ( File f : files ) {
        if ( f.getName().endsWith( ".jar" ) ) {
          jar = f;
          break;
        }
      }
      // load a tmp jar

      final File finalJar = jar;

      Thread t = new Thread() {
        KettleURLClassLoader classloader = null;

        public void run() {
          try {
            File tmpJar = File.createTempFile( "kettle_marketplace_tmp", ".jar" );
            FileInputStream fis = new FileInputStream( finalJar );
            FileOutputStream fos = new FileOutputStream( tmpJar );
            IOUtils.copy( fis, fos );
            fis.close();
            fos.close();

            // The classloader will be closed by the invoked method
            //
            @SuppressWarnings( "resource" )
            KettleURLClassLoader classloader =
                new KettleURLClassLoader( new URL[] { tmpJar.toURI().toURL() }, Spoon.getInstance().getClass()
                    .getClassLoader() );
            Class<?> clazz = classloader.loadClass( "org.pentaho.di.core.market.Market" );
            // remove the plugin, unload when done.
            Method m =
                clazz.getMethod( "uninstallMarketInSeparateClassLoader", File.class, ProgressMonitorDialog.class );
            m.invoke( null, tmpJar, null );
          } catch ( Throwable t ) {
            t.printStackTrace();
          } finally {
            if ( classloader != null ) {
              classloader.closeClassLoader();
            }
          }
        }
      };
      t.start();

    } catch ( Throwable t ) {
      t.printStackTrace();
      throw new KettleException( t );
    }
  }

  /**
   * Refreshes Spoons plugin registry, core objects and some UI things.
   * 
   * @throws KettleException
   */
  private static void refreshSpoon( ProgressMonitorDialog monitorDialog ) throws KettleException {

    if ( monitorDialog != null ) {
      monitorDialog.close();
    }

    MessageBox box = new MessageBox( Spoon.getInstance().getShell(), SWT.ICON_QUESTION | SWT.OK );
    box.setText( BaseMessages.getString( PKG, "MarketplacesDialog.RestartUpdate.Title" ) );
    box.setMessage( BaseMessages.getString( PKG, "MarketplacesDialog.RestartUpdate.Message" ) );
    box.open();

    DatabaseMeta.clearDatabaseInterfacesMap();
    PluginRegistry.init();
    Spoon spoon = Spoon.getInstance();

    // Close all Execution Results panes to avoid SWT disposal issues during refresh
    int numTabs = spoon.delegates.tabs.getTabs().size();
    TabSet tabSet = spoon.getTabSet();
    int selectedIndex = tabSet.getSelectedIndex();
    for ( int i = numTabs - 1; i >= 0; i-- ) {
      if ( i != selectedIndex ) {
        tabSet.setSelected( i );
        if ( spoon.isExecutionResultsPaneVisible() ) {
          spoon.showExecutionResults();
        }
      }
    }
    tabSet.setSelected( selectedIndex );
    if ( spoon.isExecutionResultsPaneVisible() ) {
      spoon.showExecutionResults();
    }

    // Refresh Spoon objects and UI components
    spoon.refreshCoreObjects();
    spoon.refreshTree();
    spoon.refreshGraph();
    spoon.enableMenus();
    GUIResource.getInstance().reload();
    spoon.selectionFilter.setText( spoon.selectionFilter.getText() );
  }

  /**
   * Returns the folder name for the MarketEntries type.
   * 
   * @param marketEntry
   * @return
   */
  public static String getInstallationSubfolder( MarketEntry marketEntry ) {
    String subfolder = null;
    switch ( marketEntry.getType() ) {
      case Step:
        subfolder = "steps";
        break;
      case JobEntry:
        subfolder = "jobentries";
        break;
      case Partitioner:
        subfolder = "steps";
        break;
      case SpoonPlugin:
        subfolder = "spoon";
        break;
      case Database:
        subfolder = "databases";
        break;
      case Repository:
        subfolder = "repositories";
        break;
      case HadoopShim:
        subfolder = "pentaho-big-data-plugin" + File.separator + "hadoop-configurations";
        break;
      case Mixed:
      case General:
        subfolder = "";
        break;

      default:
        subfolder = null;
    }
    return subfolder;
  }

  /**
   * This is a copy of method. That method works fine if the plugin is used in the same version it is built from. When
   * the plugin was dropped into PDI 4.2.1 then an invocation target exception was thrown when invoking
   * JarfileGenerator.deleteDirectory().
   * 
   * I placed the method here even though the cause of the exception is not that obvious. The
   * JarfileGenerator.deleteDirectory method has not changed since 4.2.1.
   * 
   * @param dir
   */
  private static void deleteDirectory( File dir ) throws KettleException {
    if ( dir != null ) {
      File[] files = dir.listFiles();
      if ( files != null ) {
        for ( int i = 0; i < files.length; i++ ) {
          if ( files[i].isDirectory() ) {
            deleteDirectory( files[i] );
          } else if ( !files[i].delete() ) {
            throw new KettleException( "Failed to delete " + files[i] );
          }
        }
      }
      if ( !dir.delete() ) {
        throw new KettleException( "Failed to delete directory " + dir );
      }
    }
  }
}
