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

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.Compression;
import org.apache.cassandra.thrift.CqlResult;
import org.apache.cassandra.thrift.CqlRow;
import org.pentaho.cassandra.CassandraColumnMetaData;
import org.pentaho.cassandra.CassandraConnection;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Class providing an input step for reading data from a table (column family)
 * in Cassandra. Accesses the schema information stored in Cassandra for
 * type information.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
public class CassandraInput extends BaseStep implements StepInterface {
  
  protected CassandraInputMeta m_meta;
  protected CassandraInputData m_data;  
  
  public CassandraInput(StepMeta stepMeta, StepDataInterface stepDataInterface,
      int copyNr, TransMeta transMeta, Trans trans) {
    
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);    
  }
  
  /** Connection to cassandra */
  protected CassandraConnection m_connection;
  
  /** Column meta data and schema information */
  protected CassandraColumnMetaData m_cassandraMeta;
  
  /** For iterating over a result set */
  protected Iterator<CqlRow> m_resultIterator;
  
  /** 
   * map of indexes into the output field structure (key is special - it's always the 
   * first field in the output row meta 
   */
  protected Map<String, Integer> m_outputFormatMap = new HashMap<String, Integer>();
  
  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
    throws KettleException {
    
    if (first) {
      first = false;
      // m_meta = (CassandraInputMeta)smi;
      m_data = (CassandraInputData)sdi;
      m_meta = (CassandraInputMeta)smi;
      
      // Get the connection to Cassandra
      String hostS = environmentSubstitute(m_meta.getCassandraHost());
      String portS = environmentSubstitute(m_meta.getCassandraPort());
      String userS = m_meta.getUsername();
      String passS = m_meta.getPassword();
      if (!Const.isEmpty(userS) && !Const.isEmpty(passS)) {
        userS = environmentSubstitute(userS);
        passS = environmentSubstitute(passS);
      }
      String keyspaceS = environmentSubstitute(m_meta.getCassandraKeyspace());
      
      if (Const.isEmpty(hostS) || Const.isEmpty(portS) || Const.isEmpty(keyspaceS)) {
        throw new KettleException("Some connection details are missing!!");
      }

      logBasic("Connecting to Cassandra node at '" + hostS + ":" + portS + "' using " +
      		"keyspace '" + keyspaceS +"'...");      
      try {
        m_connection = CassandraInputData.getCassandraConnection(hostS, 
            Integer.parseInt(portS), userS, passS);
        m_connection.setKeyspace(keyspaceS);
      } catch (Exception ex) {
        closeConnection();
        throw new KettleException(ex.getMessage(), ex);
      }
      
      // check the source column family (table) first
      String colFamName = m_data.
        getColumnFamilyNameFromCQLSelectQuery(
            environmentSubstitute(m_meta.getCQLSelectQuery()));
      
      if (Const.isEmpty(colFamName)) {
        throw new KettleException("SELECT query does not seem to contain the name" +
        		" of a column family to read from!");
      }
      
      try {
        if (!CassandraColumnMetaData.columnFamilyExists(m_connection, colFamName)) {
          throw new KettleException("The column family '" + colFamName + "' does not " +
              "seem to exist in the keyspace '" + keyspaceS);
        }
      } catch (Exception ex) {
        closeConnection();
        throw new KettleException(ex.getMessage(), ex);
      }

      // set up the output row meta
      m_data.setOutputRowMeta(new RowMeta());
      m_meta.getFields(m_data.getOutputRowMeta(), getStepname(), null, null, this);
      
      // check that there are some outgoing fields!
      if (m_data.getOutputRowMeta().size() == 0) {
        throw new KettleException("It doesn't look like the query will produce any " +
        		"output fields!");
      }
      
      // set up the lookup map
      if (!m_meta.getOutputKeyValueTimestampTuples()) {
        for (int i = 0; i < m_data.getOutputRowMeta().size(); i++) {
          String fieldName = m_data.getOutputRowMeta().getValueMeta(i).getName();
          m_outputFormatMap.put(fieldName, i);
        }
      }
      
      // column family name (key) is the first field output
      // String colFamName = m_data.getOutputRowMeta().getValueMeta(0).getName();
      try {
        logBasic("Getting meta data for column family '" + colFamName + "'");
        m_cassandraMeta = new CassandraColumnMetaData(m_connection, colFamName);
      } catch (Exception e) {
        closeConnection();
        throw new KettleException(e.getMessage(), e);
      }
      
      String queryS = environmentSubstitute(m_meta.getCQLSelectQuery());
      Compression compression = 
        m_meta.getUseCompression() ? Compression.GZIP : Compression.NONE;
      try {
        logBasic("Executing query '" + queryS + "'" 
            + (m_meta.getUseCompression() ? " (using GZIP query compression)" : "") 
            + "...");
        byte[] queryBytes = (m_meta.getUseCompression() ? 
            CassandraInputData.compressQuery(queryS, compression) : queryS.getBytes());
        CqlResult result = m_connection.getClient().
          execute_cql_query(ByteBuffer.wrap(queryBytes), compression);
        m_resultIterator = result.getRowsIterator();
      } catch (Exception e) {
        closeConnection();
        throw new KettleException(e.getMessage(), e);
      }
    }
    
    if (m_resultIterator.hasNext()) {
      CqlRow nextRow = m_resultIterator.next();
      Object[] outputRowData = null;
      
      if (m_meta.getOutputKeyValueTimestampTuples()) {
        //System.err.println("Number of columns in row: " + nextRow.getColumnsSize());
        Iterator<Column> columnIterator = nextRow.getColumnsIterator();
        
        while ((outputRowData =
          m_data.cassandraRowToKettleTupleMode(m_cassandraMeta, nextRow, 
              columnIterator)) != null) {
          
          putRow(m_data.getOutputRowMeta(), outputRowData);
          
          if (log.isRowLevel()) {
            log.logRowlevel(toString(), "Outputted row #" + getProcessed() 
                + " : " + outputRowData);
          }
        }
      } else { 
        outputRowData = 
          m_data.cassandraRowToKettle(m_cassandraMeta, nextRow, m_outputFormatMap);
        
        // output the row
        putRow(m_data.getOutputRowMeta(), outputRowData);
        
        if (log.isRowLevel()) {
          log.logRowlevel(toString(), "Outputted row #" + getProcessed() 
              + " : " + outputRowData);
        }
      }            
    } else {
      //m_connection.close();
      closeConnection();
      setOutputDone();
      return false;
    }       
    
    if (checkFeedback(getProcessed())) {
      logBasic("Read " + getProcessed() + " rows from Cassandra");
    }
    
    return true;
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
