/* Copyright (c) 2011 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.di.trans.steps.cassandrainput;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;

import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.Compression;
import org.apache.cassandra.thrift.CqlRow;
import org.pentaho.cassandra.CassandraColumnMetaData;
import org.pentaho.cassandra.CassandraConnection;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
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
   * @return a connection to cassandra
   * @throws Exception if a problem occurs during connection
   */
  public static CassandraConnection getCassandraConnection(String host, int port) throws Exception {
    return new CassandraConnection(host, port);
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
