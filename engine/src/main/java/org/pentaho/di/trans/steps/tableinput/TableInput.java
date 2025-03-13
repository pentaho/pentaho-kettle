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


package org.pentaho.di.trans.steps.tableinput;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

/**
 * Reads information from a database table by using freehand SQL
 *
 * @author Matt
 * @since 8-apr-2003
 */
public class TableInput extends BaseDatabaseStep implements StepInterface {
  private static Class<?> PKG = TableInputMeta.class; // for i18n purposes, needed by Translator2!!

  private final ReentrantLock dbLock = new ReentrantLock();

  private TableInputMeta meta;
  private TableInputData data;

  public TableInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  private RowMetaAndData readStartDate() throws KettleException {
    if ( log.isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "TableInput.Log.ReadingFromStep", data.infoStream.getStepname() ) );
    }

    RowMetaInterface parametersMeta = new RowMeta();
    Object[] parametersData = new Object[] {};

    RowSet rowSet = findInputRowSet( data.infoStream.getStepname() );
    if ( rowSet != null ) {
      Object[] rowData = getRowFrom( rowSet ); // rows are originating from "lookup_from"
      while ( rowData != null ) {
        parametersData = RowDataUtil.addRowData( parametersData, parametersMeta.size(), rowData );
        parametersMeta.addRowMeta( rowSet.getRowMeta() );

        rowData = getRowFrom( rowSet ); // take all input rows if needed!
      }

      if ( parametersMeta.size() == 0 ) {
        throw new KettleException( BaseMessages.getString( PKG,
          "TableInput.Exception.NoParametersFound", data.infoStream.getStepname() ) );
      }
    } else {
      throw new KettleException( BaseMessages.getString( PKG, "TableInput.Exception.NoRowSetFound", data.infoStream.getStepname() ) );
    }

    RowMetaAndData parameters = new RowMetaAndData( parametersMeta, parametersData );

    return parameters;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    dbLock.lock();
    try {

      if ( first ) { // we just got started

        Object[] parameters;
        RowMetaInterface parametersMeta;
        first = false;

        // Make sure we read data from source steps...
        if ( data.infoStream.getStepMeta() != null ) {
          if ( meta.isExecuteEachInputRow() ) {
            if ( log.isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "TableInput.Log.ReadingSingleRow", data.infoStream.getStepname() ) );
            }
            data.rowSet = findInputRowSet( data.infoStream.getStepname() );
            if ( data.rowSet == null ) {
              throw new KettleException( BaseMessages.getString( PKG, "TableInput.Exception.NoRowSetFound", data.infoStream.getStepname() ) );
            }
            parameters = getRowFrom( data.rowSet );
            parametersMeta = data.rowSet.getRowMeta();
          } else {
            if ( log.isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "TableInput.Log.ReadingQueryParameters", data.infoStream.getStepname() ) );
            }
            RowMetaAndData rmad = readStartDate(); // Read values in lookup table (look)
            parameters = rmad.getData();
            parametersMeta = rmad.getRowMeta();
          }
          if ( parameters != null ) {
            if ( log.isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "TableInput.Log.QueryParametersFound", parametersMeta.getString( parameters ) ) );
            }
          }
        } else {
          parameters = new Object[] {};
          parametersMeta = new RowMeta();
        }

        if ( meta.isExecuteEachInputRow() && ( parameters == null || parametersMeta.size() == 0 ) ) {
          setOutputDone(); // signal end to receiver(s)
          return false; // stop immediately, nothing to do here.
        }

        boolean success = doQuery( parametersMeta, parameters );
        if ( !success ) {
          return false;
        }
      } else {
        if ( data.thisrow != null ) { // We can expect more rows

          try {
            data.nextrow = data.db.getRow( data.rs, meta.isLazyConversionActive() );
          } catch ( KettleDatabaseException e ) {
            if ( e.getCause() instanceof SQLException && isStopped() ) {
              //This exception indicates we tried reading a row after the statment for this step was cancelled
              //this is expected and ok so do not pass the exception up
              logDebug( e.getMessage() );
              return false;
            } else {
              throw e;
            }
          }
          if ( data.nextrow != null ) {
            incrementLinesInput();
          }
        }
      }

      if ( data.thisrow == null ) { // Finished reading?

        boolean done = false;
        if ( meta.isExecuteEachInputRow() ) { // Try to get another row from the input stream
          Object[] nextRow = getRowFrom( data.rowSet );
          if ( nextRow == null ) { // Nothing more to get!

            done = true;
          } else {
            // First close the previous query, otherwise we run out of cursors!
            closePreviousQuery();

            boolean success = doQuery( data.rowSet.getRowMeta(), nextRow ); // OK, perform a new query
            if ( !success ) {
              return false;
            }

            if ( data.thisrow != null ) {
              putRow( data.rowMeta, data.thisrow ); // fill the rowset(s). (wait for empty)
              data.thisrow = data.nextrow;

              if ( checkFeedback( getLinesInput() ) ) {
                if ( log.isBasic() ) {
                  logBasic( BaseMessages.getString( PKG, "TableInput.Log.LineNumber", String.valueOf( getLinesInput() ) ) );
                }
              }
            }
          }
        } else {
          done = true;
        }

        if ( done ) {
          setOutputDone(); // signal end to receiver(s)
          return false; // end of data or error.
        }
      } else {
        putRow( data.rowMeta, data.thisrow ); // fill the rowset(s). (wait for empty)
        data.thisrow = data.nextrow;

        if ( checkFeedback( getLinesInput() ) ) {
          if ( log.isBasic() ) {
            logBasic( BaseMessages.getString( PKG, "TableInput.Log.LineNumber", String.valueOf( getLinesInput() ) ) );
          }
        }
      }

      return true;
    } finally {
      dbLock.unlock();
    }
  }

  private void closePreviousQuery() throws KettleDatabaseException {
    if ( data.db != null ) {
      data.db.closeQuery( data.rs );
    }
  }

  private boolean doQuery( RowMetaInterface parametersMeta, Object[] parameters ) throws KettleDatabaseException {
    boolean success = true;

    // Open the query with the optional parameters received from the source steps.
    String sql = null;
    if ( meta.isVariableReplacementActive() ) {
      sql = environmentSubstitute( meta.getSQL() );
    } else {
      sql = meta.getSQL();
    }

    if ( log.isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "TableInput.Log.SqlQuery", sql ) );
    }
    if ( parametersMeta.isEmpty() ) {
      data.rs = data.db.openQuery( sql, null, null, ResultSet.FETCH_FORWARD, meta.isLazyConversionActive() );
    } else {
      data.rs =
        data.db.openQuery( sql, parametersMeta, parameters, ResultSet.FETCH_FORWARD, meta
          .isLazyConversionActive() );
    }
    if ( data.rs == null ) {
      logError( BaseMessages.getString( PKG, "TableInput.Log.CanNotOpenQuery", sql ) );
      setErrors( 1 );
      stopAll();
      success = false;
    } else {
      // Keep the metadata
      data.rowMeta = data.db.getReturnRowMeta();

      // Set the origin on the row metadata...
      if ( data.rowMeta != null ) {
        for ( ValueMetaInterface valueMeta : data.rowMeta.getValueMetaList() ) {
          valueMeta.setOrigin( getStepname() );
        }
      }

      // Get the first row...
      data.thisrow = data.db.getRow( data.rs );
      if ( data.thisrow != null ) {
        incrementLinesInput();
        data.nextrow = data.db.getRow( data.rs );
        if ( data.nextrow != null ) {
          incrementLinesInput();
        }
      }
    }
    return success;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    dbLock.lock();
    try {
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "TableInput.Log.FinishedReadingQuery" ) );
      }
      try {
        closePreviousQuery();
      } catch ( KettleException e ) {
        logError( BaseMessages.getString( PKG, "TableInput.Log.ErrorClosingQuery", e.toString() ) );
        setErrors( 1 );
        stopAll();
      } finally {
        super.dispose( smi, sdi );
      }
    } finally {
      dbLock.unlock();
    }
  }

  /** Stop the running query */
  public void stopRunning( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    if ( this.isStopped() || sdi.isDisposed() ) {
      return;
    }

    dbLock.lock();
    try {
      meta = (TableInputMeta) smi;
      data = (TableInputData) sdi;

      setStopped( true );

      if ( data.db != null  && data.db.getConnection() != null && !data.isCanceled ) {
        data.db.cancelQuery();
        data.isCanceled = true;
      }
    } finally {
      dbLock.unlock();
    }
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    dbLock.lock();
    try {
      meta = (TableInputMeta) smi;
      data = (TableInputData) sdi;

      if ( super.init( smi, sdi ) ) {
        // Verify some basic things first...
        if ( Utils.isEmpty( meta.getSQL() ) ) {
          logError( BaseMessages.getString( PKG, "TableInput.Exception.SQLIsNeeded" ) );
          return false;
        }
        data.infoStream = meta.getStepIOMeta().getInfoStreams().get( 0 );
        data.db.setQueryLimit( Const.toInt( environmentSubstitute( meta.getRowLimit() ), 0 ) );

        if ( meta.getDatabaseMeta().isRequiringTransactionsOnQueries() ) {
          data.db.setCommitSize( 100 ); // needed for PGSQL it seems...
        }
        return true;
      }
      return false;
    } finally {
      dbLock.unlock();
    }
  }

  @Override
  public JSONObject doAction( String fieldName, StepMetaInterface stepMetaInterface, TransMeta transMeta,
                              Trans trans, Map<String, String> queryParamToValues ) {
    JSONObject response = new JSONObject();
    try {
      Method actionMethod = TableInput.class.getDeclaredMethod( fieldName + "Action", Map.class );
      this.setStepMetaInterface( stepMetaInterface );
      response = (JSONObject) actionMethod.invoke( this, queryParamToValues );

    } catch ( NoSuchMethodException | InvocationTargetException | IllegalAccessException e ) {
      log.logError( e.getMessage() );
      response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_METHOD_NOT_RESPONSE );
    }
    return response;
  }


  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  private JSONObject getColumnsAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    response.put( StepInterface.ACTION_STATUS, StepInterface.FAILURE_RESPONSE );
    String sql =
      getColumnsSQL( queryParams.get( "connection" ), queryParams.get( "schema" ), queryParams.get( "table" ) );
    response.put( "sql", sql );
    response.put( "actionStatus", StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  private String getColumnsSQL( String connection, String schema, String table ) {
    DatabaseMeta databaseMeta = getTransMeta().findDatabase( connection );
    LoggingObjectInterface loggingObject = new SimpleLoggingObject(
      "Table Output Step", LoggingObjectType.STEP, null );
    Database db = new Database( loggingObject, databaseMeta );
    String sql =
      "SELECT *"
        + Const.CR + "FROM "
        + databaseMeta.getQuotedSchemaTableCombination( schema, table ) + Const.CR;
    try {
      db.connect();

      RowMetaInterface fields = db.getQueryFields( sql, false );
      if ( fields != null ) {
        sql = "SELECT" + Const.CR;
        for ( int i = 0; i < fields.size(); i++ ) {
          ValueMetaInterface field = fields.getValueMeta( i );
          if ( i == 0 ) {
            sql += "  ";
          } else {
            sql += ", ";
          }
          sql += databaseMeta.quoteField( field.getName() ) + Const.CR;
        }
        sql +=
          "FROM "
            + databaseMeta.getQuotedSchemaTableCombination( schema, table )
            + Const.CR;
      }
    } catch ( KettleDatabaseException e ) {
      throw new RuntimeException( e );
    }
    return sql;
  }


  @Override
  protected Class<?> getPKG() {
    return PKG;
  }

  public boolean isWaitingForData() {
    return true;
  }

}
