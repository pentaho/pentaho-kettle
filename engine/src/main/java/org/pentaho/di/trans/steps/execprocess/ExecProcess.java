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

package org.pentaho.di.trans.steps.execprocess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Execute a process *
 *
 * @author Samatar
 * @since 03-11-2008
 *
 */

public class ExecProcess extends BaseStep implements StepInterface {
  private static Class<?> PKG = ExecProcessMeta.class; // for i18n purposes, needed by Translator2!!

  private ExecProcessMeta meta;
  private ExecProcessData data;

  public ExecProcess( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (ExecProcessMeta) smi;
    data = (ExecProcessData) sdi;

    Object[] r = getRow(); // Get row from input rowset & set row busy!
    if ( r == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;
      // get the RowMeta
      data.previousRowMeta = getInputRowMeta().clone();
      data.NrPrevFields = data.previousRowMeta.size();
      data.outputRowMeta = data.previousRowMeta;
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      // Check is process field is provided
      if ( Utils.isEmpty( meta.getProcessField() ) ) {
        logError( BaseMessages.getString( PKG, "ExecProcess.Error.ProcessFieldMissing" ) );
        throw new KettleException( BaseMessages.getString( PKG, "ExecProcess.Error.ProcessFieldMissing" ) );
      }

      // cache the position of the field
      if ( data.indexOfProcess < 0 ) {
        data.indexOfProcess = data.previousRowMeta.indexOfValue( meta.getProcessField() );
        if ( data.indexOfProcess < 0 ) {
          // The field is unreachable !
          logError( BaseMessages.getString( PKG, "ExecProcess.Exception.CouldnotFindField" )
            + "[" + meta.getProcessField() + "]" );
          throw new KettleException( BaseMessages.getString( PKG, "ExecProcess.Exception.CouldnotFindField", meta
            .getProcessField() ) );
        }
      }
      if ( meta.isArgumentsInFields() ) {
        if ( data.indexOfArguments == null ) {
          data.indexOfArguments = new int[meta.getArgumentFieldNames().length];
          for ( int i = 0; i < data.indexOfArguments.length; i++ ) {
            String fieldName = meta.getArgumentFieldNames()[i];
            data.indexOfArguments[i] = data.previousRowMeta.indexOfValue( fieldName );
            if ( data.indexOfArguments[i] < 0 ) {
              logError( BaseMessages.getString( PKG, "ExecProcess.Exception.CouldnotFindField" )
                + "[" + fieldName + "]" );
              throw new KettleException(
                BaseMessages.getString( PKG, "ExecProcess.Exception.CouldnotFindField", fieldName ) );
            }
          }
        }
      }
    } // End If first

    Object[] outputRow = RowDataUtil.allocateRowData( data.outputRowMeta.size() );
    for ( int i = 0; i < data.NrPrevFields; i++ ) {
      outputRow[i] = r[i];
    }
    // get process to execute
    String processString = data.previousRowMeta.getString( r, data.indexOfProcess );

    ProcessResult processResult = new ProcessResult();

    try {
      if ( Utils.isEmpty( processString ) ) {
        throw new KettleException( BaseMessages.getString( PKG, "ExecProcess.ProcessEmpty" ) );
      }

      // execute and return result
      if ( meta.isArgumentsInFields() ) {
        List<String> cmdArray = new ArrayList<>();
        cmdArray.add( processString );

        for ( int i = 0; i < data.indexOfArguments.length; i++ ) {
          // Runtime.exec will fail on null array elements
          // Convert to an empty string if value is null
          String argString = data.previousRowMeta.getString( r, data.indexOfArguments[i] );
          cmdArray.add( Const.NVL( argString, "" ) );
        }

        execProcess( cmdArray.toArray( new String[0] ), processResult );
      } else {
        execProcess( processString, processResult );
      }

      if ( meta.isFailWhenNotSuccess() ) {
        if ( processResult.getExistStatus() != 0 ) {
          String errorString = processResult.getErrorStream();
          if ( Utils.isEmpty( errorString ) ) {
            errorString = processResult.getOutputStream();
          }
          throw new KettleException( errorString );
        }
      }

      // Add result field to input stream
      int rowIndex = data.NrPrevFields;
      outputRow[rowIndex++] = processResult.getOutputStream();

      // Add result field to input stream
      outputRow[rowIndex++] = processResult.getErrorStream();

      // Add result field to input stream
      outputRow[rowIndex++] = processResult.getExistStatus();

      // add new values to the row.
      putRow( data.outputRowMeta, outputRow ); // copy row to output rowset(s);

      if ( log.isRowLevel() ) {
        logRowlevel( BaseMessages.getString( PKG, "ExecProcess.LineNumber", getLinesRead()
          + " : " + getInputRowMeta().getString( r ) ) );
      }
    } catch ( KettleException e ) {

      boolean sendToErrorRow = false;
      String errorMessage = null;

      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        logError( BaseMessages.getString( PKG, "ExecProcess.ErrorInStepRunning" ) + e.getMessage() );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), r, 1, errorMessage, meta.getResultFieldName(), "ExecProcess001" );
      }
    }

    return true;
  }

  private void execProcess( String process, ProcessResult processresult ) throws KettleException {
    execProcess( new String[]{ process }, processresult );
  }

  private void execProcess( String[] process, ProcessResult processresult ) throws KettleException {

    Process p = null;
    try {
      String errorMsg = null;
      // execute process
      try {
        if ( !meta.isArgumentsInFields() ) {
          p = data.runtime.exec( process[0] );
        } else {
          p = data.runtime.exec( process );
        }
      } catch ( Exception e ) {
        errorMsg = e.getMessage();
      }
      if ( p == null ) {
        processresult.setErrorStream( errorMsg );
      } else {
        // get output stream
        processresult.setOutputStream( getOutputString( new BufferedReader( new InputStreamReader( p
          .getInputStream() ) ) ) );

        // get error message
        processresult.setErrorStream( getOutputString( new BufferedReader( new InputStreamReader( p
          .getErrorStream() ) ) ) );

        // Wait until end
        p.waitFor();

        // get exit status
        processresult.setExistStatus( p.exitValue() );
      }
    } catch ( IOException ioe ) {
      throw new KettleException( "IO exception while running the process " + process + "!", ioe );
    } catch ( InterruptedException ie ) {
      throw new KettleException( "Interrupted exception while running the process " + process + "!", ie );
    } catch ( Exception e ) {
      throw new KettleException( e );
    } finally {
      if ( p != null ) {
        p.destroy();
      }
    }
  }

  private String getOutputString( BufferedReader b ) throws IOException {
    StringBuilder retvalBuff = new StringBuilder();
    String line;
    String delim = meta.getOutputLineDelimiter();
    if ( delim == null ) {
      delim = "";
    } else {
      delim = environmentSubstitute( delim );
    }

    while ( ( line = b.readLine() ) != null ) {
      if ( retvalBuff.length() > 0 ) {
        retvalBuff.append( delim );
      }
      retvalBuff.append( line );
    }
    return retvalBuff.toString();
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ExecProcessMeta) smi;
    data = (ExecProcessData) sdi;

    if ( super.init( smi, sdi ) ) {
      if ( Utils.isEmpty( meta.getResultFieldName() ) ) {
        logError( BaseMessages.getString( PKG, "ExecProcess.Error.ResultFieldMissing" ) );
        return false;
      }
      data.runtime = Runtime.getRuntime();
      return true;
    }
    return false;
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ExecProcessMeta) smi;
    data = (ExecProcessData) sdi;

    super.dispose( smi, sdi );
  }

}
