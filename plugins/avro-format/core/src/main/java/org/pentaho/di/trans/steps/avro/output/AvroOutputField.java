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

import org.pentaho.di.trans.steps.avro.AvroSpec;
import org.pentaho.di.trans.steps.avro.AvroTypeConverter;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Base class for format's input/output field - path added.
 * 
 * @author JRice <joseph.rice@hitachivantara.com>
 */
public class AvroOutputField extends BaseFormatOutputField implements IAvroOutputField {
  @Override
  public AvroSpec.DataType getAvroType() {
    return AvroSpec.DataType.values()[ formatType ];
  }

  @Override
  public void setFormatType( AvroSpec.DataType avroType ) {
    this.formatType = avroType.getId();
  }

  @Override
  public void setFormatType( int formatType ) {
    for ( AvroSpec.DataType avroType : AvroSpec.DataType.values() ) {
      if ( avroType.getId() == formatType ) {
        this.formatType = formatType;
      }
    }
  }

  @Injection( name = "FIELD_AVRO_TYPE", group = "FIELDS" )
  public void setFormatType( String typeName ) {
    try  {
      setFormatType( Integer.parseInt( typeName ) );
    } catch ( NumberFormatException nfe ) {
      for ( AvroSpec.DataType avroType : AvroSpec.DataType.values() ) {
        if ( avroType.getName().equals( typeName ) ) {
          this.formatType = avroType.getId();
          break;
        }
      }
    }
  }

  @Injection( name = "FIELD_TYPE", group = "FIELDS" )
  @Deprecated
  public void setPentahoType( String typeName ) {
    for ( int i = 0; i < ValueMetaInterface.typeCodes.length; i++ ) {
      if ( typeName.equals( ValueMetaInterface.typeCodes[ i ] ) ) {
        setFormatType( AvroTypeConverter.convertToAvroType( i ) );
        break;
      }
    }
  }


  public boolean isDecimalType() {
    return getAvroType().equals( AvroSpec.DataType.DECIMAL );
  }

  @Override
  public void setPrecision( String precision ) {
    if ( ( precision == null ) || ( precision.trim().length() == 0 ) ) {
      this.precision = isDecimalType() ? AvroSpec.DEFAULT_DECIMAL_PRECISION : 0;
    } else {
      this.precision = Integer.valueOf( precision );
      if ( ( this.precision <= 0 ) && isDecimalType() ) {
        this.precision = AvroSpec.DEFAULT_DECIMAL_PRECISION;
      }
    }
  }

  @Override
  public void setScale( String scale ) {
    if ( ( scale == null ) || ( scale.trim().length() == 0 ) ) {
      this.scale = isDecimalType() ? AvroSpec.DEFAULT_DECIMAL_SCALE : 0;
    } else {
      this.scale = Integer.valueOf( scale );
      if ( ( this.scale < 0 ) ) {
        this.scale = isDecimalType() ? AvroSpec.DEFAULT_DECIMAL_SCALE : 0;
      }
    }
  }
}
