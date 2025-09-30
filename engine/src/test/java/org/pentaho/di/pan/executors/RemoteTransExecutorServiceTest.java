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

package org.pentaho.di.pan.executors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.pan.delegates.PanTransformationDelegate;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class RemoteTransExecutorServiceTest {

  @Mock
  private Trans trans;

  @Mock
  private Repository repository;

  @Mock
  private SlaveServer slaveServer;

  @Mock
  private LogChannelInterface log;

  private RemoteTransExecutorService remoteTransExecutorService;

  private PanTransformationDelegate delegate;

  @Before
  public void setUp() {
    openMocks( this );
    remoteTransExecutorService = new RemoteTransExecutorService();
    delegate = new PanTransformationDelegate( log, repository );
  }

  @Test
  public void testExecuteTransformationRemote() throws KettleException {
    TransMeta t = mock( TransMeta.class );
    when( trans.getTransMeta() ).thenReturn( t );
    when( slaveServer.getLogChannel() ).thenReturn( log );
    TransExecutionConfiguration config = createRemoteExecutionConfiguration( slaveServer );
    Result result;
    try ( MockedStatic<Trans> staticTransMock = Mockito.mockStatic( Trans.class ) ) {
      staticTransMock.when( () -> Trans.sendToSlaveServer( eq( t ), any(), eq( repository ), any() ) ).thenReturn( "carteId" );
      result = remoteTransExecutorService.execute( log, t, repository, config, new String[0] );
    }
    assertNotNull( result );
    assertTrue( result.getResult() );
  }

  /**
   * Create an execution configuration for remote execution.
   */
  public TransExecutionConfiguration createRemoteExecutionConfiguration( SlaveServer slaveServer ) {
    TransExecutionConfiguration config = delegate.createDefaultExecutionConfiguration();

    config.setExecutingLocally( false );
    config.setExecutingRemotely( true );
    config.setRemoteServer( slaveServer );

    return config;
  }
}
