/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
