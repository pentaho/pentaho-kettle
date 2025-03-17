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


package org.pentaho.di.trans.steps.delete;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseDatabaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Delete data in a database table.
 *
 *
 * @author Tom
 * @since 28-March-2006
 */
public class Delete extends BaseDatabaseStep implements StepInterface {
  private static Class<?> PKG = DeleteMeta.class; // for i18n purposes, needed by Translator2!!

  private DeleteMeta meta;
  private DeleteData data;

  public Delete( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  private synchronized void deleteValues( RowMetaInterface rowMeta, Object[] row ) throws KettleException {
    // OK, now do the lookup.
    // We need the lookupvalues for that.
    Object[] deleteRow = new Object[data.deleteParameterRowMeta.size()];
    int deleteIndex = 0;

    for ( int i = 0; i < meta.getKeyFields().length; i++ ) {
      if ( data.keynrs[i] >= 0 ) {
        deleteRow[deleteIndex] = row[data.keynrs[i]];
        deleteIndex++;
      }
      if ( data.keynrs2[i] >= 0 ) {
        deleteRow[deleteIndex] = row[data.keynrs2[i]];
        deleteIndex++;
      }
    }

    data.db.setValues( data.deleteParameterRowMeta, deleteRow, data.prepStatementDelete );

    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "Delete.Log.SetValuesForDelete", data.deleteParameterRowMeta
        .getString( deleteRow ), rowMeta.getString( row ) ) );
    }

    data.db.insertRow( data.prepStatementDelete );
    incrementLinesUpdated();
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (DeleteMeta) smi;
    data = (DeleteData) sdi;

    boolean sendToErrorRow = false;
    String errorMessage = null;

    Object[] r = getRow(); // Get row from input rowset & set row busy!
    if ( r == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;

      // What's the output Row format?
      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      data.schemaTable =
              meta.getDatabaseMeta().getQuotedSchemaTableCombination(
                      environmentSubstitute( meta.getSchemaName() ), environmentSubstitute( meta.getTableName() ) );

      // lookup the values!
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "Delete.Log.CheckingRow" ) + getInputRowMeta().getString( r ) );
      }

      // TODO: maybe handle if the fields in lookupFields are null
      int len = meta.getKeyFields().length;
      data.keynrs = new int[len];
      data.keynrs2 = new int[len];
      for ( int i = 0; i < len; i++ ) {
        data.keynrs[i] = getInputRowMeta().indexOfValue( meta.getKeyFields()[i].getKeyStream() );
        if ( data.keynrs[i] < 0 && // couldn't find field!
                !"IS NULL".equalsIgnoreCase( meta.getKeyFields()[i].getKeyCondition() ) && // No field needed!
                !"IS NOT NULL".equalsIgnoreCase( meta.getKeyFields()[i].getKeyCondition() ) // No field needed!
        ) {
          throw new KettleStepException( BaseMessages.getString( PKG, "Delete.Exception.FieldRequired",
                  meta.getKeyFields()[i].getKeyStream() ) );
        }

        data.keynrs2[i] = ( meta.getKeyFields()[i].getKeyStream2() != null
                && meta.getKeyFields()[i].getKeyStream2().length() > 0 )
                ? getInputRowMeta().indexOfValue( meta.getKeyFields()[i].getKeyStream2() ) : -1;
        if ( data.keynrs2[i] < 0 && // couldn't find field!
                "BETWEEN".equalsIgnoreCase( meta.getKeyFields()[i].getKeyCondition() ) // 2 fields needed!
        ) {
          throw new KettleStepException( BaseMessages.getString( PKG, "Delete.Exception.FieldRequired",
                  meta.getKeyFields()[i].getKeyStream2() ) );
        }

        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "Delete.Log.FieldInfo",
                  meta.getKeyFields()[i].getKeyStream() ) + data.keynrs[i] );
        }
      }

      prepareDelete( getInputRowMeta() );
    }

    try {
      deleteValues( getInputRowMeta(), r ); // add new values to the row in rowset[0].
      putRow( data.outputRowMeta, r ); // output the same rows of data, but with a copy of the metadata

      if ( checkFeedback( getLinesRead() ) ) {
        if ( log.isBasic() ) {
          logBasic( BaseMessages.getString( PKG, "Delete.Log.LineNumber" ) + getLinesRead() );
        }
      }
    } catch ( KettleException e ) {

      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {

        logError( BaseMessages.getString( PKG, "Delete.Log.ErrorInStep" ) + e.getMessage() );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }

      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), r, 1, errorMessage, null, "DEL001" );
      }
    }

    return true;
  }

  // Lookup certain fields in a table
  public void prepareDelete( RowMetaInterface rowMeta ) throws KettleDatabaseException {
    DatabaseMeta databaseMeta = meta.getDatabaseMeta();
    data.deleteParameterRowMeta = new RowMeta();

    String sql = "DELETE FROM " + data.schemaTable + Const.CR;

    sql += "WHERE ";

    for ( int i = 0; i < meta.getKeyFields().length; i++ ) {
      if ( i != 0 ) {
        sql += "AND   ";
      }
      sql += databaseMeta.quoteField( meta.getKeyFields()[i].getKeyLookup() );
      if ( "BETWEEN".equalsIgnoreCase( meta.getKeyFields()[i].getKeyCondition() ) ) {
        sql += " BETWEEN ? AND ? ";
        data.deleteParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyFields()[i].getKeyStream() ) );
        data.deleteParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyFields()[i].getKeyStream2() ) );
      } else if ( "IS NULL".equalsIgnoreCase( meta.getKeyFields()[i].getKeyCondition() )
              || "IS NOT NULL".equalsIgnoreCase( meta.getKeyFields()[i].getKeyCondition() ) ) {
        sql += " " + meta.getKeyFields()[i].getKeyCondition() + " ";
      } else {
        sql += " " + meta.getKeyFields()[i].getKeyCondition() + " ? ";
        data.deleteParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyFields()[i].getKeyStream() ) );
      }
    }

    try {
      if ( log.isDetailed() ) {
        logDetailed( "Setting delete preparedStatement to [" + sql + "]" );
      }
      data.prepStatementDelete = data.db.getConnection().prepareStatement( databaseMeta.stripCR( sql ) );
    } catch ( SQLException ex ) {
      throw new KettleDatabaseException( "Unable to prepare statement for SQL statement [" + sql + "]", ex );
    }
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (DeleteMeta) smi;
    data = (DeleteData) sdi;

    if ( super.init( smi, sdi ) ) {
        data.db.setCommitSize( meta.getCommitSize( this ) );
        return true;
    }
    return false;
  }

  @Override
  protected Class<?> getPKG() {
    return PKG;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (DeleteMeta) smi;
    data = (DeleteData) sdi;

    if ( data.db != null ) {
      try {
        if ( !data.db.isAutoCommit() ) {
          if ( getErrors() == 0 ) {
            data.db.commit();
          } else {
            data.db.rollback();
          }
        }
        data.db.closeUpdate();
      } catch ( KettleDatabaseException e ) {
        logError( BaseMessages.getString( PKG, "Delete.Log.UnableToCommitUpdateConnection" )
                + data.db + "] :" + e.toString() );
        setErrors( 1 );
      }
    }
    super.dispose( smi, sdi );
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  private JSONObject getTableFieldAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    String connectionName = getTransMeta().environmentSubstitute( queryParams.get( "connection" ) );
    String schema = getTransMeta().environmentSubstitute( queryParams.get( "schema" ) );
    String table = getTransMeta().environmentSubstitute( queryParams.get( "table" ) );
    String[] columns = getTableFields( connectionName, schema, table );
    JSONArray columnsList = new JSONArray();
    Collections.addAll( columnsList, columns );
    response.put( "columns", columnsList );
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  private String[] getTableFields( String connection, String schema, String table ) {
    DatabaseMeta databaseMeta = getTransMeta().findDatabase( connection );
    LoggingObjectInterface loggingObject = new SimpleLoggingObject(
      "Delete Step", LoggingObjectType.STEP, null );
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

}
