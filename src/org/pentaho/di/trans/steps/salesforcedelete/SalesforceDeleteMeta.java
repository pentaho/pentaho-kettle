
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
 

/* 
 * 
 * Created on 10-07-2007
 * 
 */

package org.pentaho.di.trans.steps.salesforcedelete;

import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.salesforceinput.SalesforceConnectionUtils;
import org.pentaho.di.i18n.BaseMessages;

public class SalesforceDeleteMeta extends BaseStepMeta implements StepMetaInterface
{	
	private static Class<?> PKG = SalesforceDeleteMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	/** The salesforce url*/
	private String targeturl;
	
	/** The userName*/
	private String username;
	
	/** The password*/
	private String password;
	
	/** The module*/
	private String module;
	
	/** Deletefield */
	private String DeleteField;
	

	/** Batch size */
	private String batchSize;
	
	/** The time out */
	private  String  timeout;
	
	private boolean useCompression;

	private boolean rollbackAllChangesOnError;
	
	
	public SalesforceDeleteMeta()
	{
		super(); // allocate BaseStepMeta
	}
	/**
	 * @return Returns the rollbackAllChangesOnError.
	 */
	public boolean isRollbackAllChangesOnError()
	{
		return rollbackAllChangesOnError;
	}
    
	/**
	 * @param rollbackAllChangesOnError The rollbackAllChangesOnError to set.
	 */
	public void setRollbackAllChangesOnError(boolean rollbackAllChangesOnError)
	{
		this.rollbackAllChangesOnError = rollbackAllChangesOnError;
	}	
	/**
	 * @return Returns the useCompression.
	 */
	public boolean isUsingCompression()
	{
		return useCompression;
	}
    
	/**
	 * @param useCompression The useCompression to set.
	 */
	public void setUseCompression(boolean useCompression)
	{
		this.useCompression = useCompression;
	}
    
	/**
	 * @return Returns the TimeOut.
	 */
	public String getTimeOut()
	{
		return timeout;
	}
	/**
	 * @param TimeOut The TimeOut to set.
	 */
	public void setTimeOut(String TimeOut)
	{
		this.timeout = TimeOut;
	}
	/**
	 * @return Returns the UserName.
	 */
	public String getUserName()
	{
		return username;
	}
    
	/**
	 * @param user_name The UserNAme to set.
	 */
	public void setUserName(String user_name)
	{
		this.username = user_name;
	}
	/**
	 * @return Returns the Password.
	 */
	public String getPassword()
	{
		return password;
	}
    
	/**
	 * @param passwd The password to set.
	 */
	public void setPassword(String passwd)
	{
		this.password = passwd;
	}
  
	/**
	 * @return Returns the module.
	 */
	public String getModule()
	{
		return module;
	}
	/**
	 * @param module The module to set.
	 */
	public void setModule(String module)
	{
		this.module = module;
	}
	/**
	 * @param DeleteField The DeleteField to set.
	 */
	public void setDeleteField(String DeleteField)
	{
		this.DeleteField = DeleteField;
	}
	
	/**
	 * @return Returns the DeleteField.
	 */
	public String getDeleteField()
	{
		return this.DeleteField;
	}

	/**
	 * @param batch size.
	 */
	public void setBatchSize(String value)
	{
		this.batchSize = value;
	}
	
	/**
	 * @return Returns the batchSize.
	 */
	public String getBatchSize()
	{
		return this.batchSize;
	}

	public int getBatchSizeInt()
	{
		return Const.toInt(this.batchSize, 10);
	}

    
	/**
	 * @return Returns the targeturl.
	 */
	public String getTargetURL()
	{
		return targeturl;
	}
    
	/**
	 * @param url The url to set.
	 */
	public void setTargetURL(String urlvalue)
	{
		this.targeturl = urlvalue;
	}

   public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
 	    throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{
		SalesforceDeleteMeta retval = (SalesforceDeleteMeta)super.clone();

		return retval;
	}
    
	public String getXML()
	{
		StringBuffer retval=new StringBuffer();
		retval.append("    "+XMLHandler.addTagValue("targeturl",   targeturl));
		retval.append("    "+XMLHandler.addTagValue("username",   username));
		retval.append("    "+XMLHandler.addTagValue("password",   Encr.encryptPasswordIfNotUsingVariables(password), false));
		retval.append("    "+XMLHandler.addTagValue("module",   module));
		retval.append("    "+XMLHandler.addTagValue("DeleteField",   DeleteField));
		retval.append("    "+XMLHandler.addTagValue("batchSize",   batchSize));
		retval.append("    "+XMLHandler.addTagValue("timeout", timeout));
		retval.append("    "+XMLHandler.addTagValue("useCompression",   useCompression));
		retval.append("    "+XMLHandler.addTagValue("rollbackAllChangesOnError",   rollbackAllChangesOnError));
		
		return retval.toString();
	}

	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{
			targeturl     = XMLHandler.getTagValue(stepnode, "targeturl");
			username     = XMLHandler.getTagValue(stepnode, "username");
			password     = XMLHandler.getTagValue(stepnode, "password");
			if (password != null && password.startsWith("Encrypted")){
				password = Encr.decryptPassword(password.replace("Encrypted","").replace(" ", ""));
			}

			module     = XMLHandler.getTagValue(stepnode, "module");
			DeleteField= XMLHandler.getTagValue(stepnode, "DeleteField");
			
			batchSize = XMLHandler.getTagValue(stepnode, "batchSize");
			useCompression   = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "useCompression"));
			timeout = XMLHandler.getTagValue(stepnode, "timeout");
			rollbackAllChangesOnError   = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "rollbackAllChangesOnError"));
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}

	
	public void setDefault()
	{
		targeturl=SalesforceConnectionUtils.TARGET_DEFAULT_URL ;
		password = "";
		module = "Account";
		DeleteField = null;
		batchSize="10";
		useCompression=false;
		rollbackAllChangesOnError=false;
		timeout= "60000";

	}
	
	/* This function adds meta data to the rows being pushed out */
	public void getFields(RowMetaInterface r, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
	{

	}
	
	
	public void readRep(Repository rep, ObjectId id_step,
			List<DatabaseMeta> databases, Map<String, Counter> counters)
	    throws KettleException
	{
		try
		{
			targeturl     = rep.getStepAttributeString (id_step, "targeturl");
			module			 =  rep.getStepAttributeString(id_step, "module");
			DeleteField		 =   rep.getStepAttributeString(id_step, "DeleteField");
			username		 =  rep.getStepAttributeString(id_step, "username");
			password		 =  rep.getStepAttributeString(id_step, "password");
			batchSize = rep.getStepAttributeString(id_step, "batchSize");
			useCompression   = rep.getStepAttributeBoolean(id_step, "useCompression"); 
			timeout          =  rep.getStepAttributeString(id_step, "timeout");
			rollbackAllChangesOnError   = rep.getStepAttributeBoolean(id_step, "rollbackAllChangesOnError"); 
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "SalesforceDeleteMeta.Exception.ErrorReadingRepository"), e);
		}
	}
	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "targeturl",         targeturl);
			rep.saveStepAttribute(id_transformation, id_step, "batchSize",           batchSize);
			rep.saveStepAttribute(id_transformation, id_step, "module",   module);
			rep.saveStepAttribute(id_transformation, id_step, "DeleteField",   DeleteField);
			rep.saveStepAttribute(id_transformation, id_step, "username",   username);
			rep.saveStepAttribute(id_transformation, id_step, "password",   password);
			rep.saveStepAttribute(id_transformation, id_step, "useCompression",  useCompression);
			rep.saveStepAttribute(id_transformation, id_step, "timeout",           timeout);
			rep.saveStepAttribute(id_transformation, id_step, "rollbackAllChangesOnError",  rollbackAllChangesOnError);
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "SalesforceDeleteMeta.Exception.ErrorSavingToRepository", ""+id_step), e);
		}
	}
	

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;

		// See if we get input...
		if (input.length>0)	
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SalesforceDeleteMeta.CheckResult.NoInputExpected"), stepMeta);
		else
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SalesforceDeleteMeta.CheckResult.NoInput"), stepMeta);
		remarks.add(cr);
		
		// check URL
		if(Const.isEmpty(targeturl))
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SalesforceDeleteMeta.CheckResult.NoURL"), stepMeta);
		else
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SalesforceDeleteMeta.CheckResult.URLOk"), stepMeta);
		remarks.add(cr);
		
		// check username
		if(Const.isEmpty(username))
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SalesforceDeleteMeta.CheckResult.NoUsername"), stepMeta);
		else
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SalesforceDeleteMeta.CheckResult.UsernameOk"), stepMeta);
		remarks.add(cr);
		
		// check module
		if(Const.isEmpty(module))
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SalesforceDeleteMeta.CheckResult.NoModule"), stepMeta);
		else
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SalesforceDeleteMeta.CheckResult.ModuleOk"), stepMeta);
		remarks.add(cr);
		

		
	
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new SalesforceDelete(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new SalesforceDeleteData();
	}
	
	 public boolean supportsErrorHandling()
    {
	        return true;
    }

}