/*!
* Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
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
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.repository.pur.model.RepositoryLock;
import org.pentaho.di.ui.repository.pur.services.ILockService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

public class UnifiedRepositoryLockService implements ILockService {
  private final IUnifiedRepository pur;

  public UnifiedRepositoryLockService( IUnifiedRepository pur ) {
    this.pur = pur;
  }

  protected void lockFileById( final ObjectId id, final String message ) throws KettleException {
    pur.lockFile( id.getId(), message );
  }

  public RepositoryLock getLock( final RepositoryFile file ) throws KettleException {
    if ( file.isLocked() ) {
      return new RepositoryLock( new StringObjectId( file.getId().toString() ), file.getLockMessage(), file
          .getLockOwner(), file.getLockOwner(), file.getLockDate() );
    } else {
      return null;
    }
  }

  protected RepositoryLock getLockById( final ObjectId id ) throws KettleException {
    try {
      RepositoryFile file = pur.getFileById( id.getId() );
      return getLock( file );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to get lock for object with id [" + id + "]", e );
    }
  }

  protected void unlockFileById( ObjectId id ) throws KettleException {
    pur.unlockFile( id.getId() );
  }

  @Override
  public RepositoryLock lockJob( final ObjectId idJob, final String message ) throws KettleException {
    lockFileById( idJob, message );
    return getLockById( idJob );
  }

  @Override
  public void unlockJob( ObjectId idJob ) throws KettleException {
    unlockFileById( idJob );
  }

  @Override
  public RepositoryLock getJobLock( ObjectId idJob ) throws KettleException {
    return getLockById( idJob );
  }

  @Override
  public RepositoryLock lockTransformation( final ObjectId idTransformation, final String message )
    throws KettleException {
    lockFileById( idTransformation, message );
    return getLockById( idTransformation );
  }

  @Override
  public void unlockTransformation( ObjectId idTransformation ) throws KettleException {
    unlockFileById( idTransformation );
  }

  @Override
  public RepositoryLock getTransformationLock( ObjectId idTransformation ) throws KettleException {
    return getLockById( idTransformation );
  }

  @Override
  public boolean canUnlockFileById( final ObjectId id ) {
    return pur.canUnlockFile( id.getId() );
  }

}
