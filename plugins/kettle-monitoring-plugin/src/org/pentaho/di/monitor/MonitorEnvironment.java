/*!
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU Lesser General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*
* Copyright (c) 2002-2014 Pentaho Corporation..  All rights reserved.
*/
package org.pentaho.di.monitor;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.monitor.carte.CarteSubscriber;
import org.pentaho.di.monitor.database.DatabaseSubscriber;
import org.pentaho.di.monitor.job.JobSubscriber;
import org.pentaho.di.monitor.step.StepSubscriber;
import org.pentaho.di.monitor.trans.TransformationSubscriber;
import org.pentaho.platform.api.monitoring.IMonitoringService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MonitorEnvironment {

  private Logger logger = LoggerFactory.getLogger( MonitorEnvironment.class );

  // plugin name ( read: plugin name & plugin's folder name )
  public static final String MONITORING_PLUGIN_FOLDER_NAME = "kettle-monitoring-plugin"; //$NON-NLS-1$

  public static MonitorEnvironment instance;

  private String pluginBaseDir;

  private MonitorEnvironment() throws KettleException {

    initializeEnvironment();

    logger.info( "Initializing MonitorEnvironment... is EventBus ready ? " + isEventBusReady() );

    if ( isEventBusReady() ) {
      registerEventHandlers();
    }
  }

  public static MonitorEnvironment getInstance() throws KettleException {
    if ( instance == null ) {
      instance = new MonitorEnvironment();
    }
    return instance;
  }

  public void initializeEnvironment() throws KettleException {

    try {

      pluginBaseDir = findPluginBaseDir();

    } catch ( Exception e ) {
      throw new KettleException( e );

    }
  }

  /**
   * if eventbus is ready, proceed with registering all subscribers
   *
   * @throws KettleException
   */
  public void registerEventHandlers() throws KettleException {

    logger.info( "registering CarteSubscriber to EventBus" );
    getEventBus().register( new CarteSubscriber() );

    logger.info( "registering DatabaseSubscriber to EventBus" );
    getEventBus().register( new DatabaseSubscriber() );

    logger.info( "registering JobSubscriber to EventBus" );
    getEventBus().register( new JobSubscriber() );

    logger.info( "registering TransformationSubscriber to EventBus" );
    getEventBus().register( new TransformationSubscriber() );

    logger.info( "registering StepSubscriber to EventBus" );
    getEventBus().register( new StepSubscriber() );

    //TODO register all kettle subscribers
  }

  public String getPluginBaseDir() {
    return pluginBaseDir;
  }

  public boolean isEventBusReady() {
    return PentahoSystem.getInitializedOK()
      && PentahoSystem.getObjectFactory().objectDefined( IMonitoringService.class );
  }

  public IMonitoringService getEventBus() {
    return PentahoSystem.get( IMonitoringService.class );
  }

  private String findPluginBaseDir() {

    List<PluginInterface> plugins = PluginRegistry.getInstance().getPlugins( ExtensionPointPluginType.class );

    if ( plugins != null ) {

      for ( PluginInterface plugin : plugins ) {

        if ( plugin != null && plugin.getPluginDirectory() != null && plugin.getPluginDirectory().getPath() != null ) {

          String path = plugin.getPluginDirectory().getPath();

          if ( path.startsWith( Const.FILE_SEPARATOR + Const.FILE_SEPARATOR ) ) {
            path = path.replaceFirst( Const.FILE_SEPARATOR + Const.FILE_SEPARATOR, StringUtils.EMPTY );
          }

          if ( path.endsWith( Const.FILE_SEPARATOR ) ) {
            path = path.substring( 0, path.length() - 1 );
          }

          if ( path.endsWith( MONITORING_PLUGIN_FOLDER_NAME ) ) {
            return path;
          }
        }
      }
    }

    return null;
  }
}
