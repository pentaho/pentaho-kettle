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

import java.text.SimpleDateFormat;

public class Constants {

  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "YYYY MMM dd HH:mm:ss:SSS zzz" );

  // plugin name ( read: plugin name & plugin's folder name )
  public static final String MONITORING_PLUGIN_FOLDER_NAME = "kettle-monitoring-plugin"; //$NON-NLS-1$

  // the properties file
  public static final String MONITORING_PROPERTIES_FILE = "monitoring.properties";

  // log message transportation level key
  public static final String LOG_MESSSAGE_TRANSPORTATION_LEVEL_KEY = "log.message.transportation";

  // maximum amount of log entries transportation key
  public static final String MAX_LOG_ENTRIES_TRANSPORTATION_KEY = "max.log.entries.transportation";

  // default value for log message transportation level
  public static final String DEFAULT_LOG_MESSSAGE_TRANSPORTATION_LEVEL = "ERROR";

  // default amount of log entries transportation
  public static final String DEFAULT_LOG_ENTRIES_TRANSPORTATION = "1";

}
