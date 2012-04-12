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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.mongodb.CommandResult;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * Class providing an output step for writing data to a MongoDB collection. Supports
 * insert, truncate, upsert, multi-update (update all matching docs) and modifier 
 * update (update only certain fields) operations. Can also create and drop indexes
 * based on one or more fields.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
public class MongoDbOutput extends BaseStep implements StepInterface {
  private static Class<?> PKG = MongoDbOutputMeta.class;
  
  protected MongoDbOutputMeta m_meta;
  protected MongoDbOutputData m_data;
  
  protected MongoDbOutputData.MongoTopLevel m_mongoTopLevelStructure = 
    MongoDbOutputData.MongoTopLevel.INCONSISTENT;
  
  /** The batch size to use for insert operation */
  protected int m_batchInsertSize = 100;
  
  /** Holds a batch */
  protected List<DBObject> m_batch;
  
  public MongoDbOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, 
      int copyNr, TransMeta transMeta, Trans trans) {
    super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
  }
  
  public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
    
    Object[] row = getRow();
    
    if (row == null) {
      // no more output
      
      // check any remaining buffered objects
      if (m_batch != null && m_batch.size() > 0) {
        doBatch();
      }
      
      // INDEXING - http://www.mongodb.org/display/DOCS/Indexes
      // Indexing is computationally expensive - it needs to be
      // done after all data is inserted and done in the BACKGROUND. 
      
      // UNIQUE indexes (prevent duplicates on the
      // keys in the index) and SPARSE indexes (don't index docs that
      // don't have the key field) - current limitation is that SPARSE
      // indexes can only have a single field
      
      List<MongoDbOutputMeta.MongoIndex> indexes = m_meta.getMongoIndexes();
      if (indexes != null && indexes.size() > 0) {
        logBasic(BaseMessages.getString(PKG, "MongoDbOutput.Messages.ApplyingIndexOpps"));
        m_data.applyIndexes(indexes, log, m_meta.getTruncate());
      }
      
      disconnect();
      setOutputDone();
      return false;
    }
    
    if (first) {
      first = false;
      
      m_batchInsertSize = 100;
      
      String batchInsert = environmentSubstitute(m_meta.getBatchInsertSize());
      if (!Const.isEmpty(batchInsert)) {
        m_batchInsertSize = Integer.parseInt(batchInsert);
      }
      m_batch = new ArrayList<DBObject>(m_batchInsertSize);
      
      // output the same as the input
      m_data.setOutputRowMeta(getInputRowMeta());         
      
      m_mongoTopLevelStructure = 
        MongoDbOutputData.checkTopLevelConsistency(m_meta.m_mongoFields, this);
      if (m_mongoTopLevelStructure == MongoDbOutputData.MongoTopLevel.INCONSISTENT) {
        throw new KettleException(BaseMessages.
            getString(PKG, "MongoDbOutput.Messages.Error.InconsistentMongoTopLevel"));
      }
      
      // first check our incoming fields against our meta data for fields to insert
      RowMetaInterface rmi = getInputRowMeta();
      List<MongoDbOutputMeta.MongoField> mongoFields = 
        m_meta.getMongoFields();
      List<String> notToBeInserted = new ArrayList<String>();
      for (int i = 0; i < rmi.size(); i++) {
        ValueMetaInterface vm = rmi.getValueMeta(i);
        boolean ok = false;
        for (MongoDbOutputMeta.MongoField field : mongoFields) {
          String mongoMatch = environmentSubstitute(field.m_incomingFieldName);
          if (vm.getName().equals(mongoMatch)) {
            ok = true;
            break;
          }
        }
        
        if (!ok) {
          notToBeInserted.add(vm.getName());
        }
      }
      
      if (notToBeInserted.size() == rmi.size()) {
        throw new KettleException(BaseMessages.getString(PKG, 
            "MongoDbOutput.Messages.Error.NotInsertingAnyFields"));
      }
      
      if (notToBeInserted.size() > 0) {
        StringBuffer b = new StringBuffer();
        for (String s : notToBeInserted) {
          b.append(s).append(" ");
        }
        
        logBasic(BaseMessages.getString(PKG, "MongoDbOutput.Messages.FieldsNotToBeInserted"), 
            b.toString());
      }
      
      // init mongo fields
      for (MongoDbOutputMeta.MongoField m : m_meta.getMongoFields()) {
        m.init(this);
      }
      
      // check truncate
      if (m_meta.getTruncate()) {
        try {
          logBasic(BaseMessages.getString(PKG, "MongoDbOutput.Messages.TruncatingCollection"));
          m_data.getCollection().drop();
          
          // re-establish the collection
          String collection = environmentSubstitute(m_meta.getCollection());
          m_data.createCollection(collection);
          m_data.setCollection(m_data.getDB().getCollection(collection));
        } catch (Exception m) {
          disconnect();
          throw new KettleException(m.getMessage(), m);
        }                
      }
    }
    
    if (!isStopped()) {
      
      if (m_meta.getUpsert()) {
        /*DBObject updateQuery = MongoDbOutputData.getQueryObject(m_meta.getMongoFields(), 
            getInputRowMeta(), row, getParentVariableSpace(), m_mongoTopLevelStructure); */
        DBObject updateQuery = MongoDbOutputData.getQueryObject(m_meta.getMongoFields(), 
            getInputRowMeta(), row, this);
        
        if (log.isDebug()) {
          logDebug(BaseMessages.getString(PKG, 
              "MongoDbOutput.Messages.Debug.QueryForUpsert", updateQuery));
        }
        
        if (updateQuery != null) {
          // i.e. we have some non-null incoming query field values
          DBObject insertUpdate = null;
          
          // get the record to update the match with
          if (!m_meta.getModifierUpdate()) {
            // complete record replace or insert
            
            insertUpdate = MongoDbOutputData.kettleRowToMongo(m_meta.getMongoFields(), 
              getInputRowMeta(), row, this, 
              m_mongoTopLevelStructure);
          } else {
            // specific field update or insert
            insertUpdate = MongoDbOutputData.getModifierUpdateObject(m_meta.getMongoFields(), 
                getInputRowMeta(), row, this, m_mongoTopLevelStructure);
            if (log.isDebug()) {
              logDebug(BaseMessages.getString(PKG, 
                  "MongoDbOutput.Messages.Debug.ModifierUpdateObject", insertUpdate));
            }
          }
       
          if (insertUpdate != null) {
            WriteConcern concern = null;
            
            if (log.getLogLevel().getLevel() >= LogLevel.DETAILED.getLevel()) {
              concern = new WriteConcern(1);
            }
            WriteResult result = null;
            if (concern != null) {
              result = m_data.getCollection().update(updateQuery, insertUpdate, true, 
                  m_meta.getMulti(), concern);
            } else {
              result = m_data.getCollection().update(updateQuery, insertUpdate, true, 
                  m_meta.getMulti());
            }

            CommandResult cmd = result.getLastError();
            if (cmd != null && !cmd.ok()) {
              String message = cmd.getErrorMessage();
              logError(BaseMessages.getString(PKG, "MongoDbOutput.Messages.Error.MongoReported", message));
              try {
                cmd.throwOnError();
              } catch (MongoException me) {
                throw new KettleException(me.getMessage(), me);
              }
            }
          }          
        }
      } else {        
        // straight insert
        
        DBObject mongoInsert = MongoDbOutputData.kettleRowToMongo(m_meta.getMongoFields(), 
            getInputRowMeta(), row, this, 
            m_mongoTopLevelStructure);

        if (mongoInsert != null) {
          m_batch.add(mongoInsert);
        }
        if (m_batch.size() == m_batchInsertSize) {
          logBasic(BaseMessages.getString(PKG, "MongoDbOutput.Messages.CommitingABatch"));
          doBatch();
        }
      }
    }
    
    
    return true;
  }
  
  protected void doBatch() throws KettleException {
    WriteConcern concern = null;
    
    
    if (log.getLogLevel().getLevel() >= LogLevel.DETAILED.getLevel()) {
      concern = new WriteConcern(1);
    }
    WriteResult result = null;
    
    if (concern != null) {
      result = m_data.getCollection().insert(m_batch, concern);
    } else {
      result = m_data.getCollection().insert(m_batch);
    }
    
    CommandResult cmd = result.getLastError();    
    
    if (cmd != null && !cmd.ok()) {
      String message = cmd.getErrorMessage();
      logError(BaseMessages.getString(PKG, "MongoDbOutput.Messages.Error.MongoReported", message));
      try {
        cmd.throwOnError();
      } catch (MongoException me) {
        throw new KettleException(me.getMessage(), me);
      }
    }
    
    m_batch.clear();
  }
  
  public boolean init(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface) {
    if (super.init(stepMetaInterface, stepDataInterface)) {
      m_meta = (MongoDbOutputMeta) stepMetaInterface;
      m_data = (MongoDbOutputData) stepDataInterface;

      String hostname = environmentSubstitute(m_meta.getHostname());
      int port = Const.toInt(environmentSubstitute(m_meta.getPort()), 27017);
      String db = environmentSubstitute(m_meta.getDBName());
      String collection = environmentSubstitute(m_meta.getCollection());


      try {
        
        m_data.connect(hostname, port);
        m_data.setDB(m_data.getConnection().getDB(db));
        
        String realUser = environmentSubstitute(m_meta.getUsername());
        String realPass = Encr.
          decryptPasswordOptionallyEncrypted(environmentSubstitute(m_meta.getPassword()));
        
        if (!Const.isEmpty(realUser) || !Const.isEmpty(realPass)) {
          if (!m_data.getDB().authenticate(realUser, realPass.toCharArray())) {
            throw new KettleException(BaseMessages.getString(PKG, 
                "MongoDbOutput.Messages.Error.UnableToAuthenticate"));
          }
        }
        
        if (Const.isEmpty(collection)) {
          throw new KettleException(BaseMessages.getString(PKG, 
              "MongoDbOutput.Messages.Error.NoCollectionSpecified"));
        }
        m_data.createCollection(collection);
        m_data.setCollection(m_data.getDB().getCollection(collection));

        return true;
      } catch (UnknownHostException ex) {
        logError(BaseMessages.getString(PKG, 
            "MongoDbOutput.Messages.Error.UnknownHost", hostname), ex);
        return false;
      } catch (Exception e){
        logError(BaseMessages.getString(PKG, "MongoDbOutput.Messages.Error.ProblemConnecting", 
           hostname, "" + port), e);
        return false;
      }
    }
    
    return false;   
  }
  
  protected void disconnect() {
    if (m_data != null) {
      m_data.disconnect();
    }
  }
  
  public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
    if (m_data != null) {
      m_data.disconnect();
    }
    
    super.dispose(smi, sdi);
  }
}