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
package org.pentaho.metastore.locator.api;

import org.pentaho.metastore.api.IMetaStore;

public interface MetastoreDirectoryProvider {

  /**
   * Returns a metastore implementation at the given path. Different implementations may support different types of
   * paths (e.g. local, vfs)
   *
   *
   * @param rootFolder path to the metastore parent directory. The ".metastore" directory will be created under this
   *                  path.
   *
   * @return IMetaStore a metastore implementation at the given path, or null
   *
   */
  IMetaStore getMetastoreForDirectory( String rootFolder );

}
