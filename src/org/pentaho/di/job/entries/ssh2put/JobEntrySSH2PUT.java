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

package org.pentaho.di.job.entries.ssh2put;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.integerValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.di.core.vfs.KettleVFS;
import org.apache.commons.vfs.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.w3c.dom.Node;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SFTPv3Client;
import com.trilead.ssh2.KnownHosts;
import com.trilead.ssh2.SFTPv3FileHandle;
import com.trilead.ssh2.SFTPv3FileAttributes;
import com.trilead.ssh2.HTTPProxyData;



/**
 * This defines a SSH2 Put job entry.
 * 
 * @author Samatar
 * @since 17-12-2007
 *
 */

public class JobEntrySSH2PUT extends JobEntryBase implements Cloneable, JobEntryInterface
{
	LogWriter log = LogWriter.getInstance();
	
	private String serverName;
	private String userName;
	private String password;
	private String serverPort;
	private String ftpDirectory;
	private String localDirectory;
	private String wildcard;
    private boolean onlyGettingNewFiles;  /* Don't overwrite files */
    private boolean usehttpproxy;
    private String httpproxyhost;
    private String httpproxyport;
    private String httpproxyusername;
    private String httpproxypassword;
    private boolean publicpublickey;
    private String keyfilename;
    private String keyfilepass;
    private boolean usebasicauthentication;
    private boolean createremotefolder;
    private String afterftpput;
    private String destinationfolder;
    private boolean createdestinationfolder;
    private boolean cachehostkey;
    private int     timeout;
   
    static KnownHosts database = new KnownHosts();
   
	
	public JobEntrySSH2PUT(String n)
	{
		super(n, "");
		serverName=null;
		publicpublickey=false;
		keyfilename=null;
		keyfilepass=null;
		usehttpproxy=false;
		httpproxyhost=null;
		httpproxyport=null;
		httpproxyusername=null;
		httpproxypassword=null;
		serverPort="22";
		usebasicauthentication=false;
		createremotefolder=false;
		afterftpput="do_nothing";
		destinationfolder=null;
		createdestinationfolder=false;
		cachehostkey=false;
		timeout=0;
		setID(-1L);
		setJobEntryType(JobEntryType.SSH2_PUT);
	}

	public JobEntrySSH2PUT()
	{
		this("");
	}

	public JobEntrySSH2PUT(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntrySSH2PUT je = (JobEntrySSH2PUT) super.clone();
        return je;
    }
    
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(128);
		
		retval.append(super.getXML());
		
		retval.append("      ").append(XMLHandler.addTagValue("servername",   serverName));
		retval.append("      ").append(XMLHandler.addTagValue("username",     userName));
		retval.append("      ").append(XMLHandler.addTagValue("password",     password));
		retval.append("      ").append(XMLHandler.addTagValue("serverport",   serverPort));
		retval.append("      ").append(XMLHandler.addTagValue("ftpdirectory", ftpDirectory));
		retval.append("      ").append(XMLHandler.addTagValue("localdirectory", localDirectory));
		retval.append("      ").append(XMLHandler.addTagValue("wildcard",     wildcard));
        retval.append("      ").append(XMLHandler.addTagValue("only_new",     onlyGettingNewFiles));
        
        retval.append("      ").append(XMLHandler.addTagValue("usehttpproxy",     usehttpproxy));
        retval.append("      ").append(XMLHandler.addTagValue("httpproxyhost",   httpproxyhost));
        retval.append("      ").append(XMLHandler.addTagValue("httpproxyport",   httpproxyport));
        retval.append("      ").append(XMLHandler.addTagValue("httpproxyusername",     httpproxyusername));
        retval.append("      ").append(XMLHandler.addTagValue("httpproxypassword",     httpproxypassword));
        
        
        retval.append("      ").append(XMLHandler.addTagValue("publicpublickey",     publicpublickey));
        retval.append("      ").append(XMLHandler.addTagValue("keyfilename",   keyfilename));
        retval.append("      ").append(XMLHandler.addTagValue("keyfilepass",   keyfilepass));
        
        retval.append("      ").append(XMLHandler.addTagValue("usebasicauthentication",     usebasicauthentication));
        retval.append("      ").append(XMLHandler.addTagValue("createremotefolder",     createremotefolder));
        
        retval.append("      ").append(XMLHandler.addTagValue("afterftpput",   afterftpput));
        retval.append("      ").append(XMLHandler.addTagValue("destinationfolder",   destinationfolder));
        retval.append("      ").append(XMLHandler.addTagValue("createdestinationfolder",     createdestinationfolder));
        retval.append("      ").append(XMLHandler.addTagValue("cachehostkey",     cachehostkey));
        retval.append("      ").append(XMLHandler.addTagValue("timeout",      timeout));
        
        
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			serverName          = XMLHandler.getTagValue(entrynode, "servername");
			userName            = XMLHandler.getTagValue(entrynode, "username");
			password            = XMLHandler.getTagValue(entrynode, "password");
			serverPort      = XMLHandler.getTagValue(entrynode, "serverport");
			ftpDirectory        = XMLHandler.getTagValue(entrynode, "ftpdirectory");
			localDirectory     = XMLHandler.getTagValue(entrynode, "localdirectory");
			wildcard            = XMLHandler.getTagValue(entrynode, "wildcard");
            onlyGettingNewFiles = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "only_new") );
            
            usehttpproxy = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "usehttpproxy") );
            httpproxyhost          = XMLHandler.getTagValue(entrynode, "httpproxyhost");
            httpproxyport      = XMLHandler.getTagValue(entrynode, "httpproxyport");
            httpproxyusername            = XMLHandler.getTagValue(entrynode, "httpproxyusername");
            httpproxypassword            = XMLHandler.getTagValue(entrynode, "httpproxypassword");
            
            publicpublickey = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "publicpublickey") );
            keyfilename          = XMLHandler.getTagValue(entrynode, "keyfilename");
            keyfilepass          = XMLHandler.getTagValue(entrynode, "keyfilepass");
            
            usebasicauthentication = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "usebasicauthentication") );
            createremotefolder = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "createremotefolder") );
            
            afterftpput          = XMLHandler.getTagValue(entrynode, "afterftpput");
            destinationfolder          = XMLHandler.getTagValue(entrynode, "destinationfolder");
            
            createdestinationfolder = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "createdestinationfolder") );
            cachehostkey = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "cachehostkey") );
            timeout             = Const.toInt(XMLHandler.getTagValue(entrynode, "timeout"), 0);
            
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException(Messages.getString("JobSSH2PUT.Log.UnableLoadXML", xe.getMessage()));
		}
	}

	public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers)
	throws KettleException
	{
	try
	{
		super.loadRep(rep, id_jobentry, databases, slaveServers);
			serverName          = rep.getJobEntryAttributeString(id_jobentry, "servername");
			userName            = rep.getJobEntryAttributeString(id_jobentry, "username");
			password            = rep.getJobEntryAttributeString(id_jobentry, "password");
			serverPort 			 =rep.getJobEntryAttributeString(id_jobentry, "serverport");
			ftpDirectory        = rep.getJobEntryAttributeString(id_jobentry, "ftpdirectory");
			localDirectory     = rep.getJobEntryAttributeString(id_jobentry, "localdirectory");
			wildcard            = rep.getJobEntryAttributeString(id_jobentry, "wildcard");
			onlyGettingNewFiles = rep.getJobEntryAttributeBoolean(id_jobentry, "only_new");
			
			usehttpproxy = rep.getJobEntryAttributeBoolean(id_jobentry, "usehttpproxy");
			httpproxyhost          = rep.getJobEntryAttributeString(id_jobentry, "httpproxyhost");
			httpproxyusername            = rep.getJobEntryAttributeString(id_jobentry, "httpproxyusername");
			httpproxypassword            = rep.getJobEntryAttributeString(id_jobentry, "httpproxypassword");
			
			publicpublickey = rep.getJobEntryAttributeBoolean(id_jobentry, "publicpublickey");
			keyfilename            = rep.getJobEntryAttributeString(id_jobentry, "keyfilename");
			keyfilepass            = rep.getJobEntryAttributeString(id_jobentry, "keyfilepass");
			
			usebasicauthentication = rep.getJobEntryAttributeBoolean(id_jobentry, "usebasicauthentication");
			createremotefolder = rep.getJobEntryAttributeBoolean(id_jobentry, "createremotefolder");

			afterftpput            = rep.getJobEntryAttributeString(id_jobentry, "afterftpput");
			destinationfolder            = rep.getJobEntryAttributeString(id_jobentry, "destinationfolder");
			
			createdestinationfolder = rep.getJobEntryAttributeBoolean(id_jobentry, "createdestinationfolder");
			cachehostkey = rep.getJobEntryAttributeBoolean(id_jobentry, "cachehostkey");
			timeout             = (int)rep.getJobEntryAttributeInteger(id_jobentry, "timeout");
			
			
		}
		catch(KettleException dbe)
		{
			throw new KettleException(Messages.getString("JobSSH2PUT.Log.UnableLoadRep",""+id_jobentry,dbe.getMessage()));
		}
	}
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			rep.saveJobEntryAttribute(id_job, getID(), "servername",      serverName);
			rep.saveJobEntryAttribute(id_job, getID(), "username",        userName);
			rep.saveJobEntryAttribute(id_job, getID(), "password",        password);
			rep.saveJobEntryAttribute(id_job, getID(), "serverport",      serverPort);
			rep.saveJobEntryAttribute(id_job, getID(), "ftpdirectory",    ftpDirectory);
			rep.saveJobEntryAttribute(id_job, getID(), "localdirectory", localDirectory);
			rep.saveJobEntryAttribute(id_job, getID(), "wildcard",        wildcard);
			rep.saveJobEntryAttribute(id_job, getID(), "only_new",        onlyGettingNewFiles);
			
			rep.saveJobEntryAttribute(id_job, getID(), "usehttpproxy",        usehttpproxy);
			rep.saveJobEntryAttribute(id_job, getID(), "httpproxyhost",      httpproxyhost);
			rep.saveJobEntryAttribute(id_job, getID(), "httpproxyport",      httpproxyport);
			rep.saveJobEntryAttribute(id_job, getID(), "httpproxyusername",        httpproxyusername);
			rep.saveJobEntryAttribute(id_job, getID(), "httpproxypassword",        httpproxypassword);
			
			
			rep.saveJobEntryAttribute(id_job, getID(), "publicpublickey",        publicpublickey);
			rep.saveJobEntryAttribute(id_job, getID(), "keyfilename",      keyfilename);
			rep.saveJobEntryAttribute(id_job, getID(), "keyfilepass",      keyfilepass);
			
			rep.saveJobEntryAttribute(id_job, getID(), "usebasicauthentication",        usebasicauthentication);
			rep.saveJobEntryAttribute(id_job, getID(), "createremotefolder",        createremotefolder);
			
			
			rep.saveJobEntryAttribute(id_job, getID(), "afterftpput",        afterftpput);
			rep.saveJobEntryAttribute(id_job, getID(), "destinationfolder",        destinationfolder);
			
			
			rep.saveJobEntryAttribute(id_job, getID(), "createdestinationfolder",        createdestinationfolder);
			rep.saveJobEntryAttribute(id_job, getID(), "cachehostkey",        cachehostkey);
			rep.saveJobEntryAttribute(id_job, getID(), "timeout",         timeout);
			
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("JobSSH2PUT.Log.UnableSaveRep",""+id_job,dbe.getMessage()));
		}
	}


	/**
	 * @return Returns the directory.
	 */
	public String getFtpDirectory()
	{
		return ftpDirectory;
	}

	/**
	 * @param directory The directory to set.
	 */
	public void setFtpDirectory(String directory)
	{
		this.ftpDirectory = directory;
	}

	/**
	 * @return Returns the password.
	 */
	public String getPassword()
	{
		return password;
	}

	/**
	 * @param password The password to set.
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}

	/**
	 * @return Returns the afterftpput.
	 */
	public String getAfterFTPPut()
	{
		return afterftpput;
	}
	/**
	 * @param password The password to set.
	 */
	public void setAfterFTPPut(String afterftpputin)
	{
		this.afterftpput = afterftpputin;
	}
	
	
	
	/**
	 * @param proxypassword The httpproxypassword to set.
	 */
	public void setHTTPProxyPassword(String proxypassword)
	{
		this.httpproxypassword = proxypassword;
	}
	
	/**
	 * @return Returns the password.
	 */
	public String getHTTPProxyPassword()
	{
		return httpproxypassword;
	}

	
	/**
	 * @param keypass The keyfilepass to set.
	 */
	public void setKeyFilepass(String keypass)
	{
		this.keyfilepass = keypass;
	}
	
	
	/**
	 * @return Returns the keyfilepass.
	 */
	public String getKeyFilepass()
	{
		return keyfilepass;
	}

	
	
	
	/**
	 * @return Returns the serverName.
	 */
	public String getServerName()
	{
		return serverName;
	}

	/**
	 * @param serverName The serverName to set.
	 */
	public void setServerName(String serverName)
	{
		this.serverName = serverName;
	}
	/**
	 * @param proxyhost The httpproxyhost to set.
	 */
	public void setHTTPProxyHost(String proxyhost)
	{
		this.httpproxyhost = proxyhost;
	}
	
	/**
	 * @return Returns the httpproxyhost.
	 */
	public String getHTTPProxyHost()
	{
		return httpproxyhost;
	}
	
	/**
	 * @param filename The keyfilename to set.
	 */
	public void setKeyFilename(String keyfile)
	{
		this.keyfilename = keyfile;
	}
	
	
	/**
	 * @return Returns the keyfilename.
	 */
	public String getKeyFilename()
	{
		return keyfilename;
	}
	
	
	
	

	/**
	 * @return Returns the userName.
	 */
	public String getUserName()
	{
		return userName;
	}

	/**
	 * @param userName The userName to set.
	 */
	public void setUserName(String userName)
	{
		this.userName = userName;
	}
	
	/**
	 * @param proxyusername The httpproxyusername to set.
	 */
	public void setHTTPProxyUsername(String proxyusername)
	{
		this.httpproxyusername = proxyusername;
	}
	
	
	/**
	 * @return Returns the userName.
	 */
	public String getHTTPProxyUsername()
	{
		return httpproxyusername;
	}
	
	

	/**
	 * @return Returns the wildcard.
	 */
	public String getWildcard()
	{
		return wildcard;
	}

	/**
	 * @param wildcard The wildcard to set.
	 */
	public void setWildcard(String wildcard)
	{
		this.wildcard = wildcard;
	}

	/**
	 * @return Returns the localDirectory.
	 */
	public String getlocalDirectory()
	{
		return localDirectory;
	}

	/**
	 * @param localDirectory The localDirectory to set.
	 */
	public void setlocalDirectory(String localDirectory)
	{
		this.localDirectory = localDirectory;
	}



    /**
     * @return Returns the onlyGettingNewFiles.
     */
    public boolean isOnlyGettingNewFiles()
    {
        return onlyGettingNewFiles;
    }

    /**
     * @param onlyGettingNewFiles The onlyGettingNewFiles to set.
     */
    public void setOnlyGettingNewFiles(boolean onlyGettingNewFiles)
    {
        this.onlyGettingNewFiles = onlyGettingNewFiles;
    }
    
    
    
    
    /**
     * @param cachehostkeyin The cachehostkey to set.
     */
    public void setCacheHostKey(boolean cachehostkeyin)
    {
        this.cachehostkey = cachehostkeyin;
    }
    
    /**
     * @return Returns the cachehostkey.
     */
    public boolean isCacheHostKey()
    {
        return cachehostkey;
    }
    
    /**
     * @param httpproxy The usehttpproxy to set.
     */
    public void setUseHTTPProxy(boolean httpproxy)
    {
        this.usehttpproxy = httpproxy;
    }
    
    /**
     * @return Returns the usehttpproxy.
     */
    public boolean isUseHTTPProxy()
    {
        return usehttpproxy;
    }

    
    /**
     * @return Returns the usebasicauthentication.
     */
    public boolean isUseBasicAuthentication()
    {
        return usebasicauthentication;
    }
 
    /**
     * @param httpproxy The usebasicauthentication to set.
     */
    public void setUseBasicAuthentication(boolean usebasicauthenticationin)
    {
        this.usebasicauthentication = usebasicauthenticationin;
    }
    /**
     * @param createremotefolderin The createremotefolder to set.
     */
    public void setCreateRemoteFolder(boolean createremotefolderin)
    {
        this.createremotefolder = createremotefolderin;
    }
 
    /**
     * @return Returns the createremotefolder.
     */
    public boolean isCreateRemoteFolder()
    {
        return createremotefolder;
    } 
    
    /**
     * @param createdestinationfolderin The createdestinationfolder to set.
     */
    public void setCreateDestinationFolder(boolean createdestinationfolderin)
    {
        this.createdestinationfolder = createdestinationfolderin;
    }
 
    /**
     * @return Returns the createdestinationfolder.
     */
    public boolean isCreateDestinationFolder()
    {
        return createdestinationfolder;
    } 
    
    
    
    /**
     * @param publickey The publicpublickey to set.
     */
    public void setUsePublicKey(boolean publickey)
    {
        this.publicpublickey = publickey;
    }
    
    /**
     * @return Returns the usehttpproxy.
     */
    public boolean isUsePublicKey()
    {
        return publicpublickey;
    } 
    
    
	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}
	
	public void setHTTPProxyPort(String proxyport) {
		this.httpproxyport = proxyport;
	}
    
	public String getHTTPProxyPort() {
		return httpproxyport;
	}
    
	
	public void setDestinationFolder(String destinationfolderin) {
		this.destinationfolder = destinationfolderin;
	}
	
	public String getDestinationFolder() {
		return destinationfolder;
	}
    
	/**
	 * @param timeout The timeout to set.
	 */
	public void setTimeout(int timeout)
	{
		this.timeout = timeout;
	}

	/**
	 * @return Returns the timeout.
	 */
	public int getTimeout()
	{
		return timeout;
	}
	
	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		Result result = previousResult;
		result.setResult( false );
		
		// Get real variable value
		String realServerName=environmentSubstitute(serverName);
		int realServerPort=Const.toInt(environmentSubstitute(serverPort),22);
		String realUserName=environmentSubstitute(userName);
		String realServerPassword=environmentSubstitute(password);
		// Proxy Host
		String realProxyHost=environmentSubstitute(httpproxyhost);
		int realProxyPort=Const.toInt(environmentSubstitute(httpproxyport),22);
		String realproxyUserName=environmentSubstitute(httpproxyusername);
		String realProxyPassword=environmentSubstitute(httpproxypassword);
		// Key file
		String realKeyFilename=environmentSubstitute(keyfilename);
		String relKeyFilepass=environmentSubstitute(keyfilepass);
		// Source files
		String realLocalDirectory=environmentSubstitute(localDirectory);
		String realwildcard=environmentSubstitute(wildcard);
		// Remote destination
		String realftpDirectory=environmentSubstitute(ftpDirectory);
		// Destination folder (Move to)
		String realDestinationFolder=environmentSubstitute(destinationfolder);

		// Check for mandatory fields
		boolean mandatoryok=true;
		if(Const.isEmpty(realServerName))
		{
			mandatoryok=false;
			log.logError(toString(),Messages.getString("JobSSH2PUT.Log.ServernameMissing"));
		}
		if(usehttpproxy)
		{
			if(Const.isEmpty(realProxyHost))
			{
				mandatoryok=false;
				log.logError(toString(),Messages.getString("JobSSH2PUT.Log.HttpProxyhostMissing"));
			}
		}
		if(publicpublickey)
		{
			if(Const.isEmpty(realKeyFilename))
			{
				mandatoryok=false;
				log.logError(toString(),Messages.getString("JobSSH2PUT.Log.KeyFileMissing"));
			}else
			{
				// Let's check if folder exists...
				if(!new File(realKeyFilename).exists())
				{
					mandatoryok=false;
					log.logError(toString(),Messages.getString("JobSSH2PUT.Log.KeyFileNotExist"));
				}
			}
		}
	
		if(Const.isEmpty(realLocalDirectory))
		{
			mandatoryok=false;
			log.logError(toString(),Messages.getString("JobSSH2PUT.Log.LocalFolderMissing"));
		}
		if(afterftpput.equals("move_file"))
		{
			if(Const.isEmpty(realDestinationFolder))
			{
				mandatoryok=false;
				log.logError(toString(),Messages.getString("JobSSH2PUT.Log.DestinatFolderMissing"));
			}else{
				// Let's check if folder exists...
				if(!new File(realDestinationFolder).exists())
				{
					mandatoryok=false;
					log.logError(toString(),Messages.getString("JobSSH2PUT.Log.DestinatFolderNotExist",realDestinationFolder));
				}
			}
		}
		
		if(mandatoryok)
		{
		
			Connection conn = null;
			SFTPv3Client client = null;
	        boolean good=true;
	        
	        int nbfilestoput=0;
	        int nbput=0;
	        int nbrerror=0;
	
	
			try
			{
				// Create a connection instance 
				conn = getConnection(realServerName,realServerPort,realProxyHost,realProxyPort,realproxyUserName,realProxyPassword);
				
				if(timeout>0)
				{
					// Use timeout
					// Cache Host Key
					if(cachehostkey) conn.connect(new SimpleVerifier(database),0,timeout*1000);	
					else conn.connect(null,0,timeout*1000);	
					
				}else
				{
					// Cache Host Key
					if(cachehostkey) conn.connect(new SimpleVerifier(database));	
					else conn.connect();
				}
				
				// Authenticate
	
				boolean isAuthenticated = false;
				if(publicpublickey)
				{
					isAuthenticated=conn.authenticateWithPublicKey(realUserName, new File(realKeyFilename), relKeyFilepass);
				}else
				{
					isAuthenticated=conn.authenticateWithPassword(realUserName, realServerPassword);
				}
	
				// LET'S CHECK AUTHENTICATION ...
				if (isAuthenticated == false)
					log.logError(toString(),Messages.getString("JobSSH2PUT.Log.AuthenticationFailed"));
				else
				{
					log.logBasic(toString(),Messages.getString("JobSSH2PUT.Log.Connected",serverName,userName));
					
					client = new SFTPv3Client(conn);
					
					if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobSSH2PUT.Log.ProtocolVersion",""+client.getProtocolVersion()));
					
					
					// Check if remote directory exists
					if(realftpDirectory!=null)
					{
						if (!sshDirectoryExists(client, realftpDirectory)) 
						{
							good=false;
							if(createremotefolder)
							{
								good=CreateRemoteFolder(client,realftpDirectory);
								if(good) log.logBasic(toString(),Messages.getString("JobSSH2PUT.Log.RemoteDirectoryCreated"));
								
							}
							else
								log.logError(toString(),Messages.getString("JobSSH2PUT.Log.RemoteDirectoryNotExist",realftpDirectory));
						}
						else
							if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("JobSSH2PUT.Log.RemoteDirectoryExist",realftpDirectory));	
					}
					
					if(good)
					{
						// Get files list from local folder (source)
						String[] filelist = getFiles(realLocalDirectory);
						
						// Prepare Pattern for wildcard
						Pattern pattern = null;
						if (!Const.isEmpty(realwildcard)) pattern = Pattern.compile(realwildcard);

		
						// Let's put files now ...
						// Get the files in the list
						for (int i=0;i<filelist.length && !parentJob.isStopped();i++)
						{
							boolean getIt = true;
							// First see if the file matches the regular expression!
							if (pattern!=null)
							{
								Matcher matcher = pattern.matcher(filelist[i]);
								getIt = matcher.matches();
							}
							if(getIt)
							{
								nbfilestoput++;
								String FullFilename=filelist[i];
								
								if(realftpDirectory!=null)	FullFilename=realftpDirectory+Const.FILE_SEPARATOR+filelist[i];
								String FullLocalFilename=realLocalDirectory+Const.FILE_SEPARATOR+filelist[i];
								
								boolean putok=true;
								
							    if ( (onlyGettingNewFiles == false) ||
					                   (onlyGettingNewFiles == true) && !sshFileExists(client, FullFilename))
							       {
										putok=putFile(FullLocalFilename,FullFilename,client);
										if(!putok) 
										{
											nbrerror++;
											log.logError(toString(),Messages.getString("JobSSH2PUT.Log.Error.CanNotPutFile",filelist[i]));
										}else
										{
											nbput++;
											
										}
								   }
							       if(putok && !afterftpput.equals("do_nothing"))
							       {
										deleteOrMoveFiles(FullFilename,realDestinationFolder);
									}
							}
						}
						/********************************RESULT ********************/
						if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobSSH2PUT.Log.Result.JobEntryEnd1"));
						if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobSSH2PUT.Log.Result.TotalFiles",""+nbfilestoput));
						if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobSSH2PUT.Log.Result.TotalFilesPut",""+nbput));
						if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobSSH2PUT.Log.Result.TotalFilesError",""+nbrerror));
						if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobSSH2PUT.Log.Result.JobEntryEnd2"));
						if(nbrerror==0) result.setResult(true);
					}
		
				}
	
			}
			catch (Exception e)
			{
				result.setNrErrors(nbrerror);
				log.logError(toString(), Messages.getString("JobSSH2PUT.Log.Error.ErrorFTP",e.getMessage()));
			}
			 finally 
			 {
				 if (conn!=null)  conn.close();
				 if(client!=null) client.close();
			}
		}
		
		return result;
	}
	
	private Connection getConnection(String servername,int serverpassword,
			String proxyhost,int proxyport,String proxyusername,String proxypassword)
	{
		/* Create a connection instance */

		Connection connnect = new Connection(servername,serverpassword);
	
		/* We want to connect through a HTTP proxy */
		if(usehttpproxy)
		{
			connnect.setProxyData(new HTTPProxyData(proxyhost, proxyport));
		
			/* Now connect */
			// if the proxy requires basic authentication:
			if(usebasicauthentication)
			{
				connnect.setProxyData(new HTTPProxyData(proxyhost, proxyport, proxyusername, proxypassword));
			}
		}
		
		return connnect;
	}
	
	private boolean putFile(String localfilename,String remotefilename,SFTPv3Client sftpClient)
	{
		LogWriter log = LogWriter.getInstance();
		long filesize=-1;
		FileInputStream in=null;
		BufferedInputStream inBuf = null;
		SFTPv3FileHandle sftpFileHandle=null;
		boolean retval=false;
		try
		{
			// Put file in the default folder
			sftpFileHandle=sftpClient.createFileTruncate(remotefilename);
			
			// Associate a file input stream for the current local file
			in = new FileInputStream(localfilename);
			inBuf = new BufferedInputStream(in);
			long bytesWritten = 0;
			byte[] buf = new byte[2048];
			long length = new File(localfilename).length();
			// Write to remote file
			while (bytesWritten != length) 
			{
				int len = inBuf.read(buf, 0, buf.length);
				sftpClient.write(sftpFileHandle, bytesWritten, buf, 0, len);
				bytesWritten += len;
			}
			
			// Get File size
			filesize=getFileSize(sftpClient, remotefilename) ;
			
			if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("JobSSH2PUT.Log.FileOnRemoteHost",remotefilename,""+filesize));
			
			retval= true;
		  } catch(Exception e)  
		  {
			  // We failed to put files
			  log.logError(toString(),Messages.getString("JobSSH2PUT.Log.ErrorCopyingFile",localfilename));
		  }
			finally 
			{
				if (in != null)
					try 
					{
						in.close();
						in = null;
					} 
					catch (Exception ex) {} 
					
					if(inBuf!=null)
					try 
					{
						inBuf.close();
						inBuf = null;
					} 
					catch (Exception ex) {}
					if(sftpFileHandle!=null) 
					try 
					{
						sftpClient.closeFile(sftpFileHandle);
						sftpFileHandle=null;
					} catch (Exception ex) {}
			}
		return retval;
	}


	/**
     * Check existence of a file
     * 
     * @param sftpClient
     * @param filename
     * @return true, if file exists
     * @throws Exception
     */
    public boolean sshFileExists(SFTPv3Client sftpClient, String filename) {
        
        try {
            SFTPv3FileAttributes attributes = sftpClient.stat(filename);
            
            if (attributes != null) {
                return (attributes.isRegularFile());
            } else {
                return false;
            }
            
        } catch (Exception e) {
            return false;
        }
    }
    
    
    /**
     * Checks if a directory exists
     * 
     * @param sftpClient
     * @param directory
     * @return true, if directory exists
     */
    public boolean sshDirectoryExists(SFTPv3Client sftpClient, String directory)  {
    try {
           SFTPv3FileAttributes attributes = sftpClient.stat(directory);
              
            if (attributes != null) {
                 return (attributes.isDirectory());
              } else {
                  return false;
              }
              
        } catch (Exception e) {
              return false;
        }
    }    

    /**
     * Create remote folder
     * 
     * @param sftpClient
     * @param foldername
     * @return true, if foldername is created
     */
    private boolean CreateRemoteFolder(SFTPv3Client sftpClient, String foldername)
    {
    	LogWriter log = LogWriter.getInstance();
    	boolean retval=false;
    	
    	if(!sshDirectoryExists(sftpClient, foldername))
    	{
    		try
    		{
    			sftpClient.mkdir(foldername, 0700);
    			retval=true;
    			
    		}catch (Exception e)
    		{
    			log.logError(toString(), Messages.getString("JobSSH2PUT.Log.Error.CreatingRemoteFolder",foldername));
    		}
    	}
    	return retval;
    }
	/**
	 * Returns the file size of a file
	 * 
	 * @param sftpClient
	 * @param filename
	 * @return the size of the file
	 * @throws Exception
	 */
	public long getFileSize(SFTPv3Client sftpClient, String filename) throws Exception 
	{
		return sftpClient.stat(filename).size.longValue();
	}  
    private String[] getFiles(String localfolder)
    {
		ArrayList<String> myFileList = new ArrayList<String>();
		
		// Get all the files in the local directory...
		
		File localFiles = new File(localfolder);
		File[] children = localFiles.listFiles();
		for (int i=0; i<children.length; i++) 
		{
            // Get filename of file or directory
			if (!children[i].isDirectory()) 
			{
				//myFileList.add(children[i].getAbsolutePath());
				myFileList.add(children[i].getName());
				
			}
        } // end for
		
		String[] filelist = new String[myFileList.size()];

		myFileList.toArray(filelist);
		
		return filelist;
    }
    private boolean deleteOrMoveFiles(String filename,String destinationFolder)
    {
    	boolean retval=false;
    	
    	String LocalFullFilename=localDirectory+Const.FILE_SEPARATOR+filename;
    	
    	// Delete the file if this is needed!
		if (afterftpput.equals("delete_file")) 
		{
			new File(LocalFullFilename).delete();
			retval=true;
			if (log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobSSH2PUT.Log.DeletedFile",filename));
		}
		else if (afterftpput.equals("move_file"))
		{
			// Move File	
			FileObject destination=null;
			FileObject source=null;
			try
			{
				destination = KettleVFS.getFileObject(destinationFolder + Const.FILE_SEPARATOR+filename);
				source= KettleVFS.getFileObject(LocalFullFilename);
				source.moveTo(destination);
				retval=true;
			}
			catch (Exception e) 
			{
				log.logError(toString(), Messages.getString("JobSSH2PUT.Cant_Move_File.Label",filename,destinationFolder,e.getMessage()));
			}
			finally 
			{
				if ( destination != null ) 
				{try {destination.close();}catch (Exception ex ) {};}
				if ( source != null ) 
				{try  {source.close();}
					catch (Exception ex ) {};
				}
			}
			if (log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobSSH2PUT.Log.MovedFile",filename,ftpDirectory));
		}
		return retval;
    }
    public boolean evaluates()
	{
		return true;
	}
    public List<ResourceReference> getResourceDependencies(JobMeta jobMeta) {
        List<ResourceReference> references = super.getResourceDependencies(jobMeta);
        if (!Const.isEmpty(serverName)) {
          String realServerName = jobMeta.environmentSubstitute(serverName);
          ResourceReference reference = new ResourceReference(this);
          reference.getEntries().add( new ResourceEntry(realServerName, ResourceType.SERVER));
          references.add(reference);
        }
        return references;
      }

      @Override
      public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
      {
        andValidator().validate(this, "serverName", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
        andValidator()
            .validate(this, "localDirectory", remarks, putValidators(notBlankValidator(), fileExistsValidator())); //$NON-NLS-1$
        andValidator().validate(this, "userName", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
        andValidator().validate(this, "password", remarks, putValidators(notNullValidator())); //$NON-NLS-1$
        andValidator().validate(this, "serverPort", remarks, putValidators(integerValidator())); //$NON-NLS-1$
      }
    

}