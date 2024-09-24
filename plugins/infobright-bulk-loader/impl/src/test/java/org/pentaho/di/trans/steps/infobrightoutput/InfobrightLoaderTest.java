/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.infobrightoutput;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * User: dgriffen Date: 12/10/2018
 */
public class InfobrightLoaderTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  private StepMockHelper<InfobrightLoaderMeta, InfobrightLoaderData> stepMockHelper;
  private InfobrightLoader infobrightLoader;

  @BeforeClass
  public static void initEnvironment() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() {
    stepMockHelper = new StepMockHelper<>( "InfobrightLoader", InfobrightLoaderMeta.class,
      InfobrightLoaderData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
      .thenReturn( stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    infobrightLoader = new InfobrightLoader( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0,
      stepMockHelper.transMeta, stepMockHelper.trans );
  }

  @After
  public void tearDown() throws Exception {
    stepMockHelper.cleanUp();
  }

  /**
   * [PDI-17481] Testing the ability that if no connection is specified, we will mark it as a fail and log the
   * appropriate reason to the user by throwing a KettleException.
   */
  @Test
  public void testNoDatabaseConnection() {
    try {
      doReturn( null ).when( stepMockHelper.initStepMetaInterface ).getDatabaseMeta();
      assertFalse( infobrightLoader.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface ) );
      // Verify that the database connection being set to null throws a KettleException with the following message.
      infobrightLoader.verifyDatabaseConnection();
      // If the method does not throw a Kettle Exception, then the DB was set and not null for this test. Fail it.
      fail( "Database Connection is not null, this fails the test." );
    } catch ( KettleException aKettleException ) {
      assertThat( aKettleException.getMessage(), containsString( "There is no connection defined in this step" ) );
    }
  }
}
