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

package org.pentaho.di.trans.steps.elasticsearchbulk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.common.transport.TransportAddress;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.ListLoadSaveValidator;
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
    getterMap.put( "fields", "getFieldsMap" );
    getterMap.put( "servers", "getServers" );
    getterMap.put( "settings", "getSettingsMap" );

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
    setterMap.put( "fields", "setFieldsMap" );
    setterMap.put( "servers", "setServers" );
    setterMap.put( "settings", "setSettingsMap" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorAttributeMap =
        new HashMap<String, FieldLoadSaveValidator<?>>();
    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidatorTypeMap =
        new HashMap<String, FieldLoadSaveValidator<?>>();

    fieldLoadSaveValidatorAttributeMap.put( "fields", new MapLoadSaveValidator<String, String>(
        new StringLoadSaveValidator(), new StringLoadSaveValidator() ) );
    fieldLoadSaveValidatorAttributeMap.put( "settings", new MapLoadSaveValidator<String, String>(
        new StringLoadSaveValidator(), new StringLoadSaveValidator() ) );
    fieldLoadSaveValidatorAttributeMap.put( "servers", new ListLoadSaveValidator<ElasticSearchBulkMeta.Server>(
        new FieldLoadSaveValidator<ElasticSearchBulkMeta.Server>() {
          Random rand = new Random();

          @Override
          public ElasticSearchBulkMeta.Server getTestObject() {
            ElasticSearchBulkMeta.Server r = new ElasticSearchBulkMeta.Server();
            r.address = rand.nextLong() + "";
            r.port = rand.nextInt();
            return r;
          }

          @Override
          public boolean validateTestObject( ElasticSearchBulkMeta.Server testObject, Object actual ) {
            ElasticSearchBulkMeta.Server ac = (ElasticSearchBulkMeta.Server) actual;
            return ac.address.equals( testObject.address ) && ac.port == testObject.port;
          }
        } ) );
    fieldLoadSaveValidatorTypeMap.put( TimeUnit.class.getCanonicalName(), new TimeUnitFieldLoadSaveValidator() );

    LoadSaveTester loadSaveTester =
        new LoadSaveTester( ElasticSearchBulkMeta.class, attributes, getterMap, setterMap,
            fieldLoadSaveValidatorAttributeMap, fieldLoadSaveValidatorTypeMap );

    loadSaveTester.testRepoRoundTrip();
    loadSaveTester.testXmlRoundTrip();
  }

  @Test
  public void testGetBatchSizeInt() {
    ElasticSearchBulkMeta esbm = new ElasticSearchBulkMeta();
    int batchSize = esbm.getBatchSizeInt( new VariableSpaceImpl() );
    assertEquals( batchSize, ElasticSearchBulkMeta.DEFAULT_BATCH_SIZE );
  }

  @Test
  public void testClone() {
    ElasticSearchBulkMeta esbm = new ElasticSearchBulkMeta();
    ElasticSearchBulkMeta esbmClone = (ElasticSearchBulkMeta) esbm.clone();
    assertNotNull( esbmClone );
  }

  @Test
  public void testDefault() {
    ElasticSearchBulkMeta esbm = new ElasticSearchBulkMeta();
    esbm.setDefault();
    assertTrue( esbm.getBatchSize().equalsIgnoreCase( "" + ElasticSearchBulkMeta.DEFAULT_BATCH_SIZE ) );
    assertEquals( esbm.getIndex(), "twitter" );
  }

  @Test
  public void testSupportsErrorHandling() {
    ElasticSearchBulkMeta esbm = new ElasticSearchBulkMeta();
    boolean supportsError = esbm.supportsErrorHandling();
    assertTrue( supportsError );
  }

  @Test
  public void testGetStepData() {
    ElasticSearchBulkMeta esbm = new ElasticSearchBulkMeta();
    StepDataInterface sdi = esbm.getStepData();
    assertTrue( sdi instanceof ElasticSearchBulkData );
  }

  public class VariableSpaceImpl implements VariableSpace {
    @Override
    public String environmentSubstitute( String aString ) {
      return Integer.toString( ElasticSearchBulkMeta.DEFAULT_BATCH_SIZE );
    }

    @Override
    public String fieldSubstitute( String aString, RowMetaInterface rowMeta, Object[] rowData )
      throws KettleValueException {
      return null;
    }

    @Override
    public void injectVariables( Map<String, String> prop ) {
    }

    @Override
    public String[] environmentSubstitute( String[] string ) {
      return null;
    }

    @Override
    public boolean getBooleanValueOfVariable( String variableName, boolean defaultValue ) {
      return false;
    }

    @Override
    public String getVariable( String variableName, String defaultValue ) {
      return null;
    }

    @Override
    public String getVariable( String variableName ) {
      return null;
    }

    @Override
    public void setVariable( String variableName, String variableValue ) {
    }

    @Override
    public void setParentVariableSpace( VariableSpace parent ) {
    }

    @Override
    public VariableSpace getParentVariableSpace() {
      return null;
    }

    @Override
    public void shareVariablesWith( VariableSpace space ) {
    }

    @Override
    public void copyVariablesFrom( VariableSpace space ) {
    }

    @Override
    public void initializeVariablesFrom( VariableSpace parent ) {
    }

    @Override
    public String[] listVariables() {
      return null;
    }
  }

  public class InetSocketTransportAddressFieldLoadSaveValidator implements
      FieldLoadSaveValidator<TransportAddress> {
    @Override
    public TransportAddress getTestObject() {
      byte[] randomIP;
      // Test IPv4 and IPv6 addresses
      if ( new Random().nextBoolean() ) {
        randomIP = new byte[4];
      } else {
        randomIP = new byte[16];
      }
      new Random().nextBytes( randomIP );
      try {
        return new TransportAddress( InetAddress.getByAddress( randomIP ), new Random().nextInt( 65536 ) );
      } catch ( UnknownHostException e ) {
        return new TransportAddress( InetAddress.getLoopbackAddress(), new Random().nextInt( 65536 ) );
      }
    }

    @Override
    public boolean validateTestObject( TransportAddress testObject, Object actual ) {
      return testObject.equals( (TransportAddress) actual );
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
