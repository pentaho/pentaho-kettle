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
package org.pentaho.di.trans.steps.cassandrasstableoutput;

/*
 * Adapted from DataStax DataImportExample
 * http://www.datastax.com/wp-content/uploads/2011/08/DataImportExample.java
 * 
 * Original Disclaimer:
 * This file is an example on how to use the Cassandra SSTableSimpleUnsortedWriter class to create
 * sstables from a csv input file.
 * While this has been tested to work, this program is provided "as is" with no guarantee. Moreover,
 * it's primary aim is toward simplicity rather than completness. In partical, don't use this as an
 * example to parse csv files at home.
 * 
 */
import static org.apache.cassandra.utils.ByteBufferUtil.bytes;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.cassandra.db.marshal.AsciiType;
import org.apache.cassandra.io.sstable.SSTableSimpleUnsortedWriter;
import org.apache.log4j.helpers.ISO8601DateFormat;
import org.pentaho.di.core.exception.KettleException;

/**
 * Outputs Cassandra SSTables (sorted-string tables) to a directory.
 * 
 * Adapted from DataStax DataImportExample
 * http://www.datastax.com/wp-content/uploads/2011/08/DataImportExample.java
 *  
 * @author Rob Turner (robert{[at]}robertturner{[dot]}com{[dot]}au)
 */
public class SSTableWriter {

  private static final DateFormat ISO8601 = ISO8601DateFormat.getInstance();
  private static final int DEFAULT_BUFFER_SIZE_MB = 16;

  private String directory = System.getProperty("java.io.tmpdir");
  private String keyspace;
  private String columnFamily;
  private String keyField;
  private int bufferSize = DEFAULT_BUFFER_SIZE_MB;

  private SSTableSimpleUnsortedWriter writer;

  /**
   * Set the directory to read the sstables from
   * 
   * @param directory the directory to read the sstables from
   */
  public void setDirectory(String directory) {
    this.directory = directory;
  }

  /**
   * Set the target keyspace
   * 
   * @param keyspace the keyspace to use
   */
  public void setKeyspace(String keyspace) {
    this.keyspace = keyspace;
  }

  /**
   * Set the column family (table) to load to. Note: it is assumed that 
   * this column family exists in the keyspace apriori.
   * 
   * @param columnFamily the column family to load to.
   */
  public void setColumnFamily(String columnFamily) {
    this.columnFamily = columnFamily;
  }

  /**
   * Set the key field name
   * 
   * @param keyField the key field name
   */
  public void setKeyField(String keyField) {
    this.keyField = keyField;
  }

  /**
   * Set the buffer size (Mb) to use. A new table file is written 
   * every time the buffer is full.
   * 
   * @param bufferSize the size of the buffer to use
   */
  public void setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
  }

  /**
   * Initialization. Creates target directory if needed and establishes 
   * the writer
   * 
   * @throws Exception if a problem occurs
   */
  public void init() throws Exception {
    File directory = new File(this.directory);

    if (!directory.exists()) {
      directory.mkdir();
    }
    try {
      writer = new SSTableSimpleUnsortedWriter(directory, keyspace,
          columnFamily, AsciiType.instance, null, bufferSize);
    } catch (Throwable t) {
      throw new KettleException(
          "Failed to create SSTableSimpleUnsortedWriter", t);
    }
  }

  /**
   * Process a row of data
   * 
   * @param record a row of data as a Map of column names to values
   * @throws Exception if a problem occurs
   */
  public void processRow(Map<String, Object> record) throws Exception {

    // get UUID
    ByteBuffer uuid = valueToBytes(record.get(keyField));
    // write record
    writer.newRow(uuid);
    long timestamp = System.currentTimeMillis() * 1000;
    for (Entry<String, Object> entry : record.entrySet()) {
      // get value
      Object value = entry.getValue();
      if (isNull(value)) {
        continue;
      }
      
      // don't write the key as a column!
      if (entry.getKey().equals(keyField)) {
        continue;
      }
      
      // write
      writer.addColumn(bytes(entry.getKey()), valueToBytes(value),
          timestamp);
    }
  }

  private static final ByteBuffer valueToBytes(Object val) throws Exception {
    if (val instanceof String) {
      return bytes((String) val);
    }
    if (val instanceof Integer) {
      return bytes(((Integer) val).intValue());
    }
    if (val instanceof Float) {
      return bytes(((Float) val).floatValue());
    }
    if (val instanceof Boolean) {
      // will return "true" or "false"
      return bytes(val.toString());
    }
    if (val instanceof Date) {
      // use ISO 8601 date format
      try {
        return bytes(ISO8601.format((Date) val));
      } catch (ArrayIndexOutOfBoundsException e) {
        // something wrong with the date... just convert to string
        return bytes(val.toString());
      }
    }
    if (val instanceof Long) {
      return bytes(((Long) val).longValue());
    }
    if (val instanceof Double) {
      return bytes(((Double) val).doubleValue());
    }
    
    if (val instanceof byte[]) {
      return ByteBuffer.wrap((byte[]) val);
    }
    
    // reduce to string
    return bytes(val.toString());
  }

  static final boolean isNull(Object val) {
    if (val == null) {
      return true;
    }
    // empty strings are considered null in this context
    if (val instanceof String) {
      return "".equals(val);
    }
    return false;
  }

  /**
   * Close the writer
   * 
   * @throws Exception if a problem occurs
   */
  public void close() throws Exception {
    if (writer != null) {
      writer.close();
    }
  }
}
