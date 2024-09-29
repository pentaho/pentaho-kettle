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


package org.pentaho.di.job.entries.ftpput;

import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntryFTPPUTLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryFTPPUT> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryFTPPUT> getJobEntryClass() {
    return JobEntryFTPPUT.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( "servername", "serverport", "username", "password", "remoteDirectory", "localDirectory",
      "wildcard", "binary", "timeout", "remove", "only_new", "active", "control_encoding", "proxy_host", "proxy_port",
      "proxy_username", "proxy_password", "socksproxy_host", "socksproxy_port", "socksproxy_username",
      "socksproxy_password" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
      "servername", "getServerName",
      "serverport", "getServerPort",
      "username", "getUserName",
      "password", "getPassword",
      "remoteDirectory", "getRemoteDirectory",
      "localDirectory", "getLocalDirectory",
      "wildcard", "getWildcard",
      "binary", "isBinaryMode",
      "timeout", "getTimeout",
      "remove", "getRemove",
      "only_new", "isOnlyPuttingNewFiles",
      "active", "isActiveConnection",
      "control_encoding", "getControlEncoding",
      "proxy_host", "getProxyHost",
      "proxy_port", "getProxyPort",
      "proxy_username", "getProxyUsername",
      "proxy_password", "getProxyPassword",
      "socksproxy_host", "getSocksProxyHost",
      "socksproxy_port", "getSocksProxyPort",
      "socksproxy_username", "getSocksProxyUsername",
      "socksproxy_password", "getSocksProxyPassword"
    );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
      "servername", "setServerName",
      "serverport", "setServerPort",
      "username", "setUserName",
      "password", "setPassword",
      "remoteDirectory", "setRemoteDirectory",
      "localDirectory", "setLocalDirectory",
      "wildcard", "setWildcard",
      "binary", "setBinaryMode",
      "timeout", "setTimeout",
      "remove", "setRemove",
      "only_new", "setOnlyPuttingNewFiles",
      "active", "setActiveConnection",
      "control_encoding", "setControlEncoding",
      "proxy_host", "setProxyHost",
      "proxy_port", "setProxyPort",
      "proxy_username", "setProxyUsername",
      "proxy_password", "setProxyPassword",
      "socksproxy_host", "setSocksProxyHost",
      "socksproxy_port", "setSocksProxyPort",
      "socksproxy_username", "setSocksProxyUsername",
      "socksproxy_password", "setSocksProxyPassword" );
  }
}
