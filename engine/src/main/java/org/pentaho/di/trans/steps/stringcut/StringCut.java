/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.stringcut;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Cut strings.
 *
 * @author Samatar Hassan
 * @since 30 September 2008
 */
public class StringCut extends BaseStep implements StepInterface {
  private static Class<?> PKG = StringCutMeta.class; // for i18n purposes, needed by Translator2!!

  private StringCutMeta meta;

  private StringCutData data;

  public StringCut( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  private String CutString( String string, int cutFrom, int cutTo ) {
    String rcode = string;

    if ( !Utils.isEmpty( rcode ) ) {
      int lenCode = rcode.length();

      if ( ( cutFrom >= 0 && cutTo >= 0 ) && cutFrom > lenCode ) {
        rcode = null;
      } else if ( ( cutFrom >= 0 && cutTo >= 0 ) && ( cutTo < cutFrom ) ) {
        rcode = null;
      } else if ( ( cutFrom < 0 && cutTo < 0 ) && cutFrom < -lenCode ) {
        rcode = null;
      } else if ( ( cutFrom < 0 && cutTo < 0 ) && ( cutFrom < cutTo ) ) {
        rcode = null;
      } else {
        if ( cutTo > lenCode ) {
          cutTo = lenCode;
        }
        if ( cutTo < 0 && cutFrom == 0 && ( -cutTo ) > lenCode ) {
          cutTo = -( lenCode );
        }
        if ( cutTo < 0 && cutFrom < 0 && ( -cutTo ) > lenCode ) {
          cutTo = -( lenCode );
        }

        if ( cutFrom >= 0 && cutTo > 0 ) {
          rcode = rcode.substring( cutFrom, cutTo );
        } else if ( cutFrom < 0 && cutTo < 0 ) {
          rcode = rcode.substring( rcode.length() + cutTo, lenCode + cutFrom );
        } else if ( cutFrom == 0 && cutTo < 0 ) {
          int intFrom = rcode.length() + cutTo;
          rcode = rcode.substring( intFrom, lenCode );
        }
      }
    }

    return rcode;
  }

  private Object[] getOneRow( RowMetaInterface rowMeta, Object[] row ) throws KettleException {
    Object[] RowData = new Object[data.outputRowMeta.size()];

    // Copy the input fields.
    System.arraycopy( row, 0, RowData, 0, rowMeta.size() );
    int length = meta.getFieldInStream().length;

    int j = 0; // Index into "new fields" area, past the first {data.inputFieldsNr} records
    for ( int i = 0; i < length; i++ ) {
      String valueIn = getInputRowMeta().getString( row, data.inStreamNrs[i] );
      String value = CutString( valueIn, data.cutFrom[i], data.cutTo[i] );
      if ( Utils.isEmpty( data.outStreamNrs[i] ) ) {
        RowData[data.inStreamNrs[i]] = value;
      } else {
        RowData[data.inputFieldsNr + j] = value;
        j++;
      }
    }
    return RowData;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (StringCutMeta) smi;
    data = (StringCutData) sdi;

    Object[] r = getRow(); // Get row from input rowset & set row busy!
    if ( r == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;
      // What's the format of the output row?
      data.outputRowMeta = getInputRowMeta().clone();
      data.inputFieldsNr = data.outputRowMeta.size();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      data.inStreamNrs = new int[meta.getFieldInStream().length];
      for ( int i = 0; i < meta.getFieldInStream().length; i++ ) {
        data.inStreamNrs[i] = getInputRowMeta().indexOfValue( meta.getFieldInStream()[i] );
        if ( data.inStreamNrs[i] < 0 ) {
          throw new KettleStepException( BaseMessages.getString( PKG, "StringCut.Exception.FieldRequired", meta
            .getFieldInStream()[i] ) );
        }

        // check field type
        if ( getInputRowMeta().getValueMeta( data.inStreamNrs[i] ).getType() != ValueMetaInterface.TYPE_STRING ) {
          throw new KettleStepException( BaseMessages.getString(
            PKG, "StringCut.Exception.FieldTypeNotString", meta.getFieldInStream()[i] ) );
        }
      }

      data.outStreamNrs = new String[meta.getFieldInStream().length];
      for ( int i = 0; i < meta.getFieldInStream().length; i++ ) {
        data.outStreamNrs[i] = environmentSubstitute( meta.getFieldOutStream()[i] );
      }

      data.cutFrom = new int[meta.getFieldInStream().length];
      data.cutTo = new int[meta.getFieldInStream().length];
      for ( int i = 0; i < meta.getFieldInStream().length; i++ ) {
        if ( Utils.isEmpty( environmentSubstitute( meta.getCutFrom()[i] ) ) ) {
          data.cutFrom[i] = 0;
        } else {
          data.cutFrom[i] = Const.toInt( environmentSubstitute( meta.getCutFrom()[i] ), 0 );
        }

        if ( Utils.isEmpty( environmentSubstitute( meta.getCutTo()[i] ) ) ) {
          data.cutTo[i] = 0;
        } else {
          data.cutTo[i] = Const.toInt( environmentSubstitute( meta.getCutTo()[i] ), 0 );
        }

      } // end for
    } // end if first

    try {
      Object[] output = getOneRow( getInputRowMeta(), r );
      putRow( data.outputRowMeta, output );

      if ( checkFeedback( getLinesRead() ) ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "StringCut.Log.LineNumber" ) + getLinesRead() );
        }

      }
    } catch ( KettleException e ) {
      boolean sendToErrorRow = false;
      String errorMessage = null;

      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        logError( BaseMessages.getString( PKG, "StringCut.Log.ErrorInStep", e.getMessage() ) );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), r, 1, errorMessage, null, "StringCut001" );
      }
    }
    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    boolean rCode = true;

    meta = (StringCutMeta) smi;
    data = (StringCutData) sdi;

    if ( super.init( smi, sdi ) ) {

      return rCode;
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (StringCutMeta) smi;
    data = (StringCutData) sdi;

    super.dispose( smi, sdi );
  }

}
