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


package org.pentaho.di.ui.core.dialog;

import org.apache.http.client.ClientProtocolException;
import org.junit.Before;
import org.pentaho.di.core.exception.KettleException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;


public class ErrorDialogTest {



  @Before
  public void setup() {
  }

  @Test
  public void setErrorTextWithNoCauseException() {
    Exception e = new KettleException( "kettleMessage" );

    StringBuilder text = new StringBuilder();
    StringBuilder details = new StringBuilder();

    ErrorDialog dialog = mock( ErrorDialog.class );
    doCallRealMethod().when( dialog ).handleException( anyString(), any( Exception.class ), any( StringBuilder.class ), any( StringBuilder.class ) );

    dialog.handleException( "argMessage", e, text, details );

    assertEquals( text.toString(), e.getMessage().toString() );

  }

  @Test
  public void setErrorTextWithCauseMessageException() {
    ClientProtocolException cpe = new ClientProtocolException( "causeMessage" );
    Exception e = new KettleException( "kettleMessage", cpe );


    StringBuilder text = new StringBuilder();
    StringBuilder details = new StringBuilder();

    ErrorDialog dialog = mock( ErrorDialog.class );
    doCallRealMethod().when( dialog ).handleException( anyString(), any( Exception.class ), any( StringBuilder.class ), any( StringBuilder.class ) );

    dialog.handleException( "argMessage", e, text, details );

    Throwable cause = e.getCause();

    assertEquals(  text.toString(), cause.getMessage().toString() );

  }

  @Test
  public void setErrorTextWithCauseExceptionWithoutCauseMessage() {
    //cause without message
    ClientProtocolException cpe = new ClientProtocolException(  );
    Exception e = new KettleException( "kettleMessage", cpe );


    StringBuilder text = new StringBuilder();
    StringBuilder details = new StringBuilder();

    ErrorDialog dialog = mock( ErrorDialog.class );
    doCallRealMethod().when( dialog ).handleException( anyString(), any( Exception.class ), any( StringBuilder.class ), any( StringBuilder.class ) );

    dialog.handleException( "argMessage", e, text, details );

    assertEquals(  text.toString(), e.getMessage().toString() );

  }

}
