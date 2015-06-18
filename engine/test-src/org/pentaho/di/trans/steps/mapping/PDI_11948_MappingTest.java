package org.pentaho.di.trans.steps.mapping;

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
 * The PDI_11948_MappingTest class tests mapping step of PDI-11948 bug. It's check if HttpServletResponse object is null
 * and call or not setServletReponse( HttpServletResponse response ) method of appropriate Trans object.
 * 
 * @author Yury Bakhmutski
 * @see org.pentaho.di.trans.steps.mapping.Mapping
 * @see org.pentaho.di.trans.steps.simplemapping.SimpleMapping
 * @see org.pentaho.di.trans.steps.transexecutor.TransExecutor
 * @see org.pentaho.di.trans.steps.singlethreader.SingleThreader
 */
public class PDI_11948_MappingTest extends PDI_11948_StepsTestsParent<Mapping, MappingData> {

  @Override
  @Before
  public void init() throws Exception {
    super.init();
    stepMock = mock( Mapping.class );
    stepDataMock = mock( MappingData.class );
  }

  @Test
  public void testMappingStep() throws KettleException {

    when( stepMock.getData() ).thenReturn( stepDataMock );
    when( stepDataMock.getMappingTrans() ).thenReturn( transMock );

    // stubbing methods for null-checking
    when( stepMock.getTrans() ).thenReturn( transMock );
    when( transMock.getServletResponse() ).thenReturn( null );

    doThrow( new RuntimeException( "The getServletResponse() mustn't be executed!" ) ).when( transMock )
        .setServletReponse( any( HttpServletResponse.class ) );

    doCallRealMethod().when( stepMock ).initServletConfig();
    stepMock.initServletConfig();
  }
}
