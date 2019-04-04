/*! ******************************************************************************
*
* Pentaho Data Integration
*
* Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.logging.log4j;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.xml.DOMConfigurator;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.KettleLoggingEvent;
import org.pentaho.di.core.logging.LoggingPlugin;
import org.pentaho.di.core.logging.LoggingPluginInterface;

import java.io.File;

@LoggingPlugin(
  id = "Log4jLogging", isSeparateClassLoaderNeeded = true )
public class Log4jLogging implements LoggingPluginInterface {

  public static final String PLUGIN_PROPERTIES_FILE = "plugins" + File.separator + "kettle5-log4j-plugin" + File.separator + "log4j.xml";

  public static final String STRING_PENTAHO_DI_LOGGER_NAME = "org.pentaho.di";

  public static final String STRING_PENTAHO_DI_CONSOLE_APPENDER = "ConsoleAppender:" + STRING_PENTAHO_DI_LOGGER_NAME;

  private Logger pentahoLogger;

  public Log4jLogging() {
  }

  @Override
  public void eventAdded( KettleLoggingEvent event ) {
    switch ( event.getLevel() ) {
      case ERROR:
        pentahoLogger.log( Level.ERROR, event.getMessage() );
        break;
      case DEBUG:
      case ROWLEVEL:
        pentahoLogger.log( Level.DEBUG, event.getMessage() );
        break;
      default:
        pentahoLogger.log( Level.INFO, event.getMessage() );
        break;
    }
  }

  @Override
  public void init() {
    pentahoLogger = createLogger( STRING_PENTAHO_DI_LOGGER_NAME );
    pentahoLogger.setAdditivity( false );
    KettleLogStore.getAppender().addLoggingEventListener( this );
  }

  public void dispose() {
    KettleLogStore.getAppender().removeLoggingEventListener( this );
  }

  private void applyLog4jConfiguration() {
    LogLog.setQuietMode( true );
    LogManager.resetConfiguration();
    LogLog.setQuietMode( false );

    /**
     * On DOMConfigurator.doConfigure() no exception is ever propagated; it's caught and its stacktrace is written to System.err.
     *
     * @link https://github.com/apache/log4j/blob/v1_2_17_rc3/src/main/java/org/apache/log4j/xml/DOMConfigurator.java#L877-L878
     *
     * When the kettle5-log4j-plugin is dropped under ~/.kettle/plugins ( which is also a valid location for classic pdi plugins )
     * we get a System.err 'FileNotFoundException' stacktrace, as this is attempting to fetch the log4j.xml under a (default path) of
     * data-integration/plugins/kettle5-log4j-plugin; but in this scenario ( again, a valid one ), kettle5-log4j-plugin is under ~/.kettle/plugins
     *
     * With the inability to catch any exception ( as none is ever propagated ), the option left is to infer the starting path of this plugin's jar;
     * - If it starts with Const.getKettleDirectory(): then we know it to have been dropped in ~/.kettle/plugins ( a.k.a. Const.getKettleDirectory() )
     * - Otherwise: fallback to default/standard location, which is under <pdi-install-dir>/</>data-integration/plugins
     */
    final String log4jPath = getPluginPath().startsWith( getKettleDirPath() )
        ? ( Const.getKettleDirectory() + File.separator + PLUGIN_PROPERTIES_FILE ) : getConfigurationFileName();

    DOMConfigurator.configure( log4jPath );
  }

  /**
   * package-local visibility for testing purposes
   * 
   */
  Logger createLogger( String loggerName ) {
    applyLog4jConfiguration();
    return Logger.getLogger( loggerName );
  }

  /**
   * package-local visibility for testing purposes
   */
  String getConfigurationFileName() {
    return PLUGIN_PROPERTIES_FILE;
  }

  private String getPluginPath() {
    return new File( getClass().getProtectionDomain().getCodeSource().getLocation().getPath() ).getPath();
  }

  private String getKettleDirPath() {
    return new File( Const.getKettleDirectory() ).getPath();
  }
}
