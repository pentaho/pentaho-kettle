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


package org.pentaho.di.engine.configuration.impl.pentaho.scheduler;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.core.runconfiguration.impl.pentaho.scheduler.JobScheduleParam;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JobScheduleParamTest {


  @Test
  public void getName() {
    JobScheduleParam jobScheduleParam = mock( JobScheduleParam.class );
    when( jobScheduleParam.getName() ).thenCallRealMethod();
    String name = "hitachi";
    ReflectionTestUtils.setField( jobScheduleParam, "name", name );
    Assert.assertEquals( name, jobScheduleParam.getName() );
  }

  @Test
  public void setName() {
    JobScheduleParam jobScheduleParam = mock( JobScheduleParam.class );
    doCallRealMethod().when( jobScheduleParam ).setName( any() );
    String name = "hitachi";
    jobScheduleParam.setName( name );
    Assert.assertEquals( name, ReflectionTestUtils.getField( jobScheduleParam, "name" ) );
  }

  @Test
  public void getType() {
    JobScheduleParam jobScheduleParam = mock( JobScheduleParam.class );
    when( jobScheduleParam.getType() ).thenCallRealMethod();
    String type = "hitachi";
    ReflectionTestUtils.setField( jobScheduleParam, "type", type );
    Assert.assertEquals( type, jobScheduleParam.getType() );
  }

  @Test
  public void setType() {
    JobScheduleParam jobScheduleParam = mock( JobScheduleParam.class );
    doCallRealMethod().when( jobScheduleParam ).setType( any() );
    String type = "hitachi";
    jobScheduleParam.setType( type );
    Assert.assertEquals( type, ReflectionTestUtils.getField( jobScheduleParam, "type" ) );
  }

  @Test
  public void getStringValue() {
    JobScheduleParam jobScheduleParam = mock( JobScheduleParam.class );
    when( jobScheduleParam.getStringValue() ).thenCallRealMethod();
    List<String> stringValue = new ArrayList<>();
    stringValue.add( "hitachi" );
    ReflectionTestUtils.setField( jobScheduleParam, "stringValue", stringValue );
    Assert.assertEquals( stringValue, jobScheduleParam.getStringValue() );
  }

  @Test
  public void setStringValue() {
    JobScheduleParam jobScheduleParam = mock( JobScheduleParam.class );
    doCallRealMethod().when( jobScheduleParam ).setStringValue( any() );
    List<String> stringValue = new ArrayList<>();
    stringValue.add( "hitachi" );
    jobScheduleParam.setStringValue( stringValue );
    Assert.assertEquals( stringValue, ReflectionTestUtils.getField( jobScheduleParam, "stringValue" ) );
  }
}