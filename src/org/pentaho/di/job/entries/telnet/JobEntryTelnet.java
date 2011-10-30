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

package org.pentaho.di.job.entries.telnet;

import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import org.w3c.dom.Node;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;


/**
 * This defines a Telnet job entry.
 * 
 * @author Samatar
 * @since 05-11-2003
 *
 */

public class JobEntryTelnet extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private static Class<?> PKG = JobEntryTelnet.class; // for i18n
      
	private String hostname;
	private String port;
	private String timeout;
	
	public static final int DEFAULT_TIME_OUT= 3000;
	public static final int DEFAULT_PORT = 23;


	public JobEntryTelnet(String n)
	{
		super(n, "");
		hostname=null;
		port=String.valueOf(DEFAULT_PORT);
		timeout=String.valueOf(DEFAULT_TIME_OUT);
		setID(-1L);
	}

	public JobEntryTelnet()
	{
		this("");
	}


    public Object clone()
    {
        JobEntryTelnet je = (JobEntryTelnet) super.clone();
        return je;
    }

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(100);

		retval.append(super.getXML());
		retval.append("      ").append(XMLHandler.addTagValue("hostname",    hostname));
		retval.append("      ").append(XMLHandler.addTagValue("port", port));
		retval.append("      ").append(XMLHandler.addTagValue("timeout",   timeout));

		return retval.toString();
	}


	  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException {
	    try {
	      super.loadXML(entrynode, databases, slaveServers);
			hostname   = XMLHandler.getTagValue(entrynode, "hostname");
			port = XMLHandler.getTagValue(entrynode, "port");
			timeout     = XMLHandler.getTagValue(entrynode, "timeout");
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'Telnet' from XML node", xe);
		}
	}

	  public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException {
		 try {
			hostname   = rep.getJobEntryAttributeString(id_jobentry, "hostname");
			port = rep.getJobEntryAttributeString(id_jobentry, "port");
			timeout = rep.getJobEntryAttributeString(id_jobentry, "timeout");
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'Telnet' exists from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}

	  public void saveRep(Repository rep, ObjectId id_job) throws KettleException {
		try {
			rep.saveJobEntryAttribute(id_job, getObjectId(), "hostname",    hostname);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "port", port);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'Telnet' to the repository for id_job="+id_job, dbe);
		}
	}
	public String getPort()
	{
		return port;
	}

	public String getRealPort()
	{
		return environmentSubstitute(getPort());
	}

	public void setPort(String port)
	{
		this.port = port;
	}
	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}

	public String getHostname()
	{
		return hostname;
	}

    public String getRealHostname()
    {
        return environmentSubstitute(getHostname());
    }

	public String getTimeOut()
	{
		return timeout;
	}

	public String getRealTimeOut()
	{
		return environmentSubstitute(getTimeOut());
	}

	public void setTimeOut(String timeout)
	{
		this.timeout = timeout;
	}

  public Result execute(Result previousResult, int nr) {

        Result result = previousResult;
        
        result.setNrErrors(1);
        result.setResult(false);

        String hostname = getRealHostname();
        int port = Const.toInt(getRealPort(), DEFAULT_PORT);
    	int timeoutInt = Const.toInt(getRealTimeOut(), -1);
        
        if (Const.isEmpty(hostname)){
            // No Host was specified
            logError(BaseMessages.getString(PKG, "JobTelnet.SpecifyHost.Label"));
            return result;
        }

        try {
   
        	telnetHost(hostname, port, timeoutInt);
        	
         	if(isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobTelnet.OK.Label",hostname, port));
         	
            result.setNrErrors(0);
            result.setResult(true);
            
        } catch (Exception ex){
        	logError(BaseMessages.getString(PKG, "JobTelnet.NOK.Label",hostname, String.valueOf(port)));
            logError(BaseMessages.getString(PKG, "JobTelnet.Error.Label") + ex.getMessage());
        }
        
        	
        return result;
    }

	public boolean evaluates()
	{
		return true;
	}
	
	private static void telnetHost(String host, int port, int timeout) throws KettleException {
		Socket socket = new Socket();
		try {
    		InetSocketAddress is = new InetSocketAddress(host, port);
    		if(timeout<0) socket.connect(is);
    		else socket.connect(is,timeout);
		}catch(Exception e){
			throw new KettleException(e);
		}finally {
    		try {
    			socket.close();
    		}catch(Exception e){};	
		}
	}
	  public List<ResourceReference> getResourceDependencies(JobMeta jobMeta) {
		    List<ResourceReference> references = super.getResourceDependencies(jobMeta);
		    if (!Const.isEmpty(hostname)) {
		      String realServername = jobMeta.environmentSubstitute(hostname);
		      ResourceReference reference = new ResourceReference(this);
		      reference.getEntries().add( new ResourceEntry(realServername, ResourceType.SERVER));
		      references.add(reference);
		    }
		    return references;
	  }

	  @Override
	  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
	  {
	    andValidator().validate(this, "hostname", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
	  }
}