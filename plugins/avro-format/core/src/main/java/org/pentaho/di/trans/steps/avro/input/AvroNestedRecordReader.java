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

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AvroNestedRecordReader implements IPentahoInputFormat.IPentahoRecordReader {
  private final DataFileStream<Object> nativeAvroRecordReader;
  private final Schema avroSchema;
  private final List<? extends IAvroInputField> fields;
  private final AvroNestedReader avroNestedReader;
  private final VariableSpace avroInputStep;
  private Object[] incomingFields;
  private RowMetaAndData nextRow;
  Object[][] expandedRows = null;
  private int nextExpandedRow = 0;
  private RowMetaInterface incomingRowMeta;
  private RowMetaInterface outputRowMeta;
  private boolean isDatum;
  private int nextCallCounter = 0;

  public AvroNestedRecordReader( DataFileStream<Object> nativeAvroRecordReader,
                                 Schema avroSchema, List<? extends IAvroInputField> fields, VariableSpace avroInputStep,
                                 RowMetaInterface incomingRowMeta, Object[] incomingFields,
                                 RowMetaInterface outputRowMeta,
                                 String fileName, boolean isDataBinaryEncoded, int fieldIndexForDataStream,
                                 boolean isDatum ) {

    this.nativeAvroRecordReader = nativeAvroRecordReader;
    this.avroSchema = avroSchema;
    this.fields = fields;
    this.avroInputStep = avroInputStep;
    this.incomingRowMeta = incomingRowMeta;
    this.incomingFields = incomingFields;
    this.isDatum = isDatum;
    this.outputRowMeta = outputRowMeta;

    avroNestedReader = new AvroNestedReader();
    avroNestedReader.m_schemaToUse = avroSchema;
    avroNestedReader.m_incomingRowMeta = incomingRowMeta;
    avroNestedReader.m_outputRowMeta = outputRowMeta;
    avroNestedReader.m_jsonEncoded = !isDataBinaryEncoded;
    avroNestedReader.m_decodingFromField = fieldIndexForDataStream >= 0 ? true : false;
    avroNestedReader.m_fieldToDecodeIndex = fieldIndexForDataStream;

    try {
      if ( nativeAvroRecordReader != null ) { // Is Avro File
        avroNestedReader.m_containerReader = nativeAvroRecordReader;
      } else {

        if ( avroSchema != null ) {
          avroNestedReader.m_datumReader = new GenericDatumReader<Object>( avroSchema );
          if ( fileName != null ) {
            FileObject fileObject = KettleVFS.getFileObject( fileName );
            if ( avroNestedReader.m_jsonEncoded ) {
              avroNestedReader.m_decoder =
                DecoderFactory.get().jsonDecoder( avroSchema, KettleVFS.getInputStream( fileObject ) );
            } else {
              avroNestedReader.m_decoder =
                DecoderFactory.get().binaryDecoder( KettleVFS.getInputStream( fileObject ), null );
            }
          }
        }
      }

    } catch ( Exception e ) {
      e.printStackTrace();
    }

    ArrayList<AvroInputField> castedList = new ArrayList<AvroInputField>();
    for ( IAvroInputField field : fields ) {
      AvroInputField newField = new AvroInputField();
      newField.setAvroFieldName( field.getAvroFieldName() );
      newField.setAvroType( field.getAvroType() );
      newField.setIndexedValues( field.getIndexedValues() );
      newField.setFormatType( field.getFormatType() );
      newField.setPentahoFieldName( field.getPentahoFieldName() );
      newField.setPentahoType( field.getPentahoType() );
      newField.setPrecision( field.getPrecision() );
      newField.setScale( field.getScale() );
      newField.setStringFormat( field.getStringFormat() );

      List<String> indexes = field.getIndexedVals();
      String fieldName = field.getFormatFieldName();
      if ( fieldName.endsWith( "[0]" ) && !indexes.toString().equals( "[]" ) ) {
        String newValue = fieldName.substring( 0, fieldName.lastIndexOf( "[" ) ) + indexes.toString();
        newField.setFormatFieldName( newValue );
      } else {
        newField.setFormatFieldName( field.getFormatFieldName() );
      }

      castedList.add( newField );
    }

    avroNestedReader.m_normalFields = castedList;
    try {
      avroNestedReader.init();
    } catch ( KettleException e ) {
      e.printStackTrace();
    }
  }

  @Override
  public void close() throws IOException {
    if ( nativeAvroRecordReader != null ) {
      nativeAvroRecordReader.close();
    }
  }

  private boolean hasExpandedRows() {
    if ( expandedRows != null ) {
      if ( nextExpandedRow < expandedRows.length ) {
        return true;
      } else {
        incomingFields = null;
      }
    }
    return false;
  }

  @Override
  public Iterator<RowMetaAndData> iterator() {

    return new Iterator<RowMetaAndData>() {

      @Override
      public boolean hasNext() {
        if ( hasExpandedRows() ) {
          return true;
        }
        if ( nativeAvroRecordReader != null && nativeAvroRecordReader.hasNext() ) {
          return true;
        }
        if ( incomingFields != null ) {
          return true;
        }
        if ( isDatum && nextCallCounter == 0 ) {
          return true;
        }
        return false;
      }

      @Override
      public RowMetaAndData next() {
        nextCallCounter++;
        return getNextRowMetaAndData();
      }
    };
  }

  private RowMetaAndData getNextRowMetaAndData() {
    if ( hasExpandedRows() == false ) {
      try {
        nextExpandedRow = 0;
        expandedRows = null;
        expandedRows = avroNestedReader.avroObjectToKettle( incomingFields, avroInputStep );
        if ( expandedRows != null ) {
          nextRow = objectToRowMetaAndData( expandedRows[ nextExpandedRow ] );
        } else {
          return null;
        }
      } catch ( KettleException e ) {
        e.printStackTrace();
      }
    }

    nextRow = objectToRowMetaAndData( expandedRows[ nextExpandedRow ] );
    nextExpandedRow++;
    return nextRow;
  }

  private RowMetaAndData objectToRowMetaAndData( Object[] row ) {
    RowMetaAndData rowMetaAndData = new RowMetaAndData();
    int index = 0;

    int incomingFieldsCount = outputRowMeta.size() - fields.size();
    if ( outputRowMeta != null && incomingFields != null ) {
      for ( index = 0; index < incomingFieldsCount; index++ ) {
        rowMetaAndData.addValue( outputRowMeta.getValueMeta( index ), incomingFields[ index ] );
      }
    }

    for ( IAvroInputField metaField : fields ) {
      rowMetaAndData.addValue( metaField.getPentahoFieldName(), metaField.getPentahoType(), row[ index ] );
      String stringFormat = metaField.getStringFormat();
      if ( ( stringFormat != null ) && ( stringFormat.trim().length() > 0 ) ) {
        rowMetaAndData.getValueMeta( rowMetaAndData.size() - 1 ).setConversionMask( stringFormat );
      }
      index++;
    }

    return rowMetaAndData;
  }
}
