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

package org.pentaho.di.trans.steps.synchronizeaftermerge;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.OracleDatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseBatchException;
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
 * Performs an insert/update/delete depending on the value of a field.
 *
 * @author Samatar
 * @since 13-10-2008
 */
public class SynchronizeAfterMerge extends BaseStep implements StepInterface {

  private static Class<?> PKG = SynchronizeAfterMergeMeta.class; // for i18n purposes, needed by Translator2!!

  private SynchronizeAfterMergeMeta meta;
  private SynchronizeAfterMergeData data;

  public SynchronizeAfterMerge( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  private synchronized void lookupValues( Object[] row ) throws KettleException {

    // get operation for the current
    // do we insert, update or delete ?
    String operation = data.inputRowMeta.getString( row, data.indexOfOperationOrderField );

    boolean rowIsSafe = false;
    boolean sendToErrorRow = false;
    String errorMessage = null;
    int[] updateCounts = null;
    List<Exception> exceptionsList = null;
    boolean batchProblem = false;

    data.lookupFailure = false;
    boolean performInsert = false;
    boolean performUpdate = false;
    boolean performDelete = false;
    boolean lineSkipped = false;

    try {
      if ( operation == null ) {
        throw new KettleException( BaseMessages.getString( PKG, "SynchronizeAfterMerge.Log.OperationFieldEmpty", meta
            .getOperationOrderField() ) );
      }

      if ( meta.istablenameInField() ) {
        // get dynamic table name
        data.realTableName = data.inputRowMeta.getString( row, data.indexOfTableNameField );
        if ( Utils.isEmpty( data.realTableName ) ) {
          throw new KettleStepException( "The name of the table is not specified!" );
        }
        data.realSchemaTable =
            data.db.getDatabaseMeta().getQuotedSchemaTableCombination( data.realSchemaName, data.realTableName );
      }

      if ( operation.equals( data.insertValue ) ) {
        // directly insert data into table
        /*
         *
         * INSERT ROW
         */

        if ( log.isRowLevel() ) {
          logRowlevel( BaseMessages.getString( PKG, "SynchronizeAfterMerge.InsertRow", row.toString() ) );
        }

        // The values to insert are those in the update section
        //
        Object[] insertRowData = new Object[data.valuenrs.length];
        for ( int i = 0; i < data.valuenrs.length; i++ ) {
          insertRowData[i] = row[data.valuenrs[i]];
        }

        if ( meta.istablenameInField() ) {
          data.insertStatement = data.preparedStatements.get( data.realSchemaTable + "insert" );
          if ( data.insertStatement == null ) {
            String sql = data.db.getInsertStatement( data.realSchemaName, data.realTableName, data.insertRowMeta );

            if ( log.isDebug() ) {
              logDebug( "Preparation of the insert SQL statement: " + sql );
            }

            data.insertStatement = data.db.prepareSQL( sql );
            data.preparedStatements.put( data.realSchemaTable + "insert", data.insertStatement );
          }
        }

        // For PG & GP, we add a savepoint before the row.
        // Then revert to the savepoint afterwards... (not a transaction, so hopefully still fast)
        //
        if ( data.specialErrorHandling && data.supportsSavepoints ) {
          data.savepoint = data.db.setSavepoint();
        }

        // Set the values on the prepared statement...
        data.db.setValues( data.insertRowMeta, insertRowData, data.insertStatement );
        data.db.insertRow( data.insertStatement, data.batchMode );
        performInsert = true;
        if ( !data.batchMode ) {
          incrementLinesOutput();
        }
        if ( log.isRowLevel() ) {
          logRowlevel( "Written row: " + data.insertRowMeta.getString( insertRowData ) );
        }

      } else {

        Object[] lookupRow = new Object[data.keynrs.length];
        int lookupIndex = 0;
        for ( int i = 0; i < meta.getKeyStream().length; i++ ) {
          if ( data.keynrs[i] >= 0 ) {
            lookupRow[lookupIndex] = row[data.keynrs[i]];
            lookupIndex++;
          }
          if ( data.keynrs2[i] >= 0 ) {
            lookupRow[lookupIndex] = row[data.keynrs2[i]];
            lookupIndex++;
          }
        }
        boolean updateorDelete = false;
        if ( meta.isPerformLookup() ) {

          // LOOKUP

          if ( meta.istablenameInField() ) {
            // Prepare Lookup statement
            data.lookupStatement = data.preparedStatements.get( data.realSchemaTable + "lookup" );
            if ( data.lookupStatement == null ) {
              String sql = getLookupStatement( data.inputRowMeta );

              if ( log.isDebug() ) {
                logDebug( "Preparating SQL for insert: " + sql );
              }

              data.lookupStatement = data.db.prepareSQL( sql );
              data.preparedStatements.put( data.realSchemaTable + "lookup", data.lookupStatement );
            }
          }

          data.db.setValues( data.lookupParameterRowMeta, lookupRow, data.lookupStatement );
          if ( log.isRowLevel() ) {
            logRowlevel( BaseMessages.getString( PKG, "SynchronizeAfterMerge.Log.ValuesSetForLookup",
                data.lookupParameterRowMeta.getString( lookupRow ) ) );
          }
          Object[] add = data.db.getLookup( data.lookupStatement );
          incrementLinesInput();

          if ( add == null ) {
            // nothing was found:

            if ( data.stringErrorKeyNotFound == null ) {
              data.stringErrorKeyNotFound =
                  BaseMessages.getString( PKG, "SynchronizeAfterMerge.Exception.KeyCouldNotFound" )
                      + data.lookupParameterRowMeta.getString( lookupRow );
              data.stringFieldnames = "";
              for ( int i = 0; i < data.lookupParameterRowMeta.size(); i++ ) {
                if ( i > 0 ) {
                  data.stringFieldnames += ", ";
                }
                data.stringFieldnames += data.lookupParameterRowMeta.getValueMeta( i ).getName();
              }
            }
            data.lookupFailure = true;
            throw new KettleDatabaseException( BaseMessages.getString( PKG,
                "SynchronizeAfterMerge.Exception.KeyCouldNotFound", data.lookupParameterRowMeta.getString(
                    lookupRow ) ) );
          } else {
            if ( log.isRowLevel() ) {
              logRowlevel( BaseMessages.getString( PKG, "SynchronizeAfterMerge.Log.FoundRowForUpdate",
                  data.insertRowMeta.getString( row ) ) );
            }

            for ( int i = 0; i < data.valuenrs.length; i++ ) {
              if ( meta.getUpdate()[i].booleanValue() ) {
                ValueMetaInterface valueMeta = data.inputRowMeta.getValueMeta( data.valuenrs[i] );
                ValueMetaInterface retMeta = data.db.getReturnRowMeta().getValueMeta( i );

                Object rowvalue = row[data.valuenrs[i]];
                Object retvalue = add[i];

                if ( valueMeta.compare( rowvalue, retMeta, retvalue ) != 0 ) {
                  updateorDelete = true;
                }
              }
            }
          }
        } // end if perform lookup

        if ( operation.equals( data.updateValue ) ) {
          if ( !meta.isPerformLookup() || updateorDelete ) {
            // UPDATE :

            if ( meta.istablenameInField() ) {
              data.updateStatement = data.preparedStatements.get( data.realSchemaTable + "update" );
              if ( data.updateStatement == null ) {
                String sql = getUpdateStatement( data.inputRowMeta );

                data.updateStatement = data.db.prepareSQL( sql );
                data.preparedStatements.put( data.realSchemaTable + "update", data.updateStatement );
                if ( log.isDebug() ) {
                  logDebug( "Preparation of the Update SQL statement : " + sql );
                }
              }
            }

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

            // For PG & GP, we add a savepoint before the row.
            // Then revert to the savepoint afterwards... (not a transaction, so hopefully still fast)
            //
            if ( data.specialErrorHandling && data.supportsSavepoints ) {
              data.savepoint = data.db.setSavepoint();
            }
            data.db.setValues( data.updateParameterRowMeta, updateRow, data.updateStatement );
            if ( log.isRowLevel() ) {
              logRowlevel( BaseMessages.getString( PKG, "SynchronizeAfterMerge.Log.SetValuesForUpdate",
                  data.updateParameterRowMeta.getString( updateRow ), data.inputRowMeta.getString( row ) ) );
            }
            data.db.insertRow( data.updateStatement, data.batchMode );
            performUpdate = true;
            incrementLinesUpdated();

          } else {
            // end if operation update
            incrementLinesSkipped();
            lineSkipped = true;
          }
        } else if ( operation.equals( data.deleteValue ) ) {
          // DELETE

          if ( meta.istablenameInField() ) {
            data.deleteStatement = data.preparedStatements.get( data.realSchemaTable + "delete" );

            if ( data.deleteStatement == null ) {
              String sql = getDeleteStatement( data.inputRowMeta );
              data.deleteStatement = data.db.prepareSQL( sql );
              data.preparedStatements.put( data.realSchemaTable + "delete", data.deleteStatement );
              if ( log.isDebug() ) {
                logDebug( "Preparation of the Delete SQL statement : " + sql );
              }
            }
          }

          Object[] deleteRow = new Object[data.deleteParameterRowMeta.size()];
          int deleteIndex = 0;

          for ( int i = 0; i < meta.getKeyStream().length; i++ ) {
            if ( data.keynrs[i] >= 0 ) {
              deleteRow[deleteIndex] = row[data.keynrs[i]];
              deleteIndex++;
            }
            if ( data.keynrs2[i] >= 0 ) {
              deleteRow[deleteIndex] = row[data.keynrs2[i]];
              deleteIndex++;
            }
          }

          // For PG & GP, we add a savepoint before the row.
          // Then revert to the savepoint afterwards... (not a transaction, so hopefully still fast)
          //
          if ( data.specialErrorHandling && data.supportsSavepoints ) {
            data.savepoint = data.db.setSavepoint();
          }
          data.db.setValues( data.deleteParameterRowMeta, deleteRow, data.deleteStatement );
          if ( log.isRowLevel() ) {
            logRowlevel( BaseMessages.getString( PKG, "SynchronizeAfterMerge.Log.SetValuesForDelete",
                data.deleteParameterRowMeta.getString( deleteRow ), data.inputRowMeta.getString( row ) ) );
          }
          data.db.insertRow( data.deleteStatement, data.batchMode );
          performDelete = true;
          incrementLinesUpdated();
        } else {
          // endif operation delete
          incrementLinesSkipped();
          lineSkipped = true;
        }
      } // endif operation insert

      // If we skip a line we need to empty the buffer and skip the line in question.
      // The skipped line is never added to the buffer!
      //
      if ( performInsert || performUpdate || performDelete || ( data.batchBuffer.size() > 0 && lineSkipped ) ) {
        // Get a commit counter per prepared statement to keep track of separate tables, etc.
        //
        String tableName = data.realSchemaTable;
        if ( performInsert ) {
          tableName += "insert";
        } else if ( performUpdate ) {
          tableName += "update";
        }
        if ( performDelete ) {
          tableName += "delete";
        }

        Integer commitCounter = data.commitCounterMap.get( tableName );
        if ( commitCounter == null ) {
          commitCounter = Integer.valueOf( 0 );
        }
        data.commitCounterMap.put( tableName, Integer.valueOf( commitCounter.intValue() + 1 ) );

        // Release the savepoint if needed
        //
        if ( data.specialErrorHandling && data.supportsSavepoints ) {
          if ( data.releaseSavepoint ) {
            data.db.releaseSavepoint( data.savepoint );
          }
        }

        // Perform a commit if needed
        //
        if ( commitCounter > 0 && ( commitCounter % data.commitSize ) == 0 ) {
          if ( data.batchMode ) {
            try {
              if ( performInsert ) {
                data.insertStatement.executeBatch();
                data.db.commit();
                data.insertStatement.clearBatch();
              } else if ( performUpdate ) {
                data.updateStatement.executeBatch();
                data.db.commit();
                data.updateStatement.clearBatch();
              } else if ( performDelete ) {
                data.deleteStatement.executeBatch();
                data.db.commit();
                data.deleteStatement.clearBatch();
              }
            } catch ( SQLException ex ) {
              throw Database.createKettleDatabaseBatchException( BaseMessages.getString( PKG,
                  "SynchronizeAfterMerge.Error.UpdatingBatch" ), ex );
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
      }
    } catch ( KettleDatabaseBatchException be ) {
      errorMessage = be.toString();
      batchProblem = true;
      sendToErrorRow = true;
      updateCounts = be.getUpdateCounts();
      exceptionsList = be.getExceptionsList();

      if ( data.insertStatement != null ) {
        data.db.clearBatch( data.insertStatement );
      }
      if ( data.updateStatement != null ) {
        data.db.clearBatch( data.updateStatement );
      }
      if ( data.deleteStatement != null ) {
        data.db.clearBatch( data.deleteStatement );
      }

      if ( getStepMeta().isDoingErrorHandling() ) {
        data.db.commit( true );
      } else {
        data.db.rollback();
        StringBuilder msg = new StringBuilder( "Error batch inserting rows into table [" + data.realTableName + "]." );
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
        if ( log.isRowLevel() ) {
          logRowlevel( "Written row to error handling : " + getInputRowMeta().getString( row ) );
        }

        if ( data.specialErrorHandling && data.supportsSavepoints ) {
          if ( data.savepoint != null || !data.lookupFailure ) {
            // do this when savepoint was set, and this is not lookup failure PDI-10878
            data.db.rollback( data.savepoint );
            if ( data.releaseSavepoint ) {
              data.db.releaseSavepoint( data.savepoint );
            }
          }
        }
        sendToErrorRow = true;
        errorMessage = dbe.toString();
      } else {
        setErrors( getErrors() + 1 );
        data.db.rollback();
        throw new KettleException( "Error inserting row into table [" + data.realTableName + "] with values: "
            + data.inputRowMeta.getString( row ), dbe );
      }
    }

    if ( data.batchMode ) {
      if ( sendToErrorRow ) {
        if ( batchProblem ) {
          data.batchBuffer.add( row );
          processBatchException( errorMessage, updateCounts, exceptionsList );
        } else {
          // Simply add this row to the error row
          putError( data.inputRowMeta, row, 1L, errorMessage, null, "SUYNC002" );
        }
      } else {
        if ( !lineSkipped ) {
          data.batchBuffer.add( row );
        }

        if ( rowIsSafe ) { // A commit was done and the rows are all safe (no error)
          for ( int i = 0; i < data.batchBuffer.size(); i++ ) {
            Object[] rowb = data.batchBuffer.get( i );
            putRow( data.outputRowMeta, rowb );
            if ( data.inputRowMeta.getString( rowb, data.indexOfOperationOrderField ).equals( data.insertValue ) ) {
              incrementLinesOutput();
            }
          }
          // Clear the buffer
          data.batchBuffer.clear();
        }

        // Don't forget to pass this line to the following steps
        //
        if ( lineSkipped ) {
          putRow( data.outputRowMeta, row );
        }
      }
    } else {
      if ( sendToErrorRow ) {
        if ( data.lookupFailure ) {
          putError( data.inputRowMeta, row, 1, data.stringErrorKeyNotFound, data.stringFieldnames, "SUYNC001" );
        } else {
          putError( data.inputRowMeta, row, 1, errorMessage, null, "SUYNC001" );
        }
      }
    }
  }

  private void processBatchException( String errorMessage, int[] updateCounts, List<Exception> exceptionsList )
    throws KettleException {
    // There was an error with the commit
    // We should put all the failing rows out there...
    //
    if ( updateCounts != null ) {
      int errNr = 0;
      for ( int i = 0; i < updateCounts.length; i++ ) {
        Object[] row = data.batchBuffer.get( i );
        if ( updateCounts[i] > 0 ) {
          // send the error forward
          putRow( data.outputRowMeta, row );
          incrementLinesOutput();
        } else {
          String exMessage = errorMessage;
          if ( errNr < exceptionsList.size() ) {
            SQLException se = (SQLException) exceptionsList.get( errNr );
            errNr++;
            exMessage = se.toString();
          }
          putError( data.outputRowMeta, row, 1L, exMessage, null, "SUYNC002" );
        }
      }
    } else {
      // If we don't have update counts, it probably means the DB doesn't support it.
      // In this case we don't have a choice but to consider all inserted rows to be error rows.
      //
      for ( int i = 0; i < data.batchBuffer.size(); i++ ) {
        Object[] row = data.batchBuffer.get( i );
        putError( data.outputRowMeta, row, 1L, errorMessage, null, "SUYNC003" );
      }
    }

    // Clear the buffer afterwards...
    data.batchBuffer.clear();
  }

  // Lookup certain fields in a table
  public String getLookupStatement( RowMetaInterface rowMeta ) throws KettleDatabaseException {
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

    sql += " FROM " + data.realSchemaTable + " WHERE ";

    for ( int i = 0; i < meta.getKeyLookup().length; i++ ) {
      if ( i != 0 ) {
        sql += " AND ";
      }
      sql += databaseMeta.quoteField( meta.getKeyLookup()[i] );
      if ( "BETWEEN".equalsIgnoreCase( meta.getKeyCondition()[i] ) ) {
        sql += " BETWEEN ? AND ? ";
        data.lookupParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyStream()[i] ) );
        data.lookupParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyStream2()[i] ) );
      } else {
        if ( "IS NULL".equalsIgnoreCase( meta.getKeyCondition()[i] ) || "IS NOT NULL".equalsIgnoreCase( meta
            .getKeyCondition()[i] ) ) {
          sql += " " + meta.getKeyCondition()[i] + " ";
        } else {
          sql += " " + meta.getKeyCondition()[i] + " ? ";
          data.lookupParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyStream()[i] ) );
        }
      }
    }
    return sql;
  }

  // Lookup certain fields in a table
  public String getUpdateStatement( RowMetaInterface rowMeta ) throws KettleDatabaseException {
    DatabaseMeta databaseMeta = meta.getDatabaseMeta();
    data.updateParameterRowMeta = new RowMeta();

    String sql = "UPDATE " + data.realSchemaTable + Const.CR;
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
      sql += databaseMeta.quoteField( meta.getKeyLookup()[i] );
      if ( "BETWEEN".equalsIgnoreCase( meta.getKeyCondition()[i] ) ) {
        sql += " BETWEEN ? AND ? ";
        data.updateParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyStream()[i] ) );
        data.updateParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyStream2()[i] ) );
      } else if ( "IS NULL".equalsIgnoreCase( meta.getKeyCondition()[i] ) || "IS NOT NULL".equalsIgnoreCase( meta
          .getKeyCondition()[i] ) ) {
        sql += " " + meta.getKeyCondition()[i] + " ";
      } else {
        sql += " " + meta.getKeyCondition()[i] + " ? ";
        data.updateParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyStream()[i] ).clone() );
      }
    }
    return sql;
  }

  public String getDeleteStatement( RowMetaInterface rowMeta ) throws KettleDatabaseException {
    DatabaseMeta databaseMeta = meta.getDatabaseMeta();
    data.deleteParameterRowMeta = new RowMeta();

    String sql = "DELETE FROM " + data.realSchemaTable + Const.CR;

    sql += "WHERE ";

    for ( int i = 0; i < meta.getKeyLookup().length; i++ ) {
      if ( i != 0 ) {
        sql += "AND   ";
      }
      sql += databaseMeta.quoteField( meta.getKeyLookup()[i] );
      if ( "BETWEEN".equalsIgnoreCase( meta.getKeyCondition()[i] ) ) {
        sql += " BETWEEN ? AND ? ";
        data.deleteParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyStream()[i] ) );
        data.deleteParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyStream2()[i] ) );
      } else if ( "IS NULL".equalsIgnoreCase( meta.getKeyCondition()[i] ) || "IS NOT NULL".equalsIgnoreCase( meta
          .getKeyCondition()[i] ) ) {
        sql += " " + meta.getKeyCondition()[i] + " ";
      } else {
        sql += " " + meta.getKeyCondition()[i] + " ? ";
        data.deleteParameterRowMeta.addValueMeta( rowMeta.searchValueMeta( meta.getKeyStream()[i] ) );
      }
    }
    return sql;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (SynchronizeAfterMergeMeta) smi;
    data = (SynchronizeAfterMergeData) sdi;

    Object[] nextRow = getRow(); // Get row from input rowset & set row busy!
    if ( nextRow == null ) { // no more input to be expected...
      finishStep();
      return false;
    }

    if ( first ) {
      first = false;
      data.outputRowMeta = getInputRowMeta().clone();
      data.inputRowMeta = data.outputRowMeta;
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      if ( meta.istablenameInField() ) {
        // Cache the position of the table name field
        if ( data.indexOfTableNameField < 0 ) {
          data.indexOfTableNameField = data.inputRowMeta.indexOfValue( meta.gettablenameField() );
          if ( data.indexOfTableNameField < 0 ) {
            String message =
                "It was not possible to find table [" + meta.gettablenameField() + "] in the input fields.";
            logError( message );
            throw new KettleStepException( message );
          }
        }
      } else {
        data.realTableName = environmentSubstitute( meta.getTableName() );
        if ( Utils.isEmpty( data.realTableName ) ) {
          throw new KettleStepException( "The table name is not specified (or the input field is empty)" );
        }
        data.realSchemaTable =
            data.db.getDatabaseMeta().getQuotedSchemaTableCombination( data.realSchemaName, data.realTableName );
      }

      // Cache the position of the operation order field
      if ( data.indexOfOperationOrderField < 0 ) {
        data.indexOfOperationOrderField = data.inputRowMeta.indexOfValue( meta.getOperationOrderField() );
        if ( data.indexOfOperationOrderField < 0 ) {
          String message =
              "It was not possible to find operation field [" + meta.getOperationOrderField()
                  + "] in the input stream!";
          logError( message );
          throw new KettleStepException( message );
        }
      }

      data.insertValue = environmentSubstitute( meta.getOrderInsert() );
      data.updateValue = environmentSubstitute( meta.getOrderUpdate() );
      data.deleteValue = environmentSubstitute( meta.getOrderDelete() );

      data.insertRowMeta = new RowMeta();

      // lookup the values!
      if ( log.isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "SynchronizeAfterMerge.Log.CheckingRow" ) + Arrays.toString( nextRow ) );
      }

      data.keynrs = new int[meta.getKeyStream().length];
      data.keynrs2 = new int[meta.getKeyStream().length];
      for ( int i = 0; i < meta.getKeyStream().length; i++ ) {
        data.keynrs[i] = data.inputRowMeta.indexOfValue( meta.getKeyStream()[i] );
        if ( data.keynrs[i] < 0 && // couldn't find field!
            !"IS NULL".equalsIgnoreCase( meta.getKeyCondition()[i] ) && // No field needed!
            !"IS NOT NULL".equalsIgnoreCase( meta.getKeyCondition()[i] ) // No field needed!
        ) {
          throw new KettleStepException( BaseMessages.getString( PKG, "SynchronizeAfterMerge.Exception.FieldRequired",
              meta.getKeyStream()[i] ) );
        }
        data.keynrs2[i] = data.inputRowMeta.indexOfValue( meta.getKeyStream2()[i] );
        if ( data.keynrs2[i] < 0 && // couldn't find field!
            "BETWEEN".equalsIgnoreCase( meta.getKeyCondition()[i] ) // 2 fields needed!
        ) {
          throw new KettleStepException( BaseMessages.getString( PKG, "SynchronizeAfterMerge.Exception.FieldRequired",
              meta.getKeyStream2()[i] ) );
        }

        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "SynchronizeAfterMerge.Log.FieldHasDataNumbers", meta
              .getKeyStream()[i] ) + data.keynrs[i] );
        }
      }

      // Insert the update fields: just names. Type doesn't matter!
      for ( int i = 0; i < meta.getUpdateLookup().length; i++ ) {
        ValueMetaInterface insValue = data.insertRowMeta.searchValueMeta( meta.getUpdateLookup()[i] );
        if ( insValue == null ) { // Don't add twice!
          // we already checked that this value exists so it's probably safe to ignore lookup failure...
          ValueMetaInterface insertValue = data.inputRowMeta.searchValueMeta( meta.getUpdateStream()[i] ).clone();
          insertValue.setName( meta.getUpdateLookup()[i] );
          data.insertRowMeta.addValueMeta( insertValue );
        } else {
          throw new KettleStepException( BaseMessages.getString( PKG,
              "SynchronizeAfterMerge.Error.SameColumnInsertedTwice", insValue.getName() ) );
        }
      }

      // Cache the position of the compare fields in Row row
      //
      data.valuenrs = new int[meta.getUpdateLookup().length];
      for ( int i = 0; i < meta.getUpdateLookup().length; i++ ) {
        data.valuenrs[i] = data.inputRowMeta.indexOfValue( meta.getUpdateStream()[i] );
        if ( data.valuenrs[i] < 0 ) { // couldn't find field!
          throw new KettleStepException( BaseMessages.getString( PKG, "SynchronizeAfterMerge.Exception.FieldRequired",
              meta.getUpdateStream()[i] ) );
        }
        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "SynchronizeAfterMerge.Log.FieldHasDataNumbers", meta
              .getUpdateStream()[i] ) + data.valuenrs[i] );
        }
      }

      if ( !meta.istablenameInField() ) {
        // Prepare Lookup statement
        if ( meta.isPerformLookup() ) {
          data.lookupStatement = data.preparedStatements.get( data.realSchemaTable + "lookup" );
          if ( data.lookupStatement == null ) {
            String sql = getLookupStatement( data.inputRowMeta );
            if ( log.isDebug() ) {
              logDebug( "Preparation of the lookup SQL statement : " + sql );
            }

            data.lookupStatement = data.db.prepareSQL( sql );
            data.preparedStatements.put( data.realSchemaTable + "lookup", data.lookupStatement );
          }
        }

        // Prepare Insert statement
        data.insertStatement = data.preparedStatements.get( data.realSchemaTable + "insert" );
        if ( data.insertStatement == null ) {
          String sql = data.db.getInsertStatement( data.realSchemaName, data.realTableName, data.insertRowMeta );

          if ( log.isDebug() ) {
            logDebug( "Preparation of the Insert SQL statement : " + sql );
          }

          data.insertStatement = data.db.prepareSQL( sql );
          data.preparedStatements.put( data.realSchemaTable + "insert", data.insertStatement );
        }

        // Prepare Update Statement

        data.updateStatement = data.preparedStatements.get( data.realSchemaTable + "update" );
        if ( data.updateStatement == null ) {
          String sql = getUpdateStatement( data.inputRowMeta );

          data.updateStatement = data.db.prepareSQL( sql );
          data.preparedStatements.put( data.realSchemaTable + "update", data.updateStatement );
          if ( log.isDebug() ) {
            logDebug( "Preparation of the Update SQL statement : " + sql );
          }
        }

        // Prepare delete statement
        data.deleteStatement = data.preparedStatements.get( data.realSchemaTable + "delete" );
        if ( data.deleteStatement == null ) {
          String sql = getDeleteStatement( data.inputRowMeta );

          data.deleteStatement = data.db.prepareSQL( sql );
          data.preparedStatements.put( data.realSchemaTable + "delete", data.deleteStatement );
          if ( log.isDebug() ) {
            logDebug( "Preparation of the Delete SQL statement : " + sql );
          }
        }
      }

    } // end if first

    try {
      lookupValues( nextRow ); // add new values to the row in rowset[0].
      if ( !data.batchMode ) {
        putRow( data.outputRowMeta, nextRow ); // copy row to output rowset(s);
      }

      if ( checkFeedback( getLinesRead() ) ) {
        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "SynchronizeAfterMerge.Log.LineNumber" ) + getLinesRead() );
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

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (SynchronizeAfterMergeMeta) smi;
    data = (SynchronizeAfterMergeData) sdi;

    if ( super.init( smi, sdi ) ) {
      try {
        meta.normalizeAllocationFields();
        data.realSchemaName = environmentSubstitute( meta.getSchemaName() );
        if ( meta.istablenameInField() ) {
          if ( Utils.isEmpty( meta.gettablenameField() ) ) {
            logError( BaseMessages.getString( PKG, "SynchronizeAfterMerge.Log.Error.TableFieldnameEmpty" ) );
            return false;
          }
        }

        data.databaseMeta = meta.getDatabaseMeta();

        // if we are using Oracle then set releaseSavepoint to false
        if ( data.databaseMeta.getDatabaseInterface() instanceof OracleDatabaseMeta ) {
          data.releaseSavepoint = false;
        }

        data.commitSize = Integer.parseInt( environmentSubstitute( "" + meta.getCommitSize() ) );
        data.batchMode = data.commitSize > 0 && meta.useBatchUpdate();

        // Batch updates are not supported on PostgreSQL (and look-a-likes) together with error handling (PDI-366)
        //
        data.specialErrorHandling =
            getStepMeta().isDoingErrorHandling() && meta.getDatabaseMeta().supportsErrorHandlingOnBatchUpdates();

        data.supportsSavepoints = meta.getDatabaseMeta().getDatabaseInterface().useSafePoints();

        if ( data.batchMode && data.specialErrorHandling ) {
          data.batchMode = false;
          if ( log.isBasic() ) {
            logBasic( BaseMessages.getString( PKG, "SynchronizeAfterMerge.Log.BatchModeDisabled" ) );
          }
        }

        if ( meta.getDatabaseMeta() == null ) {
          logError( BaseMessages.getString( PKG, "SynchronizeAfterMerge.Init.ConnectionMissing", getStepname() ) );
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
        data.db.setCommit( meta.getCommitSize() );

        return true;
      } catch ( KettleException ke ) {
        logError( BaseMessages.getString( PKG, "SynchronizeAfterMerge.Log.ErrorOccurredDuringStepInitialize" ) + ke
            .getMessage() );
      }
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    finishStep();
    super.dispose( smi, sdi );
  }

  private void finishStep() {
    if ( data.db != null && data.db.getConnection() != null ) {
      try {
        if ( !data.db.getConnection().isClosed() ) {
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
            if ( data.inputRowMeta.getString( row, data.indexOfOperationOrderField ).equals( data.insertValue ) ) {
              incrementLinesOutput();
            }
          }
          // Clear the buffer
          data.batchBuffer.clear();
        }
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

        data.db.disconnect();
      }
    }
  }
}
