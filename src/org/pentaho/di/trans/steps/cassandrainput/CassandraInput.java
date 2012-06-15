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
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.Compression;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.CqlResult;
import org.apache.cassandra.thrift.CqlRow;
import org.apache.cassandra.thrift.KeyRange;
import org.apache.cassandra.thrift.KeySlice;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;
import org.pentaho.cassandra.CassandraColumnMetaData;
import org.pentaho.cassandra.CassandraConnection;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.i18n.BaseMessages;
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

      m_data = (CassandraInputData)sdi;
      m_meta = (CassandraInputMeta)smi;
      
      // Get the connection to Cassandra
      String hostS = environmentSubstitute(m_meta.getCassandraHost());
      String portS = environmentSubstitute(m_meta.getCassandraPort());
      String timeoutS = environmentSubstitute(m_meta.getSocketTimeout());
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

      logBasic(BaseMessages.getString(CassandraInputMeta.PKG, 
          "CassandraInput.Info.Connecting", hostS, portS, keyspaceS));
      try {
        if (Const.isEmpty(timeoutS)) {
          m_connection = CassandraInputData.getCassandraConnection(hostS, 
              Integer.parseInt(portS), userS, passS);
        } else {
          m_connection = CassandraInputData.getCassandraConnection(hostS, 
              Integer.parseInt(portS), userS, passS, Integer.parseInt(timeoutS));
        }
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
      try {
        logBasic(BaseMessages.getString(CassandraInputMeta.PKG, 
            "CassandraInput.Info.GettintMetaData", colFamName));
        m_cassandraMeta = new CassandraColumnMetaData(m_connection, colFamName);
      } catch (Exception e) {
        closeConnection();
        throw new KettleException(e.getMessage(), e);
      }
      
      String queryS = environmentSubstitute(m_meta.getCQLSelectQuery());
      Compression compression = 
        m_meta.getUseCompression() ? Compression.GZIP : Compression.NONE;
      try {
        if (!m_meta.getUseThriftIO()) {
          logBasic(BaseMessages.getString(CassandraInputMeta.PKG, 
              "CassandraInput.Info.ExecutingQuery", queryS, 
              (m_meta.getUseCompression() 
                  ? BaseMessages.getString(CassandraInputMeta.PKG, "CassandraInput.Info.UsingGZIPCompression") 
                      : ""))); 

          byte[] queryBytes = (m_meta.getUseCompression() ? 
              CassandraInputData.compressQuery(queryS, compression) : queryS.getBytes());

          // In Cassandra 1.1 the version of CQL to use can be set programatically. The default
          // is to use CQL v 2.0.0
          // m_connection.getClient().set_cql_version("3.0.0");
          CqlResult result = m_connection.getClient().
          execute_cql_query(ByteBuffer.wrap(queryBytes), compression);
          m_resultIterator = result.getRowsIterator();
        } else if (m_meta.getOutputKeyValueTimestampTuples()) {
          // --------------- use thrift IO (only applicable for <key, value> tuple mode at present) ----------
          List<String> userCols = (m_meta.m_specificCols != null && m_meta.m_specificCols.size() > 0)
            ? m_meta.m_specificCols : null;
          m_data.sliceModeInit(m_cassandraMeta, userCols, m_meta.m_rowLimit, m_meta.m_colLimit, 
              m_meta.m_rowBatchSize, m_meta.m_colBatchSize);
          List<Object[]> batch = 
            m_data.cassandraRowToKettleTupleSliceMode(m_cassandraMeta, m_connection);
          
          while (batch != null) {
            for (Object[] r : batch) {
              putRow(m_data.getOutputRowMeta(), r);
              if (log.isRowLevel()) {
                log.logRowlevel(toString(), "Outputted row #" + getProcessed() 
                    + " : " + r);
              }
            }
            batch = 
              m_data.cassandraRowToKettleTupleSliceMode(m_cassandraMeta, m_connection);
          }
          // done
          closeConnection();
          setOutputDone();
          return false;
          
          // --------------- end thrift IO mode ----------------------------------------------------------
        }
      } catch (Exception e) {
        closeConnection();
        throw new KettleException(e.getMessage(), e);
      }
    }
    
    if (m_resultIterator.hasNext()) {
      CqlRow nextRow = m_resultIterator.next();
      Object[] outputRowData = null;
      
      if (m_meta.getOutputKeyValueTimestampTuples()) {
        Iterator<Column> columnIterator = nextRow.getColumnsIterator();
        
        // The key always appears to be the first column in the list (even though it is separately
        // avaliable via CqlRow.getKey(). We discard it here because testing for a column named 
        // "KEY" only works if column names are textual
        // ARGHHHHH! - this assumption is only true for wildcard queries!!!!!! (i.e. select *)!!!!!!
        // So select col1, col2 etc. or ranges (which we don't support) will not include the row key
        // as the first column
        if (m_meta.m_isSelectStarQuery) {
          columnIterator.next(); // throw away the key column
        }
        
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
    if (isStopped() && stopped == true) {
      return;
    }
    super.setStopped(stopped);
    
    if (stopped) {
      closeConnection();
    }
  }
 
  protected void closeConnection() {
    if (m_connection != null) {
      logBasic(BaseMessages.getString(CassandraInputMeta.PKG, 
          "CassandraInput.Info.ClosingConnection"));
      m_connection.close();
    }
  }
}
