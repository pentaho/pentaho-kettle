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
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;

import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.Compression;
import org.apache.cassandra.thrift.CqlRow;
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
    return new CassandraConnection(host, port, username, password);
  }
  
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
      if (colValue instanceof Date) {
        ValueMeta tempDateMeta = new ValueMeta("temp", ValueMetaInterface.TYPE_DATE);
        stringV = tempDateMeta.getString(colValue);
      } else if (colValue instanceof byte[]) {
        stringV = new String((byte[]) colValue);
      }
      
      
      outputRowData[2] = stringV;
      
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
