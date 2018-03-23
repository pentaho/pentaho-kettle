/*
  * *****************************************************************************
  *
  *  Pentaho Data Integration
  *
  *  Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.repository.RepositoryDirectoryInterface;

import java.io.UnsupportedEncodingException;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doReturn;

public class SchedulerRequestTest {
  private static final String EMPTY_STRING = "";
  private static final String TEST_PARAM_NAME = "paramName";
  private static final String[] ARRAY_WITH_PARAM_NAME = new String[]{TEST_PARAM_NAME};

  private SchedulerRequest schedulerRequest;

  @Before
  public void before() {
    schedulerRequest = mock( SchedulerRequest.class );
  }

  @Test
  @SuppressWarnings( "ResultOfMethodCallIgnored" )
  public void testBuildSchedulerRequestEntity() throws UnknownParamException, UnsupportedEncodingException {
    AbstractMeta abstractMeta = mock( AbstractMeta.class );
    RepositoryDirectoryInterface repositoryDirectoryInterface = mock( RepositoryDirectoryInterface.class );

    doReturn( repositoryDirectoryInterface ).when( abstractMeta ).getRepositoryDirectory();
    doReturn( EMPTY_STRING ).when( repositoryDirectoryInterface ).getPath();
    doReturn( EMPTY_STRING ).when( abstractMeta ).getName();
    doReturn( EMPTY_STRING ).when( abstractMeta ).getDefaultExtension();
    doReturn( ARRAY_WITH_PARAM_NAME ).when( abstractMeta ).listParameters();

    doCallRealMethod().when( schedulerRequest ).buildSchedulerRequestEntity( abstractMeta );
    schedulerRequest.buildSchedulerRequestEntity( abstractMeta );

    verify( abstractMeta ).listParameters();
    verify( abstractMeta ).getParameterValue( TEST_PARAM_NAME );
  }
}
