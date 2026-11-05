package org.pentaho.s3.vfs.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileSystemOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.pentaho.amazon.s3.S3Details;
import org.pentaho.amazon.s3.S3Util;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.s3.vfs.S3FileSystemConfigBuilder;

import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Characterisation tests for {@link S3ClientManager}.
 * <p>
 * These pin the behaviour inherited from {@code S3FileSystem}, which is not necessarily the
 * behaviour worth keeping. Where a test documents something questionable it says so.
 */
class S3ClientManagerTest {

  private RecordingFactory factory;
  private FileSystemOptions fileSystemOptions;
  private S3FileSystemConfigBuilder configBuilder;
  private ConnectionManager connectionManager;
  private int invalidations;

  private String savedAccessKey;
  private String savedSecretKey;
  private String savedSessionToken;

  /** Connection properties are read through Encr, which needs an encoder. */
  @BeforeAll
  static void initKettle() throws KettleException {
    KettleClientEnvironment.init();
  }

  @BeforeEach
  void setUp() {
    factory = new RecordingFactory();
    fileSystemOptions = new FileSystemOptions();
    configBuilder = new S3FileSystemConfigBuilder( fileSystemOptions );
    connectionManager = mock( ConnectionManager.class );
    invalidations = 0;

    savedAccessKey = System.clearProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY );
    savedSecretKey = System.clearProperty( S3Util.SECRET_KEY_SYSTEM_PROPERTY );
    savedSessionToken = System.clearProperty( S3Util.SESSION_TOKEN_SYSTEM_PROPERTY );
  }

  @AfterEach
  void tearDown() {
    restoreProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY, savedAccessKey );
    restoreProperty( S3Util.SECRET_KEY_SYSTEM_PROPERTY, savedSecretKey );
    restoreProperty( S3Util.SESSION_TOKEN_SYSTEM_PROPERTY, savedSessionToken );
  }

  @Test
  void buildsClientFromConfigurationAndCachesIt() throws Exception {
    configBuilder.setAccessKey( "ak" );
    configBuilder.setSecretKey( "sk" );
    S3ClientManager manager = newManager();

    S3Client first = manager.getClient();
    S3Client second = manager.getClient();

    assertSame( first, second );
    assertEquals( 1, factory.configCalls );
    assertEquals( 0, factory.envCalls );
    assertEquals( 0, invalidations );
  }

  @Test
  void configuredCredentialsReachTheFactory() throws Exception {
    configBuilder.setAccessKey( "ak" );
    configBuilder.setSecretKey( "sk" );

    newManager().getClient();

    assertEquals( "ak", factory.lastOptions.authKeys().orElseThrow().accessKey() );
    assertEquals( "sk", factory.lastOptions.authKeys().orElseThrow().secretKey() );
  }

  /**
   * Questionable: the key caches start out unset, so the first system property that is set counts
   * as a change and throws away the client that was just built from configuration.
   */
  @Test
  void systemPropertyCredentialsDiscardTheConfiguredClient() throws Exception {
    System.setProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY, "env-ak" );
    System.setProperty( S3Util.SECRET_KEY_SYSTEM_PROPERTY, "env-sk" );
    configBuilder.setAccessKey( "ak" );
    configBuilder.setSecretKey( "sk" );

    newManager().getClient();

    assertEquals( 1, factory.configCalls );
    assertEquals( 1, factory.envCalls );
  }

  /**
   * Questionable side effect related to {@link #systemPropertyCredentialsDiscardTheConfiguredClient()}
   * and {@link Behavior#preferEnvCredentials()} being true
   * Changing the system properties for keys, as happens in S3FileOutput step, can change what client
   * is used, even forgetting the endpoint.
   */
  @Test
  @Disabled( "preferEnvCredentials" )
  void systemPropertiesDoNotReplaceTheConfiguredEndpoint() throws Exception {
    configBuilder.setEndpoint( "https://my.favorite.endpoint" );
    configBuilder.setAccessKey( "connection-ak" );
    configBuilder.setSecretKey( "connection-sk" );
    System.setProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY, "env-ak" );
    System.setProperty( S3Util.SECRET_KEY_SYSTEM_PROPERTY, "env-sk" );
    S3ClientManager manager = newManager();

    S3Client first = manager.getClient();

    assertEquals( 0, factory.envCalls );
    assertEquals( "https://my.favorite.endpoint", factory.lastOptions.base().endpoint() );
    assertEquals( "connection-ak", factory.lastOptions.authKeys().orElseThrow().accessKey() );

    System.setProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY, "changed-ak" );
    assertSame( first, manager.getClient() );

    System.clearProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY );
    System.clearProperty( S3Util.SECRET_KEY_SYSTEM_PROPERTY );
    assertSame( first, manager.getClient() );
    assertEquals( 1, factory.configCalls );
    assertEquals( 0, factory.envCalls );
  }

  /**
   * Would be broken by {@link Behavior#preferEnvCredentials()}
   */
  @Test
  void changedSystemPropertyRebuildsTheClient() throws Exception {
    System.setProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY, "env-ak" );
    S3ClientManager manager = newManager();

    S3Client first = manager.getClient();
    System.setProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY, "other-ak" );
    S3Client second = manager.getClient();

    assertNotSame( first, second );
    assertEquals( 2, factory.envCalls );
  }

  /** Temporary credentials are refreshed by replacing the token, sometimes with the same keys. */
  @Test
  void changedSessionTokenRebuildsTheClient() throws Exception {
    System.setProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY, "env-ak" );
    System.setProperty( S3Util.SECRET_KEY_SYSTEM_PROPERTY, "env-sk" );
    System.setProperty( S3Util.SESSION_TOKEN_SYSTEM_PROPERTY, "token" );
    S3ClientManager manager = newManager();

    S3Client first = manager.getClient();
    System.setProperty( S3Util.SESSION_TOKEN_SYSTEM_PROPERTY, "refreshed-token" );
    S3Client second = manager.getClient();

    assertNotSame( first, second );
    assertEquals( 2, factory.envCalls );
  }

  /** Replaced clients may still be serving transfers, so they stay open until the clients are closed. */
  @Test
  void replacedClientsAreClosedOnlyWithTheClients() throws Exception {
    System.setProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY, "env-ak" );
    S3ClientManager manager = newManager();
    S3Client first = manager.getClient();
    S3AsyncClient firstAsync = manager.getAsyncClient();

    System.setProperty( S3Util.ACCESS_KEY_SYSTEM_PROPERTY, "other-ak" );
    S3Client second = manager.getClient();
    S3AsyncClient secondAsync = manager.getAsyncClient();

    verify( first, never() ).close();
    verify( firstAsync, never() ).close();

    manager.closeClients();

    verify( first ).close();
    verify( firstAsync ).close();
    verify( second ).close();
    verify( secondAsync ).close();
  }

  /** VFS may keep using a file system after closing its communication link. */
  @Test
  void closedClientsAreRebuiltOnTheNextRequest() throws Exception {
    configBuilder.setAccessKey( "ak" );
    configBuilder.setSecretKey( "sk" );
    S3ClientManager manager = newManager();
    S3Client first = manager.getClient();

    manager.closeClients();
    S3Client second = manager.getClient();

    assertNotSame( first, second );
    verify( second, never() ).close();
  }

  @Test
  void unchangedDefaultConnectionKeepsTheClient() throws Exception {
    givenDefaultS3Connection( defaultConnectionProperties() );
    S3ClientManager manager = newManager();

    S3Client first = manager.getClient();
    S3Client second = manager.getClient();

    assertSame( first, second );
    assertEquals( 1, factory.configCalls );
  }

  /** A named connection comes from pvfs, so the default S3 connection must not apply. */
  @Test
  void namedConnectionIgnoresTheDefaultConnection() throws Exception {
    configBuilder.setName( "my-connection" );
    configBuilder.setAccessKey( "ak" );
    configBuilder.setSecretKey( "sk" );
    givenDefaultS3Connection( defaultConnectionProperties() );

    newManager().getClient();

    assertEquals( "ak", factory.lastOptions.authKeys().orElseThrow().accessKey() );
    assertEquals( 0, invalidations );
  }

  @Test
  void keysInTheRootUriOverrideConfiguredCredentials() throws Exception {
    configBuilder.setAccessKey( "ak" );
    configBuilder.setSecretKey( "sk" );
    S3ClientManager manager = newManager( "s3://uri-ak:uri-sk@s3.amazonaws.com/bucket" );

    manager.getClient();

    assertEquals( "uri-ak", factory.lastOptions.authKeys().orElseThrow().accessKey() );
    assertEquals( "uri-sk", factory.lastOptions.authKeys().orElseThrow().secretKey() );
  }

  // S3A compatibility: defaults remain connections of type s3.
  @Test
  void s3aUsesTheDefaultS3Connection() throws Exception {
    givenDefaultS3Connection( defaultConnectionProperties() );

    newManager( "s3a://bucket/" ).getClient();

    assertEquals( "default-ak", factory.lastOptions.authKeys().orElseThrow().accessKey() );
    assertEquals( "default-sk", factory.lastOptions.authKeys().orElseThrow().secretKey() );
  }

  private S3ClientManager newManager() {
    return newManager( "s3://s3.amazonaws.com/bucket" );
  }

  private S3ClientManager newManager( String rootUri ) {
    return new S3ClientManager( factory, new Behavior(), fileSystemOptions, rootUri,
      () -> connectionManager, () -> invalidations++ );
  }

  private void givenDefaultS3Connection( Map<String, String> properties ) {
    ConnectionDetails details = mock( ConnectionDetails.class );
    doReturn( properties ).when( details ).getProperties();
    doReturn( List.of( details ) ).when( connectionManager ).getConnectionDetailsByScheme( "s3" );
  }

  private static Map<String, String> defaultConnectionProperties() {
    Map<String, String> properties = new HashMap<>();
    properties.put( S3Details.PROP_DEFAULT_S3_CONFIG, "true" );
    properties.put( "accessKey", "default-ak" );
    properties.put( "secretKey", "default-sk" );
    return properties;
  }

  private static void restoreProperty( String name, String value ) {
    if ( value == null ) {
      System.clearProperty( name );
    } else {
      System.setProperty( name, value );
    }
  }

  /** Records what the manager asked for, so the tests never build a real S3 client. */
  private static class RecordingFactory extends S3ClientFactory {

    private int configCalls;
    private int envCalls;
    private S3Options lastOptions;

    RecordingFactory() {
      super( new Behavior(), DefaultBowl.getInstance() );
    }

    @Override
    public S3Client create( S3Options options ) {
      configCalls++;
      lastOptions = options;
      return mock( S3Client.class );
    }

    @Override
    public S3Client createFromEnv() {
      envCalls++;
      return mock( S3Client.class );
    }

    @Override
    public S3AsyncClient createAsyncFromEnv() {
      return mock( S3AsyncClient.class );
    }
  }

}
