/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.ui.spoon;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

public class SpoonSlaveTest {


  @Before
  public void setup() {

  }

  @Test
  public void setErrorTextWithNoCauseException() {
    Exception e = new KettleException( "kettleMessage" );

    SpoonSlave spoonSlave = mock( SpoonSlave.class );
    doCallRealMethod().when( spoonSlave ).setExceptionMessage( any( Exception.class ) );

    String message = spoonSlave.setExceptionMessage( e );

    assertEquals( message, e.getMessage().toString() );

  }
  @Test
  public void setErrorTextWithCauseMessageException() {
    ClientProtocolException cpe = new ClientProtocolException( "causeMessage" );
    Exception e = new KettleException( "kettleMessage", cpe );

    SpoonSlave spoonSlave = mock( SpoonSlave.class );
    doCallRealMethod().when( spoonSlave ).setExceptionMessage( any( Exception.class ) );

    String message = spoonSlave.setExceptionMessage( e );

    Throwable cause = e.getCause();

    assertEquals( message, cause.getMessage().toString() );

  }
  @Test
  public void setErrorTextWithCauseExceptionWithoutCauseMessage() {
    //cause without message
    ClientProtocolException cpe = new ClientProtocolException(  );
    Exception e = new KettleException( "kettleMessage", cpe );

    SpoonSlave spoonSlave = mock( SpoonSlave.class );
    doCallRealMethod().when( spoonSlave ).setExceptionMessage( any( Exception.class ) );

    String message = spoonSlave.setExceptionMessage( e );

    assertEquals( message, e.getMessage().toString()  );

  }

}
