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

package org.pentaho.di.trans.steps.cassandraoutput;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;

import org.apache.cassandra.thrift.CfDef;
import org.apache.cassandra.thrift.ColumnDef;
import org.apache.cassandra.thrift.Compression;
import org.apache.cassandra.thrift.KsDef;
import org.pentaho.cassandra.CassandraColumnMetaData;
import org.pentaho.cassandra.CassandraConnection;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Data class for the CassandraOutput step. Contains methods for obtaining
 * a connection to cassandra, creating a new column family, updating a column
 * family's meta data and constructing a batch insert CQL statement.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
public class CassandraOutputData extends BaseStepData implements
    StepDataInterface {
  
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
   * Begin a new batch cql statement
   * 
   * @param numRows the number of rows to be inserted in this batch
   * @param consistency the consistency (e.g. ONE, QUORUM etc.) to use, or null
   * to use the default.
   * 
   * @return a StringBuilder initialized for the batch.
   */
  public static StringBuilder newBatch(int numRows, String consistency) {
    
    // make a stab at a reasonable initial capacity
    StringBuilder batch = new StringBuilder(numRows * 80);
    batch.append("BEGIN BATCH");
    
    if (!Const.isEmpty(consistency)) {
      batch.append(" USING CONSISTENCY ").append(consistency);
    }
    
    batch.append("\n");
    
    return batch;
  }
  
  /**
   * Append the "APPLY BATCH" statement to complete the batch
   * 
   * @param batch the StringBuilder batch to complete
   */
  public static void completeBatch(StringBuilder batch) {
    batch.append("APPLY BATCH");
  }
  
  /**
   * Send the batch insert.
   * 
   * @param batch the CQL batch insert statement
   * @param conn the connection to use
   * @param compressCQL true if the CQL should be compressed
   * @throws Exception if a problem occurs
   */
  public static void commitBatch(StringBuilder batch, CassandraConnection conn,
      boolean compressCQL) throws Exception {
    
    // compress the batch if necessary
    byte[] toSend = null;
    if (compressCQL) {
      toSend = compressQuery(batch.toString(), Compression.GZIP);
    } else {
      toSend = batch.toString().getBytes(Charset.forName(CassandraColumnMetaData.UTF8));
    }
    
    conn.getClient().execute_cql_query(ByteBuffer.wrap(toSend), 
        compressCQL ? Compression.GZIP : Compression.NONE);
  }
  
  /**
   * converts a kettle row to CQL insert statement and adds it to 
   * the batch
   * 
   * @param batch StringBuilder for collecting the batch CQL
   * @param colFamilyName the name of the column family (table) to
   * insert into
   * @param inputMeta Kettle input row meta data
   * @param keyIndex the index of the incoming field to use as the
   * key for inserting
   * @param row the Kettle row
   * @param cassandraMeta meta data on the columns in the cassandra
   * column family (table)
   * @param insertFieldsNotInMetaData true if any Kettle fields that
   * are not in the Cassandra column family (table) meta data are to be
   * inserted. This is irrelevant if the user has opted
   * to have the step initially update the Cassandra meta data for incoming
   * fields that are not known about. 
   * 
   * @throws KettleException if the key is null in the incoming row
   */
  public static void addRowToBatch(StringBuilder batch, String colFamilyName, 
      RowMetaInterface inputMeta, int keyIndex, Object[] row, 
      CassandraColumnMetaData cassandraMeta, boolean insertFieldsNotInMetaData) 
    throws KettleException {
    
    // check the key first
    ValueMetaInterface keyMeta = inputMeta.getValueMeta(keyIndex);
    if (keyMeta.isNull(row[keyIndex])) {
      throw new KettleException("Can't insert this row because the key is null!");
    }
    
    batch.append("INSERT INTO ").append(colFamilyName).append(" (KEY");
    
    for (int i = 0; i < inputMeta.size(); i++) {
      if (i != keyIndex) {
        ValueMetaInterface colMeta = inputMeta.getValueMeta(i);
        String colName = colMeta.getName();
        if (!cassandraMeta.columnExistsInSchema(colName) && 
            !insertFieldsNotInMetaData) {
          continue;
        }
        
        // don't insert if null!
        if (colMeta.isNull(row[i])) {
          continue;
        }
        
        batch.append(", '").append(colName).append("'");
      }
    }
    
    batch.append(") VALUES (");
    // key first    
    String keyString = keyMeta.getString(row[keyIndex]);
    batch.append("'").append(keyString).append("'");
    
    for (int i = 0; i < inputMeta.size(); i++) {
      if (i != keyIndex) {
        ValueMetaInterface colMeta = inputMeta.getValueMeta(i);
        String colName = colMeta.getName();
        if (!cassandraMeta.columnExistsInSchema(colName) && 
            !insertFieldsNotInMetaData) {
          continue;
        }
        
        // don't insert if null!
        if (colMeta.isNull(row[i])) {
          continue;
        }
        
        //batch.append(", '").append(colMeta.getString(row[i])).append("'");
        batch.append(", '").append(CassandraColumnMetaData.kettleValueToCQL(colMeta, row[i])).append("'");
      }
    }
    
    batch.append(")\n");    
  }
  
  /**
   * Constructs and executes a CQL TRUNCATE statement.
   * 
   * @param conn the connection to use
   * @param columnFamily the name of the column family to truncate.
   * @throws Exception if a problem occurs.
   */
  public static void truncateColumnFamily(CassandraConnection conn,
      String columnFamily) throws Exception {
    String cqlCommand = "TRUNCATE " + columnFamily;
    
    conn.getClient().execute_cql_query(ByteBuffer.wrap(cqlCommand.getBytes()), 
        Compression.NONE);
  }
  
  /**
   * Updates the schema information for a given column family with any
   * fields in the supplied RowMeta that aren't defined in the
   * schema. Abuses the schema "comment" field to store information on
   * any indexed values that might be in the fields in the RowMeta.
   * 
   * @param conn the connection to use
   * @param colFamilyName the name of the column family to update
   * @param inputMeta the row meta containing (potentially) new fields
   * @param keyIndex the index of the key field in the row meta
   * @param cassandraMeta meta data for the cassandra column family
   * @throws Exception if a problem occurs updating the schema
   */
  public static void updateCassandraMeta(CassandraConnection conn, 
      String colFamilyName, RowMetaInterface inputMeta,
      int keyIndex, CassandraColumnMetaData cassandraMeta) throws Exception {
    
    // column families               
    KsDef keySpace = conn.describeKeyspace();
    List<CfDef> colFams = null;
    if (keySpace != null) {
      colFams = keySpace.getCf_defs();
    } else {
      throw new Exception("Unable to get meta data on keyspace.");
    }

    // look for the requested column family
    CfDef colFamDefToUpdate = null;
//    CfDef colDefs = null;
    for (CfDef fam : colFams) {
      String columnFamilyName = fam.getName(); // table name
      if (columnFamilyName.equals(colFamilyName)) {
        colFamDefToUpdate = fam;
        break;
      }
    }
    
    if (colFamDefToUpdate == null) {
      throw new Exception("Can't update meta data - unable to find " +
      		"column family '" + colFamilyName + "'");
    }
    
    String comment = colFamDefToUpdate.getComment();

    List<ValueMetaInterface> indexedVals = new ArrayList<ValueMetaInterface>();        
    for (int i = 0; i < inputMeta.size(); i++) {
      if (i != keyIndex) {
        ValueMetaInterface colMeta = inputMeta.getValueMeta(i);
        if (colMeta.getStorageType() == ValueMetaInterface.STORAGE_TYPE_INDEXED) {
          indexedVals.add(colMeta);
        }
        String colName = colMeta.getName();
        if (!cassandraMeta.columnExistsInSchema(colName)) {
          String colType = CassandraColumnMetaData.getCassandraTypeForValueMeta(colMeta);
          
          ColumnDef newCol = new ColumnDef(ByteBuffer.wrap(colName.getBytes()),                                              
            colType);
          colFamDefToUpdate.addToColumn_metadata(newCol);
        }
      }     
    }    
    
    // update the comment fields for any new indexed vals
    if (indexedVals.size() > 0) {
      String before = "";
      String after = "";
      String meta = "";
      if (comment != null && comment.length() > 0) {
        // is there any indexed value meta data there already?
        if (comment.indexOf("@@@") >= 0) {
          // have to strip out existing stuff
          before = comment.substring(0, comment.indexOf("@@@"));
          after = comment.substring(comment.lastIndexOf("@@@") + 3, comment.length());
          meta = comment.substring(comment.indexOf("@@@", comment.lastIndexOf("@@@")));
          meta.replace("@@@", "");
        }
      }
      
      StringBuffer buff = new StringBuffer();
      buff.append(meta);
      for (ValueMetaInterface vm : indexedVals) {
        String colName = vm.getName();
        if (meta.indexOf(colName) < 0) {
          // add this one
          Object[] legalVals = vm.getIndex();
          if (buff.length() > 0) {
            buff.append(";").append(colName).append(":{");
          } else {
            buff.append(colName).append(":{");
          }
          for (int i = 0; i < legalVals.length; i++) {
            buff.append(legalVals[i].toString());
            if (i != legalVals.length - 1) {
              buff.append(",");
            }
          }
          buff.append("}");
        }
      }
      
      comment = before + "@@@" + buff.toString() + "@@@" + after;
      colFamDefToUpdate.setComment(comment);
    }
    
    conn.getClient().system_update_column_family(colFamDefToUpdate);
    
    // get the cassandraMeta to refresh itself
    cassandraMeta.refresh(conn);
  }
  
  /**
   * Constructs a CQL statement to create a new column family. Uses
   * Cassandra defaults for default comparator, key_cache size etc. at present.
   * 
   * @param conn the connection to use
   * @param colFamilyName the name of the column family (table) to create.
   * @param inputMeta the row meta information on the incoming fields to be inserted
   * into this new column family
   * @param keyIndex the index of the incoming field that is to be used as the key
   * for the new column family
   * @param compressCQL true if the CQL statement is to be compressed before sending
   * to the server
   * @throws Exception if a problem occurs.
   */
  public static void createColumnFamily(CassandraConnection conn, 
      String colFamilyName, RowMetaInterface inputMeta,
      int keyIndex, boolean compressCQL) throws Exception {
    
    // TODO handle optional keywords for column family creation - default comparator, key_cache_size etc.
    // Will require more UI and Meta class setters/getters
    
    StringBuffer buff = new StringBuffer();
    buff.append("CREATE COLUMNFAMILY " + colFamilyName);
    ValueMetaInterface kvm = inputMeta.getValueMeta(keyIndex);
    buff.append(" (KEY ").append("'" 
        + CassandraColumnMetaData.getCQLTypeForValueMeta(kvm) + "'");
    buff.append(" PRIMARY KEY");
    
    List<ValueMetaInterface> indexedVals = new ArrayList<ValueMetaInterface>();
    if (inputMeta.size() > 1) {
      
      //boolean first = true;
      for (int i = 0; i < inputMeta.size(); i++) {
        if (i != keyIndex) {
          ValueMetaInterface vm = inputMeta.getValueMeta(i);
          if (vm.getStorageType() == ValueMetaInterface.STORAGE_TYPE_INDEXED) {
            indexedVals.add(vm);
          }
          String colName = vm.getName();
          String colType = "'" + CassandraColumnMetaData.getCQLTypeForValueMeta(vm) + "'";
          //  if (!first) {
          buff.append(", ");
          //  first = false;
          /*          } else {
            buff.append(colName).append(" ");
          } */
          buff.append("'" + colName + "'").append(" ");
          buff.append(colType);
        }
      }
    }
    
    // abuse the comment field to store any indexed values :-)
    if (indexedVals.size() == 0) {
      buff.append(");");
    } else {
      buff.append(" WITH comment = '@@@");
      int count = 0;
      for (ValueMetaInterface vm : indexedVals) {
        String colName = vm.getName();
        Object[] legalVals = vm.getIndex();
        buff.append(colName).append(":{");
        for (int i = 0; i < legalVals.length; i++) {
          buff.append(legalVals[i].toString());
          if (i != legalVals.length - 1) {
            buff.append(",");
          }
        }
        buff.append("}");
        if (count != indexedVals.size() - 1) {
          buff.append(";");
        }
        count++;
      }
      buff.append("@@@'");
    }
    
//    System.out.println(buff.toString());
    byte[] toSend = null;
    if (compressCQL) {
      toSend = compressQuery(buff.toString(), Compression.GZIP);
    } else {
      toSend = buff.toString().getBytes(Charset.forName(CassandraColumnMetaData.UTF8));
    }
    conn.getClient().execute_cql_query(ByteBuffer.wrap(toSend), 
        compressCQL ? Compression.GZIP : Compression.NONE);    
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
