/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/

package org.pentaho.di.trans.steps.salesforceinput;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.axis.message.MessageElement;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.i18n.BaseMessages;

import com.sforce.soap.partner.DeleteResult;
import com.sforce.soap.partner.DeletedRecord;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.GetDeletedResult;
import com.sforce.soap.partner.GetUpdatedResult;
import com.sforce.soap.partner.GetUserInfoResult;
import com.sforce.soap.partner.LoginResult;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.SessionHeader;
import com.sforce.soap.partner.SforceServiceLocator;
import com.sforce.soap.partner.SoapBindingStub;
import com.sforce.soap.partner.UpsertResult;
import com.sforce.soap.partner.sobject.SObject;

public class SalesforceConnection {
	private static Class<?> PKG = SalesforceInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	private LogWriter log;
	private String url;
	private String username;
	private String password;
	private String module;
	private int timeout;
	private String condition;
	
	private SoapBindingStub binding;
	private LoginResult loginResult;
	private GetUserInfoResult userInfo;
	private String sql;
	private String serverTimestamp;
	private QueryResult qr ;
	private GregorianCalendar startDate;
	private GregorianCalendar endDate;
	private SObject[] sObjects;
	private int recordsFilter;
	private String fieldsList;
	private int queryResultSize;
	private int recordsCount;

	private LogChannelInterface	logInterface;
	
	
	/**
	 * Construct a new Salesforce Connection
	 */
	public SalesforceConnection(LogChannelInterface logInterface, String url, String username, String password) throws KettleException {
		this.log=LogWriter.getInstance();
		this.logInterface = logInterface;
		this.url=url;
		this.username=username;
		this.password=password;
		this.timeout=0;
	
		this.binding=null;
		this.loginResult = null;
		this.userInfo = null;
		this.sql=null;
		this.serverTimestamp=null;
		this.qr=null;
		this.condition=null;
		this.startDate=null;
		this.endDate=null;
		this.sObjects=null;
		this.recordsFilter=SalesforceConnectionUtils.RECORDS_FILTER_ALL;
		this.fieldsList=null;
		this.queryResultSize=0;
		this.recordsCount=0;
		
		
		// check target URL
		if(Const.isEmpty(this.url))	throw new KettleException(BaseMessages.getString(PKG, "SalesforceInput.TargetURLMissing.Error"));
		
		// check username
		if(Const.isEmpty(this.username)) throw new KettleException(BaseMessages.getString(PKG, "SalesforceInput.UsernameMissing.Error"));
				
		if(log.isDetailed()) logInterface.logDetailed(BaseMessages.getString(PKG, "SalesforceInput.Log.NewConnection"));
	}
    
	public void setCalendar(int recordsFilter,GregorianCalendar startDate, GregorianCalendar endDate) throws KettleException {
		 this.startDate=startDate;
		 this.endDate=endDate;
		 this.recordsFilter=recordsFilter;
	}
	public void setCondition(String condition) {
		this.condition=condition;
	}
	public void setSQL(String sql) {
		this.sql=sql;
	}
	public void setFieldsList(String fieldsList) {
		this.fieldsList=fieldsList;
	}
	public void setModule(String module) {
		this.module=module;
	}
	public String getURL() {
		return this.url;
	}
	public String getSQL(){
		return this.sql;
	}
	public String getServerTimestamp(){
		return this.serverTimestamp;
	}
	public String getModule() {
		return this.module;
	}
	public QueryResult getQueryResult() {
		return this.qr;
	}
	
	public SoapBindingStub getBinding(){
		return this.binding;
	}
	public void setTimeOut(int timeout){
		this.timeout=timeout;
	}
	public void connect() throws KettleException{

		try{
			this.binding = (SoapBindingStub) new SforceServiceLocator().getSoap();
			if (log.isDetailed()) logInterface.logDetailed(BaseMessages.getString(PKG, "SalesforceInput.Log.LoginURL", binding._getProperty(SoapBindingStub.ENDPOINT_ADDRESS_PROPERTY)));
		      
	        //  Set timeout
	      	if(this.timeout>0) {
	      		binding.setTimeout(this.timeout);
	      		 if (log.isDebug())  logInterface.logDebug(BaseMessages.getString(PKG, "SalesforceInput.Log.SettingTimeout",""+this.timeout));
	      	}
	        
	      	
	      	// Set URL
	      	this.binding._setProperty(SoapBindingStub.ENDPOINT_ADDRESS_PROPERTY, this.url);
	        
	        // Attempt the login giving the user feedback
		     
	        if (log.isDetailed()) {
	        	logInterface.logDetailed(BaseMessages.getString(PKG, "SalesforceInput.Log.LoginNow"));
	        	logInterface.logDetailed("----------------------------------------->");
	        	logInterface.logDetailed(BaseMessages.getString(PKG, "SalesforceInput.Log.LoginURL",this.url));
	        	logInterface.logDetailed(BaseMessages.getString(PKG, "SalesforceInput.Log.LoginUsername",username));
	        	if(this.module!=null) logInterface.logDetailed(BaseMessages.getString(PKG, "SalesforceInput.Log.LoginModule", this.module));
	        	if(this.condition!=null) logInterface.logDetailed(BaseMessages.getString(PKG, "SalesforceInput.Log.LoginCondition",this.condition));
	        	logInterface.logDetailed("<-----------------------------------------");
	        }
	        
	        // Login
	        this.loginResult = this.binding.login(this.username, this.password);
	        
	        if (log.isDebug()) {
	        	logInterface.logDebug(BaseMessages.getString(PKG, "SalesforceInput.Log.SessionId") + " : " + this.loginResult.getSessionId());
	        	logInterface.logDebug(BaseMessages.getString(PKG, "SalesforceInput.Log.NewServerURL") + " : " + this.loginResult.getServerUrl());
	        }
	        
	        // set the session header for subsequent call authentication
	        this.binding._setProperty(SoapBindingStub.ENDPOINT_ADDRESS_PROPERTY,this.loginResult.getServerUrl());
	
	        // Create a new session header object and set the session id to that
	        // returned by the login
	        SessionHeader sh = new SessionHeader();
	        sh.setSessionId(loginResult.getSessionId());
	        this.binding.setHeader(new SforceServiceLocator().getServiceName().getNamespaceURI(), "SessionHeader", sh);
	       
	        // Return the user Infos
	        this.userInfo = this.binding.getUserInfo();
	        if (log.isDebug()) {
	        	logInterface.logDebug(BaseMessages.getString(PKG, "SalesforceInput.Log.UserInfos") + " : " + this.userInfo.getUserFullName());
	        	logInterface.logDebug("----------------------------------------->");
	        	logInterface.logDebug(BaseMessages.getString(PKG, "SalesforceInput.Log.UserName") + " : " + this.userInfo.getUserFullName());
	        	logInterface.logDebug(BaseMessages.getString(PKG, "SalesforceInput.Log.UserEmail") + " : " + this.userInfo.getUserEmail());
	        	logInterface.logDebug(BaseMessages.getString(PKG, "SalesforceInput.Log.UserLanguage") + " : " + this.userInfo.getUserLanguage());
	        	logInterface.logDebug(BaseMessages.getString(PKG, "SalesforceInput.Log.UserOrganization") + " : " + this.userInfo.getOrganizationName());    
	        	logInterface.logDebug("<-----------------------------------------");
	        }
	        
	    	this.serverTimestamp= this.binding.getServerTimestamp().toString();
	 		if(log.isDebug()) BaseMessages.getString(PKG, "SalesforceInput.Log.ServerTimestamp",""+this.serverTimestamp);
	 		
	       if(log.isDetailed()) logInterface.logDetailed(BaseMessages.getString(PKG, "SalesforceInput.Log.Connected"));
		
		}catch(Exception e){
			throw new KettleException(BaseMessages.getString(PKG, "SalesforceInput.Error.Connection"), e);
		}
	}

	 public void query(boolean specifyQuery) throws KettleException {
		 
		if(this.binding==null)  throw new KettleException(BaseMessages.getString(PKG, "SalesforceInput.Exception.CanNotGetBiding"));
		
	    try{
	    	if(!specifyQuery){
				// check if we can query this Object
			    DescribeSObjectResult describeSObjectResult = this.binding.describeSObject(this.module);
			    if (describeSObjectResult == null) throw new KettleException(BaseMessages.getString(PKG, "SalesforceInput.ErrorGettingObject"));  
			    if(!describeSObjectResult.isQueryable()) throw new KettleException(BaseMessages.getString(PKG, "SalesforceInputDialog.ObjectNotQueryable",module));
		    }
		    			        
		    if (this.sql!=null && log.isDetailed()) logInterface.logDetailed(BaseMessages.getString(PKG, "SalesforceInput.Log.SQLString") + " : " +  this.sql);        
		  
			switch (this.recordsFilter) {
				case SalesforceConnectionUtils.RECORDS_FILTER_UPDATED:
					// Updated records ...
		 			GetUpdatedResult updatedRecords = this.binding.getUpdated(this.module, this.startDate, this.endDate);
						
		 			if (updatedRecords.getIds() != null	&& updatedRecords.getIds().length > 0) {
		 				this.sObjects = this.binding.retrieve(this.fieldsList,this.module, updatedRecords.getIds());
		 				this.queryResultSize=this.sObjects.length;
		 			}
				break;
				case SalesforceConnectionUtils.RECORDS_FILTER_DELETED:
					  // Deleted records ...
			 		GetDeletedResult deletedRecordsResult = this.binding.getDeleted(this.module, this.startDate, this.endDate);
					
					DeletedRecord[] deletedRecords = deletedRecordsResult.getDeletedRecords();
					List<String> idlist = new ArrayList<String>();
					if (deletedRecords != null	&& deletedRecords.length > 0) {
						for (DeletedRecord deletedRecord : deletedRecords) {
							idlist.add(deletedRecord.getId());
						}
			 		
						this.qr = this.binding.queryAll(this.sql);
						this.sObjects = this.qr.getRecords();
						this.queryResultSize=this.sObjects.length;
					}
				break;
				default:
					// return query result
		 			this.qr = this.binding.query(this.sql);
		 			this.sObjects=this.qr.getRecords();
	 				this.queryResultSize= this.qr.getSize();
				break;
			}
			if(this.sObjects!=null) this.recordsCount=this.sObjects.length;
		}catch(Exception e){
			throw new KettleException(BaseMessages.getString( PKG, "SalesforceConnection.Exception.Query"),e);
		}
	 }
	 public void close() throws KettleException
	 {
		 try {
				if(!this.qr.isDone()) {
					this.qr.setDone(true);
					this.qr=null;
				}
				if(this.sObjects!=null) this.sObjects=null;
				if(this.binding!=null) this.binding=null;
				if(this.loginResult!=null) this.loginResult=null;
				if(this.userInfo!=null) this.userInfo=null;

				if(log.isDetailed()) logInterface.logDetailed(BaseMessages.getString(PKG, "SalesforceInput.Log.ConnectionClosed"));
			}catch(Exception e){
				throw new KettleException(BaseMessages.getString(PKG, "SalesforceInput.Error.ClosingConnection"),e);
			};
	 }
	 public int getQueryResultSize() {
		 return this.queryResultSize;
	 }
	 public int getRecordsCount(){
		return this.recordsCount;
	 }

	 public String getRecordValue(int recordIndex, int valueIndex) {
	 	SObject con=this.sObjects[recordIndex];
	 	if(con==null) return null;
	 	if(con.get_any()[valueIndex]!=null) 
			 return con.get_any()[valueIndex].getValue();
		 else
			 return null;
	 }
	 // Get SOQL meta data (not a Good way but i don't see any other way !)
	 // TODO : Go back to this one
	 // I am sure there is an easy way to return meta for a SOQL result
	 public MessageElement[] getElements() {
		 SObject con=qr.getRecords()[0];
		 if(con==null) return null;
			 return con.get_any();
	 }
	 public boolean queryMore() throws KettleException {
		 try {
			// check the done attribute on the QueryResult and call QueryMore 
			// with the QueryLocator if there are more records to be retrieved
			if(!this.qr.isDone()) {
				this.qr=this.binding.queryMore(this.qr.getQueryLocator());
				this.sObjects=this.qr.getRecords();
				this.queryResultSize= this.qr.getSize();
				return true;
			}else{
				// Query is done .. we finished !
				return false;
			}
		 }catch(Exception e){
			 throw new KettleException(BaseMessages.getString(PKG, "SalesforceInput.Error.QueringMore"),e);
		 }
	}
  public Field[] getModuleFields(String module) throws KettleException
  {
	  DescribeSObjectResult describeSObjectResult=null;
	  try  {
		  // Get object
	      describeSObjectResult = this.binding.describeSObject(module);
	      if(describeSObjectResult==null) return null;
     
		   if(!describeSObjectResult.isQueryable()){
				throw new KettleException(BaseMessages.getString(PKG, "SalesforceInputDialog.ObjectNotQueryable",this.module));
		   }else{
		        // we can query this object
	           return  describeSObjectResult.getFields();
		   }
	   } catch(Exception e){
		   throw new KettleException(BaseMessages.getString(PKG, "SalesforceInput.Error.GettingModuleFields", this.module),e);
	   }finally  {
		   if(describeSObjectResult!=null) describeSObjectResult=null;
	   }
  }  
  
  public UpsertResult[] upsert(String upsertField, SObject[] sfBuffer) throws KettleException
  {
	  try {
		  return getBinding().upsert(upsertField, sfBuffer);
	  }catch(Exception e) {
		  throw new KettleException("Erreur while doing upsert operation!", e);
	  }
  }
  public SaveResult[] insert(SObject[] sfBuffer) throws KettleException
  {
	  try {
		  return getBinding().create(sfBuffer);
	  }catch(Exception e) {
		  throw new KettleException("Erreur while doing insert operation!", e);
	  }
  }
  public SaveResult[] update(SObject[] sfBuffer) throws KettleException
  {
	  try {
		  return getBinding().update(sfBuffer);
	  }catch(Exception e) {
		  throw new KettleException("Erreur while doing update operation!", e);
	  }
  }
  public DeleteResult[] delete(String[] id) throws KettleException
  {
	  try {
		  return getBinding().delete(id);
	  }catch(Exception e) {
		  throw new KettleException("Erreur while doing delete operation!", e);
	  }
  }
  public String toString()
  {
	  return "SalesforceConnection";
  }

}
