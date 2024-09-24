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

import java.util.List;

import org.apache.commons.logging.Log;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.IUser;
import org.pentaho.platform.api.engine.security.userroledao.UserRoleInfo;
import org.pentaho.platform.security.userrole.ws.IUserRoleListWebService;

public class UserRoleListDelegate implements java.io.Serializable {

  private static final Class PKG = UserRoleListDelegate.class;

  private static final long serialVersionUID = -2895663865550206386L; /* EESOURCE: UPDATE SERIALVERUID */
  IUserRoleListWebService userDetailsRoleListWebService;
  UserRoleInfo userRoleInfo;
  Log logger;

  public UserRoleListDelegate() {

  }

  public UserRoleListDelegate( PurRepositoryMeta repositoryMeta, IUser userInfo, Log logger,
      ServiceManager serviceManager ) {
    try {
      this.logger = logger;
      userDetailsRoleListWebService =
          serviceManager.createService( userInfo.getLogin(), userInfo.getPassword(), IUserRoleListWebService.class );
      updateUserRoleList();
    } catch ( Exception e ) {
      this.logger.error( BaseMessages.getString( PKG,
        "UserRoleListDelegate.ERROR_0001_UNABLE_TO_INITIALIZE_USER_ROLE_LIST_WEBSVC" ), e ); //$NON-NLS-1$
    }

  }

  public List<String> getAllRoles() throws KettleException {
    return userRoleInfo.getRoles();
  }

  public List<String> getAllUsers() throws KettleException {
    return userRoleInfo.getUsers();
  }

  public void updateUserRoleList() {
    userRoleInfo = userDetailsRoleListWebService.getUserRoleInfo();
  }

  public IUserRoleListWebService getUserDetailsRoleListWebService() {
    return userDetailsRoleListWebService;
  }

  public void setUserDetailsRoleListWebService( IUserRoleListWebService userDetailsRoleListWebService ) {
    this.userDetailsRoleListWebService = userDetailsRoleListWebService;
  }

  public UserRoleInfo getUserRoleInfo() {
    return userRoleInfo;
  }

  public void setUserRoleInfo( UserRoleInfo userRoleInfo ) {
    this.userRoleInfo = userRoleInfo;
  }

}
