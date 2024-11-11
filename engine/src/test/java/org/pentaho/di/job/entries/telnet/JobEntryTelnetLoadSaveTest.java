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


package org.pentaho.di.job.entries.telnet;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryTelnetLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryTelnet> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  @Override
  protected Class<JobEntryTelnet> getJobEntryClass() {
    return JobEntryTelnet.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( "hostname", "port", "timeout" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
        "hostname", "getHostname",
        "port", "getPort",
        "timeout", "getTimeOut" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
        "hostname", "setHostname",
        "port", "setPort",
        "timeout", "setTimeOut" );
  }
}
