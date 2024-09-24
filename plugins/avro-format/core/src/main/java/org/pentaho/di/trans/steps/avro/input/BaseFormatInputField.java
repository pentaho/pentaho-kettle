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

package org.pentaho.di.trans.steps.avro.input;

import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.trans.steps.file.BaseFileField;

/**
 * Base input step field for various big data file formats
 *
 * @author tkafalas
 */
public class BaseFormatInputField extends BaseFileField implements IFormatInputField {
  @Injection( name = "FIELD_PATH", group = "FIELDS" )
  protected String formatFieldName = null;
  @Injection( name = "AVRO_FORMAT_TYPE", group = "FIELDS" )
  private int formatType;
  private int precision = 0;
  private int scale = 0;
  private String stringFormat = "";

  @Override
  public String getFormatFieldName() {
    return formatFieldName;
  }

  @Override
  public void setFormatFieldName( String formatFieldName ) {
    this.formatFieldName = formatFieldName;
  }

  @Override
  public String getPentahoFieldName() {
    return getName();
  }

  @Override
  public void setPentahoFieldName( String pentahoFieldName ) {
    setName( pentahoFieldName );
  }

  @Override
  public int getPentahoType() {
    return getType();
  }

  @Override
  public void setPentahoType( int pentahoType ) {
    setType( pentahoType );
  }

  @Override public int getFormatType() {
    return formatType;
  }

  @Override public void setFormatType( int formatType ) {
    this.formatType = formatType;
  }

  @Override public int getPrecision() {
    return this.precision;
  }

  @Override public void setPrecision( int precision ) {
    this.precision = precision;
  }

  @Override public int getScale() {
    return scale;
  }

  @Override public void setScale( int scale ) {
    this.scale = scale;
  }

  @Override
  public String getStringFormat() {
    return stringFormat;
  }

  @Override
  public void setStringFormat( String stringFormat ) {
    this.stringFormat = stringFormat == null ? "" : stringFormat;
  }

  public void setPentahoType( String value ) {
    setType( value );
  }
}
