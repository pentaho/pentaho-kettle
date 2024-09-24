/*!
 * Copyright 2010 - 2024 Hitachi Vantara.  All rights reserved.
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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.ui.repository.pur.services.IAbsSecurityProvider;
import org.pentaho.platform.security.policy.rolebased.ws.IAuthorizationPolicyWebService;

import java.util.List;

public class AbsSecurityProvider extends PurRepositorySecurityProvider implements IAbsSecurityProvider,
  java.io.Serializable {
  private static final long FIVE_MINUTES = 5 * 60 * 1000L;
  private static final long serialVersionUID = -41954375242408881L; /* EESOURCE: UPDATE SERIALVERUID */
  private IAuthorizationPolicyWebService authorizationPolicyWebService = null;
  private final ActiveCache<String, List<String>> allowedActionsActiveCache = new ActiveCache<>(
    key -> authorizationPolicyWebService.getAllowedActions( key ), FIVE_MINUTES );
  private final ActiveCache<String, Boolean> isAllowedActiveCache = new ActiveCache<>(
    key -> authorizationPolicyWebService.isAllowed( key ), FIVE_MINUTES );

  public AbsSecurityProvider( PurRepository repository, PurRepositoryMeta repositoryMeta, IUser userInfo,
                              ServiceManager serviceManager ) {
    super( repository, repositoryMeta, userInfo, serviceManager );
    try {
      authorizationPolicyWebService =
        serviceManager.createService( userInfo.getLogin(), userInfo.getPassword(),
          IAuthorizationPolicyWebService.class );
      if ( authorizationPolicyWebService == null ) {
        getLogger().error(
          BaseMessages.getString( AbsSecurityProvider.class,
            "AbsSecurityProvider.ERROR_0001_UNABLE_TO_INITIALIZE_AUTH_POLICY_WEBSVC" ) );
      }

    } catch ( Exception e ) {
      getLogger().error(
        BaseMessages.getString( AbsSecurityProvider.class,
          "AbsSecurityProvider.ERROR_0001_UNABLE_TO_INITIALIZE_AUTH_POLICY_WEBSVC" ), e );
    }
  }

  public List<String> getAllowedActions( String nameSpace ) throws KettleException {
    try {
      return allowedActionsActiveCache.get( nameSpace );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( AbsSecurityProvider.class,
        "AbsSecurityProvider.ERROR_0003_UNABLE_TO_ACCESS_GET_ALLOWED_ACTIONS" ), e );
    }
  }

  public boolean isAllowed( String actionName ) throws KettleException {
    try {
      return isAllowedActiveCache.get( actionName );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( AbsSecurityProvider.class,
        "AbsSecurityProvider.ERROR_0002_UNABLE_TO_ACCESS_IS_ALLOWED" ), e );
    }
  }

  @Override
  public void validateAction( RepositoryOperation... operations ) throws KettleException {

    for ( RepositoryOperation operation : operations ) {
      switch ( operation ) {
        case EXECUTE_TRANSFORMATION:
        case EXECUTE_JOB:
          checkOperationAllowed( EXECUTE_CONTENT_ACTION );
          break;

        case MODIFY_TRANSFORMATION:
        case MODIFY_JOB:
          checkOperationAllowed( CREATE_CONTENT_ACTION );
          break;

        case SCHEDULE_TRANSFORMATION:
        case SCHEDULE_JOB:
          checkOperationAllowed( SCHEDULE_CONTENT_ACTION );
          break;

        case MODIFY_DATABASE:
          checkOperationAllowed( MODIFY_DATABASE_ACTION );
          break;

        case SCHEDULER_EXECUTE:
          checkOperationAllowed( SCHEDULER_EXECUTE_ACTION );
          break;
      }
    }
  }

  /**
   * @throws KettleException if an operation is not allowed
   */
  private void checkOperationAllowed( String operation ) throws KettleException {
    if ( !isAllowed( operation ) ) {
      throw new KettleException( operation + " : permission not allowed" );
    }
  }

}
