/*!
 * Copyright 2010 - 2019 Hitachi Vantara.  All rights reserved.
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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.repository.BaseRepositorySecurityProvider;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.platform.util.RepositoryPathEncoder;

import com.pentaho.di.services.FileVersioningConfiguration;
import com.pentaho.di.services.PentahoDiPlugin;

public class PurRepositorySecurityProvider extends BaseRepositorySecurityProvider implements
    RepositorySecurityProvider, IUserRoleListChangeListener, java.io.Serializable {

  private static final long serialVersionUID = -1774142691342083217L; /* EESOURCE: UPDATE SERIALVERUID */

  private PurRepository repository;
  private UserRoleListDelegate userRoleListDelegate;
  private UserRoleDelegate userRoleDelegate;
  private static final Log logger = LogFactory.getLog( PurRepositorySecurityProvider.class );
  FileVersioningConfiguration lastFileVersioningConfiguration;
  String lastFileVersioningPath;
  Date lastVersioningTime;

  public PurRepositorySecurityProvider( PurRepository repository, PurRepositoryMeta repositoryMeta, IUser user,
      ServiceManager serviceManager ) {
    super( repositoryMeta, user );
    this.repository = repository;
    this.userRoleListDelegate = new UserRoleListDelegate( repositoryMeta, user, logger, serviceManager );
    this.setUserRoleListDelegate( userRoleListDelegate );
  }

  public PurRepository getRepository() {
    return repository;
  }

  @Override
  public boolean isVersionCommentMandatory() {

    return ( ( (PurRepositoryMeta) repositoryMeta ).isVersionCommentMandatory() );
  }

  public boolean isLockingPossible() {
    return true;
  }

  public boolean isReadOnly() {
    return false;
  }

  public boolean allowsVersionComments( String fullPath ) {
    FileVersioningConfiguration versioningConfiguration;
    try {
      versioningConfiguration = callVersioningService( fullPath );
    } catch ( KettleException e ) {
      e.printStackTrace();
      return false;
    }
    return versioningConfiguration.isVersionCommentEnabled();
  }

  public String[] getUserLogins() throws KettleException {
    List<String> users = userRoleListDelegate.getAllUsers();
    if ( users != null && users.size() > 0 ) {
      String[] returnValue = new String[users.size()];
      users.toArray( returnValue );
      return returnValue;
    }
    return null;
  }

  public List<String> getAllRoles() throws KettleException {
    return userRoleListDelegate.getAllRoles();
  }

  public List<String> getAllUsers() throws KettleException {
    return userRoleListDelegate.getAllUsers();
  }

  public UserRoleDelegate getUserRoleDelegate() {
    return userRoleDelegate;
  }

  public void setUserRoleDelegate( UserRoleDelegate userRoleDelegate ) {
    this.userRoleDelegate = userRoleDelegate;
    this.userRoleDelegate.addUserRoleListChangeListener( this );
  }

  public void setUserRoleListDelegate( UserRoleListDelegate userRoleListDelegate ) {
    this.userRoleListDelegate = userRoleListDelegate;
  }

  public UserRoleListDelegate getUserRoleListDelegate() {
    return userRoleListDelegate;
  }

  public void onChange() {
    userRoleListDelegate.updateUserRoleList();
  }

  public static Log getLogger() {
    return logger;
  }

  @Override
  public boolean isVersioningEnabled( String fullPath ) {
    FileVersioningConfiguration versioningConfiguration;
    try {
      versioningConfiguration = callVersioningService( fullPath );
    } catch ( KettleException e ) {
      e.printStackTrace();
      return false;
    }
    return versioningConfiguration.isVersioningEnabled();
  }

  private synchronized FileVersioningConfiguration callVersioningService( String fullPath ) throws KettleException {
    // If we just made this web service call on this file, don't do it again
    if ( fullPath.equals( lastFileVersioningPath ) && ( new Date() ).getTime() - lastVersioningTime.getTime() < 2000 ) {
      return lastFileVersioningConfiguration;
    }

    // Do the web service call
    PurRepositoryRestService.PurRepositoryPluginApiRevision servicePort =
        (PurRepositoryRestService.PurRepositoryPluginApiRevision) repository
            .getService( PurRepositoryRestService.PurRepositoryPluginApiRevision.class );
    PentahoDiPlugin.PurRepositoryPluginApiRevision.PathIdVersioningConfiguration fileVersioningConfigurationService =
        servicePort.pathIdVersioningConfiguration( RepositoryPathEncoder.encodeRepositoryPath( fullPath ) );
    lastFileVersioningConfiguration = fileVersioningConfigurationService.getAsFileVersioningConfigurationXml();
    lastVersioningTime = new Date();
    lastFileVersioningPath = fullPath;
    return lastFileVersioningConfiguration;
  }

}
