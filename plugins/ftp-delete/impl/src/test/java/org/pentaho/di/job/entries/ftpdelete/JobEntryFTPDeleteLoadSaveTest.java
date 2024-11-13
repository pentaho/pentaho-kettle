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


package org.pentaho.di.job.entries.ftpdelete;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.pentaho.di.job.entries.ftpsget.FTPSConnection;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;

public class JobEntryFTPDeleteLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryFTPDelete> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryFTPDelete> getJobEntryClass() {
    return JobEntryFTPDelete.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( new String[] { "protocol", "serverName", "port", "userName", "password",
      "ftpDirectory", "wildcard", "timeout", "activeConnection", "useProxy", "proxyHost", "proxyPort",
      "proxyUsername", "proxyPassword", "usePublicKey", "keyFilename", "keyFilePass", "limitSuccess",
      "successCondition", "copyPrevious", "fTPSConnectionType", "socksProxyHost", "socksProxyPort",
      "socksProxyUsername", "socksProxyPassword" } );
  }

  @Override
  protected Map<String, FieldLoadSaveValidator<?>> createAttributeValidatorsMap() {
    Map<String, FieldLoadSaveValidator<?>> validators = new HashMap<String, FieldLoadSaveValidator<?>>();
    validators.put( "fTPSConnectionType", new IntLoadSaveValidator( FTPSConnection.connection_type_Code.length ) );

    return validators;
  }

}
