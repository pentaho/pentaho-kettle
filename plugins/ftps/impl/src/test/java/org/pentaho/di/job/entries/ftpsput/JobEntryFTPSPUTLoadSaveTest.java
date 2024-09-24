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

package org.pentaho.di.job.entries.ftpsput;

import org.junit.ClassRule;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.job.entries.ftpsget.FTPSConnection;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class JobEntryFTPSPUTLoadSaveTest extends JobEntryLoadSaveTestSupport<JobEntryFTPSPUT> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Override
  protected Class<JobEntryFTPSPUT> getJobEntryClass() {
    return JobEntryFTPSPUT.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList( "servername", "serverport", "username", "password", "remoteDirectory", "localDirectory",
      "wildcard", "binary", "timeout", "remove", "only_new", "active", "proxy_host", "proxy_port",
      "proxy_username", "proxy_password", "connection_type" );
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
      "proxy_host", "getProxyHost",
      "proxy_port", "getProxyPort",
      "proxy_username", "getProxyUsername",
      "proxy_password", "getProxyPassword",
      "connection_type", "getConnectionType" );
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
      "proxy_host", "setProxyHost",
      "proxy_port", "setProxyPort",
      "proxy_username", "setProxyUsername",
      "proxy_password", "setProxyPassword",
      "connection_type", "setConnectionType" );
  }

  @Override
  protected Map<String, FieldLoadSaveValidator<?>> createAttributeValidatorsMap() {
    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidator = new HashMap<String, FieldLoadSaveValidator<?>>();
    fieldLoadSaveValidator.put( "connection_type", new FTPSConnectionLoadSaveValidator() );
    return fieldLoadSaveValidator;
  }

  public class FTPSConnectionLoadSaveValidator implements FieldLoadSaveValidator<Integer> {
    @Override
    public Integer getTestObject() {
      return new Random().nextInt( FTPSConnection.connection_type_Code.length );
    }

    @Override
    public boolean validateTestObject( Integer original, Object actual ) {
      return original.equals( actual );
    }
  }
}
