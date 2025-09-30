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
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.pan.delegates.PanTransformationDelegate;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.openMocks;

public class LocalTransExecutorServiceTest {

  @Mock
  private Repository repository;
  private LocalTransExecutorService localTransExecutorService;

  @Mock
  private LogChannelInterface log;
  private PanTransformationDelegate delegate;

  @Before
  public void setUp() {
    openMocks( this );
    localTransExecutorService = new LocalTransExecutorService();
    delegate = new PanTransformationDelegate( log, repository );
  }

  @Test
  public void testExecuteTransformationLocally() throws KettleException {
    Trans mockTrans = mock( Trans.class );
    TransExecutionConfiguration config = delegate.createDefaultExecutionConfiguration();
    config.setVariables( Map.of( "param", "value" ) );
    String[] params = new String[0];
    localTransExecutorService.execute( mockTrans, repository, config, params );
    verify( mockTrans ).setRepository( repository );
    verify( mockTrans ).setLogLevel( config.getLogLevel() );
    verify( mockTrans ).setSafeModeEnabled( config.isSafeModeEnabled() );
    verify( mockTrans ).setGatheringMetrics( config.isGatheringMetrics() );

    verify( mockTrans ).prepareExecution( params );
    verify( mockTrans ).startThreads();
    verify( mockTrans ).waitUntilFinished();
  }

}
