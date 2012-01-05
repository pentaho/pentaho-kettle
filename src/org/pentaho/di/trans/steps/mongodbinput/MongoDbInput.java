/*******************************************************************************
 *
 * Pentaho Data Integration
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

package org.pentaho.di.trans.steps.mongodbinput;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
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

import com.mongodb.Mongo;

public class MongoDbInput extends BaseStep implements StepInterface
{
	private static Class<?> PKG = MongoDbInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private MongoDbInputMeta meta;
	private MongoDbInputData data;
	
	public MongoDbInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
	  if (first) {
	    first=false;
	    
	    data.outputRowMeta = new RowMeta();
	    meta.getFields(data.outputRowMeta,getStepname(), null, null, this);
	    
	    data.cursor = data.collection.find();
	  }

	  if (data.cursor.hasNext() && !isStopped()) {
	    String json = data.cursor.next().toString();
	    Object[] row = RowDataUtil.allocateRowData(data.outputRowMeta.size());
	    int index=0;
	    
	    row[index++] = json;

	    // putRow will send the row on to the default output hop.
	    //
	    putRow(data.outputRowMeta, row);
	    
	    return true;
	  } else {

	    setOutputDone();
  
  	  return false;
	  }
	}

	public boolean init(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface)
	{
	  if (super.init(stepMetaInterface, stepDataInterface)) {
  	  meta = (MongoDbInputMeta) stepMetaInterface;
  	  data = (MongoDbInputData) stepDataInterface;
      
      String hostname = environmentSubstitute(meta.getHostname());
      int port = Const.toInt(environmentSubstitute(meta.getPort()), 27017);
      String db = environmentSubstitute(meta.getDbName());
      String collection = environmentSubstitute(meta.getCollection());
  
      try {
  
        data.mongo = new Mongo(hostname, port);
        data.db = data.mongo.getDB(db);
        
        String realUser = environmentSubstitute(meta.getAuthenticationUser());
        String realPass = Encr.decryptPasswordOptionallyEncrypted(environmentSubstitute(meta.getAuthenticationPassword()));
        
        if (!Const.isEmpty(realUser) || !Const.isEmpty(realPass)) {
          if (!data.db.authenticate(realUser, realPass.toCharArray())) {
            throw new KettleException(BaseMessages.getString(PKG, "MongoDbInput.ErrorAuthenticating.Exception"));
          }
        }
        data.collection = data.db.getCollection(collection);
  
        return true;
      } catch (Exception e) {
        logError(BaseMessages.getString(PKG, "MongoDbInput.ErrorConnectingToMongoDb.Exception", hostname, ""+port, db, collection), e);
        return false;
      }
	  } else {
	    return false;
	  }
	}
	
	@Override
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
	  if (data.cursor!=null) {
	    data.cursor.close();
	  }
	  if (data.mongo!=null) {
	    data.mongo.close();
	  }
	  
	  super.dispose(smi, sdi);
	}
	
}