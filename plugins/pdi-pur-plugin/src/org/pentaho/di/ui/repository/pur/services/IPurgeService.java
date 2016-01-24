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
package org.pentaho.di.ui.repository.pur.services;

import java.io.Serializable;
import java.util.Date;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.RepositoryElementInterface;

import com.pentaho.di.purge.PurgeUtilitySpecification;
import com.pentaho.di.purge.PurgeDeletionException;

public interface IPurgeService {
  /**
   * Delete all revisions of the <code>RepositoryElementInterface</code> dated before the specified date.
   * 
   * @param element
   *          The element to be pruned
   * @param beforeDate
   *          deleted revisions that occurred prior to this date
   * @throws KettleException
   */
  public void deleteVersionsBeforeDate( RepositoryElementInterface element, Date beforeDate ) throws KettleException;

  /**
   * Delete all revisions of the fileId dated before the specified date.
   * 
   * @param fileId
   *          The id of the Node to be pruned.
   * @param beforeDate
   *          deleted revisions that occurred prior to this date
   * @throws KettleException
   */
  public void deleteVersionsBeforeDate( Serializable fileId, Date beforeDate );

  /**
   * Delete all revisions of the <code>RepositoryElementInterface</code>
   * 
   * @param element
   *          The element whose revision history will be purged.
   * @throws KettleException
   */
  public void deleteAllVersions( RepositoryElementInterface element ) throws KettleException;

  /**
   * Delete all revisions of the repository file
   * 
   * @param fileId
   *          The id of the repository file whose revision history will be purged.
   * @throws KettleException
   */
  public void deleteAllVersions( Serializable fileId );

  /**
   * Delete specified revision of the <code>RepositoryElementInterface</code>
   * 
   * @param element
   *          The element owning the revision to be purged.
   * @param versionId
   *          The version Id to be deleted
   * @throws KettleException
   */
  public void deleteVersion( RepositoryElementInterface element, String versionId ) throws KettleException;

  /**
   * Delete specified revision of the repository file
   * 
   * @param fileId
   *          The id of the repository file owning the revision history to be purged.
   * @param versionId
   *          The version Id to be deleted
   * @throws KettleException
   */
  public void deleteVersion( Serializable fileId, Serializable versionId );

  /**
   * Delete all but the nth most recent revisions from the revision history
   * 
   * @param element
   *          The element owning the revisions to be pruned.
   * @param versionCount
   *          The number of revisions to keep. Note that if the versionCount = 0 then all traces of the file will be
   *          removed.
   * @throws KettleException
   */
  public void keepNumberOfVersions( RepositoryElementInterface element, int versionCount ) throws KettleException;

  /**
   * Delete all but the nth most recent revisions from the revision history
   * 
   * @param fileId
   *          The fileId owning the revisions to be pruned.
   * @param versionCount
   *          The number of revisions to keep. Note that if the versionCount = 0 then all traces of the file will be
   *          removed.
   * @throws KettleException
   */
  public void keepNumberOfVersions( Serializable fileId, int versionCount );

  /**
   * Implementation of the rest call to delete revisions
   * 
   * @param path
   *          The path to delete. Required unless only the shared objects are to be processed.
   * @param filterMap
   *          Allowable keys in filter map are:<br>
   *          <ul>
   *          <li>deleteAll { if true, all versions will be deleted and no other filter items will be checked }</li>
   *          <li>versionCount {Number of Versions to keep}</li>
   *          <li>beforeDate {Delete revisions before this date MM/DD/YYYY}</li>
   *          <li>sharedObjects { if true, shared objects will processed along with the provided path, if any.</li>
   *          <li>fileFilter { Works the same as the filter in the tree rest call }</li>
   * 
   */
  public void doDeleteRevisions( PurgeUtilitySpecification purgeSpecification ) throws PurgeDeletionException;
}
