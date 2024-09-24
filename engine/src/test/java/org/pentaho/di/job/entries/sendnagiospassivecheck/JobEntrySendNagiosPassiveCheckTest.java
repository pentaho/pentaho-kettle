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
package org.pentaho.di.job.entries.sendnagiospassivecheck;

import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntrySendNagiosPassiveCheckTest extends JobEntryLoadSaveTestSupport<JobEntrySendNagiosPassiveCheck> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntrySendNagiosPassiveCheck> getJobEntryClass() {
    return JobEntrySendNagiosPassiveCheck.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList(
        "port",
        "serverName",
        "password",
        "responseTimeOut",
        "connectionTimeOut",
        "senderServerName",
        "senderServiceName",
        "message" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
        "port", "getPort",
        "serverName", "getServerName",
        "password", "getPassword",
        "responseTimeOut", "getResponseTimeOut",
        "connectionTimeOut", "getConnectionTimeOut",
        "senderServerName", "getSenderServerName",
        "senderServiceName", "getSenderServiceName",
        "message", "getMessage" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
        "port", "setPort",
        "serverName", "setServerName",
        "password", "setPassword",
        "responseTimeOut", "setResponseTimeOut",
        "connectionTimeOut", "setConnectionTimeOut",
        "senderServerName", "setSenderServerName",
        "senderServiceName", "setSenderServiceName",
        "message", "setMessage" );
  }

}
