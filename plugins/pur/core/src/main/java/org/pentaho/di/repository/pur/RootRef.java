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
package org.pentaho.di.repository.pur;

import java.lang.ref.SoftReference;

import org.pentaho.di.repository.RepositoryDirectoryInterface;

public class RootRef {
  private SoftReference<RepositoryDirectoryInterface> rootRef = null;

  public synchronized void setRef( RepositoryDirectoryInterface ref ) {
    rootRef = new SoftReference<RepositoryDirectoryInterface>( ref );
  }

  public synchronized RepositoryDirectoryInterface getRef() {
    return rootRef == null ? null : rootRef.get();
  }

  public synchronized void clearRef() {
    rootRef = null;
  }
}
