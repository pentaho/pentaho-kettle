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
 
package org.pentaho.di.job.entries.ftpsput;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.integerValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.ftpsget.FTPSConnection;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.w3c.dom.Node;


/**
 * This defines an FTPS put job entry.
 * 
 * @author Samatar
 * @since 15-03-2010
 *
 */

public class JobEntryFTPSPUT extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private static Class<?> PKG = JobEntryFTPSPUT.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String serverName;
	private String serverPort;
	private String userName;
	private String password;
	private String remoteDirectory;
	private String localDirectory;
	private String wildcard;
	private boolean binaryMode;
	private int     timeout;
	private boolean remove;
    private boolean onlyPuttingNewFiles;  /* Don't overwrite files */
    private boolean activeConnection;
    private String proxyHost;
    
    private String proxyPort;    /* string to allow variable substitution */
     
    private String proxyUsername;
     
    private String proxyPassword;
    
	private int connectionType;
 
	
	public JobEntryFTPSPUT(String n)
	{
		super(n, "");
		serverName=null;
        serverPort="21";
        remoteDirectory=null;
        localDirectory=null;
        connectionType = FTPSConnection.CONNECTION_TYPE_FTP;
		setID(-1L);
	}

	public JobEntryFTPSPUT()
	{
		this("");
	}

    public Object clone()
    {
        JobEntryFTPSPUT je = (JobEntryFTPSPUT) super.clone();
        return je;
    }
    
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(200);
		
		retval.append(super.getXML());
		
		retval.append("      ").append(XMLHandler.addTagValue("servername",   serverName));
		retval.append("      ").append(XMLHandler.addTagValue("serverport",   serverPort));
		retval.append("      ").append(XMLHandler.addTagValue("username",     userName));
	    retval.append("      ").append(XMLHandler.addTagValue("password", Encr.encryptPasswordIfNotUsingVariables(getPassword())));
		retval.append("      ").append(XMLHandler.addTagValue("remoteDirectory", remoteDirectory));
		retval.append("      ").append(XMLHandler.addTagValue("localDirectory", localDirectory));
		retval.append("      ").append(XMLHandler.addTagValue("wildcard",     wildcard));
		retval.append("      ").append(XMLHandler.addTagValue("binary",       binaryMode));
		retval.append("      ").append(XMLHandler.addTagValue("timeout",      timeout));
		retval.append("      ").append(XMLHandler.addTagValue("remove",       remove));
        retval.append("      ").append(XMLHandler.addTagValue("only_new",     onlyPuttingNewFiles));
        retval.append("      ").append(XMLHandler.addTagValue("active",       activeConnection));
        
	    retval.append("      ").append(XMLHandler.addTagValue("proxy_host", proxyHost)); //$NON-NLS-1$ //$NON-NLS-2$
	    retval.append("      ").append(XMLHandler.addTagValue("proxy_port", proxyPort)); //$NON-NLS-1$ //$NON-NLS-2$
	    retval.append("      ").append(XMLHandler.addTagValue("proxy_username", proxyUsername)); //$NON-NLS-1$ //$NON-NLS-2$
	    retval.append("      ").append(XMLHandler.addTagValue("proxy_password", proxyPassword)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("connection_type",FTPSConnection.getConnectionTypeCode(connectionType)));
		
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
	  {
		try
		{
			super.loadXML(entrynode, databases, slaveServers);
			serverName      = XMLHandler.getTagValue(entrynode, "servername");
			serverPort      = XMLHandler.getTagValue(entrynode, "serverport");
			userName        = XMLHandler.getTagValue(entrynode, "username");
			password = Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(entrynode, "password")); 
			remoteDirectory   = XMLHandler.getTagValue(entrynode, "remoteDirectory");
			localDirectory = XMLHandler.getTagValue(entrynode, "localDirectory");
			wildcard        = XMLHandler.getTagValue(entrynode, "wildcard");
			binaryMode          = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "binary") );
			timeout             = Const.toInt(XMLHandler.getTagValue(entrynode, "timeout"), 10000);
			remove              = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "remove") );
			onlyPuttingNewFiles = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "only_new") );
            activeConnection    = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "active") );
 
		    proxyHost = XMLHandler.getTagValue(entrynode, "proxy_host"); //$NON-NLS-1$
		    proxyPort = XMLHandler.getTagValue(entrynode, "proxy_port"); //$NON-NLS-1$
		    proxyUsername = XMLHandler.getTagValue(entrynode, "proxy_username"); //$NON-NLS-1$
		    proxyPassword = XMLHandler.getTagValue(entrynode, "proxy_password"); //$NON-NLS-1$
		    connectionType = FTPSConnection.getConnectionTypeByCode(Const.NVL(XMLHandler.getTagValue(entrynode,	"connection_type"), ""));   
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "JobFTPSPUT.Log.UnableToLoadFromXml"), xe);			
		}
	}

	public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	 {
		try
		{
			serverName      = rep.getJobEntryAttributeString(id_jobentry, "servername");
			int intServerPort = (int)rep.getJobEntryAttributeInteger(id_jobentry, "serverport");
            serverPort = rep.getJobEntryAttributeString(id_jobentry, "serverport"); // backward compatible.
            if (intServerPort>0 && Const.isEmpty(serverPort)) serverPort = Integer.toString(intServerPort);

			userName        = rep.getJobEntryAttributeString(id_jobentry, "username");
			password = Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString(id_jobentry, "password") );
			remoteDirectory   = rep.getJobEntryAttributeString(id_jobentry, "remoteDirectory");
			localDirectory = rep.getJobEntryAttributeString(id_jobentry, "localDirectory");
			wildcard        = rep.getJobEntryAttributeString(id_jobentry, "wildcard");
			binaryMode          = rep.getJobEntryAttributeBoolean(id_jobentry, "binary");
			timeout             = (int)rep.getJobEntryAttributeInteger(id_jobentry, "timeout");
            remove              = rep.getJobEntryAttributeBoolean(id_jobentry, "remove");
            onlyPuttingNewFiles = rep.getJobEntryAttributeBoolean(id_jobentry, "only_new");
            activeConnection    = rep.getJobEntryAttributeBoolean(id_jobentry, "active");

            
		    proxyHost	= rep.getJobEntryAttributeString(id_jobentry, "proxy_host"); //$NON-NLS-1$
		    proxyPort	= rep.getJobEntryAttributeString(id_jobentry, "proxy_port"); //$NON-NLS-1$
		    proxyUsername	= rep.getJobEntryAttributeString(id_jobentry, "proxy_username"); //$NON-NLS-1$
		    proxyPassword = rep.getJobEntryAttributeString(id_jobentry, "proxy_password"); //$NON-NLS-1$
		    connectionType = FTPSConnection.getConnectionTypeByCode(Const.NVL(rep.getJobEntryAttributeString(id_jobentry,"connection_type"), ""));
		}
		catch(KettleException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "JobFTPSPUT.UnableToLoadFromRepo", String.valueOf(id_jobentry)), dbe);
		}
	}
	
	public void saveRep(Repository rep, ObjectId id_job) throws KettleException
	{
		try
		{
			rep.saveJobEntryAttribute(id_job, getObjectId(), "servername",      serverName);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "serverport",      serverPort);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "username",        userName);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "password", Encr.encryptPasswordIfNotUsingVariables(password));
			rep.saveJobEntryAttribute(id_job, getObjectId(), "remoteDirectory",    remoteDirectory);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "localDirectory", localDirectory);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "wildcard",        wildcard);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "binary",          binaryMode);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "timeout",         timeout);
            rep.saveJobEntryAttribute(id_job, getObjectId(), "remove",          remove);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "only_new",        onlyPuttingNewFiles);
            rep.saveJobEntryAttribute(id_job, getObjectId(), "active",          activeConnection);
            
		    rep.saveJobEntryAttribute(id_job, getObjectId(), "proxy_host", proxyHost); //$NON-NLS-1$
		    rep.saveJobEntryAttribute(id_job, getObjectId(), "proxy_port", proxyPort); //$NON-NLS-1$
		    rep.saveJobEntryAttribute(id_job, getObjectId(), "proxy_username", proxyUsername); //$NON-NLS-1$
		    rep.saveJobEntryAttribute(id_job, getObjectId(), "proxy_password", proxyPassword); //$NON-NLS-1$
		    rep.saveJobEntryAttribute(id_job, getObjectId(),"connection_type", FTPSConnection.getConnectionType(connectionType));
			
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "JobFTPSPUT.UnableToSaveToRepo", String.valueOf(id_job)), dbe); //$NON-NLS-1$
		}
	}
	
	/**
	 * @return Returns the binaryMode.
	 */
	public boolean isBinaryMode()
	{
		return binaryMode;
	}

	/**
	 * @param binaryMode The binaryMode to set.
	 */
	public void setBinaryMode(boolean binaryMode)
	{
		this.binaryMode = binaryMode;
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
	/**
     * @return Returns the onlyGettingNewFiles.
     */
    public boolean isOnlyPuttingNewFiles()
    {
        return onlyPuttingNewFiles;
    }

    /**
     * @param onlyPuttingNewFiles Only transfer new files to the remote host
     */
    public void setOnlyPuttingNewFiles(boolean onlyPuttingNewFiles)
    {
        this.onlyPuttingNewFiles = onlyPuttingNewFiles;
    }
    
	/**
	 * @return Returns the remoteDirectory.
	 */
	public String getRemoteDirectory()
	{
		return remoteDirectory;
	}
	
	/**
	 * @param directory The remoteDirectory to set.
	 */
	public void setRemoteDirectory(String directory)
	{
		this.remoteDirectory = directory;
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
	public String getLocalDirectory()
	{
		return localDirectory;
	}
	
	/**
	 * @param directory The localDirectory to set.
	 */
	public void setLocalDirectory(String directory)
	{
		this.localDirectory = directory;
	}
	
	/**
	 * @param remove The remove to set.
	 */
	public void setRemove(boolean remove)
	{
		this.remove = remove;
	}
	
	/**
	 * @return Returns the remove.
	 */
	public boolean getRemove()
	{
		return remove;
	}
	
	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}
	
	/**
     * @return the activeConnection
     */
    public boolean isActiveConnection()
    {
        return activeConnection;
    }

    /**
     * @param activeConnection set to true to get an active FTP connection
     */
    public void setActiveConnection(boolean activeConnection)
    {
        this.activeConnection = activeConnection;
    }

    /**
     * @return Returns the hostname of the ftp-proxy.
     */
    public String getProxyHost() 
    {
    	return proxyHost;
    }
      
    /**
     * @param proxyHost The hostname of the proxy.
     */
    public void setProxyHost(String proxyHost) 
    {
     	this.proxyHost = proxyHost;
    }
    
    /**
     * @return Returns the password which is used to authenticate at the proxy.
     */
    public String getProxyPassword() 
    {
     	return proxyPassword;
    }
    
    /**
     * @param proxyPassword The password which is used to authenticate at the proxy.
     */
    public void setProxyPassword(String proxyPassword) 
    {
     	this.proxyPassword = proxyPassword;
    }

    /**
     * @return Returns the port of the ftp-proxy.
     */
    public String getProxyPort() 
    {
      return proxyPort;
    }
    /**
     * @return the conenction type
     */
    public int getConnectionType() 
    {
    	return connectionType;
    }
    

    /**
     * @param connectionType the connectionType to set
     */
    public void setConnectionType(int type)
    {
    	connectionType = type;
    }


    /**
     * @param proxyPort The port of the ftp-proxy. 
     */
    public void setProxyPort(String proxyPort) 
    {
      this.proxyPort = proxyPort;
    }
      
    /**
     * @return Returns the username which is used to authenticate at the proxy.
     */
    public String getProxyUsername() {
      return proxyUsername;
    }
      
    /**
     * @param proxyUsername The username which is used to authenticate at the proxy.
     */
    public void setProxyUsername(String proxyUsername) {
    	this.proxyUsername = proxyUsername;
    }
    
	public Result execute(Result previousResult, int nr)
	{
        Result result = previousResult;
		result.setResult( false );
		long filesput = 0;

		if(isDetailed())	
			logDetailed(BaseMessages.getString(PKG, "JobFTPSPUT.Log.Starting"));
        
        // String substitution..
        String realServerName      = environmentSubstitute(serverName);
        String realServerPort      = environmentSubstitute(serverPort);
        String realUsername        = environmentSubstitute(userName);
        String realPassword        = environmentSubstitute(password);
        String realRemoteDirectory = environmentSubstitute(remoteDirectory);
        String realWildcard        = environmentSubstitute(wildcard);
        String realLocalDirectory  = environmentSubstitute(localDirectory);
        
        
        FTPSConnection connection=null;
        
		try {
			// Create FTPS client to host:port ... 
            int realPort = Const.toInt(environmentSubstitute(realServerPort), 0);
            // Define a new connection
            connection = new FTPSConnection(getConnectionType(), realServerName,realPort, realUsername, realPassword); 
            
            if (!Const.isEmpty(proxyHost))  {
            	  String realProxy_host = environmentSubstitute(proxyHost);
            	  String realProxy_username = environmentSubstitute(proxyUsername);
            	  String realProxy_password = environmentSubstitute(proxyPassword);
            	  connection.setProxyHost(realProxy_host);
            	  if(!Const.isEmpty(realProxy_username)) {
            		  connection.setProxyUser(realProxy_username);
            	  }
            	  if(!Const.isEmpty(realProxy_password)) {
            		  connection.setProxyPassword(realProxy_password);
            	  }
            	  if ( isDetailed() )
            	      logDetailed(BaseMessages.getString(PKG, "JobEntryFTPSPUT.OpenedProxyConnectionOn",realProxy_host));
  	  
            	  int proxyport = Const.toInt(environmentSubstitute(proxyPort), 21);
            	  if (proxyport != 0) {
            		connection.setProxyPort(proxyport);
            	  }
              } else  {
                  if ( isDetailed() )
            	      logDetailed( BaseMessages.getString(PKG, "JobEntryFTPSPUT.OpenedConnectionTo",realServerName));                
              }
              
            
          
			// set activeConnection connectmode ...            
            if (activeConnection){
                connection.setPassiveMode(false);
                if(isDetailed()) logDetailed( BaseMessages.getString(PKG, "JobFTPSPUT.Log.SetActiveConnection")); //$NON-NLS-1$
            } else{
            	connection.setPassiveMode(true);
                if(isDetailed()) logDetailed( BaseMessages.getString(PKG, "JobFTPSPUT.Log.SetPassiveConnection")); //$NON-NLS-1$
            }
            
            // Set binary mode
            if(isBinaryMode()) {
            	connection.setBinaryMode(true);
            	if(isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobFTPSPUT.Log.BinaryMod"));
            }
            
			// Set the timeout
            connection.setTimeOut(timeout);
		    if(isDetailed()) logDetailed( BaseMessages.getString(PKG, "JobFTPSPUT.Log.SetTimeout", timeout)); //$NON-NLS-1$

			// login to FTPS host ...
            connection.connect();
            if(isDetailed()) {
    			// Remove password from logging, you don't know where it ends up.
            	logDetailed( BaseMessages.getString(PKG, "JobFTPSPUT.Log.Logged", realUsername));
            	logDetailed( BaseMessages.getString(PKG, "JobFTPSPUT.WorkingDirectory", connection.getWorkingDirectory()));
            }
			
			// move to spool dir ...
			if (!Const.isEmpty(realRemoteDirectory)) {
                connection.changeDirectory(realRemoteDirectory);
                if(isDetailed()) logDetailed( BaseMessages.getString(PKG, "JobFTPSPUT.Log.ChangedDirectory", realRemoteDirectory)); //$NON-NLS-1$
			}
			realRemoteDirectory=Const.NVL(realRemoteDirectory, FTPSConnection.HOME_FOLDER);

			ArrayList<String> myFileList = new ArrayList<String>();
			File localFiles = new File(realLocalDirectory);
			File[] children = localFiles.listFiles();
			for (int i=0; i<children.length; i++) {
	            // Get filename of file or directory
				if (!children[i].isDirectory()) {
					myFileList.add(children[i].getName());
				}
			}
			
			String[] filelist = new String[myFileList.size()];
			myFileList.toArray(filelist);
			
			
			if (isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobFTPSPUT.Log.FoundFileLocalDirectory",filelist.length,realLocalDirectory));
			
			Pattern pattern = null;
			if (!Const.isEmpty(realWildcard)) {
				pattern = Pattern.compile(realWildcard);
				
			} // end if
			
			
			// Get the files in the list and execute put each file in the FTP
			for (int i=0;i<filelist.length && !parentJob.isStopped();i++) {
				boolean getIt = true;
				
				// First see if the file matches the regular expression!
				if (pattern!=null) {
					Matcher matcher = pattern.matcher(filelist[i]);
					getIt = matcher.matches();
				}
				
				if (getIt) {				
					// File exists?
					boolean fileExist= connection.isFileExists(filelist[i]);
					
					if (isDebug()) {
						if(fileExist)
							logDebug(BaseMessages.getString(PKG, "JobFTPSPUT.Log.FileExists",filelist[i]));
						else
							logDebug(BaseMessages.getString(PKG, "JobFTPSPUT.Log.FileDoesNotExists",filelist[i]));
					}
					
					if (!fileExist || (!onlyPuttingNewFiles && fileExist)) {

						String localFilename = realLocalDirectory+Const.FILE_SEPARATOR+filelist[i]; 
						if (isDebug()) logDebug(BaseMessages.getString(PKG, "JobFTPSPUT.Log.PuttingFileToRemoteDirectory",localFilename,realRemoteDirectory));
						
						connection.uploadFile(localFilename, filelist[i]);
						
						filesput++;
					
						// Delete the file if this is needed!
						if (remove) {
							new File(localFilename).delete();
							if (isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobFTPSPUT.Log.DeletedFile",localFilename));
						}
					}
				}
			}
		
			result.setResult( true );
			result.setNrLinesOutput(filesput);
			if (isDebug()) logDebug(BaseMessages.getString(PKG, "JobFTPSPUT.Log.WeHavePut",filesput));
		} catch(Exception e) {
			result.setNrErrors(1);
			logError(BaseMessages.getString(PKG, "JobFTPSPUT.Log.ErrorPuttingFiles",e.getMessage()));
            logError(Const.getStackTracker(e));
		} finally {
			 if (connection!=null) {
                try
                {
                	connection.disconnect();
                } catch(Exception e) {
                    logError(BaseMessages.getString(PKG, "JobFTPSPUT.Log.ErrorQuitingFTP",e.getMessage()));
                }
            }
		}
		
		return result;
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