/*************************************************************************************** 
 * Copyright (C) 2009 Samatar.  All rights reserved. 
 * This software was developed by Samatar and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/
 
package org.pentaho.di.trans.steps.mailinput;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entries.getpop.MailConnection;
import org.pentaho.di.job.entries.getpop.MailConnectionMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;



/**
 * Read data from POP3/IMAP server and input data to the next steps.
 * 
 * @author Samatar
 * @since 21-08-2009
 */

public class MailInput extends BaseStep implements StepInterface
{
	private static Class<?> PKG = MailInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	private MailInputMeta meta;
	private MailInputData data;
	
	public MailInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(MailInputMeta)smi;
		data=(MailInputData)sdi;

		
		Object[] outputRowData=getOneRow();
		
		if (outputRowData==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
	
		if(log.isRowLevel())
		{
			log.logRowlevel(toString(), BaseMessages.getString(PKG, "MailInput.Log.OutputRow",data.outputRowMeta.getString(outputRowData)));
		}
		putRow(data.outputRowMeta, outputRowData);  // copy row to output rowset(s);
	
		if (data.rowlimit>0 && data.rownr>=data.rowlimit)  // limit has been reached: stop now.
	    {
	        setOutputDone();
	        return false;
	    }
		
		return true;
	}
	public String[] getFolders(String realIMAPFolder) throws KettleException
	{
		data.folderenr=0;
		data.messagesCount=0;
		data.rownr=0;
		String[] folderslist=null;
		if(meta.isIncludeSubFolders()) {
			String[] folderslist0= data.mailConn.returnAllFolders(realIMAPFolder);
			if(folderslist0==null || folderslist0.length==0) {
				folderslist= new String[] {Const.NVL(realIMAPFolder, MailConnectionMeta.INBOX_FOLDER)};
			}else {
				folderslist= new String[folderslist0.length+1];
				folderslist[0]=Const.NVL(realIMAPFolder, MailConnectionMeta.INBOX_FOLDER);
				for(int i=0; i<folderslist0.length;i++) {
					folderslist[i+1]=folderslist0[i];
				}
			}
		}else
			folderslist= new String[] {Const.NVL(realIMAPFolder, MailConnectionMeta.INBOX_FOLDER)};
		return folderslist;
	}
    private void applySearch(Date beginDate, Date endDate)
    {
		// apply search term?
		String realSearchSender=environmentSubstitute(meta.getSenderSearchTerm());
		if(!Const.isEmpty(realSearchSender)) {
			// apply FROM
			data.mailConn.setSenderTerm(realSearchSender, meta.isNotTermSenderSearch());
		}
		String realSearchReceipient=environmentSubstitute(meta.getRecipientSearch());
		if(!Const.isEmpty(realSearchReceipient)) {
			// apply TO
			data.mailConn.setReceipientTerm(realSearchReceipient);
		}
		String realSearchSubject=environmentSubstitute(meta.getSubjectSearch());
		if(!Const.isEmpty(realSearchSubject)) {
			// apply Subject
			data.mailConn.setSubjectTerm(realSearchSubject, meta.isNotTermSubjectSearch());
		}
		// Received Date
		switch (meta.getConditionOnReceivedDate()) {
			case MailConnectionMeta.CONDITION_DATE_EQUAL: 
				data.mailConn.setReceivedDateTermEQ(beginDate);
			break;
			case MailConnectionMeta.CONDITION_DATE_GREATER:  
				data.mailConn.setReceivedDateTermGT(beginDate);
			break;
			case MailConnectionMeta.CONDITION_DATE_SMALLER:  
				data.mailConn.setReceivedDateTermLT(beginDate);
			break;
			case MailConnectionMeta.CONDITION_DATE_BETWEEN:  
				data.mailConn.setReceivedDateTermBetween(beginDate, endDate);
			break;
			default:
			break;
		}
		// set FlagTerm?
		if(data.usePOP) {
			// retrieve messages	
			if(meta.getRetrievemails()==1) {
				// New messages
				data.mailConn.setFlagTermNew();
			}
		}else {
			switch (meta.getValueImapList()) {
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

	private Object[] buildEmptyRow()
	{
        Object[] rowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());
 
		 return rowData;
	}

	private Object[] getOneRow()  throws KettleException
	{		
		while ((data.rownr>=data.messagesCount || data.folder==null)) {
			if (!openNextFolder()) return null;
		}	
	 
        Object[] r= buildEmptyRow();     
        if(meta.isDynamicFolder()) System.arraycopy(data.readrow, 0, r, 0, data.readrow.length);

        try {
			// Get next message
			data.mailConn.fetchNext();
			
			if(log.isDebug()) log.logDebug(toString(),BaseMessages.getString(PKG, "MailInput.Log.FetchingMessage",data.mailConn.getMessage().getMessageNumber()));
			
			// Execute for each Input field...
			for (int i=0;i<meta.getInputFields().length;i++)
			{
				int index=data.totalpreviousfields+i;
				switch (meta.getInputFields()[i].getColumn())
				{
					case MailInputField.COLUMN_MESSAGE_NR:
						r[index] = new Long(data.mailConn.getMessage().getMessageNumber());
						break;
					case MailInputField.COLUMN_SUBJECT:
						r[index] = data.mailConn.getMessage().getSubject();
						break;
					case MailInputField.COLUMN_SENDER:
						String From=null;
						if(data.mailConn.getMessage().getFrom()!=null) {
							for(int f=0; f<data.mailConn.getMessage().getFrom().length; f++) {
								if(From==null)
									From=data.mailConn.getMessage().getFrom()[f].toString();
								else
									From+=";"+data.mailConn.getMessage().getFrom()[f].toString();
							}
						}
						r[index] =From;
						break;
					case MailInputField.COLUMN_REPLY_TO:
						String replyto=null;
						if(data.mailConn.getMessage().getFrom()!=null) {
							for(int f=0; f<data.mailConn.getMessage().getReplyTo().length; f++) {
								if(replyto==null)
									replyto=data.mailConn.getMessage().getReplyTo()[f].toString();
								else
									replyto+=";"+data.mailConn.getMessage().getReplyTo()[f].toString();
							}
						}
						r[index]=replyto;
						break;
					case MailInputField.COLUMN_RECIPIENTS:
						String Recipients=null;
						for(int f=0; f<data.mailConn.getMessage().getAllRecipients().length; f++) {
							if(Recipients==null)
								Recipients=data.mailConn.getMessage().getAllRecipients()[f].toString();
							else
								Recipients+=";"+data.mailConn.getMessage().getAllRecipients()[f].toString();
						}
						r[index]=Recipients;
						break;
					case MailInputField.COLUMN_DESCRIPTION:
						r[index]=data.mailConn.getMessage().getDescription();
						break;
					case MailInputField.COLUMN_BODY:
						r[index]=data.mailConn.getMessageBody();
						break;
					case MailInputField.COLUMN_RECEIVED_DATE:
						r[index]= new Date(data.mailConn.getMessage().getReceivedDate().getTime());
						break;
					case MailInputField.COLUMN_SENT_DATE:
						r[index]= new Date(data.mailConn.getMessage().getReceivedDate().getTime());
						break;
					case MailInputField.COLUMN_CONTENT_TYPE:
						r[index]=data.mailConn.getMessage().getContentType();
						break;
					case MailInputField.COLUMN_FOLDER_NAME:
						r[index]=data.mailConn.getFolderName();
						break;
					case MailInputField.COLUMN_SIZE:
						r[index]=new Long(data.mailConn.getMessage().getSize());
						break;
					case MailInputField.COLUMN_FLAG_DRAFT:
						r[index]=new Boolean(data.mailConn.isMessageDraft());
						break;
					case MailInputField.COLUMN_FLAG_FLAGGED:
						r[index]=new Boolean(data.mailConn.isMessageFlagged());
						break;
					case MailInputField.COLUMN_FLAG_NEW:
						r[index]=new Boolean(data.mailConn.isMessageNew());
						break;
					case MailInputField.COLUMN_FLAG_READ:
						r[index]=new Boolean(data.mailConn.isMessageRead());
						break;
					case MailInputField.COLUMN_FLAG_DELETED:
						r[index]=new Boolean(data.mailConn.isMessageDeleted());
						break;
					case MailInputField.COLUMN_ATTACHED_FILES_COUNT:
						r[index]=new Long(data.mailConn.getAttachedFilesCount(null));
						break;
					default:
						break;
				}
			}   // End of loop over fields...
			
			
			 incrementLinesInput();
			 data.rownr++;

        }catch(Exception e) {
        	throw new KettleException("Error adding values to row!", e);
        }


		return r;
	}
	private boolean openNextFolder() {
		try {
			if(!meta.isDynamicFolder()) {
				// static folders list
				// let's check if we fetched all values in list
				 if (data.folderenr>=data.folders.length){	
					 // We have fetched all folders
	            	if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "MailInput.Log.FinishedProcessing"));
	                return false;
	            }
			}else {
				// dynamic folders
				if(first) {
					first=false;
					
					data.readrow=getRow();     // Get row from input rowset & set row busy!
					if (data.readrow==null) {
						if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "MailInput.Log.FinishedProcessing"));
					    return false;
					}
					
	            	data.inputRowMeta = getInputRowMeta();
		            data.outputRowMeta = data.inputRowMeta.clone();
		            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
		            
		            // Get total previous fields
		            data.totalpreviousfields=data.inputRowMeta.size();
					
					if(Const.isEmpty(meta.getFolderField())) {
						log.logError(toString(), BaseMessages.getString(PKG, "MailInput.Error.DynamicFolderFieldMissing"));
						stopAll();
						setErrors(1);
						return false;	
					}
					
					data.indexOfFolderField=data.inputRowMeta.indexOfValue(meta.getFolderField());
					if(data.indexOfFolderField<0){
						log.logError(toString(), BaseMessages.getString(PKG, "MailInput.Error.DynamicFolderUnreachable",meta.getFolderField()));
						stopAll();
						setErrors(1);
						return false;
					}
					
					// get folder
					String foldername=data.inputRowMeta.getString(data.readrow, data.indexOfFolderField);
					if(log.isDebug()) log.logDebug(toString(),BaseMessages.getString(PKG, "MailInput.Log.FoldernameInStream", meta.getFolderField(),foldername));
					data.folders=getFolders(foldername);
				} // end if first
				
				 if (data.folderenr>=data.folders.length) {	
					 // we have fetched all values for input row
					 // grab another row
					 data.readrow=getRow();     // Get row from input rowset & set row busy!
					 if (data.readrow==null) {
						if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "MailInput.Log.FinishedProcessing"));
					    return false;
					 }
					 // get folder
				     String foldername=data.inputRowMeta.getString(data.readrow, data.indexOfFolderField);
					 data.folders=getFolders(foldername);
				 }
			}
			
			 // Get the current folder
			 data.folder=data.folders[data.folderenr];
				
			  // Move folder pointer ahead!
			 data.folderenr++;
			
			
			// open folder
			if(!data.usePOP && !Const.isEmpty(data.folder)) {
				data.mailConn.openFolder(data.folder, false);
			} else {
				data.mailConn.openFolder(false);
			}
			
			// retrieve messages	
			data.mailConn.retrieveMessages();
			data.messagesCount=data.mailConn.getMessagesCount();
			
			if(log.isDebug()) log.logDebug(toString(),BaseMessages.getString(PKG, "MailInput.Log.MessagesInFolder",data.folder,data.messagesCount));	
			
		} catch(Exception e){
			logError("Error opening folder "+data.folderenr + " "+ data.folder+ ": "+  e.toString());
			logError(Const.getStackTracker(e));
			stopAll();
			setErrors(1);
			return false;
		}
		return true;
	}
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(MailInputMeta)smi;
		data=(MailInputData)sdi;
		
		if (super.init(smi, sdi))
		{
			if(!meta.isDynamicFolder()) {
				try {
					// Create the output row meta-data
		            data.outputRowMeta = new RowMeta();
		            meta.getFields(data.outputRowMeta, getStepname(), null, null, this); // get the metadata populated
	
				} catch(Exception e){
					logError(BaseMessages.getString(PKG, "MailInput.ErrorInit",e.toString()));
					logError(Const.getStackTracker(e));
					return false;
				}
			}
			data.usePOP=meta.getProtocol().equals(MailConnectionMeta.PROTOCOL_STRING_POP3);
			
			String realserver=environmentSubstitute(meta.getServerName());
			String realusername=environmentSubstitute(meta.getUserName());
			String realpassword=environmentSubstitute(meta.getPassword());  
			int realport=Const.toInt(environmentSubstitute(meta.getPort()),-1);
			String realProxyUsername=environmentSubstitute(meta.getProxyUsername());
			if(!meta.isDynamicFolder()) {
				String reallimitrow= environmentSubstitute(meta.getRowLimit());
				data.rowlimit=Const.toInt(reallimitrow, 0);
			}
			Date beginDate=null;
			Date endDate=null;
			SimpleDateFormat df  = new SimpleDateFormat();
			
			// check search terms
			// Received Date
			try {
				switch (meta.getConditionOnReceivedDate()) {
					case MailConnectionMeta.CONDITION_DATE_EQUAL: 
					case MailConnectionMeta.CONDITION_DATE_GREATER:  
					case MailConnectionMeta.CONDITION_DATE_SMALLER:  
						String realBeginDate=environmentSubstitute(meta.getReceivedDate1());
						if(Const.isEmpty(realBeginDate))
							throw new KettleException(BaseMessages.getString(PKG, "MailInput.Error.ReceivedDateSearchTermEmpty"));
						beginDate=df.parse(realBeginDate);
					break;
					case MailConnectionMeta.CONDITION_DATE_BETWEEN:  
						realBeginDate=environmentSubstitute(meta.getReceivedDate1());
						if(Const.isEmpty(realBeginDate))
							throw new KettleException(BaseMessages.getString(PKG, "MailInput.Error.ReceivedDatesSearchTermEmpty"));
						beginDate=df.parse(realBeginDate);
						String realEndDate=environmentSubstitute(meta.getReceivedDate2());
						if(Const.isEmpty(realEndDate))
							throw new KettleException(BaseMessages.getString(PKG, "MailInput.Error.ReceivedDatesSearchTermEmpty"));
						endDate=df.parse(realEndDate);
					break;
					default:
					break;
				}
			}catch(Exception e){
				logError(BaseMessages.getString(PKG, "MailInput.Error.SettingSearchTerms",e.getMessage()));
				setErrors(1);
				stopAll();
			}
			try {	
				// create a mail connection object			
				data.mailConn= new MailConnection(log, data.usePOP?MailConnectionMeta.PROTOCOL_POP3:MailConnectionMeta.PROTOCOL_IMAP
						,realserver,realport, realusername, realpassword, meta.isUseSSL(), meta.isUseProxy(), realProxyUsername);
				// connect
				data.mailConn.connect();
				// Need to apply search filters?
				applySearch(beginDate, endDate);
				
				if(!meta.isDynamicFolder()) {
					// pass static folder name
					String realIMAPFolder=environmentSubstitute(meta.getIMAPFolder());
					// return folders list
					// including sub folders if necessary
					data.folders=getFolders(realIMAPFolder);
				}
			}catch(Exception e){
					logError(BaseMessages.getString(PKG, "MailInput.Error.OpeningConnection",e.getMessage()));
					setErrors(1);
					stopAll();
				}
		    return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (MailInputMeta)smi;
	    data = (MailInputData)sdi;
	    
	    if(data.mailConn!=null)
	    {
	    	try {
	    		data.mailConn.disconnect();
	    		data.mailConn=null;
	    	}catch(Exception e){};
	    }
	    
	    super.dispose(smi, sdi);
	}

}
