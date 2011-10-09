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


package org.pentaho.di.trans.steps.wmiinput;

import java.util.logging.Level;

import org.jinterop.dcom.common.JISystem;
import org.jinterop.dcom.core.IJIComObject;
import org.jinterop.dcom.core.JIArray;
import org.jinterop.dcom.core.JIComServer;
import org.jinterop.dcom.core.JISession;
import org.jinterop.dcom.core.JIString;
import org.jinterop.dcom.core.JIVariant;
import org.jinterop.dcom.impls.automation.IJIDispatch;
import org.jinterop.dcom.impls.automation.IJIEnumVariant;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;

import static org.jinterop.dcom.core.JIProgId.valueOf;
import static org.jinterop.dcom.impls.automation.IJIDispatch.IID;
import static org.jinterop.dcom.impls.JIObjectFactory.narrowObject;


/**
 * @author Samatar
 * @since 01-10-2011
 */


public class WMIQuery {
	private static Class<?> PKG = WMIInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	/** values provided by client **/
	private String domain;
	private String userName;
	private String password;
	private String hostName;
	private int rowLimit;
	private LogChannelInterface log;
	
	
	/** internal variables **/
	protected JISession session;
	protected JIComServer comServer;
	protected IJIDispatch wbemLocator;
	protected IJIDispatch wbemServices;
	protected IJIEnumVariant enumVARIANT;
	
	/** variable that stores total rows count returned for the WMQ **/
	protected int rowsCount;
	/** current row processed **/
	protected int rowProcessed;
	

	
	public WMIQuery(LogChannelInterface logChannel) throws KettleException {
		this(logChannel, "ECTE", "localhost", "shassan", "Egencia2015");
		
	}
	
	/**
	 * Define a new WMIQuery.
	 *
	 * @param logChannel
	 * @param domain
	 * @param hostName
	 * @param user
	 * @param password
	 * 
	 * @throws KettleException
	 */
	public WMIQuery(LogChannelInterface logChannel, String domain, String hostName, String user, String password) 
	throws KettleException  {
		this.log=logChannel;
		this.domain=domain;
		this.hostName= hostName;
		this.userName=user;
		this.password=password;
		
		this.rowsCount=0;
		this.rowProcessed=0;
		this.rowLimit=0;
		
	}
	
	/**
	 * Create a new session.
	 *
	 * @throws KettleException
	 */
	public void connect() throws KettleException {
		try {

			JISystem.getLogger().setLevel(Level.OFF);
			JISystem.setAutoRegisteration(true);
			if(Const.isEmpty(getHost())) {
				this.session = JISession.createSession();
			}else {
				this.session = JISession.createSession(getDomain(), getUserName(), this.password);
			}
			this.session.useSessionSecurity(true);
			
			if(log.isDebug()) {
				log.logDebug(BaseMessages.getString(PKG, "WMIQuery.SessionCreated", this.session.getSessionIdentifier()));
			}
			this.comServer = new JIComServer(valueOf("WbemScripting.SWbemLocator"), this.hostName , this.session);
			this.wbemLocator = (IJIDispatch) narrowObject(this.comServer.createInstance().queryInterface(IID));
			
		}catch(Exception e) {
			throw new KettleException(e);
		}
	}
	
	/**
	 * Execute a WMQ statement
	 * and returns the total returned rows count
	 * use getNextRow to get next row
	 * 
	 * @param queryString : WMQ query to run
	 * @return total rows count
	 * @throws KettleException
	 */
	public int openQuery(String queryString) throws KettleException {
		return openQuery(queryString, false); 
	}
	
	/**
	 * Returns Row meta for a WMQ statement
	 * 
	 * @param queryString : WMQ query to run
	 * @return Row meta
	 * @throws KettleException
	 */
	public RowMetaInterface getRowMeta(String queryString) throws KettleException {
		
		
		try {
            RowMetaInterface retval= new RowMeta();
            
			// run WMQ
			openQuery(queryString, true); 
			
			// get first row
			
	        Object[] values = enumVARIANT.next(1);
	        JIArray array = (JIArray)values[0];
	        Object[] arrayObj = (Object[])array.getArrayInstance();
	       
            IJIDispatch wbemObject_dispatch = (IJIDispatch) narrowObject(((JIVariant)arrayObj[0]).getObjectAsComObject());
            JIVariant variant2 = (JIVariant)(wbemObject_dispatch.callMethodA("GetObjectText_",new Object[]{new Integer(1)}))[0];
       
            String props[] = variant2.getObjectAsString().getString().split("[\n\r(\r\n)\\n]");                  
            
            for (String s : props) { 
	  			if(s.indexOf("=")>0) {
	  				String[] vars=s.split("=");
	  				ValueMetaInterface v = new ValueMeta(vars[0].trim(), ValueMeta.TYPE_STRING);
	
	  				retval.addValueMeta(v);
	  			}
            }
            return retval;
		}catch(Exception e) {
			throw new KettleException(e);
		}
	}
	
	/**
	 * Execute a WMQ statement
	 * and returns the total returned rows count
	 * use getNextRow to get next row
	 * 
	 * @param queryString : WMQ query to run
	 * @param onlyMeta : turn to TRUE in order to retrieve only meta
	 * 					and ignore data
	 * @return total rows count
	 * @throws KettleException
	 */
	private int openQuery(String queryString, boolean onlyMeta) throws KettleException {
		
		try {

			Object[] params = new Object[] { 
					new JIString(this.hostName), 
					new JIString("ROOT\\CIMV2"),
					JIVariant.OPTIONAL_PARAM(), 
					JIVariant.OPTIONAL_PARAM(), 
					JIVariant.OPTIONAL_PARAM(),
					JIVariant.OPTIONAL_PARAM(), 
					new Integer(0), 
					JIVariant.OPTIONAL_PARAM() 
			};
			JIVariant results[] = wbemLocator.callMethodA("ConnectServer", params);
			wbemServices = (IJIDispatch) narrowObject(results[0].getObjectAsComObject());
			
			results = wbemServices.callMethodA("ExecQuery", new Object[]{new JIString(queryString), JIVariant.OPTIONAL_PARAM(), JIVariant.OPTIONAL_PARAM(),JIVariant.OPTIONAL_PARAM()});
			IJIDispatch wbemObjectSet_dispatch = (IJIDispatch) narrowObject((results[0]).getObjectAsComObject());
			JIVariant variant = wbemObjectSet_dispatch.get("_NewEnum");
			IJIComObject object2 = variant.getObjectAsComObject();

			enumVARIANT = (IJIEnumVariant) narrowObject(object2.queryInterface(IJIEnumVariant.IID));
			
			if(!onlyMeta) {
				JIVariant Count = wbemObjectSet_dispatch.get("Count");
				this.rowsCount = Count.getObjectAsInt();
				if(this.rowLimit>0 && this.rowsCount> this.rowLimit) {
					this.rowsCount=this.rowLimit;
				}
				
				if(log.isDetailed()) {
					log.logDetailed(BaseMessages.getString(PKG, "WMIQuery.RowsCount", String.valueOf(this.rowsCount)));
				}
			}
			
			return this.rowsCount;
			
		}catch(Exception e) {
			throw new KettleException (e);
		}
	}
	
	
	/**
	 * Returns next row data
	 * returns null where there is no more row
	 * use getNextRow to get next row
	 * 
	 * @return next row
	 * @throws KettleException
	 */
	public Object[] getNextRow() throws KettleException {
		
		if(this.rowsCount==0) return null;
		
		this.rowProcessed++;
		if(this.rowProcessed>=this.rowsCount) return null;
		
		try {
			
	        Object[] values = enumVARIANT.next(1);
	        JIArray array = (JIArray)values[0];
	        Object[] arrayObj = (Object[])array.getArrayInstance();
	       
            IJIDispatch wbemObject_dispatch = (IJIDispatch) narrowObject(((JIVariant)arrayObj[0]).getObjectAsComObject());
            JIVariant variant2 = (JIVariant)(wbemObject_dispatch.callMethodA("GetObjectText_",new Object[]{new Integer(1)}))[0];               
          
            return  getRowData(variant2.getObjectAsString().getString());

		}catch(Exception e) {
			throw new KettleException(e);
		}
	}
	
	
	/**
	 * Returns row data
	 * extract property name and value
	 * @param objectAsString : WMQ statement to run
	 * 
	 * @return row
	 */
	private Object[] getRowData(String objectAsString) {
	      
		
        String props[] = objectAsString.split("[\n\r(\r\n)\\n]");                  
         
		Object[] retval = new Object[props.length];
		
        int index=0;
  		for (String s : props) { 
  			if(s.indexOf("=")>0) {
  				String[] vars=s.split("=");
  		
  				// get value
  		  		String value=vars[1].trim();
  		  		if(value.substring(value.length()-1, value.length()).equals(";")) {
  		  			value=value.substring(0, value.length()-1).trim();
  		  		}
  		  		if(value.substring(value.length()-1, value.length()).equals("\"")) {
		  			value=value.substring(0, value.length()-1).trim();
		  		}

	  		  	if(value.substring(0, 1).equals("\"")) {
		  			value=value.substring(1, value.length()-1).trim();
		  		}
		  		  	
		  		retval[index++]=value;	
  			}
  		}
  		return retval;
	}
	
	/**
	 * Close session.
	 *
	 * @throws KettleException
	 */
	public void close() throws KettleException {
		try {
			if (this.session != null) {
				try {
					JISession.destroySession(this.session);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}catch(Exception e) {
			throw new KettleException (e);
		}
	}

	
	
	/**
	 * Return domain name.
	 * 
	 * @return domain
	 */
	public String getDomain() {
		return this.domain;
	}
	
	
	/**
	 * Set domain.
	 * 
	 * @param domain name
	 */
	public void setDomain(String value) {
		this.domain=value;
	}
	

	/**
	 * Set host.
	 * 
	 * @param host name
	 */
	public void setHost(String value) {
		this.hostName=value;
	}
	
	
	/**
	 * Return host name.
	 * 
	 * @return hostname
	 */
	public String getHost() {
		return this.hostName;
	}
	
	
	
	/**
	 * Set User name.
	 * 
	 * @param username
	 */
	public void setUserName(String value) {
		this.userName=value;
	}
	
	/**
	 * Return user name.
	 * 
	 * @return username
	 */
	public String getUserName() {
		return this.userName;
	}
	
	/**
	 * Set password.
	 * 
	 * @param password
	 */
	public void setPassword(String value) {
		this.password=value;
	}
	
	
	/**
	 * Set query limit.
	 * 
	 * @param query limit (integer)
	 */
	public void setQueryLimit(int limit) {
		this.rowLimit= limit;
	}
}
