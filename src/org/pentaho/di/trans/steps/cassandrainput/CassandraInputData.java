/*******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.cassandrainput;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;

import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnOrSuperColumn;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.Compression;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.CqlRow;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.KeyRange;
import org.apache.cassandra.thrift.KeySlice;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;
import org.apache.cassandra.thrift.TimedOutException;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.thrift.TException;
import org.pentaho.cassandra.CassandraColumnMetaData;
import org.pentaho.cassandra.CassandraConnection;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Data class for the CassandraInput step. Contains some utility methods for obtaining
 * a connection to cassandra, translating a row from cassandra to Kettle and for
 * compressing a query. 
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
public class CassandraInputData extends BaseStepData implements StepDataInterface {
    
  /** The output data format */
  protected RowMetaInterface m_outputRowMeta;
    
  /**
   * Get the output row format
   * 
   * @return the output row format
   */
  public RowMetaInterface getOutputRowMeta() {
    return m_outputRowMeta;
  }
  
  /**
   * Set the output row format
   * 
   * @param rmi the output row format
   */
  public void setOutputRowMeta(RowMetaInterface rmi) {
    m_outputRowMeta = rmi;
  }
  
  /**
   * Get a connection to cassandra
   * 
   * @param host the hostname of a cassandra node
   * @param port the port that cassandra is listening on
   * @param username the username for (optional) authentication
   * @param password the password for (optional) authentication
   * @return a connection to cassandra
   * @throws Exception if a problem occurs during connection
   */
  public static CassandraConnection getCassandraConnection(String host, 
      int port, String username, String password) throws Exception {
    return new CassandraConnection(host, port, username, password, -1);
  }
  
  /**
   * Get a connection to cassandra
   * 
   * @param host the hostname of a cassandra node
   * @param port the port that cassandra is listening on
   * @param username the username for (optional) authentication
   * @param password the password for (optional) authentication
   * @param timeout the socket timeout to use
   * @return a connection to cassandra
   * @throws Exception if a problem occurs during connection
   */
  public static CassandraConnection getCassandraConnection(String host,
      int port, String username, String password, int timeout) throws Exception {
    return new CassandraConnection(host, port, username, password, timeout);
  }
  
  // ------------------------------------------------------------------
  // The following code implements pure Thrift-based <key, col_name, value, timestamp>
  // tuple extraction
  protected boolean m_newSliceQuery = false;
  protected List<String> m_requestedCols = null;
  protected int m_sliceRowsMax;
  protected int m_sliceColsMax;
  protected int m_sliceRowsBatchSize;
  protected int m_sliceColsBatchSize;
  protected SliceRange m_sliceRange;
  protected KeyRange m_keyRange;
  protected SlicePredicate m_slicePredicate;
  protected ColumnParent m_colParent;
  int m_rowIndex;
  int m_colIndex;
  
  // current batch of rows
  protected List<KeySlice> m_cassandraRows;
  
  // current batch of columns from current row
  protected List<ColumnOrSuperColumn> m_currentCols;
  protected List<Object[]> m_converted;
  protected int m_colCount;
  protected int m_rowCount;
  public void sliceModeInit(CassandraColumnMetaData meta, List<String> colNames, 
      int maxRows, int maxCols, int rowBatchSize, int colBatchSize) throws KettleException {

    m_newSliceQuery = true;
    m_requestedCols = colNames;
    m_sliceRowsMax = maxRows;
    m_sliceColsMax = maxCols;
    m_sliceRowsBatchSize = rowBatchSize;
    m_sliceColsBatchSize = colBatchSize;
    m_rowIndex = 0;
    m_colIndex = 0;
    
    if (m_sliceColsBatchSize <= 0) {
      m_sliceColsBatchSize = Integer.MAX_VALUE;
    }
    
    if (m_sliceRowsBatchSize <= 0) {
      m_sliceRowsBatchSize = Integer.MAX_VALUE;
    }
    
    List<ByteBuffer> specificCols = null;    
    if (m_requestedCols != null && m_requestedCols.size() > 0) {
      specificCols = new ArrayList<ByteBuffer>();
      
      // encode the textual column names
      for (String colName : m_requestedCols) {
        ByteBuffer encoded = meta.columnNameToByteBuffer(colName);
        specificCols.add(encoded);
      }
    }
    
    m_slicePredicate = new SlicePredicate();
    
    if (specificCols == null) {
      m_sliceRange = new SliceRange(ByteBuffer.wrap(new byte[0]), ByteBuffer.wrap(new byte[0]), 
          false, m_sliceColsBatchSize);
      m_slicePredicate.setSlice_range(m_sliceRange);
    } else {
      m_slicePredicate.setColumn_names(specificCols);
    }
    
    m_keyRange = new KeyRange(m_sliceRowsBatchSize);
    m_keyRange.setStart_key(new byte[0]);
    m_keyRange.setEnd_key(new byte[0]);
        
    m_colParent = new ColumnParent(meta.getColumnFamilyName());
    m_converted = new ArrayList<Object[]>();
  }
  
  private void advanceToNonEmptyRow() {
    KeySlice row = m_cassandraRows.get(m_rowIndex);
    m_currentCols = row.getColumns();
    
    // TODO this needs to be verified - is true for CQL queries (wildcard query has key as first col)
    // no requested cols means we could get the key as a column??? in the first batch of columns.
    /*int skipSize = (m_requestedCols == null || m_requestedCols.size() == 0)
      ? 1 : 0; */
    int skipSize = 0;
    while (m_currentCols.size() == skipSize && 
        m_rowIndex < m_cassandraRows.size() - 1) {
      m_rowIndex++;
      row = m_cassandraRows.get(m_rowIndex);
      m_currentCols = row.getColumns();
    }
    
    if (m_currentCols.size() == skipSize) {
      // we've been through the batch and there are no columns in any of these rows -
      // so nothing to output! Indicate this by setting currentCols to null
      m_currentCols = null;        
    }
  }
  
  private void getNextBatchOfRows(CassandraConnection conn) throws Exception {

    // reset the column range (if necessary)
    if (m_requestedCols == null) {
      m_sliceRange = m_sliceRange.setStart(ByteBuffer.wrap(new byte[0]));
      m_sliceRange = m_sliceRange.setFinish(ByteBuffer.wrap(new byte[0]));

      m_slicePredicate.setSlice_range(m_sliceRange);
    }
    
    // set the key range start to the last key from the last batch of rows
    m_keyRange.setStart_key(m_cassandraRows.get(m_cassandraRows.size() - 1).getKey());
    m_cassandraRows = conn.getClient().get_range_slices(m_colParent, 
        m_slicePredicate, m_keyRange, ConsistencyLevel.ONE);
    
    m_colCount = 0;
    
    // key ranges are *inclusive* of the start key - we will have already processed the first
    // row in the last batch. Hence start at index 1 of this batch
    m_rowIndex = 1;
    if (m_cassandraRows == null || m_cassandraRows.size() <= 1 
        || m_rowCount == m_sliceRowsMax) {
      // indicate done
      m_currentCols = null;
      m_cassandraRows = null;
    } else {
      advanceToNonEmptyRow();
    }
  }
  
  private void getNextBatchOfColumns(CassandraConnection conn) throws Exception {
    m_sliceRange = m_sliceRange.setStart(m_currentCols.get(m_currentCols.size() - 1).
        getColumn().bufferForName());
    m_slicePredicate.setSlice_range(m_sliceRange);
    
    // fetch the next bunch of columns for the current row
    m_currentCols = conn.getClient().get_slice(m_cassandraRows.get(m_rowIndex).bufferForKey(), 
        m_colParent, m_slicePredicate, ConsistencyLevel.ONE);
    
    // as far as I understand it - these things are always inclusive of the start element,
    // so we need to skip the first element cause it was processed already in the last batch
    // of columns
    if (m_currentCols == null || m_currentCols.size() <= 1) {
      // no more columns in the current row - move to the next row
      m_rowCount++;
      m_rowIndex++;
      m_colCount = 0;
      
      if (m_rowIndex == m_cassandraRows.size()) {
        getNextBatchOfRows(conn);
        
        while (m_cassandraRows != null && m_currentCols == null) {
          // keep going until we get some rows with columns!
          getNextBatchOfRows(conn);
        }            
      } else {
        advanceToNonEmptyRow();
        
        while (m_cassandraRows != null && m_currentCols == null) {
          // keep going until we get some rows with columns!
          getNextBatchOfRows(conn);
        }                                            
      }            
    } else {
      // we need to discard the first col in the list since we will have processed
      // that already in the batch
      m_currentCols.remove(0);
    }
  }
  
  public List<Object[]> cassandraRowToKettleTupleSliceMode(CassandraColumnMetaData metaData,
      CassandraConnection conn) throws KettleException {
    m_converted.clear();
    
    int timeouts = 0;
    
    try {
      while (timeouts < 5) {
        try {
          if (m_newSliceQuery) {
            m_cassandraRows = conn.getClient().get_range_slices(m_colParent, 
                m_slicePredicate, m_keyRange, ConsistencyLevel.ONE);
            if (m_cassandraRows == null || m_cassandraRows.size() == 0) {
              // done
              //m_currentCols = null;
              return null;
            } else {
              advanceToNonEmptyRow();
              while (m_cassandraRows != null && m_currentCols == null) {
                // keep going until we get some rows with columns!
                getNextBatchOfRows(conn);
              }
              
              if (m_cassandraRows == null) {
                // we're done
                return null;
              }
              
/*              KeySlice row = m_cassandraRows.get(0);
              m_currentCols = row.getColumns(); */
              m_colCount = 0;
              m_rowCount = 0;
              m_newSliceQuery = false;
            }
          } else {
            // determine what we need to get next - more columns from current row, or start next row
            // or get next row batch or done
            
            if (m_rowCount == m_sliceRowsMax) {
              // hit our LIMIT of rows - done
              return null;
            }
            
            if (m_rowIndex == m_cassandraRows.size()) {
              // get next batch of rows
              getNextBatchOfRows(conn);
              while (m_cassandraRows != null && m_currentCols == null) {
                // keep going until we get some rows with columns!
                getNextBatchOfRows(conn);
              }
              
              if (m_cassandraRows == null) {
                // we're done
                return null;
              }
            } else if (m_colCount == -1) {
              // get next row
              KeySlice row = m_cassandraRows.get(m_rowIndex);
              m_currentCols = row.getColumns();
              
              m_colCount = 0;
            } else {
              getNextBatchOfColumns(conn);
              
              if (m_cassandraRows == null) {
                // we're done
                return null;
              }
            }
          }

          break;
        } catch (TimedOutException e) {
          timeouts++;
        }
      }
      
      if (timeouts == 5) {
        throw new KettleException("Maximum number of consecutive timeouts exceeded");
      }
      
/*      if (m_currentCols == null || m_currentCols.size() == 0) {
        // must be done
        return null;
      } */
      
      KeySlice row = m_cassandraRows.get(m_rowIndex);
      Object rowKey = metaData.getKeyValue(row);
      if (rowKey == null) {
        throw new KettleException("Unable to obtain a key value for the row!");
      }
      String keyName = metaData.getKeyName();
      int keyIndex = m_outputRowMeta.indexOfValue(keyName);
      if (keyIndex < 0) {
        throw new KettleException("Unable to find the key field name '" + keyName 
            + "' in the output row meta data!");
      }
      for (int i = 0; i < m_currentCols.size(); i++) {
        Object[] outputRowData = RowDataUtil.allocateRowData(m_outputRowMeta.size());
        outputRowData[keyIndex] = rowKey;
        
        Column col = m_currentCols.get(i).getColumn();
        String colName = metaData.getColumnName(col);
        
        // TODO do we need to check for the key as a column (like with
        // CQL rows?)
        
        Object colValue = metaData.getColumnValue(col);
        if (colValue == null) {
          // skip null columns (only applies if we're processing
          // a specified list of columns rather than all columns).
          continue;
        }
        
        outputRowData[1] = colName;
        String stringV = colValue.toString();
        outputRowData[2] = stringV;

        if (colValue instanceof Date) {
          ValueMeta tempDateMeta = new ValueMeta("temp", ValueMetaInterface.TYPE_DATE);
          stringV = tempDateMeta.getString(colValue);
          outputRowData[2] = stringV;
        } else if (colValue instanceof byte[]) {
          outputRowData[2] = colValue;
        }            
        
        // the timestamp as a date object
        long timestampL = col.getTimestamp();
        outputRowData[3] = timestampL;
        m_converted.add(outputRowData);
        
        m_colCount++;
        if (m_colCount == m_sliceColsMax && m_requestedCols == null) {
          // max number of cols reached for this row
          m_colCount = -1; // indicate move to the next row
          
          m_rowCount++;
          m_rowIndex++;
          break; // don't process any more
        }        
      }
      
      if (m_requestedCols != null) {
        // assume that we don't need to page columns when the user has
        // explicitly named the ones that they want
        m_colCount = -1;
        m_rowCount++;
        m_rowIndex++;
      }

    } catch (Exception ex) {
      throw new KettleException(ex.getMessage(), ex);
    }
        
    return m_converted;
  }
  
  // --------------- End Thrift-based tuple mode -------------------------
  
  /**
   * Converts a cassandra row to a Kettle row in the key, colName, colValue, timestamp
   * format
   * 
   * @param metaData meta data on the cassandra column family being read from
   * @param cassandraRow a row from the column family
   * @param cassandraColIter an interator over columns for the current row
   * 
   * @return a Kettle row
   * @throws KettleException if a problem occurs
   */
  public Object[] cassandraRowToKettleTupleMode(CassandraColumnMetaData metaData,
      CqlRow cassandraRow, Iterator<Column> cassandraColIter)
    throws KettleException {
    
    Object[] outputRowData = RowDataUtil.allocateRowData(m_outputRowMeta.size());
    Object key = metaData.getKeyValue(cassandraRow);
    if (key == null) {
      throw new KettleException("Unable to obtain a key value for the row!");
    }
    
    String keyName = metaData.getKeyName();
    int keyIndex = m_outputRowMeta.indexOfValue(keyName);
    if (keyIndex < 0) {
      throw new KettleException("Unable to find the key field name '" + keyName 
          + "' in the output row meta data!");
    }
    outputRowData[keyIndex] = key;
//    System.out.println("Got a key value for a row " + key.toString());
    
    // advance the iterator to the next column
    if (cassandraColIter.hasNext()) {
  //    System.out.println("We have more columns.....");
      Column aCol = cassandraColIter.next();
      
/*      if (aCol.bufferForValue() == null) {
        System.out.println("******* The value of this column is null!");
      } */
      String colName = metaData.getColumnName(aCol);
      
      // skip the key
      if (colName.equals("KEY")) {
        if (cassandraColIter.hasNext()) {
          aCol = cassandraColIter.next();
          colName = metaData.getColumnName(aCol);
        } else {
          // run out of columns
          return null;
        }
      }      
      
      // for queries that specify column names we need to check that the value
      // is not null in this row
      while (metaData.getColumnValue(aCol) == null) {
        if (cassandraColIter.hasNext()) {
          aCol = cassandraColIter.next();
          colName = metaData.getColumnName(aCol);
        } else {
          return null;
        }
      }
      
      outputRowData[1] = colName;
      
      // do the value (stored as a string)
      Object colValue = metaData.getColumnValue(aCol);
      //System.err.println("KEY " + key.toString() + " Column name: " + colName + " Value " + colValue);
      String stringV = colValue.toString();
      outputRowData[2] = stringV;

      if (colValue instanceof Date) {
        ValueMeta tempDateMeta = new ValueMeta("temp", ValueMetaInterface.TYPE_DATE);
        stringV = tempDateMeta.getString(colValue);
        outputRowData[2] = stringV;
      } else if (colValue instanceof byte[]) {
        //        stringV = new String((byte[]) colValue);
        outputRowData[2] = colValue;
      }            
      
      // the timestamp as a date object
      long timestampL = aCol.getTimestamp();
      outputRowData[3] = timestampL;      
    } else {
//      System.out.println("No more columns for this row!!!!!!!");
      return null; // signify no more columns for this row...
    }
        
    return outputRowData;
  }
  
  /**
   * Converts a cassandra row to a Kettle row
   * 
   * @param metaData meta data on the cassandra column family being read from
   * @param cassandraRow a row from the column family
   * @param outputFormatMap a Map of output field names to indexes in the outgoing 
   * Kettle row structure
   * @return a Kettle row
   * @throws KettleException if a problem occurs
   */
  public Object[] cassandraRowToKettle(CassandraColumnMetaData metaData,
      CqlRow cassandraRow, Map<String, Integer> outputFormatMap) throws KettleException {    
    
    Object[] outputRowData = RowDataUtil.allocateRowData(m_outputRowMeta.size());
    Object key = metaData.getKeyValue(cassandraRow);
    if (key == null) {
      throw new KettleException("Unable to obtain a key value for the row!");
    }
    
    String keyName = metaData.getKeyName();
    int keyIndex = m_outputRowMeta.indexOfValue(keyName);
    if (keyIndex < 0) {
      throw new KettleException("Unable to find the key field name '" + keyName 
          + "' in the output row meta data!");
    }
    outputRowData[keyIndex] = key;
    
    // do the columns
    List<Column> rowColumns = cassandraRow.getColumns();
    for (Column aCol : rowColumns) {
      String colName = metaData.getColumnName(aCol);        
      Integer outputIndex = outputFormatMap.get(colName);
      if (outputIndex != null) {
        Object colValue = metaData.getColumnValue(aCol);
        outputRowData[outputIndex.intValue()] = colValue;
      }
    }
    
    return outputRowData;
  }
  
  /**
   * Extract the column family name (table name) from a CQL SELECT
   * query. Assumes that any kettle variables have been already substituted
   * in the query
   * 
   * @param subQ the query with vars substituted
   * @return the column family name or null if the query is malformed
   */
  public static String getColumnFamilyNameFromCQLSelectQuery(String subQ) {
    
    String result = null;
    
    if (Const.isEmpty(subQ)) {
      return null;
    }
    
    // assumes env variables already replaced in query!

    if (!subQ.toLowerCase().startsWith("select")) {
      // not a select statement!
      return null;
    }
    
    if (subQ.indexOf(';') < 0) {
      // query must end with a ';' or it will wait for more!
      return null;
    }
    
    //subQ = subQ.toLowerCase();
    
    // strip off where clause (if any)
    if (subQ.toLowerCase().lastIndexOf("where") > 0) {
      subQ = subQ.substring(0, subQ.toLowerCase().lastIndexOf("where"));
    }
    
    // determine the source column family
    // look for a FROM that is surrounded by space
    int fromIndex = subQ.toLowerCase().indexOf("from");
    String tempS = subQ.toLowerCase();
    int offset = fromIndex;
    while (fromIndex > 0 && tempS.charAt(fromIndex - 1) != ' ' && 
        (fromIndex + 4 < tempS.length()) && tempS.charAt(fromIndex + 4) != ' ') {
      tempS = tempS.substring(fromIndex + 4, tempS.length());
      fromIndex = tempS.indexOf("from");
      offset += (4 + fromIndex);
    }
    
    fromIndex = offset;
    
//    int fromIndex = subQ.toLowerCase().lastIndexOf("from");
    if (fromIndex < 0) {
      return null; // no from clause
    }
    
    result = subQ.substring(fromIndex + 4, subQ.length()).trim();
    if (result.indexOf(' ') > 0) {
      result = result.substring(0, result.indexOf(' '));
    } else {
      result = result.replace(";", "");
    }
    
    if (result.length() == 0) {
      return null; // no column family specified
    }
    
    return result;
  }
  
  /**
   * Compress a CQL query
   * 
   * @param queryStr the CQL query
   * @param compression compression option (GZIP is the only option - so far)
   * @return an array of bytes containing the compressed query
   */
  public static byte[] compressQuery(String queryStr, Compression compression) {
    byte[] data = queryStr.getBytes(Charset.forName(CassandraColumnMetaData.UTF8));
    
    Deflater compressor = new Deflater();
    compressor.setInput(data);
    compressor.finish();

    ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];

    while (!compressor.finished()) {
      int size = compressor.deflate(buffer);
      byteArray.write(buffer, 0, size);
    }
    
    return byteArray.toByteArray();
  }
}
