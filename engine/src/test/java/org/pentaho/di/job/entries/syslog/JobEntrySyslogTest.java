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
package org.pentaho.di.job.entries.syslog;

import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntrySyslogTest extends JobEntryLoadSaveTestSupport<JobEntrySyslog> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntrySyslog> getJobEntryClass() {
    return JobEntrySyslog.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList(
        "serverName",
        "port",
        "message",
        "facility",
        "priority",
        "datePattern",
        "addTimestamp",
        "addHostname" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
        "serverName", "getServerName",
        "port", "getPort",
        "message", "getMessage",
        "facility", "getFacility",
        "priority", "getPriority",
        "datePattern", "getDatePattern",
        "addTimestamp", "isAddTimestamp",
        "addHostname", "isAddHostName" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
        "serverName", "setServerName",
        "port", "setPort",
        "message", "setMessage",
        "facility", "setFacility",
        "priority", "setPriority",
        "datePattern", "setDatePattern",
        "addTimestamp", "addTimestamp",
        "addHostname", "addHostName" );
  }

}
