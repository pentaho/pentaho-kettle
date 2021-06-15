/*******************************************************************************
 * Copyright (c) 2009, 2014 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.testfixture.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;


/**
 * <p>
 * <strong>IMPORTANT:</strong> This class is <em>not</em> part the public RAP
 * API. It may change or disappear without further notice. Use this class at
 * your own risk.
 * </p>
 */
public class TestServletOutputStream extends ServletOutputStream {

  private final ByteArrayOutputStream stream = new ByteArrayOutputStream();

  @Override
  public void write( int bytes ) throws IOException {
    stream.write( bytes );
  }

  public ByteArrayOutputStream getContent() {
    return stream;
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void setWriteListener( WriteListener writeListener ) {
  }

}
