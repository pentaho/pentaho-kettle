package org.pentaho.di.engine.configuration.impl.pentaho.scheduler;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.reflection.Whitebox.getInternalState;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;

public class JobScheduleParamTest {


  @Test
  public void getName() {
    JobScheduleParam jobScheduleParam = mock( JobScheduleParam.class );
    when( jobScheduleParam.getName() ).thenCallRealMethod();
    String name = "hitachi";
    setInternalState( jobScheduleParam, "name", name );
    Assert.assertEquals( name, jobScheduleParam.getName() );
  }

  @Test
  public void setName() {
    JobScheduleParam jobScheduleParam = mock( JobScheduleParam.class );
    doCallRealMethod().when( jobScheduleParam ).setName( any() );
    String name = "hitachi";
    jobScheduleParam.setName( name );
    Assert.assertEquals( name, getInternalState( jobScheduleParam, "name" ) );
  }

  @Test
  public void getType() {
    JobScheduleParam jobScheduleParam = mock( JobScheduleParam.class );
    when( jobScheduleParam.getType() ).thenCallRealMethod();
    String type = "hitachi";
    setInternalState( jobScheduleParam, "type", type );
    Assert.assertEquals( type, jobScheduleParam.getType() );
  }

  @Test
  public void setType() {
    JobScheduleParam jobScheduleParam = mock( JobScheduleParam.class );
    doCallRealMethod().when( jobScheduleParam ).setType( any() );
    String type = "hitachi";
    jobScheduleParam.setType( type );
    Assert.assertEquals( type, getInternalState( jobScheduleParam, "type" ) );
  }

  @Test
  public void getStringValue() {
    JobScheduleParam jobScheduleParam = mock( JobScheduleParam.class );
    when( jobScheduleParam.getStringValue() ).thenCallRealMethod();
    List<String> stringValue = new ArrayList<>();
    stringValue.add( "hitachi" );
    setInternalState( jobScheduleParam, "stringValue", stringValue );
    Assert.assertEquals( stringValue, jobScheduleParam.getStringValue() );
  }

  @Test
  public void setStringValue() {
    JobScheduleParam jobScheduleParam = mock( JobScheduleParam.class );
    doCallRealMethod().when( jobScheduleParam ).setStringValue( any() );
    List<String> stringValue = new ArrayList<>();
    stringValue.add( "hitachi" );
    jobScheduleParam.setStringValue( stringValue );
    Assert.assertEquals( stringValue, getInternalState( jobScheduleParam, "stringValue" ) );
  }
}