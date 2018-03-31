/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.mail;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileType;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Send mail step. based on Mail job entry
 *
 * @author Samatar
 * @since 28-07-2008
 */

public class Mail extends BaseStep implements StepInterface {
  private static Class<?> PKG = MailMeta.class; // for i18n purposes, needed by Translator2!!

  private MailMeta meta;
  private MailData data;

  public Mail( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    meta = (MailMeta) smi;
    data = (MailData) sdi;

    Object[] r = getRow(); // get row, set busy!
    if ( r == null ) { // no more input to be expected...

      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;

      // get the RowMeta
      data.previousRowMeta = getInputRowMeta().clone();

      // Check is filename field is provided
      if ( Utils.isEmpty( meta.getDestination() ) ) {
        throw new KettleException( BaseMessages.getString( PKG, "Mail.Log.DestinationFieldEmpty" ) );
      }

      // Check is replyname field is provided
      if ( Utils.isEmpty( meta.getReplyAddress() ) ) {
        throw new KettleException( BaseMessages.getString( PKG, "Mail.Log.ReplyFieldEmpty" ) );
      }

      // Check is SMTP server is provided
      if ( Utils.isEmpty( meta.getServer() ) ) {
        throw new KettleException( BaseMessages.getString( PKG, "Mail.Log.ServerFieldEmpty" ) );
      }

      // Check Attached filenames when dynamic
      if ( meta.isDynamicFilename() && Utils.isEmpty( meta.getDynamicFieldname() ) ) {
        throw new KettleException( BaseMessages.getString( PKG, "Mail.Log.DynamicFilenameFielddEmpty" ) );
      }

      // Check Attached zipfilename when dynamic
      if ( meta.isZipFilenameDynamic() && Utils.isEmpty( meta.getDynamicZipFilenameField() ) ) {
        throw new KettleException( BaseMessages.getString( PKG, "Mail.Log.DynamicZipFilenameFieldEmpty" ) );
      }

      if ( meta.isZipFiles() && Utils.isEmpty( meta.getZipFilename() ) ) {
        throw new KettleException( BaseMessages.getString( PKG, "Mail.Log.ZipFilenameEmpty" ) );
      }

      // check authentication
      if ( meta.isUsingAuthentication() ) {
        // check authentication user
        if ( Utils.isEmpty( meta.getAuthenticationUser() ) ) {
          throw new KettleException( BaseMessages.getString( PKG, "Mail.Log.AuthenticationUserFieldEmpty" ) );
        }

        // check authentication pass
        if ( Utils.isEmpty( meta.getAuthenticationPassword() ) ) {
          throw new KettleException( BaseMessages.getString( PKG, "Mail.Log.AuthenticationPasswordFieldEmpty" ) );
        }
      }

      // cache the position of the destination field
      if ( data.indexOfDestination < 0 ) {
        String realDestinationFieldname = meta.getDestination();
        data.indexOfDestination = data.previousRowMeta.indexOfValue( realDestinationFieldname );
        if ( data.indexOfDestination < 0 ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "Mail.Exception.CouldnotFindDestinationField", realDestinationFieldname ) );
        }
      }

      // Cc
      if ( !Utils.isEmpty( meta.getDestinationCc() ) ) {
        // cache the position of the Cc field
        if ( data.indexOfDestinationCc < 0 ) {
          String realDestinationCcFieldname = meta.getDestinationCc();
          data.indexOfDestinationCc = data.previousRowMeta.indexOfValue( realDestinationCcFieldname );
          if ( data.indexOfDestinationCc < 0 ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "Mail.Exception.CouldnotFindDestinationCcField", realDestinationCcFieldname ) );
          }
        }
      }
      // BCc
      if ( !Utils.isEmpty( meta.getDestinationBCc() ) ) {
        // cache the position of the BCc field
        if ( data.indexOfDestinationBCc < 0 ) {
          String realDestinationBCcFieldname = meta.getDestinationBCc();
          data.indexOfDestinationBCc = data.previousRowMeta.indexOfValue( realDestinationBCcFieldname );
          if ( data.indexOfDestinationBCc < 0 ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "Mail.Exception.CouldnotFindDestinationBCcField", realDestinationBCcFieldname ) );
          }
        }
      }
      // Sender Name
      if ( !Utils.isEmpty( meta.getReplyName() ) ) {
        // cache the position of the sender field
        if ( data.indexOfSenderName < 0 ) {
          String realSenderName = meta.getReplyName();
          data.indexOfSenderName = data.previousRowMeta.indexOfValue( realSenderName );
          if ( data.indexOfSenderName < 0 ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "Mail.Exception.CouldnotFindReplyNameField", realSenderName ) );
          }
        }
      }
      // Sender address
      // cache the position of the sender field
      if ( data.indexOfSenderAddress < 0 ) {
        String realSenderAddress = meta.getReplyAddress();
        data.indexOfSenderAddress = data.previousRowMeta.indexOfValue( realSenderAddress );
        if ( data.indexOfSenderAddress < 0 ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "Mail.Exception.CouldnotFindReplyAddressField", realSenderAddress ) );
        }
      }

      // Reply to
      if ( !Utils.isEmpty( meta.getReplyToAddresses() ) ) {
        // cache the position of the reply to field
        if ( data.indexOfReplyToAddresses < 0 ) {
          String realReplyToAddresses = meta.getReplyToAddresses();
          data.indexOfReplyToAddresses = data.previousRowMeta.indexOfValue( realReplyToAddresses );
          if ( data.indexOfReplyToAddresses < 0 ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "Mail.Exception.CouldnotFindReplyToAddressesField", realReplyToAddresses ) );
          }
        }
      }

      // Contact Person
      if ( !Utils.isEmpty( meta.getContactPerson() ) ) {
        // cache the position of the destination field
        if ( data.indexOfContactPerson < 0 ) {
          String realContactPerson = meta.getContactPerson();
          data.indexOfContactPerson = data.previousRowMeta.indexOfValue( realContactPerson );
          if ( data.indexOfContactPerson < 0 ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "Mail.Exception.CouldnotFindContactPersonField", realContactPerson ) );
          }
        }
      }
      // Contact Phone
      if ( !Utils.isEmpty( meta.getContactPhone() ) ) {
        // cache the position of the destination field
        if ( data.indexOfContactPhone < 0 ) {
          String realContactPhone = meta.getContactPhone();
          data.indexOfContactPhone = data.previousRowMeta.indexOfValue( realContactPhone );
          if ( data.indexOfContactPhone < 0 ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "Mail.Exception.CouldnotFindContactPhoneField", realContactPhone ) );
          }
        }
      }
      // cache the position of the Server field
      if ( data.indexOfServer < 0 ) {
        String realServer = meta.getServer();
        data.indexOfServer = data.previousRowMeta.indexOfValue( realServer );
        if ( data.indexOfServer < 0 ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "Mail.Exception.CouldnotFindServerField", realServer ) );
        }
      }
      // Port
      if ( !Utils.isEmpty( meta.getPort() ) ) {
        // cache the position of the port field
        if ( data.indexOfPort < 0 ) {
          String realPort = meta.getPort();
          data.indexOfPort = data.previousRowMeta.indexOfValue( realPort );
          if ( data.indexOfPort < 0 ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "Mail.Exception.CouldnotFindPortField", realPort ) );
          }
        }
      }
      // Authentication
      if ( meta.isUsingAuthentication() ) {
        // cache the position of the Authentication user field
        if ( data.indexOfAuthenticationUser < 0 ) {
          String realAuthenticationUser = meta.getAuthenticationUser();
          data.indexOfAuthenticationUser = data.previousRowMeta.indexOfValue( realAuthenticationUser );
          if ( data.indexOfAuthenticationUser < 0 ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "Mail.Exception.CouldnotFindAuthenticationUserField", realAuthenticationUser ) );
          }
        }

        // cache the position of the Authentication password field
        if ( data.indexOfAuthenticationPass < 0 ) {
          String realAuthenticationPassword = Utils.resolvePassword( variables, meta.getAuthenticationPassword() );
          data.indexOfAuthenticationPass = data.previousRowMeta.indexOfValue( realAuthenticationPassword );
          if ( data.indexOfAuthenticationPass < 0 ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "Mail.Exception.CouldnotFindAuthenticationPassField", realAuthenticationPassword ) );
          }
        }
      }
      // Mail Subject
      if ( !Utils.isEmpty( meta.getSubject() ) ) {
        // cache the position of the subject field
        if ( data.indexOfSubject < 0 ) {
          String realSubject = meta.getSubject();
          data.indexOfSubject = data.previousRowMeta.indexOfValue( realSubject );
          if ( data.indexOfSubject < 0 ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "Mail.Exception.CouldnotFindSubjectField", realSubject ) );
          }
        }
      }
      // Mail Comment
      if ( !Utils.isEmpty( meta.getComment() ) ) {
        // cache the position of the comment field
        if ( data.indexOfComment < 0 ) {
          String realComment = meta.getComment();
          data.indexOfComment = data.previousRowMeta.indexOfValue( realComment );
          if ( data.indexOfComment < 0 ) {
            throw new KettleException( BaseMessages.getString(
              PKG, "Mail.Exception.CouldnotFindCommentField", realComment ) );
          }
        }
      }

      if ( meta.isAttachContentFromField() ) {
        // We are dealing with file content directly loaded from file
        // and not physical file
        String attachedContentField = meta.getAttachContentField();
        if ( Utils.isEmpty( attachedContentField ) ) {
          // Empty Field
          throw new KettleException( BaseMessages.getString( PKG, "Mail.Exception.AttachedContentFieldEmpty" ) );
        }
        data.indexOfAttachedContent = data.previousRowMeta.indexOfValue( attachedContentField );
        if ( data.indexOfAttachedContent < 0 ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "Mail.Exception.CouldnotFindAttachedContentField", attachedContentField ) );
        }
        // Attached content filename
        String attachedContentFileNameField = meta.getAttachContentFileNameField();
        if ( Utils.isEmpty( attachedContentFileNameField ) ) {
          // Empty Field
          throw new KettleException( BaseMessages.getString(
            PKG, "Mail.Exception.AttachedContentFileNameFieldEmpty" ) );
        }
        data.IndexOfAttachedFilename = data.previousRowMeta.indexOfValue( attachedContentFileNameField );
        if ( data.IndexOfAttachedFilename < 0 ) {
          throw new KettleException( BaseMessages.getString(
            PKG, "Mail.Exception.CouldnotFindAttachedContentFileNameField", attachedContentFileNameField ) );
        }

      } else {

        // Dynamic Zipfilename
        if ( meta.isZipFilenameDynamic() ) {
          // cache the position of the attached source filename field
          if ( data.indexOfDynamicZipFilename < 0 ) {
            String realZipFilename = meta.getDynamicZipFilenameField();
            data.indexOfDynamicZipFilename = data.previousRowMeta.indexOfValue( realZipFilename );
            if ( data.indexOfDynamicZipFilename < 0 ) {
              throw new KettleException( BaseMessages.getString(
                PKG, "Mail.Exception.CouldnotSourceAttachedZipFilenameField", realZipFilename ) );
            }
          }
        }
        data.zipFileLimit = Const.toLong( environmentSubstitute( meta.getZipLimitSize() ), 0 );
        if ( data.zipFileLimit > 0 ) {
          data.zipFileLimit = data.zipFileLimit * 1048576; // Mo
        }

        if ( !meta.isZipFilenameDynamic() ) {
          data.ZipFilename = environmentSubstitute( meta.getZipFilename() );
        }

        // Attached files
        if ( meta.isDynamicFilename() ) {
          // cache the position of the attached source filename field
          if ( data.indexOfSourceFilename < 0 ) {
            String realSourceattachedFilename = meta.getDynamicFieldname();
            data.indexOfSourceFilename = data.previousRowMeta.indexOfValue( realSourceattachedFilename );
            if ( data.indexOfSourceFilename < 0 ) {
              throw new KettleException( BaseMessages.getString(
                PKG, "Mail.Exception.CouldnotSourceAttachedFilenameField", realSourceattachedFilename ) );
            }
          }

          // cache the position of the attached wildcard field
          if ( !Utils.isEmpty( meta.getSourceWildcard() ) ) {
            if ( data.indexOfSourceWildcard < 0 ) {
              String realSourceattachedWildcard = meta.getDynamicWildcard();
              data.indexOfSourceWildcard = data.previousRowMeta.indexOfValue( realSourceattachedWildcard );
              if ( data.indexOfSourceWildcard < 0 ) {
                throw new KettleException( BaseMessages.getString(
                  PKG, "Mail.Exception.CouldnotSourceAttachedWildcard", realSourceattachedWildcard ) );
              }
            }
          }
        } else {
          // static attached filenames
          data.realSourceFileFoldername = environmentSubstitute( meta.getSourceFileFoldername() );
          data.realSourceWildcard = environmentSubstitute( meta.getSourceWildcard() );
        }
      }

      // check embedded images
      if ( meta.getEmbeddedImages() != null && meta.getEmbeddedImages().length > 0 ) {
        FileObject image = null;
        data.embeddedMimePart = new HashSet<MimeBodyPart>();
        try {
          for ( int i = 0; i < meta.getEmbeddedImages().length; i++ ) {
            String imageFile = environmentSubstitute( meta.getEmbeddedImages()[i] );
            String contentID = environmentSubstitute( meta.getContentIds()[i] );
            image = KettleVFS.getFileObject( imageFile );

            if ( image.exists() && image.getType() == FileType.FILE ) {
              // Create part for the image
              MimeBodyPart imagePart = new MimeBodyPart();
              // Load the image
              URLDataSource fds = new URLDataSource( image.getURL() );
              imagePart.setDataHandler( new DataHandler( fds ) );
              // Setting the header
              imagePart.setHeader( "Content-ID", "<" + contentID + ">" );
              // keep this part for further user
              data.embeddedMimePart.add( imagePart );
              logBasic( BaseMessages.getString( PKG, "Mail.Log.ImageAdded", imageFile ) );

            } else {
              logError( BaseMessages.getString( PKG, "Mail.Log.WrongImage", imageFile ) );
            }
          }
        } catch ( Exception e ) {
          logError( BaseMessages.getString( PKG, "Mail.Error.AddingImage", e.getMessage() ) );
        } finally {
          if ( image != null ) {
            try {
              image.close();
            } catch ( Exception e ) { /* Ignore */
            }
          }
        }
      }

    } // end if first

    boolean sendToErrorRow = false;
    String errorMessage = null;

    try {
      // get values
      String maildestination = data.previousRowMeta.getString( r, data.indexOfDestination );
      if ( Utils.isEmpty( maildestination ) ) {
        throw new KettleException( "Mail.Error.MailDestinationEmpty" );
      }
      String maildestinationCc = null;
      if ( data.indexOfDestinationCc > -1 ) {
        maildestinationCc = data.previousRowMeta.getString( r, data.indexOfDestinationCc );
      }
      String maildestinationBCc = null;
      if ( data.indexOfDestinationBCc > -1 ) {
        maildestinationBCc = data.previousRowMeta.getString( r, data.indexOfDestinationBCc );
      }

      String mailsendername = null;
      if ( data.indexOfSenderName > -1 ) {
        mailsendername = data.previousRowMeta.getString( r, data.indexOfSenderName );
      }
      String mailsenderaddress = data.previousRowMeta.getString( r, data.indexOfSenderAddress );

      // reply addresses
      String mailreplyToAddresses = null;
      if ( data.indexOfReplyToAddresses > -1 ) {
        mailreplyToAddresses = data.previousRowMeta.getString( r, data.indexOfReplyToAddresses );
      }

      String contactperson = null;
      if ( data.indexOfContactPerson > -1 ) {
        contactperson = data.previousRowMeta.getString( r, data.indexOfContactPerson );
      }
      String contactphone = null;
      if ( data.indexOfContactPhone > -1 ) {
        contactphone = data.previousRowMeta.getString( r, data.indexOfContactPhone );
      }

      String servername = data.previousRowMeta.getString( r, data.indexOfServer );
      if ( Utils.isEmpty( servername ) ) {
        throw new KettleException( "Mail.Error.MailServerEmpty" );
      }
      int port = -1;
      if ( data.indexOfPort > -1 ) {
        port = Const.toInt( "" + data.previousRowMeta.getInteger( r, data.indexOfPort ), -1 );
      }

      String authuser = null;
      if ( data.indexOfAuthenticationUser > -1 ) {
        authuser = data.previousRowMeta.getString( r, data.indexOfAuthenticationUser );
      }
      String authpass = null;
      if ( data.indexOfAuthenticationPass > -1 ) {
        authpass = data.previousRowMeta.getString( r, data.indexOfAuthenticationPass );
      }

      String subject = null;
      if ( data.indexOfSubject > -1 ) {
        subject = data.previousRowMeta.getString( r, data.indexOfSubject );
      }

      String comment = null;
      if ( data.indexOfComment > -1 ) {
        comment = data.previousRowMeta.getString( r, data.indexOfComment );
      }

      // send email...
      sendMail(
        r, servername, port, mailsenderaddress, mailsendername, maildestination, maildestinationCc,
        maildestinationBCc, contactperson, contactphone, authuser, authpass, subject, comment,
        mailreplyToAddresses );

      putRow( getInputRowMeta(), r ); // copy row to possible alternate rowset(s).); // copy row to output rowset(s);

      if ( log.isRowLevel() ) {
        logRowlevel( BaseMessages.getString( PKG, "Mail.Log.LineNumber", getLinesRead()
          + " : " + getInputRowMeta().getString( r ) ) );
      }

    } catch ( Exception e ) {
      if ( getStepMeta().isDoingErrorHandling() ) {
        sendToErrorRow = true;
        errorMessage = e.toString();
      } else {
        throw new KettleException( BaseMessages.getString( PKG, "Mail.Error.General" ), e );
      }
      if ( sendToErrorRow ) {
        // Simply add this row to the error row
        putError( getInputRowMeta(), r, 1, errorMessage, null, "MAIL001" );
      }
    }

    return true;
  }

  public void sendMail( Object[] r, String server, int port, String senderAddress, String senderName,
    String destination, String destinationCc, String destinationBCc, String contactPerson, String contactPhone,
    String authenticationUser, String authenticationPassword, String mailsubject, String comment,
    String replyToAddresses ) throws Exception {

    // Send an e-mail...
    // create some properties and get the default Session

    String protocol = "smtp";
    if ( meta.isUsingSecureAuthentication() ) { // PDI-2955
      // if (meta.isUsingAuthentication()) {
      if ( meta.getSecureConnectionType().equals( "TLS" ) ) {
        // Allow TLS authentication
        data.props.put( "mail.smtp.starttls.enable", "true" );
      } else {
        protocol = "smtps";
        // required to get rid of a SSL exception :
        // nested exception is:
        // javax.net.ssl.SSLException: Unsupported record version Unknown
        data.props.put( "mail.smtps.quitwait", "false" );
      }
    }
    data.props.put( "mail." + protocol + ".host", server );
    if ( port != -1 ) {
      data.props.put( "mail." + protocol + ".port", "" + port ); // needs to be supplied as a string, not as an integer
    }

    if ( isDebug() ) {
      data.props.put( "mail.debug", "true" );
    }

    if ( meta.isUsingAuthentication() ) {
      data.props.put( "mail." + protocol + ".auth", "true" );
    }

    Session session = Session.getInstance( data.props );
    session.setDebug( isDebug() );

    // create a message
    Message msg = new MimeMessage( session );

    // set message priority
    if ( meta.isUsePriority() ) {
      String priority_int = "1";
      if ( meta.getPriority().equals( "low" ) ) {
        priority_int = "3";
      }
      if ( meta.getPriority().equals( "normal" ) ) {
        priority_int = "2";
      }

      msg.setHeader( "X-Priority", priority_int ); // (String)int between 1= high and 3 = low.
      msg.setHeader( "Importance", meta.getImportance() );
      // seems to be needed for MS Outlook.
      // where it returns a string of high /normal /low.
      msg.setHeader( "Sensitivity", meta.getSensitivity() );
      // Possible values are normal, personal, private, company-confidential
    }

    // set Email sender
    String email_address = senderAddress;
    if ( !Utils.isEmpty( email_address ) ) {
      // get sender name
      if ( !Utils.isEmpty( senderName ) ) {
        email_address = senderName + '<' + email_address + '>';
      }
      msg.setFrom( new InternetAddress( email_address ) );
    } else {
      throw new MessagingException( BaseMessages.getString( PKG, "Mail.Error.ReplyEmailNotFilled" ) );
    }

    // Set reply to
    if ( !Utils.isEmpty( replyToAddresses ) ) {
      // get replay to
      // Split the mail-address: space separated
      String[] reply_Address_List = replyToAddresses.split( " " );
      InternetAddress[] address = new InternetAddress[reply_Address_List.length];

      for ( int i = 0; i < reply_Address_List.length; i++ ) {
        address[i] = new InternetAddress( reply_Address_List[i] );
      }

      // To add the real reply-to
      msg.setReplyTo( address );
    }

    // Split the mail-address: space separated
    String[] destinations = destination.split( " " );
    InternetAddress[] address = new InternetAddress[destinations.length];
    for ( int i = 0; i < destinations.length; i++ ) {
      address[i] = new InternetAddress( destinations[i] );
    }

    msg.setRecipients( Message.RecipientType.TO, address );

    String realdestinationCc = destinationCc;
    if ( !Utils.isEmpty( realdestinationCc ) ) {
      // Split the mail-address Cc: space separated
      String[] destinationsCc = realdestinationCc.split( " " );
      InternetAddress[] addressCc = new InternetAddress[destinationsCc.length];
      for ( int i = 0; i < destinationsCc.length; i++ ) {
        addressCc[i] = new InternetAddress( destinationsCc[i] );
      }

      msg.setRecipients( Message.RecipientType.CC, addressCc );
    }

    String realdestinationBCc = destinationBCc;
    if ( !Utils.isEmpty( realdestinationBCc ) ) {
      // Split the mail-address BCc: space separated
      String[] destinationsBCc = realdestinationBCc.split( " " );
      InternetAddress[] addressBCc = new InternetAddress[destinationsBCc.length];
      for ( int i = 0; i < destinationsBCc.length; i++ ) {
        addressBCc[i] = new InternetAddress( destinationsBCc[i] );
      }

      msg.setRecipients( Message.RecipientType.BCC, addressBCc );
    }

    if ( mailsubject != null ) {
      msg.setSubject( mailsubject );
    }

    msg.setSentDate( new Date() );
    StringBuilder messageText = new StringBuilder();

    if ( comment != null ) {
      messageText.append( comment ).append( Const.CR ).append( Const.CR );
    }

    if ( meta.getIncludeDate() ) {
      messageText.append( BaseMessages.getString( PKG, "Mail.Log.Comment.MsgDate" ) + ": " ).append(
        XMLHandler.date2string( new Date() ) ).append( Const.CR ).append( Const.CR );
    }

    if ( !meta.isOnlySendComment() && ( !Utils.isEmpty( contactPerson ) || !Utils.isEmpty( contactPhone ) ) ) {
      messageText.append( BaseMessages.getString( PKG, "Mail.Log.Comment.ContactInfo" ) + " :" ).append( Const.CR );
      messageText.append( "---------------------" ).append( Const.CR );
      messageText.append( BaseMessages.getString( PKG, "Mail.Log.Comment.PersonToContact" ) + " : " ).append(
        contactPerson ).append( Const.CR );
      messageText
        .append( BaseMessages.getString( PKG, "Mail.Log.Comment.Tel" ) + "  : " ).append( contactPhone ).append(
          Const.CR );
      messageText.append( Const.CR );
    }
    data.parts = new MimeMultipart();

    MimeBodyPart part1 = new MimeBodyPart(); // put the text in the
    // 1st part

    if ( meta.isUseHTML() ) {
      if ( !Utils.isEmpty( meta.getEncoding() ) ) {
        part1.setContent( messageText.toString(), "text/html; " + "charset=" + meta.getEncoding() );
      } else {
        part1.setContent( messageText.toString(), "text/html; " + "charset=ISO-8859-1" );
      }
    } else {
      part1.setText( messageText.toString() );
    }

    data.parts.addBodyPart( part1 );

    if ( meta.isAttachContentFromField() ) {
      // attache file content
      addAttachedContent( data.previousRowMeta.getString( r, data.IndexOfAttachedFilename ), data.previousRowMeta
        .getString( r, data.indexOfAttachedContent ) );
    } else {
      // attached files
      if ( meta.isDynamicFilename() ) {
        setAttachedFilesList( r, log );
      } else {
        setAttachedFilesList( null, log );
      }
    }

    // add embedded images
    addImagePart();

    if ( data.nrEmbeddedImages > 0 && data.nrattachedFiles == 0 ) {
      // If we need to embedd images...
      // We need to create a "multipart/related" message.
      // otherwise image will appear as attached file
      data.parts.setSubType( "related" );
    }

    msg.setContent( data.parts );

    Transport transport = null;
    try {
      transport = session.getTransport( protocol );
      if ( meta.isUsingAuthentication() ) {
        if ( port != -1 ) {
          transport.connect( Const.NVL( server, "" ), port, Const.NVL( authenticationUser, "" ), Const.NVL(
            authenticationPassword, "" ) );
        } else {
          transport.connect( Const.NVL( server, "" ), Const.NVL( authenticationUser, "" ), Const.NVL(
            authenticationPassword, "" ) );
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

  }

  private void setAttachedFilesList( Object[] r, LogChannelInterface log ) throws Exception {
    String realSourceFileFoldername = null;
    String realSourceWildcard = null;
    FileObject sourcefile = null;
    FileObject file = null;

    ZipOutputStream zipOutputStream = null;
    File masterZipfile = null;

    if ( meta.isZipFilenameDynamic() ) {
      data.ZipFilename = data.previousRowMeta.getString( r, data.indexOfDynamicZipFilename );
    }

    try {

      if ( meta.isDynamicFilename() ) {
        // dynamic attached filenames
        if ( data.indexOfSourceFilename > -1 ) {
          realSourceFileFoldername = data.previousRowMeta.getString( r, data.indexOfSourceFilename );
        }

        if ( data.indexOfSourceWildcard > -1 ) {
          realSourceWildcard = data.previousRowMeta.getString( r, data.indexOfSourceWildcard );
        }

      } else {
        // static attached filenames
        realSourceFileFoldername = data.realSourceFileFoldername;
        realSourceWildcard = data.realSourceWildcard;
      }

      if ( !Utils.isEmpty( realSourceFileFoldername ) ) {
        sourcefile = KettleVFS.getFileObject( realSourceFileFoldername, getTransMeta() );
        if ( sourcefile.exists() ) {
          long FileSize = 0;
          FileObject[] list = null;
          if ( sourcefile.getType() == FileType.FILE ) {
            list = new FileObject[1];
            list[0] = sourcefile;
          } else {
            list = sourcefile.findFiles( new TextFileSelector( sourcefile.toString(), realSourceWildcard ) );
          }
          if ( list.length > 0 ) {

            boolean zipFiles = meta.isZipFiles();
            if ( zipFiles && data.zipFileLimit == 0 ) {
              masterZipfile =
                new File( System.getProperty( "java.io.tmpdir" ) + Const.FILE_SEPARATOR + data.ZipFilename );

              zipOutputStream = new ZipOutputStream( new FileOutputStream( masterZipfile ) );
            }

            for ( int i = 0; i < list.length; i++ ) {

              file = KettleVFS.getFileObject( KettleVFS.getFilename( list[i] ), getTransMeta() );

              if ( zipFiles ) {

                if ( data.zipFileLimit == 0 ) {
                  ZipEntry zipEntry = new ZipEntry( file.getName().getBaseName() );
                  zipOutputStream.putNextEntry( zipEntry );

                  // Now put the content of this file into this archive...
                  BufferedInputStream inputStream = new BufferedInputStream( file.getContent().getInputStream() );
                  int c;
                  while ( ( c = inputStream.read() ) >= 0 ) {
                    zipOutputStream.write( c );
                  }
                  inputStream.close();
                  zipOutputStream.closeEntry();
                } else {
                  FileSize += file.getContent().getSize();
                }
              } else {
                addAttachedFilePart( file );
              }
            } // end for
            if ( zipFiles ) {
              if ( isDebug() ) {
                logDebug( BaseMessages.getString( PKG, "Mail.Log.FileSize", "" + FileSize ) );
              }
              if ( isDebug() ) {
                logDebug( BaseMessages.getString( PKG, "Mail.Log.LimitSize", "" + data.zipFileLimit ) );
              }

              if ( data.zipFileLimit > 0 && FileSize > data.zipFileLimit ) {

                masterZipfile =
                  new File( System.getProperty( "java.io.tmpdir" ) + Const.FILE_SEPARATOR + data.ZipFilename );

                zipOutputStream = new ZipOutputStream( new FileOutputStream( masterZipfile ) );

                for ( int i = 0; i < list.length; i++ ) {

                  file = KettleVFS.getFileObject( KettleVFS.getFilename( list[i] ), getTransMeta() );

                  ZipEntry zipEntry = new ZipEntry( file.getName().getBaseName() );
                  zipOutputStream.putNextEntry( zipEntry );

                  // Now put the content of this file into this archive...
                  BufferedInputStream inputStream = new BufferedInputStream( file.getContent().getInputStream() );
                  int c;
                  while ( ( c = inputStream.read() ) >= 0 ) {
                    zipOutputStream.write( c );
                  }
                  inputStream.close();
                  zipOutputStream.closeEntry();

                }

              }
              if ( data.zipFileLimit > 0 && FileSize > data.zipFileLimit || data.zipFileLimit == 0 ) {
                file = KettleVFS.getFileObject( masterZipfile.getAbsolutePath(), getTransMeta() );
                addAttachedFilePart( file );
              }
            }
          }
        } else {
          logError( BaseMessages.getString( PKG, "Mail.Error.SourceFileFolderNotExists", realSourceFileFoldername ) );
        }
      }
    } catch ( Exception e ) {
      logError( e.getMessage() );
    } finally {
      if ( sourcefile != null ) {
        try {
          sourcefile.close();
        } catch ( Exception e ) {
          // Ignore errors
        }
      }
      if ( file != null ) {
        try {
          file.close();
        } catch ( Exception e ) {
          // Ignore errors
        }
      }

      if ( zipOutputStream != null ) {
        try {
          zipOutputStream.finish();
          zipOutputStream.close();
        } catch ( IOException e ) {
          logError( "Unable to close attachement zip file archive : " + e.toString() );
        }
      }
    }

  }

  private void addAttachedFilePart( FileObject file ) throws Exception {
    // create a data source

    MimeBodyPart files = new MimeBodyPart();
    // create a data source
    URLDataSource fds = new URLDataSource( file.getURL() );
    // get a data Handler to manipulate this file type;
    files.setDataHandler( new DataHandler( fds ) );
    // include the file in the data source
    files.setFileName( file.getName().getBaseName() );
    // insist on base64 to preserve line endings
    files.addHeader( "Content-Transfer-Encoding", "base64" );
    // add the part with the file in the BodyPart();
    data.parts.addBodyPart( files );
    if ( isDetailed() ) {
      logDetailed( BaseMessages.getString( PKG, "Mail.Log.AttachedFile", fds.getName() ) );
    }

  }

  private void addAttachedContent( String filename, String fileContent ) throws Exception {
    // create a data source

    MimeBodyPart mbp = new MimeBodyPart();
    // get a data Handler to manipulate this file type;
    mbp.setDataHandler( new DataHandler( new ByteArrayDataSource( fileContent.getBytes(), "application/x-any" ) ) );
    // include the file in the data source
    mbp.setFileName( filename );
    // add the part with the file in the BodyPart();
    data.parts.addBodyPart( mbp );

  }

  private void addImagePart() throws Exception {
    data.nrEmbeddedImages = 0;
    if ( data.embeddedMimePart != null && data.embeddedMimePart.size() > 0 ) {
      for ( Iterator<MimeBodyPart> i = data.embeddedMimePart.iterator(); i.hasNext(); ) {
        MimeBodyPart part = i.next();
        data.parts.addBodyPart( part );
        data.nrEmbeddedImages++;
      }
    }
  }

  private class TextFileSelector implements FileSelector {
    String fileWildcard = null;
    String sourceFolder = null;

    public TextFileSelector( String sourcefolderin, String filewildcard ) {
      if ( !Utils.isEmpty( sourcefolderin ) ) {
        sourceFolder = sourcefolderin;
      }

      if ( !Utils.isEmpty( filewildcard ) ) {
        fileWildcard = filewildcard;
      }
    }

    public boolean includeFile( FileSelectInfo info ) {
      boolean returncode = false;
      try {
        if ( !info.getFile().toString().equals( sourceFolder ) ) {
          // Pass over the Base folder itself
          String short_filename = info.getFile().getName().getBaseName();

          if ( info.getFile().getParent().equals( info.getBaseFolder() )
            || ( ( !info.getFile().getParent().equals( info.getBaseFolder() ) && meta.isIncludeSubFolders() ) ) ) {
            if ( ( info.getFile().getType() == FileType.FILE && fileWildcard == null )
              || ( info.getFile().getType() == FileType.FILE && fileWildcard != null && GetFileWildcard(
                short_filename, fileWildcard ) ) ) {
              returncode = true;
            }
          }
        }
      } catch ( Exception e ) {
        logError( BaseMessages.getString( PKG, "Mail.Error.FindingFiles", info.getFile().toString(), e
          .getMessage() ) );
        returncode = false;
      }
      return returncode;
    }

    public boolean traverseDescendents( FileSelectInfo info ) {
      return true;
    }
  }

  /**********************************************************
   *
   * @param selectedfile
   * @param wildcard
   * @return True if the selectedfile matches the wildcard
   **********************************************************/
  private boolean GetFileWildcard( String selectedfile, String wildcard ) {
    Pattern pattern = null;
    boolean getIt = true;

    if ( !Utils.isEmpty( wildcard ) ) {
      pattern = Pattern.compile( wildcard );
      // First see if the file matches the regular expression!
      if ( pattern != null ) {
        Matcher matcher = pattern.matcher( selectedfile );
        getIt = matcher.matches();
      }
    }

    return getIt;
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (MailMeta) smi;
    data = (MailData) sdi;

    if ( super.init( smi, sdi ) ) {
      // Add init code here.
      return true;
    }
    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (MailMeta) smi;
    data = (MailData) sdi;

    if ( data.embeddedMimePart != null ) {
      data.embeddedMimePart.clear();
    }
    data.parts = null;
    super.dispose( meta, data );
  }

}
