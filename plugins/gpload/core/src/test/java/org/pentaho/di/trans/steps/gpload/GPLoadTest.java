/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2019-2024 Hitachi Vantara..  All rights reserved.
 */
package org.pentaho.di.trans.steps.gpload;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class GPLoadTest {
  private StepMockHelper<GPLoadMeta, GPLoadData> stepMockHelper;
  private GPLoad gpLoad;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() {
    stepMockHelper = new StepMockHelper<>( "TEST_GP_LOADER", GPLoadMeta.class, GPLoadData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    gpLoad = spy( new GPLoad( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0,
      stepMockHelper.transMeta, stepMockHelper.trans ) );
  }

  @After
  public void cleanUp() {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testNoDatabaseConnection() {
    assertFalse( gpLoad.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface ) );

    try {
      // Verify that the database connection being set to null throws a KettleException with the following message.
      gpLoad.verifyDatabaseConnection();
      // If the method does not throw a Kettle Exception, then the DB was set and not null for this test. Fail it.
      fail( "Database Connection is not null, this fails the test." );
    } catch ( KettleException aKettleException ) {
      assertThat( aKettleException.getMessage(), containsString( "There is no connection defined in this step." ) );
    }
  }
}
