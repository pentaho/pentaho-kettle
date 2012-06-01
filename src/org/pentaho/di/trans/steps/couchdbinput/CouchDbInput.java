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

package org.pentaho.di.trans.steps.couchdbinput;

import java.io.BufferedInputStream;
import java.io.IOException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.pentaho.di.cluster.SlaveConnectionManager;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
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

public class CouchDbInput extends BaseStep implements StepInterface
{
	private static Class<?> PKG = CouchDbInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CouchDbInputMeta meta;
	private CouchDbInputData data;
	
	public CouchDbInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
	  try {
  	  if (first) {
  	    first=false;
  	    
  	    data.outputRowMeta = new RowMeta();
  	    meta.getFields(data.outputRowMeta,getStepname(), null, null, this);
  	    
  	    // Skip over first introduction row containing the number of results...
  	    //
  	    // Example:  {"total_rows":3,"offset":0,"rows":[
  	    //
  	    data.buffer = new StringBuilder(1000);
  	    data.open=0;
  	    boolean cont=true;
  	    int c = data.bufferedInputStream.read();
  	    while (c>=0 && cont && !isStopped()) {
  	      data.buffer.append((char)c);
  	      
  	      switch((char)c) {
  	      case '{' : 
  	        data.open++;
  
  	        // Second JSON nested block means: another row of data...
  	        if (data.open==2) {
  	          logBasic("Read header: >>"+data.buffer.substring(0, data.buffer.length()-1)+"<<");
  	          data.buffer.delete(0, data.buffer.length()-1);
  	          cont=false; // Stop the while loop.
  	        }
  
  	        break;
  	      case '}' : data.open--; 
  	        break;
  	      
  	      case '"' : 
  	        // skip until the next "
  	        //
  	        int prev = c;
  	        c=data.bufferedInputStream.read();
  	        while (c!='"' && prev!='\\' && c>=0) {
  	          data.buffer.append((char)c);
  	          prev=c;
  	          c=data.bufferedInputStream.read();
  	        }
  	      }
  	      
  	      if (cont) {
  	        c = data.bufferedInputStream.read();
  	      }
  	    }
  	    
        if (c<0) {
          setOutputDone();
          return false;
        }
  	  }
  
  	  // read one JSON block from the data until no data is left on the input stream
  	  //
      boolean cont=true;
      int c = data.bufferedInputStream.read();
      while (c>=0 && cont && !isStopped()) {
        data.buffer.append((char)c);
        
        switch((char)c) {
        case '{' : 
          data.open++;
  
          // Second JSON nested block means: another row of data...
          if (data.open==2) {

            sendBufferRow(false);
            
            cont=false; // Stop the while loop.
          }
  
          break;
        case '}' : data.open--; 
          break;
        }
        
        if (cont) {
          c = data.bufferedInputStream.read();
        }
      }
      
      if (c<0) {
        if (data.buffer.length()>0) {
          sendBufferRow(true);
        }
        setOutputDone();
        return false;
      }
     
      return true;
	  } catch(IOException e) {
	    throw new KettleException("Unable to read from the CouchDB REST web service", e);
	  }
	}

	private void sendBufferRow(boolean lastRow) throws KettleStepException {

	  int pos = data.buffer.length()-2;

    if (lastRow) {
      // Get rid of any ]} at the end of the last row.
      //
      pos = removeTrailingSpaces(data.buffer, pos);
      pos = removeTrailingCharacter(data.buffer, pos, '}');
      pos = removeTrailingSpaces(data.buffer, pos);
      pos = removeTrailingCharacter(data.buffer, pos, ']');
    }

    pos = removeTrailingSpaces(data.buffer, pos);
    pos = removeTrailingCharacter(data.buffer, pos, ',');
    
    String json = data.buffer.substring(0, pos+1);
    data.buffer.delete(0, data.buffer.length()-1);
    
    if (log.isDebug()) logDebug("Read row: "+json);

    Object[] row = RowDataUtil.allocateRowData(data.outputRowMeta.size());
    int index=0;
    row[index++] = json;
    
    // putRow will send the row on to the default output hop.
    //
    putRow(data.outputRowMeta, row);
  }

  private int removeTrailingCharacter(StringBuilder buffer, int pos, char c) {
    if (data.buffer.charAt(pos)==c) pos--;
    return pos;
  }

  private int removeTrailingSpaces(StringBuilder buffer, int pos) {
    while (pos>=0 && Const.isSpace(data.buffer.charAt(pos))) pos--;
    return pos;
  }

  public boolean init(StepMetaInterface stepMetaInterface, StepDataInterface stepDataInterface) {
	  if (super.init(stepMetaInterface, stepDataInterface)) {
  	  meta = (CouchDbInputMeta) stepMetaInterface;
  	  data = (CouchDbInputData) stepDataInterface;
      
      String hostname = environmentSubstitute(meta.getHostname());
      int port = Const.toInt(environmentSubstitute(meta.getPort()), 5984);
      String db = environmentSubstitute(meta.getDbName());
      String design = environmentSubstitute(meta.getDesignDocument());
      String view = environmentSubstitute(meta.getViewName());

      if (Const.isEmpty(design)) {
        log.logError("Please provide a design document to use");
        return false;
      }
      
      if (Const.isEmpty(view)) {
        log.logError("Please provide a view name to look at");
        return false;
      }
  
      String realUser = environmentSubstitute(meta.getAuthenticationUser());
      String realPass = Encr.decryptPasswordOptionallyEncrypted(environmentSubstitute(meta.getAuthenticationPassword()));
      
      String url = "http://"+hostname;
      if (port>=0) {
        url+=":"+port;
      }
      url+="/"+db;
      url+="/_design/"+design;
      url+="/_view/"+view;

      logBasic("Querying CouchDB view on URL: "+url);

      try {

        HttpClient client = SlaveConnectionManager.getInstance().createHttpClient();
        // client.setTimeout(10000);
        // client.setConnectionTimeout(10000);

        if (!Const.isEmpty(realUser)) {
          Credentials credentials = new UsernamePasswordCredentials(realUser, realPass);
          client.getState().setCredentials(AuthScope.ANY, credentials);
          client.getParams().setAuthenticationPreemptive(true);      
        }

        HttpMethod method = new GetMethod(url);

        // Execute request
        // 
        data.inputStream=null;
        data.bufferedInputStream=null;

        int result = client.executeMethod(method);

        // the response
        //
        data.inputStream = method.getResponseBodyAsStream();
        data.bufferedInputStream = new BufferedInputStream(data.inputStream, 1000);
                
        if (result<200 || result>=300) {
          StringBuilder err = new StringBuilder();
          int c;
          while ((c=data.bufferedInputStream.read())>=0) {
            err.append((char)c);
          }
          logError("Web request returned code "+result+" : "+err.toString());
          return false;
        }
      
      data.counter = 0;
      
      return true;
    } catch(Exception e) {
        logError(BaseMessages.getString(PKG, "CouchDbInput.ErrorConnectingToCouchDb.Exception", hostname, ""+port, db, view), e);
        return false;
      }
	  }
	  return true;
	}
	
	@Override
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
	  
    if (data.bufferedInputStream!=null) {
      try {
        data.bufferedInputStream.close();
      } catch(Exception e) {
        setErrors(1);
        logError("Error closing data stream", e);
      }
    }
	  
	  super.dispose(smi, sdi);
	}
	
}