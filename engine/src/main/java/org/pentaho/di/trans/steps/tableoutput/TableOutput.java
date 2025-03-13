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


package org.pentaho.di.trans.steps.tableoutput;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.PartitionDatabaseMeta;
import org.pentaho.di.core.database.SqlScriptStatement;
import org.pentaho.di.core.exception.KettleDatabaseBatchException;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Writes rows to a database table.
 *
 * @author Matt Casters
 * @since 6-apr-2003
 */
public class TableOutput extends BaseDatabaseStep implements StepInterface {
  private static Class<?> PKG = TableOutputMeta.class; // for i18n purposes, needed by Translator2!!

  private TableOutputMeta meta;
  private TableOutputData data;

  public TableOutput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (TableOutputMeta) smi;
    data = (TableOutputData) sdi;

    Object[] r = getRow(); // this also waits for a previous step to be finished.
    if ( r == null ) { // no more input to be expected...
      // truncate the table if there are no rows at all coming into this step
      if ( first && meta.truncateTable() ) {
        truncateTable();
      }
      return false;
    }

    if ( first ) {
      first = false;
      if ( meta.truncateTable() ) {
        truncateTable();
      }
      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      if ( !meta.specifyFields() ) {
        // Just take the input row
        data.insertRowMeta = getInputRowMeta().clone();
      } else {

        data.insertRowMeta = new RowMeta();

        //
        // Cache the position of the compare fields in Row row
        //
        data.valuenrs = new int[meta.getFieldDatabase().length];
        for ( int i = 0; i < meta.getFieldDatabase().length; i++ ) {
          data.valuenrs[i] = getInputRowMeta().indexOfValue( meta.getFieldStream()[i] );
          if ( data.valuenrs[i] < 0 ) {
            throw new KettleStepException( BaseMessages.getString(
              PKG, "TableOutput.Exception.FieldRequired", meta.getFieldStream()[i] ) );
          }
        }

        for ( int i = 0; i < meta.getFieldDatabase().length; i++ ) {
          ValueMetaInterface insValue = getInputRowMeta().searchValueMeta( meta.getFieldStream()[i] );
          if ( insValue != null ) {
            ValueMetaInterface insertValue = insValue.clone();
            insertValue.setName( meta.getFieldDatabase()[i] );
            data.insertRowMeta.addValueMeta( insertValue );
          } else {
            throw new KettleStepException( BaseMessages.getString(
              PKG, "TableOutput.Exception.FailedToFindField", meta.getFieldStream()[i] ) );
          }
        }
      }
    }

    try {
      Object[] outputRowData = writeToTable( getInputRowMeta(), r );
      if ( outputRowData != null ) {
        putRow( data.outputRowMeta, outputRowData ); // in case we want it go further...
        incrementLinesOutput();
      }

      if ( checkFeedback( getLinesRead() ) ) {
        if ( log.isBasic() ) {
          logBasic( "linenr " + getLinesRead() );
        }
      }
    } catch ( KettleException e ) {
      logError( "Because of an error, this step can't continue: ", e );
      setErrors( 1 );
      stopAll();
      setOutputDone(); // signal end to receiver(s)
      return false;
    }

    return true;
  }

  protected Object[] writeToTable( RowMetaInterface rowMeta, Object[] r ) throws KettleException {

    if ( r == null ) { // Stop: last line or error encountered
      if ( log.isDetailed() ) {
        logDetailed( "Last line inserted: stop" );
      }
      return null;
    }

    PreparedStatement insertStatement = null;
    Object[] insertRowData;
    Object[] outputRowData = r;

    String tableName = null;

    boolean sendToErrorRow = false;
    String errorMessage = null;
    boolean rowIsSafe = false;
    int[] updateCounts = null;
    List<Exception> exceptionsList = null;
    boolean batchProblem = false;
    Object generatedKey = null;

    if ( meta.isTableNameInField() ) {
      // Cache the position of the table name field
      if ( data.indexOfTableNameField < 0 ) {
        String realTablename = environmentSubstitute( meta.getTableNameField() );
        data.indexOfTableNameField = rowMeta.indexOfValue( realTablename );
        if ( data.indexOfTableNameField < 0 ) {
          String message = "Unable to find table name field [" + realTablename + "] in input row";
          logError( message );
          throw new KettleStepException( message );
        }
        if ( !meta.isTableNameInTable() && !meta.specifyFields() ) {
          data.insertRowMeta.removeValueMeta( data.indexOfTableNameField );
        }
      }
      tableName = rowMeta.getString( r, data.indexOfTableNameField );
      if ( !meta.isTableNameInTable() && !meta.specifyFields() ) {
        // If the name of the table should not be inserted itself, remove the table name
        // from the input row data as well. This forcibly creates a copy of r
        //
        insertRowData = RowDataUtil.removeItem( rowMeta.cloneRow( r ), data.indexOfTableNameField );
      } else {
        insertRowData = r;
      }
    } else if ( meta.isPartitioningEnabled()
      && ( meta.isPartitioningDaily() || meta.isPartitioningMonthly() )
      && ( meta.getPartitioningField() != null && meta.getPartitioningField().length() > 0 ) ) {
      // Initialize some stuff!
      if ( data.indexOfPartitioningField < 0 ) {
        data.indexOfPartitioningField =
          rowMeta.indexOfValue( environmentSubstitute( meta.getPartitioningField() ) );
        if ( data.indexOfPartitioningField < 0 ) {
          throw new KettleStepException( "Unable to find field ["
            + meta.getPartitioningField() + "] in the input row!" );
        }

        if ( meta.isPartitioningDaily() ) {
          data.dateFormater = new SimpleDateFormat( "yyyyMMdd" );
        } else {
          data.dateFormater = new SimpleDateFormat( "yyyyMM" );
        }
      }

      ValueMetaInterface partitioningValue = rowMeta.getValueMeta( data.indexOfPartitioningField );
      if ( !partitioningValue.isDate() || r[data.indexOfPartitioningField] == null ) {
        throw new KettleStepException(
          "Sorry, the partitioning field needs to contain a data value and can't be empty!" );
      }

      Object partitioningValueData = rowMeta.getDate( r, data.indexOfPartitioningField );
      tableName =
        environmentSubstitute( meta.getTableName() )
          + "_" + data.dateFormater.format( (Date) partitioningValueData );
      insertRowData = r;
    } else {
      tableName = data.tableName;
      insertRowData = r;
    }

    if ( meta.specifyFields() ) {
      //
      // The values to insert are those in the fields sections
      //
      insertRowData = new Object[data.valuenrs.length];
      for ( int idx = 0; idx < data.valuenrs.length; idx++ ) {
        insertRowData[idx] = r[data.valuenrs[idx]];
      }
    }

    if ( Utils.isEmpty( tableName ) ) {
      throw new KettleStepException( "The tablename is not defined (empty)" );
    }

    insertStatement = data.preparedStatements.get( tableName );
    if ( insertStatement == null ) {
      String sql =
        data.db
          .getInsertStatement( environmentSubstitute( meta.getSchemaName() ), tableName, data.insertRowMeta );
      if ( log.isDetailed() ) {
        logDetailed( "Prepared statement : " + sql );
      }
      insertStatement = data.db.prepareSQL( sql, meta.isReturningGeneratedKeys() );
      data.preparedStatements.put( tableName, insertStatement );
    }

    try {
      // For PG & GP, we add a savepoint before the row.
      // Then revert to the savepoint afterwards... (not a transaction, so hopefully still fast)
      //
      if ( data.useSafePoints ) {
        data.savepoint = data.db.setSavepoint();
      }
      data.db.setValues( data.insertRowMeta, insertRowData, insertStatement );
      data.db.insertRow( insertStatement, data.batchMode, false ); // false: no commit, it is handled in this step differently
      if ( isRowLevel() ) {
        logRowlevel( "Written row: " + data.insertRowMeta.getString( insertRowData ) );
      }

      // Get a commit counter per prepared statement to keep track of separate tables, etc.
      //
      Integer commitCounter = data.commitCounterMap.get( tableName );
      if ( commitCounter == null ) {
        commitCounter = Integer.valueOf( 1 );
      } else {
        commitCounter++;
      }
      data.commitCounterMap.put( tableName, Integer.valueOf( commitCounter.intValue() ) );

      // Release the savepoint if needed
      //
      if ( data.useSafePoints ) {
        if ( data.releaseSavepoint ) {
          data.db.releaseSavepoint( data.savepoint );
        }
      }

      // Perform a commit if needed
      //

      if ( ( data.commitSize > 0 ) && ( ( commitCounter % data.commitSize ) == 0 ) ) {
        if ( data.db.getUseBatchInsert( data.batchMode ) ) {
          try {
            insertStatement.executeBatch();
            data.db.commit();
            insertStatement.clearBatch();
          } catch ( SQLException ex ) {
            throw Database.createKettleDatabaseBatchException( "Error updating batch", ex );
          } catch ( Exception ex ) {
            throw new KettleDatabaseException( "Unexpected error inserting row", ex );
          }
        } else {
          // insertRow normal commit
          data.db.commit();
        }
        // Clear the batch/commit counter...
        //
        data.commitCounterMap.put( tableName, Integer.valueOf( 0 ) );
        rowIsSafe = true;
      } else {
        rowIsSafe = false;
      }

      // See if we need to get back the keys as well...
      if ( meta.isReturningGeneratedKeys() ) {
        RowMetaAndData extraKeys = data.db.getGeneratedKeys( insertStatement );

        if ( extraKeys.getRowMeta().size() > 0 ) {
          // Send out the good word!
          // Only 1 key at the moment. (should be enough for now :-)
          generatedKey = extraKeys.getRowMeta().getInteger( extraKeys.getData(), 0 );
        } else {
          // we have to throw something here, else we don't know what the
          // type is of the returned key(s) and we would violate our own rule
          // that a hop should always contain rows of the same type.
          throw new KettleStepException( "No generated keys while \"return generated keys\" is active!" );
        }
      }
    } catch ( KettleDatabaseBatchException be ) {
      errorMessage = be.toString();
      batchProblem = true;
      sendToErrorRow = true;
      updateCounts = be.getUpdateCounts();
      exceptionsList = be.getExceptionsList();

      if ( getStepMeta().isDoingErrorHandling() ) {
        data.db.clearBatch( insertStatement );
        data.db.commit( true );
      } else {
        data.db.clearBatch( insertStatement );
        data.db.rollback();
        StringBuilder msg = new StringBuilder( "Error batch inserting rows into table [" + tableName + "]." );
        msg.append( Const.CR );
        msg.append( "Errors encountered (first 10):" ).append( Const.CR );
        for ( int x = 0; x < be.getExceptionsList().size() && x < 10; x++ ) {
          Exception exception = be.getExceptionsList().get( x );
          if ( exception.getMessage() != null ) {
            msg.append( exception.getMessage() ).append( Const.CR );
          }
        }
        throw new KettleException( msg.toString(), be );
      }
    } catch ( KettleDatabaseException dbe ) {
      if ( getStepMeta().isDoingErrorHandling() ) {
        if ( isRowLevel() ) {
          logRowlevel( "Written row to error handling : " + getInputRowMeta().getString( r ) );
        }

        if ( data.useSafePoints ) {
          data.db.rollback( data.savepoint );
          if ( data.releaseSavepoint ) {
            data.db.releaseSavepoint( data.savepoint );
          }
          // data.db.commit(true); // force a commit on the connection too.
        }

        sendToErrorRow = true;
        errorMessage = dbe.toString();
      } else {
        if ( meta.ignoreErrors() ) {
          if ( data.warnings < 20 ) {
            if ( log.isBasic() ) {
              logBasic( "WARNING: Couldn't insert row into table: "
                + rowMeta.getString( r ) + Const.CR + dbe.getMessage() );
            }
          } else if ( data.warnings == 20 ) {
            if ( log.isBasic() ) {
              logBasic( "FINAL WARNING (no more then 20 displayed): Couldn't insert row into table: "
                + rowMeta.getString( r ) + Const.CR + dbe.getMessage() );
            }
          }
          data.warnings++;
        } else {
          setErrors( getErrors() + 1 );
          data.db.rollback();
          throw new KettleException( "Error inserting row into table ["
            + tableName + "] with values: " + rowMeta.getString( r ), dbe );
        }
      }
    }

    // We need to add a key
    if ( generatedKey != null ) {
      outputRowData = RowDataUtil.addValueData( outputRowData, rowMeta.size(), generatedKey );
    }

    if ( data.batchMode ) {
      if ( sendToErrorRow ) {
        if ( batchProblem ) {
          data.batchBuffer.add( outputRowData );
          outputRowData = null;

          processBatchException( errorMessage, updateCounts, exceptionsList );
        } else {
          // Simply add this row to the error row
          putError( rowMeta, r, 1L, errorMessage, null, "TOP001" );
          outputRowData = null;
        }
      } else {
        data.batchBuffer.add( outputRowData );
        outputRowData = null;

        if ( rowIsSafe ) { // A commit was done and the rows are all safe (no error)
          for ( int i = 0; i < data.batchBuffer.size(); i++ ) {
            Object[] row = data.batchBuffer.get( i );
            putRow( data.outputRowMeta, row );
            incrementLinesOutput();
          }
          // Clear the buffer
          data.batchBuffer.clear();
        }
      }
    } else {
      if ( sendToErrorRow ) {
        putError( rowMeta, r, 1, errorMessage, null, "TOP001" );
        outputRowData = null;
      }
    }

    return outputRowData;
  }

  public boolean isRowLevel() {
    return log.isRowLevel();
  }

  private void processBatchException( String errorMessage, int[] updateCounts, List<Exception> exceptionsList ) throws KettleException {
    // There was an error with the commit
    // We should put all the failing rows out there...
    //
    if ( updateCounts != null ) {
      int errNr = 0;
      for ( int i = 0; i < updateCounts.length; i++ ) {
        Object[] row = data.batchBuffer.get( i );
        if ( updateCounts[i] > 0 ) {
          // send the error foward
          putRow( data.outputRowMeta, row );
          incrementLinesOutput();
        } else {
          String exMessage = errorMessage;
          if ( errNr < exceptionsList.size() ) {
            SQLException se = (SQLException) exceptionsList.get( errNr );
            errNr++;
            exMessage = se.toString();
          }
          putError( data.outputRowMeta, row, 1L, exMessage, null, "TOP0002" );
        }
      }
    } else {
      // If we don't have update counts, it probably means the DB doesn't support it.
      // In this case we don't have a choice but to consider all inserted rows to be error rows.
      //
      for ( int i = 0; i < data.batchBuffer.size(); i++ ) {
        Object[] row = data.batchBuffer.get( i );
        putError( data.outputRowMeta, row, 1L, errorMessage, null, "TOP0003" );
      }
    }

    // Clear the buffer afterwards...
    data.batchBuffer.clear();
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (TableOutputMeta) smi;
    data = (TableOutputData) sdi;

    if ( super.init( smi, sdi ) ) {
      try {
        data.commitSize = Integer.parseInt( environmentSubstitute( meta.getCommitSize() ) );

        data.databaseMeta = meta.getDatabaseMeta();
        DatabaseInterface dbInterface = data.databaseMeta.getDatabaseInterface();

        // Batch updates are not supported on PostgreSQL (and look-a-likes)
        // together with error handling (PDI-366).
        // For these situations we can use savepoints to help out.
        data.useSafePoints =
          data.databaseMeta.getDatabaseInterface().useSafePoints() && getStepMeta().isDoingErrorHandling();

        // Get the boolean that indicates whether or not we can/should release
        // savepoints during data load.
        data.releaseSavepoint = dbInterface.releaseSavepoint();

        // Disable batch mode in case
        // - we use an unlimited commit size
        // - if we need to pick up auto-generated keys
        // - if you are running the transformation as a single database transaction (unique connections)
        // - if we are reverting to save-points
        data.batchMode =
          meta.useBatchUpdate()
            && data.commitSize > 0 && !meta.isReturningGeneratedKeys()
            && !getTransMeta().isUsingUniqueConnections() && !data.useSafePoints;

        // Per PDI-6211 : give a warning that batch mode operation in combination with step error handling can lead to
        // incorrectly processed rows.
        if ( getStepMeta().isDoingErrorHandling() && !dbInterface.supportsErrorHandlingOnBatchUpdates() ) {
          log.logMinimal( BaseMessages.getString(
            PKG, "TableOutput.Warning.ErrorHandlingIsNotFullySupportedWithBatchProcessing" ) );
        }

        if ( !dbInterface.supportsStandardTableOutput() ) {
          throw new KettleException( dbInterface.getUnsupportedTableOutputMessage() );
        }

        if ( log.isBasic() ) {
          logBasic( "Connected to database [" + meta.getDatabaseMeta() + "] (commit=" + data.commitSize + ")" );
        }

        // Postpone commit as long as possible. PDI-2091
        if ( data.commitSize == 0 ) {
          data.commitSize = Integer.MAX_VALUE;
        }
        data.db.setCommitSize( data.commitSize );

        if ( !meta.isPartitioningEnabled() && !meta.isTableNameInField() ) {
          data.tableName = environmentSubstitute( meta.getTableName() );
        }

        return true;
      } catch ( KettleException e ) {
        logError( "An error occurred intialising this step: " + e.getMessage() );
        stopAll();
        setErrors( 1 );
      }
    }
    return false;
  }

  @Override
  protected Class<?> getPKG() {
    return PKG;
  }

  void truncateTable() throws KettleDatabaseException {
    if ( !meta.isPartitioningEnabled() && !meta.isTableNameInField() ) {
      // Only the first one truncates in a non-partitioned step copy
      //
      if ( meta.truncateTable()
        && ( ( getCopy() == 0 && getUniqueStepNrAcrossSlaves() == 0 ) || !Utils.isEmpty( getPartitionID() ) ) ) {
        data.db.truncateTable( environmentSubstitute( meta.getSchemaName() ), environmentSubstitute( meta
          .getTableName() ) );

      }
    }
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (TableOutputMeta) smi;
    data = (TableOutputData) sdi;

    if ( data.db != null ) {
      try {
        for ( String schemaTable : data.preparedStatements.keySet() ) {
          // Get a commit counter per prepared statement to keep track of separate tables, etc.
          //
          Integer batchCounter = data.commitCounterMap.get( schemaTable );
          if ( batchCounter == null ) {
            batchCounter = 0;
          }

          PreparedStatement insertStatement = data.preparedStatements.get( schemaTable );

          data.db.emptyAndCommit( insertStatement, data.batchMode, batchCounter );
        }
        for ( int i = 0; i < data.batchBuffer.size(); i++ ) {
          Object[] row = data.batchBuffer.get( i );
          putRow( data.outputRowMeta, row );
          incrementLinesOutput();
        }
        // Clear the buffer
        data.batchBuffer.clear();
      } catch ( KettleDatabaseBatchException be ) {
        if ( getStepMeta().isDoingErrorHandling() ) {
          // Right at the back we are experiencing a batch commit problem...
          // OK, we have the numbers...
          try {
            processBatchException( be.toString(), be.getUpdateCounts(), be.getExceptionsList() );
          } catch ( KettleException e ) {
            logError( "Unexpected error processing batch error", e );
            setErrors( 1 );
            stopAll();
          }
        } else {
          logError( "Unexpected batch update error committing the database connection.", be );
          setErrors( 1 );
          stopAll();
        }
      } catch ( Exception dbe ) {
        logError( "Unexpected error committing the database connection.", dbe );
        logError( Const.getStackTracker( dbe ) );
        setErrors( 1 );
        stopAll();
      } finally {
        setOutputDone();

        if ( getErrors() > 0 ) {
          try {
            data.db.rollback();
          } catch ( KettleDatabaseException e ) {
            logError( "Unexpected error rolling back the database connection.", e );
          }
        }
      }
      super.dispose( smi, sdi );
    }
  }

  @Override
  public JSONObject doAction( String fieldName, StepMetaInterface stepMetaInterface, TransMeta transMeta,
                              Trans trans, Map<String, String> queryParamToValues ) {
    JSONObject response = new JSONObject();
    try {
      Method actionMethod = TableOutput.class.getDeclaredMethod( fieldName + "Action", Map.class );
      this.setStepMetaInterface( stepMetaInterface );
      response = (JSONObject) actionMethod.invoke( this, queryParamToValues );

    } catch ( NoSuchMethodException | InvocationTargetException | IllegalAccessException e ) {
      log.logError( e.getMessage() );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_METHOD_NOT_RESPONSE );
    }
    return response;
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  private JSONObject getSQLAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
    try {
      SQLStatement sql = sql( queryParams.get( "stepName" ), queryParams.get( "connection" ) );
      if ( Objects.nonNull( sql ) ) {
        if ( !sql.hasError() ) {
          if ( sql.hasSQL() ) {
            response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
            response.put( "sqlString", sql.getSQL() );
          } else {
            response.put( "details", BaseMessages.getString( PKG, "TableOutput.NoSQL.DialogMessage" ) );
          }
        } else {
          response.put( "details", sql.getError() );
        }
      } else {
        response.put( "details", BaseMessages.getString( PKG, "TableOutput.NoSQL.EmptyCSVFields" ) );
      }

    } catch ( KettleStepException e ) {
      log.logError( e.getMessage() );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_METHOD_NOT_RESPONSE );
      response.put( "details", e.getMessage() );
    }
    return response;
  }

  public SQLStatement sql( String stepName, String connection ) throws KettleStepException {

    TableOutputMeta info = (TableOutputMeta) getStepMetaInterface();
    info.setDatabaseMeta( getTransMeta().findDatabase( connection ) );

    RowMetaInterface prev = getTransMeta().getPrevStepFields( stepName );
    if ( info.isTableNameInField() && !info.isTableNameInTable() && info.getTableNameField().length() > 0 ) {
      int idx = prev.indexOfValue( info.getTableNameField() );
      if ( idx >= 0 ) {
        prev.removeValueMeta( idx );
      }
    }
    StepMeta stepMeta = getTransMeta().findStep( stepName );

    if ( info.specifyFields() ) {
      // Only use the fields that were specified.
      RowMetaInterface prevNew = new RowMeta();

      for ( int i = 0; i < info.getFieldDatabase().length; i++ ) {
        ValueMetaInterface insValue = prev.searchValueMeta( info.getFieldStream()[ i ] );
        if ( insValue != null ) {
          ValueMetaInterface insertValue = insValue.clone();
          insertValue.setName( info.getFieldDatabase()[ i ] );
          prevNew.addValueMeta( insertValue );
        } else {
          throw new KettleStepException( BaseMessages.getString(
            PKG, "TableOutputDialog.FailedToFindField.Message", info.getFieldStream()[ i ] ) );
        }
      }
      prev = prevNew;
    }

    boolean autoInc = false;
    String pk = null;

    // Add the auto-increment field too if any is present.
    //
    if ( info.isReturningGeneratedKeys() && !Utils.isEmpty( info.getGeneratedKeyField() ) ) {
      ValueMetaInterface valueMeta = new ValueMetaInteger( info.getGeneratedKeyField() );
      valueMeta.setLength( 15 );
      prev.addValueMeta( 0, valueMeta );
      autoInc = true;
      pk = info.getGeneratedKeyField();
    }

    if ( isValidRowMeta( prev ) ) {
      SQLStatement sql = info.getSQLStatements( getTransMeta(), stepMeta, prev, pk, autoInc, pk );
      return sql;
    } else {
      return null;
    }

  }

  private static boolean isValidRowMeta( RowMetaInterface rowMeta ) {
    for ( ValueMetaInterface value : rowMeta.getValueMetaList() ) {
      String name = value.getName();
      if ( name == null || name.isEmpty() ) {
        return false;
      }
    }
    return true;
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  private JSONObject getSchemaAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
    try {
      String[] schemas = getSchemaNames( queryParams.get( "connection" ) );
      JSONArray schemaNames = new JSONArray();
      for ( String schema : schemas ) {
        schemaNames.add( schema );
      }
      response.put( "schemaNames", schemaNames );
      response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    } catch ( KettleDatabaseException e ) {
      log.logError( e.getMessage() );
    }
    return response;
  }

  public String[] getSchemaNames( String dbname ) throws KettleDatabaseException {
    DatabaseMeta databaseMeta = getTransMeta().findDatabase( dbname );
    if ( databaseMeta != null ) {
      LoggingObjectInterface loggingObject = new SimpleLoggingObject(
        "Table Output Step", LoggingObjectType.STEP, null );
      Database database = new Database( loggingObject, databaseMeta );
      try {
        database.connect();
        return Const.sortStrings( database.getSchemas() );
      } finally {
        database.disconnect();
      }
    }
    return new String[ 0 ];
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  private JSONObject getExecAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    String connectionName = queryParams.get( "connection" );
    String sqlScript = queryParams.get( "sqlScript" );
    TableOutputSQLDTO outputSQLDTO = exec( connectionName, sqlScript );
    if ( Objects.nonNull( outputSQLDTO.getBufferRowMeta() ) ) {
      response = generateSQLResultsJSON( outputSQLDTO );
    }
    response.put( "isError", outputSQLDTO.isError() );
    response.put( "message", outputSQLDTO.message );
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  private TableOutputSQLDTO exec( String connectionName, String sqlScript ) {

    DatabaseMeta ci = getTransMeta().findDatabase( connectionName );
    TableOutputMeta info = (TableOutputMeta) getStepMetaInterface();
    info.setDatabaseMeta( ci );
    DatabaseMeta connection = ci;
    if ( ci == null ) {
      return null;
    }
    StringBuilder message = new StringBuilder();
    TableOutputSQLDTO tableOutputSQLDTO = new TableOutputSQLDTO();
    LoggingObjectInterface loggingObject = new SimpleLoggingObject( "Table Output Step", LoggingObjectType.STEP, null );
    Database db = new Database( loggingObject, ci );
    boolean first = true;
    PartitionDatabaseMeta[] partitioningInformation = ci.getPartitioningInformation();
    for ( int partitionNr = 0; first
      || ( partitioningInformation != null && partitionNr < partitioningInformation.length ); partitionNr++ ) {
      first = false;
      String partitionId = null;
      if ( partitioningInformation != null && partitioningInformation.length > 0 ) {
        partitionId = partitioningInformation[ partitionNr ].getPartitionId();
      }
      try {
        db.connect( partitionId );
        List<SqlScriptStatement> statements = ci.getDatabaseInterface().getSqlScriptStatements( sqlScript + Const.CR );
        int nrstats = 0;
        for ( SqlScriptStatement sql : statements ) {
          if ( sql.isQuery() ) {
            // A Query
            log.logDetailed( "launch SELECT statement: " + Const.CR + sql );
            nrstats++;
            tableOutputSQLDTO.setQuery( true );
            try {
              List<Object[]> rows = db.getRows( sql.getStatement(), 1000 );
              RowMetaInterface rowMeta = db.getReturnRowMeta();
              if ( rows.size() > 0 ) {
                tableOutputSQLDTO.setBufferRowData( rows );
                tableOutputSQLDTO.setBufferRowMeta( rowMeta );
                return tableOutputSQLDTO;
              }
            } catch ( KettleDatabaseException dbe ) {
              tableOutputSQLDTO.setMessage( "An error occured while executing the following sql : " + sql );
            }
          } else {
            log.logDetailed( "launch DDL statement: " + Const.CR + sql );
            // A DDL statement
            nrstats++;
            int startLogLine = KettleLogStore.getLastBufferLineNr();
            try {
              log.logDetailed( "Executing SQL: " + Const.CR + sql );
              db.execStatement( sql.getStatement() );
              // Clear the database cache, in case we're using one...
              DBCache dbcache = getTransMeta().getDbCache();
              if ( dbcache != null ) {
                dbcache.clear( ci.getName() );
              }
            } catch ( Exception dbe ) {
              tableOutputSQLDTO.setError( true );
              tableOutputSQLDTO.setMessage( "Error while executing : " + sql );
            } finally {
              int endLogLine = KettleLogStore.getLastBufferLineNr();
              sql.setLoggingText( KettleLogStore.getAppender().getLogBufferFromTo(
                db.getLogChannelId(), true, startLogLine, endLogLine ).toString() );
              sql.setComplete( true );
            }
          }
        }
      } catch ( KettleDatabaseException dbe ) {
        tableOutputSQLDTO.setError( true );
        tableOutputSQLDTO.setMessage(
          "Unable to connect to the database\\!\\nPlease check the connection setting for " + connectionName );
      } finally {
        db.disconnect();
      }
    }
    return tableOutputSQLDTO;
  }


  public JSONObject generateSQLResultsJSON( TableOutputSQLDTO metaData ) {
    JSONArray columnInfoArray = new JSONArray();
    for ( int i = 0; i < metaData.bufferRowMeta.size(); i++ ) {
      String columnName = metaData.bufferRowMeta.getValueMeta( i ).getName();
      columnInfoArray.add( columnName );
    }
    JSONArray rowsArray = new JSONArray();
    for ( int i = 0; i < metaData.bufferRowData.size(); i++ ) {
      Object[] row = metaData.bufferRowData.get( i );

      JSONArray dataArray = new JSONArray();
      int columnCount = Math.min( row.length, columnInfoArray.size() );
      for ( int j = 0; j < columnCount; j++ ) {
        Object data = row[ j ];
        dataArray.add( data != null ? data.toString() : null );
      }

      JSONObject rowObject = new JSONObject();
      rowObject.put( "data", dataArray );

      rowsArray.add( rowObject );
    }
    JSONObject stepJSON = new JSONObject();
    stepJSON.put( "columnInfo", columnInfoArray );
    stepJSON.put( "rows", rowsArray );

    return stepJSON;
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  private JSONObject getTableFieldAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    String connectionName = queryParams.get( "connection" );
    String schema = queryParams.get( "schema" );
    String table = queryParams.get( "table" );
    String[] columns = getTableFields( connectionName, schema, table );
    JSONArray columnsList = new JSONArray();
    for ( String column : columns ) {
      columnsList.add( column );
    }
    response.put( "columns", columnsList );
    response.put( "actionStatus", StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  private String[] getTableFields( String connection, String schema, String table ) {
    DatabaseMeta databaseMeta = getTransMeta().findDatabase( connection );
    LoggingObjectInterface loggingObject = new SimpleLoggingObject(
      "Table Output Step", LoggingObjectType.STEP, null );
    Database db = new Database( loggingObject, databaseMeta );
    try {
      db.connect();
      RowMetaInterface r =
        db.getTableFieldsMeta( schema, table );
      if ( null != r ) {
        String[] fieldNames = r.getFieldNames();
        if ( null != fieldNames ) {
          return fieldNames;
        }
      }
    } catch ( Exception e ) {
      // ignore any errors here. drop downs will not be
      // filled, but no problem for the user
    } finally {
      try {
        if ( db != null ) {
          db.disconnect();
        }
      } catch ( Exception ignored ) {
        // ignore any errors here. Nothing we can do if
        // connection fails to close properly
        db = null;
      }
    }
    return null;
  }


  /**
   * Allows subclasses of TableOuput to get hold of the step meta
   *
   * @return
   */
  protected TableOutputMeta getMeta() {
    return meta;
  }

  /**
   * Allows subclasses of TableOutput to get hold of the data object
   *
   * @return
   */
  protected TableOutputData getData() {
    return data;
  }

  protected void setMeta( TableOutputMeta meta ) {
    this.meta = meta;
  }

  protected void setData( TableOutputData data ) {
    this.data = data;
  }
}
