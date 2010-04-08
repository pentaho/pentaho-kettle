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
 
package org.pentaho.di.job.entries.ftpput;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.integerValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.io.File;
import java.net.InetAddress;
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
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.w3c.dom.Node;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPTransferType;

/**
 * This defines an FTP put job entry.
 * 
 * @author Samatar
 * @since 15-09-2007
 *
 */

public class JobEntryFTPPUT extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private static Class<?> PKG = JobEntryFTPPUT.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

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
    private String  controlEncoding;      /* how to convert list of filenames e.g. */
    private String proxyHost;
    
    private String proxyPort;    /* string to allow variable substitution */
     
    private String proxyUsername;
     
    private String proxyPassword;
    
    /**
     * Implicit encoding used before PDI v2.4.1
     */
    static private String LEGACY_CONTROL_ENCODING = "US-ASCII";
    
    /**
     * Default encoding when making a new ftp job entry instance.
     */
    static private String DEFAULT_CONTROL_ENCODING = "ISO-8859-1";   
	
	public JobEntryFTPPUT(String n)
	{
		super(n, "");
		serverName=null;
        serverPort="21";
        remoteDirectory=null;
        localDirectory=null;
		setID(-1L);
		setControlEncoding(DEFAULT_CONTROL_ENCODING);
	}

	public JobEntryFTPPUT()
	{
		this("");
	}

    public Object clone()
    {
        JobEntryFTPPUT je = (JobEntryFTPPUT) super.clone();
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
        retval.append("      ").append(XMLHandler.addTagValue("control_encoding",  controlEncoding));
        
	    retval.append("      ").append(XMLHandler.addTagValue("proxy_host", proxyHost)); //$NON-NLS-1$ //$NON-NLS-2$
	    retval.append("      ").append(XMLHandler.addTagValue("proxy_port", proxyPort)); //$NON-NLS-1$ //$NON-NLS-2$
	    retval.append("      ").append(XMLHandler.addTagValue("proxy_username", proxyUsername)); //$NON-NLS-1$ //$NON-NLS-2$
	    retval.append("      ").append(XMLHandler.addTagValue("proxy_password", proxyPassword)); //$NON-NLS-1$ //$NON-NLS-2$
	    
		
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
            controlEncoding     = XMLHandler.getTagValue(entrynode, "control_encoding");
            
		    proxyHost = XMLHandler.getTagValue(entrynode, "proxy_host"); //$NON-NLS-1$
		    proxyPort = XMLHandler.getTagValue(entrynode, "proxy_port"); //$NON-NLS-1$
		    proxyUsername = XMLHandler.getTagValue(entrynode, "proxy_username"); //$NON-NLS-1$
		    proxyPassword = XMLHandler.getTagValue(entrynode, "proxy_password"); //$NON-NLS-1$
            
            if ( controlEncoding == null )
            {
            	// if we couldn't retrieve an encoding, assume it's an old instance and
            	// put in the the encoding used before v 2.4.0
            	controlEncoding = LEGACY_CONTROL_ENCODING;
            } 
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "JobFTPPUT.Log.UnableToLoadFromXml"), xe);			
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
            controlEncoding     = rep.getJobEntryAttributeString(id_jobentry, "control_encoding");
            if ( controlEncoding == null )
            {
            	// if we couldn't retrieve an encoding, assume it's an old instance and
            	// put in the the encoding used before v 2.4.0
            	controlEncoding = LEGACY_CONTROL_ENCODING;
            }
            
		    proxyHost	= rep.getJobEntryAttributeString(id_jobentry, "proxy_host"); //$NON-NLS-1$
		    proxyPort	= rep.getJobEntryAttributeString(id_jobentry, "proxy_port"); //$NON-NLS-1$
		    proxyUsername	= rep.getJobEntryAttributeString(id_jobentry, "proxy_username"); //$NON-NLS-1$
		    proxyPassword = rep.getJobEntryAttributeString(id_jobentry, "proxy_password"); //$NON-NLS-1$
		}
		catch(KettleException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "JobFTPPUT.UnableToLoadFromRepo", String.valueOf(id_jobentry)), dbe);
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
            rep.saveJobEntryAttribute(id_job, getObjectId(), "control_encoding",controlEncoding);
            
		    rep.saveJobEntryAttribute(id_job, getObjectId(), "proxy_host", proxyHost); //$NON-NLS-1$
		    rep.saveJobEntryAttribute(id_job, getObjectId(), "proxy_port", proxyPort); //$NON-NLS-1$
		    rep.saveJobEntryAttribute(id_job, getObjectId(), "proxy_username", proxyUsername); //$NON-NLS-1$
		    rep.saveJobEntryAttribute(id_job, getObjectId(), "proxy_password", proxyPassword); //$NON-NLS-1$
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(BaseMessages.getString(PKG, "JobFTPPUT.UnableToSaveToRepo", String.valueOf(id_job)), dbe); //$NON-NLS-1$
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
     * Get the control encoding to be used for ftp'ing
     * 
     * @return the used encoding
     */
    public String getControlEncoding()
    {
        return controlEncoding;
    }
    
    /**
     *  Set the encoding to be used for ftp'ing. This determines how
     *  names are translated in dir e.g. It does impact the contents
     *  of the files being ftp'ed.
     *  
     *  @param encoding The encoding to be used.
     */
    public void setControlEncoding(String encoding)
    {
    	this.controlEncoding = encoding;
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

		if(log.isDetailed())	
			logDetailed(BaseMessages.getString(PKG, "JobFTPPUT.Log.Starting"));
        
        // String substitution..
        String realServerName      = environmentSubstitute(serverName);
        String realServerPort      = environmentSubstitute(serverPort);
        String realUsername        = environmentSubstitute(userName);
        String realPassword        = environmentSubstitute(password);
        String realRemoteDirectory = environmentSubstitute(remoteDirectory);
        String realWildcard        = environmentSubstitute(wildcard);
        String realLocalDirectory  = environmentSubstitute(localDirectory);
        
        
        FTPClient ftpclient=null;
        
		try
		{
			// Create ftp client to host:port ...
			ftpclient = new FTPClient();
            ftpclient.setRemoteAddr(InetAddress.getByName(realServerName));
            if(!Const.isEmpty(realServerPort))
            {
            	 ftpclient.setRemotePort(Const.toInt(realServerPort, 21));
            }
            
            if (!Const.isEmpty(proxyHost)) 
            {
          	  String realProxy_host = environmentSubstitute(proxyHost);
          	  ftpclient.setRemoteAddr(InetAddress.getByName(realProxy_host));
          	  if ( log.isDetailed() )
          	      logDetailed(BaseMessages.getString(PKG, "JobEntryFTPPUT.OpenedProxyConnectionOn",realProxy_host));

          	  // FIXME: Proper default port for proxy    	  
          	  int port = Const.toInt(environmentSubstitute(proxyPort), 21);
          	  if (port != 0) 
          	  {
          	     ftpclient.setRemotePort(port);
          	  }
            } 
            else 
            {
                ftpclient.setRemoteAddr(InetAddress.getByName(realServerName));
                
                if ( log.isDetailed() )
          	      logDetailed(BaseMessages.getString(PKG, "JobEntryFTPPUT.OpenConnection",realServerName));                
            }
            

			// set activeConnection connectmode ...
            if (activeConnection)
            {
                ftpclient.setConnectMode(FTPConnectMode.ACTIVE);
                if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobFTPPUT.Log.SetActiveConnection"));
            }
            else
            {
                ftpclient.setConnectMode(FTPConnectMode.PASV);
                if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobFTPPUT.Log.SetPassiveConnection"));
            }
			
        	// Set the timeout
            if (timeout>0) 
            {
				ftpclient.setTimeout(timeout);
				if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobFTPPUT.Log.SetTimeout",""+timeout));
            }
            
			ftpclient.setControlEncoding(controlEncoding);
			if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobFTPPUT.Log.SetEncoding",controlEncoding));

			// login to ftp host ...
            ftpclient.connect();
			ftpclient.login(realUsername, realPassword);
				
            // set BINARY
            if (binaryMode) 
            {
            	ftpclient.setType(FTPTransferType.BINARY);
            	if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobFTPPUT.Log.BinaryMode"));
            }
			
			//  Remove password from logging, you don't know where it ends up.
			if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobFTPPUT.Log.Logged",realUsername));

			// move to spool dir ...
			if (!Const.isEmpty(realRemoteDirectory))
			{
				ftpclient.chdir(realRemoteDirectory);
				if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobFTPPUT.Log.ChangedDirectory",realRemoteDirectory));
			}
			
			// Get all the files in the local directory...
			int x = 0;
			
			// Joerg:  ..that's for Java5 
			// ArrayList<String> myFileList = new ArrayList<String>();
			ArrayList<String> myFileList = new ArrayList<String>();
			
			
			File localFiles = new File(realLocalDirectory);
			File[] children = localFiles.listFiles();
			for (int i=0; i<children.length; i++) {
	            // Get filename of file or directory
				if (!children[i].isDirectory()) {
					// myFileList.add(children[i].getAbsolutePath());
					myFileList.add(children[i].getName());
					x = x+1;
					
				}
	        } // end for
			
			// Joerg:  ..that's for Java5
			// String[] filelist = myFileList.toArray(new String[myFileList.size()]);
			
			String[] filelist = new String[myFileList.size()];
			myFileList.toArray(filelist);
			
			
			if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobFTPPUT.Log.FoundFileLocalDirectory",""+filelist.length,realLocalDirectory));
			
			Pattern pattern = null;
			if (!Const.isEmpty(realWildcard)) 
			{
				pattern = Pattern.compile(realWildcard);
				
			} // end if
			
			
			// Get the files in the list and execute ftp.put() for each file
			for (int i=0;i<filelist.length && !parentJob.isStopped();i++)
			{
				boolean getIt = true;
				
				// First see if the file matches the regular expression!
				if (pattern!=null)
				{
					Matcher matcher = pattern.matcher(filelist[i]);
					getIt = matcher.matches();
				}
				
				if (getIt)
				{
					
					// File exists?
					boolean fileExist=false;
					try
					{
						fileExist=ftpclient.exists(filelist[i]);
						
					}
					catch (Exception e){
						// Assume file does not exist !!
					}
					
					if (log.isDebug()) 
					{
						if(fileExist)
							logDebug(BaseMessages.getString(PKG, "JobFTPPUT.Log.FileExists",filelist[i]));
						else
							logDebug(BaseMessages.getString(PKG, "JobFTPPUT.Log.FileDoesNotExists",filelist[i]));
					}
					
					if (!fileExist || (!onlyPuttingNewFiles && fileExist))
					{
						if (log.isDebug()) logDebug(BaseMessages.getString(PKG, "JobFTPPUT.Log.PuttingFileToRemoteDirectory",filelist[i],realRemoteDirectory));
						
						String localFilename = realLocalDirectory+Const.FILE_SEPARATOR+filelist[i]; 
						ftpclient.put(localFilename, filelist[i]);
						
						filesput++;
					
						// Delete the file if this is needed!
						if (remove) 
						{
							new File(localFilename).delete();
							if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobFTPPUT.Log.DeletedFile",localFilename));
						}
					}
				}
			}
		
			result.setResult( true );
			if (log.isDetailed()) logDebug(BaseMessages.getString(PKG, "JobFTPPUT.Log.WeHavePut",""+filesput));
		}
		catch(Exception e)
		{
			result.setNrErrors(1);
			logError(BaseMessages.getString(PKG, "JobFTPPUT.Log.ErrorPuttingFiles",e.getMessage()));
            logError(Const.getStackTracker(e));
		} finally 
		{
			 if (ftpclient!=null && ftpclient.connected())
	            {
	                try
	                {
	                    ftpclient.quit();
	                }
	                catch(Exception e)
	                {
	                    logError(BaseMessages.getString(PKG, "JobFTPPUT.Log.ErrorQuitingFTP",e.getMessage()));
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