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

package org.pentaho.di.www;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;

/**
 * @see BaseCartePlugin
 * @author nhudak
 */
public interface CarteRequestHandler {
  void handleRequest( CarteRequest request ) throws IOException;

  interface CarteRequest {
    String getMethod();

    Map<String, Collection<String>> getHeaders();

    String getHeader( String name );

    Map<String, Collection<String>> getParameters();

    String getParameter( String name );

    InputStream getInputStream() throws IOException;

    CarteResponse respond( int status );
  }

  interface CarteResponse {
    void with( String contentType, WriterResponse response ) throws IOException;

    void with( String contentType, OutputStreamResponse response ) throws IOException;

    void withMessage( String text ) throws IOException;
  }

  interface WriterResponse {
    void write( PrintWriter writer ) throws IOException;
  }

  interface OutputStreamResponse {
    void write( OutputStream outputStream ) throws IOException;
  }
}
