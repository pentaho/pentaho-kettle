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

package org.pentaho.di.job.entries.snmptrap;

import java.util.Arrays;

import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.job.entry.loadSave.JobEntryLoadSaveTestSupport;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

public class JobEntrySNMPTrapTest extends JobEntryLoadSaveTestSupport<JobEntrySNMPTrap> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void beforeClass() throws KettleException {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    String passwordEncoderPluginID =
        Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );
    Encr.init( passwordEncoderPluginID );
  }

  @Override
  protected Class<JobEntrySNMPTrap> getJobEntryClass() {
    return JobEntrySNMPTrap.class;
  }

  @Override
  protected List<String> listCommonAttributes() {
    return Arrays.asList(
        "serverName",
        "port",
        "timeout",
        "nrretry",
        "comString",
        "message",
        "oid",
        "targettype",
        "user",
        "passphrase",
        "engineid" );
  }

  @Override
  protected Map<String, String> createGettersMap() {
    return toMap(
        "serverName", "getServerName",
        "port", "getPort",
        "timeout", "getTimeout",
        "nrretry", "getRetry",
        "comString", "getComString",
        "message",  "getMessage",
        "oid", "getOID",
        "targettype", "getTargetType",
        "user", "getUser",
        "passphrase", "getPassPhrase",
        "engineid", "getEngineID" );
  }

  @Override
  protected Map<String, String> createSettersMap() {
    return toMap(
        "serverName", "setServerName",
        "port", "setPort",
        "timeout", "setTimeout",
        "nrretry", "setRetry",
        "comString", "setComString",
        "message",  "setMessage",
        "oid", "setOID",
        "targettype", "setTargetType",
        "user", "setUser",
        "passphrase", "setPassPhrase",
        "engineid", "setEngineID" );
  }

}
