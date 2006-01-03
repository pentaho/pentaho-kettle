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
 
package be.ibridge.kettle.job.entry.sftp;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.entry.JobEntryBase;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;

/**
 * This defines an FTP job entry.
 * 
 * @author Matt
 * @since 05-11-2003
 *
 */

public class JobEntrySFTP extends JobEntryBase implements JobEntryInterface
{
	private String serverName;
	private int 	serverPort;
	private String userName;
	private String password;
	private String sftpDirectory;
	private String targetDirectory;
	private String wildcard;
	private boolean remove;
	
	public JobEntrySFTP(String n)
	{
		super(n, "");
		serverName=null;
		setID(-1L);
		setType(JobEntryInterface.TYPE_JOBENTRY_SFTP);
	}

	public JobEntrySFTP()
	{
		this("");
	}

	public JobEntrySFTP(JobEntryBase jeb)
	{
		super(jeb);
	}

	public String getXML()
	{
		String retval ="";
		
		retval+=super.getXML();
		
		retval+="      "+XMLHandler.addTagValue("servername",   serverName);
		retval+="      "+XMLHandler.addTagValue("serverport",   serverPort);
		retval+="      "+XMLHandler.addTagValue("username",     userName);
		retval+="      "+XMLHandler.addTagValue("password",     password);
		retval+="      "+XMLHandler.addTagValue("sftpdirectory", sftpDirectory);
		retval+="      "+XMLHandler.addTagValue("targetdirectory", targetDirectory);
		retval+="      "+XMLHandler.addTagValue("wildcard",     wildcard);
		retval+="      "+XMLHandler.addTagValue("remove",       remove);
		
		return retval;
	}
	
	public void loadXML(Node entrynode, ArrayList databases)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			serverName      = XMLHandler.getTagValue(entrynode, "servername");
			serverPort      = Const.toInt(XMLHandler.getTagValue(entrynode, "serverport"), 22);
			userName        = XMLHandler.getTagValue(entrynode, "username");
			password        = XMLHandler.getTagValue(entrynode, "password");
			sftpDirectory    = XMLHandler.getTagValue(entrynode, "sftpdirectory");
			targetDirectory = XMLHandler.getTagValue(entrynode, "targetdirectory");
			wildcard        = XMLHandler.getTagValue(entrynode, "wildcard");
			remove          = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "remove") );
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load file exists job entry from XML node", xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			serverName      = rep.getJobEntryAttributeString(id_jobentry, "servername");
			serverPort      = (int)rep.getJobEntryAttributeInteger(id_jobentry, "serverport");
			userName        = rep.getJobEntryAttributeString(id_jobentry, "username");
			password        = rep.getJobEntryAttributeString(id_jobentry, "password");
			sftpDirectory    = rep.getJobEntryAttributeString(id_jobentry, "sftpdirectory");
			targetDirectory = rep.getJobEntryAttributeString(id_jobentry, "targetdirectory");
			wildcard        = rep.getJobEntryAttributeString(id_jobentry, "wildcard");
			remove          = rep.getJobEntryAttributeBoolean(id_jobentry, "remove");
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry for type file exists from the repository for id_jobentry="+id_jobentry, dbe);
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
			rep.saveJobEntryAttribute(id_job, getID(), "targetdirectory", targetDirectory);
			rep.saveJobEntryAttribute(id_job, getID(), "wildcard",        wildcard);
			rep.saveJobEntryAttribute(id_job, getID(), "remove",          remove);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("unable to save jobentry of type 'file exists' to the repository for id_job="+id_job, dbe);
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
	 * @return Returns the targetDirectory.
	 */
	public String getTargetDirectory()
	{
		return targetDirectory;
	}
	
	/**
	 * @param targetDirectory The targetDirectory to set.
	 */
	public void setTargetDirectory(String targetDirectory)
	{
		this.targetDirectory = targetDirectory;
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
	
	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
	
	
	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();

        Result result = new Result(nr);
		result.setResult( false );
		long filesRetrieved = 0;

		log.logDetailed(toString(), "Start of SFTP job entry");
		
		SFTPClient sftpclient = null;
		try
		{
			// Create sftp client to host ...
			sftpclient = new SFTPClient(InetAddress.getByName(serverName), serverPort, userName);
			log.logDetailed(toString(), "Opened SFTP connection to server ["+serverName+"] on port ["+serverPort+"] with username ["+userName+"]");
	
			// login to ftp host ...
			sftpclient.login(password);
			log.logDetailed(toString(), "logged in using password "+password);

			// move to spool dir ...
			if (sftpDirectory!=null && sftpDirectory.length()>0)
			{
				sftpclient.chdir(sftpDirectory);
				log.logDetailed(toString(), "Changed to directory ["+sftpDirectory+"]");
			}
			
			// Get all the files in the current directory...
			String[] filelist = sftpclient.dir();
			log.logDetailed(toString(), "Found "+filelist.length+" files in the remote scp directory");

			Pattern pattern = null;
			if (wildcard!=null && wildcard.length()>0) 
			{
				pattern = Pattern.compile(wildcard);
				
			}
			
			// Get the files in the list...
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
					log.logDebug(toString(), "Getting file ["+filelist[i]+"] to directory ["+targetDirectory+"]");
					sftpclient.get(targetDirectory+Const.FILE_SEPARATOR+filelist[i], filelist[i]);
					filesRetrieved++; 
					log.logDetailed(toString(), "Got file ["+filelist[i]+"]");
					
					
					// Delete the file if this is needed!
					if (remove) 
					{
						sftpclient.delete(filelist[i]);
						log.logDetailed(toString(), "deleted file ["+filelist[i]+"]");
					}
				}
			}

			result.setResult( true );
			result.setNrFilesRetrieved(filesRetrieved);
		}
		catch(Exception e)
		{
			result.setNrErrors(1);
			e.printStackTrace();
			log.logError(toString(), "Error getting files from SCP : "+e.getMessage());
		} finally {
			// close connection, if possible
			try {
				if(sftpclient != null) sftpclient.disconnect();
			} catch (Exception e) {
				// just ignore this, makes no big difference
			}
		}
		
		return result;
	}

	public boolean evaluates()
	{
		return true;
	}
}
