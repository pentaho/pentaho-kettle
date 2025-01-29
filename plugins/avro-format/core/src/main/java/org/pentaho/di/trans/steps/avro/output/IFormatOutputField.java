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


package org.pentaho.di.trans.steps.avro.output;

public interface IFormatOutputField {
  String getFormatFieldName();

  void setFormatFieldName( String formatFieldName );

  String getPentahoFieldName();

  void setPentahoFieldName( String pentahoFieldName );

  boolean getAllowNull();

  void setAllowNull( boolean allowNull );

  String getDefaultValue();

  void setDefaultValue( String defaultValue );

  int getPrecision();

  void setPrecision( String precision );

  int getScale();

  void setScale( String scale );

  int getFormatType();

  void setFormatType( int formatType );

  int getPentahoType();

  void setPentahoType( int pentahoType );
}
