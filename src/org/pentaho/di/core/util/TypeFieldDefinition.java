package org.pentaho.di.core.util;

import org.pentaho.di.core.row.ValueMeta;

public class TypeFieldDefinition {
  private int type;
  private String fieldName;
  
  /**
   * @param type
   * @param fieldName
   */
  public TypeFieldDefinition(int type, String fieldName) {
    this.type = type;
    this.fieldName = fieldName;
  }
  /**
   * @return the type
   */
  public int getType() {
    return type;
  }
  /**
   * @param type the type to set
   */
  public void setType(int type) {
    this.type = type;
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
  
  public String getTypeDescription() {
    switch(type) {
    case ValueMeta.TYPE_BOOLEAN: return "boolean";
    case ValueMeta.TYPE_INTEGER: return "int";
    default: return ValueMeta.getTypeDesc(type);
    }
  }
  
  public String getMemberName() {
    return fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
  }
}
