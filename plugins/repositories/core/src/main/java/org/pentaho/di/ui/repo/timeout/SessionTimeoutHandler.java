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


package org.pentaho.di.ui.repo.timeout;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.KettleRepositoryLostException;
import org.pentaho.di.ui.repo.controller.RepositoryConnectController;
import org.pentaho.di.ui.repo.dialog.RepositoryConnectionDialog;
import org.pentaho.di.ui.repo.service.BrowserAuthenticationService;
import org.pentaho.di.ui.repo.service.BrowserAuthenticationService.SessionInfo;
import org.pentaho.di.ui.repo.util.PurRepositoryUtils;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.session.AuthenticationContext;
import org.pentaho.di.ui.spoon.session.SpoonSessionManager;

public class SessionTimeoutHandler {

  private static Class<?> PKG = SessionTimeoutHandler.class;

  private static final int STACK_ELEMENTS_TO_SKIP = 3;

  private static final String EXCEPTION_CLASS_NAME = "ClientTransportException";

  private final RepositoryConnectController repositoryConnectController;

  private final AtomicBoolean needToLogin = new AtomicBoolean( false );

  private final AtomicBoolean reinvoke = new AtomicBoolean( false );

  /**
   * Prevents re-entrant login dialogs: once the login/re-auth flow is in progress any
   * further 401-triggered handle() calls on other threads simply rethrow without showing
   * a second dialog or opening a second browser window.
   */
  private final AtomicBoolean isHandlingLogin = new AtomicBoolean( false );

  private static final LogChannelInterface log =
    KettleLogStore.getLogChannelInterfaceFactory().create( SessionTimeoutHandler.class );

  public SessionTimeoutHandler( RepositoryConnectController repositoryConnectController ) {
    this.repositoryConnectController = repositoryConnectController;
  }

  public RepositoryConnectController getRepositoryConnectController() {
    return repositoryConnectController;
  }

  public Object handle( Object objectToHandle, Throwable exception, Method method, Object[] args ) throws Throwable {
    if ( !lookupForConnectTimeoutError( exception ) || calledFromThisHandler() ) {
      throw exception;
    }

    // Another thread is already showing the login/re-auth dialog — don't open a second one.
    if ( isHandlingLogin.get() ) {
      throw exception;
    }

    // Reset per-invocation state so stale flags from a previous handle() cycle cannot
    // cause an immediate re-loop on the very next proxied call.
    reinvoke.set( false );
    needToLogin.set( false );

    retryInvocationOnce( objectToHandle, method, args );

    try {
      ConnectionManager.getInstance().reset();
    } catch ( Exception e ) {
      // Log but don't fail
    }

    needToLogin.set( true );
    Object reinvokeResult = performLoginAndReinvoke( objectToHandle, method, args );
    if ( reinvokeResult != null ) {
      return reinvokeResult;
    }

    if ( reinvoke.get() ) {
      return method.invoke( objectToHandle, args );
    }
    throw exception;
  }

  /**
   * Attempts a single immediate retry of the invocation. If the retry also fails with a timeout
   * error, the failure is swallowed so the login flow can proceed. Any other exception is rethrown.
   */
  private void retryInvocationOnce( Object objectToHandle, Method method, Object[] args ) throws KettleException {
    try {
      method.invoke( objectToHandle, args );
    } catch ( IllegalAccessException ex2 ) {
      throw new KettleException( ex2 );
    } catch ( InvocationTargetException ex2 ) {
      if ( !lookupForConnectTimeoutError( ex2 ) ) {
        throw new KettleException( ex2.getCause() );
      }
    }
  }

  /**
   * Synchronizes on {@code this} to ensure only one thread shows the login screen.
   * Returns the result of re-invoking the method after a successful login, or {@code null}
   * when no reinvocation was performed in this synchronized block.
   */
  private Object performLoginAndReinvoke( Object objectToHandle, Method method, Object[] args )
    throws LoginSuccessReinvokeException {
    synchronized ( this ) {
      if ( !needToLogin.get() ) {
        return null;
      }
      isHandlingLogin.set( true );
      boolean loginSucceeded;
      try {
        loginSucceeded = showLoginScreen( repositoryConnectController );
      } finally {
        isHandlingLogin.set( false );
      }
      needToLogin.set( false );
      if ( loginSucceeded ) {
        return handleLoginSuccess( objectToHandle, method, args );
      } else {
        handleLoginCanceled();
      }
    }
    return null;
  }

  /**
   * Called after the user successfully logs in. Initializes repository providers and
   * re-invokes the original method.
   */
  private Object handleLoginSuccess( Object objectToHandle, Method method, Object[] args )
      throws LoginSuccessReinvokeException {
    reinvoke.set( true );
    initializeRepositoryProvidersAfterReconnection( getSpoon() );
    try {
      return method.invoke( objectToHandle, args );
    } catch ( InvocationTargetException ex ) {
      throw new LoginSuccessReinvokeException( "Failed to re-invoke operation after successful login",
          ex.getCause() );
    } catch ( IllegalAccessException | IllegalArgumentException ex ) {
      throw new LoginSuccessReinvokeException( "Unable to invoke operation after successful login", ex );
    }
  }

  static class LoginSuccessReinvokeException extends KettleException {
    private static final long serialVersionUID = 1L;

    LoginSuccessReinvokeException( String message, Throwable cause ) {
      super( message, cause );
    }
  }

  /**
   * Called when the user cancels the login screen. Closes the repository and throws
   * {@link KettleRepositoryLostException} to stop the current operation.
   */
  private void handleLoginCanceled() throws KettleRepositoryLostException {
    log.logBasic( "User canceled reconnection after session expiry - disconnecting repository" );
    try {
      Spoon spoon = getSpoon();
      if ( spoon != null ) {
        spoon.getDisplay().syncExec( () -> {
          try {
            spoon.closeRepository();
          } catch ( Exception ex ) {
            log.logError( "Error closing repository after user canceled reconnection", ex );
          }
        } );
      }
    } catch ( Exception e ) {
      log.logError( "Error during repository disconnect after cancel", e );
    }
    throw new KettleRepositoryLostException( "User canceled repository reconnection after session timeout" );
  }



  boolean lookupForConnectTimeoutError( Throwable root ) {
    while ( root != null ) {
      if ( EXCEPTION_CLASS_NAME.equals( root.getClass().getSimpleName() ) ) {
        String errorMessage = root.getMessage();
        if ( errorMessage.contains( RepositoryConnectController.ERROR_401 ) ) {
          return true;
        } else {
          return false;
        }
      } else {
        root = root.getCause();
      }
    }
    return false;
  }

  boolean calledFromThisHandler() {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    for ( int i = STACK_ELEMENTS_TO_SKIP; i < stackTrace.length; i++ ) {
      if ( stackTrace[i].getClassName().equals( SessionTimeoutHandler.class.getCanonicalName() ) ) {
        return true;
      }
    }
    return false;
  }

  boolean showLoginScreen( RepositoryConnectController repositoryConnectController ) {
    RepositoryMeta repositoryMeta = repositoryConnectController.getConnectedRepository();
    repositoryConnectController.setRelogin( true );

    String serverUrl = PurRepositoryUtils.getServerUrl( repositoryMeta );
    boolean wasBrowserAuth = detectBrowserAuth( serverUrl );
    log.logBasic( "Session expired - browser auth previously used: " + wasBrowserAuth );

    if ( !confirmSessionExpiry() ) {
      return false;
    }

    if ( wasBrowserAuth ) {
      boolean browserReauthSucceeded = tryBrowserReauth( serverUrl, repositoryMeta, repositoryConnectController );
      if ( browserReauthSucceeded ) {
        return true;
      }
    }

    // Default: show username/password dialog.
    // The dialog creates SWT widgets and runs its own event loop, so it MUST execute
    // on the SWT UI thread. When called from a background thread (e.g. the worker
    // spawned by SaveProgressDialog / ProgressMonitorDialog), running it directly
    // would cause an SWT thread-access violation and hang.
    log.logBasic( "Showing username/password login dialog" );
    final Spoon spoon = getSpoon();
    if ( spoon == null || spoon.getDisplay() == null ) {
      log.logError( "Cannot show login dialog — no display available" );
      return false;
    }
    final String repoName = repositoryMeta.getName();
    final boolean[] loginResult = { false };
    spoon.getDisplay().syncExec( () -> {
      try {
        RepositoryConnectionDialog loginDialog = new RepositoryConnectionDialog( spoon.getShell() );
        loginResult[0] = loginDialog.createDialog( repoName );
      } catch ( Exception e ) {
        log.logError( "Error showing login dialog", e );
        loginResult[0] = false;
      }
    } );
    return loginResult[0];
  }

  /**
   * Returns {@code true} if the previous connection used browser-based authentication.
   */
  private boolean detectBrowserAuth( String serverUrl ) {
    if ( serverUrl == null ) {
      return false;
    }
    AuthenticationContext authContext = SpoonSessionManager.getInstance().getAuthenticationContext( serverUrl );
    return authContext != null && authContext.wasPreviouslyAuthenticated();
  }

  /**
   * Shows the session-expiry confirmation dialog to the user.
   *
   * @return {@code true} if the user clicked OK, {@code false} if they cancelled or an error occurred
   */
  private boolean confirmSessionExpiry() {
    final boolean[] userConfirmed = { false };
    final Spoon spoon = getSpoon();
    if ( spoon != null && spoon.getDisplay() != null ) {
      spoon.getDisplay().syncExec( () -> {
        try {
          MessageBox mb = new MessageBox( spoon.getShell(), SWT.OK | SWT.CANCEL | SWT.ICON_WARNING );
          mb.setText( "Session Expired" );
          mb.setMessage( "Your repository session has expired. Click OK to reconnect." );
          userConfirmed[0] = ( mb.open() == SWT.OK );
        } catch ( Exception e ) {
          log.logError( "Error showing session expiry dialog", e );
          userConfirmed[0] = false;
        }
      } );
    } else {
      userConfirmed[0] = true; // No UI available, proceed
    }
    return userConfirmed[0];
  }

  /**
   * Attempts to re-authenticate via the browser flow and reconnect the repository.
   *
   * @return {@code true} if browser re-authentication and reconnection both succeeded
   */
  private boolean tryBrowserReauth( String serverUrl, RepositoryMeta repositoryMeta,
      RepositoryConnectController repositoryConnectController ) {
    try {
      log.logBasic( "Triggering browser re-authentication" );
      String authorizationUri = PurRepositoryUtils.getSsoAuthorizationUri( repositoryMeta );
      if ( authorizationUri != null && !authorizationUri.trim().isEmpty() ) {
        log.logBasic( "Re-authenticating with saved SSO provider authorization URI" );
      } else {
        log.logBasic( "No saved SSO provider authorization URI found; using generic browser auth" );
      }

      BrowserAuthenticationService authService = createBrowserAuthenticationService();
      SessionInfo sessionInfo = authService.authenticate( serverUrl, authorizationUri ).get();

      if ( sessionInfo == null || sessionInfo.getJsessionId() == null ) {
        log.logBasic( "Browser auth returned null session info" );
        return false;
      }

      return storeNewSessionAndReconnect( serverUrl, sessionInfo, repositoryMeta, repositoryConnectController );
    } catch ( InterruptedException e ) {
      Thread.currentThread().interrupt();
      log.logError( "Browser auth interrupted, falling back to username/password dialog", e );
      return false;
    } catch ( ExecutionException e ) {
      log.logError( "Browser auth failed, falling back to username/password dialog", e );
      return false;
    } catch ( Exception e ) {
      log.logError( "Unexpected error during browser auth, falling back to username/password dialog", e );
      return false;
    }
  }

  BrowserAuthenticationService createBrowserAuthenticationService() {
    return new BrowserAuthenticationService();
  }

  /**
   * Stores the new JSESSIONID and reconnects the repository using browser-auth credentials.
   *
   * @return {@code true} if the repository connected successfully after storing the session
   */
  private boolean storeNewSessionAndReconnect( String serverUrl, SessionInfo sessionInfo,
      RepositoryMeta repositoryMeta, RepositoryConnectController repositoryConnectController ) throws KettleException {
    log.logBasic( "Browser auth successful, reconnecting" );
    SpoonSessionManager.getInstance().getAuthenticationContext( serverUrl )
      .storeJSessionId( sessionInfo.getJsessionId() );

    repositoryConnectController.reconnectToRepository(
      repositoryMeta.getName(),
      sessionInfo.getUsername(),
      AuthenticationContext.SESSION_AUTH_TOKEN
    );

    if ( !repositoryConnectController.isConnected() ) {
      log.logError( "Failed to connect with browser auth" );
      return false;
    }

    log.logBasic( "Successfully reconnected with browser auth" );
    initializeRepositoryProvidersAfterReconnection( getSpoon() );
    return true;
  }

  /**
   * Initializes repository providers after a successful reconnection.
   * This loads the directory tree to ensure providers are ready for operations.
   *
   * @param spoon the Spoon instance, may be null
   */
  private void initializeRepositoryProvidersAfterReconnection( Spoon spoon ) {
    try {
      if ( spoon != null ) {
        Repository repo = spoon.getRepository();
        if ( repo != null ) {
          repo.loadRepositoryDirectoryTree();
        }
      }
    } catch ( Exception ex ) {
      log.logDebug( "Error initializing repository providers after reconnection", ex );
    }
  }

  private Spoon getSpoon() {
    return Spoon.getInstance();
  }

}
