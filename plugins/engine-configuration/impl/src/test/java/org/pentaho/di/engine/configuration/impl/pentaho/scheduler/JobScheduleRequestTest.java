/*
 * *****************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2019-2024 by Hitachi Vantara : http://www.pentaho.com
 *
 *  *******************************************************************************
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 *
 */

package org.pentaho.di.engine.configuration.impl.pentaho.scheduler;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JobScheduleRequestTest {

  @Test
  public void getInputFile() {
    JobScheduleRequest jobScheduleRequest = mock( JobScheduleRequest.class );
    when( jobScheduleRequest.getInputFile() ).thenCallRealMethod();
    String inputFile = "hitachi";
    ReflectionTestUtils.setField( jobScheduleRequest, "inputFile", inputFile );
    Assert.assertEquals( inputFile, jobScheduleRequest.getInputFile() );
  }

  @Test
  public void setInputFile() {
    JobScheduleRequest jobScheduleRequest = mock( JobScheduleRequest.class );
    doCallRealMethod().when( jobScheduleRequest ).setInputFile( any() );
    String inputFile = "hitachi";
    jobScheduleRequest.setInputFile( inputFile );
    Assert.assertEquals( inputFile, ReflectionTestUtils.getField( jobScheduleRequest, "inputFile" ) );
  }

  @Test
  public void getJobParameters() {
    JobScheduleRequest jobScheduleRequest = mock( JobScheduleRequest.class );
    when( jobScheduleRequest.getJobParameters() ).thenCallRealMethod();
    List<String> jobParameters = new ArrayList<>();
    jobParameters.add( "hitachi" );
    ReflectionTestUtils.setField( jobScheduleRequest, "jobParameters", jobParameters );
    Assert.assertEquals( jobParameters, jobScheduleRequest.getJobParameters() );
  }

  @Test
  public void setJobParameters() {
    JobScheduleRequest jobScheduleRequest = mock( JobScheduleRequest.class );
    doCallRealMethod().when( jobScheduleRequest ).setJobParameters( any() );
    List<JobScheduleParam> jobParameters = new ArrayList<>();
    JobScheduleParam jobScheduleParam = new JobScheduleParam();
    jobParameters.add( jobScheduleParam );
    jobScheduleRequest.setJobParameters( jobParameters );
    Assert.assertEquals( jobParameters, ReflectionTestUtils.getField( jobScheduleRequest, "jobParameters" ) );
  }

  @Test
  public void getPdiParameters() {
    JobScheduleRequest jobScheduleRequest = mock( JobScheduleRequest.class );
    when( jobScheduleRequest.getPdiParameters() ).thenCallRealMethod();
    Map<String, String> pdiParameters = new HashMap<>();
    pdiParameters.put( "hitachi", "vantara" );
    ReflectionTestUtils.setField( jobScheduleRequest, "pdiParameters", pdiParameters );
    Assert.assertEquals( pdiParameters, jobScheduleRequest.getPdiParameters() );
  }

  @Test
  public void setPdiParameters() {
    JobScheduleRequest jobScheduleRequest = mock( JobScheduleRequest.class );
    doCallRealMethod().when( jobScheduleRequest ).setPdiParameters( any() );
    Map<String, String> pdiParameters = new HashMap<>();
    pdiParameters.put( "hitachi", "vantara" );
    jobScheduleRequest.setPdiParameters( pdiParameters );
    Assert.assertEquals( pdiParameters, ReflectionTestUtils.getField( jobScheduleRequest, "pdiParameters" ) );
  }
}