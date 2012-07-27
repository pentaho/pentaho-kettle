package org.pentaho.di.core.logging;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.metrics.MetricsSnapshotType;

public class Metrics implements MetricsInterface {
  
  // Database
  //
  public static Metrics METRIC_DATABASE_CONNECT_START = new Metrics(MetricsSnapshotType.START, "METRIC_DATABASE_CONNECT", "Connect to database");
  public static Metrics METRIC_DATABASE_CONNECT_STOP = new Metrics(MetricsSnapshotType.STOP, "METRIC_DATABASE_CONNECT", "Connect to database");
  public static Metrics METRIC_DATABASE_PREPARE_SQL_START = new Metrics(MetricsSnapshotType.START, "METRIC_DATABASE_PREPARE_SQL","Prepare SQL statement");
  public static Metrics METRIC_DATABASE_PREPARE_SQL_STOP = new Metrics(MetricsSnapshotType.STOP, "METRIC_DATABASE_PREPARE_SQL","Prepare SQL statement");
  public static Metrics METRIC_DATABASE_CREATE_SQL_START = new Metrics(MetricsSnapshotType.START, "METRIC_DATABASE_CREATE_SQL", "Create SQL statement");
  public static Metrics METRIC_DATABASE_CREATE_SQL_STOP = new Metrics(MetricsSnapshotType.STOP, "METRIC_DATABASE_CREATE_SQL", "Create SQL statement");
  public static Metrics METRIC_DATABASE_SQL_VALUES_START = new Metrics(MetricsSnapshotType.START, "METRIC_DATABASE_SQL_VALUES", "Set values on perpared statement");
  public static Metrics METRIC_DATABASE_SQL_VALUES_STOP = new Metrics(MetricsSnapshotType.STOP, "METRIC_DATABASE_SQL_VALUES", "Set values on perpared statement");
  public static Metrics METRIC_DATABASE_EXECUTE_SQL_START = new Metrics(MetricsSnapshotType.START, "METRIC_DATABASE_EXECUTE_SQL", "Execute SQL statement");
  public static Metrics METRIC_DATABASE_EXECUTE_SQL_STOP = new Metrics(MetricsSnapshotType.STOP, "METRIC_DATABASE_EXECUTE_SQL", "Execute SQL statement");
  public static Metrics METRIC_DATABASE_OPEN_QUERY_START = new Metrics(MetricsSnapshotType.START, "METRIC_DATABASE_OPEN_QUERY", "Open SQL query");
  public static Metrics METRIC_DATABASE_OPEN_QUERY_STOP = new Metrics(MetricsSnapshotType.STOP, "METRIC_DATABASE_OPEN_QUERY", "Open SQL query");
  public static Metrics METRIC_DATABASE_GET_ROW_META_START = new Metrics(MetricsSnapshotType.START, "METRIC_DATABASE_GET_ROW_META", "Get row metadata");
  public static Metrics METRIC_DATABASE_GET_ROW_META_STOP = new Metrics(MetricsSnapshotType.STOP, "METRIC_DATABASE_GET_ROW_META", "Get row metadata");
  public static Metrics METRIC_DATABASE_SET_LOOKUP_START = new Metrics(MetricsSnapshotType.START, "METRIC_DATABASE_SET_LOOKUP", "Set lookup values");
  public static Metrics METRIC_DATABASE_SET_LOOKUP_STOP = new Metrics(MetricsSnapshotType.STOP, "METRIC_DATABASE_SET_LOOKUP", "Set lookup values");
  public static Metrics METRIC_DATABASE_PREPARE_UPDATE_START = new Metrics(MetricsSnapshotType.START, "METRIC_DATABASE_PREPARE_UPDATE", "Prepare update");
  public static Metrics METRIC_DATABASE_PREPARE_UPDATE_STOP = new Metrics(MetricsSnapshotType.STOP, "METRIC_DATABASE_PREPARE_UPDATE", "Prepare update");
  public static Metrics METRIC_DATABASE_PREPARE_DELETE_START = new Metrics(MetricsSnapshotType.START, "METRIC_DATABASE_PREPARE_DELETE", "Prepare delete");
  public static Metrics METRIC_DATABASE_PREPARE_DELETE_STOP = new Metrics(MetricsSnapshotType.STOP, "METRIC_DATABASE_PREPARE_DELETE", "Prepare delete");
  public static Metrics METRIC_DATABASE_PREPARE_DBPROC_START = new Metrics(MetricsSnapshotType.START, "METRIC_DATABASE_PREPARE_DBPROC", "Prepare DB procedure");
  public static Metrics METRIC_DATABASE_PREPARE_DBPROC_STOP = new Metrics(MetricsSnapshotType.STOP, "METRIC_DATABASE_PREPARE_DBPROC", "Prepare DB procedure");
  public static Metrics METRIC_DATABASE_GET_LOOKUP_START = new Metrics(MetricsSnapshotType.START, "METRIC_DATABASE_GET_LOOKUP", "Get lookup");
  public static Metrics METRIC_DATABASE_GET_LOOKUP_STOP = new Metrics(MetricsSnapshotType.STOP, "METRIC_DATABASE_GET_LOOKUP", "Get lookup");
  public static Metrics METRIC_DATABASE_GET_DBMETA_START = new Metrics(MetricsSnapshotType.START, "METRIC_DATABASE_GET_DBMETA", "Get DB metadata");
  public static Metrics METRIC_DATABASE_GET_DBMETA_STOP = new Metrics(MetricsSnapshotType.STOP, "METRIC_DATABASE_GET_DBMETA", "Get DB metadata");
  public static Metrics METRIC_DATABASE_GET_ROW_START = new Metrics(MetricsSnapshotType.START, "METRIC_DATABASE_GET_ROW", "Get row from DB");
  public static Metrics METRIC_DATABASE_GET_ROW_STOP = new Metrics(MetricsSnapshotType.STOP, "METRIC_DATABASE_GET_ROW", "Get row from DB");
  
  // Plugin registry...
  //
  public static Metrics METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSIONS_START = new Metrics(MetricsSnapshotType.START, "METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSIONS", "Register all plugin extensions");
  public static Metrics METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSIONS_STOP = new Metrics(MetricsSnapshotType.STOP, "METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSIONS", "Register all plugin extensions");
  public static Metrics METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSION_START = new Metrics(MetricsSnapshotType.START, "METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSION", "Register a plugin extension");
  public static Metrics METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSION_STOP = new Metrics(MetricsSnapshotType.STOP, "METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSION", "Register a plugin extension");
  public static Metrics METRIC_PLUGIN_REGISTRY_PLUGIN_REGISTRATION_START = new Metrics(MetricsSnapshotType.START, "METRIC_PLUGIN_REGISTRY_PLUGIN_REGISTRATION", "Register plugins");
  public static Metrics METRIC_PLUGIN_REGISTRY_PLUGIN_REGISTRATION_STOP = new Metrics(MetricsSnapshotType.STOP, "METRIC_PLUGIN_REGISTRY_PLUGIN_REGISTRATION", "Register plugins");
  public static Metrics METRIC_PLUGIN_REGISTRY_PLUGIN_TYPE_REGISTRATION_START = new Metrics(MetricsSnapshotType.START, "METRIC_PLUGIN_REGISTRY_PLUGIN_TYPE_REGISTRATION", "Register plugins of a certain type");
  public static Metrics METRIC_PLUGIN_REGISTRY_PLUGIN_TYPE_REGISTRATION_STOP = new Metrics(MetricsSnapshotType.STOP, "METRIC_PLUGIN_REGISTRY_PLUGIN_TYPE_REGISTRATION", "Register plugins of a certain type");

  // Transformation
  //
  public static Metrics METRIC_TRANSFORMATION_EXECUTION_START = new Metrics(MetricsSnapshotType.START, "METRIC_TRANSFORMATION_EXECUTION", "Execute a transformation");
  public static Metrics METRIC_TRANSFORMATION_EXECUTION_STOP = new Metrics(MetricsSnapshotType.STOP, "METRIC_TRANSFORMATION_EXECUTION", "Execute a transformation");
  public static Metrics METRIC_TRANSFORMATION_INIT_START = new Metrics(MetricsSnapshotType.START, "METRIC_TRANSFORMATION_INIT", "Initialize a transformation");
  public static Metrics METRIC_TRANSFORMATION_INIT_STOP = new Metrics(MetricsSnapshotType.STOP, "METRIC_TRANSFORMATION_INIT", "Initialize a transformation");
  public static Metrics METRIC_STEP_EXECUTION_START = new Metrics(MetricsSnapshotType.START, "METRIC_STEP_EXECUTION", "Execute a step");
  public static Metrics METRIC_STEP_EXECUTION_STOP = new Metrics(MetricsSnapshotType.STOP, "METRIC_STEP_EXECUTION", "Execute a step");
  public static Metrics METRIC_STEP_INIT_START= new Metrics(MetricsSnapshotType.START, "METRIC_STEP_INIT", "Initialize a step");
  public static Metrics METRIC_STEP_INIT_STOP= new Metrics(MetricsSnapshotType.STOP, "METRIC_STEP_INIT", "Initialize a step");

  
  private String code;
  private String description;
  private MetricsSnapshotType type;
  
  public Metrics(MetricsSnapshotType type, String code, String description) {
    this.type = type;
    this.code = code;
    this.description = description;
  }
  
  public String getDescription() {
    return description;
  }
  
  public String getCode() {
    return code;
  }
  
  public MetricsSnapshotType getType() {
    return type;
  }
  
  public static List<MetricsInterface> getDefaultMetrics() {
    List<MetricsInterface> metrics = new ArrayList<MetricsInterface>();
    
    for (Field field : Metrics.class.getDeclaredFields()) {
      if (field.getType().equals(Metrics.class) && field.getName().startsWith("METRIC_")) {
        field.setAccessible(true);
        try {
          metrics.add( (MetricsInterface) field.get(null) );
        } catch(Exception e) {
          e.printStackTrace(); // it either works or doesn't, seems more like a JRE problem if it doesn't.
        }
      }
    }
    
    return metrics;
  }
}
