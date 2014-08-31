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
package org.pentaho.di.monitor.trans;

import com.google.common.eventbus.Subscribe;
import org.pentaho.di.monitor.base.IKettleMonitoringSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformationSubscriber implements IKettleMonitoringSubscriber {

  private Logger logger = LoggerFactory.getLogger( TransformationSubscriber.class );

  @Override
  public String getSubscriberId() {
    return getClass().getName();
  }

  @Subscribe
  public void handleEvent( TransformationEvent event ) {

    if ( event == null ) {
      return;
    }

    logger.info( "[Event Bus Subscriber] just received " + event.toString() );

    // TODO event 2 SNMP task
  }
}
