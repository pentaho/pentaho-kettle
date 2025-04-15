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
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.attributes.metastore.EmbeddedMetaStore;
import org.pentaho.di.core.runconfiguration.impl.extension.RunConfigurationInjectExtensionPoint;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.runconfiguration.api.RunConfiguration;
import org.pentaho.di.core.runconfiguration.impl.RunConfigurationManager;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobExecutionExtension;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.trans.TransExecutionConfiguration;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bmorrise on 5/4/17.
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class RunConfigurationInjectExtensionPointTest {

  RunConfigurationInjectExtensionPoint runConfigurationInjectExtensionPoint;

  private final String runConfName = "RUN_CONF";

  @Mock private RunConfigurationManager runConfigurationManager;
  @Mock private TransExecutionConfiguration transExecutionConfiguration;
  @Mock private AbstractMeta abstractMeta;
  @Mock private LogChannelInterface log;
  @Mock private EmbeddedMetaStore embeddedMetaStore;
  @Mock private RunConfiguration runConfiguration;

  @Mock private JobExecutionExtension executionExt;
  @Mock private JobEntryCopy jobEntryCopy;
  @Mock private Result result;
  @Mock private Job job;
  @Mock private JobMeta jobMeta;

  private Map<JobEntryCopy, JobEntryJob> jobs = new HashMap<>();
  private Map<JobEntryCopy, JobEntryTrans> trans = new HashMap<>();

  @Mock private JobEntryTrans jobEntryTrans;
  @Mock private JobEntryJob jobEntryJobs;

  @Before
  public void setup() {
    runConfigurationInjectExtensionPoint = new RunConfigurationInjectExtensionPoint();
    runConfigurationInjectExtensionPoint.setRunConfigurationManager( runConfigurationManager );
    executionExt = new JobExecutionExtension( job, result, jobEntryCopy, false );

    when( runConfigurationManager.load( runConfName ) ).thenReturn( runConfiguration );

    when( job.getJobMeta() ).thenReturn( jobMeta );
    when( jobMeta.getEmbeddedMetaStore() ).thenReturn( embeddedMetaStore );
    when( jobMeta.environmentSubstitute( anyString() ) ).thenReturn( runConfName );

    when( jobEntryJobs.getRunConfiguration() ).thenReturn( runConfName );
    when( jobEntryTrans.getRunConfiguration() ).thenReturn( runConfName );

    trans.put( jobEntryCopy, jobEntryTrans );
    jobs.put( jobEntryCopy, jobEntryJobs );

    when( job.getActiveJobEntryTransformations() ).thenReturn( trans );
    when( job.getActiveJobEntryJobs() ).thenReturn( jobs );
  }

  @Test
  public void testCallExtensionPoint() throws Exception {
    runConfigurationInjectExtensionPoint.callExtensionPoint( log, executionExt );

    verify( runConfigurationManager, times( 2 ) ).load( eq( runConfName ) );
  }

}
