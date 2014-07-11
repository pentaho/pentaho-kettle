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

public abstract class MonitorAbstract {

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
   * @see http://wiki.pentaho.com/display/EAI/PDI+Extension+Point+Plugins
   * <p/>
   */
  public void callExtensionPoint( LogChannelInterface logChannelInterface, Object o ) throws KettleException {

    this.logChannelInterface = logChannelInterface; // kettle's provided log channel

    getLog().logDebug( getClass().getName() + ".callExtensionPoint() triggered" );

    try {

      if ( MonitorEnvironment.getInstance().isEventBusReady() ) {

        MonitorEnvironment.getInstance().getEventBus().post( toKettleEvent( o ) ); // async event bus

      } else {
        getLog().logBasic( "Monitoring event bus not available; discarding " + getClass().getSimpleName() + " event" );
      }

    } catch ( Throwable t ) {
      getLog().logError( getClass().getName(), t );
      throw new KettleException( t );
    }
  }

  protected LogChannelInterface getLog() throws KettleException {
    return logChannelInterface;
  }
}
