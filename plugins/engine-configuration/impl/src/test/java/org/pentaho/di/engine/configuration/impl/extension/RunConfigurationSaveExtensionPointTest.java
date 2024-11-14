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


package org.pentaho.di.engine.configuration.impl.extension;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.attributes.metastore.EmbeddedMetaStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.engine.configuration.api.RunConfiguration;
import org.pentaho.di.engine.configuration.impl.RunConfigurationManager;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.job.entry.JobEntryRunConfigurableInterface;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class RunConfigurationSaveExtensionPointTest {

  private static final int JOB_ENTRY_COUNT = 10;
  private static final String RUN_CONFIGURATION = "Run Configuration ";
  public static final String RUN_CONFIG_VARIBLE = "${RUN_CONFIG_VARIBLE}";

  @Mock private JobMeta jobMeta;
  @Mock private LogChannelInterface log;
  @Mock private EmbeddedMetaStore embeddedMetaStore;
  @Mock private RunConfigurationManager runConfigurationManager;

  @Before
  public void setup() {
    when( jobMeta.getEmbeddedMetaStore() ).thenReturn( embeddedMetaStore );
    for ( int i = 0; i < JOB_ENTRY_COUNT; i++ ) {
      String configurationName = RUN_CONFIGURATION + i;
      RunConfiguration runConfiguration = mock( RunConfiguration.class );
      lenient().when( runConfiguration.getName() ).thenReturn( configurationName );
      lenient().when( runConfigurationManager.load( configurationName ) ).thenReturn( runConfiguration );
    }
  }

  @Test
  public void testCallExtensionPointMultipleConfigs() throws Exception {
    List<JobEntryCopy> jobEntryCopies = new ArrayList<>();
    for ( int i = 0; i < JOB_ENTRY_COUNT; i++ ) {
      Class<? extends JobEntryRunConfigurableInterface> type =
        i < JOB_ENTRY_COUNT / 2 ? JobEntryJob.class : JobEntryTrans.class;
      String configurationName = RUN_CONFIGURATION + i;
      jobEntryCopies.add( createCopy( type, configurationName ) );
    }
    when( jobMeta.getJobCopies() ).thenReturn( jobEntryCopies );

    RunConfigurationSaveExtensionPoint runConfigurationSaveExtensionPoint =
      new RunConfigurationSaveExtensionPoint();
    runConfigurationSaveExtensionPoint.setRunConfigurationManager( runConfigurationManager );
    runConfigurationSaveExtensionPoint.callExtensionPoint( log, new Object[] { jobMeta } );

    for ( int i = 0; i < JOB_ENTRY_COUNT; i++ ) {
      String configurationName = RUN_CONFIGURATION + i;
      verify( runConfigurationManager ).load( configurationName );
    }
  }

  @Test
  public void testCallExtensionPointOneConfig() throws Exception {
    List<JobEntryCopy> jobEntryCopies = new ArrayList<>();
    for ( int i = 0; i < JOB_ENTRY_COUNT; i++ ) {
      Class<? extends JobEntryRunConfigurableInterface> type =
        i < JOB_ENTRY_COUNT / 2 ? JobEntryJob.class : JobEntryTrans.class;
      jobEntryCopies.add( createCopy( type, RUN_CONFIGURATION + "0" ) );
    }
    when( jobMeta.getJobCopies() ).thenReturn( jobEntryCopies );

    RunConfigurationSaveExtensionPoint runConfigurationSaveExtensionPoint =
      new RunConfigurationSaveExtensionPoint();
    runConfigurationSaveExtensionPoint.setRunConfigurationManager( runConfigurationManager );
    runConfigurationSaveExtensionPoint.callExtensionPoint( log, new Object[] { jobMeta } );

    verify( runConfigurationManager, times( 1 ) ).load( anyString() );
  }

  @Test
  public void testCallExtensionPointAllConfigs() throws Exception {
    List<JobEntryCopy> jobEntryCopies = new ArrayList<>();
    jobEntryCopies.add( createCopy( JobEntryJob.class, RUN_CONFIGURATION + "0" ) );
    jobEntryCopies.add( createCopy( JobEntryTrans.class, RUN_CONFIGURATION + "1" ) );
    jobEntryCopies.add( createCopy( JobEntryJob.class, RUN_CONFIGURATION + "2" ) );
    jobEntryCopies.add( createCopy( JobEntryTrans.class, RUN_CONFIG_VARIBLE ) );
    when( jobMeta.getJobCopies() ).thenReturn( jobEntryCopies );

    RunConfigurationSaveExtensionPoint runConfigurationSaveExtensionPoint =
      new RunConfigurationSaveExtensionPoint();
    runConfigurationSaveExtensionPoint.setRunConfigurationManager( runConfigurationManager );
    runConfigurationSaveExtensionPoint.callExtensionPoint( log, new Object[] { jobMeta } );

    verify( runConfigurationManager ).load();
  }

  @Test
  public void testCallExtensionPointWithNullConfig() throws  Exception {
    List<JobEntryCopy> jobEntryCopies = new ArrayList<>();
    jobEntryCopies.add( createCopy( JobEntryJob.class, RUN_CONFIGURATION + "0" ) );
    jobEntryCopies.add( createCopy( JobEntryTrans.class, RUN_CONFIGURATION + "1" ) );
    jobEntryCopies.add( createCopy( JobEntryJob.class, RUN_CONFIGURATION + "2" ) );
    jobEntryCopies.add( createCopy( JobEntryTrans.class, RUN_CONFIG_VARIBLE ) );
    jobEntryCopies.add( createCopy( JobEntryTrans.class, null ) );
    jobEntryCopies.add( createCopy( JobEntryTrans.class, "" ) );
    jobEntryCopies.add( createCopy( JobEntryJob.class, null ) );
    jobEntryCopies.add( createCopy( JobEntryJob.class, "" ) );
    when( jobMeta.getJobCopies() ).thenReturn( jobEntryCopies );

    RunConfigurationSaveExtensionPoint runConfigurationSaveExtensionPoint =
      new RunConfigurationSaveExtensionPoint();
    runConfigurationSaveExtensionPoint.setRunConfigurationManager( runConfigurationManager );
    runConfigurationSaveExtensionPoint.callExtensionPoint( log, new Object[] { jobMeta } );

    verify( runConfigurationManager ).load();
  }

  private JobEntryCopy createCopy( Class<? extends JobEntryRunConfigurableInterface> type,
                                   String runConfigurationName ) {
    JobEntryCopy jobEntryCopy = mock( JobEntryCopy.class );
    JobEntryRunConfigurableInterface jobEntryInterface = mock( type );

    when( jobEntryCopy.getEntry() ).thenReturn( (JobEntryInterface) jobEntryInterface );
    when( jobEntryInterface.getRunConfiguration() ).thenReturn( runConfigurationName );

    return jobEntryCopy;
  }
}
