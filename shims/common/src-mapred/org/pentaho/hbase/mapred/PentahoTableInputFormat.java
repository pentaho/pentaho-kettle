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

package org.pentaho.hbase.mapred;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.mapred.TableInputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapred.JobConf;
import org.pentaho.di.core.Const;

/**
 * Extends the mapred TableInputFormat and adds the ability to specify 
 * the table to read from via a property (rather than abusing the input
 * path). Also adds more configuration properties (like those int the 
 * mapreduce package's implementation).<p>
 * 
 * The following properties can be set in Pentaho MR job to configure the 
 * split:<br><br>
 * 
 * <code>
 * hbase.mapred.inputtable // name of the HBase table to read from
 * hbase.mapred.tablecolumns // space delimited list of columns in ColFam:ColName format (ColName can be ommitted to read all columns from a family)
 * hbase.mapreduce.scan.cachedrows // number of rows for caching that will be passed to scanners
 * hbase.mapreduce.scan.timestamp // timestamp used to filter columns with a specific time stamp
 * hbase.mapreduce.scan.timerange.start // starting timestamp to filter in a given timestamp range
 * hbase.mapreduce.scan.timerange.end // end timestamp to filter in a given timestamp range
 * </code>
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 */
public class PentahoTableInputFormat extends TableInputFormat {
  
  // Note that the hbase.mapred.tablecolumns property is inherited
  // from TableInputFormat. This property expects a space-delimited list
  // of column names to read in the format "ColumnFamily:ColumnName". The
  // ColumnName may be ommitted in order to read *all* columns from the 
  // specified family
  
  /** The name of the table to read from */
  public static final String INPUT_TABLE = "hbase.mapred.inputtable";
  
  /** The number of rows (integer) for caching that will be passed to scanners. */
  public static final String SCAN_CACHEDROWS = "hbase.mapreduce.scan.cachedrows";
  
  /** The timestamp (long) used to filter columns with a specific timestamp. */
  public static final String SCAN_TIMESTAMP = "hbase.mapreduce.scan.timestamp";
  
  /** The starting timestamp (long) used to filter columns with a specific range of versions. */
  public static final String SCAN_TIMERANGE_START = "hbase.mapreduce.scan.timerange.start";
  
  /** The ending timestamp (long) used to filter columns with a specific range of versions. */
  public static final String SCAN_TIMERANGE_END = "hbase.mapreduce.scan.timerange.end";
  
  protected final Log PLOG = LogFactory.getLog(PentahoTableInputFormat.class);
  
  public void configure(JobConf job) {
    
    String tableName = job.get(INPUT_TABLE);
    
    // columns can be colFam:colName or colFam: 
    // the later can be used to set up a scan that 
    String colArg = job.get(COLUMN_LIST);

    if (!Const.isEmpty(colArg)) {
      String[] colNames = colArg.split(" ");
      byte [][] m_cols = new byte[colNames.length][];
      for (int i = 0; i < m_cols.length; i++) {
        String colN = colNames[i];
        m_cols[i] = Bytes.toBytes(colN);
      }
      setInputColumns(m_cols);
    }
    
    try {
      setHTable(new HTable(HBaseConfiguration.create(job), tableName));
    } catch (Exception e) {
      PLOG.error(StringUtils.stringifyException(e));
    }
    
    // set our table record reader
    PentahoTableRecordReader rr = new PentahoTableRecordReader();
    String cacheSize = job.get(SCAN_CACHEDROWS); 
    if (!Const.isEmpty(cacheSize)) {
      rr.setScanCacheRowSize(Integer.parseInt(cacheSize));
    }
    
    setTableRecordReader(rr);
  }
  
  public void validateInput(JobConf job) throws IOException {
    // expecting a table name
    String tableName = job.get(INPUT_TABLE);
    if (Const.isEmpty(tableName)) {
      throw new IOException("expecting one table name");
    }

    // connected to table?                                                                                                                             
    if (getHTable() == null) {
      throw new IOException("could not connect to table '" +
        tableName + "'");
    }

    // expecting at least one column/column family                                                                                                                   
    String colArg = job.get(COLUMN_LIST);
    if (colArg == null || colArg.length() == 0) {
      throw new IOException("expecting at least one column/column family");
    }
  }
}
