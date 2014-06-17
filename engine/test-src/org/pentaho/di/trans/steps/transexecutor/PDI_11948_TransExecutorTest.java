package org.pentaho.di.trans.steps.transexecutor;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.steps.PDI_11948_StepsTestsParent;

/**
 * The PDI_11948_SimpleMappingTest class tests Transformation Executor step of PDI-11948 bug. It's check if
 * HttpServletResponse object is null and call or not setServletReponse( HttpServletResponse response ) method of
 * appropriate Trans object.
 * 
 * @author Yury Bakhmutski
 * @see org.pentaho.di.trans.steps.simplemapping.TransExecutor
 */
public class PDI_11948_TransExecutorTest extends PDI_11948_StepsTestsParent<TransExecutor, TransExecutorData> {

  @Override
  @Before
  public void init() throws Exception {
    super.init();
    stepMock = mock( TransExecutor.class );
    stepDataMock = mock( TransExecutorData.class );
  }

  @Test
  public void testMappingStep() throws KettleException {

    when( stepMock.getData() ).thenReturn( stepDataMock );
    when( stepDataMock.getExecutorTrans() ).thenReturn( transMock );

    // stubbing methods for null-checking
    when( stepMock.getTrans() ).thenReturn( transMock );
    when( transMock.getServletResponse() ).thenReturn( null );

    doThrow( new RuntimeException( "The getServletResponse() mustn't be executed!" ) ).when( transMock )
        .setServletReponse( any( HttpServletResponse.class ) );

    doCallRealMethod().when( stepMock ).initServletConfig();
    stepMock.initServletConfig();
  }
}
