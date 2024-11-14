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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link org.pentaho.di.www.GetPropertiesServlet}
 */
public class GetPropertiesServletTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @BeforeClass
  public static void beforeClass() throws KettleException {
    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    String passwordEncoderPluginID =
      Const.NVL( EnvUtil.getSystemProperty( Const.KETTLE_PASSWORD_ENCODER_PLUGIN ), "Kettle" );
    Encr.init( passwordEncoderPluginID );
  }

  @Test
  public void getContextPath() throws Exception {

    GetPropertiesServlet servlet = new GetPropertiesServlet();
    servlet.setJettyMode( true );
    HttpServletRequest mockHttpServletRequest = mock( HttpServletRequest.class );
    HttpServletResponse mockHttpServletResponse = mock( HttpServletResponse.class );
    StringWriter out = new StringWriter();
    PrintWriter printWriter = new PrintWriter( out );

    when( mockHttpServletRequest.getContextPath() ).thenReturn( GetPropertiesServlet.CONTEXT_PATH );
    when( mockHttpServletRequest.getParameter( "xml" ) ).thenReturn( "Y" );
    when( mockHttpServletResponse.getWriter() ).thenReturn( printWriter );

    when( mockHttpServletResponse.getOutputStream() ).thenReturn( new ServletOutputStream() {
      private ByteArrayOutputStream baos = new ByteArrayOutputStream();

      @Override public void write( int b ) throws IOException {
        baos.write( b );
      }

      public String toString() {
        return baos.toString();
      }
    } );

    servlet.doGet( mockHttpServletRequest, mockHttpServletResponse );
    //check that response is not contains sample text
    Assert.assertFalse( mockHttpServletResponse.getOutputStream().toString()
      .startsWith( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" ) );
  }
}
