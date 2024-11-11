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

package org.pentaho.di.trans.steps.avro;

import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.pentaho.di.trans.steps.avro.input.IAvroInputField;
import org.pentaho.di.trans.steps.avro.input.AvroInputField;
import org.pentaho.di.trans.steps.avro.input.PentahoAvroInputFormat;
import org.apache.avro.Conversions;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.util.Utf8;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class AvroToPdiConverter {
  Schema avroSchema;
  AvroSpec.DataType avroDataType = null;

  public AvroToPdiConverter( Schema avroSchema ) {
    this.avroSchema = avroSchema;
  }

  public Object converAvroToPdi(Object avroData, AvroInputField avroInputField, Schema avroField ) {

    //Might need to make getting the avroFieldName smarter here
    String avroFieldName = avroInputField.getPathParts().get( avroInputField.getPathParts().size() - 1 );
    Schema.Type primitiveAvroType = avroField.getType();

    primitiveAvroType = avroField.getType();

    switch ( primitiveAvroType ) {
      case INT:
        avroDataType = AvroSpec.DataType.INTEGER;
        break;
      case LONG:
        avroDataType = AvroSpec.DataType.LONG;
        break;
      case BYTES:
        avroDataType = AvroSpec.DataType.BYTES;
        break;
      case FIXED:
        avroDataType = AvroSpec.DataType.FIXED;
        break;
      case FLOAT:
        avroDataType = AvroSpec.DataType.FLOAT;
        break;
      case DOUBLE:
        avroDataType = AvroSpec.DataType.DOUBLE;
        break;
      case STRING:
        avroDataType = AvroSpec.DataType.STRING;
        break;
      case BOOLEAN:
        avroDataType = AvroSpec.DataType.BOOLEAN;
        break;
    }

    Object pentahoData = null;

    if ( isNotNull( avroData ) ) {
      if ( avroData instanceof Utf8 ) {
        avroData = avroData.toString();
      }

      int pentahoType = avroInputField.getPentahoType();
      switch ( avroDataType ) {
        case BOOLEAN:
          pentahoData = convertToPentahoType( pentahoType, (Boolean) avroData );
          break;
        case DATE:
          pentahoData = convertToPentahoType( pentahoType, (Integer) avroData );
          break;
        case FLOAT:
          pentahoData = convertToPentahoType( pentahoType, (Float) avroData );
          break;
        case DOUBLE:
          pentahoData = convertToPentahoType( pentahoType, (Double) avroData );
          break;
        case LONG:
          pentahoData = convertToPentahoType( pentahoType, (Long) avroData );
          break;
        case DECIMAL:
          pentahoData = convertToPentahoType( pentahoType, (ByteBuffer) avroData, avroField );
          break;
        case INTEGER:
          pentahoData = convertToPentahoType( pentahoType, (Integer) avroData );
          break;
        case STRING:
          if ( avroData instanceof GenericData.EnumSymbol ) {
            pentahoData = ( (GenericData.EnumSymbol) avroData ).toString();
          } else if ( avroData instanceof TextNode) {
            pentahoData = convertToPentahoType( pentahoType, ( (TextNode) avroData ).asText(), avroInputField );
          } else {
            pentahoData = convertToPentahoType( pentahoType, (String) avroData, avroInputField );
          }
          break;
        case BYTES:
          pentahoData = convertToPentahoType( pentahoType, (ByteBuffer) avroData, avroField );
          break;
        case FIXED:
          pentahoData = convertToPentahoType( pentahoType, ( (GenericData.Fixed) avroData ).bytes(), avroField );
          break;
        case TIMESTAMP_MILLIS:
          pentahoData = convertToPentahoType( pentahoType, (Long) avroData );
          break;
      }
    }

    return pentahoData;
  }

  private Object convertToPentahoType( int pentahoType, Float avroData ) {
    Object pentahoData = null;
    if ( isNotNull( avroData ) ) {
      try {
        switch ( pentahoType ) {
          case ValueMetaInterface.TYPE_STRING:
            pentahoData = avroData.toString();
            break;
          case ValueMetaInterface.TYPE_INTEGER:
            pentahoData = new Long( avroData.longValue() );
            break;
          case ValueMetaInterface.TYPE_NUMBER:
            pentahoData = Double.parseDouble( avroData.toString() );
            break;
          case ValueMetaInterface.TYPE_BIGNUMBER:
            new BigDecimal( avroData.toString() );
            break;
          case ValueMetaInterface.TYPE_TIMESTAMP:
            pentahoData = new Timestamp( avroData.longValue() );
            break;
          case ValueMetaInterface.TYPE_DATE:
            pentahoData = new Date( avroData.longValue() );
            break;
          case ValueMetaInterface.TYPE_BOOLEAN:
            pentahoData = ( avroData == 0 ? Boolean.FALSE : Boolean.TRUE );
            break;
        }
      } catch ( Exception e ) {
        // If unable to do the type conversion just ignore. null will be returned.
      }
    }
    return pentahoData;
  }

  private Object convertToPentahoType( int pentahoType, ByteBuffer avroData, Schema field ) {
    Object pentahoData = null;
    if ( isNotNull( avroData ) ) {
      try {
        switch ( pentahoType ) {
          case ValueMetaInterface.TYPE_BIGNUMBER:
            Conversions.DecimalConversion converter = new Conversions.DecimalConversion();
            Schema schema = field;
            if ( schema.getType().equals( Schema.Type.UNION ) ) {
              List<Schema> schemas = field.getTypes();
              for ( Schema s : schemas ) {
                if ( !s.getName().equalsIgnoreCase( "null" ) ) {
                  schema = s;
                  break;
                }
              }
            }
            Object precision = schema.getObjectProp( AvroSpec.DECIMAL_PRECISION );
            Object scale = schema.getObjectProp( AvroSpec.DECIMAL_SCALE );
            LogicalTypes.Decimal decimalType =
              LogicalTypes.decimal( Integer.parseInt( precision.toString() ), Integer.parseInt( scale.toString() ) );
            pentahoData = converter.fromBytes( avroData, avroSchema, decimalType );
            break;
          case ValueMetaInterface.TYPE_BINARY:
            pentahoData = new byte[ avroData.remaining() ];
            avroData.get( (byte[]) pentahoData );
            break;
        }
      } catch ( Exception e ) {
        // If unable to do the type conversion just ignore. null will be returned.
      }
    }
    return pentahoData;
  }

  private Object convertToPentahoType( int pentahoType, Long avroData ) {
    Object pentahoData = null;
    if ( isNotNull( avroData ) ) {
      try {
        switch ( pentahoType ) {
          case ValueMetaInterface.TYPE_STRING:
            pentahoData = avroData.toString();
            break;
          case ValueMetaInterface.TYPE_INTEGER:
            pentahoData = avroData.longValue();
            break;
          case ValueMetaInterface.TYPE_NUMBER:
            pentahoData = new Double( avroData.doubleValue() );
            break;
          case ValueMetaInterface.TYPE_BIGNUMBER:
            pentahoData = BigDecimal.valueOf( avroData );
            break;
          case ValueMetaInterface.TYPE_TIMESTAMP:
            pentahoData = new Timestamp( avroData );
            break;
          case ValueMetaInterface.TYPE_DATE:
            pentahoData = new Date( avroData );
            break;
          case ValueMetaInterface.TYPE_BOOLEAN:
            pentahoData = ( avroData == 0 ? Boolean.FALSE : Boolean.TRUE );
            break;
        }
      } catch ( Exception e ) {
        // If unable to do the type conversion just ignore. null will be returned.
      }
    }
    return pentahoData;
  }

  private Object convertToPentahoType( int pentahoType, Integer avroData ) {
    Object pentahoData = null;
    if ( isNotNull( avroData ) ) {
      try {
        switch ( pentahoType ) {
          case ValueMetaInterface.TYPE_STRING:
            pentahoData = avroData.toString();
            break;
          case ValueMetaInterface.TYPE_INTEGER:
            pentahoData = new Long( avroData.longValue() );
            break;
          case ValueMetaInterface.TYPE_NUMBER:
            pentahoData = new Double( avroData.doubleValue() );
            break;
          case ValueMetaInterface.TYPE_BIGNUMBER:
            pentahoData = BigDecimal.valueOf( avroData );
            break;
          case ValueMetaInterface.TYPE_TIMESTAMP:
            pentahoData = new Timestamp( avroData );
            break;
          case ValueMetaInterface.TYPE_DATE:
            LocalDate localDate = LocalDate.ofEpochDay( 0 ).plusDays( avroData );
            pentahoData = Date.from( localDate.atStartOfDay( ZoneId.systemDefault() ).toInstant() );
            break;
          case ValueMetaInterface.TYPE_BOOLEAN:
            pentahoData = ( avroData == 0 ? Boolean.FALSE : Boolean.TRUE );
            break;
        }
      } catch ( Exception e ) {
        // If unable to do the type conversion just ignore. null will be returned.
      }
    }
    return pentahoData;
  }

  private Object convertToPentahoType( int pentahoType, Double avroData ) {
    Object pentahoData = null;
    if ( isNotNull( avroData ) ) {
      try {
        switch ( pentahoType ) {
          case ValueMetaInterface.TYPE_STRING:
            pentahoData = avroData.toString();
            break;
          case ValueMetaInterface.TYPE_INTEGER:
            pentahoData = new Long( avroData.longValue() );
            break;
          case ValueMetaInterface.TYPE_NUMBER:
            pentahoData = avroData;
            break;
          case ValueMetaInterface.TYPE_BIGNUMBER:
            pentahoData = BigDecimal.valueOf( avroData );
            break;
          case ValueMetaInterface.TYPE_TIMESTAMP:
            pentahoData = new Timestamp( avroData.longValue() );
            break;
          case ValueMetaInterface.TYPE_DATE:
            pentahoData = new Date( avroData.longValue() );
            break;
          case ValueMetaInterface.TYPE_BOOLEAN:
            pentahoData = ( avroData == 0 ? Boolean.FALSE : Boolean.TRUE );
            break;
        }
      } catch ( Exception e ) {
        // If unable to do the type conversion just ignore. null will be returned.
      }
    }
    return pentahoData;
  }

  private Object convertToPentahoType( int pentahoType, Boolean avroData ) {
    Object pentahoData = null;
    if ( isNotNull( avroData ) ) {
      try {
        switch ( pentahoType ) {
          case ValueMetaInterface.TYPE_STRING:
            pentahoData = avroData.toString();
            break;
          case ValueMetaInterface.TYPE_BOOLEAN:
            pentahoData = avroData;
            break;
          case ValueMetaInterface.TYPE_INTEGER:
            pentahoData = avroData ? new Long( 1 ) : new Long( 0 );
            break;
          case ValueMetaInterface.TYPE_NUMBER:
            pentahoData = avroData ? new Double( 1 ) : new Double( 0 );
            break;
          case ValueMetaInterface.TYPE_BIGNUMBER:
            pentahoData = avroData ? new BigDecimal( 1 ) : new BigDecimal( 0 );
            break;
        }
      } catch ( Exception e ) {
        // If unable to do the type conversion just ignore. null will be returned.
      }
    }
    return pentahoData;
  }

  private Object convertToPentahoType( int pentahoType, String avroData, IAvroInputField avroInputField ) {
    Object pentahoData = null;
    if ( isNotNull( avroData ) ) {
      try {
        switch ( pentahoType ) {
          case ValueMetaInterface.TYPE_INET:
            pentahoData = InetAddress.getByName( avroData.toString() );
            break;
          case ValueMetaInterface.TYPE_STRING:
            pentahoData = avroData;
            break;
          case ValueMetaInterface.TYPE_INTEGER:
            pentahoData = Long.parseLong( avroData );
            break;
          case ValueMetaInterface.TYPE_NUMBER:
            pentahoData = Double.parseDouble( avroData );
            break;
          case ValueMetaInterface.TYPE_BIGNUMBER:
            pentahoData = new BigDecimal( avroData );
            break;
          case ValueMetaInterface.TYPE_TIMESTAMP:
            pentahoData = new Timestamp( Long.parseLong( avroData ) );
            break;
          case ValueMetaInterface.TYPE_DATE:
            String dateFormatStr = avroInputField.getStringFormat();
            if ( ( dateFormatStr == null ) || ( dateFormatStr.trim().length() == 0 ) ) {
              dateFormatStr = ValueMetaBase.DEFAULT_DATE_FORMAT_MASK;
            }
            SimpleDateFormat datePattern = new SimpleDateFormat( dateFormatStr );
            pentahoData = datePattern.parse( avroData );
            break;
          case ValueMetaInterface.TYPE_BOOLEAN:
            pentahoData = Boolean.valueOf( "Y".equalsIgnoreCase( avroData ) || "TRUE".equalsIgnoreCase( avroData )
              || "YES".equalsIgnoreCase( avroData ) || "1".equals( avroData ) );
            break;
        }
      } catch ( Exception e ) {
        // If unable to do the type conversion just ignore. null will be returned.
      }
    }
    return pentahoData;
  }

  private Object convertToPentahoType( int pentahoType, byte[] avroData, Schema field ) {
    Object pentahoData = null;
    if ( isNotNull( avroData ) ) {
      try {
        switch ( pentahoType ) {
          case ValueMetaInterface.TYPE_BINARY:
            pentahoData = avroData;
            break;
        }
      } catch ( Exception e ) {
        // If unable to do the type conversion just ignore. null will be returned.
      }
    }
    return pentahoData;
  }

  private boolean isLegacySchema( Schema schema ) {
    if ( schema.getFields().size() > 0 ) {
      Schema.Field field = schema.getFields().get( 0 );
      return field != null && field.name() != null && field.name()
        .contains( PentahoAvroInputFormat.FieldName.FIELDNAME_DELIMITER );
    } else {
      return false;
    }
  }

  private boolean isNotNull( Object avroData ) {
    return avroData != null && !( avroData instanceof NullNode );
  }
}
