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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryCommonValidations;
import org.pentaho.di.repository.RepositorySecurityUserValidator;
import org.pentaho.di.repository.pur.model.EERoleInfo;
import org.pentaho.di.repository.pur.model.EEUserInfo;
import org.pentaho.di.repository.pur.model.IRole;
import org.pentaho.di.ui.repository.pur.services.IRoleSupportSecurityManager;
import org.pentaho.di.ui.repository.pur.services.RepositorySecurityRoleValidator;
import org.pentaho.platform.security.userroledao.ws.UserRoleException;

import java.util.List;

public class PurRepositorySecurityManager implements IRoleSupportSecurityManager, IUserRoleListChangeListener,
    java.io.Serializable, RepositorySecurityUserValidator, RepositorySecurityRoleValidator {

  private static final long serialVersionUID = 6820830385234412904L; /* EESOURCE: UPDATE SERIALVERUID */

  private PurRepository repository;
  private UserRoleDelegate userRoleDelegate;
  private static final Log logger = LogFactory.getLog( UserRoleDelegate.class );

  public PurRepositorySecurityManager( PurRepository repository, PurRepositoryMeta repositoryMeta, IUser user,
      ServiceManager serviceManager ) {
    this.repository = repository;
    this.userRoleDelegate = new UserRoleDelegate( this, repositoryMeta, user, logger, serviceManager );
    userRoleDelegate.addUserRoleListChangeListener( this );
    this.setUserRoleDelegate( userRoleDelegate );
  }

  public UserRoleDelegate getUserRoleDelegate() {
    return userRoleDelegate;
  }

  public void setUserRoleDelegate( UserRoleDelegate userRoleDelegate ) {
    this.userRoleDelegate = userRoleDelegate;
  }

  public PurRepository getRepository() {
    return repository;
  }

  public boolean supportsMetadata() {
    return true;
  }

  public boolean supportsRevisions() {
    return true;
  }

  public boolean supportsUsers() {
    return true;
  }

  public void delUser( ObjectId id_user ) throws KettleException {
  }

  public ObjectId getUserID( String login ) throws KettleException {
    return null;
  }

  public ObjectId[] getUserIDs() throws KettleException {
    return null;
  }

  public IUser loadUserInfo( String login ) throws KettleException {
    // Create a UserInfo object
    IUser user = constructUser();
    user.setLogin( login );
    user.setName( login );
    return user;
  }

  public IUser loadUserInfo( String login, String password ) throws KettleException {
    // Create a UserInfo object
    IUser user = constructUser();
    user.setLogin( login );
    user.setPassword( password );
    user.setName( login );
    return user;
  }

  public void renameUser( ObjectId id_user, String newname ) throws KettleException {
  }

  public void saveUserInfo( IUser user ) throws KettleException {
    normalizeUserInfo( user );
    if ( !validateUserInfo( user ) ) {
      throw new KettleException( BaseMessages.getString( PurRepositorySecurityManager.class,
          "PurRepositorySecurityManager.ERROR_0001_INVALID_NAME" ) );
    }
    userRoleDelegate.createUser( user );
  }

  @Override
  public boolean validateUserInfo( IUser user ) {
    return RepositoryCommonValidations.checkUserInfo( user );
  }

  @Override
  public void normalizeUserInfo( IUser user ) {
    RepositoryCommonValidations.normalizeUserInfo( user );
  }

  public void createRole( IRole newRole ) throws KettleException {
    normalizeRoleInfo( newRole );
    if ( !validateRoleInfo( newRole ) ) {
      throw new KettleException( BaseMessages.getString( PurRepositorySecurityManager.class,
          "PurRepositorySecurityManager.ERROR_0001_INVALID_NAME" ) );
    }
    userRoleDelegate.createRole( newRole );
  }

  @Override
  public boolean validateRoleInfo( IRole role ) {
    return StringUtils.isNotBlank( role.getName() );
  }

  @Override
  public void normalizeRoleInfo( IRole role ) {
    role.setName( role.getName().trim() );
  }

  public void deleteRoles( List<IRole> roles ) throws KettleException {
    userRoleDelegate.deleteRoles( roles );
  }

  public void deleteUsers( List<IUser> users ) throws KettleException {
    userRoleDelegate.deleteUsers( users );
  }

  public IRole getRole( String name ) throws KettleException {
    return userRoleDelegate.getRole( name );
  }

  public List<IRole> getRoles() throws KettleException {
    return userRoleDelegate.getRoles();
  }

  public List<IRole> getDefaultRoles() throws KettleException {
    return userRoleDelegate.getDefaultRoles();
  }

  public void updateRole( IRole role ) throws KettleException {
    userRoleDelegate.updateRole( role );
  }

  public void updateUser( IUser user ) throws KettleException {
    userRoleDelegate.updateUser( user );
  }

  public void delUser( String name ) throws KettleException {
    userRoleDelegate.deleteUser( name );

  }

  public void deleteRole( String name ) throws KettleException {
    userRoleDelegate.deleteRole( name );

  }

  public List<IUser> getUsers() throws KettleException {
    return userRoleDelegate.getUsers();
  }

  public void setRoles( List<IRole> roles ) throws KettleException {
    userRoleDelegate.setRoles( roles );

  }

  public void setUsers( List<IUser> users ) throws KettleException {
    userRoleDelegate.setUsers( users );
  }

  public IRole constructRole() throws KettleException {
    return new EERoleInfo();
  }

  public IUser constructUser() throws KettleException {
    return new EEUserInfo();
  }

  public void onChange() {
    try {
      userRoleDelegate.updateUserRoleInfo();
    } catch ( UserRoleException e ) {
      e.printStackTrace();
    }
  }

  public static Log getLogger() {
    return logger;
  }

  public boolean isManaged() throws KettleException {
    return userRoleDelegate.isManaged();
  }

}
