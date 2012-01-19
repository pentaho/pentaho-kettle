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

import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
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
 * Class providing an output step for writing data to a cassandra table (column family). 
 * Can create the specified column family (if it doesn't already exist) and can update
 * column family meta data.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
@Step(id = "CassandraOutput", image = "Cassandra.png", name = "Cassandra Output", description="Writes to a Cassandra table", categoryDescription="Big Data")
public class CassandraOutputMeta extends BaseStepMeta implements
    StepMetaInterface {
  
  /** The host to contact */
  protected String m_cassandraHost = "localhost";
  
  /** The port that cassandra is listening on */
  protected String m_cassandraPort = "9160";
  
  /** The username to use for authentication */
  protected String m_username;
  
  /** The password to use for authentication */
  protected String m_password;
  
  /** The keyspace (database) to use */
  protected String m_cassandraKeyspace;
  
  /** The column family (table) to write to */
  protected String m_columnFamily = "";
  
  /** The consistency level to use - null or empty string result in the default */
  protected String m_consistency = "";
  
  /** 
   * The batch size - i.e. how many rows to collect before inserting them via
   * a batch CQL statement
   */
  protected String m_batchSize = "100";
  
  /** Whether to use GZIP compression of CQL queries */
  protected boolean m_useCompression = false;
  
  /** Whether to create the specified column family (table) if it doesn't exist */
  protected boolean m_createColumnFamily = true;
  
  /** The field in the incoming data to use as the key for inserts */
  protected String m_keyField = "";
  
  /** 
   * Whether or not to insert incoming fields that are not in the cassandra
   * table's meta data. Has no affect if the user has opted to update the meta
   * data for unknown incoming fields
   */
  protected boolean m_insertFieldsNotInMeta = false;
  
  /** 
   * Whether or not to initially update the column family meta data with
   * any unknown incoming fields
   */
  protected boolean m_updateCassandraMeta = false;
  
  /** Whether to truncate the column family (table) before inserting */
  protected boolean m_truncateColumnFamily = false;
  
  /** 
   * Any CQL statements to execute before inserting the first row. Can be used, for
   * example, to create secondary indexes on columns in a column family. 
   */
  protected String m_aprioriCQL = "";
  
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
   * Set the column family (table) to write to
   * 
   * @param colFam the name of the column family to write to
   */
  public void setColumnFamilyName(String colFam) {
    m_columnFamily = colFam;
  }
  
  /**
   * Get the name of the column family to write to
   * 
   * @return the name of the columm family to write to
   */
  public String getColumnFamilyName() {
    return m_columnFamily;
  }
  
  /**
   * Set whether to create the specified column family (table) if it
   * doesn't already exist
   * 
   * @param create true if the specified column family is to
   * be created if it doesn't already exist
   */
  public void setCreateColumnFamily(boolean create) {
    m_createColumnFamily = create;
  }
  
  /**
   * Get whether to create the specified column family (table) if it
   * doesn't already exist
   * 
   * @return true if the specified column family is to
   * be created if it doesn't already exist
   */
  public boolean getCreateColumnFamily() {
    return m_createColumnFamily;
  }
  
  /**
   * Set the consistency to use (e.g. ONE, QUORUM etc).
   * 
   * @param consistency the consistency to use
   */
  public void setConsistency(String consistency) {
    m_consistency = consistency;
  }
  
  /**
   * Get the consistency to use
   * 
   * @return the consistency
   */
  public String getConsistency() {
    return m_consistency;
  }
  
  /**
   * Set the batch size to use (i.e. max rows to send via a CQL batch insert
   * statement)
   * 
   * @param batchSize the max number of rows to send in each CQL batch
   * insert
   */
  public void setBatchSize(String batchSize) {
    m_batchSize = batchSize;
  }
  
  /**
   * Get the batch size to use (i.e. max rows to send via a CQL batch insert
   * statement)
   * 
   * @return the batch size.
   */
  public String getBatchSize() {
    return m_batchSize;
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
   * Set whether or not to insert any incoming fields that are not in
   * the Cassandra table's column meta data. This has no affect if the
   * user has opted to first update the meta data with any unknown columns.
   * 
   * @param insert true if incoming fields not found in the table's meta
   * data are to be inserted (and validated according to the default validator
   * for the table)
   */
  public void setInsertFieldsNotInMeta(boolean insert) {
    m_insertFieldsNotInMeta = insert;
  }
  
  /**
   * Get whether or not to insert any incoming fields that are not in
   * the Cassandra table's column meta data. This has no affect if the
   * user has opted to first update the meta data with any unknown
   * columns.
   * 
   * @return true if incoming fields not found in the table's meta
   * data are to be inserted (and validated according to the default validator
   * for the table)
   */
  public boolean getInsertFieldsNotInMeta() {
    return m_insertFieldsNotInMeta;
  }
  
  /**
   * Set the incoming field to use as the key for inserts
   * 
   * @param keyField the name of the incoming field to use
   * as the key
   */
  public void setKeyField(String keyField) {
    m_keyField = keyField;
  }  
  
  /**
   * Get the name of the incoming field to use as the key
   * for inserts
   * 
   * @return the name of the incoming field to use as the key
   * for inserts
   */
  public String getKeyField() {
    return m_keyField;
  }
  
  /**
   * Set whether to update the column family meta data with any
   * unknown incoming columns
   * 
   * @param u true if the meta data is to be updated with any
   * unknown incoming columns
   */
  public void setUpdateCassandraMeta(boolean u) {
    m_updateCassandraMeta = u;
  }
  
  /**
   * Get whether to update the column family meta data with
   * any unknown incoming columns
   * 
   * @return true if the meta data is to be updated with any unknown
   * incoming columns
   */
  public boolean getUpdateCassandraMeta() {
    return m_updateCassandraMeta;
  }
  
  /**
   * Set whether to first truncate (remove all data) the column
   * family (table) before inserting.
   * 
   * @param t true if the column family is to be initially truncated.
   */
  public void setTruncateColumnFamily(boolean t) {
    m_truncateColumnFamily = t;
  }
  
  /**
   * Get whether to first truncate (remove all data) the column
   * family (table) before inserting.
   * 
   * @return true if the column family is to be initially truncated.
   */
  public boolean getTruncateColumnFamily() {
    return m_truncateColumnFamily;
  }
  
  /**
   * Set any cql statements (separated by ;'s) to execute before
   * inserting the first row into the column family. Can be used
   * to do tasks like creating secondary indexes on columns in the
   * table.
   * 
   * @param cql cql statements (separated by ;'s) to execute
   */
  public void setAprioriCQL(String cql) {
    m_aprioriCQL = cql;
  }
  
  /**
   * Get any cql statements (separated by ;'s) to execute before
   * inserting the first row into the column family. Can be used
   * to do tasks like creating secondary indexes on columns in the
   * table.
   * 
   * @return cql statements (separated by ;'s) to execute
   */
  public String getAprioriCQL() {
    return m_aprioriCQL;
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
    
    if (!Const.isEmpty(m_password)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("password", 
          Encr.encryptPasswordIfNotUsingVariables(m_password)));
    }
    
    if (!Const.isEmpty(m_username)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("cassandra_user", 
          m_username));
    }
    
    if (!Const.isEmpty(m_cassandraKeyspace)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("cassandra_keyspace", 
          m_cassandraKeyspace));
    }
    
    if (!Const.isEmpty(m_cassandraKeyspace)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("cassandra_keyspace", 
          m_cassandraKeyspace));
    }
    
    if (!Const.isEmpty(m_columnFamily)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("column_family", 
          m_columnFamily));
    }
    
    if (!Const.isEmpty(m_keyField)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("key_field", 
          m_keyField));
    }
    
    if (!Const.isEmpty(m_consistency)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("consistency", 
          m_consistency));
    }
    
    if (!Const.isEmpty(m_batchSize)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("batch_size", 
          m_batchSize));
    }
    
    retval.append("\n    ").append(XMLHandler.addTagValue("create_column_family", 
        m_createColumnFamily));
    
    retval.append("\n    ").append(XMLHandler.addTagValue("use_compression", 
        m_useCompression));
    
    retval.append("\n    ").append(XMLHandler.
        addTagValue("insert_fields_not_in_meta", m_insertFieldsNotInMeta));
    
    retval.append("\n    ").append(XMLHandler.
        addTagValue("update_cassandra_meta", m_updateCassandraMeta));        
    
    retval.append("\n    ").append(XMLHandler.
        addTagValue("truncate_column_family", m_truncateColumnFamily));
    
    if (!Const.isEmpty(m_aprioriCQL)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("apriori_cql", 
          m_aprioriCQL));
    }
            
    return retval.toString();
  }
  
  public void loadXML(Node stepnode, List<DatabaseMeta> databases,
      Map<String, Counter> counters) throws KettleXMLException {
    m_cassandraHost = XMLHandler.getTagValue(stepnode, "cassandra_host");
    m_cassandraPort = XMLHandler.getTagValue(stepnode, "cassandra_port");
    m_username = XMLHandler.getTagValue(stepnode, "username");
    m_password = XMLHandler.getTagValue(stepnode, "password");
    m_cassandraKeyspace = XMLHandler.getTagValue(stepnode, "cassandra_keyspace");
    m_columnFamily = XMLHandler.getTagValue(stepnode, "column_family");
    m_keyField = XMLHandler.getTagValue(stepnode, "key_field");
    m_consistency = XMLHandler.getTagValue(stepnode, "consistency");
    m_batchSize = XMLHandler.getTagValue(stepnode, "batch_size");

    m_createColumnFamily = XMLHandler.getTagValue(stepnode, "create_column_family").
      equalsIgnoreCase("Y");
    m_useCompression = XMLHandler.getTagValue(stepnode, "use_compression").
      equalsIgnoreCase("Y");
    m_insertFieldsNotInMeta = XMLHandler.getTagValue(stepnode, "insert_fields_not_in_meta").
      equalsIgnoreCase("Y");
    m_updateCassandraMeta = XMLHandler.getTagValue(stepnode, "update_cassandra_meta").
      equalsIgnoreCase("Y");
    m_truncateColumnFamily = XMLHandler.getTagValue(stepnode, "truncate_column_family").
      equalsIgnoreCase("Y");
    
    m_aprioriCQL = XMLHandler.getTagValue(stepnode, "apriori_cql");
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
    m_columnFamily = rep.getStepAttributeString(id_step, 0, "column_family");
    m_keyField = rep.getStepAttributeString(id_step, 0, "key_field");
    m_consistency = rep.getStepAttributeString(id_step, 0, "consistency");
    m_batchSize = rep.getStepAttributeString(id_step, 0, "batch_size");
       
    m_createColumnFamily = rep.getStepAttributeString(id_step, 0, "create_column_family").
      equalsIgnoreCase("Y");
    m_useCompression = rep.getStepAttributeString(id_step, 0, "use_compression").
      equalsIgnoreCase("Y");
    m_insertFieldsNotInMeta = rep.getStepAttributeString(id_step, 0, "insert_fields_not_in_meta").
      equalsIgnoreCase("Y");
    m_updateCassandraMeta = rep.getStepAttributeString(id_step, 0, "update_cassandra_meta").
      equalsIgnoreCase("Y");
    m_truncateColumnFamily = rep.getStepAttributeString(id_step, 0, "truncate_column_family").
      equalsIgnoreCase("Y");
    
    m_aprioriCQL = rep.getStepAttributeString(id_step, 0, "apriori_cql");
    
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
    
    if (!Const.isEmpty(m_columnFamily)) {
      rep.saveStepAttribute(id_transformation, id_step, "column_family",
          m_columnFamily);
    }
    
    if (!Const.isEmpty(m_keyField)) {
      rep.saveStepAttribute(id_transformation, id_step, "key_field",
          m_keyField);
    }
    
    if (!Const.isEmpty(m_consistency)) {
      rep.saveStepAttribute(id_transformation, id_step, "consistency",
          m_consistency);
    }
    
    if (!Const.isEmpty(m_batchSize)) {
      rep.saveStepAttribute(id_transformation, id_step, "batch_size",
          m_batchSize);
    }

    rep.saveStepAttribute(id_transformation, id_step, "create_column_family",
        m_createColumnFamily);
    rep.saveStepAttribute(id_transformation, id_step, "use_compression",
        m_useCompression);
    rep.saveStepAttribute(id_transformation, id_step, "insert_fields_not_in_meta",
        m_insertFieldsNotInMeta);
    rep.saveStepAttribute(id_transformation, id_step, "update_cassandra_meta",
        m_updateCassandraMeta);
    rep.saveStepAttribute(id_transformation, id_step, "truncate_column_family",
        m_truncateColumnFamily);
    
    if (!Const.isEmpty(m_aprioriCQL)) {
      rep.saveStepAttribute(id_transformation, id_step, "apriori_cql",
          m_aprioriCQL);
    }
   
  }
  
  public void check(List<CheckResultInterface> remarks, TransMeta transMeta,
      StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output,
      RowMetaInterface info) {

    CheckResult cr;

    if ((prev == null) || (prev.size() == 0)) {
      cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING,
          "Not receiving any fields from previous steps!", stepMeta);
      remarks.add(cr);
    } else {
      cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
          "Step is connected to previous one, receiving " + prev.size() +
          " fields", stepMeta);
      remarks.add(cr);
    }

    // See if we have input streams leading to this step!
    if (input.length > 0) {
      cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
          "Step is receiving info from other steps.", stepMeta);
      remarks.add(cr);
    } else {
      cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
          "No input received from other steps!", stepMeta);
      remarks.add(cr);
    }
  }
  
  public StepInterface getStep(StepMeta stepMeta,
      StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans) {
    
    return new CassandraOutput(stepMeta, stepDataInterface, copyNr,
        transMeta, trans);
  }

  public StepDataInterface getStepData() {
    return new CassandraOutputData();
  }

  public void setDefault() {
    m_cassandraHost = "localhost";
    m_cassandraPort = "9160";
    m_columnFamily = "";
    m_batchSize = "100";
    m_useCompression = false;
    m_insertFieldsNotInMeta = false;
    m_updateCassandraMeta = false;
    m_truncateColumnFamily = false;
    m_aprioriCQL = "";
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
   

   return new CassandraOutputDialog(shell, meta, transMeta, name);
  }

}
