/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.job.entries.mail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.ClassRule;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class JobEntryMailLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryMail> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryMail> getJobEntryClass() {
    return JobEntryMail.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( new String[] { "server", "port", "destination", "destinationCc", "destinationBCc",
      "replyAddress", "replyName", "subject", "includeDate", "contactPerson", "contactPhone", "comment",
      "includingFiles", "zipFiles", "zipFilename", "usingAuthentication", "usingSecureAuthentication",
      "authenticationUser", "authenticationPassword", "onlySendComment", "useHTML", "usePriority",
      "encoding", "priority", "importance", "sensitivity", "secureConnectionType", "replyToAddresses",
      "fileType", "embeddedimages", "contentids" } );
  }

  @Override
  protected Map<String, FieldLoadSaveValidator<?>> createAttributeValidatorsMap() {
    Map<String, FieldLoadSaveValidator<?>> validators = new HashMap<String, FieldLoadSaveValidator<?>>();
    validators.put( "fileType", new PrimitiveIntArrayLoadSaveValidator(
      new IntLoadSaveValidator( ResultFile.fileTypeCode.length ) ) );

    int entries = new Random().nextInt( 20 ) + 1;
    validators.put( "embeddedimages", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), entries ) );
    validators.put( "contentids", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), entries ) );
    return validators;
  }

}
