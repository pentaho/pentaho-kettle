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

package org.pentaho.di.trans.steps.salesforcedelete;


import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.util.Assert.assertTrue;

public class SalesforceDeleteTest {

  private StepMockHelper<SalesforceDeleteMeta, SalesforceDeleteData> smh;

  @Before
  public void setUp() {
    smh =
      new StepMockHelper<SalesforceDeleteMeta, SalesforceDeleteData>( "SalesforceDelete", SalesforceDeleteMeta.class,
        SalesforceDeleteData.class );
    when( smh.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      smh.logChannelInterface );
  }

  @After
  public void cleanUp() {
    smh.cleanUp();
  }

  @Test
  public void testModulesAction() {
    SalesforceDelete salesforceDelete =
      new SalesforceDelete( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    Map<String, String> queryParams = new HashMap<>();
    JSONObject response = salesforceDelete.modulesAction( queryParams );
    assertTrue( response.containsKey( "actionStatus" ) );
  }

  @Test
  public void test_testButtonAction() {
    SalesforceDelete salesforceDelete =
      new SalesforceDelete( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans );
    Map<String, String> queryParams = new HashMap<>();

    JSONObject response = salesforceDelete.testButtonAction( queryParams );
    assertTrue( response.containsKey( "connectionStatus" ) );
  }
}