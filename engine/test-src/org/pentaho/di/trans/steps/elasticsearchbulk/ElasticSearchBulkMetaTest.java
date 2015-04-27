/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.elasticsearchbulk;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.ArrayLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.MapLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.StringLoadSaveValidator;

public class ElasticSearchBulkMetaTest {
  @Test
  public void testRoundTrip() throws KettleException {
    List<String> attributes =
      Arrays.asList( "index", "type", "batchSize", "timeout", "timeoutUnit", "isJson", "jsonField", "idOutputField",
        "idField", "overwriteIfExists", "useOutput", "stopOnError", "fields", "servers", "settings" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "index", "getIndex" );
    getterMap.put( "type", "getType" );
    getterMap.put( "batchSize", "getBatchSize" );
    getterMap.put( "timeout", "getTimeOut" );
    getterMap.put( "timeoutUnit", "getTimeoutUnit" );
    getterMap.put( "isJson", "isJsonInsert" );
    getterMap.put( "jsonField", "getJsonField" );
    getterMap.put( "idOutputField", "getIdOutField" );
    getterMap.put( "idField", "getIdInField" );
    getterMap.put( "overwriteIfExists", "isOverWriteIfSameId" );
    getterMap.put( "useOutput", "isUseOutput" );
    getterMap.put( "stopOnError", "isStopOnError" );
    getterMap.put( "fields", "getFields" );
    getterMap.put( "servers", "getServers" );
    getterMap.put( "settings", "getSettings" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "index", "setIndex" );
    setterMap.put( "type", "setType" );
    setterMap.put( "batchSize", "setBatchSize" );
    setterMap.put( "timeout", "setTimeOut" );
    setterMap.put( "timeoutUnit", "setTimeoutUnit" );
    setterMap.put( "isJson", "setJsonInsert" );
    setterMap.put( "jsonField", "setJsonField" );
    setterMap.put( "idOutputField", "setIdOutField" );
    setterMap.put( "idField", "setIdInField" );
    setterMap.put( "overwriteIfExists", "setOverWriteIfSameId" );
    setterMap.put( "useOutput", "setUseOutput" );
    setterMap.put( "stopOnError", "setStopOnError" );
    setterMap.put( "fields", "setFields" );
    setterMap.put( "servers", "setServers" );
    setterMap.put( "settings", "setSettings" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap =
      new HashMap<String, FieldLoadSaveValidator<?>>();
    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorTypeMap =
      new HashMap<String, FieldLoadSaveValidator<?>>();

    fieldLoadSaveValidatorAttributeMap.put( "fields",
      new MapLoadSaveValidator<String, String>( new StringLoadSaveValidator(), new StringLoadSaveValidator() ) );
    fieldLoadSaveValidatorAttributeMap.put( "settings",
      new MapLoadSaveValidator<String, String>( new StringLoadSaveValidator(), new StringLoadSaveValidator() ) );

    fieldLoadSaveValidatorTypeMap.put( InetSocketTransportAddress[].class.getCanonicalName(),
      new ArrayLoadSaveValidator<InetSocketTransportAddress>( new InetSocketTransportAddressFieldLoadSaveValidator() ) );
    fieldLoadSaveValidatorTypeMap.put( TimeUnit.class.getCanonicalName(), new TimeUnitFieldLoadSaveValidator() );

    LoadSaveTester loadSaveTester = new LoadSaveTester( ElasticSearchBulkMeta.class, attributes, getterMap, setterMap,
      fieldLoadSaveValidatorAttributeMap, fieldLoadSaveValidatorTypeMap );

    loadSaveTester.testRepoRoundTrip();
    loadSaveTester.testXmlRoundTrip();
  }

  public class InetSocketTransportAddressFieldLoadSaveValidator implements
      FieldLoadSaveValidator<InetSocketTransportAddress> {
    @Override
    public InetSocketTransportAddress getTestObject() {
      byte[] randomIP;
      // Test IPv4 and IPv6 addresses
      if ( new Random().nextBoolean() ) {
        randomIP = new byte[4];
      } else {
        randomIP = new byte[16];
      }
      new Random().nextBytes( randomIP );
      try {
        return new InetSocketTransportAddress( InetAddress.getByAddress( randomIP ), new Random().nextInt( 65536 ) );
      } catch ( UnknownHostException e ) {
        return new InetSocketTransportAddress( InetAddress.getLoopbackAddress(), new Random().nextInt( 65536 ) );
      }
    }

    @Override
    public boolean validateTestObject( InetSocketTransportAddress testObject, Object actual ) {
      return testObject.equals( (InetSocketTransportAddress) actual );
    }
  }

  public class TimeUnitFieldLoadSaveValidator implements FieldLoadSaveValidator<TimeUnit> {
    @Override
    public TimeUnit getTestObject() {
      return TimeUnit.values()[new Random().nextInt( TimeUnit.values().length )];
    }

    @Override
    public boolean validateTestObject( TimeUnit testObject, Object actual ) {
      return testObject.equals( (TimeUnit) actual );
    }
  }
}
