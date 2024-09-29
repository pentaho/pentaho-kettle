/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.salesforce;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
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
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

}
