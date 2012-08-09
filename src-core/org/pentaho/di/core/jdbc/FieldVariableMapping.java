package org.pentaho.di.core.jdbc;

import java.util.List;

public class FieldVariableMapping {
  
  public enum MappingType {
    SQL, // convert SQL for one field 
    DIRECT, // convert field value in where clause to a single String value
    SQL_ALL, // convert the whole where clause condition into a SQL condition.
    ;
    
    public static MappingType getMappingType(String typeString) {
      try {
        return valueOf(typeString);
      } catch (Exception e) {
        return DIRECT;
      }
    }
  }
  
  private String fieldName;
  private String variableName;
  private MappingType mappingType;
  
  public FieldVariableMapping() {
    mappingType=MappingType.DIRECT;
  }

  /**
   * @param fieldName
   * @param variableName
   * @param mappingType
   */
  public FieldVariableMapping(String fieldName, String variableName, MappingType mappingType) {
    this.fieldName = fieldName;
    this.variableName = variableName;
    this.mappingType = mappingType;
  }
  
  /**
   * Find a field-variable mapping by field name, perform case insensitive comparison.
   * @param mappings The list of mappings
   * @param fieldName the name of the field to look for. 
   * @return the field-variable mapping or null if nothing could be found.
   */
  public static FieldVariableMapping findFieldVariableMappingByFieldName(List<FieldVariableMapping> mappings, String fieldName) {
    for (FieldVariableMapping mapping : mappings) {
      if (mapping.getFieldName().equalsIgnoreCase(fieldName)) {
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
   * @param fieldName the fieldName to set
   */
  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  /**
   * @return the variableName
   */
  public String getVariableName() {
    return variableName;
  }

  /**
   * @param variableName the variableName to set
   */
  public void setVariableName(String variableName) {
    this.variableName = variableName;
  }

  /**
   * @return the mappingType
   */
  public MappingType getMappingType() {
    return mappingType;
  }

  /**
   * @param mappingType the mappingType to set
   */
  public void setMappingType(MappingType mappingType) {
    this.mappingType = mappingType;
  }
  
  
  
}
