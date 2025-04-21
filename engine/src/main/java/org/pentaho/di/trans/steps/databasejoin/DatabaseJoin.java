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


package org.pentaho.di.trans.steps.databasejoin;

import java.sql.ResultSet;
import java.util.concurrent.locks.ReentrantLock;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

/**
 * Use values from input streams to joins with values in a database. Freehand SQL can be used to do this.
 *
 * @author Matt
 * @since 26-apr-2003
 */
public class DatabaseJoin extends BaseDatabaseStep implements StepInterface {
  private static Class<?> PKG = DatabaseJoinMeta.class; // for i18n purposes, needed by Translator2!!

  private final ReentrantLock dbLock = new ReentrantLock();

  public DatabaseJoin( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans, true );
  }

  private void lookupValues( DatabaseJoinMeta meta, DatabaseJoinData data,
      RowMetaInterface rowMeta, Object[] rowData ) throws KettleException {
    dbLock.lock();
    final ResultSet rs;
    try {
      if ( first ) {
        first = false;
        prepareSQL( meta, data );
        data.outputRowMeta = rowMeta.clone();
        meta.getFields( getTransMeta().getBowl(),
            data.outputRowMeta, getStepname(), new RowMetaInterface[] { meta.getTableFields(), }, null, this,
            repository, metaStore );

        data.lookupRowMeta = new RowMeta();

        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "DatabaseJoin.Log.CheckingRow" ) + rowMeta.getString( rowData ) );
        }

        data.keynrs = new int[meta.getParameterField().length];

        for ( int i = 0; i < meta.getParameterField().length; i++ ) {
          data.keynrs[i] = rowMeta.indexOfValue( meta.getParameterField()[i] );
          if ( data.keynrs[i] < 0 ) {
            throw new KettleStepException( BaseMessages.getString( PKG, "DatabaseJoin.Exception.FieldNotFound", meta
                .getParameterField()[i] ) );
          }

          data.lookupRowMeta.addValueMeta( rowMeta.getValueMeta( data.keynrs[i] ).clone() );
        }
      }

      // Construct the parameters row...
      Object[] lookupRowData = new Object[data.lookupRowMeta.size()];
      for ( int i = 0; i < data.keynrs.length; i++ ) {
        lookupRowData[i] = rowData[data.keynrs[i]];
      }

      // Set the values on the prepared statement (for faster exec.)
      rs = data.db.openQuery( data.pstmt, data.lookupRowMeta, lookupRowData );

      // Get a row from the database...
      //
      Object[] add = data.db.getRow( rs );
      RowMetaInterface addMeta = data.db.getReturnRowMeta();

      incrementLinesInput();

      int counter = 0;
      while ( add != null && ( meta.getRowLimit() == 0 || counter < meta.getRowLimit() ) ) {
        counter++;

        Object[] newRow = RowDataUtil.resizeArray( rowData, data.outputRowMeta.size() );
        int newIndex = rowMeta.size();
        for ( int i = 0; i < addMeta.size(); i++ ) {
          newRow[newIndex++] = add[i];
        }
        // we have to clone, otherwise we only get the last new value
        putRow( data.outputRowMeta, data.outputRowMeta.cloneRow( newRow ) );

        if ( log.isRowLevel() ) {
          logRowlevel( BaseMessages.getString( PKG, "DatabaseJoin.Log.PutoutRow" )
              + data.outputRowMeta.getString( newRow ) );
        }

        // Get a new row
        if ( meta.getRowLimit() == 0 || counter < meta.getRowLimit() ) {
          add = data.db.getRow( rs );
          incrementLinesInput();
        }
      }

      // Nothing found? Perhaps we have to put something out after all?
      if ( counter == 0 && meta.isOuterJoin() ) {
        if ( data.notfound == null ) {
          // Just return null values for all values...
          //
          data.notfound = new Object[data.db.getReturnRowMeta().size()];
        }
        Object[] newRow = RowDataUtil.resizeArray( rowData, data.outputRowMeta.size() );
        int newIndex = rowMeta.size();
        for ( int i = 0; i < data.notfound.length; i++ ) {
          newRow[newIndex++] = data.notfound[i];
        }
        putRow( data.outputRowMeta, newRow );
      }

      data.db.closeQuery( rs );
    } finally {
      dbLock.unlock();
    }
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    boolean sendToErrorRow = false;
    String errorMessage = null;

    Object[] r = getRow(); // Get row from input rowset & set row busy!
    if ( r == null ) { // no more input to be expected...
      setOutputDone();
      return false;
    }

    try {
      lookupValues( (DatabaseJoinMeta) smi, (DatabaseJoinData) sdi, getInputRowMeta(), r ); // add new values to the row in rowset[0].
      if ( checkFeedback( getLinesRead() ) ) {
        if ( log.isBasic() ) {
          logBasic( BaseMessages.getString( PKG, "DatabaseJoin.Log.LineNumber" ) + getLinesRead() );
        }
      }
    } catch ( KettleException e ) {
      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {

        logError( BaseMessages.getString( PKG, "DatabaseJoin.Log.ErrorInStepRunning" ) + e.getMessage(), e );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), r, 1, errorMessage, null, "DBJOIN001" );
      }
    }

    return true;
  }

  /**
   * Stop the running query
   * [PDI-17820] - In the Database Join step data.isCancelled is checked before synchronization and set after synchronization is completed.
   *
   * To cancel a prepared statement we need a valid database connection which we do not have if disposed has already been called
   *
   *
   * */
  public void stopRunning( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    if ( this.isStopped() || sdi.isDisposed() ) {
      return;
    }

    final DatabaseJoinData data = (DatabaseJoinData) sdi;

    dbLock.lock();
    try {
      if ( data.db != null && data.db.getConnection() != null && !data.isCanceled ) {
        data.db.cancelStatement( data.pstmt );
        setStopped( true );
        data.isCanceled = true;
      }
    } finally {
      dbLock.unlock();
    }
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {

    final DatabaseJoinMeta meta = (DatabaseJoinMeta) smi;
    final DatabaseJoinData data = (DatabaseJoinData) sdi;
    dbLock.lock();

    boolean initialized = super.init( smi, sdi );
    if ( initialized ) {
      data.db.setQueryLimit( meta.getRowLimit() );
    }
    dbLock.unlock();

    return initialized;
  }

  private void prepareSQL( DatabaseJoinMeta meta, DatabaseJoinData data ) throws KettleDatabaseException {
    try {
      String sql = meta.getSql();
      if ( meta.isVariableReplace() ) {
        sql = environmentSubstitute( sql );
      }
      // Prepare the SQL statement
      data.pstmt = data.db.prepareSQL( sql );
      if ( log.isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "DatabaseJoin.Log.SQLStatement", sql ) );
      }
    } catch ( KettleException e ) {
      logError( BaseMessages.getString( PKG, "DatabaseJoin.Log.DatabaseError" ) + e.getMessage() );
      throw e;
    }
  }

  @Override
  protected Class<?> getPKG() {
    return PKG;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    dbLock.lock();
    try {
      super.dispose( smi, sdi );
    } finally {
      dbLock.unlock();
    }
  }
}
