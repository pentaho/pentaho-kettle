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
import java.util.Map;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.pur.model.AbsRoleInfo;
import org.pentaho.di.repository.pur.model.IAbsRole;
import org.pentaho.di.repository.pur.model.IRole;
import org.pentaho.di.ui.repository.pur.services.IAbsSecurityManager;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;
import org.pentaho.platform.security.policy.rolebased.ws.IRoleAuthorizationPolicyRoleBindingDaoWebService;

public class AbsSecurityManager extends PurRepositorySecurityManager implements IAbsSecurityManager,
    java.io.Serializable {

  private static final long serialVersionUID = -7472721270945456826L; /* EESOURCE: UPDATE SERIALVERUID */

  private IRoleAuthorizationPolicyRoleBindingDaoWebService authorizationPolicyRoleBindingService = null;

  private RoleBindingStruct roleBindingStruct = null;

  public AbsSecurityManager( PurRepository repository, PurRepositoryMeta repositoryMeta, IUser userInfo,
      ServiceManager serviceManager ) {
    super( repository, repositoryMeta, userInfo, serviceManager );
    try {
      authorizationPolicyRoleBindingService =
          serviceManager.createService( userInfo.getLogin(), userInfo.getPassword(),
              IRoleAuthorizationPolicyRoleBindingDaoWebService.class );
      if ( authorizationPolicyRoleBindingService == null ) {
        getLogger().error(
            BaseMessages.getString( AbsSecurityManager.class,
                "AbsSecurityManager.ERROR_0001_UNABLE_TO_INITIALIZE_ROLE_BINDING_WEBSVC" ) ); //$NON-NLS-1$
      }
    } catch ( Exception e ) {
      getLogger().error(
          BaseMessages.getString( AbsSecurityManager.class,
              "AbsSecurityManager.ERROR_0001_UNABLE_TO_INITIALIZE_ROLE_BINDING_WEBSVC" ), e ); //$NON-NLS-1$
    }
  }

  public void initialize( String locale ) throws KettleException {
    if ( authorizationPolicyRoleBindingService != null ) {
      try {
        roleBindingStruct = authorizationPolicyRoleBindingService.getRoleBindingStruct( locale );
      } catch ( Exception e ) {
        throw new KettleException( BaseMessages.getString( AbsSecurityManager.class,
            "AbsSecurityManager.ERROR_0002_UNABLE_TO_GET_LOGICAL_ROLES" ), e ); //$NON-NLS-1$
      }
    } else {
      throw new KettleException( BaseMessages.getString( AbsSecurityManager.class,
          "AbsSecurityManager.ERROR_0005_INSUFFICIENT_PRIVELEGES" ) ); //$NON-NLS-1$
    }
  }

  @Override
  public IRole getRole( String name ) throws KettleException {
    IRole role = super.getRole( name );
    if ( role instanceof IAbsRole ) {
      List<String> logicalRoles = getLogicalRoles( role.getName() );
      if ( logicalRoles != null && logicalRoles.size() > 0 ) {
        ( (IAbsRole) role ).setLogicalRoles( logicalRoles );
      }
    }
    return role;
  }

  @Override
  public List<IRole> getRoles() throws KettleException {
    List<IRole> roles = super.getRoles();
    for ( IRole role : roles ) {
      if ( role instanceof IAbsRole ) {
        List<String> logicalRoles = getLogicalRoles( role.getName() );
        if ( logicalRoles != null && logicalRoles.size() > 0 ) {
          ( (IAbsRole) role ).setLogicalRoles( logicalRoles );
        }
      }
    }
    return roles;
  }

  @Override
  public IRole constructRole() throws KettleException {
    return new AbsRoleInfo();
  }

  public List<String> getLocalizedLogicalRoles( String runtimeRole, String locale ) throws KettleException {
    if ( authorizationPolicyRoleBindingService != null ) {
      List<String> localizedLogicalRoles = new ArrayList<String>();
      if ( roleBindingStruct != null && roleBindingStruct.logicalRoleNameMap != null ) {
        List<String> logicalRoles = getLogicalRoles( runtimeRole );
        for ( String logicalRole : logicalRoles ) {
          localizedLogicalRoles.add( roleBindingStruct.logicalRoleNameMap.get( logicalRole ) );
        }
      } else {
        throw new KettleException( BaseMessages.getString( AbsSecurityManager.class,
            "AbsSecurityManager.ERROR_0003_UNABLE_TO_ACCESS_ROLE_BINDING_WEBSVC" ) ); //$NON-NLS-1$
      }
      return localizedLogicalRoles;
    } else {
      throw new KettleException( BaseMessages.getString( AbsSecurityManager.class,
          "AbsSecurityManager.ERROR_0005_INSUFFICIENT_PRIVELEGES" ) ); //$NON-NLS-1$
    }
  }

  public List<String> getLogicalRoles( String runtimeRole ) throws KettleException {
    if ( authorizationPolicyRoleBindingService != null ) {
      if ( roleBindingStruct != null && roleBindingStruct.bindingMap != null
          && roleBindingStruct.bindingMap.containsKey( runtimeRole ) ) {
        return roleBindingStruct.bindingMap.get( runtimeRole );
      }
      return null;
    } else {
      throw new KettleException( BaseMessages.getString( AbsSecurityManager.class,
          "AbsSecurityManager.ERROR_0005_INSUFFICIENT_PRIVELEGES" ) ); //$NON-NLS-1$
    }
  }

  public void setLogicalRoles( String rolename, List<String> logicalRoles ) throws KettleException {
    if ( authorizationPolicyRoleBindingService != null ) {
      try {
        authorizationPolicyRoleBindingService.setRoleBindings( rolename, logicalRoles );
      } catch ( Exception e ) {
        throw new KettleException( BaseMessages.getString( AbsSecurityManager.class,
            "AbsSecurityManager.ERROR_0004_UNABLE_TO_APPLY_LOGICAL_ROLES_TO_RUNTIME_ROLE", rolename ), e ); //$NON-NLS-1$
      }
    } else {
      throw new KettleException( BaseMessages.getString( AbsSecurityManager.class,
          "AbsSecurityManager.ERROR_0005_INSUFFICIENT_PRIVELEGES" ) ); //$NON-NLS-1$
    }
  }

  public Map<String, String> getAllLogicalRoles( String locale ) throws KettleException {
    if ( authorizationPolicyRoleBindingService != null ) {
      return roleBindingStruct.logicalRoleNameMap;
    } else {
      throw new KettleException( BaseMessages.getString( AbsSecurityManager.class,
          "AbsSecurityManager.ERROR_0005_INSUFFICIENT_PRIVELEGES" ) ); //$NON-NLS-1$
    }
  }

}
