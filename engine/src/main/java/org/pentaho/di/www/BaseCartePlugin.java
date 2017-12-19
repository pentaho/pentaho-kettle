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

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMultimap;
import org.pentaho.di.core.annotations.CarteServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author nhudak
 */
public abstract class BaseCartePlugin extends BaseHttpServlet implements CartePluginInterface, CarteRequestHandler {
  /**
   * @param req  http servlet request
   * @param resp http servlet response
   * @throws IOException
   * @deprecated Should not be called directly. Use {@link #service(HttpServletRequest, HttpServletResponse)} instead
   */
  @Deprecated
  @Override public void doGet( HttpServletRequest req, final HttpServletResponse resp ) throws IOException {
    service( req, resp );
  }

  @Override protected void service( HttpServletRequest req, HttpServletResponse resp ) throws IOException {
    if ( isJettyMode() && !req.getContextPath().endsWith( getContextPath() ) ) {
      return;
    }

    if ( log.isDebug() ) {
      logDebug( getService() );
    }

    handleRequest( new CarteRequestImpl( req, resp ) );
  }

  @Override public abstract void handleRequest( CarteRequest request ) throws IOException;

  @Override public abstract String getContextPath();

  public String getService() {
    return getContextPath() + " (" + toString() + ")";
  }

  public String toString() {
    CarteServlet carteServlet = this.getClass().getAnnotation( CarteServlet.class );
    return carteServlet != null ? carteServlet.name() : super.toString();
  }

  private static FluentIterable<String> fromEnumeration( Enumeration enumeration ) {
    Iterable<?> list = Collections.list( enumeration );
    return FluentIterable.from( list ).filter( String.class );
  }

  private class CarteRequestImpl implements CarteRequest {
    private final HttpServletRequest req;
    private final HttpServletResponse resp;

    public CarteRequestImpl( HttpServletRequest req, HttpServletResponse resp ) {
      this.req = req;
      this.resp = resp;
    }

    @Override public String getMethod() {
      return req.getMethod();
    }

    @Override public Map<String, Collection<String>> getHeaders() {
      ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
      for ( String name : fromEnumeration( req.getHeaderNames() ) ) {
        builder.putAll( name, fromEnumeration( req.getHeaders( name ) ) );
      }
      return builder.build().asMap();
    }

    @Override public String getHeader( String name ) {
      return req.getHeader( name );
    }

    @Override public Map<String, Collection<String>> getParameters() {
      ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
      for ( String name : fromEnumeration( req.getParameterNames() ) ) {
        builder.putAll( name, req.getParameterValues( name ) );
      }
      return builder.build().asMap();
    }

    @Override public String getParameter( String name ) {
      return req.getParameter( name );
    }

    @Override public InputStream getInputStream() throws IOException {
      return req.getInputStream();
    }

    @Override public CarteResponse respond( int status ) {
      resp.setStatus( status );

      return new CarteResponse() {
        @Override public void with( String contentType, WriterResponse response ) throws IOException {
          resp.setContentType( contentType );
          response.write( resp.getWriter() );
        }

        @Override public void with( String contentType, OutputStreamResponse response ) throws IOException {
          resp.setContentType( contentType );
          response.write( resp.getOutputStream() );
        }

        @Override public void withMessage( String text ) throws IOException {
          resp.setContentType( "text/plain" );
          resp.getWriter().println( text );
        }
      };
    }
  }
}
