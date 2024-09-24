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

package org.pentaho.di.job.entries.getpop;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;

public class JobEntryGetPOPLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryGetPOP> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryGetPOP> getJobEntryClass() {
    return JobEntryGetPOP.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( new String[] { "serverName", "userName", "password", "useSSL", "port", "outputDirectory",
      "filenamePattern", "retrievemails", "firstMails", "delete", "saveMessage", "saveAttachment",
      "differentFolderForAttachment", "protocol", "attachmentFolder", "attachmentWildcard", "valueImapList",
      "firstIMAPMails", "IMAPFolder", "senderSearchTerm", "notTermSenderSearch", "receipientSearch",
      "notTermReceipientSearch", "subjectSearch", "notTermSubjectSearch", "bodySearch", "notTermBodySearch",
      "conditionReceivedDate", "notTermReceivedDateSearch", "receivedDate1", "receivedDate2", "actiontype",
      "moveToIMAPFolder", "createMoveToFolder", "createLocalFolder", "afterGetIMAP", "includeSubFolders",
      "useProxy", "proxyUsername" } );
  }

  @Override
  protected Map<String, FieldLoadSaveValidator<?>> createAttributeValidatorsMap() {
    Map<String, FieldLoadSaveValidator<?>> validators = new HashMap<String, FieldLoadSaveValidator<?>>();
    validators.put( "valueImapList", new IntLoadSaveValidator( MailConnectionMeta.valueIMAPListCode.length ) );
    validators.put( "conditionReceivedDate", new IntLoadSaveValidator( MailConnectionMeta.conditionDateCode.length ) );
    validators.put( "actiontype", new IntLoadSaveValidator( MailConnectionMeta.actionTypeCode.length ) );
    validators.put( "afterGetIMAP", new IntLoadSaveValidator( MailConnectionMeta.afterGetIMAPCode.length ) );

    return validators;
  }

}
