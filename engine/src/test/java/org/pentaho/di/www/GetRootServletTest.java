/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.www;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

/**
 * Tests for GetRootServlet class
 *
 * @author Pavel Sakun
 * @see GetRootServlet
 */
public class GetRootServletTest {
  @Test
  public void testDoGetReturn404StatusCode() throws ServletException, IOException {
    GetRootServlet servlet = new GetRootServlet();
    servlet.setJettyMode( true );
    HttpServletRequest request =
        when( mock( HttpServletRequest.class ).getRequestURI() ).thenReturn( "/wrong_path" ).getMock();
    HttpServletResponse response = mock( HttpServletResponse.class );
    servlet.doGet( request, response );
    verify( response ).setStatus( HttpServletResponse.SC_NOT_FOUND );
  }
}
