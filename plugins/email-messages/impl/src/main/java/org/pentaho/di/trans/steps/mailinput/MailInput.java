/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.trans.steps.mailinput;

import jakarta.mail.Header;
import jakarta.mail.Message;
import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.commons.lang3.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entries.getpop.IEmailAuthenticationResponse;
import org.pentaho.di.job.entries.getpop.MailConnection;
import org.pentaho.di.job.entries.getpop.MailConnectionMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

/**
 * Read data from POP3/IMAP server and input data to the next steps.
 *
 * @author Samatar
 * @since 21-08-2009
 */

public class MailInput extends BaseStep implements StepInterface {
  private static Class<?> PKG = MailInputMeta.class; // for i18n purposes, needed by Translator2!!

  private MailInputMeta meta;
  private MailInputData data;

  private final MessageParser instance = new MessageParser();

  private IEmailAuthenticationResponse token;
  public MailInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
    Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  @Override
  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (MailInputMeta) smi;
    data = (MailInputData) sdi;

    Object[] outputRowData = getOneRow();

    if ( outputRowData == null ) { // no more input to be expected...
      setOutputDone();
      return false;
    }

    if ( isRowLevel() ) {
      log.logRowlevel( toString(), BaseMessages.getString( PKG, "MailInput.Log.OutputRow", data.outputRowMeta
        .getString( outputRowData ) ) );
    }
    putRow( data.outputRowMeta, outputRowData ); // copy row to output rowset(s);

    if ( data.rowlimit > 0 && data.rownr >= data.rowlimit ) { // limit has been reached: stop now.
      setOutputDone();
      return false;
    }

    return true;
  }

  public String[] getFolders( String realIMAPFolder ) throws KettleException {
    data.folderenr = 0;
    data.messagesCount = 0;
    data.rownr = 0;
    String[] folderslist = null;
    if ( meta.isIncludeSubFolders() ) {
      String[] folderslist0 = data.mailConn.returnAllFolders( realIMAPFolder );
      if ( folderslist0 == null || folderslist0.length == 0 ) {
        // mstor's default folder has no name
        folderslist =
          data.mailConn.getProtocol() == MailConnectionMeta.PROTOCOL_MBOX
            ? new String[] { "" }
            : new String[] { Const.NVL( realIMAPFolder, MailConnectionMeta.INBOX_FOLDER ) };
      } else {
        folderslist = new String[folderslist0.length + 1];
        folderslist[0] = Const.NVL( realIMAPFolder, MailConnectionMeta.INBOX_FOLDER );
        for ( int i = 0; i < folderslist0.length; i++ ) {
          folderslist[i + 1] = folderslist0[i];
        }
      }
    } else {
      folderslist =
        data.mailConn.getProtocol() == MailConnectionMeta.PROTOCOL_MBOX
          ? new String[] { "" }
          : new String[] { Const.NVL( realIMAPFolder, MailConnectionMeta.INBOX_FOLDER ) };
    }
    return folderslist;
  }

  private void applySearch( Date beginDate, Date endDate ) {
    // apply search term?
    String realSearchSender = environmentSubstitute( meta.getSenderSearchTerm() );
    if ( !Utils.isEmpty( realSearchSender ) ) {
      // apply FROM
      data.mailConn.setSenderTerm( realSearchSender, meta.isNotTermSenderSearch() );
    }
    String realSearchRecipient = environmentSubstitute( meta.getRecipientSearch() );
    if ( !Utils.isEmpty( realSearchRecipient ) ) {
      // apply TO
      data.mailConn.setReceipientTerm( realSearchRecipient );
    }
    String realSearchSubject = environmentSubstitute( meta.getSubjectSearch() );
    if ( !Utils.isEmpty( realSearchSubject ) ) {
      // apply Subject
      data.mailConn.setSubjectTerm( realSearchSubject, meta.isNotTermSubjectSearch() );
    }
    // Received Date
    switch ( meta.getConditionOnReceivedDate() ) {
      case MailConnectionMeta.CONDITION_DATE_EQUAL:
        data.mailConn.setReceivedDateTermEQ( beginDate );
        break;
      case MailConnectionMeta.CONDITION_DATE_GREATER:
        data.mailConn.setReceivedDateTermGT( beginDate );
        break;
      case MailConnectionMeta.CONDITION_DATE_SMALLER:
        data.mailConn.setReceivedDateTermLT( beginDate );
        break;
      case MailConnectionMeta.CONDITION_DATE_BETWEEN:
        data.mailConn.setReceivedDateTermBetween( beginDate, endDate );
        break;
      default:
        break;
    }
    // set FlagTerm?
    if ( !data.usePOP ) {
      //POP3 does not support any flags.
      //but still use ones for IMAP and maybe for MBOX?
      switch ( meta.getValueImapList() ) {
        case MailConnectionMeta.VALUE_IMAP_LIST_NEW:
          data.mailConn.setFlagTermNew();
          break;
        case MailConnectionMeta.VALUE_IMAP_LIST_OLD:
          data.mailConn.setFlagTermOld();
          break;
        case MailConnectionMeta.VALUE_IMAP_LIST_READ:
          data.mailConn.setFlagTermRead();
          break;
        case MailConnectionMeta.VALUE_IMAP_LIST_UNREAD:
          data.mailConn.setFlagTermUnread();
          break;
        case MailConnectionMeta.VALUE_IMAP_LIST_FLAGGED:
          data.mailConn.setFlagTermFlagged();
          break;
        case MailConnectionMeta.VALUE_IMAP_LIST_NOT_FLAGGED:
          data.mailConn.setFlagTermNotFlagged();
          break;
        case MailConnectionMeta.VALUE_IMAP_LIST_DRAFT:
          data.mailConn.setFlagTermDraft();
          break;
        case MailConnectionMeta.VALUE_IMAP_LIST_NOT_DRAFT:
          data.mailConn.setFlagTermNotDraft();
          break;
        default:
          break;
      }
    }
  }

  /**
   * Build an empty row based on the meta-data...
   *
   * @return
   */
  private Object[] buildEmptyRow() {
    return RowDataUtil.allocateRowData( data.outputRowMeta.size() );
  }

  private boolean isFolderExhausted() {
    return data.folder == null || !data.folderIterator.hasNext();
  }

  private Object[] getOneRow() throws KettleException {

    while ( isFolderExhausted() ) {
      if ( !openNextFolder() ) {
        return null;
      }
    }

    Object[] r = buildEmptyRow();
    if ( meta.isDynamicFolder() ) {
      System.arraycopy( data.readrow, 0, r, 0, data.readrow.length );
    }

    try {
      Message message = data.folderIterator.next();

      if ( isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "MailInput.Log.FetchingMessage", message.getMessageNumber() ) );
      }

      try {
        instance.parseToArray( r, message );
      } catch ( Exception e ) {
        String msg = e.getMessage();
        if ( meta.isStopOnError() ) {
          throw new KettleException( msg, e );
        } else {
          logError( msg, e );
        }
      }

      incrementLinesInput();
      data.rownr++;
    } catch ( Exception e ) {
      throw new KettleException( "Error adding values to row!", e );
    }

    return r;
  }

  private boolean openNextFolder() {
    try {
      if ( !meta.isDynamicFolder() ) {
        // static folders list
        // let's check if we fetched all values in list
        if ( data.folderenr >= data.folders.length ) {
          // We have fetched all folders
          if ( isDetailed() ) {
            logDetailed( BaseMessages.getString( PKG, "MailInput.Log.FinishedProcessing" ) );
          }
          return false;
        }
      } else {
        // dynamic folders
        if ( first ) {
          first = false;

          data.readrow = getRow(); // Get row from input rowset & set row busy!
          if ( data.readrow == null ) {
            if ( isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "MailInput.Log.FinishedProcessing" ) );
            }
            return false;
          }

          data.inputRowMeta = getInputRowMeta();
          data.outputRowMeta = data.inputRowMeta.clone();
          meta.getFields( getTransMeta().getBowl(), data.outputRowMeta, getStepname(), null, null, this, repository,
            metaStore );

          // Get total previous fields
          data.totalpreviousfields = data.inputRowMeta.size();

          if ( Utils.isEmpty( meta.getFolderField() ) ) {
            logError( BaseMessages.getString( PKG, "MailInput.Error.DynamicFolderFieldMissing" ) );
            stopAll();
            setErrors( 1 );
            return false;
          }

          data.indexOfFolderField = data.inputRowMeta.indexOfValue( meta.getFolderField() );
          if ( data.indexOfFolderField < 0 ) {
            logError( BaseMessages.getString( PKG, "MailInput.Error.DynamicFolderUnreachable", meta
              .getFolderField() ) );
            stopAll();
            setErrors( 1 );
            return false;
          }

          // get folder
          String folderName = data.inputRowMeta.getString( data.readrow, data.indexOfFolderField );
          if ( isDebug() ) {
            logDebug( BaseMessages.getString(
              PKG, "MailInput.Log.FoldernameInStream", meta.getFolderField(), folderName ) );
          }
          data.folders = getFolders( folderName );
        } // end if first

        if ( data.folderenr >= data.folders.length ) {
          // we have fetched all values for input row
          // grab another row
          data.readrow = getRow(); // Get row from input rowset & set row busy!
          if ( data.readrow == null ) {
            if ( isDetailed() ) {
              logDetailed( BaseMessages.getString( PKG, "MailInput.Log.FinishedProcessing" ) );
            }
            return false;
          }
          // get folder
          data.folders = getFolders( data.inputRowMeta.getString( data.readrow, data.indexOfFolderField ) );
        }
      }

      data.start = parseIntWithSubstitute( meta.getStart() );
      data.end = parseIntWithSubstitute( meta.getEnd() );
      // Get the current folder
      data.folder = data.folders[data.folderenr];

      // Move folder pointer ahead!
      data.folderenr++;

      // open folder
      if ( !data.usePOP && !Utils.isEmpty( data.folder ) ) {
        data.mailConn.openFolder( data.folder, false );
      } else {
        data.mailConn.openFolder( false );
      }

      if ( meta.useBatch() || ( !Utils.isEmpty( environmentSubstitute( meta.getFirstMails() ) )
                                  && Integer.parseInt( environmentSubstitute( meta.getFirstMails() ) ) > 0  ) ) {
        // get data by pieces
        Integer batchSize = meta.useBatch() ? meta.getBatchSize()
            : Integer.parseInt( environmentSubstitute( meta.getFirstMails() ) );
        Integer start = meta.useBatch() ? data.start : 1;
        Integer end = meta.useBatch() ? data.end : batchSize;
        data.folderIterator =
          new BatchFolderIterator( data.mailConn.getFolder(), batchSize, start, end ); // TODO:args

        if ( data.mailConn.getSearchTerm() != null ) { // add search filter
          data.folderIterator =
            new SearchEnabledFolderIterator( data.folderIterator, data.mailConn.getSearchTerm() );
        }
      } else { // fetch all
        data.mailConn.retrieveMessages();
        data.folderIterator = new ArrayIterator( data.mailConn.getMessages() );
      }

      if ( isDebug() ) {
        logDebug( BaseMessages.getString( PKG, "MailInput.Log.MessagesInFolder", data.folder, data.messagesCount ) );
      }
    } catch ( Exception e ) {
      logError( "Error opening folder " + data.folderenr + " " + data.folder + ": " + e.toString() );
      logError( Const.getStackTracker( e ) );
      stopAll();
      setErrors( 1 );
      return false;
    }
    return true;
  }

  @Override
  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (MailInputMeta) smi;
    data = (MailInputData) sdi;

    if ( !super.init( smi, sdi ) ) {
      return false;
    }

    if ( !meta.isDynamicFolder() ) {
      try {
        // Create the output row meta-data
        data.outputRowMeta = new RowMeta();
        meta.getFields( getTransMeta().getBowl(), data.outputRowMeta, getStepname(), null, null, this, repository,
          metaStore ); // get the metadata populated

      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "MailInput.ErrorInit", e.toString() ) );
        logError( Const.getStackTracker( e ) );
        return false;
      }
    }
    data.usePOP = meta.getProtocol().equals( MailConnectionMeta.PROTOCOL_STRING_POP3 );

    String realServer = environmentSubstitute( meta.getServerName() );
    if ( meta.getProtocol().equals( MailConnectionMeta.PROTOCOL_STRING_MBOX )
      && StringUtils.startsWith( realServer, "file://" ) ) {
      realServer = StringUtils.remove( realServer, "file://" );
    }

    String realUserName = environmentSubstitute( meta.getUserName() );
    String realPassword = Utils.resolvePassword( variables, meta.getPassword() );
    int realPort = Const.toInt( environmentSubstitute( meta.getPort() ), -1 );
    String realProxyUsername = environmentSubstitute( meta.getProxyUsername() );
    if ( !meta.isDynamicFolder() ) {
      //Limit field has absolute priority
      String realLimitRow = environmentSubstitute( meta.getRowLimit() );
      int limit = Const.toInt( realLimitRow, 0 );
      //Limit field has absolute priority
      if ( limit == 0 ) {
        limit = getReadFirst( meta.getProtocol() );
      }
      data.rowlimit = limit;
    }
    Date beginDate = null;
    Date endDate = null;
    SimpleDateFormat df = new SimpleDateFormat( MailInputMeta.DATE_PATTERN );

    // check search terms
    // Received Date
    try {
      String realBeginDate;

      switch ( meta.getConditionOnReceivedDate() ) {
        case MailConnectionMeta.CONDITION_DATE_EQUAL:
        case MailConnectionMeta.CONDITION_DATE_GREATER:
        case MailConnectionMeta.CONDITION_DATE_SMALLER:
          realBeginDate = environmentSubstitute( meta.getReceivedDate1() );
          if ( Utils.isEmpty( realBeginDate ) ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "MailInput.Error.ReceivedDateSearchTermEmpty" ) );
          }
          beginDate = df.parse( realBeginDate );
          break;
        case MailConnectionMeta.CONDITION_DATE_BETWEEN:
          realBeginDate = environmentSubstitute( meta.getReceivedDate1() );
          if ( Utils.isEmpty( realBeginDate ) ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "MailInput.Error.ReceivedDatesSearchTermEmpty" ) );
          }
          beginDate = df.parse( realBeginDate );
          String realEndDate = environmentSubstitute( meta.getReceivedDate2() );
          if ( Utils.isEmpty( realEndDate ) ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "MailInput.Error.ReceivedDatesSearchTermEmpty" ) );
          }
          endDate = df.parse( realEndDate );
          break;
        default:
          break;
      }
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "MailInput.Error.SettingSearchTerms", e.getMessage() ) );
      setErrors( 1 );
      stopAll();
    }
    try {
      if( MailInputMeta.AUTENTICATION_OAUTH.equals( meta.isUsingAuthentication() ) ) {
        realPassword = "Bearer " + meta.getOauthToken( meta.getTokenUrl(), meta.getScope(), meta.getClientId(),
                meta.getSecretKey(), meta.getGrantType(), meta.getRefresh_token(), meta.getAuthorization_code(), meta.getRedirectUri()).getAccessToken();
      }
      // create a mail connection object
      data.mailConn =
        new MailConnection(
          getTransMeta().getBowl(), log, MailConnectionMeta.getProtocolFromString(
            meta.getProtocol(), MailConnectionMeta.PROTOCOL_IMAP ), realServer, realPort, realUserName,
          realPassword, meta.isUseSSL(), meta.isUseProxy(), realProxyUsername );
      // connect
      data.mailConn.connect();
      // Need to apply search filters?
      applySearch( beginDate, endDate );

      if ( !meta.isDynamicFolder() ) {
        // pass static folder name
        String realIMAPFolder = environmentSubstitute( meta.getIMAPFolder() );
        // return folders list
        // including sub folders if necessary
        data.folders = getFolders( realIMAPFolder );
      }
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "MailInput.Error.OpeningConnection", e.getMessage() ) );
      setErrors( 1 );
      stopAll();
    }
    data.nrFields = meta.getInputFields() != null ? meta.getInputFields().length : 0;

    return true;
  }

  private int getReadFirst( String protocol ) {
    if ( protocol.equals( MailConnectionMeta.PROTOCOL_STRING_POP3 ) ) {
      return Const.toInt( meta.getFirstMails(), 0 );
    }
    if ( protocol.equals( MailConnectionMeta.PROTOCOL_STRING_IMAP ) ) {
      return Const.toInt( meta.getFirstIMAPMails(), 0 );
    }
    //and we do not have this option for MBOX on UI.
    return 0;
  }

  @Override
  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (MailInputMeta) smi;
    data = (MailInputData) sdi;

    if ( data.mailConn != null ) {
      try {
        data.mailConn.disconnect();
        data.mailConn = null;
      } catch ( Exception e ) { /* Ignore */
      }
    }

    super.dispose( smi, sdi );
  }

  private Integer parseIntWithSubstitute( String toParse ) {
    toParse = environmentSubstitute( toParse );
    if ( !StringUtils.isEmpty( toParse ) ) {
      try {
        return Integer.parseInt( toParse );
      } catch ( NumberFormatException e ) {
        log.logError( e.getLocalizedMessage() );
      }
    }
    return null;
  }

  /**
   * Extracted message parse algorithm to be able to unit test separately
   *
   */
  class MessageParser {

    Object[] parseToArray( Object[] r, Message message ) throws Exception {

      // Execute for each Input field...
      for ( int i = 0; i < data.nrFields; i++ ) {
        int index = data.totalpreviousfields + i;

        try {
          switch ( meta.getInputFields()[i].getColumn() ) {
            case MailInputField.COLUMN_MESSAGE_NR:
              r[index] = new Long( message.getMessageNumber() );
              break;
            case MailInputField.COLUMN_SUBJECT:
              r[index] = message.getSubject();
              break;
            case MailInputField.COLUMN_SENDER:
              r[index] = StringUtils.join( message.getFrom(), ";" );
              break;
            case MailInputField.COLUMN_REPLY_TO:
              r[index] = StringUtils.join( message.getReplyTo(), ";" );
              break;
            case MailInputField.COLUMN_RECIPIENTS:
              r[index] = StringUtils.join( message.getAllRecipients(), ";" );
              break;
            case MailInputField.COLUMN_DESCRIPTION:
              r[index] = message.getDescription();
              break;
            case MailInputField.COLUMN_BODY:
              r[index] = data.mailConn.getMessageBody( message );
              break;
            case MailInputField.COLUMN_RECEIVED_DATE:
              Date receivedDate = message.getReceivedDate();
              r[index] = receivedDate != null ? new Date( receivedDate.getTime() ) : null;
              break;
            case MailInputField.COLUMN_SENT_DATE:
              Date sentDate = message.getSentDate();
              r[index] = sentDate != null ? new Date( sentDate.getTime() ) : null;
              break;
            case MailInputField.COLUMN_CONTENT_TYPE:
              r[index] = message.getContentType();
              break;
            case MailInputField.COLUMN_FOLDER_NAME:
              r[index] = data.mailConn.getFolderName();
              break;
            case MailInputField.COLUMN_SIZE:
              r[index] = new Long( message.getSize() );
              break;
            case MailInputField.COLUMN_FLAG_DRAFT:
              r[index] = new Boolean( data.mailConn.isMessageDraft( message ) );
              break;
            case MailInputField.COLUMN_FLAG_FLAGGED:
              r[index] = new Boolean( data.mailConn.isMessageFlagged( message ) );
              break;
            case MailInputField.COLUMN_FLAG_NEW:
              r[index] = new Boolean( data.mailConn.isMessageNew( message ) );
              break;
            case MailInputField.COLUMN_FLAG_READ:
              r[index] = new Boolean( data.mailConn.isMessageRead( message ) );
              break;
            case MailInputField.COLUMN_FLAG_DELETED:
              r[index] = new Boolean( data.mailConn.isMessageDeleted( message ) );
              break;
            case MailInputField.COLUMN_ATTACHED_FILES_COUNT:
              r[index] = new Long( data.mailConn.getAttachedFilesCount( message, null ) );
              break;
            case MailInputField.COLUMN_HEADER:
              String name = meta.getInputFields()[i].getName();
              // *only one name
              String[] arr = { name };
              // this code was before generic epoch
              Enumeration<?> en = message.getMatchingHeaders( arr );
              if ( en == null ) {
                r[index] = "";
                break;
              }
              List<String> headers = new ArrayList<>();
              while ( en.hasMoreElements() ) {
                Header next = Header.class.cast( en.nextElement() );
                headers.add( next.getValue() );
              }
              // [PDI-6532] if there is no matching headers return empty String
              r[index] = headers.isEmpty() ? "" : StringUtils.join( headers, ";" );
              break;
            case MailInputField.COLUMN_BODY_CONTENT_TYPE:
              r[index] = data.mailConn.getMessageBodyContentType( message );
              break;
            default:
              break;
          }
        } catch ( Exception e ) {
          String errMsg = "Error adding value for field " + meta.getInputFields()[i].getName();
          throw new Exception( errMsg, e );
        }
      }
      return r;
    }
  }
}
