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

package org.pentaho.di.trans.steps.avro.input;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.trans.steps.avro.AvroSpec;
import org.apache.avro.LogicalType;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.commons.vfs2.FileExtensionSelector;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PentahoAvroInputFormat implements IPentahoAvroInputFormat {

  private String fileName;
  private String schemaFileName;
  private List<? extends IAvroInputField> inputFields;
  private List<? extends IAvroLookupField> lookupFields;
  private String inputStreamFieldName;
  private boolean useFieldAsInputStream;
  private boolean useFieldAsSchema;
  private boolean isDataBinaryEncoded;
  private InputStream inputStream;
  private VariableSpace variableSpace;
  private Object[] incomingFields = null;
  private boolean isDatum;
  private String schemaFieldName;
  private Bowl bowl;

  private RowMetaInterface incomingRowMeta;
  private RowMetaInterface outputRowMeta;

  @Override
  public IPentahoRecordReader createRecordReader( IPentahoInputSplit split ) throws Exception {

    DataFileStream<Object> nestedDfs = null;
    if ( !this.isDatum ) {
      nestedDfs = createNestedDataFileStream();
      if ( nestedDfs == null ) {
        throw new Exception( "Unable to read data from file " + fileName );
      }
    }
    Schema avroSchema = readAvroSchema();
    int dataFieldIndex = useFieldAsInputStream ? determineStringFieldIndex( inputStreamFieldName ) : -1;

    return new AvroNestedRecordReader( bowl, nestedDfs, avroSchema, getFields(), variableSpace, incomingRowMeta,
      incomingFields, outputRowMeta, fileName, isDataBinaryEncoded, dataFieldIndex, isDatum );

  }

  @VisibleForTesting
  public Schema readAvroSchema() throws Exception {
    if ( useFieldAsSchema ) {
      return new Schema.Parser().parse( ( (String) incomingFields[ determineStringFieldIndex( schemaFieldName ) ] ) );
    } else {
      if ( schemaFileName != null && schemaFileName.length() > 0 ) {
        return new Schema.Parser().parse( KettleVFS.getInstance( bowl )
                                          .getInputStream( schemaFileName, variableSpace ) );
      } else if ( ( fileName != null && fileName.length() > 0 ) || ( useFieldAsInputStream && inputStream != null ) ) {
        Schema schema;
        DataFileStream<GenericRecord> dataFileStream = createDataFileStream();
        schema = dataFileStream.getSchema();
        dataFileStream.close();
        return schema;
      }
    }
    throw new Exception( "The file you provided does not contain a schema."
      + "  Please choose a schema file, or another file that contains a schema." );
  }

  @Override
  public List<? extends IAvroLookupField> getLookupFields() {
    return lookupFields;
  }

  @Override
  public void setLookupFields( List<? extends IAvroLookupField> lookupFields ) {
    this.lookupFields = lookupFields;
  }

  @Override
  public List<? extends IAvroInputField> getFields() throws Exception {
    if ( this.inputFields != null ) {
      return inputFields;
    } else {
      return getLeafFields();
    }
  }

  @Override
  public void setInputFields( List<? extends IAvroInputField> fields ) throws Exception {
    this.inputFields = fields;
  }

  @Override
  public void setInputFile( String fileName ) throws Exception {
    this.fileName = fileName;
  }

  @Override
  public void setInputSchemaFile( String schemaFileName ) throws Exception {
    this.schemaFileName = schemaFileName;
  }

  @Override
  public String getInputStreamFieldName() {
    return inputStreamFieldName;
  }

  @Override
  public void setInputStreamFieldName( String inputStreamFieldName ) {
    this.inputStreamFieldName = inputStreamFieldName;
    //this.useFieldAsInputStream = inputStreamFieldName != null && !inputStreamFieldName.isEmpty();
  }

  @Override
  public boolean isUseFieldAsInputStream() {
    return useFieldAsInputStream;
  }

  @Override
  public void setInputStream( InputStream inputStream ) {
    this.inputStream = inputStream;
  }


  @Override
  public void setSplitSize( long blockSize ) throws Exception {
    //do nothing 
  }

  @Override
  public void setBowl( Bowl bowl ) {
    this.bowl = bowl;
  }

  private DataFileStream<GenericRecord> createDataFileStream() throws Exception {
    DatumReader<GenericRecord> datumReader;
    if ( useFieldAsInputStream ) {
      datumReader = new GenericDatumReader<GenericRecord>();
      inputStream.reset();
      return new DataFileStream<GenericRecord>( inputStream, datumReader );
    }
    if ( schemaFileName != null && schemaFileName.length() > 0 ) {
      Schema schema = new Schema.Parser().parse( KettleVFS.getInstance( bowl )
                                                 .getInputStream( schemaFileName, variableSpace ) );
      datumReader = new GenericDatumReader<GenericRecord>( schema );
    } else {
      datumReader = new GenericDatumReader<GenericRecord>();
    }
    FileObject fileObject = KettleVFS.getInstance( bowl ).getFileObject( fileName, variableSpace );
    if ( fileObject.isFile() ) {
      this.inputStream = fileObject.getContent().getInputStream();
      return new DataFileStream<>( inputStream, datumReader );
    } else {
      FileObject[] avroFiles = fileObject.findFiles( new FileExtensionSelector("com/pentaho/di/trans/steps/avro") );
      if ( !Utils.isEmpty( avroFiles ) ) {
        this.inputStream = avroFiles[ 0 ].getContent().getInputStream();
        return new DataFileStream<>( inputStream, datumReader );
      }
      return null;
    }
  }

  private DataFileStream<Object> createNestedDataFileStream() throws Exception {
    DatumReader<Object> datumReader;
    if ( useFieldAsInputStream ) {
      datumReader = new GenericDatumReader<Object>();
      inputStream.reset();
      return new DataFileStream<Object>( inputStream, datumReader );
    }
    if ( schemaFileName != null && schemaFileName.length() > 0 ) {
      Schema schema = new Schema.Parser().parse( KettleVFS.getInstance( bowl )
                                                 .getInputStream( schemaFileName, variableSpace ) );
      datumReader = new GenericDatumReader<Object>( schema );
    } else {
      datumReader = new GenericDatumReader<Object>();
    }
    FileObject fileObject = KettleVFS.getInstance( bowl ).getFileObject( fileName, variableSpace );
    if ( fileObject.isFile() ) {
      this.inputStream = fileObject.getContent().getInputStream();
      return new DataFileStream<>( inputStream, datumReader );
    } else {
      FileObject[] avroFiles = fileObject.findFiles( new FileExtensionSelector("com/pentaho/di/trans/steps/avro") );
      if ( !Utils.isEmpty( avroFiles ) ) {
        this.inputStream = avroFiles[ 0 ].getContent().getInputStream();
        return new DataFileStream<>( inputStream, datumReader );
      }
      return null;
    }
  }

  public List<? extends IAvroInputField> getDefaultFields() throws Exception {
    ArrayList<AvroInputField> fields = new ArrayList<>();

    Schema avroSchema = readAvroSchema();
    for ( Schema.Field f : avroSchema.getFields() ) {
      AvroSpec.DataType actualAvroType = findActualDataType( f );
      AvroSpec.DataType supportedAvroType = null;
      if ( actualAvroType != null && isSupported( actualAvroType ) ) {
        supportedAvroType = actualAvroType;
      }

      if ( supportedAvroType == null ) {
        // Todo: log a message about skipping unsupported fields
        continue;
      }

      int pentahoType = 0;
      switch ( supportedAvroType ) {
        case DATE:
          pentahoType = ValueMetaInterface.TYPE_DATE;
          break;
        case DOUBLE:
          pentahoType = ValueMetaInterface.TYPE_NUMBER;
          break;
        case FLOAT:
          pentahoType = ValueMetaInterface.TYPE_NUMBER;
          break;
        case LONG:
          pentahoType = ValueMetaInterface.TYPE_INTEGER;
          break;
        case BOOLEAN:
          pentahoType = ValueMetaInterface.TYPE_BOOLEAN;
          break;
        case INTEGER:
          pentahoType = ValueMetaInterface.TYPE_INTEGER;
          break;
        case STRING:
          pentahoType = ValueMetaInterface.TYPE_STRING;
          break;
        case BYTES:
          pentahoType = ValueMetaInterface.TYPE_BINARY;
          break;
        case DECIMAL:
          pentahoType = ValueMetaInterface.TYPE_BIGNUMBER;
          break;
        case TIMESTAMP_MILLIS:
          pentahoType = ValueMetaInterface.TYPE_TIMESTAMP;
          break;
      }

      // If this is a Pentaho 8 Avro field name, use the ValueMetaInterface type encoded in the Avro field name instead
      FieldName fieldName = parseFieldName( f.name() );
      if ( fieldName != null ) {
        pentahoType = fieldName.type;
      }

      AvroInputField avroInputField = new AvroInputField();
      avroInputField.setFormatFieldName( f.name() );
      avroInputField.setPentahoFieldName( avroInputField.getDisplayableAvroFieldName() );
      avroInputField.setFormatFieldName( f.name() );
      avroInputField.setPentahoType( pentahoType );
      avroInputField.setAvroType( actualAvroType );
      fields.add( avroInputField );
    }

    return fields;
  }

  private AvroSpec.DataType findActualDataType( Schema.Field field ) {
    AvroSpec.DataType avroDataType = null;
    LogicalType logicalType = null;
    Schema.Type primitiveAvroType = null;

    if ( field.schema().getType().equals( Schema.Type.UNION ) ) {
      for ( Schema typeSchema : field.schema().getTypes() ) {
        if ( !typeSchema.getType().equals( Schema.Type.NULL ) ) {
          logicalType = typeSchema.getLogicalType();
          primitiveAvroType = typeSchema.getType();
          break;
        }
      }
    } else {
      logicalType = field.schema().getLogicalType();
      primitiveAvroType = field.schema().getType();
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

    return avroDataType;
  }

  private boolean isSupported( AvroSpec.DataType actualAvroType ) {
    return ( actualAvroType == AvroSpec.DataType.DATE )
      || ( actualAvroType == AvroSpec.DataType.DECIMAL )
      || ( actualAvroType == AvroSpec.DataType.TIMESTAMP_MILLIS )
      || ( actualAvroType.isPrimitiveType()
      && actualAvroType != AvroSpec.DataType.NULL );
  }

  public static FieldName parseFieldName( String fieldName ) {
    if ( fieldName == null || !fieldName.contains( FieldName.FIELDNAME_DELIMITER ) ) {
      return null;
    }

    String[] splits = fieldName.split( FieldName.FIELDNAME_DELIMITER );

    if ( splits.length == 0 || splits.length > 3 ) {
      return null;
    } else {
      return new FieldName( splits[ 0 ], Integer.valueOf( splits[ 1 ] ), Boolean.parseBoolean( splits[ 2 ] ) );
    }
  }

  /**
   * @deprecated This is only used to read the schema generated using 8.0
   */
  public static class FieldName {
    public final String name;
    public final int type;
    public final boolean allowNull;
    public static final String FIELDNAME_DELIMITER = "_delimiter_";

    public FieldName( String name, int type, boolean allowNull ) {
      this.name = name;
      this.type = type;
      this.allowNull = allowNull;
    }

    public String getLegacyFieldName() {
      return name + FIELDNAME_DELIMITER + type + FIELDNAME_DELIMITER + allowNull;
    }
  }

  public VariableSpace getVariableSpace() {
    return variableSpace;
  }

  @Override
  public void setVariableSpace( VariableSpace variableSpace ) {
    this.variableSpace = variableSpace;
  }

  public void setIncomingFields( Object[] incomingFields ) {
    this.incomingFields = incomingFields;
  }

  public Object[] getIncomingFields() {
    return incomingFields;
  }

  public RowMetaInterface getOutputRowMeta() {
    return outputRowMeta;
  }

  @Override
  public void setIncomingRowMeta( RowMetaInterface incomingRowMeta ) {
    this.incomingRowMeta = incomingRowMeta;
  }

  @Override
  public void setOutputRowMeta( RowMetaInterface outputRowMeta ) {
    this.outputRowMeta = outputRowMeta;
  }

  @Override
  public List<? extends IAvroInputField> getLeafFields() throws Exception {
    List<? extends IAvroInputField> inputFields = null;
    Schema s = readAvroSchema();
    inputFields = AvroNestedFieldGetter.getLeafFields( s );
    return inputFields;
  }

  @Override
  public void setIsDataBinaryEncoded( boolean isBinary ) {
    this.isDataBinaryEncoded = isBinary;
  }

  @Override
  public void setDatum( boolean isDatum ) {
    this.isDatum = isDatum;
  }

  @Override
  public void setUseFieldAsSchema( boolean useFieldAsSchema ) {
    this.useFieldAsSchema = useFieldAsSchema;
  }

  @Override
  public void setSchemaFieldName( String schemaFieldName ) {
    this.schemaFieldName = schemaFieldName;
  }

  @Override
  public void setUseFieldAsInputStream( boolean useFieldAsInputStream ) {
    this.useFieldAsInputStream = useFieldAsInputStream;
  }

  private int determineStringFieldIndex( String fieldName ) throws Exception {
    int index = incomingRowMeta.indexOfValue( fieldName );
    if ( index >= 0 ) {
      ValueMetaInterface fieldMeta = incomingRowMeta.getValueMeta( index );
      if ( !fieldMeta.isString() && !fieldMeta.isBinary() ) {
        throw new Exception( "Field " + fieldName + " is not a string." );
      }
      return index;
    }
    throw new Exception( "Could not locate field " + fieldName );
  }
}
