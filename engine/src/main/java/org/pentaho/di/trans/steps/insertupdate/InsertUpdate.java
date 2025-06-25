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


package org.pentaho.di.trans.steps.insertupdate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.apache.commons.lang.ArrayUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.BaseDatabaseStep;

/**
 * Performs a lookup in a database table. If the key doesn't exist it inserts values into the table, otherwise it
 * performs an update of the changed values. If nothing changed, do nothing.
 *
 * @author Matt
 * @since 26-apr-2003
 */
public class InsertUpdate extends BaseDatabaseStep implements StepInterface {
  private static Class<?> PKG = InsertUpdateMeta.class; // for i18n purposes, needed by Translator2!!

  private InsertUpdateMeta meta;
  private InsertUpdateData data;

  public InsertUpdate( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  protected synchronized void lookupValues( RowMetaInterface rowMeta, Object[] row ) throws KettleException {
    // OK, now do the lookup.
    // We need the lookupvalues for that.
    Object[] lookupRow = new Object[ data.lookupParameterRowMeta.size() ];
    int lookupIndex = 0;

    for ( int i = 0; i < data.keynrs.length; i++ ) {
      if ( data.keynrs[ i ] >= 0 ) {
        lookupRow[ lookupIndex ] = row[ data.keynrs[ i ] ];
        lookupIndex++;
      }
      if ( data.keynrs2[ i ] >= 0 ) {
        lookupRow[ lookupIndex ] = row[ data.keynrs2[ i ] ];
        lookupIndex++;
      }
    }

    data.db.setValues( data.lookupParameterRowMeta, lookupRow, data.prepStatementLookup );

    if ( log.isDebug() ) {
      logDebug( BaseMessages.getString( PKG, "InsertUpdate.Log.ValuesSetForLookup" )
        + data.lookupParameterRowMeta.getString( lookupRow ) );
    }
    Object[] add = data.db.getLookup( data.prepStatementLookup );
    incrementLinesInput();

    if ( add == null ) {
      /*
       * nothing was found:
       *
       * INSERT ROW
       */
      if ( log.isRowLevel() ) {
        logRowlevel( BaseMessages.getString( PKG, "InsertUpdate.InsertRow" ) + rowMeta.getString( row ) );
      }

      // The values to insert are those in the update section (all fields should be specified)
      // For the others, we have no definite mapping!
      //
      Object[] insertRow = new Object[ data.valuenrs.length ];
      for ( int i = 0; i < data.valuenrs.length; i++ ) {
        insertRow[ i ] = row[ data.valuenrs[ i ] ];
      }

      // Set the values on the prepared statement...
      data.db.setValuesInsert( data.insertRowMeta, insertRow );

      // Insert the row
      data.db.insertRow();

      incrementLinesOutput();
    } else {
      if ( !meta.isUpdateBypassed() ) {
        if ( log.isRowLevel() ) {
          logRowlevel( BaseMessages.getString( PKG, "InsertUpdate.Log.FoundRowForUpdate" )
            + rowMeta.getString( row ) );
        }

        /*
         * Row was found:
         *
         * UPDATE row or do nothing?
         */
        boolean update = false;
        for ( int i = 0; i < data.valuenrs.length; i++ ) {
          if ( meta.getUpdateFields()[ i ].getUpdate().booleanValue() ) {
            ValueMetaInterface valueMeta = rowMeta.getValueMeta( data.valuenrs[ i ] );
            ValueMetaInterface retMeta = data.db.getReturnRowMeta().getValueMeta( i );

            Object rowvalue = row[ data.valuenrs[ i ] ];
            Object retvalue = add[ i ];

            if ( retMeta.compare( retvalue, valueMeta, rowvalue ) != 0 ) {
              update = true;
            }
          }
        }
        if ( update ) {
          // Create the update row...
          Object[] updateRow = new Object[ data.updateParameterRowMeta.size() ];
          int j = 0;
          for ( int i = 0; i < data.valuenrs.length; i++ ) {
            if ( meta.getUpdateFields()[ i ].getUpdate().booleanValue() ) {
              updateRow[ j ] = row[ data.valuenrs[ i ] ]; // the setters
              j++;
            }
          }
          // add the where clause parameters, they are exactly the same for lookup and update
          for ( int i = 0; i < lookupRow.length; i++ ) {
            updateRow[ j + i ] = lookupRow[ i ];
          }

          if ( log.isRowLevel() ) {
            logRowlevel( BaseMessages.getString( PKG, "InsertUpdate.Log.UpdateRow" )
              + data.lookupParameterRowMeta.getString( lookupRow ) );
          }
          data.db.setValues( data.updateParameterRowMeta, updateRow, data.prepStatementUpdate );
          data.db.insertRow( data.prepStatementUpdate );
          incrementLinesUpdated();
        } else {
          incrementLinesSkipped();
        }
      } else {
        if ( log.isRowLevel() ) {
          logRowlevel( BaseMessages.getString( PKG, "InsertUpdate.Log.UpdateBypassed" ) + rowMeta.getString( row ) );
        }
        incrementLinesSkipped();
      }
    }
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (InsertUpdateMeta) smi;
    data = (InsertUpdateData) sdi;

    boolean sendToErrorRow = false;
    String errorMessage = null;

    Object[] r = getRow(); // Get row from input rowset & set row busy!
    if ( r == null ) {
      // no more input to be expected...

      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;

      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields( getTransMeta().getBowl(), data.outputRowMeta, getStepname(), null, null, this, repository,
        metaStore );

      data.schemaTable =
        meta.getDatabaseMeta().getQuotedSchemaTableCombination(
          environmentSubstitute( meta.getSchemaName() ), environmentSubstitute( meta.getTableName() ) );

      // lookup the values!
      if ( log.isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "InsertUpdate.Log.CheckingRow" ) + getInputRowMeta().getString( r ) );
      }

      ArrayList<Integer> keynrs = new ArrayList<Integer>( meta.getKeyFields().length );
      ArrayList<Integer> keynrs2 = new ArrayList<Integer>( meta.getKeyFields().length );

      for ( int i = 0; i < meta.getKeyFields().length; i++ ) {
        int keynr = getInputRowMeta().indexOfValue( meta.getKeyFields()[ i ].getKeyStream() );

        if ( keynr < 0 && // couldn't find field!
          !"IS NULL".equalsIgnoreCase( meta.getKeyFields()[ i ].getKeyCondition() ) && // No field needed!
          !"IS NOT NULL".equalsIgnoreCase( meta.getKeyFields()[ i ].getKeyCondition() ) // No field needed!
        ) {
          throw new KettleStepException( BaseMessages.getString( PKG, "InsertUpdate.Exception.FieldRequired", meta
            .getKeyFields()[ i ].getKeyStream() ) );
        }
        keynrs.add( keynr );

        // this operator needs two bindings
        if ( "= ~NULL".equalsIgnoreCase( meta.getKeyFields()[ i ].getKeyCondition() ) ) {
          keynrs.add( keynr );
          keynrs2.add( -1 );
        }

        int keynr2 = getInputRowMeta().indexOfValue( meta.getKeyFields()[ i ].getKeyStream2() );
        if ( keynr2 < 0 && // couldn't find field!
          "BETWEEN".equalsIgnoreCase( meta.getKeyFields()[ i ].getKeyCondition() ) // 2 fields needed!
        ) {
          throw new KettleStepException( BaseMessages.getString( PKG, "InsertUpdate.Exception.FieldRequired", meta
            .getKeyFields()[ i ].getKeyStream2() ) );
        }
        keynrs2.add( keynr2 );

        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "InsertUpdate.Log.FieldHasDataNumbers",
            meta.getKeyFields()[ i ].getKeyStream() )
            + "" + keynrs.get( keynrs.size() - 1 ) );
        }
      }

      data.keynrs = ArrayUtils.toPrimitive( keynrs.toArray( new Integer[ 0 ] ) );
      data.keynrs2 = ArrayUtils.toPrimitive( keynrs2.toArray( new Integer[ 0 ] ) );

      // Cache the position of the compare fields in Row row
      //
      data.valuenrs = new int[ meta.getUpdateFields().length ];
      for ( int i = 0; i < meta.getUpdateFields().length; i++ ) {
        data.valuenrs[ i ] = getInputRowMeta().indexOfValue( meta.getUpdateFields()[ i ].getUpdateStream() );
        if ( data.valuenrs[ i ] < 0 ) {
          // couldn't find field!

          throw new KettleStepException( BaseMessages.getString( PKG, "InsertUpdate.Exception.FieldRequired", meta
            .getUpdateFields()[ i ].getUpdateStream() ) );
        }
        if ( log.isDebug() ) {
          logDebug( BaseMessages
            .getString( PKG, "InsertUpdate.Log.FieldHasDataNumbers", meta.getUpdateFields()[ i ].getUpdateStream() )
            + data.valuenrs[ i ] );
        }
      }

      setLookup( getInputRowMeta() );

      data.insertRowMeta = new RowMeta();

      // Insert the update fields: just names. Type doesn't matter!
      for ( int i = 0; i < meta.getUpdateFields().length; i++ ) {
        ValueMetaInterface insValue =
          data.insertRowMeta.searchValueMeta( meta.getUpdateFields()[ i ].getUpdateLookup() );
        if ( insValue == null ) {
          // Don't add twice!

          // we already checked that this value exists so it's probably safe to ignore lookup failure...
          ValueMetaInterface insertValue =
            getInputRowMeta().searchValueMeta( meta.getUpdateFields()[ i ].getUpdateStream() ).clone();
          insertValue.setName( meta.getUpdateFields()[ i ].getUpdateLookup() );
          data.insertRowMeta.addValueMeta( insertValue );
        } else {
          throw new KettleStepException( "The same column can't be inserted into the target row twice: "
            + insValue.getName() ); // TODO i18n
        }
      }
      data.db.prepareInsert(
        data.insertRowMeta, environmentSubstitute( meta.getSchemaName() ), environmentSubstitute( meta
          .getTableName() ) );

      if ( !meta.isUpdateBypassed() ) {
        List<String> updateColumns = new ArrayList<String>();
        for ( int i = 0; i < meta.getUpdateFields().length; i++ ) {
          if ( meta.getUpdateFields()[ i ].getUpdate().booleanValue() ) {
            updateColumns.add( meta.getUpdateFields()[ i ].getUpdateLookup() );
          }
        }
        prepareUpdate( getInputRowMeta() );
      }
    }

    try {
      lookupValues( getInputRowMeta(), r ); // add new values to the row in rowset[0].
      putRow( data.outputRowMeta, r ); // Nothing changed to the input, return the same row, pass a "cloned" metadata
      // row.

      if ( checkFeedback( getLinesRead() ) ) {
        if ( log.isBasic() ) {
          logBasic( BaseMessages.getString( PKG, "InsertUpdate.Log.LineNumber" ) + getLinesRead() );
        }
      }
    } catch ( KettleException e ) {
      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        logError( BaseMessages.getString( PKG, "InsertUpdate.Log.ErrorInStep" ), e );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }

      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), r, 1, errorMessage, null, "ISU001" );
      }
    }

    return true;
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  public JSONObject getSQLAction( Map<String, String> queryParamToValues ) {
    JSONObject response = new JSONObject();
    try {
      RowMetaInterface prev = getTransMeta().getPrevStepFields( getStepname() );
      InsertUpdateMeta insertUpdateMeta = ( InsertUpdateMeta ) getStepMetaInterface();
      SQLStatement sql = insertUpdateMeta.getSQLStatements(
              getTransMeta(), getStepMeta(), prev, repository, metaStore );
      if ( !sql.hasError() ) {
        response.put( "sql", sql.getSQL() );
      } else {
        response.put( "error", sql.getError() );
      }

    } catch ( Exception e ) {
      response.put( "error", "Error generating SQL: " + e.getMessage() );
    }
    return response;
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  public JSONObject getComparatorsAction( Map<String, String> queryParamToValues ) {
    JSONObject response = new JSONObject();
    JSONArray comparators = new JSONArray();
    String[] comparatorValues = {
            "=", "= ~NULL", "<>", "<", "<=", ">", ">=", "LIKE", "BETWEEN", "IS NULL", "IS NOT NULL"
    };

    for ( String comparator : comparatorValues ) {
      JSONObject comparatorJson = new JSONObject();
      comparatorJson.put( "id", comparator );
      comparatorJson.put( "name", comparator );
      comparators.add( comparatorJson );
    }
    response.put( "comparators", comparators );
    return response;
  }

  public void setLookup( RowMetaInterface rowMeta ) throws KettleDatabaseException {
    data.lookupParameterRowMeta = new RowMeta();
    data.lookupReturnRowMeta = new RowMeta();

    DatabaseMeta databaseMeta = meta.getDatabaseMeta();

    String sql = "SELECT ";

    for ( int i = 0; i < meta.getUpdateFields().length; i++ ) {
      if ( i != 0 ) {
        sql += ", ";
      }
      sql += databaseMeta.quoteField( meta.getUpdateFields()[ i ].getUpdateLookup() );
      data.lookupReturnRowMeta.addValueMeta(
        rowMeta.searchValueMeta( meta.getUpdateFields()[ i ].getUpdateStream() ).clone() );
    }

    sql += " FROM " + data.schemaTable + " WHERE ";

    for ( int i = 0; i < meta.getKeyFields().length; i++ ) {
      if ( i != 0 ) {
        sql += " AND ";
      }

      sql += " ( ( ";

      sql += databaseMeta.quoteField( meta.getKeyFields()[ i ].getKeyLookup() );
      if ( "BETWEEN".equalsIgnoreCase( meta.getKeyFields()[ i ].getKeyCondition() ) ) {
        sql += " BETWEEN ? AND ? ";
        data.lookupParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyFields()[ i ].getKeyStream() ) );
        data.lookupParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyFields()[ i ].getKeyStream2() ) );
      } else {
        if ( "IS NULL".equalsIgnoreCase( meta.getKeyFields()[ i ].getKeyCondition() )
          || "IS NOT NULL".equalsIgnoreCase( meta.getKeyFields()[ i ].getKeyCondition() ) ) {
          sql += " " + meta.getKeyFields()[ i ].getKeyCondition() + " ";
        } else if ( "= ~NULL".equalsIgnoreCase( meta.getKeyFields()[ i ].getKeyCondition() ) ) {

          sql += " IS NULL AND ";

          if ( databaseMeta.requiresCastToVariousForIsNull() ) {
            sql += " CAST(? AS VARCHAR(256)) IS NULL ";
          } else {
            sql += " ? IS NULL ";
          }
          // null check
          data.lookupParameterRowMeta.addValueMeta(
            rowMeta.searchValueMeta( meta.getKeyFields()[ i ].getKeyStream() ) );
          sql += " ) OR ( " + databaseMeta.quoteField( meta.getKeyFields()[ i ].getKeyLookup() ) + " = ? ";
          // equality check, cloning so auto-rename because of adding same fieldname does not cause problems
          data.lookupParameterRowMeta.addValueMeta(
            rowMeta.searchValueMeta( meta.getKeyFields()[ i ].getKeyStream() ).clone() );

        } else {
          sql += " " + meta.getKeyFields()[ i ].getKeyCondition() + " ? ";
          data.lookupParameterRowMeta.addValueMeta(
            rowMeta.searchValueMeta( meta.getKeyFields()[ i ].getKeyStream() ) );
        }
      }
      sql += " ) ) ";
    }

    try {
      if ( log.isDetailed() ) {
        logDetailed( "Setting preparedStatement to [" + sql + "]" );
      }
      data.prepStatementLookup = data.db.getConnection().prepareStatement( databaseMeta.stripCR( sql ) );
    } catch ( SQLException ex ) {
      throw new KettleDatabaseException( "Unable to prepare statement for SQL statement [" + sql + "]", ex );
    }
  }

  // Lookup certain fields in a table
  public void prepareUpdate( RowMetaInterface rowMeta ) throws KettleDatabaseException {
    DatabaseMeta databaseMeta = meta.getDatabaseMeta();
    data.updateParameterRowMeta = new RowMeta();

    String sql = "UPDATE " + data.schemaTable + Const.CR;
    sql += "SET ";

    boolean comma = false;

    for ( int i = 0; i < meta.getUpdateFields().length; i++ ) {
      if ( meta.getUpdateFields()[ i ].getUpdate().booleanValue() ) {
        if ( comma ) {
          sql += ",   ";
        } else {
          comma = true;
        }

        sql += databaseMeta.quoteField( meta.getUpdateFields()[ i ].getUpdateLookup() );
        sql += " = ?" + Const.CR;
        data.updateParameterRowMeta.addValueMeta(
          rowMeta.searchValueMeta( meta.getUpdateFields()[ i ].getUpdateStream() ).clone() );
      }
    }

    sql += "WHERE ";

    for ( int i = 0; i < meta.getKeyFields().length; i++ ) {
      if ( i != 0 ) {
        sql += "AND   ";
      }
      sql += " ( ( ";
      sql += databaseMeta.quoteField( meta.getKeyFields()[ i ].getKeyLookup() );
      if ( "BETWEEN".equalsIgnoreCase( meta.getKeyFields()[ i ].getKeyCondition() ) ) {
        sql += " BETWEEN ? AND ? ";
        data.updateParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyFields()[ i ].getKeyStream() ) );
        data.updateParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyFields()[ i ].getKeyStream2() ) );
      } else if ( "IS NULL".equalsIgnoreCase( meta.getKeyFields()[ i ].getKeyCondition() )
        || "IS NOT NULL".equalsIgnoreCase( meta.getKeyFields()[ i ].getKeyCondition() ) ) {
        sql += " " + meta.getKeyFields()[ i ].getKeyCondition() + " ";
      } else if ( "= ~NULL".equalsIgnoreCase( meta.getKeyFields()[ i ].getKeyCondition() ) ) {

        sql += " IS NULL AND ";

        if ( databaseMeta.requiresCastToVariousForIsNull() ) {
          sql += "CAST(? AS VARCHAR(256)) IS NULL";
        } else {
          sql += "? IS NULL";
        }
        // null check
        data.updateParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyFields()[ i ].getKeyStream() ) );
        sql += " ) OR ( " + databaseMeta.quoteField( meta.getKeyFields()[ i ].getKeyLookup() ) + " = ?";
        // equality check, cloning so auto-rename because of adding same fieldname does not cause problems
        data.updateParameterRowMeta.addValueMeta(
          rowMeta.searchValueMeta( meta.getKeyFields()[ i ].getKeyStream() ).clone() );

      } else {
        sql += " " + meta.getKeyFields()[ i ].getKeyCondition() + " ? ";
        data.updateParameterRowMeta.addValueMeta(
          rowMeta.searchValueMeta( meta.getKeyFields()[ i ].getKeyStream() ).clone() );
      }
      sql += " ) ) ";
    }

    try {
      if ( log.isDetailed() ) {
        logDetailed( "Setting update preparedStatement to [" + sql + "]" );
      }
      data.prepStatementUpdate = data.db.getConnection().prepareStatement( databaseMeta.stripCR( sql ) );
    } catch ( SQLException ex ) {
      throw new KettleDatabaseException( "Unable to prepare statement for SQL statement [" + sql + "]", ex );
    }
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (InsertUpdateMeta) smi;
    data = (InsertUpdateData) sdi;

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
    meta = (InsertUpdateMeta) smi;
    data = (InsertUpdateData) sdi;

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
        data.db.closeInsert();
      } catch ( KettleDatabaseException e ) {
        logError( BaseMessages.getString( PKG, "InsertUpdate.Log.UnableToCommitConnection" ) + e.toString() );
        setErrors( 1 );
      }
    }
    super.dispose( smi, sdi );
  }

}
