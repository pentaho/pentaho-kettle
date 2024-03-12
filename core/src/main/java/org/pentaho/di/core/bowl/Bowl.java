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
package org.pentaho.di.core.bowl;

import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.IMetaStore;


/**
 * A Bowl is a generic container/context/workspace concept. Different plugin implementations may implement this for
 * additional features.
 *
 */
public interface Bowl {

  /**
   * Gets a Metastore that handles any defaulting required for execution-time handling of metastores, for the Bowl.
   *
   * @return IMetaStore A metastore for execution with the Bowl. Never null.
   */
  IMetaStore getMetastore() throws MetaStoreException;

  /**
   * Gets a Metastore only for accessing any bowl-specific objects.
   *
   *
   * @return IMetaStore A metastore for the specified Bowl. Never null.
   */
  IMetaStore getExplicitMetastore() throws MetaStoreException;

}
