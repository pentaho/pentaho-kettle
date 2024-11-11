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


package org.pentaho.di.trans.steps.avro.input;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;

public class AvroLookupField implements IAvroLookupField {
  @Injection( name = "LOOKUP_FIELD_NAME", group = "LOOKUP_FIELDS" )
  public String fieldName = "";

  @Injection( name = "LOOKUP_VARIABLE_NAME", group = "LOOKUP_FIELDS" )
  public String variableName = "";

  @Injection( name = "LOOKUP_DEFAULT_VALUE", group = "LOOKUP_FIELDS" )
  public String defaultValue = "";

  protected String cleansedVariableName;
  protected String resolvedFieldName;
  protected String resolvedDefaultvalue;

  /** False if this field does not exist in the incoming row stream */
  protected boolean isValid = true;

  /** Index of this field in the incoming row stream */
  protected int inputIndex = -1;

  protected ValueMetaInterface fieldVM;

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName( String fieldName ) {
    this.fieldName = fieldName;
  }

  public String getVariableName() {
    return variableName;
  }

  public void setVariableName( String variableName ) {
    this.variableName = variableName;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue( String defaultValue ) {
    this.defaultValue = defaultValue;
  }

  public boolean init( RowMetaInterface inRowMeta, VariableSpace space ) {

    if ( inRowMeta == null ) {
      isValid = false;
      return false;
    }

    resolvedFieldName = ( space != null ) ? space.environmentSubstitute( fieldName ) : fieldName;

    inputIndex = inRowMeta.indexOfValue( resolvedFieldName );
    if ( inputIndex < 0 ) {
      isValid = false;

      return isValid;
    }

    fieldVM = inRowMeta.getValueMeta( inputIndex );

    if ( !Const.isEmpty( variableName ) ) {
      cleansedVariableName = variableName.replaceAll( "\\.", "_" );
    } else {
      isValid = false;
      return isValid;
    }

    resolvedDefaultvalue = space.environmentSubstitute( defaultValue );

    return isValid;
  }

  public void setVariable( VariableSpace space, Object[] inRow ) {
    if ( !isValid ) {
      return;
    }

    String valueToSet = "";
    try {
      if ( fieldVM.isNull( inRow[inputIndex] ) ) {
        if ( !Const.isEmpty( resolvedDefaultvalue ) ) {
          valueToSet = resolvedDefaultvalue;
        } else {
          valueToSet = "null";
        }
      } else {
        valueToSet = fieldVM.getString( inRow[inputIndex] );
      }
    } catch ( KettleValueException e ) {
      valueToSet = "null";
    }

    space.setVariable( cleansedVariableName, valueToSet );
  }

}
