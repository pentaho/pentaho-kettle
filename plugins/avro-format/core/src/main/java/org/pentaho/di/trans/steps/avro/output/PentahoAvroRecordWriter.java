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

import org.pentaho.di.trans.steps.avro.AvroSpec;
import org.apache.avro.Conversions;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by tkafalas on 8/28/2017.
 */
public class PentahoAvroRecordWriter implements IPentahoOutputFormat.IPentahoRecordWriter {
  private final DataFileWriter<GenericRecord> nativeAvroRecordWriter;
  private final Schema schema;
  private final List<? extends IAvroOutputField> outputFields;

  public PentahoAvroRecordWriter( DataFileWriter<GenericRecord> recordWriter, Schema schema,
                                  List<? extends IAvroOutputField> outputFields ) {
    this.nativeAvroRecordWriter = recordWriter;
    this.schema = schema;
    this.outputFields = outputFields;
  }

  @Override
  public void write( RowMetaAndData row ) {
    try {
      nativeAvroRecordWriter.append( createAvroRecord( row ) );
    } catch ( IOException e ) {
      // Do nothing
    }
  }

  public GenericRecord createAvroRecord( RowMetaAndData row ) {
    RowMetaInterface rmi = row.getRowMeta();
    GenericRecord outputRecord = new GenericData.Record( schema );

    try {
      for ( IAvroOutputField field : outputFields ) {
        if ( field != null ) {
          AvroSpec.DataType avroType = field.getAvroType();
          int fieldMetaIndex = rmi.indexOfValue( field.getPentahoFieldName() );
          ValueMetaInterface vmi = rmi.getValueMeta( fieldMetaIndex );
          String avroFieldName = field.getFormatFieldName();
          String pentahoFieldName = field.getPentahoFieldName();
          String defaultValue = null;
          if ( !field.getAllowNull() ) {
            defaultValue = field.getDefaultValue();
          }
          switch ( avroType ) {
            case BOOLEAN:
              Boolean booleanValue = null;
              if ( row.isEmptyValue( pentahoFieldName ) ) {
                booleanValue =
                  ( defaultValue != null && defaultValue.length() > 0 ) ? Boolean.parseBoolean( defaultValue ) : null;
              } else {
                booleanValue = row.getBoolean( fieldMetaIndex, false );
              }
              outputRecord.put( avroFieldName, booleanValue );
              break;
            case DATE:
              Date defaultDate = null;
              if ( defaultValue != null && defaultValue.length() > 0 ) {
                String conversionMask =
                  ( vmi.getConversionMask() == null ) ? ValueMetaBase.DEFAULT_DATE_PARSE_MASK : vmi.getConversionMask();
                DateFormat dateFormat = new SimpleDateFormat( conversionMask );
                try {
                  defaultDate = dateFormat.parse( defaultValue );
                } catch ( ParseException pe ) {
                  // Do nothing
                }
              }

              Integer dateInDays = null;
              Date dateFromRow = row.getDate( fieldMetaIndex, defaultDate );

              if ( dateFromRow != null ) {
                ValueMetaInterface valueMeta = row.getValueMeta( fieldMetaIndex );
                TimeZone timeZone = valueMeta.getDateFormatTimeZone();
                if ( timeZone == null ) {
                  timeZone = TimeZone.getDefault();
                }
                LocalDate localDate = dateFromRow.toInstant().atZone( timeZone.toZoneId() ).toLocalDate();
                dateInDays = Math.toIntExact( ChronoUnit.DAYS.between( LocalDate.ofEpochDay( 0 ), localDate ) );
              }
              outputRecord.put( avroFieldName, dateInDays );
              break;
            case FLOAT:
              Float floatValue = null;
              if ( row.isEmptyValue( pentahoFieldName ) ) {
                floatValue =
                  ( defaultValue != null && defaultValue.length() > 0 ) ? Float.parseFloat( defaultValue ) : null;
              } else {
                floatValue = (float) row.getNumber( fieldMetaIndex, 0 );
              }
              if ( floatValue != null ) {
                floatValue = applyScale( floatValue, field );
              }
              outputRecord.put( avroFieldName, floatValue );
              break;
            case DOUBLE:
              Double doubleValue = null;
              if ( row.isEmptyValue( pentahoFieldName ) ) {
                doubleValue =
                  ( defaultValue != null && defaultValue.length() > 0 ) ? Double.parseDouble( defaultValue ) : null;
              } else {
                doubleValue = row.getNumber( fieldMetaIndex, 0 );
              }
              if ( doubleValue != null ) {
                doubleValue = applyScale( doubleValue, field );
              }
              outputRecord.put( avroFieldName, doubleValue );
              break;
            case LONG:
              Long longValue = null;
              if ( row.isEmptyValue( pentahoFieldName ) ) {
                longValue =
                  ( defaultValue != null && defaultValue.length() > 0 ) ? Long.parseLong( defaultValue ) : null;
              } else {
                longValue = row.getInteger( fieldMetaIndex, 0 );
              }
              outputRecord.put( avroFieldName, longValue );
              break;
            case DECIMAL:
              Conversions.DecimalConversion converter = new Conversions.DecimalConversion();
              if ( defaultValue != null && defaultValue.length() > 0 ) {
                BigDecimal defaultBigDecimal = new BigDecimal( field.getDefaultValue() );
                BigDecimal bigDecimal = row.getBigNumber( fieldMetaIndex, defaultBigDecimal );
                LogicalTypes.Decimal decimalType = LogicalTypes.decimal( bigDecimal.precision(), bigDecimal.scale() );
                ByteBuffer byteBuffer = converter.toBytes( bigDecimal, schema, decimalType );
                outputRecord.put( avroFieldName, byteBuffer );
              } else {
                BigDecimal bigDecimal = row.getBigNumber( fieldMetaIndex, null );
                if ( bigDecimal != null ) {
                  bigDecimal = bigDecimal.round( new MathContext( field.getPrecision(),
                    RoundingMode.HALF_UP ) ).setScale( field.getScale(), RoundingMode.HALF_UP );
                  LogicalTypes.Decimal decimalType = LogicalTypes.decimal( bigDecimal.precision(), bigDecimal.scale() );
                  ByteBuffer byteBuffer = converter.toBytes( bigDecimal, schema, decimalType );
                  outputRecord.put( avroFieldName, byteBuffer );
                } else {
                  outputRecord.put( avroFieldName, null );
                }
              }
              break;
            case INTEGER:
              Long tmpLong = null;
              if ( row.isEmptyValue( pentahoFieldName ) ) {
                tmpLong = ( defaultValue != null && defaultValue.length() > 0 ) ? Long.parseLong( defaultValue ) : null;
              } else {
                tmpLong = row.getInteger( fieldMetaIndex, 0 );
              }
              outputRecord.put( avroFieldName, tmpLong != null ? new Integer( tmpLong.intValue() ) : null );
              break;
            case STRING:
              if ( defaultValue != null ) {
                outputRecord.put( avroFieldName, row.getString( fieldMetaIndex, String.valueOf( defaultValue ) ) );
              } else {
                outputRecord.put( avroFieldName, row.getString( fieldMetaIndex, null ) );
              }
              break;
            case BYTES:
              if ( defaultValue != null ) {
                outputRecord.put( avroFieldName,
                  ByteBuffer.wrap( row.getBinary( fieldMetaIndex, vmi.getBinary( defaultValue.getBytes() ) ) ) );
              } else {
                byte[] bytes = row.getBinary( fieldMetaIndex, null );
                outputRecord.put( avroFieldName, bytes != null ? ByteBuffer.wrap( bytes ) : null );
              }
              break;
            case TIMESTAMP_MILLIS:
              Date defaultTimeStamp = null;
              if ( defaultValue != null && defaultValue.length() > 0 ) {
                String conversionMask =
                  ( vmi.getConversionMask() == null ) ? ValueMetaBase.DEFAULT_TIMESTAMP_PARSE_MASK
                    : vmi.getConversionMask();
                DateFormat dateFormat = new SimpleDateFormat( conversionMask );
                try {
                  defaultTimeStamp = dateFormat.parse( defaultValue );
                } catch ( ParseException pe ) {
                  defaultTimeStamp = null;
                }
              }
              Date timeStamp = row.getDate( fieldMetaIndex, defaultTimeStamp );
              outputRecord.put( avroFieldName, timeStamp != null ? timeStamp.getTime() : null );
              break;
          }

        }
      }
    } catch ( ArithmeticException e ) {
      throw new IllegalArgumentException( "The date has too much day from epoch day!", e );
    } catch ( KettleValueException e ) {
      throw new IllegalArgumentException( "some exception while writing avro", e );
    }
    return outputRecord;
  }

  private double applyScale( double number, IAvroOutputField outputField ) {
    if ( outputField.getScale() > 0 ) {
      BigDecimal bd = new BigDecimal( number );
      bd = bd.setScale( outputField.getScale(), BigDecimal.ROUND_HALF_UP );
      number = bd.doubleValue();
    }
    return number;
  }

  private float applyScale( float number, IAvroOutputField outputField ) {
    if ( outputField.getScale() > 0 ) {
      BigDecimal bd = new BigDecimal( number );
      bd = bd.setScale( outputField.getScale(), BigDecimal.ROUND_HALF_UP );
      number = bd.floatValue();
    }
    return number;
  }

  @Override
  public void close() throws IOException {
    nativeAvroRecordWriter.close();
  }
}
