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

package org.pentaho.di.trans.steps.insertupdate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
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
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Performs a lookup in a database table. If the key doesn't exist it inserts values into the table, otherwise it
 * performs an update of the changed values. If nothing changed, do nothing.
 *
 * @author Matt
 * @since 26-apr-2003
 */
public class InsertUpdate extends BaseStep implements StepInterface {
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
    Object[] lookupRow = new Object[data.lookupParameterRowMeta.size()];
    int lookupIndex = 0;

    for ( int i = 0; i < data.keynrs.length; i++ ) {
      if ( data.keynrs[i] >= 0 ) {
        lookupRow[lookupIndex] = row[data.keynrs[i]];
        lookupIndex++;
      }
      if ( data.keynrs2[i] >= 0 ) {
        lookupRow[lookupIndex] = row[data.keynrs2[i]];
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
      Object[] insertRow = new Object[data.valuenrs.length];
      for ( int i = 0; i < data.valuenrs.length; i++ ) {
        insertRow[i] = row[data.valuenrs[i]];
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
          if ( meta.getUpdate()[i].booleanValue() ) {
            ValueMetaInterface valueMeta = rowMeta.getValueMeta( data.valuenrs[i] );
            ValueMetaInterface retMeta = data.db.getReturnRowMeta().getValueMeta( i );

            Object rowvalue = row[data.valuenrs[i]];
            Object retvalue = add[i];

            if ( retMeta.compare( retvalue, valueMeta, rowvalue ) != 0 ) {
              update = true;
            }
          }
        }
        if ( update ) {
          // Create the update row...
          Object[] updateRow = new Object[data.updateParameterRowMeta.size()];
          int j = 0;
          for ( int i = 0; i < data.valuenrs.length; i++ ) {
            if ( meta.getUpdate()[i].booleanValue() ) {
              updateRow[j] = row[data.valuenrs[i]]; // the setters
              j++;
            }
          }
          // add the where clause parameters, they are exactly the same for lookup and update
          for ( int i = 0; i < lookupRow.length; i++ ) {
            updateRow[j + i] = lookupRow[i];
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
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      data.schemaTable =
        meta.getDatabaseMeta().getQuotedSchemaTableCombination(
          environmentSubstitute( meta.getSchemaName() ), environmentSubstitute( meta.getTableName() ) );

      // lookup the values!
      if ( log.isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "InsertUpdate.Log.CheckingRow" ) + getInputRowMeta().getString( r ) );
      }

      ArrayList<Integer> keynrs = new ArrayList<Integer>( meta.getKeyStream().length );
      ArrayList<Integer> keynrs2 = new ArrayList<Integer>( meta.getKeyStream().length );

      for ( int i = 0; i < meta.getKeyStream().length; i++ ) {
        int keynr = getInputRowMeta().indexOfValue( meta.getKeyStream()[i] );

        if ( keynr < 0 && // couldn't find field!
          !"IS NULL".equalsIgnoreCase( meta.getKeyCondition()[i] ) && // No field needed!
          !"IS NOT NULL".equalsIgnoreCase( meta.getKeyCondition()[i] ) // No field needed!
        ) {
          throw new KettleStepException( BaseMessages.getString( PKG, "InsertUpdate.Exception.FieldRequired", meta
            .getKeyStream()[i] ) );
        }
        keynrs.add( keynr );

        // this operator needs two bindings
        if ( "= ~NULL".equalsIgnoreCase( meta.getKeyCondition()[i] ) ) {
          keynrs.add( keynr );
          keynrs2.add( -1 );
        }

        int keynr2 = getInputRowMeta().indexOfValue( meta.getKeyStream2()[i] );
        if ( keynr2 < 0 && // couldn't find field!
          "BETWEEN".equalsIgnoreCase( meta.getKeyCondition()[i] ) // 2 fields needed!
        ) {
          throw new KettleStepException( BaseMessages.getString( PKG, "InsertUpdate.Exception.FieldRequired", meta
            .getKeyStream2()[i] ) );
        }
        keynrs2.add( keynr2 );

        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "InsertUpdate.Log.FieldHasDataNumbers", meta.getKeyStream()[i] )
            + "" + keynrs.get( keynrs.size() - 1 ) );
        }
      }

      data.keynrs = ArrayUtils.toPrimitive( keynrs.toArray( new Integer[0] ) );
      data.keynrs2 = ArrayUtils.toPrimitive( keynrs2.toArray( new Integer[0] ) );

      // Cache the position of the compare fields in Row row
      //
      data.valuenrs = new int[meta.getUpdateLookup().length];
      for ( int i = 0; i < meta.getUpdateLookup().length; i++ ) {
        data.valuenrs[i] = getInputRowMeta().indexOfValue( meta.getUpdateStream()[i] );
        if ( data.valuenrs[i] < 0 ) {
          // couldn't find field!

          throw new KettleStepException( BaseMessages.getString( PKG, "InsertUpdate.Exception.FieldRequired", meta
            .getUpdateStream()[i] ) );
        }
        if ( log.isDebug() ) {
          logDebug( BaseMessages
            .getString( PKG, "InsertUpdate.Log.FieldHasDataNumbers", meta.getUpdateStream()[i] )
            + data.valuenrs[i] );
        }
      }

      setLookup( getInputRowMeta() );

      data.insertRowMeta = new RowMeta();

      // Insert the update fields: just names. Type doesn't matter!
      for ( int i = 0; i < meta.getUpdateLookup().length; i++ ) {
        ValueMetaInterface insValue = data.insertRowMeta.searchValueMeta( meta.getUpdateLookup()[i] );
        if ( insValue == null ) {
          // Don't add twice!

          // we already checked that this value exists so it's probably safe to ignore lookup failure...
          ValueMetaInterface insertValue = getInputRowMeta().searchValueMeta( meta.getUpdateStream()[i] ).clone();
          insertValue.setName( meta.getUpdateLookup()[i] );
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
        for ( int i = 0; i < meta.getUpdate().length; i++ ) {
          if ( meta.getUpdate()[i].booleanValue() ) {
            updateColumns.add( meta.getUpdateLookup()[i] );
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

  public void setLookup( RowMetaInterface rowMeta ) throws KettleDatabaseException {
    data.lookupParameterRowMeta = new RowMeta();
    data.lookupReturnRowMeta = new RowMeta();

    DatabaseMeta databaseMeta = meta.getDatabaseMeta();

    String sql = "SELECT ";

    for ( int i = 0; i < meta.getUpdateLookup().length; i++ ) {
      if ( i != 0 ) {
        sql += ", ";
      }
      sql += databaseMeta.quoteField( meta.getUpdateLookup()[i] );
      data.lookupReturnRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getUpdateStream()[i] ).clone() );
    }

    sql += " FROM " + data.schemaTable + " WHERE ";

    for ( int i = 0; i < meta.getKeyLookup().length; i++ ) {
      if ( i != 0 ) {
        sql += " AND ";
      }

      sql += " ( ( ";

      sql += databaseMeta.quoteField( meta.getKeyLookup()[i] );
      if ( "BETWEEN".equalsIgnoreCase( meta.getKeyCondition()[i] ) ) {
        sql += " BETWEEN ? AND ? ";
        data.lookupParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyStream()[i] ) );
        data.lookupParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyStream2()[i] ) );
      } else {
        if ( "IS NULL".equalsIgnoreCase( meta.getKeyCondition()[i] )
          || "IS NOT NULL".equalsIgnoreCase( meta.getKeyCondition()[i] ) ) {
          sql += " " + meta.getKeyCondition()[i] + " ";
        } else if ( "= ~NULL".equalsIgnoreCase( meta.getKeyCondition()[i] ) ) {

          sql += " IS NULL AND ";

          if ( databaseMeta.requiresCastToVariousForIsNull() ) {
            sql += " CAST(? AS VARCHAR(256)) IS NULL ";
          } else {
            sql += " ? IS NULL ";
          }
          // null check
          data.lookupParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyStream()[i] ) );
          sql += " ) OR ( " + databaseMeta.quoteField( meta.getKeyLookup()[i] ) + " = ? ";
          // equality check, cloning so auto-rename because of adding same fieldname does not cause problems
          data.lookupParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyStream()[i] ).clone() );

        } else {
          sql += " " + meta.getKeyCondition()[i] + " ? ";
          data.lookupParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyStream()[i] ) );
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

    for ( int i = 0; i < meta.getUpdateLookup().length; i++ ) {
      if ( meta.getUpdate()[i].booleanValue() ) {
        if ( comma ) {
          sql += ",   ";
        } else {
          comma = true;
        }

        sql += databaseMeta.quoteField( meta.getUpdateLookup()[i] );
        sql += " = ?" + Const.CR;
        data.updateParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getUpdateStream()[i] ).clone() );
      }
    }

    sql += "WHERE ";

    for ( int i = 0; i < meta.getKeyLookup().length; i++ ) {
      if ( i != 0 ) {
        sql += "AND   ";
      }
      sql += " ( ( ";
      sql += databaseMeta.quoteField( meta.getKeyLookup()[i] );
      if ( "BETWEEN".equalsIgnoreCase( meta.getKeyCondition()[i] ) ) {
        sql += " BETWEEN ? AND ? ";
        data.updateParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyStream()[i] ) );
        data.updateParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyStream2()[i] ) );
      } else if ( "IS NULL".equalsIgnoreCase( meta.getKeyCondition()[i] )
        || "IS NOT NULL".equalsIgnoreCase( meta.getKeyCondition()[i] ) ) {
        sql += " " + meta.getKeyCondition()[i] + " ";
      } else if ( "= ~NULL".equalsIgnoreCase( meta.getKeyCondition()[i] ) ) {

        sql += " IS NULL AND ";

        if ( databaseMeta.requiresCastToVariousForIsNull() ) {
          sql += "CAST(? AS VARCHAR(256)) IS NULL";
        } else {
          sql += "? IS NULL";
        }
        // null check
        data.updateParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyStream()[i] ) );
        sql += " ) OR ( " + databaseMeta.quoteField( meta.getKeyLookup()[i] ) + " = ?";
        // equality check, cloning so auto-rename because of adding same fieldname does not cause problems
        data.updateParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyStream()[i] ).clone() );

      } else {
        sql += " " + meta.getKeyCondition()[i] + " ? ";
        data.updateParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyStream()[i] ).clone() );
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
      try {
        if ( meta.getDatabaseMeta() == null ) {
          logError( BaseMessages.getString( PKG, "InsertUpdate.Init.ConnectionMissing", getStepname() ) );
          return false;
        }
        data.db = new Database( this, meta.getDatabaseMeta() );
        data.db.shareVariablesWith( this );
        if ( getTransMeta().isUsingUniqueConnections() ) {
          synchronized ( getTrans() ) {
            data.db.connect( getTrans().getTransactionId(), getPartitionID() );
          }
        } else {
          data.db.connect( getPartitionID() );
        }
        data.db.setCommit(  meta.getCommitSize( this ) );

        return true;
      } catch ( KettleException ke ) {
        logError( BaseMessages.getString( PKG, "InsertUpdate.Log.ErrorOccurredDuringStepInitialize" )
          + ke.getMessage() );
      }
    }
    return false;
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
      } finally {
        data.db.disconnect();
      }
    }
    super.dispose( smi, sdi );
  }

}
