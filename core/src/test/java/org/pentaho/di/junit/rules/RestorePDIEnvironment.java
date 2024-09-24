/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.junit.rules;

import org.apache.commons.io.FileUtils;
import org.junit.rules.ExternalResource;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.extension.ExtensionPointMap;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelFactory;
import org.pentaho.di.core.logging.MetricsRegistry;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.timestamp.SimpleTimestampFormat;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLHandlerCache;
import org.pentaho.di.i18n.LanguageChoice;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

public class RestorePDIEnvironment extends ExternalResource {
  private Properties originalProperties;
  private Locale originalLocale;
  private Locale originalFormatLocale;
  private TimeZone originalTimezone;
  private Path tmpKettleHome;

  /**
   * Creates a {@code RestorePDIEnvironment} rule that restores all system properties and resets any PDI related
   * environment instances.
   */
  public RestorePDIEnvironment() { }

  void defaultInit() throws Throwable {
    // make sure static class initializers are correctly initialized
    // re-init
    cleanUp();
    KettleClientEnvironment.init();

    // initialize some classes, this will fail if some tests init this classes before any PDI init()
    // the best thing to do is to invoke this ClassRule in every test
    Class.forName( Database.class.getName() );
    Class.forName( Timestamp.class.getName() );
    Class.forName( ValueMetaBase.class.getName() );
    Class.forName( SimpleTimestampFormat.class.getName() );
    Class.forName( SimpleDateFormat.class.getName() );
    Class.forName( XMLHandler.class.getName() );
    Class.forName( LogChannel.class.getName() );
    DatabaseMeta.init();
    ExtensionPointMap.getInstance().reInitialize();
    KettleVFS.getInstance().reset(); // reinit
  }

  void cleanUp() {
    KettleClientEnvironment.reset();
    PluginRegistry.getInstance().reset();
    MetricsRegistry.getInstance().reset();
    ExtensionPointMap.getInstance().reset();
    if ( KettleLogStore.isInitialized() ) {
      KettleLogStore.getInstance().reset();
    }
    KettleLogStore.setLogChannelInterfaceFactory( new LogChannelFactory() );
    if ( Props.isInitialized() ) {
      Props.getInstance().reset();
    }
    KettleVFS.getInstance().reset();
    XMLHandlerCache.getInstance().clear();
    ValueMetaFactory.pluginRegistry = PluginRegistry.getInstance();
    // under no circumstance reset the LoggingRegistry
//    LoggingRegistry.getInstance().reset();
  }

  @Override protected void before() throws Throwable {
    originalProperties = System.getProperties();
    System.setProperties( copyOf( originalProperties ) );

    originalLocale = Locale.getDefault();
    originalFormatLocale = Locale.getDefault( Locale.Category.FORMAT );
    originalTimezone = TimeZone.getDefault();
    TimeZone.setDefault( TimeZone.getTimeZone( "UTC" ) );
    Locale.setDefault( Locale.US );
    Locale.setDefault( Locale.Category.FORMAT, Locale.US );
    LanguageChoice.getInstance().setDefaultLocale( Locale.US );

    tmpKettleHome = Files.createTempDirectory( Long.toString( System.nanoTime() ) );
    System.setProperty( "file.encoding", "UTF-8" );
    System.setProperty( "user.timezone", "UTC" );
    System.setProperty( "KETTLE_HOME", tmpKettleHome.toString() );
    System.setProperty( Const.KETTLE_DISABLE_CONSOLE_LOGGING, "Y" );
    System.clearProperty( Const.VFS_USER_DIR_IS_ROOT );
    System.clearProperty( Const.KETTLE_LENIENT_STRING_TO_NUMBER_CONVERSION );
    System.clearProperty( Const.KETTLE_COMPATIBILITY_DB_IGNORE_TIMEZONE );
    System.clearProperty( Const.KETTLE_DEFAULT_INTEGER_FORMAT );
    System.clearProperty( Const.KETTLE_DEFAULT_NUMBER_FORMAT );
    System.clearProperty( Const.KETTLE_DEFAULT_BIGNUMBER_FORMAT );
    System.clearProperty( Const.KETTLE_DEFAULT_DATE_FORMAT );
    System.clearProperty( Const.KETTLE_DEFAULT_TIMESTAMP_FORMAT );
    System.clearProperty( Const.KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL );

    defaultInit();
  }

  private Properties copyOf( Properties originalProperties ) {
    Properties copy = new Properties();
    copy.putAll( originalProperties );
    return copy;
  }

  @Override protected void after() {
    cleanUp();

    System.setProperties( originalProperties );
    Locale.setDefault( originalLocale );
    Locale.setDefault( Locale.Category.FORMAT, originalFormatLocale );
    LanguageChoice.getInstance().setDefaultLocale( originalLocale );
    TimeZone.setDefault( originalTimezone );
    FileUtils.deleteQuietly( tmpKettleHome.toFile() );
  }

}
