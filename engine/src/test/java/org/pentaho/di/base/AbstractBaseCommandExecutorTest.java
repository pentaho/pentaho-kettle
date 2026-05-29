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

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.pentaho.di.cli.auth.BrokerAuthClient;
import org.pentaho.di.cli.auth.BrokerAuthClient.BrokerFlowResult;
import org.pentaho.di.cli.auth.BrokerDiscoveryClient;
import org.pentaho.di.cli.auth.BrowserAuthSessionHolder;
import org.pentaho.di.cli.config.CliConfig;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.core.service.PluginServiceLoader;
import org.pentaho.di.pan.CommandExecutorResult;
import org.pentaho.di.pan.CommandLineOptionProvider;
import org.pentaho.di.pan.Pan;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.version.BuildVersion;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbstractBaseCommandExecutorTest {

  private static final String SERVER_URL = "http://localhost:8080/pentaho";

  private AbstractBaseCommandExecutor executor;
  private LogChannelInterface executorLog;
  private CliConfig cliConfig;
  private BrowserAuthSessionHolder holder;
  private BrokerDiscoveryClient discoveryClient;
  private BrokerAuthClient brokerAuthClient;
  private RepositoryMeta repositoryMeta;
  private Repository repository;
  private String repositoryServerUrl;
  private String promptedUsername;
  private String promptedPassword;
  private Repository repositoryToReturn;
  private String capturedUsername;
  private String capturedPassword;
  private String capturedAuthorizeUrl;
  private int connectCallCount;
  private int loadRepositoryInfoCallCount;
  private boolean browserOpenResult;
  private boolean delegateBrowserOpenToSuper;
  private RepositoriesMeta repositoriesMetaToReturn;
  private KettleException connectFailure;
  private int connectFailuresRemaining;

  @Before
  public void setUp() {
    cliConfig = mock( CliConfig.class );
    when( cliConfig.getAuthPreferredIdp() ).thenReturn( null );
    when( cliConfig.getBrokerReadTimeoutSeconds() ).thenReturn( 30 );

    holder = mock( BrowserAuthSessionHolder.class );
    discoveryClient = mock( BrokerDiscoveryClient.class );
    brokerAuthClient = mock( BrokerAuthClient.class );
    repositoryMeta = mock( RepositoryMeta.class );
    repository = mock( Repository.class );

    executor = spy( new AbstractBaseCommandExecutor() { } );
    executorLog = mock( LogChannelInterface.class );
    when( executorLog.isDebug() ).thenReturn( true );
    when( executorLog.getLogLevel() ).thenReturn( LogLevel.DEBUG );
    executor.setLog( executorLog );
    executor.setPkgClazz( Pan.class );

    repositoryServerUrl = SERVER_URL;
    repositoryToReturn = repository;
    browserOpenResult = true;

    try {
      doAnswer( invocation -> {
        loadRepositoryInfoCallCount++;
        if ( repositoriesMetaToReturn != null ) {
          return repositoriesMetaToReturn;
        }
        return invocation.callRealMethod();
      } ).when( executor ).loadRepositoryInfo( anyString(), anyString() );
    } catch ( KettleException e ) {
      throw new AssertionError( e );
    }
    doAnswer( invocation -> repositoryServerUrl ).when( executor ).getRepositoryServerUrl( any( RepositoryMeta.class ) );
    doAnswer( invocation -> promptedUsername ).when( executor ).promptForUsername();
    doAnswer( invocation -> promptedPassword ).when( executor ).promptForPassword();
    doAnswer( invocation -> discoveryClient ).when( executor ).createBrokerDiscoveryClient();
    doAnswer( invocation -> brokerAuthClient ).when( executor ).createBrokerAuthClient( any( BrokerDiscoveryClient.class ), anyString() );
    try {
      doAnswer( invocation -> {
        connectCallCount++;
        capturedUsername = invocation.getArgument( 1 );
        capturedPassword = invocation.getArgument( 2 );
        if ( connectFailure != null && connectFailuresRemaining > 0 ) {
          connectFailuresRemaining--;
          throw connectFailure;
        }
        return repositoryToReturn;
      } ).when( executor ).connectToRepository(
        any( RepositoryMeta.class ),
        ArgumentMatchers.any(),
        ArgumentMatchers.any(),
        any( RepositoryOperation[].class ) );
    } catch ( KettleException e ) {
      throw new AssertionError( e );
    }
    doAnswer( invocation -> {
      if ( delegateBrowserOpenToSuper ) {
        return invocation.callRealMethod();
      }
      capturedAuthorizeUrl = invocation.getArgument( 0 );
      return browserOpenResult;
    } ).when( executor ).tryOpenBrowser( anyString() );
  }

  @Test
  public void establishRepositoryConnectionWithBrowserAuthUsesProvidedPassword() throws Exception {
    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig() ) {
      Repository result = executor.establishRepositoryConnectionWithBrowserAuth(
        repositoryMeta, "alice", "secret", true, true, false, "keycloak",
        RepositoryOperation.EXECUTE_TRANSFORMATION );

      assertSame( repository, result );
      assertEquals( "alice", capturedUsername );
      assertEquals( "secret", capturedPassword );
      assertEquals( 1, connectCallCount );
      mockedCliConfig.verifyNoMoreInteractions();
    }
  }

  @Test
  public void establishRepositoryConnectionWithBrowserAuthLegacyOverloadUsesProvidedPassword() throws Exception {
    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig() ) {
      Repository result = executor.establishRepositoryConnectionWithBrowserAuth(
        repositoryMeta, "alice", "secret", true, true,
        RepositoryOperation.EXECUTE_TRANSFORMATION );

      assertSame( repository, result );
      assertEquals( "alice", capturedUsername );
      assertEquals( "secret", capturedPassword );
      assertEquals( 1, connectCallCount );
      mockedCliConfig.verifyNoMoreInteractions();
    }
  }

  @Test
  public void establishRepositoryConnectionWithBrowserAuthServiceAccountOverloadUsesProvidedPassword() throws Exception {
    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig() ) {
      Repository result = executor.establishRepositoryConnectionWithBrowserAuth(
        repositoryMeta, "alice", "secret", true, true, false,
        RepositoryOperation.EXECUTE_TRANSFORMATION );

      assertSame( repository, result );
      assertEquals( "alice", capturedUsername );
      assertEquals( "secret", capturedPassword );
      mockedCliConfig.verifyNoMoreInteractions();
    }
  }

  @Test
  public void passwordlessConnectionWithMissingServerUrlPromptsForCredentials() throws Exception {
    repositoryServerUrl = null;
    promptedUsername = "prompt-user";
    promptedPassword = "prompt-pass";

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig() ) {
      Repository result = executor.establishRepositoryConnectionWithBrowserAuth(
        repositoryMeta, null, "", false, false, false, (String) null,
        RepositoryOperation.EXECUTE_TRANSFORMATION );

      assertSame( repository, result );
      assertEquals( "prompt-user", capturedUsername );
      assertEquals( "prompt-pass", capturedPassword );
      assertEquals( 1, connectCallCount );
      mockedCliConfig.verify( CliConfig::getInstance, never() );
    }
  }

  @Test
  public void serviceAccountRequiresResolvableServerUrl() {
    repositoryServerUrl = null;

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig() ) {
      try {
        executor.establishRepositoryConnectionWithBrowserAuth(
          repositoryMeta, null, "", false, false, true, (String) null,
          RepositoryOperation.EXECUTE_TRANSFORMATION );
        fail( "Expected service-account server URL failure" );
      } catch ( KettleException e ) {
        assertTrue( e.getMessage().contains( "service-account" ) );
      }
      mockedCliConfig.verify( CliConfig::getInstance, never() );
    }
  }

  @Test
  public void establishRepositoryConnectionWithBrowserAuthUsesExistingOAuthToken() throws Exception {
    when( discoveryClient.getAvailableIdps( SERVER_URL ) ).thenReturn( Collections.emptyList() );
    when( discoveryClient.isBrokerOAuthAvailable( SERVER_URL ) ).thenReturn( true );
    when( holder.hasValidOAuthToken( SERVER_URL ) ).thenReturn( true );
    when( holder.getSessionUsername( SERVER_URL ) ).thenReturn( "oauth-user" );

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig();
          MockedStatic<BrowserAuthSessionHolder> mockedHolder = mockHolder() ) {
      Repository result = executor.establishRepositoryConnectionWithBrowserAuth(
        repositoryMeta, "ignored", "", false, false, false, (String) null,
        RepositoryOperation.EXECUTE_TRANSFORMATION );

      assertSame( repository, result );
      assertEquals( "oauth-user", capturedUsername );
      assertNull( capturedPassword );
      verify( holder ).hasValidOAuthToken( SERVER_URL );
      verify( holder, never() ).hasValidSession( SERVER_URL );
      mockedCliConfig.verify( CliConfig::getInstance );
      mockedHolder.verify( BrowserAuthSessionHolder::getInstance );
    }
  }

  @Test
  public void establishRepositoryConnectionWithBrowserAuthRefreshesExpiredToken() throws Exception {
    when( discoveryClient.getAvailableIdps( SERVER_URL ) ).thenReturn( Collections.emptyList() );
    when( discoveryClient.isBrokerOAuthAvailable( SERVER_URL ) ).thenReturn( true );
    when( holder.hasValidOAuthToken( SERVER_URL ) ).thenReturn( false );
    when( holder.isOAuthTokenExpiredButRefreshable( SERVER_URL ) ).thenReturn( true );
    when( holder.getRefreshHandle( SERVER_URL ) ).thenReturn( "refresh-handle" );
    when( holder.getBrokerAuthHandle( SERVER_URL ) ).thenReturn( "broker-handle" );
    when( holder.getOAuthIdpRegistrationId( SERVER_URL ) ).thenReturn( "keycloak" );
    when( holder.getSessionUsername( SERVER_URL ) ).thenReturn( "refreshed-user" );
    when( brokerAuthClient.refreshAccessToken( SERVER_URL, "refresh-handle", "broker-handle" ) )
      .thenReturn( completedResult( "refreshed-user", "new-token", "rotated-refresh" ) );

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig();
          MockedStatic<BrowserAuthSessionHolder> mockedHolder = mockHolder() ) {
      Repository result = executor.establishRepositoryConnectionWithBrowserAuth(
        repositoryMeta, "ignored", "", false, false, false, (String) null,
        RepositoryOperation.EXECUTE_TRANSFORMATION );

      assertSame( repository, result );
      assertEquals( "refreshed-user", capturedUsername );
      assertNull( capturedPassword );
      verify( holder ).storeOAuthToken( any() );
      verify( holder ).setRefreshHandle( SERVER_URL, "rotated-refresh" );
      mockedCliConfig.verify( CliConfig::getInstance, atLeastOnce() );
      mockedHolder.verify( BrowserAuthSessionHolder::getInstance );
    }
  }

  @Test
  public void browserAuthFallsBackToDeviceCodeWhenBrowserCannotOpen() throws Exception {
    browserOpenResult = false;
    when( discoveryClient.getAvailableIdps( SERVER_URL ) ).thenReturn( Collections.singletonList( "keycloak" ) );
    when( discoveryClient.isBrokerOAuthAvailable( SERVER_URL ) ).thenReturn( true );
    when( holder.hasValidOAuthToken( SERVER_URL ) ).thenReturn( false );
    when( holder.isOAuthTokenExpiredButRefreshable( SERVER_URL ) ).thenReturn( false );
    when( holder.hasValidSession( SERVER_URL ) ).thenReturn( false );
    when( brokerAuthClient.startPkceFlow( SERVER_URL, "keycloak" ) )
      .thenReturn( pendingPkceStart( "pkce-handle", "http://auth.example" ) );
    when( brokerAuthClient.startDeviceCodeFlow( SERVER_URL, "keycloak" ) )
      .thenReturn( pendingDeviceCodeStart( "device-handle" ) );
    when( brokerAuthClient.pollUntilComplete( SERVER_URL, "device-handle" ) )
      .thenReturn( completedResult( "device-user", "device-token", "device-refresh" ) );

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig();
          MockedStatic<BrowserAuthSessionHolder> mockedHolder = mockHolder() ) {
      Repository result = executor.establishRepositoryConnectionWithBrowserAuth(
        repositoryMeta, null, "", true, false, false, (String) null,
        RepositoryOperation.EXECUTE_TRANSFORMATION );

      assertSame( repository, result );
      assertEquals( "http://auth.example", capturedAuthorizeUrl );
      assertEquals( "device-user", capturedUsername );
      verify( holder ).storeOAuthToken( any() );
      mockedCliConfig.verify( CliConfig::getInstance, atLeastOnce() );
      mockedHolder.verify( BrowserAuthSessionHolder::getInstance );
    }
  }

  @Test
  public void deviceCodeMissingHandleFallsBackToPromptedCredentials() throws Exception {
    promptedUsername = "prompt-user";
    promptedPassword = "prompt-pass";
    when( discoveryClient.getAvailableIdps( SERVER_URL ) ).thenReturn( Collections.singletonList( "keycloak" ) );
    when( discoveryClient.isBrokerOAuthAvailable( SERVER_URL ) ).thenReturn( true );
    when( holder.hasValidOAuthToken( SERVER_URL ) ).thenReturn( false );
    when( holder.isOAuthTokenExpiredButRefreshable( SERVER_URL ) ).thenReturn( false );
    when( holder.hasValidSession( SERVER_URL ) ).thenReturn( false );
    when( brokerAuthClient.startDeviceCodeFlow( SERVER_URL, "keycloak" ) )
      .thenReturn( pendingDeviceCodeStart( null ) );

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig();
          MockedStatic<BrowserAuthSessionHolder> mockedHolder = mockHolder() ) {
      Repository result = executor.establishRepositoryConnectionWithBrowserAuth(
        repositoryMeta, null, "", false, true, false, (String) null,
        RepositoryOperation.EXECUTE_TRANSFORMATION );

      assertSame( repository, result );
      assertEquals( "prompt-user", capturedUsername );
      assertEquals( "prompt-pass", capturedPassword );
      mockedCliConfig.verify( CliConfig::getInstance, atLeastOnce() );
      mockedHolder.verify( BrowserAuthSessionHolder::getInstance );
    }
  }

  @Test
  public void deviceCodeCancelledFallsBackToPromptedCredentials() throws Exception {
    promptedUsername = "prompt-user";
    promptedPassword = "prompt-pass";
    when( discoveryClient.getAvailableIdps( SERVER_URL ) ).thenReturn( Collections.singletonList( "keycloak" ) );
    when( discoveryClient.isBrokerOAuthAvailable( SERVER_URL ) ).thenReturn( true );
    when( holder.hasValidOAuthToken( SERVER_URL ) ).thenReturn( false );
    when( holder.isOAuthTokenExpiredButRefreshable( SERVER_URL ) ).thenReturn( false );
    when( holder.hasValidSession( SERVER_URL ) ).thenReturn( false );
    when( brokerAuthClient.startDeviceCodeFlow( SERVER_URL, "keycloak" ) )
      .thenReturn( pendingDeviceCodeStart( "device-handle" ) );
    when( brokerAuthClient.pollUntilComplete( SERVER_URL, "device-handle" ) )
      .thenReturn( cancelledResult( "device-handle" ) );

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig();
          MockedStatic<BrowserAuthSessionHolder> mockedHolder = mockHolder() ) {
      Repository result = executor.establishRepositoryConnectionWithBrowserAuth(
        repositoryMeta, null, "", false, true, false, (String) null,
        RepositoryOperation.EXECUTE_TRANSFORMATION );

      assertSame( repository, result );
      assertEquals( "prompt-user", capturedUsername );
      assertEquals( "prompt-pass", capturedPassword );
      mockedCliConfig.verify( CliConfig::getInstance, atLeastOnce() );
      mockedHolder.verify( BrowserAuthSessionHolder::getInstance );
    }
  }

  @Test
  public void deviceCodeTimeoutFallsBackToPromptedCredentials() throws Exception {
    promptedUsername = "prompt-user";
    promptedPassword = "prompt-pass";
    when( discoveryClient.getAvailableIdps( SERVER_URL ) ).thenReturn( Collections.singletonList( "keycloak" ) );
    when( discoveryClient.isBrokerOAuthAvailable( SERVER_URL ) ).thenReturn( true );
    when( holder.hasValidOAuthToken( SERVER_URL ) ).thenReturn( false );
    when( holder.isOAuthTokenExpiredButRefreshable( SERVER_URL ) ).thenReturn( false );
    when( holder.hasValidSession( SERVER_URL ) ).thenReturn( false );
    when( brokerAuthClient.startDeviceCodeFlow( SERVER_URL, "keycloak" ) )
      .thenReturn( pendingDeviceCodeStart( "device-handle" ) );
    when( brokerAuthClient.pollUntilComplete( SERVER_URL, "device-handle" ) )
      .thenReturn( incompleteResult( "device-handle", null ) );

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig();
          MockedStatic<BrowserAuthSessionHolder> mockedHolder = mockHolder() ) {
      Repository result = executor.establishRepositoryConnectionWithBrowserAuth(
        repositoryMeta, null, "", false, true, false, (String) null,
        RepositoryOperation.EXECUTE_TRANSFORMATION );

      assertSame( repository, result );
      assertEquals( "prompt-user", capturedUsername );
      assertEquals( "prompt-pass", capturedPassword );
      mockedCliConfig.verify( CliConfig::getInstance, atLeastOnce() );
      mockedHolder.verify( BrowserAuthSessionHolder::getInstance );
    }
  }

  @Test
  public void pkceMissingAuthorizeUrlFallsBackToDeviceCode() throws Exception {
    when( discoveryClient.getAvailableIdps( SERVER_URL ) ).thenReturn( Collections.singletonList( "keycloak" ) );
    when( discoveryClient.isBrokerOAuthAvailable( SERVER_URL ) ).thenReturn( true );
    when( holder.hasValidOAuthToken( SERVER_URL ) ).thenReturn( false );
    when( holder.isOAuthTokenExpiredButRefreshable( SERVER_URL ) ).thenReturn( false );
    when( holder.hasValidSession( SERVER_URL ) ).thenReturn( false );
    when( brokerAuthClient.startPkceFlow( SERVER_URL, "keycloak" ) )
      .thenReturn( pendingPkceStart( "pkce-handle", null ) );
    when( brokerAuthClient.startDeviceCodeFlow( SERVER_URL, "keycloak" ) )
      .thenReturn( pendingDeviceCodeStart( "device-handle" ) );
    when( brokerAuthClient.pollUntilComplete( SERVER_URL, "device-handle" ) )
      .thenReturn( completedResult( "device-user", "device-token", "refresh" ) );

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig();
          MockedStatic<BrowserAuthSessionHolder> mockedHolder = mockHolder() ) {
      Repository result = executor.establishRepositoryConnectionWithBrowserAuth(
        repositoryMeta, null, "", true, false, false, (String) null,
        RepositoryOperation.EXECUTE_TRANSFORMATION );

      assertSame( repository, result );
      assertEquals( "device-user", capturedUsername );
      mockedCliConfig.verify( CliConfig::getInstance, atLeastOnce() );
      mockedHolder.verify( BrowserAuthSessionHolder::getInstance );
    }
  }

  @Test
  public void pkceCancelledFallsBackToPromptedCredentials() throws Exception {
    promptedUsername = "prompt-user";
    promptedPassword = "prompt-pass";
    when( discoveryClient.getAvailableIdps( SERVER_URL ) ).thenReturn( Collections.singletonList( "keycloak" ) );
    when( discoveryClient.isBrokerOAuthAvailable( SERVER_URL ) ).thenReturn( true );
    when( holder.hasValidOAuthToken( SERVER_URL ) ).thenReturn( false );
    when( holder.isOAuthTokenExpiredButRefreshable( SERVER_URL ) ).thenReturn( false );
    when( holder.hasValidSession( SERVER_URL ) ).thenReturn( false );
    when( brokerAuthClient.startPkceFlow( SERVER_URL, "keycloak" ) )
      .thenReturn( pendingPkceStart( "pkce-handle", "http://auth.example" ) );
    when( brokerAuthClient.pollUntilComplete( SERVER_URL, "pkce-handle" ) )
      .thenReturn( cancelledResult( "pkce-handle" ) );

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig();
          MockedStatic<BrowserAuthSessionHolder> mockedHolder = mockHolder() ) {
      Repository result = executor.establishRepositoryConnectionWithBrowserAuth(
        repositoryMeta, null, "", true, false, false, (String) null,
        RepositoryOperation.EXECUTE_TRANSFORMATION );

      assertSame( repository, result );
      assertEquals( "prompt-user", capturedUsername );
      assertEquals( "prompt-pass", capturedPassword );
      mockedCliConfig.verify( CliConfig::getInstance, atLeastOnce() );
      mockedHolder.verify( BrowserAuthSessionHolder::getInstance );
    }
  }

  @Test
  public void pkceIncompletePollFallsBackToDeviceCode() throws Exception {
    when( discoveryClient.getAvailableIdps( SERVER_URL ) ).thenReturn( Collections.singletonList( "keycloak" ) );
    when( discoveryClient.isBrokerOAuthAvailable( SERVER_URL ) ).thenReturn( true );
    when( holder.hasValidOAuthToken( SERVER_URL ) ).thenReturn( false );
    when( holder.isOAuthTokenExpiredButRefreshable( SERVER_URL ) ).thenReturn( false );
    when( holder.hasValidSession( SERVER_URL ) ).thenReturn( false );
    when( brokerAuthClient.startPkceFlow( SERVER_URL, "keycloak" ) )
      .thenReturn( pendingPkceStart( "pkce-handle", "http://auth.example" ) );
    when( brokerAuthClient.pollUntilComplete( SERVER_URL, "pkce-handle" ) )
      .thenReturn( incompleteResult( "pkce-handle", "still pending" ) );
    when( brokerAuthClient.startDeviceCodeFlow( SERVER_URL, "keycloak" ) )
      .thenReturn( pendingDeviceCodeStart( "device-handle" ) );
    when( brokerAuthClient.pollUntilComplete( SERVER_URL, "device-handle" ) )
      .thenReturn( completedResult( "device-user", "device-token", "refresh" ) );

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig();
          MockedStatic<BrowserAuthSessionHolder> mockedHolder = mockHolder() ) {
      Repository result = executor.establishRepositoryConnectionWithBrowserAuth(
        repositoryMeta, null, "", true, false, false, (String) null,
        RepositoryOperation.EXECUTE_TRANSFORMATION );

      assertSame( repository, result );
      assertEquals( "device-user", capturedUsername );
      mockedCliConfig.verify( CliConfig::getInstance, atLeastOnce() );
      mockedHolder.verify( BrowserAuthSessionHolder::getInstance );
    }
  }

  @Test
  public void multipleIdpsWithInteractiveAuthRequiresPreferredIdp() {
    when( discoveryClient.getAvailableIdps( SERVER_URL ) ).thenReturn( Arrays.asList( "keycloak", "azure" ) );

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig() ) {
      try {
        executor.establishRepositoryConnectionWithBrowserAuth(
          repositoryMeta, null, "", true, false, false, (String) null,
          RepositoryOperation.EXECUTE_TRANSFORMATION );
        fail( "Expected multiple-IdP selection failure" );
      } catch ( KettleException e ) {
        assertTrue( e.getMessage().contains( "Multiple OAuth IdPs" ) );
      }
      mockedCliConfig.verify( CliConfig::getInstance );
    }
  }

  @Test
  public void explicitPreferredIdpMustBeEnabledOnServer() {
    when( discoveryClient.getAvailableIdps( SERVER_URL ) ).thenReturn( Collections.singletonList( "keycloak" ) );

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig() ) {
      try {
        executor.establishRepositoryConnectionWithBrowserAuth(
          repositoryMeta, null, "", true, false, false, "azure",
          RepositoryOperation.EXECUTE_TRANSFORMATION );
        fail( "Expected preferred-IdP validation failure" );
      } catch ( KettleException e ) {
        assertTrue( e.getMessage() != null && !e.getMessage().isEmpty() );
        assertEquals( 0, connectCallCount );
      }
      mockedCliConfig.verify( CliConfig::getInstance, never() );
    }
  }

  @Test
  public void establishRepositoryConnectionWithBrowserAuthUsesServiceAccountClientCredentials() throws Exception {
    when( discoveryClient.getAvailableIdps( SERVER_URL ) ).thenReturn( Collections.singletonList( "svc" ) );
    when( discoveryClient.isBrokerOAuthAvailable( SERVER_URL ) ).thenReturn( true );
    when( holder.hasValidOAuthToken( SERVER_URL ) ).thenReturn( false );
    when( brokerAuthClient.clientCredentials( SERVER_URL, "svc" ) )
      .thenReturn( completedResult( "service-user", "service-token", null ) );

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig();
          MockedStatic<BrowserAuthSessionHolder> mockedHolder = mockHolder() ) {
      Repository result = executor.establishRepositoryConnectionWithBrowserAuth(
        repositoryMeta, null, "", false, false, true, (String) null,
        RepositoryOperation.EXECUTE_TRANSFORMATION );

      assertSame( repository, result );
      assertEquals( "service-user", capturedUsername );
      assertNull( capturedPassword );
      verify( holder ).clearSession( SERVER_URL );
      verify( holder ).storeOAuthToken( any() );
      mockedCliConfig.verify( CliConfig::getInstance, atLeastOnce() );
      mockedHolder.verify( BrowserAuthSessionHolder::getInstance );
    }
  }

  @Test
  public void tryOpenBrowserReturnsFalseForInvalidUrl() {
    delegateBrowserOpenToSuper = true;
    assertFalse( executor.tryOpenBrowser( "not a uri" ) );
  }

  @Test
  public void tryOpenBrowserReturnsFalseWhenDesktopIsUnsupported() {
    AbstractBaseCommandExecutor utilityExecutor = newUtilityExecutor();

    try ( MockedStatic<Desktop> mockedDesktop = mockStatic( Desktop.class ) ) {
      mockedDesktop.when( Desktop::isDesktopSupported ).thenReturn( false );

      assertFalse( utilityExecutor.tryOpenBrowser( "http://auth.example" ) );
    }
  }

  @Test
  public void tryOpenBrowserReturnsFalseWhenBrowseActionIsUnsupported() {
    AbstractBaseCommandExecutor utilityExecutor = newUtilityExecutor();
    Desktop desktop = mock( Desktop.class );

    try ( MockedStatic<Desktop> mockedDesktop = mockStatic( Desktop.class ) ) {
      mockedDesktop.when( Desktop::isDesktopSupported ).thenReturn( true );
      mockedDesktop.when( Desktop::getDesktop ).thenReturn( desktop );
      when( desktop.isSupported( Desktop.Action.BROWSE ) ).thenReturn( false );

      assertFalse( utilityExecutor.tryOpenBrowser( "http://auth.example" ) );
    }
  }

  @Test
  public void tryOpenBrowserReturnsFalseWhenBrowseThrows() throws Exception {
    AbstractBaseCommandExecutor utilityExecutor = newUtilityExecutor();
    Desktop desktop = mock( Desktop.class );

    try ( MockedStatic<Desktop> mockedDesktop = mockStatic( Desktop.class ) ) {
      mockedDesktop.when( Desktop::isDesktopSupported ).thenReturn( true );
      mockedDesktop.when( Desktop::getDesktop ).thenReturn( desktop );
      when( desktop.isSupported( Desktop.Action.BROWSE ) ).thenReturn( true );
      doThrow( new RuntimeException( "boom" ) ).when( desktop ).browse( any() );

      assertFalse( utilityExecutor.tryOpenBrowser( "http://auth.example" ) );
    }
  }

  @Test
  public void exitWithStatusUpdatesResult() {
    assertEquals( 7, executor.exitWithStatus( 7 ).getExitStatus() );
    assertEquals( 7, executor.getResult().getExitStatus() );
  }

  @Test
  public void calculateAndPrintElapsedTimeCoversAllRanges() {
    DateRange lessThanMinute = new DateRange( 0L, 45_000L );
    DateRange minutes = new DateRange( 0L, 61_000L );
    DateRange hours = new DateRange( 0L, 3_661_000L );
    DateRange days = new DateRange( 0L, 90_061_000L );

    assertEquals( 45, executor.calculateAndPrintElapsedTime(
      lessThanMinute.start(), lessThanMinute.stop(), "start", "seconds", "minutes", "hours", "days" ) );
    assertEquals( 61, executor.calculateAndPrintElapsedTime(
      minutes.start(), minutes.stop(), "start", "seconds", "minutes", "hours", "days" ) );
    assertEquals( 3661, executor.calculateAndPrintElapsedTime(
      hours.start(), hours.stop(), "start", "seconds", "minutes", "hours", "days" ) );
    assertEquals( 90061, executor.calculateAndPrintElapsedTime(
      days.start(), days.stop(), "start", "seconds", "minutes", "hours", "days" ) );

    verify( executorLog, times( 8 ) ).logMinimal( anyString() );
  }

  @Test
  public void printVersionLogsResolvedBuildVersion() {
    BuildVersion buildVersion = mock( BuildVersion.class );
    when( buildVersion.getVersion() ).thenReturn( "11.1" );
    when( buildVersion.getRevision() ).thenReturn( "abc123" );
    when( buildVersion.getBuildDate() ).thenReturn( "2026-05-29" );

    try ( MockedStatic<BuildVersion> mockedBuildVersion = mockStatic( BuildVersion.class ) ) {
      mockedBuildVersion.when( BuildVersion::getInstance ).thenReturn( buildVersion );

      executor.printVersion( "version.key" );

      verify( executorLog ).logBasic( anyString() );
    }
  }

  @Test
  public void loadRepositoryConnectionReturnsNullWhenRepositoryNameIsMissing() throws Exception {
    assertNull( executor.loadRepositoryConnection( null, "loading", "missing", "finding" ) );
    assertEquals( 0, loadRepositoryInfoCallCount );
  }

  @Test
  public void loadRepositoryConnectionReturnsMatchingRepository() throws Exception {
    RepositoriesMeta repositoriesMeta = mock( RepositoriesMeta.class );
    when( repositoriesMeta.findRepository( "repo" ) ).thenReturn( repositoryMeta );
    repositoriesMetaToReturn = repositoriesMeta;

    RepositoryMeta result = executor.loadRepositoryConnection( "repo", "loading", "missing", "finding" );

    assertSame( repositoryMeta, result );
    assertEquals( 1, loadRepositoryInfoCallCount );
  }

  @Test
  public void loadRepositoryInfoReadsRepositoryData() throws Exception {
    AbstractBaseCommandExecutor utilityExecutor = newUtilityExecutor();
    LogChannelInterface repositoriesLog = mock( LogChannelInterface.class );

    try ( MockedConstruction<RepositoriesMeta> construction = mockConstruction( RepositoriesMeta.class,
      ( mock, context ) -> when( mock.getLog() ).thenReturn( repositoriesLog ) ) ) {
      RepositoriesMeta result = utilityExecutor.loadRepositoryInfo( "loading", "missing" );

      RepositoriesMeta constructed = construction.constructed().get( 0 );
      assertSame( constructed, result );
      verify( repositoriesLog ).setLogLevel( utilityExecutor.getLog().getLogLevel() );
      verify( constructed ).readData();
    }
  }

  @Test
  public void loadRepositoryInfoWrapsReadFailures() {
    AbstractBaseCommandExecutor utilityExecutor = newUtilityExecutor();
    LogChannelInterface repositoriesLog = mock( LogChannelInterface.class );

    try ( MockedConstruction<RepositoriesMeta> construction = mockConstruction( RepositoriesMeta.class,
      ( mock, context ) -> {
        when( mock.getLog() ).thenReturn( repositoriesLog );
        doThrow( new RuntimeException( "boom" ) ).when( mock ).readData();
      } ) ) {
      utilityExecutor.loadRepositoryInfo( "loading", "missing" );
      fail( "Expected repository loading failure" );
    } catch ( KettleException e ) {
      assertNotNull( e.getCause() );
      assertEquals( "boom", e.getCause().getMessage() );
    }
  }

  @Test
  public void loadRepositoryDirectoryReturnsNullWhenRepositoryIsMissing() throws Exception {
    assertNull( executor.loadRepositoryDirectory( null, "/public", "no.repo", "alloc", "missing.dir" ) );
    verify( executorLog ).logBasic( anyString() );
  }

  @Test
  public void loadRepositoryDirectoryReturnsRootWhenDirectoryNameIsBlank() throws Exception {
    RepositoryDirectoryInterface root = mock( RepositoryDirectoryInterface.class );
    when( repository.loadRepositoryDirectoryTree() ).thenReturn( root );

    RepositoryDirectoryInterface result = executor.loadRepositoryDirectory(
      repository, "", "no.repo", "alloc", "missing.dir" );

    assertSame( root, result );
  }

  @Test
  public void loadRepositoryDirectoryLogsWhenNamedDirectoryIsMissing() throws Exception {
    RepositoryDirectoryInterface root = mock( RepositoryDirectoryInterface.class );
    when( repository.loadRepositoryDirectoryTree() ).thenReturn( root );
    when( root.findDirectory( "/missing" ) ).thenReturn( null );

    assertNull( executor.loadRepositoryDirectory( repository, "/missing", "no.repo", "alloc", "missing.dir" ) );
    verify( executorLog ).logBasic( anyString() );
  }

  @Test
  public void expiredRefreshFailureFallsBackToExistingBrowserSession() throws Exception {
    when( discoveryClient.getAvailableIdps( SERVER_URL ) ).thenReturn( Collections.emptyList() );
    when( discoveryClient.isBrokerOAuthAvailable( SERVER_URL ) ).thenReturn( true );
    when( holder.hasValidOAuthToken( SERVER_URL ) ).thenReturn( false );
    when( holder.isOAuthTokenExpiredButRefreshable( SERVER_URL ) ).thenReturn( true );
    when( holder.getRefreshHandle( SERVER_URL ) ).thenReturn( "refresh-handle" );
    when( holder.getBrokerAuthHandle( SERVER_URL ) ).thenReturn( "broker-handle" );
    when( holder.hasValidSession( SERVER_URL ) ).thenReturn( true );
    when( holder.getSessionUsername( SERVER_URL ) ).thenReturn( "browser-user" );
    when( brokerAuthClient.refreshAccessToken( SERVER_URL, "refresh-handle", "broker-handle" ) )
      .thenReturn( failedResult( "refresh failed" ) );

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig();
          MockedStatic<BrowserAuthSessionHolder> mockedHolder = mockHolder() ) {
      Repository result = executor.establishRepositoryConnectionWithBrowserAuth(
        repositoryMeta, null, "", false, false, false, (String) null,
        RepositoryOperation.EXECUTE_TRANSFORMATION );

      assertSame( repository, result );
      assertEquals( "browser-user", capturedUsername );
      verify( holder ).clearOAuthToken( SERVER_URL );
      mockedCliConfig.verify( CliConfig::getInstance, atLeastOnce() );
      mockedHolder.verify( BrowserAuthSessionHolder::getInstance, atLeastOnce() );
    }
  }

  @Test
  public void serviceAccountFailsWhenBrokerSupportIsUnavailable() {
    when( discoveryClient.getAvailableIdps( SERVER_URL ) ).thenReturn( Collections.emptyList() );
    when( discoveryClient.isBrokerOAuthAvailable( SERVER_URL ) ).thenReturn( false );
    when( holder.hasValidOAuthToken( SERVER_URL ) ).thenReturn( false );

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig();
          MockedStatic<BrowserAuthSessionHolder> mockedHolder = mockHolder() ) {
      try {
        executor.establishRepositoryConnectionWithBrowserAuth(
          repositoryMeta, null, "", false, false, true, (String) null,
          RepositoryOperation.EXECUTE_TRANSFORMATION );
        fail( "Expected service-account auth failure" );
      } catch ( KettleException e ) {
        assertTrue( e.getMessage() != null && !e.getMessage().isEmpty() );
        assertEquals( 0, connectCallCount );
      }
      mockedCliConfig.verify( CliConfig::getInstance, atLeastOnce() );
      mockedHolder.verify( BrowserAuthSessionHolder::getInstance );
    }
  }

  @Test
  public void deviceCodeFailureFallsBackToPromptedCredentials() throws Exception {
    promptedUsername = "prompt-user";
    promptedPassword = "prompt-pass";
    when( discoveryClient.getAvailableIdps( SERVER_URL ) ).thenReturn( Collections.singletonList( "keycloak" ) );
    when( discoveryClient.isBrokerOAuthAvailable( SERVER_URL ) ).thenReturn( true );
    when( holder.hasValidOAuthToken( SERVER_URL ) ).thenReturn( false );
    when( holder.isOAuthTokenExpiredButRefreshable( SERVER_URL ) ).thenReturn( false );
    when( holder.hasValidSession( SERVER_URL ) ).thenReturn( false );
    when( brokerAuthClient.startDeviceCodeFlow( SERVER_URL, "keycloak" ) )
      .thenReturn( failedResult( "device flow failed" ) );

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig();
          MockedStatic<BrowserAuthSessionHolder> mockedHolder = mockHolder() ) {
      Repository result = executor.establishRepositoryConnectionWithBrowserAuth(
        repositoryMeta, null, "", false, true, false, (String) null,
        RepositoryOperation.EXECUTE_TRANSFORMATION );

      assertSame( repository, result );
      assertEquals( "prompt-user", capturedUsername );
      assertEquals( "prompt-pass", capturedPassword );
      mockedCliConfig.verify( CliConfig::getInstance, atLeastOnce() );
      mockedHolder.verify( BrowserAuthSessionHolder::getInstance );
    }
  }

  @Test
  public void pkceStartFailureFallsBackToDeviceCode() throws Exception {
    when( discoveryClient.getAvailableIdps( SERVER_URL ) ).thenReturn( Collections.singletonList( "keycloak" ) );
    when( discoveryClient.isBrokerOAuthAvailable( SERVER_URL ) ).thenReturn( true );
    when( holder.hasValidOAuthToken( SERVER_URL ) ).thenReturn( false );
    when( holder.isOAuthTokenExpiredButRefreshable( SERVER_URL ) ).thenReturn( false );
    when( holder.hasValidSession( SERVER_URL ) ).thenReturn( false );
    when( brokerAuthClient.startPkceFlow( SERVER_URL, "keycloak" ) )
      .thenReturn( failedResult( "pkce failed" ) );
    when( brokerAuthClient.startDeviceCodeFlow( SERVER_URL, "keycloak" ) )
      .thenReturn( pendingDeviceCodeStart( "device-handle" ) );
    when( brokerAuthClient.pollUntilComplete( SERVER_URL, "device-handle" ) )
      .thenReturn( completedResult( "device-user", "device-token", "device-refresh" ) );

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig();
          MockedStatic<BrowserAuthSessionHolder> mockedHolder = mockHolder() ) {
      Repository result = executor.establishRepositoryConnectionWithBrowserAuth(
        repositoryMeta, null, "", true, false, false, (String) null,
        RepositoryOperation.EXECUTE_TRANSFORMATION );

      assertSame( repository, result );
      assertEquals( "device-user", capturedUsername );
      verify( holder ).storeOAuthToken( any() );
      mockedCliConfig.verify( CliConfig::getInstance, atLeastOnce() );
      mockedHolder.verify( BrowserAuthSessionHolder::getInstance );
    }
  }

  @Test
  public void missingPromptedUsernameThrowsRepositoryMissingUsername() {
    repositoryServerUrl = null;
    promptedUsername = null;

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig() ) {
      try {
        executor.establishRepositoryConnectionWithBrowserAuth(
          repositoryMeta, null, "", false, false, false, (String) null,
          RepositoryOperation.EXECUTE_TRANSFORMATION );
        fail( "Expected username failure" );
      } catch ( KettleException e ) {
        assertTrue( e.getMessage().contains( "username" ) );
      }
      mockedCliConfig.verify( CliConfig::getInstance, never() );
    }
  }

  @Test
  public void missingPromptedPasswordThrowsRepositoryMissingPassword() {
    repositoryServerUrl = null;
    promptedUsername = "prompt-user";
    promptedPassword = null;

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig() ) {
      try {
        executor.establishRepositoryConnectionWithBrowserAuth(
          repositoryMeta, null, "", false, false, false, (String) null,
          RepositoryOperation.EXECUTE_TRANSFORMATION );
        fail( "Expected password failure" );
      } catch ( KettleException e ) {
        assertTrue( e.getMessage().contains( "password" ) );
      }
      mockedCliConfig.verify( CliConfig::getInstance, never() );
    }
  }

  @Test
  public void createBrokerAuthClientUsesConfiguredBrokerTimeoutForPlainMode() {
    AbstractBaseCommandExecutor utilityExecutor = newUtilityExecutor();
    BrokerDiscoveryClient discovery = mock( BrokerDiscoveryClient.class );
    List<List<?>> constructorArguments = new ArrayList<>();
    when( discovery.isDpopEnabled( SERVER_URL ) ).thenReturn( false );

    try ( MockedStatic<CliConfig> mockedCliConfig = mockStatic( CliConfig.class );
          MockedConstruction<BrokerAuthClient> construction = mockConstruction( BrokerAuthClient.class,
            ( mock, context ) -> constructorArguments.add( new ArrayList<>( context.arguments() ) ) ) ) {
      mockedCliConfig.when( CliConfig::getInstance ).thenReturn( cliConfig );

      BrokerAuthClient client = utilityExecutor.createBrokerAuthClient( discovery, SERVER_URL );

      assertNotNull( client );
      assertEquals( 1, construction.constructed().size() );
      assertEquals( 1, constructorArguments.get( 0 ).size() );
      assertEquals( 30_000, constructorArguments.get( 0 ).get( 0 ) );
    }
  }

  @Test
  public void createBrokerAuthClientUsesDpopBuilderWhenEnabled() {
    AbstractBaseCommandExecutor utilityExecutor = newUtilityExecutor();
    BrokerDiscoveryClient discovery = mock( BrokerDiscoveryClient.class );
    List<List<?>> constructorArguments = new ArrayList<>();
    when( discovery.isDpopEnabled( SERVER_URL ) ).thenReturn( true );

    try ( MockedStatic<CliConfig> mockedCliConfig = mockStatic( CliConfig.class );
          MockedConstruction<BrokerAuthClient> construction = mockConstruction( BrokerAuthClient.class,
            ( mock, context ) -> constructorArguments.add( new ArrayList<>( context.arguments() ) ) ) ) {
      mockedCliConfig.when( CliConfig::getInstance ).thenReturn( cliConfig );

      BrokerAuthClient client = utilityExecutor.createBrokerAuthClient( discovery, SERVER_URL );

      assertNotNull( client );
      assertEquals( 1, construction.constructed().size() );
      assertEquals( 2, constructorArguments.get( 0 ).size() );
      assertEquals( 30_000, constructorArguments.get( 0 ).get( 0 ) );
      assertTrue( constructorArguments.get( 0 ).get( 1 ) instanceof org.pentaho.di.cli.auth.DPoPProofBuilder );
    }
  }

  @Test
  public void multipleIdpsWithoutInteractiveFlagsFallsBackToPromptedCredentials() throws Exception {
    promptedUsername = "fallback-user";
    promptedPassword = "fallback-pass";
    when( discoveryClient.getAvailableIdps( SERVER_URL ) ).thenReturn( Arrays.asList( "keycloak", "azure" ) );
    when( discoveryClient.isBrokerOAuthAvailable( SERVER_URL ) ).thenReturn( true );
    when( holder.hasValidOAuthToken( SERVER_URL ) ).thenReturn( false );
    when( holder.isOAuthTokenExpiredButRefreshable( SERVER_URL ) ).thenReturn( false );
    when( holder.hasValidSession( SERVER_URL ) ).thenReturn( false );

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig();
          MockedStatic<BrowserAuthSessionHolder> mockedHolder = mockHolder() ) {
      Repository result = executor.establishRepositoryConnectionWithBrowserAuth(
        repositoryMeta, null, "", false, false, false, (String) null,
        RepositoryOperation.EXECUTE_TRANSFORMATION );

      assertSame( repository, result );
      assertEquals( "fallback-user", capturedUsername );
      assertEquals( "fallback-pass", capturedPassword );
      mockedCliConfig.verify( CliConfig::getInstance, atLeastOnce() );
      mockedHolder.verify( BrowserAuthSessionHolder::getInstance );
    }
  }

  @Test
  public void rejectedSessionRefreshesAndReconnects() throws Exception {
    connectFailure = new KettleException( "401 Unauthorized" );
    connectFailuresRemaining = 1;
    when( discoveryClient.getAvailableIdps( SERVER_URL ) ).thenReturn( Collections.emptyList() );
    when( discoveryClient.isBrokerOAuthAvailable( SERVER_URL ) ).thenReturn( true );
    when( holder.hasValidOAuthToken( SERVER_URL ) ).thenReturn( true, true );
    when( holder.getSessionUsername( SERVER_URL ) ).thenReturn( "oauth-user" );
    when( holder.isOAuthTokenExpiredButRefreshable( SERVER_URL ) ).thenReturn( true );
    when( holder.getRefreshHandle( SERVER_URL ) ).thenReturn( "refresh-handle" );
    when( holder.getBrokerAuthHandle( SERVER_URL ) ).thenReturn( "broker-handle" );
    when( holder.getOAuthIdpRegistrationId( SERVER_URL ) ).thenReturn( "keycloak" );
    when( brokerAuthClient.refreshAccessToken( SERVER_URL, "refresh-handle", "broker-handle" ) )
      .thenReturn( completedResult( "oauth-user", "new-token", "rotated-refresh" ) );

    try ( MockedStatic<CliConfig> mockedCliConfig = mockCliConfig();
          MockedStatic<BrowserAuthSessionHolder> mockedHolder = mockHolder() ) {
      Repository result = executor.establishRepositoryConnectionWithBrowserAuth(
        repositoryMeta, null, "", false, false, false, (String) null,
        RepositoryOperation.EXECUTE_TRANSFORMATION );

      assertSame( repository, result );
      assertEquals( 2, connectCallCount );
      verify( holder ).storeOAuthToken( any() );
      verify( holder ).clearSession( SERVER_URL );
      mockedCliConfig.verify( CliConfig::getInstance, atLeastOnce() );
      mockedHolder.verify( BrowserAuthSessionHolder::getInstance, atLeastOnce() );
    }
  }

  @Test
  public void connectToRepositoryInitializesAndValidatesOperations() throws Exception {
    AbstractBaseCommandExecutor utilityExecutor = newUtilityExecutor();
    PluginRegistry pluginRegistry = mock( PluginRegistry.class );
    Repository connectedRepository = mock( Repository.class );
    RepositorySecurityProvider securityProvider = mock( RepositorySecurityProvider.class );
    LogChannelInterface repositoryLog = mock( LogChannelInterface.class );

    when( connectedRepository.getLog() ).thenReturn( repositoryLog );
    when( connectedRepository.getSecurityProvider() ).thenReturn( securityProvider );

    try ( MockedStatic<PluginRegistry> mockedPluginRegistry = mockStatic( PluginRegistry.class ) ) {
      mockedPluginRegistry.when( PluginRegistry::getInstance ).thenReturn( pluginRegistry );
      when( pluginRegistry.loadClass( RepositoryPluginType.class, repositoryMeta, Repository.class ) )
        .thenReturn( connectedRepository );

      Repository result = utilityExecutor.connectToRepository(
        repositoryMeta, "alice", null, RepositoryOperation.EXECUTE_TRANSFORMATION );

      assertSame( connectedRepository, result );
      verify( connectedRepository ).init( repositoryMeta );
      verify( connectedRepository ).connect( "alice", null );
      verify( securityProvider ).validateAction( RepositoryOperation.EXECUTE_TRANSFORMATION );
    }
  }

  @Test
  public void connectToRepositoryTranslatesUnauthorizedFailures() throws Exception {
    AbstractBaseCommandExecutor utilityExecutor = newUtilityExecutor();
    PluginRegistry pluginRegistry = mock( PluginRegistry.class );
    Repository connectedRepository = mock( Repository.class );
    LogChannelInterface repositoryLog = mock( LogChannelInterface.class );

    when( connectedRepository.getLog() ).thenReturn( repositoryLog );

    try ( MockedStatic<PluginRegistry> mockedPluginRegistry = mockStatic( PluginRegistry.class ) ) {
      mockedPluginRegistry.when( PluginRegistry::getInstance ).thenReturn( pluginRegistry );
      when( pluginRegistry.loadClass( RepositoryPluginType.class, repositoryMeta, Repository.class ) )
        .thenReturn( connectedRepository );
      doThrow( new KettleException( "401 Unauthorized" ) ).when( connectedRepository ).connect( "alice", null );

      try {
        utilityExecutor.connectToRepository( repositoryMeta, "alice", null );
        fail( "Expected invalid credentials failure" );
      } catch ( KettleException e ) {
        assertTrue( e.getMessage() != null && !e.getMessage().isEmpty() );
      }
    }
  }

  @Test
  public void getRepositoryServerUrlReturnsLocationUrlWhenPresent() {
    AbstractBaseCommandExecutor utilityExecutor = newUtilityExecutor();
    RepositoryMetaWithLocation repositoryMetaWithLocation = mock( RepositoryMetaWithLocation.class );

    when( repositoryMetaWithLocation.getRepositoryLocation() )
      .thenReturn( new TestRepositoryLocation( SERVER_URL ) );

    assertEquals( SERVER_URL, utilityExecutor.getRepositoryServerUrl( repositoryMetaWithLocation ) );
  }

  @Test
  public void getRepositoryServerUrlReturnsNullWhenLookupFails() {
    AbstractBaseCommandExecutor utilityExecutor = newUtilityExecutor();

    assertNull( utilityExecutor.getRepositoryServerUrl( mock( RepositoryMeta.class ) ) );
    verify( utilityExecutor.getLog() ).logDebug( anyString() );
  }

  @SuppressWarnings( "java:S2093" ) // Suppress try-with-resources warning for System.setOut
  @Test
  public void printRepositoryDirectoriesWritesAllReturnedNames() throws Exception {
    RepositoryDirectoryInterface directory = mock( RepositoryDirectoryInterface.class );
    ObjectId objectId = mock( ObjectId.class );
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    when( directory.getObjectId() ).thenReturn( objectId );
    when( repository.getDirectoryNames( objectId ) ).thenReturn( new String[] { "/public", "/home" } );

    try {
      System.setOut( new PrintStream( out ) );
      executor.printRepositoryDirectories( repository, directory );
    } finally {
      System.setOut( originalOut );
    }

    String output = out.toString( StandardCharsets.UTF_8 );
    assertTrue( output.contains( "/public" ) );
    assertTrue( output.contains( "/home" ) );
  }

  @Test
  public void printParameterHandlesDefaultAndNonDefaultMessages() {
    executor.printParameter( "name", "value", null, "desc" );
    executor.printParameter( "name", "value", "default", "desc" );

    verify( executorLog, times( 2 ) ).logBasic( anyString() );
  }

  @Test
  public void convertReturnsKeyValuePairsAndEmptyArrayForNullInput() {
    Map<String, String> values = new LinkedHashMap<>();
    values.put( "alpha", "1" );
    values.put( "beta", "2" );

    assertArrayEquals( new String[] { "alpha=1", "beta=2" }, executor.convert( values ) );
    assertArrayEquals( new String[ 0 ], executor.convert( null ) );
  }

  @Test
  public void isEnabledRecognizesSupportedValues() {
    assertTrue( executor.isEnabled( "Y" ) );
    assertTrue( executor.isEnabled( "true" ) );
    assertFalse( executor.isEnabled( "N" ) );
    assertFalse( executor.isEnabled( null ) );
  }

  @Test
  public void decodeBase64ToZipFileWritesRequestedPath() throws Exception {
    Path tempDir = Files.createTempDirectory( "abstract-base-explicit" );
    Path target = tempDir.resolve( "payload.zip" );

    File file = executor.decodeBase64ToZipFile( "aGVsbG8=", target.toString() );

    assertNotNull( file );
    assertEquals( "hello", Files.readString( target, StandardCharsets.UTF_8 ) );
  }

  @Test
  public void decodeBase64ToZipFileUsesDefaultHomeDirectoryPath() throws Exception {
    Path tempDir = Files.createTempDirectory( "abstract-base-default" );

    try ( MockedStatic<Const> mockedConst = mockStatic( Const.class ) ) {
      mockedConst.when( Const::getUserHomeDirectory ).thenReturn( tempDir.toString() );

      File file = executor.decodeBase64ToZipFile( "aGVsbG8=", false );

      assertNotNull( file );
      assertTrue( file.exists() );
      assertTrue( file.getParentFile().getAbsolutePath().startsWith( tempDir.toString() ) );
      assertEquals( "hello", Files.readString( file.toPath(), StandardCharsets.UTF_8 ) );
    }
  }

  @Test
  public void decodeBase64ToZipFileReturnsNullForEmptyInput() throws Exception {
    assertNull( executor.decodeBase64ToZipFile( null, "ignored.zip" ) );
    assertNull( executor.decodeBase64ToZipFile( "", "ignored.zip" ) );
  }

  @Test
  public void validateAndSetPluginContextStopsAtFirstFailure() throws Exception {
    LogChannelInterface log = mock( LogChannelInterface.class );
    Repository repositoryMock = mock( Repository.class );
    Params params = mock( Params.class );
    CommandLineOptionProvider first = mock( CommandLineOptionProvider.class );
    CommandLineOptionProvider second = mock( CommandLineOptionProvider.class );
    CommandExecutorResult success = mock( CommandExecutorResult.class );
    CommandExecutorResult failure = mock( CommandExecutorResult.class );
    Map<String, String> pluginParams = Collections.singletonMap( "key", "value" );

    when( params.getPluginParams() ).thenReturn( pluginParams );
    when( success.getCode() ).thenReturn( 0 );
    when( failure.getCode() ).thenReturn( 8 );
    when( failure.getDescription() ).thenReturn( "bad plugin argument" );
    when( first.handleParameter( log, pluginParams, repositoryMock ) ).thenReturn( success );
    when( second.handleParameter( log, pluginParams, repositoryMock ) ).thenReturn( failure );

    try ( MockedStatic<PluginServiceLoader> mockedLoader = mockStatic( PluginServiceLoader.class ) ) {
      mockedLoader.when( () -> PluginServiceLoader.loadServices( CommandLineOptionProvider.class ) )
        .thenReturn( Arrays.asList( first, second ) );

      CommandExecutorResult result = AbstractBaseCommandExecutor.validateAndSetPluginContext( log, params, repositoryMock );

      assertSame( failure, result );
      verify( log ).logError( "bad plugin argument" );
    }
  }

  @Test
  public void createBrokerDiscoveryClientCachesTheCreatedInstance() {
    AbstractBaseCommandExecutor utilityExecutor = newUtilityExecutor();

    BrokerDiscoveryClient first = utilityExecutor.createBrokerDiscoveryClient();
    BrokerDiscoveryClient second = utilityExecutor.createBrokerDiscoveryClient();

    assertNotNull( first );
    assertSame( first, second );
  }

  private AbstractBaseCommandExecutor newUtilityExecutor() {
    AbstractBaseCommandExecutor utilityExecutor = spy( new AbstractBaseCommandExecutor() { } );
    LogChannelInterface utilityLog = mock( LogChannelInterface.class );
    when( utilityLog.isDebug() ).thenReturn( true );
    when( utilityLog.getLogLevel() ).thenReturn( LogLevel.DEBUG );
    utilityExecutor.setLog( utilityLog );
    utilityExecutor.setPkgClazz( Pan.class );
    return utilityExecutor;
  }

  private MockedStatic<CliConfig> mockCliConfig() {
    MockedStatic<CliConfig> mockedCliConfig = mockStatic( CliConfig.class );
    mockedCliConfig.when( CliConfig::getInstance ).thenReturn( cliConfig );
    return mockedCliConfig;
  }

  private MockedStatic<BrowserAuthSessionHolder> mockHolder() {
    MockedStatic<BrowserAuthSessionHolder> mockedHolder = mockStatic( BrowserAuthSessionHolder.class );
    mockedHolder.when( BrowserAuthSessionHolder::getInstance ).thenReturn( holder );
    return mockedHolder;
  }

  private BrokerFlowResult completedResult( String username, String accessToken, String refreshHandle ) {
    return new BrokerFlowResult(
      null, "COMPLETED", null, username, null,
      null, null, null, null, null,
      null, null, accessToken, 0, null, 4102444800L, refreshHandle );
  }

  private BrokerFlowResult failedResult( String error ) {
    return new BrokerFlowResult(
      null, "FAILED", null, null, null,
      null, null, null, null, error,
      null, null, null, 0, null, 0L, null );
  }

  private BrokerFlowResult pendingPkceStart( String authHandle, String authorizeUrl ) {
    return new BrokerFlowResult(
      authHandle, "PENDING", BrokerAuthClient.GRANT_AUTHORIZATION_CODE, null, null,
      null, null, null, null, null,
      authorizeUrl, null, null, 0, null, 0L, null );
  }

  private BrokerFlowResult pendingDeviceCodeStart( String authHandle ) {
    return new BrokerFlowResult(
      authHandle, "PENDING", BrokerAuthClient.GRANT_DEVICE_CODE, null, null,
      "USER-CODE", "http://verify.example", "http://verify.example/direct", null, null,
      null, null, null, 0, null, 0L, null );
  }

  private BrokerFlowResult cancelledResult( String authHandle ) {
    return new BrokerFlowResult(
      authHandle, "CANCELLED", null, null, null,
      null, null, null, null, null,
      null, null, null, 0, null, 0L, null );
  }

  private BrokerFlowResult incompleteResult( String authHandle, String error ) {
    return new BrokerFlowResult(
      authHandle, "PENDING", null, null, null,
      null, null, null, null, error,
      null, null, null, 0, null, 0L, null );
  }

  public interface RepositoryMetaWithLocation extends RepositoryMeta {
    TestRepositoryLocation getRepositoryLocation();
  }

  private record TestRepositoryLocation(String url) {
    public String getUrl() {
      return url;
    }
  }

  private record DateRange(Date start, Date stop) {
    private DateRange( long startMillis, long stopMillis ) {
      this( new Date( startMillis ), new Date( stopMillis ) );
    }
  }
}
