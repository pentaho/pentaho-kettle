/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2024 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
