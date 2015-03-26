package org.pentaho.di.core.variables;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Variables tests.
 *
 * @author Yury Bakhmutski
 * @see Variables
 */
public class VariablesTest {

  /**
   * Test for PDI-12893 issue. 
   * Checks if an ConcurrentModificationException while iterating over the System properties is occurred.  
   */
  @Test
  public void testinItializeVariablesFrom() {
    final Variables variablesMock = mock( Variables.class );
    doCallRealMethod().when( variablesMock ).initializeVariablesFrom( any( VariableSpace.class ) );

    @SuppressWarnings( "unchecked" )
    final Map<String, String> propertiesMock = mock( Map.class );
    when( variablesMock.getProperties() ).thenReturn( propertiesMock );

    doAnswer( new Answer<Map<String, String>>() {
      final String keyStub = "key";

      @Override
      public Map<String, String> answer( InvocationOnMock invocation ) throws Throwable {
        if ( System.getProperty( keyStub ) == null ) {
          modifySystemproperties();
        }

        if ( invocation.getArguments()[1] != null ) {
          propertiesMock.put( (String) invocation.getArguments()[0], System.getProperties().getProperty(
              (String) invocation.getArguments()[1] ) );
        }
        return propertiesMock;
      }
    } ).when( propertiesMock ).put( anyString(), anyString() );

    variablesMock.initializeVariablesFrom( null );
  }

  private void modifySystemproperties() {
    final String keyStub = "key";
    final String valueStub = "value";

    Thread thread = new Thread( new Runnable() {
      @Override
      public void run() {
        System.setProperty( keyStub, valueStub );
      }
    } );
    thread.start();
  }

}
