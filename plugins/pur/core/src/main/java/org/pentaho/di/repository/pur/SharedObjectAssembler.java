/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;

/**
 * Assembles {@link SharedObjectInterface}s of type {@link T} from related repository objects.
 * 
 * @author jganoff
 * 
 * @param <T>
 *          Type of {@link SharedObjectInterface} this adapter assembles
 */
public interface SharedObjectAssembler<T extends SharedObjectInterface> {
  /**
   * Assemble a shared object of type {@link T}.
   * 
   * @param file
   *          File reference for a shared object.
   * @param data
   *          Data representing the shared object identified by {@link file}.
   * @param version
   *          Version this shared object was fetched at.
   * 
   * @return Shared object assembled from the provided parameters.
   */
  public T assemble( RepositoryFile file, NodeRepositoryFileData data, VersionSummary version ) throws KettleException;
}
