/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.jdbc;

import java.util.List;

import org.pentaho.di.core.Const;

/**
 * This class is no longer used
 *
 * Data Service client code is now available in the pdi-dataservice-plugin project
 *
 */
@Deprecated
public class FieldVariableMapping {

  public enum MappingType {
    FIELDMAP, // Helper mapping to map output fields to SQL columns
      SQL_WHERE, // generates the where clause condition into a SQL condition.
      JSON_QUERY; // generates the JSON query (MongoDB mainly)

    public static MappingType getMappingType( String typeString ) {
      try {
        return valueOf( typeString );
      } catch ( Exception e ) {
        return FIELDMAP;
      }
    }
  }

  private String fieldName;
  private String targetName;
  private String variableName;
  private MappingType mappingType;

  public FieldVariableMapping() {
    mappingType = MappingType.FIELDMAP;
  }

  /**
   * @param fieldName
   * @param variableName
   * @param mappingType
   */
  public FieldVariableMapping( String fieldName, String targetName, String variableName, MappingType mappingType ) {
    this.fieldName = fieldName;
    this.targetName = targetName;
    this.variableName = variableName;
    this.mappingType = mappingType;
  }

  /**
   * Find a field-variable mapping by field name, perform case insensitive comparison.
   *
   * @param mappings
   *          The list of mappings
   * @param fieldName
   *          the name of the field to look for.
   * @return the field-variable mapping or null if nothing could be found.
   */
  public static FieldVariableMapping findFieldVariableMappingByFieldName( List<FieldVariableMapping> mappings,
    String fieldName ) {
    for ( FieldVariableMapping mapping : mappings ) {
      if ( !Const.isEmpty( mapping.getFieldName() ) && mapping.getFieldName().equalsIgnoreCase( fieldName ) ) {
        return mapping;
      }
    }
    return null;
  }

  /**
   * @return the fieldName
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * @param fieldName
   *          the fieldName to set
   */
  public void setFieldName( String fieldName ) {
    this.fieldName = fieldName;
  }

  /**
   * @return the variableName
   */
  public String getVariableName() {
    return variableName;
  }

  /**
   * @param variableName
   *          the variableName to set
   */
  public void setVariableName( String variableName ) {
    this.variableName = variableName;
  }

  /**
   * @return the mappingType
   */
  public MappingType getMappingType() {
    return mappingType;
  }

  /**
   * @param mappingType
   *          the mappingType to set
   */
  public void setMappingType( MappingType mappingType ) {
    this.mappingType = mappingType;
  }

  /**
   * @return the targetName
   */
  public String getTargetName() {
    return targetName;
  }

  /**
   * @param targetName
   *          the targetName to set
   */
  public void setTargetName( String targetName ) {
    this.targetName = targetName;
  }

}
