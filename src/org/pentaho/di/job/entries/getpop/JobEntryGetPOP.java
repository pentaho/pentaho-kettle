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

package org.pentaho.di.job.entries.getpop;

import static org.pentaho.di.job.entry.validator.AbstractFileValidator.putVariableSpace;
import static org.pentaho.di.job.entry.validator.AndValidator.putValidators;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.andValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.fileExistsValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.integerValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notBlankValidator;
import static org.pentaho.di.job.entry.validator.JobEntryValidatorUtils.notNullValidator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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

import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeUtility;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobEntryType;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.w3c.dom.Node;

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
    super(n, ""); //$NON-NLS-1$
    servername = null;
    username = null;
    password = null;
    usessl = false;
    sslport = "995"; //$NON-NLS-1$
    outputdirectory = null;
    filenamepattern = null;
    retrievemails = 0;
    firstmails = null;
    delete = false;

    setID(-1L);
    setJobEntryType(JobEntryType.GET_POP);
  }

  public JobEntryGetPOP()
  {
    this(""); //$NON-NLS-1$
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
    StringBuffer retval = new StringBuffer(200);

    retval.append(super.getXML());
    retval.append("      ").append(XMLHandler.addTagValue("servername", servername)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("username", username)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("password", Encr.encryptPasswordIfNotUsingVariables(password))); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("usessl", usessl)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("sslport", sslport)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("outputdirectory", outputdirectory)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("filenamepattern", filenamepattern)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("retrievemails", retrievemails)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("firstmails", firstmails)); //$NON-NLS-1$ //$NON-NLS-2$
    retval.append("      ").append(XMLHandler.addTagValue("delete", delete)); //$NON-NLS-1$ //$NON-NLS-2$

    return retval.toString();
  }

  public void loadXML(Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers, Repository rep) throws KettleXMLException
  {
    try
    {
      super.loadXML(entrynode, databases, slaveServers);
      servername = XMLHandler.getTagValue(entrynode, "servername"); //$NON-NLS-1$
      username = XMLHandler.getTagValue(entrynode, "username"); //$NON-NLS-1$
      password = Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(entrynode, "password"));
      usessl = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "usessl")); //$NON-NLS-1$ //$NON-NLS-2$
      sslport = XMLHandler.getTagValue(entrynode, "sslport"); //$NON-NLS-1$
      outputdirectory = XMLHandler.getTagValue(entrynode, "outputdirectory"); //$NON-NLS-1$
      filenamepattern = XMLHandler.getTagValue(entrynode, "filenamepattern"); //$NON-NLS-1$
      retrievemails = Const.toInt(XMLHandler.getTagValue(entrynode, "retrievemails"), -1); //$NON-NLS-1$
      firstmails = XMLHandler.getTagValue(entrynode, "firstmails"); //$NON-NLS-1$
      delete = "Y".equalsIgnoreCase(XMLHandler.getTagValue(entrynode, "delete")); //$NON-NLS-1$ //$NON-NLS-2$
    } catch (KettleXMLException xe)
    {
      throw new KettleXMLException(Messages.getString("JobEntryGetPOP.UnableToLoadFromXml"), xe); //$NON-NLS-1$
    }
  }

  public void loadRep(Repository rep, long id_jobentry, List<DatabaseMeta> databases, List<SlaveServer> slaveServers) throws KettleException
  {
    try
    {
      super.loadRep(rep, id_jobentry, databases, slaveServers);
      servername = rep.getJobEntryAttributeString(id_jobentry, "servername"); //$NON-NLS-1$
      username = rep.getJobEntryAttributeString(id_jobentry, "username"); //$NON-NLS-1$
      password = Encr.decryptPasswordOptionallyEncrypted(rep.getJobEntryAttributeString(id_jobentry, "password"));

      usessl = rep.getJobEntryAttributeBoolean(id_jobentry, "usessl"); //$NON-NLS-1$
      int intSSLPort = (int) rep.getJobEntryAttributeInteger(id_jobentry, "sslport"); //$NON-NLS-1$
      sslport = rep.getJobEntryAttributeString(id_jobentry, "sslport"); // backward compatible. //$NON-NLS-1$
      if (intSSLPort > 0 && Const.isEmpty(sslport))
        sslport = Integer.toString(intSSLPort);

      outputdirectory = rep.getJobEntryAttributeString(id_jobentry, "outputdirectory"); //$NON-NLS-1$
      filenamepattern = rep.getJobEntryAttributeString(id_jobentry, "filenamepattern"); //$NON-NLS-1$
      retrievemails = (int) rep.getJobEntryAttributeInteger(id_jobentry, "retrievemails"); //$NON-NLS-1$
      firstmails = rep.getJobEntryAttributeString(id_jobentry, "firstmails"); //$NON-NLS-1$
      delete = rep.getJobEntryAttributeBoolean(id_jobentry, "delete"); //$NON-NLS-1$
    } catch (KettleException dbe)
    {
      throw new KettleException(
          Messages.getString("JobEntryGetPOP.UnableToLoadFromRepo", String.valueOf(id_jobentry)), dbe); //$NON-NLS-1$
    }
  }

  public void saveRep(Repository rep, long id_job) throws KettleException
  {
    try
    {
      super.saveRep(rep, id_job);

      rep.saveJobEntryAttribute(id_job, getID(), "servername", servername); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "username", username); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "password", Encr.encryptPasswordIfNotUsingVariables(password)); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "usessl", usessl); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "sslport", sslport); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "outputdirectory", outputdirectory); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "filenamepattern", filenamepattern); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "retrievemails", retrievemails); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "firstmails", firstmails); //$NON-NLS-1$
      rep.saveJobEntryAttribute(id_job, getID(), "delete", delete); //$NON-NLS-1$
    } catch (KettleDatabaseException dbe)
    {
      throw new KettleException(Messages.getString("JobEntryGetPOP.UnableToSaveToRepo", String.valueOf(id_job)), dbe); //$NON-NLS-1$
    }
  }

  public String getSSLPort()
  {
    return sslport;
  }

  public String getRealSSLPort()
  {
    return environmentSubstitute(getSSLPort());
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
    return environmentSubstitute(getFirstMails());
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
    return environmentSubstitute(getOutputDirectory());
  }

  public String getRealFilenamePattern()
  {
    return environmentSubstitute(getFilenamePattern());
  }

  public String getRealUsername()
  {
    return environmentSubstitute(getUserName());
  }

  public String getRealServername()
  {
    return environmentSubstitute(getServerName());
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
    return environmentSubstitute(getPassword());
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

  @SuppressWarnings(
  { "unchecked" })
  public Result execute(Result previousResult, int nr, Repository rep, Job parentJob)
  {
    LogWriter log = LogWriter.getInstance();
    Result result = previousResult;
    result.setResult(false);
    result.setNrErrors(1);

    FileObject fileObject = null;

    //Get system properties

    //Properties prop = System.getProperties();
    Properties prop = new Properties();
    prop.setProperty("mail.pop3s.rsetbeforequit", "true"); //$NON-NLS-1$ //$NON-NLS-2$
    prop.setProperty("mail.pop3.rsetbeforequit", "true"); //$NON-NLS-1$ //$NON-NLS-2$

    //Create session object
    //Session sess = Session.getInstance(prop, null);
    Session sess = Session.getDefaultInstance(prop, null);
    sess.setDebug(true);

    try
    {

      int nbrmailtoretrieve = Const.toInt(firstmails, 0);
      fileObject = KettleVFS.getFileObject(getRealOutputDirectory());

      // Check if output folder exists
      if (!fileObject.exists())
      {
        log.logError(toString(), Messages.getString("JobGetMailsFromPOP.FolderNotExists.Label", getRealOutputDirectory())); //$NON-NLS-1$
      } else
      {

        String host = getRealServername();
        String user = getRealUsername();
        String pwd = getRealPassword();

        Store st = null;

        if (!getUseSSL())
        {

          //Create POP3 object
          st = sess.getStore("pop3"); //$NON-NLS-1$

          // Try to connect to the server
          st.connect(host, user, pwd);
        } else
        {
          // Ssupports POP3 connection with SSL, the connection is established via SSL.

          String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory"; //$NON-NLS-1$

          //Properties pop3Props = new Properties();

          prop.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY); //$NON-NLS-1$
          prop.setProperty("mail.pop3.socketFactory.fallback", "false"); //$NON-NLS-1$ //$NON-NLS-2$
          prop.setProperty("mail.pop3.port", getRealSSLPort()); //$NON-NLS-1$
          prop.setProperty("mail.pop3.socketFactory.port", getRealSSLPort()); //$NON-NLS-1$

          URLName url = new URLName("pop3", host, Const.toInt(getRealSSLPort(), 995), "", user, pwd); //$NON-NLS-1$ //$NON-NLS-2$

          st = new POP3SSLStore(sess, url);

          st.connect();

        }

        log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.LoggedWithUser.Label") + user); //$NON-NLS-1$

        //Open the INBOX FOLDER
        // For POP3, the only folder available is the INBOX.
        Folder f = st.getFolder("INBOX"); //$NON-NLS-1$

        if (f == null)
        {
          log.logError(toString(), Messages.getString("JobGetMailsFromPOP.InvalidFolder.Label")); //$NON-NLS-1$

        } else
        {
          // Open folder
          if (delete)
          {
            f.open(Folder.READ_WRITE);
          } else
          {
            f.open(Folder.READ_ONLY);
          }

          Message messageList[] = f.getMessages();

          log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.TotalMessagesFolder.Label", f.getName(), String.valueOf(messageList.length))); //$NON-NLS-1$
          log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.TotalUnreadMessagesFolder.Label", f.getName(), String.valueOf(f.getUnreadMessageCount()))); //$NON-NLS-1$

          // Get emails
          Message msg_list[] = getPOPMessages(f, retrievemails);

          if (msg_list.length > 0)
          {
            List<File> current_file_POP = new ArrayList<File>();
            List<String> current_filepath_POP = new ArrayList<String>();
            int nb_email_POP = 1;
            DateFormat dateFormat = new SimpleDateFormat("hhmmss_mmddyyyy"); //$NON-NLS-1$

            String startpattern = "name"; //$NON-NLS-1$
            if (!Const.isEmpty(getRealFilenamePattern()))
            {
              startpattern = getRealFilenamePattern();
            }

            for (int i = 0; i < msg_list.length; i++)

            {

              /*if(msg[i].isMimeType("text/plain"))
               {
               log.logDetailed(toString(), "Expediteur: "+msg[i].getFrom()[0]);
               log.logDetailed(toString(), "Sujet: "+msg[i].getSubject());
               log.logDetailed(toString(), "Texte: "+(String)msg[i].getContent());

               }*/

              if ((nb_email_POP <= nbrmailtoretrieve && retrievemails == 2) || (retrievemails != 2))
              {

                Message msg_POP = msg_list[i];
                log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.EmailFrom.Label", msg_list[i].getFrom()[0].toString())); //$NON-NLS-1$
                log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.EmailSubject.Label", msg_list[i].getSubject())); //$NON-NLS-1$

                String localfilename_message = startpattern
                    + "_" + dateFormat.format(new Date()) + "_" + (i + 1) + ".mail"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

                log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.LocalFilename.Label", localfilename_message)); //$NON-NLS-1$

                File filename_message = new File(getRealOutputDirectory(), localfilename_message);
                OutputStream os_filename = new FileOutputStream(filename_message);
                Enumeration<Header> enums_POP = msg_POP.getAllHeaders();
                while (enums_POP.hasMoreElements())

                {
                  Header header_POP = enums_POP.nextElement();
                  os_filename.write(new StringBuffer(header_POP.getName()).append(": ").append(header_POP.getValue()) //$NON-NLS-1$
                      .append("\r\n").toString().getBytes()); //$NON-NLS-1$
                }
                os_filename.write("\r\n".getBytes()); //$NON-NLS-1$
                InputStream in_POP = msg_POP.getInputStream();
                byte[] buffer_POP = new byte[1024];
                int length_POP = 0;
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
                  handleMultipart(getRealOutputDirectory(), (Multipart) content);
                }

                // Check if mail has to be deleted
                if (delete)
                {
                  log.logDetailed(toString(), Messages.getString("JobGetMailsFromPOP.DeleteEmail.Label")); //$NON-NLS-1$
                  msg_POP.setFlag(javax.mail.Flags.Flag.DELETED, true);
                }
              }

            }
          }
          //close the folder, passing in a true value to expunge the deleted message
          if (f != null)
            f.close(true);
          if (st != null)
            st.close();

          f = null;
          st = null;
          sess = null;

          result.setNrErrors(0);
          result.setResult(true);

        }
      }

    }

    catch (NoSuchProviderException e)
    {
      log.logError(toString(), Messages.getString("JobEntryGetPOP.ProviderException", e.getMessage())); //$NON-NLS-1$
    } catch (MessagingException e)
    {
      log.logError(toString(), Messages.getString("JobEntryGetPOP.MessagingException", e.getMessage())); //$NON-NLS-1$
    }

    catch (Exception e)
    {
      log.logError(toString(), Messages.getString("JobEntryGetPOP.GeneralException", e.getMessage())); //$NON-NLS-1$
    }

    finally
    {
      if (fileObject != null)
      {
        try
        {
          fileObject.close();
        } catch (IOException ex)
        {
        }
        ;
      }
      sess = null;

    }

    return result;
  }

  public static void handleMultipart(String foldername, Multipart multipart) throws MessagingException, IOException
  {
    for (int i = 0, n = multipart.getCount(); i < n; i++)
    {
      handlePart(foldername, multipart.getBodyPart(i));
    }
  }

  public static void handlePart(String foldername, Part part) throws MessagingException, IOException
  {
    String disposition = part.getDisposition();
    // String contentType = part.getContentType();

    if ((disposition != null)
        && (disposition.equalsIgnoreCase(Part.ATTACHMENT) || disposition.equalsIgnoreCase(Part.INLINE)))
    {
      saveFile(foldername, MimeUtility.decodeText(part.getFileName()), part.getInputStream());
    }
  }

  public static void saveFile(String foldername, String filename, InputStream input) throws IOException
  {

    // LogWriter log = LogWriter.getInstance();

    if (filename == null)
    {
      filename = File.createTempFile("xx", ".out").getName(); //$NON-NLS-1$ //$NON-NLS-2$
    }
    // Do no overwrite existing file
    File file = new File(foldername, filename);
    for (int i = 0; file.exists(); i++)
    {
      file = new File(foldername, filename + i);
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

  public Message[] getPOPMessages(Folder folder, int retrievemails) throws Exception
  {
    // Get  messages ..
    try
    {
      int unreadMsgs = folder.getUnreadMessageCount();
      Message msgsAll[] = folder.getMessages();
      int msgCount = msgsAll.length;

      if (retrievemails == 1)
      {
        Message msgsUnread[] = folder.getMessages(msgCount - unreadMsgs + 1, msgCount);
        return (msgsUnread);

      } else
      {
        return (msgsAll);
      }
    }

    catch (Exception e)
    {
      return null;
    }

  }

  public void check(List<CheckResultInterface> remarks, JobMeta jobMeta)
  {
    andValidator().validate(this, "serverName", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
    andValidator().validate(this, "userName", remarks, putValidators(notBlankValidator())); //$NON-NLS-1$
    andValidator().validate(this, "password", remarks, putValidators(notNullValidator())); //$NON-NLS-1$

    ValidatorContext ctx = new ValidatorContext();
    putVariableSpace(ctx, getVariables());
    putValidators(ctx, notBlankValidator(), fileExistsValidator());
    andValidator().validate(this, "outputDirectory", remarks, ctx);//$NON-NLS-1$

    andValidator().validate(this, "SSLPort", remarks, putValidators(integerValidator())); //$NON-NLS-1$
  }

  public List<ResourceReference> getResourceDependencies(JobMeta jobMeta)
  {
    List<ResourceReference> references = super.getResourceDependencies(jobMeta);
    if (!Const.isEmpty(servername)) 
    {
      String realServername = jobMeta.environmentSubstitute(servername);
      ResourceReference reference = new ResourceReference(this);
      reference.getEntries().add(new ResourceEntry(realServername, ResourceType.SERVER));
      references.add(reference);
    }
    return references;
  }
}