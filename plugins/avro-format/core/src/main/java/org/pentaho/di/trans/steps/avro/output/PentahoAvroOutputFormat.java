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

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.trans.steps.avro.AvroSpec;
import org.apache.avro.Schema;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.FileAlreadyExistsException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Deflater;

/**
 * @author tkafalas
 */
public class PentahoAvroOutputFormat implements IPentahoAvroOutputFormat {
  private String outputFilename;
  private List<? extends IAvroOutputField> fields;
  private CodecFactory codecFactory;

  private String nameSpace;
  private String recordName;
  private String docValue;
  private String schemaFilename;
  private Schema schema = null;
  ObjectNode schemaObjectNode = null;
  private VariableSpace variableSpace;

  @Override
  public IPentahoRecordWriter createRecordWriter() throws Exception {
    validate();
    if ( fields == null || StringUtils.isEmpty( nameSpace ) || StringUtils.isEmpty( recordName ) || StringUtils
      .isEmpty( outputFilename ) ) {
      throw new Exception(
        "Invalid state.  One of the following required fields is null:  'nameSpace', 'recordNum', or 'outputFileName" );
    }
    Schema schema = getSchema();
    writeAvroSchemaToFile( schemaFilename );
    DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>( schema );
    DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<GenericRecord>( datumWriter );
    dataFileWriter.setCodec( codecFactory );
    dataFileWriter.create( schema, KettleVFS.getOutputStream( outputFilename, variableSpace, false ) );
    return new PentahoAvroRecordWriter( dataFileWriter, schema, fields );
  }

  private void validate() throws Exception {
    SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy/mm/dd HH:mm:ss" );
    String date = dateFormat.format( new Date() );

    StringBuffer errors = new StringBuffer();
    if ( StringUtils.isEmpty( outputFilename ) ) {
      errors.append( "\n" );
      errors.append( date + " - Please set Folder/File name" );
    }
    if ( StringUtils.isEmpty( nameSpace ) ) {
      errors.append( "\n" );
      errors.append( date + " - Please set the Avro Schema Namespace" );
    }
    if ( StringUtils.isEmpty( recordName ) ) {
      errors.append( "\n" );
      errors.append( date + " - Please set the Avro Schema Record name." );
    }
    if ( !StringUtils.isEmpty( errors.toString() ) ) {
      throw new Exception( errors.toString() );
    }
  }

  @Override
  public void setFields( List<? extends IAvroOutputField> fields ) throws Exception {
    this.fields = fields;
    schema = null;
    schemaObjectNode = null;
  }


  @Override public void setOutputFile( String file, boolean override ) throws Exception {
    if ( !override && KettleVFS.fileExists( file, variableSpace ) ) {
      throw new FileAlreadyExistsException( file );
    }

    this.outputFilename = file;
  }

  @Override
  public void setCompression( COMPRESSION compression ) {
    switch ( compression ) {
      case SNAPPY:
        codecFactory = CodecFactory.snappyCodec();
        break;
      case DEFLATE:
        codecFactory = CodecFactory.deflateCodec( Deflater.DEFAULT_COMPRESSION );
        break;
      default:
        codecFactory = CodecFactory.nullCodec();
        break;
    }
  }

  @Override
  public void setNameSpace( String namespace ) {
    this.nameSpace = namespace;
    schema = null;
    schemaObjectNode = null;
  }

  @Override
  public void setRecordName( String recordName ) {
    this.recordName = recordName;
    schema = null;
    schemaObjectNode = null;
  }

  @Override
  public void setDocValue( String docValue ) {
    this.docValue = docValue;
    schema = null;
    schemaObjectNode = null;
  }

  @Override
  public void setSchemaFilename( String schemaFilename ) {
    this.schemaFilename = schemaFilename;
  }

  protected Schema getSchema() {
    if ( schema == null ) {
      ObjectNode schemaObjectNode = getSchemaObjectNode();
      if ( schemaObjectNode != null ) {
        schema = new Schema.Parser().parse( schemaObjectNode.toString() );
      }
    }
    return schema;
  }

  protected ObjectNode getSchemaObjectNode() {
    if ( schemaObjectNode == null ) {
      if ( fields != null ) {
        ObjectMapper mapper = new ObjectMapper();
        schemaObjectNode = mapper.createObjectNode();

        schemaObjectNode.put( AvroSpec.NAMESPACE_NODE, nameSpace );
        schemaObjectNode.put( AvroSpec.TYPE_NODE, AvroSpec.TYPE_RECORD );
        schemaObjectNode.put( AvroSpec.NAME_NODE, recordName );
        schemaObjectNode.put( AvroSpec.DOC, docValue );

        ArrayNode fieldNodes = mapper.createArrayNode();
        Iterator<? extends IAvroOutputField> fields = this.fields.iterator();
        while ( fields.hasNext() ) {
          IAvroOutputField f = fields.next();
          if ( f.getAvroType() == null ) {
            throw new RuntimeException( "Field: " + f.getFormatFieldName() + " has undefined type. " );
          }

          AvroSpec.DataType type = f.getAvroType();
          ObjectNode fieldNode = mapper.createObjectNode();

          fieldNode.put( AvroSpec.NAME_NODE, f.getFormatFieldName() );
          if ( type.isPrimitiveType() ) {
            if ( f.getAllowNull() ) {
              ArrayNode arrayNode = mapper.createArrayNode().add( AvroSpec.DataType.NULL.getType() );
              arrayNode.add( type.getType() );
              fieldNode.putPOJO( AvroSpec.TYPE_NODE, arrayNode );
            } else {
              fieldNode.put( AvroSpec.TYPE_NODE, type.getType() );
            }
          } else {
            ObjectNode typeNode = mapper.createObjectNode();
            typeNode.put( AvroSpec.LOGICAL_TYPE, type.getLogicalType() );
            typeNode.put( AvroSpec.TYPE_NODE, type.getBaseType() );

            if ( AvroSpec.DataType.DECIMAL == type ) {
              typeNode.put( AvroSpec.DECIMAL_PRECISION, f.getPrecision() );
              typeNode.put( AvroSpec.DECIMAL_SCALE, f.getScale() );
            }

            if ( f.getAllowNull() ) {
              ArrayNode arrayNode = mapper.createArrayNode().add( AvroSpec.DataType.NULL.getType() );
              arrayNode.add( typeNode );
              fieldNode.set( AvroSpec.TYPE_NODE, arrayNode );
            } else {
              fieldNode.set( AvroSpec.TYPE_NODE, typeNode );
            }
          }
          if ( f.getDefaultValue() != null && StringUtils.isNotEmpty( f.getDefaultValue() ) ) {
            //default value has to be converted to required JsonNode type for further validation in Schema.class (org.apache.avro.Schema.java)
            JsonNode defaultObject = mapper.convertValue( mapDefaultValuesToDataTypes( f.getDefaultValue(), type ), JsonNode.class );
            fieldNode.putIfAbsent( AvroSpec.DEFAULT_NODE, defaultObject );
          }
          fieldNodes.add( fieldNode );
        }
        schemaObjectNode.putPOJO( AvroSpec.FIELDS_NODE, fieldNodes );
      }
    }
    return schemaObjectNode;
  }

  protected void writeAvroSchemaToFile( String schemaFilename ) throws KettleFileException, IOException {
    ObjectNode schemaObjectNode = this.getSchemaObjectNode();
    if ( schemaObjectNode != null && schemaFilename != null ) {
      ObjectMapper mapper = new ObjectMapper();
      ObjectWriter writer = mapper.writer( new DefaultPrettyPrinter() );
      writer.writeValue( KettleVFS.getOutputStream( schemaFilename, variableSpace, false ), schemaObjectNode );
    }
  }

  @Override
  public void setVariableSpace( VariableSpace variableSpace ) {
    this.variableSpace = variableSpace;
  }
  /**
   * Converts the provided default value(String.class) to corresponding data type as provided by 'type' parameter.
   * <p>
   * @param defaultValue The default value provided.
   * @param type The AvroSpec data type.
   * @return The converted data type object.
   */
  private Object mapDefaultValuesToDataTypes( String defaultValue, AvroSpec.DataType type ) {
    try {
      switch ( type ) {
        case BOOLEAN:
          return Boolean.parseBoolean( defaultValue );
        case DATE:
          return getDateTypeForDefaultValue( defaultValue );
        case FLOAT:
          return Float.parseFloat( defaultValue );
        case DOUBLE:
          return Double.parseDouble( defaultValue );
        case LONG:
        case INTEGER:
          return Long.parseLong( defaultValue );
        case DECIMAL:
          return getDecimalTypeForDefaultValue( defaultValue );
        case STRING:
          return String.valueOf( defaultValue );
        case BYTES:
          return defaultValue.getBytes();
        case TIMESTAMP_MILLIS:
          return getTimeStampTypeForDefaultValue( defaultValue );
        default:
          return defaultValue;
      }
    } catch ( NumberFormatException | ParseException exception ) {
      throw new IllegalArgumentException( "Not a valid default value " + defaultValue + " for data type " + type + " - " + exception.getMessage());
    }

  }

  /**
   * Converts the default value as Big decimal value and return bytes.
   * <p>
   * @param defaultValue The default value provided.
   * @return The byte array of big decimal value.
   */
  private byte[] getDecimalTypeForDefaultValue( String defaultValue ) {
    BigDecimal bigDecimalValue = new BigDecimal( defaultValue );
    return bigDecimalValue.unscaledValue().toByteArray();
  }

  /**
   * Converts the default value to Date.
   *
   * <P>
   *  ValueMetaBase.DEFAULT_DATE_PARSE_MASK is the default date parser used for
   *  Avro To PDI and vice-versa conversions.
   * @see PentahoAvroRecordWriter#createAvroRecord
   * @see org.pentaho.di.trans.steps.avro.AvroToPdiConverter #convertToPentahoType(int, String, IAvroInputField)
   * </P>
   *
   * <p>
   * @param defaultValue The default value provided.
   * @return The integer value of converted default value to Date.
   */
  private int getDateTypeForDefaultValue( String defaultValue ) throws ParseException {
    Date defaultDate;
    DateFormat dateFormat = new SimpleDateFormat( ValueMetaBase.DEFAULT_DATE_PARSE_MASK );

    try {
      defaultDate = dateFormat.parse( defaultValue );
      LocalDate localDate = defaultDate.toInstant()
              .atZone( ZoneId.systemDefault() )
              .toLocalDate();
      return (int) localDate.toEpochDay();
    } catch ( ParseException pe ) {
      throw new ParseException( "The default value " + defaultValue + " should be of format : " + ValueMetaBase.DEFAULT_DATE_PARSE_MASK, pe.getErrorOffset() );
    }

  }

  /**
   * Converts the default value to TimeStamp(in Milliseconds).
   *
   * <P>
   *  ValueMetaBase.DEFAULT_TIMESTAMP_PARSE_MASK is the default timestamp parser used for
   *  Avro To PDI and vice-versa conversions.
   * @see PentahoAvroRecordWriter#createAvroRecord
   * </P>
   *
   * <p>
   * @param defaultValue The default value provided.
   * @return The long value of converted default value to Date and Time.
   */
  private long getTimeStampTypeForDefaultValue( String defaultValue ) throws ParseException {
    Date defaultTimeStamp;
    DateFormat dateFormat = new SimpleDateFormat( ValueMetaBase.DEFAULT_TIMESTAMP_PARSE_MASK );

    try {
      defaultTimeStamp = dateFormat.parse( defaultValue );
      return defaultTimeStamp.getTime();
    } catch ( ParseException pe ) {
      throw new ParseException( "The default value " + defaultValue + " should be of format : " + ValueMetaBase.DEFAULT_TIMESTAMP_PARSE_MASK, pe.getErrorOffset() );
    }
  }
}
