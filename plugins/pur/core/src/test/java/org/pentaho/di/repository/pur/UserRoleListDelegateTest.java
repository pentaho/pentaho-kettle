/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.security.userroledao.UserRoleInfo;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.security.userrole.ws.IUserRoleListWebService;

public class UserRoleListDelegateTest implements java.io.Serializable {

  static final long serialVersionUID = -125535373810768433L; /* EESOURCE: UPDATE SERIALVERUID */

  UserRoleListDelegate listDelegate;
  IUserRoleListWebService service;
  static List<String> roles;
  static List<String> users;

  @Before
  public void init() {
    listDelegate = new UserRoleListDelegate();
    roles = new ArrayList<String>();
    roles.add( "ROLE_DEV" ); //$NON-NLS-1$
    roles.add( "ROLE_ADMIN" ); //$NON-NLS-1$
    roles.add( "ROLE_DEVMGR" ); //$NON-NLS-1$
    roles.add( "ROLE_CEO" ); //$NON-NLS-1$
    roles.add( "ROLE_CTO" ); //$NON-NLS-1$
    roles.add( "ROLE_AUTHENTICATED" ); //$NON-NLS-1$
    roles.add( "ROLE_IS" ); //$NON-NLS-1$
    users = new ArrayList<String>();
    users.add( "pat" ); //$NON-NLS-1$
    users.add( "tiffany" ); //$NON-NLS-1$
    users.add( "joe" ); //$NON-NLS-1$
    users.add( "suzy" ); //$NON-NLS-1$
    service = new UserDetailsRoleListService();
    listDelegate.setUserDetailsRoleListWebService( service );
    listDelegate.setUserRoleInfo( service.getUserRoleInfo() );
  }

  @Test
  public void testService() throws Exception {
    Assert.assertEquals( listDelegate.getAllUsers().size(), 4 );
    Assert.assertEquals( listDelegate.getAllRoles().size(), 7 );
    Assert.assertEquals( listDelegate.getUserRoleInfo().getRoles().size(), 7 );
    Assert.assertEquals( listDelegate.getUserRoleInfo().getUsers().size(), 4 );
  }

  public static class UserDetailsRoleListService implements IUserRoleListWebService {

    public List<String> getAllRoles() {
      return roles;
    }

    public List<String> getAllUsers() {

      return users;
    }

    public UserRoleInfo getUserRoleInfo() {
      UserRoleInfo info = new UserRoleInfo();
      info.setRoles( roles );
      info.setUsers( users );
      return info;
    }

    @Override
    public List<String> getAllRolesForTenant( Tenant arg0 ) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public List<String> getAllUsersForTenant( Tenant arg0 ) {
      // TODO Auto-generated method stub
      return null;
    }

  }
}
