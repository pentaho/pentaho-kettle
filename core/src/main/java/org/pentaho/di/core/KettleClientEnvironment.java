/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.extension.ExtensionPointPluginType;
import org.pentaho.di.core.logging.ConsoleLoggingEventListener;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LoggingPluginInterface;
import org.pentaho.di.core.logging.LoggingPluginType;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;
import org.pentaho.di.core.row.value.ValueMetaPluginType;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.i18n.BaseMessages;

/**
 * This singleton is responsible for initializing the Kettle client environment and remembering if it is initialized.
 * More specifically it loads client plugins like value meta plugins and other core Kettle functionality.
 *
 * @author matt
 *
 */
public class KettleClientEnvironment {
  /** For i18n purposes, needed by Translator2!! */
  private static Class<?> PKG = Const.class;

  private static KettleClientEnvironment instance = null;

  private static Boolean initialized;

  public enum ClientType {
    SPOON, PAN, KITCHEN, CARTE, DI_SERVER, OTHER;
    public String getID(){
      if( this != OTHER ) {
        return this.name();
      }
      return instance.clientID;
    }
  }

  private ClientType client;
  // used when type is OTHER
  private String clientID = null;


  public static synchronized void init() throws KettleException {
    init( Arrays.asList( LoggingPluginType.getInstance(),
      ValueMetaPluginType.getInstance(),
      DatabasePluginType.getInstance(),
      ExtensionPointPluginType.getInstance(),
      TwoWayPasswordEncoderPluginType.getInstance() ) );
  }

  public static synchronized void init( List<PluginTypeInterface> pluginsToLoad ) throws KettleException {
    if ( initialized != null ) {
      return;
    }

    if ( KettleClientEnvironment.instance == null ) {
      KettleClientEnvironment.instance = new KettleClientEnvironment();
    }

    createKettleHome();

    // Read the kettle.properties file before anything else
    //
    EnvUtil.environmentInit();

    // Initialize the logging back-end.
    //
    KettleLogStore.init();

    // Add console output so that folks see what's going on...
    // TODO: make this configurable...
    //
    if ( !"Y".equalsIgnoreCase( System.getProperty( Const.KETTLE_DISABLE_CONSOLE_LOGGING, "N" ) ) ) {
      KettleLogStore.getAppender().addLoggingEventListener( new ConsoleLoggingEventListener() );
    }

    // Load plugins
    //
    pluginsToLoad.forEach( PluginRegistry::addPluginType );
    PluginRegistry.init( true );

    List<PluginInterface> logginPlugins = PluginRegistry.getInstance().getPlugins( LoggingPluginType.class );
    initLogginPlugins( logginPlugins );

    String passwordEncoderPluginID = Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );

    Encr.init( passwordEncoderPluginID );

    initialized = new Boolean( true );
  }

  public static boolean isInitialized() {
    return initialized != null;
  }

  /**
   * Creates the kettle home area, which is a directory containing a default kettle.properties file
   */
  public static void createKettleHome() {

    // Try to create the directory...
    //
    String directory = Const.getKettleDirectory();
    File dir = new File( directory );
    try {
      dir.mkdirs();

      // Also create a file called kettle.properties
      //
      createDefaultKettleProperties( directory );
    } catch ( Exception e ) {
      // ignore - should likely propagate the error

    }
  }

  private static void initLogginPlugins( List<PluginInterface> logginPlugins ) throws KettlePluginException {
    for ( PluginInterface plugin : logginPlugins ) {
      LoggingPluginInterface loggingPlugin = (LoggingPluginInterface) PluginRegistry.getInstance().loadClass( plugin );
      loggingPlugin.init();
    }
  }

  /**
   * Creates the default kettle properties file, containing the standard header.
   *
   * @param directory
   *          the directory
   */
  private static void createDefaultKettleProperties( String directory ) {

    String kpFile = directory + Const.FILE_SEPARATOR + Const.KETTLE_PROPERTIES;
    File file = new File( kpFile );
    if ( !file.exists() ) {
      FileOutputStream out = null;
      try {
        out = new FileOutputStream( file );
        out.write( Const.getKettlePropertiesFileHeader().getBytes() );
      } catch ( IOException e ) {
        System.err
          .println( BaseMessages.getString(
            PKG, "Props.Log.Error.UnableToCreateDefaultKettleProperties.Message", Const.KETTLE_PROPERTIES,
            kpFile ) );
        System.err.println( e.getStackTrace() );
      } finally {
        if ( out != null ) {
          try {
            out.close();
          } catch ( IOException e ) {
            System.err.println( BaseMessages.getString(
              PKG, "Props.Log.Error.UnableToCreateDefaultKettleProperties.Message", Const.KETTLE_PROPERTIES,
              kpFile ) );
            System.err.println( e.getStackTrace() );
          }
        }
      }
    }
  }

  public void setClient( ClientType client ) {
    this.client = client;
  }

  /**
   * Set the Client ID which has significance when the ClientType == OTHER
   * @param id
   */
  public void setClientID( String id ) {
    this.clientID = id;
  }

  public ClientType getClient() {
    return this.client;
  }

  /**
   * Return this singleton. Craete it if it hasn't been.
   *
   * @return
   */
  public static KettleClientEnvironment getInstance() {

    if ( KettleClientEnvironment.instance == null ) {
      KettleClientEnvironment.instance = new KettleClientEnvironment();
    }

    return KettleClientEnvironment.instance;
  }
}
