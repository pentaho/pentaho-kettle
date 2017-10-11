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

package org.pentaho.di.core.logging;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.metrics.MetricsSnapshotType;

public class Metrics implements MetricsInterface {

  // Database
  //
  public static Metrics METRIC_DATABASE_CONNECT_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_DATABASE_CONNECT", "Connect to database" );
  public static Metrics METRIC_DATABASE_CONNECT_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_DATABASE_CONNECT", "Connect to database" );
  public static Metrics METRIC_DATABASE_PREPARE_SQL_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_DATABASE_PREPARE_SQL", "Prepare SQL statement" );
  public static Metrics METRIC_DATABASE_PREPARE_SQL_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_DATABASE_PREPARE_SQL", "Prepare SQL statement" );
  public static Metrics METRIC_DATABASE_CREATE_SQL_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_DATABASE_CREATE_SQL", "Create SQL statement" );
  public static Metrics METRIC_DATABASE_CREATE_SQL_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_DATABASE_CREATE_SQL", "Create SQL statement" );
  public static Metrics METRIC_DATABASE_SQL_VALUES_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_DATABASE_SQL_VALUES", "Set values on perpared statement" );
  public static Metrics METRIC_DATABASE_SQL_VALUES_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_DATABASE_SQL_VALUES", "Set values on perpared statement" );
  public static Metrics METRIC_DATABASE_EXECUTE_SQL_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_DATABASE_EXECUTE_SQL", "Execute SQL statement" );
  public static Metrics METRIC_DATABASE_EXECUTE_SQL_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_DATABASE_EXECUTE_SQL", "Execute SQL statement" );
  public static Metrics METRIC_DATABASE_OPEN_QUERY_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_DATABASE_OPEN_QUERY", "Open SQL query" );
  public static Metrics METRIC_DATABASE_OPEN_QUERY_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_DATABASE_OPEN_QUERY", "Open SQL query" );
  public static Metrics METRIC_DATABASE_GET_ROW_META_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_DATABASE_GET_ROW_META", "Get row metadata" );
  public static Metrics METRIC_DATABASE_GET_ROW_META_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_DATABASE_GET_ROW_META", "Get row metadata" );
  public static Metrics METRIC_DATABASE_SET_LOOKUP_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_DATABASE_SET_LOOKUP", "Set lookup values" );
  public static Metrics METRIC_DATABASE_SET_LOOKUP_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_DATABASE_SET_LOOKUP", "Set lookup values" );
  public static Metrics METRIC_DATABASE_PREPARE_UPDATE_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_DATABASE_PREPARE_UPDATE", "Prepare update" );
  public static Metrics METRIC_DATABASE_PREPARE_UPDATE_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_DATABASE_PREPARE_UPDATE", "Prepare update" );
  public static Metrics METRIC_DATABASE_PREPARE_DELETE_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_DATABASE_PREPARE_DELETE", "Prepare delete" );
  public static Metrics METRIC_DATABASE_PREPARE_DELETE_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_DATABASE_PREPARE_DELETE", "Prepare delete" );
  public static Metrics METRIC_DATABASE_PREPARE_DBPROC_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_DATABASE_PREPARE_DBPROC", "Prepare DB procedure" );
  public static Metrics METRIC_DATABASE_PREPARE_DBPROC_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_DATABASE_PREPARE_DBPROC", "Prepare DB procedure" );
  public static Metrics METRIC_DATABASE_GET_LOOKUP_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_DATABASE_GET_LOOKUP", "Get lookup" );
  public static Metrics METRIC_DATABASE_GET_LOOKUP_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_DATABASE_GET_LOOKUP", "Get lookup" );
  public static Metrics METRIC_DATABASE_GET_DBMETA_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_DATABASE_GET_DBMETA", "Get DB metadata" );
  public static Metrics METRIC_DATABASE_GET_DBMETA_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_DATABASE_GET_DBMETA", "Get DB metadata" );

  public static Metrics METRIC_DATABASE_GET_ROW_COUNT = new Metrics(
    MetricsSnapshotType.COUNT, "METRIC_DATABASE_GET_ROW_COUNT", "Get row from DB (count)" );
  public static Metrics METRIC_DATABASE_GET_ROW_SUM_TIME = new Metrics(
    MetricsSnapshotType.SUM, "METRIC_DATABASE_GET_ROW_SUM_TIME", "Get row from DB (total time)" );
  public static Metrics METRIC_DATABASE_GET_ROW_MIN_TIME = new Metrics(
    MetricsSnapshotType.MIN, "METRIC_DATABASE_GET_ROW_MIN_TIME", "Get row from DB (min time)" );
  public static Metrics METRIC_DATABASE_GET_ROW_MAX_TIME = new Metrics(
    MetricsSnapshotType.MAX, "METRIC_DATABASE_GET_ROW_MAX_TIME", "Get row from DB (max time)" );

  // Plugin registry...
  //
  public static Metrics METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSIONS_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSIONS", "Register all plugin extensions" );
  public static Metrics METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSIONS_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSIONS", "Register all plugin extensions" );
  public static Metrics METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSION_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSION", "Register a plugin extension" );
  public static Metrics METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSION_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSION", "Register a plugin extension" );
  public static Metrics METRIC_PLUGIN_REGISTRY_PLUGIN_REGISTRATION_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_PLUGIN_REGISTRY_PLUGIN_REGISTRATION", "Register plugins" );
  public static Metrics METRIC_PLUGIN_REGISTRY_PLUGIN_REGISTRATION_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_PLUGIN_REGISTRY_PLUGIN_REGISTRATION", "Register plugins" );
  public static Metrics METRIC_PLUGIN_REGISTRY_PLUGIN_TYPE_REGISTRATION_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_PLUGIN_REGISTRY_PLUGIN_TYPE_REGISTRATION",
    "Register plugins of a certain type" );
  public static Metrics METRIC_PLUGIN_REGISTRY_PLUGIN_TYPE_REGISTRATION_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_PLUGIN_REGISTRY_PLUGIN_TYPE_REGISTRATION",
    "Register plugins of a certain type" );

  // Transformation
  //
  public static Metrics METRIC_TRANSFORMATION_EXECUTION_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_TRANSFORMATION_EXECUTION", "Execute a transformation" );
  public static Metrics METRIC_TRANSFORMATION_EXECUTION_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_TRANSFORMATION_EXECUTION", "Execute a transformation" );
  public static Metrics METRIC_TRANSFORMATION_INIT_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_TRANSFORMATION_INIT", "Initialize a transformation" );
  public static Metrics METRIC_TRANSFORMATION_INIT_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_TRANSFORMATION_INIT", "Initialize a transformation" );
  public static Metrics METRIC_STEP_EXECUTION_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_STEP_EXECUTION", "Execute a step" );
  public static Metrics METRIC_STEP_EXECUTION_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_STEP_EXECUTION", "Execute a step" );
  public static Metrics METRIC_STEP_INIT_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_STEP_INIT", "Initialize a step" );
  public static Metrics METRIC_STEP_INIT_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_STEP_INIT", "Initialize a step" );

  // Logging back-end
  //
  public static Metrics METRIC_CENTRAL_LOG_STORE_TIMEOUT_CLEAN_TIME = new Metrics(
    MetricsSnapshotType.SUM, "METRIC_CENTRAL_LOG_STORE_TIMEOUT_CLEAN_TIME",
    "Time consumed removing old log records (ms)" );
  public static Metrics METRIC_CENTRAL_LOG_STORE_TIMEOUT_CLEAN_COUNT = new Metrics(
    MetricsSnapshotType.COUNT, "METRIC_CENTRAL_LOG_STORE_TIMEOUT_CLEAN_COUNT",
    "Number of times removed old log records" );
  public static Metrics METRIC_LOGGING_REGISTRY_CLEAN_TIME = new Metrics(
    MetricsSnapshotType.SUM, "METRIC_LOGGING_REGISTRY_CLEAN_TIME",
    "Time consumed removing old log registry entries (ms)" );
  public static Metrics METRIC_LOGGING_REGISTRY_CLEAN_COUNT = new Metrics(
    MetricsSnapshotType.COUNT, "METRIC_LOGGING_REGISTRY_CLEAN_COUNT",
    "Number of times removed old log registry entries" );
  public static Metrics METRIC_LOGGING_REGISTRY_GET_CHILDREN_TIME = new Metrics(
    MetricsSnapshotType.SUM, "METRIC_LOGGING_REGISTRY_GET_CHILDREN_TIME",
    "Time consumed getting log registry children (ms)" );
  public static Metrics METRIC_LOGGING_REGISTRY_GET_CHILDREN_COUNT = new Metrics(
    MetricsSnapshotType.COUNT, "METRIC_LOGGING_REGISTRY_GET_CHILDREN_COUNT",
    "Number of times retrieved log registry children" );

  // Job
  //
  public static Metrics METRIC_JOB_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_JOB_EXECUTION", "Execute a job" );
  public static Metrics METRIC_JOB_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_JOB_EXECUTION", "Execute a job" );
  public static Metrics METRIC_JOBENTRY_START = new Metrics(
    MetricsSnapshotType.START, "METRIC_JOBENTRY_EXECUTION", "Execute a job entry" );
  public static Metrics METRIC_JOBENTRY_STOP = new Metrics(
    MetricsSnapshotType.STOP, "METRIC_JOBENTRY_EXECUTION", "Execute a job entry" );

  private String code;
  private String description;
  private MetricsSnapshotType type;

  public Metrics( MetricsSnapshotType type, String code, String description ) {
    this.type = type;
    this.code = code;
    this.description = description;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getCode() {
    return code;
  }

  @Override
  public MetricsSnapshotType getType() {
    return type;
  }

  @Override
  public boolean equals( Object obj ) {
    if ( !( obj instanceof MetricsInterface ) ) {
      return false;
    }
    if ( this == obj ) {
      return true;
    }

    return ( (MetricsInterface) obj ).getCode().equalsIgnoreCase( code );
  }

  public static List<MetricsInterface> getDefaultMetrics() {
    List<MetricsInterface> metrics = new ArrayList<MetricsInterface>();

    for ( Field field : Metrics.class.getDeclaredFields() ) {
      if ( field.getType().equals( Metrics.class ) && field.getName().startsWith( "METRIC_" ) ) {
        field.setAccessible( true );
        try {
          metrics.add( (MetricsInterface) field.get( null ) );
        } catch ( Exception e ) {
          e.printStackTrace(); // it either works or doesn't, seems more like a JRE problem if it doesn't.
        }
      }
    }

    return metrics;
  }
}
