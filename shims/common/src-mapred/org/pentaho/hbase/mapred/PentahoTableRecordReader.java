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

import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapred.TableRecordReader;

/**
 * Subclasses TableRecordReader from the mapred package in order to add more
 * configuration options (ala the implemetation in mapreduce).
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 */
public class PentahoTableRecordReader extends TableRecordReader {  
  
  /** Our actual record reader implementation */
  private PentahoTableRecordReaderImpl m_recordReaderImpl = new PentahoTableRecordReaderImpl();
  
  public void setScanCacheRowSize(int size) {
    m_recordReaderImpl.setScanCacheRowSize(size);
  }
  
  public void setTimestamp(long ts) {
    m_recordReaderImpl.setTimestamp(ts);
  }
  
  public void setTimeStampRange(long start, long end) {
    m_recordReaderImpl.setTimeStampRange(start, end);
  }
  
  /**                                                                                                                                                  
   * Restart from survivable exceptions by creating a new scanner.                                                                                     
   *                                                                                                                                                   
   * @param firstRow                                                                                                                                   
   * @throws IOException                                                                                                                               
   */
  public void restart(byte[] firstRow) throws IOException {
    m_recordReaderImpl.restart(firstRow);
  }

  /**                                                                                                                                                  
   * Build the scanner. Not done in constructor to allow for extension.                                                                                
   *                                                                                                                                                   
   * @throws IOException                                                                                                                               
   */
  public void init() throws IOException {
    m_recordReaderImpl.restart(m_recordReaderImpl.getStartRow());
  }

  /**                                                                                                                                                  
   * @param htable the {@link HTable} to scan.                                                                                                         
   */
  public void setHTable(HTable htable) {
    m_recordReaderImpl.setHTable(htable);
  }

  /**                                                                                                                                                  
   * @param inputColumns the columns to be placed in {@link Result}.                                                                                   
   */
  public void setInputColumns(final byte [][] inputColumns) {
    m_recordReaderImpl.setInputColumns(inputColumns);
  }

  /**                                                                                                                                                  
   * @param startRow the first row in the split                                                                                                        
   */
  public void setStartRow(final byte [] startRow) {
    m_recordReaderImpl.setStartRow(startRow);
  }
  
  /**                                                                                                                                                  
   *                                                                                                                                                   
   * @param endRow the last row in the split                                                                                                           
   */
  public void setEndRow(final byte [] endRow) {
    m_recordReaderImpl.setEndRow(endRow);
  }

  /**                                                                                                                                                  
   * @param rowFilter the {@link Filter} to be used.                                                                                                   
   */
  public void setRowFilter(Filter rowFilter) {
    m_recordReaderImpl.setRowFilter(rowFilter);
  }

  public void close() {
    m_recordReaderImpl.close();
  }

  /**                                                                                                                                                  
   * @return ImmutableBytesWritable                                                                                                                    
   *                                                                                                                                                   
   * @see org.apache.hadoop.mapred.RecordReader#createKey()                                                                                            
   */
  public ImmutableBytesWritable createKey() {
    return m_recordReaderImpl.createKey();
  }
  
  /**                                                                                                                                                  
   * @return RowResult                                                                                                                                 
   *                                                                                                                                                   
   * @see org.apache.hadoop.mapred.RecordReader#createValue()                                                                                          
   */
  public Result createValue() {
    return m_recordReaderImpl.createValue();
  }

  public long getPos() {

    // This should be the ordinal tuple in the range;                                                                                                  
    // not clear how to calculate...                                                                                                                   
    return m_recordReaderImpl.getPos();
  }

  public float getProgress() {
    // Depends on the total number of tuples and getPos                                                                                                
    return m_recordReaderImpl.getPos();
  }

  /**                                                                                                                                                  
   * @param key HStoreKey as input key.                                                                                                                
   * @param value MapWritable as input value                                                                                                           
   * @return true if there was more data                                                                                                               
   * @throws IOException                                                                                                                               
   */
  public boolean next(ImmutableBytesWritable key, Result value)
  throws IOException {
    return m_recordReaderImpl.next(key, value);
  }
}
