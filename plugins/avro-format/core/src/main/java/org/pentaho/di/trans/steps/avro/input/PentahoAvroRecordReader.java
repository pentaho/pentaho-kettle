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

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.trans.steps.avro.AvroSpec;
import org.apache.avro.Conversions;
import org.apache.avro.LogicalType;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.util.Utf8;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by rmansoor on 10/5/2017.
 */
public class PentahoAvroRecordReader implements IPentahoAvroInputFormat.IPentahoRecordReader {

  private final DataFileStream<GenericRecord> nativeAvroRecordReader;
  private final Schema avroSchema;
  private final List<? extends IAvroInputField> fields;
  private boolean legacySchema;

  public PentahoAvroRecordReader( DataFileStream<GenericRecord> nativeAvroRecordReader,
                                  Schema avroSchema, List<? extends IAvroInputField> fields ) {
    this.nativeAvroRecordReader = nativeAvroRecordReader;
    this.avroSchema = avroSchema;
    this.legacySchema = isLegacySchema( avroSchema );
    this.fields = fields;
  }

  @Override public void close() throws IOException {
    nativeAvroRecordReader.close();
  }

  @Override public Iterator<RowMetaAndData> iterator() {
    return new Iterator<RowMetaAndData>() {

      @Override public boolean hasNext() {
        return nativeAvroRecordReader.hasNext();
      }

      @Override public RowMetaAndData next() {
        return getRowMetaAndData( nativeAvroRecordReader.next() );
      }
    };
  }

  @VisibleForTesting
  public RowMetaAndData getRowMetaAndData( GenericRecord avroRecord ) {
    RowMetaAndData rowMetaAndData = new RowMetaAndData();
    Schema.Field avroField = null;
    String avroFieldName = null;
    String metaFieldName = null;
    for ( IAvroInputField metaField : fields ) {
      // Check if the schema is generated using 8.0. If it is, then properly read the schema fields
      if ( legacySchema ) {
        metaFieldName = metaField.getAvroFieldName();
        if ( !metaFieldName.contains( PentahoAvroInputFormat.FieldName.FIELDNAME_DELIMITER ) ) {
          // First we will attempt to read it with allowsNull value of false.
          PentahoAvroInputFormat.FieldName fieldName = new PentahoAvroInputFormat.FieldName( metaFieldName,
            metaField.getPentahoType(), false );
          avroFieldName = fieldName.getLegacyFieldName();
          avroField = avroSchema.getField( avroFieldName );
          if ( avroField == null ) {
            // We were not able to find the field with allowsNull value of false. Trying true now.
            fieldName = new PentahoAvroInputFormat.FieldName( metaFieldName,
              metaField.getPentahoType(), true );
            avroFieldName = fieldName.getLegacyFieldName();
            avroField = avroSchema.getField( avroFieldName );
          }
        } else {
          avroFieldName = metaField.getAvroFieldName();
          avroField = avroSchema.getField( avroFieldName );
        }
      } else {
        // Schema was not generated using 8.0. Getting the field from the schema
        avroFieldName = metaField.getAvroFieldName();
        avroField = avroSchema.getField( avroFieldName );
      }

      if ( avroField == null ) {
        continue;
      }

      if ( metaField != null ) {
        AvroSpec.DataType avroDataType = null;
        LogicalType logicalType = null;
        Schema.Type primitiveAvroType = null;

        if ( avroField.schema().getType().equals( Schema.Type.UNION ) ) {
          for ( Schema typeSchema : avroField.schema().getTypes() ) {
            if ( !typeSchema.getType().equals( Schema.Type.NULL ) ) {
              logicalType = typeSchema.getLogicalType();
              primitiveAvroType = typeSchema.getType();
              break;
            }
          }
        } else {
          logicalType = avroField.schema().getLogicalType();
          primitiveAvroType = avroField.schema().getType();
        }

        if ( logicalType != null ) {
          for ( AvroSpec.DataType tmpType : AvroSpec.DataType.values() ) {
            if ( !tmpType.isPrimitiveType() && tmpType.getType().equals( logicalType.getName() ) ) {
              avroDataType = tmpType;
              break;
            }
          }
        } else {
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
        }

        Object avroData = avroRecord.get( avroFieldName );
        Object pentahoData = null;

        if ( avroData != null ) {
          if ( avroData instanceof Utf8 ) {
            avroData = avroData.toString();
          }

          int pentahoType = metaField.getPentahoType();
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
              pentahoData = convertToPentahoType( pentahoType, (String) avroData, metaField );
              break;
            case BYTES:
              pentahoData = convertToPentahoType( pentahoType, (ByteBuffer) avroData, avroField );
              break;
            case TIMESTAMP_MILLIS:
              pentahoData = convertToPentahoType( pentahoType, (Long) avroData );
              break;
          }
        }
        rowMetaAndData.addValue( metaField.getPentahoFieldName(), metaField.getPentahoType(), pentahoData );
        String stringFormat = metaField.getStringFormat();
        if ( ( stringFormat != null ) && ( stringFormat.trim().length() > 0 ) ) {
          rowMetaAndData.getValueMeta( rowMetaAndData.size() - 1 ).setConversionMask( stringFormat );
        }
      }
    }
    return rowMetaAndData;
  }

  private Object convertToPentahoType( int pentahoType, Float avroData ) {
    Object pentahoData = null;
    if ( avroData != null ) {
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

  private Object convertToPentahoType( int pentahoType, ByteBuffer avroData, Schema.Field field ) {
    Object pentahoData = null;
    if ( avroData != null ) {
      try {
        switch ( pentahoType ) {
          case ValueMetaInterface.TYPE_BIGNUMBER:
            Conversions.DecimalConversion converter = new Conversions.DecimalConversion();
            Schema schema = field.schema();
            if ( schema.getType().equals( Schema.Type.UNION ) ) {
              List<Schema> schemas = field.schema().getTypes();
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
    if ( avroData != null ) {
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
    if ( avroData != null ) {
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
    if ( avroData != null ) {
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
    if ( avroData != null ) {
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
    if ( avroData != null ) {
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

  private boolean isLegacySchema( Schema schema ) {
    if ( schema.getFields().size() > 0 ) {
      Schema.Field field = schema.getFields().get( 0 );
      return field != null && field.name() != null && field.name()
        .contains( PentahoAvroInputFormat.FieldName.FIELDNAME_DELIMITER );
    } else {
      return false;
    }
  }

}
