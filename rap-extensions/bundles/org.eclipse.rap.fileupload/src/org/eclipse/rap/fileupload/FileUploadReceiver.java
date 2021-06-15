/*******************************************************************************
 * Copyright (c) 2011, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.fileupload;

import java.io.IOException;
import java.io.InputStream;


/**
 * Instances of this interface are responsible for reading and processing the data from a file
 * upload.
 */
public abstract class FileUploadReceiver {

  /**
   * Reads and processes all data from the provided input stream.
   *
   * @param stream the stream to read from
   * @param details the details of the uploaded file like file name, content-type and size
   * @throws IOException if an input / output error occurs
   */
  public abstract void receive( InputStream stream, FileDetails details ) throws IOException;

}
