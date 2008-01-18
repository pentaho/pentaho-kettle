 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
package be.ibridge.kettle.job.entry.ftpput;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.enterprisedt.net.ftp.FTPException;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.JobEntryBase;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;

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
		setID(-1L);
		setType(JobEntryInterface.TYPE_JOBENTRY_FTPPUT);
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
	
	public void loadXML(Node entrynode, ArrayList databases, Repository rep) throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
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
			throw new KettleXMLException("Unable to load job entry of type 'SFTP' from XML node", xe);			
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
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
			throw new KettleException("Unable to load job entry of type 'SFTP' from the repository for id_jobentry="+id_jobentry, dbe);
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
			throw new KettleException("Unable to save job entry of type 'SFTP' to the repository for id_job="+id_job, dbe);
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
	public void setRemoteDirectory(String remoteDirectoryin)
	{
		this.remoteDirectory = remoteDirectoryin;
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
	 * @param localDirectory The localDirectory to set.
	 */
	public void setLocalDirectory(String localDirectoryin)
	{
		this.localDirectory = localDirectoryin;
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
        String realServerName      = StringUtil.environmentSubstitute(serverName);
        String realServerPort      = StringUtil.environmentSubstitute(serverPort);
        String realUsername        = StringUtil.environmentSubstitute(userName);
        String realPassword        = StringUtil.environmentSubstitute(password);
        String realRemoteDirectory = StringUtil.environmentSubstitute(remoteDirectory);
        String realWildcard        = StringUtil.environmentSubstitute(wildcard);
        String realLocalDirectory  = StringUtil.environmentSubstitute(localDirectory);
        
        
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
			ArrayList myFileList = new ArrayList();
			
			
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
						
						// Put file
						ftpclient.put(localFilename, filelist[i]);

						filesput++;
					
						// Delete the file if this is needed!
						if (remove) 
						{
							new File(localFilename).delete();
							if (log.isDetailed()) log.logDetailed(toString(), Messages.getString("JobFTPPUT.Log.DeletedFile",localFilename));
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
    
    public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) {
        return new JobEntryFTPPUTDialog(shell,this,jobMeta);
    }
    
   
    
    
}