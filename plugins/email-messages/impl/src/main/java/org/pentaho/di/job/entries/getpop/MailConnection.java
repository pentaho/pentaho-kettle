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

import com.google.common.annotations.VisibleForTesting;
import com.sun.mail.imap.IMAPSSLStore;
import com.sun.mail.pop3.POP3SSLStore;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;

import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeUtility;
import javax.mail.search.AndTerm;
import javax.mail.search.BodyTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.NotTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MailConnection handles the process of connecting to, reading from POP3/IMAP.
 *
 * @author Samatar
 * @since 01-04-2009
 *
 */

public class MailConnection {
  private static Class<?> PKG = JobEntryGetPOP.class; // for i18n purposes, needed by Translator2!!

  /**
   * Target mail server.
   */
  private String server;

  private int port;
  private String username;
  private String password;
  private boolean usessl;
  private boolean write;
  private boolean useproxy;
  private String proxyusername;
  /**
   * Protocol used. Should be PROTOCOL_POP3 (0) for POP3 and PROTOCOL_IMAP (1) to IMAP
   */
  private int protocol;

  private Properties prop;
  private Session session = null;
  private Store store = null;
  private Folder folder = null;
  /**
   * Contains the list of retrieved messages
   */
  private Message[] messages;
  /**
   * Contains the current message
   */
  private Message message;
  private SearchTerm searchTerm = null;

  /**
   * Counts the number of message fetched
   */
  private int messagenr;

  /**
   * Counts the number of message saved in a file
   */
  private int nrSavedMessages;

  /**
   * Counts the number of message move to a folder
   */
  private int nrMovedMessages;

  /**
   * Counts the number of message deleted
   */
  private int nrDeletedMessages;

  /**
   * Counts the number of attached files saved in a file
   */
  private int nrSavedAttachedFiles;

  /**
   * IMAP folder if user want to move some messages
   */
  private Folder destinationIMAPFolder = null;

  private LogChannelInterface log;

  /**
   * Construct a new Database MailConnection
   *
   * @param protocol
   *          the protocol used : MailConnection.PROTOCOL_POP3 or MailConnection.PROTOCOL_IMAP.
   * @param server
   *          the target server (ip ou name)
   * @param port
   *          port number on the server
   * @param password
   * @param usessl
   *          specify if the connection is established via SSL
   * @param useproxy
   *          specify if we use proxy authentication
   * @param proxyusername
   *          proxy authorised user
   */
  public MailConnection( LogChannelInterface log, int protocol, String server, int port, String username,
    String password, boolean usessl, boolean useproxy, String proxyusername ) throws KettleException {

    this.log = log;

    // Get system properties
    try {
      this.prop = System.getProperties();
    } catch ( SecurityException s ) {
      this.prop = new Properties();
    }

    this.port = port;
    this.server = server;
    this.username = username;
    this.password = password;
    this.usessl = usessl;
    this.protocol = protocol;
    this.nrSavedMessages = 0;
    this.nrDeletedMessages = 0;
    this.nrMovedMessages = 0;
    this.nrSavedAttachedFiles = 0;
    this.messagenr = -1;
    this.useproxy = useproxy;
    this.proxyusername = proxyusername;

    try {

      if ( useproxy ) {
        // Need here to pass a proxy
        // use SASL authentication
        this.prop.put( "mail.imap.sasl.enable", "true" );
        this.prop.put( "mail.imap.sasl.authorizationid", proxyusername );
      }

      if ( protocol == MailConnectionMeta.PROTOCOL_POP3 ) {
        this.prop.setProperty( "mail.pop3s.rsetbeforequit", "true" );
        this.prop.setProperty( "mail.pop3.rsetbeforequit", "true" );
      } else if ( protocol == MailConnectionMeta.PROTOCOL_MBOX ) {
        this.prop.setProperty( "mstor.mbox.metadataStrategy", "none" ); // mstor.mbox.metadataStrategy={none|xml|yaml}
        this.prop.setProperty( "mstor.cache.disabled", "true" ); // prevent diskstore fail
      }

      String protocolString =
        ( protocol == MailConnectionMeta.PROTOCOL_POP3 ) ? "pop3" : protocol == MailConnectionMeta.PROTOCOL_MBOX
          ? "mstor" : "imap";
      if ( usessl && protocol != MailConnectionMeta.PROTOCOL_MBOX ) {
        // Supports IMAP/POP3 connection with SSL, the connection is established via SSL.
        this.prop
          .setProperty( "mail." + protocolString + ".socketFactory.class", "javax.net.ssl.SSLSocketFactory" );
        this.prop.setProperty( "mail." + protocolString + ".socketFactory.fallback", "false" );
        this.prop.setProperty( "mail." + protocolString + ".port", "" + port );
        this.prop.setProperty( "mail." + protocolString + ".socketFactory.port", "" + port );

        // Create session object
        this.session = Session.getInstance( this.prop, null );
        this.session.setDebug( log.isDebug() );
        if ( this.port == -1 ) {
          this.port =
            ( ( protocol == MailConnectionMeta.PROTOCOL_POP3 )
              ? MailConnectionMeta.DEFAULT_SSL_POP3_PORT : MailConnectionMeta.DEFAULT_SSL_IMAP_PORT );
        }
        URLName url = new URLName( protocolString, server, port, "", username, password );
        this.store =
          ( protocol == MailConnectionMeta.PROTOCOL_POP3 )
            ? new POP3SSLStore( this.session, url ) : new IMAPSSLStore( this.session, url );
        url = null;
      } else {
        this.session = Session.getInstance( this.prop, null );
        this.session.setDebug( log.isDebug() );
        if ( protocol == MailConnectionMeta.PROTOCOL_MBOX ) {
          this.store = this.session.getStore( new URLName( protocolString + ":" + server ) );
        } else {
          this.store = this.session.getStore( protocolString );
        }
      }

      if ( log.isDetailed() ) {
        log.logDetailed( BaseMessages.getString( PKG, "JobGetMailsFromPOP.NewConnectionDefined" ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobGetMailsFromPOP.Error.NewConnection", Const.NVL(
        this.server, "" ) ), e );
    }
  }

  /**
   * @return Returns the connection status. true if the connection is still opened
   */
  public boolean isConnected() {
    return ( this.store != null && this.store.isConnected() );
  }

  /**
   * @return Returns the use of SSL. true if the connection use SSL
   */
  public boolean isUseSSL() {
    return this.usessl;
  }

  /**
   * @return Returns the use of proxy. true if the connection use proxy
   */
  public boolean isUseProxy() {
    return this.useproxy;
  }

  /**
   * @return Returns the proxy username.
   */
  public String getProxyUsername() {
    return this.proxyusername;
  }

  /**
   * @return Returns the store
   *
   */
  public Store getStore() {
    return this.store;
  }

  /**
   * @return Returns the folder
   *
   */
  public Folder getFolder() {
    return this.folder;
  }

  /**
   * Open the connection.
   *
   * @throws KettleException
   *           if something went wrong.
   */
  public void connect() throws KettleException {
    if ( log.isDetailed() ) {
      log.logDetailed( BaseMessages.getString(
        PKG, "JobGetMailsFromPOP.Connecting", this.server, this.username, "" + this.port ) );
    }
    try {
      if ( this.usessl || this.protocol == MailConnectionMeta.PROTOCOL_MBOX ) {
        // Supports IMAP/POP3 connection with SSL,
        // the connection is established via SSL.
        this.store.connect();
      } else {
        if ( this.port > -1 ) {
          this.store.connect( this.server, this.port, this.username, this.password );
        } else {
          this.store.connect( this.server, this.username, this.password );
        }
      }
      if ( log.isDetailed() ) {
        log.logDetailed( BaseMessages.getString(
          PKG, "JobGetMailsFromPOP.Connected", this.server, this.username, "" + this.port ) );
      }
    } catch ( Exception e ) {
      throw new KettleException(
        BaseMessages.getString( PKG, "JobGetMailsFromPOP.Error.Connecting", this.server, this.username, Const
          .NVL( "" + this.port, "" ) ), e );
    }
  }

  /**
   * Open the default folder (INBOX)
   *
   * @param write
   *          open the folder in write mode
   * @throws KettleException
   *           if something went wrong.
   */
  public void openFolder( boolean write ) throws KettleException {
    openFolder( null, true, write );
  }

  /**
   * Open the folder.
   *
   * @param foldername
   *          the name of the folder to open
   * @param write
   *          open the folder in write mode
   * @throws KettleException
   *           if something went wrong.
   */
  public void openFolder( String foldername, boolean write ) throws KettleException {
    openFolder( foldername, false, write );
  }

  /**
   * Open the folder.
   *
   * @param foldername
   *          the name of the folder to open
   * @param defaultFolder
   *          true to open the default folder (INBOX)
   * @param write
   *          open the folder in write mode
   * @throws KettleException
   *           if something went wrong.
   */
  public void openFolder( String foldername, boolean defaultFolder, boolean write ) throws KettleException {
    this.write = write;
    try {
      if ( getFolder() != null ) {
        // A folder is already opened
        // before make sure to close it
        closeFolder( true );
      }

      if ( defaultFolder ) {
        if ( protocol == MailConnectionMeta.PROTOCOL_MBOX ) {
          this.folder = this.store.getDefaultFolder();
        } else {
          // get the default folder
          this.folder = getRecursiveFolder( MailConnectionMeta.INBOX_FOLDER );
        }

        if ( this.folder == null ) {
          throw new KettleException( BaseMessages.getString( PKG, "JobGetMailsFromPOP.InvalidDefaultFolder.Label" ) );
        }

        if ( ( folder.getType() & Folder.HOLDS_MESSAGES ) == 0 ) {
          throw new KettleException( BaseMessages.getString( PKG, "MailConnection.DefaultFolderCanNotHoldMessage" ) );
        }
      } else {
        // Open specified Folder (for IMAP/MBOX)
        if ( this.protocol == MailConnectionMeta.PROTOCOL_IMAP
          || this.protocol == MailConnectionMeta.PROTOCOL_MBOX ) {
          this.folder = getRecursiveFolder( foldername );
        }
        if ( this.folder == null || !this.folder.exists() ) {
          throw new KettleException( BaseMessages.getString( PKG, "JobGetMailsFromPOP.InvalidFolder.Label" ) );
        }
      }
      if ( this.write ) {
        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString(
            PKG, "MailConnection.OpeningFolderInWriteMode.Label", getFolderName() ) );
        }
        this.folder.open( Folder.READ_WRITE );
      } else {
        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString(
            PKG, "MailConnection.OpeningFolderInReadMode.Label", getFolderName() ) );
        }
        this.folder.open( Folder.READ_ONLY );
      }

      if ( log.isDetailed() ) {
        log.logDetailed( BaseMessages.getString( PKG, "JobGetMailsFromPOP.FolderOpened.Label", getFolderName() ) );
      }
      if ( log.isDebug() ) {
        // display some infos on folder
        //CHECKSTYLE:LineLength:OFF
        log.logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.FolderOpened.Name", getFolderName() ) );
        log.logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.FolderOpened.FullName", this.folder.getFullName() ) );
        log.logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.FolderOpened.Url", this.folder.getURLName().toString() ) );
        log.logDebug( BaseMessages.getString( PKG, "JobGetMailsFromPOP.FolderOpened.Subscribed", "" + this.folder.isSubscribed() ) );
      }

    } catch ( Exception e ) {
      throw new KettleException( defaultFolder
        ? BaseMessages.getString( PKG, "JobGetMailsFromPOP.Error.OpeningDefaultFolder" )
        : BaseMessages.getString( PKG, "JobGetMailsFromPOP.Error.OpeningFolder", foldername ), e );
    }
  }

  private Folder getRecursiveFolder( String foldername ) throws MessagingException {
    Folder dfolder;
    String[] folderparts = foldername.split( "/" );
    dfolder = this.getStore().getDefaultFolder();
    // Open destination folder
    for ( int i = 0; i < folderparts.length; i++ ) {
      dfolder = dfolder.getFolder( folderparts[i] );
    }
    return dfolder;
  }

  /**
   * Clear search terms.
   */
  public void clearFilters() {
    this.nrSavedMessages = 0;
    this.nrDeletedMessages = 0;
    this.nrMovedMessages = 0;
    this.nrSavedAttachedFiles = 0;
    if ( this.searchTerm != null ) {
      this.searchTerm = null;
    }
  }

  /**
   * Disconnect from the server and close folder, connection.
   *
   * @throws KettleException
   */
  public void disconnect() throws KettleException {
    disconnect( true );
  }

  /**
   * Close folder.
   *
   * @param expunge
   *          expunge folder
   * @throws KettleException
   */
  public void closeFolder( boolean expunge ) throws KettleException {
    try {
      if ( this.folder != null && this.folder.isOpen() ) {
        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString( PKG, "MailConnection.ClosingFolder", getFolderName() ) );
        }
        this.folder.close( expunge );
        this.folder = null;
        this.messages = null;
        this.message = null;
        this.messagenr = -1;
        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString( PKG, "MailConnection.FolderClosed", getFolderName() ) );
        }
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "JobGetMailsFromPOP.Error.ClosingFolder", getFolderName() ), e );
    }
  }

  /**
   * Add search term.
   *
   * @param term
   *          search term to add
   */
  private void addSearchTerm( SearchTerm term ) {
    if ( this.searchTerm != null ) {
      this.searchTerm = new AndTerm( this.searchTerm, term );
    } else {
      this.searchTerm = term;
    }
  }

  public SearchTerm getSearchTerm() {
    return searchTerm;
  }

  /**
   * Set filter on subject.
   *
   * @param subject
   *          messages will be filtered on subject
   * @param notTerm
   *          negate condition
   */
  public void setSubjectTerm( String subject, boolean notTerm ) {
    if ( !Utils.isEmpty( subject ) ) {
      if ( notTerm ) {
        addSearchTerm( new NotTerm( new SubjectTerm( subject ) ) );
      } else {
        addSearchTerm( new SubjectTerm( subject ) );
      }
    }
  }

  /**
   * Search all messages with body containing the word bodyfilter
   *
   * @param bodyfilter
   * @param notTerm
   *          negate condition
   */
  public void setBodyTerm( String bodyfilter, boolean notTerm ) {
    if ( !Utils.isEmpty( bodyfilter ) ) {
      if ( notTerm ) {
        addSearchTerm( new NotTerm( new BodyTerm( bodyfilter ) ) );
      } else {
        addSearchTerm( new BodyTerm( bodyfilter ) );
      }
    }
  }

  /**
   * Set filter on message sender.
   *
   * @param sender
   *          messages will be filtered on sender
   * @param notTerm
   *          negate condition
   */
  public void setSenderTerm( String sender, boolean notTerm ) {
    if ( !Utils.isEmpty( sender ) ) {
      if ( notTerm ) {
        addSearchTerm( new NotTerm( new FromStringTerm( sender ) ) );
      } else {
        addSearchTerm( new FromStringTerm( sender ) );
      }
    }
  }

  /**
   * Set filter on receipient.
   *
   * @param receipient
   *          messages will be filtered on receipient
   */
  public void setReceipientTerm( String receipient ) {
    if ( !Utils.isEmpty( receipient ) ) {
      addSearchTerm( new RecipientStringTerm( Message.RecipientType.TO, receipient ) );
    }
  }

  /**
   * Set filter on message received date.
   *
   * @param receiveddate
   *          messages will be filtered on receiveddate
   */
  public void setReceivedDateTermEQ( Date receiveddate ) {
    if ( this.protocol == MailConnectionMeta.PROTOCOL_POP3 ) {
      log.logError( BaseMessages.getString( PKG, "MailConnection.Error.ReceivedDatePOP3Unsupported" ) );
    } else {
      addSearchTerm( new ReceivedDateTerm( ComparisonTerm.EQ, receiveddate ) );
    }
  }

  /**
   * Set filter on message received date.
   *
   * @param futureDate
   *          messages will be filtered on futureDate
   */
  public void setReceivedDateTermLT( Date futureDate ) {
    if ( this.protocol == MailConnectionMeta.PROTOCOL_POP3 ) {
      log.logError( BaseMessages.getString( PKG, "MailConnection.Error.ReceivedDatePOP3Unsupported" ) );
    } else {
      addSearchTerm( new ReceivedDateTerm( ComparisonTerm.LT, futureDate ) );
    }
  }

  /**
   * Set filter on message received date.
   *
   * @param pastDate
   *          messages will be filtered on pastDate
   */
  public void setReceivedDateTermGT( Date pastDate ) {
    if ( this.protocol == MailConnectionMeta.PROTOCOL_POP3 ) {
      log.logError( BaseMessages.getString( PKG, "MailConnection.Error.ReceivedDatePOP3Unsupported" ) );
    } else {
      addSearchTerm( new ReceivedDateTerm( ComparisonTerm.GT, pastDate ) );
    }
  }

  public void setReceivedDateTermBetween( Date beginDate, Date endDate ) {
    if ( this.protocol == MailConnectionMeta.PROTOCOL_POP3 ) {
      log.logError( BaseMessages.getString( PKG, "MailConnection.Error.ReceivedDatePOP3Unsupported" ) );
    } else {
      addSearchTerm( new AndTerm( new ReceivedDateTerm( ComparisonTerm.LT, endDate ), new ReceivedDateTerm(
        ComparisonTerm.GT, beginDate ) ) );
    }
  }

  public void setFlagTermNew() {
    addSearchTerm( new FlagTerm( new Flags( Flags.Flag.RECENT ), true ) );
  }

  public void setFlagTermOld() {
    addSearchTerm( new FlagTerm( new Flags( Flags.Flag.RECENT ), false ) );
  }

  public void setFlagTermRead() {
    addSearchTerm( new FlagTerm( new Flags( Flags.Flag.SEEN ), true ) );
  }

  public void setFlagTermUnread() {
    addSearchTerm( new FlagTerm( new Flags( Flags.Flag.SEEN ), false ) );
  }

  public void setFlagTermFlagged() {
    addSearchTerm( new FlagTerm( new Flags( Flags.Flag.FLAGGED ), true ) );
  }

  public void setFlagTermNotFlagged() {
    addSearchTerm( new FlagTerm( new Flags( Flags.Flag.FLAGGED ), false ) );
  }

  public void setFlagTermDraft() {
    addSearchTerm( new FlagTerm( new Flags( Flags.Flag.DRAFT ), true ) );
  }

  public void setFlagTermNotDraft() {
    addSearchTerm( new FlagTerm( new Flags( Flags.Flag.DRAFT ), false ) );
  }

  /**
   * Retrieve all messages from server
   *
   * @throws KettleException
   */
  public void retrieveMessages() throws KettleException {
    try {
      // search term?
      if ( this.searchTerm != null ) {
        this.messages = this.folder.search( this.searchTerm );
      } else {
        this.messages = this.folder.getMessages();
      }
    } catch ( Exception e ) {
      this.messages = null;
      throw new KettleException( BaseMessages.getString(
        PKG, "MailConnection.Error.RetrieveMessages", getFolderName() ), e );
    }
  }

  /*
   * public void retrieveUnreadMessages() throws Exception { if(this.protocol==PROTOCOL_POP3) throw new
   * KettleException("Cette fonction est uniquement accessible pour le protocol POP3!"); try { Message msgsAll[]; //
   * search term? if(this.searchTerm!=null) { msgsAll = this.folder.search(this.searchTerm); }else { msgsAll =
   * this.folder.getMessages(); } int unreadMsgs = this.folder.getUnreadMessageCount(); int msgCount = msgsAll.length;
   *
   * this.messages = this.folder.getMessages(msgCount - unreadMsgs + 1, msgCount); } catch (Exception e) {
   * this.messages= null; } }
   */

  /**
   * Disconnect from the server and close folder, connection.
   *
   * @param expunge
   *          expunge folder
   * @throws KettleException
   */
  public void disconnect( boolean expunge ) throws KettleException {
    if ( log.isDebug() ) {
      log.logDebug( BaseMessages.getString( PKG, "MailConnection.ClosingConnection" ) );
    }
    try {
      // close the folder, passing in a true value to expunge the deleted message
      closeFolder( expunge );
      clearFilters();
      if ( this.store != null ) {
        this.store.close();
        this.store = null;
      }
      if ( this.session != null ) {
        this.session = null;
      }
      if ( this.destinationIMAPFolder != null ) {
        this.destinationIMAPFolder.close( expunge );
      }

      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "MailConnection.ConnectionClosed" ) );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "JobGetMailsFromPOP.Error.ClosingConnection" ), e );
    }
  }

  /**
   * Export message content to a filename.
   *
   * @param filename
   *          the target filename
   * @param foldername
   *          the parent folder of filename
   * @throws KettleException
   */

  public void saveMessageContentToFile( String filename, String foldername ) throws KettleException {
    OutputStream os = null;
    try {
      os = KettleVFS.getOutputStream( foldername + ( foldername.endsWith( "/" ) ? "" : "/" ) + filename, false );
      getMessage().writeTo( os );
      updateSavedMessagesCounter();
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "MailConnection.Error.SavingMessageContent", ""
        + this.message.getMessageNumber(), filename, foldername ), e );
    } finally {
      if ( os != null ) {
        IOUtils.closeQuietly( os );
      }
    }
  }

  /**
   * Save attached files to a folder.
   *
   * @param foldername
   *          the target foldername
   * @throws KettleException
   */
  public void saveAttachedFiles( String foldername ) throws KettleException {
    saveAttachedFiles( foldername, null );
  }

  /**
   * Save attached files to a folder.
   *
   * @param foldername
   *          the target foldername
   * @param pattern
   *          regular expression to filter on files
   * @throws KettleException
   */
  public void saveAttachedFiles( String foldername, Pattern pattern ) throws KettleException {
    Object content = null;
    try {
      content = getMessage().getContent();
      if ( content instanceof Multipart ) {
        handleMultipart( foldername, (Multipart) content, pattern );
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "MailConnection.Error.SavingAttachedFiles", ""
        + this.message.getMessageNumber(), foldername ), e );
    } finally {
      if ( content != null ) {
        content = null;
      }
    }
  }

  private void handleMultipart( String foldername, Multipart multipart, Pattern pattern ) throws KettleException {
    try {
      for ( int i = 0, n = multipart.getCount(); i < n; i++ ) {
        handlePart( foldername, multipart.getBodyPart( i ), pattern );
      }
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  private void handlePart( String foldername, Part part, Pattern pattern ) throws KettleException {
    try {
      String disposition = part.getDisposition();

      // The RFC2183 doesn't REQUIRE Content-Disposition header field so we'll create one to
      // fake out the code below.
      if ( disposition == null || disposition.length() < 1 ) {
        disposition = Part.ATTACHMENT;
      }

      if ( disposition.equalsIgnoreCase( Part.ATTACHMENT ) || disposition.equalsIgnoreCase( Part.INLINE ) ) {
        String MimeText = null;
        try {
          MimeText = MimeUtility.decodeText( part.getFileName() );
        } catch ( Exception e ) {
          // Ignore errors
        }
        if ( MimeText != null ) {
          String filename = MimeUtility.decodeText( part.getFileName() );
          if ( isWildcardMatch( filename, pattern ) ) {
            // Save file
            saveFile( foldername, filename, part.getInputStream() );
            updateSavedAttachedFilesCounter();
            if ( log.isDetailed() ) {
              log.logDetailed( BaseMessages.getString( PKG, "JobGetMailsFromPOP.AttachedFileSaved", filename, ""
                + getMessage().getMessageNumber(), foldername ) );
            }
          }
        }
      }
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  @VisibleForTesting
  static String findValidTarget( String folderName, final String fileName ) throws KettleException {
    if ( fileName == null || folderName == null ) {
      throw new IllegalArgumentException( "Cannot have null arguments to findValidTarget" );
    }
    String fileNameRoot = FilenameUtils.getBaseName( fileName ), ext = "." + FilenameUtils.getExtension( fileName );
    if ( ( ext.length() == 1 ) ) { // only a "."
      ext = "";
    }
    String rtn = "", base = FilenameUtils.concat( folderName, fileNameRoot );
    int baseSz = base.length();
    StringBuilder build = new StringBuilder( baseSz ).append( base );
    int i = -1;
    do {
      i++;
      build.setLength( baseSz ); // bring string back to size
      build.append( i > 0 ? Integer.toString( i ) : "" ).append( ext );
      rtn = build.toString();
    } while ( KettleVFS.fileExists( rtn ) );

    return rtn;
  }

  private static void saveFile( String foldername, String filename, InputStream input ) throws KettleException {
    OutputStream fos = null;
    BufferedOutputStream bos = null;
    BufferedInputStream bis = null;
    try {
      // Do no overwrite existing file
      String targetFileName;
      if ( filename == null ) {
        File f = File.createTempFile( "xx", ".out" );
        f.deleteOnExit(); // Clean up file
        filename = f.getName();
        targetFileName = foldername + "/" + filename; // Note - createTempFile Used - so will be unique
      } else {
        targetFileName = findValidTarget( foldername, filename );
      }
      fos = KettleVFS.getOutputStream( targetFileName, false );
      bos = new BufferedOutputStream( fos );
      bis = new BufferedInputStream( input );
      IOUtils.copy( bis, bos );
      bos.flush();
    } catch ( Exception e ) {
      throw new KettleException( e );
    } finally {
      if ( bis != null ) {
        IOUtils.closeQuietly( bis );
        bis = null; // Help the GC
      }
      if ( bos != null ) {
        IOUtils.closeQuietly( bos );
        bos = null; // Help the GC
        // Note - closing the BufferedOuputStream closes the underlying output stream according to the Javadoc
      }
    }
  }

  private boolean isWildcardMatch( String filename, Pattern pattern ) {
    boolean retval = true;
    if ( pattern != null ) {
      Matcher matcher = pattern.matcher( filename );
      retval = ( matcher.matches() );
    }
    return retval;
  }

  /**
   * Delete current fetched message
   *
   * @throws KettleException
   */
  public void deleteMessage() throws KettleException {
    try {
      this.message.setFlag( Flags.Flag.DELETED, true );
      updateDeletedMessagesCounter();
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "MailConnection.Error.DeletingMessage", ""
        + getMessage().getMessageNumber() ), e );
    }
  }

  /**
   * Set destination folder
   *
   * @param foldername
   *          destination foldername
   * @param createFolder
   *          flag create folder if needed
   * @throws KettleException
   */
  public void setDestinationFolder( String foldername, boolean createFolder ) throws KettleException {
    try {
      String[] folderparts = foldername.split( "/" );
      Folder f = this.getStore().getDefaultFolder();
      // Open destination folder
      for ( int i = 0; i < folderparts.length; i++ ) {
        f = f.getFolder( folderparts[i] );
        if ( !f.exists() ) {
          if ( createFolder ) {
            // Create folder
            f.create( Folder.HOLDS_MESSAGES );
          } else {
            throw new KettleException( BaseMessages.getString( PKG, "MailConnection.Error.FolderNotFound", foldername ) );
          }
        }
      }
      this.destinationIMAPFolder = f;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  /**
   * Move current message to a target folder. (IMAP) You must call setDestinationFolder before calling this method
   *
   * @throws KettleException
   */
  public void moveMessage() throws KettleException {
    try {
      // move all messages
      this.folder.copyMessages( new Message[] { this.message }, this.destinationIMAPFolder );
      updatedMovedMessagesCounter();
      // Make sure to delete messages
      deleteMessage();
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "MailConnection.Error.MovingMessage", ""
        + getMessage().getMessageNumber(), this.destinationIMAPFolder.getName() ), e );

    }
  }

  /**
   * Returns the foldername.
   *
   * @return foldername
   */
  public String getFolderName() {
    if ( this.folder == null ) {
      return "";
    }
    return this.folder.getName();
  }

  /**
   * Returns the server name/Ip.
   *
   * @return server
   */
  public String getServer() {
    return server;
  }

  /**
   * Returns the protocol.
   *
   * @return protocol
   */
  public int getProtocol() {
    return protocol;
  }

  /**
   * Returns all messages.
   *
   * @return all messages
   */
  public Message[] getMessages() {
    return messages;
  }

  private void updateMessageNr() {
    this.messagenr++;
  }

  private int getMessageNr() {
    return this.messagenr;
  }

  /**
   * Get next message.
   *
   * @throws KettleException
   */
  public void fetchNext() throws KettleException {
    updateMessageNr();
    try {
      this.message = this.messages[getMessageNr()];
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "MailConnection.Error.FetchingMessages" ), e );
    }
  }

  /**
   * Returns the current message.
   *
   * @return current message
   */
  public Message getMessage() {
    return this.message;
  }

  /**
   * Returns the number of messages.
   *
   * @return messages count
   */
  public int getMessagesCount() {
    return this.messages.length;
  }

  public void updateSavedMessagesCounter() {
    this.nrSavedMessages++;
  }

  public int getSavedMessagesCounter() {
    return this.nrSavedMessages;
  }

  public int getSavedAttachedFilesCounter() {
    return this.nrSavedAttachedFiles;
  }

  public void updateSavedAttachedFilesCounter() {
    this.nrSavedAttachedFiles++;
  }

  public int getDeletedMessagesCounter() {
    return this.nrDeletedMessages;
  }

  private void updateDeletedMessagesCounter() {
    this.nrDeletedMessages++;
  }

  private void setDeletedMessagesCounter() {
    this.nrDeletedMessages = getMessagesCount();
  }

  /**
   * Returns count of moved messages.
   *
   * @return count of moved messages
   */
  public int getMovedMessagesCounter() {
    return this.nrMovedMessages;
  }

  /**
   * Update count of moved messages.
   */
  private void updatedMovedMessagesCounter() {
    this.nrMovedMessages++;
  }

  /**
   * Set count of moved messages.
   */
  private void setMovedMessagesCounter() {
    this.nrMovedMessages = getMessagesCount();
  }

  /**
   * Delete messages.
   *
   * @throws KettleException
   */
  public void deleteMessages( boolean setCounter ) throws KettleException {
    try {
      this.folder.setFlags( this.messages, new Flags( Flags.Flag.DELETED ), true );
      if ( setCounter ) {
        setDeletedMessagesCounter();
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "MailConnection.Error.DeletingMessage" ), e );
    }
  }

  /**
   * Move messages to a folder. You must call setDestinationFolder before calling this method
   *
   * @throws KettleException
   */
  public void moveMessages() throws KettleException {
    try {
      this.folder.copyMessages( this.messages, this.destinationIMAPFolder );
      deleteMessages( false );
      setMovedMessagesCounter();
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "MailConnection.Error.MovingMessages", this.destinationIMAPFolder.getName() ), e );
    }
  }

  /**
   * Check if a folder exists on server (only IMAP).
   *
   * @param foldername
   *          the name of the folder
   * @return true is folder exists
   */
  public boolean folderExists( String foldername ) {
    boolean retval = false;
    Folder dfolder = null;
    try {
      // Open destination folder
      dfolder = getRecursiveFolder( foldername );
      if ( dfolder.exists() ) {
        retval = true;
      }
    } catch ( Exception e ) {
      // Ignore errors
    } finally {
      try {
        if ( dfolder != null ) {
          dfolder.close( false );
        }
      } catch ( Exception e ) { /* Ignore */
      }
    }
    return retval;
  }

  private HashSet<String> returnSubfolders( Folder folder ) throws KettleException {
    HashSet<String> list = new HashSet<String>();
    try {
      if ( ( folder.getType() & Folder.HOLDS_FOLDERS ) != 0 ) {
        Folder[] f = folder.list();
        for ( int i = 0; i < f.length; i++ ) {
          // Search for sub folders
          if ( ( f[i].getType() & Folder.HOLDS_FOLDERS ) != 0 ) {
            list.add( f[i].getFullName() );
            list.addAll( returnSubfolders( f[i] ) );
          }
        }
      }
    } catch ( MessagingException m ) {
      throw new KettleException( m );
    }
    return list;
  }

  /**
   * Returns all subfolders of the specified folder
   *
   * @param folder
   *          parent folder
   * @return sub folders
   */
  public String[] returnAllFolders( Folder folder ) throws KettleException {
    HashSet<String> list = new HashSet<String>();
    list = returnSubfolders( folder );
    return list.toArray( new String[list.size()] );
  }

  /**
   * Returns all subfolders of the current folder
   *
   * @return sub folders
   */
  public String[] returnAllFolders() throws KettleException {
    return returnAllFolders( getFolder() );
  }

  /**
   * Returns all subfolders of the folder folder
   *
   * @param folder
   *          target folder
   * @return sub folders
   */
  public String[] returnAllFolders( String folder ) throws KettleException {

    Folder dfolder = null;
    String[] retval = null;
    try {
      if ( Utils.isEmpty( folder ) ) {
        // Default folder
        dfolder = getStore().getDefaultFolder();
      } else {
        dfolder = getStore().getFolder( folder );
      }
      retval = returnAllFolders( dfolder );
    } catch ( Exception e ) {
      // Ignore errors
    } finally {
      try {
        if ( dfolder != null ) {
          dfolder.close( false );
        }
      } catch ( Exception e ) { /* Ignore */
      }
    }
    return retval;
  }

  public String getMessageBody() throws Exception {
    return getMessageBody( getMessage() );
  }

  /**
   * Return the primary text content of the message.
   */
  public String getMessageBody( Message m ) throws MessagingException, IOException {
    return getMessageBodyOrContentType( m, false );
  }

  public String getMessageBodyContentType( Message m ) throws MessagingException, IOException {
    return getMessageBodyOrContentType( m, true );
  }

  private String getMessageBodyOrContentType( Part p, final boolean returnContentType ) throws MessagingException,
    IOException {
    if ( p.isMimeType( "text/*" ) ) {
      String s = (String) p.getContent();
      return returnContentType ? p.getContentType() : s;
    }

    if ( p.isMimeType( "multipart/alternative" ) ) {
      // prefer html text over plain text
      Multipart mp = (Multipart) p.getContent();
      String text = null;
      for ( int i = 0; i < mp.getCount(); i++ ) {
        Part bp = mp.getBodyPart( i );
        if ( bp.isMimeType( "text/plain" ) ) {
          if ( text == null ) {
            text = getMessageBodyOrContentType( bp, returnContentType );
          }
        }
      }
      return text;
    } else if ( p.isMimeType( "multipart/*" ) ) {
      Multipart mp = (Multipart) p.getContent();
      for ( int i = 0; i < mp.getCount(); i++ ) {
        String s = getMessageBodyOrContentType( mp.getBodyPart( i ), returnContentType );
        if ( s != null ) {
          return s;
        }
      }
    }

    return null;
  }

  /**
   * Returns if message is new
   *
   * @return true if new message
   */
  public boolean isMessageNew() {
    return isMessageNew( getMessage() );
  }

  public boolean isMessageNew( Message msg ) {
    try {
      return msg.isSet( Flag.RECENT );
    } catch ( MessagingException e ) {
      return false;
    }
  }

  /**
   * Returns if message is read
   *
   * @return true if message is read
   */
  public boolean isMessageRead() {
    return isMessageRead( getMessage() );
  }

  public boolean isMessageRead( Message msg ) {
    try {
      return msg.isSet( Flag.SEEN );
    } catch ( MessagingException e ) {
      return false;
    }
  }

  /**
   * Returns if message is read
   *
   * @return true if message is flagged
   */
  public boolean isMessageFlagged() {
    return isMessageFlagged( getMessage() );
  }

  public boolean isMessageFlagged( Message msg ) {
    try {
      return msg.isSet( Flag.FLAGGED );
    } catch ( MessagingException e ) {
      return false;
    }
  }

  /**
   * Returns if message is deleted
   *
   * @return true if message is deleted
   */
  public boolean isMessageDeleted() {
    return isMessageDeleted( getMessage() );
  }

  public boolean isMessageDeleted( Message msg ) {
    try {
      return msg.isSet( Flag.DELETED );
    } catch ( MessagingException e ) {
      return false;
    }
  }

  /**
   * Returns if message is Draft
   *
   * @return true if message is Draft
   */
  public boolean isMessageDraft() {
    return isMessageDraft( getMessage() );
  }

  public boolean isMessageDraft( Message msg ) {
    try {
      return msg.isSet( Flag.DRAFT );
    } catch ( MessagingException e ) {
      return false;
    }
  }

  public String toString() {
    if ( getServer() != null ) {
      return getServer();
    } else {
      return "-";
    }
  }

  /**
   * Returns attached files count for the current message
   *
   * @return true if message is Draft
   * @param pattern
   *          (optional)
   */
  public int getAttachedFilesCount( Pattern pattern ) throws KettleException {
    return getAttachedFilesCount( getMessage(), pattern );
  }

  public int getAttachedFilesCount( Message message, Pattern pattern ) throws KettleException {
    Object content = null;
    int retval = 0;
    try {
      content = message.getContent();
      if ( content instanceof Multipart ) {
        Multipart multipart = (Multipart) content;
        for ( int i = 0, n = multipart.getCount(); i < n; i++ ) {
          Part part = multipart.getBodyPart( i );
          String disposition = part.getDisposition();

          if ( ( disposition != null )
            && ( disposition.equalsIgnoreCase( Part.ATTACHMENT ) || disposition.equalsIgnoreCase( Part.INLINE ) ) ) {
            String MimeText = null;
            try {
              MimeText = MimeUtility.decodeText( part.getFileName() );
            } catch ( Exception e ) {
              // Ignore errors
            }
            if ( MimeText != null ) {
              String filename = MimeUtility.decodeText( part.getFileName() );
              if ( isWildcardMatch( filename, pattern ) ) {
                retval++;
              }
            }
          }
        }
      }
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG, "MailConnection.Error.CountingAttachedFiles", ""
        + this.message.getMessageNumber() ), e );
    } finally {
      if ( content != null ) {
        content = null;
      }
    }
    return retval;
  }
}
