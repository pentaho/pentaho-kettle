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

        @Override
        public String getOwner() {
          return file.getCreatorId();
        }
      } );
    }
    return trash;
  }

}
