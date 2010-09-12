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

package org.pentaho.di.job.entries.webserviceavailable;

import java.util.List;
import org.w3c.dom.Node;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;


import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * This defines a webservice available job entry.
 * 
 * @author Samatar
 * @since 05-11-2009
 *
 */

public class JobEntryWebServiceAvailable extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private static Class<?> PKG = JobEntryWebServiceAvailable.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String url;
	private String connectTimeOut;
	private String readTimeOut;
	
	
	public JobEntryWebServiceAvailable(String n)
	{
		super(n, "");
		url=null;
		connectTimeOut="0";
		readTimeOut="0";
		setID(-1L);
	}

	public JobEntryWebServiceAvailable()
	{
		this("");
	}

    public Object clone()
    {
        JobEntryWebServiceAvailable je = (JobEntryWebServiceAvailable) super.clone();
        return je;
    }
    
	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
		retval.append(super.getXML());		
		retval.append("      ").append(XMLHandler.addTagValue("url",   url));
		retval.append("      ").append(XMLHandler.addTagValue("connectTimeOut",   connectTimeOut));
		retval.append("      ").append(XMLHandler.addTagValue("readTimeOut",   readTimeOut));
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep)
	throws KettleXMLException
	{
	try
	{
		super.loadXML(entrynode, databases, slaveServers);	
			url      = XMLHandler.getTagValue(entrynode, "url");
			connectTimeOut      = XMLHandler.getTagValue(entrynode, "connectTimeOut");
			readTimeOut      = XMLHandler.getTagValue(entrynode, "readTimeOut");
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "JobEntryWebServiceAvailable.ERROR_0001_Cannot_Load_Job_Entry_From_Xml_Node"), xe);
		}
	}

	public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	{
		try
		{
			url = rep.getJobEntryAttributeString(id_jobentry, "url");
			connectTimeOut = rep.getJobEntryAttributeString(id_jobentry, "connectTimeOut");
			readTimeOut = rep.getJobEntryAttributeString(id_jobentry, "readTimeOut");
		}
		catch(KettleException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "JobEntryWebServiceAvailable.ERROR_0002_Cannot_Load_Job_From_Repository",""+id_jobentry), dbe);
		}
	}
	
	public void saveRep(Repository rep, ObjectId id_job) throws KettleException
	{
		try
		{
			rep.saveJobEntryAttribute(id_job, getObjectId(), "url", url);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "connectTimeOut", connectTimeOut);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "readTimeOut", readTimeOut);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "JobEntryWebServiceAvailable.ERROR_0003_Cannot_Save_Job_Entry",""+id_job), dbe);
		}
	}

	public void setURL(String url)
	{
		this.url = url;
	}
	
	public String getURL()
	{
		return url;
	}
	public void setConnectTimeOut(String timeout)
	{
		this.connectTimeOut = timeout;
	}
	
	public String getConnectTimeOut()
	{
		return connectTimeOut;
	}
	public void setReadTimeOut(String timeout)
	{
		this.readTimeOut = timeout;
	}
	
	public String getReadTimeOut()
	{
		return readTimeOut;
	}
    public Result execute(Result previousResult, int nr)
	{
		Result result = previousResult;
		result.setResult( false );

        String realURL = environmentSubstitute(getURL());
        
		if (!Const.isEmpty(realURL)){
			int connectTimeOut = Const.toInt(environmentSubstitute(getConnectTimeOut()),0);
			int readTimeOut = Const.toInt(environmentSubstitute(getReadTimeOut()),0);
            InputStream in=null;
            try {
            	
                URLConnection conn = new URL(realURL).openConnection();   
                conn.setConnectTimeout(connectTimeOut);   
                conn.setReadTimeout(readTimeOut);   
                in = conn.getInputStream();
                // Web service is available
                result.setResult(true);
            } catch (Exception e) {
                result.setNrErrors(1);
                String message = BaseMessages.getString(PKG, "JobEntryWebServiceAvailable.ERROR_0004_Exception", realURL, e.toString());
                logError( message); //$NON-NLS-1$
                result.setLogText(message);
            }finally {
            	if(in!=null)
            	{
            		try{
            			in.close();
            		}catch(Exception e){};
            	}
            }
		}else{
			result.setNrErrors(1);
			String message = BaseMessages.getString(PKG, "JobEntryWebServiceAvailable.ERROR_0005_No_URL_Defined");
			logError( message); //$NON-NLS-1$
			result.setLogText(message);
		}

		return result;
	}    

	public boolean evaluates()
	{
		return true;
	}
    
}
