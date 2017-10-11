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

package org.pentaho.di.trans.steps.dimensionlookup;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.hash.ByteArrayHashMap;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaFactory;
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
 * Manages a slowly changing dimension (lookup or update)
 *
 * @author Matt
 * @since 14-may-2003
 */
public class DimensionLookup extends BaseStep implements StepInterface {
  private static Class<?> PKG = DimensionLookupMeta.class; // for i18n purposes, needed by Translator2!!

  private static final int CREATION_METHOD_AUTOINC = 1;
  private static final int CREATION_METHOD_SEQUENCE = 2;
  private static final int CREATION_METHOD_TABLEMAX = 3;

  private int techKeyCreation;

  private DimensionLookupMeta meta;
  private DimensionLookupData data;
  int[] columnLookupArray = null;

  public DimensionLookup( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  protected void setMeta( DimensionLookupMeta meta ) {
    this.meta = meta;
  }

  protected void setData( DimensionLookupData data ) {
    this.data = data;
  }

  private void setTechKeyCreation( int method ) {
    techKeyCreation = method;
  }

  private int getTechKeyCreation() {
    return techKeyCreation;
  }

  private void determineTechKeyCreation() {
    String keyCreation = meta.getTechKeyCreation();
    if ( meta.getDatabaseMeta().supportsAutoinc()
      && DimensionLookupMeta.CREATION_METHOD_AUTOINC.equals( keyCreation ) ) {
      setTechKeyCreation( CREATION_METHOD_AUTOINC );
    } else if ( meta.getDatabaseMeta().supportsSequences()
      && DimensionLookupMeta.CREATION_METHOD_SEQUENCE.equals( keyCreation ) ) {
      setTechKeyCreation( CREATION_METHOD_SEQUENCE );
    } else {
      setTechKeyCreation( CREATION_METHOD_TABLEMAX );
    }
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (DimensionLookupMeta) smi;
    data = (DimensionLookupData) sdi;

    Object[] r = getRow(); // Get row from input rowset & set row busy!
    if ( r == null ) { // no more input to be expected...

      setOutputDone(); // signal end to receiver(s)
      return false;
    }

    if ( first ) {
      first = false;

      data.schemaTable =
        meta.getDatabaseMeta().getQuotedSchemaTableCombination( data.realSchemaName, data.realTableName );

      data.inputRowMeta = getInputRowMeta().clone();
      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      // Get the fields that need conversion to normal storage...
      // Modify the storage type of the input data...
      //
      data.lazyList = new ArrayList<Integer>();
      for ( int i = 0; i < data.inputRowMeta.size(); i++ ) {
        ValueMetaInterface valueMeta = data.inputRowMeta.getValueMeta( i );
        if ( valueMeta.isStorageBinaryString() ) {
          data.lazyList.add( i );
          valueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
        }
      }

      // The start date value column (if applicable)
      //
      data.startDateFieldIndex = -1;
      if ( data.startDateChoice == DimensionLookupMeta.START_DATE_ALTERNATIVE_COLUMN_VALUE ) {
        data.startDateFieldIndex = data.inputRowMeta.indexOfValue( meta.getStartDateFieldName() );
        if ( data.startDateFieldIndex < 0 ) {
          throw new KettleStepException( BaseMessages.getString(
            PKG, "DimensionLookup.Exception.StartDateValueColumnNotFound", meta.getStartDateFieldName() ) );
        }
      }

      // Lookup values
      data.keynrs = new int[meta.getKeyStream().length];
      for ( int i = 0; i < meta.getKeyStream().length; i++ ) {
        // logDetailed("Lookup values key["+i+"] --> "+key[i]+", row==null?"+(row==null));
        data.keynrs[i] = data.inputRowMeta.indexOfValue( meta.getKeyStream()[i] );
        if ( data.keynrs[i] < 0 ) { // couldn't find field!
          throw new KettleStepException( BaseMessages.getString(
            PKG, "DimensionLookup.Exception.KeyFieldNotFound", meta.getKeyStream()[i] ) );
        }
      }

      // Return values
      data.fieldnrs = new int[meta.getFieldStream().length];
      for ( int i = 0; meta.getFieldStream() != null && i < meta.getFieldStream().length; i++ ) {
        if ( !DimensionLookupMeta.isUpdateTypeWithoutArgument( meta.isUpdate(), meta.getFieldUpdate()[i] ) ) {
          data.fieldnrs[i] = data.outputRowMeta.indexOfValue( meta.getFieldStream()[i] );
          if ( data.fieldnrs[i] < 0 ) {
            throw new KettleStepException( BaseMessages.getString(
              PKG, "DimensionLookup.Exception.KeyFieldNotFound", meta.getFieldStream()[i] ) );
          }
        } else {
          data.fieldnrs[i] = -1;
        }

      }

      if ( !meta.isUpdate() && meta.isPreloadingCache() ) {
        preloadCache();
      } else {
        // Caching...
        //
        if ( data.cacheKeyRowMeta == null ) {
          // KEY : the natural key(s)
          //
          data.cacheKeyRowMeta = new RowMeta();
          for ( int i = 0; i < data.keynrs.length; i++ ) {
            ValueMetaInterface key = data.inputRowMeta.getValueMeta( data.keynrs[i] );
            data.cacheKeyRowMeta.addValueMeta( key.clone() );
          }

          data.cache =
            new ByteArrayHashMap( meta.getCacheSize() > 0 ? meta.getCacheSize() : 5000, data.cacheKeyRowMeta );
        }
      }

      if ( !Utils.isEmpty( meta.getDateField() ) ) {
        data.datefieldnr = data.inputRowMeta.indexOfValue( meta.getDateField() );
      } else {
        data.datefieldnr = -1;
      }

      // Initialize the start date value in case we don't have one in the input rows
      //
      data.valueDateNow = determineDimensionUpdatedDate( r );

      determineTechKeyCreation();

      data.notFoundTk = new Long( meta.getDatabaseMeta().getNotFoundTK( isAutoIncrement() ) );
      // if (meta.getKeyRename()!=null && meta.getKeyRename().length()>0) data.notFoundTk.setName(meta.getKeyRename());

      if ( getCopy() == 0 ) {
        checkDimZero();
      }

      setDimLookup( data.outputRowMeta );
    }

    // convert row to normal storage...
    //
    for ( int lazyFieldIndex : data.lazyList ) {
      ValueMetaInterface valueMeta = getInputRowMeta().getValueMeta( lazyFieldIndex );
      r[lazyFieldIndex] = valueMeta.convertToNormalStorageType( r[lazyFieldIndex] );
    }

    try {
      Object[] outputRow = lookupValues( data.inputRowMeta, r ); // add new values to the row in rowset[0].
      putRow( data.outputRowMeta, outputRow ); // copy row to output rowset(s);

      if ( checkFeedback( getLinesRead() ) ) {
        if ( log.isBasic() ) {
          logBasic( BaseMessages.getString( PKG, "DimensionLookup.Log.LineNumber" ) + getLinesRead() );
        }
      }
    } catch ( KettleException e ) {
      logError( BaseMessages.getString( PKG, "DimensionLookup.Log.StepCanNotContinueForErrors", e.getMessage() ) );
      logError( Const.getStackTracker( e ) );
      setErrors( 1 );
      stopAll();
      setOutputDone(); // signal end to receiver(s)
      return false;
    }

    return true;
  }

  private Date determineDimensionUpdatedDate( Object[] row ) throws KettleException {
    if ( data.datefieldnr < 0 ) {
      return getTrans().getCurrentDate(); // start of transformation...
    } else {
      Date rtn = data.inputRowMeta.getDate( row, data.datefieldnr ); // Date field in the input row
      if ( rtn != null ) {
        return rtn;
      } else {
        // Fix for PDI-4816
        String inputRowMetaStringMeta = null;
        try {
          inputRowMetaStringMeta = data.inputRowMeta.toStringMeta();
        } catch ( Exception ex ) {
          inputRowMetaStringMeta = "No row input meta";
        }
        throw new KettleStepException( BaseMessages.getString(
          PKG, "DimensionLookup.Exception.NullDimensionUpdatedDate", inputRowMetaStringMeta ) );
      }
    }
  }

  /**
   * Pre-load the cache by reading the whole dimension table from disk...
   *
   * @throws KettleException
   *           in case there is a database or cache problem.
   */
  private void preloadCache() throws KettleException {
    try {
      DatabaseMeta databaseMeta = meta.getDatabaseMeta();

      // tk, version, from, to, natural keys, retrieval fields...
      //
      String sql = "SELECT " + databaseMeta.quoteField( meta.getKeyField() );
      // sql+=", "+databaseMeta.quoteField(meta.getVersionField());
      for ( int i = 0; i < meta.getKeyLookup().length; i++ ) {
        sql += ", " + meta.getKeyLookup()[i]; // the natural key field in the table
      }
      for ( int i = 0; i < meta.getFieldLookup().length; i++ ) {
        sql += ", " + meta.getFieldLookup()[i]; // the extra fields to retrieve...
      }
      sql += ", " + databaseMeta.quoteField( meta.getDateFrom() ); // extra info in cache
      sql += ", " + databaseMeta.quoteField( meta.getDateTo() ); // extra info in cache

      sql += " FROM " + data.schemaTable;
      logDetailed( "Pre-loading cache by reading from database with: " + Const.CR + sql + Const.CR );

      List<Object[]> rows = data.db.getRows( sql, -1 );
      RowMetaInterface rowMeta = data.db.getReturnRowMeta();

      data.preloadKeyIndexes = new int[meta.getKeyLookup().length];
      for ( int i = 0; i < data.preloadKeyIndexes.length; i++ ) {
        data.preloadKeyIndexes[i] = rowMeta.indexOfValue( meta.getKeyLookup()[i] ); // the field in the table
      }
      data.preloadFromDateIndex = rowMeta.indexOfValue( meta.getDateFrom() );
      data.preloadToDateIndex = rowMeta.indexOfValue( meta.getDateTo() );

      data.preloadCache =
        new DimensionCache( rowMeta, data.preloadKeyIndexes, data.preloadFromDateIndex, data.preloadToDateIndex );
      data.preloadCache.setRowCache( rows );

      logDetailed( "Sorting the cache rows..." );
      data.preloadCache.sortRows();
      logDetailed( "Sorting of cached rows finished." );

      // Also see what indexes to take to populate the lookup row...
      // We only ever compare indexes and the lookup date in the cache, the rest is not needed...
      //
      data.preloadIndexes = new ArrayList<Integer>();
      for ( int i = 0; i < meta.getKeyStream().length; i++ ) {
        int index = data.inputRowMeta.indexOfValue( meta.getKeyStream()[i] );
        if ( index < 0 ) {
          // Just to be safe...
          //
          throw new KettleStepException( BaseMessages.getString(
            PKG, "DimensionLookup.Exception.KeyFieldNotFound", meta.getFieldStream()[i] ) );
        }
        data.preloadIndexes.add( index );
      }

      // This is all for now...
    } catch ( Exception e ) {
      throw new KettleException( "Error encountered during cache pre-load", e );
    }
  }

  private synchronized Object[] lookupValues( RowMetaInterface rowMeta, Object[] row ) throws KettleException {
    Object[] outputRow = new Object[data.outputRowMeta.size()];

    RowMetaInterface lookupRowMeta;
    Object[] lookupRow;

    Object[] returnRow = null;

    Long technicalKey;
    Long valueVersion;
    Date valueDate = null;
    Date valueDateFrom = null;
    Date valueDateTo = null;

    // Determine the lookup date ("now") if we have a field that carries said
    // date.
    // If not, the system date is taken.
    //
    valueDate = determineDimensionUpdatedDate( row );

    if ( !meta.isUpdate() && meta.isPreloadingCache() ) {
      // Obtain a result row from the pre-load cache...
      //
      // Create a row to compare with
      //
      RowMetaInterface preloadRowMeta = data.preloadCache.getRowMeta();

      // In this case it's all the same. (simple)
      //
      data.returnRowMeta = data.preloadCache.getRowMeta();
      lookupRowMeta = preloadRowMeta;
      lookupRow = new Object[preloadRowMeta.size()];

      // Assemble the lookup row, convert data if needed...
      //
      for ( int i = 0; i < data.preloadIndexes.size(); i++ ) {
        int from = data.preloadIndexes.get( i ); // Input row index
        int to = data.preloadCache.getKeyIndexes()[i]; // Lookup row index

        // From data type...
        //
        ValueMetaInterface fromValueMeta = rowMeta.getValueMeta( from );

        // to date type...
        //
        ValueMetaInterface toValueMeta = data.preloadCache.getRowMeta().getValueMeta( to );

        // From value:
        //
        Object fromData = row[from];

        // To value:
        //
        Object toData = toValueMeta.convertData( fromValueMeta, fromData );

        // Set the key in the row...
        //
        lookupRow[to] = toData;
      }

      // Also set the lookup date on the "end of date range" (toDate) position
      //
      lookupRow[data.preloadFromDateIndex] = valueDate;

      // Look up the row in the pre-load cache...
      //
      int index = data.preloadCache.lookupRow( lookupRow );
      if ( index >= 0 ) {
        returnRow = data.preloadCache.getRow( index );
      } else {
        returnRow = null; // Nothing found!
      }

    } else {
      lookupRow = new Object[data.lookupRowMeta.size()];
      lookupRowMeta = data.lookupRowMeta;

      // Construct the lookup row...
      //
      for ( int i = 0; i < meta.getKeyStream().length; i++ ) {
        try {
          lookupRow[i] = row[data.keynrs[i]];
        } catch ( Exception e ) { // TODO : remove exception??
          throw new KettleStepException(
            BaseMessages
              .getString(
                PKG,
                "DimensionLookup.Exception.ErrorDetectedInGettingKey", i + "", data.keynrs[i] + "/" + rowMeta.size(),
                rowMeta.getString( row ) ) );
        }
      }

      lookupRow[meta.getKeyStream().length] = valueDate; // ? >= date_from
      lookupRow[meta.getKeyStream().length + 1] = valueDate; // ? < date_to

      if ( isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "DimensionLookup.Log.LookupRow" )
          + data.lookupRowMeta.getString( lookupRow ) );
      }

      // Do the lookup and see if we can find anything in the database.
      // But before that, let's see if we can find anything in the cache
      //
      if ( meta.getCacheSize() >= 0 ) {
        returnRow = getFromCache( lookupRow, valueDate );
      }

      // Nothing found in the cache?
      // Perform the lookup in the database...
      //
      if ( returnRow == null ) {
        data.db.setValues( data.lookupRowMeta, lookupRow, data.prepStatementLookup );
        returnRow = data.db.getLookup( data.prepStatementLookup );
        data.returnRowMeta = data.db.getReturnRowMeta();

        incrementLinesInput();

        if ( returnRow != null && meta.getCacheSize() >= 0 ) {
          addToCache( lookupRow, returnRow );
        }
      }
    }

    // This next block of code handles the dimension key LOOKUP ONLY.
    // We handle this case where "update = false" first for performance reasons
    //
    if ( !meta.isUpdate() ) {
      if ( returnRow == null ) {
        returnRow = new Object[data.returnRowMeta.size()];
        returnRow[0] = data.notFoundTk;

        if ( meta.getCacheSize() >= 0 ) { // need -oo to +oo as well...
          returnRow[returnRow.length - 2] = data.min_date;
          returnRow[returnRow.length - 1] = data.max_date;
        }
      }
      // else {
      // We found the return values in row "add".
      // Throw away the version nr...
      // add.removeValue(1);

      // Rename the key field if needed. Do it directly in the row...
      // if (meta.getKeyRename()!=null && meta.getKeyRename().length()>0)
      // add.getValue(0).setName(meta.getKeyRename());
      // }

    } else {
      // This is the "update=true" case where we update the dimension table...
      // It is an "Insert - update" algorithm for slowly changing dimensions
      //
      // The dimension entry was not found, we need to add it!
      //
      if ( returnRow == null ) {
        if ( isRowLevel() ) {
          logRowlevel( BaseMessages.getString( PKG, "DimensionLookup.Log.NoDimensionEntryFound" )
            + lookupRowMeta.getString( lookupRow ) + ")" );
        }

        // Date range: ]-oo,+oo[
        //

        if ( data.startDateChoice == DimensionLookupMeta.START_DATE_ALTERNATIVE_SYSDATE ) {
          // use the time the step execution begins as the date from.
          // before, the current system time was used. this caused an exclusion of the row in the
          // lookup portion of the step that uses this 'valueDate' and not the current time.
          // the result was multiple inserts for what should have been 1 [PDI-4317]
          valueDateFrom = valueDate;
        } else {
          valueDateFrom = data.min_date;
        }

        valueDateTo = data.max_date;
        valueVersion = new Long( 1L ); // Versions always start at 1.

        // get a new value from the sequence generator chosen.
        //
        technicalKey = null;
        switch ( getTechKeyCreation() ) {
          case CREATION_METHOD_TABLEMAX:
            // What's the next value for the technical key?
            technicalKey =
              data.db.getNextValue( getTrans().getCounters(), data.realSchemaName, data.realTableName, meta
                .getKeyField() );
            break;
          case CREATION_METHOD_AUTOINC:
            technicalKey = null; // Set to null to flag auto-increment usage
            break;
          case CREATION_METHOD_SEQUENCE:
            technicalKey =
              data.db.getNextSequenceValue( data.realSchemaName, meta.getSequenceName(), meta.getKeyField() );
            if ( technicalKey != null && isRowLevel() ) {
              logRowlevel( BaseMessages.getString( PKG, "DimensionLookup.Log.FoundNextSequence" )
                + technicalKey.toString() );
            }
            break;
          default:
            break;
        }

        /*
         * INSERT INTO table(version, datefrom, dateto, fieldlookup) VALUES(valueVersion, valueDateFrom, valueDateTo,
         * row.fieldnrs) ;
         */

        technicalKey =
          dimInsert( data.inputRowMeta, row, technicalKey, true, valueVersion, valueDateFrom, valueDateTo );

        incrementLinesOutput();
        returnRow = new Object[data.returnRowMeta.size()];
        int returnIndex = 0;

        returnRow[returnIndex] = technicalKey;
        returnIndex++;

        // See if we need to store this record in the cache as well...
        /*
         * TODO: we can't really assume like this that the cache layout of the incoming rows (below) is the same as the
         * stored data. Storing this in the cache gives us data/metadata collision errors. (class cast problems etc)
         * Perhaps we need to convert this data to the target data types. Alternatively, we can use a separate cache in
         * the future. Reference: PDI-911
         *
         * if (meta.getCacheSize()>=0) { Object[] values = getCacheValues(rowMeta, row, technicalKey, valueVersion,
         * valueDateFrom, valueDateTo);
         *
         * // put it in the cache... if (values!=null) { addToCache(lookupRow, values); } }
         */

        if ( isRowLevel() ) {
          logRowlevel( BaseMessages.getString( PKG, "DimensionLookup.Log.AddedDimensionEntry" )
            + data.returnRowMeta.getString( returnRow ) );
        }
      } else {
        //
        // The entry was found: do we need to insert, update or both?
        //
        if ( isRowLevel() ) {
          logRowlevel( BaseMessages.getString( PKG, "DimensionLookup.Log.DimensionEntryFound" )
            + data.returnRowMeta.getString( returnRow ) );
        }

        // What's the key? The first value of the return row
        technicalKey = data.returnRowMeta.getInteger( returnRow, 0 );
        valueVersion = data.returnRowMeta.getInteger( returnRow, 1 );

        // Date range: ]-oo,+oo[
        valueDateFrom = meta.getMinDate();
        valueDateTo = meta.getMaxDate();

        // The other values, we compare with
        int cmp;

        // If everything is the same: don't do anything
        // If one of the fields is different: insert or update
        // If all changed fields have update = Y, update
        // If one of the changed fields has update = N, insert

        boolean insert = false;
        boolean identical = true;
        boolean punch = false;

        // Column lookup array - initialize to all -1
        if ( columnLookupArray == null ) {
          columnLookupArray = new int[meta.getFieldStream().length];
          for ( int i = 0; i < columnLookupArray.length; i++ ) {
            columnLookupArray[i] = -1;
          }
        }
        // Integer returnRowColNum = null;
        int returnRowColNum = -1;
        String findColumn = null;
        for ( int i = 0; i < meta.getFieldStream().length; i++ ) {
          if ( data.fieldnrs[i] >= 0 ) {
            // Only compare real fields, not last updated row, last version, etc
            //
            ValueMetaInterface v1 = data.outputRowMeta.getValueMeta( data.fieldnrs[i] );
            Object valueData1 = row[data.fieldnrs[i]];
            findColumn = meta.getFieldStream()[i];
            // find the returnRowMeta based on the field in the fieldLookup list
            ValueMetaInterface v2 = null;
            Object valueData2 = null;
            // Fix for PDI-8122
            // See if it's already been computed.
            returnRowColNum = columnLookupArray[i];
            if ( returnRowColNum == -1 ) {
              // It hasn't been found yet - search the list and make sure we're comparing
              // the right column to the right column.
              for ( int j = 2; j < data.returnRowMeta.size(); j++ ) { // starting at 2 because I know that 0 and 1 are
                                                                      // poked in by Kettle.
                v2 = data.returnRowMeta.getValueMeta( j );
                if ( ( v2.getName() != null ) && ( v2.getName().equalsIgnoreCase( findColumn ) ) ) { // is this the
                                                                                                     // right column?
                  columnLookupArray[i] = j; // yes - record the "j" into the columnLookupArray at [i] for the next time
                                            // through the loop
                  valueData2 = returnRow[j]; // get the valueData2 for comparison
                  break; // get outta here.
                } else {
                  // Reset to null because otherwise, we'll get a false finding at the end.
                  // This could be optimized to use a temporary variable to avoid the repeated set if necessary
                  // but it will never be as slow as the database lookup anyway
                  v2 = null;
                }
              }
            } else {
              // We have a value in the columnLookupArray - use the value stored there.
              v2 = data.returnRowMeta.getValueMeta( returnRowColNum );
              valueData2 = returnRow[returnRowColNum];
            }
            if ( v2 == null ) {
              // If we made it here, then maybe someone tweaked the XML in the transformation
              // and we're matching a stream column to a column that doesn't really exist. Throw an exception.
              throw new KettleStepException( BaseMessages.getString(
                PKG, "DimensionLookup.Exception.ErrorDetectedInComparingFields", meta.getFieldStream()[i] ) );
            }

            try {
              cmp = v1.compare( valueData1, v2, valueData2 );
            } catch ( ClassCastException e ) {
              throw e;
            }

            // Not the same and update = 'N' --> insert
            if ( cmp != 0 ) {
              identical = false;
            }

            // Field flagged for insert: insert
            if ( cmp != 0 && meta.getFieldUpdate()[i] == DimensionLookupMeta.TYPE_UPDATE_DIM_INSERT ) {
              insert = true;
            }

            // Field flagged for punchthrough
            if ( cmp != 0 && meta.getFieldUpdate()[i] == DimensionLookupMeta.TYPE_UPDATE_DIM_PUNCHTHROUGH ) {
              punch = true;
            }

            if ( isRowLevel() ) {
              logRowlevel( BaseMessages
                .getString(
                  PKG,
                  "DimensionLookup.Log.ComparingValues", "" + v1, "" + v2, String.valueOf( cmp ), String
                    .valueOf( identical ), String.valueOf( insert ), String.valueOf( punch ) ) );
            }
          }
        }

        // After comparing the record in the database and the data in the input
        // and taking into account the rules of the slowly changing dimension,
        // we found out whether or not to perform an insert or an update.
        //
        if ( !insert ) { // Just an update of row at key = valueKey
          if ( !identical ) {
            if ( isRowLevel() ) {
              logRowlevel( BaseMessages.getString( PKG, "DimensionLookup.Log.UpdateRowWithValues" )
                + data.inputRowMeta.getString( row ) );
            }
            /*
             * UPDATE d_customer SET fieldlookup[] = row.getValue(fieldnrs) WHERE returnkey = dimkey
             */
            dimUpdate( rowMeta, row, technicalKey, valueDate );
            incrementLinesUpdated();

            // We need to capture this change in the cache as well...
            if ( meta.getCacheSize() >= 0 ) {
              Object[] values =
                getCacheValues( rowMeta, row, technicalKey, valueVersion, valueDateFrom, valueDateTo );
              addToCache( lookupRow, values );
            }
          } else {
            if ( isRowLevel() ) {
              logRowlevel( BaseMessages.getString( PKG, "DimensionLookup.Log.SkipLine" ) );
            }
            // Don't do anything, everything is file in de dimension.
            incrementLinesSkipped();
          }
        } else {
          if ( isRowLevel() ) {
            logRowlevel( BaseMessages.getString( PKG, "DimensionLookup.Log.InsertNewVersion" )
              + technicalKey.toString() );
          }

          Long valueNewVersion = valueVersion + 1;
          // From date (valueDate) is calculated at the start of this method to
          // be either the system date or the value in a column
          //
          valueDateFrom = valueDate;
          valueDateTo = data.max_date;

          // First try to use an AUTOINCREMENT field
          if ( meta.getDatabaseMeta().supportsAutoinc() && isAutoIncrement() ) {
            technicalKey = null; // value to accept new key...
          } else if ( meta.getDatabaseMeta().supportsSequences()
            // Try to get the value by looking at a SEQUENCE (oracle mostly)
            && meta.getSequenceName() != null && meta.getSequenceName().length() > 0 ) {
            technicalKey =
              data.db.getNextSequenceValue( data.realSchemaName, meta.getSequenceName(), meta.getKeyField() );
            if ( technicalKey != null && isRowLevel() ) {
              logRowlevel( BaseMessages.getString( PKG, "DimensionLookup.Log.FoundNextSequence2" )
                + technicalKey.toString() );
            }
          } else {
            // Use our own sequence here...
            // What's the next value for the technical key?
            technicalKey =
              data.db.getNextValue( getTrans().getCounters(), data.realSchemaName, data.realTableName, meta
                .getKeyField() );
          }

          // update our technicalKey with the return of the insert
          technicalKey =
            dimInsert( rowMeta, row, technicalKey, false, valueNewVersion, valueDateFrom, valueDateTo );
          incrementLinesOutput();

          // We need to capture this change in the cache as well...
          if ( meta.getCacheSize() >= 0 ) {
            Object[] values =
              getCacheValues( rowMeta, row, technicalKey, valueNewVersion, valueDateFrom, valueDateTo );
            addToCache( lookupRow, values );
          }
        }
        if ( punch ) { // On of the fields we have to punch through has changed!
          /*
           * This means we have to update all versions:
           *
           * UPDATE dim SET punchf1 = val1, punchf2 = val2, ... WHERE fieldlookup[] = ? ;
           *
           * --> update ALL versions in the dimension table.
           */
          dimPunchThrough( rowMeta, row );
          incrementLinesUpdated();
        }

        returnRow = new Object[data.returnRowMeta.size()];
        returnRow[0] = technicalKey;
        if ( isRowLevel() ) {
          logRowlevel( BaseMessages.getString( PKG, "DimensionLookup.Log.TechnicalKey" ) + technicalKey );
        }
      }
    }

    if ( isRowLevel() ) {
      logRowlevel( BaseMessages.getString( PKG, "DimensionLookup.Log.AddValuesToRow" )
        + data.returnRowMeta.getString( returnRow ) );
    }

    // Copy the results to the output row...
    //
    // First copy the input row values to the output..
    //
    for ( int i = 0; i < rowMeta.size(); i++ ) {
      outputRow[i] = row[i];
    }

    int outputIndex = rowMeta.size();
    int inputIndex = 0;

    // Then the technical key...
    //
    if ( data.returnRowMeta.getValueMeta( 0 ).isBigNumber() && returnRow[0] instanceof Long ) {
      if ( isDebug() ) {
        log.logDebug( "Changing the type of the technical key from TYPE_BIGNUMBER to an TYPE_INTEGER" );
      }
      ValueMetaInterface tkValueMeta = data.returnRowMeta.getValueMeta( 0 );
      data.returnRowMeta.setValueMeta( 0, ValueMetaFactory.cloneValueMeta(
        tkValueMeta, ValueMetaInterface.TYPE_INTEGER ) );
    }

    outputRow[outputIndex++] = data.returnRowMeta.getInteger( returnRow, inputIndex++ );

    // skip the version in the input
    inputIndex++;

    // Then get the "extra fields"...
    // don't return date from-to fields, they can be returned when explicitly
    // specified in lookup fields.
    while ( inputIndex < returnRow.length && outputIndex < outputRow.length ) {
      outputRow[outputIndex] = returnRow[inputIndex];
      outputIndex++;
      inputIndex++;
    }

    // Finaly, check the date range!
    /*
     * TODO: WTF is this??? [May be it makes sense to keep the return date from-to fields within min/max range, but even
     * then the code below is wrong]. Value date; if (data.datefieldnr>=0) date = row.getValue(data.datefieldnr); else
     * date = new Value("date", new Date()); // system date
     *
     * if (data.min_date.compare(date)>0) data.min_date.setValue( date.getDate() ); if (data.max_date.compare(date)<0)
     * data.max_date.setValue( date.getDate() );
     */

    return outputRow;
  }

  /**
   * table: dimension table keys[]: which dim-fields do we use to look up key? retval: name of the key to return
   * datefield: do we have a datefield? datefrom, dateto: date-range, if any.
   */
  private void setDimLookup( RowMetaInterface rowMeta ) throws KettleDatabaseException {
    DatabaseMeta databaseMeta = meta.getDatabaseMeta();

    data.lookupRowMeta = new RowMeta();

    /*
     * DEFAULT, SYSDATE, START_TRANS, COLUMN_VALUE :
     *
     * SELECT <tk>, <version>, ... , FROM <table> WHERE key1=keys[1] AND key2=keys[2] ... AND <datefrom> <= <datefield>
     * AND <dateto> > <datefield> ;
     *
     * NULL :
     *
     * SELECT <tk>, <version>, ... , FROM <table> WHERE key1=keys[1] AND key2=keys[2] ... AND ( <datefrom> is null OR
     * <datefrom> <= <datefield> ) AND <dateto> >= <datefield>
     */
    String sql =
      "SELECT "
        + databaseMeta.quoteField( meta.getKeyField() ) + ", "
        + databaseMeta.quoteField( meta.getVersionField() );

    if ( !Utils.isEmpty( meta.getFieldLookup() ) ) {
      for ( int i = 0; i < meta.getFieldLookup().length; i++ ) {
        // Don't retrieve the fields without input
        if ( !Utils.isEmpty( meta.getFieldLookup()[i] )
          && !DimensionLookupMeta.isUpdateTypeWithoutArgument( meta.isUpdate(), meta.getFieldUpdate()[i] ) ) {
          sql += ", " + databaseMeta.quoteField( meta.getFieldLookup()[i] );

          if ( !Utils.isEmpty( meta.getFieldStream()[i] )
            && !meta.getFieldLookup()[i].equals( meta.getFieldStream()[i] ) ) {
            sql += " AS " + databaseMeta.quoteField( meta.getFieldStream()[i] );
          }
        }
      }
    }
    if ( meta.getCacheSize() >= 0 ) {
      sql +=
        ", " + databaseMeta.quoteField( meta.getDateFrom() ) + ", " + databaseMeta.quoteField( meta.getDateTo() );
    }

    sql += " FROM " + data.schemaTable + " WHERE ";

    for ( int i = 0; i < meta.getKeyLookup().length; i++ ) {
      if ( i != 0 ) {
        sql += " AND ";
      }
      sql += databaseMeta.quoteField( meta.getKeyLookup()[i] ) + " = ? ";
      data.lookupRowMeta.addValueMeta( rowMeta.getValueMeta( data.keynrs[i] ) );
    }

    String dateFromField = databaseMeta.quoteField( meta.getDateFrom() );
    String dateToField = databaseMeta.quoteField( meta.getDateTo() );

    if ( meta.isUsingStartDateAlternative()
      && ( meta.getStartDateAlternative() == DimensionLookupMeta.START_DATE_ALTERNATIVE_NULL )
      || ( meta.getStartDateAlternative() == DimensionLookupMeta.START_DATE_ALTERNATIVE_COLUMN_VALUE ) ) {
      // Null as a start date is possible...
      //
      sql += " AND ( " + dateFromField + " IS NULL OR " + dateFromField + " <= ? )" + Const.CR;
      sql += " AND " + dateToField + " > ?" + Const.CR;

      data.lookupRowMeta.addValueMeta( new ValueMetaDate( meta.getDateFrom() ) );
      data.lookupRowMeta.addValueMeta( new ValueMetaDate( meta.getDateTo() ) );
    } else {
      // Null as a start date is NOT possible
      //
      sql += " AND ? >= " + dateFromField + Const.CR;
      sql += " AND ? < " + dateToField + Const.CR;

      data.lookupRowMeta.addValueMeta( new ValueMetaDate( meta.getDateFrom() ) );
      data.lookupRowMeta.addValueMeta( new ValueMetaDate( meta.getDateTo() ) );
    }

    try {
      logDetailed( "Dimension Lookup setting preparedStatement to [" + sql + "]" );
      data.prepStatementLookup = data.db.getConnection().prepareStatement( databaseMeta.stripCR( sql ) );
      if ( databaseMeta.supportsSetMaxRows() ) {
        data.prepStatementLookup.setMaxRows( 1 ); // alywas get only 1 line back!
      }
      if ( databaseMeta.getDatabaseInterface().isMySQLVariant() ) {
        data.prepStatementLookup.setFetchSize( 0 ); // Make sure to DISABLE Streaming Result sets
      }
      logDetailed( "Finished preparing dimension lookup statement." );
    } catch ( SQLException ex ) {
      throw new KettleDatabaseException( "Unable to prepare dimension lookup", ex );
    }
  }

  protected boolean isAutoIncrement() {
    return techKeyCreation == CREATION_METHOD_AUTOINC;
  }

  /**
   * This inserts new record into dimension Optionally, if the entry already exists, update date range from previous
   * version of the entry.
   */
  public Long dimInsert( RowMetaInterface inputRowMeta, Object[] row, Long technicalKey, boolean newEntry,
    Long versionNr, Date dateFrom, Date dateTo ) throws KettleException {
    DatabaseMeta databaseMeta = meta.getDatabaseMeta();

    if ( data.prepStatementInsert == null && data.prepStatementUpdate == null ) { // first time: construct prepared statement
      RowMetaInterface insertRowMeta = new RowMeta();

      /*
       * Construct the SQL statement...
       *
       * INSERT INTO d_customer(keyfield, versionfield, datefrom, dateto, key[], fieldlookup[], last_updated,
       * last_inserted, last_version) VALUES (val_key ,val_version , val_datfrom, val_datto, keynrs[], fieldnrs[],
       * last_updated, last_inserted, last_version) ;
       */

      String sql = "INSERT INTO " + data.schemaTable + "( ";

      if ( !isAutoIncrement() ) {
        sql += databaseMeta.quoteField( meta.getKeyField() ) + ", "; // NO
                                                                     // AUTOINCREMENT
        insertRowMeta.addValueMeta( data.outputRowMeta.getValueMeta( inputRowMeta.size() ) ); // the first return value
                                                                                              // after the input
      } else {
        if ( databaseMeta.needsPlaceHolder() ) {
          sql += "0, "; // placeholder on informix!
        }
      }

      sql +=
        databaseMeta.quoteField( meta.getVersionField() )
          + ", " + databaseMeta.quoteField( meta.getDateFrom() ) + ", "
          + databaseMeta.quoteField( meta.getDateTo() );
      insertRowMeta.addValueMeta( new ValueMetaInteger( meta.getVersionField() ) );
      insertRowMeta.addValueMeta( new ValueMetaDate( meta.getDateFrom() ) );
      insertRowMeta.addValueMeta( new ValueMetaDate( meta.getDateTo() ) );

      for ( int i = 0; i < meta.getKeyLookup().length; i++ ) {
        sql += ", " + databaseMeta.quoteField( meta.getKeyLookup()[i] );
        insertRowMeta.addValueMeta( inputRowMeta.getValueMeta( data.keynrs[i] ) );
      }

      for ( int i = 0; i < meta.getFieldLookup().length; i++ ) {
        // Ignore last_version, last_updated etc, they are handled below (at the
        // back of the row).
        //
        if ( !DimensionLookupMeta.isUpdateTypeWithoutArgument( meta.isUpdate(), meta.getFieldUpdate()[i] ) ) {
          sql += ", " + databaseMeta.quoteField( meta.getFieldLookup()[i] );
          insertRowMeta.addValueMeta( inputRowMeta.getValueMeta( data.fieldnrs[i] ) );
        }
      }

      // Finally, the special update fields...
      //
      for ( int i = 0; i < meta.getFieldUpdate().length; i++ ) {
        ValueMetaInterface valueMeta = null;
        switch ( meta.getFieldUpdate()[i] ) {
          case DimensionLookupMeta.TYPE_UPDATE_DATE_INSUP:
          case DimensionLookupMeta.TYPE_UPDATE_DATE_INSERTED:
            valueMeta = new ValueMetaDate( meta.getFieldLookup()[i] );
            break;
          case DimensionLookupMeta.TYPE_UPDATE_LAST_VERSION:
            valueMeta = new ValueMetaBoolean( meta.getFieldLookup()[i] );
            break;
          default:
            break;
        }
        if ( valueMeta != null ) {
          sql += ", " + databaseMeta.quoteField( valueMeta.getName() );
          insertRowMeta.addValueMeta( valueMeta );
        }
      }

      sql += ") VALUES (";

      if ( !isAutoIncrement() ) {
        sql += "?, ";
      }
      sql += "?, ?, ?";

      for ( int i = 0; i < data.keynrs.length; i++ ) {
        sql += ", ?";
      }

      for ( int i = 0; i < meta.getFieldLookup().length; i++ ) {
        // Ignore last_version, last_updated, etc. These are handled below...
        //
        if ( !DimensionLookupMeta.isUpdateTypeWithoutArgument( meta.isUpdate(), meta.getFieldUpdate()[i] ) ) {
          sql += ", ?";
        }
      }

      // The special update fields...
      //
      for ( int i = 0; i < meta.getFieldUpdate().length; i++ ) {
        switch ( meta.getFieldUpdate()[i] ) {
          case DimensionLookupMeta.TYPE_UPDATE_DATE_INSUP:
          case DimensionLookupMeta.TYPE_UPDATE_DATE_INSERTED:
          case DimensionLookupMeta.TYPE_UPDATE_LAST_VERSION:
            sql += ", ?";
            break;
          default:
            break;
        }
      }

      sql += " )";

      try {
        if ( technicalKey == null && databaseMeta.supportsAutoGeneratedKeys() ) {
          logDetailed( "SQL w/ return keys=[" + sql + "]" );
          data.prepStatementInsert =
            data.db.getConnection().prepareStatement(
              databaseMeta.stripCR( sql ), Statement.RETURN_GENERATED_KEYS );
        } else {
          logDetailed( "SQL=[" + sql + "]" );
          data.prepStatementInsert = data.db.getConnection().prepareStatement( databaseMeta.stripCR( sql ) );
        }
        // pstmt=con.prepareStatement(sql, new String[] { "klant_tk" } );
      } catch ( SQLException ex ) {
        throw new KettleDatabaseException( "Unable to prepare dimension insert :" + Const.CR + sql, ex );
      }

      /*
       * UPDATE d_customer SET dateto = val_datnow, last_updated = <now> last_version = false WHERE keylookup[] =
       * keynrs[] AND versionfield = val_version - 1 ;
       */
      RowMetaInterface updateRowMeta = new RowMeta();

      String sql_upd = "UPDATE " + data.schemaTable + Const.CR;

      // The end of the date range
      //
      sql_upd += "SET " + databaseMeta.quoteField( meta.getDateTo() ) + " = ?" + Const.CR;
      updateRowMeta.addValueMeta( new ValueMetaDate( meta.getDateTo() ) );

      // The special update fields...
      //
      for ( int i = 0; i < meta.getFieldUpdate().length; i++ ) {
        ValueMetaInterface valueMeta = null;
        switch ( meta.getFieldUpdate()[i] ) {
          case DimensionLookupMeta.TYPE_UPDATE_DATE_INSUP:
          case DimensionLookupMeta.TYPE_UPDATE_DATE_UPDATED:
            valueMeta = new ValueMetaDate( meta.getFieldLookup()[i] );
            break;
          case DimensionLookupMeta.TYPE_UPDATE_LAST_VERSION:
            valueMeta = new ValueMetaBoolean( meta.getFieldLookup()[i] );
            break;
          default:
            break;
        }
        if ( valueMeta != null ) {
          sql_upd += ", " + databaseMeta.quoteField( valueMeta.getName() ) + " = ?" + Const.CR;
          updateRowMeta.addValueMeta( valueMeta );
        }
      }

      sql_upd += "WHERE ";
      for ( int i = 0; i < meta.getKeyLookup().length; i++ ) {
        if ( i > 0 ) {
          sql_upd += "AND   ";
        }
        sql_upd += databaseMeta.quoteField( meta.getKeyLookup()[i] ) + " = ?" + Const.CR;
        updateRowMeta.addValueMeta( inputRowMeta.getValueMeta( data.keynrs[i] ) );
      }
      sql_upd += "AND   " + databaseMeta.quoteField( meta.getVersionField() ) + " = ? ";
      updateRowMeta.addValueMeta( new ValueMetaInteger( meta.getVersionField() ) );

      try {
        logDetailed( "Preparing update: " + Const.CR + sql_upd + Const.CR );
        data.prepStatementUpdate = data.db.getConnection().prepareStatement( databaseMeta.stripCR( sql_upd ) );
      } catch ( SQLException ex ) {
        throw new KettleDatabaseException( "Unable to prepare dimension update :" + Const.CR + sql_upd, ex );
      }

      data.insertRowMeta = insertRowMeta;
      data.updateRowMeta = updateRowMeta;
    }

    Object[] insertRow = new Object[data.insertRowMeta.size()];
    int insertIndex = 0;
    if ( !isAutoIncrement() ) {
      insertRow[insertIndex++] = technicalKey;
    }

    // Caller is responsible for setting proper version number depending
    // on if newEntry == true
    insertRow[insertIndex++] = versionNr;

    switch ( data.startDateChoice ) {
      case DimensionLookupMeta.START_DATE_ALTERNATIVE_NONE:
        insertRow[insertIndex++] = dateFrom;
        break;
      case DimensionLookupMeta.START_DATE_ALTERNATIVE_SYSDATE:
        // use the time the step execution begins as the date from (passed in as dateFrom).
        // before, the current system time was used. this caused an exclusion of the row in the
        // lookup portion of the step that uses this 'valueDate' and not the current time.
        // the result was multiple inserts for what should have been 1 [PDI-4317]
        insertRow[insertIndex++] = dateFrom;
        break;
      case DimensionLookupMeta.START_DATE_ALTERNATIVE_START_OF_TRANS:
        insertRow[insertIndex++] = getTrans().getStartDate();
        break;
      case DimensionLookupMeta.START_DATE_ALTERNATIVE_NULL:
        insertRow[insertIndex++] = null;
        break;
      case DimensionLookupMeta.START_DATE_ALTERNATIVE_COLUMN_VALUE:
        insertRow[insertIndex++] = inputRowMeta.getDate( row, data.startDateFieldIndex );
        break;
      default:
        throw new KettleStepException( BaseMessages.getString(
          PKG, "DimensionLookup.Exception.IllegalStartDateSelection", Integer.toString( data.startDateChoice ) ) );
    }

    insertRow[insertIndex++] = dateTo;

    for ( int i = 0; i < data.keynrs.length; i++ ) {
      insertRow[insertIndex++] = row[data.keynrs[i]];
    }
    for ( int i = 0; i < data.fieldnrs.length; i++ ) {
      if ( data.fieldnrs[i] >= 0 ) {
        // Ignore last_version, last_updated, etc. These are handled below...
        //
        insertRow[insertIndex++] = row[data.fieldnrs[i]];
      }
    }
    // The special update fields...
    //
    for ( int i = 0; i < meta.getFieldUpdate().length; i++ ) {
      switch ( meta.getFieldUpdate()[i] ) {
        case DimensionLookupMeta.TYPE_UPDATE_DATE_INSUP:
        case DimensionLookupMeta.TYPE_UPDATE_DATE_INSERTED:
          insertRow[insertIndex++] = new Date();
          break;
        case DimensionLookupMeta.TYPE_UPDATE_LAST_VERSION:
          insertRow[insertIndex++] = Boolean.TRUE;
          break; // Always the last version on insert.
        default:
          break;
      }
    }

    if ( isDebug() ) {
      logDebug( "rins, size=" + data.insertRowMeta.size() + ", values=" + data.insertRowMeta.getString( insertRow ) );
    }

    // INSERT NEW VALUE!
    data.db.setValues( data.insertRowMeta, insertRow, data.prepStatementInsert );
    data.db.insertRow( data.prepStatementInsert );

    if ( isDebug() ) {
      logDebug( "Row inserted!" );
    }
    if ( technicalKey == null && databaseMeta.supportsAutoGeneratedKeys() ) {
      try {
        RowMetaAndData keys = data.db.getGeneratedKeys( data.prepStatementInsert );
        if ( keys.getRowMeta().size() > 0 ) {
          technicalKey = keys.getRowMeta().getInteger( keys.getData(), 0 );
        } else {
          throw new KettleDatabaseException(
            "Unable to retrieve value of auto-generated technical key : no value found!" );
        }
      } catch ( Exception e ) {
        throw new KettleDatabaseException(
          "Unable to retrieve value of auto-generated technical key : unexpected error: ", e );
      }
    }

    if ( !newEntry ) { // we have to update the previous version in the dimension!
      /*
       * UPDATE d_customer SET dateto = val_datfrom , last_updated = <now> , last_version = false WHERE keylookup[] =
       * keynrs[] AND versionfield = val_version - 1 ;
       */
      Object[] updateRow = new Object[data.updateRowMeta.size()];
      int updateIndex = 0;

      switch ( data.startDateChoice ) {
        case DimensionLookupMeta.START_DATE_ALTERNATIVE_NONE:
          updateRow[updateIndex++] = dateFrom;
          break;
        case DimensionLookupMeta.START_DATE_ALTERNATIVE_SYSDATE:
          updateRow[updateIndex++] = new Date();
          break;
        case DimensionLookupMeta.START_DATE_ALTERNATIVE_START_OF_TRANS:
          updateRow[updateIndex++] = getTrans().getCurrentDate();
          break;
        case DimensionLookupMeta.START_DATE_ALTERNATIVE_NULL:
          updateRow[updateIndex++] = null;
          break;
        case DimensionLookupMeta.START_DATE_ALTERNATIVE_COLUMN_VALUE:
          updateRow[updateIndex++] = inputRowMeta.getDate( row, data.startDateFieldIndex );
          break;
        default:
          throw new KettleStepException( BaseMessages.getString(
            "DimensionLookup.Exception.IllegalStartDateSelection", Integer.toString( data.startDateChoice ) ) );
      }

      // The special update fields...
      //
      for ( int i = 0; i < meta.getFieldUpdate().length; i++ ) {
        switch ( meta.getFieldUpdate()[i] ) {
          case DimensionLookupMeta.TYPE_UPDATE_DATE_INSUP:
            updateRow[updateIndex++] = new Date();
            break;
          case DimensionLookupMeta.TYPE_UPDATE_LAST_VERSION:
            updateRow[updateIndex++] = Boolean.FALSE;
            break; // Never the last version on this update
          case DimensionLookupMeta.TYPE_UPDATE_DATE_UPDATED:
            updateRow[updateIndex++] = new Date();
            break;
          default:
            break;
        }
      }

      for ( int i = 0; i < data.keynrs.length; i++ ) {
        updateRow[updateIndex++] = row[data.keynrs[i]];
      }

      updateRow[updateIndex++] = versionNr - 1;

      if ( isRowLevel() ) {
        logRowlevel( "UPDATE using rupd=" + data.updateRowMeta.getString( updateRow ) );
      }

      // UPDATE VALUES

      // set values for update
      //
      data.db.setValues( data.updateRowMeta, updateRow, data.prepStatementUpdate );
      if ( isDebug() ) {
        logDebug( "Values set for update (" + data.updateRowMeta.size() + ")" );
      }
      data.db.insertRow( data.prepStatementUpdate ); // do the actual update
      if ( isDebug() ) {
        logDebug( "Row updated!" );
      }
    }

    return technicalKey;
  }

  @Override
  public boolean isRowLevel() {
    return log.isRowLevel();
  }

  @Override
  public boolean isDebug() {
    return log.isDebug();
  }

  public void dimUpdate( RowMetaInterface rowMeta, Object[] row, Long dimkey, Date valueDate ) throws KettleDatabaseException {
    if ( data.prepStatementDimensionUpdate == null ) {
      // first time: construct prepared statement
      //
      data.dimensionUpdateRowMeta = new RowMeta();

      // Construct the SQL statement...
      /*
       * UPDATE d_customer SET fieldlookup[] = row.getValue(fieldnrs) , last_updated = <now> WHERE returnkey = dimkey ;
       */

      String sql = "UPDATE " + data.schemaTable + Const.CR + "SET ";
      boolean comma = false;
      for ( int i = 0; i < meta.getFieldLookup().length; i++ ) {
        if ( !DimensionLookupMeta.isUpdateTypeWithoutArgument( meta.isUpdate(), meta.getFieldUpdate()[i] ) ) {
          if ( comma ) {
            sql += ", ";
          } else {
            sql += "  ";
          }
          comma = true;
          sql += meta.getDatabaseMeta().quoteField( meta.getFieldLookup()[i] ) + " = ?" + Const.CR;
          data.dimensionUpdateRowMeta.addValueMeta( rowMeta.getValueMeta( data.fieldnrs[i] ) );
        }
      }

      // The special update fields...
      //
      for ( int i = 0; i < meta.getFieldUpdate().length; i++ ) {
        ValueMetaInterface valueMeta = null;
        switch ( meta.getFieldUpdate()[i] ) {
          case DimensionLookupMeta.TYPE_UPDATE_DATE_INSUP:
          case DimensionLookupMeta.TYPE_UPDATE_DATE_UPDATED:
            valueMeta = new ValueMetaDate( meta.getFieldLookup()[i] );
            break;
          default:
            break;
        }
        if ( valueMeta != null ) {
          if ( comma ) {
            sql += ", ";
          } else {
            sql += "  ";
          }
          comma = true;
          sql += meta.getDatabaseMeta().quoteField( valueMeta.getName() ) + " = ?" + Const.CR;
          data.dimensionUpdateRowMeta.addValueMeta( valueMeta );
        }
      }

      sql += "WHERE  " + meta.getDatabaseMeta().quoteField( meta.getKeyField() ) + " = ?";
      data.dimensionUpdateRowMeta
        .addValueMeta( new ValueMetaInteger( meta.getKeyField() ) ); // The tk

      try {
        if ( isDebug() ) {
          logDebug( "Preparing statement: [" + sql + "]" );
        }
        data.prepStatementDimensionUpdate =
          data.db.getConnection().prepareStatement( meta.getDatabaseMeta().stripCR( sql ) );
      } catch ( SQLException ex ) {
        throw new KettleDatabaseException( "Couldn't prepare statement :" + Const.CR + sql, ex );
      }
    }

    // Assemble information
    // New
    Object[] dimensionUpdateRow = new Object[data.dimensionUpdateRowMeta.size()];
    int updateIndex = 0;
    for ( int i = 0; i < data.fieldnrs.length; i++ ) {
      // Ignore last_version, last_updated, etc. These are handled below...
      //
      if ( data.fieldnrs[i] >= 0 ) {
        dimensionUpdateRow[updateIndex++] = row[data.fieldnrs[i]];
      }
    }
    for ( int i = 0; i < meta.getFieldUpdate().length; i++ ) {
      switch ( meta.getFieldUpdate()[i] ) {
        case DimensionLookupMeta.TYPE_UPDATE_DATE_INSUP:
        case DimensionLookupMeta.TYPE_UPDATE_DATE_UPDATED:
          dimensionUpdateRow[updateIndex++] = valueDate;
          break;
        default:
          break;
      }
    }
    dimensionUpdateRow[updateIndex++] = dimkey;

    data.db.setValues( data.dimensionUpdateRowMeta, dimensionUpdateRow, data.prepStatementDimensionUpdate );
    data.db.insertRow( data.prepStatementDimensionUpdate );
  }

  // This updates all versions of a dimension entry.
  //
  public void dimPunchThrough( RowMetaInterface rowMeta, Object[] row ) throws KettleDatabaseException {
    if ( data.prepStatementPunchThrough == null ) { // first time: construct prepared statement
      DatabaseMeta databaseMeta = meta.getDatabaseMeta();
      data.punchThroughRowMeta = new RowMeta();

      /*
       * UPDATE table SET punchv1 = fieldx, ... , last_updated = <now> WHERE keylookup[] = keynrs[] ;
       */

      String sql_upd = "UPDATE " + data.schemaTable + Const.CR;
      sql_upd += "SET ";
      boolean first = true;
      for ( int i = 0; i < meta.getFieldLookup().length; i++ ) {
        if ( meta.getFieldUpdate()[i] == DimensionLookupMeta.TYPE_UPDATE_DIM_PUNCHTHROUGH ) {
          if ( !first ) {
            sql_upd += ", ";
          } else {
            sql_upd += "  ";
          }
          first = false;
          sql_upd += databaseMeta.quoteField( meta.getFieldLookup()[i] ) + " = ?" + Const.CR;
          data.punchThroughRowMeta.addValueMeta( rowMeta.getValueMeta( data.fieldnrs[i] ) );
        }
      }
      // The special update fields...
      //
      for ( int i = 0; i < meta.getFieldUpdate().length; i++ ) {
        ValueMetaInterface valueMeta = null;
        switch ( meta.getFieldUpdate()[i] ) {
          case DimensionLookupMeta.TYPE_UPDATE_DATE_INSUP:
          case DimensionLookupMeta.TYPE_UPDATE_DATE_UPDATED:
            valueMeta = new ValueMetaDate( meta.getFieldLookup()[i] );
            break;
          default:
            break;
        }
        if ( valueMeta != null ) {
          sql_upd += ", " + databaseMeta.quoteField( valueMeta.getName() ) + " = ?" + Const.CR;
          data.punchThroughRowMeta.addValueMeta( valueMeta );
        }
      }

      sql_upd += "WHERE ";
      for ( int i = 0; i < meta.getKeyLookup().length; i++ ) {
        if ( i > 0 ) {
          sql_upd += "AND   ";
        }
        sql_upd += databaseMeta.quoteField( meta.getKeyLookup()[i] ) + " = ?" + Const.CR;
        data.punchThroughRowMeta.addValueMeta( rowMeta.getValueMeta( data.keynrs[i] ) );
      }

      try {
        data.prepStatementPunchThrough =
          data.db.getConnection().prepareStatement( meta.getDatabaseMeta().stripCR( sql_upd ) );
      } catch ( SQLException ex ) {
        throw new KettleDatabaseException( "Unable to prepare dimension punchThrough update statement : "
          + Const.CR + sql_upd, ex );
      }
    }

    Object[] punchThroughRow = new Object[data.punchThroughRowMeta.size()];
    int punchIndex = 0;

    for ( int i = 0; i < meta.getFieldLookup().length; i++ ) {
      if ( meta.getFieldUpdate()[i] == DimensionLookupMeta.TYPE_UPDATE_DIM_PUNCHTHROUGH ) {
        punchThroughRow[punchIndex++] = row[data.fieldnrs[i]];
      }
    }
    for ( int i = 0; i < meta.getFieldUpdate().length; i++ ) {
      switch ( meta.getFieldUpdate()[i] ) {
        case DimensionLookupMeta.TYPE_UPDATE_DATE_INSUP:
        case DimensionLookupMeta.TYPE_UPDATE_DATE_UPDATED:
          punchThroughRow[punchIndex++] = new Date();
          break;
        default:
          break;
      }
    }
    for ( int i = 0; i < data.keynrs.length; i++ ) {
      punchThroughRow[punchIndex++] = row[data.keynrs[i]];
    }

    // UPDATE VALUES
    data.db.setValues( data.punchThroughRowMeta, punchThroughRow, data.prepStatementPunchThrough ); // set values for
                                                                                                    // update
    data.db.insertRow( data.prepStatementPunchThrough ); // do the actual punch through update
  }

  /**
   * Keys: - natural key fields Values: - Technical key - lookup fields / extra fields (allows us to compare or
   * retrieve) - Date_from - Date_to
   *
   * @param row
   *          The input row
   * @param technicalKey
   *          the technical key value
   * @param valueDateFrom
   *          the start of valid date range
   * @param valueDateTo
   *          the end of the valid date range
   * @return the values to store in the cache as a row.
   */
  private Object[] getCacheValues( RowMetaInterface rowMeta, Object[] row, Long technicalKey, Long valueVersion,
    Date valueDateFrom, Date valueDateTo ) {
    if ( data.cacheValueRowMeta == null ) {
      return null; // nothing is in the cache.
    }

    Object[] cacheValues = new Object[data.cacheValueRowMeta.size()];
    int cacheIndex = 0;

    cacheValues[cacheIndex++] = technicalKey;

    cacheValues[cacheIndex++] = valueVersion;

    for ( int i = 0; i < data.fieldnrs.length; i++ ) {
      // Ignore last_version, last_updated, etc. These are handled below...
      //
      if ( data.fieldnrs[i] >= 0 ) {
        cacheValues[cacheIndex++] = row[data.fieldnrs[i]];
      }
    }

    cacheValues[cacheIndex++] = valueDateFrom;

    cacheValues[cacheIndex++] = valueDateTo;

    return cacheValues;
  }

  /**
   * Adds a row to the cache In case we are doing updates, we need to store the complete rows from the database. These
   * are the values we need to store
   *
   * Key: - natural key fields Value: - Technical key - lookup fields / extra fields (allows us to compare or retrieve)
   * - Date_from - Date_to
   *
   * @param keyValues
   * @param returnValues
   * @throws KettleValueException
   */
  private void addToCache( Object[] keyValues, Object[] returnValues ) throws KettleValueException {
    if ( data.cacheValueRowMeta == null ) {
      data.cacheValueRowMeta = assembleCacheValueRowMeta();
    }

    // store it in the cache if needed.
    byte[] keyPart = RowMeta.extractData( data.cacheKeyRowMeta, keyValues );
    byte[] valuePart = RowMeta.extractData( data.cacheValueRowMeta, returnValues );
    data.cache.put( keyPart, valuePart );

    // check if the size is not too big...
    // Allow for a buffer overrun of 20% and then remove those 20% in one go.
    // Just to keep performance in track.
    //
    int tenPercent = meta.getCacheSize() / 10;
    if ( meta.getCacheSize() > 0 && data.cache.size() > meta.getCacheSize() + tenPercent ) {
      // Which cache entries do we delete here?
      // We delete those with the lowest technical key...
      // Those would arguably be the "oldest" dimension entries.
      // Oh well... Nothing is going to be perfect here...
      //
      // Getting the lowest 20% requires some kind of sorting algorithm and I'm not sure we want to do that.
      // Sorting is slow and even in the best case situation we need to do 2 passes over the cache entries...
      //
      // Perhaps we should get 20% random values and delete everything below the lowest but one TK.
      //
      List<byte[]> keys = data.cache.getKeys();
      int sizeBefore = keys.size();
      List<Long> samples = new ArrayList<Long>();

      // Take 10 sample technical keys....
      int stepsize = keys.size() / 5;
      if ( stepsize < 1 ) {
        stepsize = 1; // make shure we have no endless loop
      }
      for ( int i = 0; i < keys.size(); i += stepsize ) {
        byte[] key = keys.get( i );
        byte[] value = data.cache.get( key );
        if ( value != null ) {
          Object[] values = RowMeta.getRow( data.cacheValueRowMeta, value );
          Long tk = data.cacheValueRowMeta.getInteger( values, 0 );
          samples.add( tk );
        }
      }
      // Sort these 5 elements...
      Collections.sort( samples );

      // What is the smallest?
      // Take the second, not the fist in the list, otherwise we would be removing a single entry = not good.
      if ( samples.size() > 1 ) {
        data.smallestCacheKey = samples.get( 1 );
      } else if ( !samples.isEmpty() ) { // except when there is only one sample
        data.smallestCacheKey = samples.get( 0 );
      } else {
        // If no samples found nothing to remove, we're done
        return;
      }

      // Remove anything in the cache <= smallest.
      // This makes it almost single pass...
      // This algorithm is not 100% correct, but I guess it beats sorting the whole cache all the time.
      //
      for ( int i = 0; i < keys.size(); i++ ) {
        byte[] key = keys.get( i );
        byte[] value = data.cache.get( key );
        if ( value != null ) {
          Object[] values = RowMeta.getRow( data.cacheValueRowMeta, value );
          long tk = data.cacheValueRowMeta.getInteger( values, 0 ).longValue();
          if ( tk <= data.smallestCacheKey ) {
            data.cache.remove( key ); // this one has to go.
          }
        }
      }

      int sizeAfter = data.cache.size();
      logDetailed( "Reduced the lookup cache from " + sizeBefore + " to " + sizeAfter + " rows." );
    }

    if ( isRowLevel() ) {
      logRowlevel( "Cache store: key=" + keyValues + "    values=" + returnValues );
    }
  }

  /**
   * @return the cache value row metadata. The items that are cached is basically the return row metadata:<br>
   *         - Technical key (Integer) - Version (Integer) -
   */
  private RowMetaInterface assembleCacheValueRowMeta() {
    RowMetaInterface cacheRowMeta = data.returnRowMeta.clone();
    // The technical key and version are always an Integer...
    //
    /*
     * cacheRowMeta.getValueMeta(0).setType(ValueMetaInterface.TYPE_INTEGER);
     * cacheRowMeta.getValueMeta(1).setType(ValueMetaInterface.TYPE_INTEGER);
     */
    return cacheRowMeta;
  }

  private Object[] getFromCache( Object[] keyValues, Date dateValue ) throws KettleValueException {
    if ( data.cacheValueRowMeta == null ) {
      // nothing in the cache yet, no lookup was ever performed
      if ( data.returnRowMeta == null ) {
        return null;
      }

      data.cacheValueRowMeta = assembleCacheValueRowMeta();
    }

    byte[] key = RowMeta.extractData( data.cacheKeyRowMeta, keyValues );
    byte[] value = data.cache.get( key );
    if ( value != null ) {
      Object[] row = RowMeta.getRow( data.cacheValueRowMeta, value );

      // See if the dateValue is between the from and to date ranges...
      // The last 2 values are from and to
      long time = dateValue.getTime();
      long from = ( (Date) row[row.length - 2] ).getTime();
      long to = ( (Date) row[row.length - 1] ).getTime();
      if ( time >= from && time < to ) { // sanity check to see if we have the right version
        if ( isRowLevel() ) {
          logRowlevel( "Cache hit: key="
            + data.cacheKeyRowMeta.getString( keyValues ) + "  values=" + data.cacheValueRowMeta.getString( row ) );
        }
        return row;
      }
    }
    return null;
  }

  public void checkDimZero() throws KettleException {
    // Don't insert anything when running in lookup mode.
    //
    if ( !meta.isUpdate() ) {
      return;
    }

    DatabaseMeta databaseMeta = meta.getDatabaseMeta();
    int start_tk = databaseMeta.getNotFoundTK( isAutoIncrement() );

    if ( meta.isAutoIncrement() ) {
      // See if there are rows in the table
      // If so, we can't insert the unknown row anymore...
      //
      String sql =
        "SELECT count(*) FROM "
          + data.schemaTable + " WHERE " + databaseMeta.quoteField( meta.getKeyField() ) + " = " + start_tk;
      RowMetaAndData r = data.db.getOneRow( sql );
      Long count = r.getRowMeta().getInteger( r.getData(), 0 );
      if ( count.longValue() != 0 ) {
        return; // Can't insert below the rows already in there...
      }
    }

    String sql =
      "SELECT count(*) FROM "
        + data.schemaTable + " WHERE " + databaseMeta.quoteField( meta.getKeyField() ) + " = " + start_tk;
    RowMetaAndData r = data.db.getOneRow( sql );
    Long count = r.getRowMeta().getInteger( r.getData(), 0 );
    if ( count.longValue() == 0 ) {
      String isql = null;
      try {
        if ( !databaseMeta.supportsAutoinc() || !isAutoIncrement() ) {
          isql =
            "insert into "
              + data.schemaTable + "(" + databaseMeta.quoteField( meta.getKeyField() ) + ", "
              + databaseMeta.quoteField( meta.getVersionField() ) + ") values (0, 1)";
        } else {
          isql =
            databaseMeta.getSQLInsertAutoIncUnknownDimensionRow( data.schemaTable, databaseMeta.quoteField( meta
              .getKeyField() ), databaseMeta.quoteField( meta.getVersionField() ) );
        }

        data.db.execStatement( databaseMeta.stripCR( isql ) );
      } catch ( KettleException e ) {
        throw new KettleDatabaseException( "Error inserting 'unknown' row in dimension ["
          + data.schemaTable + "] : " + isql, e );
      }
    }
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (DimensionLookupMeta) smi;
    data = (DimensionLookupData) sdi;

    if ( super.init( smi, sdi ) ) {
      meta.actualizeWithInjectedValues();
      data.min_date = meta.getMinDate();
      data.max_date = meta.getMaxDate();

      data.realSchemaName = environmentSubstitute( meta.getSchemaName() );
      data.realTableName = environmentSubstitute( meta.getTableName() );

      data.startDateChoice = DimensionLookupMeta.START_DATE_ALTERNATIVE_NONE;
      if ( meta.isUsingStartDateAlternative() ) {
        data.startDateChoice = meta.getStartDateAlternative();
      }
      if ( meta.getDatabaseMeta() == null ) {
        logError( BaseMessages.getString( PKG, "DimensionLookup.Init.ConnectionMissing", getStepname() ) );
        return false;
      }
      data.db = new Database( this, meta.getDatabaseMeta() );
      data.db.shareVariablesWith( this );
      try {
        if ( getTransMeta().isUsingUniqueConnections() ) {
          synchronized ( getTrans() ) {
            data.db.connect( getTrans().getTransactionId(), getPartitionID() );
          }
        } else {
          data.db.connect( getPartitionID() );
        }

        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "DimensionLookup.Log.ConnectedToDB" ) );
        }
        data.db.setCommit( meta.getCommitSize() );

        return true;
      } catch ( KettleException ke ) {
        logError( BaseMessages.getString( PKG, "DimensionLookup.Log.ErrorOccurredInProcessing" ) + ke.getMessage() );
      }
    }
    return false;
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (DimensionLookupMeta) smi;
    data = (DimensionLookupData) sdi;
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
        logError( BaseMessages.getString( PKG, "DimensionLookup.Log.ErrorOccurredInProcessing" ) + e.getMessage() );
      } finally {
        data.db.disconnect();
      }
    }
    super.dispose( smi, sdi );
  }
}
