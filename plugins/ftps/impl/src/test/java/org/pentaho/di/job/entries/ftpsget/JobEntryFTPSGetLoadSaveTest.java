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


package org.pentaho.di.job.entries.ftpsget;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JobEntryFTPSGetLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryFTPSGet> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryFTPSGet> getJobEntryClass() {
    return JobEntryFTPSGet.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( new String[] { "port", "serverName", "userName", "password", "FTPSDirectory",
      "targetDirectory", "wildcard", "binaryMode", "timeout", "remove", "onlyGettingNewFiles",
      "activeConnection", "moveFiles", "moveToDirectory", "dateInFilename", "timeInFilename",
      "specifyFormat", "dateTimeFormat", "addDateBeforeExtension", "addToResult", "createMoveFolder",
      "proxy_host", "proxy_port", "proxy_username", "proxy_password", "ifFileExists", "limit",
      "success_condition", "connection_type" } );
  }

  @Override
  protected Map<String, FieldLoadSaveValidator<?>> createAttributeValidatorsMap() {
    Map<String, FieldLoadSaveValidator<?>> validators = new HashMap<String, FieldLoadSaveValidator<?>>();
    validators.put( "connection_type", new IntLoadSaveValidator( FTPSConnection.connection_type_Code.length ) );
    validators.put( "ifFileExists", new IntLoadSaveValidator( JobEntryFTPSGet.FILE_EXISTS_ACTIONS.length ) );

    return validators;
  }

}
