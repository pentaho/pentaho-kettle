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
