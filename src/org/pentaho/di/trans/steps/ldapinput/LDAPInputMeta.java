 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

/* 
 * 
 * Created on 4-apr-2003
 * 
 */

package org.pentaho.di.trans.steps.ldapinput;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class LDAPInputMeta extends BaseStepMeta implements StepMetaInterface
{	

	/** Flag indicating that we use authentication for connection */
	private boolean useAuthentication;

	/** Flag indicating that a row number field should be included in the output */
	private  boolean includeRowNumber;
	
	/** The name of the field in the output containing the row number*/
	private  String  rowNumberField;
	
	
	/** The maximum number or lines to read */
	private  long  rowLimit;

	/** The Host name*/
	private  String  Host;
	
	/** The User name*/
	private  String  UserName;
	
	/** The Password to use in LDAP authentication*/
	private String Password;
	
	/** The Port*/
	private  String  Port;
	
	/** The Filter string*/
	private  String  FilterString;
	
	/** The Search Base*/
	private  String  SearchBase;
	
	/** The fields to import... */
	private LDAPInputField inputFields[];
	
	private static final String YES = "Y";
	
    public final static String type_trim_code[] = { "none", "left", "right", "both" };
    
	public LDAPInputMeta()
	{
		super(); // allocate BaseStepMeta
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
	public void setUseAuthentication(boolean useauthenticationin)
	{
		this.useAuthentication=useauthenticationin;
	}
		
	/**
     * @return Returns the input fields.
     */
    public LDAPInputField[] getInputFields()
    {
        return inputFields;
    }
    
    /**
     * @param inputFields The input fields to set.
     */
    public void setInputFields(LDAPInputField[] inputFields)
    {
        this.inputFields = inputFields;
    }

    
    /**
     * @return Returns the includeRowNumber.
     */
    public boolean includeRowNumber()
    {
        return includeRowNumber;
    }
 
    
    /**
     * @param includeRowNumber The includeRowNumber to set.
     */
    public void setIncludeRowNumber(boolean includeRowNumber)
    {
        this.includeRowNumber = includeRowNumber;
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
    public void setHost(String hostin)
    {
    	this.Host=hostin;
    }
    
    
    /**
     * @return Returns the user name.
     */
    public String getUserName()
    {
    	return UserName;
    }
    
    /**
     * @param username The username to set.
     */
    public void setUserName(String usernamein)
    {
    	this.UserName=usernamein;
    }
    
    /**
     * @param password The password to set.
     */
    public void setPassword(String passwordin)
    {
    	this.Password=passwordin;
    }
    
    /**
     * @return Returns the password.
     */
    public String getPassword()
    {
    	return Password;
    }
    
  
    

    
    /**
     * @return Returns the Port.
     */
    public String getPort()
    {
    	return Port;
    }
    
    
    /**
     * @param port The port to set.
     */
    public void setBaseDn(String portin)
    {
    	this.Port=portin;
    }
    
    
    /**
     * @return Returns the filter string.
     */
    public String getFilterString()
    {
    	return FilterString;
    }
    
    /**
     * @param filter string The filter string to set.
     */
    public void setFilterString(String filterstringin)
    {
    	this.FilterString=filterstringin;
    }
    
    
    
    /**
     * @return Returns the search string.
     */
    public String getSearchBase()
    {
    	return SearchBase;
    }
    
    /**
     * @param Search Base The filter Search Base to set.
     */
    public void setSearchBase(String searchbasein)
    {
    	this.SearchBase=searchbasein;
    }
    
    
    
    /**
     * @return Returns the rowLimit.
     */
    public long getRowLimit()
    {
        return rowLimit;
    }
    
    /**
     * @param rowLimit The rowLimit to set.
     */
    public void setRowLimit(long rowLimit)
    {
        this.rowLimit = rowLimit;
    }

    /**
     * @return Returns the rowNumberField.
     */
    public String getRowNumberField()
    {
        return rowNumberField;
    }
    

    
    
    /**
     * @param rowNumberField The rowNumberField to set.
     */
    public void setRowNumberField(String rowNumberField)
    {
        this.rowNumberField = rowNumberField;
    }
    
    
 
      
    
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleXMLException
	{
    	readData(stepnode);
	}

	public Object clone()
	{
		LDAPInputMeta retval = (LDAPInputMeta)super.clone();
		
		int nrFields = inputFields.length;

		retval.allocate(nrFields);
		
		for (int i=0;i<nrFields;i++)
		{
            if (inputFields[i]!=null)
            {
                retval.inputFields[i] = (LDAPInputField)inputFields[i].clone();
            }
		}
		
		return retval;
	}
    
    public String getXML()
    {
        StringBuffer retval=new StringBuffer(500);
        
        
        retval.append("    ").append(XMLHandler.addTagValue("useauthentication",  useAuthentication));
        retval.append("    ").append(XMLHandler.addTagValue("rownum",          includeRowNumber));
        retval.append("    ").append(XMLHandler.addTagValue("rownum_field",    rowNumberField));
        retval.append("    ").append(XMLHandler.addTagValue("host",    Host));
        retval.append("    ").append(XMLHandler.addTagValue("username",    UserName));
        retval.append("    ").append(XMLHandler.addTagValue("password", Encr.encryptPasswordIfNotUsingVariables(Password)));

        retval.append("    ").append(XMLHandler.addTagValue("port",    Port));
        retval.append("    ").append(XMLHandler.addTagValue("filterstring",    FilterString));
        retval.append("    ").append(XMLHandler.addTagValue("searchbase",    SearchBase));
         
        /*
		 * Describe the fields to read
		 */
		retval.append("    <fields>").append(Const.CR);
		for (int i=0;i<inputFields.length;i++)
		{
			retval.append("      <field>").append(Const.CR);
			retval.append("        ").append(XMLHandler.addTagValue("name",      inputFields[i].getName()) );
			retval.append("        ").append(XMLHandler.addTagValue("attribute",      inputFields[i].getAttribute()));
			retval.append("        ").append(XMLHandler.addTagValue("type",      inputFields[i].getTypeDesc()) );
            retval.append("        ").append(XMLHandler.addTagValue("format", inputFields[i].getFormat()));
            retval.append("        ").append(XMLHandler.addTagValue("length",    inputFields[i].getLength()) );
            retval.append("        ").append(XMLHandler.addTagValue("precision", inputFields[i].getPrecision()));
            retval.append("        ").append(XMLHandler.addTagValue("currency", inputFields[i].getCurrencySymbol()));
            retval.append("        ").append(XMLHandler.addTagValue("decimal", inputFields[i].getDecimalSymbol()));
            retval.append("        ").append(XMLHandler.addTagValue("group", inputFields[i].getGroupSymbol()));
			retval.append("        ").append(XMLHandler.addTagValue("trim_type", inputFields[i].getTrimTypeCode() ) );
			retval.append("        ").append(XMLHandler.addTagValue("repeat",    inputFields[i].isRepeated()) );

			retval.append("      </field>").append(Const.CR);
		}
		retval.append("    </fields>").append(Const.CR);
        
        
        retval.append("    ").append(XMLHandler.addTagValue("limit", rowLimit));

        return retval.toString();
    }

	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{
			
			
			useAuthentication  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "useauthentication"));
			includeRowNumber  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "rownum"));
			rowNumberField    = XMLHandler.getTagValue(stepnode, "rownum_field");
			Host    = XMLHandler.getTagValue(stepnode, "host");
			UserName    = XMLHandler.getTagValue(stepnode, "username");
			setPassword(Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(stepnode, "password")));

			Port    = XMLHandler.getTagValue(stepnode, "port");
			FilterString    = XMLHandler.getTagValue(stepnode, "filterstring");
			SearchBase = XMLHandler.getTagValue(stepnode, "searchbase");
	
			Node fields     = XMLHandler.getSubNode(stepnode,  "fields");
			int nrFields    = XMLHandler.countNodes(fields,    "field");
	
			allocate(nrFields);
			

			for (int i=0;i<nrFields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
				inputFields[i] = new LDAPInputField();
				
				inputFields[i].setName( XMLHandler.getTagValue(fnode, "name") );
				inputFields[i].setAttribute(XMLHandler.getTagValue(fnode, "attribute") );
				inputFields[i].setType( ValueMeta.getType(XMLHandler.getTagValue(fnode, "type")) );
				inputFields[i].setLength( Const.toInt(XMLHandler.getTagValue(fnode, "length"), -1) );
				inputFields[i].setPrecision( Const.toInt(XMLHandler.getTagValue(fnode, "precision"), -1) );
				String srepeat      = XMLHandler.getTagValue(fnode, "repeat");
				inputFields[i].setTrimType( getTrimTypeByCode(XMLHandler.getTagValue(fnode, "trim_type")) );
				
				if (srepeat!=null) inputFields[i].setRepeated( YES.equalsIgnoreCase(srepeat) ); 
				else               inputFields[i].setRepeated( false );
				
				inputFields[i].setFormat(XMLHandler.getTagValue(fnode, "format"));
				inputFields[i].setCurrencySymbol(XMLHandler.getTagValue(fnode, "currency"));
				inputFields[i].setDecimalSymbol(XMLHandler.getTagValue(fnode, "decimal"));
				inputFields[i].setGroupSymbol(XMLHandler.getTagValue(fnode, "group"));

			}
			
			// Is there a limit on the number of rows we process?
			rowLimit = Const.toLong(XMLHandler.getTagValue(stepnode, "limit"), 0L);
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("LDAPInputMeta.UnableToLoadFromXML"), e);
		}
	}
	
	public void allocate(int nrfields)
	{

		inputFields = new LDAPInputField[nrfields];        
	}
	
	public void setDefault()
	{

		useAuthentication=false;
		includeRowNumber = false;
		rowNumberField   = "";
		Host="";
		UserName="";
		Password="";
		Port="389";
		FilterString="objectclass=*";
		SearchBase="";
		

		int nrFields =0;

		allocate(nrFields);	
	
		for (int i=0;i<nrFields;i++)
		{
		    inputFields[i] = new LDAPInputField("field"+(i+1));
		}

		rowLimit=0;
	}
	public void getFields(RowMetaInterface r, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		
		int i;
		for (i=0;i<inputFields.length;i++)
		{
		    LDAPInputField field = inputFields[i];       
	        
			int type=field.getType();
			if (type==ValueMeta.TYPE_NONE) type=ValueMeta.TYPE_STRING;
			ValueMetaInterface v=new ValueMeta(space.environmentSubstitute(field.getName()), type);
			v.setLength(field.getLength(), field.getPrecision());
			v.setOrigin(name);
			r.addValueMeta(v);
	        
		}
		
		
			
		
		if (includeRowNumber)
		{
			ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(rowNumberField), ValueMeta.TYPE_INTEGER);
			v.setLength(ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0);
			v.setOrigin(name);
			r.addValueMeta(v);
		}
	}
	
	  public final static int getTrimTypeByCode(String tt)
		{
			if (tt!=null) 
			{		
			    for (int i=0;i<type_trim_code.length;i++)
			    {
				    if (type_trim_code[i].equalsIgnoreCase(tt)) return i;
			    }
			}
			return 0;
		}
	
	
	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException
	{
	
		try
		{
			
			useAuthentication  = rep.getStepAttributeBoolean(id_step, "useauthentication");
			includeRowNumber  = rep.getStepAttributeBoolean(id_step, "rownum");
			rowNumberField    = rep.getStepAttributeString (id_step, "rownum_field");
			Host    = rep.getStepAttributeString (id_step, "host");
			UserName    = rep.getStepAttributeString (id_step, "username");
			Password = Encr.decryptPasswordOptionallyEncrypted(rep.getJobEntryAttributeString(id_step, "password"));

			Port    = rep.getStepAttributeString (id_step, "port");
			FilterString    = rep.getStepAttributeString (id_step, "filterstring");
			SearchBase    = rep.getStepAttributeString (id_step, "searchbase");
			
			rowLimit          = rep.getStepAttributeInteger(id_step, "limit");
	
			int nrFields      = rep.countNrStepAttributes(id_step, "field_name");
            
			allocate(nrFields);

		
			for (int i=0;i<nrFields;i++)
			{
			    LDAPInputField field = new LDAPInputField();
			    
				field.setName( rep.getStepAttributeString (id_step, i, "field_name") );
				field.setAttribute( rep.getStepAttributeString (id_step, i, "field_attribute") );
				field.setType(ValueMeta.getType( rep.getStepAttributeString (id_step, i, "field_type") ) );
				field.setFormat( rep.getStepAttributeString (id_step, i, "field_format") );
				field.setCurrencySymbol( rep.getStepAttributeString (id_step, i, "field_currency") );
				field.setDecimalSymbol( rep.getStepAttributeString (id_step, i, "field_decimal") );
				field.setGroupSymbol( rep.getStepAttributeString (id_step, i, "field_group") );
				field.setLength( (int)rep.getStepAttributeInteger(id_step, i, "field_length") );
				field.setPrecision( (int)rep.getStepAttributeInteger(id_step, i, "field_precision") );
				field.setTrimType( LDAPInputField.getTrimTypeByCode( rep.getStepAttributeString (id_step, i, "field_trim_type") ));
				field.setRepeated( rep.getStepAttributeBoolean(id_step, i, "field_repeat") );

				inputFields[i] = field;
			}
        }
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("LDAPInputMeta.Exception.ErrorReadingRepository"), e);
		}
	}
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			
			rep.saveStepAttribute(id_transformation, id_step, "useauthentication",  useAuthentication);
			rep.saveStepAttribute(id_transformation, id_step, "rownum",          includeRowNumber);
			rep.saveStepAttribute(id_transformation, id_step, "rownum_field",    rowNumberField);
			rep.saveStepAttribute(id_transformation, id_step, "host",    Host);
			rep.saveStepAttribute(id_transformation, id_step, "username", UserName);
			rep.saveJobEntryAttribute(id_transformation, id_step, "password", Encr.encryptPasswordIfNotUsingVariables(Password));

			rep.saveStepAttribute(id_transformation, id_step, "port",   Port);
			rep.saveStepAttribute(id_transformation, id_step, "filterstring",   FilterString);
			rep.saveStepAttribute(id_transformation, id_step, "searchbase",  SearchBase);
			rep.saveStepAttribute(id_transformation, id_step, "limit",           rowLimit);

			for (int i=0;i<inputFields.length;i++)
			{
			    LDAPInputField field = inputFields[i];
			    
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",          field.getName());
				rep.saveStepAttribute(id_transformation, id_step, i, "fied_attribute",       field.getAttribute());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_type",          field.getTypeDesc());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_format",        field.getFormat());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_currency",      field.getCurrencySymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_decimal",       field.getDecimalSymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_group",         field.getGroupSymbol());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_length",        field.getLength());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_precision",     field.getPrecision());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_trim_type",     field.getTrimTypeCode());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_repeat",        field.isRepeated());

			}
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("LDAPInputMeta.Exception.ErrorSavingToRepository", ""+id_step), e);
		}
	}
	

	
	
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
	
		CheckResult cr;

		// See if we get input...
		if (input.length>0)
		{		
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("LDAPInputMeta.CheckResult.NoInputExpected"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("LDAPInputMeta.CheckResult.NoInput"), stepMeta);
			remarks.add(cr);
		}
		
      
		
	
		
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new LDAPInput(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new LDAPInputData();
	}

	
}