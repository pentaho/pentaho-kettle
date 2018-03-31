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

package org.pentaho.di.trans.steps.writetolog;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Write data to log.
 *
 * @author Samatar
 * @since 30-06-2008
 */

public class WriteToLog extends BaseStep implements StepInterface {
  private static Class<?> PKG = WriteToLogMeta.class; // for i18n purposes, needed by Translator2!!

  private WriteToLogMeta meta;
  private WriteToLogData data;
  private int rowCounter = 0;
  private boolean rowCounterLimitHit = false;

  public WriteToLog( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    meta = (WriteToLogMeta) smi;
    data = (WriteToLogData) sdi;

    Object[] r = getRow(); // get row, set busy!
    if ( r == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }

    // Limit hit? skip
    if ( rowCounterLimitHit ) {
      putRow( getInputRowMeta(), r ); // copy row to output
      return true;
    }

    if ( first ) {
      first = false;

      if ( meta.getFieldName() != null && meta.getFieldName().length > 0 ) {
        data.fieldnrs = new int[meta.getFieldName().length];

        for ( int i = 0; i < data.fieldnrs.length; i++ ) {
          data.fieldnrs[i] = getInputRowMeta().indexOfValue( meta.getFieldName()[i] );
          if ( data.fieldnrs[i] < 0 ) {
            logError( BaseMessages.getString( PKG, "WriteToLog.Log.CanNotFindField", meta.getFieldName()[i] ) );
            throw new KettleException( BaseMessages.getString( PKG, "WriteToLog.Log.CanNotFindField", meta
              .getFieldName()[i] ) );
          }
        }
      } else {
        data.fieldnrs = new int[getInputRowMeta().size()];
        for ( int i = 0; i < data.fieldnrs.length; i++ ) {
          data.fieldnrs[i] = i;
        }
      }
      data.fieldnr = data.fieldnrs.length;
      data.loglevel = meta.getLogLevelByDesc();
      data.logmessage = Const.NVL( this.environmentSubstitute( meta.getLogMessage() ), "" );
      if ( !Utils.isEmpty( data.logmessage ) ) {
        data.logmessage += Const.CR + Const.CR;
      }

    } // end if first

    StringBuilder out = new StringBuilder();
    out.append( Const.CR
      + "------------> " + BaseMessages.getString( PKG, "WriteToLog.Log.NLigne", "" + getLinesRead() )
      + "------------------------------" + Const.CR );

    out.append( getRealLogMessage() );

    // Loop through fields
    for ( int i = 0; i < data.fieldnr; i++ ) {
      String fieldvalue = getInputRowMeta().getString( r, data.fieldnrs[i] );

      if ( meta.isdisplayHeader() ) {
        String fieldname = getInputRowMeta().getFieldNames()[data.fieldnrs[i]];
        out.append( fieldname + " = " + fieldvalue + Const.CR );
      } else {
        out.append( fieldvalue + Const.CR );
      }
    }
    out.append( Const.CR + "====================" );

    setLog( data.loglevel, out );

    // Increment counter
    if ( meta.isLimitRows() && ++rowCounter >= meta.getLimitRowsNumber() ) {
      rowCounterLimitHit = true;
    }

    putRow( getInputRowMeta(), r ); // copy row to output

    return true;
  }

  private void setLog( LogLevel loglevel, StringBuilder msg ) {
    switch ( loglevel ) {
      case ERROR:
        // Output message to log
        // Log level = ERREUR
        logError( msg.toString() );
        break;
      case MINIMAL:
        // Output message to log
        // Log level = MINIMAL
        logMinimal( msg.toString() );
        break;
      case BASIC:
        // Output message to log
        // Log level = BASIC
        logBasic( msg.toString() );
        break;
      case DETAILED:
        // Output message to log
        // Log level = DETAILED
        logDetailed( msg.toString() );
        break;
      case DEBUG:
        // Output message to log
        // Log level = DEBUG
        logDebug( msg.toString() );
        break;
      case ROWLEVEL:
        // Output message to log
        // Log level = ROW LEVEL
        logRowlevel( msg.toString() );
        break;
      case NOTHING:
        // Output nothing to log
        // Log level = NOTHING
        break;
      default:
        break;
    }
  }

  public String getRealLogMessage() {
    return data.logmessage;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (WriteToLogMeta) smi;
    data = (WriteToLogData) sdi;

    if ( super.init( smi, sdi ) ) {
      // Add init code here.
      return true;
    }
    return false;
  }

}
