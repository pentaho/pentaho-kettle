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

package org.pentaho.di.job.entries.mail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.pentaho.di.core.ResultFile;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.PrimitiveIntArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class JobEntryMailLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryMail> {

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
