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

import org.pentaho.di.core.injection.Injection;

public class BaseFormatOutputField implements IFormatOutputField {
  public static final int DEFAULT_DECIMAL_PRECISION = 10;
  public static final int DEFAULT_DECIMAL_SCALE = 0;

  protected int formatType;

  protected int pentahoType;

  @Injection( name = "FIELD_PATH", group = "FIELDS" )
  protected String formatFieldName;

  @Injection( name = "FIELD_NAME", group = "FIELDS" )
  protected String pentahoFieldName;

  @Injection( name = "FIELD_NULLABLE", group = "FIELDS" )
  protected boolean allowNull;

  @Injection( name = "FIELD_IF_NULL", group = "FIELDS" )
  protected String defaultValue;

  @Injection( name = "FIELD_DECIMAL_PRECISION", group = "FIELDS" )
  protected int precision;

  @Injection( name = "FIELD_DECIMAL_SCALE", group = "FIELDS" )
  protected int scale;

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
    return pentahoFieldName;
  }

  @Override
  public void setPentahoFieldName( String pentahoFieldName ) {
    this.pentahoFieldName = pentahoFieldName;
  }

  @Override
  public boolean getAllowNull() {
    return allowNull;
  }

  @Override
  public void setAllowNull( boolean allowNull ) {
    this.allowNull = allowNull;
  }

  @Override
  public String getDefaultValue() {
    return defaultValue;
  }

  @Override
  public void setDefaultValue( String defaultValue ) {
    this.defaultValue = defaultValue;
  }

  @Injection( name = "FIELD_NULL_STRING", group = "FIELDS" )
  public void setAllowNull( String allowNull ) {
    if ( allowNull != null && allowNull.length() > 0 ) {
      if ( allowNull.equalsIgnoreCase( "yes" ) || allowNull.equalsIgnoreCase( "y" ) ) {
        this.allowNull = true;
      } else if ( allowNull.equalsIgnoreCase( "no" ) || allowNull.equalsIgnoreCase( "n" ) ) {
        this.allowNull = false;
      } else {
        this.allowNull = Boolean.parseBoolean( allowNull );
      }
    }
  }

  @Override
  public int getFormatType() {
    return formatType;
  }

  @Override
  public void setFormatType( int formatType ) {
    this.formatType = formatType;
  }

  @Override
  public int getPrecision() {
    return precision;
  }

  @Override
  public void setPrecision( String precision ) {
    if ( precision == null || precision.equals( "" ) ) {
      this.precision = DEFAULT_DECIMAL_PRECISION;
    } else {
      this.precision = Integer.valueOf( precision );
      if ( this.precision <= 0 ) {
        this.precision = DEFAULT_DECIMAL_PRECISION;
      }
    }
  }

  @Override
  public int getScale() {
    return scale;
  }

  @Override
  public void setScale( String scale ) {
    if ( scale == null || scale.equals( "" ) ) {
      this.scale = DEFAULT_DECIMAL_SCALE;
    } else {
      this.scale = Integer.valueOf( scale );
      if ( this.scale < 0 ) {
        this.scale = DEFAULT_DECIMAL_SCALE;
      }
    }
  }

  @Override
  public int getPentahoType() {
    return pentahoType;
  }

  @Override
  public void setPentahoType( int pentahoType ) {
    this.pentahoType = pentahoType;
  }
}
