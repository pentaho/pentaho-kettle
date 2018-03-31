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

package org.pentaho.di.trans.steps.dbproc;

import java.sql.SQLException;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Retrieves values from a database by calling database stored procedures or functions
 *
 * @author Matt
 * @since 26-apr-2003
 *
 */
public class DBProc extends BaseStep implements StepInterface {
  private static Class<?> PKG = DBProcMeta.class; // for i18n purposes, needed by Translator2!!

  private DBProcMeta meta;
  private DBProcData data;

  public DBProc( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  private Object[] runProc( RowMetaInterface rowMeta, Object[] rowData ) throws KettleException {
    if ( first ) {
      first = false;

      // get the RowMeta for the output
      //
      data.outputMeta = data.inputRowMeta.clone();
      meta.getFields( data.outputMeta, getStepname(), null, null, this, repository, metaStore );

      data.argnrs = new int[meta.getArgument().length];
      for ( int i = 0; i < meta.getArgument().length; i++ ) {
        if ( !meta.getArgumentDirection()[i].equalsIgnoreCase( "OUT" ) ) { // IN or INOUT
          data.argnrs[i] = rowMeta.indexOfValue( meta.getArgument()[i] );
          if ( data.argnrs[i] < 0 ) {
            logError( BaseMessages.getString( PKG, "DBProc.Log.ErrorFindingField" ) + meta.getArgument()[i] + "]" );
            throw new KettleStepException( BaseMessages.getString( PKG, "DBProc.Exception.CouldnotFindField", meta
              .getArgument()[i] ) );
          }
        } else {
          data.argnrs[i] = -1;
        }
      }

      data.db.setProcLookup( environmentSubstitute( meta.getProcedure() ), meta.getArgument(), meta
        .getArgumentDirection(), meta.getArgumentType(), meta.getResultName(), meta.getResultType() );
    }

    Object[] outputRowData = RowDataUtil.resizeArray( rowData, data.outputMeta.size() );
    int outputIndex = rowMeta.size();

    data.db.setProcValues( rowMeta, rowData, data.argnrs, meta.getArgumentDirection(), !Utils.isEmpty( meta
      .getResultName() ) );

    RowMetaAndData add =
      data.db.callProcedure( meta.getArgument(), meta.getArgumentDirection(), meta.getArgumentType(), meta
        .getResultName(), meta.getResultType() );
    int addIndex = 0;

    // Function return?
    if ( !Utils.isEmpty( meta.getResultName() ) ) {
      outputRowData[outputIndex++] = add.getData()[addIndex++]; // first is the function return
    }

    // We are only expecting the OUT and INOUT arguments here.
    // The INOUT values need to replace the value with the same name in the row.
    //
    for ( int i = 0; i < data.argnrs.length; i++ ) {
      if ( meta.getArgumentDirection()[i].equalsIgnoreCase( "OUT" ) ) {
        // add
        outputRowData[outputIndex++] = add.getData()[addIndex++];
      } else if ( meta.getArgumentDirection()[i].equalsIgnoreCase( "INOUT" ) ) {
        // replace
        outputRowData[data.argnrs[i]] = add.getData()[addIndex];
        addIndex++;
      }
      // IN not taken
    }
    return outputRowData;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (DBProcMeta) smi;
    data = (DBProcData) sdi;

    boolean sendToErrorRow = false;
    String errorMessage = null;

    // A procedure/function could also have no input at all
    // However, we would still need to know how many times it gets executed.
    // In short: the procedure gets executed once for every input row.
    //
    Object[] r;

    if ( data.readsRows ) {
      r = getRow(); // Get row from input rowset & set row busy!
      if ( r == null ) { // no more input to be expected...

        setOutputDone();
        return false;
      }
      data.inputRowMeta = getInputRowMeta();
    } else {
      r = new Object[] {}; // empty row
      incrementLinesRead();
      data.inputRowMeta = new RowMeta(); // empty row metadata too
      data.readsRows = true; // make it drop out of the loop at the next entrance to this method
    }

    try {
      Object[] outputRowData = runProc( data.inputRowMeta, r ); // add new values to the row in rowset[0].
      putRow( data.outputMeta, outputRowData ); // copy row to output rowset(s);

      if ( checkFeedback( getLinesRead() ) ) {
        if ( log.isBasic() ) {
          logBasic( BaseMessages.getString( PKG, "DBProc.LineNumber" ) + getLinesRead() );
        }
      }
    } catch ( KettleException e ) {

      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
        // CHE: Read the chained SQL exceptions and add them
        // to the errorMessage
        SQLException nextSQLExOnChain = null;
        if ( ( e.getCause() != null ) && ( e.getCause() instanceof SQLException ) ) {
          nextSQLExOnChain = ( (SQLException) e.getCause() ).getNextException();
          while ( nextSQLExOnChain != null ) {
            errorMessage = errorMessage + nextSQLExOnChain.getMessage() + Const.CR;
            nextSQLExOnChain = nextSQLExOnChain.getNextException();
          }
        }
      } else {

        logError( BaseMessages.getString( PKG, "DBProc.ErrorInStepRunning" ) + e.getMessage() );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }

      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), r, 1, errorMessage, null, "DBP001" );
      }
    }

    return true;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (DBProcMeta) smi;
    data = (DBProcData) sdi;

    if ( super.init( smi, sdi ) ) {
      data.readsRows = getStepMeta().getRemoteInputSteps().size() > 0;
      List<StepMeta> previous = getTransMeta().findPreviousSteps( getStepMeta() );
      if ( previous != null && previous.size() > 0 ) {
        data.readsRows = true;
      }

      data.db = new Database( this, meta.getDatabase() );
      data.db.shareVariablesWith( this );
      try {
        if ( getTransMeta().isUsingUniqueConnections() ) {
          synchronized ( getTrans() ) {
            data.db.connect( getTrans().getTransactionId(), getPartitionID() );
          }
        } else {
          data.db.connect( getPartitionID() );
        }

        if ( !meta.isAutoCommit() ) {
          if ( log.isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "DBProc.Log.AutoCommit" ) );
          }
          data.db.setCommit( 9999 );
        }
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "DBProc.Log.ConnectedToDB" ) );
        }

        return true;
      } catch ( KettleException e ) {
        logError( BaseMessages.getString( PKG, "DBProc.Log.DBException" ) + e.getMessage() );
        if ( data.db != null ) {
          data.db.disconnect();
        }
      }
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (DBProcMeta) smi;
    data = (DBProcData) sdi;

    if ( data.db != null ) {
      // CHE: Properly close the callable statement
      try {
        data.db.closeProcedureStatement();
      } catch ( KettleDatabaseException e ) {
        logError( BaseMessages.getString( PKG, "DBProc.Log.CloseProcedureError" ) + e.getMessage() );
      }

      try {
        if ( !meta.isAutoCommit() ) {
          data.db.commit();
        }
      } catch ( KettleDatabaseException e ) {
        logError( BaseMessages.getString( PKG, "DBProc.Log.CommitError" ) + e.getMessage() );
      } finally {
        data.db.disconnect();
      }
    }
    super.dispose( smi, sdi );
  }

}
