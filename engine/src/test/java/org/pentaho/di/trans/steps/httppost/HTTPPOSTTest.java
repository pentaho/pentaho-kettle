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


package org.pentaho.di.trans.steps.httppost;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

public class HTTPPOSTTest {

  @Test
  public void getRequestBodyParametersAsStringWithNullEncoding() throws KettleException {
    HTTPPOST http = mock( HTTPPOST.class );
    doCallRealMethod().when( http ).getRequestBodyParamsAsStr( any( NameValuePair[].class ), nullable( String.class ) );

    NameValuePair[] pairs = new NameValuePair[] {
      new BasicNameValuePair( "u", "usr" ),
      new BasicNameValuePair( "p", "pass" )
    };

    assertEquals( "u=usr&p=pass", http.getRequestBodyParamsAsStr( pairs, null ) );
  }

}
