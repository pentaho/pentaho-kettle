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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.attributes.metastore.EmbeddedMetaStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.engine.configuration.impl.RunConfigurationManager;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by bmorrise on 5/15/17.
 */
@RunWith( MockitoJUnitRunner.StrictStubs.class )
public class RunConfigurationImportExtensionPointTest {

  private RunConfigurationImportExtensionPoint runConfigurationImportExtensionPoint;

  @Mock private RunConfigurationManager runConfigurationManager;
  @Mock private AbstractMeta abstractMeta;
  @Mock private LogChannelInterface log;
  @Mock private EmbeddedMetaStore embeddedMetaStore;

  @Before
  public void setup() {
    runConfigurationImportExtensionPoint = new RunConfigurationImportExtensionPoint();
    runConfigurationImportExtensionPoint.setRunConfigurationManager( runConfigurationManager );

    when( abstractMeta.getEmbeddedMetaStore() ).thenReturn( embeddedMetaStore );
  }

  @Test
  public void testCallExtensionPoint() throws Exception {
    runConfigurationImportExtensionPoint.callExtensionPoint( log, abstractMeta );

    verify( abstractMeta ).getEmbeddedMetaStore();
  }

  @Test
  public void shouldCreateRunConfigurationsForSlaveServer() throws Exception {
    JobMeta jobMeta = mock( JobMeta.class );
    JobEntryCopy jobEntryCopy1 = mock( JobEntryCopy.class );
    JobEntryCopy jobEntryCopy2 = mock( JobEntryCopy.class );
    JobEntryCopy jobEntryCopy3 = mock( JobEntryCopy.class );

    JobEntryTrans trans1 = mock( JobEntryTrans.class );
    JobEntryTrans trans2 = mock( JobEntryTrans.class );
    JobEntryTrans trans3 = mock( JobEntryTrans.class );

    ArgumentCaptor<DefaultRunConfiguration> rcCaptor =  ArgumentCaptor.forClass( DefaultRunConfiguration.class );
    when( jobMeta.getEmbeddedMetaStore() ).thenReturn( embeddedMetaStore );
    when( jobMeta.getJobCopies() ).thenReturn( Arrays.asList( jobEntryCopy1, jobEntryCopy2, jobEntryCopy3 ) );
    when( jobEntryCopy1.getEntry() ).thenReturn( trans1 );
    when( jobEntryCopy2.getEntry() ).thenReturn( trans2 );
    when( jobEntryCopy3.getEntry() ).thenReturn( trans3 );

    when( trans1.getRemoteSlaveServerName() ).thenReturn( "carte1" );
    when( trans2.getRemoteSlaveServerName() ).thenReturn( "carte1" );
    when( trans3.getRemoteSlaveServerName() ).thenReturn( "carte2" );
    when( trans1.getRunConfiguration() ).thenReturn( null );
    when( trans2.getRunConfiguration() ).thenReturn( null );
    when( trans3.getRunConfiguration() ).thenReturn( null );
    when( runConfigurationManager.getNames() ).thenReturn( Collections.singletonList( "pentaho_auto_carte1_config" ) );

    runConfigurationImportExtensionPoint.callExtensionPoint( log, jobMeta );

    verify( runConfigurationManager, times( 2 ) ).save( rcCaptor.capture() );
    verify( trans1 ).setRunConfiguration( "pentaho_auto_carte1_config_1" );
    verify( trans2 ).setRunConfiguration( "pentaho_auto_carte1_config_1" );
    verify( trans3 ).setRunConfiguration( "pentaho_auto_carte2_config" );

    List<DefaultRunConfiguration> allValues = rcCaptor.getAllValues();
    DefaultRunConfiguration runConfiguration1 = allValues.get( 0 );
    assertEquals( "pentaho_auto_carte1_config_1", runConfiguration1.getName() );
    assertEquals( "carte1", runConfiguration1.getServer() );

    DefaultRunConfiguration runConfiguration2 = allValues.get( 1 );
    assertEquals( "pentaho_auto_carte2_config", runConfiguration2.getName() );
    assertEquals( "carte2", runConfiguration2.getServer() );
  }

  @Test
  public void testCreateRunConfigurationName() throws Exception {
    assertEquals( "pentaho_auto_carte_config",
      runConfigurationImportExtensionPoint.createRunConfigurationName( Collections.emptyList(), "carte" ) );

    assertEquals( "pentaho_auto_carte_config_3",
      runConfigurationImportExtensionPoint.createRunConfigurationName(
        Arrays.asList( "pentaho_auto_carte_config_2", "pentaho_auto_carte_config_manuallyUpdated" ), "carte" ) );
  }
}
