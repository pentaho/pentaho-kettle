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

package org.pentaho.di.trans.steps.execsqlrow;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Execute one or more SQL statements in a script, one time or parameterised (for every row)
 *
 * @author Matt
 * @since 10-sep-2005
 */
public class ExecSQLRow extends BaseStep implements StepInterface {
  private static Class<?> PKG = ExecSQLRowMeta.class; // for i18n purposes, needed by Translator2!!

  private ExecSQLRowMeta meta;
  private ExecSQLRowData data;

  public ExecSQLRow( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public static final RowMetaAndData getResultRow( Result result, String upd, String ins, String del, String read ) {
    RowMetaAndData resultRow = new RowMetaAndData();

    if ( upd != null && upd.length() > 0 ) {
      ValueMetaInterface meta = new ValueMetaInteger( upd );
      meta.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      resultRow.addValue( meta, new Long( result.getNrLinesUpdated() ) );
    }

    if ( ins != null && ins.length() > 0 ) {
      ValueMetaInterface meta = new ValueMetaInteger( ins );
      meta.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      resultRow.addValue( meta, new Long( result.getNrLinesOutput() ) );
    }

    if ( del != null && del.length() > 0 ) {
      ValueMetaInterface meta = new ValueMetaInteger( del );
      meta.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      resultRow.addValue( meta, new Long( result.getNrLinesDeleted() ) );
    }

    if ( read != null && read.length() > 0 ) {
      ValueMetaInterface meta = new ValueMetaInteger( read );
      meta.setLength( ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0 );
      resultRow.addValue( meta, new Long( result.getNrLinesRead() ) );
    }

    return resultRow;
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (ExecSQLRowMeta) smi;
    data = (ExecSQLRowData) sdi;

    boolean sendToErrorRow = false;
    String errorMessage = null;

    Object[] row = getRow();
    if ( row == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }

    if ( first ) { // we just got started

      first = false;

      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      // Check is SQL field is provided
      if ( Utils.isEmpty( meta.getSqlFieldName() ) ) {
        throw new KettleException( BaseMessages.getString( PKG, "ExecSQLRow.Error.SQLFieldFieldMissing" ) );
      }

      // cache the position of the field
      if ( data.indexOfSQLFieldname < 0 ) {
        data.indexOfSQLFieldname = this.getInputRowMeta().indexOfValue( meta.getSqlFieldName() );
        if ( data.indexOfSQLFieldname < 0 ) {
          // The field is unreachable !
          throw new KettleException( BaseMessages.getString( PKG, "ExecSQLRow.Exception.CouldnotFindField", meta
            .getSqlFieldName() ) );
        }
      }

    }

    // get SQL
    String sql = getInputRowMeta().getString( row, data.indexOfSQLFieldname );

    try {
      if ( meta.isSqlFromfile() ) {
        if ( Utils.isEmpty( sql ) ) {
          // empty filename
          throw new KettleException( BaseMessages.getString( PKG, "ExecSQLRow.Log.EmptySQLFromFile" ) );
        }
        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "ExecSQLRow.Log.ExecutingSQLFromFile", sql ) );
        }
        data.result = data.db.execStatementsFromFile( sql, meta.IsSendOneStatement() );
      } else {
        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "ExecSQLRow.Log.ExecutingSQLScript" ) + Const.CR + sql );
        }
        if ( meta.IsSendOneStatement() ) {
          data.result = data.db.execStatement( sql );
        } else {
          data.result = data.db.execStatements( sql );
        }
      }

      RowMetaAndData add =
        getResultRow( data.result, meta.getUpdateField(), meta.getInsertField(), meta.getDeleteField(), meta
          .getReadField() );
      row = RowDataUtil.addRowData( row, getInputRowMeta().size(), add.getData() );

      if ( meta.getCommitSize() > 0 ) {
        if ( !data.db.isAutoCommit() ) {
          if ( meta.getCommitSize() == 1 ) {
            data.db.commit();
          } else if ( getLinesWritten() % meta.getCommitSize() == 0 ) {
            data.db.commit();
          }
        }
      }

      putRow( data.outputRowMeta, row ); // send it out!

      if ( checkFeedback( getLinesWritten() ) ) {
        if ( log.isBasic() ) {
          logBasic( BaseMessages.getString( PKG, "ExecSQLRow.Log.LineNumber" ) + getLinesWritten() );
        }
      }
    } catch ( KettleException e ) {
      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        logError( BaseMessages.getString( PKG, "ExecSQLRow.Log.ErrorInStep" ) + e.getMessage() );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), row, 1, errorMessage, null, "ExecSQLRow001" );
      }
    }
    return true;
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ExecSQLRowMeta) smi;
    data = (ExecSQLRowData) sdi;

    if ( log.isBasic() ) {
      logBasic( BaseMessages.getString( PKG, "ExecSQLRow.Log.FinishingReadingQuery" ) );
    }

    if ( data.db != null ) {
      try {
        if ( !data.db.isAutoCommit() ) {
          if ( getErrors() == 0 ) {
            data.db.commit();
          } else {
            data.db.rollback();
          }
        }
      } catch ( KettleDatabaseException e ) {
        logError( BaseMessages.getString( PKG, "Update.Log.UnableToCommitUpdateConnection" )
          + data.db + "] :" + e.toString() );
        setErrors( 1 );
      } finally {
        data.db.disconnect();
      }
    }

    super.dispose( smi, sdi );
  }

  /** Stop the running query */
  @Override
  public void stopRunning( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (ExecSQLRowMeta) smi;
    data = (ExecSQLRowData) sdi;

    if ( data.db != null ) {
      data.db.cancelQuery();
    }
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ExecSQLRowMeta) smi;
    data = (ExecSQLRowData) sdi;

    if ( super.init( smi, sdi ) ) {
      if ( meta.getDatabaseMeta() == null ) {
        logError( BaseMessages.getString( PKG, "ExecSQLRow.Init.ConnectionMissing", getStepname() ) );
        return false;
      }
      data.db = new Database( this, meta.getDatabaseMeta() );
      data.db.shareVariablesWith( this );

      // Connect to the database
      try {
        if ( getTransMeta().isUsingUniqueConnections() ) {
          synchronized ( getTrans() ) {
            data.db.connect( getTrans().getTransactionId(), getPartitionID() );
          }
        } else {
          data.db.connect( getPartitionID() );
        }

        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "ExecSQLRow.Log.ConnectedToDB" ) );
        }

        if ( meta.getCommitSize() >= 1 ) {
          data.db.setCommit( meta.getCommitSize() );
        }
        return true;
      } catch ( KettleException e ) {
        logError( BaseMessages.getString( PKG, "ExecSQLRow.Log.ErrorOccurred" ) + e.getMessage() );
        setErrors( 1 );
        stopAll();
      }
    }

    return false;
  }

}
