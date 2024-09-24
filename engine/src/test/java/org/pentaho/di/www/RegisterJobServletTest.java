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
package org.pentaho.di.www;

import org.junit.Test;

import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobConfiguration;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.util.Assert.assertFalse;
import static org.pentaho.di.core.util.Assert.assertTrue;

public class RegisterJobServletTest {

  @Test
  public void createJobGatheringMetricsTrueTest() throws Exception {
    assertTrue( createJob( true ).isGatheringMetrics() );
  }

  @Test
  public void createJobGatheringMetricsFalseTest() throws Exception {
    assertFalse( createJob( false ).isGatheringMetrics() );
  }

  private Job createJob( boolean gatheringMetrics ) throws Exception {
    RegisterJobServlet jobServlet = new RegisterJobServlet();
    JobMap jobMap = mock( JobMap.class );
    jobServlet.setup( new TransformationMap(), jobMap, new SocketRepository( new LogChannel( "test" ) ), new ArrayList<>() );
    JobConfiguration conf = mock( JobConfiguration.class );
    JobExecutionConfiguration execConf = mock( JobExecutionConfiguration.class );
    JobMeta jobMeta = mock( JobMeta.class );
    SlaveServerConfig slaveServerConfig = mock( SlaveServerConfig.class );
    when( conf.getJobExecutionConfiguration() ).thenReturn( execConf );
    when( execConf.isGatheringMetrics() ).thenReturn( gatheringMetrics );
    when( conf.getJobMeta() ).thenReturn( jobMeta );
    when( jobMap.getSlaveServerConfig() ).thenReturn( slaveServerConfig );
    when( jobMeta.listParameters() ).thenReturn( new String[] {} );
    return jobServlet.createJob( conf );
  }
}
