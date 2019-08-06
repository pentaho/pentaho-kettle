/*!
 * Copyright 2010 - 2019 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.repository.pur;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.net.ConnectException;

import com.sun.xml.ws.client.ClientTransportException;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.repository.KettleAuthenticationException;
import org.pentaho.di.repository.KettleRepositoryLostException;

public class UnifiedRepositoryInvocationHandlerTest {
  private static interface IFace {
    Object doNotThrowException();

    Object throwSomeException();

    Object throwChainedConnectException();

    Object throwClientTransportException();
  };

  private static final Object returnValue = "return-value";
  private static final RuntimeException rte = new RuntimeException( "some-exception" );
  private static final ConnectException connectException = new ConnectException();
  private static final ClientTransportException clientTransportExceptionException = mock( ClientTransportException.class );

  private static final IFace wrappee = new IFace() {

    @Override
    public Object doNotThrowException() {
      return returnValue;
    }

    @Override
    public Object throwSomeException() {
      throw rte;
    }

    @Override
    public Object throwChainedConnectException() {
      throw new RuntimeException( "wrapper-exception", connectException );
    }

    @Override
    public Object throwClientTransportException() {
      throw new RuntimeException( "wrapper-exception", clientTransportExceptionException );
    }

  };

  IFace testee;

  @Before
  public void setUp() {
    testee = UnifiedRepositoryInvocationHandler.forObject( wrappee, IFace.class );
  }

  @Test
  public void testNormalCall() {
    assertEquals( "the method did not return what was expected", returnValue, testee.doNotThrowException() );
  }

  @Test
  public void testThrowingSomeException() {
    try {
      testee.throwSomeException();
    } catch ( RuntimeException actual ) {
      assertEquals( "did not get the expected runtime exception", rte, actual );
    }
  }

  @Test
  public void testThrowingConnectException() {
    try {
      testee.throwChainedConnectException();
    } catch ( KettleRepositoryLostException krle ) {
      Throwable found = krle;
      while ( found != null ) {
        if ( connectException.equals( found ) ) {
          break;
        }
        found = found.getCause();
      }
      assertNotNull( "Should have found the original ConnectException" );
    } catch ( Throwable other ) {
      fail( "Should not catch something other than KettleRepositoryLostException" );
    }
  }

  @Test
  public void testThrowingClientTransportException() {
    try {
      testee.throwClientTransportException();
    } catch ( KettleAuthenticationException kae ) {
      Throwable found = kae;
      while ( found != null ) {
        if ( clientTransportExceptionException.equals( found ) ) {
          break;
        }
        found = found.getCause();
      }
      assertNotNull( "Should have found the original ClientTransportException" );
    } catch ( Throwable other ) {
      fail( "Should not catch something other than KettleAuthenticationException" );
    }
  }
}
