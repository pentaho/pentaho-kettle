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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.ui.repository.pur.services.ITrashService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

public class UnifiedRepositoryTrashService implements ITrashService {
  private final IUnifiedRepository pur;
  private final RootRef rootRef;

  public UnifiedRepositoryTrashService( IUnifiedRepository pur, RootRef rootRef ) {
    this.pur = pur;
    this.rootRef = rootRef;
  }

  @Override
  public void delete( final List<ObjectId> ids ) throws KettleException {
    for ( ObjectId id : ids ) {
      pur.deleteFile( id.getId(), true, null );
    }
    rootRef.clearRef();
  }

  @Override
  public void undelete( final List<ObjectId> ids ) throws KettleException {
    for ( ObjectId id : ids ) {
      pur.undeleteFile( id.getId(), null );
    }
    rootRef.clearRef();
  }

  @Override
  public List<IDeletedObject> getTrash() throws KettleException {
    List<IDeletedObject> trash = new ArrayList<IDeletedObject>();
    List<RepositoryFile> deletedChildren = pur.getDeletedFiles();

    for ( final RepositoryFile file : deletedChildren ) {
      trash.add( new IDeletedObject() {

        @Override
        public String getOriginalParentPath() {
          return file.getOriginalParentFolderPath();
        }

        @Override
        public Date getDeletedDate() {
          return file.getDeletedDate();
        }

        @Override
        public String getType() {
          if ( file.getName().endsWith( RepositoryObjectType.TRANSFORMATION.getExtension() ) ) {
            return RepositoryObjectType.TRANSFORMATION.name();
          } else if ( file.getName().endsWith( RepositoryObjectType.JOB.getExtension() ) ) {
            return RepositoryObjectType.JOB.name();
          } else {
            return null;
          }
        }

        @Override
        public ObjectId getId() {
          return new StringObjectId( file.getId().toString() );
        }

        @Override
        public String getName() {
          return file.getTitle();
        }

      } );
    }

    return trash;
  }
}
