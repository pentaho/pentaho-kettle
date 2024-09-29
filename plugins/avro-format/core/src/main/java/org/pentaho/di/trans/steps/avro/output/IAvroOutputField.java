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

package org.pentaho.di.trans.steps.avro.output;

import org.pentaho.di.trans.steps.avro.AvroSpec;

public interface IAvroOutputField  {

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

  AvroSpec.DataType getAvroType();

  void setFormatType( AvroSpec.DataType type );
}
