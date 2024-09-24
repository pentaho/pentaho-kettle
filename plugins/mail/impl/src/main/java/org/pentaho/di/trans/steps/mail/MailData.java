/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.trans.steps.mail;

import java.util.HashSet;
import java.util.Properties;

import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

/**
 * Send mail step. based on Mail job entry
 *
 * @author Samatar
 * @since 28-07-2008
 */
public class MailData extends BaseStepData implements StepDataInterface {
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

  public int indexOfAttachedContent;
  public int IndexOfAttachedFilename;

  public MailData() {
    super();
    indexOfDestination = -1;
    indexOfDestinationCc = -1;
    indexOfDestinationBCc = -1;
    indexOfSenderName = -1;
    indexOfSenderAddress = -1;
    indexOfContactPerson = -1;
    indexOfContactPhone = -1;
    indexOfServer = -1;
    indexOfPort = -1;
    indexOfAuthenticationUser = -1;
    indexOfAuthenticationPass = -1;
    indexOfSubject = -1;
    indexOfComment = -1;
    indexOfSourceFilename = -1;
    indexOfSourceWildcard = -1;
    zipFileLimit = 0;
    indexOfDynamicZipFilename = -1;
    props = new Properties();
    indexOfReplyToAddresses = -1;
    embeddedMimePart = null;
    nrEmbeddedImages = 0;
    nrattachedFiles = 0;
    indexOfAttachedContent = -1;
    IndexOfAttachedFilename = -1;
  }

}
