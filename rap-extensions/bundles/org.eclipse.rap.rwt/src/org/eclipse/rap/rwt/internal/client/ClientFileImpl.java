/*******************************************************************************
 * Copyright (c) 2014, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.client;

import org.eclipse.rap.rwt.client.ClientFile;


public class ClientFileImpl implements ClientFile {

  private final String fileId;
  private final String name;
  private final String type;
  private final long size;

  public ClientFileImpl( String fileId, String name, String type, long size ) {
    this.fileId = fileId;
    this.name = name;
    this.type = type;
    this.size = size;
  }

  public String getFileId() {
    return fileId;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public long getSize() {
    return size;
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append( name );
    buffer.append( ", " );
    buffer.append( type );
    buffer.append( ", " );
    buffer.append( size );
    buffer.append( " byte" );
    return "ClientFile{ " + buffer.toString() + " }";
  }

}
