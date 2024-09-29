/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.avro;

import java.util.Arrays;

public class AvroSpec {
  @SuppressWarnings( "squid:S1192" ) // string constants
  public enum DataType {
    NULL( 0, true, "null", null, "Null" ),
    BOOLEAN( 1, true, "boolean", null, "Boolean" ),
    INTEGER( 2, true, "int", null, "Integer" ),
    LONG( 3, true, "long", null, "Long" ),
    FLOAT( 4, true, "float", null, "Float" ),
    DOUBLE( 5, true, "double", null, "Double" ),
    BYTES( 6, true, "bytes", null, "Bytes" ),
    STRING( 7, true, "string", null, "String" ),
    RECORD( 8, false, "record", null, "Record" ),
    ENUM( 9, false, "enum", null, "Enum" ),
    ARRAY( 10, false, "array", null, "Array" ),
    MAP( 11, false, "map", null, "Map" ),
    FIXED( 12, false, "fixed", null, "Fixed" ),
    DECIMAL( 13, false, "bytes", "decimal", "Decimal" ),
    DATE( 14, false, "int", "date", "Date" ),
    TIME_MILLIS( 15, false, "int", "time-millis", "Time" ),
    TIME_MICROS( 16, false, "long", "time-micros", "Time In Microseconds" ),
    TIMESTAMP_MILLIS( 17, false, "long", "timestamp-millis", "Timestamp" ),
    TIMESTAMP_MICROS( 18, false, "long", "timestamp-micros", "Timestamp In Microseconds" ),
    DURATION( 19, false, "fixed", "duration", "Duration" ),
    DECIMAL_FIXED( 20, false, "fixed", "decimal", "Decimal Fixed" );

    private final int id;
    private final boolean isPrimitive;
    private final String baseType;
    private final String logicalType;
    private final String name;

    DataType( int id, boolean isPrimitiveType, String baseType, String logicalType, String name ) {
      this.id = id;
      this.isPrimitive = isPrimitiveType;
      this.baseType = baseType;
      this.logicalType = logicalType;
      this.name = name;
    }

    public static DataType getDataType( int id ) {
      // Enum values() returns vals in order they are defined
      return Arrays.asList( DataType.values() ).get( id );
    }

    public int getId() {
      return id;
    }

    public boolean isPrimitiveType() {
      return isPrimitive;
    }

    public boolean isComplexType() {
      return !isPrimitive && ( logicalType == null );
    }

    public boolean isLogicalType() {
      return logicalType != null;
    }

    public String getBaseType() {
      return baseType;
    }

    public String getLogicalType() {
      return logicalType;
    }

    public String getType() {
      return isLogicalType() ? logicalType : baseType;
    }

    public String getName() {
      return name;
    }

  }

  public static final String TYPE_RECORD = "record";
  public static final String DOC = "doc";
  public static final String FIELDS_NODE = "fields";
  public static final String NAMESPACE_NODE = "namespace";
  public static final String NAME_NODE = "name";
  public static final String TYPE_NODE = "type";
  public static final String DEFAULT_NODE = "default";
  public static final String LOGICAL_TYPE = "logicalType";
  public static final String DECIMAL_PRECISION = "precision";
  public static final String DECIMAL_SCALE = "scale";
  public static final int DEFAULT_DECIMAL_PRECISION = 10;
  public static final int DEFAULT_DECIMAL_SCALE = 0;
}
