/*!
 * Copyright 2010 - 2024 Hitachi Vantara.  All rights reserved.
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
package org.pentaho.di.repository.pur;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;

public interface ISharedObjectsTransformer extends ITransformer {

  /**
   * @deprecated Shared Objects from the Repository are now accessed through a Bowl, they are no longer loaded into or
   *             saved from the Meta.
   */
  @Deprecated
  void loadSharedObjects( final RepositoryElementInterface element,
      final Map<RepositoryObjectType, List<? extends SharedObjectInterface>> sharedObjectsByType )
    throws KettleException;

  /**
   * @deprecated Shared Objects from the Repository are now accessed through a Bowl, they are no longer loaded into or
   *             saved from the Meta.
   */
  @Deprecated
  void saveSharedObjects( final RepositoryElementInterface element, final String versionComment )
    throws KettleException;
}
