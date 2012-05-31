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

package org.pentaho.di.trans.steps.cassandraoutput;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.cassandra.CassandraColumnMetaData;
import org.pentaho.cassandra.CassandraConnection;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Class providing an output step for writing data to a cassandra table (column family). 
 * Can create the specified column family (if it doesn't already exist) and can update
 * column family meta data.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
public class CassandraOutput extends BaseStep implements StepInterface {
  
  protected CassandraOutputMeta m_meta;
  protected CassandraOutputData m_data;  
  
  public CassandraOutput(StepMeta stepMeta, StepDataInterface stepDataInterface,
      int copyNr, TransMeta transMeta, Trans trans) {
    
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);    
  }
  
  /** Column meta data and schema information */
  protected CassandraColumnMetaData m_cassandraMeta;
  
  /** Holds batch insert CQL statement */
  protected StringBuilder m_batchInsert;
  
  /** Current batch of rows to insert */
  protected List<Object[]> m_batch;
  
  /** The number of rows seen so far for this batch */
  protected int m_rowsSeen;
  
  /** The batch size to use */
  protected int m_batchSize = 100;
  
  /** The consistency to use - null means to use the cassandra default */
  protected String m_consistency = null;
  
  /** The name of the column family (table) to write to */
  protected String m_columnFamilyName;

  /** The index of the key field in the incoming rows */
  protected int m_keyIndex = -1;

  protected int m_cqlBatchInsertTimeout = 0;

  /** Default batch split factor */
  protected int m_batchSplitFactor = 10;

  protected void initialize(StepMetaInterface smi, StepDataInterface sdi) 
    throws KettleException {

      m_meta = (CassandraOutputMeta)smi;
      m_data = (CassandraOutputData)sdi;
      
      first = false;
      m_rowsSeen = 0;

      // Get the connection to Cassandra
      String hostS = environmentSubstitute(m_meta.getCassandraHost());
      String portS = environmentSubstitute(m_meta.getCassandraPort());
      String userS = m_meta.getUsername();
      String passS = m_meta.getPassword();
      String batchTimeoutS = environmentSubstitute(m_meta.getCQLBatchInsertTimeout());
      String batchSplitFactor = environmentSubstitute(m_meta.getCQLSubBatchSize());      

      if (!Const.isEmpty(userS) && !Const.isEmpty(passS)) {
        userS = environmentSubstitute(userS);
        passS = environmentSubstitute(passS);
      }
      String keyspaceS = environmentSubstitute(m_meta.getCassandraKeyspace());
      m_columnFamilyName = environmentSubstitute(m_meta.getColumnFamilyName());
      String keyField = environmentSubstitute(m_meta.getKeyField());

      try {

        if (!Const.isEmpty(batchTimeoutS)) {
          try {
            m_cqlBatchInsertTimeout = Integer.parseInt(batchTimeoutS);
            if (m_cqlBatchInsertTimeout < 500) {
              logBasic("Using minimum batch insert timeout of 500 milliseconds");
              m_cqlBatchInsertTimeout = 500;
            }
          } catch (NumberFormatException e) {
            logError("Can't parse batch insert timeout - setting to 10,000");
            m_cqlBatchInsertTimeout = 10000;
          }
        }

        if (!Const.isEmpty(batchSplitFactor)) {
          try {
            m_batchSplitFactor = Integer.parseInt(batchSplitFactor);
          } catch (NumberFormatException e) {
            logError("Can't parse sub batch size - setting to 10");
          }
        }

        if (Const.isEmpty(hostS) || Const.isEmpty(portS) || Const.isEmpty(keyspaceS)) {
          throw new KettleException("Some connection details are missing!!");
        }

        if (Const.isEmpty(m_columnFamilyName)) {
          throw new KettleException("No column family (table) has been specified!");
        }      

        if (Const.isEmpty(keyField)) {
          throw new KettleException("The incoming field to use as the key for inserting " +
          "has not been specified!");
        }

        // check that the specified key field is present in the incoming data
        m_keyIndex = getInputRowMeta().indexOfValue(keyField);
        if (m_keyIndex < 0) {
          throw new KettleException("Can't find key field '" + keyField + "' in the incoming " +
            "data!");
        }

        logBasic("Connecting to Cassandra node at '" + hostS + ":" + portS + "' using " +
            "keyspace '" + keyspaceS +"'...");
        CassandraConnection connection = null;        

        try {
          connection = openConnection();

          if (!CassandraColumnMetaData.columnFamilyExists(connection, m_columnFamilyName)) {
            if (m_meta.getCreateColumnFamily()) {
              // create the column family (table)
              boolean result = CassandraOutputData.createColumnFamily(connection, m_columnFamilyName, getInputRowMeta(), m_keyIndex, 
                  m_meta.getUseCompression());
              if (!result) {
                throw new KettleException("Need at least one incoming field apart from the key!");
              }
            } else {
              throw new KettleException("Column family '" + m_columnFamilyName + "' does not" +
                  " exist in keyspace '" + keyspaceS + "'. Turn on the " +
                  "create column family option if you want " +
                  "to have this column family created automatically " +
              "using the incoming field meta data.");
            }
          }                


          // get the column family meta data

          logBasic("Getting meta data for column family '" + m_columnFamilyName + "'");
          m_cassandraMeta = new CassandraColumnMetaData(connection, m_columnFamilyName);

          // check that we have at least one incoming field apart from the key
          if (CassandraOutputData.numFieldsToBeWritten(m_columnFamilyName, getInputRowMeta(), 
              m_keyIndex, m_cassandraMeta, m_meta.getInsertFieldsNotInMeta()) < 2) {
            throw new KettleException("Must insert at least one other field apart from the key!");
          }



          // output (downstream) is the same as input
          m_data.setOutputRowMeta(getInputRowMeta());

          String batchSize = environmentSubstitute(m_meta.getBatchSize());
          if (!Const.isEmpty(batchSize)) {
            try {
              m_batchSize = Integer.parseInt(batchSize);
            } catch (NumberFormatException e) {
              logError("Can't parse batch size - setting to 100");
              m_batchSize = 100;
            }                        
          } else {
            throw new KettleException("No batch size set!");
          }

          if (m_meta.getUpdateCassandraMeta()) {
            // Update cassandra meta data for unknown incoming fields?

            CassandraOutputData.updateCassandraMeta(connection, m_columnFamilyName, 
                getInputRowMeta(), m_keyIndex, m_cassandraMeta);
          }

          // Truncate (remove all data from) column family first?
          if (m_meta.getTruncateColumnFamily()) {
            CassandraOutputData.truncateColumnFamily(connection, m_columnFamilyName);

          }

          // Try to execute any apriori CQL commands?
          if (!Const.isEmpty(m_meta.getAprioriCQL())) {
            String aprioriCQL = environmentSubstitute(m_meta.getAprioriCQL());
            logBasic("Executing the following CQL prior to writing to column family '" 
                + m_columnFamilyName + "'\n\n" + aprioriCQL);
            CassandraOutputData.executeAprioriCQL(connection, aprioriCQL, log, 
                m_meta.getUseCompression());
          }
        } finally {
          if (connection != null) {
            closeConnection(connection);
            connection = null;
          }
        }

        m_consistency = environmentSubstitute(m_meta.getConsistency());      
        m_batchInsert = CassandraOutputData.newBatch(m_batchSize, m_consistency);
        m_batch = new ArrayList<Object[]>();


      } catch (Exception ex) {
        logError("A problem occurred durining initialization of the step", ex);
      }
  }
  
  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
    throws KettleException {
    
    Object[] r = getRow();
    
    if (r == null) {
      // no more output
      
      // flush the last batch
      if (m_rowsSeen > 0) {
        doBatch();
      }
      m_batchInsert = null;
      m_batch = null;
      
      // clean up/close connections
//      closeConnection();
      setOutputDone();
      return false;
    }
    
    if (first) {
      initialize(smi, sdi);
    }
        
    m_batch.add(r);
    m_rowsSeen++;
    
    if (m_rowsSeen == m_batchSize) {
      doBatch();    
    }
        
    return true;
  }
  
  protected void doBatch() throws KettleException {
    
    try {
      doBatch(m_batch);
    } catch (Exception e) {      
      logError("Commit failed - " + m_batchInsert.toString(), e);
      throw new KettleException(e.fillInStackTrace());
    }
    
    // ready for a new batch
    m_batch.clear();
    m_rowsSeen = 0;
  }
  
  protected void doBatch(List<Object[]> batch) throws Exception {
    // stopped?
    if (isStopped()) {
      logDebug("Stopped, skipping batch...");
      return;
    }
    // ignore empty batch
    if (batch == null || batch.isEmpty()) {
      logDebug("Empty batch, skipping processing...");
      return;
    }
    // construct CQL and commit
    CassandraConnection connection = null;
    int size = batch.size();
    try {
      // construct CQL
      m_batchInsert = CassandraOutputData.newBatch(m_batchSize,
          m_consistency);
      for (Object[] r : batch) {
        // add the row to the batch
        CassandraOutputData.addRowToBatch(m_batchInsert,
            m_columnFamilyName, getInputRowMeta(), m_keyIndex, r,
            m_cassandraMeta, m_meta.getInsertFieldsNotInMeta());
      }
      CassandraOutputData.completeBatch(m_batchInsert);
      // commit
      connection = openConnection();
      logDetailed("Committing batch to column family '" 
          + m_columnFamilyName + "' (" + size + " rows)");
      // CassandraOutputData.commitBatch(m_batchInsert, connection,
      // m_meta.getUseCompression(), m_connTimeout);
      CassandraOutputData.commitBatch(m_batchInsert, connection,
          m_meta.getUseCompression(), m_cqlBatchInsertTimeout);
    } catch (Exception e) {
      closeConnection(connection);
      connection = null;
      logDetailed(
          "Failed to insert batch (" + size + " rows) - "
          + e.getMessage(), e);
      logDetailed("Will now try splitting into sub-batches...");
      
      // is it possible to divide and conquer?
      if (size == 1) {
        // single error row - found it!
        if (getStepMeta().isDoingErrorHandling()) { 
          putError(getInputRowMeta(), batch.get(0), 1L, e.getMessage(),
              null, "ERR_INSERT01");
        }
      } else if (size > m_batchSplitFactor) {
        // split into smaller batches and try separately
        List<Object[]> subBatch = new ArrayList<Object[]>();
        while (batch.size() > m_batchSplitFactor) {
          while (subBatch.size() < m_batchSplitFactor
              && batch.size() > 0) {
            // remove from the right - avoid internal shifting
            subBatch.add(batch.remove(batch.size() - 1));
          }
          doBatch(subBatch);
          subBatch.clear();
        }
        doBatch(batch);
      } else {
        // try each row individually
        List<Object[]> subBatch = new ArrayList<Object[]>();
        while (batch.size() > 0) {
          subBatch.clear();
          // remove from the right - avoid internal shifting
          subBatch.add(batch.remove(batch.size() - 1));
          doBatch(subBatch);
        }
      }
    } finally {
      closeConnection(connection);
      connection = null;
    }
  }    
  
  public void setStopped(boolean stopped) {
    if (isStopped() && stopped == true) {
      return;
    }
    
    super.setStopped(stopped);    
  }
  
  protected CassandraConnection openConnection() throws KettleException {
    // Get the connection to Cassandra
    String hostS = environmentSubstitute(m_meta.getCassandraHost());
    String portS = environmentSubstitute(m_meta.getCassandraPort());
    String userS = m_meta.getUsername();
    String passS = m_meta.getPassword();
    String timeoutS = environmentSubstitute(m_meta.getSocketTimeout());         
    
    if (!Const.isEmpty(userS) && !Const.isEmpty(passS)) {
      userS = environmentSubstitute(userS);
      passS = environmentSubstitute(passS);
    }
    String keyspaceS = environmentSubstitute(m_meta.getCassandraKeyspace());

    CassandraConnection connection = null;
    
    try {
      if (Const.isEmpty(timeoutS)) {
        connection = CassandraOutputData.getCassandraConnection(hostS, 
            Integer.parseInt(portS), userS, passS);
      } else {
        int sockTimeout = 30000;
        try {
          sockTimeout = Integer.parseInt(timeoutS);
        } catch (NumberFormatException e) {
          logError("Can't parse socket timeout - setting to 30,000");
        }
        connection = CassandraOutputData.getCassandraConnection(hostS, 
            Integer.parseInt(portS), userS, passS, sockTimeout);
      }
      connection.setKeyspace(keyspaceS);
    } catch (Exception ex) {
      closeConnection(connection);
      throw new KettleException(ex.getMessage(), ex);
    }
        
    return connection;
  }
  
  protected void closeConnection(CassandraConnection conn) {
    if (conn != null) {
      logBasic("Closing connection...");
      conn.close();
    }
  }    
}
