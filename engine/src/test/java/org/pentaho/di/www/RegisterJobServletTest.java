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

package org.pentaho.di.www;


import jakarta.servlet.ReadListener;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobConfiguration;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.metastore.stores.delegate.DelegatingMetaStore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.util.Assert.assertFalse;
import static org.pentaho.di.core.util.Assert.assertTrue;



public class RegisterJobServletTest {

  private RegisterJobServlet registerJobServlet;

  @Before
  public void setup() {
    registerJobServlet = new RegisterJobServlet();
    TransformationMap transformationMap = mock( TransformationMap.class );
    SlaveServerConfig slaveServerConfig = mock( SlaveServerConfig.class );
    DelegatingMetaStore delegatingMetaStore = mock( DelegatingMetaStore.class );
    registerJobServlet.transformationMap = transformationMap;
    when( transformationMap.getSlaveServerConfig() ).thenReturn( slaveServerConfig );
    when( slaveServerConfig.getMetaStore() ).thenReturn( delegatingMetaStore );
  }

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
    when( jobMeta.getBowl() ).thenReturn( DefaultBowl.getInstance() );
    return jobServlet.createJob( conf );
  }

  @Test
  public void doGetInvalidXmlTest() throws Exception {
    String xml = "<somexml></xml>";
    doGetTest( xml, HttpServletResponse.SC_BAD_REQUEST );
  }

  @Test
  public void doGetJobOkTest() throws Exception {

    String xml = "<job_configuration>"
      +             "<job>"
      +               "<info>"
      +                 "<name>test</name>"
      +               "</info>"
      +             "</job>"
      +             "<job_execution_configuration>"
      +               "<variables>"
      +                 "<variable>"
      +                   "<name>foo</name>"
      +                   "<value>bar</value>"
      +                 "</variable>"
      +               "</variables>"
      +             "</job_execution_configuration>"
      +          "</job_configuration>";

    doGetTest( xml, HttpServletResponse.SC_OK );
  }

  private void doGetTest( String xmlJob, int expectedResponseCode ) throws Exception {
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    ServletOutputStream outputStream = mock( ServletOutputStream.class );
    when( mockHttpServletResponse.getOutputStream() ).thenReturn( outputStream );
    final ByteArrayInputStream is = new ByteArrayInputStream( xmlJob.getBytes() );
    when( mockHttpServletRequest.getInputStream() ).thenReturn( new ServletInputStream() {
      @Override
      public boolean isFinished() {
        return false;
      }

      @Override
      public boolean isReady() {
        return false;
      }

      @Override
      public void setReadListener(ReadListener readListener) {

      }

      @Override public int read() throws IOException {
        return is.read();
      }
    } );
    registerJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    verify( mockHttpServletResponse, times( expectedResponseCode == HttpServletResponse.SC_OK ? 0 : 1 ) ).setStatus( expectedResponseCode );
  }


}
