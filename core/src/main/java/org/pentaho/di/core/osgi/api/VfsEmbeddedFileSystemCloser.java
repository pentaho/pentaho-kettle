/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.di.core.osgi.api;

/**
 * Created by tkafalas on 7/24/2017.
 */
public interface VfsEmbeddedFileSystemCloser {
  void closeFileSystem( String key );
}
