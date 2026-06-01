/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

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

  private static final String ERROR_0002_UNABLE_TO_GET_LOGICAL_ROLES =
      "AbsSecurityManager.ERROR_0002_UNABLE_TO_GET_LOGICAL_ROLES"; //$NON-NLS-1$

  private IRoleAuthorizationPolicyRoleBindingDaoWebService authorizationPolicyRoleBindingService = null;

  private RoleBindingStruct roleBindingStruct = null;

  private final transient ServiceManager serviceManager;

  public AbsSecurityManager( PurRepository repository, PurRepositoryMeta repositoryMeta, IUser userInfo,
      ServiceManager serviceManager ) {
    super( repository, repositoryMeta, userInfo, serviceManager );
    this.serviceManager = serviceManager;
    createAuthorizationPolicyService( userInfo );
  }

  /**
   * Creates (or recreates) the authorization policy role binding web service stub.
   *
   * @param userInfo the user whose credentials are used to create the service
   */
  private void createAuthorizationPolicyService( IUser userInfo ) {
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
        // The web service stub may be stale after a session timeout and reconnection
        // (e.g. "close method has already been invoked"). Recreate the stub with fresh
        // credentials from the repository and retry once.
        roleBindingStruct = retryGetRoleBindingStruct( locale, e );
      }
    } else {
      throw new KettleException( BaseMessages.getString( AbsSecurityManager.class,
          "AbsSecurityManager.ERROR_0005_INSUFFICIENT_PRIVELEGES" ) ); //$NON-NLS-1$
    }
  }

  /**
   * Attempts to recreate the web service stub and retry the {@code getRoleBindingStruct} call.
   * This handles the case where the original stub became stale after a session timeout and reconnection.
   *
   * @param locale the locale to pass to the service
   * @param originalException the exception from the first attempt
   * @return the {@link RoleBindingStruct} if the retry succeeds
   * @throws KettleException if the retry also fails or the stub cannot be recreated
   */
  private RoleBindingStruct retryGetRoleBindingStruct( String locale, Exception originalException )
      throws KettleException {
    getLogger().info(
        BaseMessages.getString( AbsSecurityManager.class,
            ERROR_0002_UNABLE_TO_GET_LOGICAL_ROLES )
            + " - attempting to recreate web service stub and retry" );
    IUser currentUser = getRepository().getUserInfo();
    if ( currentUser != null && serviceManager != null ) {
      createAuthorizationPolicyService( currentUser );
      if ( authorizationPolicyRoleBindingService != null ) {
        try {
          return authorizationPolicyRoleBindingService.getRoleBindingStruct( locale );
        } catch ( Exception retryException ) {
          throw new KettleException( BaseMessages.getString( AbsSecurityManager.class,
              ERROR_0002_UNABLE_TO_GET_LOGICAL_ROLES ), retryException );
        }
      }
    }
    throw new KettleException( BaseMessages.getString( AbsSecurityManager.class,
        ERROR_0002_UNABLE_TO_GET_LOGICAL_ROLES ), originalException );
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
