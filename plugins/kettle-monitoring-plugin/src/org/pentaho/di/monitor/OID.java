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

/**
 * This class centralizes all SNMP Event OID's for easier maintenance
 */
public class OID {

  public static final String PENTAHO          = "1.3.6.1.3.1";
  public static final String PLATFORM         = PENTAHO + ".1";
  public static final String DATA_INTEGRATION = PENTAHO + ".2";

  // base event objects
  public static final String CARTE            = DATA_INTEGRATION + ".1";
  public static final String DATABASE         = DATA_INTEGRATION + ".2";
  public static final String JOB              = DATA_INTEGRATION + ".3";
  public static final String TRANSFORMATION   = DATA_INTEGRATION + ".4";
  public static final String STEP             = DATA_INTEGRATION + ".5";

  // carte event object attributes
  public static final String CARTE_HOSTNAME   = CARTE + ".1";
  public static final String CARTE_PORT       = CARTE + ".2";
  public static final String CARTE_STARTED    = CARTE + ".3";
  public static final String CARTE_LOG        = CARTE + ".4";

  // database event object attributes
  public static final String DATABASE_NAME            = DATABASE + ".1";
  public static final String DATABASE_HOSTNAME        = DATABASE + ".2";
  public static final String DATABASE_PORT            = DATABASE + ".3";
  public static final String DATABASE_USER            = DATABASE + ".4";
  public static final String DATABASE_PARTITION_ID    = DATABASE + ".5";
  public static final String DATABASE_CONN_NAME       = DATABASE + ".6";
  public static final String DATABASE_CONN_TYPE       = DATABASE + ".7";
  public static final String DATABASE_CONN_ACCESS     = DATABASE + ".8";
  public static final String DATABASE_CONN_GROUP      = DATABASE + ".9";
  public static final String DATABASE_NR_EXEC_COMMITS = DATABASE + ".10";
  public static final String DATABASE_LOG             = DATABASE + ".11";
  public static final String DATABASE_CONNECTED       = DATABASE + ".12";

  // job event object attributes
  public static final String JOB_NAME                  = JOB + ".1";
  public static final String JOB_FILENAME              = JOB + ".2";
  public static final String JOB_DIRECTORY             = JOB + ".3";
  public static final String JOB_REPO_ID               = JOB + ".4";
  public static final String JOB_REPO_NAME             = JOB + ".5";
  public static final String JOB_PARENT_JOB            = JOB + ".6";
  public static final String JOB_PARENT_TRANS          = JOB + ".7";
  public static final String JOB_EXEC_SERVER           = JOB + ".8";
  public static final String JOB_EXEC_USER             = JOB + ".9";
  public static final String JOB_BATCH_ID              = JOB + ".10";
  public static final String JOB_PARENT_BATCH_ID       = JOB + ".11";
  public static final String JOB_LOG_CHANNEL_ID        = JOB + ".12";
  public static final String JOB_PARENT_LOG_CHANNEL_ID = JOB + ".13";
  public static final String JOB_STATUS                = JOB + ".14";
  public static final String JOB_ERROR_COUNT           = JOB + ".15";
  public static final String JOB_RUNTIME               = JOB + ".16";
  public static final String JOB_LOG                   = JOB + ".17";
  public static final String JOB_SUCCESS               = JOB + ".18";
  public static final String JOB_ENTRY_ID              = JOB + ".19";
  public static final String JOB_ENTRY_NAME            = JOB + ".20";
  public static final String JOB_RUN_IN_PARALLEL       = JOB + ".21";

  // transformation event object attributes
  public static final String TRANSFORMATION_NAME                  = TRANSFORMATION + ".1";
  public static final String TRANSFORMATION_FILENAME              = TRANSFORMATION + ".2";
  public static final String TRANSFORMATION_DIRECTORY             = TRANSFORMATION + ".3";
  public static final String TRANSFORMATION_REPO_ID               = TRANSFORMATION + ".4";
  public static final String TRANSFORMATION_REPO_NAME             = TRANSFORMATION + ".5";
  public static final String TRANSFORMATION_PARENT_JOB            = TRANSFORMATION + ".6";
  public static final String TRANSFORMATION_PARENT_TRANS          = TRANSFORMATION + ".7";
  public static final String TRANSFORMATION_EXEC_SERVER           = TRANSFORMATION + ".8";
  public static final String TRANSFORMATION_EXEC_USER             = TRANSFORMATION + ".9";
  public static final String TRANSFORMATION_BATCH_ID              = TRANSFORMATION + ".10";
  public static final String TRANSFORMATION_PARENT_BATCH_ID       = TRANSFORMATION + ".11";
  public static final String TRANSFORMATION_LOG_CHANNEL_ID        = TRANSFORMATION + ".12";
  public static final String TRANSFORMATION_PARENT_LOG_CHANNEL_ID = TRANSFORMATION + ".13";
  public static final String TRANSFORMATION_STATUS                = TRANSFORMATION + ".14";
  public static final String TRANSFORMATION_ERROR_COUNT           = TRANSFORMATION + ".15";
  public static final String TRANSFORMATION_RUNTIME               = TRANSFORMATION + ".16";
  public static final String TRANSFORMATION_LINES_READ            = TRANSFORMATION + ".17";
  public static final String TRANSFORMATION_LINES_WRITTEN         = TRANSFORMATION + ".18";
  public static final String TRANSFORMATION_LINES_UPDATED         = TRANSFORMATION + ".19";
  public static final String TRANSFORMATION_LINES_REJECTED        = TRANSFORMATION + ".20";
  public static final String TRANSFORMATION_LINES_INPUT           = TRANSFORMATION + ".21";
  public static final String TRANSFORMATION_LINES_OUTPUT          = TRANSFORMATION + ".22";
  public static final String TRANSFORMATION_LOG                   = TRANSFORMATION + ".23";
  public static final String TRANSFORMATION_SUCCESS               = TRANSFORMATION + ".24";

  // step event object attributes
  public static final String STEP_NAME          = STEP + ".1";

}
