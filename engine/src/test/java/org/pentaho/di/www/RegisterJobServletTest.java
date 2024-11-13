/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.www;


import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobConfiguration;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobMeta;
import org.pentaho.metastore.stores.delegate.DelegatingMetaStore;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
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
      @Override public int read() throws IOException {
        return is.read();
      }
    } );
    registerJobServlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    verify( mockHttpServletResponse, times( expectedResponseCode == HttpServletResponse.SC_OK ? 0 : 1 ) ).setStatus( expectedResponseCode );
  }


}
