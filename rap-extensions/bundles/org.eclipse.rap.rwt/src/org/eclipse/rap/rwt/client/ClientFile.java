/*******************************************************************************
 * Copyright (c) 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.client;

import org.eclipse.rap.rwt.client.service.ClientFileUploader;


/**
 * Represents a file on the client.
 *
 * @see ClientFileUploader
 * @since 2.3
 */
public interface ClientFile {

  /**
   * The name of the file on the client, without path information. If the client can not
   * determine the file name the string will be empty.
   *
   * @return the file name as a string. May be empty, but never <code>null</code>.
   */
  String getName();

  /**
   * The string in lower case representing the MIME type of the file. If the client can not
   * determine the file type the string will be empty.
   *
   * @return the type of the file. May be empty, but never <code>null</code>.
   */
  String getType();

  /**
   * Returns the size of the File on the client.
   *
   * @return the size in bytes.
   */
  long getSize();

}
