/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
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
