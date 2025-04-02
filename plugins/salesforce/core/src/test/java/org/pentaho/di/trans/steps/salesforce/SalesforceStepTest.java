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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class SalesforceStepTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private StepMockHelper<SalesforceStepMeta, SalesforceStepData> smh;

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  @Before
  public void setUp() throws KettleException {
    smh =
      new StepMockHelper<SalesforceStepMeta, SalesforceStepData>( "Salesforce", SalesforceStepMeta.class,
          SalesforceStepData.class );
    when( smh.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      smh.logChannelInterface );
    when( smh.trans.isRunning() ).thenReturn( true );
  }

  @After
  public void cleanUp() {
    smh.cleanUp();
  }

  @Test
  public void testErrorHandling() {
    SalesforceStepMeta meta = mock( SalesforceStepMeta.class, Mockito.CALLS_REAL_METHODS );
    assertFalse( meta.supportsErrorHandling() );
  }

  @Test
  public void testInitDispose() {
    SalesforceStepMeta meta = mock( SalesforceStepMeta.class, Mockito.CALLS_REAL_METHODS );
    SalesforceStep step = spy( new MockSalesforceStep( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans ) );

    /*
     * Salesforce Step should fail if username and password are not set
     * We should not set a default account for all users
     */
    meta.setDefault();
    assertFalse( step.init( meta, smh.stepDataInterface ) );

    meta.setDefault();
    meta.setTargetURL( null );
    assertFalse( step.init( meta, smh.stepDataInterface ) );

    meta.setDefault();
    meta.setUsername( "anonymous" );
    assertFalse( step.init( meta, smh.stepDataInterface ) );

    meta.setDefault();
    meta.setUsername( "anonymous" );
    meta.setPassword( "myPwd" );
    meta.setModule( null );
    assertFalse( step.init( meta, smh.stepDataInterface ) );

    /*
     * After setting username and password, we should have enough defaults to properly init
     */
    meta.setDefault();
    meta.setUsername( "anonymous" );
    meta.setPassword( "myPwd" );
    assertTrue( step.init( meta, smh.stepDataInterface ) );

    // Dispose check
    assertNotNull( smh.stepDataInterface.connection );
    step.dispose( meta, smh.stepDataInterface );
    assertNull( smh.stepDataInterface.connection );
  }

  class MockSalesforceStep extends SalesforceStep {
    public MockSalesforceStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
        TransMeta transMeta, Trans trans ) {
      super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
    }
  }

  @Test
  public void createIntObjectTest() throws KettleValueException {
    SalesforceStep step =
      spy( new MockSalesforceStep( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans ) );
    ValueMetaInterface valueMeta = Mockito.mock( ValueMetaInterface.class );
    Mockito.when( valueMeta.getType() ).thenReturn( ValueMetaInterface.TYPE_INTEGER );
    Object value = step.normalizeValue( valueMeta, 100L );
    Assert.assertTrue( value instanceof Integer );
  }

  @Test
  public void createDateObjectTest() throws KettleValueException, ParseException {
    SalesforceStep step =
      spy( new MockSalesforceStep( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans ) );
    ValueMetaInterface valueMeta = Mockito.mock( ValueMetaInterface.class );
    DateFormat dateFormat = new SimpleDateFormat( "dd-MM-yyyy hh:mm:ss" );
    Date date = dateFormat.parse( "12-10-2017 15:10:25" );
    Mockito.when( valueMeta.isDate() ).thenReturn( true );
    Mockito.when( valueMeta.getDateFormatTimeZone() ).thenReturn( TimeZone.getTimeZone( "UTC" ) );
    Mockito.when( valueMeta.getDate( Mockito.eq( date ) ) ).thenReturn( date );
    Object value = step.normalizeValue( valueMeta, date );
    Assert.assertTrue( value instanceof Calendar );
    DateFormat minutesDateFormat = new SimpleDateFormat( "mm:ss" );
    //check not missing minutes and seconds
    Assert.assertEquals( minutesDateFormat.format( date ), minutesDateFormat.format( ( (Calendar) value ).getTime() ) );

  }

  @Test
  public void test_testButtonAction() {
    Map<String, String> queryParams = new HashMap<>();
    SalesforceStep step = spy( new MockSalesforceStep( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans ) );
    JSONObject response = step.testButtonAction( queryParams );
    assert ( response.containsKey( "connectionStatus" ) );
    assertNotNull( response );
  }

  @Test
  public void testModulesAction() {
    Map<String, String> queryParams = new HashMap<>();
    SalesforceStep step = spy( new MockSalesforceStep( smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans ) );
    JSONObject response = step.modulesAction(queryParams);
    assert(response.containsKey( "actionStatus" ));
    assertNotNull( response );
  }


  @Test
  public void test_testConnection() throws KettleException {
    SalesforceStep salesforceStep = spy(new MockSalesforceStep(smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans));
    SalesforceStepMeta stepMeta = mock(SalesforceStepMeta.class);
    SalesforceStepData data = mock(SalesforceStepData.class);
    salesforceStep.init( stepMeta, data );
    when(salesforceStep.getStepMetaInterface()).thenReturn( stepMeta );

    try (MockedConstruction<SalesforceConnection> mocked = Mockito.mockConstruction(SalesforceConnection.class, (mock, context) -> {
      doNothing().when(mock).connect();

    })) {
      boolean connection = salesforceStep.testConnection();
      assertTrue( connection );
    }
  }

  @Test
  public void test_testConnection_failure() throws KettleException {
    SalesforceStep salesforceStep = spy(new MockSalesforceStep(smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans));
    SalesforceStepMeta stepMeta = mock(SalesforceStepMeta.class);
    SalesforceStepData data = mock(SalesforceStepData.class);
    salesforceStep.init( stepMeta, data );
    boolean connection = salesforceStep.testConnection();
    assertFalse( connection );
  }


  @Test
  public void getModulesActionTest() throws KettleException {
    SalesforceStep salesforceStep = spy(new MockSalesforceStep(smh.stepMeta, smh.stepDataInterface, 0, smh.transMeta, smh.trans));
    SalesforceStepMeta stepMeta = mock(SalesforceStepMeta.class);
    SalesforceStepData data = mock(SalesforceStepData.class);
    salesforceStep.init( stepMeta, data );
    when(salesforceStep.getStepMetaInterface()).thenReturn( stepMeta );

    try (MockedConstruction<SalesforceConnection> mocked = Mockito.mockConstruction(SalesforceConnection.class, (mock, context) -> {
      doNothing().when(mock).connect();
      when(mock.getAllAvailableObjects(anyBoolean())).thenReturn(new String[]{"Account", "Contact"});
    })) {
      JSONObject response = salesforceStep.modulesAction( null );
      assert(response.containsKey( "actionStatus" ));
      assertEquals(  StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );

      Map<String, String> queryParams = new HashMap<>();
      queryParams.put( "moduleFlag", "true");
      response = salesforceStep.modulesAction( queryParams );
      assertNotNull(response);
      assert(response.containsKey( "modules" ));
    }
  }

  @Test
  public void testGetConnection() {

    SalesforceStepMeta salesforceStepMeta = spy( SalesforceStepMeta.class );

    when(smh.stepMeta.getStepMetaInterface()).thenReturn( salesforceStepMeta );

  }

}
