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

import java.io.IOException;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class AvroOutput extends BaseStep implements StepInterface {

  private AvroOutputMeta meta;

  private AvroOutputData data;

  public AvroOutput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                     Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public synchronized boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    try {
      meta = (AvroOutputMeta) smi;
      data = (AvroOutputData) sdi;

      if ( data.output == null ) {
        try {
          init();
        } catch ( UnsupportedOperationException e ) {
          getLogChannel().logError( e.getMessage() );
          setErrors( 1 );
          setOutputDone();
          return false;
        } catch ( Exception e ) {
          String error = e.getMessage().replaceAll( "TRANS_NAME", getTrans().getName() );
          error = error.replaceAll( "STEP_NAME", getStepname() );
          getLogChannel().logError( error );
          setErrors( 1 );
          setOutputDone();
          return false;
        }
      }

      Object[] currentRow = getRow();
      if ( currentRow != null ) {
        //create new outputMeta
        RowMetaInterface outputRMI = new RowMeta();
        //create data equals with output fileds
        Object[] outputData = new Object[ meta.getOutputFields().size() ];
        for ( int i = 0; i < meta.getOutputFields().size(); i++ ) {
          int inputRowIndex = getInputRowMeta().indexOfValue( meta.getOutputFields().get( i ).getPentahoFieldName() );
          if ( inputRowIndex == -1 ) {
            throw new KettleException( "Field name [" + meta.getOutputFields().get( i ).getPentahoFieldName()
              + " ] couldn't be found in the input stream!" );
          } else {
            ValueMetaInterface vmi = ValueMetaFactory.cloneValueMeta( getInputRowMeta().getValueMeta( inputRowIndex ) );
            //add output value meta according output fields
            outputRMI.addValueMeta( i, vmi );
            //add output data according output fields
            outputData[ i ] = currentRow[ inputRowIndex ];
          }
        }
        RowMetaAndData row = new RowMetaAndData( outputRMI, outputData );
        data.writer.write( row );
        putRow( row.getRowMeta(), row.getData() );
        return true;
      } else {
        // no more input to be expected...
        closeWriter();
        setOutputDone();
        return false;
      }
    } catch ( KettleException ex ) {
      throw ex;
    } catch ( Exception ex ) {
      throw new KettleException( ex );
    }
  }

  public void init() throws Exception {
    //Set Embedded NamedCluter MetatStore Provider Key so that it can be passed to VFS
    if ( getTransMeta().getNamedClusterEmbedManager() != null ) {
      getTransMeta().getNamedClusterEmbedManager()
        .passEmbeddedMetastoreKey( getTransMeta(), getTransMeta().getEmbeddedMetastoreProviderKey() );
    }
    TransMeta parentTransMeta = meta.getParentStepMeta().getParentTransMeta();
    data.output = new PentahoAvroOutputFormat();
    data.output.setBowl( getTransMeta().getBowl() );
    data.output
      .setOutputFile( parentTransMeta.environmentSubstitute( meta.constructOutputFilename( meta.getFilename() ) ),
        meta.isOverrideOutput() );
    data.output.setFields( meta.getOutputFields() );
    data.output.setVariableSpace( parentTransMeta );
    IPentahoAvroOutputFormat.COMPRESSION compression;
    try {
      compression = IPentahoAvroOutputFormat.COMPRESSION
        .valueOf( parentTransMeta.environmentSubstitute( meta.getCompressionType() ).toUpperCase() );
    } catch ( Exception ex ) {
      compression = IPentahoAvroOutputFormat.COMPRESSION.UNCOMPRESSED;
    }
    data.output.setCompression( compression );
    data.output.setNameSpace( parentTransMeta.environmentSubstitute( meta.getNamespace() ) );
    data.output.setRecordName( parentTransMeta.environmentSubstitute( meta.getRecordName() ) );
    data.output.setDocValue( parentTransMeta.environmentSubstitute( meta.getDocValue() ) );
    if ( meta.getSchemaFilename() != null && meta.getSchemaFilename().length() != 0 ) {
      data.output.setSchemaFilename(
        parentTransMeta.environmentSubstitute( meta.constructOutputFilename( meta.getSchemaFilename() ) ) );
    }
    data.writer = data.output.createRecordWriter();
  }

  public void closeWriter() throws KettleException {
    try {
      data.writer.close();
    } catch ( IOException e ) {
      throw new KettleException( e );
    }
    data.output = null;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (AvroOutputMeta) smi;
    data = (AvroOutputData) sdi;
    return super.init( smi, sdi );
  }
}
