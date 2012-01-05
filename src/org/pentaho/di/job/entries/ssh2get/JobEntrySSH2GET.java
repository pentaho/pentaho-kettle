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

package org.pentaho.di.job.entries.ssh2get;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.integerValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.w3c.dom.Node;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.HTTPProxyData;
import com.trilead.ssh2.KnownHosts;
import com.trilead.ssh2.SFTPv3Client;
import com.trilead.ssh2.SFTPv3DirectoryEntry;
import com.trilead.ssh2.SFTPv3FileAttributes;
import com.trilead.ssh2.SFTPv3FileHandle;



/**
 * This defines a SSH2 GET job entry.
 * 
 * @author Samatar
 * @since 17-12-2007
 *
 */

public class JobEntrySSH2GET extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private static Class<?> PKG = JobEntrySSH2GET.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String serverName;
	private String userName;
	private String password;
	private String serverPort;
	private String ftpDirectory;
	private String localDirectory;
	private String wildcard;
    private boolean onlyGettingNewFiles;  /* Don't overwrite files */
    private boolean usehttpproxy;
    private String httpProxyHost;
    private String httpproxyport;
    private String httpproxyusername;
    private String httpProxyPassword;
    private boolean publicpublickey;
    private String keyFilename;
    private String keyFilePass;
    private boolean useBasicAuthentication;
    private String afterFtpPut;
    private String destinationfolder;
    private boolean createdestinationfolder;
    private boolean cachehostkey;
    private int     timeout;
    boolean createtargetfolder;
    boolean includeSubFolders;
    
    static KnownHosts database = new KnownHosts();
    int nbfilestoget=0;
    int nbgot=0;
    int nbrerror=0;
   
	
	public JobEntrySSH2GET(String n)
	{
		super(n, "");
		serverName=null;
		publicpublickey=false;
		keyFilename=null;
		keyFilePass=null;
		usehttpproxy=false;
		httpProxyHost=null;
		httpproxyport=null;
		httpproxyusername=null;
		httpProxyPassword=null;
		serverPort="22";
		useBasicAuthentication=false;
		afterFtpPut="do_nothing";
		destinationfolder=null;
		includeSubFolders=false;
		createdestinationfolder=false;
		createtargetfolder=false;
		cachehostkey=false;
		timeout=0;
		setID(-1L);
	}

	public JobEntrySSH2GET()
	{
		this("");
	}

    public Object clone()
    {
        JobEntrySSH2GET je = (JobEntrySSH2GET) super.clone();
        return je;
    }
    
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(128);
		
		retval.append(super.getXML());
		
		retval.append("      ").append(XMLHandler.addTagValue("servername",   serverName));
		retval.append("      ").append(XMLHandler.addTagValue("username",     userName));
	    retval.append("      ").append(XMLHandler.addTagValue("password", Encr.encryptPasswordIfNotUsingVariables(getPassword())));
		retval.append("      ").append(XMLHandler.addTagValue("serverport",   serverPort));
		retval.append("      ").append(XMLHandler.addTagValue("ftpdirectory", ftpDirectory));
		retval.append("      ").append(XMLHandler.addTagValue("localdirectory", localDirectory));
		retval.append("      ").append(XMLHandler.addTagValue("wildcard",     wildcard));
        retval.append("      ").append(XMLHandler.addTagValue("only_new",     onlyGettingNewFiles));
        
        retval.append("      ").append(XMLHandler.addTagValue("usehttpproxy",     usehttpproxy));
        retval.append("      ").append(XMLHandler.addTagValue("httpproxyhost",   httpProxyHost));
        retval.append("      ").append(XMLHandler.addTagValue("httpproxyport",   httpproxyport));
        retval.append("      ").append(XMLHandler.addTagValue("httpproxyusername",     httpproxyusername));
        retval.append("      ").append(XMLHandler.addTagValue("httpproxypassword",     httpProxyPassword));
        
        
        retval.append("      ").append(XMLHandler.addTagValue("publicpublickey",     publicpublickey));
        retval.append("      ").append(XMLHandler.addTagValue("keyfilename",   keyFilename));
        retval.append("      ").append(XMLHandler.addTagValue("keyfilepass",   keyFilePass));
        
        retval.append("      ").append(XMLHandler.addTagValue("usebasicauthentication",     useBasicAuthentication));
        retval.append("      ").append(XMLHandler.addTagValue("afterftpput",   afterFtpPut));
        retval.append("      ").append(XMLHandler.addTagValue("destinationfolder",   destinationfolder));
        retval.append("      ").append(XMLHandler.addTagValue("createdestinationfolder",     createdestinationfolder));
        retval.append("      ").append(XMLHandler.addTagValue("cachehostkey",     cachehostkey));
        retval.append("      ").append(XMLHandler.addTagValue("timeout",      timeout));
        retval.append("      ").append(XMLHandler.addTagValue("createtargetfolder",     createtargetfolder));
        retval.append("      ").append(XMLHandler.addTagValue("includeSubFolders",     includeSubFolders));
        
        
        
        
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			serverName          = XMLHandler.getTagValue(entrynode, "servername");
			userName            = XMLHandler.getTagValue(entrynode, "username");
		    password = Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(entrynode, "password")); 
			serverPort      = XMLHandler.getTagValue(entrynode, "serverport");
			ftpDirectory        = XMLHandler.getTagValue(entrynode, "ftpdirectory");
			localDirectory     = XMLHandler.getTagValue(entrynode, "localdirectory");
			wildcard            = XMLHandler.getTagValue(entrynode, "wildcard");
            onlyGettingNewFiles = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "only_new") );
            
            usehttpproxy = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "usehttpproxy") );
            httpProxyHost          = XMLHandler.getTagValue(entrynode, "httpproxyhost");
            httpproxyport      = XMLHandler.getTagValue(entrynode, "httpproxyport");
            httpproxyusername            = XMLHandler.getTagValue(entrynode, "httpproxyusername");
            httpProxyPassword            = XMLHandler.getTagValue(entrynode, "httpproxypassword");
            
            publicpublickey = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "publicpublickey") );
            keyFilename          = XMLHandler.getTagValue(entrynode, "keyfilename");
            keyFilePass          = XMLHandler.getTagValue(entrynode, "keyfilepass");
            
            useBasicAuthentication = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "usebasicauthentication") );
            afterFtpPut          = XMLHandler.getTagValue(entrynode, "afterftpput");
            destinationfolder          = XMLHandler.getTagValue(entrynode, "destinationfolder");
            
            createdestinationfolder = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "createdestinationfolder") );
            cachehostkey = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "cachehostkey") );
            timeout             = Const.toInt(XMLHandler.getTagValue(entrynode, "timeout"), 0);
            
            createtargetfolder = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "createtargetfolder") );
            includeSubFolders = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "includeSubFolders") );
            
            
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "JobSSH2GET.Log.UnableLoadXML", xe.getMessage()));
		}
	}

	public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	{
		try
		{
			serverName          = rep.getJobEntryAttributeString(id_jobentry, "servername");
			userName            = rep.getJobEntryAttributeString(id_jobentry, "username");
			password = Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString(id_jobentry, "password") );
			serverPort 			 =rep.getJobEntryAttributeString(id_jobentry, "serverport");
			ftpDirectory        = rep.getJobEntryAttributeString(id_jobentry, "ftpdirectory");
			localDirectory     = rep.getJobEntryAttributeString(id_jobentry, "localdirectory");
			wildcard            = rep.getJobEntryAttributeString(id_jobentry, "wildcard");
			onlyGettingNewFiles = rep.getJobEntryAttributeBoolean(id_jobentry, "only_new");
			
			usehttpproxy = rep.getJobEntryAttributeBoolean(id_jobentry, "usehttpproxy");
			httpProxyHost          = rep.getJobEntryAttributeString(id_jobentry, "httpproxyhost");
			httpproxyusername            = rep.getJobEntryAttributeString(id_jobentry, "httpproxyusername");
			httpProxyPassword            = rep.getJobEntryAttributeString(id_jobentry, "httpproxypassword");
			
			publicpublickey = rep.getJobEntryAttributeBoolean(id_jobentry, "publicpublickey");
			keyFilename            = rep.getJobEntryAttributeString(id_jobentry, "keyfilename");
			keyFilePass            = rep.getJobEntryAttributeString(id_jobentry, "keyfilepass");
			
			useBasicAuthentication = rep.getJobEntryAttributeBoolean(id_jobentry, "usebasicauthentication");
			afterFtpPut            = rep.getJobEntryAttributeString(id_jobentry, "afterftpput");
			destinationfolder            = rep.getJobEntryAttributeString(id_jobentry, "destinationfolder");
			
			createdestinationfolder = rep.getJobEntryAttributeBoolean(id_jobentry, "createdestinationfolder");
			cachehostkey = rep.getJobEntryAttributeBoolean(id_jobentry, "cachehostkey");
			timeout             = (int)rep.getJobEntryAttributeInteger(id_jobentry, "timeout");
			
			createtargetfolder = rep.getJobEntryAttributeBoolean(id_jobentry, "createtargetfolder");
			includeSubFolders = rep.getJobEntryAttributeBoolean(id_jobentry, "includeSubFolders");
			
			
			
		}
		catch(KettleException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "JobSSH2GET.Log.UnableLoadRep",""+id_jobentry,dbe.getMessage()));
		}
	}
	
	public void saveRep(Repository rep, ObjectId id_job) throws KettleException
	{
		try
		{
			rep.saveJobEntryAttribute(id_job, getObjectId(), "servername",      serverName);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "username",        userName);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "password", Encr.encryptPasswordIfNotUsingVariables(password));
			rep.saveJobEntryAttribute(id_job, getObjectId(), "serverport",      serverPort);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "ftpdirectory",    ftpDirectory);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "localdirectory", localDirectory);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "wildcard",        wildcard);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "only_new",        onlyGettingNewFiles);
			
			rep.saveJobEntryAttribute(id_job, getObjectId(), "usehttpproxy",        usehttpproxy);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "httpproxyhost",      httpProxyHost);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "httpproxyport",      httpproxyport);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "httpproxyusername",        httpproxyusername);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "httpproxypassword",        httpProxyPassword);
			
			rep.saveJobEntryAttribute(id_job, getObjectId(), "publicpublickey",        publicpublickey);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "keyfilename",      keyFilename);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "keyfilepass",      keyFilePass);
			
			rep.saveJobEntryAttribute(id_job, getObjectId(), "usebasicauthentication",        useBasicAuthentication);
			
			rep.saveJobEntryAttribute(id_job, getObjectId(), "afterftpput",        afterFtpPut);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "destinationfolder",        destinationfolder);
			
			rep.saveJobEntryAttribute(id_job, getObjectId(), "createdestinationfolder",        createdestinationfolder);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "cachehostkey",        cachehostkey);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "timeout",         timeout);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "createtargetfolder",        createtargetfolder);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "includeSubFolders",        includeSubFolders);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "JobSSH2GET.Log.UnableSaveRep",""+id_job,dbe.getMessage()));
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
		return afterFtpPut;
	}
	/**
	 * @param afterFtpPut The action after (FTP/SSH) transfer to execute
	 */
	public void setAfterFTPPut(String afterFtpPut)
	{
		this.afterFtpPut = afterFtpPut;
	}
	
	
	
	/**
	 * @param proxyPassword The httpproxypassword to set.
	 */
	public void setHTTPProxyPassword(String proxyPassword)
	{
		this.httpProxyPassword = proxyPassword;
	}
	
	/**
	 * @return Returns the password.
	 */
	public String getHTTPProxyPassword()
	{
		return httpProxyPassword;
	}

	
	/**
	 * @param keyFilePass The key file pass to set.
	 */
	public void setKeyFilePass(String keyFilePass)
	{
		this.keyFilePass = keyFilePass;
	}
	
	
	/**
	 * @return Returns the key file pass.
	 */
	public String getKeyFilePass()
	{
		return keyFilePass;
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
		this.httpProxyHost = proxyhost;
	}
	
	/**
	 * @return Returns the HTTP proxy host.
	 */
	public String getHTTPProxyHost()
	{
		return httpProxyHost;
	}
	
	/**
	 * @param keyfilename The key filename to set.
	 */
	public void setKeyFilename(String keyfilename)
	{
		this.keyFilename = keyfilename;
	}
	
	
	/**
	 * @return Returns the key filename.
	 */
	public String getKeyFilename()
	{
		return keyFilename;
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
     * @return Returns the use basic authentication flag.
     */
    public boolean isUseBasicAuthentication()
    {
        return useBasicAuthentication;
    }
 
    /**
     * @param useBasicAuthentication The use basic authentication flag to set.
     */
    public void setUseBasicAuthentication(boolean useBasicAuthentication)
    {
        this.useBasicAuthentication = useBasicAuthentication;
    }
  
    
    /**
     * @param includeSubFolders The include sub folders flag to set.
     */
    public void setIncludeSubFolders(boolean includeSubFolders)
    {
        this.includeSubFolders = includeSubFolders;
    }
    
    /**
     * @return Returns the include sub folders flag.
     */
    public boolean isIncludeSubFolders()
    {
        return includeSubFolders;
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
     * @return Returns the CreateTargetFolder.
     */
    public boolean isCreateTargetFolder()
    {
        return createtargetfolder;
    }  
    /**
     * @param createtargetfolderin The createtargetfolder to set.
     */
    public void setCreateTargetFolder(boolean createtargetfolderin)
    {
        this.createtargetfolder = createtargetfolderin;
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
	
	public Result execute(Result previousResult, int nr)
	{
		Result result = previousResult;
		result.setResult( false );
		
		if(log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "JobSSH2GET.Log.GettingFieldsValue"));
		
		// Get real variable value
		String realServerName=environmentSubstitute(serverName);
		int realServerPort=Const.toInt(environmentSubstitute(serverPort),22);
		String realUserName=environmentSubstitute(userName);
		String realServerPassword=Encr.decryptPasswordOptionallyEncrypted(environmentSubstitute(password));
		// Proxy Host
		String realProxyHost=environmentSubstitute(httpProxyHost);
		int realProxyPort=Const.toInt(environmentSubstitute(httpproxyport),22);
		String realproxyUserName=environmentSubstitute(httpproxyusername);
		String realProxyPassword=Encr.decryptPasswordOptionallyEncrypted(environmentSubstitute(httpProxyPassword));
		// Key file
		String realKeyFilename=environmentSubstitute(keyFilename);
		String relKeyFilepass=environmentSubstitute(keyFilePass);
		// target files
		String realLocalDirectory=environmentSubstitute(localDirectory);
		String realwildcard=environmentSubstitute(wildcard);
		// Remote source 
		String realftpDirectory=environmentSubstitute(ftpDirectory);
		// Destination folder (Move to)
		String realDestinationFolder=environmentSubstitute(destinationfolder);
		
		try{
			// Remote source 
			realftpDirectory=FTPUtils.normalizePath(realftpDirectory);
			// Destination folder (Move to)
			realDestinationFolder=FTPUtils.normalizePath(realDestinationFolder);
		}catch(Exception e){
			logError(BaseMessages.getString(PKG, "JobSSH2GET.Log.CanNotNormalizePath",e.getMessage()));
			result.setNrErrors(1);
			return result;
		}

		// Check for mandatory fields
		if(log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "JobSSH2GET.Log.CheckingMandatoryFields"));
		
		boolean mandatoryok=true;
		if(Const.isEmpty(realServerName))
		{
			mandatoryok=false;
			logError(BaseMessages.getString(PKG, "JobSSH2GET.Log.ServernameMissing"));
		}
		if(usehttpproxy)
		{
			if(Const.isEmpty(realProxyHost))
			{
				mandatoryok=false;
				logError(BaseMessages.getString(PKG, "JobSSH2GET.Log.HttpProxyhostMissing"));
			}
		}
		if(publicpublickey)
		{
			if(Const.isEmpty(realKeyFilename))
			{
				mandatoryok=false;
				logError(BaseMessages.getString(PKG, "JobSSH2GET.Log.KeyFileMissing"));
			}else
			{
				// Let's check if key file exists...
				if(!new File(realKeyFilename).exists())
				{
					mandatoryok=false;
					logError(BaseMessages.getString(PKG, "JobSSH2GET.Log.KeyFileNotExist"));
				}
			}
		}
	
		if(Const.isEmpty(realLocalDirectory))
		{
			mandatoryok=false;
			logError(BaseMessages.getString(PKG, "JobSSH2GET.Log.LocalFolderMissing"));
		}else{
			// Check if target folder exists...
			if(!new File(realLocalDirectory).exists())
			{
				
				if(createtargetfolder)
				{
					// Create Target folder
					if(!CreateFolder(realLocalDirectory)) mandatoryok=false;
	
				}else
				{
					mandatoryok=false;
					logError(BaseMessages.getString(PKG, "JobSSH2GET.Log.LocalFolderNotExists", realLocalDirectory));
				}
			}else{
				if(!new File(realLocalDirectory).isDirectory())
				{
					mandatoryok=false;
					logError(BaseMessages.getString(PKG, "JobSSH2GET.Log.LocalFolderNotFolder",realLocalDirectory));
				}
			}
		}
		if(afterFtpPut.equals("move_file"))
		{
			if(Const.isEmpty(realDestinationFolder))
			{
				mandatoryok=false;
				logError(BaseMessages.getString(PKG, "JobSSH2GET.Log.DestinatFolderMissing"));
			}
		}
		
		if(mandatoryok)
		{
			
			Connection conn = null;
			SFTPv3Client client = null;
	        boolean good=true;
	        
			try
			{
				// Create a connection instance 
				conn = getConnection(realServerName,realServerPort,realProxyHost,realProxyPort,realproxyUserName,realProxyPassword);
				if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobSSH2GET.Log.ConnectionInstanceCreated"));
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
					logError(BaseMessages.getString(PKG, "JobSSH2GET.Log.AuthenticationFailed"));
				else
				{
					if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "JobSSH2GET.Log.Connected",serverName,userName));
					
					client = new SFTPv3Client(conn);
					
					if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobSSH2GET.Log.ProtocolVersion",""+client.getProtocolVersion()));
					
					// Check if ftp (source) directory exists
					if(!Const.isEmpty(realftpDirectory))
					{
						if (!sshDirectoryExists(client, realftpDirectory)) 
						{
							good=false;
							logError(BaseMessages.getString(PKG, "JobSSH2GET.Log.RemoteDirectoryNotExist",realftpDirectory));
						}
						else
							if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobSSH2GET.Log.RemoteDirectoryExist",realftpDirectory));	
					}
				
					
					if(realDestinationFolder!=null)
					{
						// Check now destination folder
						if(!sshDirectoryExists(client , realDestinationFolder))
						{
							if(createdestinationfolder)
							{
								if(!CreateRemoteFolder(client,realDestinationFolder)) good=false;	
							}else
							{
								good=false;
								logError(BaseMessages.getString(PKG, "JobSSH2GET.Log.DestinatFolderNotExist",realDestinationFolder));
							}
						}
					}
					
					if(good)
					{
						Pattern pattern=null;
				        if (!Const.isEmpty(realwildcard))
				        {
				        	pattern = Pattern.compile(realwildcard);
				        }
				        
						if(includeSubFolders)
						{
							if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobSSH2GET.Log.RecursiveModeOn"));
							copyRecursive( realftpDirectory ,realLocalDirectory, client,pattern,parentJob);
						}else{
							if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobSSH2GET.Log.RecursiveModeOff"));
							GetFiles(realftpDirectory, realLocalDirectory,client,pattern,parentJob);
						}
						
						/********************************RESULT ********************/
						if(log.isDetailed()) 
						{
							logDetailed(BaseMessages.getString(PKG, "JobSSH2GET.Log.Result.JobEntryEnd1"));
						    logDetailed(BaseMessages.getString(PKG, "JobSSH2GET.Log.Result.TotalFiles",""+nbfilestoget));
							logDetailed(BaseMessages.getString(PKG, "JobSSH2GET.Log.Result.TotalFilesPut",""+nbgot));
							logDetailed(BaseMessages.getString(PKG, "JobSSH2GET.Log.Result.TotalFilesError",""+nbrerror));
							logDetailed(BaseMessages.getString(PKG, "JobSSH2GET.Log.Result.JobEntryEnd2"));
						}
						if(nbrerror==0) result.setResult(true);
						/********************************RESULT ********************/
					}
		
				}
	
			}
			catch (Exception e)
			{
				result.setNrErrors(nbrerror);
				logError(BaseMessages.getString(PKG, "JobSSH2GET.Log.Error.ErrorFTP",e.getMessage()));
			}
			 finally 
			 {
				 if (conn!=null)  conn.close();
				 if(client!=null) client.close();
			}
		}
		
		return result;
	}

	private Connection getConnection(String servername,int serverport,
			String proxyhost,int proxyport,String proxyusername,String proxypassword)
	{
		/* Create a connection instance */

		Connection conn = new Connection(servername,serverport);
	
		/* We want to connect through a HTTP proxy */
		if(usehttpproxy)
		{
			conn.setProxyData(new HTTPProxyData(proxyhost, proxyport));
		
			/* Now connect */
			// if the proxy requires basic authentication:
			if(useBasicAuthentication)
			{
				conn.setProxyData(new HTTPProxyData(proxyhost, proxyport, proxyusername, proxypassword));
			}
		}
		
		return conn;
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
     * Check existence of a local file
     * 
     * @param filename
     * @return true, if file exists
     */
    public boolean FileExists(String filename) {
        
    	FileObject file=null;
        try {
        	file=KettleVFS.getFileObject(filename, this);
        	if(!file.exists()) return false;
        	else
        	{
        		if(file.getType() == FileType.FILE) return true;
        		else return false;
        	}
        } catch (Exception e) {
            return false;
        }
    }
    
	/**
	 * Checks if file is a directory
	 * 
	 * @param sftpClient
	 * @param filename
	 * @return true, if filename is a directory
	 */
	public boolean isDirectory(SFTPv3Client sftpClient, String filename)  
	{
		try {
			return sftpClient.stat(filename).isDirectory();
		} 
		catch(Exception e)  {}
		return false;
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
	/**********************************************************
	 * 
	 * @param selectedfile
	 * @param wildcard
	 * @param pattern
	 * @return True if the selectedfile matches the wildcard
	 **********************************************************/
	private boolean getFileWildcard(String selectedfile,Pattern pattern)
	{
		boolean getIt=true;
		// First see if the file matches the regular expression!
		if (pattern!=null)
		{
			Matcher matcher = pattern.matcher(selectedfile);
			getIt = matcher.matches();
		}
		
		return getIt;
	}
   
    private boolean deleteOrMoveFiles(SFTPv3Client sftpClient, String filename,String destinationFolder)
    {
    	boolean retval=false;

    	// Delete the file if this is needed!
		if (afterFtpPut.equals("delete_file")) 
		{
			try
			{
				sftpClient.rm(filename);
				retval=true;
				if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobSSH2GET.Log.DeletedFile",filename));
			}catch (Exception e)
			{
				logError(BaseMessages.getString(PKG, "JobSSH2GET.Log.Error.CanNotDeleteRemoteFile",filename));
			}
			
		}
		else if (afterFtpPut.equals("move_file"))
		{
			String DestinationFullFilename=destinationFolder+Const.FILE_SEPARATOR+filename;
			try
			{
				sftpClient.mv(filename, DestinationFullFilename);
				retval=true;
				if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobSSH2GET.Log.DeletedFile",filename));
			}catch (Exception e)
			{
				logError(BaseMessages.getString(PKG, "JobSSH2GET.Log.Error.MovedFile",filename,destinationFolder));
			}
		
		}
		return retval;
    }
	/**
	 * copy a directory from the remote host to the local one.
	 * 
	 * @param sourceLocation the source directory on the remote host
	 * @param targetLocation the target directory on the local host
	 * @param sftpClient is an instance of SFTPv3Client that makes SFTP client connection over SSH-2
	 * @return the number of files successfully copied
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private void GetFiles(String sourceLocation, String targetLocation,
		SFTPv3Client sftpClient,Pattern pattern, Job parentJob) throws Exception 
	{

		String sourceFolder=".";
		if (!Const.isEmpty(sourceLocation)) 
			sourceFolder=sourceLocation + FTPUtils.FILE_SEPARATOR;
		else
			sourceFolder+=FTPUtils.FILE_SEPARATOR;
		
		Vector<SFTPv3DirectoryEntry> filelist = sftpClient.ls(sourceFolder);
		
		if(filelist!=null)
		{
			Iterator<SFTPv3DirectoryEntry> iterator = filelist.iterator();
	
			while (iterator.hasNext() && !parentJob.isStopped()) 
			{
				SFTPv3DirectoryEntry dirEntry = iterator.next();
	
				if (dirEntry == null) continue;
	            
				if (dirEntry.filename.equals(".")
					|| dirEntry.filename.equals("..") || isDirectory(sftpClient, sourceFolder+dirEntry.filename))
					continue;
				
				if(getFileWildcard(dirEntry.filename,pattern))
				{
					// Copy file from remote host
					copyFile(sourceFolder + dirEntry.filename, targetLocation + FTPUtils.FILE_SEPARATOR + dirEntry.filename, sftpClient);
				}
				
			} 
		}
	}  
	 
	/**
	 * copy a directory from the remote host to the local one recursivly.
	 * 
	 * @param sourceLocation the source directory on the remote host
	 * @param targetLocation the target directory on the local host
	 * @param sftpClient is an instance of SFTPv3Client that makes SFTP client connection over SSH-2
	 * @return the number of files successfully copied
	 * @throws Exception
	 */
  	private void copyRecursive(String sourceLocation, String targetLocation,
		SFTPv3Client sftpClient,Pattern pattern,Job parentJob) throws Exception 
	{
		String sourceFolder="."+FTPUtils.FILE_SEPARATOR;
		if (sourceLocation!=null) sourceFolder=sourceLocation;
			
		if (this.isDirectory(sftpClient, sourceFolder)) {	
	        Vector<?> filelist = sftpClient.ls(sourceFolder);
	        Iterator<?> iterator = filelist.iterator();
	
	        while (iterator.hasNext()) {
	
	        SFTPv3DirectoryEntry dirEntry = (SFTPv3DirectoryEntry) iterator .next();
	
	        if (dirEntry == null)   continue;
	        if (dirEntry.filename.equals(".")  || dirEntry.filename.equals(".."))  continue;
	        copyRecursive(sourceFolder + FTPUtils.FILE_SEPARATOR+dirEntry.filename, targetLocation + Const.FILE_SEPARATOR 
	        		+ dirEntry.filename, sftpClient,pattern,parentJob);
        } 
       } else if (isFile(sftpClient, sourceFolder))
       {
    	  if(getFileWildcard(sourceFolder,pattern))
            copyFile(sourceFolder, targetLocation, sftpClient);
       }
  }
	/**
	 * Checks if file is a file
	 * 
	 * @param sftpClient
	 * @param filename
	 * @return true, if filename is a directory
	 */
	public boolean isFile(SFTPv3Client sftpClient, String filename)  
	{
		try 
		{
			return sftpClient.stat(filename).isRegularFile();
		} 
		catch(Exception e)  {}
		return false;
	}
	  
	/**
	 * 
	 * @param sourceLocation
	 * @param targetLocation
	 * @param sftpClient
	 * @return
	 */
	private void copyFile(String sourceLocation, String targetLocation,	SFTPv3Client sftpClient)  
	{
    
		SFTPv3FileHandle sftpFileHandle = null;
		FileOutputStream fos            = null;
		File transferFile               = null;
		long remoteFileSize             = -1;     
		boolean filecopied=true;

		try 
		{
           
			transferFile = new File(targetLocation);
			
			if ((onlyGettingNewFiles == false) ||
	                 (onlyGettingNewFiles == true) && !FileExists(transferFile.getAbsolutePath()))
			{	
				
				new File(transferFile.getParent()).mkdirs();
	            
				remoteFileSize = this.getFileSize(sftpClient, sourceLocation);
			    
				if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobSSH2GET.Log.ReceivingFile",sourceLocation,transferFile.getAbsolutePath(),""+remoteFileSize));              
	            
				sftpFileHandle = sftpClient.openFileRO(sourceLocation);

				fos = null;
				long offset = 0;
	
				fos = new FileOutputStream(transferFile);
				byte[] buffer = new byte[2048];
				while (true) 
				{
					int len = sftpClient.read(sftpFileHandle, offset,buffer, 0, buffer.length);
					if (len <= 0)	break;
					fos.write(buffer, 0, len);
					offset += len;
				}
				fos.flush();
				fos.close();
				fos = null;
	
				nbfilestoget++;
				if (remoteFileSize > 0 && remoteFileSize != transferFile.length())
				{
					filecopied=false;
				    nbrerror++;
					logError(BaseMessages.getString(PKG, "JobSSH2GET.Log.Error.RemoteFileLocalDifferent",""+remoteFileSize,transferFile.length()+"","" + offset));
				}
				else
				{
				    nbgot++;
				    if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobSSH2GET.Log.RemoteFileLocalCopied",sourceLocation,transferFile+""));
				}
			}
			// Let's now delete or move file if needed...
			if(filecopied && !afterFtpPut.equals("do_nothing"))
			{
				deleteOrMoveFiles(sftpClient, sourceLocation,environmentSubstitute(destinationfolder));
			}

		} 
		catch (Exception e) 
		{
			nbrerror++;
			logError(BaseMessages.getString(PKG, "JobSSH2GET.Log.Error.WritingFile",transferFile.getAbsolutePath(),e.getMessage()));
		} 
		finally 
		{
			try { 
					if(sftpFileHandle!=null)
					{
						sftpClient.closeFile(sftpFileHandle); 
						sftpFileHandle = null;
					}
					if (fos != null)
					try 
					{
						fos.close();
						fos = null;
					} 
					catch (Exception ex) 
					{
					}
				} 
			catch(Exception e ) {} 

		}  
	}
	
	private boolean CreateFolder(String filefolder)
	{
		FileObject folder=null;
		try
		{
			folder=	 KettleVFS.getFileObject(filefolder, this);
			
    		if(!folder.exists())	
    		{
    			if(createtargetfolder)
    			{
    				folder.createFolder();
    				if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobSSH2GET.Log.FolderCreated",folder.toString()));
    			}
    			else
    				return false;
    			
    		}
    		return true;
		}
		catch (Exception e) {
			logError(BaseMessages.getString(PKG, "JobSSH2GET.Log.CanNotCreateFolder", folder.toString()));
			
		}
		 finally {
         	if ( folder != null )
         	{
         		try  {
         			folder.close();
         		}
         		catch (Exception ex ) {};
         	}
         }
		 return false;
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
    	boolean retval=false;
    	
    	if(!sshDirectoryExists(sftpClient, foldername))
    	{
    		try
    		{
    			sftpClient.mkdir(foldername, 0700);
    			retval=true;
    			if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobSSH2GET.Log.RemoteFolderCreated",foldername));
    			
    		}catch (Exception e)
    		{
    			logError(BaseMessages.getString(PKG, "JobSSH2GET.Log.Error.CreatingRemoteFolder",foldername));
    		}
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