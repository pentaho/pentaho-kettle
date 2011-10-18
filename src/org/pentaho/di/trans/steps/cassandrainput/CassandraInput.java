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
  
  protected TransMeta m_transMeta;
  
  public CassandraInput(StepMeta stepMeta, StepDataInterface stepDataInterface,
      int copyNr, TransMeta transMeta, Trans trans) {
    
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    
    m_transMeta = transMeta;    
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
      String hostS = m_transMeta.environmentSubstitute(m_meta.getCassandraHost());
      String portS = m_transMeta.environmentSubstitute(m_meta.getCassandraPort());
      String keyspaceS = m_transMeta.environmentSubstitute(m_meta.getCassandraKeyspace());
      
      if (Const.isEmpty(hostS) || Const.isEmpty(portS) || Const.isEmpty(keyspaceS)) {
        throw new KettleException("Some connection details are missing!!");
      }

      logBasic("Connecting to Cassandra node at '" + hostS + ":" + portS + "' using " +
      		"keyspace '" + keyspaceS +"'...");      
      try {
        m_connection = CassandraInputData.getCassandraConnection(hostS, 
            Integer.parseInt(portS));
        m_connection.setKeyspace(keyspaceS);
      } catch (Exception ex) {
        closeConnection();
        throw new KettleException(ex.fillInStackTrace());
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
      for (int i = 0; i < m_data.getOutputRowMeta().size(); i++) {
        String fieldName = m_data.getOutputRowMeta().getValueMeta(i).getName();
        m_outputFormatMap.put(fieldName, i);
      }
      
      // column family name (key) is the first field output
      String colFamName = m_data.getOutputRowMeta().getValueMeta(0).getName();
      try {
        logBasic("Getting meta data for column family '" + colFamName + "'");
        m_cassandraMeta = new CassandraColumnMetaData(m_connection, colFamName);
      } catch (Exception e) {
        closeConnection();
        throw new KettleException(e.fillInStackTrace());
      }
      
      String queryS = m_transMeta.environmentSubstitute(m_meta.getCQLSelectQuery());
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
        throw new KettleException(e.fillInStackTrace());
      }
    }
    
    if (m_resultIterator.hasNext()) {
      CqlRow nextRow = m_resultIterator.next();
      
      Object[] outputRowData = 
        m_data.cassandraRowToKettle(m_cassandraMeta, nextRow, m_outputFormatMap);      
      
      // output the row
      putRow(m_data.getOutputRowMeta(), outputRowData);
      
      if (log.isRowLevel()) {
        log.logRowlevel(toString(), "Outputted row #" + getProcessed() 
            + " : " + outputRowData);
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
