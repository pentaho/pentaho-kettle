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


package org.pentaho.di.job.entries.http;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class JobEntryHTTPLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryHTTP> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryHTTP> getJobEntryClass() {
    return JobEntryHTTP.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( new String[] { "url", "targetFilename", "fileAppended", "dateTimeAdded",
      "targetFilenameExtension", "uploadFilename", "runForEveryRow", "urlFieldname", "uploadFieldname",
      "destinationFieldname", "username", "password", "proxyHostname", "proxyPort", "nonProxyHosts",
      "addFilenameToResult", "headerName", "headerValue" } );
  }

  @Override
  protected Map<String, FieldLoadSaveValidator<?>> createAttributeValidatorsMap() {
    Map<String, FieldLoadSaveValidator<?>> validators = new HashMap<String, FieldLoadSaveValidator<?>>();
    int entries = new Random().nextInt( 20 ) + 1;
    validators.put( "headerName", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), entries ) );
    validators.put( "headerValue", new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), entries ) );
    return validators;
  }

}
