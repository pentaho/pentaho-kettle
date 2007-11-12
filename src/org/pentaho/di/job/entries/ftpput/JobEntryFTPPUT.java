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

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.enterprisedt.net.ftp.FTPException;

/**
 * This defines an FTP put job entry.
 * 
 * @author Samatar
 * @since 15-09-2007
 *
 */
public class JobEntryFTPPUT extends JobEntryBase implements Cloneable, JobEntryInterface
{
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
		setJobEntryType(JobEntryType.FTP_PUT);
		setControlEncoding(DEFAULT_CONTROL_ENCODING);
	}

	public JobEntryFTPPUT()
	{
		this("");
	}

	public JobEntryFTPPUT(JobEntryBase jeb)
	{
		super(jeb);
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
		retval.append("      ").append(XMLHandler.addTagValue("password",     password));
		retval.append("      ").append(XMLHandler.addTagValue("remoteDirectory", remoteDirectory));
		retval.append("      ").append(XMLHandler.addTagValue("localDirectory", localDirectory));
		retval.append("      ").append(XMLHandler.addTagValue("wildcard",     wildcard));
		retval.append("      ").append(XMLHandler.addTagValue("binary",       binaryMode));
		retval.append("      ").append(XMLHandler.addTagValue("timeout",      timeout));
		retval.append("      ").append(XMLHandler.addTagValue("remove",       remove));
        retval.append("      ").append(XMLHandler.addTagValue("only_new",     onlyPuttingNewFiles));
        retval.append("      ").append(XMLHandler.addTagValue("active",       activeConnection));
        retval.append("      ").append(XMLHandler.addTagValue("control_encoding",  controlEncoding));
		
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
			password        = XMLHandler.getTagValue(entrynode, "password");
			remoteDirectory   = XMLHandler.getTagValue(entrynode, "remoteDirectory");
			localDirectory = XMLHandler.getTagValue(entrynode, "localDirectory");
			wildcard        = XMLHandler.getTagValue(entrynode, "wildcard");
			binaryMode          = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "binary") );
			timeout             = Const.toInt(XMLHandler.getTagValue(entrynode, "timeout"), 10000);
			remove              = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "remove") );
			onlyPuttingNewFiles = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "only_new") );
            activeConnection    = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "active") );
            controlEncoding     = XMLHandler.getTagValue(entrynode, "control_encoding");
            if ( controlEncoding == null )
            {
            	// if we couldn't retrieve an encoding, assume it's an old instance and
            	// put in the the encoding used before v 2.4.0
            	controlEncoding = LEGACY_CONTROL_ENCODING;
            }       ;
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException(Messages.getString("JobFTPPUT.Log.UnableToLoadFromXml"), xe);			
		}
	}

	  public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
	  {
		try
		{
			super.loadRep(rep, id_jobentry, databases, slaveServers);
			serverName      = rep.getJobEntryAttributeString(id_jobentry, "servername");
			int intServerPort = (int)rep.getJobEntryAttributeInteger(id_jobentry, "serverport");
            serverPort = rep.getJobEntryAttributeString(id_jobentry, "serverport"); // backward compatible.
            if (intServerPort>0 && Const.isEmpty(serverPort)) serverPort = Integer.toString(intServerPort);

			userName        = rep.getJobEntryAttributeString(id_jobentry, "username");
			password        = rep.getJobEntryAttributeString(id_jobentry, "password");
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
		}
		catch(KettleException dbe)
		{
			throw new KettleException(Messages.getString("JobFTPPUT.UnableToLoadFromRepo", String.valueOf(id_jobentry)), dbe);
		}
	}
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			rep.saveJobEntryAttribute(id_job, getID(), "servername",      serverName);
			rep.saveJobEntryAttribute(id_job, getID(), "serverport",      serverPort);
			rep.saveJobEntryAttribute(id_job, getID(), "username",        userName);
			rep.saveJobEntryAttribute(id_job, getID(), "password",        password);
			rep.saveJobEntryAttribute(id_job, getID(), "remoteDirectory",    remoteDirectory);
			rep.saveJobEntryAttribute(id_job, getID(), "localDirectory", localDirectory);
			rep.saveJobEntryAttribute(id_job, getID(), "wildcard",        wildcard);
			rep.saveJobEntryAttribute(id_job, getID(), "binary",          binaryMode);
			rep.saveJobEntryAttribute(id_job, getID(), "timeout",         timeout);
            rep.saveJobEntryAttribute(id_job, getID(), "remove",          remove);
			rep.saveJobEntryAttribute(id_job, getID(), "only_new",        onlyPuttingNewFiles);
            rep.saveJobEntryAttribute(id_job, getID(), "active",          activeConnection);
            rep.saveJobEntryAttribute(id_job, getID(), "control_encoding",controlEncoding);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException(Messages.getString("JobFTPPUT.UnableToSaveToRepo", String.valueOf(id_job)), dbe); //$NON-NLS-1$
			   
		
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
     * @param onlyGettingNewFiles The onlyGettingNewFiles to set.
     */
    public void setOnlyPuttingNewFiles(boolean onlyPuttingNewFilesin)
    {
        this.onlyPuttingNewFiles = onlyPuttingNewFilesin;
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
     * @param activeConnection the activeConnection to set
     */
    public void setActiveConnection(boolean passive)
    {
        this.activeConnection = passive;
    }
	
	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();

        Result result = previousResult;
		result.setResult( false );
		long filesput = 0;


		log.logDetailed(toString(), Messages.getString("JobFTPPUT.Log.Starting"));
        
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
            ftpclient.setRemotePort(Const.toInt(realServerPort, 21));
            
            if (log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobFTPPUT.Log.OpenConnection",realServerName));

			// set activeConnection connectmode ...
            if (activeConnection)
            {
                ftpclient.setConnectMode(FTPConnectMode.ACTIVE);
                if (log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobFTPPUT.Log.SetActiveConnection"));
            }
            else
            {
                ftpclient.setConnectMode(FTPConnectMode.PASV);
                if (log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobFTPPUT.Log.SetPassiveConnection"));
            }
			
        	// Set the timeout
            if (timeout>0) 
            {
				ftpclient.setTimeout(timeout);
				if (log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobFTPPUT.Log.SetTimeout",""+timeout));
            }
            
			ftpclient.setControlEncoding(controlEncoding);
			if (log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobFTPPUT.Log.SetEncoding",controlEncoding));

			// login to ftp host ...
            ftpclient.connect();
			ftpclient.login(realUsername, realPassword);
				
            // set BINARY
            if (binaryMode) 
            {
            	ftpclient.setType(FTPTransferType.BINARY);
            	if (log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobFTPPUT.Log.BinaryMode"));
            }
			
			//  Remove password from logging, you don't know where it ends up.
			if (log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobFTPPUT.Log.Logged",realUsername));

			// move to spool dir ...
			if (!Const.isEmpty(realRemoteDirectory))
			{
				ftpclient.chdir(realRemoteDirectory);
				if (log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobFTPPUT.Log.ChangedDirectory",realRemoteDirectory));
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
			
			
			if (log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobFTPPUT.Log.FoundFileLocalDirectory",""+filelist.length,realLocalDirectory));
			
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
					catch (FTPException e){
						// Assume file does not exist !!
					}
					
					if (log.isDebug()) 
					{
						if(fileExist)
							log.logDebug(toString(),Messages.getString("JobFTPPUT.Log.FileExists",filelist[i]));
						else
							log.logDebug(toString(),Messages.getString("JobFTPPUT.Log.FileDoesNotExists",filelist[i]));
					}
					
					if (!fileExist || (!onlyPuttingNewFiles && fileExist))
					{
						if (log.isDebug()) log.logDebug(toString(), Messages.getString("JobFTPPUT.Log.PuttingFileToRemoteDirectory",filelist[i],realRemoteDirectory));
						
						String localFilename = realLocalDirectory+Const.FILE_SEPARATOR+filelist[i]; 
						ftpclient.put(localFilename, filelist[i]);
						
						filesput++;
					
						// Delete the file if this is needed!
						if (remove) 
						{
							children[i].delete();
							if (log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobFTPPUT.Log.DeletedFile",filelist[i]));
						}
					}
				}
			}
		
			result.setResult( true );
			if (log.isDetailed()) log.logDebug(toString(), Messages.getString("JobFTPPUT.Log.WeHavePut",""+filesput));
		}
		catch(Exception e)
		{
			result.setNrErrors(1);
			log.logError(toString(), Messages.getString("JobFTPPUT.Log.ErrorPuttingFiles",e.getMessage()));
            log.logError(toString(), Const.getStackTracker(e));
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
	                    log.logError(toString(), Messages.getString("JobFTPPUT.Log.ErrorQuitingFTP",e.getMessage()));
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