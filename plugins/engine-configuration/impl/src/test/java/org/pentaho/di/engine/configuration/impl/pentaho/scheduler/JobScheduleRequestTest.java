package org.pentaho.di.engine.configuration.impl.pentaho.scheduler;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.reflection.Whitebox.getInternalState;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;

public class JobScheduleRequestTest {

  @Test
  public void getInputFile() {
    JobScheduleRequest jobScheduleRequest = mock( JobScheduleRequest.class );
    when( jobScheduleRequest.getInputFile() ).thenCallRealMethod();
    String inputFile = "hitachi";
    setInternalState( jobScheduleRequest, "inputFile", inputFile );
    Assert.assertEquals( inputFile, jobScheduleRequest.getInputFile() );
  }

  @Test
  public void setInputFile() {
    JobScheduleRequest jobScheduleRequest = mock( JobScheduleRequest.class );
    doCallRealMethod().when( jobScheduleRequest ).setInputFile( any() );
    String inputFile = "hitachi";
    jobScheduleRequest.setInputFile( inputFile );
    Assert.assertEquals( inputFile, getInternalState( jobScheduleRequest, "inputFile" ) );
  }

  @Test
  public void getJobParameters() {
    JobScheduleRequest jobScheduleRequest = mock( JobScheduleRequest.class );
    when( jobScheduleRequest.getJobParameters() ).thenCallRealMethod();
    List<String> jobParameters = new ArrayList<>();
    jobParameters.add( "hitachi" );
    setInternalState( jobScheduleRequest, "jobParameters", jobParameters );
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
    Assert.assertEquals( jobParameters, getInternalState( jobScheduleRequest, "jobParameters" ) );
  }

  @Test
  public void getPdiParameters() {
    JobScheduleRequest jobScheduleRequest = mock( JobScheduleRequest.class );
    when( jobScheduleRequest.getPdiParameters() ).thenCallRealMethod();
    Map<String, String> pdiParameters = new HashMap<>();
    pdiParameters.put( "hitachi", "vantara" );
    setInternalState( jobScheduleRequest, "pdiParameters", pdiParameters );
    Assert.assertEquals( pdiParameters, jobScheduleRequest.getPdiParameters() );
  }

  @Test
  public void setPdiParameters() {
    JobScheduleRequest jobScheduleRequest = mock( JobScheduleRequest.class );
    doCallRealMethod().when( jobScheduleRequest ).setPdiParameters( any() );
    Map<String, String> pdiParameters = new HashMap<>();
    pdiParameters.put( "hitachi", "vantara" );
    jobScheduleRequest.setPdiParameters( pdiParameters );
    Assert.assertEquals( pdiParameters, getInternalState( jobScheduleRequest, "pdiParameters" ) );
  }
}