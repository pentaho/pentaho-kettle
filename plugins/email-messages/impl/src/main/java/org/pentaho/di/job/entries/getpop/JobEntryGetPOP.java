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

package org.pentaho.di.job.entries.getpop;

import org.pentaho.di.core.annotations.JobEntry;
import org.pentaho.di.job.entry.validator.AbstractFileValidator;
import org.pentaho.di.job.entry.validator.AndValidator;
import org.pentaho.di.job.entry.validator.JobEntryValidatorUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.mail.Flags.Flag;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.validator.ValidatorContext;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.resource.ResourceEntry;
import org.pentaho.di.resource.ResourceEntry.ResourceType;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

/**
 * This defines an get pop job entry.
 *
 * @author Samatar
 * @since 01-03-2007
 *
 */
@JobEntry( id = "GET_POP", name = "JobEntry.GetPOP.TypeDesc",
        i18nPackageName = "org.pentaho.di.job.entries.getpop",
        description = "JobEntry.GetPOP.Tooltip",
        categoryDescription = "i18n:org.pentaho.di.job:JobCategory.Category.Mail",
        image = "ui/images/GETPOP.svg",
        documentationUrl = "http://wiki.pentaho.com/display/EAI/Get+Mails+from+POP" )

public class JobEntryGetPOP extends JobEntryBase implements Cloneable, JobEntryInterface {
  private static Class<?> PKG = JobEntryGetPOP.class; // for i18n purposes, needed by Translator2!!

  static final int FOLDER_OUTPUT = 0;
  static final int FOLDER_ATTACHMENTS = 1;

  public int actiontype;

  public int conditionReceivedDate;

  public int valueimaplist;

  public int aftergetimap;

  private String servername;
  private String username;
  private String password;
  private boolean usessl;
  private String sslport;
  private boolean useproxy;
  private String proxyusername;
  private String outputdirectory;
  private String filenamepattern;
  private String firstmails;
  public int retrievemails;
  private boolean delete;
  private String protocol;
  private boolean saveattachment;
  private boolean savemessage;
  private boolean usedifferentfolderforattachment;
  private String attachmentfolder;
  private String attachmentwildcard;
  private String imapfirstmails;
  private String imapfolder;
  // search term
  private String senderSearch;
  private boolean notTermSenderSearch;
  private String receipientSearch;
  private String subjectSearch;
  private String bodySearch;
  private boolean notTermBodySearch;
  private String receivedDate1;
  private String receivedDate2;
  private boolean notTermSubjectSearch;
  private boolean notTermReceipientSearch;
  private boolean notTermReceivedDateSearch;
  private boolean includesubfolders;
  private String moveToIMAPFolder;
  private boolean createmovetofolder;
  private boolean createlocalfolder;

  private static final String DEFAULT_FILE_NAME_PATTERN = "name_{SYS|hhmmss_MMddyyyy|}_#IdFile#.mail";

  public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
  private static final String FILENAME_ID_PATTERN = "#IdFile#";
  private static final String FILENAME_SYS_DATE_OPEN = "{SYS|";
  private static final String FILENAME_SYS_DATE_CLOSE = "|}";

  private Pattern attachementPattern;

  public JobEntryGetPOP( String n ) {
    super( n, "" );
    servername = null;
    username = null;
    password = null;
    usessl = false;
    sslport = null;
    useproxy = false;
    proxyusername = null;
    outputdirectory = null;
    filenamepattern = DEFAULT_FILE_NAME_PATTERN;
    retrievemails = 0;
    firstmails = null;
    delete = false;
    protocol = MailConnectionMeta.PROTOCOL_STRING_POP3;
    saveattachment = true;
    savemessage = true;
    usedifferentfolderforattachment = false;
    attachmentfolder = null;
    attachmentwildcard = null;
    imapfirstmails = "0";
    valueimaplist = MailConnectionMeta.VALUE_IMAP_LIST_ALL;
    imapfolder = null;
    // search term
    senderSearch = null;
    notTermSenderSearch = false;
    notTermReceipientSearch = false;
    notTermSubjectSearch = false;
    bodySearch = null;
    notTermBodySearch = false;
    receivedDate1 = null;
    receivedDate2 = null;
    notTermReceivedDateSearch = false;
    receipientSearch = null;
    subjectSearch = null;
    actiontype = MailConnectionMeta.ACTION_TYPE_GET;
    moveToIMAPFolder = null;
    createmovetofolder = false;
    createlocalfolder = false;
    aftergetimap = MailConnectionMeta.AFTER_GET_IMAP_NOTHING;
    includesubfolders = false;
  }

  public JobEntryGetPOP() {
    this( "" );
  }

  public Object clone() {
    JobEntryGetPOP je = (JobEntryGetPOP) super.clone();
    return je;
  }

  public String getXML() {
    StringBuilder retval = new StringBuilder( 550 );
    retval.append( super.getXML() );
    retval.append( "      " ).append( XMLHandler.addTagValue( "servername", servername ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "username", username ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "password", Encr.encryptPasswordIfNotUsingVariables( password ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "usessl", usessl ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "sslport", sslport ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "outputdirectory", outputdirectory ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "filenamepattern", filenamepattern ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "retrievemails", retrievemails ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "firstmails", firstmails ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "delete", delete ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "savemessage", savemessage ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "saveattachment", saveattachment ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "usedifferentfolderforattachment", usedifferentfolderforattachment ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "protocol", protocol ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "attachmentfolder", attachmentfolder ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "attachmentwildcard", attachmentwildcard ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "valueimaplist", MailConnectionMeta.getValueImapListCode( valueimaplist ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "imapfirstmails", imapfirstmails ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "imapfolder", imapfolder ) );
    // search term
    retval.append( "      " ).append( XMLHandler.addTagValue( "sendersearch", senderSearch ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "nottermsendersearch", notTermSenderSearch ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "receipientsearch", receipientSearch ) );
    retval
      .append( "      " ).append( XMLHandler.addTagValue( "nottermreceipientsearch", notTermReceipientSearch ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "subjectsearch", subjectSearch ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "nottermsubjectsearch", notTermSubjectSearch ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "bodysearch", bodySearch ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "nottermbodysearch", notTermBodySearch ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "conditionreceiveddate", MailConnectionMeta
        .getConditionDateCode( conditionReceivedDate ) ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "nottermreceiveddatesearch", notTermReceivedDateSearch ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "receiveddate1", receivedDate1 ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "receiveddate2", receivedDate2 ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "actiontype", MailConnectionMeta.getActionTypeCode( actiontype ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "movetoimapfolder", moveToIMAPFolder ) );

    retval.append( "      " ).append( XMLHandler.addTagValue( "createmovetofolder", createmovetofolder ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "createlocalfolder", createlocalfolder ) );
    retval.append( "      " ).append(
      XMLHandler.addTagValue( "aftergetimap", MailConnectionMeta.getAfterGetIMAPCode( aftergetimap ) ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "includesubfolders", includesubfolders ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "useproxy", useproxy ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "proxyusername", proxyusername ) );
    return retval.toString();
  }

  public void loadXML( Node entrynode, List<DatabaseMeta> databases, List<SlaveServer> slaveServers,
    Repository rep, IMetaStore metaStore ) throws KettleXMLException {
    try {
      super.loadXML( entrynode, databases, slaveServers );
      servername = XMLHandler.getTagValue( entrynode, "servername" );
      username = XMLHandler.getTagValue( entrynode, "username" );
      password = Encr.decryptPasswordOptionallyEncrypted( XMLHandler.getTagValue( entrynode, "password" ) );
      usessl = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "usessl" ) );
      sslport = XMLHandler.getTagValue( entrynode, "sslport" );
      outputdirectory = XMLHandler.getTagValue( entrynode, "outputdirectory" );
      filenamepattern = XMLHandler.getTagValue( entrynode, "filenamepattern" );
      if ( Utils.isEmpty( filenamepattern ) ) {
        filenamepattern = DEFAULT_FILE_NAME_PATTERN;
      }
      retrievemails = Const.toInt( XMLHandler.getTagValue( entrynode, "retrievemails" ), -1 );
      firstmails = XMLHandler.getTagValue( entrynode, "firstmails" );
      delete = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "delete" ) );

      protocol =
        Const.NVL( XMLHandler.getTagValue( entrynode, "protocol" ), MailConnectionMeta.PROTOCOL_STRING_POP3 );

      String sm = XMLHandler.getTagValue( entrynode, "savemessage" );
      if ( Utils.isEmpty( sm ) ) {
        savemessage = true;
      } else {
        savemessage = "Y".equalsIgnoreCase( sm );
      }

      String sa = XMLHandler.getTagValue( entrynode, "saveattachment" );
      if ( Utils.isEmpty( sa ) ) {
        saveattachment = true;
      } else {
        saveattachment = "Y".equalsIgnoreCase( sa );
      }

      usedifferentfolderforattachment =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "usedifferentfolderforattachment" ) );
      attachmentfolder = XMLHandler.getTagValue( entrynode, "attachmentfolder" );
      attachmentwildcard = XMLHandler.getTagValue( entrynode, "attachmentwildcard" );
      valueimaplist =
        MailConnectionMeta.getValueImapListByCode( Const.NVL( XMLHandler
          .getTagValue( entrynode, "valueimaplist" ), "" ) );
      imapfirstmails = XMLHandler.getTagValue( entrynode, "imapfirstmails" );
      imapfolder = XMLHandler.getTagValue( entrynode, "imapfolder" );
      // search term
      senderSearch = XMLHandler.getTagValue( entrynode, "sendersearch" );
      notTermSenderSearch = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "nottermsendersearch" ) );
      receipientSearch = XMLHandler.getTagValue( entrynode, "receipientsearch" );
      notTermReceipientSearch =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "nottermreceipientsearch" ) );
      subjectSearch = XMLHandler.getTagValue( entrynode, "subjectsearch" );
      notTermSubjectSearch = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "nottermsubjectsearch" ) );
      bodySearch = XMLHandler.getTagValue( entrynode, "bodysearch" );
      notTermBodySearch = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "nottermbodysearch" ) );
      conditionReceivedDate =
        MailConnectionMeta.getConditionByCode( Const.NVL( XMLHandler.getTagValue(
          entrynode, "conditionreceiveddate" ), "" ) );
      notTermReceivedDateSearch =
        "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "nottermreceiveddatesearch" ) );
      receivedDate1 = XMLHandler.getTagValue( entrynode, "receivedDate1" );
      receivedDate2 = XMLHandler.getTagValue( entrynode, "receivedDate2" );
      actiontype =
        MailConnectionMeta.getActionTypeByCode( Const
          .NVL( XMLHandler.getTagValue( entrynode, "actiontype" ), "" ) );
      moveToIMAPFolder = XMLHandler.getTagValue( entrynode, "movetoimapfolder" );
      createmovetofolder = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "createmovetofolder" ) );
      createlocalfolder = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "createlocalfolder" ) );
      aftergetimap =
        MailConnectionMeta.getAfterGetIMAPByCode( Const.NVL(
          XMLHandler.getTagValue( entrynode, "aftergetimap" ), "" ) );
      includesubfolders = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "includesubfolders" ) );
      useproxy = "Y".equalsIgnoreCase( XMLHandler.getTagValue( entrynode, "useproxy" ) );
      proxyusername = XMLHandler.getTagValue( entrynode, "proxyusername" );
    } catch ( KettleXMLException xe ) {
      throw new KettleXMLException( "Unable to load job entry of type 'get pop' from XML node", xe );
    }
  }

  public int getValueImapList() {
    return valueimaplist;
  }

  public void setValueImapList( int value ) {
    this.valueimaplist = value;
  }

  public void loadRep( Repository rep, IMetaStore metaStore, ObjectId id_jobentry, List<DatabaseMeta> databases,
    List<SlaveServer> slaveServers ) throws KettleException {
    try {
      servername = rep.getJobEntryAttributeString( id_jobentry, "servername" );
      username = rep.getJobEntryAttributeString( id_jobentry, "username" );
      password =
        Encr.decryptPasswordOptionallyEncrypted( rep.getJobEntryAttributeString( id_jobentry, "password" ) );
      usessl = rep.getJobEntryAttributeBoolean( id_jobentry, "usessl" );
      sslport = rep.getJobEntryAttributeString( id_jobentry, "sslport" ); // backward compatible.
      outputdirectory = rep.getJobEntryAttributeString( id_jobentry, "outputdirectory" );
      filenamepattern = rep.getJobEntryAttributeString( id_jobentry, "filenamepattern" );
      if ( Utils.isEmpty( filenamepattern ) ) {
        filenamepattern = DEFAULT_FILE_NAME_PATTERN;
      }
      retrievemails = (int) rep.getJobEntryAttributeInteger( id_jobentry, "retrievemails" );
      firstmails = rep.getJobEntryAttributeString( id_jobentry, "firstmails" );
      delete = rep.getJobEntryAttributeBoolean( id_jobentry, "delete" );

      protocol =
        Const.NVL(
          rep.getJobEntryAttributeString( id_jobentry, "protocol" ), MailConnectionMeta.PROTOCOL_STRING_POP3 );

      String sv = rep.getJobEntryAttributeString( id_jobentry, "savemessage" );
      if ( Utils.isEmpty( sv ) ) {
        savemessage = true;
      } else {
        savemessage = rep.getJobEntryAttributeBoolean( id_jobentry, "savemessage" );
      }

      String sa = rep.getJobEntryAttributeString( id_jobentry, "saveattachment" );
      if ( Utils.isEmpty( sa ) ) {
        saveattachment = true;
      } else {
        saveattachment = rep.getJobEntryAttributeBoolean( id_jobentry, "saveattachment" );
      }

      usedifferentfolderforattachment =
        rep.getJobEntryAttributeBoolean( id_jobentry, "usedifferentfolderforattachment" );
      attachmentfolder = rep.getJobEntryAttributeString( id_jobentry, "attachmentfolder" );
      attachmentwildcard = rep.getJobEntryAttributeString( id_jobentry, "attachmentwildcard" );
      valueimaplist =
        MailConnectionMeta.getValueListImapListByCode( Const.NVL( rep.getJobEntryAttributeString(
          id_jobentry, "valueimaplist" ), "" ) );
      imapfirstmails = rep.getJobEntryAttributeString( id_jobentry, "imapfirstmails" );
      imapfolder = rep.getJobEntryAttributeString( id_jobentry, "imapfolder" );
      // search term
      senderSearch = rep.getJobEntryAttributeString( id_jobentry, "sendersearch" );
      notTermSenderSearch = rep.getJobEntryAttributeBoolean( id_jobentry, "nottermsendersearch" );
      receipientSearch = rep.getJobEntryAttributeString( id_jobentry, "receipientsearch" );
      notTermReceipientSearch = rep.getJobEntryAttributeBoolean( id_jobentry, "nottermreceipientsearch" );
      subjectSearch = rep.getJobEntryAttributeString( id_jobentry, "subjectsearch" );
      notTermSubjectSearch = rep.getJobEntryAttributeBoolean( id_jobentry, "nottermsubjectsearch" );
      bodySearch = rep.getJobEntryAttributeString( id_jobentry, "bodysearch" );
      notTermBodySearch = rep.getJobEntryAttributeBoolean( id_jobentry, "nottermbodysearch" );
      conditionReceivedDate =
        MailConnectionMeta.getConditionByCode( Const.NVL( rep.getJobEntryAttributeString(
          id_jobentry, "conditionreceiveddate" ), "" ) );
      notTermReceivedDateSearch = rep.getJobEntryAttributeBoolean( id_jobentry, "nottermreceiveddatesearch" );
      receivedDate1 = rep.getJobEntryAttributeString( id_jobentry, "receiveddate1" );
      receivedDate2 = rep.getJobEntryAttributeString( id_jobentry, "receiveddate2" );
      actiontype =
        MailConnectionMeta.getActionTypeByCode( Const.NVL( rep.getJobEntryAttributeString(
          id_jobentry, "actiontype" ), "" ) );
      moveToIMAPFolder = rep.getJobEntryAttributeString( id_jobentry, "movetoimapfolder" );
      createmovetofolder = rep.getJobEntryAttributeBoolean( id_jobentry, "createmovetofolder" );
      createlocalfolder = rep.getJobEntryAttributeBoolean( id_jobentry, "createlocalfolder" );
      aftergetimap =
        MailConnectionMeta.getAfterGetIMAPByCode( Const.NVL( rep.getJobEntryAttributeString(
          id_jobentry, "aftergetimap" ), "" ) );
      includesubfolders = rep.getJobEntryAttributeBoolean( id_jobentry, "includesubfolders" );
      useproxy = rep.getJobEntryAttributeBoolean( id_jobentry, "useproxy" );
      proxyusername = rep.getJobEntryAttributeString( id_jobentry, "proxyusername" );
    } catch ( KettleException dbe ) {
      throw new KettleException(
        "Unable to load job entry of type 'get pop' exists from the repository for id_jobentry=" + id_jobentry,
        dbe );
    }
  }

  public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_job ) throws KettleException {
    try {

      rep.saveJobEntryAttribute( id_job, getObjectId(), "servername", servername );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "username", username );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "password", Encr
        .encryptPasswordIfNotUsingVariables( password ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "usessl", usessl );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "sslport", sslport );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "outputdirectory", outputdirectory );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "filenamepattern", filenamepattern );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "retrievemails", retrievemails );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "firstmails", firstmails );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "delete", delete );

      rep.saveJobEntryAttribute( id_job, getObjectId(), "protocol", protocol );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "savemessage", savemessage );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "saveattachment", saveattachment );
      rep.saveJobEntryAttribute(
        id_job, getObjectId(), "usedifferentfolderforattachment", usedifferentfolderforattachment );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "attachmentfolder", attachmentfolder );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "attachmentwildcard", attachmentwildcard );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "valueimaplist", MailConnectionMeta
        .getValueImapListCode( valueimaplist ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "imapfirstmails", imapfirstmails );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "imapfolder", imapfolder );
      // search term
      rep.saveJobEntryAttribute( id_job, getObjectId(), "sendersearch", senderSearch );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "nottermsendersearch", notTermSenderSearch );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "receipientsearch", receipientSearch );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "nottermreceipientsearch", notTermReceipientSearch );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "subjectsearch", subjectSearch );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "nottermsubjectsearch", notTermSubjectSearch );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "bodysearch", bodySearch );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "nottermbodysearch", notTermBodySearch );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "conditionreceiveddate", MailConnectionMeta
        .getConditionDateCode( conditionReceivedDate ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "nottermreceiveddatesearch", notTermReceivedDateSearch );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "receiveddate1", receivedDate1 );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "receiveddate2", receivedDate2 );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "actiontype", MailConnectionMeta
        .getActionTypeCode( actiontype ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "movetoimapfolder", moveToIMAPFolder );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "createmovetofolder", createmovetofolder );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "createlocalfolder", createlocalfolder );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "aftergetimap", MailConnectionMeta
        .getAfterGetIMAPCode( aftergetimap ) );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "includesubfolders", includesubfolders );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "useproxy", useproxy );
      rep.saveJobEntryAttribute( id_job, getObjectId(), "proxyusername", proxyusername );
    } catch ( KettleDatabaseException dbe ) {
      throw new KettleException( "Unable to save job entry of type 'get pop' to the repository for id_job="
        + id_job, dbe );
    }

  }

  public String getPort() {
    return sslport;
  }

  public String getRealPort() {
    return environmentSubstitute( getPort() );
  }

  public void setPort( String sslport ) {
    this.sslport = sslport;
  }

  public void setFirstMails( String firstmails ) {
    this.firstmails = firstmails;
  }

  public String getFirstMails() {
    return firstmails;
  }

  public boolean isIncludeSubFolders() {
    return includesubfolders;
  }

  public void setIncludeSubFolders( boolean includesubfolders ) {
    this.includesubfolders = includesubfolders;
  }

  public void setFirstIMAPMails( String firstmails ) {
    this.imapfirstmails = firstmails;
  }

  public String getFirstIMAPMails() {
    return imapfirstmails;
  }

  public void setSenderSearchTerm( String senderSearch ) {
    this.senderSearch = senderSearch;
  }

  public String getSenderSearchTerm() {
    return this.senderSearch;
  }

  public void setNotTermSenderSearch( boolean notTermSenderSearch ) {
    this.notTermSenderSearch = notTermSenderSearch;
  }

  public boolean isNotTermSenderSearch() {
    return this.notTermSenderSearch;
  }

  public void setNotTermSubjectSearch( boolean notTermSubjectSearch ) {
    this.notTermSubjectSearch = notTermSubjectSearch;
  }

  public void setNotTermBodySearch( boolean notTermBodySearch ) {
    this.notTermBodySearch = notTermBodySearch;
  }

  public boolean isNotTermSubjectSearch() {
    return this.notTermSubjectSearch;
  }

  public boolean isNotTermBodySearch() {
    return this.notTermBodySearch;
  }

  public void setNotTermReceivedDateSearch( boolean notTermReceivedDateSearch ) {
    this.notTermReceivedDateSearch = notTermReceivedDateSearch;
  }

  public boolean isNotTermReceivedDateSearch() {
    return this.notTermReceivedDateSearch;
  }

  public void setNotTermReceipientSearch( boolean notTermReceipientSearch ) {
    this.notTermReceipientSearch = notTermReceipientSearch;
  }

  public boolean isNotTermReceipientSearch() {
    return this.notTermReceipientSearch;
  }

  public void setCreateMoveToFolder( boolean createfolder ) {
    this.createmovetofolder = createfolder;
  }

  public boolean isCreateMoveToFolder() {
    return this.createmovetofolder;
  }

  public void setReceipientSearch( String receipientSearch ) {
    this.receipientSearch = receipientSearch;
  }

  public String getReceipientSearch() {
    return this.receipientSearch;
  }

  public void setSubjectSearch( String subjectSearch ) {
    this.subjectSearch = subjectSearch;
  }

  public String getSubjectSearch() {
    return this.subjectSearch;
  }

  public void setBodySearch( String bodySearch ) {
    this.bodySearch = bodySearch;
  }

  public String getBodySearch() {
    return this.bodySearch;
  }

  public String getReceivedDate1() {
    return this.receivedDate1;
  }

  public void setReceivedDate1( String inputDate ) {
    this.receivedDate1 = inputDate;
  }

  public String getReceivedDate2() {
    return this.receivedDate2;
  }

  public void setReceivedDate2( String inputDate ) {
    this.receivedDate2 = inputDate;
  }

  public void setMoveToIMAPFolder( String foldername ) {
    this.moveToIMAPFolder = foldername;
  }

  public String getMoveToIMAPFolder() {
    return this.moveToIMAPFolder;
  }

  public void setCreateLocalFolder( boolean createfolder ) {
    this.createlocalfolder = createfolder;
  }

  public boolean isCreateLocalFolder() {
    return this.createlocalfolder;
  }

  public void setConditionOnReceivedDate( int conditionReceivedDate ) {
    this.conditionReceivedDate = conditionReceivedDate;
  }

  public int getConditionOnReceivedDate() {
    return this.conditionReceivedDate;
  }

  public void setActionType( int actiontype ) {
    this.actiontype = actiontype;
  }

  public int getActionType() {
    return this.actiontype;
  }

  public void setAfterGetIMAP( int afterget ) {
    this.aftergetimap = afterget;
  }

  public int getAfterGetIMAP() {
    return this.aftergetimap;
  }

  public String getRealFirstMails() {
    return environmentSubstitute( getFirstMails() );
  }

  public void setServerName( String servername ) {
    this.servername = servername;
  }

  public String getServerName() {
    return servername;
  }

  public void setUserName( String username ) {
    this.username = username;
  }

  public String getUserName() {
    return username;
  }

  public void setOutputDirectory( String outputdirectory ) {
    this.outputdirectory = outputdirectory;
  }

  public void setFilenamePattern( String filenamepattern ) {
    this.filenamepattern = filenamepattern;
  }

  /**
   * <li>0 = retrieve all <li>2 = retrieve unread
   *
   * @param nr
   * @see {@link #setValueImapList(int)}
   */
  public void setRetrievemails( int nr ) {
    retrievemails = nr;
  }

  public int getRetrievemails() {
    return this.retrievemails;
  }

  public String getFilenamePattern() {
    return filenamepattern;
  }

  public String getOutputDirectory() {
    return outputdirectory;
  }

  public String getRealOutputDirectory() {
    return environmentSubstitute( getOutputDirectory() );
  }

  public String getRealFilenamePattern() {
    return environmentSubstitute( getFilenamePattern() );
  }

  public String getRealUsername() {
    return environmentSubstitute( getUserName() );
  }

  public String getRealServername() {
    return environmentSubstitute( getServerName() );
  }

  public String getRealProxyUsername() {
    return environmentSubstitute( geProxyUsername() );
  }

  public String geProxyUsername() {
    return this.proxyusername;
  }

  /**
   * @return Returns the password.
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password string for resolving
   * @return Returns resolved decrypted password or null
   * in case of param returns null.
   */
  public String getRealPassword( String password ) {
    return Utils.resolvePassword( variables, password );
  }

  public String getAttachmentFolder() {
    return attachmentfolder;
  }

  public String getRealAttachmentFolder() {
    return environmentSubstitute( getAttachmentFolder() );
  }

  public void setAttachmentFolder( String foldername ) {
    this.attachmentfolder = foldername;
  }

  /**
   * @param delete
   *          The delete to set.
   */
  public void setDelete( boolean delete ) {
    this.delete = delete;
  }

  /**
   * @return Returns the delete.
   */
  public boolean getDelete() {
    return delete;
  }

  public String getProtocol() {
    return protocol;
  }

  public void setProtocol( String protocol ) {
    this.protocol = protocol;
  }

  public String getIMAPFolder() {
    return imapfolder;
  }

  public void setIMAPFolder( String folder ) {
    this.imapfolder = folder;
  }

  public void setAttachmentWildcard( String wildcard ) {
    attachmentwildcard = wildcard;
  }

  public String getAttachmentWildcard() {
    return attachmentwildcard;
  }

  /**
   * @param usessl
   *          The usessl to set.
   */
  public void setUseSSL( boolean usessl ) {
    this.usessl = usessl;
  }

  /**
   * @return Returns the usessl.
   */
  public boolean isUseSSL() {
    return this.usessl;
  }

  /**
   * @return Returns the useproxy.
   */
  public boolean isUseProxy() {
    return this.useproxy;
  }

  public void setUseProxy( boolean useprox ) {
    this.useproxy = useprox;
  }

  public boolean isSaveAttachment() {
    return saveattachment;
  }

  public void setProxyUsername( String username ) {
    this.proxyusername = username;
  }

  public String getProxyUsername() {
    return this.proxyusername;
  }

  public void setSaveAttachment( boolean saveattachment ) {
    this.saveattachment = saveattachment;
  }

  public boolean isSaveMessage() {
    return savemessage;
  }

  public void setSaveMessage( boolean savemessage ) {
    this.savemessage = savemessage;
  }

  public void setDifferentFolderForAttachment( boolean usedifferentfolder ) {
    this.usedifferentfolderforattachment = usedifferentfolder;
  }

  public boolean isDifferentFolderForAttachment() {
    return this.usedifferentfolderforattachment;
  }

  /**
   * @param password
   *          The password to set.
   */
  public void setPassword( String password ) {
    this.password = password;
  }

  public Result execute( Result previousResult, int nr ) throws KettleException {
    Result result = previousResult;
    result.setResult( false );

    //FileObject fileObject = null;
    MailConnection mailConn = null;
    Date beginDate = null;
    Date endDate = null;

    SimpleDateFormat df = new SimpleDateFormat( DATE_PATTERN );

    try {

      boolean usePOP3 = getProtocol().equals( MailConnectionMeta.PROTOCOL_STRING_POP3 );
      boolean moveafter = false;
      int nbrmailtoretrieve =
        usePOP3 ? ( getRetrievemails() == 2 ? Const.toInt( getFirstMails(), 0 ) : 0 ) : Const.toInt(
          getFirstIMAPMails(), 0 );

      String realOutputFolder = createOutputDirectory( JobEntryGetPOP.FOLDER_OUTPUT );
      String targetAttachmentFolder = createOutputDirectory( JobEntryGetPOP.FOLDER_ATTACHMENTS );

      // Check destination folder
      String realMoveToIMAPFolder = environmentSubstitute( getMoveToIMAPFolder() );
      if ( getProtocol().equals( MailConnectionMeta.PROTOCOL_STRING_IMAP )
        && ( getActionType() == MailConnectionMeta.ACTION_TYPE_MOVE )
        || ( getActionType() == MailConnectionMeta.ACTION_TYPE_GET
        && getAfterGetIMAP() == MailConnectionMeta.AFTER_GET_IMAP_MOVE ) ) {
        if ( Utils.isEmpty( realMoveToIMAPFolder ) ) {
          throw new KettleException( BaseMessages
            .getString( PKG, "JobGetMailsFromPOP.Error.MoveToIMAPFolderEmpty" ) );
        }
        moveafter = true;
      }

      // check search terms
      // Received Date
      switch ( getConditionOnReceivedDate() ) {
        case MailConnectionMeta.CONDITION_DATE_EQUAL:
        case MailConnectionMeta.CONDITION_DATE_GREATER:
        case MailConnectionMeta.CONDITION_DATE_SMALLER:
          String realBeginDate = environmentSubstitute( getReceivedDate1() );
          if ( Utils.isEmpty( realBeginDate ) ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "JobGetMailsFromPOP.Error.ReceivedDateSearchTermEmpty" ) );
          }
          beginDate = df.parse( realBeginDate );
          break;
        case MailConnectionMeta.CONDITION_DATE_BETWEEN:
          realBeginDate = environmentSubstitute( getReceivedDate1() );
          if ( Utils.isEmpty( realBeginDate ) ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "JobGetMailsFromPOP.Error.ReceivedDatesSearchTermEmpty" ) );
          }
          beginDate = df.parse( realBeginDate );
          String realEndDate = environmentSubstitute( getReceivedDate2() );
          if ( Utils.isEmpty( realEndDate ) ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "JobGetMailsFromPOP.Error.ReceivedDatesSearchTermEmpty" ) );
          }
          endDate = df.parse( realEndDate );
          break;
        default:
          break;
      }

      String realserver = getRealServername();
      String realusername = getRealUsername();
      String realpassword = getRealPassword( getPassword() );
      String realFilenamePattern = getRealFilenamePattern();
      int realport = Const.toInt( environmentSubstitute( sslport ), -1 );
      String realIMAPFolder = environmentSubstitute( getIMAPFolder() );
      String realProxyUsername = getRealProxyUsername();

      initVariables();
      // create a mail connection object
      mailConn =
        new MailConnection(
          log, MailConnectionMeta.getProtocolFromString( getProtocol(), MailConnectionMeta.PROTOCOL_IMAP ),
          realserver, realport, realusername, realpassword, isUseSSL(), isUseProxy(), realProxyUsername );
      // connect
      mailConn.connect();

      if ( moveafter ) {
        // Set destination folder
        // Check if folder exists
        mailConn.setDestinationFolder( realMoveToIMAPFolder, isCreateMoveToFolder() );
      }

      // apply search term?
      String realSearchSender = environmentSubstitute( getSenderSearchTerm() );
      if ( !Utils.isEmpty( realSearchSender ) ) {
        // apply FROM
        mailConn.setSenderTerm( realSearchSender, isNotTermSenderSearch() );
      }
      String realSearchReceipient = environmentSubstitute( getReceipientSearch() );
      if ( !Utils.isEmpty( realSearchReceipient ) ) {
        // apply TO
        mailConn.setReceipientTerm( realSearchReceipient );
      }
      String realSearchSubject = environmentSubstitute( getSubjectSearch() );
      if ( !Utils.isEmpty( realSearchSubject ) ) {
        // apply Subject
        mailConn.setSubjectTerm( realSearchSubject, isNotTermSubjectSearch() );
      }
      String realSearchBody = environmentSubstitute( getBodySearch() );
      if ( !Utils.isEmpty( realSearchBody ) ) {
        // apply body
        mailConn.setBodyTerm( realSearchBody, isNotTermBodySearch() );
      }
      // Received Date
      switch ( getConditionOnReceivedDate() ) {
        case MailConnectionMeta.CONDITION_DATE_EQUAL:
          mailConn.setReceivedDateTermEQ( beginDate );
          break;
        case MailConnectionMeta.CONDITION_DATE_GREATER:
          mailConn.setReceivedDateTermGT( beginDate );
          break;
        case MailConnectionMeta.CONDITION_DATE_SMALLER:
          mailConn.setReceivedDateTermLT( beginDate );
          break;
        case MailConnectionMeta.CONDITION_DATE_BETWEEN:
          mailConn.setReceivedDateTermBetween( beginDate, endDate );
          break;
        default:
          break;
      }
      // set FlagTerm?
      if ( usePOP3 ) {
        // retrieve messages
        if ( getRetrievemails() == 1 ) {
          // New messages
          // POP doesn't support the concept of "new" messages!
          mailConn.setFlagTermUnread();
        }
      } else {
        switch ( getValueImapList() ) {
          case MailConnectionMeta.VALUE_IMAP_LIST_NEW:
            mailConn.setFlagTermNew();
            break;
          case MailConnectionMeta.VALUE_IMAP_LIST_OLD:
            mailConn.setFlagTermOld();
            break;
          case MailConnectionMeta.VALUE_IMAP_LIST_READ:
            mailConn.setFlagTermRead();
            break;
          case MailConnectionMeta.VALUE_IMAP_LIST_UNREAD:
            mailConn.setFlagTermUnread();
            break;
          case MailConnectionMeta.VALUE_IMAP_LIST_FLAGGED:
            mailConn.setFlagTermFlagged();
            break;
          case MailConnectionMeta.VALUE_IMAP_LIST_NOT_FLAGGED:
            mailConn.setFlagTermNotFlagged();
            break;
          case MailConnectionMeta.VALUE_IMAP_LIST_DRAFT:
            mailConn.setFlagTermDraft();
            break;
          case MailConnectionMeta.VALUE_IMAP_LIST_NOT_DRAFT:
            mailConn.setFlagTermNotDraft();
            break;
          default:
            break;
        }
      }
      // open folder and retrieve messages
      fetchOneFolder(
        mailConn, usePOP3, realIMAPFolder, realOutputFolder, targetAttachmentFolder, realMoveToIMAPFolder,
        realFilenamePattern, nbrmailtoretrieve, df );

      if ( isIncludeSubFolders() ) {
        // Fetch also sub folders?
        if ( isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "JobGetPOP.FetchingSubFolders" ) );
        }
        String[] subfolders = mailConn.returnAllFolders();
        if ( subfolders.length == 0 ) {
          if ( isDebug() ) {
            logDebug( BaseMessages.getString( PKG, "JobGetPOP.NoSubFolders" ) );
          }
        } else {
          for ( int i = 0; i < subfolders.length; i++ ) {
            fetchOneFolder(
              mailConn, usePOP3, subfolders[i], realOutputFolder, targetAttachmentFolder, realMoveToIMAPFolder,
              realFilenamePattern, nbrmailtoretrieve, df );
          }
        }
      }

      result.setResult( true );
      result.setNrFilesRetrieved( mailConn.getSavedAttachedFilesCounter() );
      result.setNrLinesWritten( mailConn.getSavedMessagesCounter() );
      result.setNrLinesDeleted( mailConn.getDeletedMessagesCounter() );
      result.setNrLinesUpdated( mailConn.getMovedMessagesCounter() );

      if ( isDetailed() ) {
        logDetailed( "=======================================" );
        logDetailed( BaseMessages.getString( PKG, "JobGetPOP.Log.Info.SavedMessages", ""
          + mailConn.getSavedMessagesCounter() ) );
        logDetailed( BaseMessages.getString( PKG, "JobGetPOP.Log.Info.DeletedMessages", ""
          + mailConn.getDeletedMessagesCounter() ) );
        logDetailed( BaseMessages.getString( PKG, "JobGetPOP.Log.Info.MovedMessages", ""
          + mailConn.getMovedMessagesCounter() ) );
        if ( getActionType() == MailConnectionMeta.ACTION_TYPE_GET && isSaveAttachment() ) {
          logDetailed( BaseMessages.getString( PKG, "JobGetPOP.Log.Info.AttachedMessagesSuccess", ""
            + mailConn.getSavedAttachedFilesCounter() ) );
        }
        logDetailed( "=======================================" );
      }
    } catch ( Exception e ) {
      result.setNrErrors( 1 );
      logError( "Unexpected error: " + e.getMessage() );
      logError( Const.getStackTracker( e ) );
    } finally {
      try {
        if ( mailConn != null ) {
          mailConn.disconnect();
          mailConn = null;
        }
      } catch ( Exception e ) { /* Ignore */
      }
    }

    return result;
  }

  void fetchOneFolder( MailConnection mailConn, boolean usePOP3, String realIMAPFolder,
    String realOutputFolder, String targetAttachmentFolder, String realMoveToIMAPFolder,
    String realFilenamePattern, int nbrmailtoretrieve, SimpleDateFormat df ) throws KettleException {
    try {
      // if it is not pop3 and we have non-default imap folder...
      if ( !usePOP3 && !Utils.isEmpty( realIMAPFolder ) ) {
        mailConn.openFolder( realIMAPFolder, true );
      } else {
        mailConn.openFolder( true );
      }

      mailConn.retrieveMessages();

      int messagesCount = mailConn.getMessagesCount();

      if ( isDetailed() ) {
        logDetailed( BaseMessages.getString( PKG, "JobGetMailsFromPOP.TotalMessagesFolder.Label", ""
          + messagesCount, Const.NVL( mailConn.getFolderName(), MailConnectionMeta.INBOX_FOLDER ) ) );
      }

      messagesCount =
        nbrmailtoretrieve > 0
          ? ( nbrmailtoretrieve > messagesCount ? messagesCount : nbrmailtoretrieve ) : messagesCount;

      if ( messagesCount > 0 ) {
        switch ( getActionType() ) {
          case MailConnectionMeta.ACTION_TYPE_DELETE:
            if ( nbrmailtoretrieve > 0 ) {
              // We need to fetch all messages in order to retrieve
              // only the first nbrmailtoretrieve ...
              for ( int i = 0; i < messagesCount && !parentJob.isStopped(); i++ ) {
                // Get next message
                mailConn.fetchNext();
                // Delete this message
                mailConn.deleteMessage();
                if ( isDebug() ) {
                  logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.MessageDeleted", "" + i ) );
                }
              }
            } else {
              // Delete messages
              mailConn.deleteMessages( true );
              if ( isDebug() ) {
                logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.MessagesDeleted", "" + messagesCount ) );
              }
            }
            break;
          case MailConnectionMeta.ACTION_TYPE_MOVE:
            if ( nbrmailtoretrieve > 0 ) {
              // We need to fetch all messages in order to retrieve
              // only the first nbrmailtoretrieve ...
              for ( int i = 0; i < messagesCount && !parentJob.isStopped(); i++ ) {
                // Get next message
                mailConn.fetchNext();
                // Move this message
                mailConn.moveMessage();
                if ( isDebug() ) {
                  logDebug( BaseMessages.getString(
                    PKG, "JobGetMailsFromPOP.MessageMoved", "" + i, realMoveToIMAPFolder ) );
                }
              }
            } else {
              // Move all messages
              mailConn.moveMessages();
              if ( isDebug() ) {
                logDebug( BaseMessages.getString(
                  PKG, "JobGetMailsFromPOP.MessagesMoved", "" + messagesCount, realMoveToIMAPFolder ) );
              }
            }
            break;
          default:
            // Get messages and save it in a local file
            // also save attached files if needed
            for ( int i = 0; i < messagesCount && !parentJob.isStopped(); i++ ) {
              // Get next message
              mailConn.fetchNext();
              int messagenumber = mailConn.getMessage().getMessageNumber();
              boolean okPOP3 = usePOP3 ? true : false; // (mailConn.getMessagesCounter()<nbrmailtoretrieve &&
                                                       // retrievemails==2)||(retrievemails!=2):false;
              boolean okIMAP = !usePOP3;

              if ( okPOP3 || okIMAP ) {
                // display some infos on the current message
                //
                if ( isDebug() && mailConn.getMessage() != null ) {
                  logDebug( "--------------------------------------------------" );
                  logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.MessageNumber.Label", ""
                    + messagenumber ) );
                  if ( mailConn.getMessage().getReceivedDate() != null ) {
                    logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.ReceivedDate.Label", df
                      .format( mailConn.getMessage().getReceivedDate() ) ) );
                  }
                  logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.ContentType.Label", mailConn
                    .getMessage().getContentType() ) );
                  logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.EmailFrom.Label", Const.NVL( mailConn
                    .getMessage().getFrom()[0].toString(), "" ) ) );
                  logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.EmailSubject.Label", Const.NVL(
                    mailConn.getMessage().getSubject(), "" ) ) );
                }
                if ( isSaveMessage() ) {
                  // get local message filename
                  String localfilename_message = replaceTokens( realFilenamePattern, i );

                  if ( isDebug() ) {
                    logDebug( BaseMessages.getString(
                      PKG, "JobGetMailsFromPOP.LocalFilename.Label", localfilename_message ) );
                  }

                  // save message content in the file
                  mailConn.saveMessageContentToFile( localfilename_message, realOutputFolder );
                  // PDI-10942 explicitly set message as read
                  mailConn.getMessage().setFlag( Flag.SEEN, true );

                  if ( isDetailed() ) {
                    logDetailed( BaseMessages.getString( PKG, "JobGetMailsFromPOP.MessageSaved.Label", ""
                      + messagenumber, localfilename_message, realOutputFolder ) );
                  }
                }

                // Do we need to save attached file?
                if ( isSaveAttachment() ) {
                  mailConn.saveAttachedFiles( targetAttachmentFolder, attachementPattern );
                }
                // We successfully retrieved message
                // do we need to make another action (delete, move)?
                if ( usePOP3 ) {
                  if ( getDelete() ) {
                    mailConn.deleteMessage();
                    if ( isDebug() ) {
                      logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.MessageDeleted", ""
                        + messagenumber ) );
                    }
                  }
                } else {
                  switch ( getAfterGetIMAP() ) {
                    case MailConnectionMeta.AFTER_GET_IMAP_DELETE:
                      // Delete messages
                      mailConn.deleteMessage();
                      if ( isDebug() ) {
                        logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.MessageDeleted", ""
                          + messagenumber ) );
                      }
                      break;
                    case MailConnectionMeta.AFTER_GET_IMAP_MOVE:
                      // Move messages
                      mailConn.moveMessage();
                      if ( isDebug() ) {
                        logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.MessageMoved", ""
                          + messagenumber, realMoveToIMAPFolder ) );
                      }
                      break;
                    default:
                  }
                }

              }
            }
            break;
        }
      }
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  public boolean evaluates() {
    return true;
  }

  private String replaceTokens( String aString, int idfile ) {
    String localfilename_message = aString;
    localfilename_message = localfilename_message.replaceAll( FILENAME_ID_PATTERN, "" + ( idfile + 1 ) );
    localfilename_message =
      substituteDate( localfilename_message, FILENAME_SYS_DATE_OPEN, FILENAME_SYS_DATE_CLOSE, new Date() );
    return localfilename_message;

  }

  private String substituteDate( String aString, String open, String close, Date datetime ) {
    if ( aString == null ) {
      return null;
    }
    StringBuilder buffer = new StringBuilder();
    String rest = aString;

    // search for closing string
    int i = rest.indexOf( open );
    while ( i > -1 ) {
      int j = rest.indexOf( close, i + open.length() );
      // search for closing string
      if ( j > -1 ) {
        String varName = rest.substring( i + open.length(), j );
        DateFormat dateFormat = new SimpleDateFormat( varName );
        Object Value = dateFormat.format( datetime );

        buffer.append( rest.substring( 0, i ) );
        buffer.append( Value );
        rest = rest.substring( j + close.length() );
      } else {
        // no closing tag found; end the search
        buffer.append( rest );
        rest = "";
      }
      // keep searching
      i = rest.indexOf( close );
    }
    buffer.append( rest );
    return buffer.toString();
  }

  private void initVariables() {
    // Attachment wildcard
    attachementPattern = null;
    String realAttachmentWildcard = environmentSubstitute( getAttachmentWildcard() );
    if ( !Utils.isEmpty( realAttachmentWildcard ) ) {
      attachementPattern = Pattern.compile( realAttachmentWildcard );
    }
  }

  public void check( List<CheckResultInterface> remarks, JobMeta jobMeta, VariableSpace space,
    Repository repository, IMetaStore metaStore ) {
    JobEntryValidatorUtils.andValidator().validate( this, "serverName", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "userName", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notBlankValidator() ) );
    JobEntryValidatorUtils.andValidator().validate( this, "password", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.notNullValidator() ) );

    ValidatorContext ctx = new ValidatorContext();
    AbstractFileValidator.putVariableSpace( ctx, getVariables() );
    AndValidator.putValidators( ctx, JobEntryValidatorUtils.notBlankValidator(),
        JobEntryValidatorUtils.fileExistsValidator() );
    JobEntryValidatorUtils.andValidator().validate( this, "outputDirectory", remarks, ctx );

    JobEntryValidatorUtils.andValidator().validate( this, "SSLPort", remarks,
        AndValidator.putValidators( JobEntryValidatorUtils.integerValidator() ) );
  }

  public List<ResourceReference> getResourceDependencies( JobMeta jobMeta ) {
    List<ResourceReference> references = super.getResourceDependencies( jobMeta );
    if ( !Utils.isEmpty( servername ) ) {
      String realServername = jobMeta.environmentSubstitute( servername );
      ResourceReference reference = new ResourceReference( this );
      reference.getEntries().add( new ResourceEntry( realServername, ResourceType.SERVER ) );
      references.add( reference );
    }
    return references;
  }

  String createOutputDirectory( int folderType ) throws KettleException, FileSystemException, IllegalArgumentException {
    if ( ( folderType != JobEntryGetPOP.FOLDER_OUTPUT ) && ( folderType != JobEntryGetPOP.FOLDER_ATTACHMENTS ) ) {
      throw new IllegalArgumentException( "Invalid folderType argument" );
    }
    String folderName = "";
    switch ( folderType ) {
      case JobEntryGetPOP.FOLDER_OUTPUT:
        folderName = getRealOutputDirectory();
        break;
      case JobEntryGetPOP.FOLDER_ATTACHMENTS:
        if ( isSaveAttachment() && isDifferentFolderForAttachment() ) {
          folderName = getRealAttachmentFolder();
        } else {
          folderName = getRealOutputDirectory();
        }
        break;
    }
    if ( Utils.isEmpty( folderName ) ) {
      switch ( folderType ) {
        case JobEntryGetPOP.FOLDER_OUTPUT:
          throw new KettleException( BaseMessages
            .getString( PKG, "JobGetMailsFromPOP.Error.OutputFolderEmpty" ) );
        case JobEntryGetPOP.FOLDER_ATTACHMENTS:
          throw new KettleException( BaseMessages
            .getString( PKG, "JobGetMailsFromPOP.Error.AttachmentFolderEmpty" ) );
      }
    }
    FileObject folder = KettleVFS.getFileObject( folderName, this );
    if ( folder.exists() ) {
      if ( folder.getType() != FileType.FOLDER ) {
        switch ( folderType ) {
          case JobEntryGetPOP.FOLDER_OUTPUT:
            throw new KettleException( BaseMessages.getString(
              PKG, "JobGetMailsFromPOP.Error.NotAFolderNot", folderName ) );
          case JobEntryGetPOP.FOLDER_ATTACHMENTS:
            throw new KettleException( BaseMessages.getString(
              PKG, "JobGetMailsFromPOP.Error.AttachmentFolderNotAFolder", folderName ) );
        }
      }
      if ( isDebug() ) {
        switch ( folderType ) {
          case JobEntryGetPOP.FOLDER_OUTPUT:
            logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.Log.OutputFolderExists", folderName ) );
            break;
          case JobEntryGetPOP.FOLDER_ATTACHMENTS:
            logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.Log.AttachmentFolderExists", folderName ) );
            break;
        }
      }
    } else {
      if ( isCreateLocalFolder() ) {
        folder.createFolder();
      } else {
        switch ( folderType ) {
          case JobEntryGetPOP.FOLDER_OUTPUT:
            throw new KettleException( BaseMessages.getString(
              PKG, "JobGetMailsFromPOP.Error.OutputFolderNotExist", folderName ) );
          case JobEntryGetPOP.FOLDER_ATTACHMENTS:
            throw new KettleException( BaseMessages.getString(
              PKG, "JobGetMailsFromPOP.Error.AttachmentFolderNotExist", folderName ) );
        }
      }
    }

    String returnValue = KettleVFS.getFilename( folder );
    try {
      folder.close();
    } catch ( IOException ignore ) {
      //Ignore error, as the folder was created successfully
    }
    return returnValue;
  }
}
