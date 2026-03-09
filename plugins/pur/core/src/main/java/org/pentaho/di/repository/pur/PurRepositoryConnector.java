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

import com.pentaho.di.services.PentahoDiPlugin;
import com.pentaho.pdi.ws.IRepositorySyncWebService;
import com.pentaho.pdi.ws.RepositorySyncException;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.ExecutorUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.repository.pur.model.EEUserInfo;
import org.pentaho.di.ui.repository.pur.services.IAbsSecurityManager;
import org.pentaho.di.ui.repository.pur.services.IAbsSecurityProvider;
import org.pentaho.di.ui.repository.pur.services.IAclService;
import org.pentaho.di.ui.repository.pur.services.IConnectionAclService;
import org.pentaho.di.ui.repository.pur.services.ILockService;
import org.pentaho.di.ui.repository.pur.services.IRevisionService;
import org.pentaho.di.ui.repository.pur.services.IRoleSupportSecurityManager;
import org.pentaho.di.ui.repository.pur.services.ITrashService;
import org.pentaho.di.ui.spoon.session.AuthenticationContext;
import org.pentaho.di.ui.spoon.session.SpoonSessionManager;
import org.pentaho.platform.security.policy.rolebased.ws.IAuthorizationPolicyWebService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.webservices.jaxws.IUnifiedRepositoryJaxwsWebService;
import org.pentaho.platform.repository2.unified.webservices.jaxws.UnifiedRepositoryToWebServiceAdapter;

import jakarta.xml.ws.WebServiceException;

import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class PurRepositoryConnector implements IRepositoryConnector {
  private static final String TRUST_USER = "_trust_user_";
  private static final String SINGLE_DI_SERVER_INSTANCE = "singleDiServerInstance";
  private static final String REMOTE_DI_SERVER_INSTANCE = "remoteDiServerInstance";
  private static Class<?> PKG = PurRepository.class;
  private final LogChannelInterface log;
  private final PurRepository purRepository;
  private final PurRepositoryMeta repositoryMeta;
  private final RootRef rootRef;
  private ServiceManager serviceManager;

  public PurRepositoryConnector( PurRepository purRepository, PurRepositoryMeta repositoryMeta, RootRef rootRef ) {
    log = new LogChannel( this.getClass().getSimpleName() );
    if ( purRepository != null && purRepository.getLog() != null ) {
      log.setLogLevel( purRepository.getLog().getLogLevel() );
    }
    this.purRepository = purRepository;
    this.repositoryMeta = repositoryMeta;
    this.rootRef = rootRef;
  }

  private boolean allowedActionsContains( AbsSecurityProvider provider, String action ) throws KettleException {
    List<String> allowedActions = provider.getAllowedActions( IAbsSecurityProvider.NAMESPACE );
    for ( String actionName : allowedActions ) {
      if ( action != null && action.equals( actionName ) ) {
        return true;
      }
    }
    return false;
  }

  public synchronized RepositoryConnectResult connect( final String username, final String password )
    throws KettleException {
    if ( serviceManager != null ) {
      disconnect();
    }

    final boolean useSessionAuth = AuthenticationContext.SESSION_AUTH_TOKEN.equals( password );
    if ( useSessionAuth ) {
      resolveSessionId();
    }

    serviceManager = new WebServiceManager( repositoryMeta.getRepositoryLocation().getUrl(), username );
    RepositoryServiceRegistry purRepositoryServiceRegistry = new RepositoryServiceRegistry();
    final String decryptedPassword = useSessionAuth ? "" : Encr.decryptPasswordOptionallyEncrypted( password );
    final RepositoryConnectResult result = new RepositoryConnectResult( purRepositoryServiceRegistry );

    try {
      /*
       * Three scenarios: 1. Connect in process: username fetched using PentahoSessionHolder; no authentication occurs
       * 2. Connect externally with trust: username specified is assumed authenticated if IP of calling code is trusted
       * 3. Connect externally: authentication occurs normally (i.e. password is checked)
       */
      IUser user1 = new EEUserInfo();
      user1.setLogin( username );
      user1.setPassword( decryptedPassword );
      user1.setName( username );
      result.setUser( user1 );

      RepositoryConnectResult inProcessResult = tryInProcessConnect( decryptedPassword, result );
      if ( inProcessResult != null ) {
        return inProcessResult;
      }

      ExecutorService executor = getExecutor();
      Future<Boolean> authorizationFuture = buildAuthorizationFuture( executor, result );
      Future<WebServiceException> repoFuture = buildRepoWebServiceFuture( executor, username, decryptedPassword, result );
      Future<Exception> syncFuture = buildSyncWebServiceFuture( executor, username, decryptedPassword, result );
      Future<String> sessionFuture = buildSessionServiceFuture( executor, username, decryptedPassword, useSessionAuth );

      applyFutureResults( result, authorizationFuture, repoFuture, syncFuture, sessionFuture );

      registerRepositoryServices( purRepositoryServiceRegistry, username, decryptedPassword, result );

      result.setSuccess( true );
    } catch ( NullPointerException npe ) {
      result.setSuccess( false );
      throw new KettleException( BaseMessages.getString( PKG, "PurRepository.LoginException.Message" ) );
    } catch ( InterruptedException ie ) {
      result.setSuccess( false );
      serviceManager.close();
      Thread.currentThread().interrupt();
      throw new KettleException( ie );
    } catch ( Exception e ) {
      result.setSuccess( false );
      serviceManager.close();
      throw new KettleException( e );
    }
    return result;
  }

  /**
   * Validates that a JSESSIONID is available when session-based authentication is requested.
   *
   * @throws KettleException if no valid JSESSIONID can be found
   */
  private void resolveSessionId() throws KettleException {
    String jsessionId = null;
    try {
      AuthenticationContext authContext =
        SpoonSessionManager.getInstance()
          .getAuthenticationContext( repositoryMeta.getRepositoryLocation().getUrl() );
      if ( authContext != null && authContext.isAuthenticated() ) {
        jsessionId = authContext.getJSessionId();
      }
    } catch ( Exception e ) {
      log.logError( "Error retrieving JSESSIONID from SpoonSessionManager", e );
    }

    if ( jsessionId != null && !jsessionId.trim().isEmpty() ) {
      log.logBasic( "Using browser-captured JSESSIONID for authentication" );
    } else {
      throw new KettleException( "Browser session authentication requested but no JSESSIONID found" );
    }
  }

  /**
   * Attempts an in-process connection via PentahoSystem when the BI Platform context is available.
   *
   * @return a completed {@link RepositoryConnectResult} if in-process connection succeeded, {@code null} otherwise
   */
  private RepositoryConnectResult tryInProcessConnect( final String decryptedPassword,
      final RepositoryConnectResult result ) {
    if ( PentahoSystem.getApplicationContext() == null || PentahoSessionHolder.getSession() == null
        || !PentahoSessionHolder.getSession().isAuthenticated() || !inProcess() ) {
      return null;
    }

    result.setUnifiedRepository( PentahoSystem.get( IUnifiedRepository.class ) );
    if ( result.getUnifiedRepository() == null ) {
      return null;
    }

    if ( log.isDebug() ) {
      log.logDebug( BaseMessages.getString( PKG, "PurRepositoryConnector.ConnectInProgress.Begin" ) );
    }
    String name = PentahoSessionHolder.getSession().getName();
    IUser inProcessUser = new EEUserInfo();
    inProcessUser.setLogin( name );
    inProcessUser.setName( name );
    inProcessUser.setPassword( decryptedPassword );
    result.setUser( inProcessUser );
    result.setSuccess( true );
    result.getUser().setAdmin(
      PentahoSystem.get( IAuthorizationPolicyWebService.class ).isAllowed( RepositorySecurityProvider.ADMINISTER_SECURITY_ACTION )
    );
    if ( log.isDebug() ) {
      log.logDebug( BaseMessages.getString(
        PKG, "PurRepositoryConnector.ConnectInProgress", name, result.getUnifiedRepository() ) );
    }
    // for now, there is no need to support the security manager
    return result;
  }

  /**
   * Submits the async task that creates the security provider (and optionally the security manager).
   */
  private Future<Boolean> buildAuthorizationFuture( ExecutorService executor,
      final RepositoryConnectResult result ) {
    return executor.submit( () -> {
      // We need to add the service class in the list in the order of dependencies
      // IRoleSupportSecurityManager depends RepositorySecurityManager to be present
      if ( log.isBasic() ) {
        log.logBasic( BaseMessages.getString( PKG, "PurRepositoryConnector.CreateServiceProvider.Start" ) );
      }
      result.setSecurityProvider( new AbsSecurityProvider( purRepository, repositoryMeta, result.getUser(),
          serviceManager ) );
      if ( log.isBasic() ) {
        log.logBasic( BaseMessages.getString( PKG, "PurRepositoryConnector.CreateServiceProvider.End" ) ); //$NON-NLS-1$
      }

      // If the user does not have access to administer security we do not need to add them to the service list
      if ( !allowedActionsContains( (AbsSecurityProvider) result.getSecurityProvider(),
          IAbsSecurityProvider.ADMINISTER_SECURITY_ACTION ) ) {
        return false;
      }

      result.setSecurityManager( new AbsSecurityManager( purRepository, repositoryMeta, result.getUser(),
          serviceManager ) );
      // Set the reference of the security manager to security provider for user role list change event
      ( (PurRepositorySecurityProvider) result.getSecurityProvider() )
          .setUserRoleDelegate( ( (PurRepositorySecurityManager) result.getSecurityManager() )
              .getUserRoleDelegate() );
      return true;
    } );
  }

  /**
   * Submits the async task that creates the unified repository web-service and its adapter.
   */
  private Future<WebServiceException> buildRepoWebServiceFuture( ExecutorService executor, final String username,
      final String decryptedPassword, final RepositoryConnectResult result ) {
    return executor.submit( () -> {
      try {
        if ( log.isBasic() ) {
          log.logBasic( BaseMessages.getString( PKG, "PurRepositoryConnector.CreateRepositoryWebService.Start" ) ); //$NON-NLS-1$
        }
        IUnifiedRepositoryJaxwsWebService repoWebService =
            serviceManager.createService( username, decryptedPassword, IUnifiedRepositoryJaxwsWebService.class );
        if ( log.isBasic() ) {
          log.logBasic( BaseMessages.getString( PKG, "PurRepositoryConnector.CreateRepositoryWebService.End" ) ); //$NON-NLS-1$
          log.logBasic( BaseMessages.getString( PKG, "PurRepositoryConnector.CreateUnifiedRepositoryToWebServiceAdapter.Start" ) ); //$NON-NLS-1$
        }
        result.setUnifiedRepository( new UnifiedRepositoryToWebServiceAdapter( repoWebService ) );
      } catch ( WebServiceException wse ) {
        return wse;
      }
      return null;
    } );
  }

  /**
   * Submits the async task that performs the repository sync handshake.
   */
  private Future<Exception> buildSyncWebServiceFuture( ExecutorService executor, final String username,
      final String decryptedPassword, final RepositoryConnectResult result ) {
    return executor.submit( () -> {
      try {
        if ( log.isBasic() ) {
          log.logBasic( BaseMessages.getString( PKG, "PurRepositoryConnector.CreateRepositorySyncWebService.Start" ) );
        }
        IRepositorySyncWebService syncWebService =
            serviceManager.createService( username, decryptedPassword, IRepositorySyncWebService.class );
        if ( log.isBasic() ) {
          log.logBasic( BaseMessages.getString( PKG, "PurRepositoryConnector.CreateRepositorySyncWebService.Sync" ) ); //$NON-NLS-1$
        }
        syncWebService.sync( repositoryMeta.getName(), repositoryMeta.getRepositoryLocation().getUrl() );
      } catch ( RepositorySyncException e ) {
        log.logError( e.getMessage(), e );
        result.setConnectMessage( e.getMessage() ); // this message will be presented to the user in spoon
        return null;
      } catch ( WebServiceException e ) {
        // if we can speak to the repository okay but not the sync service, assume we're talking to a BA Server
        log.logError( e.getMessage(), e );
        return new Exception( BaseMessages.getString( PKG, "PurRepository.BAServerLogin.Message" ), e );
      }
      return null;
    } );
  }

  /**
   * Submits the async task that fetches the authenticated username from the session REST endpoint,
   * or short-circuits immediately when session-based auth is in use.
   */
  private Future<String> buildSessionServiceFuture( ExecutorService executor, final String username,
      final String decryptedPassword, final boolean useSessionAuth ) {
    return executor.submit( () -> resolveSessionUsername( username, decryptedPassword, useSessionAuth ) );
  }

  private String resolveSessionUsername( String username, String decryptedPassword, boolean useSessionAuth ) {
    if ( useSessionAuth ) {
      if ( log.isBasic() ) {
        log.logBasic( "Skipping session service call for browser auth - using provided username: " + username );
      }
      return username;
    }
    return fetchUsernameFromSessionService( username, decryptedPassword );
  }

  private String fetchUsernameFromSessionService( String username, String decryptedPassword ) {
    try {
      if ( log.isBasic() ) {
        log.logBasic( BaseMessages.getString( PKG, "PurRepositoryConnector.SessionService.Start" ) );
      }
      CredentialsProvider provider = new BasicCredentialsProvider();
      provider.setCredentials( AuthScope.ANY, new UsernamePasswordCredentials( username, decryptedPassword ) );
      HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider( provider ).build();
      HttpGet method = new HttpGet( repositoryMeta.getRepositoryLocation().getUrl() + "/api/session/userName" );
      if ( StringUtils.isNotBlank( System.getProperty( "pentaho.repository.client.attemptTrust" ) ) ) {
        method.addHeader( TRUST_USER, username );
      }
      HttpResponse response = client.execute( method );
      if ( log.isBasic() ) {
        log.logBasic( BaseMessages.getString( PKG, "PurRepositoryConnector.SessionService.Sync" ) );
      }
      return EntityUtils.toString( response.getEntity() );
    } catch ( Exception e ) {
      if ( log.isError() ) {
        log.logError( BaseMessages.getString( PKG, "PurRepositoryConnector.Error.EnableToGetUser" ), e );
      }
      return null;
    }
  }

  /**
   * Awaits all submitted futures and applies their results to {@code result}.
   */
  private void applyFutureResults( final RepositoryConnectResult result,
      Future<Boolean> authorizationFuture, Future<WebServiceException> repoFuture,
      Future<Exception> syncFuture, Future<String> sessionFuture )
      throws KettleException, InterruptedException, java.util.concurrent.ExecutionException {
    WebServiceException repoException = repoFuture.get();
    if ( repoException != null ) {
      log.logError( repoException.getMessage() );
      throw new KettleException( BaseMessages.getString( PKG, "PurRepository.FailedLogin.Message" ), repoException );
    }

    Exception syncException = syncFuture.get();
    if ( syncException != null ) {
      throw new KettleException( syncException );
    }

    boolean isAdmin = authorizationFuture.get();
    result.getUser().setAdmin( isAdmin );

    String userName = sessionFuture.get();
    if ( userName != null ) {
      result.getUser().setLogin( userName );
    }
  }

  /**
   * Registers all repository services into the service registry after a successful connection.
   */
  private void registerRepositoryServices( RepositoryServiceRegistry registry, final String username,
      final String decryptedPassword, final RepositoryConnectResult result ) throws MalformedURLException {
    if ( log.isBasic() ) {
      log.logBasic( BaseMessages.getString( PKG, "PurRepositoryConnector.RegisterSecurityProvider.Start" ) );
    }

    registry.registerService( RepositorySecurityProvider.class, result.getSecurityProvider() );
    registry.registerService( IAbsSecurityProvider.class, result.getSecurityProvider() );

    if ( Boolean.TRUE.equals( result.getUser().isAdmin() ) ) {
      registry.registerService( RepositorySecurityManager.class, result.getSecurityManager() );
      registry.registerService( IRoleSupportSecurityManager.class, result.getSecurityManager() );
      registry.registerService( IAbsSecurityManager.class, result.getSecurityManager() );
    }

    registry.registerService( PentahoDiPlugin.PurRepositoryPluginApiRevision.class,
        serviceManager.createService( username, decryptedPassword,
            PentahoDiPlugin.PurRepositoryPluginApiRevision.class ) );

    registry.registerService( IRevisionService.class,
        new UnifiedRepositoryRevisionService( result.getUnifiedRepository(), rootRef ) );
    registry.registerService( IAclService.class,
        new UnifiedRepositoryConnectionAclService( result.getUnifiedRepository() ) );
    registry.registerService( IConnectionAclService.class,
        new UnifiedRepositoryConnectionAclService( result.getUnifiedRepository() ) );
    registry.registerService( ITrashService.class,
        new UnifiedRepositoryTrashService( result.getUnifiedRepository(), rootRef ) );
    registry.registerService( ILockService.class,
        new UnifiedRepositoryLockService( result.getUnifiedRepository() ) );

    if ( log.isBasic() ) {
      log.logBasic( BaseMessages.getString( PKG, "PurRepositoryConnector.RepositoryServicesRegistered.End" ) );
    }
  }
  ExecutorService getExecutor() {
    return ExecutorUtil.getExecutor();
  }
  @Override
  public synchronized void disconnect() {
    if ( serviceManager != null ) {
      serviceManager.close();
    }
    serviceManager = null;
  }

  public LogChannelInterface getLog() {
    return log;
  }

  @Override
  public ServiceManager getServiceManager() {
    return serviceManager;
  }

  public static boolean inProcess() {
    if ( "true".equals( PentahoSystem.getSystemSetting( SINGLE_DI_SERVER_INSTANCE, "true" ) ) ) { //$NON-NLS-1$ //$NON-NLS-2$
      return true;
    }
    boolean remoteDiServer =
        BooleanUtils.toBoolean( PentahoSystem.getSystemSetting( REMOTE_DI_SERVER_INSTANCE, "false" ) ); //$NON-NLS-1$
    return !remoteDiServer && PentahoSystem.getApplicationContext().getFullyQualifiedServerURL() != null;
  }
}
