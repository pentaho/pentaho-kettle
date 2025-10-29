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

package org.pentaho.di.trans.steps.salesforce;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SalesforceStepHelperTest {

  SalesforceStepHelper salesforceStepHelper;
  SalesforceStepMeta salesforceStepMeta;
  TransMeta transMeta;

  @Before
  public void setUp() {
    salesforceStepMeta = mock( SalesforceStepMeta.class );
    salesforceStepHelper = new SalesforceStepHelper( salesforceStepMeta );
    transMeta = mock( TransMeta.class );
    KettleLogStore.init();
  }

  @Test
  public void test_testButtonAction() {
    JSONObject response = salesforceStepHelper.testButtonAction( transMeta );
    assertTrue( response.containsKey( "connectionStatus" ) );
  }

  @Test
  public void testModulesAction() {
    Map<String, String> queryParams = new HashMap<>();
    JSONObject response = salesforceStepHelper.modulesAction( transMeta, queryParams );
    assertTrue( response.containsKey( "actionStatus" ) );
  }


  @Test
  public void test_testConnection() {
    try ( MockedConstruction<SalesforceConnection> ignored = Mockito.mockConstruction( SalesforceConnection.class,
        ( mock, context ) -> {
          doNothing().when( mock ).connect();
        } ) ) {
      boolean connection = salesforceStepHelper.testConnection( transMeta );
      assertTrue( connection );
    }
  }

  @Test
  public void test_testConnection_failure() {
    boolean connection = salesforceStepHelper.testConnection( transMeta );
    assertFalse( connection );
  }


  @Test
  public void getModulesActionTest() {
    try ( MockedConstruction<SalesforceConnection> ignored = Mockito.mockConstruction( SalesforceConnection.class,
        ( mock, context ) -> {
          doNothing().when( mock ).connect();
          when( mock.getAllAvailableObjects( anyBoolean() ) ).thenReturn( new String[] { "Account", "Contact" } );
        } ) ) {
      JSONObject response = salesforceStepHelper.modulesAction( transMeta, null );
      assert ( response.containsKey( "actionStatus" ) );
      assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );

      Map<String, String> queryParams = new HashMap<>();
      queryParams.put( "moduleFlag", "true" );
      response = salesforceStepHelper.modulesAction( transMeta, queryParams );
      assertNotNull( response );
      assertTrue( response.containsKey( "modules" ) );
    }
  }
}
