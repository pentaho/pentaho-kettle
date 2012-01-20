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

package org.pentaho.di.trans.steps.mongodboutput;

import java.util.ArrayList;
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
 * Class providing an output step for writing data to a MongoDB collection. Supports
 * insert, truncate, upsert, multi-update (update all matching docs) and modifier 
 * update (update only certain fields) operations. Can also create and drop indexes
 * based on one or more fields.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
@Step(id = "MongoDbOutput", image = "MongoDB.png", name = "MongoDb Output", description="Writes to a Mongo DB collection", categoryDescription="Big Data")
public class MongoDbOutputMeta extends BaseStepMeta implements
    StepMetaInterface {
  
  private static Class<?> PKG = MongoDbOutputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
  
  /**
   * Class encapsulating paths to document fields
   * 
   * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
   */
  public static class MongoField {
    
    /** Incoming Kettle field name */
    public String m_incomingFieldName = "";
    
    /** 
     * Dot separated path to the corresponding mongo field
     */
    public String m_mongoDocPath = "";
    
    /**
     * Whether to use the incoming field name as the mongo field key name. If
     * false then the user must supply the terminating field/key name.
     */
    public boolean m_useIncomingFieldNameAsMongoFieldName;
    
    /** Whether this field is used in the query for an update operation */
    public boolean m_updateMatchField;
    
    /** 
     * Ignored if not doing a modifier update since all
     * mongo paths are involved in a standard upsert. If null/empty then
     * this field is not being updated in the modifier update case.
     * 
     * $set
     * $inc
     * $push - append value to array (or set to [value] if field doesn't exist)
     * 
     * (support any others?)
     */
    public String m_modifierUpdateOperation = "N/A";
  }
  
  /**
   * Class encapsulating index definitions
   * 
   * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
   */
  public static class MongoIndex {
    
    /**
     * Dot notation for accessing a fields - e.g. person.address.street. Can also
     * specify entire embedded documents as an index (rather than a primitive key) -
     * e.g. person.address.
     * 
     * Multiple fields are comma-separated followed by an optional "direction" indicator
     * for the index (1 or -1). If omitted, direction is assumed to be 1.
     */
    public String m_pathToFields = "";
    
    /** whether to drop this index - default is create */
    public boolean m_drop;
    
    // other options unique, sparse    
    public boolean m_unique;
    public boolean m_sparse;
    
    public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append(m_pathToFields + " (unique = " 
          + new Boolean(m_unique).toString() + " sparse = "
          + new Boolean(m_sparse).toString() + ")");
      
      return buff.toString();
    }
  }
  
  /** Hostname/IP address of mongo server */
  protected String m_hostname = "localhost";
  
  /** Port that mongo is listening on */ 
  protected String m_port = "27017";
  
  /** The DB to use */
  protected String m_dbName;
  
  /** The collection to use */
  protected String m_collection;
  
  /** Whether to truncate the collection */
  protected boolean m_truncate;
  
  /** Username for authentication */
  protected String m_username;
  
  /** Password for authentication */
  protected String m_password;
  
  /** True if upserts are to be performed */ 
  protected boolean m_upsert;
  
  /** whether to update all records that match during an upsert or just the first */ 
  protected boolean m_multi;
  
  /** 
   * Modifier update involves updating only some fields and is
   * efficient because of low network overhead. Is also particularly
   * efficient for $incr operations since the queried object does
   * not have to be returned in order to increment the field and then
   * saved again.
   * 
   * If modifier update is false, then the standard update/insert operation
   * is performed which involves replacing the matched object with
   * a new object involving all the user-defined mongo paths
   */
  protected boolean m_modifierUpdate;
  
  /** The batch size for inserts */
  protected String m_batchInsertSize = "100";
  
  /** The list of paths to document fields for incoming kettle values */
  protected List<MongoField> m_mongoFields;
  
  /** The list of index definitions (if any) */
  protected List<MongoIndex> m_mongoIndexes;
  
  public void setDefault() {
    m_hostname = "localhost";
    m_port = "27017";
    m_collection = "";
    m_dbName = "";
    m_upsert = false;
    m_modifierUpdate = false;
    m_truncate = false;
    m_batchInsertSize = "100";
  }
  
  /**
   * Set the list of document paths
   * 
   * @param mongoFields the list of document paths
   */
  public void setMongoFields(List<MongoField> mongoFields) {
    m_mongoFields = mongoFields;
  }
  
  /**
   * Get the list of document paths
   * 
   * @return the list of document paths
   */
  public List<MongoField> getMongoFields() {
    return m_mongoFields;
  }
  
  /**
   * Set the list of document indexes for creation/dropping
   * 
   * @param mongoIndexes the list of indexes
   */
  public void setMongoIndexes(List<MongoIndex> mongoIndexes) {
    m_mongoIndexes = mongoIndexes;
  }
  
  /**
   * Get the list of document indexes for creation/dropping
   * 
   * @return the list of indexes
   */
  public List<MongoIndex> getMongoIndexes() {
    return m_mongoIndexes;
  }
  
  /**
   * Set the hostname of the mongo server
   * 
   * @param host the hostname
   */
  public void setHostname(String host) {
    m_hostname = host;
  }
  
  /**
   * Get the hostname of the mongo server
   * 
   * @return the hostname
   */
  public String getHostname() {
    return m_hostname;
  }
  
  /**
   * Set the port that the server is listening on
   * 
   * @param port the port that the server is listening on
   */
  public void setPort(String port) {
    m_port = port;
  }
  
  /**
   * Get the port that the server is listening on
   * 
   * @return the port that the server is listening on
   */
  public String getPort() {
    return m_port;
  }
  
  /**
   * Set the database name to use
   * 
   * @param db the database name to use
   */
  public void setDBName(String db) {
    m_dbName = db;
  }
  
  /**
   * Get the database name to use
   * 
   * @return the database name to use
   */
  public String getDBName() {
    return m_dbName;
  }
  
  /**
   * Set the collection to use
   * 
   * @param collection the collection to use
   */
  public void setCollection(String collection) {
    m_collection = collection;
  }
  
  /**
   * Get the collection to use
   * 
   * @return the collection to use
   */
  public String getCollection() {
    return m_collection;
  }
  
  /**
   * Set the username to authenticate with
   * 
   * @param username the username to authenticate with
   */
  public void setUsername(String username) {
    m_username = username;
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
   * @param password the password to authenticate with
   */
  public void setPassword(String password) {
    m_password = password;
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
   * Set whether to upsert rather than insert
   * 
   * @param upsert true if we'll upsert rather than insert
   */
  public void setUpsert(boolean upsert) {
    m_upsert = upsert;
  }
  
  /**
   * Get whether to upsert rather than insert
   * 
   * @return true if we'll upsert rather than insert
   */
  public boolean getUpsert() {
    return m_upsert;
  }
  
  /**
   * Set whether the upsert should update all matching records rather than just
   * the first.
   * 
   * @param multi true if all matching records get updated when each row is upserted
   */
  public void setMulti(boolean multi) {
    m_multi = multi;
  }
  
  /**
   * Get whether the upsert should update all matching records rather than just
   * the first.
   * 
   * @return true if all matching records get updated when each row is upserted
   */
  public boolean getMulti() {
    return m_multi;
  }
  
  /**
   * Set whether the upsert operation is a modifier update - i.e where only
   * specified fields in each document get modified rather than a whole document replace.
   * 
   * @param u true if the upsert operation is to be a modifier update
   */
  public void setModifierUpdate(boolean u) {
    m_modifierUpdate = u;
  }
  
  /**
   * Get whether the upsert operation is a modifier update - i.e where only
   * specified fields in each document get modified rather than a whole document replace.
   * 
   * @return true if the upsert operation is to be a modifier update
   */
  public boolean getModifierUpdate() {
    return m_modifierUpdate;
  }
  
  /**
   * Set whether to truncate the collection before inserting
   * 
   * @param truncate true if the all records in the collection are to be
   * deleted
   */
  public void setTruncate(boolean truncate) {
    m_truncate = truncate;
  }
  
  /**
   * Get whether to truncate the collection before inserting
   * 
   * @return true if the all records in the collection are to be
   * deleted
   */
  public boolean getTruncate() {
    return m_truncate;
  }
  
  /**
   * Get the batch insert size
   * 
   * @return the batch insert size
   */
  public String getBatchInsertSize() {
    return m_batchInsertSize;
  }
  
  /**
   * Set the batch insert size
   * 
   * @param size the batch insert size
   */
  public void setBatchInsertSize(String size) {
    m_batchInsertSize = size;
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
    
    return new MongoDbOutput(stepMeta, stepDataInterface, copyNr,
        transMeta, trans);
  }

  public StepDataInterface getStepData() {
    return new MongoDbOutputData();
  }
  
  public String getXML() {
    StringBuffer retval = new StringBuffer();
    
    if (!Const.isEmpty(m_hostname)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("mongo_host", 
          m_hostname));
    }
    if (!Const.isEmpty(m_port)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("mongo_port", 
          m_port));            
    }    
    if (!Const.isEmpty(m_username)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("mongo_user", 
          m_username));
    }
    if (!Const.isEmpty(m_password)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("mongo_password", 
          Encr.encryptPasswordIfNotUsingVariables(m_password)));
    }
    if (!Const.isEmpty(m_dbName)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("mongo_db", 
          m_dbName));
    }
    if (!Const.isEmpty(m_collection)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("mongo_collection", 
          m_collection));
    }
    if (!Const.isEmpty(m_batchInsertSize)) {
      retval.append("\n    ").append(XMLHandler.addTagValue("batch_insert_size", 
          m_batchInsertSize));
    }    
    
    retval.append("\n    ").append(XMLHandler.
        addTagValue("truncate", m_truncate));
    retval.append("\n    ").append(XMLHandler.
        addTagValue("upsert", m_upsert));
    retval.append("\n    ").append(XMLHandler.
        addTagValue("multi", m_multi));
    retval.append("\n    ").append(XMLHandler.
        addTagValue("modifier_update", m_modifierUpdate));
    
    if (m_mongoFields != null && m_mongoFields.size() > 0) {
      retval.append("\n    ").append(XMLHandler.openTag("mongo_fields"));
      
      for (MongoField field : m_mongoFields) {
        retval.append("\n      ").append(XMLHandler.openTag("mongo_field"));
        
        retval.append("\n         ").append(XMLHandler.addTagValue("incoming_field_name", field.m_incomingFieldName));
        retval.append("\n         ").append(XMLHandler.addTagValue("mongo_doc_path", field.m_mongoDocPath));
        retval.append("\n         ").append(XMLHandler.addTagValue("use_incoming_field_name_as_mongo_field_name", 
            field.m_useIncomingFieldNameAsMongoFieldName));
        retval.append("\n         ").append(XMLHandler.addTagValue("update_match_field", field.m_updateMatchField));
        retval.append("\n         ").append(XMLHandler.addTagValue("modifier_update_operation", field.m_modifierUpdateOperation));
        
        retval.append("\n      ").append(XMLHandler.closeTag("mongo_field"));        
      }
      
      retval.append("\n    ").append(XMLHandler.closeTag("mongo_fields"));
    }
    
    if (m_mongoIndexes != null && m_mongoIndexes.size() > 0) {
      retval.append("\n    ").append(XMLHandler.openTag("mongo_indexes"));
      
      for (MongoIndex index : m_mongoIndexes) {
        retval.append("\n      ").append(XMLHandler.openTag("mongo_index"));
        
        retval.append("\n         ").append(XMLHandler.addTagValue("path_to_fields", index.m_pathToFields));
        retval.append("\n         ").append(XMLHandler.addTagValue("drop", index.m_drop));
        retval.append("\n         ").append(XMLHandler.addTagValue("unique", index.m_unique));
        retval.append("\n         ").append(XMLHandler.addTagValue("sparse", index.m_sparse));
        
        retval.append("\n      ").append(XMLHandler.closeTag("mongo_index"));
      }
      
      retval.append("\n    ").append(XMLHandler.closeTag("mongo_indexes"));
    }
    
    return retval.toString();
  }

  public void loadXML(Node stepnode, List<DatabaseMeta> databases,
      Map<String, Counter> counters) throws KettleXMLException {

    m_hostname = XMLHandler.getTagValue(stepnode, "mongo_host");
    m_port = XMLHandler.getTagValue(stepnode, "mongo_port");
    m_username = XMLHandler.getTagValue(stepnode, "mongo_user");
    m_password = XMLHandler.getTagValue(stepnode, "mongo_password");
    m_dbName = XMLHandler.getTagValue(stepnode, "mongo_db");
    m_collection = XMLHandler.getTagValue(stepnode, "mongo_collection");
    m_batchInsertSize = XMLHandler.getTagValue(stepnode, "batch_insert_size");
    
    m_truncate = XMLHandler.getTagValue(stepnode, "truncate").
      equalsIgnoreCase("Y");
    m_upsert = XMLHandler.getTagValue(stepnode, "upsert").
      equalsIgnoreCase("Y");
    m_multi = XMLHandler.getTagValue(stepnode, "multi").
      equalsIgnoreCase("Y");
    m_modifierUpdate = XMLHandler.getTagValue(stepnode, "modifier_update").
      equalsIgnoreCase("Y");
    
    Node fields = XMLHandler.getSubNode(stepnode, "mongo_fields");
    if (fields != null && XMLHandler.countNodes(fields, "mongo_field") > 0) {
      int nrfields = XMLHandler.countNodes(fields, "mongo_field");      
      m_mongoFields = new ArrayList<MongoField>();
      
      for (int i = 0; i < nrfields; i++) {
        Node fieldNode = XMLHandler.getSubNodeByNr(fields, "mongo_field", i);
        
        MongoField newField = new MongoField();
        newField.m_incomingFieldName = XMLHandler.getTagValue(fieldNode, "incoming_field_name");
        newField.m_mongoDocPath = XMLHandler.getTagValue(fieldNode, "mongo_doc_path");
        newField.m_useIncomingFieldNameAsMongoFieldName = 
          XMLHandler.getTagValue(fieldNode, "use_incoming_field_name_as_mongo_field_name").
            equalsIgnoreCase("Y");
        newField.m_updateMatchField = 
          XMLHandler.getTagValue(fieldNode, "update_match_field").equalsIgnoreCase("Y");
        
        newField.m_modifierUpdateOperation = XMLHandler.getTagValue(fieldNode, 
            "modifier_update_operation");
        
        m_mongoFields.add(newField);
      }
    }
    
    fields = XMLHandler.getSubNode(stepnode, "mongo_indexes");
    if (fields != null && XMLHandler.countNodes(fields, "mongo_index") > 0) {
      int nrfields = XMLHandler.countNodes(fields, "mongo_index");
      
      m_mongoIndexes = new ArrayList<MongoIndex>();
      
      for (int i = 0; i < nrfields; i++) {
        Node fieldNode = XMLHandler.getSubNodeByNr(fields, "mongo_index", i);
        
        MongoIndex newIndex = new MongoIndex();
        
        newIndex.m_pathToFields = XMLHandler.getTagValue(fieldNode, "path_to_fields");
        newIndex.m_drop = 
          XMLHandler.getTagValue(fieldNode, "drop").equalsIgnoreCase("Y");
        newIndex.m_unique = 
          XMLHandler.getTagValue(fieldNode, "unique").equalsIgnoreCase("Y");
        newIndex.m_sparse = 
          XMLHandler.getTagValue(fieldNode, "sparse").equalsIgnoreCase("Y");
        
        m_mongoIndexes.add(newIndex);
      }
    }
  }

  public void readRep(Repository rep, ObjectId id_step,
      List<DatabaseMeta> databases, Map<String, Counter> counters)
      throws KettleException {
    
    m_hostname = rep.getStepAttributeString(id_step, 0, "mongo_host");
    m_port = rep.getStepAttributeString(id_step, 0, "mongo_port");
    m_username = rep.getStepAttributeString(id_step, 0, "mongo_user");
    m_password = rep.getStepAttributeString(id_step, 0, "mongo_password");
    m_dbName = rep.getStepAttributeString(id_step, 0, "mongo_db");
    m_collection = rep.getStepAttributeString(id_step, 0, "mongo_collection");
    m_batchInsertSize = rep.getStepAttributeString(id_step, 0, "batch_insert_size");
    
    m_truncate = rep.getStepAttributeString(id_step, 0, "truncate").
      equalsIgnoreCase("Y");
    m_upsert = rep.getStepAttributeString(id_step, 0, "upsert").
      equalsIgnoreCase("Y");
    m_multi = rep.getStepAttributeString(id_step, 0, "multi").
      equalsIgnoreCase("Y");
    m_modifierUpdate = rep.getStepAttributeString(id_step, 0, "modifier_update").
      equalsIgnoreCase("Y");
    
    int nrfields = rep.countNrStepAttributes(id_step, "incoming_field_name");
    
    if (nrfields > 0) {
      m_mongoFields = new ArrayList<MongoField>();
      
      for (int i = 0; i < nrfields; i++) {
        MongoField newField = new MongoField();
        
        newField.m_incomingFieldName = 
          rep.getStepAttributeString(id_step, i, "incoming_field_name");
        newField.m_mongoDocPath = 
          rep.getStepAttributeString(id_step, i, "mongo_doc_path");
        
        newField.m_useIncomingFieldNameAsMongoFieldName = 
          rep.getStepAttributeString(id_step, i, "use_incoming_field_name_as_mongo_field_name").
            equalsIgnoreCase("Y");
        newField.m_updateMatchField = 
          rep.getStepAttributeString(id_step, i, "update_match_field").
            equalsIgnoreCase("Y");
        newField.m_modifierUpdateOperation = 
          rep.getStepAttributeString(id_step, i, "modifier_update_operation");
        
        m_mongoFields.add(newField);
      }
    }
    
    nrfields = rep.countNrStepAttributes(id_step, "path_to_fields");
    if (nrfields > 0) {
      m_mongoIndexes = new ArrayList<MongoIndex>();
      
      for (int i = 0; i < nrfields; i++) {
        MongoIndex newIndex = new MongoIndex();
        
        newIndex.m_pathToFields = 
          rep.getStepAttributeString(id_step, i, "path_to_fields");
        newIndex.m_drop = 
          rep.getStepAttributeString(id_step, i, "drop").equalsIgnoreCase("Y");
        newIndex.m_unique = 
          rep.getStepAttributeString(id_step, i, "unique").equalsIgnoreCase("Y");
        newIndex.m_sparse = 
          rep.getStepAttributeString(id_step, i, "sparse").equalsIgnoreCase("Y");
        
        m_mongoIndexes.add(newIndex);
      }
    }
  }

  public void saveRep(Repository rep, ObjectId id_transformation,
      ObjectId id_step) throws KettleException {
    
    if (!Const.isEmpty(m_hostname)) {
      rep.saveStepAttribute(id_transformation, id_step, "mongo_host", 
          m_hostname);
    }
    if (!Const.isEmpty(m_port)) {
      rep.saveStepAttribute(id_transformation, id_step, "mongo_port", 
          m_port);
    }
    if (!Const.isEmpty(m_username)) {
      rep.saveStepAttribute(id_transformation, id_step, "mongo_user", 
          m_username);
    }
    if (!Const.isEmpty(m_password)) {
      rep.saveStepAttribute(id_transformation, id_step, "password",
          Encr.encryptPasswordIfNotUsingVariables(m_password));
    }
    if (!Const.isEmpty(m_dbName)) {
      rep.saveStepAttribute(id_transformation, id_step, "mongo_db", 
          m_dbName);
    }
    if (!Const.isEmpty(m_collection)) {
      rep.saveStepAttribute(id_transformation, id_step, "mongo_collection", 
          m_collection);
    }
    if (!Const.isEmpty(m_batchInsertSize)) {
      rep.saveStepAttribute(id_transformation, id_step, "batch_insert_size", 
          m_batchInsertSize);
    }
    
    rep.saveStepAttribute(id_transformation, id_step, "truncate",
        m_truncate);
    rep.saveStepAttribute(id_transformation, id_step, "upsert",
        m_upsert);
    rep.saveStepAttribute(id_transformation, id_step, "multi",
        m_multi);
    rep.saveStepAttribute(id_transformation, id_step, "modifier_update",
        m_modifierUpdate);
    
    if (m_mongoFields != null && m_mongoFields.size() > 0) {
      for (int i = 0; i < m_mongoFields.size(); i++) {
        MongoField field = m_mongoFields.get(i);
        
        rep.saveStepAttribute(id_transformation, id_step, i, "incoming_field_name", 
            field.m_incomingFieldName);
        rep.saveStepAttribute(id_transformation, id_step, i, "mongo_doc_path", 
            field.m_mongoDocPath);
        rep.saveStepAttribute(id_transformation, id_step, i, "use_incoming_field_name_as_mongo_field_name", 
            field.m_useIncomingFieldNameAsMongoFieldName);
        rep.saveStepAttribute(id_transformation, id_step, i, "update_match_field", 
            field.m_updateMatchField);
        rep.saveStepAttribute(id_transformation, id_step, i, "modifier_update_operation", 
            field.m_modifierUpdateOperation);
      }
    }
    
    if (m_mongoIndexes != null && m_mongoIndexes.size() > 0) {
      for (int i = 0; i < m_mongoIndexes.size(); i++) {
        MongoIndex mongoIndex = m_mongoIndexes.get(i);
        
        rep.saveStepAttribute(id_transformation, id_step, i, "path_to_fields", 
            mongoIndex.m_pathToFields);
        rep.saveStepAttribute(id_transformation, id_step, i, "drop", 
            mongoIndex.m_drop);
        rep.saveStepAttribute(id_transformation, id_step, i, "unique", 
            mongoIndex.m_unique);
        rep.saveStepAttribute(id_transformation, id_step, i, "sparse", 
            mongoIndex.m_sparse);
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
   

   return new MongoDbOutputDialog(shell, meta, transMeta, name);
  }
}
