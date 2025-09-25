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
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.pan.EnhancedPanCommandExecutor;
import org.pentaho.di.pan.delegates.PanTransformationDelegate;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class LocalTransExecutorTest {
  @Mock
  private Trans trans;
  @Mock
  private Repository repository;
  private LocalTransExecutorService localTransExecutorService;
  private LogChannelInterface log;

  @Before
  public void setUp() {
    KettleLogStore.init();
    openMocks( this );
    log = new LogChannel( "LocalTransExecutorService" );
    localTransExecutorService = new LocalTransExecutorService();
  }

  @Test
  public void testExecuteTransformationLocally() throws KettleException {
    KettleEnvironment.init();
    TransMeta t = new TransMeta( DefaultBowl.getInstance(),
      EnhancedPanCommandExecutor.class.getResource( "hello-world.ktr" ).getPath() );
    when( repository.getBowl() ).thenReturn( DefaultBowl.getInstance() );
    TransExecutionConfiguration config = PanTransformationDelegate.createDefaultExecutionConfiguration();
    Result result = localTransExecutorService.execute( log, t, repository, config, new String[0] );
    assertNotNull( result );
    assertTrue( result.getResult() );
    assertEquals( 0, result.getNrErrors() );
  }

}
