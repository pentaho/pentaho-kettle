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

package org.pentaho.di.trans.steps.sql;

import java.util.ArrayList;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
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
public class ExecSQL extends BaseStep implements StepInterface {
  private static Class<?> PKG = ExecSQLMeta.class; // for i18n purposes, needed by Translator2!!

  private ExecSQLMeta meta;

  private ExecSQLData data;

  public ExecSQL( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
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
    meta = (ExecSQLMeta) smi;
    data = (ExecSQLData) sdi;

    if ( !meta.isExecutedEachInputRow() ) {
      RowMetaAndData resultRow =
        getResultRow( data.result, meta.getUpdateField(), meta.getInsertField(), meta.getDeleteField(), meta
          .getReadField() );
      putRow( resultRow.getRowMeta(), resultRow.getData() );
      setOutputDone(); // Stop processing, this is all we do!
      return false;
    }

    Object[] row = getRow();
    if ( row == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }

    if ( first ) { // we just got started

      first = false;

      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      // Find the indexes of the arguments
      data.argumentIndexes = new int[meta.getArguments().length];
      for ( int i = 0; i < meta.getArguments().length; i++ ) {
        data.argumentIndexes[i] = this.getInputRowMeta().indexOfValue( meta.getArguments()[i] );
        if ( data.argumentIndexes[i] < 0 ) {
          logError( BaseMessages.getString( PKG, "ExecSQL.Log.ErrorFindingField" ) + meta.getArguments()[i] + "]" );
          throw new KettleStepException( BaseMessages.getString( PKG, "ExecSQL.Exception.CouldNotFindField", meta
            .getArguments()[i] ) );
        }
        if ( meta.isParams() ) {
          if ( i == 0 ) {
            // Define parameters meta
            data.paramsMeta = new RowMeta();
          }
          data.paramsMeta.addValueMeta( getInputRowMeta().getValueMeta( data.argumentIndexes[i] ) );
        }
      }

      if ( !meta.isParams() ) {
        // We need to replace question marks by string value

        // Find the locations of the question marks in the String...
        // We replace the question marks with the values...
        // We ignore quotes etc. to make inserts easier...
        data.markerPositions = new ArrayList<Integer>();
        int len = data.sql.length();
        int pos = len - 1;
        while ( pos >= 0 ) {
          if ( data.sql.charAt( pos ) == '?' ) {
            data.markerPositions.add( Integer.valueOf( pos ) ); // save the
          }
          // marker
          // position
          pos--;
        }
      }
    }

    String sql;
    Object[] paramsData = null;
    if ( meta.isParams() ) {
      // Get parameters data
      paramsData = new Object[data.argumentIndexes.length];
      sql = this.data.sql;
      for ( int i = 0; i < this.data.argumentIndexes.length; i++ ) {
        paramsData[i] = row[data.argumentIndexes[i]];
      }
    } else {
      int numMarkers = data.markerPositions.size();
      if ( numMarkers > 0 ) {
        StringBuilder buf = new StringBuilder( data.sql );

        // Replace the values in the SQL string...
        //
        for ( int i = 0; i < numMarkers; i++ ) {
          // Get the appropriate value from the input row...
          //
          int index = data.argumentIndexes[data.markerPositions.size() - i - 1];
          ValueMetaInterface valueMeta = getInputRowMeta().getValueMeta( index );
          Object valueData = row[index];

          // replace the '?' with the String in the row.
          //
          int pos = data.markerPositions.get( i );
          String replaceValue = valueMeta.getString( valueData );
          replaceValue = Const.NVL( replaceValue, "" );
          if ( meta.isQuoteString() && ( valueMeta.getType() == ValueMetaInterface.TYPE_STRING ) ) {
            // Have the database dialect do the quoting.
            // This also adds the quotes around the string
            replaceValue = meta.getDatabaseMeta().quoteSQLString( replaceValue );
          }
          buf.replace( pos, pos + 1, replaceValue );
        }
        sql = buf.toString();
      } else {
        sql = data.sql;
      }
    }
    if ( log.isRowLevel() ) {
      logRowlevel( BaseMessages.getString( PKG, "ExecSQL.Log.ExecutingSQLScript" ) + Const.CR + sql );
    }

    boolean sendToErrorRow = false;
    String errorMessage = null;
    try {

      if ( meta.isSingleStatement() ) {
        data.result = data.db.execStatement( sql, data.paramsMeta, paramsData );
      } else {
        data.result = data.db.execStatements( sql, data.paramsMeta, paramsData );
      }

      RowMetaAndData add =
        getResultRow( data.result, meta.getUpdateField(), meta.getInsertField(), meta.getDeleteField(), meta
          .getReadField() );

      row = RowDataUtil.addRowData( row, getInputRowMeta().size(), add.getData() );

      if ( !data.db.isAutoCommit() ) {
        data.db.commit();
      }

      putRow( data.outputRowMeta, row ); // send it out!

      if ( checkFeedback( getLinesWritten() ) ) {
        if ( log.isBasic() ) {
          logBasic( BaseMessages.getString( PKG, "ExecSQL.Log.LineNumber" ) + getLinesWritten() );
        }
      }
    } catch ( KettleException e ) {
      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        throw new KettleStepException( BaseMessages.getString( PKG, "ExecSQL.Log.ErrorInStep" ), e );
      }

      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), row, 1, errorMessage, null, "ExecSQL001" );
      }
    }
    return true;
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ExecSQLMeta) smi;
    data = (ExecSQLData) sdi;

    if ( log.isBasic() ) {
      logBasic( BaseMessages.getString( PKG, "ExecSQL.Log.FinishingReadingQuery" ) );
    }

    if ( data.db != null ) {
      data.db.disconnect();
    }

    super.dispose( smi, sdi );
  }

  /** Stop the running query */
  @Override
  public void stopRunning( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (ExecSQLMeta) smi;
    data = (ExecSQLData) sdi;

    if ( data.db != null && !data.isCanceled ) {
      synchronized ( data.db ) {
        data.db.cancelQuery();
      }
      data.isCanceled = true;
    }
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (ExecSQLMeta) smi;
    data = (ExecSQLData) sdi;

    if ( super.init( smi, sdi ) ) {
      if ( meta.getDatabaseMeta() == null ) {
        logError( BaseMessages.getString( PKG, "ExecSQL.Init.ConnectionMissing", getStepname() ) );
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
          logDetailed( BaseMessages.getString( PKG, "ExecSQL.Log.ConnectedToDB" ) );
        }

        if ( meta.isReplaceVariables() ) {
          data.sql = environmentSubstitute( meta.getSql() );
        } else {
          data.sql = meta.getSql();
        }
        // If the SQL needs to be executed once, this is a starting step
        // somewhere.
        if ( !meta.isExecutedEachInputRow() ) {
          if ( meta.isSingleStatement() ) {
            data.result = data.db.execStatement( data.sql );
          } else {
            data.result = data.db.execStatements( data.sql );
          }
          if ( !data.db.isAutoCommit() ) {
            data.db.commit();
          }
        }
        return true;
      } catch ( KettleException e ) {
        logError( BaseMessages.getString( PKG, "ExecSQL.Log.ErrorOccurred" ) + e.getMessage() );
        setErrors( 1 );
        stopAll();
      }
    }

    return false;
  }

}
