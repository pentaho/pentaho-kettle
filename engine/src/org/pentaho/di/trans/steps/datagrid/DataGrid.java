/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.datagrid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;

/**
 * Generates a number of (empty or the same) rows
 *
 * @author Matt
 * @since 4-apr-2003
 */
public class DataGrid extends BaseStep implements StepInterface {
  private static Class<?> PKG = DataGridMeta.class; // for i18n purposes, needed by Translator2!!

  private DataGridMeta meta;
  private DataGridData data;

  public DataGrid( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );

    meta = (DataGridMeta) getStepMeta().getStepMetaInterface();
    data = (DataGridData) stepDataInterface;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    if ( data.linesWritten >= meta.getDataLines().size() ) {
      // no more rows to be written
      setOutputDone();
      return false;
    }

    if ( first ) {
      // The output meta is the original input meta + the
      // additional constant fields.

      first = false;
      data.linesWritten = 0;

      data.outputRowMeta = new RowMeta();
      meta.getFields( data.outputRowMeta, getStepname(), null, null, this, repository, metaStore );
      // Read values from lookup step (look)
      if ( !readLookupValues() ) {
        logError( BaseMessages.getString( PKG, "DataGrid.Log.UnableToReadDataFromInfoStream" ) );
        setErrors( 1 );
        stopAll();
        return false;
      }
      setIndexAfterLookup();
      // Use these metadata values to convert data...
      //
      data.convertMeta = data.outputRowMeta.cloneToType( ValueMetaInterface.TYPE_STRING );
    }

    Object[] outputRowData = RowDataUtil.allocateRowData( data.outputRowMeta.size() );
    List<String> outputLine = meta.getDataLines().get( data.linesWritten );

    for ( int i = 0; i < data.outputRowMeta.size(); i++ ) {
      if ( meta.isSetEmptyString()[ i ] ) {
        // Set empty string
        outputRowData[ i ] = StringUtil.EMPTY_STRING;
      } else {

        ValueMetaInterface valueMeta = data.outputRowMeta.getValueMeta( i );
        ValueMetaInterface convertMeta = data.convertMeta.getValueMeta( i );
        String valueData = outputLine.get( i );

        if ( valueData != null && valueMeta.isNull( valueData ) ) {
          valueData = null;
        }

        if ( valueMeta.getStorageType() == ValueMetaInterface.STORAGE_TYPE_BINARY_STRING ) {
          ValueMetaInterface convertStorageMeta = convertMeta.clone();
          convertStorageMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
          Object origin = valueMeta.convertDataFromString( valueData, convertStorageMeta, null, null, 0 );
          ValueMetaInterface convertStorageMeta2 = valueMeta.clone();
          convertStorageMeta2.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
          outputRowData[ i ] = convertStorageMeta2.convertToBinaryStringStorageType( origin );
        } else if ( valueMeta.getStorageType() == ValueMetaInterface.STORAGE_TYPE_INDEXED ) {
          ValueMetaInterface convertStorageMeta = convertMeta.clone();
          convertStorageMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
          Object origin = valueMeta.convertDataFromString( valueData, convertStorageMeta, null, null, 0 );
          Object[] index = valueMeta.getIndex();
          if ( origin == null ) {
            outputRowData[ i ] = null;
          } else {
            if ( index == null ) {
              outputRowData[ i ] = null;
            } else {
              if ( origin.getClass().isArray() ) {
                for ( int k = 0; k < index.length; k++ ) {
                  if ( Arrays.equals( (byte[]) index[ k ], (byte[]) origin ) ) {
                    outputRowData[ i ] = k;
                    break;
                  }
                }
              } else {
                outputRowData[ i ] = Arrays.asList( index ).indexOf( origin );
              }
            }
          }
          // in case we are generating number of index outputRowData[ i ] = i;
        } else {
          outputRowData[ i ] = valueMeta.convertDataFromString( valueData, convertMeta, null, null, 0 );
        }
      }
    }

    putRow( data.outputRowMeta, outputRowData );
    data.linesWritten++;

    if ( log.isRowLevel() ) {
      log.logRowlevel( toString(), BaseMessages.getString( PKG, "DataGrid.Log.Wrote.Row", Long
        .toString( getLinesWritten() ), data.outputRowMeta.getString( outputRowData ) ) );
    }

    if ( checkFeedback( getLinesWritten() ) ) {
      if ( log.isBasic() ) {
        logBasic( BaseMessages.getString( PKG, "DataGrid.Log.LineNr", Long.toString( getLinesWritten() ) ) );
      }
    }

    return true;
  }

  private void setIndexAfterLookup() {
    int i = 0;
    for ( ValueMetaInterface valueMeta : data.outputRowMeta.getValueMetaList() ) {
      if ( valueMeta.isStorageIndexed() ) {
        String indexName = meta.getFieldIndexName()[ i ];
        Set index = data.look.get( indexName );
        if ( index != null ) {
          valueMeta.setIndex( index.toArray() );
        }
      }
      i++;
    }
  }

  private boolean readLookupValues() throws KettleException {
    if ( !hasIndexedFields() ) {
      return true;
    }
    List<StreamInterface> infoStreams = meta.getStepIOMeta().getInfoStreams();
    if ( infoStreams == null || infoStreams.isEmpty() ) {
      logError( BaseMessages.getString( PKG, "DataGrid.Log.NoLookupStepSpecified" ) );      //no info streams
      return false;
    }

    for ( StreamInterface infoStream : infoStreams ) {
      //data.infoStream = meta.getStepIOMeta().getInfoStreams().get( 0 );
      //data.infoStream = infoStream;
      Set<String> indexFieldsNamesProcessed = new HashSet<String>();
      RowMetaInterface infoCache = null;
      RowMetaInterface infoMeta;
      if ( infoStream.getStepMeta() == null ) {
        logError( BaseMessages.getString( PKG, "DataGrid.Log.NoLookupStepSpecified" ) );
        return false;
      }

      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "DataGrid.Log.ReadingFromStream" )
          + infoStream.getStepname() + "]" );
      }

      boolean firstRun = true;
      // Which row set do we read from?
      //
      RowSet rowSet = findInputRowSet( infoStream.getStepname() );
      Object[] rowData = getRowFrom( rowSet ); // rows are originating from "lookup_from"

      while ( rowData != null ) {
        if ( firstRun ) {
          infoMeta = rowSet.getRowMeta().clone();
          // Check lookup field
          int indexOfLookupField;
          infoCache = new RowMeta();
          for ( String indexName : data.indexFieldsNames.keySet() ) {
            indexOfLookupField = infoMeta.indexOfValue( environmentSubstitute( indexName ) );
            if ( indexOfLookupField >= 0 ) {
              data.indexFieldsNames.put( indexName, indexOfLookupField );
              ValueMetaInterface keyValueMeta = infoMeta.getValueMeta( indexOfLookupField );
              keyValueMeta.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
              infoCache.addValueMeta( keyValueMeta );
              data.look.put( indexName, new LinkedHashSet() );
            } else {
              indexFieldsNamesProcessed.add( indexName );
            }
          }
        }

        if ( log.isRowLevel() ) {
          logRowlevel( BaseMessages.getString( PKG, "DataGrid.Log.ReadLookupRow" )
            + rowSet.getRowMeta().getString( rowData ) );
        }

        // Look up the keys in the source rows
        // and store values in cache

        List<Object> storeDataList = new ArrayList<Object>();
        for ( String indexName : data.indexFieldsNames.keySet() ) {
          int i = data.indexFieldsNames.get( indexName );
          if ( data.look.get( indexName ) == null || indexFieldsNamesProcessed.contains( indexName ) ) {
            continue;
          }
          if ( rowData[ i ] != null ) {
            ValueMetaInterface fromStreamRowMeta = rowSet.getRowMeta().getValueMeta( i );
            Object obj;
            if ( fromStreamRowMeta.isStorageBinaryString() ) {
              obj = fromStreamRowMeta.convertToNormalStorageType( rowData[ i ] );
            } else {
              obj = rowData[ i ];
            }
            storeDataList.add( obj );
            addToCache( indexName, obj );
          }
        }

        if ( isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "DataGrid.Log.AddingValueToCache", infoCache
            .getString( storeDataList.toArray() ) ) );
        }

        rowData = getRowFrom( rowSet );

        if ( firstRun ) {
          firstRun = false;
        }
      }
    }


    return true;
  }

  private void addToCache( String indexName, Object object ) throws KettleException {
    try {
      data.look.get( indexName ).add( object );
    } catch ( java.lang.OutOfMemoryError o ) {
      // exception out of memory
      throw new KettleException( BaseMessages.getString( PKG, "DataGrid.Error.JavaHeap", o.toString() ) );
    }
  }

  private boolean hasIndexedFields() {
    boolean result = false;
    int i = 0;
    for ( ValueMetaInterface valueMeta : data.outputRowMeta.getValueMetaList() ) {
      if ( valueMeta.isStorageIndexed() ) {
        result = true;
        String indexName = meta.getFieldIndexName()[ i ];
        data.indexFieldsNames.put( indexName, 0 );
      }
      i++;
    }
    return result;
  }

  public boolean isWaitingForData() {
    return true;
  }

}
