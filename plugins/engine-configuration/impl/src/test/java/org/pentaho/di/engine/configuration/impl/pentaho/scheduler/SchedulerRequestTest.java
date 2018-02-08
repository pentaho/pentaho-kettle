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
