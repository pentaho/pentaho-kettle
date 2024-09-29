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

package org.pentaho.di.trans.steps.ldapoutput;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.BooleanLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class LDAPOutputMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  LoadSaveTester loadSaveTester;

  @Before
  public void setUp() throws Exception {
    List<String> attributes =
        Arrays.asList( "updateLookup", "updateStream", "update", "useAuthentication", "Host", "userName", "password",
            "port", "dnFieldName", "failIfNotExist", "searchBase", "multiValuedSeparator", "operationType", "oldDnFieldName",
            "newDnFieldName", "deleteRDN" );

    Map<String, String> getterMap = new HashMap<String, String>() {
      {
        put( "updateLookup", "getUpdateLookup" );
        put( "updateStream", "getUpdateStream" );
        put( "update", "getUpdate" );
        put( "useAuthentication", "getUseAuthentication" );
        put( "Host", "getHost" );
        put( "userName", "getUserName" );
        put( "password", "getPassword" );
        put( "port", "getPort" );
        put( "dnFieldName", "getDnField" );
        put( "failIfNotExist", "isFailIfNotExist" );
        put( "searchBase", "getSearchBaseDN" );
        put( "multiValuedSeparator", "getMultiValuedSeparator" );
        put( "operationType", "getOperationType" );
        put( "oldDnFieldName", "getOldDnFieldName" );
        put( "newDnFieldName", "getNewDnFieldName" );
        put( "deleteRDN", "isDeleteRDN" );
      }
    };

    Map<String, String> setterMap = new HashMap<String, String>() {
      {
        put( "updateLookup", "setUpdateLookup" );
        put( "updateStream", "setUpdateStream" );
        put( "update", "setUpdate" );
        put( "useAuthentication", "setUseAuthentication" );
        put( "Host", "setHost" );
        put( "userName", "setUserName" );
        put( "password", "setPassword" );
        put( "port", "setPort" );
        put( "dnFieldName", "setDnField" );
        put( "failIfNotExist", "setFailIfNotExist" );
        put( "searchBase", "setSearchBaseDN" );
        put( "multiValuedSeparator", "setMultiValuedSeparator" );
        put( "operationType", "setOperationType" );
        put( "oldDnFieldName", "setOldDnFieldName" );
        put( "newDnFieldName", "setNewDnFieldName" );
        put( "deleteRDN", "setDeleteRDN" );
      }
    };
    FieldLoadSaveValidator<String[]> stringArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<String>( new StringLoadSaveValidator(), 3 );
    FieldLoadSaveValidator<Boolean[]> booleanArrayLoadSaveValidator =
        new ArrayLoadSaveValidator<Boolean>( new BooleanLoadSaveValidator(), 3 );
    Map<String, FieldLoadSaveValidator<?>> attrValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();
    attrValidatorMap.put( "update", booleanArrayLoadSaveValidator );
    attrValidatorMap.put( "updateLookup", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "updateStream", stringArrayLoadSaveValidator );
    attrValidatorMap.put( "operationType", new IntLoadSaveValidator( 5 ) );

    Map<String, FieldLoadSaveValidator<?>> typeValidatorMap = new HashMap<String, FieldLoadSaveValidator<?>>();

    loadSaveTester = new LoadSaveTester( LDAPOutputMeta.class, attributes, getterMap, setterMap, attrValidatorMap, typeValidatorMap );

    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    String passwordEncoderPluginID =
        Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );
    Encr.init( passwordEncoderPluginID );
  }

  @Test
  public void testSerialization() throws KettleException {
    loadSaveTester.testSerialization();
  }

}
