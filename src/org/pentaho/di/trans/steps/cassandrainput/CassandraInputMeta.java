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

import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.cassandra.CassandraColumnMetaData;
import org.pentaho.cassandra.CassandraConnection;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/**
 * Class providing an input step for reading data from an Cassandra 
 * column family (table).
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
@Step(id = "CassandraInput", image = "Cassandra.png", name = "Cassandra Input", description="Reads data from a Cassandra table", categoryDescription="Big Data")
public class CassandraInputMeta extends BaseStepMeta implements StepMetaInterface {
  
  /** The host to contact */
  protected String m_cassandraHost = "localhost";
  
  /** The port that cassandra is listening on */
  protected String m_cassandraPort = "9160";
  
  /** Username for authentication */
  protected String m_username;
  
  /** Password for authentication */
  protected String m_password;
  
  /** The keyspace (database) to use */
  protected String m_cassandraKeyspace;
  
  /** Whether to use GZIP compression of CQL queries */
  protected boolean m_useCompression;
  
  /** The select query to execute */
  protected String m_cqlSelectQuery = "SELECT <fields> FROM <column family> WHERE <condition>;";
  
  protected boolean m_outputKeyValueTimestampTuples;
  
  /**
   * Set the cassandra node hostname to connect to
   * 
   * @param host the host to connect to
   */
  public void setCassandraHost(String host) {
    m_cassandraHost = host;
  }
  
  /**
   * Get the name of the cassandra node to connect to
   * 
   * @return the name of the cassandra node to connect to
   */
  public String getCassandraHost() {
    return m_cassandraHost;
  }
  
  /**
   * Set the port that cassandra is listening on
   * 
   * @param port the port that cassandra is listening on
   */
  public void setCassandraPort(String port) {
    m_cassandraPort = port;
  }
  
  /**
   * Get the port that cassandra is listening on
   * 
   * @return the port that cassandra is listening on
   */
  public String getCassandraPort() {
    return m_cassandraPort;
  }
  
  /**
   * Set the keyspace (db) to use
   * 
   * @param keyspace the keyspace to use
   */
  public void setCassandraKeyspace(String keyspace) {
    m_cassandraKeyspace = keyspace;
  }
  
  /**
   * Get the keyspace (db) to use
   * 
   * @return the keyspace (db) to use
   */
  public String getCassandraKeyspace() {
    return m_cassandraKeyspace;
  }
  
  /**
   * Set whether to compress (GZIP) CQL queries when transmitting them
   * to the server
   * 
   * @param c true if CQL queries are to be compressed
   */
  public void setUseCompression(boolean c) {
    m_useCompression = c;
  }
  
  /**
   * Get whether CQL queries will be compressed (GZIP) or not
   * 
   * @return true if CQL queries will be compressed when sending to the server
   */
  public boolean getUseCompression() {
    return m_useCompression;
  }
  
  /**
   * Set the CQL SELECT query to execute.
   * 
   * @param query the query to execute
   */
  public void setCQLSelectQuery(String query) {
    m_cqlSelectQuery = query;
  }
  
  /**
   * Get the CQL SELECT query to execute
   * 
   * @return the query to execute
   */
  public String getCQLSelectQuery() {
    return m_cqlSelectQuery;
  }
  
  /**
   * Set the username to authenticate with
   * 
   * @param un the username to authenticate with
   */
  public void setUsername(String un) {
    m_username = un;
  }
  
  /**
   * Get the username to authenticate with
   * 
   * @return the username to authenticate with
   */
  public String getUsername() {
    return m_username;
  }
  
  /**
   * Set the password to authenticate with
   * 
   * @param pass the password to authenticate with
   */
  public void setPassword(String pass) {
    m_password = pass;
  }
  
  /**
   * Get the password to authenticate with
   * 
   * @return the password to authenticate with
   */
  public String getPassword() {
    return m_password;
  }
  
  /**
   * Set whether to output key, column, timestamp tuples as rows rather
   * than standard row format.
   * 
   * @param o true if tuples are to be output
   */
  public void setOutputKeyValueTimestampTuples(boolean o) {
    m_outputKeyValueTimestampTuples = o;
  }
  
  /**
   * Get whether to output key, column, timestamp tuples as rows rather
   * than standard row format.
   * 
   * @return true if tuples are to be output
   */
  public boolean getOutputKeyValueTimestampTuples() {
    return m_outputKeyValueTimestampTuples;
  }
  
  public String getXML() {
    StringBuffer retval = new StringBuffer();
    
    if (!Const.isEmpty(m_cassandraHost)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("cassandra_host", 
          m_cassandraHost));
    }
    
    if (!Const.isEmpty(m_cassandraPort)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("cassandra_port", 
          m_cassandraPort));
    }
    
    if (!Const.isEmpty(m_username)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("username", 
          m_username));
    }
    
    if (!Const.isEmpty(m_password)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("password", 
          Encr.encryptPasswordIfNotUsingVariables(m_password)));
    }
    
    if (!Const.isEmpty(m_cassandraKeyspace)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("cassandra_keyspace", 
          m_cassandraKeyspace));
    }
    
    retval.append("\n    ").append(XMLHandler.addTagValue("use_compression", 
        m_useCompression));
    
    if (!Const.isEmpty(m_cqlSelectQuery)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("cql_select_query", 
          m_cqlSelectQuery));
    }
    
    retval.append("\n    ").append(XMLHandler.addTagValue("output_key_value_timestamp_tuples", 
        m_outputKeyValueTimestampTuples));
            
    return retval.toString();
  }
  
  public void loadXML(Node stepnode, List<DatabaseMeta> databases,
      Map<String, Counter> counters) throws KettleXMLException {
    m_cassandraHost = XMLHandler.getTagValue(stepnode, "cassandra_host");
    m_cassandraPort = XMLHandler.getTagValue(stepnode, "cassandra_port");
    m_username = XMLHandler.getTagValue(stepnode, "username");
    m_password = XMLHandler.getTagValue(stepnode, "password");
    if (!Const.isEmpty(m_password)) {
      m_password = Encr.decryptPasswordOptionallyEncrypted(m_password);
    }
    m_cassandraKeyspace = XMLHandler.getTagValue(stepnode, "cassandra_keyspace");
    m_cqlSelectQuery = XMLHandler.getTagValue(stepnode, "cql_select_query");
    m_useCompression = XMLHandler.getTagValue(stepnode, "use_compression").
      equalsIgnoreCase("Y");
    
    String kV = XMLHandler.getTagValue(stepnode, "output_key_value_timestamp_tuples");
    
    if (kV != null) {
      m_outputKeyValueTimestampTuples = kV.equalsIgnoreCase("Y");
    }
  }
  
  public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases,
      Map<String, Counter> counters) throws KettleException {
    m_cassandraHost = rep.getStepAttributeString(id_step, 0, "cassandra_host");
    m_cassandraPort = rep.getStepAttributeString(id_step, 0, "cassandra_port");
    m_username = rep.getStepAttributeString(id_step, 0, "username");
    m_password = rep.getStepAttributeString(id_step, 0, "password");
    if (!Const.isEmpty(m_password)) {
      m_password = Encr.decryptPasswordOptionallyEncrypted(m_password);
    }
    m_cassandraKeyspace = rep.getStepAttributeString(id_step, 0, "cassandra_keyspace");
    m_cqlSelectQuery = rep.getStepAttributeString(id_step, 0, "cql_select_query");
    m_useCompression = rep.getStepAttributeString(id_step, 0, "use_compression").
      equalsIgnoreCase("Y");
    
    String kV = rep.getStepAttributeString(id_step, 0, "output_key_value_timestamp_tuples");
    
    if (kV != null) {
      m_outputKeyValueTimestampTuples = kV.equalsIgnoreCase("Y");
    }
  }

  public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
      throws KettleException {
    if (!Const.isEmpty(m_cassandraHost)) {
      rep.saveStepAttribute(id_transformation, id_step, "cassandra_host",
          m_cassandraHost);
    }
    
    if (!Const.isEmpty(m_cassandraPort)) {
      rep.saveStepAttribute(id_transformation, id_step, "cassandra_port",
          m_cassandraPort);
    }
    
    if (!Const.isEmpty(m_username)) {
      rep.saveStepAttribute(id_transformation, id_step, "username",
          m_username);
    }
    
    if (!Const.isEmpty(m_password)) {
      rep.saveStepAttribute(id_transformation, id_step, "password",
          Encr.encryptPasswordIfNotUsingVariables(m_password));
    }
    
    if (!Const.isEmpty(m_cassandraKeyspace)) {
      rep.saveStepAttribute(id_transformation, id_step, "cassandra_keyspace",
          m_cassandraKeyspace);
    }
    
    rep.saveStepAttribute(id_transformation, id_step, "use_compression",
        m_useCompression);
    
    if (!Const.isEmpty(m_cqlSelectQuery)) {
      rep.saveStepAttribute(id_transformation, id_step, "cql_select_query",
          m_cqlSelectQuery);
    }
    
    rep.saveStepAttribute(id_transformation, id_step, "output_key_value_timestamp_tuples",
        m_outputKeyValueTimestampTuples);
  }

  public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
      StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output,
      RowMetaInterface info) {
    // TODO Auto-generated method stub
    
  }

  public StepInterface getStep(StepMeta stepMeta,
      StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans) {
    
    return new CassandraInput(stepMeta, stepDataInterface, copyNr,
        transMeta, trans);
  }

  public StepDataInterface getStepData() {
    return new CassandraInputData();
  }

  public void setDefault() {
    m_cassandraHost = "localhost";
    m_cassandraPort = "9160";
    m_cqlSelectQuery = "SELECT <fields> FROM <column family> WHERE <condition>;";
    m_useCompression = false;
  }  
  
  public void getFields(RowMetaInterface rowMeta, String origin, 
      RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) 
    throws KettleStepException {
    
    rowMeta.clear(); // start afresh - eats the input
    
    if (Const.isEmpty(m_cassandraKeyspace)) {
      // no keyspace!
      return;
    }
    
    String colFamName = null;
    if (!Const.isEmpty(m_cqlSelectQuery)) {            
      String subQ = space.environmentSubstitute(m_cqlSelectQuery);
      
      if (!subQ.toLowerCase().startsWith("select")) {
        // not a select statement!
        logError("No 'SELECT' in query!");
        return;
      }
      
      if (subQ.indexOf(';') < 0) {
        // query must end with a ';' or it will wait for more!
        logError("Query must be terminated by a ';'");
        return;
      }
      
      //subQ = subQ.toLowerCase();
      
      // strip off where clause (if any)      
      if (subQ.toLowerCase().lastIndexOf("where") > 0) {
        subQ = subQ.substring(0, subQ.toLowerCase().lastIndexOf("where"));
      }
      
      // first determine the source column family
      // look for a FROM that is surrounded by space
      int fromIndex = subQ.toLowerCase().indexOf("from");
      String tempS = subQ.toLowerCase();
      int offset = fromIndex;
      while (fromIndex > 0 && tempS.charAt(fromIndex - 1) != ' ' && 
          (fromIndex + 4 < tempS.length()) && tempS.charAt(fromIndex + 4) != ' ') {
        tempS = tempS.substring(fromIndex + 4, tempS.length());
        fromIndex = tempS.indexOf("from");
        offset += (4 + fromIndex);
      }
      
      fromIndex = offset;
//      int fromIndex = subQ.toLowerCase().lastIndexOf("from");
      if (fromIndex < 0) {
        logError("Must specify a column family using a 'FROM' clause");
        return; // no from clause
      }      
      
      colFamName = subQ.substring(fromIndex + 4, subQ.length()).trim();
      if (colFamName.indexOf(' ') > 0) {
        colFamName = colFamName.substring(0, colFamName.indexOf(' '));
      } else {
        colFamName = colFamName.replace(";", "");
      }
      
      if (colFamName.length() == 0) {
        return; // no column family specified
      }      
      
      // now determine if its a select * or specific set of columns
      String[] cols = null;
      if (subQ.indexOf("*") > 0) {
        // nothing special to do here
      } else {        
        String colsS = subQ.substring(subQ.indexOf('\''), fromIndex);
        cols = colsS.split(",");
      }
      
      // try and connect to get meta data
      String hostS = space.environmentSubstitute(m_cassandraHost);
      String portS = space.environmentSubstitute(m_cassandraPort);
      String userS = m_username;
      String passS = m_password;
      if (!Const.isEmpty(userS) && !Const.isEmpty(passS)) {
        userS = space.environmentSubstitute(m_username);
        passS = space.environmentSubstitute(m_password);
      }
      String keyspaceS = space.environmentSubstitute(m_cassandraKeyspace);
      CassandraConnection conn = null;
      try {
        conn = CassandraInputData.getCassandraConnection(hostS, 
            Integer.parseInt(portS), userS, passS);
        conn.setKeyspace(keyspaceS);        
      } catch (Exception ex) {
        logError(ex.getMessage(), ex);
        return;
      }
      try {
        CassandraColumnMetaData colMeta = new CassandraColumnMetaData(conn, colFamName);
        
        // Do the key first
        ValueMetaInterface km = colMeta.getValueMetaForKey();
        rowMeta.addValueMeta(km);
        
        if (getOutputKeyValueTimestampTuples()) {
          // special case where user has asked for all row keys, columns and
          // timestamps output as separate rows.
          ValueMetaInterface vm = new ValueMeta("ColumnName", ValueMetaInterface.TYPE_STRING);
          rowMeta.addValueMeta(vm);
          vm = new ValueMeta("ColumnValue", ValueMetaInterface.TYPE_STRING);
          rowMeta.addValueMeta(vm);
          vm = new ValueMeta("Timestamp", ValueMetaInterface.TYPE_INTEGER);
          rowMeta.addValueMeta(vm);
          
          conn.close();
          return;
        }
        
        if (cols == null) {
          // select * - use all the columns that are defined in the schema
          List<ValueMetaInterface> vms = colMeta.getValueMetasForSchema();
          for (ValueMetaInterface vm : vms) {
            rowMeta.addValueMeta(vm);
          }
        } else {
          // do the individual columns
          for (String col : cols) {
            col = col.trim();
            col = col.replace("'", "");
            col = col.replace("\"", "");
            if (!colMeta.columnExistsInSchema(col)) {
              // this one isn't known about in about in the schema - we can output it
              // as long as its values satisfy the default validator...
              logBasic("Query specifies column '" + col + "', however this column is " +
                        "not in the column family schema. The default column family " +
                        "validator will be used");
            }
            ValueMetaInterface vm = colMeta.getValueMetaForColumn(col);
            rowMeta.addValueMeta(vm);
          }
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        logBasic("Unable to connect to retrieve column meta data for '" + colFamName 
            + "' at this stage.", ex);
        return;
      } finally {
        if (conn != null) {
          conn.close();
        }
      }            
    }
  }

  /**
   * Get the UI for this step.
   *
   * @param shell a <code>Shell</code> value
   * @param meta a <code>StepMetaInterface</code> value
   * @param transMeta a <code>TransMeta</code> value
   * @param name a <code>String</code> value
   * @return a <code>StepDialogInterface</code> value
   */
  public StepDialogInterface getDialog(Shell shell, 
                                       StepMetaInterface meta,
                                       TransMeta transMeta, 
                                       String name) {

    return new CassandraInputDialog(shell, meta, transMeta, name);
  }
}
