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

package org.pentaho.di.base;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.cli.config.CliConfig;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.pan.CommandExecutorResult;
import org.pentaho.di.pan.CommandLineOptionProvider;
import org.pentaho.di.cli.auth.BrokerAuthClient;
import org.pentaho.di.cli.auth.BrokerAuthClient.BrokerFlowResult;
import org.pentaho.di.cli.auth.BrokerDiscoveryClient;
import org.pentaho.di.cli.auth.BrowserAuthSessionHolder;
import org.pentaho.di.cli.auth.DPoPProofBuilder;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.version.BuildVersion;
import org.pentaho.metastore.api.IMetaStore;

import java.awt.Desktop;
import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractBaseCommandExecutor {

  private static final String TOKEN_TYPE_BEARER = "Bearer";

  private record ResolvedAuthContext(String serverUrl, boolean useBrowserAuth, boolean useDeviceCode,
                                     boolean useServiceAccount, String registrationId) {
  }

  private record RepositoryConnectionResolution(Repository repository, String username, String password) {
  }

  private SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss.SSS" );
  private BrokerDiscoveryClient brokerDiscoveryClient;

  public static final String YES = "Y";

  private LogChannelInterface log;
  private Class<?> pkgClazz;
  IMetaStore metaStore = MetaStoreConst.getDefaultMetastore();
  private Bowl bowl = DefaultBowl.getInstance();

  private Result result = new Result();

  protected Result exitWithStatus( final int exitStatus ) {
    getResult().setExitStatus( exitStatus );
    return getResult();
  }

  protected void logDebug( final String messageKey ) {
    if ( getLog().isDebug() ) {
      getLog().logDebug( BaseMessages.getString( getPkgClazz(), messageKey ) );
    }
  }

  protected void logDebug( final String messageKey, String... messageTokens ) {
    if ( getLog().isDebug() ) {
      getLog().logDebug( BaseMessages.getString( getPkgClazz(), messageKey, messageTokens ) );
    }
  }

  protected String message( final String messageKey, String... messageTokens ) {
    return BaseMessages.getString( getPkgClazz(), messageKey, messageTokens );
  }

  protected void printBasicLine( String text ) {
    getLog().logBasic( text );
  }

  protected int calculateAndPrintElapsedTime( Date start, Date stop, String startStopMsgTkn,
                                              String processingEndAfterMsgTkn,
                                              String processingEndAfterLongMsgTkn,
                                              String processingEndAfterLongerMsgTkn,
                                              String processingEndAfterLongestMsgTkn ) {

    String begin = getDateFormat().format( start );
    String end = getDateFormat().format( stop );

    getLog().logMinimal( BaseMessages.getString( getPkgClazz(), startStopMsgTkn, begin, end ) );

    long millis = stop.getTime() - start.getTime();
    int seconds = (int) ( millis / 1000 );
    if ( seconds <= 60 ) {
      getLog().logMinimal(
        BaseMessages.getString( getPkgClazz(), processingEndAfterMsgTkn, String.valueOf( seconds ) ) );
    } else if ( seconds <= 60 * 60 ) {
      int min = ( seconds / 60 );
      int rem = ( seconds % 60 );
      getLog().logMinimal( BaseMessages.getString( getPkgClazz(), processingEndAfterLongMsgTkn, String.valueOf( min ),
        String.valueOf( rem ), String.valueOf( seconds ) ) );
    } else if ( seconds <= 60 * 60 * 24 ) {
      int rem;
      int hour = ( seconds / ( 60 * 60 ) );
      rem = ( seconds % ( 60 * 60 ) );
      int min = rem / 60;
      rem = rem % 60;
      getLog().logMinimal(
        BaseMessages.getString( getPkgClazz(), processingEndAfterLongerMsgTkn, String.valueOf( hour ),
          String.valueOf( min ), String.valueOf( rem ), String.valueOf( seconds ) ) );
    } else {
      int rem;
      int days = ( seconds / ( 60 * 60 * 24 ) );
      rem = ( seconds % ( 60 * 60 * 24 ) );
      int hour = rem / ( 60 * 60 );
      rem = rem % ( 60 * 60 );
      int min = rem / 60;
      rem = rem % 60;
      getLog().logMinimal(
        BaseMessages.getString( getPkgClazz(), processingEndAfterLongestMsgTkn, String.valueOf( days ),
          String.valueOf( hour ), String.valueOf( min ), String.valueOf( rem ), String.valueOf( seconds ) ) );
    }

    return seconds;
  }

  protected void printVersion( String kettleVersionMsgTkn ) {
    BuildVersion buildVersion = BuildVersion.getInstance();
    getLog().logBasic( BaseMessages.getString( getPkgClazz(), kettleVersionMsgTkn, buildVersion.getVersion(),
      buildVersion.getRevision(), buildVersion.getBuildDate() ) );
  }

  public RepositoryMeta loadRepositoryConnection( final String repoName, String loadingAvailableRepMsgTkn,
                                                  String noRepsDefinedMsgTkn, String findingRepMsgTkn )
    throws KettleException {

    RepositoriesMeta repsinfo;

    if ( Utils.isEmpty( repoName )
      || ( repsinfo = loadRepositoryInfo( loadingAvailableRepMsgTkn, noRepsDefinedMsgTkn ) ) == null ) {
      return null;
    }

    logDebug( findingRepMsgTkn, repoName );
    return repsinfo.findRepository( repoName );
  }

  public RepositoriesMeta loadRepositoryInfo( String loadingAvailableRepMsgTkn, String noRepsDefinedMsgTkn )
    throws KettleException {

    RepositoriesMeta repsinfo = new RepositoriesMeta();
    repsinfo.getLog().setLogLevel( getLog().getLogLevel() );

    logDebug( loadingAvailableRepMsgTkn );

    try {
      repsinfo.readData();
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( getPkgClazz(), noRepsDefinedMsgTkn ), e );
    }

    return repsinfo;
  }

  public RepositoryDirectoryInterface loadRepositoryDirectory( Repository repository, String dirName,
                                                               String noRepoProvidedMsgTkn,
                                                               String allocateAndConnectRepoMsgTkn,
                                                               String cannotFindDirMsgTkn ) throws KettleException {

    if ( repository == null ) {
      printBasicLine( BaseMessages.getString( getPkgClazz(), noRepoProvidedMsgTkn ) );
      return null;
    }

    RepositoryDirectoryInterface directory;

    // Default is the root directory
    logDebug( allocateAndConnectRepoMsgTkn );
    directory = repository.loadRepositoryDirectoryTree();

    if ( !StringUtils.isEmpty( dirName ) ) {

      directory = directory.findDirectory( dirName ); // Find the directory name if one is specified...

      if ( directory == null ) {
        printBasicLine( BaseMessages.getString( getPkgClazz(), cannotFindDirMsgTkn, dirName ) );
      }
    }
    return directory;
  }

  public Repository establishRepositoryConnection( RepositoryMeta repositoryMeta, final String username,
                                                   final String password,
                                                   final RepositoryOperation... operations ) throws KettleException {

    Repository rep = PluginRegistry.getInstance().loadClass( RepositoryPluginType.class, repositoryMeta,
      Repository.class );
    rep.init( repositoryMeta );
    rep.getLog().setLogLevel( getLog().getLogLevel() );
    rep.connect( username, password );

    if ( operations != null ) {
      // throws KettleSecurityException if username does have permission for
      // given operations
      rep.getSecurityProvider().validateAction( operations );
    }

    return rep;
  }

  /**
   * Immutable result of a single authentication attempt in the priority cascade.
   * A non-null {@code username} means authentication was established.
   */
  private record AuthResult(String username) {
    static final AuthResult NONE = new AuthResult( null );

    static AuthResult of( String username ) {
      return username != null ? new AuthResult( username ) : NONE;
    }

    boolean isEstablished() {
      return username != null;
    }
  }

  /**
   * Establish repository connection using the authentication priority cascade.
   * If {@code password} is non-empty it is used directly, bypassing all
   * alternative auth flows. Otherwise, the cascade runs:
   * <ol>
   * <li><b>P0</b> Service account (--service-account): client_credentials grant
   * (RFC 6749 §4.4). Non-interactive; re-acquires token on expiry. No
   * fallback to human flows when this flag is set.</li>
   * <li><b>P1</b> Valid OAuth access token (requires OAuth config)</li>
   * <li><b>P1b</b> Silent OAuth token refresh — skipped for service accounts,
   * which re-request rather than refresh</li>
   * <li><b>P2</b> Existing browser session / JSESSIONID (always tried — no flags
   * needed); skipped for service accounts</li>
   * <li><b>P3</b> Interactive device code flow (--device-code, requires OAuth
   * config)</li>
   * <li><b>P4</b> Browser PKCE with headless device-code fallback
   * (--browser-auth)</li>
   * <li><b>P5</b> Username / password prompt (last resort)</li>
   * </ol>
   *
   * @param repositoryMeta    The repository metadata
   * @param username          Username hint (optional for token-based flows)
   * @param password          Password; non-empty bypasses all alternative auth
   * @param useBrowserAuth    Whether to attempt browser PKCE flow
   * @param useDeviceCode     Whether to explicitly use device code flow
   * @param useServiceAccount Whether to use client credentials (service account)
   *                          flow
   * @param operations        Required repository operations to validate
   * @return The connected Repository instance
   * @throws KettleException if connection fails
   */
  @SuppressWarnings( "java:S107" )
  public Repository establishRepositoryConnectionWithBrowserAuth( RepositoryMeta repositoryMeta,
                                                                  final String username, final String password,
                                                                  final boolean useBrowserAuth,
                                                                  final boolean useDeviceCode,
                                                                  final boolean useServiceAccount,
                                                                  final String preferredIdp,
                                                                  final RepositoryOperation... operations )
    throws KettleException {
    if ( !Utils.isEmpty( password ) ) {
      return connectToRepository( repositoryMeta, username, password, operations );
    }

    RepositoryConnectionResolution resolution = resolvePasswordlessConnection( repositoryMeta, username,
      useBrowserAuth, useDeviceCode, useServiceAccount, preferredIdp, operations );
    if ( resolution.repository() != null ) {
      return resolution.repository();
    }
    return connectToRepository( repositoryMeta, resolution.username(), resolution.password(), operations );
  }

  /**
   * Backwards-compatible overload that defaults {@code useServiceAccount} to
   * {@code false}. Existing callers (e.g. {@code KitchenCommandExecutor}) that
   * have not yet been updated to pass the service-account flag continue to work
   * without modification.
   */
  public Repository establishRepositoryConnectionWithBrowserAuth( RepositoryMeta repositoryMeta,
                                                                  final String username, final String password,
                                                                  final boolean useBrowserAuth,
                                                                  final boolean useDeviceCode,
                                                                  final boolean useServiceAccount,
                                                                  final RepositoryOperation... operations )
    throws KettleException {
    return establishRepositoryConnectionWithBrowserAuth(
      repositoryMeta, username, password, useBrowserAuth, useDeviceCode, useServiceAccount, null, operations );
  }

  public Repository establishRepositoryConnectionWithBrowserAuth( RepositoryMeta repositoryMeta,
                                                                  final String username, final String password,
                                                                  final boolean useBrowserAuth,
                                                                  final boolean useDeviceCode,
                                                                  final RepositoryOperation... operations )
    throws KettleException {
    return establishRepositoryConnectionWithBrowserAuth(
      repositoryMeta, username, password, useBrowserAuth, useDeviceCode, false, null, operations );
  }

  /**
   * Run the auth priority cascade and return the first successful
   * {@link AuthResult}.
   *
   * <p>
   * Target architecture: Pan is a thin initiator of broker-owned flows.
   * All auth orchestration (PKCE verifier, device-code polling, CC secret
   * resolution) is server-side. Pan only calls broker endpoints.
   *
   * <p>
   * Service-account mode ({@code useServiceAccount=true}) is an exclusive,
   * non-interactive path: reuse a still-valid token (P1) or acquire a new
   * broker session via the CC proxy (P0). Human flows are deliberately skipped.
   */
  private AuthResult resolveAuthentication( String serverUrl,
                                            boolean useBrowserAuth, boolean useDeviceCode, boolean useServiceAccount,
                                            String registrationId ) {

    BrowserAuthSessionHolder holder = BrowserAuthSessionHolder.getInstance();
    BrokerDiscoveryClient discoveryClient = createBrokerDiscoveryClient();
    boolean brokerAvailable = discoveryClient.isBrokerOAuthAvailable( serverUrl );
    BrokerAuthClient brokerClient = createBrokerAuthClient( discoveryClient, serverUrl );

    if ( useServiceAccount ) {
      return resolveServiceAccountAuthentication( holder, brokerClient, serverUrl, brokerAvailable,
        registrationId );
    }

    if ( !brokerAvailable ) {
      getLog().logDebug( message( "BaseCommandExecutor.Auth.BrokerUnavailable" ) );
    }

    AuthResult auth = resolveExistingAuthentication( holder, brokerClient, serverUrl, brokerAvailable );
    if ( auth.isEstablished() ) {
      return auth;
    }

    return resolveInteractiveAuthentication( holder, brokerClient, serverUrl, brokerAvailable,
      useBrowserAuth, useDeviceCode, registrationId );
  }

  /**
   * P0: Acquire a new access token via the server-side CC proxy.
   * The broker resolves selected-IdP client credentials server-side.
   */
  private AuthResult tryBrokerClientCredentials( BrowserAuthSessionHolder holder,
                                                 BrokerAuthClient brokerClient, String serverUrl,
                                                 String registrationId ) {
    getLog().logBasic( message( "BaseCommandExecutor.Auth.RequestServiceAccountToken" ) );
    BrokerFlowResult flowResult = brokerClient.clientCredentials( serverUrl, registrationId );
    if ( !flowResult.isCompleted() || StringUtils.isBlank( flowResult.accessToken() ) ) {
      getLog().logError( message( "BaseCommandExecutor.Auth.ServiceAccountBrokerFailure",
        errorOrUnknown( flowResult.error() ) ) );
      return AuthResult.NONE;
    }

    holder.clearSession( serverUrl );
    holder.storeOAuthToken( createOAuthTokenData( serverUrl, flowResult.accessToken(), registrationId,
      flowResult.accessTokenExpirySeconds(), flowResult.username() ) );
    holder.setRefreshHandle( serverUrl, null );
    holder.setBrokerAuthHandle( serverUrl, null );
    getLog().logBasic( message( "BaseCommandExecutor.Auth.ServiceAccountEstablished", flowResult.username() ) );
    return AuthResult.of( flowResult.username() );
  }

  private AuthResult tryOAuthToken( BrowserAuthSessionHolder holder, String serverUrl ) {
    if ( !holder.hasValidOAuthToken( serverUrl ) ) {
      return AuthResult.NONE;
    }
    getLog().logBasic( message( "BaseCommandExecutor.Auth.UseExistingOAuthToken" ) );
    return AuthResult.of( holder.getSessionUsername( serverUrl ) );
  }

  private AuthResult tryBrowserSession( BrowserAuthSessionHolder holder, String serverUrl ) {
    if ( !holder.hasValidSession( serverUrl ) ) {
      return AuthResult.NONE;
    }
    getLog().logBasic( message( "BaseCommandExecutor.Auth.UseExistingBrowserSession" ) );
    return AuthResult.of( holder.getSessionUsername( serverUrl ) );
  }

  /**
   * P3: Start a broker-owned device-code flow, display user code, poll for
   * completion, and exchange the resulting access token for a Pentaho session.
   */
  private AuthResult tryBrokerDeviceCode( BrowserAuthSessionHolder holder,
                                          BrokerAuthClient brokerClient, String serverUrl, String registrationId ) {
    getLog().logBasic( message( "BaseCommandExecutor.Auth.StartDeviceCode" ) );
    BrokerFlowResult startResult = brokerClient.startDeviceCodeFlow( serverUrl, registrationId );
    if ( startResult.isFailed() ) {
      getLog().logMinimal(
        message( "BaseCommandExecutor.Auth.DeviceCodeStartFailed", errorOrUnknown( startResult.error() ) ) );
      return AuthResult.NONE;
    }
    if ( startResult.authHandle() == null ) {
      getLog().logMinimal( message( "BaseCommandExecutor.Auth.DeviceCodeMissingHandle" ) );
      return AuthResult.NONE;
    }

    // Display user-code and verification URI for the user
    if ( startResult.userCode() != null ) {
      getLog().logMinimal( message( "BaseCommandExecutor.Auth.DeviceCodeVisitUrl", startResult.verificationUri() ) );
      getLog().logMinimal( message( "BaseCommandExecutor.Auth.DeviceCodeEnterCode", startResult.userCode() ) );
      if ( startResult.verificationUriComplete() != null ) {
        getLog().logMinimal( message( "BaseCommandExecutor.Auth.DeviceCodeOpenDirect",
          startResult.verificationUriComplete() ) );
      }
      logPollingWait( "device authentication" );
    }

    final String deviceHandle = startResult.authHandle();
    Thread cancelHook = new Thread( () -> brokerClient.cancelAuth( serverUrl, deviceHandle ), "broker-device-cancel" );
    Runtime.getRuntime().addShutdownHook( cancelHook );
    BrokerFlowResult pollResult;
    try {
      pollResult = brokerClient.pollUntilComplete( serverUrl, deviceHandle );
    } finally {
      try {
        Runtime.getRuntime().removeShutdownHook( cancelHook );
      } catch ( IllegalStateException ignored ) {
        // JVM is already shutting down — the hook has already fired or is firing
      }
    }
    if ( pollResult.isCancelled() ) {
      getLog().logMinimal( message( "BaseCommandExecutor.Auth.DeviceCodeCancelled" ) );
      return AuthResult.NONE;
    }
    if ( !pollResult.isCompleted() ) {
      getLog().logMinimal( message( "BaseCommandExecutor.Auth.DeviceCodeFailed",
        pollResult.error() != null ? pollResult.error()
          : message( "BaseCommandExecutor.Auth.DeviceCodeTimedOut" ) ) );
      return AuthResult.NONE;
    }

    return exchangeTokenForSession( holder, serverUrl, pollResult,
      message( "BaseCommandExecutor.Auth.FlowNameDeviceCode" ), false, registrationId );
  }

  /**
   * P4: Start a broker-owned PKCE flow. Opens the authorize URL in the default
   * browser; on failure or timeout falls back directly to device-code:
   * <ol>
   * <li>IdP PKCE (browser → IdP authorize URL → Spring Security OAuth2
   * callback)</li>
   * <li>Device-code (terminal-only, no browser required)</li>
   * </ol>
   */
  private AuthResult tryBrokerPkce( BrowserAuthSessionHolder holder,
                                    BrokerAuthClient brokerClient, String serverUrl, String registrationId ) {
    getLog().logBasic( message( "BaseCommandExecutor.Auth.PkceStart" ) );
    BrokerFlowResult startResult = brokerClient.startPkceFlow( serverUrl, registrationId );
    if ( startResult.isFailed() ) {
      getLog().logMinimal( message( "BaseCommandExecutor.Auth.PkceStartFailed",
        errorOrUnknown( startResult.error() ) ) );
      return tryBrokerDeviceCode( holder, brokerClient, serverUrl, registrationId );
    }

    String authorizeUrl = startResult.authorizeUrl();
    if ( authorizeUrl == null || authorizeUrl.isBlank() ) {
      getLog().logMinimal( message( "BaseCommandExecutor.Auth.PkceMissingAuthorizeUrl" ) );
      return tryBrokerDeviceCode( holder, brokerClient, serverUrl, registrationId );
    }

    // Authorize URL contains state and code_challenge — keep it out of production
    // logs.
    getLog().logDebug( message( "BaseCommandExecutor.Auth.PkceAuthorizeUrl", authorizeUrl ) );

    // Attempt to open the browser. If the desktop is not available (headless
    // server, SSH session), skip directly to device-code.
    boolean browserOpened = tryOpenBrowser( authorizeUrl );
    if ( !browserOpened ) {
      getLog().logMinimal( message( "BaseCommandExecutor.Auth.PkceHeadlessFallback" ) );
      return tryBrokerDeviceCode( holder, brokerClient, serverUrl, registrationId );
    }

    getLog().logMinimal( message( "BaseCommandExecutor.Auth.PkceBrowserOpened" ) );
    logPollingWait( message( "BaseCommandExecutor.Auth.PollLabelPkceCallback" ) );

    final String pkceHandle = startResult.authHandle();
    Thread cancelHook = new Thread( () -> brokerClient.cancelAuth( serverUrl, pkceHandle ), "broker-pkce-cancel" );
    Runtime.getRuntime().addShutdownHook( cancelHook );
    BrokerFlowResult pollResult;
    try {
      pollResult = brokerClient.pollUntilComplete( serverUrl, pkceHandle );
    } finally {
      try {
        Runtime.getRuntime().removeShutdownHook( cancelHook );
      } catch ( IllegalStateException ignored ) {
        // JVM is already shutting down — the hook has already fired or is firing
      }
    }
    if ( pollResult.isCompleted() ) {
      AuthResult auth = exchangeTokenForSession( holder, serverUrl, pollResult,
        message( "BaseCommandExecutor.Auth.FlowNamePkce" ), false, registrationId );
      if ( auth.isEstablished() ) {
        return auth;
      }
    }

    if ( pollResult.isCancelled() ) {
      getLog().logMinimal( message( "BaseCommandExecutor.Auth.PkceCancelled" ) );
      return AuthResult.NONE;
    }

    getLog().logMinimal( message( "BaseCommandExecutor.Auth.PkceNotCompleteFallback" ) );
    return tryBrokerDeviceCode( holder, brokerClient, serverUrl, registrationId );
  }

  /**
   * Open {@code url} in the system default browser.
   *
   * @return {@code true} if the browser was successfully launched
   */
  private boolean tryOpenBrowser( String authorizeUrl ) {
    try {
      if ( !Desktop.isDesktopSupported() ) {
        getLog().logDebug( message( "BaseCommandExecutor.Auth.DesktopNotSupported" ) );
        return false;
      }
      Desktop desktop = Desktop.getDesktop();
      if ( !desktop.isSupported( Desktop.Action.BROWSE ) ) {
        getLog().logDebug( message( "BaseCommandExecutor.Auth.DesktopBrowseNotSupported" ) );
        return false;
      }
      desktop.browse( new URI( authorizeUrl ) );
      return true;
    } catch ( Exception e ) {
      getLog().logDebug( message( "BaseCommandExecutor.Auth.BrowserOpenFailed", e.getMessage() ) );
      return false;
    }
  }

  /**
   * Stores the completed broker auth result in the holder, then returns an
   * established {@link AuthResult}.
   * <p>
   * Two scenarios, no session-exchange required in either case:
   * <ul>
   * <li><strong>Scenario A — Pentaho form login</strong> (only {@code session_id}
   * present): the server captured the PUC browser session after the user
   * authenticated with a Pentaho username/password. The JSESSIONID is stored
   * and injected as {@code Cookie: JSESSIONID=<id>} on every SOAP/REST call.
   * {@code session_expiry} (epoch-ms) is used directly so the holder's
   * validity check honours the server-reported idle timeout.</li>
   * <li><strong>Scenario B — IdP SSO</strong> ({@code access_token} present):
   * the user completed an OAuth2 login with the IdP. The access token is
   * stored as a Bearer credential; {@code PentahoOAuthTokenLoginFilter}
   * validates it on every incoming request so no server-side session is
   * needed for PUR calls. {@code accessTokenExpirySeconds} (epoch-seconds)
   * drives client-side expiry checking.</li>
   * </ul>
   * When both are present (e.g. Spring Security PKCE success path) the OAuth
   * token takes precedence because
   * { BrowserAuthSessionHolder#isOAuthSession}
   * checks {@code oauthAccessToken != null} first.
   */
  private AuthResult exchangeTokenForSession( BrowserAuthSessionHolder holder, String serverUrl,
                                              BrokerFlowResult completedResult, String flowName,
                                              boolean sessionCaptureFlow, String registrationId ) {
    boolean stored = false;

    // Scenario A: JSESSIONID — only relevant for session-capture flows.
    // PKCE and device-code flows use Bearer tokens exclusively; even if the
    // server returns a session_id as a side effect of the OAuth2 callback,
    // storing it would mix two auth mechanisms in the credential file.
    String sessionId = completedResult.sessionId();
    if ( sessionCaptureFlow && StringUtils.isNotBlank( sessionId ) ) {
      String sessionCookie = "JSESSIONID=" + sessionId;
      holder.storeSession( serverUrl, sessionId, sessionCookie,
        completedResult.username(), completedResult.sessionExpiry() );
      stored = true;
    }

    // Scenario B: IdP access token — use Bearer auth directly for all PUR calls.
    // PentahoOAuthTokenLoginFilter validates Authorization: Bearer <token> on each
    // request; no session-exchange round-trip needed.
    String accessToken = completedResult.accessToken();
    if ( StringUtils.isNotBlank( accessToken ) ) {
      // For token flows (PKCE, device-code) evict any stale JSESSIONID fields.
      // clearSessionFields() is a no-op when the session object is fresh.
      holder.clearSessionFields( serverUrl );
      holder.storeOAuthToken( createOAuthTokenData( serverUrl, accessToken, registrationId,
        completedResult.accessTokenExpirySeconds(), completedResult.username() ) );
      // Rewrite refresh metadata on every completed auth flow so stale values
      // from a previous run cannot survive. The auth handle is kept in-memory
      // only because the server-side auth state expires within minutes.
      String refreshHandle = StringUtils.trimToNull( completedResult.refreshHandle() );
      holder.setRefreshHandle( serverUrl, refreshHandle );
      holder.setBrokerAuthHandle( serverUrl,
        refreshHandle != null ? StringUtils.trimToNull( completedResult.authHandle() ) : null );
      stored = true;
    }

    if ( stored ) {
      getLog().logBasic( message( "BaseCommandExecutor.Auth.FlowSuccess",
        flowName, completedResult.username() ) );
      return AuthResult.of( completedResult.username() );
    }

    getLog().logMinimal( message( "BaseCommandExecutor.Auth.FlowMissingTokenOrSession", flowName ) );
    return AuthResult.NONE;
  }

  /**
   * P1b: Silently refresh an expired IdP access token using the stored broker
   * refresh handle. Returns {@code true} if the refresh succeeded and the new
   * access token has been persisted in {@code holder}.
   *
   * <p>
   * The broker client adds random jitter (0–300 ms) before the HTTP call to
   * spread load when many Pan clients all encounter a 401 simultaneously.
   */
  private boolean tryTokenRefresh( BrowserAuthSessionHolder holder,
                                   BrokerAuthClient brokerClient, String serverUrl ) {
    String refreshHandle = holder.getRefreshHandle( serverUrl );
    if ( refreshHandle == null || refreshHandle.isBlank() ) {
      return false;
    }
    String brokerAuthHandle = holder.getBrokerAuthHandle( serverUrl );
    BrokerFlowResult refreshResult = brokerClient.refreshAccessToken( serverUrl, refreshHandle, brokerAuthHandle );
    if ( !refreshResult.isCompleted() || StringUtils.isBlank( refreshResult.accessToken() ) ) {
      getLog().logMinimal( message( "BaseCommandExecutor.Auth.TokenRefreshFailed",
        refreshResult.error() != null
          ? refreshResult.error()
          : message( "BaseCommandExecutor.Auth.TokenRefreshMissingAccessToken" ) ) );
      // Clear the stale OAuth token and refresh handle. Without this,
      // hasValidSession() still returns true (via hasRefreshCapability()),
      // causing P2 to fire with a misleading "Using existing browser
      // authentication session" message even when the session is purely
      // OAuth-based. If a JSESSIONID is also stored (PKCE Scenario A+B),
      // clearOAuthToken() leaves it intact so P2 can use it legitimately.
      holder.clearOAuthToken( serverUrl );
      return false;
    }
    // Persist the new access token. The username stays the same; the holder
    // preserves all other session fields (session ID, IdP registration ID, etc.).
    holder.storeOAuthToken( new BrowserAuthSessionHolder.OAuthTokenData(
      serverUrl,
      refreshResult.accessToken(),
      null,
      TOKEN_TYPE_BEARER,
      holder.getOAuthIdpRegistrationId( serverUrl ),
      0,
      refreshResult.accessTokenExpirySeconds(),
      holder.getSessionUsername( serverUrl ) ) );
    // Update the refresh handle if the server rotated it.
    if ( refreshResult.refreshHandle() != null && !refreshResult.refreshHandle().isBlank() ) {
      holder.setRefreshHandle( serverUrl, refreshResult.refreshHandle() );
    }
    getLog().logDebug( message( "BaseCommandExecutor.Auth.TokenRefreshDebug",
      String.valueOf( refreshResult.accessTokenExpirySeconds() - java.time.Instant.now().getEpochSecond() ) ) );
    return true;
  }

  private BrowserAuthSessionHolder.OAuthTokenData createOAuthTokenData( String serverUrl, String accessToken,
                                                                        String idpRegistrationId,
                                                                        long accessTokenExpirySeconds,
                                                                        String username ) {
    return new BrowserAuthSessionHolder.OAuthTokenData(
      serverUrl,
      accessToken,
      null,
      TOKEN_TYPE_BEARER,
      idpRegistrationId,
      0,
      accessTokenExpirySeconds,
      username );
  }

  private String errorOrUnknown( String errorMessage ) {
    return errorMessage != null ? errorMessage : message( "BaseCommandExecutor.Auth.UnknownError" );
  }

  private void logPollingWait( String flowLabel ) {
    getLog().logBasic( message( "BaseCommandExecutor.Auth.PollingWait",
      flowLabel,
      String.valueOf( BrokerAuthClient.DEFAULT_POLL_INTERVAL_MS / 1000 ),
      String.valueOf( BrokerAuthClient.MAX_POLL_DURATION_MS / 60_000 ) ) );
  }

  private RepositoryConnectionResolution resolvePasswordlessConnection( RepositoryMeta repositoryMeta,
                                                                        String username, boolean useBrowserAuth,
                                                                        boolean useDeviceCode,
                                                                        boolean useServiceAccount,
                                                                        String preferredIdp,
                                                                        RepositoryOperation... operations )
    throws KettleException {
    String serverUrl = getRepositoryServerUrl( repositoryMeta );
    if ( serverUrl == null ) {
      return new RepositoryConnectionResolution( null,
        resolveUsernameForMissingServerUrl( username, useServiceAccount ),
        ensurePassword() );
    }

    ResolvedAuthContext authContext = new ResolvedAuthContext(
      serverUrl,
      useBrowserAuth,
      useDeviceCode,
      useServiceAccount,
      resolveBrokerRegistrationId( serverUrl, useBrowserAuth, useDeviceCode, useServiceAccount, preferredIdp ) );
    AuthResult auth = resolveAuthentication( authContext.serverUrl(), authContext.useBrowserAuth(),
      authContext.useDeviceCode(), authContext.useServiceAccount(), authContext.registrationId() );
    if ( auth.isEstablished() ) {
      return connectWithResolvedAuthentication( repositoryMeta, authContext, auth.username(), operations );
    }

    if ( useServiceAccount ) {
      throw new KettleException( message( "BaseCommandExecutor.Auth.ServiceAccountNoSession" ) );
    }
    return promptForRepositoryCredentials( username );
  }

  private RepositoryConnectionResolution connectWithResolvedAuthentication( RepositoryMeta repositoryMeta,
                                                                            ResolvedAuthContext authContext,
                                                                            String username,
                                                                            RepositoryOperation... operations )
    throws KettleException {
    try {
      Repository repository = connectToRepository( repositoryMeta, username, null, operations );
      return new RepositoryConnectionResolution( repository, username, null );
    } catch ( KettleException e ) {
      if ( !isRejectedSessionError( e ) ) {
        throw e;
      }
      return handleRejectedResolvedAuthentication( authContext, username );
    }
  }

  private RepositoryConnectionResolution handleRejectedResolvedAuthentication( ResolvedAuthContext authContext,
                                                                               String username )
    throws KettleException {
    BrowserAuthSessionHolder holder = BrowserAuthSessionHolder.getInstance();
    BrokerDiscoveryClient discoveryClient = createBrokerDiscoveryClient();
    BrokerAuthClient brokerClient = createBrokerAuthClient( discoveryClient, authContext.serverUrl() );
    if ( !attemptSilentRefresh( holder, brokerClient, authContext.serverUrl() ) ) {
      getLog().logMinimal( message( "BaseCommandExecutor.Auth.StoredSessionRejected" ) );
    }

    holder.clearSession( authContext.serverUrl() );
    AuthResult retry = resolveAuthentication( authContext.serverUrl(), authContext.useBrowserAuth(),
      authContext.useDeviceCode(), authContext.useServiceAccount(), authContext.registrationId() );
    if ( retry.isEstablished() ) {
      return new RepositoryConnectionResolution( null, retry.username(), null );
    }
    if ( authContext.useServiceAccount() ) {
      throw new KettleException( message( "BaseCommandExecutor.Auth.ServiceAccountRejectedAfterReauth" ) );
    }
    return promptForRepositoryCredentials( username );
  }

  private boolean attemptSilentRefresh( BrowserAuthSessionHolder holder,
                                        BrokerAuthClient brokerClient,
                                        String serverUrl ) {
    if ( !holder.isOAuthTokenExpiredButRefreshable( serverUrl ) ) {
      return false;
    }
    getLog().logMinimal( message( "BaseCommandExecutor.Auth.AccessTokenExpiredRefresh" ) );
    boolean refreshed = tryTokenRefresh( holder, brokerClient, serverUrl );
    if ( refreshed ) {
      getLog().logBasic( message( "BaseCommandExecutor.Auth.RefreshRetryConnection" ) );
      return true;
    }
    getLog().logMinimal( message( "BaseCommandExecutor.Auth.RefreshReauthenticate" ) );
    return false;
  }

  private boolean isRejectedSessionError( KettleException exception ) {
    String message = exception.getMessage() != null ? exception.getMessage() : "";
    return message.contains( "401" ) || message.contains( "Unauthorized" )
      || message.contains( "Invalid username or password" );
  }

  private String resolveUsernameForMissingServerUrl( String username, boolean useServiceAccount )
    throws KettleException {
    if ( useServiceAccount ) {
      throw new KettleException( message( "BaseCommandExecutor.Auth.ServiceAccountRequiresResolvableServerUrl" ) );
    }
    return ensureUsername( username );
  }

  private RepositoryConnectionResolution promptForRepositoryCredentials( String username ) throws KettleException {
    return new RepositoryConnectionResolution( null, ensureUsername( username ), ensurePassword() );
  }

  private String resolveBrokerRegistrationId( String serverUrl,
                                              boolean useBrowserAuth,
                                              boolean useDeviceCode,
                                              boolean useServiceAccount,
                                              String explicitPreferredIdp ) throws KettleException {
    String registrationId = resolvePreferredRegistrationId( explicitPreferredIdp );
    BrokerDiscoveryClient discoveryClient = createBrokerDiscoveryClient();
    List<String> availableIdps = discoveryClient.getAvailableIdps( serverUrl );

    if ( registrationId != null ) {
      if ( !availableIdps.isEmpty() && !availableIdps.contains( registrationId ) ) {
        throw new KettleException( message( "BaseCommandExecutor.Auth.PreferredIdpNotEnabled",
          registrationId, serverUrl, String.join( ", ", availableIdps ) ) );
      }
      return registrationId;
    }

    if ( availableIdps.size() == 1 ) {
      return availableIdps.get( 0 );
    }

    if ( availableIdps.size() > 1 ) {
      String message = message(
        "BaseCommandExecutor.Auth.MultipleIdpsSelected", serverUrl, String.join( ", ", availableIdps ) );
      if ( useBrowserAuth || useDeviceCode || useServiceAccount ) {
        throw new KettleException( message );
      }
      getLog().logMinimal( message( "BaseCommandExecutor.Auth.MultipleIdpsFallback", message ) );
    }

    return null;
  }

  protected BrokerDiscoveryClient createBrokerDiscoveryClient() {
    if ( brokerDiscoveryClient == null ) {
      brokerDiscoveryClient = new BrokerDiscoveryClient();
    }
    return brokerDiscoveryClient;
  }

  private BrokerAuthClient createBrokerAuthClient( BrokerDiscoveryClient discoveryClient, String serverUrl ) {
    int brokerReadTimeoutMs = CliConfig.getInstance().getBrokerReadTimeoutSeconds() * 1000;
    return discoveryClient.isDpopEnabled( serverUrl )
      ? new BrokerAuthClient( brokerReadTimeoutMs, new DPoPProofBuilder() )
      : new BrokerAuthClient( brokerReadTimeoutMs );
  }

  private String resolvePreferredRegistrationId( String explicitPreferredIdp ) {
    String registrationId = normalizePreferredIdp( explicitPreferredIdp );
    if ( registrationId != null ) {
      return registrationId;
    }
    return normalizePreferredIdp( CliConfig.getInstance().getAuthPreferredIdp() );
  }

  private String normalizePreferredIdp( String preferredIdp ) {
    return StringUtils.isBlank( preferredIdp ) ? null : preferredIdp.trim();
  }

  private AuthResult resolveServiceAccountAuthentication( BrowserAuthSessionHolder holder,
                                                          BrokerAuthClient brokerClient,
                                                          String serverUrl, boolean brokerAvailable,
                                                          String registrationId ) {
    if ( !brokerAvailable ) {
      getLog().logError( message( "BaseCommandExecutor.Auth.ServiceAccountRequiresCcSupport" ) );
      return AuthResult.NONE;
    }

    AuthResult tokenAuth = tryOAuthToken( holder, serverUrl );
    if ( tokenAuth.isEstablished() ) {
      return tokenAuth;
    }
    return tryBrokerClientCredentials( holder, brokerClient, serverUrl, registrationId );
  }

  private AuthResult resolveExistingAuthentication( BrowserAuthSessionHolder holder,
                                                    BrokerAuthClient brokerClient, String serverUrl,
                                                    boolean brokerAvailable ) {
    if ( brokerAvailable ) {
      AuthResult tokenAuth = tryOAuthToken( holder, serverUrl );
      if ( tokenAuth.isEstablished() ) {
        return tokenAuth;
      }
    }

    if ( brokerAvailable && holder.isOAuthTokenExpiredButRefreshable( serverUrl ) ) {
      getLog().logBasic( message( "BaseCommandExecutor.Auth.AccessTokenExpiredRefresh" ) );
      boolean refreshed = tryTokenRefresh( holder, brokerClient, serverUrl );
      if ( refreshed ) {
        return AuthResult.of( holder.getSessionUsername( serverUrl ) );
      }
      getLog().logMinimal( message( "BaseCommandExecutor.Auth.SilentRefreshContinueInteractive" ) );
    }

    return tryBrowserSession( holder, serverUrl );
  }

  private AuthResult resolveInteractiveAuthentication( BrowserAuthSessionHolder holder,
                                                       BrokerAuthClient brokerClient, String serverUrl,
                                                       boolean brokerAvailable,
                                                       boolean useBrowserAuth, boolean useDeviceCode,
                                                       String registrationId ) {
    if ( !brokerAvailable ) {
      return AuthResult.NONE;
    }

    if ( useDeviceCode ) {
      AuthResult deviceCodeAuth = tryBrokerDeviceCode( holder, brokerClient, serverUrl, registrationId );
      if ( deviceCodeAuth.isEstablished() ) {
        return deviceCodeAuth;
      }
    }

    if ( useBrowserAuth ) {
      return tryBrokerPkce( holder, brokerClient, serverUrl, registrationId );
    }
    return AuthResult.NONE;
  }

  private Repository connectToRepository( RepositoryMeta repositoryMeta, String username, String password,
                                          RepositoryOperation... operations ) throws KettleException {
    Repository rep = PluginRegistry.getInstance().loadClass( RepositoryPluginType.class, repositoryMeta,
      Repository.class );
    rep.init( repositoryMeta );
    rep.getLog().setLogLevel( getLog().getLogLevel() );
    try {
      rep.connect( username, password );
    } catch ( KettleException e ) {
      String msg = e.getMessage() != null ? e.getMessage() : "";
      if ( msg.contains( "401" ) || msg.contains( "Unauthorized" ) || msg.contains( "FailedLogin" ) ) {
        throw new KettleException( message( "BaseCommandExecutor.Auth.RepositoryInvalidCredentials" ) );
      }
      throw e;
    }
    if ( operations != null ) {
      rep.getSecurityProvider().validateAction( operations );
    }
    return rep;
  }

  private String ensureUsername( String existingUsername ) throws KettleException {
    if ( !Utils.isEmpty( existingUsername ) ) {
      return existingUsername;
    }
    String prompted = promptForUsername();
    if ( Utils.isEmpty( prompted ) ) {
      throw new KettleException( message( "BaseCommandExecutor.Auth.RepositoryMissingUsername" ) );
    }
    return prompted;
  }

  private String ensurePassword() throws KettleException {
    String prompted = promptForPassword();
    if ( Utils.isEmpty( prompted ) ) {
      throw new KettleException( message( "BaseCommandExecutor.Auth.RepositoryMissingPassword" ) );
    }
    return prompted;
  }

  /**
   * Get the server URL from repository metadata.
   * This extracts the base URL for PUR repositories.
   *
   * @param repositoryMeta The repository metadata
   * @return The server URL, or null if not available
   */
  protected String getRepositoryServerUrl( RepositoryMeta repositoryMeta ) {
    // Try to get the URL from repository location
    // This works for PurRepositoryMeta which has getRepositoryLocation()
    try {
      Method getLocationMethod = repositoryMeta.getClass().getMethod( "getRepositoryLocation" );
      Object location = getLocationMethod.invoke( repositoryMeta );
      if ( location != null ) {
        Method getUrlMethod = location.getClass().getMethod( "getUrl" );
        Object url = getUrlMethod.invoke( location );
        if ( url != null ) {
          return url.toString();
        }
      }
    } catch ( Exception e ) {
      // Not a PUR repository or reflection failed
      getLog().logDebug( message( "BaseCommandExecutor.Auth.RepositoryUrlLookupFailed", e.getMessage() ) );
    }
    return null;
  }

  /**
   * Prompt for password from console.
   *
   * @return The password entered, or null if console not available
   */
  protected String promptForPassword() {
    Console console = System.console();
    if ( console != null ) {
      getLog().logMinimal( message( "BaseCommandExecutor.Auth.PromptPassword" ) );
      char[] passwordChars = console.readPassword();
      if ( passwordChars != null ) {
        return new String( passwordChars );
      }
    }
    return null;
  }

  /**
   * Prompt for username from console.
   *
   * @return The username entered, or null if console not available
   */
  protected String promptForUsername() {
    Console console = System.console();
    if ( console != null ) {
      getLog().logMinimal( message( "BaseCommandExecutor.Auth.PromptUsername" ) );
      String username = console.readLine();
      if ( username != null && !username.trim().isEmpty() ) {
        return username.trim();
      }
    }
    return null;
  }

  public void printRepositoryDirectories( Repository repository, RepositoryDirectoryInterface directory )
    throws KettleException {

    String[] directories = repository.getDirectoryNames( directory.getObjectId() );

    if ( directories != null ) {
      for ( String dir : directories ) {
        System.out.println( dir );
      }
    }
  }

  protected void printParameter( String name, String value, String defaultValue, String description ) {
    if ( Utils.isEmpty( defaultValue ) ) {
      printBasicLine( message( "BaseCommandExecutor.PrintParameter.NoDefault",
        name, Const.NVL( value, "" ), Const.NVL( description, "" ) ) );
    } else {
      printBasicLine( message( "BaseCommandExecutor.PrintParameter.WithDefault",
        name, Const.NVL( value, "" ), defaultValue, Const.NVL( description, "" ) ) );
    }
  }

  protected String[] convert( Map<String, String> map ) {

    List<String> list = new ArrayList<>();

    if ( map != null ) {
      map.keySet().forEach( key -> list.add( key + "=" + map.get( key ) ) );
    }

    return list.toArray( new String[] {} );
  }

  public boolean isEnabled( final String value ) {
    return YES.equalsIgnoreCase( value ) || Boolean.parseBoolean(
      value ); // both are NPE safe, both are case-insensitive
  }

  /**
   * Decodes the provided base64String into a default path. Resulting zip file is
   * UUID-named for concurrency’s sake.
   *
   * @param base64Zip       BASE64 representation of a file
   * @param deleteOnJvmExit true if we want this newly generated file to be marked
   *                        for deletion on JVM termination, false otherwise
   * @return File the newly created File
   */
  public File decodeBase64ToZipFile( Serializable base64Zip, boolean deleteOnJvmExit ) throws IOException {

    String basePath = !StringUtils.isEmpty( Const.getUserHomeDirectory() ) ? Const.getUserHomeDirectory()
      : new File( "." ).getAbsolutePath();
    String zipFilePath = basePath + File.separator + java.util.UUID.randomUUID() + ".zip";
    File f = decodeBase64ToZipFile( base64Zip, zipFilePath );

    if ( f != null && deleteOnJvmExit ) {
      f.deleteOnExit();
    }

    return f;
  }

  /**
   * Decodes the provided base64String into the specified filePath. Parent
   * directories must already exist.
   *
   * @param base64Zip BASE64 representation of a file
   * @param filePath  String The path to which the base64String is to be decoded
   * @return File the newly created File
   */
  public File decodeBase64ToZipFile( Serializable base64Zip, String filePath ) throws IOException {

    if ( base64Zip == null || Utils.isEmpty( base64Zip.toString() ) ) {
      return null;
    }

    // Decode base64String to byte[]
    byte[] decodedBytes = Base64.getDecoder().decode( base64Zip.toString() );
    File file = new File( filePath );

    // Try-with-resources, write to file, ensure fos is always closed
    try ( FileOutputStream fos = new FileOutputStream( file ) ) {
      fos.write( decodedBytes );
    }

    return file;
  }

  /**
   * Pass the plugin parameters to the CommandLineOptionProvider for validation
   * and setting the plugin context.
   *
   * @param log        LogChannelInterface for logging
   * @param params     Params containing the plugin parameters
   * @param repository Repository object when connected
   * @return CommandExecutorResult contains the result of the of validation.
   */
  protected static CommandExecutorResult validateAndSetPluginContext( LogChannelInterface log, Params params,
                                                                      Repository repository ) throws KettleException {
    CommandExecutorResult result = null;
    Map<String, String> paramMap = params.getPluginParams();

    for ( CommandLineOptionProvider provider : PluginServiceLoader.loadServices( CommandLineOptionProvider.class ) ) {
      result = provider.handleParameter( log, paramMap, repository );
      if ( result.getCode() != 0 ) {
        log.logError( result.getDescription() );
        break; // if result is NOT SUCCESS, break out of the loop
      }
    }

    return result;
  }

  public LogChannelInterface getLog() {
    return log;
  }

  public void setLog( LogChannelInterface log ) {
    this.log = log;
  }

  public Class<?> getPkgClazz() {
    return pkgClazz;
  }

  public void setPkgClazz( Class<?> pkgClazz ) {
    this.pkgClazz = pkgClazz;
  }

  public IMetaStore getMetaStore() {
    return metaStore;
  }

  public void setMetaStore( IMetaStore metaStore ) {
    this.metaStore = metaStore;
  }

  public SimpleDateFormat getDateFormat() {
    return dateFormat;
  }

  public void setDateFormat( SimpleDateFormat dateFormat ) {
    this.dateFormat = dateFormat;
  }

  public Result getResult() {
    return result;
  }

  public void setResult( Result result ) {
    this.result = result;
  }

  public Bowl getBowl() {
    return bowl;
  }

  public void setBowl( Bowl bowl ) {
    this.bowl = Objects.requireNonNull( bowl );
  }
}
