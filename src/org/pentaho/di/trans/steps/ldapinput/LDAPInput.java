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
 

package org.pentaho.di.trans.steps.ldapinput;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Read LDAP Host, convert them to rows and writes these to one or more output streams.
 * 
 * @author Samatar
 * @since 21-09-2007
 */
public class LDAPInput extends BaseStep implements StepInterface
{
	private LDAPInputMeta meta;
	private LDAPInputData data;

	
	public LDAPInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
    	
		String port=environmentSubstitute(meta.getPort());
		String hostname=environmentSubstitute(meta.getHost());
		String username=environmentSubstitute(meta.getUserName());
		String password=environmentSubstitute(meta.getPassword());
        //Set the filter string.  The more exact of the search string
		String filter=environmentSubstitute(meta.getFilterString());
		//Set the Search base.This is the place where the search will
		String searchbase=environmentSubstitute(meta.getSearchBase());
	
		
		NamingEnumeration<SearchResult> results=null;
		DirContext ctx = null;
		data.rownr=0;
		linesInput=0;
		
	    String [] attrReturned = new String [meta.getInputFields().length];
	    Object[] outputRowData = null;
        
        if (first)
        {
            first = false;
            // Create the output row meta-data
            data.outputRowMeta = new RowMeta();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this); // get the metadata populated
            
            
            // Create convert meta-data objects that will contain Date & Number formatters
            //
            data.convertRowMeta = data.outputRowMeta.clone();
            for (int i=0;i<data.convertRowMeta.size();i++) data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);

            
            
            // For String to <type> conversions, we allocate a conversion meta data row as well...
			//
			data.convertRowMeta = data.outputRowMeta.clone();
			for (int i=0;i<data.convertRowMeta.size();i++) {
				data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);           
            
			}
			
		   // Get user selection attributes
		   for (int i=0;i<meta.getInputFields().length;i++)
		   {
		    	attrReturned[i]=environmentSubstitute(meta.getInputFields()[i].getAttribute());
			
		   }
        }
	
		try 
		{	

			// Try to connect to LDAP server
             ctx = connectServerLdap(hostname,username, password,port);
		     if (ctx==null)
		     {
		    	 logError(Messages.getString("LDAPInput.Error.UnableToConnectToServer"));
		     }
		     
		     logBasic(Messages.getString("LDAPInput.ConnectedToServer.Message",hostname,username));
		     if (log.isDetailed()) logDetailed(Messages.getString("LDAPInput.ClassUsed.Message",ctx.getClass().getName()));
		     // Get the schema tree root
		     DirContext schema = ctx.getSchema("");
		     
		     if (log.isDetailed()) logDetailed(Messages.getString("LDAPInput.SchemaList.Message",""+schema.list("")));
		     
 		     SearchControls controls = new SearchControls();
		     controls.setCountLimit(meta.getRowLimit());
		     //controls.setTimeLimit(0);
		    
		     // Limit returned attributes to user selection
		     controls.setReturningAttributes(attrReturned);
		     
		     
		     if(Const.isEmpty(searchbase))
		     {
			     // get Search Base
			     Attributes attrs = ctx.getAttributes("", new String[] { "namingContexts" });
				 Attribute attr = attrs.get("namingContexts");
				  
				 searchbase=attr.get().toString();
				 if (log.isDetailed()) logBasic(Messages.getString("LDAPInput.SearchBaseFound",searchbase) );
		     } 
		
	         controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	         
	         results = ctx.search(searchbase,filter.replace("\n\r", "").replace("\n", ""), controls);
	        
	         // Get value for attributes

	         Attribute attr=null; 
	         String attrvalue=null;
	       
		       while (((meta.getRowLimit()>0 &&  data.rownr<meta.getRowLimit()) || meta.getRowLimit()==0)  && (results.hasMore()))  
				{
		    	    
	                SearchResult searchResult = (SearchResult) results.next();
	                Attributes attributes = searchResult.getAttributes();      
	                
					// Create new row				
					outputRowData = buildEmptyRow();
							
					// Execute for each Input field...
					for (int i=0;i<meta.getInputFields().length;i++)
					{
						// Get attribute value
		            	attr = attributes.get(environmentSubstitute(meta.getInputFields()[i].getAttribute())); 
		                if (attr!=null) attrvalue=(String) attr.get();
		               
						// DO Trimming!
						switch (meta.getInputFields()[i].getTrimType())
						{
						case LDAPInputField.TYPE_TRIM_LEFT:
							attrvalue = Const.ltrim(attrvalue);
							break;
						case LDAPInputField.TYPE_TRIM_RIGHT:
							attrvalue = Const.rtrim(attrvalue);
							break;
						case LDAPInputField.TYPE_TRIM_BOTH:
							attrvalue = Const.trim(attrvalue);
							break;
						default:
							break;
						}
							      
						
						// DO CONVERSIONS...
						//
						ValueMetaInterface targetValueMeta = data.outputRowMeta.getValueMeta(i);
						ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta(i);
						outputRowData[i] = targetValueMeta.convertData(sourceValueMeta, attrvalue);
						
						// Do we need to repeat this field if it is null?
						if (meta.getInputFields()[i].isRepeated())
						{
							if (data.previousRow!=null && Const.isEmpty(attrvalue))
							{
								outputRowData[i] = data.previousRow[i];
							}
						}
			            
			    
			 			
					}    // End of loop over fields...
	                    
					int rowIndex = meta.getInputFields().length;
				
					
			        // See if we need to add the row number to the row...  
			        if (meta.includeRowNumber() && !Const.isEmpty(meta.getRowNumberField()))
			        {
			            outputRowData[rowIndex++] = new Long(data.rownr);
			        }
			        
					
					RowMetaInterface irow = getInputRowMeta();
					
					data.previousRow = irow==null?outputRowData:(Object[])irow.cloneRow(outputRowData); // copy it to make
					// surely the next step doesn't change it in between...
					data.rownr++;
					linesInput++;
		           
					putRow(data.outputRowMeta, outputRowData);  // copy row to output rowset(s);
	            }
		        
		     
			    ctx.close();
			    log.logBasic(Messages.getString("LDAPInput.log.Deconnection"),Messages.getString("LDAPInput.log.Deconnection.Done"));
			     
			    if(log.isDebug()) log.logDebug(Messages.getString("LDAPInput.log.ReadRow"), outputRowData.toString());
		
			    if ((linesInput > 0) && (linesInput % Const.ROWS_UPDATE) == 0) logBasic(Messages.getString("LDAPInput.log.LineRow") + linesInput);
			     
		} 
		catch(Exception e)
		{
			logError(Messages.getString("LDAPInput.log.Exception"), e);
			stopAll();
			setErrors(1);
		} 
		finally
	    {
			
            if (results != null) {
                try {
                    results.close();
                } catch (Exception e) {
                }
            }
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (Exception e) {
                }
            }
	    }
	
			

        
        setOutputDone();  // signal end to receiver(s)
        return false;     // This is the end of this step.
	}		
	
	 public InitialDirContext connectServerLdap(String hostname,String username, String password,String port) throws NamingException {
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
		 
	        Hashtable<String, String> env = new Hashtable<String, String>();

	        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	        env.put(Context.PROVIDER_URL, "ldap://"+hostname + ":" + Const.toInt(port, 389));
	        env.put(Context.SECURITY_AUTHENTICATION, "simple" );
	        if (meta.UseAuthentication())
	        {
	        	env.put(Context.SECURITY_PRINCIPAL, username);
	        	env.put(Context.SECURITY_CREDENTIALS, password); 
	        }

	        return new InitialDirContext(env);
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

		super.dispose(smi, sdi);
	}
	
	//
	// Run is were the action happens!
	//
	public void run()
	{			    
		try
		{
			logBasic(Messages.getString("LDAPInput.Log.StartingRun"));		
			
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError("Unexpected error : ");
			logError(Const.getStackTracker(e));
			setErrors(1);
			stopAll();
		}
		finally
		{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}
}