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

package org.pentaho.di.trans.steps.avro.output;

import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.steps.file.BaseFileField;

/**
 * Base class for format's input/output field - path added.
 * 
 * @author <alexander_buloichik@epam.com>
 */
public class FormatInputOutputField extends BaseFileField {
  @Injection( name = "FIELD_PATH", group = "FIELDS" )
  protected String path;

  @Injection( name = "FIELD_NULLABLE", group = "FIELDS" )
  protected boolean nullable = true;

  protected int sourceType;

  public String getPath() {
    return path;
  }

  public void setPath( String path ) {
    this.path = path;
  }

  public boolean isNullable() {
    return nullable;
  }

  public void setNullable( boolean nullable ) {
    this.nullable = nullable;
  }

  /**
   * @return The field type when read from the source before it was possibly overriden in the UI
   * (eg. AvroInput step)
   */
  public int getSourceType() {
    return sourceType;
  }

  public void setSourceType( int sourceType ) {
    this.sourceType = sourceType;
  }

  @Injection( name = "FIELD_SOURCE_TYPE", group = "FIELDS" )
  public void setSourceType( String value ) {
    this.sourceType = ValueMetaFactory.getIdForValueMeta( value );
  }

  public String getSourceTypeDesc() {
    return ValueMetaFactory.getValueMetaName( sourceType );
  }
}
