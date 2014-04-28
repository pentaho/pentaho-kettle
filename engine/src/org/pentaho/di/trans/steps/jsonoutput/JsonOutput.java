/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.jsonoutput;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.vfs.FileObject;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ObjectNode;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Converts input rows to one or more XML files.
 * 
 * @author Matt
 * @since 14-jan-2006
 */
public class JsonOutput extends BaseStep implements StepInterface {
  private static Class<?> PKG = JsonOutput.class; // for i18n purposes, needed by Translator2!!

  private JsonOutputMeta meta;
  private JsonOutputData data;

  private interface CompatibilityFactory {
    public void execute( Object[] row ) throws KettleException;
    JsonNode getJsonNode();
  }

  private class CompatibilityMode implements CompatibilityFactory {
    @Override
    public void execute( Object[] row ) throws KettleException {

      for ( int i = 0; i < data.nrFields; i++ ) {
        JsonOutputField outputField = meta.getOutputFields()[i];

        ValueMetaInterface v = data.inputRowMeta.getValueMeta( data.fieldIndexes[i] );

        // Create a new object with specified fields
        ObjectNode jo = data.mapper.createObjectNode();
        addJsonField( jo, outputField, v, row, i );
        data.jsonArray.add( jo );
      }
      attemptToFlush( row );
    }

    /**
     * Avoid make changes to compatibility mode.
     */
    @Override
    public JsonNode getJsonNode() {
      ObjectNode jsonDoc = data.mapper.createObjectNode();
      jsonDoc.put( data.realBlocName, data.jsonArray );
      return jsonDoc;
    }
  }

  private class FixedMode implements CompatibilityFactory {
    @Override
    public void execute( Object[] row ) throws KettleException {

      // Create a new object with specified fields
      ObjectNode jo = data.mapper.createObjectNode();

      for ( int i = 0; i < data.nrFields; i++ ) {
        JsonOutputField outputField = meta.getOutputFields()[i];
        ValueMetaInterface v = data.inputRowMeta.getValueMeta( data.fieldIndexes[i] );
        addJsonField( jo, outputField, v, row, i );
      }
      data.jsonArray.add( jo );
      attemptToFlush( row );
    }

    /**
     * based on patch from PDI-7243
     */
    @Override
    public JsonNode getJsonNode() {
      JsonNode jsonDoc;
      if ( !data.realBlocName.isEmpty() ) {
        ObjectNode jsonObj = data.mapper.createObjectNode();

        if ( data.nrRowsInBloc == 1 ) {
          // rows in bloc == 1 is special case: do not output array, just output the data as object
          if ( data.jsonArray.size() > 0 ) {
            jsonObj.put( data.realBlocName, data.jsonArray.get( 0 ) );
          } else {
            // empty object, since JSON doesn't support
            jsonObj.put( data.realBlocName, data.mapper.createObjectNode() );
          }
          // direct 'null'
        } else {
          jsonObj.put( data.realBlocName, data.jsonArray );
        }
        jsonDoc = jsonObj;
      } else {
        if ( data.nrRowsInBloc == 1 ) {
          // rows in bloc == 1 is special case: do not output array, just output the data as object
          if ( data.jsonArray.size() > 0 ) {
            jsonDoc = data.jsonArray.get( 0 );
          } else {
            jsonDoc = data.mapper.createObjectNode(); // empty object, since JSON doesn't support direct 'null'
          }
        } else {
          jsonDoc = data.jsonArray;
        }
      }
      return jsonDoc;
    }
  }

  private void attemptToFlush( Object[] row ) throws KettleStepException {
    data.nrRow++;
    if ( data.nrRowsInBloc > 0 ) {
      if ( data.nrRow % data.nrRowsInBloc == 0 ) {
        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "JsonOutputLog.OutputingRow" ), data.nrRow );
        }
        data.nrRow = 0; //aware of integer overflow
        // We can now output an object
        outPutRow( row );
      }
    }
  }

  private void addJsonField( ObjectNode jo, JsonOutputField outputField, ValueMetaInterface v, Object[] row, int i ) throws KettleValueException {
    if ( data.inputRowMeta.isNull( row, data.fieldIndexes[i] ) ) {
      jo.put( outputField.getElementName(), (String) null );
    } else {
      switch ( v.getType() ) {
        case ValueMeta.TYPE_BOOLEAN:
          jo.put( outputField.getElementName(), data.inputRowMeta.getBoolean( row, data.fieldIndexes[i] ) );
          break;
        case ValueMeta.TYPE_INTEGER:
          jo.put( outputField.getElementName(), data.inputRowMeta.getInteger( row, data.fieldIndexes[i] ) );
          break;
        case ValueMeta.TYPE_NUMBER:
          jo.put( outputField.getElementName(), data.inputRowMeta.getNumber( row, data.fieldIndexes[i] ) );
          break;
        case ValueMeta.TYPE_BIGNUMBER:
          jo.put( outputField.getElementName(), data.inputRowMeta.getBigNumber( row, data.fieldIndexes[i] ) );
          break;
        default:
          jo.put( outputField.getElementName(), data.inputRowMeta.getString( row, data.fieldIndexes[i] ) );
          break;
      }
    }
  }

  private CompatibilityFactory compatibilityFactory;

  public JsonOutput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );

    // Here we decide whether or not to build the structure in
    // compatible mode or fixed mode
    JsonOutputMeta jsonOutputMeta = (JsonOutputMeta) ( stepMeta.getStepMetaInterface() );
    if ( jsonOutputMeta.isCompatibilityMode() ) {
      compatibilityFactory = new CompatibilityMode();
    } else {
      compatibilityFactory = new FixedMode();
    }
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    meta = (JsonOutputMeta) smi;
    data = (JsonOutputData) sdi;

    Object[] r = getRow(); // This also waits for a row to be finished.
    if ( r == null ) {
      // no more input to be expected...
      if ( !data.rowsAreSafe ) {
        // Let's output the remaining unsafe data
        outPutRow( r );
      }

      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;
      data.inputRowMeta = getInputRowMeta();
      data.inputRowMetaSize = data.inputRowMeta.size();
      if ( data.outputValue ) {
        data.outputRowMeta = data.inputRowMeta.clone();
        meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );
      }

      // Cache the field name indexes
      //
      data.nrFields = meta.getOutputFields().length;
      data.fieldIndexes = new int[data.nrFields];
      for ( int i = 0; i < data.nrFields; i++ ) {
        data.fieldIndexes[i] = data.inputRowMeta.indexOfValue( meta.getOutputFields()[i].getFieldName() );
        if ( data.fieldIndexes[i] < 0 ) {
          throw new KettleException( BaseMessages.getString( PKG, "JsonOutput.Exception.FieldNotFound" ) );
        }
        JsonOutputField field = meta.getOutputFields()[i];
        field.setElementName( environmentSubstitute( field.getElementName() ) );
      }
    }

    data.rowsAreSafe = false;
    compatibilityFactory.execute( r );

    if ( data.writeToFile && !data.outputValue ) {
      putRow( data.inputRowMeta, r ); // in case we don't output generated json
      incrementLinesOutput();
    }
    return true;
  }

  private void outPutRow( Object[] rowData ) throws KettleStepException {
    String value = null;
    // We can now output either an object (if blocName != "") or array if (blocName empty)
    JsonNode jsonDoc = this.compatibilityFactory.getJsonNode();
    try {
      value = data.mapper.writeValueAsString( jsonDoc );
    } catch ( Exception e ) {
      throw new KettleStepException( "Cannot encode JSON", e );
    }
    // PDI - 11183 fix NPE if there is no rows
    if ( data.outputValue && !first ) {
      Object[] outputRowData = RowDataUtil.addValueData( rowData, data.inputRowMetaSize, value );
      putRow( data.outputRowMeta, outputRowData );
      incrementLinesOutput();
    }

    if ( data.writeToFile ) {
      // Open a file
      if ( !openNewFile() ) {
        throw new KettleStepException( BaseMessages.getString( PKG, "JsonOutput.Error.OpenNewFile", buildFilename() ) );
      }
      // Write data to file
      try {
        data.writer.write( value );
      } catch ( Exception e ) {
        throw new KettleStepException( BaseMessages.getString( PKG, "JsonOutput.Error.Writing" ), e );
      }
      // Close file      
      //closeFile();
    }
    // Data are safe
    data.rowsAreSafe = true;
    data.jsonArray = data.mapper.createArrayNode();
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (JsonOutputMeta) smi;
    data = (JsonOutputData) sdi;

    if ( !super.init( smi, sdi ) ) {
      return false;
    }

    // Here we decide whether or not to build the structure in
    // compatible mode or fixed mode
    ///JsonOutputMeta jsonOutputMeta = (JsonOutputMeta) ( smi.getStepMetaInterface() );
    if ( meta.isCompatibilityMode() ) {
      compatibilityFactory = new CompatibilityMode();
      //turn off pretty print for compatibility mode.
      data.mapper.configure( SerializationConfig.Feature.INDENT_OUTPUT, false );
    } else {
      compatibilityFactory = new FixedMode();
    }

    data.writeToFile = ( meta.getOperationType() != JsonOutputMeta.OPERATION_TYPE_OUTPUT_VALUE );
    data.outputValue = ( meta.getOperationType() != JsonOutputMeta.OPERATION_TYPE_WRITE_TO_FILE );

    if ( data.outputValue ) {
      // We need to have output field name
      if ( Const.isEmpty( environmentSubstitute( meta.getOutputValue() ) ) ) {
        logError( BaseMessages.getString( PKG, "JsonOutput.Error.MissingOutputFieldName" ) );
        stopAll();
        setErrors( 1 );
        return false;
      }
    }
    if ( data.writeToFile ) {
      // We need to have output field name
      if ( !meta.isServletOutput() && Const.isEmpty( meta.getFileName() ) ) {
        logError( BaseMessages.getString( PKG, "JsonOutput.Error.MissingTargetFilename" ) );
        stopAll();
        setErrors( 1 );
        return false;
      }
      if ( !meta.isDoNotOpenNewFileInit() ) {
        if ( !openNewFile() ) {
          logError( BaseMessages.getString( PKG, "JsonOutput.Error.OpenNewFile", buildFilename() ) );
          stopAll();
          setErrors( 1 );
          return false;
        }
      }

    }
    data.realBlocName = Const.NVL( environmentSubstitute( meta.getJsonBloc() ), "" );
    data.nrRowsInBloc = Const.toInt( environmentSubstitute( meta.getNrRowsInBloc() ), 0 );
    return true;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (JsonOutputMeta) smi;
    data = (JsonOutputData) sdi;
    if ( data.jsonArray != null ) {
      data.jsonArray = null;
    }
    closeFile();
    super.dispose( smi, sdi );

  }

  private void createParentFolder( String filename ) throws KettleStepException {
    if ( !meta.isCreateParentFolder() ) {
      return;
    }
    // Check for parent folder
    FileObject parentfolder = null;
    try {
      // Get parent folder
      parentfolder = KettleVFS.getFileObject( filename, getTransMeta() ).getParent();
      if ( !parentfolder.exists() ) {
        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "JsonOutput.Error.ParentFolderNotExist", parentfolder.getName() ) );
        }
        parentfolder.createFolder();
        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "JsonOutput.Log.ParentFolderCreated" ) );
        }
      }
    } catch ( Exception e ) {
      throw new KettleStepException( BaseMessages.getString( PKG, "JsonOutput.Error.ErrorCreatingParentFolder",
          parentfolder.getName() ) );
    } finally {
      if ( parentfolder != null ) {
        try {
          parentfolder.close();
        } catch ( Exception ex ) { /* Ignore */
        }
      }
    }
  }

  public boolean openNewFile() {
    if ( data.writer != null ) {
      return true;
    }
    boolean retval = false;

    try {

      if ( meta.isServletOutput() ) {
        data.writer = getTrans().getServletPrintWriter();
      } else {
        String filename = buildFilename();
        createParentFolder( filename );
        FileObject fileObject = KettleVFS.getFileObject( filename, getTransMeta() );
        if ( meta.AddToResult() ) {
          // Add this to the result file names...
          ResultFile resultFile =
              new ResultFile( ResultFile.FILE_TYPE_GENERAL, fileObject,
                  getTransMeta().getName(), getStepname() );
          resultFile.setComment( BaseMessages.getString( PKG, "JsonOutput.ResultFilenames.Comment" ) );
          addResultFile( resultFile );
        }

        OutputStream outputStream = KettleVFS.getOutputStream( fileObject, meta.isFileAppended() );

        if ( !Const.isEmpty( meta.getEncoding() ) ) {
          data.writer =
              new OutputStreamWriter( new BufferedOutputStream( outputStream, 5000 ), environmentSubstitute( meta
                  .getEncoding() ) );
        } else {
          data.writer = new OutputStreamWriter( new BufferedOutputStream( outputStream, 5000 ) );
        }

        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JsonOutput.FileOpened", filename ) );
        }

        data.splitnr++;
      }

      retval = true;

    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JsonOutput.Error.OpeningFile", e.toString() ) );
    }

    return retval;
  }

  public String buildFilename() {
    return meta.buildFilename( environmentSubstitute( meta.getFileName() ), getCopy(), data.splitnr );
  }

  private boolean closeFile() {
    if ( data.writer == null ) {
      return true;
    }
    boolean retval = false;

    try {
      data.writer.close();
      data.writer = null;
      retval = true;
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JsonOutput.Error.ClosingFile", e.toString() ) );
      setErrors( 1 );
      retval = false;
    }

    return retval;
  }
}
