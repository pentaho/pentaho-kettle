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

package org.pentaho.di.job.entries.sftpput;

import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.integerValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.sftp.SFTPClient;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.w3c.dom.Node;

/**
 * This defines an SFTP put job entry.
 *
 * @author Matt
 * @since 05-11-2003
 *
 */
public class JobEntrySFTPPUT extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private static Class<?> PKG = JobEntrySFTPPUT.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String serverName;
	private String serverPort;
	private String userName;
	private String password;
	private String sftpDirectory;
	private String localDirectory;
	private String wildcard;
	private boolean remove;
	private boolean copyprevious;
	private boolean addFilenameResut;


	public JobEntrySFTPPUT(String n)
	{
		super(n, "");
		serverName=null;
        serverPort="22";
        copyprevious=false;
        addFilenameResut=false;
		setID(-1L);
	}

	public JobEntrySFTPPUT()
	{
		this("");
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
		retval.append("      ").append(XMLHandler.addTagValue("password",     Encr.encryptPasswordIfNotUsingVariables(password)));
		retval.append("      ").append(XMLHandler.addTagValue("sftpdirectory", sftpDirectory));
		retval.append("      ").append(XMLHandler.addTagValue("localdirectory", localDirectory));
		retval.append("      ").append(XMLHandler.addTagValue("wildcard",     wildcard));
		retval.append("      ").append(XMLHandler.addTagValue("remove",       remove));
		retval.append("      ").append(XMLHandler.addTagValue("copyprevious", copyprevious));
		retval.append("      ").append(XMLHandler.addTagValue("addFilenameResut", addFilenameResut));
		
		

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
			password        = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue(entrynode, "password") );
			sftpDirectory   = XMLHandler.getTagValue(entrynode, "sftpdirectory");
			localDirectory  = XMLHandler.getTagValue(entrynode, "localdirectory");
			wildcard        = XMLHandler.getTagValue(entrynode, "wildcard");
			remove          = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "remove") );
			copyprevious    = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "copyprevious") );
			addFilenameResut    = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "addFilenameResut") );
			
			

		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'SFTPPUT' from XML node", xe);
		}
	}

	public void loadRep(Repository rep, ObjectId id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers)
		throws KettleException
	{
		try
		{
			serverName      = rep.getJobEntryAttributeString(id_jobentry, "servername");
			serverPort = rep.getJobEntryAttributeString(id_jobentry, "serverport");

			userName        = rep.getJobEntryAttributeString(id_jobentry, "username");
			password        = Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString(id_jobentry, "password") );
			sftpDirectory   = rep.getJobEntryAttributeString(id_jobentry, "sftpdirectory");
			localDirectory  = rep.getJobEntryAttributeString(id_jobentry, "localdirectory");
			wildcard        = rep.getJobEntryAttributeString(id_jobentry, "wildcard");
			remove          = rep.getJobEntryAttributeBoolean(id_jobentry, "remove");
			copyprevious          = rep.getJobEntryAttributeBoolean(id_jobentry, "copyprevious");
			addFilenameResut          = rep.getJobEntryAttributeBoolean(id_jobentry, "addFilenameResut");
			
			
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'SFTPPUT' from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}

	public void saveRep(Repository rep, ObjectId id_job) throws KettleException
	{
		try
		{
			rep.saveJobEntryAttribute(id_job, getObjectId(), "servername",      serverName);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "serverport",      serverPort);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "username",        userName);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "password",        Encr.encryptPasswordIfNotUsingVariables(password));
			rep.saveJobEntryAttribute(id_job, getObjectId(), "sftpdirectory",    sftpDirectory);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "localdirectory", localDirectory);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "wildcard",        wildcard);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "remove",          remove);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "copyprevious",   copyprevious);
			rep.saveJobEntryAttribute(id_job, getObjectId(), "addFilenameResut",   addFilenameResut);
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
	public boolean isCopyPrevious()
	{
		return copyprevious;
	}
	
	public void setCopyPrevious(boolean copyprevious)
	{
		this.copyprevious=copyprevious;
	}
	public boolean isAddFilenameResut()
	{
		return addFilenameResut;
	}
	public void setAddFilenameResut(boolean addFilenameResut)
	{
		this.addFilenameResut=addFilenameResut;
	}
	
	
	public String getServerPort() {
		return serverPort;
	}

	public void setServerPort(String serverPort) {
		this.serverPort = serverPort;
	}


	public Result execute(Result previousResult, int nr) throws KettleException
	{
    Result result = previousResult;
		List<RowMetaAndData> rows = result.getRows();
		result.setResult( false );

		if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobSFTPPUT.Log.StartJobEntry"));
		ArrayList<FileObject> myFileList = new ArrayList<FileObject>();
		
		if(copyprevious)
		{
			if(rows.size()==0)
			{
				if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobSFTPPUT.ArgsFromPreviousNothing"));
				result.setResult(true);
				return result;
			}
			
			try{
				RowMetaAndData resultRow = null;
				// Copy the input row to the (command line) arguments
				for (int iteration=0;iteration<rows.size();iteration++) 
				{			
					resultRow = rows.get(iteration);
				
					// Get file names
					String file_previous = resultRow.getString(0,null);
					if(!Const.isEmpty(file_previous))
					{
						FileObject file=KettleVFS.getFileObject(file_previous, this);
						if(!file.exists())
							logError(BaseMessages.getString(PKG, "JobSFTPPUT.Log.FilefromPreviousNotFound",file_previous));
						else
						{
							myFileList.add(file);
							if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "JobSFTPPUT.Log.FilenameFromResult",file_previous));
						}
					}
				}
			}catch(Exception e)	{
				logError(BaseMessages.getString(PKG, "JobSFTPPUT.Error.ArgFromPrevious"));
				result.setNrErrors(1);
				return result;
			}
			
		}
		SFTPClient sftpclient = null;

        // String substitution..
        String realServerName      = environmentSubstitute(serverName);
        String realServerPort      = environmentSubstitute(serverPort);
        String realUsername        = environmentSubstitute(userName);
        String realPassword        = environmentSubstitute(password);
        String realSftpDirString   = environmentSubstitute(sftpDirectory);
        String realWildcard        = environmentSubstitute(wildcard);
        String realLocalDirectory  = environmentSubstitute(localDirectory);

		try
		{
			// Create sftp client to host ...
			sftpclient = new SFTPClient(InetAddress.getByName(realServerName), Const.toInt(realServerPort, 22), realUsername);
			if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobSFTPPUT.Log.OpenedConnection",realServerName,""+realServerPort,realUsername));
			
			// login to ftp host ...
			sftpclient.login(realPassword);
			// Don't show the password in the logs, it's not good for security audits
			//logDetailed("logged in using password "+realPassword); // Logging this seems a bad idea! Oh well.

			// move to spool dir ...
			if (!Const.isEmpty(realSftpDirString))
			{
				sftpclient.chdir(realSftpDirString);
				if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobSFTPPUT.Log.ChangedDirectory",realSftpDirString));
			} // end if

			if(!copyprevious)
			{
				// Get all the files in the local directory...
				myFileList = new ArrayList<FileObject>();
	
				FileObject localFiles = KettleVFS.getFileObject(realLocalDirectory, this);
				FileObject[] children = localFiles.getChildren();
				if (children!=null) {
					for (int i=0; i<children.length; i++) {
			            // Get filename of file or directory
						if (children[i].getType().equals(FileType.FILE)) {
							// myFileList.add(children[i].getAbsolutePath());
							myFileList.add(children[i]);
						}
			        } // end for
				}
			}
			
			if(myFileList==null || myFileList.size()==0)
			{
				logError(BaseMessages.getString(PKG, "JobSFTPPUT.Error.NoFileToSend"));
				result.setNrErrors(1);
				return result;
			}

			if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobSFTPPUT.Log.RowsFromPreviousResult",""+myFileList.size()));

			Pattern pattern = null;
			if(!copyprevious)
			{
				if (!Const.isEmpty(realWildcard)) {
					pattern = Pattern.compile(realWildcard);
				}
			}

			// Get the files in the list and execute sftp.put() for each file
			for (int i=0;i<myFileList.size() && !parentJob.isStopped();i++)
			{
				FileObject myFile = myFileList.get(i);
				String localFilename = myFile.toString();
				String destinationFilename=myFile.getName().getBaseName();
				boolean getIt = true;

				// First see if the file matches the regular expression!
				if (pattern!=null)
				{
					Matcher matcher = pattern.matcher(destinationFilename);
					getIt = matcher.matches();
				}

				if (getIt)
				{
					if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "JobSFTPPUT.Log.PuttingFile",localFilename,realSftpDirString));

					sftpclient.put(myFile, destinationFilename);
					
					if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobSFTPPUT.Log.TransferedFile",localFilename));
					
					// Delete the file if this is needed!
					if (remove)
					{
						myFile.delete();
						if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobSFTPPUT.Log.DeletedFile",localFilename));
					}
					else
					{
						if(addFilenameResut)
						{
							// Add to the result files...
							ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, myFile, parentJob.getJobname(), toString());
							result.getResultFiles().put(resultFile.getFile().toString(), resultFile);
							if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "JobSFTPPUT.Log.FilenameAddedToResultFilenames", localFilename));
						}
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
			logError(BaseMessages.getString(PKG, "JobSFTPPUT.Exception",e.getMessage()));
            logError(Const.getStackTracker(e));
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