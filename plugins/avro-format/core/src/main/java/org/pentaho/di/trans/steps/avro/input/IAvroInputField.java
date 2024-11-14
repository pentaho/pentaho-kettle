/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.avro.input;

import org.pentaho.di.trans.steps.avro.AvroSpec;

import java.util.List;

public interface IAvroInputField {
  static final String FILENAME_DELIMITER = "_delimiter_";

  String getAvroFieldName();

  void setAvroFieldName( String avroFieldName );

  AvroSpec.DataType getAvroType();

  void setAvroType( AvroSpec.DataType avroType );

  void setAvroType( String avroType );

  String getDisplayableAvroFieldName();

  void setIndexedVals( List<String> mindexedVals );

  List<String> getIndexedVals();

  void setIndexedValues( String indexedValues );

  String getIndexedValues();

  String getFormatFieldName();

  void setFormatFieldName( String formatFieldName );

  String getPentahoFieldName();

  void setPentahoFieldName( String pentahoFieldName );

  int getPentahoType();

  void setPentahoType( int pentahoType );

  void setPentahoType( String value );

  int getFormatType();

  void setFormatType( int formatType );

  int getPrecision();

  void setPrecision( int precision );

  int getScale();

  void setScale( int scale );

  String getStringFormat();

  void setStringFormat( String stringFormat );
}
