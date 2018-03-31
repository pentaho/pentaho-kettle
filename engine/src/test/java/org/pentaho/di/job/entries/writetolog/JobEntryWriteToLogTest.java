/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.job.entries.writetolog;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.verification.VerificationMode;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.job.Job;

/**
 * @author Christopher Songer
 */
public class JobEntryWriteToLogTest {

  private Job parentJob;
  private JobEntryWriteToLog jobEntry;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleLogStore.init();
  }

  @Before
  public void setUp() throws Exception {
    parentJob = mock( Job.class );
    doReturn( false ).when( parentJob ).isStopped();

    jobEntry = new JobEntryWriteToLog();
    jobEntry = spy( jobEntry );
  }

  @Test
  public void errorMessageIsNotLoggedWhenParentJobLogLevelIsNothing() {
    verifyErrorMessageForParentJobLogLevel( LogLevel.NOTHING, never() );
  }

  @Test
  public void errorMessageIsLoggedWhenParentJobLogLevelIsError() {
    verifyErrorMessageForParentJobLogLevel( LogLevel.ERROR, times( 1 ) );
  }

  @Test
  public void errorMessageIsLoggedWhenParentJobLogLevelIsMinimal() {
    verifyErrorMessageForParentJobLogLevel( LogLevel.MINIMAL, times( 1 ) );
  }

  private void verifyErrorMessageForParentJobLogLevel( LogLevel parentJobLogLevel, VerificationMode mode ) {
    jobEntry.setLogMessage( "TEST" );
    jobEntry.setEntryLogLevel( LogLevel.ERROR );

    doReturn( parentJobLogLevel ).when( parentJob ).getLogLevel();
    jobEntry.setParentJob( parentJob );

    LogChannelInterface logChannel = spy( jobEntry.createLogChannel() );
    doReturn( logChannel ).when( jobEntry ).createLogChannel();

    jobEntry.evaluate( new Result() );
    verify( logChannel, mode ).logError( "TEST" + Const.CR );
  }
}
