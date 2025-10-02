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
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.bowl.BaseBowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.bowl.UpdateSubscriber;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.shared.RepositorySharedObjectsIO;
import org.pentaho.di.shared.SharedObjectsIO;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.IMetaStore;

import java.util.Objects;

public class RepositoryBowl extends BaseBowl implements HasRepositoryInterface{

  private static Class<?> PKG = RepositoryBowl.class;

  private final Repository repository;
  private final RepositorySharedObjectsIO sharedObjectsIO;
  // Need to hang on to this reference until 'this' goes out of scope.
  private UpdateSubscriber defaultBowlUpdater;

  public RepositoryBowl( Repository repository ) {
    this.repository = Objects.requireNonNull( repository );
    this.sharedObjectsIO = new RepositorySharedObjectsIO( repository, () ->
      getManager( SlaveServerManagementInterface.class ).getAll() );
  }

  @Override
  public <T> T getManager( Class<T> managerClass) throws KettleException {
    T manager = super.getManager( managerClass );
    synchronized( this ) {
      if ( defaultBowlUpdater == null && managerClass == ConnectionManager.class ) {
        // Ensure changes to this Bowl's VFS connections propagate back to the DefaultBowl's VFS connections
        // This allows backwards-compatible steps that use backwards-compatible APIs that use DefaultBowl to see changes
        // written through this RepositoryBowl.
        ConnectionManager mgr = (ConnectionManager) manager;
        ConnectionManager defaultMgr = DefaultBowl.getInstance().getManager( ConnectionManager.class );
        defaultBowlUpdater = defaultMgr::notifyChanged;
        // inform the Default manager about changes in our connections
        mgr.addSubscriber( defaultBowlUpdater );
      }
    }
    return manager;
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

  @Override
  public Repository getRepository() {
    return repository;
  }

  @Override
  public void setRepository(Repository repository) {
    throw new UnsupportedOperationException( " not supported " );
  }
}
