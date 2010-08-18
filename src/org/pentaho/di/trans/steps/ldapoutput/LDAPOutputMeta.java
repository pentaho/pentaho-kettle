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


package org.pentaho.di.trans.steps.ldapoutput;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

public class LDAPOutputMeta extends BaseStepMeta implements StepMetaInterface
{	
	private static Class<?> PKG = LDAPOutputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	/** Flag indicating that we use authentication for connection */
	private boolean useAuthentication;

	/** The Host name*/
	private  String  Host;
	
	/** The User name*/
	private  String  userName;
	
	/** The Password to use in LDAP authentication*/
	private String password;
	
	/** The Port*/
	private  String  port;
	
	/**	The name of DN field */
	private String dnFieldName;
	
	private boolean failIfNotExist;
	

    /** Field value to update */
	private String updateLookup[];
	
    /** Stream name to update value with */
	private String updateStream[];
	
    /** boolean indicating if field needs to be updated */
	private Boolean update[];
	
	/** Operations type */	
	private String searchBase;
	
	
	/** Multi valued separator **/
	private String multiValuedSeparator;
	
	private int operationType;
	
	/**
	 * The operations description
	 */
	public final static String operationTypeDesc[] = {
			BaseMessages.getString(PKG, "LDAPOutputMeta.operationType.Insert"),
			BaseMessages.getString(PKG, "LDAPOutputMeta.operationType.Upsert"),
			BaseMessages.getString(PKG, "LDAPOutputMeta.operationType.Update"),
			BaseMessages.getString(PKG, "LDAPOutputMeta.operationType.Add"),
			BaseMessages.getString(PKG, "LDAPOutputMeta.operationType.Delete")
			};
	
	/**
	 * The operations type codes
	 */
	public final static String operationTypeCode[] = { "insert", "upsert", "update", "add", "delete" };

	public final static int OPERATION_TYPE_INSERT = 0;

	public final static int OPERATION_TYPE_UPSERT = 1;

	public final static int OPERATION_TYPE_UPDATE = 2;
	
	public final static int OPERATION_TYPE_ADD = 3;
	
	public final static int OPERATION_TYPE_DELETE = 4;
	
	private int referralType;
	
	/**
	 * The referrals description
	 */
	public final static String referralTypeDesc[] = {
			BaseMessages.getString(PKG, "LDAPOutputMeta.referralType.Follow"),
			BaseMessages.getString(PKG, "LDAPOutputMeta.referralType.Ignore")
			};
	
	/**
	 * The referrals type codes
	 */
	public final static String referralTypeCode[] = { "follow", "ignore"};

	public final static int REFERRAL_TYPE_FOLLOW = 0;

	public final static int REFERRAL_TYPE_IGNORE = 1;


	private int derefAliasesType;
	
	/**
	 * The derefAliasess description
	 */
	public final static String derefAliasesTypeDesc[] = {
			BaseMessages.getString(PKG, "LDAPOutputMeta.derefAliasesType.Always"),
			BaseMessages.getString(PKG, "LDAPOutputMeta.derefAliasesType.Never"),
			BaseMessages.getString(PKG, "LDAPOutputMeta.derefAliasesType.Searching"),
			BaseMessages.getString(PKG, "LDAPOutputMeta.derefAliasesType.Finding")
			};
	
	/**
	 * The derefAliasess type codes
	 */
	public final static String derefAliasesTypeCode[] = { "always", "never", "searching", "finding"};

	public final static int DEREFALIASES_TYPE_ALWAYS = 0;

	public final static int DEREFALIASES_TYPE_NEVER = 1;

	public final static int DEREFALIASES_TYPE_SEARCHING = 2;
	
	public final static int DEREFALIASES_TYPE_FINDING = 3;
	
	public LDAPOutputMeta()
	{
		super(); // allocate BaseStepMeta
	}
	public Boolean[] getUpdate() {
		return update;
	}

	public void setUpdate(Boolean[] update) {
		this.update = update;
	}
   public int getOperationType() {
		return operationType;
	}
   public int getReferralType() {
		return referralType;
	}
   public int getDerefAliasesType() {
		return derefAliasesType;
	}
	    
	public static int getOperationTypeByDesc(String tt) {
		if (tt == null)
			return 0;

		for (int i = 0; i < operationTypeDesc.length; i++) {
			if (operationTypeDesc[i].equalsIgnoreCase(tt))
				return i;
		}
		// If this fails, try to match using the code.
		return getOperationTypeByCode(tt);
	}
		
		public static int getReferralTypeByDesc(String tt) {
			if (tt == null)
				return 0;

			for (int i = 0; i < referralTypeDesc.length; i++) {
				if (referralTypeDesc[i].equalsIgnoreCase(tt))
					return i;
			}
			// If this fails, try to match using the code.
			return getReferralTypeByCode(tt);
		}
		public static int getDerefAliasesTypeByDesc(String tt) {
			if (tt == null)
				return 0;

			for (int i = 0; i < derefAliasesTypeDesc.length; i++) {
				if (derefAliasesTypeDesc[i].equalsIgnoreCase(tt))
					return i;
			}
			// If this fails, try to match using the code.
			return getReferralTypeByCode(tt);
		}
	    private static int getOperationTypeByCode(String tt) {
			if (tt == null)
				return 0;

			for (int i = 0; i < operationTypeCode.length; i++) {
				if (operationTypeCode[i].equalsIgnoreCase(tt))
					return i;
			}
			return 0;
		}
	    
	    private static int getReferralTypeByCode(String tt) {
			if (tt == null)
				return 0;

			for (int i = 0; i < referralTypeCode.length; i++) {
				if (referralTypeCode[i].equalsIgnoreCase(tt))
					return i;
			}
			return 0;
		}
	    private static int getDerefAliasesTypeByCode(String tt) {
			if (tt == null)
				return 0;

			for (int i = 0; i < derefAliasesTypeCode.length; i++) {
				if (derefAliasesTypeCode[i].equalsIgnoreCase(tt))
					return i;
			}
			return 0;
		}
		public void setOperationType(int operationType) {
			this.operationType = operationType;
		}
		
		public void setReferralType(int value) {
			this.referralType = value;
		}
		public void setDerefAliasesType(int value) {
			this.derefAliasesType = value;
		}
		public static String getOperationTypeDesc(int i) {
			if (i < 0 || i >= operationTypeDesc.length)
				return operationTypeDesc[0];
			return operationTypeDesc[i];
		}
		
		public static String getReferralTypeDesc(int i) {
			if (i < 0 || i >= referralTypeDesc.length)
				return referralTypeDesc[0];
			return referralTypeDesc[i];
		}
		
		public static String getDerefAliasesTypeDesc(int i) {
			if (i < 0 || i >= derefAliasesTypeDesc.length)
				return derefAliasesTypeDesc[0];
			return derefAliasesTypeDesc[i];
		}
    /**
     * @return Returns the updateStream.
     */
    public String[] getUpdateStream()
    {
        return updateStream;
    }
    
    /**
     * @param updateStream The updateStream to set.
     */
    public void setUpdateStream(String[] updateStream)
    {
        this.updateStream = updateStream;
    }
	 /**
     * @return Returns the updateLookup.
     */
    public String[] getUpdateLookup()
    {
        return updateLookup;
    }
    
    /**
     * @param updateLookup The updateLookup to set.
     */
    public void setUpdateLookup(String[] updateLookup)
    {
        this.updateLookup = updateLookup;
    }
	/**
     * @return Returns the input useAuthentication.
     */
	public boolean UseAuthentication()
	{
		return useAuthentication;
	}
	
	 /**
     * @param useAuthentication The useAuthentication to set.
     */
	public void setUseAuthentication(boolean useAuthentication)
	{
		this.useAuthentication=useAuthentication;
	}
	
   
    /**
     * @return Returns the host name.
     */
    public String getHost()
    {
    	return Host;
    }
    
    /**
     * @param host The host to set.
     */
    public void setHost(String host)
    {
    	this.Host=host;
    }
    
    
    /**
     * @return Returns the user name.
     */
    public String getUserName()
    {
    	return userName;
    }
    
    /**
     * @param userName The username to set.
     */
    public void setUserName(String userName)
    {
    	this.userName=userName;
    }
    
    /**
     * @param password The password to set.
     */
    public void setPassword(String password)
    {
    	this.password=password;
    }
    
    /**
     * @return Returns the password.
     */
    public String getPassword()
    {
    	return password;
    }
    
   public void setDnField(String value)
   {
	   this.dnFieldName=value;
   }
    
   public String getDnField()
   {
	  return this.dnFieldName;
   }
    
    
    /**
     * @return Returns the Port.
     */
    public String getPort()
    {
    	return port;
    }
    
    
    /**
     * @param port The port to set.
     */
    public void setPort(String port)
    {
    	this.port=port;
    }
    /**
     * @return Returns the failIfNotExist.
     */
    public boolean isFailIfNotExist()
    {
    	return failIfNotExist;
    }
    
    
    /**
     * @param failIfNotExist The failIfNotExist to set.
     */
    public void setFailIfNotExist(boolean value)
    {
    	this.failIfNotExist=value;
    }
    
   
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleXMLException
	{
    	readData(stepnode);
	}

	public Object clone()
	{
		LDAPOutputMeta retval = (LDAPOutputMeta)super.clone();
		int nrvalues  = updateLookup.length;

		retval.allocate(nrvalues);

		for (int i=0;i<nrvalues;i++)
		{
			retval.updateLookup[i] = updateLookup[i];
			retval.updateStream[i] = updateStream[i];
			retval.update[i]       = update[i];
		}
		return retval;
	}
    

	   /**
     * @param searchBase The searchBase filed.
     */
    public void setSearchBaseDN(String searchBase)
    {
        this.searchBase = searchBase;
    }
    
    
    /**
     * @return Returns the searchBase.
     */
    public String getSearchBaseDN()
    {
        return searchBase;
    }
    
    
    /**
     * @param multiValuedSeparator The multi-valued separator filed.
     */
    public void setMultiValuedSeparator(String multiValuedSeparator)
    {
        this.multiValuedSeparator = multiValuedSeparator;
    }
    
    
    /**
     * @return Returns the multi valued separator.
     */
    public String getMultiValuedSeparator()
    {
        return multiValuedSeparator;
    }
	public void allocate(int nrvalues)
	{
		updateLookup = new String[nrvalues];
		updateStream = new String[nrvalues];    
		update       = new Boolean[nrvalues];
	}
	private static String getOperationTypeCode(int i) {
		if (i < 0 || i >= operationTypeCode.length)
			return operationTypeCode[0];
		return operationTypeCode[i];
	}
	
	public static String getReferralTypeCode(int i) {
		if (i < 0 || i >= referralTypeCode.length)
			return referralTypeCode[0];
		return referralTypeCode[i];
	}
	
	public static String getDerefAliasesCode(int i) {
		if (i < 0 || i >= derefAliasesTypeCode.length)
			return derefAliasesTypeCode[0];
		return derefAliasesTypeCode[i];
	}
    public String getXML()
    {
        StringBuffer retval=new StringBuffer(500);

        retval.append("    ").append(XMLHandler.addTagValue("useauthentication",  useAuthentication));
        retval.append("    ").append(XMLHandler.addTagValue("host",    Host));
        retval.append("    ").append(XMLHandler.addTagValue("username",    userName));
        retval.append("    ").append(XMLHandler.addTagValue("password", Encr.encryptPasswordIfNotUsingVariables(password)));
        retval.append("    ").append(XMLHandler.addTagValue("port",    port));
        retval.append("    ").append(XMLHandler.addTagValue("dnFieldName",    dnFieldName));
        retval.append("    ").append(XMLHandler.addTagValue("failIfNotExist",    failIfNotExist));
        retval.append("    ").append(XMLHandler.addTagValue("operationType",getOperationTypeCode(operationType)));
        retval.append("    ").append(XMLHandler.addTagValue("multivaluedseparator", multiValuedSeparator));
        retval.append("    ").append(XMLHandler.addTagValue("searchBase", searchBase));
        retval.append("    ").append(XMLHandler.addTagValue("referralType",getReferralTypeCode(referralType)));
        retval.append("    ").append(XMLHandler.addTagValue("derefAliasesType",getDerefAliasesCode(derefAliasesType)));
        
		retval.append("    <fields>"+Const.CR);
		
		for (int i=0;i<updateLookup.length;i++)
		{
			retval.append("      <field>").append(Const.CR); //$NON-NLS-1$
			retval.append("        ").append(XMLHandler.addTagValue("name", updateLookup[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("field", updateStream[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("update", update[i].booleanValue()));
			retval.append("      </field>").append(Const.CR); //$NON-NLS-1$
		}
		
		retval.append("      </fields>"+Const.CR);

        return retval.toString();
    }

	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{
			
			useAuthentication  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "useauthentication"));
			Host    = XMLHandler.getTagValue(stepnode, "host");
			userName    = XMLHandler.getTagValue(stepnode, "username");
			setPassword(Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(stepnode, "password")));

			port    = XMLHandler.getTagValue(stepnode, "port");
			dnFieldName    = XMLHandler.getTagValue(stepnode, "dnFieldName");
			failIfNotExist    = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "failIfNotExist"));
			operationType = getOperationTypeByCode(Const.NVL(XMLHandler.getTagValue(stepnode,	"operationType"), ""));
			multiValuedSeparator = XMLHandler.getTagValue(stepnode, "multivaluedseparator");
			searchBase = XMLHandler.getTagValue(stepnode, "searchBase");
			referralType = getReferralTypeByCode(Const.NVL(XMLHandler.getTagValue(stepnode,	"referralType"), ""));
			derefAliasesType = getDerefAliasesTypeByCode(Const.NVL(XMLHandler.getTagValue(stepnode,	"derefAliasesType"), ""));
			
			Node fields     = XMLHandler.getSubNode(stepnode,  "fields");
			int nrFields    = XMLHandler.countNodes(fields,    "field");
	
			allocate( nrFields);

			for (int i=0;i<nrFields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				
				updateLookup[i]    = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$
				updateStream[i]    = XMLHandler.getTagValue(fnode, "field"); //$NON-NLS-1$
				if (updateStream[i]==null) updateStream[i]=updateLookup[i]; // default: the same name!
				String updateValue = XMLHandler.getTagValue(fnode, "update"); //$NON-NLS-1$
				if(updateValue==null) {
					//default TRUE
					update[i] = Boolean.TRUE;
				} else 
                {
                    if (updateValue.equalsIgnoreCase("Y"))
                        update[i] = Boolean.TRUE;
                    else
                        update[i] = Boolean.FALSE; 
				}
			}
			
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "LDAPOutputMeta.UnableToLoadFromXML"), e);
		}
	}
	
	
	public void setDefault()
	{
		useAuthentication=false;
		Host="";
		userName="";
		password="";
		port="389";
		dnFieldName=null;
		failIfNotExist=true;
		multiValuedSeparator=";";
		searchBase=null;
		
		int nrFields =0;
		allocate(nrFields);	
		
		for (int i=0;i<nrFields;i++){
			updateLookup[i]="name"+(i+1); //$NON-NLS-1$
			updateStream[i]="field"+(i+1); //$NON-NLS-1$
			update[i]=Boolean.TRUE; //$NON-NLS-1$
		}
        operationType=OPERATION_TYPE_INSERT;
        referralType = REFERRAL_TYPE_FOLLOW;
        derefAliasesType= DEREFALIASES_TYPE_ALWAYS;
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException
	{
	
		try
		{
			useAuthentication  = rep.getStepAttributeBoolean(id_step, "useauthentication");
			Host    = rep.getStepAttributeString (id_step, "host");
			userName    = rep.getStepAttributeString (id_step, "username");
			password      = Encr.decryptPasswordOptionallyEncrypted( rep.getStepAttributeString (id_step, "password") );	
			port    = rep.getStepAttributeString (id_step, "port");
			dnFieldName    = rep.getStepAttributeString (id_step, "dnFieldName");	
			failIfNotExist    = rep.getStepAttributeBoolean(id_step, "failIfNotExist");
	      	operationType = getOperationTypeByCode(Const.NVL(rep.getStepAttributeString(id_step, "operationType"), ""));
			multiValuedSeparator    = rep.getStepAttributeString (id_step, "multivaluedseparator");
			searchBase    = rep.getStepAttributeString (id_step, "searchBase");
		   	referralType = getReferralTypeByCode(Const.NVL(rep.getStepAttributeString(id_step, "referralType"), ""));
		   	derefAliasesType = getDerefAliasesTypeByCode(Const.NVL(rep.getStepAttributeString(id_step, "referralType"), ""));
		   	
			int nrFields      = rep.countNrStepAttributes(id_step, "field_name");
			allocate(nrFields);

			
			for (int i=0;i<nrFields;i++)
			{
				updateLookup[i]  = rep.getStepAttributeString(id_step, i, "field_name"); //$NON-NLS-1$
				updateStream[i]  = rep.getStepAttributeString(id_step, i, "field_attribut"); //$NON-NLS-1$
				update[i]        = Boolean.valueOf(rep.getStepAttributeBoolean(id_step, i, "value_update",true)); 
			}
        }
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "LDAPOutputMeta.Exception.ErrorReadingRepository"), e);
		}
	}
	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "useauthentication",  useAuthentication);
			rep.saveStepAttribute(id_transformation, id_step, "host",    Host);
			rep.saveStepAttribute(id_transformation, id_step, "username", userName);
			rep.saveStepAttribute(id_transformation, id_step, "password", Encr.encryptPasswordIfNotUsingVariables(password));

			rep.saveStepAttribute(id_transformation, id_step, "port",   port);
			rep.saveStepAttribute(id_transformation, id_step, "dnFieldName",   dnFieldName);
			rep.saveStepAttribute(id_transformation, id_step, "failIfNotExist",   failIfNotExist);
            rep.saveStepAttribute(id_transformation, id_step, "operationType", getOperationTypeCode(operationType));
			rep.saveStepAttribute(id_transformation, id_step, "multivaluedseparator", multiValuedSeparator);
			rep.saveStepAttribute(id_transformation, id_step, "searchBase", searchBase);
			rep.saveStepAttribute(id_transformation, id_step, "referralType", getReferralTypeCode(referralType));
			rep.saveStepAttribute(id_transformation, id_step, "derefAliasesType", getDerefAliasesCode(derefAliasesType));
			
			for (int i=0;i<updateLookup.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",    updateLookup[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "field_attribut",  updateStream[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "value_update",  update[i].booleanValue());

			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "LDAPOutputMeta.Exception.ErrorSavingToRepository", ""+id_step), e);
		}
	}
	

	
	
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
	
		CheckResult cr;

		
		// See if we get input...
		if (input.length>0)
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "LDAPOutputMeta.CheckResult.NoInputExpected"), stepMeta);
		else
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "LDAPOutputMeta.CheckResult.NoInput"), stepMeta);
		remarks.add(cr);
		
		// Check hostname
		if(Const.isEmpty(Host))
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "LDAPOutputMeta.CheckResult.HostnameMissing"), stepMeta);
		else
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "LDAPOutputMeta.CheckResult.HostnameOk"), stepMeta);
		remarks.add(cr);
		
		// check return fields
		if(updateLookup.length==0)
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "LDAPOutputUpdateMeta.CheckResult.NoFields"), stepMeta);
		else
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "LDAPOutputUpdateMeta.CheckResult.FieldsOk"), stepMeta);
		
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new LDAPOutput(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new LDAPOutputData();
	}
    public boolean supportsErrorHandling()
    {
        return true;
    } 

    public String toString()
    {
    	return "LDAPConnection " + getName();
    }
}