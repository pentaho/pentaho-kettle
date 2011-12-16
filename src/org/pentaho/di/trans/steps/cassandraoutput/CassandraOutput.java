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
  
  protected TransMeta m_transMeta;
  
  public CassandraOutput(StepMeta stepMeta, StepDataInterface stepDataInterface,
      int copyNr, TransMeta transMeta, Trans trans) {
    
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    
    m_transMeta = transMeta;    
  }

  /** Connection to cassandra */
  protected CassandraConnection m_connection;
  
  /** Column meta data and schema information */
  protected CassandraColumnMetaData m_cassandraMeta;
  
  /** Holds batch insert CQL statement */
  protected StringBuilder m_batchInsert;
  
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
      
      // clean up/close connections
      closeConnection();
      setOutputDone();
      return false;
    }
    
    if (first) {
      first = false;
      m_rowsSeen = 0;
      m_meta = (CassandraOutputMeta)smi;
      m_data = (CassandraOutputData)sdi;
      
      // Get the connection to Cassandra
      String hostS = m_transMeta.environmentSubstitute(m_meta.getCassandraHost());
      String portS = m_transMeta.environmentSubstitute(m_meta.getCassandraPort());
      String userS = m_meta.getUsername();
      String passS = m_meta.getPassword();
      if (!Const.isEmpty(userS) && !Const.isEmpty(passS)) {
        userS = m_transMeta.environmentSubstitute(userS);
        passS = m_transMeta.environmentSubstitute(passS);
      }
      String keyspaceS = m_transMeta.environmentSubstitute(m_meta.getCassandraKeyspace());
      m_columnFamilyName = m_transMeta.environmentSubstitute(m_meta.getColumnFamilyName());
      String keyField = m_transMeta.environmentSubstitute(m_meta.getKeyField());
      
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
      
      try {
        m_connection = CassandraOutputData.getCassandraConnection(hostS, 
            Integer.parseInt(portS), userS, passS);
        m_connection.setKeyspace(keyspaceS);
      } catch (Exception ex) {
        closeConnection();
        throw new KettleException(ex.fillInStackTrace());
      }
      
      try {
        if (!CassandraColumnMetaData.columnFamilyExists(m_connection, m_columnFamilyName)) {
          if (m_meta.getCreateColumnFamily()) {
            // create the column family (table)
            boolean result = CassandraOutputData.createColumnFamily(m_connection, m_columnFamilyName, getInputRowMeta(), m_keyIndex, 
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
      } catch (Exception e) {
        closeConnection();
        throw new KettleException(e.fillInStackTrace());
      }
      
      // get the column family meta data
      try {
        logBasic("Getting meta data for column family '" + m_columnFamilyName + "'");
        m_cassandraMeta = new CassandraColumnMetaData(m_connection, m_columnFamilyName);
        
        // check that we have at least one incoming field apart from the key
        if (CassandraOutputData.numFieldsToBeWritten(m_columnFamilyName, getInputRowMeta(), 
            m_keyIndex, m_cassandraMeta, m_meta.getInsertFieldsNotInMeta()) < 2) {
          throw new KettleException("Must insert at least one other field apart from the key!");
        }
      } catch (Exception e) {
        closeConnection();
        throw new KettleException(e.fillInStackTrace());
      }
      
      
      // output (downstream) is the same as input
      m_data.setOutputRowMeta(getInputRowMeta());
      
      String batchSize = m_transMeta.environmentSubstitute(m_meta.getBatchSize());
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
        try {
          CassandraOutputData.updateCassandraMeta(m_connection, m_columnFamilyName, 
              getInputRowMeta(), m_keyIndex, m_cassandraMeta);
        } catch (Exception e) {
          closeConnection();
          throw new KettleException(e.fillInStackTrace());
        }
      }
      
      // Truncate (remove all data from) column family first?
      if (m_meta.getTruncateColumnFamily()) {
        try {
          CassandraOutputData.truncateColumnFamily(m_connection, m_columnFamilyName);
        } catch (Exception e) {
          closeConnection();
          throw new KettleException(e.fillInStackTrace());
        }
      }
      
      // Try to execute any apriori CQL commands?
      if (!Const.isEmpty(m_meta.getAprioriCQL())) {
        String aprioriCQL = m_transMeta.environmentSubstitute(m_meta.getAprioriCQL());
        logBasic("Executing the following CQL prior to writing to column family '" 
            + m_columnFamilyName + "'\n\n" + aprioriCQL);
        CassandraOutputData.executeAprioriCQL(m_connection, aprioriCQL, log, 
            m_meta.getUseCompression());

      }
      
      m_consistency = m_transMeta.environmentSubstitute(m_meta.getConsistency());      
      m_batchInsert = CassandraOutputData.newBatch(m_batchSize, m_consistency);            
    }
    
    // add the row to the batch
    CassandraOutputData.addRowToBatch(m_batchInsert, m_columnFamilyName, getInputRowMeta(), 
        m_keyIndex, r, m_cassandraMeta, m_meta.getInsertFieldsNotInMeta());
    m_rowsSeen++;
    
    if (m_rowsSeen == m_batchSize) {
      doBatch();    
    }
    
    
    return true;
  }
  
  protected void doBatch() throws KettleException {
    
    // execute the CQL insert for this batch      
    logDetailed("Committing batch to column family '" 
        + m_columnFamilyName + "'");
    CassandraOutputData.completeBatch(m_batchInsert);
//    System.out.println(m_batchInsert.toString());
    try {
      CassandraOutputData.commitBatch(m_batchInsert, m_connection, 
          m_meta.getUseCompression());
    } catch (Exception e) {
      closeConnection();
      System.out.println(m_batchInsert.toString());
      throw new KettleException(e.fillInStackTrace());
    }
    
    // ready for a new batch
    m_batchInsert = CassandraOutputData.newBatch(m_batchSize, m_consistency);
    m_rowsSeen = 0;
  }
  
  public void setStopped(boolean stopped) {
    super.setStopped(stopped);
    
    if (stopped) {
      closeConnection();
    }
  }
  
  protected void closeConnection() {
    if (m_connection != null) {
      logBasic("Closing connection...");
      m_connection.close();
    }
  }    
}
