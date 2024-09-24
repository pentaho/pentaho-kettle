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

public interface IAvroLookupField {

  String getFieldName();

  void setFieldName( String fieldName );

  String getVariableName();

  void setVariableName( String variableName );

  String getDefaultValue();

  void setDefaultValue( String defaultValue );
}
