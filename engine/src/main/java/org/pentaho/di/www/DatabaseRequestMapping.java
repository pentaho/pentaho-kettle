/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.www;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.database.AzureSqlDataBaseMeta;
import org.pentaho.di.core.database.DatabricksDatabaseMeta;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.GenericDatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.database.MSSQLServerDatabaseMeta;
import org.pentaho.di.core.database.MSSQLServerNativeDatabaseMeta;
import org.pentaho.di.core.database.OracleDatabaseMeta;
import org.pentaho.di.core.database.PartitionDatabaseMeta;
import org.pentaho.di.core.database.RedshiftDatabaseMeta;
import org.pentaho.di.core.database.SAPDBDatabaseMeta;
import org.pentaho.di.core.database.SnowflakeHVDatabaseMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static org.pentaho.di.core.database.AzureSqlDataBaseMeta.CLIENT_ID;
import static org.pentaho.di.core.database.AzureSqlDataBaseMeta.CLIENT_SECRET_KEY;
import static org.pentaho.di.core.database.AzureSqlDataBaseMeta.IS_ALWAYS_ENCRYPTION_ENABLED;
import static org.pentaho.di.core.database.AzureSqlDataBaseMeta.JDBC_AUTH_METHOD;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.IAM_ACCESS_KEY_ID;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.IAM_SECRET_ACCESS_KEY;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.IAM_SESSION_TOKEN;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.IAM_PROFILE_NAME;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.AUTHENTICATION_METHOD;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.IAM_ROLE;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.AWS_ACCESS_KEY;
import static org.pentaho.di.core.database.RedshiftDatabaseMeta.AWS_ACCESS_KEY_ID;
import static org.pentaho.di.core.database.SnowflakeHVDatabaseMeta.WAREHOUSE;

public class DatabaseRequestMapping {
  private static final String EXTRA_OPTION = "EXTRA_OPTION_";
  private static final String POOLING = "POOLING_";
  private DatabaseRequestMapping() { }

  public static DatabaseMeta fetchDatabaseMeta( JSONObject dbConnectionJson ) throws KettleException {
    String databaseType = setStringValueFromJson( dbConnectionJson, "databaseType" );
    if ( StringUtils.isBlank( databaseType ) ) {
      throw new KettleException( "Please specify database type" );
    }

    String accessType = setStringValueFromJson( dbConnectionJson, "accessType" );
    if ( StringUtils.isBlank( accessType ) ) {
      throw new KettleException( "Please specify access type" );
    }

    String connectionName = setStringValueFromJson( dbConnectionJson, "connectionName" );
    String databaseName = setStringValueFromJson( dbConnectionJson, "databaseName" );
    String hostName = setStringValueFromJson( dbConnectionJson, "hostName" );
    String port = setStringValueFromJson( dbConnectionJson, "databasePort" );
    String username = setStringValueFromJson( dbConnectionJson, "username" );
    String password = setStringValueFromJson( dbConnectionJson, "password" );

    DatabaseMeta databaseMeta = new DatabaseMeta( connectionName, databaseType, accessType, hostName,
            databaseName, port, username, password );

    //set advanced section values
    JSONArray attributes = (JSONArray) dbConnectionJson.get( "attributeDTOs" );
    if ( Objects.isNull( attributes ) ) {
      return databaseMeta;
    }

    Map<String, String> attributesMap = new HashMap<>();
    for ( Object object : attributes ) {
      JSONObject jsonObject = (JSONObject) object;
      String code = (String) jsonObject.get( "code" );
      String attribute = (String) jsonObject.get( "attribute" );
      attributesMap.put( code, attribute );
    }

    databaseMeta.setStreamingResults( setBooleanValue( attributesMap, "STREAM_RESULTS" ) );

    databaseMeta.setDataTablespace( setStringValueFromJson( dbConnectionJson, "dataTablespace" ) );
    databaseMeta.setIndexTablespace( setStringValueFromJson( dbConnectionJson, "indexTablespace" ) );

    databaseMeta.setSupportsBooleanDataType( setBooleanValue( attributesMap, "SUPPORTS_BOOLEAN_DATA_TYPE" ) );
    databaseMeta.setSupportsTimestampDataType( setBooleanValue( attributesMap, "SUPPORTS_TIMESTAMP_DATA_TYPE" ) );
    databaseMeta.setQuoteAllFields( setBooleanValue( attributesMap, "QUOTE_ALL_FIELDS" ) );
    databaseMeta.setForcingIdentifiersToLowerCase(
            setBooleanValue( attributesMap, "FORCE_IDENTIFIERS_TO_LOWERCASE" ) );
    databaseMeta.setForcingIdentifiersToUpperCase(
            setBooleanValue( attributesMap, "FORCE_IDENTIFIERS_TO_UPPERCASE" ) );
    databaseMeta.setPreserveReservedCase( setBooleanValue( attributesMap, "PRESERVE_RESERVED_WORD_CASE" ) );
    databaseMeta.setPreferredSchemaName( setStringValue( attributesMap, "PREFERRED_SCHEMA_NAME" ) );
    databaseMeta.setConnectSQL( setStringValue( attributesMap, "SQL_CONNECT" ) );

    if ( databaseMeta.getDatabaseInterface() instanceof OracleDatabaseMeta ) {
      //Oracle Database specific settings
      ((OracleDatabaseMeta) databaseMeta.getDatabaseInterface()).setStrictBigNumberInterpretation(
          setBooleanValue( attributesMap, "strictNumberInterpretation" ) );
    } else if ( databaseMeta.getDatabaseInterface() instanceof MSSQLServerDatabaseMeta ) {
      // SQL Server double decimal separator
      databaseMeta.setUsingDoubleDecimalAsSchemaTableSeparator(
              setBooleanValue( attributesMap, "MSSQL_DOUBLE_DECIMAL_SEPARATOR" ) );
    } else if ( databaseMeta.getDatabaseInterface() instanceof SAPDBDatabaseMeta ) {
      // SAP settings
      databaseMeta.getAttributes().put( "SAPLanguage", setStringValue( attributesMap, "SAPLanguage" ) );
      databaseMeta.getAttributes().put( "SAPSystemNumber", setStringValue( attributesMap, "SAPSystemNumber" ) );
      databaseMeta.getAttributes().put( "SAPClient", setStringValue( attributesMap, "SAPClient" ) );
    } else if ( databaseMeta.getDatabaseInterface() instanceof SnowflakeHVDatabaseMeta ) {
      // Snowflake settings
      databaseMeta.getAttributes().put( WAREHOUSE, setStringValue( attributesMap, WAREHOUSE ) );
    } else if ( databaseMeta.getDatabaseInterface() instanceof GenericDatabaseMeta ) {
      // Generic settings
      databaseMeta.getAttributes().put(
              GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL,
              setStringValue( attributesMap, GenericDatabaseMeta.ATRRIBUTE_CUSTOM_URL ) );

      databaseMeta.getAttributes().put(
              GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS,
              setStringValue( attributesMap, GenericDatabaseMeta.ATRRIBUTE_CUSTOM_DRIVER_CLASS ) );
    } else if ( databaseMeta.getDatabaseInterface() instanceof MSSQLServerNativeDatabaseMeta ) {
      // Microsoft SQL Server Use Integrated Security
      databaseMeta.getAttributes().put( MSSQLServerNativeDatabaseMeta.ATTRIBUTE_USE_INTEGRATED_SECURITY,
              setStringValue( attributesMap, MSSQLServerNativeDatabaseMeta.ATTRIBUTE_USE_INTEGRATED_SECURITY ) );
    } else if ( databaseMeta.getDatabaseInterface() instanceof AzureSqlDataBaseMeta ) {
      // Azure Database settings
      databaseMeta.getAttributes().put( JDBC_AUTH_METHOD, setStringValue( attributesMap, JDBC_AUTH_METHOD ) );
      databaseMeta.getAttributes().put( CLIENT_ID, setStringValue( attributesMap, CLIENT_ID ) );
      databaseMeta.getAttributes().put( CLIENT_SECRET_KEY, setStringValue( attributesMap, CLIENT_SECRET_KEY ) );
      databaseMeta.getAttributes().put( IS_ALWAYS_ENCRYPTION_ENABLED,
              setStringValue( attributesMap, IS_ALWAYS_ENCRYPTION_ENABLED ) );
    } else if ( databaseMeta.getDatabaseInterface() instanceof DatabricksDatabaseMeta ) {
      // Databricks settings
      databaseMeta.getAttributes().put( "HTTP_PATH", setStringValue( attributesMap, "HTTP_PATH" ) );
      databaseMeta.getAttributes().put( "ACCESS_TOKEN", setStringValue( attributesMap, "ACCESS_TOKEN" ) );
      databaseMeta.getAttributes().put( "AUTH_METHOD", setStringValue( attributesMap, "AUTH_METHOD" ) );
    } else if ( databaseMeta.getDatabaseInterface() instanceof RedshiftDatabaseMeta ) {
      // Redshift Settings
      databaseMeta.getAttributes().put( JDBC_AUTH_METHOD, setStringValue( attributesMap, JDBC_AUTH_METHOD ) );
      databaseMeta.getAttributes().put( IAM_ACCESS_KEY_ID, setStringValue( attributesMap, IAM_ACCESS_KEY_ID ) );
      databaseMeta.getAttributes().put( IAM_SECRET_ACCESS_KEY, setStringValue( attributesMap, IAM_SECRET_ACCESS_KEY ) );
      databaseMeta.getAttributes().put( IAM_SESSION_TOKEN, setStringValue( attributesMap, IAM_SESSION_TOKEN ) );
      databaseMeta.getAttributes().put( IAM_PROFILE_NAME, setStringValue( attributesMap, IAM_PROFILE_NAME ) );
      databaseMeta.getAttributes().put( AUTHENTICATION_METHOD, setStringValue( attributesMap, AUTHENTICATION_METHOD ) );
      databaseMeta.getAttributes().put( IAM_ROLE, setStringValue( attributesMap, IAM_ROLE ) );
      databaseMeta.getAttributes().put( AWS_ACCESS_KEY, setStringValue( attributesMap, AWS_ACCESS_KEY ) );
      databaseMeta.getAttributes().put( AWS_ACCESS_KEY_ID, setStringValue( attributesMap, AWS_ACCESS_KEY_ID ) );
    }

    // set connection option parameters
    attributesMap.keySet().forEach( key -> {
      if ( key.startsWith( EXTRA_OPTION ) ) {
        databaseMeta.addExtraOption( databaseType, key.substring( key.indexOf( "." ) + 1 ),
                setStringValue( attributesMap, key ) );
      }
    } );

    // set connection pooling properties
    boolean isConnectionPoolingUsed = setBooleanValue( attributesMap, "USE_POOLING" );
    if ( isConnectionPoolingUsed ) {
      databaseMeta.setUsingConnectionPool( true );
      databaseMeta.setInitialPoolSize( setIntegerValue( attributesMap, "INITIAL_POOL_SIZE" ) );
      databaseMeta.setMaximumPoolSize( setIntegerValue( attributesMap, "MAXIMUM_POOL_SIZE" ) );

      Properties properties = new Properties();
      attributesMap.keySet().forEach( key -> {
        if ( key.startsWith( POOLING ) ) {
          properties.put( key.replace( POOLING, "" ), setStringValue( attributesMap, key ) );
        }
      } );

      databaseMeta.setConnectionPoolingProperties( properties );
    }

    //set clustering properties
    boolean isPartitioned = setBooleanValue( attributesMap, "IS_CLUSTERED" );
    databaseMeta.setPartitioned( isPartitioned );

    if ( isPartitioned ) {
      int noOfPartitions = setIntegerValueFromJson( dbConnectionJson, "noOfPartitions" );
      List<PartitionDatabaseMeta> partitionList = new ArrayList<>();
      setPartitioningInformation( attributesMap, partitionList, noOfPartitions );

      PartitionDatabaseMeta[] partitionDatabaseMetaArray = new PartitionDatabaseMeta[partitionList.size()];
      partitionList.toArray( partitionDatabaseMetaArray );
      databaseMeta.getDatabaseInterface().setPartitioningInformation( partitionDatabaseMetaArray );
    }

    return databaseMeta;
  }

  private static void setPartitioningInformation( Map<String, String> attributesMap,
                                                  List<PartitionDatabaseMeta> partitionList, int noOfPartitions ) {
    for ( int i = 0; i < noOfPartitions; i++ ) {
      PartitionDatabaseMeta partitionDatabaseMeta = new PartitionDatabaseMeta();
      partitionDatabaseMeta.setPartitionId( setStringValue( attributesMap, "CLUSTER_PARTITION_" + i ) );
      partitionDatabaseMeta.setDatabaseName( setStringValue( attributesMap, "CLUSTER_DBNAME_" + i ) );
      partitionDatabaseMeta.setHostname( setStringValue( attributesMap, "CLUSTER_HOSTNAME_" + i ) );
      partitionDatabaseMeta.setUsername( setStringValue( attributesMap, "CLUSTER_USERNAME_" + i ) );
      partitionDatabaseMeta.setPassword( setStringValue( attributesMap, "CLUSTER_PASSWORD_" + i ) );
      partitionDatabaseMeta.setPort( setStringValue( attributesMap, "CLUSTER_PORT_" + i ) );
      partitionList.add( partitionDatabaseMeta );
    }
  }

  private static Boolean setBooleanValue( Map<String, String> map, String key ) {
    if ( Objects.nonNull(  key ) ) {
      String value = map.get( key );
      return Boolean.parseBoolean( value );
    }

    return false;
  }

  private static String setStringValueFromJson( JSONObject jsonObject, String key ) {
    if ( Objects.nonNull( jsonObject.get( key ) ) ) {
      return Objects.toString( jsonObject.get( key ) );
    }

    return StringUtils.EMPTY;
  }

  private static String setStringValue( Map<String, String> map, String key ) {
    if ( Objects.nonNull( map.get( key ) ) ) {
      return Objects.toString( map.get( key ) );
    }

    return StringUtils.EMPTY;
  }

  private static Integer setIntegerValueFromJson( JSONObject jsonObject, String key ) {
    if ( Objects.nonNull( jsonObject.get( key ) ) ) {
      String value = Objects.toString( jsonObject.get( key ) );
      try {
        return Integer.parseInt( value );
      } catch ( NumberFormatException nfe ) {
        return 0;
      }
    }

    return 0;
  }

  private static Integer setIntegerValue( Map<String, String> map, String key ) {
    if ( Objects.nonNull( map.get( key ) ) ) {
      String value = Objects.toString( map.get( key ) );
      try {
        return Integer.parseInt( value );
      } catch ( NumberFormatException nfe ) {
        return 0;
      }
    }

    return 0;
  }
}
