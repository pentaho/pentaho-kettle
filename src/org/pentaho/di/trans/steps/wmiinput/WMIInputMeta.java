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

package org.pentaho.di.trans.steps.wmiinput;


import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * @author Samatar
 * @since 01-10-2011
 */
public class WMIInputMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = WMIInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String wmiQuery;
	private String rowLimit;
    
    private boolean variableReplacementActive;
    
    
    private String domain;
    private String host;
    private String userName;
    private String password;
    
	public WMIInputMeta()
	{
		super();
	}
	
	/**
	 * @return Returns the password.
	 */
	public String getPassword()
	{
		return password;
	}
	
	/**
	 * @param host The password to set.
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}
	/**
	 * @return Returns the host.
	 */
	public String getUserName()
	{
		return userName;
	}
	
	/**
	 * @param host The userName to set.
	 */
	public void setUserName(String userName)
	{
		this.userName = userName;
	}
	/**
	 * @return Returns the host.
	 */
	public String getHost()
	{
		return host;
	}
	
	/**
	 * @param host The host to set.
	 */
	public void setHost(String host)
	{
		this.host = host;
	}
	/**
	 * @return Returns the domain.
	 */
	public String getDomain()
	{
		return domain;
	}
	
	/**
	 * @param domain The domain to set.
	 */
	public void setDomain(String domain)
	{
		this.domain = domain;
	}

	
	/**
	 * @return Returns the wmi.
	 */
	public String getWMI()
	{
		return wmiQuery;
	}
	
	/**
	 * @param wmiQuery The wmiQuery to set.
	 */
	public void setWMIQuery(String wmiQuery)
	{
		this.wmiQuery = wmiQuery;
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleXMLException
	{
		readData(stepnode, databases);
	}


	public Object clone()
	{
		WMIInputMeta retval = (WMIInputMeta)super.clone();
		return retval;
	}
	
	private void readData(Node stepnode, List<? extends SharedObjectInterface> databases)
	throws KettleXMLException
	{
		try
		{
			wmiQuery               = XMLHandler.getTagValue(stepnode, "wmiQuery");
			rowLimit                  = XMLHandler.getTagValue(stepnode, "limit");
            variableReplacementActive = "Y".equals(XMLHandler.getTagValue(stepnode, "variables_active"));
            
            domain               = XMLHandler.getTagValue(stepnode, "domain");
            host               = XMLHandler.getTagValue(stepnode, "host");
            userName               = XMLHandler.getTagValue(stepnode, "userName");
            setPassword(Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(stepnode, "password")));
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}

	public void setDefault()
	{
		wmiQuery        = "SELECT <values> FROM <classe> WHERE <conditions>";
		rowLimit   = "0";
		domain=null;
		host=null;
		userName=null;
		password=null;
	}


	  public void getFields(RowMetaInterface row, String origin, RowMetaInterface[] info, 
			  StepMeta nextStep, VariableSpace space) throws KettleStepException 
	  {

		WMIQuery query =null;
		String wmq= space.environmentSubstitute(wmiQuery);
		try {
		
			if(Const.isEmpty(wmq)) {
				throw new KettleException(BaseMessages.getString(PKG, "WMIInput.Error.WMIQueryEmpty"));
			}
			
			// Open a new connection
			query = new WMIQuery(new LogChannel(this));
			
			// Now connect
			query.connect();
			
			// Retrieve meta data
			RowMetaInterface add =query.getRowMeta(wmq);
			
			if (add!=null) {
				for (int i=0;i<add.size();i++) {
					ValueMetaInterface v=add.getValueMeta(i);
					v.setOrigin(origin);
				}
				row.addRowMeta(add );
			}
	
		}catch(Exception e) {
			throw new KettleStepException(BaseMessages.getString(PKG, "WMIInputMeta.GetFields.Error", wmq), e);
		}finally {
			// don't forget close connection
			try {
				if(query!=null) query.close();
			}catch(Exception e){};
		}
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
	
		retval.append("    "+XMLHandler.addTagValue("wmiQuery",        wmiQuery));
		retval.append("    "+XMLHandler.addTagValue("limit",      rowLimit));
        retval.append("    "+XMLHandler.addTagValue("variables_active",   variableReplacementActive));
        
        retval.append("    "+XMLHandler.addTagValue("domain",        domain));
        retval.append("    "+XMLHandler.addTagValue("host",        host));
        retval.append("    "+XMLHandler.addTagValue("userName",        userName));
        retval.append("    "+XMLHandler.addTagValue("password", Encr.encryptPasswordIfNotUsingVariables(password)));

        
		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			
			wmiQuery            =      rep.getStepAttributeString (id_step, "wmiQuery");
			rowLimit                  = rep.getStepAttributeString(id_step, "limit");
            variableReplacementActive =      rep.getStepAttributeBoolean(id_step, "variables_active");
            
        	domain            =      rep.getStepAttributeString (id_step, "domain");
        	host            =      rep.getStepAttributeString (id_step, "host");
        	userName            =      rep.getStepAttributeString (id_step, "userName");
        	password              = Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString (id_step, "password") );	
			
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}
	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "wmiQuery",              wmiQuery);
			rep.saveStepAttribute(id_transformation, id_step, "limit",            rowLimit);
            rep.saveStepAttribute(id_transformation, id_step, "variables_active", variableReplacementActive);
            
            rep.saveStepAttribute(id_transformation, id_step, "domain",              domain);
            rep.saveStepAttribute(id_transformation, id_step, "host",              host);
            rep.saveStepAttribute(id_transformation, id_step, "userName",              userName);
            rep.saveStepAttribute(id_transformation, id_step, "password", Encr.encryptPasswordIfNotUsingVariables(password));

		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		
		
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Step is not expecting info from input steps.", stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "No input expected, no input provided.", stepMeta);
			remarks.add(cr);
		}
		
		
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new WMIInput(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new WMIInputData();
	}

	
    /**
     * @return Returns the variableReplacementActive.
     */
    public boolean isVariableReplacementActive()
    {
        return variableReplacementActive;
    }

    /**
     * @param variableReplacementActive The variableReplacementActive to set.
     */
    public void setVariableReplacementActive(boolean variableReplacementActive)
    {
        this.variableReplacementActive = variableReplacementActive;
    }
	/**
	 * @return Returns the rowLimit.
	 */
	public String getRowLimit()
	{
		return rowLimit;
	}
	
	/**
	 * @param rowLimit The rowLimit to set.
	 */
	public void setRowLimit(String rowLimit)
	{
		this.rowLimit = rowLimit;
	}
}
