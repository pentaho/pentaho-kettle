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

public interface IFormatInputField {

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
