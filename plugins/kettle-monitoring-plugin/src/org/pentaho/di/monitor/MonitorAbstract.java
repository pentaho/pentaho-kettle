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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.monitor.base.IKettleMonitoringEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MonitorAbstract {

  private Logger logger = LoggerFactory.getLogger( getClass() );

  // kettle's provided log channel
  private LogChannelInterface logChannelInterface;

  /**
   * implementing classes use this to transfer data from their specific objects to an IKettleEvent
   *
   * @param o implementing class specific object
   * @return IKettleEvent event object
   */
  public abstract IKettleMonitoringEvent toKettleEvent( Object o ) throws KettleException;

  /**
   * This method is the entrance point to a specific event handling. The 'specific event' triggering is defined via the
   * 'extensionPointId' attribute of each classe's ExtensionPoint annotation.
   * <p/>
   * Each implementing class of this abstract then does its own specific event handling, if it wishes so.
   * <p/>
   * For detailed information regarding anotation declaration check <br/> http://wiki.pentaho
   * .com/display/EAI/PDI+Extension+Point+Plugins
   * <p/>
   *
   * @param logChannelInterface spoon's standard log interface channel
   * @param o                   one of Job/JobMeta/Transformation/TransMeta/... object
   * @throws KettleException if something goes wrong
   * @link http://wiki.pentaho.com/display/EAI/PDI+Extension+Point+Plugins
   * <p/>
   */
  public void callExtensionPoint( LogChannelInterface logChannelInterface, Object o ) throws KettleException {

    this.logChannelInterface = logChannelInterface; // kettle's provided log channel

    try {

      if ( MonitorEnvironment.getInstance().isEventBusReady() ) {

        IKettleMonitoringEvent e = toKettleEvent( o );

        if ( e != null ) {

          MonitorEnvironment.getInstance().getEventBus().post( e ); // async event bus

        } else {
          logInfo( "Discarding null monitoringEvent returned from " + getClass().getSimpleName() );
        }
      } else {
        logInfo( "Event bus not available; discarding " + getClass().getSimpleName() + " event" );
      }

    } catch ( Throwable t ) {
      logError( getClass().getName(), t );
      throw new KettleException( t );
    }
  }

  protected void logDebug( String message ) throws KettleException {
    logger.debug( message );
    getLogChannelInterface().logDebug( message );
  }

  protected void logInfo( String message ) throws KettleException {
    logger.info( message );
    getLogChannelInterface().logBasic( message );
  }

  protected void logWarn( String message ) throws KettleException {
    logger.warn( message );
    getLogChannelInterface().logBasic( message );
  }

  protected void logError( String message , Throwable t ) throws KettleException {
    logger.error( message , t );
    getLogChannelInterface().logError( message , t );
  }

  protected LogChannelInterface getLogChannelInterface() {
    return logChannelInterface;
  }
}
