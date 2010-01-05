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

import java.util.HashSet;
import java.util.Properties;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Send mail step.
 * based on Mail job entry
 * @author Samatar
 * @since 28-07-2008
 */
public class MailData extends BaseStepData implements StepDataInterface
{
	public int indexOfDestination;
	public int indexOfDestinationCc;
	public int indexOfDestinationBCc;
	
	public int indexOfSenderName;
	public int indexOfSenderAddress;
	
	public int indexOfContactPerson;
	public int indexOfContactPhone;
	
	public int indexOfServer;
	public int indexOfPort;
	
	public int indexOfAuthenticationUser;
	public int indexOfAuthenticationPass;
	
	public int indexOfSubject;
	public int indexOfComment;
	
	public int indexOfSourceFilename;
	public int indexOfSourceWildcard;
	public long zipFileLimit;
	
	public int indexOfDynamicZipFilename;
	
	public String ZipFilename;

    Properties props;
	
	public MimeMultipart parts;
	
	public RowMetaInterface previousRowMeta;
	
	public int indexOfReplyToAddresses;
	
	public String realSourceFileFoldername;
	
	public String realSourceWildcard;
	
	public int nrEmbeddedImages;
	public int nrattachedFiles;
	
	public HashSet<MimeBodyPart> embeddedMimePart;
	
	/**
	 * 
	 */
	public MailData()
	{
		super();
		indexOfDestination=-1;
		indexOfDestinationCc=-1;
		indexOfDestinationBCc=-1;
		indexOfSenderName=-1;
		indexOfSenderAddress=-1;
		indexOfContactPerson=-1;
		indexOfContactPhone=-1;
		indexOfServer=-1;
		indexOfPort=-1;
		indexOfAuthenticationUser=-1;
		indexOfAuthenticationPass=-1;
		indexOfSubject=-1;
		indexOfComment=-1;
		indexOfSourceFilename=-1;
		indexOfSourceWildcard=-1;
		zipFileLimit=0;
		indexOfDynamicZipFilename=-1;
		props= new Properties();
		indexOfReplyToAddresses=-1;
		embeddedMimePart=null;
		nrEmbeddedImages=0;
		nrattachedFiles=0;
		
	}

}
