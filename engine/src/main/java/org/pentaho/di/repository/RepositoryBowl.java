/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2025 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.repository;

import org.pentaho.di.cluster.SlaveServerManagementInterface;
import org.pentaho.di.core.bowl.BaseBowl;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.shared.RepositorySharedObjectsIO;
import org.pentaho.di.shared.SharedObjectsIO;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.IMetaStore;

import java.util.Objects;

public class RepositoryBowl extends BaseBowl {

  private static Class<?> PKG = RepositoryBowl.class;

  private final Repository repository;
  private final RepositorySharedObjectsIO sharedObjectsIO;

  public RepositoryBowl( Repository repository ) {
    this.repository = Objects.requireNonNull( repository );
    this.sharedObjectsIO = new RepositorySharedObjectsIO( repository, () ->
      getManager( SlaveServerManagementInterface.class ).getAll() );
  }

  @Override
  public IMetaStore getMetastore() throws MetaStoreException {
    return repository.getRepositoryMetaStore();
  }

  @Override
  public SharedObjectsIO getSharedObjectsIO() {
    return sharedObjectsIO;
  }

  @Override
  public VariableSpace getADefaultVariableSpace() {
    VariableSpace space = new Variables();
    space.initializeVariablesFrom( null );
    return space;
  }

  @Override
  public String getLevelDisplayName() {
    return BaseMessages.getString(PKG, "Repository.Level.Name");
  }

  public Repository getRepository() {
    return repository;
  }
}
