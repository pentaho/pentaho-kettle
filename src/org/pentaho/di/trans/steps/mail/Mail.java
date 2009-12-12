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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
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

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileType;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogWriter;
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
 * Send mail step.
 * based on Mail job entry
 * @author Samatar
 * @since 28-07-2008
 */

public class Mail extends BaseStep implements StepInterface
{
	private static Class<?> PKG = MailMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private MailMeta meta;
	private MailData data;
	
	public Mail(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(MailMeta)smi;
		data=(MailData)sdi;

		Object[] r=getRow();    // get row, set busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		if(first)
		{
			first=false;
			
			// get the RowMeta
			data.previousRowMeta = getInputRowMeta().clone();
			
			// Check is filename field is provided
			if (Const.isEmpty(meta.getDestination()))
				throw new KettleException(BaseMessages.getString(PKG, "Mail.Log.DestinationFieldEmpty"));
			
			
			// Check is replyname field is provided
			if (Const.isEmpty(meta.getReplyAddress()))
				throw new KettleException(BaseMessages.getString(PKG, "Mail.Log.ReplyFieldEmpty"));
			
			// Check is SMTP server is provided
			if (Const.isEmpty(meta.getServer()))
				throw new KettleException(BaseMessages.getString(PKG, "Mail.Log.ServerFieldEmpty"));
			
			
			// Check Attached filenames when dynamic
			if (meta.isDynamicFilename() && Const.isEmpty(meta.getDynamicFieldname()))
				throw new KettleException(BaseMessages.getString(PKG, "Mail.Log.DynamicFilenameFielddEmpty"));
			
			
			// Check  Attached zipfilename when dynamic
			if (meta.isZipFilenameDynamic() && Const.isEmpty(meta.getDynamicZipFilenameField()))
				throw new KettleException(BaseMessages.getString(PKG, "Mail.Log.DynamicZipFilenameFieldEmpty"));
			
			if(meta.isZipFiles() && Const.isEmpty(meta.getZipFilename()))
				throw new KettleException(BaseMessages.getString(PKG, "Mail.Log.ZipFilenameEmpty"));	
			
			// check authentication 
			if(meta.isUsingAuthentication()) {
				// check authentication user
				if (Const.isEmpty(meta.getAuthenticationUser()))
					throw new KettleException(BaseMessages.getString(PKG, "Mail.Log.AuthenticationUserFieldEmpty"));
				
				// check authentication pass
				if (Const.isEmpty(meta.getAuthenticationPassword()))
					throw new KettleException(BaseMessages.getString(PKG, "Mail.Log.AuthenticationPasswordFieldEmpty"));
			}
			
			// cache the position of the destination field			
			if (data.indexOfDestination<0) {	
				String realDestinationFieldname=meta.getDestination();
				data.indexOfDestination =data.previousRowMeta.indexOfValue(realDestinationFieldname);
				if (data.indexOfDestination<0)
					throw new KettleException(BaseMessages.getString(PKG, "Mail.Exception.CouldnotFindDestinationField",realDestinationFieldname)); //$NON-NLS-1$ //$NON-NLS-2$
			}  
			
			// Cc
			if (!Const.isEmpty(meta.getDestinationCc())) {
				// cache the position of the Cc field			
				if (data.indexOfDestinationCc<0) {	
					String realDestinationCcFieldname=meta.getDestinationCc();
					data.indexOfDestinationCc =data.previousRowMeta.indexOfValue(realDestinationCcFieldname);
					if (data.indexOfDestinationCc<0)
						throw new KettleException(BaseMessages.getString(PKG, "Mail.Exception.CouldnotFindDestinationCcField",realDestinationCcFieldname)); //$NON-NLS-1$ //$NON-NLS-2$
				}  
			}
			// BCc
			if (!Const.isEmpty(meta.getDestinationBCc())) {
				// cache the position of the BCc field			
				if (data.indexOfDestinationBCc<0) {	
					String realDestinationBCcFieldname=meta.getDestinationBCc();
					data.indexOfDestinationBCc =data.previousRowMeta.indexOfValue(realDestinationBCcFieldname);
					if (data.indexOfDestinationBCc<0)
						throw new KettleException(BaseMessages.getString(PKG, "Mail.Exception.CouldnotFindDestinationBCcField",realDestinationBCcFieldname)); //$NON-NLS-1$ //$NON-NLS-2$
				}  
			}
			// Sender Name
			if (!Const.isEmpty(meta.getReplyName())){
				// cache the position of the sender field			
				if (data.indexOfSenderName<0){	
					String realSenderName=meta.getReplyName();
					data.indexOfSenderName =data.previousRowMeta.indexOfValue(realSenderName);
					if (data.indexOfSenderName<0)
						throw new KettleException(BaseMessages.getString(PKG, "Mail.Exception.CouldnotFindReplyNameField",realSenderName)); //$NON-NLS-1$ //$NON-NLS-2$
				}  
			}
			// Sender address
			// cache the position of the sender field			
			if (data.indexOfSenderAddress<0) {	
				String realSenderAddress=meta.getReplyAddress();
				data.indexOfSenderAddress =data.previousRowMeta.indexOfValue(realSenderAddress);
				if (data.indexOfSenderAddress<0)
					throw new KettleException(BaseMessages.getString(PKG, "Mail.Exception.CouldnotFindReplyAddressField",realSenderAddress)); //$NON-NLS-1$ //$NON-NLS-2$
			}  
			
			// Reply to
			if (!Const.isEmpty(meta.getReplyToAddresses())){
				// cache the position of the reply to field			
				if (data.indexOfReplyToAddresses<0){	
					String realReplyToAddresses=meta.getReplyToAddresses();
					data.indexOfReplyToAddresses =data.previousRowMeta.indexOfValue(realReplyToAddresses);
					if (data.indexOfReplyToAddresses<0)
						throw new KettleException(BaseMessages.getString(PKG, "Mail.Exception.CouldnotFindReplyToAddressesField",realReplyToAddresses)); //$NON-NLS-1$ //$NON-NLS-2$
				}  
			}
			
			// Contact Person
			if (!Const.isEmpty(meta.getContactPerson())) {
				// cache the position of the destination field			
				if (data.indexOfContactPerson<0) {	
					String realContactPerson=meta.getContactPerson();
					data.indexOfContactPerson =data.previousRowMeta.indexOfValue(realContactPerson);
					if (data.indexOfContactPerson<0)
						throw new KettleException(BaseMessages.getString(PKG, "Mail.Exception.CouldnotFindContactPersonField",realContactPerson)); //$NON-NLS-1$ //$NON-NLS-2$
				}  
			}
			// Contact Phone
			if (!Const.isEmpty(meta.getContactPhone())){
				// cache the position of the destination field			
				if (data.indexOfContactPhone<0){	
					String realContactPhone=meta.getContactPhone();
					data.indexOfContactPhone =data.previousRowMeta.indexOfValue(realContactPhone);
					if (data.indexOfContactPhone<0)
						throw new KettleException(BaseMessages.getString(PKG, "Mail.Exception.CouldnotFindContactPhoneField",realContactPhone)); //$NON-NLS-1$ //$NON-NLS-2$
				}  
			}
			// cache the position of the Server field			
			if (data.indexOfServer<0){	
				String realServer=meta.getServer();
				data.indexOfServer =data.previousRowMeta.indexOfValue(realServer);
				if (data.indexOfServer<0)
					throw new KettleException(BaseMessages.getString(PKG, "Mail.Exception.CouldnotFindServerField",realServer)); //$NON-NLS-1$ //$NON-NLS-2$
			} 
			// Port
			if (!Const.isEmpty(meta.getPort())){
				// cache the position of the port field			
				if (data.indexOfPort<0){	
					String realPort=meta.getPort();
					data.indexOfPort =data.previousRowMeta.indexOfValue(realPort);
					if (data.indexOfPort<0)
						throw new KettleException(BaseMessages.getString(PKG, "Mail.Exception.CouldnotFindPortField",realPort)); //$NON-NLS-1$ //$NON-NLS-2$
				}  
			}
			// Authentication
			if(meta.isUsingAuthentication()){
				// cache the position of the Authentication user field			
				if (data.indexOfAuthenticationUser<0){	
					String realAuthenticationUser=meta.getAuthenticationUser();
					data.indexOfAuthenticationUser =data.previousRowMeta.indexOfValue(realAuthenticationUser);
					if (data.indexOfAuthenticationUser<0)
						throw new KettleException(BaseMessages.getString(PKG, "Mail.Exception.CouldnotFindAuthenticationUserField",realAuthenticationUser)); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				// cache the position of the Authentication password field			
				if (data.indexOfAuthenticationPass<0){	
					String realAuthenticationPassword=meta.getAuthenticationPassword();
					data.indexOfAuthenticationPass =data.previousRowMeta.indexOfValue(realAuthenticationPassword);
					if (data.indexOfAuthenticationPass<0)
						throw new KettleException(BaseMessages.getString(PKG, "Mail.Exception.CouldnotFindAuthenticationPassField",realAuthenticationPassword)); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			// Mail Subject
			if (!Const.isEmpty(meta.getSubject())){
				// cache the position of the subject field			
				if (data.indexOfSubject<0){	
					String realSubject=meta.getSubject();
					data.indexOfSubject =data.previousRowMeta.indexOfValue(realSubject);
					if (data.indexOfSubject<0)
						throw new KettleException(BaseMessages.getString(PKG, "Mail.Exception.CouldnotFindSubjectField",realSubject)); //$NON-NLS-1$ //$NON-NLS-2$
				}  
			}
			// Mail Comment
			if (!Const.isEmpty(meta.getComment())){
				// cache the position of the comment field			
				if (data.indexOfComment<0){	
					String realComment=meta.getComment();
					data.indexOfComment =data.previousRowMeta.indexOfValue(realComment);
					if (data.indexOfComment<0)
						throw new KettleException(BaseMessages.getString(PKG, "Mail.Exception.CouldnotFindCommentField",realComment)); //$NON-NLS-1$ //$NON-NLS-2$
				}  
			}	
			
			// Dynamic Zipfilename
			if (meta.isZipFilenameDynamic()){
				// cache the position of the attached source filename field			
				if (data.indexOfDynamicZipFilename<0){	
					String realZipFilename=meta.getDynamicZipFilenameField();
					data.indexOfDynamicZipFilename =data.previousRowMeta.indexOfValue(realZipFilename);
					if (data.indexOfDynamicZipFilename<0)
						throw new KettleException(BaseMessages.getString(PKG, "Mail.Exception.CouldnotSourceAttachedZipFilenameField",realZipFilename)); //$NON-NLS-1$ //$NON-NLS-2$
				}  
			}
			data.zipFileLimit=Const.toLong(environmentSubstitute(meta.getZipLimitSize()), 0);
			if(data.zipFileLimit>0) data.zipFileLimit=data.zipFileLimit*1048576; // Mo
			
			if(!meta.isZipFilenameDynamic()) data.ZipFilename=environmentSubstitute(meta.getZipFilename());
			 
			
			// Attached files
			if(meta.isDynamicFilename()){
				// cache the position of the attached source filename field			
				if (data.indexOfSourceFilename<0){	
					String realSourceattachedFilename=meta.getDynamicFieldname();
					data.indexOfSourceFilename =data.previousRowMeta.indexOfValue(realSourceattachedFilename);
					if (data.indexOfSourceFilename<0)
						throw new KettleException(BaseMessages.getString(PKG, "Mail.Exception.CouldnotSourceAttachedFilenameField",realSourceattachedFilename)); //$NON-NLS-1$ //$NON-NLS-2$
				}  
				
				// cache the position of the attached wildcard field		
				if(!Const.isEmpty(meta.getSourceWildcard())){
					if (data.indexOfSourceWildcard<0){	
						String realSourceattachedWildcard=meta.getDynamicWildcard();
						data.indexOfSourceWildcard =data.previousRowMeta.indexOfValue(realSourceattachedWildcard);
						if (data.indexOfSourceWildcard<0)
							throw new KettleException(BaseMessages.getString(PKG, "Mail.Exception.CouldnotSourceAttachedWildcard",realSourceattachedWildcard)); //$NON-NLS-1$ //$NON-NLS-2$
					}  
				}		
			}else
			{
				// static attached filenames
				data.realSourceFileFoldername=environmentSubstitute(meta.getSourceFileFoldername()) ; 
				data.realSourceWildcard=environmentSubstitute(meta.getSourceWildcard()) ; 
			}
		} // end if first
		
		boolean sendToErrorRow=false;
		String errorMessage = null;
		 
		try{
			// get values
			String maildestination= data.previousRowMeta.getString(r,data.indexOfDestination);
			if(Const.isEmpty(maildestination))	throw new KettleException("Mail.Error.MailDestinationEmpty");
			String maildestinationCc= null;
			if(data.indexOfDestinationCc>-1) maildestinationCc=data.previousRowMeta.getString(r,data.indexOfDestinationCc);
			String maildestinationBCc= null;
			if(data.indexOfDestinationBCc>-1) maildestinationBCc=data.previousRowMeta.getString(r,data.indexOfDestinationBCc);
			
			String mailsendername= null;
			if(data.indexOfSenderName>-1) mailsendername=data.previousRowMeta.getString(r,data.indexOfSenderName);
			String mailsenderaddress=data.previousRowMeta.getString(r,data.indexOfSenderAddress);
			
			// reply addresses
			String mailreplyToAddresses= null;
			if(data.indexOfReplyToAddresses>-1) mailreplyToAddresses=data.previousRowMeta.getString(r,data.indexOfReplyToAddresses);
			
			String contactperson= null;
			if(data.indexOfContactPerson>-1) contactperson=data.previousRowMeta.getString(r,data.indexOfContactPerson);
			String contactphone= null;
			if(data.indexOfContactPhone>-1) contactphone=data.previousRowMeta.getString(r,data.indexOfContactPhone);
			
			String servername=data.previousRowMeta.getString(r,data.indexOfServer);
			if(Const.isEmpty(servername))	throw new KettleException("Mail.Error.MailServerEmpty");
			int port=-1;
			if(data.indexOfPort>-1) port= Const.toInt(""+data.previousRowMeta.getInteger(r,data.indexOfPort),-1);

			String authuser=null;
			if(data.indexOfAuthenticationUser>-1) authuser=data.previousRowMeta.getString(r,data.indexOfAuthenticationUser);
			String authpass=null;
			if(data.indexOfAuthenticationPass>-1) authpass=data.previousRowMeta.getString(r,data.indexOfAuthenticationPass);
			
			String subject=null;
			if(data.indexOfSubject>-1) subject=data.previousRowMeta.getString(r,data.indexOfSubject);
			
			String comment=null;
			if(data.indexOfComment>-1) comment=data.previousRowMeta.getString(r,data.indexOfComment);
			
			// send email...
			sendMail(r,servername, port,mailsenderaddress,mailsendername,maildestination,
					maildestinationCc,maildestinationBCc,contactperson,contactphone,
					authuser,authpass,subject,comment,mailreplyToAddresses);
			
			putRow(getInputRowMeta(), r);     // copy row to possible alternate rowset(s).);  // copy row to output rowset(s);

	        if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "Mail.Log.LineNumber",getLinesRead()+" : "+getInputRowMeta().getString(r)));
	        
		} catch (Exception e)
		 {
			 if (getStepMeta().isDoingErrorHandling()){
		        sendToErrorRow = true;
		        errorMessage = e.toString();
			}
			else {
				throw new KettleException(BaseMessages.getString(PKG, "Mail.Error.General"), e);
			}
			if (sendToErrorRow){
			   // Simply add this row to the error row
			   putError(getInputRowMeta(), r, 1, errorMessage, null, "MAIL001");
			}
		 }
			
		return true;
	}

	  public  void sendMail(Object[] r,String server, int port,
			  String senderAddress,String senderName,String destination,String destinationCc,
			  String destinationBCc,
			  String contactPerson, String contactPhone,
			  String authenticationUser,String authenticationPassword,
			  String mailsubject, String comment, String replyToAddresses) throws Exception
	  {

	      // Send an e-mail...
	      // create some properties and get the default Session

	      String protocol = "smtp";
	      if (meta.isUsingSecureAuthentication()) { //PDI-2955
	      //if (meta.isUsingAuthentication()) {
	    	if (meta.getSecureConnectionType().equals("TLS")){
	    		// Allow TLS authentication
	    		data.props.put("mail.smtp.starttls.enable","true"); 
	    	}
	    	else{
	    		 protocol = "smtps";
	    	      // required to get rid of a SSL exception :
	    	      //  nested exception is:
	    	  	  //  javax.net.ssl.SSLException: Unsupported record version Unknown
	    	      data.props.put("mail.smtps.quitwait", "false");
	    	}
	      }
	      data.props.put("mail." + protocol + ".host", server);
	      if (port!=-1) data.props.put("mail." + protocol + ".port", port);
	      boolean debug = LogWriter.getInstance().getLogLevel() >= LogWriter.LOG_LEVEL_DEBUG;

	      if (debug) data.props.put("mail.debug", "true");

	      if (meta.isUsingAuthentication())  data.props.put("mail." + protocol + ".auth", "true");

	      Session session = Session.getInstance(data.props);
	      session.setDebug(debug);

	      // create a message
	      Message msg = new MimeMessage(session);
	      
	      // set message priority
	      if (meta.isUsePriority()) {
	    	 String priority_int="1";
	    	 if (meta.getPriority().equals("low"))  		  priority_int="3";
	    	 if (meta.getPriority().equals("normal")) 		  priority_int="2";
	    	  
			 msg.setHeader("X-Priority",priority_int); //(String)int between 1= high and 3 = low.
			 msg.setHeader("Importance", meta.getImportance());
			 //seems to be needed for MS Outlook.
			 //where it returns a string of high /normal /low.
	      }
	
	      // set Email sender
	      String email_address = senderAddress;
	      if (!Const.isEmpty(email_address)){
	    	// get sender name
	    	if(!Const.isEmpty(senderName)) email_address=senderName+'<'+email_address+'>';	 	 
	        msg.setFrom(new InternetAddress(email_address));
	      } else {
	        throw new MessagingException(BaseMessages.getString(PKG, "Mail.Error.ReplyEmailNotFilled"));
	      }
	
	      // Set reply to 
	      if (!Const.isEmpty(replyToAddresses))
	      { 
		      // get replay to
	    	  // Split the mail-address: space separated
		      String[] reply_Address_List =replyToAddresses.split(" "); 
		      InternetAddress[] address = new InternetAddress[reply_Address_List.length]; 
		      
		      for (int i = 0; i < reply_Address_List.length; i++) 
		    	  address[i] = new InternetAddress(reply_Address_List[i]); 
		      
		      // To add the real reply-to 
		      msg.setReplyTo(address); 
	      }
	      
	      // Split the mail-address: space separated
	      String destinations[] = destination.split(" ");
	      InternetAddress[] address = new InternetAddress[destinations.length];
	      for (int i = 0; i < destinations.length; i++)
	        address[i] = new InternetAddress(destinations[i]);
	
	      msg.setRecipients(Message.RecipientType.TO, address);
	
	      String realdestinationCc=destinationCc;
	      if (!Const.isEmpty(realdestinationCc))
	      {
	        // Split the mail-address Cc: space separated
	        String destinationsCc[] = realdestinationCc.split(" ");
	        InternetAddress[] addressCc = new InternetAddress[destinationsCc.length];
	        for (int i = 0; i < destinationsCc.length; i++)
	          addressCc[i] = new InternetAddress(destinationsCc[i]);
	
	        msg.setRecipients(Message.RecipientType.CC, addressCc);
	      }
	
	      String realdestinationBCc=destinationBCc;
	      if (!Const.isEmpty(realdestinationBCc))
	      {
	        // Split the mail-address BCc: space separated
	        String destinationsBCc[] = realdestinationBCc.split(" ");
	        InternetAddress[] addressBCc = new InternetAddress[destinationsBCc.length];
	        for (int i = 0; i < destinationsBCc.length; i++)
	          addressBCc[i] = new InternetAddress(destinationsBCc[i]);
	
	        msg.setRecipients(Message.RecipientType.BCC, addressBCc);
	      }
	
	      if (mailsubject!=null)   msg.setSubject(mailsubject);

	      msg.setSentDate(new Date());
	      StringBuffer messageText = new StringBuffer();
	
	      if (comment != null)   messageText.append(comment).append(Const.CR).append(Const.CR);
	    
	
	      if (meta.getIncludeDate())
	        messageText.append(BaseMessages.getString(PKG, "Mail.Log.Comment.MsgDate") +": ").append(XMLHandler.date2string(new Date())).append(Const.CR).append(
	            Const.CR);
	
	
	      if (!meta.isOnlySendComment()
	          && (!Const.isEmpty(contactPerson) || !Const.isEmpty(contactPhone))){
	        messageText.append(BaseMessages.getString(PKG, "Mail.Log.Comment.ContactInfo") + " :").append(Const.CR);
	        messageText.append("---------------------").append(Const.CR);
	        messageText.append(BaseMessages.getString(PKG, "Mail.Log.Comment.PersonToContact")+" : ").append(contactPerson).append(Const.CR);
	        messageText.append(BaseMessages.getString(PKG, "Mail.Log.Comment.Tel") + "  : ").append(contactPhone).append(Const.CR);
	        messageText.append(Const.CR);
	      }
	      data.parts = new MimeMultipart(); 
	      
	      MimeBodyPart part1 = new MimeBodyPart(); // put the text in the
	      // 1st part
	
	      if (meta.isUseHTML()){
	        if (!Const.isEmpty(meta.getEncoding()))
	          part1.setContent(messageText.toString(), "text/html; " + "charset=" + meta.getEncoding());
	        else
	          part1.setContent(messageText.toString(), "text/html; " + "charset=ISO-8859-1");
	      } else
	        part1.setText(messageText.toString());
	
	      data.parts.addBodyPart(part1);
	      
	      // attached files
	      if(meta.isDynamicFilename()) setAttachedFilesList(r, log);
	      else setAttachedFilesList(null,log);
  
	      msg.setContent(data.parts);
	      
	      Transport transport = null;
	      try {
	        transport = session.getTransport(protocol);
	        if (meta.isUsingAuthentication()) {
	          if (port!=-1){
	            transport.connect(Const.NVL(server, ""), port, Const.NVL(authenticationUser, ""), 
	            		Const.NVL(authenticationPassword, ""));
	          }else {
	            transport.connect(Const.NVL(server, ""), Const.NVL(
	                authenticationUser, ""), Const.NVL(authenticationPassword, ""));
	          }
	        } else {
	          transport.connect();
	        }
	        transport.sendMessage(msg, msg.getAllRecipients());
	      } finally {
	        if (transport != null) transport.close();
	      }
	     

	  }
	  private void setAttachedFilesList(Object[] r, LogChannelInterface log) throws Exception
	  {
		  String realSourceFileFoldername=null;
		  String realSourceWildcard=null;
		  FileObject sourcefile=null;
		  FileObject file=null;
		  
		  ZipOutputStream zipOutputStream = null;
		  File masterZipfile = null;

		  if(meta.isZipFilenameDynamic())  data.ZipFilename=data.previousRowMeta.getString(r,data.indexOfDynamicZipFilename);

		  try{

			  if(meta.isDynamicFilename()) {
				 // dynamic attached filenames
				  if(data.indexOfSourceFilename>-1)
					  realSourceFileFoldername= data.previousRowMeta.getString(r,data.indexOfSourceFilename);
				  
				  if(data.indexOfSourceWildcard>-1)
					  realSourceWildcard= data.previousRowMeta.getString(r,data.indexOfSourceWildcard);
				  
			  }else {
				// static attached filenames
				realSourceFileFoldername=data.realSourceFileFoldername ; 
				realSourceWildcard=data.realSourceWildcard; 
			  }
			  
			
				if(!Const.isEmpty(realSourceFileFoldername)){
					sourcefile=KettleVFS.getFileObject(realSourceFileFoldername, getTransMeta());
					if(sourcefile.exists()){
						long FileSize=0;
						FileObject list[]=null;
						if(sourcefile.getType()==FileType.FILE) 
						{
							list = new FileObject[1];
							list[0]=sourcefile; 
						}
						else
							list = sourcefile.findFiles(new TextFileSelector (sourcefile.toString(),realSourceWildcard));  
						if(list.length>0){

							 boolean zipFiles=meta.isZipFiles();
							if(zipFiles && data.zipFileLimit==0){
					            masterZipfile = new File(System.getProperty("java.io.tmpdir") + Const.FILE_SEPARATOR
					                    + data.ZipFilename);
					                
					            zipOutputStream = new ZipOutputStream(new FileOutputStream(masterZipfile));
							}
					
						 	for ( int i=0; i < list.length; i++ ) {
	
						    	  file=KettleVFS.getFileObject(KettleVFS.getFilename(list[i]), getTransMeta());
						    	  
						    	  if(zipFiles){
						    		  
						    		  if(data.zipFileLimit==0)
						    		  {
							    		  ZipEntry zipEntry = new ZipEntry(file.getName().getBaseName());
						                  zipOutputStream.putNextEntry(zipEntry);
	
						                  // Now put the content of this file into this archive...
						                  BufferedInputStream inputStream = new BufferedInputStream(file.getContent().getInputStream());
						                  int c;
						                  while ((c = inputStream.read()) >= 0)
						                  {
						                    zipOutputStream.write(c);
						                  }
						                  inputStream.close();
						                  zipOutputStream.closeEntry();  
						    		  }else
						    			  FileSize+=file.getContent().getSize();
						    	  }else
						    	  {
						    		  addAttachedFilePart(file);
						    	  }
					        } // end for
						 	if(zipFiles) {	
						 		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "Mail.Log.FileSize",""+FileSize));
						 		if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "Mail.Log.LimitSize",""+data.zipFileLimit));
						 		
						 		if(data.zipFileLimit>0 && FileSize>data.zipFileLimit){
							
							            masterZipfile = new File(System.getProperty("java.io.tmpdir") + Const.FILE_SEPARATOR
							                    + data.ZipFilename);
							                
							            zipOutputStream = new ZipOutputStream(new FileOutputStream(masterZipfile));
							
						 			for ( int i=0; i < list.length; i++ ) {
						 				
						 				file=KettleVFS.getFileObject(KettleVFS.getFilename(list[i]), getTransMeta());
						 				
						 				 ZipEntry zipEntry = new ZipEntry(file.getName().getBaseName());
						                  zipOutputStream.putNextEntry(zipEntry);
	
						                  // Now put the content of this file into this archive...
						                  BufferedInputStream inputStream = new BufferedInputStream(file.getContent().getInputStream());
						                  int c;
						                  while ((c = inputStream.read()) >= 0)
						                  {
						                    zipOutputStream.write(c);
						                  }
						                  inputStream.close();
						                  zipOutputStream.closeEntry();  
						 				
						 			}
						 			
						 		}
						 		if(data.zipFileLimit>0 && FileSize>data.zipFileLimit || data.zipFileLimit==0)
						 		{
						 			file=KettleVFS.getFileObject(masterZipfile.getAbsolutePath(), getTransMeta());
						 			addAttachedFilePart(file); 
						 		}
						 	}
						}
					}else{
						logError(BaseMessages.getString(PKG, "Mail.Error.SourceFileFolderNotExists",realSourceFileFoldername));
					}
				}	
			}catch(Exception e)
			{
				logError(e.getMessage());
			}
			finally{
				if(sourcefile!=null){try{sourcefile.close();}catch(Exception e){}}
				if(file!=null){try{file.close();}catch(Exception e){}}
				
				  if (zipOutputStream != null){
	                try{
	                  zipOutputStream.finish();
	                  zipOutputStream.close();
	                } catch (IOException e)
	                {
	                  logError("Unable to close attachement zip file archive : " + e.toString());
	                }
	              }
			}
			
	  }
	  private void addAttachedFilePart(FileObject file) throws Exception
	  {
		  // create a data source
		  
		  MimeBodyPart files = new MimeBodyPart();
	      // create a data source
          URLDataSource fds = new URLDataSource(file.getURL());
          // get a data Handler to manipulate this file type;
          files.setDataHandler(new DataHandler(fds));
          // include the file in the data source
          files.setFileName(file.getName().getBaseName());
          // add the part with the file in the BodyPart();
          data.parts.addBodyPart(files);
          if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "Mail.Log.AttachedFile",fds.getName()));
          
	  }
	  private class TextFileSelector implements FileSelector 
		{
			LogWriter log = LogWriter.getInstance();
			String file_wildcard=null,source_folder=null;
			
			public TextFileSelector(String sourcefolderin,String filewildcard) 
			 {
				 if ( !Const.isEmpty(sourcefolderin))
					 source_folder=sourcefolderin;
				
				 if ( !Const.isEmpty(filewildcard))
					 file_wildcard=filewildcard;
			 }
			 
			public boolean includeFile(FileSelectInfo info) 
			{
				boolean returncode=false;
				try
				{
					if (!info.getFile().toString().equals(source_folder))
					{
						// Pass over the Base folder itself
						String short_filename= info.getFile().getName().getBaseName();
						
						if (info.getFile().getParent().equals(info.getBaseFolder()) ||
							((!info.getFile().getParent().equals(info.getBaseFolder()) && meta.isIncludeSubFolders())))	
						 {
							if((info.getFile().getType() == FileType.FILE && file_wildcard==null) ||
							(info.getFile().getType() == FileType.FILE && file_wildcard!=null && GetFileWildcard(short_filename,file_wildcard)))
								returncode=true;
						 }	
					}	
				}
				catch (Exception e) 
				{
					logError(BaseMessages.getString(PKG, "Mail.Error.FindingFiles", info.getFile().toString(),e.getMessage()));
					 returncode= false;
				}
				return returncode;
			}

			public boolean traverseDescendents(FileSelectInfo info) 
			{
				return true;
			}
		}
	  /**********************************************************
		 * 
		 * @param selectedfile
		 * @param wildcard
		 * @return True if the selectedfile matches the wildcard
		 **********************************************************/
		private boolean GetFileWildcard(String selectedfile, String wildcard)
		{
			Pattern pattern = null;
			boolean getIt=true;
		
	        if (!Const.isEmpty(wildcard))
	        {
	        	 pattern = Pattern.compile(wildcard);
				// First see if the file matches the regular expression!
				if (pattern!=null)
				{
					Matcher matcher = pattern.matcher(selectedfile);
					getIt = matcher.matches();
				}
	        }
			
			return getIt;
		}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(MailMeta)smi;
		data=(MailData)sdi;
		
		if (super.init(smi, sdi)){
		    // Add init code here.
		    return true;
		}
		return false;
	}
	
	  //
    //
    // Run is were the action happens!
	//
	// Run is were the action happens!
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}
    
}
