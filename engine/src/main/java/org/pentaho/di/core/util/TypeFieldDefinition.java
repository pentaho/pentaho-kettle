/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.core.util;

import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;

public class TypeFieldDefinition {
  private int type;
  private String fieldName;

  /**
   * @param type
   * @param fieldName
   */
  public TypeFieldDefinition( int type, String fieldName ) {
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
   * @param type
   *          the type to set
   */
  public void setType( int type ) {
    this.type = type;
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

  public String getTypeDescription() {
    switch ( type ) {
      case ValueMetaInterface.TYPE_BOOLEAN:
        return "boolean";
      case ValueMetaInterface.TYPE_INTEGER:
        return "int";
      default:
        return ValueMetaFactory.getValueMetaName( type );
    }
  }

  public String getMemberName() {
    return fieldName.substring( 0, 1 ).toLowerCase() + fieldName.substring( 1 );
  }
}
