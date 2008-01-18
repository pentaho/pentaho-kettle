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
 
package be.ibridge.kettle.job.entry.sftpput;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

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
import be.ibridge.kettle.job.entry.sftp.SFTPClient;
import be.ibridge.kettle.repository.Repository;

/**
 * This defines an SFTP put job entry.
 * 
 * @author Matt
 * @since 05-11-2003
 *
 */
public class JobEntrySFTPPUT extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String serverName;
	private String serverPort;
	private String userName;
	private String password;
	private String sftpDirectory;
	private String localDirectory;
	private String wildcard;
	private boolean remove;
	
	
	public JobEntrySFTPPUT(String n)
	{
		super(n, "");
		serverName=null;
        serverPort="22";
		setID(-1L);
		setType(JobEntryInterface.TYPE_JOBENTRY_SFTPPUT);
	}

	public JobEntrySFTPPUT()
	{
		this("");
	}

	public JobEntrySFTPPUT(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntrySFTPPUT je = (JobEntrySFTPPUT) super.clone();
        return je;
    }
    
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(300);
		
		retval.append(super.getXML());
		
		retval.append("      ").append(XMLHandler.addTagValue("servername",   serverName));
		retval.append("      ").append(XMLHandler.addTagValue("serverport",   serverPort));
		retval.append("      ").append(XMLHandler.addTagValue("username",     userName));
		retval.append("      ").append(XMLHandler.addTagValue("password",     password));
		retval.append("      ").append(XMLHandler.addTagValue("sftpdirectory", sftpDirectory));
		retval.append("      ").append(XMLHandler.addTagValue("localdirectory", localDirectory));
		retval.append("      ").append(XMLHandler.addTagValue("wildcard",     wildcard));
		retval.append("      ").append(XMLHandler.addTagValue("remove",       remove));		
		
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
			sftpDirectory   = XMLHandler.getTagValue(entrynode, "sftpdirectory");
			localDirectory  = XMLHandler.getTagValue(entrynode, "localdirectory");
			wildcard        = XMLHandler.getTagValue(entrynode, "wildcard");
			remove          = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "remove") );
			
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'SFTPPUT' from XML node", xe);			
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
			sftpDirectory   = rep.getJobEntryAttributeString(id_jobentry, "sftpdirectory");
			localDirectory  = rep.getJobEntryAttributeString(id_jobentry, "localdirectory");
			wildcard        = rep.getJobEntryAttributeString(id_jobentry, "wildcard");
			remove          = rep.getJobEntryAttributeBoolean(id_jobentry, "remove");			
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'SFTPPUT' from the repository for id_jobentry="+id_jobentry, dbe);
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
			rep.saveJobEntryAttribute(id_job, getID(), "sftpdirectory",    sftpDirectory);
			rep.saveJobEntryAttribute(id_job, getID(), "localdirectory", localDirectory);
			rep.saveJobEntryAttribute(id_job, getID(), "wildcard",        wildcard);
			rep.saveJobEntryAttribute(id_job, getID(), "remove",          remove);			
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'SFTPPUT' to the repository for id_job="+id_job, dbe);
		}
	}
	
	/**
	 * @return Returns the directory.
	 */
	public String getScpDirectory()
	{
		return sftpDirectory;
	}
	
	/**
	 * @param directory The directory to set.
	 */
	public void setScpDirectory(String directory)
	{
		this.sftpDirectory = directory;
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
	 * @return Returns the localdirectory.
	 */
	public String getLocalDirectory()
	{
		return localDirectory;
	}
	
	/**
	 * @param localDirectory The localDirectory to set.
	 */
	public void setLocalDirectory(String localDirectory)
	{
		this.localDirectory = localDirectory;
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
	
	
	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();

        Result result = previousResult;
		result.setResult( false );
		long filesRetrieved = 0;

		log.logDetailed(toString(), "Start of SFTP job entry");
		
		SFTPClient sftpclient = null;
        
        // String substitution..
        String realServerName      = StringUtil.environmentSubstitute(serverName);
        String realServerPort      = StringUtil.environmentSubstitute(serverPort);
        String realUsername        = StringUtil.environmentSubstitute(userName);
        String realPassword        = StringUtil.environmentSubstitute(password);
        String realSftpDirString   = StringUtil.environmentSubstitute(sftpDirectory);
        String realWildcard        = StringUtil.environmentSubstitute(wildcard);
        String realLocalDirectory  = StringUtil.environmentSubstitute(localDirectory);
        
		try
		{
			// Create sftp client to host ...
			sftpclient = new SFTPClient(InetAddress.getByName(realServerName), Const.toInt(realServerPort, 22), realUsername);
			log.logDetailed(toString(), "Opened SFTP connection to server ["+realServerName+"] on port ["+realServerPort+"] with username ["+realUsername+"]");
	
			// login to ftp host ...
			sftpclient.login(realPassword);
			// Don't show the password in the logs, it's not good for security audits
			//log.logDetailed(toString(), "logged in using password "+realPassword); // Logging this seems a bad idea! Oh well.

			// move to spool dir ...
			if (!Const.isEmpty(realSftpDirString))
			{
				sftpclient.chdir(realSftpDirString);
				log.logDetailed(toString(), "Changed to directory ["+realSftpDirString+"]");
			} // end if
			
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
			
			
			log.logDetailed(toString(), "Found "+filelist.length+" files in the local directory");
			
			
			Pattern pattern = null;
			if (!Const.isEmpty(realWildcard)) 
			{
				pattern = Pattern.compile(realWildcard);
				
			} // end if
			
			// Get the files in the list and execute sftp.put() for each file
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
					log.logDebug(toString(), "putting file ["+filelist[i]+"] to directory ["+realSftpDirString+"]");
					
					String localFilename = realLocalDirectory+Const.FILE_SEPARATOR+filelist[i]; 
					sftpclient.put(localFilename, filelist[i]);
					filesRetrieved++; 
					
					// Add to the result files...JKU:  no idea if this is needed!!!
					// ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, new File(localFilename), parentJob.getJobname(), toString());
                    // result.getResultFiles().put(resultFile.getFile().toString(), resultFile);

					log.logDetailed(toString(), "Transfered file ["+filelist[i]+"]");
					
					// Delete the file if this is needed!
					if (remove) 
					{
						new File(localFilename).delete();
						log.logDetailed(toString(), "deleted local file ["+localFilename+"]");
					}
				}
			} // end for

			result.setResult( true );
			// JKU: no idea if this is needed...!
			// result.setNrFilesRetrieved(filesRetrieved);
		} // end try
		catch(Exception e)
		{
			result.setNrErrors(1);
			log.logError(toString(), "Error getting files from SCP : "+e.getMessage());
            log.logError(toString(), Const.getStackTracker(e));
		} finally {
			// close connection, if possible
			try {
				if(sftpclient != null) sftpclient.disconnect();
			} catch (Exception e) {
				// just ignore this, makes no big difference
			} // end catch
		} // end finallly
        
        return result;
	} // JKU: end function execute()
        

	public boolean evaluates()
	{
		return true;
	}
    
    public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) {
        return new JobEntrySFTPPUTDialog(shell,this,jobMeta);
    }
}