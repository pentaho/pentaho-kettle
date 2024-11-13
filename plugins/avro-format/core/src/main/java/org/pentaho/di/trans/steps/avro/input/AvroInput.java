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

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.file.IBaseFileInputReader;

import javax.tools.FileObject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvroInput extends BaseStep {

  public class IndexedLookupField extends AvroLookupField {
    int index = -1;

    public int getIndex() {
      return index;
    }

    public void setIndex( int index ) {
      this.index = index;
    }
  }

  private Object[] inputToStepRow;
  protected AvroInputMeta meta;
  protected AvroInputData data;

  public AvroInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  private IndexedLookupField resolveLookupField( IAvroLookupField lookupField ) {
    IndexedLookupField indexedLookupField = new IndexedLookupField();
    RowMetaInterface inputRowMeta = getInputRowMeta();

    int index = inputRowMeta.indexOfValue( lookupField.getFieldName() );
    if ( index < 0 ) {
      return null;
    }
    indexedLookupField.setIndex( index );

    String variableName = lookupField.getVariableName();
    if ( Utils.isEmpty( variableName ) ) {
      return null;
    }
    indexedLookupField.setVariableName( variableName.replaceAll( "\\.", "_" ) );

    indexedLookupField.setFieldName( this.environmentSubstitute( lookupField.getFieldName() ) );
    indexedLookupField.setDefaultValue( this.environmentSubstitute( lookupField.getDefaultValue() ) );

    return indexedLookupField;
  }


  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (AvroInputMeta) smi;
    data = (AvroInputData) sdi;

    do {
      try {
        if ( data.input == null || data.reader == null || data.rowIterator == null ) {
          if ( !initializeSource() ) {
            break; //We have processed all rows streaming in
          }
        }

        if ( data.rowIterator.hasNext() ) {
          updateVariableSpaceWithLookupFields( getInputRowMeta() );
          RowMetaAndData row = data.rowIterator.next();
          putRow( row.getRowMeta(), row.getData() );
          return true;
        }
        //Finished with Avro file
        fileFinishedHousekeeping();

      } catch ( KettleException ex ) {
        throw ex;
      } catch ( Exception ex ) {
        throw new KettleException( ex );
      }

    } while ( ( meta.getDataLocationType() == AvroInputMetaBase.LocationDescriptor.FIELD_NAME ) );

    setOutputDone();
    return false;
  }

  private void updateVariableSpaceWithLookupFields( RowMetaInterface rowMeta ) {
    for ( IAvroLookupField lookupField : data.input.getLookupFields() ) {
      String valueToSet = "";
      try {
        ValueMetaInterface valueMeta = rowMeta.getValueMeta( ( (IndexedLookupField) lookupField ).getIndex() );
        if ( valueMeta.isNull( this.inputToStepRow[ ( (IndexedLookupField) lookupField ).getIndex() ] ) ) {
          if ( !Utils.isEmpty( lookupField.getDefaultValue() ) ) {
            valueToSet = lookupField.getDefaultValue();
          } else {
            valueToSet = "null";
          }
        } else {
          valueToSet = valueMeta.getString( this.inputToStepRow[ ( (IndexedLookupField) lookupField ).getIndex() ] );
        }
      } catch ( Exception e ) {
        valueToSet = "null";
      }

      this.setVariable( lookupField.getVariableName(), valueToSet );
    }
  }

  private void fileFinishedHousekeeping() {
    try {
      if ( data.reader != null ) {
        data.reader.close();
      }
    } catch ( IOException e ) {
      //Don't care if we can't close
    }
    data.reader = null;
    data.input = null;
  }

  public static List<? extends IAvroInputField> getLeafFields(String schemaPath, String dataPath, VariableSpace variableSpace ) throws Exception {
    IPentahoAvroInputFormat in = new PentahoAvroInputFormat();
    in.setVariableSpace( variableSpace );
    in.setInputSchemaFile( schemaPath );
    in.setInputFile( dataPath );
    return in.getLeafFields();
  }

  /**
   * @return false if no rows to process
   * @throws Exception
   */
  private boolean initializeSource() throws Exception {
    inputToStepRow = getRow();
    if ( inputToStepRow == null && ( meta.getDataLocationType()
      == AvroInputMetaBase.LocationDescriptor.FIELD_NAME ) ) {
      fileFinishedHousekeeping();
      return false;
    }
    if ( meta.getDataLocation() == null ) {
      throw new KettleException( "No data location defined" );
    }

    // setup the output row meta
    RowMetaInterface outRowMeta = null;
    outRowMeta = getInputRowMeta();
    if ( outRowMeta != null ) {
      outRowMeta = outRowMeta.clone();
    } else {
      outRowMeta = new RowMeta();
    }

    data.input = new PentahoAvroInputFormat();
    meta.getFields( outRowMeta, getStepname(), null, null, this, null, null );
    data.input.setIncomingRowMeta( getInputRowMeta() );
    data.input.setOutputRowMeta( outRowMeta );
    Boolean isDatum = false;
    Boolean useFieldAsSchema = false;
    String inputSchemaFileName = null;
    String inputFileName = null;
    data.input.setVariableSpace( this.getTransMeta() );

    AvroInputMetaBase.SourceFormat sourceFormat = AvroInputMetaBase.SourceFormat.values[ meta.getFormat() ];
    if ( sourceFormat == AvroInputMetaBase.SourceFormat.DATUM_BINARY
      || sourceFormat == AvroInputMetaBase.SourceFormat.DATUM_JSON ) {
      isDatum = true;
    }
    if ( sourceFormat != AvroInputMetaBase.SourceFormat.DATUM_JSON ) {
      data.input.setIsDataBinaryEncoded( true );
    }

    if ( meta.getDataLocationType() == AvroInputMetaBase.LocationDescriptor.FILE_NAME ) {
      inputFileName =
        meta.getParentStepMeta().getParentTransMeta().environmentSubstitute( meta.getDataLocation() );
    }

    if ( meta.getSchemaLocationType() == AvroInputMetaBase.LocationDescriptor.FILE_NAME ) {
      inputSchemaFileName =
        meta.getParentStepMeta().getParentTransMeta().environmentSubstitute( meta.getSchemaLocation() );
      data.input.setInputSchemaFile( inputSchemaFileName );
    } else {
      useFieldAsSchema = true;
      data.input.setSchemaFieldName( meta.getSchemaLocation() );
    }
    data.input.setDatum( isDatum );
    data.input.setUseFieldAsSchema( useFieldAsSchema );

    if ( !isDatum && inputFileName != null ) {
      checkForLegacyFieldNames( inputSchemaFileName, inputFileName );
    }
    data.input.setInputFields( Arrays.asList( meta.getInputFields() ) );

    ArrayList<IndexedLookupField> lookupFields = new ArrayList<>();
    if ( getInputRowMeta() != null ) {
      for ( AvroLookupField lookupField : meta.getLookupFields() ) {
        IndexedLookupField indexedLookupField = resolveLookupField( lookupField );
        if ( indexedLookupField != null ) {
          lookupFields.add( indexedLookupField );
        }
      }
    }
    data.input.setLookupFields( lookupFields );

    if ( inputFileName != null ) {
      data.input.setInputFile( inputFileName );
      data.input.setInputStreamFieldName( null );
    } else if ( meta.getDataLocationType() == AvroInputMetaBase.LocationDescriptor.FIELD_NAME ) {
      data.input.setInputStreamFieldName( meta.getDataLocation() );
      data.input.setUseFieldAsInputStream( true );
      int fieldIndex = getInputRowMeta().indexOfValue( data.input.getInputStreamFieldName() );
      if ( fieldIndex == -1 ) {
        throw new KettleException(
          "Field '" + data.input.getInputStreamFieldName() + "' was not found in step's input fields" );
      }
      data.input
        .setInputStream( new ByteArrayInputStream( getInputRowMeta().getBinary( inputToStepRow, fieldIndex ) ) );
    } else {
      throw new KettleException( "Unknown field location type" );
    }

    data.input.setIncomingFields( inputToStepRow );
    data.reader = data.input.createRecordReader( null );
    data.rowIterator = data.reader.iterator();

    return true;
  }


  public void checkForLegacyFieldNames( String schemaFileName, String avroFileName ) {
    // This routine will detect any field names in the schema that use the "_delimiter_" hack introduced in 8.0, find
    // the truncated avro field names in the field list and rename them to what the avro file actually has.
    try {
      if ( !data.input.isUseFieldAsInputStream() ) {
        List<? extends IAvroInputField> rawAvroFields = AvroInput
          .getLeafFields( schemaFileName, avroFileName, this.getTransMeta() );
        Map<String, String> hackedFieldNames = new HashMap<>();
        int pointer;
        String fieldName;
        for ( IAvroInputField rawField : rawAvroFields ) {
          fieldName = rawField.getAvroFieldName();
          pointer = fieldName.indexOf( AvroInputField.FILENAME_DELIMITER );
          if ( pointer >= 0 ) {
            hackedFieldNames.put( fieldName.substring( 0, pointer ), fieldName );
          }
        }
        if ( hackedFieldNames.size() > 0 ) {
          //remove any items that also have the short fieldNname in the avro file
          rawAvroFields.stream().forEach( rawField -> {
            if ( hackedFieldNames.containsKey( rawField.getAvroFieldName() ) ) {
              hackedFieldNames.remove( rawField.getAvroFieldName() );
            }
          } );
        }

        //Now expand fieldNames that need it
        if ( hackedFieldNames.size() > 0 ) {
          for ( AvroInputField field : meta.getInputFields() ) {
            if ( hackedFieldNames.containsKey( field.getName() ) ) {
              field.setAvroFieldName( hackedFieldNames.get( field.getName() ) );
            }
          }
        }
      }
    } catch ( Exception e ) {
      // Swallow any exception - Inability to check for legacy hacked fields should not be fatal in itself
    }
  }

  protected boolean init() {
    return true;
  }

  @Override public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    //Set Embedded NamedCluter MetatStore Provider Key so that it can be passed to VFS
    if ( getTransMeta().getNamedClusterEmbedManager() != null ) {
      getTransMeta().getNamedClusterEmbedManager()
        .passEmbeddedMetastoreKey( getTransMeta(), getTransMeta().getEmbeddedMetastoreProviderKey() );
    }
    return super.init( smi, sdi );
  }

  protected IBaseFileInputReader createReader( AvroInputMeta meta, AvroInputData data, FileObject file )
    throws Exception {
    return null;
  }

}
