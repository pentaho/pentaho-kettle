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
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.ui.repository.pur.services.IRevisionService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.VersionSummary;

public class UnifiedRepositoryRevisionService implements IRevisionService {
  private final IUnifiedRepository unifiedRepository;
  private final RootRef rootRef;

  public UnifiedRepositoryRevisionService( IUnifiedRepository unifiedRepository, RootRef rootRef ) {
    this.unifiedRepository = unifiedRepository;
    this.rootRef = rootRef;
  }

  @Override
  public List<ObjectRevision> getRevisions( final RepositoryElementInterface element ) throws KettleException {
    return getRevisions( element.getObjectId() );
  }

  @Override
  public List<ObjectRevision> getRevisions( ObjectId fileId ) throws KettleException {
    String absPath = null;
    try {
      List<ObjectRevision> versions = new ArrayList<ObjectRevision>();
      List<VersionSummary> versionSummaries = unifiedRepository.getVersionSummaries( fileId.getId() );
      for ( VersionSummary versionSummary : versionSummaries ) {
        versions.add( new PurObjectRevision( versionSummary.getId(), versionSummary.getAuthor(), versionSummary
            .getDate(), versionSummary.getMessage() ) );
      }
      return versions;
    } catch ( Exception e ) {
      throw new KettleException( "Could not retrieve version history of object with path [" + absPath + "]", e );
    }
  }

  @Override
  public void restoreJob( ObjectId id_job, String revision, String versionComment ) throws KettleException {
    unifiedRepository.restoreFileAtVersion( id_job.getId(), revision, versionComment );
    rootRef.clearRef();
  }

  @Override
  public void restoreTransformation( ObjectId id_transformation, String revision, String versionComment )
    throws KettleException {
    unifiedRepository.restoreFileAtVersion( id_transformation.getId(), revision, versionComment );
    rootRef.clearRef();
  }

}
