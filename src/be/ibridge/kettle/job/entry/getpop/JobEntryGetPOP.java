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
 
package be.ibridge.kettle.job.entry.getpop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import javax.mail.*;
import javax.mail.internet.*;

import org.apache.commons.vfs.FileObject;
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
import be.ibridge.kettle.core.vfs.KettleVFS;
import be.ibridge.kettle.job.Job;
import be.ibridge.kettle.job.JobMeta;
import be.ibridge.kettle.job.entry.JobEntryBase;
import be.ibridge.kettle.job.entry.JobEntryDialogInterface;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;

import com.sun.mail.pop3.POP3SSLStore;


/**
 * This defines an SQL job entry.
 * 
 * @author Samatar
 * @since 01-03-2007
 *
 */

public class JobEntryGetPOP extends JobEntryBase implements Cloneable, JobEntryInterface
{
	private String servername;
	private String username;
	private String password;
	private boolean usessl;
	private String sslport;
	private String outputdirectory;
	private String filenamepattern;
	private String firstmails;
	public int retrievemails;
	private boolean delete;

	
	public JobEntryGetPOP(String n)
	{
		super(n, "");
		servername=null;
		username=null;
		password=null;
		usessl=false;
		sslport="995";
		outputdirectory=null;
		filenamepattern=null;
		retrievemails=0;
		firstmails=null;
		delete=false;

		setID(-1L);
		setType(JobEntryInterface.TYPE_JOBENTRY_GET_POP);
	}

	public JobEntryGetPOP()
	{
		this("");
	}

	public JobEntryGetPOP(JobEntryBase jeb)
	{
		super(jeb);
	}

    public Object clone()
    {
        JobEntryGetPOP je = (JobEntryGetPOP) super.clone();
        return je;
    }
    
	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
		retval.append(super.getXML());		
		retval.append("      ").append(XMLHandler.addTagValue("servername",   servername));
		retval.append("      ").append(XMLHandler.addTagValue("username",   username));
		retval.append("      ").append(XMLHandler.addTagValue("password",     password));
		retval.append("      ").append(XMLHandler.addTagValue("usessl",       usessl));
		retval.append("      ").append(XMLHandler.addTagValue("sslport",   sslport));
		retval.append("      ").append(XMLHandler.addTagValue("outputdirectory",     outputdirectory));
		retval.append("      ").append(XMLHandler.addTagValue("filenamepattern",     filenamepattern));
		retval.append("      ").append(XMLHandler.addTagValue("retrievemails",  retrievemails));
		retval.append("      ").append(XMLHandler.addTagValue("firstmails",     firstmails));
		retval.append("      ").append(XMLHandler.addTagValue("delete",       delete));
		
		return retval.toString();
	}
	
	public void loadXML(Node entrynode, ArrayList databases, Repository rep)
		throws KettleXMLException
	{
		try
		{
			super.loadXML(entrynode, databases);
			servername      = XMLHandler.getTagValue(entrynode, "servername");
			username      = XMLHandler.getTagValue(entrynode, "username");
			password      = XMLHandler.getTagValue(entrynode, "password");
			usessl          = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "usessl") );
			sslport      = XMLHandler.getTagValue(entrynode, "sslport");
			outputdirectory      = XMLHandler.getTagValue(entrynode, "outputdirectory");
			filenamepattern      = XMLHandler.getTagValue(entrynode, "filenamepattern");
			retrievemails        = Const.toInt(XMLHandler.getTagValue(entrynode, "retrievemails"), -1);
			firstmails      = XMLHandler.getTagValue(entrynode, "firstmails");
			delete          = "Y".equalsIgnoreCase( XMLHandler.getTagValue(entrynode, "delete") );
		}
		catch(KettleXMLException xe)
		{
			throw new KettleXMLException("Unable to load job entry of type 'get pop' from XML node", xe);
		}
	}

	public void loadRep(Repository rep, long id_jobentry, ArrayList databases)
		throws KettleException
	{
		try
		{
			super.loadRep(rep, id_jobentry, databases);
			servername = rep.getJobEntryAttributeString(id_jobentry, "servername");
			username = rep.getJobEntryAttributeString(id_jobentry, "username");
			password        = rep.getJobEntryAttributeString(id_jobentry, "password");
			usessl          = rep.getJobEntryAttributeBoolean(id_jobentry, "usessl");
			int intSSLPort = (int)rep.getJobEntryAttributeInteger(id_jobentry, "sslport");
			sslport = rep.getJobEntryAttributeString(id_jobentry, "sslport"); // backward compatible.
			if (intSSLPort>0 && Const.isEmpty(sslport)) sslport = Integer.toString(intSSLPort);

			outputdirectory        = rep.getJobEntryAttributeString(id_jobentry, "outputdirectory");
			filenamepattern        = rep.getJobEntryAttributeString(id_jobentry, "filenamepattern");
			retrievemails=(int) rep.getJobEntryAttributeInteger(id_jobentry, "retrievemails");
			firstmails= rep.getJobEntryAttributeString(id_jobentry, "firstmails");
			delete          = rep.getJobEntryAttributeBoolean(id_jobentry, "delete");
		}
		catch(KettleException dbe)
		{
			throw new KettleException("Unable to load job entry of type 'get pop' exists from the repository for id_jobentry="+id_jobentry, dbe);
		}
	}
	
	public void saveRep(Repository rep, long id_job)
		throws KettleException
	{
		try
		{
			super.saveRep(rep, id_job);
			
			rep.saveJobEntryAttribute(id_job, getID(), "servername", servername);
			rep.saveJobEntryAttribute(id_job, getID(), "username", username);
			rep.saveJobEntryAttribute(id_job, getID(), "password",        password);
			rep.saveJobEntryAttribute(id_job, getID(), "usessl",          usessl);
			rep.saveJobEntryAttribute(id_job, getID(), "sslport",      sslport);
			rep.saveJobEntryAttribute(id_job, getID(), "outputdirectory",        outputdirectory);
			rep.saveJobEntryAttribute(id_job, getID(), "filenamepattern",        filenamepattern);
			rep.saveJobEntryAttribute(id_job, getID(), "retrievemails", retrievemails);
			rep.saveJobEntryAttribute(id_job, getID(), "firstmails",        firstmails);
			rep.saveJobEntryAttribute(id_job, getID(), "delete",          delete);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleException("Unable to save job entry of type 'get pop' to the repository for id_job="+id_job, dbe);
		}

	}

	public String getSSLPort() 
	{
		return sslport;
	}

	public String getRealSSLPort()
	{
		return StringUtil.environmentSubstitute(getSSLPort());
	}
	public void setSSLPort(String sslport) 
	{
		this.sslport = sslport;
	}

	public void setFirstMails(String firstmails)
	{
		this.firstmails = firstmails;
	}
	public String getFirstMails()
	{
		return firstmails;
	}
	public String getRealFirstMails()
	{
		return StringUtil.environmentSubstitute(getFirstMails());
	}
	public void setServerName(String servername)
	{
		this.servername = servername;
	}
	
	public String getServerName()
	{
		return servername;
	}
	public void setUserName(String username)
	{
		this.username = username;
	}
	
	public String getUserName()
	{
		return username;
	}

	public void setOutputDirectory(String outputdirectory)
	{
		this.outputdirectory = outputdirectory;
	}
	public void setFilenamePattern(String filenamepattern)
	{
		this.filenamepattern = filenamepattern;
	}
	public String getFilenamePattern()
	{
		return filenamepattern;
	}
	public String getOutputDirectory()
	{
		return outputdirectory;
	}
	public String getRealOutputDirectory()
	{
		return StringUtil.environmentSubstitute(getOutputDirectory());
	}
	public String getRealFilenamePattern()
	{
		return StringUtil.environmentSubstitute(getFilenamePattern());
	}
	public String getRealUsername()
	{
		return StringUtil.environmentSubstitute(getUserName());
	}
    public String getRealServername()
    {
        return StringUtil.environmentSubstitute(getServerName());
    }

	/**
	 * @return Returns the password.
	 */
	public String getPassword()
	{
		return password;
	}
	
	public String getRealPassword()
	{
		return StringUtil.environmentSubstitute(getPassword());
	}
	/**
	 * @param delete The delete to set.
	 */
	public void setDelete(boolean delete)
	{
		this.delete = delete;
	}
	
	/**
	 * @return Returns the delete.
	 */
	public boolean getDelete()
	{
		return delete;
	}
	
	/**
	 * @param usessl The usessl to set.
	 */
	public void setUseSSL(boolean usessl)
	{
		this.usessl = usessl;
	}

	/**
	 * @return Returns the usessl.
	 */
	public boolean getUseSSL()
	{
		return usessl;
	}
	
	

	/**
	 * @param password The password to set.
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
	{
		LogWriter log = LogWriter.getInstance();
		Result result = previousResult;
		result.setResult( false );
		result.setNrErrors(1);

		FileObject fileObject = null;
		
		//Get system properties
		
		//Properties prop = System.getProperties();
		Properties prop = new Properties();
		prop.setProperty("mail.pop3s.rsetbeforequit","true"); 
		prop.setProperty("mail.pop3.rsetbeforequit","true"); 
		
		//Create session object
		//Session sess = Session.getInstance(prop, null);
		Session sess = Session.getDefaultInstance( prop, null );
		sess.setDebug(true);

		try
		{

			int nbrmailtoretrieve=Const.toInt(firstmails, 0);
			fileObject = KettleVFS.getFileObject(getRealOutputDirectory());

			// Check if output folder exists
			if (   !fileObject.exists() )
			{
				log.logError(toString(), Messages.getString("JobGetMailsFromPOP.FolderNotExists1.Label") + 
					getRealOutputDirectory() + Messages.getString("JobGetMailsFromPOP.FolderNotExists2.Label"));
			}
			else
			{

				String host=getRealServername();
				String user=getRealUsername();
				String pwd=getRealPassword();  

				Store st=null;

				if (!getUseSSL())
				{
					
					//Create POP3 object					
					st=sess.getStore("pop3"); 
					
					// Try to connect to the server
					st.connect(host,user,pwd);
				}
				else
				{
					// Ssupports POP3 connection with SSL, the connection is established via SSL.

					String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
        
					//Properties pop3Props = new Properties();
        
					prop.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
					prop.setProperty("mail.pop3.socketFactory.fallback", "false");
					prop.setProperty("mail.pop3.port",  getRealSSLPort());
					prop.setProperty("mail.pop3.socketFactory.port", getRealSSLPort());
					
					URLName url = new URLName("pop3", host, Const.toInt(getRealSSLPort(),995), "",	user, pwd);
        				
					st = new POP3SSLStore(sess, url);

					st.connect();

				}
		
				log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.LoggedWithUser.Label") + user);
			
				//Open the INBOX FOLDER
				// For POP3, the only folder available is the INBOX. 
				Folder f = st.getFolder("INBOX");
				
					
				if (f == null) 
				{
					log.logError(toString(), Messages.getString("JobGetMailsFromPOP.InvalidFolder.Label"));
		                    
				}
				else 
				{
					// Open folder
					if (delete)
					{
						f.open(Folder.READ_WRITE); 
					}
					else
					{
						f.open(Folder.READ_ONLY); 
					}
				
					Message messageList[] = f.getMessages();
	  
					log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.TotalMessagesFolder1.Label") 
						+ f.getName() + Messages.getString("JobGetMailsFromPOP.TotalMessagesFolder2.Label")  + messageList.length);
					log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.TotalUnreadMessagesFolder1.Label") 
						+ f.getName() + Messages.getString("JobGetMailsFromPOP.TotalUnreadMessagesFolder2.Label")  + f.getUnreadMessageCount());
		   						
		    			
					// Get emails 
					Message msg_list[]=getPOPMessages(f, retrievemails);
		    			
		    		if (msg_list.length>0)
					{
						List current_file_POP = new ArrayList();
						List current_filepath_POP = new ArrayList();
						int nb_email_POP=1;    
						DateFormat dateFormat = new SimpleDateFormat("hhmmss_MMddyyyy");

						String startpattern="name";
						if (!Const.isEmpty(getRealFilenamePattern()))
						{
							startpattern = getRealFilenamePattern();
						}
						

						for(int i=0;i<msg_list.length;i++)
			    		
						{
			    					
							/*if(msg[i].isMimeType("text/plain"))
							 {
							 log.logDetailed(toString(), "Expediteur: "+msg[i].getFrom()[0]);
							 log.logDetailed(toString(), "Sujet: "+msg[i].getSubject());
							 log.logDetailed(toString(), "Texte: "+(String)msg[i].getContent());
			    		
							 }*/	    	
			    			
							if ((nb_email_POP<=nbrmailtoretrieve && retrievemails==2)||(retrievemails!=2))
							{

								Message msg_POP = msg_list[i];
								log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.EmailFrom.Label")  + msg_list[i].getFrom()[0]);
								log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.EmailSubject.Label") + msg_list[i].getSubject());
								
								
								String localfilename_message = startpattern + "_" + dateFormat.format(new Date()) + "_" +(i + 1) + ".mail";
								
								log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.LocalFilename1.Label") 
									+ localfilename_message + Messages.getString("JobGetMailsFromPOP.LocalFilename2.Label"));

								File filename_message = new File(getRealOutputDirectory(),	localfilename_message);
								OutputStream os_filename = new FileOutputStream(filename_message);
								Enumeration enums_POP = msg_POP.getAllHeaders();
								while (enums_POP.hasMoreElements()) 

								{
									Header header_POP = (Header) enums_POP.nextElement();
									os_filename.write(new StringBuffer(header_POP.getName())
										.append(": ").append(header_POP.getValue())
										.append("\r\n").toString().getBytes());
								}
								os_filename.write("\r\n".getBytes());
								InputStream in_POP = msg_POP.getInputStream();
								byte[] buffer_POP = new byte[1024];
								int length_POP= 0;
								while ((length_POP = in_POP.read(buffer_POP, 0, 1024)) != -1) 
								{
									os_filename.write(buffer_POP, 0, length_POP);
									
										
								}
								os_filename.close();
								nb_email_POP++;
								current_file_POP.add(filename_message);
								current_filepath_POP.add(filename_message.getPath());


								// Check attachments
								Object content = msg_POP.getContent();
								if (content instanceof Multipart) 
								{
									handleMultipart(getRealOutputDirectory(),(Multipart)content);
								} 
								
																
															
								// Check if mail has to be deleted
								if (delete)
								{
									log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.DeleteEmail.Label"));
									msg_POP.setFlag(javax.mail.Flags.Flag.DELETED, true);
								}
							}


						}
					}
					//close the folder, passing in a true value to expunge the deleted message
					if(f != null) f.close(true);
					if (st != null) st.close();

					f = null;
					st = null;
					sess = null;
	    			
		    		result.setNrErrors(0);		
					result.setResult( true );

		            	
				}
			}
				
		}

		catch(NoSuchProviderException e)
		{
			log.logError(toString(), "provider error: "+e.getMessage());
		}
		catch(MessagingException e)
		{
			log.logError(toString(), "Message error: "+e.getMessage());
		}
			
		catch(Exception e)
		{
			log.logError(toString(), "Inexpected error: "+e.getMessage());
		} 
	
		finally 
		{
			if ( fileObject != null )
			{
				try  
				{
					fileObject.close();
				}
				catch ( IOException ex ) {};
			}
			sess = null;

		}
		
		return result;
	}

	public static void handleMultipart(String foldername,Multipart multipart) 
		throws MessagingException, IOException 
	{
		for (int i=0, n=multipart.getCount(); i<n; i++) 
		{
			handlePart(foldername,multipart.getBodyPart(i));
		}
	}

	public static void handlePart(String foldername,Part part) 
		throws MessagingException, IOException 
	{
		String disposition = part.getDisposition();
		// String contentType = part.getContentType();

		if ((disposition != null) && ( disposition.equalsIgnoreCase(Part.ATTACHMENT) || disposition.equalsIgnoreCase(Part.INLINE) ) ) 
		{
	 		saveFile(foldername,MimeUtility.decodeText(part.getFileName()), part.getInputStream());
		} 
	}


	public static void saveFile(String foldername,String filename,
		InputStream input) throws IOException 
	{
	
		// LogWriter log = LogWriter.getInstance();

		if (filename == null) 
		{
			filename = File.createTempFile("xx", ".out").getName();
		}
		// Do no overwrite existing file
		File file = new File(foldername,filename);
		for (int i=0; file.exists(); i++) 
		{
			file = new File(foldername,filename+i);
		}
		FileOutputStream fos = new FileOutputStream(file);
		BufferedOutputStream bos = new BufferedOutputStream(fos);

		BufferedInputStream bis = new BufferedInputStream(input);
		int aByte;
		while ((aByte = bis.read()) != -1) 
		{
			bos.write(aByte);
		}


		bos.flush();
		bos.close();
		bis.close();
	}




	public boolean evaluates()
	{
		return true;
	}
    
    public JobEntryDialogInterface getDialog(Shell shell,JobEntryInterface jei,JobMeta jobMeta,String jobName,Repository rep) {
        return new JobEntryGetPOPDialog(shell,this,jobMeta);
    }
	public Message[] getPOPMessages(Folder folder, int retrievemails)
		throws Exception
	{
		 // Get  messages ..
		try 
		{
			int unreadMsgs = folder.getUnreadMessageCount();
			Message msgsAll[] = folder.getMessages();
			int msgCount   = msgsAll.length;
				

			if (retrievemails ==1)
			{
				Message msgsUnread[] = folder.getMessages(msgCount - unreadMsgs + 1, msgCount);
				return(msgsUnread);
				
			}
			else
			{
				return(msgsAll);
			}
		}
		
		catch (Exception e) 
		{ 
			return null;
		}
		
	}
}
