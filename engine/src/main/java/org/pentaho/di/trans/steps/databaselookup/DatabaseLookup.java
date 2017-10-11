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

package org.pentaho.di.trans.steps.databaselookup;

import java.util.Arrays;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.databaselookup.readallcache.ReadAllCache;

import com.google.common.annotations.VisibleForTesting;

/**
 * Looks up values in a database using keys from input streams.
 *
 * @author Matt
 * @since 26-apr-2003
 */
public class DatabaseLookup extends BaseStep implements StepInterface {
  private static Class<?> PKG = DatabaseLookupMeta.class; // for i18n purposes, needed by Translator2!!

  private DatabaseLookupMeta meta;
  private DatabaseLookupData data;

  public DatabaseLookup( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                         Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  /**
   * Performs the lookup based on the meta-data and the input row.
   *
   * @param row The row to use as lookup data and the row to add the returned lookup fields to
   * @return the resulting row after the lookup values where added
   * @throws KettleException In case something goes wrong.
   */
  @VisibleForTesting
  synchronized Object[] lookupValues( RowMetaInterface inputRowMeta, Object[] row ) throws KettleException {
    Object[] outputRow = RowDataUtil.resizeArray( row, data.outputRowMeta.size() );

    Object[] lookupRow = new Object[ data.lookupMeta.size() ];
    int lookupIndex = 0;

    for ( int i = 0; i < meta.getStreamKeyField1().length; i++ ) {
      if ( data.keynrs[ i ] >= 0 ) {
        ValueMetaInterface input = inputRowMeta.getValueMeta( data.keynrs[ i ] );
        ValueMetaInterface value = data.lookupMeta.getValueMeta( lookupIndex );
        lookupRow[ lookupIndex ] = row[ data.keynrs[ i ] ];

        // Try to convert type if needed
        if ( input.getType() != value.getType()
          || ValueMetaInterface.STORAGE_TYPE_BINARY_STRING == input.getStorageType() ) {
          lookupRow[ lookupIndex ] = value.convertData( input, lookupRow[ lookupIndex ] );
          value.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
        }
        lookupIndex++;
      }
      if ( data.keynrs2[ i ] >= 0 ) {
        ValueMetaInterface input = inputRowMeta.getValueMeta( data.keynrs2[ i ] );
        ValueMetaInterface value = data.lookupMeta.getValueMeta( lookupIndex );
        lookupRow[ lookupIndex ] = row[ data.keynrs2[ i ] ];

        // Try to convert type if needed
        if ( input.getType() != value.getType()
          || ValueMetaInterface.STORAGE_TYPE_BINARY_STRING == input.getStorageType() ) {
          lookupRow[ lookupIndex ] = value.convertData( input, lookupRow[ lookupIndex ] );
          value.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
        }
        lookupIndex++;
      }
    }

    Object[] add;
    boolean cache_now = false;
    boolean cacheHit = false;

    // First, check if we looked up before
    if ( meta.isCached() ) {
      add = data.cache.getRowFromCache( data.lookupMeta, lookupRow );
      if ( add != null ) {
        cacheHit = true;
      }
    } else {
      add = null;
    }

    if ( add == null ) {
      if ( !( meta.isCached() && meta.isLoadingAllDataInCache() ) || data.hasDBCondition ) { // do not go to the
        // database when all rows
        // are in (exception LIKE
        // operator)
        if ( log.isRowLevel() ) {
          logRowlevel( BaseMessages.getString( PKG, "DatabaseLookup.Log.AddedValuesToLookupRow1" )
            + meta.getStreamKeyField1().length
            + BaseMessages.getString( PKG, "DatabaseLookup.Log.AddedValuesToLookupRow2" )
            + data.lookupMeta.getString( lookupRow ) );
        }

        data.db.setValuesLookup( data.lookupMeta, lookupRow );
        add = data.db.getLookup( meta.isFailingOnMultipleResults() );
        cache_now = true;
      }
    }

    if ( add == null ) { // nothing was found, unknown code: add default values
      if ( meta.isEatingRowOnLookupFailure() ) {
        return null;
      }
      if ( getStepMeta().isDoingErrorHandling() ) {
        putError( getInputRowMeta(), row, 1L, "No lookup found", null, "DBL001" );

        // return false else we would still be processed.
        return null;
      }

      if ( log.isRowLevel() ) {
        logRowlevel( BaseMessages.getString( PKG, "DatabaseLookup.Log.NoResultsFoundAfterLookup" ) );
      }

      add = new Object[ data.returnMeta.size() ];
      for ( int i = 0; i < meta.getReturnValueField().length; i++ ) {
        if ( data.nullif[ i ] != null ) {
          add[ i ] = data.nullif[ i ];
        } else {
          add[ i ] = null;
        }
      }
    } else {
      if ( log.isRowLevel() ) {
        logRowlevel( BaseMessages.getString( PKG, "DatabaseLookup.Log.FoundResultsAfterLookup" )
          + Arrays.toString( add ) );
      }

      // Only verify the data types if the data comes from the DB, NOT when we have a cache hit
      // In that case, we already know the data type is OK.
      if ( !cacheHit ) {
        incrementLinesInput();

        int[] types = meta.getReturnValueDefaultType();

        // The assumption here is that the types are in the same order
        // as the returned lookup row, but since we make the lookup row
        // that should not be a problem.
        //
        for ( int i = 0; i < types.length; i++ ) {
          ValueMetaInterface returned = data.db.getReturnRowMeta().getValueMeta( i );
          ValueMetaInterface expected = data.returnMeta.getValueMeta( i );

          if ( returned != null && types[ i ] > 0 && types[ i ] != returned.getType() ) {
            // Set the type to the default return type
            add[ i ] = expected.convertData( returned, add[ i ] );
          }
        }
      }
    }

    // Store in cache if we need to!
    // If we already loaded all data into the cache, storing more makes no sense.
    //
    if ( meta.isCached() && cache_now && !meta.isLoadingAllDataInCache() && data.allEquals ) {
      data.cache.storeRowInCache( meta, data.lookupMeta, lookupRow, add );
    }

    for ( int i = 0; i < data.returnMeta.size(); i++ ) {
      outputRow[ inputRowMeta.size() + i ] = add[ i ];
    }

    return outputRow;
  }

  // visible for testing purposes
  void determineFieldsTypesQueryingDb() throws KettleException {
    final String[] keyFields = meta.getTableKeyField();
    data.keytypes = new int[ keyFields.length ];

    String schemaTable =
      meta.getDatabaseMeta().getQuotedSchemaTableCombination(
        environmentSubstitute( meta.getSchemaName() ), environmentSubstitute( meta.getTablename() ) );

    RowMetaInterface fields = data.db.getTableFields( schemaTable );
    if ( fields != null ) {
      // Fill in the types...
      for ( int i = 0; i < keyFields.length; i++ ) {
        ValueMetaInterface key = fields.searchValueMeta( keyFields[ i ] );
        if ( key != null ) {
          data.keytypes[ i ] = key.getType();
        } else {
          throw new KettleStepException( BaseMessages.getString(
            PKG, "DatabaseLookup.ERROR0001.FieldRequired5.Exception" )
            + keyFields[ i ]
            + BaseMessages.getString( PKG, "DatabaseLookup.ERROR0001.FieldRequired6.Exception" ) );
        }
      }

      final String[] returnFields = meta.getReturnValueField();
      final int returnFieldsOffset = getInputRowMeta().size();
      for ( int i = 0; i < returnFields.length; i++ ) {
        ValueMetaInterface returnValueMeta = fields.searchValueMeta( returnFields[ i ] );
        if ( returnValueMeta != null ) {
          ValueMetaInterface v = data.outputRowMeta.getValueMeta( returnFieldsOffset + i );
          if ( v.getType() != returnValueMeta.getType() ) {
            ValueMetaInterface clone = returnValueMeta.clone();
            clone.setName( v.getName() );
            data.outputRowMeta.setValueMeta( returnFieldsOffset + i, clone );
          }
        }
      }
    } else {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "DatabaseLookup.ERROR0002.UnableToDetermineFieldsOfTable" )
        + schemaTable + "]" );
    }
  }

  private void initNullIf() throws KettleException {
    final String[] returnFields = meta.getReturnValueField();

    data.nullif = new Object[ returnFields.length ];

    for ( int i = 0; i < returnFields.length; i++ ) {
      if ( !Utils.isEmpty( meta.getReturnValueDefault()[ i ] ) ) {
        ValueMetaInterface stringMeta = new ValueMetaString( "string" );
        ValueMetaInterface returnMeta = data.outputRowMeta.getValueMeta( i + getInputRowMeta().size() );
        data.nullif[ i ] = returnMeta.convertData( stringMeta, meta.getReturnValueDefault()[ i ] );
      } else {
        data.nullif[ i ] = null;
      }
    }
  }

  private void initLookupMeta() throws KettleException {
    // Count the number of values in the lookup as well as the metadata to send along with it.
    //
    data.lookupMeta = new RowMeta();

    for ( int i = 0; i < meta.getStreamKeyField1().length; i++ ) {
      if ( data.keynrs[ i ] >= 0 ) {
        ValueMetaInterface inputValueMeta = getInputRowMeta().getValueMeta( data.keynrs[ i ] );

        // Try to convert type if needed in a clone, we don't want to
        // change the type in the original row
        //
        ValueMetaInterface value = ValueMetaFactory.cloneValueMeta( inputValueMeta, data.keytypes[ i ] );

        data.lookupMeta.addValueMeta( value );
      }
      if ( data.keynrs2[ i ] >= 0 ) {
        ValueMetaInterface inputValueMeta = getInputRowMeta().getValueMeta( data.keynrs2[ i ] );

        // Try to convert type if needed in a clone, we don't want to
        // change the type in the original row
        //
        ValueMetaInterface value = ValueMetaFactory.cloneValueMeta( inputValueMeta, data.keytypes[ i ] );

        data.lookupMeta.addValueMeta( value );
      }
    }
  }

  private void initReturnMeta() {
    // We also want to know the metadata of the return values beforehand (null handling)
    data.returnMeta = new RowMeta();

    for ( int i = 0; i < meta.getReturnValueField().length; i++ ) {
      ValueMetaInterface v = data.outputRowMeta.getValueMeta( getInputRowMeta().size() + i ).clone();
      data.returnMeta.addValueMeta( v );
    }
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    Object[] r = getRow(); // Get row from input rowset & set row busy!
    if ( r == null ) { // no more input to be expected...
      setOutputDone();
      return false;
    }

    meta = (DatabaseLookupMeta) smi;
    data = (DatabaseLookupData) sdi;

    if ( first ) {
      first = false;

      // create the output metadata
      data.outputRowMeta = getInputRowMeta().clone();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );

      data.db.setLookup(
        environmentSubstitute( meta.getSchemaName() ), environmentSubstitute( meta.getTablename() ),
        meta.getTableKeyField(), meta.getKeyCondition(), meta.getReturnValueField(),
        meta.getReturnValueNewName(), meta.getOrderByClause(), meta.isFailingOnMultipleResults()
      );

      // lookup the values!
      if ( log.isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "DatabaseLookup.Log.CheckingRow" )
          + getInputRowMeta().getString( r ) );
      }

      data.keynrs = new int[ meta.getStreamKeyField1().length ];
      data.keynrs2 = new int[ meta.getStreamKeyField1().length ];

      for ( int i = 0; i < meta.getStreamKeyField1().length; i++ ) {
        data.keynrs[ i ] = getInputRowMeta().indexOfValue( meta.getStreamKeyField1()[ i ] );
        if ( data.keynrs[ i ] < 0 && // couldn't find field!
          !"IS NULL".equalsIgnoreCase( meta.getKeyCondition()[ i ] ) && // No field needed!
          !"IS NOT NULL".equalsIgnoreCase( meta.getKeyCondition()[ i ] ) // No field needed!
        ) {
          throw new KettleStepException( BaseMessages.getString(
            PKG, "DatabaseLookup.ERROR0001.FieldRequired1.Exception" )
            + meta.getStreamKeyField1()[ i ]
            + BaseMessages.getString( PKG, "DatabaseLookup.ERROR0001.FieldRequired2.Exception" ) );
        }
        data.keynrs2[ i ] = getInputRowMeta().indexOfValue( meta.getStreamKeyField2()[ i ] );
        if ( data.keynrs2[ i ] < 0 && // couldn't find field!
          "BETWEEN".equalsIgnoreCase( meta.getKeyCondition()[ i ] ) // 2 fields needed!
        ) {
          throw new KettleStepException( BaseMessages.getString(
            PKG, "DatabaseLookup.ERROR0001.FieldRequired3.Exception" )
            + meta.getStreamKeyField2()[ i ]
            + BaseMessages.getString( PKG, "DatabaseLookup.ERROR0001.FieldRequired4.Exception" ) );
        }
        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "DatabaseLookup.Log.FieldHasIndex1" )
            + meta.getStreamKeyField1()[ i ] + BaseMessages.getString( PKG, "DatabaseLookup.Log.FieldHasIndex2" )
            + data.keynrs[ i ] );
        }
      }

      if ( meta.isCached() ) {
        data.cache = DefaultCache.newCache( data, meta.getCacheSize() );
      }

      determineFieldsTypesQueryingDb();

      initNullIf();

      initLookupMeta();

      initReturnMeta();

      // If the user selected to load all data into the cache at startup, that's what we do now...
      //
      if ( meta.isCached() && meta.isLoadingAllDataInCache() ) {
        loadAllTableDataIntoTheCache();
      }

    }

    if ( log.isRowLevel() ) {
      logRowlevel( BaseMessages.getString( PKG, "DatabaseLookup.Log.GotRowFromPreviousStep" )
        + getInputRowMeta().getString( r ) );
    }

    try {
      // add new lookup values to the row
      Object[] outputRow = lookupValues( getInputRowMeta(), r );

      if ( outputRow != null ) {
        // copy row to output rowset(s);
        putRow( data.outputRowMeta, outputRow );

        if ( log.isRowLevel() ) {
          logRowlevel( BaseMessages.getString( PKG, "DatabaseLookup.Log.WroteRowToNextStep" )
            + getInputRowMeta().getString( r ) );
        }
        if ( checkFeedback( getLinesRead() ) ) {
          logBasic( "linenr " + getLinesRead() );
        }
      }
    } catch ( KettleException e ) {
      if ( getStepMeta().isDoingErrorHandling() ) {
        putError( getInputRowMeta(), r, 1, e.getMessage(), null, "DBLOOKUPD001" );
      } else {
        logError( BaseMessages.getString( PKG, "DatabaseLookup.ERROR003.UnexpectedErrorDuringProcessing" )
          + e.getMessage() );
        setErrors( 1 );
        stopAll();
        setOutputDone(); // signal end to receiver(s)
        return false;
      }
    }

    return true;
  }

  private void loadAllTableDataIntoTheCache() throws KettleException {
    DatabaseMeta dbMeta = meta.getDatabaseMeta();

    Database db = getDatabase( dbMeta );
    connectDatabase( db );

    try {
      // We only want to get the used table fields...
      //
      String sql = "SELECT ";

      for ( int i = 0; i < meta.getStreamKeyField1().length; i++ ) {
        if ( i > 0 ) {
          sql += ", ";
        }
        sql += dbMeta.quoteField( meta.getTableKeyField()[ i ] );
      }

      // Also grab the return field...
      //
      for ( int i = 0; i < meta.getReturnValueField().length; i++ ) {
        sql += ", " + dbMeta.quoteField( meta.getReturnValueField()[ i ] );
      }
      // The schema/table
      //
      sql += " FROM "
        + dbMeta.getQuotedSchemaTableCombination(
          environmentSubstitute( meta.getSchemaName() ),
          environmentSubstitute( meta.getTablename() ) );

      // order by?
      if ( meta.getOrderByClause() != null && meta.getOrderByClause().length() != 0 ) {
        sql += " ORDER BY " + meta.getOrderByClause();
      }

      // Now that we have the SQL constructed, let's store the rows...
      //
      List<Object[]> rows = db.getRows( sql, 0 );
      if ( rows != null && rows.size() > 0 ) {
        if ( data.allEquals ) {
          putToDefaultCache( db, rows );
        } else {
          putToReadOnlyCache( db, rows );
        }
      }
    } catch ( Exception e ) {
      throw new KettleException( e );
    } finally {
      if ( db != null ) {
        db.disconnect();
      }
    }
  }

  private void putToDefaultCache( Database db, List<Object[]> rows ) {
    final int keysAmount = meta.getStreamKeyField1().length;
    RowMetaInterface prototype = copyValueMetasFrom( db.getReturnRowMeta(), keysAmount );

    // Copy the data into 2 parts: key and value...
    //
    for ( Object[] row : rows ) {
      int index = 0;
      // not sure it is efficient to re-create the same on every row,
      // but this was done earlier, so I'm keeping this behaviour
      RowMetaInterface keyMeta = prototype.clone();
      Object[] keyData = new Object[ keysAmount ];
      for ( int i = 0; i < keysAmount; i++ ) {
        keyData[ i ] = row[ index++ ];
      }
      // RowMeta valueMeta = new RowMeta();
      Object[] valueData = new Object[ data.returnMeta.size() ];
      for ( int i = 0; i < data.returnMeta.size(); i++ ) {
        valueData[ i ] = row[ index++ ];
        // valueMeta.addValueMeta(returnRowMeta.getValueMeta(index++));
      }
      // Store the data...
      //
      data.cache.storeRowInCache( meta, keyMeta, keyData, valueData );
      incrementLinesInput();
    }
  }

  private RowMetaInterface copyValueMetasFrom( RowMetaInterface source, int n ) {
    RowMeta result = new RowMeta();
    for ( int i = 0; i < n; i++ ) {
      // don't need cloning here,
      // because value metas will be cloned during iterating through rows
      result.addValueMeta( source.getValueMeta( i ) );
    }
    return result;
  }

  private void putToReadOnlyCache( Database db, List<Object[]> rows ) {
    ReadAllCache.Builder cacheBuilder = new ReadAllCache.Builder( data, rows.size() );

    // all keys have the same row meta,
    // it is useless to re-create it each time
    RowMetaInterface returnRowMeta = db.getReturnRowMeta();
    cacheBuilder.setKeysMeta( returnRowMeta.clone() );

    final int keysAmount = meta.getStreamKeyField1().length;
    // Copy the data into 2 parts: key and value...
    //
    final int valuesAmount = data.returnMeta.size();
    for ( Object[] row : rows ) {
      Object[] keyData = new Object[ keysAmount ];
      System.arraycopy( row, 0, keyData, 0, keysAmount );

      Object[] valueData = new Object[ valuesAmount ];
      System.arraycopy( row, keysAmount, valueData, 0, valuesAmount );

      cacheBuilder.add( keyData, valueData );
      incrementLinesInput();
    }
    data.cache = cacheBuilder.build();
  }

  /**
   * Stop the running query
   */
  @Override
  public void stopRunning( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (DatabaseLookupMeta) smi;
    data = (DatabaseLookupData) sdi;

    if ( data.db != null && !data.isCanceled ) {
      synchronized ( data.db ) {
        data.db.cancelQuery();
      }
      data.isCanceled = true;
    }
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (DatabaseLookupMeta) smi;
    data = (DatabaseLookupData) sdi;

    if ( super.init( smi, sdi ) ) {
      if ( meta.getDatabaseMeta() == null ) {
        logError( BaseMessages.getString( PKG, "DatabaseLookup.Init.ConnectionMissing", getStepname() ) );
        return false;
      }
      data.db = getDatabase( meta.getDatabaseMeta() );
      try {
        connectDatabase( data.db );

        // See if all the lookup conditions are "equal"
        // This might speed up things in the case when we load all data in the cache
        //
        data.allEquals = true;
        data.hasDBCondition = false;
        data.conditions = new int[ meta.getKeyCondition().length ];
        for ( int i = 0; i < meta.getKeyCondition().length; i++ ) {
          data.conditions[ i ] =
            Const.indexOfString( meta.getKeyCondition()[ i ], DatabaseLookupMeta.conditionStrings );
          if ( !( "=".equals( meta.getKeyCondition()[ i ] ) || "IS NULL".equalsIgnoreCase( meta.getKeyCondition()[ i ] ) ) ) {
            data.allEquals = false;
          }
          if ( data.conditions[ i ] == DatabaseLookupMeta.CONDITION_LIKE ) {
            data.hasDBCondition = true;
          }
        }

        return true;
      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "DatabaseLookup.ERROR0004.UnexpectedErrorDuringInit" )
          + e.toString() );
        if ( data.db != null ) {
          data.db.disconnect();
        }
      }
    }
    return false;
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (DatabaseLookupMeta) smi;
    data = (DatabaseLookupData) sdi;

    if ( data.db != null ) {
      data.db.disconnect();
    }

    // Recover memory immediately, allow in-memory data to be garbage collected
    //
    data.cache = null;

    super.dispose( smi, sdi );
  }

  /*
   * this method is required in order to
   * provide ability for unit tests to
   * mock the main database instance for the step
   * (@see org.pentaho.di.trans.steps.databaselookup.PDI5436Test)
   */
  Database getDatabase( DatabaseMeta meta ) {
    return new Database( this, meta );
  }

  private void connectDatabase( Database database ) throws KettleDatabaseException {
    database.shareVariablesWith( this );
    if ( getTransMeta().isUsingUniqueConnections() ) {
      synchronized ( getTrans() ) {
        database.connect( getTrans().getTransactionId(), getPartitionID() );
      }
    } else {
      database.connect( getPartitionID() );
    }

    database.setCommit( 100 ); // we never get a commit, but it just turns off auto-commit.

    if ( log.isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "DatabaseLookup.Log.ConnectedToDatabase" ) );
    }
  }
}
