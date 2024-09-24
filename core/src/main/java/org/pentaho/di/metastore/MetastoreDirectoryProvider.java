/*!
 * Copyright 2024 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
