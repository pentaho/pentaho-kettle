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
