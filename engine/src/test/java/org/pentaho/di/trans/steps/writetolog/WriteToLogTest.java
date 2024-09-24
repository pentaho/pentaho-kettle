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

package org.pentaho.di.trans.steps.writetolog;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.util.GenericStepData;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WriteToLogTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();
  private StepMockHelper<WriteToLogMeta, GenericStepData> stepMockHelper;
  private WriteToLog writeToLog;

  @BeforeClass
  public static void initEnvironment() throws KettleException {
    KettleEnvironment.init();
  }

  @Before
  public void setUp() {
    stepMockHelper = new StepMockHelper<>( "WriteToLog", WriteToLogMeta.class, GenericStepData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
      .thenReturn( stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
    writeToLog = new WriteToLog( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
      stepMockHelper.trans );
  }

  @After
  public void tearDown() throws Exception {
    stepMockHelper.cleanUp();
  }

  @Test
  public void processRow_NullRow() throws Exception {
    WriteToLog writeToLogSpy = spy( writeToLog );
    doReturn( null ).when( writeToLogSpy ).getRow();
    WriteToLogMeta meta = mock( WriteToLogMeta.class );
    WriteToLogData data = mock( WriteToLogData.class );

    assertFalse( writeToLogSpy.processRow( meta, data ) );

    verify( writeToLogSpy, times( 0 ) ).getInputRowMeta();
    verify( writeToLogSpy, times( 0 ) ).getLogLevel();
  }

  @Test
  public void processRow_StepLogLevelLowerThanRunLogLevel() throws Exception {
    WriteToLog writeToLogSpy = spy( writeToLog );
    Object[] rows = { new Object() };
    doReturn( rows ).when( writeToLogSpy ).getRow();
    WriteToLogMeta meta = mock( WriteToLogMeta.class );
    WriteToLogData data = mock( WriteToLogData.class );
    // Run log level
    data.loglevel = LogLevel.DEBUG;
    // Step log level (lower than Run's)
    doReturn( LogLevel.BASIC ).when( writeToLogSpy ).getLogLevel();

    // Let's pretend that it's not the first invocation
    writeToLogSpy.first = false;

    assertTrue( writeToLogSpy.processRow( meta, data ) );

    // It shouldn't have done any calculation
    verify( writeToLogSpy, times( 0 ) ).getRealLogMessage();
  }

  @Test
  public void processRow_StepLogLevelEqualToRunLogLevel() throws Exception {
    WriteToLog writeToLogSpy = spy( writeToLog );
    Object[] rows = { new Object() };
    doReturn( rows ).when( writeToLogSpy ).getRow();
    WriteToLogMeta meta = mock( WriteToLogMeta.class );
    WriteToLogData data = mock( WriteToLogData.class );
    data.fieldnr = 0;
    // Run log level
    data.loglevel = LogLevel.DEBUG;
    // Step log level (equal to Run's)
    doReturn( LogLevel.DEBUG ).when( writeToLogSpy ).getLogLevel();

    // Let's pretend that it's not the first invocation
    writeToLogSpy.first = false;

    assertTrue( writeToLogSpy.processRow( meta, data ) );

    verify( writeToLogSpy, times( 1 ) ).getRealLogMessage();
  }

  @Test
  public void processRow_StepLogLevelHigherThanRunLogLevel() throws Exception {
    WriteToLog writeToLogSpy = spy( writeToLog );
    Object[] rows = { new Object() };
    doReturn( rows ).when( writeToLogSpy ).getRow();
    WriteToLogMeta meta = mock( WriteToLogMeta.class );
    WriteToLogData data = mock( WriteToLogData.class );
    data.fieldnr = 0;
    // Run log level
    data.loglevel = LogLevel.DEBUG;
    // Step log level (higher than Run's)
    doReturn( LogLevel.ROWLEVEL ).when( writeToLogSpy ).getLogLevel();

    // Let's pretend that it's not the first invocation
    writeToLogSpy.first = false;

    assertTrue( writeToLogSpy.processRow( meta, data ) );

    verify( writeToLogSpy, times( 1 ) ).getRealLogMessage();
  }
}
