/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/

package org.pentaho.di.trans.steps.mail;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;


/**
 * Send mail step.
 * based on Mail job entry
 * @author Samatar
 * @since 28-07-2008
 */

public class MailMeta extends BaseStepMeta implements StepMetaInterface
{
	  private String server;

	  private String destination;

	  private String destinationCc;

	  private String destinationBCc;

	  private String replyAddress;
	  
	  private String replyName;

	  private String subject;

	  private boolean includeDate;
	  
	  private boolean includeSubFolders;
	  
	  private boolean zipFilenameDynamic;
	  
	  private boolean isFilenameDynamic;
	  
	  private String dynamicFieldname;
	  
	  private String dynamicWildcard;
	  
	  private String dynamicZipFilename;
	  
	  private String sourcefilefoldername;
	  
	  private String sourcewildcard;

	  private String contactPerson;

	  private String contactPhone;

	  private String comment;

	  private boolean includingFiles;

	  private int fileType[];

	  private boolean zipFiles;

	  private String zipFilename;
	  
	  private String ziplimitsize;

	  private boolean usingAuthentication;

	  private String authenticationUser;

	  private String authenticationPassword;

	  private boolean onlySendComment;

	  private boolean useHTML;

	  private boolean usingSecureAuthentication;
	  
	  private boolean usePriority;

	  private String port;
	  
	  private String priority;
	  
	  private String importance;
	  
	  private String secureconnectiontype;
	  

	  /** The encoding to use for reading: null or empty string means system default encoding */
	  private String encoding;
	  
	public MailMeta()
	{
		super(); // allocate BaseStepMeta
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{
		Object retval = super.clone();
		return retval;
	}
	 public void allocate(int nrFileTypes)
	  {
	    this.fileType = new int[nrFileTypes];
	  }
	private void readData(Node stepnode)
	{
      setServer(XMLHandler.getTagValue(stepnode, "server"));
      setPort(XMLHandler.getTagValue(stepnode, "port"));
      setDestination(XMLHandler.getTagValue(stepnode, "destination"));
      setDestinationCc(XMLHandler.getTagValue(stepnode, "destinationCc"));
      setDestinationBCc(XMLHandler.getTagValue(stepnode, "destinationBCc"));
      setReplyAddress(XMLHandler.getTagValue(stepnode, "replyto"));
      setReplyName(XMLHandler.getTagValue(stepnode, "replytoname"));
      setSubject(XMLHandler.getTagValue(stepnode, "subject"));
      setIncludeDate("Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include_date")));
      setIncludeSubFolders("Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include_subfolders")));
      setZipFilenameDynamic("Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "zipFilenameDynamic")));
      setisDynamicFilename("Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "isFilenameDynamic")));
      setDynamicFieldname(XMLHandler.getTagValue(stepnode, "dynamicFieldname"));
      setDynamicWildcard(XMLHandler.getTagValue(stepnode, "dynamicWildcard"));
      setDynamicZipFilenameField(XMLHandler.getTagValue(stepnode, "dynamicZipFilename"));
      setSourceFileFoldername(XMLHandler.getTagValue(stepnode, "sourcefilefoldername"));
      setSourceWildcard(XMLHandler.getTagValue(stepnode, "sourcewildcard"));
      setContactPerson(XMLHandler.getTagValue(stepnode, "contact_person"));
      setContactPhone(XMLHandler.getTagValue(stepnode, "contact_phone"));
      setComment(XMLHandler.getTagValue(stepnode, "comment"));
      setIncludingFiles("Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "include_files")));
      setUsingAuthentication("Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "use_auth")));
      setUsingSecureAuthentication("Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "use_secure_auth")));
      setAuthenticationUser(XMLHandler.getTagValue(stepnode, "auth_user"));
      setAuthenticationPassword(Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(stepnode, "auth_password")));
      setOnlySendComment("Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "only_comment")));
      setUseHTML("Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "use_HTML")));
      setUsePriority("Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "use_Priority")));
      setEncoding(XMLHandler.getTagValue(stepnode, "encoding"));
      setPriority(XMLHandler.getTagValue(stepnode, "priority"));
      setImportance(XMLHandler.getTagValue(stepnode, "importance"));
      setSecureConnectionType(XMLHandler.getTagValue(stepnode, "secureconnectiontype"));
      Node ftsnode = XMLHandler.getSubNode(stepnode, "filetypes");
      int nrTypes = XMLHandler.countNodes(ftsnode, "filetype");
      allocate(nrTypes);
      for (int i = 0; i < nrTypes; i++) {
        Node ftnode = XMLHandler.getSubNodeByNr(ftsnode, "filetype", i);
        fileType[i] = ResultFile.getType(XMLHandler.getNodeValue(ftnode));
      }
      setZipFiles("Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "zip_files")));
      setZipFilename(XMLHandler.getTagValue(stepnode, "zip_name"));
      setZipLimitSize(XMLHandler.getTagValue(stepnode, "zip_limit_size"));
  }

	public void setDefault()
	{
	}
	  public String getXML()
	  {
	    StringBuffer retval = new StringBuffer(300);

	    retval.append(super.getXML());

	    retval.append("      ").append(XMLHandler.addTagValue("server", this.server));
	    retval.append("      ").append(XMLHandler.addTagValue("port", this.port));
	    retval.append("      ").append(XMLHandler.addTagValue("destination", this.destination));
	    retval.append("      ").append(XMLHandler.addTagValue("destinationCc", this.destinationCc));
	    retval.append("      ").append(XMLHandler.addTagValue("destinationBCc", this.destinationBCc));
	    retval.append("      ").append(XMLHandler.addTagValue("replyto", this.replyAddress));
	    retval.append("      ").append(XMLHandler.addTagValue("replytoname", this.replyName));
	    retval.append("      ").append(XMLHandler.addTagValue("subject", this.subject));
	    retval.append("      ").append(XMLHandler.addTagValue("include_date", this.includeDate));
	    retval.append("      ").append(XMLHandler.addTagValue("include_subfolders", this.includeSubFolders));
	    retval.append("      ").append(XMLHandler.addTagValue("zipFilenameDynamic", this.zipFilenameDynamic));
	    retval.append("      ").append(XMLHandler.addTagValue("isFilenameDynamic",this.isFilenameDynamic));
	    retval.append("      ").append(XMLHandler.addTagValue("dynamicFieldname", this.dynamicFieldname));
	    retval.append("      ").append(XMLHandler.addTagValue("dynamicWildcard", this.dynamicWildcard));
	    retval.append("      ").append(XMLHandler.addTagValue("dynamicZipFilename", this.dynamicZipFilename));
	    retval.append("      ").append(XMLHandler.addTagValue("sourcefilefoldername", this.sourcefilefoldername));
	    retval.append("      ").append(XMLHandler.addTagValue("sourcewildcard", this.sourcewildcard));
	    retval.append("      ").append(XMLHandler.addTagValue("contact_person", this.contactPerson));
	    retval.append("      ").append(XMLHandler.addTagValue("contact_phone", this.contactPhone));
	    retval.append("      ").append(XMLHandler.addTagValue("comment", this.comment));
	    retval.append("      ").append(XMLHandler.addTagValue("include_files", this.includingFiles));
	    retval.append("      ").append(XMLHandler.addTagValue("zip_files", this.zipFiles));
	    retval.append("      ").append(XMLHandler.addTagValue("zip_name", this.zipFilename));
	    retval.append("      ").append(XMLHandler.addTagValue("zip_limit_size", this.ziplimitsize));
	    retval.append("      ").append(XMLHandler.addTagValue("use_auth", this.usingAuthentication));
	    retval.append("      ").append(XMLHandler.addTagValue("use_secure_auth", this.usingSecureAuthentication));
	    retval.append("      ").append(XMLHandler.addTagValue("auth_user", this.authenticationUser));
	    retval.append("      ").append(XMLHandler.addTagValue("auth_password", Encr.encryptPasswordIfNotUsingVariables(this.authenticationPassword)));
	    retval.append("      ").append(XMLHandler.addTagValue("only_comment", this.onlySendComment));
	    retval.append("      ").append(XMLHandler.addTagValue("use_HTML", this.useHTML));
	    retval.append("      ").append(XMLHandler.addTagValue("use_Priority", this.usePriority));
	    retval.append("    " + XMLHandler.addTagValue("encoding", this.encoding));
	    retval.append("    " + XMLHandler.addTagValue("priority", this.priority));
	    retval.append("    " + XMLHandler.addTagValue("importance", this.importance));
	    retval.append("    " + XMLHandler.addTagValue("secureconnectiontype", this.secureconnectiontype));

	    retval.append("      <filetypes>");
	    if (fileType != null)
	      for (int i = 0; i < fileType.length; i++) {
	        retval.append("        ").append(XMLHandler.addTagValue("filetype", ResultFile.getTypeCode(this.fileType[i])));
	      }
	    retval.append("      </filetypes>");

	    return retval.toString();
	  }

	  public void setServer(String s)
	  {
	    this.server = s;
	  }

	  public String getServer()
	  {
	    return this.server;
	  }

	  public void setDestination(String dest)
	  {
	    this.destination = dest;
	  }

	  public void setDestinationCc(String destCc)
	  {
	    this.destinationCc = destCc;
	  }

	  public void setDestinationBCc(String destBCc)
	  {
		  this.destinationBCc = destBCc;
	  }

	  public String getDestination()
	  {
	    return this.destination;
	  }

	  public String getDestinationCc()
	  {
	    return this.destinationCc;
	  }

	  public String getDestinationBCc()
	  {

	    return this.destinationBCc;
	  }

	  public void setReplyAddress(String reply)
	  {
		  this.replyAddress = reply;
	  }
	  public String getReplyAddress()
	  {
	    return this.replyAddress;
	  }
	  public void setReplyName(String replyname)
	  {
		  this.replyName = replyname;
	  }

	  public String getReplyName()
	  {
	    return this.replyName;
	  }


	  public void setSubject(String subj)
	  {
		  this.subject = subj;
	  }

	  public String getSubject()
	  {
	    return this.subject;
	  }

	  public void setIncludeDate(boolean incl)
	  {
	    this.includeDate = incl;
	  }
	  public void setIncludeSubFolders(boolean incl)
	  {
	    this.includeSubFolders = incl;
	  } 
	  
	  public boolean isIncludeSubFolders()
	  {
		  return this.includeSubFolders;
	  }
	  
	  public boolean isZipFilenameDynamic()
	  {
		  return this.zipFilenameDynamic;
	  }
	  public void setZipFilenameDynamic(boolean isdynamic)
	  {
		  this.zipFilenameDynamic = isdynamic;
	  }
	  public void setisDynamicFilename(boolean isdynamic)
	  {
		  this.isFilenameDynamic = isdynamic;
	  }
	  public void  setDynamicWildcard(String dynamicwildcard)
	  {
		this.dynamicWildcard  =dynamicwildcard;
	  }
	  public void  setDynamicZipFilenameField(String dynamiczipfilename)
	  {
		this.dynamicZipFilename  =dynamiczipfilename;
	  }
	  
	  public String getDynamicZipFilenameField()
	  {
		return this.dynamicZipFilename;
	  }
	  
	  public String getDynamicWildcard()
	  {
		return this.dynamicWildcard;
	  }
	  
	  public void  setSourceFileFoldername(String sourcefile)
	  {
		this.sourcefilefoldername  =sourcefile;
	  }
	  public String getSourceFileFoldername()
	  {
		return this.sourcefilefoldername;
	  }
	  
	  public void  setSourceWildcard(String wildcard)
	  {
		this.sourcewildcard  =wildcard;
	  }
	  public String getSourceWildcard()
	  {
		return this.sourcewildcard;
	  }
	  
	  
	
	  public void setDynamicFieldname(String dynamicfield)
	  {
		this.dynamicFieldname  =dynamicfield;
	  }
	  
	  public String getDynamicFieldname()
	  {
		return this.dynamicFieldname;
	  }
	  
	  public boolean getIncludeDate()
	  {
	    return this.includeDate;
	  }
	  public boolean isDynamicFilename()
	  {
	    return this.isFilenameDynamic;
	  }

	  
	  
	  public void setContactPerson(String person)
	  {
		  this.contactPerson = person;
	  }

	  public String getContactPerson()
	  {
	    return this.contactPerson;
	  }

	  public void setContactPhone(String phone)
	  {
		  this.contactPhone = phone;
	  }

	  public String getContactPhone()
	  {
	    return this.contactPhone;
	  }

	  public void setComment(String comm)
	  {
		  this.comment = comm;
	  }

	  public String getComment()
	  {
	    return this.comment;
	  }

	  /**
	   * @return the result file types to select for attachement </b>
	   * @see ResultFile
	   */
	  public int[] getFileType()
	  {
	    return this.fileType;
	  }

	  /**
	   * @param fileType the result file types to select for attachement
	   * @see ResultFile
	   */
	  public void setFileType(int[] fileType)
	  {
	    this.fileType = fileType;
	  }

	  public boolean isIncludingFiles()
	  {
	    return this.includingFiles;
	  }

	  public void setIncludingFiles(boolean includeFiles)
	  {
	    this.includingFiles = includeFiles;
	  }
	  
	  /**
	   * @return Returns the zipFilename.
	   */
	  public String getZipFilename()
	  {
	    return this.zipFilename;
	  }

	  
	  /**
	   * @return Returns the ziplimitsize.
	   */
	  public String getZipLimitSize()
	  {
	    return this.ziplimitsize;
	  }

	  /**
	   * @param ziplimitsize The ziplimitsize to set.
	   */
	  public void setZipLimitSize(String ziplimitsize)
	  {
	    this.ziplimitsize = ziplimitsize;
	  }
	  
	  
	  /**
	   * @param zipFilename The zipFilename to set.
	   */
	  public void setZipFilename(String zipFilename)
	  {
	    this.zipFilename = zipFilename;
	  }

	  /**
	   * @return Returns the zipFiles.
	   */
	  public boolean isZipFiles()
	  {
	    return zipFiles;
	  }

	  /**
	   * @param zipFiles The zipFiles to set.
	   */
	  public void setZipFiles(boolean zipFiles)
	  {
	    this.zipFiles = zipFiles;
	  }

	  /**
	   * @return Returns the authenticationPassword.
	   */
	  public String getAuthenticationPassword()
	  {
	    return this.authenticationPassword;
	  }

	  /**
	   * @param authenticationPassword The authenticationPassword to set.
	   */
	  public void setAuthenticationPassword(String authenticationPassword)
	  {
	    this.authenticationPassword = authenticationPassword;
	  }

	  /**
	   * @return Returns the authenticationUser.
	   */
	  public String getAuthenticationUser()
	  {
	    return this.authenticationUser;
	  }

	  /**
	   * @param authenticationUser The authenticationUser to set.
	   */
	  public void setAuthenticationUser(String authenticationUser)
	  {
	    this.authenticationUser = authenticationUser;
	  }

	  /**
	   * @return Returns the usingAuthentication.
	   */
	  public boolean isUsingAuthentication()
	  {
	    return this.usingAuthentication;
	  }

	  /**
	   * @param usingAuthentication The usingAuthentication to set.
	   */
	  public void setUsingAuthentication(boolean usingAuthentication)
	  {
	    this.usingAuthentication = usingAuthentication;
	  }

	  /**
	   * @return the onlySendComment flag
	   */
	  public boolean isOnlySendComment()
	  {
	    return this.onlySendComment;
	  }

	  /**
	   * @param onlySendComment the onlySendComment flag to set
	   */
	  public void setOnlySendComment(boolean onlySendComment)
	  {
	    this.onlySendComment = onlySendComment;
	  }

	  /**
	   * @return the useHTML flag
	   */
	  public boolean isUseHTML()
	  {
	    return this.useHTML;
	  }

	  /**
	   * @param useHTML the useHTML to set
	   */
	  public void setUseHTML(boolean UseHTML)
	  {
	    this.useHTML = UseHTML;
	  }

	  /**
	   * @return the encoding
	   */
	  public String getEncoding()
	  {
	    return this.encoding;
	  }
	  
	  
	  /**
	   * @return the secure connection type
	   */
	  public String getSecureConnectionType()
	  {
	    return this.secureconnectiontype;
	  }
	  
	  /**
	   * @param secureconnectiontype the secureconnectiontype to set
	   */
	  public void setSecureConnectionType(String secureconnectiontypein)
	  {
	    this.secureconnectiontype=secureconnectiontypein;
	  }
	  

	  /**
	   * @param encoding the encoding to set
	   */
	  public void setEncoding(String encoding)
	  {
	    this.encoding = encoding;
	  }

	  /**
	   * @return the usingSecureAuthentication
	   */
	  public boolean isUsingSecureAuthentication()
	  {
	    return this.usingSecureAuthentication;
	  }

	  /**
	   * @param usingSecureAuthentication the usingSecureAuthentication to set
	   */
	  public void setUsingSecureAuthentication(boolean usingSecureAuthentication)
	  {
	    this.usingSecureAuthentication = usingSecureAuthentication;
	  }
	  

	  /**
	   * @return the port
	   */
	  public String getPort()
	  {
	    return this.port;
	  }

	  /**
	   * @param port the port to set
	   */
	  public void setPort(String port)
	  {
	    this.port = port;
	  }
	  
	  
	  
	  /**
	   * @param usePriority the usePriority to set
	   */
	  public void setUsePriority(boolean usePriorityin)
	  {
	    this.usePriority = usePriorityin;
	  }
	  
	  /**
	   * @return the usePriority flag
	   */
	  public boolean isUsePriority()
	  {
	    return this.usePriority;
	  }
	  
	  
	  /**
	   * @return the priority
	   */
	  public String getPriority()
	  {
	    return this.priority;
	  }
	  
	  /**
	   * @param importance the importance to set
	   */
	  public void setImportance(String importancein)
	  {
	    this.importance = importancein;
	  }

	  
	  /**
	   * @return the importance
	   */
	  public String getImportance()
	  {
	    return this.importance;
	  }
	  
	  /**
	   * @param priority the priority to set
	   */
	  public void setPriority(String priorityin)
	  {
	    this.priority = priorityin;
	  }
	    public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	    throws KettleException
	{
		 try
		    {

		      // First load the common parts like name & description, then the attributes...
		      //
			 this.server = rep.getJobEntryAttributeString(id_step, "server");
			 this.port = rep.getJobEntryAttributeString(id_step, "port");
			 this.destination = rep.getJobEntryAttributeString(id_step, "destination");
			 this.destinationCc = rep.getJobEntryAttributeString(id_step, "destinationCc");
			 this.destinationBCc = rep.getJobEntryAttributeString(id_step, "destinationBCc");
			 this.replyAddress = rep.getJobEntryAttributeString(id_step, "replyto");
			 this.replyName = rep.getJobEntryAttributeString(id_step, "replytoname");
		      
			 this.subject = rep.getJobEntryAttributeString(id_step, "subject");
			 this.includeDate = rep.getJobEntryAttributeBoolean(id_step, "include_date");
			 this.includeSubFolders = rep.getJobEntryAttributeBoolean(id_step, "include_subfolders");
			 this.zipFilenameDynamic = rep.getJobEntryAttributeBoolean(id_step, "zipFilenameDynamic");
		      
		      
			 this.isFilenameDynamic = rep.getJobEntryAttributeBoolean(id_step, "isFilenameDynamic");
			 this.dynamicFieldname = rep.getJobEntryAttributeString(id_step, "dynamicFieldname");
		     this.dynamicWildcard = rep.getJobEntryAttributeString(id_step, "dynamicWildcard");
		     this.dynamicZipFilename = rep.getJobEntryAttributeString(id_step, "dynamicZipFilename");
		      
		     this.sourcefilefoldername = rep.getJobEntryAttributeString(id_step, "sourcefilefoldername");
		     this.sourcewildcard = rep.getJobEntryAttributeString(id_step, "sourcewildcard");

		     this.contactPerson = rep.getJobEntryAttributeString(id_step, "contact_person");
		     this.contactPhone = rep.getJobEntryAttributeString(id_step, "contact_phone");
		     this.comment = rep.getJobEntryAttributeString(id_step, "comment");
		     this.encoding = rep.getJobEntryAttributeString(id_step, "encoding");
		     this.priority = rep.getJobEntryAttributeString(id_step, "priority");
		     this.importance = rep.getJobEntryAttributeString(id_step, "importance");

		     this.includingFiles = rep.getJobEntryAttributeBoolean(id_step, "include_files");

		     this.usingAuthentication = rep.getJobEntryAttributeBoolean(id_step, "use_auth");
		     this.usingSecureAuthentication = rep.getJobEntryAttributeBoolean(id_step, "use_secure_auth");
		     this.authenticationUser = rep.getJobEntryAttributeString(id_step, "auth_user");
		     this.authenticationPassword = Encr.decryptPasswordOptionallyEncrypted(rep.getJobEntryAttributeString(id_step, "auth_password"));

		     this.onlySendComment = rep.getJobEntryAttributeBoolean(id_step, "only_comment");
		     this.useHTML = rep.getJobEntryAttributeBoolean(id_step, "use_HTML");
		     this.usePriority = rep.getJobEntryAttributeBoolean(id_step, "use_Priority");
		     this.secureconnectiontype = rep.getJobEntryAttributeString(id_step, "secureconnectiontype");

		      
		      int nrTypes = rep.countNrJobEntryAttributes(id_step, "file_type");
		      allocate(nrTypes);

		      for (int i = 0; i < nrTypes; i++) {
		        String typeCode = rep.getJobEntryAttributeString(id_step, i, "file_type");
		        this.fileType[i] = ResultFile.getType(typeCode);
		      }

		      this.zipFiles = rep.getJobEntryAttributeBoolean(id_step, "zip_files");
		      this.zipFilename = rep.getJobEntryAttributeString(id_step, "zip_name");
		      this.ziplimitsize = rep.getJobEntryAttributeString(id_step, "zip_limit_size");
		      
		    } catch (KettleDatabaseException dbe)
		    {
		      throw new KettleException("Unable to load job entry of type 'mail' from the repository with id_step="
		          + id_step, dbe);
		    }

	}
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
	    {

	      rep.saveJobEntryAttribute(id_step, getID(), "server", this.server);
	      rep.saveJobEntryAttribute(id_step, getID(), "port", this.port);
	      rep.saveJobEntryAttribute(id_step, getID(), "destination", this.destination);
	      rep.saveJobEntryAttribute(id_step, getID(), "destinationCc", this.destinationCc);
	      rep.saveJobEntryAttribute(id_step, getID(), "destinationBCc", this.destinationBCc);
	      rep.saveJobEntryAttribute(id_step, getID(), "replyto", this.replyAddress);
	      rep.saveJobEntryAttribute(id_step, getID(), "replytoname", this.replyName);
	      
	      rep.saveJobEntryAttribute(id_step, getID(), "subject", this.subject);
	      rep.saveJobEntryAttribute(id_step, getID(), "include_date", this.includeDate);
	      rep.saveJobEntryAttribute(id_step, getID(), "include_subfolders", this.includeSubFolders);
	      rep.saveJobEntryAttribute(id_step, getID(), "zipFilenameDynamic", this.zipFilenameDynamic);

	      rep.saveJobEntryAttribute(id_step, getID(), "isFilenameDynamic", isFilenameDynamic);
	      rep.saveJobEntryAttribute(id_step, getID(), "dynamicFieldname", dynamicFieldname);
	      rep.saveJobEntryAttribute(id_step, getID(), "dynamicWildcard", dynamicWildcard);
	      rep.saveJobEntryAttribute(id_step, getID(), "dynamicZipFilename", dynamicZipFilename);
	      
	      rep.saveJobEntryAttribute(id_step, getID(), "sourcefilefoldername", sourcefilefoldername);
	      rep.saveJobEntryAttribute(id_step, getID(), "sourcewildcard", sourcewildcard);
	
	      rep.saveJobEntryAttribute(id_step, getID(), "contact_person", contactPerson);
	      rep.saveJobEntryAttribute(id_step, getID(), "contact_phone", contactPhone);
	      rep.saveJobEntryAttribute(id_step, getID(), "comment", comment);
	      rep.saveJobEntryAttribute(id_step, getID(), "encoding", encoding);
	      rep.saveJobEntryAttribute(id_step, getID(), "priority", priority);
	      rep.saveJobEntryAttribute(id_step, getID(), "importance", importance);
	      
	      
	      rep.saveJobEntryAttribute(id_step, getID(), "include_files", includingFiles);
	      rep.saveJobEntryAttribute(id_step, getID(), "use_auth", usingAuthentication);
	      rep.saveJobEntryAttribute(id_step, getID(), "use_secure_auth", usingSecureAuthentication);
	      rep.saveJobEntryAttribute(id_step, getID(), "auth_user", authenticationUser);
	      rep.saveJobEntryAttribute(id_step, getID(), "auth_password", Encr.encryptPasswordIfNotUsingVariables(authenticationPassword));

	      rep.saveJobEntryAttribute(id_step, getID(), "only_comment", onlySendComment);
	      rep.saveJobEntryAttribute(id_step, getID(), "use_HTML", useHTML);
	      rep.saveJobEntryAttribute(id_step, getID(), "use_Priority", usePriority);
	      rep.saveJobEntryAttribute(id_step, getID(), "secureconnectiontype", secureconnectiontype);
	      
	      

	      if (fileType != null)
	      {
	        for (int i = 0; i < fileType.length; i++)
	        {
	          rep.saveJobEntryAttribute(id_step, getID(), i, "file_type", ResultFile.getTypeCode(fileType[i]));
	        }
	      }

	      rep.saveJobEntryAttribute(id_step, getID(), "zip_files", zipFiles);
	      rep.saveJobEntryAttribute(id_step, getID(), "zip_name", zipFilename);
	      rep.saveJobEntryAttribute(id_step, getID(), "zip_limit_size", ziplimitsize);
	      

	    } catch (KettleDatabaseException dbe)
	    {
	      throw new KettleException("Unable to save job entry of type 'mail' to the repository for id_step=" + id_step, dbe);
	    }

	}
	
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{	CheckResult cr;
		if (prev==null || prev.size()==0)
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, Messages.getString("MailMeta.CheckResult.NotReceivingFields"), stepinfo); //$NON-NLS-1$
		else
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MailMeta.CheckResult.StepRecevingData",prev.size()+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
		remarks.add(cr);
		
		
		// See if we have input streams leading to this step!
		if (input.length>0)
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MailMeta.CheckResult.StepRecevingData2"), stepinfo); //$NON-NLS-1$
		else
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("MailMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
		remarks.add(cr);
		
		// Servername
		if(Const.isEmpty(server))
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("MailMeta.CheckResult.ServerEmpty"), stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MailMeta.CheckResult.ServerOk"), stepinfo);
			remarks.add(cr);
			// is the field exists?
			if(prev.indexOfValue(transMeta.environmentSubstitute(server))<0)
		    	cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, Messages.getString("MailMeta.CheckResult.ServerFieldNotFound",server), stepinfo);
			remarks.add(cr);
		}
		
		
		// port number
		if(Const.isEmpty(port))
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, Messages.getString("MailMeta.CheckResult.PortEmpty"), stepinfo);
		else
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MailMeta.CheckResult.PortOk"), stepinfo);
		remarks.add(cr);
		
		// reply address
		if(Const.isEmpty(replyAddress))
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("MailMeta.CheckResult.ReplayAddressEmpty"), stepinfo);
		else
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MailMeta.CheckResult.ReplayAddressOk"), stepinfo);
		remarks.add(cr);
		
		// Destination
		if(Const.isEmpty(destination))
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("MailMeta.CheckResult.DestinationEmpty"), stepinfo);
		else
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MailMeta.CheckResult.DestinationOk"), stepinfo);
		remarks.add(cr);
		
		// Subject
		if(Const.isEmpty(subject))
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, Messages.getString("MailMeta.CheckResult.SubjectEmpty"), stepinfo);
		else
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MailMeta.CheckResult.SubjectOk"), stepinfo);
		remarks.add(cr);
		
		// Comment
		if(Const.isEmpty(comment))
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, Messages.getString("MailMeta.CheckResult.CommentEmpty"), stepinfo);
		else
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MailMeta.CheckResult.CommentEmpty"), stepinfo);
		remarks.add(cr);
		
		if(isFilenameDynamic)
		{
			// Dynamic Filename field
			if(Const.isEmpty(dynamicFieldname))
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("MailMeta.CheckResult.DynamicFilenameFieldEmpty"), stepinfo);
			else
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MailMeta.CheckResult.DynamicFilenameFieldOk"), stepinfo);
			remarks.add(cr);
			
		}else
		{
			// static filename
			if(Const.isEmpty(sourcefilefoldername))
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("MailMeta.CheckResult.SourceFilenameEmpty"), stepinfo);
			else
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MailMeta.CheckResult.SourceFilenameOk"), stepinfo);
			remarks.add(cr);
		}
		
		if(isZipFiles())
		{
			if(isFilenameDynamic)
			{
				// dynamic zipfilename
				if(Const.isEmpty(getDynamicZipFilenameField()))
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("MailMeta.CheckResult.DynamicZipfilenameEmpty"), stepinfo);
				else
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MailMeta.CheckResult.DynamicZipfilenameOK"), stepinfo);
				remarks.add(cr);
				
			}else
			{
				// static zipfilename
				if(Const.isEmpty(zipFilename))
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("MailMeta.CheckResult.ZipfilenameEmpty"), stepinfo);
				else
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("MailMeta.CheckResult.ZipfilenameOk"), stepinfo);
				remarks.add(cr);
			}
		}
	}
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new Mail(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new MailData();
	}

    public boolean supportsErrorHandling()
    {
        return true;
    }
}
