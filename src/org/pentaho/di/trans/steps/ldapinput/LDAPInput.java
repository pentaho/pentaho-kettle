/*************************************************************************************** 
 * Copyright (C) 2007 Samatar, Brahim.  All rights reserved. 
 * This software was developed by Samatar, Brahim and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar, Brahim.  
 * The Initial Developer is Samatar, Brahim.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/
 

package org.pentaho.di.trans.steps.ldapinput;

import java.util.HashSet;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.core.encryption.Encr;
/**
 * Read LDAP Host, convert them to rows and writes these to one or more output streams.
 * 
 * @author Samatar
 * @since 21-09-2007
 */
public class LDAPInput extends BaseStep implements StepInterface
{
	private static Class<?> PKG = LDAPInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private LDAPInputMeta meta;
	private LDAPInputData data;

	
	public LDAPInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
        if (first)
        {
            first = false;
            // Create the output row meta-data
            data.outputRowMeta = new RowMeta();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this); // get the metadata populated
            
            // Create convert meta-data objects that will contain Date & Number formatters
            //

			data.convertRowMeta = data.outputRowMeta.clone();
			for (int i=0;i<data.convertRowMeta.size();i++) {
					data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);           
			}
   
			// Try to connect to LDAP server
            connectServerLdap();
           
	        // Get multi valued field separator
	        data.multi_valuedFieldSeparator=environmentSubstitute(meta.getMultiValuedSeparator()) ;
	        
	        data.nrfields = meta.getInputFields().length;
	 
        }
	
        Object[] outputRowData=null;

		try 
		{	
			outputRowData =getOneRow();
			
			if(outputRowData==null)
			{
				setOutputDone();
				return false;
			}
		
			putRow(data.outputRowMeta, outputRowData);  // copy row to output rowset(s);
		    
			if(log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "LDAPInput.log.ReadRow"), data.outputRowMeta.getString(outputRowData));
			
		    if (checkFeedback(getLinesInput()))
		    {
		    	if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "LDAPInput.log.LineRow") + getLinesInput());
		    }
		    
		    return true; 
		} 
		catch(Exception e)
		{
	        boolean sendToErrorRow=false;
			String errorMessage = null;
			if (getStepMeta().isDoingErrorHandling())
			{
		         sendToErrorRow = true;
		         errorMessage = e.toString();
			}
			else
			{
				logError(BaseMessages.getString(PKG, "LDAPInput.log.Exception", e.getMessage()));
				setErrors(1);
				logError(Const.getStackTracker(e));
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;				
			}
			if (sendToErrorRow)
			{
			   // Simply add this row to the error row
			   putError(getInputRowMeta(), outputRowData, 1, errorMessage, null, "LDAPINPUT001");
			}
		} 
		return true;
	}		
	private Object[] getOneRow()  throws KettleException
	{
		
		 while (!data.results.hasMoreElements())
		 {
			if(data.pagingSet)
			{
				// we are using paging...
				// we need here to check the response controls
				// and pass back cookie to next page
				try 
				{
					// examine response controls
					Control[] rc = data.ctx.getResponseControls();
					if (rc != null) 
					{
						for (int i = 0; i < rc.length; i++) 
						{
							if (rc[i] instanceof PagedResultsResponseControl) 
							{
								PagedResultsResponseControl prc = (PagedResultsResponseControl) rc[i];
								data.cookie = prc.getCookie();
							}
						}
					}
					// pass the cookie back for the next page
					data.ctx.setRequestControls(new javax.naming.ldap.Control[] 
					        { new PagedResultsControl(data.pageSize, data.cookie,Control.CRITICAL) });
						
					 if ((data.cookie != null) && (data.cookie.length != 0))
					 {
						 // get search result for the page
						 data.results = data.ctx.search(data.searchbase,data.filter, data.controls);
					 }else
					 {
						 return null;
					 }
				 
				}catch(Exception e)
				{
					throw new KettleException(BaseMessages.getString(PKG, "LDAPInput.Exception.ErrorPaging"), e);
				}
			 
				while (!data.results.hasMoreElements())
				{
				   return null;
				}
			}else
			{
				// User do not want to use paging
				// we have already returned all the result
				return null;
			}
		 }

		 // Build an empty row based on the meta-data		  
		 Object[] outputRowData=buildEmptyRow();

		 try{	
			 SearchResult searchResult = (SearchResult) data.results.next();
             Attributes attributes = searchResult.getAttributes();     	
             
             if(attributes!=null)
             {
				// Execute for each Input field...
				for (int i=0;i<meta.getInputFields().length;i++)
				{
					LDAPInputField field = meta.getInputFields()[i];
					// Get attribute value
					
					Attribute attr = attributes.get(field.getRealAttribute()); 
	                if (attr!=null) 
	                {
	                	//Let's try to get value of this attribute
	                	outputRowData[i]= getAttributeValue(field, attr, i, outputRowData[i]);		      
	                }
            
					// Do we need to repeat this field if it is null?
					if (field.isRepeated())
					{
						if (data.previousRow!=null && outputRowData[i]==null)
						{
							outputRowData[i] = data.previousRow[i];
						}
					}
	
				}    // End of loop over fields...

		        // See if we need to add the row number to the row...  
		        if (meta.includeRowNumber() && !Const.isEmpty(meta.getRowNumberField()))
		        {
		            outputRowData[ data.nrfields] = new Long(data.rownr);
		        }
		        
				RowMetaInterface irow = getInputRowMeta();
				
				data.previousRow = irow==null?outputRowData:(Object[])irow.cloneRow(outputRowData); // copy it to make
				// surely the next step doesn't change it in between...
				data.rownr++;
				
				incrementLinesInput();
             }         
			
		 }
		 catch (Exception e)
		 {
			throw new KettleException(BaseMessages.getString(PKG, "LDAPInput.Exception.CanNotReadLDAP"), e);
		 }
		return outputRowData;
	}
	private Object getAttributeValue(LDAPInputField field,Attribute attr, int i, Object outputRowData) throws Exception {

		if(field.getType() ==  ValueMetaInterface.TYPE_BINARY) {
			// It's a binary field
			// no need to convert, just return the value as it
			try  {
				return (byte[])attr.get();
			}catch(java.lang.ClassCastException e){
				return attr.get().toString().getBytes();
			}
		}
		
		String retval=null;
		if(field.getReturnType() == LDAPInputField.FETCH_ATTRIBUTE_AS_BINARY && field.getType() ==  ValueMetaInterface.TYPE_STRING) {
			// Convert byte[] to string
			if (field.isObjectSid()) {
				return extractBytesAndConvertToString(attr, true);
			}else {
				return extractBytesAndConvertToString(attr, false);
			}
		}
		
		// extract as string
		retval=extractString(attr); 	
		
		// DO Trimming!
		switch (field.getTrimType())
		{
			case LDAPInputField.TYPE_TRIM_LEFT:
				retval = Const.ltrim(retval);
			break;
			case LDAPInputField.TYPE_TRIM_RIGHT:
				retval = Const.rtrim(retval);
			break;
			case LDAPInputField.TYPE_TRIM_BOTH:
				retval = Const.trim(retval);
			break;
			default:
			break;
		}
		
		// DO CONVERSIONS...
		//
		ValueMetaInterface targetValueMeta = data.outputRowMeta.getValueMeta(i);
		ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta(i);
		return targetValueMeta.convertData(sourceValueMeta, retval);
		
	}
	private String extractString(Attribute attr) throws Exception {
    	StringBuilder attrStr = new StringBuilder();
		for (NamingEnumeration<?> eattr = attr.getAll() ; eattr.hasMore();) 
		{
			if (attrStr.length() > 0) {
				attrStr.append(data.multi_valuedFieldSeparator);
			}
			attrStr.append(eattr.next().toString());
		}
		return attrStr.toString(); 	
	}
	private String extractBytesAndConvertToString(Attribute attr, boolean isSID) throws Exception {
		byte[] b;
		try {
			b= (byte[])(byte[]) attr.get();
		}catch(Exception e) {
			// Get bytes from String
			b =  attr.get().toString().getBytes();
		}
		if(isSID) return getSIDAsString(b);
		else return byteToHexEncode( b);
	}

	
   /**
    *  Convert the SID into string format
    * @param SID
    * @return String representation of SID
    */
	private static String getSIDAsString(byte[] SID)
	{
		long version;
		long authority;
		long count;
		long rid;
		String strSID;
		strSID = "S";
		version = SID[0];
		strSID = strSID + "-" + Long.toString(version);
		authority = SID[4];
		for (int i = 0;i<4;i++) {
			authority <<= 8;
			authority += SID[4+i] & 0xFF;
		}
		strSID = strSID + "-" + Long.toString(authority);
		count = SID[2];
		count <<= 8;
		count += SID[1] & 0xFF;
		for (int j=0;j<count;j++) {
			rid = SID[11 + (j*4)] & 0xFF;
			for (int k=1;k<4;k++) {
				rid <<= 8;
				rid += SID[11-k + (j*4)] & 0xFF;
			}
			strSID = strSID + "-" + Long.toString(rid);
		}
		return strSID;
	}
	
	/**
	* Converts the GUID to a readable string format
	* @param inArr
	* @return the formatted GUID
	*/
	private static String byteToHexEncode ( byte [] inArr )
	{
		StringBuffer guid = new StringBuffer ();
		for ( int i = 0; i < inArr.length; i++ ) {
			StringBuffer dblByte = new StringBuffer ( Integer.toHexString ( inArr [ i ] & 0xff ) );
			if ( dblByte.length () == 1 ) {
				guid.append ( "0" );
			}
			guid.append ( dblByte );
		}
		return guid.toString ();
	}

	 public void connectServerLdap() throws KettleException {
		 //TODO : Add SSL Authentication
			/*
			//---
			//SSL
	
			// Dynamically set JSSE as a security provider
			Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
	
			// Dynamically set the property that JSSE uses to identify
			// the keystore that holds trusted root certificates
			String certifPath=System.getProperty("user.dir")+"\\certificats\\"+rb.getString("certificat").trim();
			System.setProperty("javax.net.ssl.trustStore", certifPath);*/
		 try
		 {
			String port=environmentSubstitute(meta.getPort());
			int portint=Const.toInt(port, 389);
			String hostname=environmentSubstitute(meta.getHost());
			String username=environmentSubstitute(meta.getUserName());
			String password=Encr.decryptPasswordOptionallyEncrypted(environmentSubstitute(meta.getPassword()));
	        //Set the filter string.  The more exact of the search string
			data.filter=environmentSubstitute(meta.getFilterString()).replace("\n\r", "").replace("\n", "");
			//Set the Search base.This is the place where the search will
			data.searchbase=environmentSubstitute(meta.getSearchBase());
			 
			 
	        Hashtable<String, String> env = new Hashtable<String, String>();

	        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	        if(hostname.indexOf("ldap://")>=0)
	        	env.put(Context.PROVIDER_URL,hostname + ":" + portint);
	        else
	        	env.put(Context.PROVIDER_URL, "ldap://"+hostname + ":" + portint);
	        env.put(Context.SECURITY_AUTHENTICATION, "simple" );
	        
			// Limit returned attributes to user selection
			String[] attrReturned = new String [meta.getInputFields().length];
			
			data.attributesBinary= new HashSet<String>();
			// Get user selection attributes
			for (int i=0;i<meta.getInputFields().length;i++) {
				LDAPInputField field = meta.getInputFields()[i];
				// get real attribute name
				String name =environmentSubstitute(field.getAttribute());
				field.setRealAttribute(name);
				
				//specify attributes to be returned in binary format
	        	if(field.getReturnType() == LDAPInputField.FETCH_ATTRIBUTE_AS_BINARY) {
	    	        if(!data.attributesBinary.contains(name)) {
		    	        env.put("java.naming.ldap.attributes.binary", name);
	    	        	data.attributesBinary.add(name);
	    	        }
	        	}

				attrReturned[i]=name;
			}

	       // TODO : Add referral handling
	       if (meta.UseAuthentication())
	       {
	        	env.put(Context.SECURITY_PRINCIPAL, username);
	        	env.put(Context.SECURITY_CREDENTIALS, password); 
	       }
	       env.put(Context.REFERRAL, "follow");
	       data.ctx=new InitialLdapContext(env, null);
	       if (data.ctx==null)
		   {
			   logError(BaseMessages.getString(PKG, "LDAPInput.Error.UnableToConnectToServer"));
			   throw new KettleException(BaseMessages.getString(PKG, "LDAPInput.Error.UnableToConnectToServer"));
		   }
	       if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "LDAPInput.Log.ConnectedToServer",hostname,username));
		   if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "LDAPInput.ClassUsed.Message",data.ctx.getClass().getName()));
		   // Get the schema tree root
		   //DirContext schema = data.ctx.getSchema("");  
		   //if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "LDAPInput.SchemaList.Message",""+schema.list(""))); 
	       
		   data.controls = new SearchControls();
		   data.controls.setCountLimit(meta.getRowLimit());
		    
		   // Time Limit
		   if(meta.getTimeLimit()>0)   data.controls.setTimeLimit(meta.getTimeLimit() * 1000);
		   
		   data.controls.setReturningAttributes(attrReturned);
		     
		     
		   if(Const.isEmpty(data.searchbase))
		   {
			    // get Search Base
			    Attributes attrs = data.ctx.getAttributes("", new String[] { "namingContexts" });
				Attribute attr = attrs.get("namingContexts");
				  
				data.searchbase=attr.get().toString();
				if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "LDAPInput.SearchBaseFound",data.searchbase) );
		    } 
		    //Specify the search scope
		   data.controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	         
	        //Set the page size?
	        if(meta.isPaging())
	        {
	        	data.pageSize=Const.toInt(environmentSubstitute(meta.getPageSize()),-1);
	        	if(data.pageSize>-1)
	        	{
	        		// paging is activated
	        		data.pagingSet=true;
	        		//Request the paged results control
	    			Control[] ctls = new Control[]{new PagedResultsControl(data.pageSize,true)};
	    			data.ctx.setRequestControls(ctls);

	        		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "LDAPInput.Log.PageSize",data.pageSize) );
	        	}
	        }
	        // Apply search base and return result
	        data.results = data.ctx.search(data.searchbase,data.filter, data.controls);
	        
		 }catch (Exception e)
		 {
			 throw new KettleException(BaseMessages.getString(PKG, "LDAPinput.Exception.ErrorConnecting", e.getMessage()));
		 }
	  }
	
	/**
	 * Build an empty row based on the meta-data...
	 * 
	 * @return
	 */

	private Object[] buildEmptyRow()
	{
        Object[] rowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());
 
		 return rowData;
	}


	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(LDAPInputMeta)smi;
		data=(LDAPInputData)sdi;
		
		if (super.init(smi, sdi))
		{
			data.rownr = 1L;
			
			return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(LDAPInputMeta)smi;
		data=(LDAPInputData)sdi;
		if(data.ctx!=null)
		{
			try
			{
				data.ctx.close();
				if(data.results!=null) data.results=null;
				if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "LDAPInput.log.Disconnection.Done"));
			}
			catch (Exception e)
			{
	             logError(BaseMessages.getString(PKG, "LDAPInput.Exception.ErrorDisconecting",e.toString()));
	             logError(Const.getStackTracker(e));
			}
		}
		data.attributesBinary=null;
		
		super.dispose(smi, sdi);
	}
		
}
