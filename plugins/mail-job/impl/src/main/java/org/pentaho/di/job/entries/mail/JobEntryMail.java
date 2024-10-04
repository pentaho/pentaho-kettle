/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.job.entries.mail;

import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileType;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.gui.JobTracker;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobEntryResult;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * Describes a Mail Job Entry.
 *
 * @author Matt Created on 17-06-2003
 *
 */
@JobEntry( id = "MAIL", name = "JobEntry.Mail.TypeDesc",
        i18nPackageName = "org.pentaho.di.job.entries.mail",
        description = "JobEntry.Mail.Tooltip",
        categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.Mail",
        image = "ui/images/MAIL.svg",
        documentationUrl = "http://wiki.pentaho.com/display/EAI/Mail" )
public class JobEntryMail extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryMail.class; // for i18n purposes, needed by Translator2!!

  private String server;

  private String destination;

  private String destinationCc;

  private String destinationBCc;

  /** Caution : It's sender address and NOT reply address **/
  private String replyAddress;

  /** Caution : It's sender name name and NOT reply name **/
  private String replyName;

  private String subject;

  private boolean includeDate;

  private String contactPerson;

  private String contactPhone;

  private String comment;

  private boolean includingFiles;

  private int[] fileType;

  private boolean zipFiles;

  private String zipFilename;

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

  private String sensitivity;

  private String secureConnectionType;

  /** The encoding to use for reading: null or empty string means system default encoding */
  private String encoding;

  /** The reply to addresses */
  private String replyToAddresses;

  public String[] embeddedimages;

  public String[] contentids;

  public JobEntryMail( String n ) {
    super( n, "" );
    allocate( 0 );
  }

  public JobEntryMail() {
    this( "" );
    allocate( 0 );
  }

  public void allocate( int nrFileTypes ) {
    fileType = new int[nrFileTypes];
  }

  public void allocateImages( int nrImages ) {
    embeddedimages = new String[nrImages];
    contentids = new String[nrImages];
  }

  public Object clone() {
    JobEntryMail je = (JobEntryMail) super.clone();
    if ( fileType != null ) {
      int nrFileTypes = fileType.length;
      je.allocate( nrFileTypes );
      System.arraycopy( fileType, 0, je.fileType, 0, nrFileTypes );
    }
    if ( embeddedimages != null ) {
      int nrImages = embeddedimages.length;
      je.allocateImages( nrImages );
      System.arraycopy( embeddedimages, 0, je.embeddedimages, 0, nrImages );
      System.arraycopy( contentids, 0, je.contentids, 0, nrImages );
    }
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 600 );

    retval.append( super.getXML() );

    retval.append( "      " ).append( XMLHandler.addTagValue( "server", server ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "port", port ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "destination", destination ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "destinationCc", destinationCc ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "destinationBCc", destinationBCc ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "replyto", replyAddress ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "replytoname", replyName ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "subject", subject ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "include_date", includeDate ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "contact_person", contactPerson ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "contact_phone", contactPhone ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "comment", comment ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "include_files", includingFiles ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "zip_files", zipFiles ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "zip_name", zipFilename ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "use_auth", usingAuthentication ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "use_secure_auth", usingSecureAuthentication ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "auth_user", authenticationUser ) );
    retval.append( "      " ).append(
      XMLHandler
        .addTagValue( "auth_password", Encr.encryptPasswordIfNotUsingVariables( authenticationPassword ) ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "only_comment", onlySendComment ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "use_HTML", useHTML ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "use_Priority", usePriority ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "encoding", encoding ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "priority", priority ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "importance", importance ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "sensitivity", sensitivity ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "secureconnectiontype", secureConnectionType ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "replyToAddresses", replyToAddresses ) );

    retval.append( "      <filetypes>" );
    if ( fileType != null ) {
      for ( int i = 0; i < fileType.length; i++ ) {
        retval.append( "        " ).append(
          XMLHandler.addTagValue( "filetype", ResultFile.getTypeCode( fileType[i] ) ) );
      }
    }
    retval.append( "      </filetypes>" );

    retval.append( "      <embeddedimages>" ).append( Const.CR );
    if ( embeddedimages != null ) {
      for ( int i = 0; i < embeddedimages.length; i++ ) {
        retval.append( "        <embeddedimage>" ).append( Const.CR );
        retval.append( "          " ).append( XMLHandler.addTagValue( "image_name", embeddedimages[i] ) );
        retval.append( "          " ).append( XMLHandler.addTagValue( "content_id", contentids[i] ) );
        retval.append( "        </embeddedimage>" ).append( Const.CR );
      }
    }
    retval.append( "      </embeddedimages>" ).append( Const.CR );

    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      setServer( XMLHandler.getTagValue( entrynode, "server" ) );
      setPort( XMLHandler.getTagValue( entrynode, "port" ) );
      setDestination( XMLHandler.getTagValue( entrynode, "destination" ) );
      setDestinationCc( XMLHandler.getTagValue( entrynode, "destinationCc" ) );
      setDestinationBCc( XMLHandler.getTagValue( entrynode, "destinationBCc" ) );
      setReplyAddress( XMLHandler.getTagValue( entrynode, "replyto" ) );
      setReplyName( XMLHandler.getTagValue( entrynode, "replytoname" ) );
      setSubject( XMLHandler.getTagValue( entrynode, "subject" ) );
      setIncludeDate( "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "include_date" ) ) );
      setContactPerson( XMLHandler.getTagValue( entrynode, "contact_person" ) );
      setContactPhone( XMLHandler.getTagValue( entrynode, "contact_phone" ) );
      setComment( XMLHandler.getTagValue( entrynode, "comment" ) );
      setIncludingFiles( "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "include_files" ) ) );

      setUsingAuthentication( "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "use_auth" ) ) );
      setUsingSecureAuthentication( "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "use_secure_auth" ) ) );
      setAuthenticationUser( XMLHandler.getTagValue( entrynode, "auth_user" ) );
      setAuthenticationPassword( Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue(
        entrynode, "auth_password" ) ) );

      setOnlySendComment( "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "only_comment" ) ) );
      setUseHTML( "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "use_HTML" ) ) );

      setUsePriority( "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "use_Priority" ) ) );

      setEncoding( XMLHandler.getTagValue( entrynode, "encoding" ) );
      setPriority( XMLHandler.getTagValue( entrynode, "priority" ) );
      setImportance( XMLHandler.getTagValue( entrynode, "importance" ) );
      setSensitivity( XMLHandler.getTagValue( entrynode, "sensitivity" ) );
      setSecureConnectionType( XMLHandler.getTagValue( entrynode, "secureconnectiontype" ) );

      Node ftsnode = XMLHandler.getSubNode( entrynode, "filetypes" );
      int nrTypes = XMLHandler.countNodes( ftsnode, "filetype" );
      allocate( nrTypes );
      for ( int i = 0; i < nrTypes; i++ ) {
        Node ftnode = XMLHandler.getSubNodeByNr( ftsnode, "filetype", i );
        fileType[i] = ResultFile.getType( XMLHandler.getNodeValue( ftnode ) );
      }

      setZipFiles( "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "zip_files" ) ) );
      setZipFilename( XMLHandler.getTagValue( entrynode, "zip_name" ) );
      setReplyToAddresses( XMLHandler.getTagValue( entrynode, "replyToAddresses" ) );

      Node images = XMLHandler.getSubNode( entrynode, "embeddedimages" );

      // How many field embedded images ?
      int nrImages = XMLHandler.countNodes( images, "embeddedimage" );
      allocateImages( nrImages );

      // Read them all...
      for ( int i = 0; i < nrImages; i++ ) {
        Node fnode = XMLHandler.getSubNodeByNr( images, "embeddedimage", i );

        embeddedimages[i] = XMLHandler.getTagValue( fnode, "image_name" );
        contentids[i] = XMLHandler.getTagValue( fnode, "content_id" );
      }

    } catch ( KettleException xe ) {
      throw new KettleXMLException( "Unable to load job entry of type 'mail' from XML node", xe );
    }
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      // First load the common parts like name & description, then the attributes...
      //
      server = rep.getJobEntryAttributeString( id_jobentry, "server" );
      port = rep.getJobEntryAttributeString( id_jobentry, "port" );
      destination = rep.getJobEntryAttributeString( id_jobentry, "destination" );
      destinationCc = rep.getJobEntryAttributeString( id_jobentry, "destinationCc" );
      destinationBCc = rep.getJobEntryAttributeString( id_jobentry, "destinationBCc" );
      replyAddress = rep.getJobEntryAttributeString( id_jobentry, "replyto" );
      replyName = rep.getJobEntryAttributeString( id_jobentry, "replytoname" );
      subject = rep.getJobEntryAttributeString( id_jobentry, "subject" );
      includeDate = rep.getJobEntryAttributeBoolean( id_jobentry, "include_date" );
      contactPerson = rep.getJobEntryAttributeString( id_jobentry, "contact_person" );
      contactPhone = rep.getJobEntryAttributeString( id_jobentry, "contact_phone" );
      comment = rep.getJobEntryAttributeString( id_jobentry, "comment" );
      encoding = rep.getJobEntryAttributeString( id_jobentry, "encoding" );
      priority = rep.getJobEntryAttributeString( id_jobentry, "priority" );
      importance = rep.getJobEntryAttributeString( id_jobentry, "importance" );
      sensitivity = rep.getJobEntryAttributeString( id_jobentry, "sensitivity" );
      includingFiles = rep.getJobEntryAttributeBoolean( id_jobentry, "include_files" );
      usingAuthentication = rep.getJobEntryAttributeBoolean( id_jobentry, "use_auth" );
      usingSecureAuthentication = rep.getJobEntryAttributeBoolean( id_jobentry, "use_secure_auth" );
      authenticationUser = rep.getJobEntryAttributeString( id_jobentry, "auth_user" );
      authenticationPassword =
        Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( id_jobentry, "auth_password" ) );
      onlySendComment = rep.getJobEntryAttributeBoolean( id_jobentry, "only_comment" );
      useHTML = rep.getJobEntryAttributeBoolean( id_jobentry, "use_HTML" );
      usePriority = rep.getJobEntryAttributeBoolean( id_jobentry, "use_Priority" );
      secureConnectionType = rep.getJobEntryAttributeString( id_jobentry, "secureconnectiontype" );

      int nrTypes = rep.countNrJobEntryAttributes( id_jobentry, "file_type" );
      allocate( nrTypes );

      for ( int i = 0; i < nrTypes; i++ ) {
        String typeCode = rep.getJobEntryAttributeString( id_jobentry, i, "file_type" );
        fileType[i] = ResultFile.getType( typeCode );
      }

      zipFiles = rep.getJobEntryAttributeBoolean( id_jobentry, "zip_files" );
      zipFilename = rep.getJobEntryAttributeString( id_jobentry, "zip_name" );
      replyToAddresses = rep.getJobEntryAttributeString( id_jobentry, "replyToAddresses" );

      // How many arguments?
      int imagesnr = rep.countNrJobEntryAttributes( id_jobentry, "embeddedimage" );
      allocateImages( imagesnr );

      // Read them all...
      for ( int a = 0; a < imagesnr; a++ ) {
        embeddedimages[a] = rep.getJobEntryAttributeString( id_jobentry, a, "embeddedimage" );
        contentids[a] = rep.getJobEntryAttributeString( id_jobentry, a, "contentid" );
      }

    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to load job entry of type 'mail' from the repository with id_jobentry="
        + id_jobentry, dbe );
    }

  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {
      rep.saveJobEntryAttribute( id_job, getObjectId(), "server", server );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "port", port );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "destination", destination );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "destinationCc", destinationCc );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "destinationBCc", destinationBCc );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "replyto", replyAddress );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "replytoname", replyName );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "subject", subject );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "include_date", includeDate );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "contact_person", contactPerson );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "contact_phone", contactPhone );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "comment", comment );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "encoding", encoding );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "priority", priority );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "importance", importance );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "sensitivity", sensitivity );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "include_files", includingFiles );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "use_auth", usingAuthentication );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "use_secure_auth", usingSecureAuthentication );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "auth_user", authenticationUser );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "auth_password", Encr
        .encryptPasswordIfNotUsingVariables( authenticationPassword ) );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "only_comment", onlySendComment );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "use_HTML", useHTML );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "use_Priority", usePriority );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "secureconnectiontype", secureConnectionType );

      if ( fileType != null ) {
        for ( int i = 0; i < fileType.length; i++ ) {
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "file_type", ResultFile.getTypeCode( fileType[i] ) );
        }
      }

      rep.saveJobEntryAttribute( id_job, getObjectId(), "zip_files", zipFiles );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "zip_name", zipFilename );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "replyToAddresses", replyToAddresses );

      // save the arguments...
      if ( embeddedimages != null ) {
        for ( int i = 0; i < embeddedimages.length; i++ ) {
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "embeddedimage", embeddedimages[i] );
          rep.saveJobEntryAttribute( id_job, getObjectId(), i, "contentid", contentids[i] );
        }
      }

    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException(
        "Unable to save job entry of type 'mail' to the repository for id_job=" + id_job, dbe );
    }

  }

  public void setServer( String s ) {
    server = s;
  }

  public String getServer() {
    return server;
  }

  public void setDestination( String dest ) {
    destination = dest;
  }

  public void setDestinationCc( String destCc ) {
    destinationCc = destCc;
  }

  public void setDestinationBCc( String destBCc ) {
    destinationBCc = destBCc;
  }

  public String getDestination() {
    return destination;
  }

  public String getDestinationCc() {
    return destinationCc;
  }

  public String getDestinationBCc() {

    return destinationBCc;
  }

  public void setReplyAddress( String reply ) {
    replyAddress = reply;
  }

  public String getReplyAddress() {
    return replyAddress;
  }

  public void setReplyName( String replyname ) {
    this.replyName = replyname;
  }

  public String getReplyName() {
    return replyName;
  }

  public void setSubject( String subj ) {
    subject = subj;
  }

  public String getSubject() {
    return subject;
  }

  public void setIncludeDate( boolean incl ) {
    includeDate = incl;
  }

  public boolean getIncludeDate() {
    return includeDate;
  }

  public void setContactPerson( String person ) {
    contactPerson = person;
  }

  public String getContactPerson() {
    return contactPerson;
  }

  public void setContactPhone( String phone ) {
    contactPhone = phone;
  }

  public String getContactPhone() {
    return contactPhone;
  }

  public void setComment( String comm ) {
    comment = comm;
  }

  public String getComment() {
    return comment;
  }

  /**
   * @return the result file types to select for attachment </b>
   * @see ResultFile
   */
  public int[] getFileType() {
    return fileType;
  }

  /**
   * @param fileType
   *          the result file types to select for attachment
   * @see ResultFile
   */
  public void setFileType( int[] fileType ) {
    this.fileType = fileType;
  }

  public boolean isIncludingFiles() {
    return includingFiles;
  }

  public void setIncludingFiles( boolean includeFiles ) {
    this.includingFiles = includeFiles;
  }

  /**
   * @return Returns the zipFilename.
   */
  public String getZipFilename() {
    return zipFilename;
  }

  /**
   * @param zipFilename
   *          The zipFilename to set.
   */
  public void setZipFilename( String zipFilename ) {
    this.zipFilename = zipFilename;
  }

  /**
   * @return Returns the zipFiles.
   */
  public boolean isZipFiles() {
    return zipFiles;
  }

  /**
   * @param zipFiles
   *          The zipFiles to set.
   */
  public void setZipFiles( boolean zipFiles ) {
    this.zipFiles = zipFiles;
  }

  /**
   * @return Returns the authenticationPassword.
   */
  public String getAuthenticationPassword() {
    return authenticationPassword;
  }

  /**
   * @param authenticationPassword
   *          The authenticationPassword to set.
   */
  public void setAuthenticationPassword( String authenticationPassword ) {
    this.authenticationPassword = authenticationPassword;
  }

  /**
   * @return Returns the authenticationUser.
   */
  public String getAuthenticationUser() {
    return authenticationUser;
  }

  /**
   * @param authenticationUser
   *          The authenticationUser to set.
   */
  public void setAuthenticationUser( String authenticationUser ) {
    this.authenticationUser = authenticationUser;
  }

  /**
   * @return Returns the usingAuthentication.
   */
  public boolean isUsingAuthentication() {
    return usingAuthentication;
  }

  /**
   * @param usingAuthentication
   *          The usingAuthentication to set.
   */
  public void setUsingAuthentication( boolean usingAuthentication ) {
    this.usingAuthentication = usingAuthentication;
  }

  /**
   * @return the onlySendComment flag
   */
  public boolean isOnlySendComment() {
    return onlySendComment;
  }

  /**
   * @param onlySendComment
   *          the onlySendComment flag to set
   */
  public void setOnlySendComment( boolean onlySendComment ) {
    this.onlySendComment = onlySendComment;
  }

  /**
   * @return the useHTML flag
   */
  public boolean isUseHTML() {
    return useHTML;
  }

  /**
   * @param useHTML
   *          the useHTML to set
   */
  public void setUseHTML( boolean useHTML ) {
    this.useHTML = useHTML;
  }

  /**
   * @return the encoding
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * @return the secure connection type
   */
  public String getSecureConnectionType() {
    return secureConnectionType;
  }

  /**
   * @param secureConnectionType
   *          the secure connection type to set
   */
  public void setSecureConnectionType( String secureConnectionType ) {
    this.secureConnectionType = secureConnectionType;
  }

  /**
   * @param encoding
   *          the encoding to set
   */
  public void setEncoding( String encoding ) {
    this.encoding = encoding;
  }

  /**
   * @param secureconnectiontype
   *          the replayToAddresses to set
   */
  public void setReplyToAddresses( String replyToAddresses ) {
    this.replyToAddresses = replyToAddresses;
  }

  /**
   * @return replayToAddresses
   */
  public String getReplyToAddresses() {
    return this.replyToAddresses;
  }

  /**
   * @param usePriority
   *          the usePriority to set
   */
  public void setUsePriority( boolean usePriority ) {
    this.usePriority = usePriority;
  }

  /**
   * @return the usePriority flag
   */
  public boolean isUsePriority() {
    return usePriority;
  }

  /**
   * @return the priority
   */
  public String getPriority() {
    return priority;
  }

  /**
   * @param importance
   *          the importance to set
   */
  public void setImportance( String importance ) {
    this.importance = importance;
  }

  /**
   * @return the importance
   */
  public String getImportance() {
    return importance;
  }

  public String getSensitivity() {
    return sensitivity;
  }

  public void setSensitivity( String sensitivity ) {
    this.sensitivity = sensitivity;
  }

  /**
   * @param priority
   *          the priority to set
   */
  public void setPriority( String priority ) {
    this.priority = priority;
  }

  public Result execute( Result result, int nr ) {
    File masterZipfile = null;

    // Send an e-mail...
    // create some properties and get the default Session
    Properties props = new Properties();
    if ( Utils.isEmpty( server ) ) {
      logError( BaseMessages.getString( PKG, "JobMail.Error.HostNotSpecified" ) );

      result.setNrErrors( 1L );
      result.setResult( false );
      return result;
    }

    String protocol = "smtp";
    if ( usingSecureAuthentication ) {
      if ( secureConnectionType.equals( "TLS" ) ) {
        // Allow TLS authentication
        props.put( "mail.smtp.starttls.enable", "true" );
      } else {

        protocol = "smtps";
        // required to get rid of a SSL exception :
        // nested exception is:
        // javax.net.ssl.SSLException: Unsupported record version Unknown
        props.put( "mail.smtps.quitwait", "false" );
      }

    }

    props.put( "mail." + protocol + ".host", environmentSubstitute( server ) );
    if ( !Utils.isEmpty( port ) ) {
      props.put( "mail." + protocol + ".port", environmentSubstitute( port ) );
    }

    if ( log.isDebug() ) {
      props.put( "mail.debug", "true" );
    }

    if ( usingAuthentication ) {
      props.put( "mail." + protocol + ".auth", "true" );

      /*
       * authenticator = new Authenticator() { protected PasswordAuthentication getPasswordAuthentication() { return new
       * PasswordAuthentication( StringUtil.environmentSubstitute(Const.NVL(authenticationUser, "")),
       * StringUtil.environmentSubstitute(Const.NVL(authenticationPassword, "")) ); } };
       */
    }

    Session session = Session.getInstance( props );
    session.setDebug( log.isDebug() );

    try {
      // create a message
      Message msg = new MimeMessage( session );

      // set message priority
      if ( usePriority ) {
        String priority_int = "1";
        if ( priority.equals( "low" ) ) {
          priority_int = "3";
        }
        if ( priority.equals( "normal" ) ) {
          priority_int = "2";
        }

        msg.setHeader( "X-Priority", priority_int ); // (String)int between 1= high and 3 = low.
        msg.setHeader( "Importance", importance );
        // seems to be needed for MS Outlook.
        // where it returns a string of high /normal /low.
        msg.setHeader( "Sensitivity", sensitivity );
        // Possible values are normal, personal, private, company-confidential

      }

      // Set Mail sender (From)
      String sender_address = environmentSubstitute( replyAddress );
      if ( !Utils.isEmpty( sender_address ) ) {
        String sender_name = environmentSubstitute( replyName );
        if ( !Utils.isEmpty( sender_name ) ) {
          sender_address = sender_name + '<' + sender_address + '>';
        }
        msg.setFrom( new InternetAddress( sender_address ) );
      } else {
        throw new MessagingException( BaseMessages.getString( PKG, "JobMail.Error.ReplyEmailNotFilled" ) );
      }

      // set Reply to addresses
      String reply_to_address = environmentSubstitute( replyToAddresses );
      if ( !Utils.isEmpty( reply_to_address ) ) {
        // Split the mail-address: space separated
        String[] reply_Address_List = environmentSubstitute( reply_to_address ).split( " " );
        InternetAddress[] address = new InternetAddress[reply_Address_List.length];
        for ( int i = 0; i < reply_Address_List.length; i++ ) {
          address[i] = new InternetAddress( reply_Address_List[i] );
        }
        msg.setReplyTo( address );
      }

      // Split the mail-address: space separated
      String[] destinations = environmentSubstitute( destination ).split( " " );
      InternetAddress[] address = new InternetAddress[destinations.length];
      for ( int i = 0; i < destinations.length; i++ ) {
        address[i] = new InternetAddress( destinations[i] );
      }
      msg.setRecipients( Message.RecipientType.TO, address );

      String realCC = environmentSubstitute( getDestinationCc() );
      if ( !Utils.isEmpty( realCC ) ) {
        // Split the mail-address Cc: space separated
        String[] destinationsCc = realCC.split( " " );
        InternetAddress[] addressCc = new InternetAddress[destinationsCc.length];
        for ( int i = 0; i < destinationsCc.length; i++ ) {
          addressCc[i] = new InternetAddress( destinationsCc[i] );
        }

        msg.setRecipients( Message.RecipientType.CC, addressCc );
      }

      String realBCc = environmentSubstitute( getDestinationBCc() );
      if ( !Utils.isEmpty( realBCc ) ) {
        // Split the mail-address BCc: space separated
        String[] destinationsBCc = realBCc.split( " " );
        InternetAddress[] addressBCc = new InternetAddress[destinationsBCc.length];
        for ( int i = 0; i < destinationsBCc.length; i++ ) {
          addressBCc[i] = new InternetAddress( destinationsBCc[i] );
        }

        msg.setRecipients( Message.RecipientType.BCC, addressBCc );
      }
      String realSubject = environmentSubstitute( subject );
      if ( !Utils.isEmpty( realSubject ) ) {
        msg.setSubject( realSubject );
      }

      msg.setSentDate( new Date() );
      StringBuilder messageText = new StringBuilder();
      String endRow = isUseHTML() ? "<br>" : Const.CR;
      String realComment = environmentSubstitute( comment );
      if ( !Utils.isEmpty( realComment ) ) {
        messageText.append( realComment ).append( Const.CR ).append( Const.CR );
      }
      if ( !onlySendComment ) {

        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.Job" ) ).append( endRow );
        messageText.append( "-----" ).append( endRow );
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.JobName" ) + "    : " ).append(
          parentJob.getJobMeta().getName() ).append( endRow );
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.JobDirectory" ) + "  : " ).append(
          parentJob.getJobMeta().getRepositoryDirectory() ).append( endRow );
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.JobEntry" ) + "   : " ).append(
          getName() ).append( endRow );
        messageText.append( Const.CR );
      }

      if ( includeDate ) {
        messageText
          .append( endRow ).append( BaseMessages.getString( PKG, "JobMail.Log.Comment.MsgDate" ) + ": " )
          .append( XMLHandler.date2string( new Date() ) ).append( endRow ).append( endRow );
      }
      if ( !onlySendComment && result != null ) {
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.PreviousResult" ) + ":" ).append(
          endRow );
        messageText.append( "-----------------" ).append( endRow );
        messageText
          .append( BaseMessages.getString( PKG, "JobMail.Log.Comment.JobEntryNr" ) + "         : " ).append(
            result.getEntryNr() ).append( endRow );
        messageText
          .append( BaseMessages.getString( PKG, "JobMail.Log.Comment.Errors" ) + "               : " ).append(
            result.getNrErrors() ).append( endRow );
        messageText
          .append( BaseMessages.getString( PKG, "JobMail.Log.Comment.LinesRead" ) + "           : " ).append(
            result.getNrLinesRead() ).append( endRow );
        messageText
          .append( BaseMessages.getString( PKG, "JobMail.Log.Comment.LinesWritten" ) + "        : " ).append(
            result.getNrLinesWritten() ).append( endRow );
        messageText
          .append( BaseMessages.getString( PKG, "JobMail.Log.Comment.LinesInput" ) + "          : " ).append(
            result.getNrLinesInput() ).append( endRow );
        messageText
          .append( BaseMessages.getString( PKG, "JobMail.Log.Comment.LinesOutput" ) + "         : " ).append(
            result.getNrLinesOutput() ).append( endRow );
        messageText
          .append( BaseMessages.getString( PKG, "JobMail.Log.Comment.LinesUpdated" ) + "        : " ).append(
            result.getNrLinesUpdated() ).append( endRow );
        messageText
          .append( BaseMessages.getString( PKG, "JobMail.Log.Comment.LinesRejected" ) + "       : " ).append(
            result.getNrLinesRejected() ).append( endRow );
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.Status" ) + "  : " ).append(
          result.getExitStatus() ).append( endRow );
        messageText
          .append( BaseMessages.getString( PKG, "JobMail.Log.Comment.Result" ) + "               : " ).append(
            result.getResult() ).append( endRow );
        messageText.append( endRow );
      }

      if ( !onlySendComment
        && ( !Utils.isEmpty( environmentSubstitute( contactPerson ) ) || !Utils
          .isEmpty( environmentSubstitute( contactPhone ) ) ) ) {
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.ContactInfo" ) + " :" ).append(
          endRow );
        messageText.append( "---------------------" ).append( endRow );
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.PersonToContact" ) + " : " ).append(
          environmentSubstitute( contactPerson ) ).append( endRow );
        messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.Tel" ) + "  : " ).append(
          environmentSubstitute( contactPhone ) ).append( endRow );
        messageText.append( endRow );
      }

      // Include the path to this job entry...
      if ( !onlySendComment ) {
        JobTracker jobTracker = parentJob.getJobTracker();
        if ( jobTracker != null ) {
          messageText.append( BaseMessages.getString( PKG, "JobMail.Log.Comment.PathToJobentry" ) + ":" ).append(
            endRow );
          messageText.append( "------------------------" ).append( endRow );

          addBacktracking( jobTracker, messageText );
          if ( isUseHTML() ) {
            messageText.replace( 0, messageText.length(), messageText.toString().replace( Const.CR, endRow ) );
          }
        }
      }

      MimeMultipart parts = new MimeMultipart();
      MimeBodyPart part1 = new MimeBodyPart(); // put the text in the
      // Attached files counter
      int nrattachedFiles = 0;

      // 1st part

      if ( useHTML ) {
        if ( !Utils.isEmpty( getEncoding() ) ) {
          part1.setContent( messageText.toString(), "text/html; " + "charset=" + getEncoding() );
        } else {
          part1.setContent( messageText.toString(), "text/html; " + "charset=ISO-8859-1" );
        }
      } else {
        part1.setText( messageText.toString() );
      }

      parts.addBodyPart( part1 );

      if ( includingFiles && result != null ) {
        List<ResultFile> resultFiles = result.getResultFilesList();
        if ( resultFiles != null && !resultFiles.isEmpty() ) {
          if ( !zipFiles ) {
            // Add all files to the message...
            //
            for ( ResultFile resultFile : resultFiles ) {
              FileObject file = resultFile.getFile();
              if ( file != null && file.exists() ) {
                boolean found = false;
                for ( int i = 0; i < fileType.length; i++ ) {
                  if ( fileType[i] == resultFile.getType() ) {
                    found = true;
                  }
                }
                if ( found ) {
                  // create a data source
                  MimeBodyPart files = new MimeBodyPart();
                  URLDataSource fds = new URLDataSource( file.getURL() );

                  // get a data Handler to manipulate this file type;
                  files.setDataHandler( new DataHandler( fds ) );
                  // include the file in the data source
                  files.setFileName( file.getName().getBaseName() );

                  // insist on base64 to preserve line endings
                  files.addHeader( "Content-Transfer-Encoding", "base64" );

                  // add the part with the file in the BodyPart();
                  parts.addBodyPart( files );
                  nrattachedFiles++;
                  logBasic( "Added file '" + fds.getName() + "' to the mail message." );
                }
              }
            }
          } else {
            // create a single ZIP archive of all files
            masterZipfile =
              new File( System.getProperty( "java.io.tmpdir" )
                + Const.FILE_SEPARATOR + environmentSubstitute( zipFilename ) );
            ZipOutputStream zipOutputStream = null;
            try {
              zipOutputStream = new ZipOutputStream( new FileOutputStream( masterZipfile ) );

              for ( ResultFile resultFile : resultFiles ) {
                boolean found = false;
                for ( int i = 0; i < fileType.length; i++ ) {
                  if ( fileType[i] == resultFile.getType() ) {
                    found = true;
                  }
                }
                if ( found ) {
                  FileObject file = resultFile.getFile();
                  ZipEntry zipEntry = new ZipEntry( file.getName().getBaseName() );
                  zipOutputStream.putNextEntry( zipEntry );

                  // Now put the content of this file into this archive...
                  BufferedInputStream inputStream = new BufferedInputStream( KettleVFS.getInputStream( file ) );
                  try {
                    int c;
                    while ( ( c = inputStream.read() ) >= 0 ) {
                      zipOutputStream.write( c );
                    }
                  } finally {
                    inputStream.close();
                  }
                  zipOutputStream.closeEntry();
                  nrattachedFiles++;
                  logBasic( "Added file '" + file.getName().getURI() + "' to the mail message in a zip archive." );
                }
              }
            } catch ( Exception e ) {
              logError( "Error zipping attachement files into file ["
                + masterZipfile.getPath() + "] : " + e.toString() );
              logError( Const.getStackTracker( e ) );
              result.setNrErrors( 1 );
            } finally {
              if ( zipOutputStream != null ) {
                try {
                  zipOutputStream.finish();
                  zipOutputStream.close();
                } catch ( IOException e ) {
                  logError( "Unable to close attachement zip file archive : " + e.toString() );
                  logError( Const.getStackTracker( e ) );
                  result.setNrErrors( 1 );
                }
              }
            }

            // Now attach the master zip file to the message.
            if ( result.getNrErrors() == 0 ) {
              // create a data source
              MimeBodyPart files = new MimeBodyPart();
              FileDataSource fds = new FileDataSource( masterZipfile );
              // get a data Handler to manipulate this file type;
              files.setDataHandler( new DataHandler( fds ) );
              // include the file in the data source
              files.setFileName( fds.getName() );
              // add the part with the file in the BodyPart();
              parts.addBodyPart( files );
            }
          }
        }
      }

      int nrEmbeddedImages = 0;
      if ( embeddedimages != null && embeddedimages.length > 0 ) {
        FileObject imageFile = null;
        for ( int i = 0; i < embeddedimages.length; i++ ) {
          String realImageFile = environmentSubstitute( embeddedimages[i] );
          String realcontenID = environmentSubstitute( contentids[i] );
          if ( messageText.indexOf( "cid:" + realcontenID ) < 0 ) {
            if ( log.isDebug() ) {
              log.logDebug( "Image [" + realImageFile + "] is not used in message body!" );
            }
          } else {
            try {
              boolean found = false;
              imageFile = KettleVFS.getFileObject( realImageFile, this );
              if ( imageFile.exists() && imageFile.getType() == FileType.FILE ) {
                found = true;
              } else {
                log.logError( "We can not find [" + realImageFile + "] or it is not a file" );
              }
              if ( found ) {
                // Create part for the image
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                // Load the image
                URLDataSource fds = new URLDataSource( imageFile.getURL() );
                messageBodyPart.setDataHandler( new DataHandler( fds ) );
                // Setting the header
                messageBodyPart.setHeader( "Content-ID", "<" + realcontenID + ">" );
                // Add part to multi-part
                parts.addBodyPart( messageBodyPart );
                nrEmbeddedImages++;
                log.logBasic( "Image '" + fds.getName() + "' was embedded in message." );
              }
            } catch ( Exception e ) {
              log.logError( "Error embedding image [" + realImageFile + "] in message : " + e.toString() );
              log.logError( Const.getStackTracker( e ) );
              result.setNrErrors( 1 );
            } finally {
              if ( imageFile != null ) {
                try {
                  imageFile.close();
                } catch ( Exception e ) { /* Ignore */
                }
              }
            }
          }
        }
      }

      if ( nrEmbeddedImages > 0 && nrattachedFiles == 0 ) {
        // If we need to embedd images...
        // We need to create a "multipart/related" message.
        // otherwise image will appear as attached file
        parts.setSubType( "related" );
      }
      // put all parts together
      msg.setContent( parts );

      Transport transport = null;
      try {
        transport = session.getTransport( protocol );
        String authPass = getPassword( authenticationPassword );

        if ( usingAuthentication ) {
          if ( !Utils.isEmpty( port ) ) {
            transport.connect(
              environmentSubstitute( Const.NVL( server, "" ) ),
              Integer.parseInt( environmentSubstitute( Const.NVL( port, "" ) ) ),
              environmentSubstitute( Const.NVL( authenticationUser, "" ) ),
              authPass );
          } else {
            transport.connect(
              environmentSubstitute( Const.NVL( server, "" ) ),
              environmentSubstitute( Const.NVL( authenticationUser, "" ) ),
              authPass );
          }
        } else {
          transport.connect();
        }
        transport.sendMessage( msg, msg.getAllRecipients() );
      } finally {
        if ( transport != null ) {
          transport.close();
        }
      }
    } catch ( IOException e ) {
      logError( "Problem while sending message: " + e.toString() );
      result.setNrErrors( 1 );
    } catch ( MessagingException mex ) {
      logError( "Problem while sending message: " + mex.toString() );
      result.setNrErrors( 1 );

      Exception ex = mex;
      do {
        if ( ex instanceof SendFailedException ) {
          SendFailedException sfex = (SendFailedException) ex;

          Address[] invalid = sfex.getInvalidAddresses();
          if ( invalid != null ) {
            logError( "    ** Invalid Addresses" );
            for ( int i = 0; i < invalid.length; i++ ) {
              logError( "         " + invalid[i] );
              result.setNrErrors( 1 );
            }
          }

          Address[] validUnsent = sfex.getValidUnsentAddresses();
          if ( validUnsent != null ) {
            logError( "    ** ValidUnsent Addresses" );
            for ( int i = 0; i < validUnsent.length; i++ ) {
              logError( "         " + validUnsent[i] );
              result.setNrErrors( 1 );
            }
          }

          Address[] validSent = sfex.getValidSentAddresses();
          if ( validSent != null ) {
            // System.out.println("    ** ValidSent Addresses");
            for ( int i = 0; i < validSent.length; i++ ) {
              logError( "         " + validSent[i] );
              result.setNrErrors( 1 );
            }
          }
        }
        if ( ex instanceof MessagingException ) {
          ex = ( (MessagingException) ex ).getNextException();
        } else {
          ex = null;
        }
      } while ( ex != null );
    } finally {
      if ( masterZipfile != null && masterZipfile.exists() ) {
        masterZipfile.delete();
      }
    }

    if ( result.getNrErrors() > 0 ) {
      result.setResult( false );
    } else {
      result.setResult( true );
    }

    return result;
  }

  private void addBacktracking( JobTracker jobTracker, StringBuilder messageText ) {
    addBacktracking( jobTracker, messageText, 0 );
  }

  private void addBacktracking( JobTracker jobTracker, StringBuilder messageText, int level ) {
    int nr = jobTracker.nrJobTrackers();

    messageText.append( Const.rightPad( " ", level * 2 ) );
    messageText.append( Const.NVL( jobTracker.getJobName(), "-" ) );
    JobEntryResult jer = jobTracker.getJobEntryResult();
    if ( jer != null ) {
      messageText.append( " : " );
      if ( jer.getJobEntryName() != null ) {
        messageText.append( " : " );
        messageText.append( jer.getJobEntryName() );
      }
      if ( jer.getResult() != null ) {
        messageText.append( " : " );
        messageText.append( "[" + jer.getResult().toString() + "]" );
      }
      if ( jer.getReason() != null ) {
        messageText.append( " : " );
        messageText.append( jer.getReason() );
      }
      if ( jer.getComment() != null ) {
        messageText.append( " : " );
        messageText.append( jer.getComment() );
      }
      if ( jer.getLogDate() != null ) {
        messageText.append( " (" );
        messageText.append( XMLHandler.date2string( jer.getLogDate() ) );
        messageText.append( ')' );
      }
    }
    messageText.append( Const.CR );

    for ( int i = 0; i < nr; i++ ) {
      JobTracker jt = jobTracker.getJobTracker( i );
      addBacktracking( jt, messageText, level + 1 );
    }
  }

  public boolean evaluates() {
    return true;
  }

  public boolean isUnconditional() {
    return true;
  }

  /**
   * @return the usingSecureAuthentication
   */
  public boolean isUsingSecureAuthentication() {
    return usingSecureAuthentication;
  }

  /**
   * @param usingSecureAuthentication
   *          the usingSecureAuthentication to set
   */
  public void setUsingSecureAuthentication( boolean usingSecureAuthentication ) {
    this.usingSecureAuthentication = usingSecureAuthentication;
  }

  /**
   * @return the port
   */
  public String getPort() {
    return port;
  }

  /**
   * @param port
   *          the port to set
   */
  public void setPort( String port ) {
    this.port = port;
  }

  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    String realServername = jobMeta.environmentSubstitute( server );
    ResourceReference reference = new ResourceReference( this );
    reference.getEntries().add( new ResourceEntry( realServername, ResourceType.SERVER ) );
    references.add( reference );
    return references;
  }

  @Override
  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {

    JobEntryValidatorUtils.andValidator().validate( this, "server", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator()
      .validate( this, "replyAddress", remarks, AndValidator.putValidators(
          JobEntryValidatorUtils.notBlankValidator(), JobEntryValidatorUtils.emailValidator() ) );

    JobEntryValidatorUtils.andValidator().validate( this, "destination", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );

    if ( usingAuthentication ) {
      JobEntryValidatorUtils.andValidator().validate( this, "authenticationUser", remarks,
          AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
      JobEntryValidatorUtils.andValidator().validate( this, "authenticationPassword", remarks,
          AndValidator.putValidators( JobEntryValidatorUtils.notNullValidator() ) );
    }

    JobEntryValidatorUtils.andValidator().validate( this, "port", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.integerValidator() ) );

  }

  public String getPassword( String authPassword ) {
    return Encr.decryptPasswordOptionallyEncrypted(
        environmentSubstitute( Const.NVL( authPassword, "" ) ) );
  }

}
