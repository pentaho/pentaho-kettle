/*******************************************************************************
 * Copyright (c) 2009, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.testfixture.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;


/**
 * <p>
 * <strong>IMPORTANT:</strong> This class is <em>not</em> part the public RAP
 * API. It may change or disappear without further notice. Use this class at
 * your own risk.
 * </p>
 */
public class TestResponse implements HttpServletResponse {

  private TestServletOutputStream outStream;
  private String contentType;
  private String characterEncoding;
  private final Map<String,Cookie> cookies;
  private final Map<String,String> headers;
  private int errorStatus;
  private int status;
  private String redirect;
  private PrintWriter printWriter;

  public TestResponse() {
    characterEncoding = "UTF-8";
    outStream = new TestServletOutputStream();
    cookies = new HashMap<String,Cookie>();
    headers = new HashMap<String,String>();
  }

  public void addCookie( Cookie arg0 ) {
    cookies.put( arg0.getName(), arg0 );
  }

  public Cookie getCookie( String cookieName ) {
    return cookies.get( cookieName );
  }

  public boolean containsHeader( String arg0 ) {
    return false;
  }

  public String encodeURL( String arg0 ) {
    return arg0;
  }

  public String encodeRedirectURL( String arg0 ) {
    return arg0;
  }

  public String encodeUrl( String arg0 ) {
    return arg0;
  }

  public String encodeRedirectUrl( String arg0 ) {
    return arg0;
  }

  public void sendError( int code, String message )
    throws IOException
  {
    errorStatus = code;
    getWriter().write( "HTTP ERROR " + code + "\nReason: " + message );
  }

  public void sendError( int code ) throws IOException {
    errorStatus = code;
    getWriter().write( "HTTP ERROR " + code );
  }

  public int getErrorStatus() {
    return errorStatus;
  }

  public void sendRedirect( String arg0 ) throws IOException {
    redirect = arg0;
  }

  public String getRedirect() {
    return redirect;
  }

  public void setDateHeader( String arg0, long arg1 ) {
    headers.put( arg0, new Date( arg1 ).toString() );
  }

  public void addDateHeader( String arg0, long arg1 ) {
    headers.put( arg0, new Date( arg1 ).toString() );
  }

  public void setHeader( String arg0, String arg1 ) {
    headers.put( arg0, arg1 );
  }

  public String getHeader( String name ) {
    return headers.get( name );
  }

  public void addHeader( String arg0, String arg1 ) {
    headers.put( arg0, arg1 );
  }

  public void setIntHeader( String arg0, int arg1 ) {
  }

  public void addIntHeader( String arg0, int arg1 ) {
  }

  public void setStatus( int arg0 ) {
    status = arg0;
  }

  public void setStatus( int arg0, String arg1 ) {
  }

  public ServletOutputStream getOutputStream() throws IOException {
    return outStream;
  }

  public PrintWriter getWriter() throws IOException {
    if( printWriter == null ) {
      printWriter = new PrintWriter( new OutputStreamWriter( outStream, characterEncoding ) );
    }
    return printWriter;
  }

  public void setContentLength( int arg0 ) {
  }

  public void setContentLengthLong( long len ) {
  }

  public void setContentType( String contentType ) {
    this.contentType = contentType;
    setHeader( "Content-Type", contentType );
  }

  public String getContentType() {
    return getHeader( "Content-Type" );
  }

  public void setCharacterEncoding( String charset ) {
    characterEncoding = charset;
    setHeader( "Content-Type", contentType + "; charset=" + charset );
  }

  public String getCharacterEncoding() {
    return characterEncoding;
  }

  public void setBufferSize( int arg0 ) {
  }

  public int getBufferSize() {
    return 0;
  }

  public void flushBuffer() throws IOException {
  }

  public void resetBuffer() {
  }

  public boolean isCommitted() {
    return false;
  }

  public void reset() {
  }

  public void setLocale( Locale arg0 ) {
  }

  public Locale getLocale() {
    return null;
  }

  public String getContent() {
    String result = "";
    if( printWriter != null ) {
      printWriter.flush();
      ByteArrayOutputStream content = outStream.getContent();
      try {
        result = content.toString( characterEncoding );
      } catch( UnsupportedEncodingException exception ) {
        throw new RuntimeException( exception );
      }
    }
    return result;
  }

  public void clearContent() {
    outStream = new TestServletOutputStream();
    printWriter = null;
  }

  public int getStatus() {
    return status;
  }

  public Collection<String> getHeaders( String name ) {
    return null;
  }

  public Collection<String> getHeaderNames() {
    return null;
  }

}
