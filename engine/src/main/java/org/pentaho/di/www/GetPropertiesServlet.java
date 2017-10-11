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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.util.EnvUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetPropertiesServlet extends BodyHttpServlet {

  private static final long serialVersionUID = 4872614637561572356L;

  public static final String CONTEXT_PATH = "/kettle/properties";

  @Override
  public String getContextPath() {
    return CONTEXT_PATH;
  }

  @Override
  WebResult generateBody( HttpServletRequest request, HttpServletResponse response, boolean useXML ) throws Exception {
    ServletOutputStream out = response.getOutputStream();
    Properties kettleProperties = EnvUtil.readProperties( Const.KETTLE_PROPERTIES );
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    if ( useXML ) {
      kettleProperties.storeToXML( os, "" );
    } else {
      kettleProperties.store( os, "" );
    }
    out.write( Encr.encryptPassword( os.toString() ).getBytes() );
    return null;
  }

  @Override
  protected void startXml( HttpServletResponse response, PrintWriter out ) throws IOException {
    response.setContentType( "text/xml" );
    response.setCharacterEncoding( Const.XML_ENCODING );
  }
}
