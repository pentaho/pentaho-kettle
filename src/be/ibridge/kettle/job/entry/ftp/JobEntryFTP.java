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
 
package be.ibridge.kettle.job.entry.ftp;
import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.ResultFile;
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

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;

/**
 * This defines an FTP job entry.
 * 
 * @author Matt
 * @since 05-11-2003
 *
 */

public class JobEntryFTP extends JobEntryBase implements JobEntryInterface
{
	private static Logger log4j = Logger.getLogger(JobEntryFTP.class);
	
	private String serverName;
	private String userName;
	private String password;
	private String ftpDirectory;
	private String targetDirectory;
	private String wildcard;
	private boolean binaryMode;
	private int     timeout;
	private boolean remove;
    private boolean onlyGettingNewFiles;  /* Don't overwrite files */
	
	public JobEntryFTP(String n)
	{
		super(n, "");
		serverName=null;
		setID(-1L);
		setType(JobEntryInterface.TYPE_JOBENTRY_FTP);
	}

	public JobEntryFTP()
	{
		this("");
	}

	public JobEntryFTP(JobEntryBase jeb)
	{
		super(jeb);
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(128);
		
		retval.append(super.getXML());
		
		retval.append("      ").append(XMLHandler.addTagValue("servername",   serverName));
		retval.append("      ").append(XMLHandler.addTagValue("username",     userName));
		retval.append("      ").append(XMLHandler.addTagValue("password",     password));
		retval.append("      ").append(XMLHandler.addTagValue("ftpdirectory", ftpDirectory));
		retval.append("      ").append(XMLHandler.addTagValue("targetdirectory", targetDirectory));
		retval.append("      ").append(XMLHandler.addTagValue("wildcard",     wildcard));
		retval.append("      ").append(XMLHandler.addTagValue("binary",       binaryMode));
		retval.append("      ").append(XMLHandler.addTagValue("timeout",      timeout));
		retval.append("      ").append(XMLHandler.addTagValue("remove",       remove));
        retval.append("      ").append(XMLHandler.addTagValue("only_new",     onlyGettingNewFiles));
		
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, ArrayList databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			serverName          = XMLHandler.getTagValue(entrynode, "servername");
			userName            = XMLHandler.getTagValue(entrynode, "username");
			password            = XMLHandler.getTagValue(entrynode, "password");
			ftpDirectory        = XMLHandler.getTagValue(entrynode, "ftpdirectory");
			targetDirectory     = XMLHandler.getTagValue(entrynode, "targetdirectory");
			wildcard            = XMLHandler.getTagValue(entrynode, "wildcard");
			binaryMode          = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "binary") );
			timeout             = Const.toInt(XMLHandler.getTagValue(entrynode, "timeout"), 10000);
			remove              = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "remove") );
            onlyGettingNewFiles = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "only_new") );
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
			serverName          = rep.getJobEntryAttributeString(id_jobentry, "servername");
			userName            = rep.getJobEntryAttributeString(id_jobentry, "username");
			password            = rep.getJobEntryAttributeString(id_jobentry, "password");
			ftpDirectory        = rep.getJobEntryAttributeString(id_jobentry, "ftpdirectory");
			targetDirectory     = rep.getJobEntryAttributeString(id_jobentry, "targetdirectory");
			wildcard            = rep.getJobEntryAttributeString(id_jobentry, "wildcard");
			binaryMode          = rep.getJobEntryAttributeBoolean(id_jobentry, "binary");
			timeout             = (int)rep.getJobEntryAttributeInteger(id_jobentry, "timeout");
            remove              = rep.getJobEntryAttributeBoolean(id_jobentry, "remove");
			onlyGettingNewFiles = rep.getJobEntryAttributeBoolean(id_jobentry, "only_new");
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
			rep.saveJobEntryAttribute(id_job, getID(), "username",        userName);
			rep.saveJobEntryAttribute(id_job, getID(), "password",        password);
			rep.saveJobEntryAttribute(id_job, getID(), "ftpdirectory",    ftpDirectory);
			rep.saveJobEntryAttribute(id_job, getID(), "targetdirectory", targetDirectory);
			rep.saveJobEntryAttribute(id_job, getID(), "wildcard",        wildcard);
			rep.saveJobEntryAttribute(id_job, getID(), "binary",          binaryMode);
			rep.saveJobEntryAttribute(id_job, getID(), "timeout",         timeout);
            rep.saveJobEntryAttribute(id_job, getID(), "remove",          remove);
			rep.saveJobEntryAttribute(id_job, getID(), "only_new",        onlyGettingNewFiles);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("unable to save jobentry of type 'file exists' to the repository for id_job="+id_job, dbe);
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
	
	public Result execute(Result prev_result, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();

        log4j.info("Started FTP job to "+serverName);

		Result result = new Result(nr);
		result.setResult( false );
		long filesRetrieved = 0;

		log.logDetailed(toString(), "Start of FTP job entry");

		try
		{
			// Create ftp client to host:port ...
			FTPClient ftpclient = new FTPClient();
            ftpclient.setRemoteAddr(InetAddress.getByName(serverName));
            
			log.logDetailed(toString(), "Opened FTP connection to server ["+serverName+"]");
	
			// set passive connectmode ...
			ftpclient.setConnectMode(FTPConnectMode.PASV);
			log.logDetailed(toString(), "set Passive ftp connection mode");
			
			// Set the timeout
			ftpclient.setTimeout(timeout);
			log.logDetailed(toString(), "set timeout to "+timeout);

			// login to ftp host ...
			ftpclient.login(userName, password);
			//  Remove password from logging, you don't know where it ends up.
			log.logDetailed(toString(), "logged in using "+userName);

			// move to spool dir ...
			if (ftpDirectory!=null && ftpDirectory.length()>0)
			{
				ftpclient.chdir(ftpDirectory);
				log.logDetailed(toString(), "Changed to directory ["+ftpDirectory+"]");
			}
			
			// Get all the files in the current directory...
			String[] filelist = ftpclient.dir();
			log.logDetailed(toString(), "Found "+filelist.length+" files in the remote ftp directory");

			// set transfertype ...
			if (binaryMode) 
			{
				ftpclient.setType(FTPTransferType.BINARY);
				log.logDetailed(toString(), "set binary transfer mode");
			}
			else
			{
				ftpclient.setType(FTPTransferType.ASCII);
				log.logDetailed(toString(), "set ASCII transfer mode");
			}

			// Some FTP servers return a message saying no files found as a string in the filenlist
			// e.g. Solaris 8
			// CHECK THIS !!!
			if (filelist.length == 1)
			{
                String translatedWildcard = StringUtil.environmentSubstitute(wildcard);
                if (filelist[0].startsWith(translatedWildcard))
				{
					throw new FTPException(filelist[0]);
				}
			}

			Pattern pattern = null;
			if (wildcard!=null && wildcard.length()>0) 
			{
                String translatedWildcard = StringUtil.environmentSubstitute(wildcard);
                pattern = Pattern.compile(translatedWildcard);
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
                    log.logDebug(toString(), "Getting file ["+filelist[i]+"] to directory ["+StringUtil.environmentSubstitute(targetDirectory)+"]");
					String targetFilename = getTargetFilename(filelist[i]);
                    File targetFile = new File(targetFilename);

                    if ( (onlyGettingNewFiles == false) ||
                    	 (onlyGettingNewFiles == true) && needsDownload(filelist[i]))
                    {
    					ftpclient.get(targetFilename, filelist[i]);
    					filesRetrieved++; 

    					// Add to the result files...
    					ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, targetFile, parentJob.getJobname(), toString());
                        resultFile.setComment("Downloaded from ftp server "+serverName);
    					result.getResultFiles().add(resultFile);

    					log.logDetailed(toString(), "Got file ["+filelist[i]+"]");
                    }

					// Delete the file if this is needed!
					if (remove) 
					{
						ftpclient.delete(filelist[i]);
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
			log.logError(toString(), "Error getting files from FTP : "+e.getMessage());
		}
		
		return result;
	}

    /**
     * @param string the filename from the FTP server
     * 
     * @return the calculated target filename
     */
	protected String getTargetFilename(String string)
    {
        return StringUtil.environmentSubstitute(targetDirectory)+Const.FILE_SEPARATOR+string;
    }

    public boolean evaluates()
	{
		return true;
	}
    
    /**
     * See if the filename on the FTP server needs downloading.
     * The default is to check the presence of the file in the target directory.
     * If you need other functionality, extend this class and build it into a plugin.
     * 
     * @param filename The filename to check
     * @return true if the file needs downloading
     */
    protected boolean needsDownload(String filename)
    {
        File file = new File(getTargetFilename(filename));
        return !file.exists();
    }
    
    public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) {
        return new JobEntryFTPDialog(shell,this,jobMeta);
    }

}
