/*!
 * Copyright 2010 - 2016 Pentaho Corporation.  All rights reserved.
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

import org.apache.jackrabbit.api.JackrabbitWorkspace;
import org.apache.jackrabbit.api.security.authorization.PrivilegeManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.repository2.unified.IRepositoryFileDao;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.springframework.extensions.jcr.JcrCallback;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.security.AccessControlException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Andrey Khayrutdinov
 */
class PurRepositoryTestingUtils {

  /**
   * Creates a session for {@code username}. No tenant is set.
   * 
   * @param username
   *          user name
   * @return {@code StandaloneSession} instance
   */
  static StandaloneSession createSession( String username ) {
    return createSession( null, username );
  }

  /**
   * Creates a session for {@code username} and sets {@code tenant} as its tenant.
   * 
   * @param tenant
   *          tenant
   * @param username
   *          user name
   * @return {@code StandaloneSession} instance
   */
  static StandaloneSession createSession( ITenant tenant, String username ) {
    String tenantId = ( tenant == null ) ? null : tenant.getId();
    StandaloneSession session = new StandaloneSession( username );
    session.setAuthenticated( tenantId, username );
    return session;
  }

  /**
   * Creates an {@code Authentication} instance for {@code userName} and with {@code roles} granted.
   * 
   * @param userName
   *          user name
   * @param roles
   *          user roles
   * @return {@code Authentication} instance
   */
  static Authentication createAuthentication( String userName, String... roles ) {
    List<GrantedAuthority> authorities = new ArrayList<>();
    if ( roles != null ) {
      for ( String role : roles ) {
        authorities.add( new SimpleGrantedAuthority( role ) );
      }
    }
    UserDetails userDetails = new User( userName, "", true, true, true, true, authorities );
    return new UsernamePasswordAuthenticationToken( userDetails, "", authorities );
  }

  /**
   * Sets Pentaho Session and authentication.
   * 
   * @param session
   *          session
   * @param authentication
   *          authentication
   */
  static void setSession( IPentahoSession session, Authentication authentication ) {
    PentahoSessionHolder.setSession( session );
    SecurityContextHolder.getContext().setAuthentication( authentication );
  }

  /**
   * Creates a callback for setting up {@code username}'s home folder. In the folder exists, noting is done. If
   * {@code tenant}'s home folder does not exist nothing is done.
   * 
   * @param tenant
   *          tenant
   * @param username
   *          user name
   * @param principleId
   *          user principle's id
   * @param authenticatedRoleId
   *          user authenticated role's id
   * @param fileDao
   *          file dao
   * @return callback for performing the action
   */
  static TransactionCallbackWithoutResult createUserHomeDirCallback( final ITenant tenant, final String username,
      final String principleId, final String authenticatedRoleId, final IRepositoryFileDao fileDao ) {
    return new TransactionCallbackWithoutResult() {
      public void doInTransactionWithoutResult( final TransactionStatus status ) {
        String tenantRootFolderPath = ServerRepositoryPaths.getTenantRootFolderPath( tenant );
        RepositoryFile tenantRootFolder = fileDao.getFileByAbsolutePath( tenantRootFolderPath );
        if ( tenantRootFolder == null ) {
          return;
        }

        String userHomeFolderPath = ServerRepositoryPaths.getUserHomeFolderPath( tenant, username );
        RepositoryFile userHomeFolder = fileDao.getFileByAbsolutePath( userHomeFolderPath );
        if ( userHomeFolder != null ) {
          return;
        }

        RepositoryFileSid userSid = new RepositoryFileSid( principleId );

        String tenantHomeFolderPath = ServerRepositoryPaths.getTenantHomeFolderPath( tenant );
        RepositoryFile tenantHomeFolder = fileDao.getFileByAbsolutePath( tenantHomeFolderPath );
        RepositoryFileAcl.Builder aclsForUserHomeFolder;
        if ( tenantHomeFolder == null ) {
          RepositoryFileSid ownerSid = new RepositoryFileSid( principleId, RepositoryFileSid.Type.USER );

          RepositoryFileSid tenantAuthenticatedRoleSid =
              new RepositoryFileSid( authenticatedRoleId, RepositoryFileSid.Type.ROLE );

          RepositoryFileAcl.Builder aclsForTenantHomeFolder =
              new RepositoryFileAcl.Builder( userSid ).ace( tenantAuthenticatedRoleSid, EnumSet
                  .of( RepositoryFilePermission.READ ) );

          aclsForUserHomeFolder =
              new RepositoryFileAcl.Builder( userSid ).ace( ownerSid, EnumSet.of( RepositoryFilePermission.ALL ) );
          tenantHomeFolder =
              fileDao.createFolder( tenantRootFolder.getId(), new RepositoryFile.Builder( ServerRepositoryPaths
                  .getTenantHomeFolderName() ).folder( true ).build(), aclsForTenantHomeFolder.build(),
                  "tenant home folder" );
        } else {
          RepositoryFileSid ownerSid = new RepositoryFileSid( principleId, RepositoryFileSid.Type.USER );
          aclsForUserHomeFolder =
              new RepositoryFileAcl.Builder( userSid ).ace( ownerSid, EnumSet.of( RepositoryFilePermission.ALL ) );
        }

        fileDao.createFolder( tenantHomeFolder.getId(), new RepositoryFile.Builder( username ).folder( true ).build(),
            aclsForUserHomeFolder.build(), "user home folder" );
      }
    };
  }

  /**
   * Create a {@linkplain JcrCallback} for setting up ACL management in test repository
   * 
   * @return acl management callback
   */
  static JcrCallback setAclManagementCallback() {
    return new JcrCallback() {
      @Override
      public Object doInJcr( Session session ) throws IOException, RepositoryException {
        PentahoJcrConstants pentahoJcrConstants = new PentahoJcrConstants( session );
        Workspace workspace = session.getWorkspace();
        PrivilegeManager privilegeManager = ( (JackrabbitWorkspace) workspace ).getPrivilegeManager();
        try {
          privilegeManager.getPrivilege( pentahoJcrConstants.getPHO_ACLMANAGEMENT_PRIVILEGE() );
        } catch ( AccessControlException ace ) {
          privilegeManager.registerPrivilege( pentahoJcrConstants.getPHO_ACLMANAGEMENT_PRIVILEGE(), false,
              new String[0] );
        }
        session.save();
        return null;
      }
    };
  }

}
