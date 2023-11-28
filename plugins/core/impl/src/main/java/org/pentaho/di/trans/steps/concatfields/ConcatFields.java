/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.concatfields;

import java.io.UnsupportedEncodingException;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutput;

/*
 * ConcatFields step - derived from the TextFileOutput step
 * @author jb
 * @since 2012-08-31
 *
 */
public class ConcatFields extends TextFileOutput implements StepInterface {

  private static Class<?> PKG = ConcatFields.class; // for i18n purposes, needed by Translator2!!

  public ConcatFieldsMeta meta;
  public ConcatFieldsData data;

  public ConcatFields( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                       Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans ); // allocate TextFileOutput
  }

  @Override
  public synchronized boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (ConcatFieldsMeta) smi;
    data = (ConcatFieldsData) sdi;

    boolean result = true;
    boolean bEndedLineWrote = false;

    Object[] r = getRow(); // This also waits for a row to be finished.

    if ( r != null && first ) {
      first = false;

      data.inputRowMeta = getInputRowMeta();
      data.outputRowMeta = data.inputRowMeta.clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      // the field precisions and lengths are altered! see TextFileOutputMeta.getFields().
      // otherwise trim(), padding etc. will not work
      data.inputRowMetaModified = getInputRowMeta().clone();
      meta
        .getFieldsModifyInput( data.inputRowMetaModified, getStepname(), null, null, this, repository, metaStore );

      data.posTargetField = data.outputRowMeta.indexOfValue( meta.getTargetFieldName() );
      if ( data.posTargetField < 0 ) {
        throw new KettleStepException( BaseMessages.getString(
          PKG, "ConcatFields.Error.TargetFieldNotFoundOutputStream", "" + meta.getTargetFieldName() ) );
      }

      data.fieldnrs = new int[ meta.getOutputFields().length ];
      for ( int i = 0; i < meta.getOutputFields().length; i++ ) {
        data.fieldnrs[ i ] = data.inputRowMetaModified.indexOfValue( meta.getOutputFields()[ i ].getName() );
        if ( data.fieldnrs[ i ] < 0 ) {
          throw new KettleStepException( BaseMessages.getString(
            PKG, "ConcatFields.Error.FieldNotFoundInputStream", "" + meta.getOutputFields()[ i ].getName() ) );
        }
      }

      meta.calcMetaWithFieldOptions( data );

      // prepare for fast data dump (StringBuilder size)
      data.targetFieldLengthFastDataDump = meta.getTargetFieldLength();
      if ( data.targetFieldLengthFastDataDump <= 0 ) { // try it as a guess: 50 * size
        if ( meta.getOutputFields().length == 0 ) {
          data.targetFieldLengthFastDataDump = 50 * getInputRowMeta().size();
        } else {
          data.targetFieldLengthFastDataDump = 50 * meta.getOutputFields().length;
        }
      }
      prepareForReMap();

      checkAndWriteHeader();
    }

    if ( ( r == null && data.outputRowMeta != null && meta.isFooterEnabled() )
      || ( r != null && getLinesWritten() > 0 && meta.getSplitEvery() > 0
      && ( ( getLinesWritten() + 1 - data.headerOffsetForSplitRows ) % meta.getSplitEvery() ) == 0 ) ) {
      if ( data.outputRowMeta != null ) {
        if ( meta.isFooterEnabled() ) {
          writeHeader();
          if ( isHeaderOffsetForSplitRowsAllowed() ) {
            data.headerOffsetForSplitRows++;
          }
          // add an empty line for the header
          Object[] row = new Object[ data.outputRowMeta.size() ];
          putRowFromStream( row );
        }
      }

      if ( r == null ) {
        // add tag to last line if needed
        writeEndedLine();
        bEndedLineWrote = true;
        putRowFromStream( r );
      }

    }

    if ( r == null ) { // no more input to be expected...

      if ( false == bEndedLineWrote ) {
        // add tag to last line if needed
        writeEndedLine();
        bEndedLineWrote = true;
        putRowFromStream( r );
      }

      setOutputDone();
      setLinesOutput( 0 ); // we have to tweak it, no output here
      return false;
    }

    if ( !meta.isFastDump() ) {
      // instead of writing to file, writes it to a stream
      writeRow( data.inputRowMetaModified, r );
      setLinesOutput( 0 ); // we have to tweak it, no output here
      r = putRowFromStream( r );
    } else { // fast data dump
      r = putRowFastDataDump( r );
    }

    if ( log.isRowLevel() ) {
      logRowlevel( BaseMessages.getString( PKG, "ConcatFields.Log.WriteRow" )
        + getLinesWritten() + " : " + data.outputRowMeta.getString( r ) );
    }
    if ( checkFeedback( getLinesRead() ) ) {
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "ConcatFields.Log.LineNumber" ) + getLinesRead() );
      }
    }

    return result;
  }

  void checkAndWriteHeader() throws KettleStepException {
    // See if we have to write a header-line)
    if ( !meta.isFileAppended() && ( meta.isHeaderEnabled() || meta.isFooterEnabled() ) ) {
      if ( !meta.isFileNameInField() && meta.isHeaderEnabled() && data.outputRowMeta != null ) {
        writeHeader();
        // add an empty line for the header
        Object[] row = new Object[ data.outputRowMeta.size() ];
        putRowFromStream( row );
      }
    }
  }

  void prepareForReMap() throws KettleStepException {
    // prepare for re-map when removeSelectedFields
    if ( meta.isRemoveSelectedFields() ) {
      data.remainingFieldsInputOutputMapping = new int[ data.outputRowMeta.size() - 1 ]; // -1: don't need the new
      // target field
      String[] fieldNames = data.outputRowMeta.getFieldNames();
      for ( int i = 0; i < fieldNames.length - 1; i++ ) { // -1: don't search the new target field
        data.remainingFieldsInputOutputMapping[ i ] = data.inputRowMetaModified.indexOfValue( fieldNames[ i ] );
        if ( data.remainingFieldsInputOutputMapping[ i ] < 0 ) {
          throw new KettleStepException( BaseMessages.getString( PKG,
            "ConcatFields.Error.RemainingFieldNotFoundInputStream", "" + fieldNames[ i ] ) );
        }
      }
    }
  }

  // reads the row from the stream, flushs, add target field and call putRow()
  Object[] putRowFromStream( Object[] r ) throws KettleStepException {

    byte[] targetBinary = ( (ConcatFieldsOutputStream) data.writer ).read();
    if ( r == null && targetBinary == null ) {
      return null; // special condition of header/footer/split
    }

    Object[] outputRowData = prepareOutputRow( r );

    // add target field
    if ( outputRowData == null ) { // special condition of header/footer/split
      outputRowData = new Object[ data.outputRowMeta.size() ];
    }
    if ( targetBinary != null ) {
      if ( !data.hasEncoding ) {
        outputRowData[ data.posTargetField ] = new String( targetBinary );
      } else { // handle encoding
        try {
          outputRowData[ data.posTargetField ] = new String( targetBinary, meta.getEncoding() );
        } catch ( UnsupportedEncodingException e ) {
          throw new KettleStepException( BaseMessages.getString( PKG, "ConcatFields.Error.UnsupportedEncoding", ""
            + meta.getEncoding() ) );
        }
      }
    } else {
      outputRowData[ data.posTargetField ] = null;
    }

    putRow( data.outputRowMeta, outputRowData );
    return outputRowData;
  }

  // concat as a fast data dump (no formatting) and call putRow()
  // this method is only called from a normal line, never from header/footer/split stuff
  Object[] putRowFastDataDump( Object[] r ) throws KettleStepException {

    Object[] outputRowData = prepareOutputRow( r );

    StringBuilder targetString = new StringBuilder( data.targetFieldLengthFastDataDump ); // use a good capacity

    if ( meta.getOutputFields() == null || meta.getOutputFields().length == 0 ) {
      // all values in stream
      for ( int i = 0; i < getInputRowMeta().size(); i++ ) {
        if ( i > 0 ) {
          targetString.append( data.stringSeparator );
        }
        concatFieldFastDataDump( targetString, r[ i ], "" ); // "": no specific null value defined
      }
    } else {
      for ( int i = 0; i < data.fieldnrs.length; i++ ) {
        if ( i > 0 ) {
          targetString.append( data.stringSeparator );
        }
        concatFieldFastDataDump( targetString, r[ data.fieldnrs[ i ] ], data.stringNullValue[ i ] );
      }
    }

    outputRowData[ data.posTargetField ] = new String( targetString );

    putRow( data.outputRowMeta, outputRowData );
    return outputRowData;
  }

  private void concatFieldFastDataDump( StringBuilder targetField, Object valueData, String nullString ) {

    if ( meta.isEnclosureForced() ) {
      targetField.append( data.stringEnclosure );
    }
    if ( valueData != null ) {
      targetField.append( valueData );
    } else {
      targetField.append( nullString );
    }
    if ( meta.isEnclosureForced() ) {
      targetField.append( data.stringEnclosure );
    }
  }

  // reserve room for the target field and eventually re-map the fields
  Object[] prepareOutputRow( Object[] r ) {
    Object[] outputRowData = null;
    if ( !meta.isRemoveSelectedFields() ) {
      // reserve room for the target field
      outputRowData = RowDataUtil.resizeArray( r, data.outputRowMeta.size() );
    } else {
      // reserve room for the target field and re-map the fields
      outputRowData = new Object[ data.outputRowMeta.size() + RowDataUtil.OVER_ALLOCATE_SIZE ];
      if ( r != null ) {
        // re-map the fields
        for ( int i = 0; i < data.remainingFieldsInputOutputMapping.length; i++ ) { // BTW: the new target field is not
          // here
          outputRowData[ i ] = r[ data.remainingFieldsInputOutputMapping[ i ] ];
        }
      }
    }
    return outputRowData;
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ConcatFieldsMeta) smi;
    data = (ConcatFieldsData) sdi;

    // since we can no call the initial init() from BaseStep we have to tweak here
    meta.setDoNotOpenNewFileInit( true ); // do not open a file in init

    data.writer = new ConcatFieldsOutputStream();

    initStringDataFields();

    if ( isHeaderOffsetForSplitRowsAllowed() ) {
      data.headerOffsetForSplitRows = 1;
    }

    boolean rv = super.init( smi, sdi ); // calls also initBinaryDataFields();
    data.binaryNewline = new byte[] {}; // tweak the CR/LF handling
    return rv;
  }

  // init separator,enclosure, null values for fast data dump
  private void initStringDataFields() {
    data.stringSeparator = "";
    data.stringEnclosure = "";

    if ( !Utils.isEmpty( meta.getSeparator() ) ) {
      data.stringSeparator = environmentSubstitute( meta.getSeparator() );
    }
    if ( !Utils.isEmpty( meta.getEnclosure() ) ) {
      data.stringEnclosure = environmentSubstitute( meta.getEnclosure() );
    }

    data.stringNullValue = new String[ meta.getOutputFields().length ];
    for ( int i = 0; i < meta.getOutputFields().length; i++ ) {
      data.stringNullValue[ i ] = "";
      String nullString = meta.getOutputFields()[ i ].getNullString();
      if ( !Utils.isEmpty( nullString ) ) {
        data.stringNullValue[ i ] = nullString;
      }
    }
  }

  protected boolean closeFile() {
    boolean retval = false;

    try {
      if ( data.writer != null ) {
        data.writer.flush();
      }
      data.writer = null;
      if ( log.isDebug() ) {
        logDebug( "Closing normal file ..." );
      }
      retval = true;
    } catch ( Exception e ) {
      logError( "Exception trying to close file: " + e.toString() );
      setErrors( 1 );
      //Clean resources
      data.writer = null;
      data.out = null;
      data.fos = null;
      retval = false;
    }

    return retval;
  }

  private boolean isHeaderOffsetForSplitRowsAllowed() {
    //Check if retro compatibility is set or not, to guaranty compatibility with older versions
    //If this variable is not set, then data.headerOffsetForSplitRows will always be 0
    //If this variable is set to Y, then data.headerOffsetForSplitRows will increment on every Split Rows, and
    //this value is used to correct the offset in populating the header data at right position
    String val = getVariable( Const.KETTLE_COMPATIBILITY_CONCAT_FIELDS_SPLIT_ROWS_HEADER_OFFSET, "N" );

    return Boolean.TRUE.equals( ValueMetaBase.convertStringToBoolean( Const.NVL( val, "N" ) ) );
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    if ( data.oneFileOpened ) {
      closeFile();
    }

    try {
      if ( data.fos != null ) {
        data.fos.close();
      }
    } catch ( Exception e ) {
      data.fos = null;
      logError( "Unexpected error closing file", e );
      setErrors( 1 );
    }

    super.dispose( smi, sdi );
    // since we can no call the initial dispose() from BaseStep we may need to tweak
    // when the dispose() from TextFileOutput will have bad effects in the future due to changes and call this manually
    // sdi.setStatus(StepExecutionStatus.STATUS_DISPOSED);
    // but we try to avoid
  }

}
