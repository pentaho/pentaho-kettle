/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.repository.filerep;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.repository.RepositoryCapabilities;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.repository.UserInfo;

public class KettleFileRepositorySecurityProvider implements RepositorySecurityProvider {

  private RepositoryMeta repositoryMeta;
  private RepositoryCapabilities capabilities;

  public KettleFileRepositorySecurityProvider( RepositoryMeta repositoryMeta ) {
    this.repositoryMeta = repositoryMeta;
    this.capabilities = repositoryMeta.getRepositoryCapabilities();
  }

  public UserInfo getUserInfo() {
    return null;
  }

  public RepositoryMeta getRepositoryMeta() {
    return repositoryMeta;
  }

  public void validateAction( RepositoryOperation... operations ) throws KettleException, KettleSecurityException {

    for ( RepositoryOperation operation : operations ) {
      switch ( operation ) {
        case READ_TRANSFORMATION:
          break;
        case MODIFY_TRANSFORMATION:
          if ( capabilities.isReadOnly() ) {
            throw new KettleException( operation + " : repository is read-only" );
          }
          break;
        case DELETE_TRANSFORMATION:
          if ( capabilities.isReadOnly() ) {
            throw new KettleException( operation + " : repository is read-only" );
          }
          break;
        case EXECUTE_TRANSFORMATION:
          break;
        case LOCK_TRANSFORMATION:
          break;

        case READ_JOB:
          break;
        case MODIFY_JOB:
          if ( capabilities.isReadOnly() ) {
            throw new KettleException( operation + " : repository is read-only" );
          }
          break;
        case DELETE_JOB:
          if ( capabilities.isReadOnly() ) {
            throw new KettleException( operation + " : repository is read-only" );
          }
          break;
        case EXECUTE_JOB:
          break;
        case LOCK_JOB:
          break;

        case MODIFY_DATABASE:
          if ( capabilities.isReadOnly() ) {
            throw new KettleException( operation + " : repository is read-only" );
          }
          break;
        case DELETE_DATABASE:
          if ( capabilities.isReadOnly() ) {
            throw new KettleException( operation + " : repository is read-only" );
          }
          break;
        case EXPLORE_DATABASE:
          break;

        case MODIFY_SLAVE_SERVER:
        case MODIFY_CLUSTER_SCHEMA:
        case MODIFY_PARTITION_SCHEMA:
          if ( capabilities.isReadOnly() ) {
            throw new KettleException( operation + " : repository is read-only" );
          }
          break;
        case DELETE_SLAVE_SERVER:
        case DELETE_CLUSTER_SCHEMA:
        case DELETE_PARTITION_SCHEMA:
          if ( capabilities.isReadOnly() ) {
            throw new KettleException( operation + " : repository is read-only" );
          }
          break;

        default:
          throw new KettleException( "Operation [" + operation + "] is unknown to the security handler." );

      }
    }
  }

  public boolean isReadOnly() {
    return capabilities.isReadOnly();
  }

  public boolean isLockingPossible() {
    return capabilities.supportsLocking();
  }

  public boolean allowsVersionComments( String fullPath ) {
    return false;
  }

  public boolean isVersionCommentMandatory() {
    return false;
  }

  public List<String> getAllRoles() throws KettleException {
    throw new UnsupportedOperationException();
  }

  public List<String> getAllUsers() throws KettleException {
    throw new UnsupportedOperationException();
  }

  public String[] getUserLogins() throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isVersioningEnabled( String fullPath ) {
    return false;
  }
}
