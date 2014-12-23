package org.pentaho.di.trans.steps.ivwloader;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class IngresVectorwise_PDI_12555_Test {

  @Test
  public void testReplace() {
    String input = "\\\"Name\"";
    String[] from = new String[] { "\"" };
    String[] to = new String[] { "\\\"" };

    final IngresVectorwiseLoader ingresVectorwiseLoaderMock = mock( IngresVectorwiseLoader.class );
    doCallRealMethod().when( ingresVectorwiseLoaderMock ).replace( anyString(), any( String[].class ),
        any( String[].class ) );

    String actual = ingresVectorwiseLoaderMock.replace( input, from, to );
    String expected = "\\\\\"Name\\\"";

    assertEquals( actual, expected );
  }
  
  @Test
  public void testMasqueradPassword() {
    String cmdUsingVwload = "this is the string without brackets";

    final IngresVectorwiseLoader ingresVectorwiseLoaderMock = mock( IngresVectorwiseLoader.class );
    doCallRealMethod().when( ingresVectorwiseLoaderMock ).masqueradPassword( anyString() );

    ingresVectorwiseLoaderMock.masqueradPassword( cmdUsingVwload );
  }
}
