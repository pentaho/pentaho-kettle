package org.pentaho.di.trans.steps.mapping;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;

public class PDI_11948_Test {
  @Mock
  Mapping mappingMock;
  @Mock
  Trans transMock;
  @Mock
  MappingData mappingDataMock;

  @Before
  public void init() throws Exception {
    MockitoAnnotations.initMocks( this );
  }

  @Test
  public void testInitServletConfig() throws KettleException {

    when( mappingMock.getData() ).thenReturn( mappingDataMock );
    when( mappingDataMock.getMappingTrans() ).thenReturn( transMock );

    // stubbing methods for null-checking
    when( mappingMock.getTrans() ).thenReturn( transMock );
    when( transMock.getServletResponse() ).thenReturn( null );

    doThrow( new RuntimeException( "The getServletResponse() mustn't be executed!" ) ).when( transMock )
        .setServletReponse( any( HttpServletResponse.class ) );

    doCallRealMethod().when( mappingMock ).initServletConfig();
    mappingMock.initServletConfig();

  }

}
