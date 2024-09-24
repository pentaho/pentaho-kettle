/*!
 * Copyright 2010 - 2018 Hitachi Vantara.  All rights reserved.
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
package com.pentaho.di.purge;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.ui.repository.pur.services.IPurgeService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest.FILES_TYPE_FILTER;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileTreeDto;

/**
 * Created by tkafalas 7/14/14.
 */
public class UnifiedRepositoryPurgeService implements IPurgeService {
  private final IUnifiedRepository unifiedRepository;
  public static DefaultUnifiedRepositoryWebService repoWs; // Scoped public only so that test class can set mock
  private static String[] sharedObjectFolders = new String[] { "/etc/pdi/databases", "/etc/pdi/clusterSchemas",
    "/etc/pdi/partitionSchemas", "/etc/pdi/slaveServers" };

  public UnifiedRepositoryPurgeService( IUnifiedRepository unifiedRepository ) {
    this.unifiedRepository = unifiedRepository;
  }

  @Override
  public void deleteVersionsBeforeDate( RepositoryElementInterface element, Date beforeDate ) throws KettleException {
    try {
      Serializable fileId = element.getObjectId().getId();
      deleteVersionsBeforeDate( fileId, beforeDate );
    } catch ( Exception e ) {
      processDeleteException( e );
    }
  }

  @Override
  public void deleteVersionsBeforeDate( Serializable fileId, Date beforeDate ) {
    List<VersionSummary> versionList = unifiedRepository.getVersionSummaries( fileId );
    int listSize = versionList.size();
    int removedCount = 0;
    for ( VersionSummary versionSummary : versionList ) {
      if ( listSize - removedCount <= 1 ) {
        break; // Don't delete the last instance of this file.
      }
      if ( versionSummary.getDate().before( beforeDate ) ) {
        Serializable versionId = versionSummary.getId();
        getLogger().debug( "removing version " + versionId.toString() );
        unifiedRepository.deleteFileAtVersion( fileId, versionId );
        removedCount++;
      }
    }
  }

  @Override
  public void deleteAllVersions( RepositoryElementInterface element ) throws KettleException {
    Serializable fileId = element.getObjectId().getId();
    deleteAllVersions( fileId );
  }

  @Override
  public void deleteAllVersions( Serializable fileId ) {
    keepNumberOfVersions( fileId, 1 );
  }

  @Override
  public void deleteVersion( RepositoryElementInterface element, String versionId ) throws KettleException {
    String fileId = element.getObjectId().getId();
    deleteVersion( fileId, versionId );
  }

  @Override
  public void deleteVersion( Serializable fileId, Serializable versionId ) {
    unifiedRepository.deleteFileAtVersion( fileId, versionId );
  }

  @Override
  public void keepNumberOfVersions( RepositoryElementInterface element, int versionCount ) throws KettleException {
    String fileId = element.getObjectId().getId();
    keepNumberOfVersions( fileId, versionCount );
  }

  @Override
  public void keepNumberOfVersions( Serializable fileId, int versionCount ) {
    List<VersionSummary> versionList = unifiedRepository.getVersionSummaries( fileId );
    int i = 0;
    int listSize = versionList.size();
    if ( listSize > versionCount ) {
      getLogger().info( "version count: removing versions" );
    }
    for ( VersionSummary versionSummary : versionList ) {
      if ( i < listSize - versionCount ) {
        Serializable versionId = versionSummary.getId();
        getLogger().debug( "removing version " + versionId.toString() );
        unifiedRepository.deleteFileAtVersion( fileId, versionId );
        i++;
      } else {
        break;
      }
    }
  }

  private void processDeleteException( Throwable e ) throws KettleException {
    throw new KettleException( "Unable to complete revision deletion", e );
  }

  public void doDeleteRevisions( PurgeUtilitySpecification purgeSpecification ) throws PurgeDeletionException {
    if ( purgeSpecification != null ) {
      getLogger().setCurrentFilePath( purgeSpecification.getPath() );
      logConfiguration( purgeSpecification );
      if ( purgeSpecification.getPath() != null && !purgeSpecification.getPath().isEmpty() ) {
        processRevisionDeletion( purgeSpecification );
      }

      // Now do shared objects if required
      if ( purgeSpecification.isSharedObjects() ) {
        if ( purgeSpecification.isPurgeFiles() ) {
          for ( String sharedObjectpath : sharedObjectFolders ) {
            purgeSpecification.fileFilter = "*";
            purgeSpecification.setPath( sharedObjectpath );
            processRevisionDeletion( purgeSpecification );
          }
        } else {
          throw new PurgeDeletionException( "Must purge files before shared objects" );
        }
      }
    }
  }

  private void processRevisionDeletion( PurgeUtilitySpecification purgeSpecification ) throws PurgeDeletionException {

    RepositoryRequest repositoryRequest =
        new RepositoryRequest( purgeSpecification.getPath(), true, -1, purgeSpecification.getFileFilter() );
    repositoryRequest.setTypes( FILES_TYPE_FILTER.FILES_FOLDERS );
    repositoryRequest.setIncludeMemberSet( new HashSet<String>( Arrays.asList( new String[] { "name", "id", "folder",
      "path", "versioned", "versionId", "locked" } ) ) );

    getLogger().debug( "Creating file list" );
    RepositoryFileTreeDto tree = getRepoWs().getTreeFromRequest( repositoryRequest );

    processPurgeForTree( tree, purgeSpecification );
  }

  private void processPurgeForTree( RepositoryFileTreeDto tree, PurgeUtilitySpecification purgeSpecification ) {

    for ( RepositoryFileTreeDto child : tree.getChildren() ) {
      try {
        if ( !child.getChildren().isEmpty() ) {
          processPurgeForTree( child, purgeSpecification );
        }
        RepositoryFileDto file = child.getFile();
        getLogger().setCurrentFilePath( file.getPath() );
        if ( file.isVersioned() ) {
          if ( purgeSpecification.isPurgeFiles() ) {
            getLogger().info( "Purge File" );
            keepNumberOfVersions( file.getId(), 1 ); // the latest version will be removed with deleteFileWithPermanentFlag
            getRepoWs().deleteFileWithPermanentFlag( file.getId(), true, "purge utility" );
          } else if ( purgeSpecification.isPurgeRevisions() ) {
            getLogger().info( "Purging Revisions" );
            deleteAllVersions( file.getId() );
          } else {
            if ( purgeSpecification.getBeforeDate() != null ) {
              getLogger().info( "Checking/purging by Revision date" );
              deleteVersionsBeforeDate( file.getId(), purgeSpecification.getBeforeDate() );
            }
            if ( purgeSpecification.getVersionCount() >= 0 ) {
              getLogger().info( "Checking/purging by number of Revisions" );
              keepNumberOfVersions( file.getId(), purgeSpecification.getVersionCount() );
            }
          }
        }
      } catch ( Exception e ) {
        getLogger().error( e );
      }
    }
  }

  public static DefaultUnifiedRepositoryWebService getRepoWs() {
    if ( repoWs == null ) {
      repoWs = new DefaultUnifiedRepositoryWebService();
    }
    return repoWs;
  }

  /**
   * Get the formal logger ( or create one that just logs regularly )
   */
  private PurgeUtilityLogger getLogger() {
    return PurgeUtilityLogger.getPurgeUtilityLogger();
  }

  private void logConfiguration( PurgeUtilitySpecification purgeSpecification ) {
    if ( purgeSpecification.getFileFilter() != null && purgeSpecification.getFileFilter().isEmpty() ) {
      getLogger().info( "Configure File Filter" + purgeSpecification.getFileFilter() );
    }
    if ( purgeSpecification.isPurgeFiles() ) {
      getLogger().info( "Configure PurgeAllFiles: true" );
    }
    if ( purgeSpecification.isPurgeRevisions() ) {
      getLogger().info( "Configure PurgeAllRevisions: true" );
    }
    if ( purgeSpecification.isSharedObjects() ) {
      getLogger().info( "Configure ShareObjects: true" );
    }
    if ( purgeSpecification.getBeforeDate() != null ) {
      getLogger().info( "Configure purgeBeforeDate: " + purgeSpecification.getBeforeDate().toString() );
    }
    if ( purgeSpecification.getVersionCount() != -1 ) {
      getLogger().info( "Configure versionCount: " + purgeSpecification.getVersionCount() );
    }
  }
}
