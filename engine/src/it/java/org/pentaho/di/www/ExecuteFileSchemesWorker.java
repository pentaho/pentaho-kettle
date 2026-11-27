/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/

package org.pentaho.di.www;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.BaseVFSConnectionDetails;
import org.pentaho.di.connections.vfs.BaseVFSConnectionProvider;
import org.pentaho.di.connections.vfs.VFSRoot;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.filerep.KettleFileRepositoryMeta;
import org.pentaho.di.trans.Trans;
import org.w3c.dom.Document;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Exercises both Carte execute APIs using parsed server configuration and isolated files, HTTP, PVFS and repository
 * fixtures. The PVFS fixture uses the production PVFS resolver with a test-only local connection provider.
 */
@RunWith( Parameterized.class )
public class ExecuteFileSchemesWorker {

  /**
   * An absent list defaults to PVFS, while an explicit list replaces that default, even when it is empty.
   * Each configuration is exercised separately against both execute APIs.
   */
  @Parameterized.Parameters( name = "{0}" )
  public static Collection<Object[]> configurations() {
    return Arrays.asList( new Object[][] {
      { "default", "", false, false, true },
      { "empty", "<allowedExecuteFileSchemes/>", false, false, false },
      { "file only", "<allowedExecuteFileSchemes><scheme>file</scheme></allowedExecuteFileSchemes>",
        true, false, false },
      { "file and http", "<allowedExecuteFileSchemes><scheme>file</scheme><scheme>http</scheme>"
        + "</allowedExecuteFileSchemes>", true, true, false },
      { "pvfs and file", "<allowedExecuteFileSchemes><scheme>pvfs</scheme><scheme>file</scheme>"
        + "</allowedExecuteFileSchemes>", true, false, true }
    } );
  }

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private final String configuration;
  private final boolean fileAllowed;
  private final boolean httpAllowed;
  private final boolean pvfsAllowed;
  private final AtomicInteger resourceRequests = new AtomicInteger();
  private final AtomicInteger resourceGets = new AtomicInteger();
  private final HttpClient client = HttpClient.newBuilder().connectTimeout( Duration.ofSeconds( 10 ) ).build();
  private static final TestFileProvider FILE_PROVIDER = new TestFileProvider();
  private static final String REPOSITORY_NAME = "execute-it";
  private HttpServer resourceServer;
  private Carte carte;
  private CarteSingleton singleton;
  private SlaveServerConfig previousConfig;
  private TransformationMap previousTransMap;
  private JobMap previousJobMap;
  private SocketRepository previousSocketRepository;
  private Path jobFile;
  private Path transFile;
  private String connectionName;
  private int port;

  public ExecuteFileSchemesWorker( String name, String configuration, boolean fileAllowed, boolean httpAllowed,
                                   boolean pvfsAllowed ) {
    this.configuration = configuration;
    this.fileAllowed = fileAllowed;
    this.httpAllowed = httpAllowed;
    this.pvfsAllowed = pvfsAllowed;
  }

  /**
   * Starts Carte with a parsed configuration, temporary file repository and loopback HTTP fixture server.
   * The PVFS connection points to the same temporary files through the test-only provider below.
   */
  @Before
  public void setUp() throws Exception {
    KettleEnvironment.init();
    jobFile = copyFixture( "job/one-step-job.kjb", "job.kjb" );
    transFile = copyFixture( "trans/one-step-trans.ktr", "trans.ktr" );
    Path repository = temporaryFolder.newFolder( "repository" ).toPath();
    Path folder = Files.createDirectory( repository.resolve( "folder" ) );
    Files.copy( jobFile, folder.resolve( "one-step-job.kjb" ) );
    Files.copy( transFile, folder.resolve( "one-step-trans.ktr" ) );
    RepositoriesMeta repositories = new RepositoriesMeta();
    repositories.addRepository( new KettleFileRepositoryMeta( "KettleFileRepository", REPOSITORY_NAME,
      "Temporary execute API repository", repository.toString() ) );
    repositories.writeData();
    connectionName = "execute-it-" + UUID.randomUUID();
    TestFileConnection connection = new TestFileConnection();
    connection.setName( connectionName );
    connection.setRootPath( temporaryFolder.getRoot().toPath().toUri().getRawPath() );
    FILE_PROVIDER.connection = connection;
    ConnectionManager manager = ConnectionManager.getInstance();
    manager.addConnectionProvider( "file", FILE_PROVIDER );
    assertSame( "Test file provider must be registered", FILE_PROVIDER, manager.getConnectionProvider( "file" ) );
    manager.reset();

    resourceServer = HttpServer.create( new InetSocketAddress( InetAddress.getByName( "127.0.0.1" ), 0 ), 0 );
    resourceServer.createContext( "/job.kjb", exchange -> serveFixture( exchange, jobFile ) );
    resourceServer.createContext( "/trans.ktr", exchange -> serveFixture( exchange, transFile ) );
    resourceServer.start();

    singleton = CarteSingleton.getInstance();
    previousConfig = CarteSingleton.getSlaveServerConfig();
    previousTransMap = singleton.getTransformationMap();
    previousJobMap = singleton.getJobMap();
    previousSocketRepository = singleton.getSocketRepository();
    singleton.setTransformationMap( new TransformationMap() );
    singleton.setJobMap( new JobMap() );
    singleton.setSocketRepository( new SocketRepository( new LogChannel( "ExecuteFileSchemesIT" ) ) );

    String xml = "<slave_config>" + configuration + "<slaveserver><name>integration-test</name>"
      + "<hostname>127.0.0.1</hostname><port>0</port><username>test</username><password>test</password>"
      + "</slaveserver></slave_config>";
    Document document = XMLHandler.loadXMLString( xml );
    SlaveServerConfig config =
      new SlaveServerConfig( new LogChannel( "ExecuteFileSchemesIT" ), XMLHandler.getSubNode( document, "slave_config" ) );
    carte = new Carte( config, false );
    port = ( (ServerConnector) carte.getWebServer().getServer().getConnectors()[0] ).getLocalPort();
  }

  /**
   * Stops outstanding executions and both servers and restores the Carte singleton state used by the next case.
   * The worker JVM owns the remaining Kettle state and exits after all cases have run.
   */
  @After
  public void tearDown() {
    try {
      if ( carte != null ) {
        try {
          for ( CarteObjectEntry entry : singleton.getTransformationMap().getTransformationObjects() ) {
            Trans trans = singleton.getTransformationMap().getTransformation( entry );
            if ( trans != null && !trans.isFinishedOrStopped() ) {
              trans.stopAll();
            }
            singleton.getTransformationMap().removeTransformation( entry );
          }
          for ( CarteObjectEntry entry : singleton.getJobMap().getJobObjects() ) {
            Job job = singleton.getJobMap().getJob( entry );
            if ( job != null && !job.isFinished() ) {
              job.stopAll();
              job.waitUntilFinished( 10000 );
            }
            singleton.getJobMap().removeJob( entry );
          }
        } finally {
          carte.getWebServer().stopServer();
        }
      }
    } finally {
      try {
        if ( singleton != null ) {
          if ( carte == null ) {
            singleton.getSocketRepository().closeAll();
          }
          singleton.setTransformationMap( previousTransMap );
          singleton.setJobMap( previousJobMap );
          singleton.setSocketRepository( previousSocketRepository );
          CarteSingleton.setSlaveServerConfig( previousConfig );
        }
      } finally {
        if ( resourceServer != null ) {
          resourceServer.stop( 0 );
        }
      }
    }
  }

  @Test
  public void enforcesConfiguredSchemesForJobs() throws Exception {
    assertConfiguredSchemes( true );
  }

  @Test
  public void enforcesConfiguredSchemesForTransformations() throws Exception {
    assertConfiguredSchemes( false );
  }

  /**
   * Checks allowed and rejected sources, including repository paths that do not require a file scheme.
   * A file URI is also tested with a repository configured: it must still obey the scheme allow-list.
   */
  private void assertConfiguredSchemes( boolean job ) throws Exception {
    Path file = job ? jobFile : transFile;
    assertExecution( job, file.toUri().toString(), fileAllowed );
    assertExecution( job, file.toAbsolutePath().toString(), fileAllowed );

    String resourceUrl = "http://127.0.0.1:" + resourceServer.getAddress().getPort()
      + ( job ? "/job.kjb" : "/trans.ktr" );
    int requestsBefore = resourceRequests.get();
    int getsBefore = resourceGets.get();
    assertExecution( job, resourceUrl, httpAllowed );
    if ( httpAllowed ) {
      assertTrue( "Allowed HTTP execution must load the fixture", resourceGets.get() > getsBefore );
    } else {
      assertEquals( "Rejected HTTP execution must not fetch the fixture", requestsBefore, resourceRequests.get() );
    }
    assertRejected( job, "ftp://127.0.0.1/fixture" );

    String pvfs = "pvfs://" + connectionName + ( job ? "/job.kjb" : "/trans.ktr" );
    assertExecution( job, pvfs, pvfsAllowed );
    assertExecution( job, "/folder/" + ( job ? "one-step-job" : "one-step-trans" ), true, true );
    assertExecution( job, file.toUri().toString(), fileAllowed, true );
  }

  private Path copyFixture( String resource, String name ) throws Exception {
    Path destination = temporaryFolder.getRoot().toPath().resolve( name );
    Files.copy( Path.of( System.getProperty( "execute.it.fixtures" ), resource ), destination );
    return destination;
  }

  private void serveFixture( HttpExchange exchange, Path fixture ) throws IOException {
    resourceRequests.incrementAndGet();
    if ( "HEAD".equals( exchange.getRequestMethod() ) ) {
      exchange.sendResponseHeaders( 200, -1 );
      exchange.close();
      return;
    }
    resourceGets.incrementAndGet();
    byte[] content = Files.readAllBytes( fixture );
    exchange.sendResponseHeaders( 200, content.length );
    try ( var output = exchange.getResponseBody() ) {
      output.write( content );
    }
  }

  private void assertExecution( boolean job, String path, boolean allowed ) throws Exception {
    assertExecution( job, path, allowed, false );
  }

  /**
   * For allowed paths, verifies that Carte actually runs the fixture, not just that it accepts the request.
   * Rejected paths must return HTTP 400 without registering an execution.
   */
  private void assertExecution( boolean job, String path, boolean allowed, boolean repository ) throws Exception {
    if ( !allowed ) {
      assertRejected( job, path, repository );
      return;
    }
    HttpResponse<String> response = execute( job, path, repository );
    assertEquals( response.body(), 200, response.statusCode() );
    if ( job ) {
      assertTrue( response.body(), response.body().contains( "<result>OK</result>" ) );
      Job executed = singleton.getJobMap().getJob( "one-step-job" );
      assertNotNull( "Job must be registered by Carte", executed );
      executed.waitUntilFinished( 10000 );
      assertTrue( "Job must finish", executed.isFinished() );
      assertEquals( 0, executed.getErrors() );
      assertNotNull( executed.getResult() );
      assertTrue( executed.getResult().getResult() );
      singleton.getJobMap().removeJob( singleton.getJobMap().getFirstCarteObjectEntry( "one-step-job" ) );
    } else {
      Trans executed = singleton.getTransformationMap().getTransformation( "one-step-trans" );
      assertNotNull( "Transformation must be registered by Carte", executed );
      assertTrue( "Transformation must finish", executed.isFinished() );
      assertEquals( 0, executed.getErrors() );
      assertFalse( executed.getSteps().isEmpty() );
      assertEquals( 10, executed.getSteps().get( 0 ).step.getLinesWritten() );
      for ( CarteObjectEntry entry : singleton.getTransformationMap().getTransformationObjects() ) {
        singleton.getTransformationMap().removeTransformation( entry );
      }
    }
  }

  private void assertRejected( boolean job, String path ) throws Exception {
    assertRejected( job, path, false );
  }

  private void assertRejected( boolean job, String path, boolean repository ) throws Exception {
    int jobsBefore = singleton.getJobMap().getJobObjects().size();
    int transformationsBefore = singleton.getTransformationMap().getTransformationObjects().size();
    HttpResponse<String> response = execute( job, path, repository );
    assertEquals( response.body(), 400, response.statusCode() );
    assertTrue( response.body(), response.body().contains( "Execution is not allowed" ) );
    assertEquals( jobsBefore, singleton.getJobMap().getJobObjects().size() );
    assertEquals( transformationsBefore, singleton.getTransformationMap().getTransformationObjects().size() );
  }

  private HttpResponse<String> execute( boolean job, String path, boolean repository ) throws Exception {
    String endpoint = job ? "/kettle/executeJob/?job=" : "/kettle/executeTrans/?trans=";
    URI uri = URI.create( "http://127.0.0.1:" + port + endpoint
      + URLEncoder.encode( path, StandardCharsets.UTF_8 ) + ( repository ? "&rep=" + REPOSITORY_NAME : "" ) );
    String credentials = Base64.getEncoder().encodeToString( "test:test".getBytes( StandardCharsets.UTF_8 ) );
    HttpRequest request = HttpRequest.newBuilder( uri ).timeout( Duration.ofSeconds( 30 ) )
      .header( "Authorization", "Basic " + credentials ).GET().build();
    return client.send( request, HttpResponse.BodyHandlers.ofString() );
  }

  public static class TestFileConnection extends BaseVFSConnectionDetails {
    private String name;
    private VariableSpace space;

    @Override public String getName() {
      return name;
    }

    @Override public void setName( String name ) {
      this.name = name;
    }

    @Override public String getType() {
      return "file";
    }

    @Override public String getDescription() {
      return "Temporary test connection";
    }

    @Override public VariableSpace getSpace() {
      return space;
    }

    @Override public void setSpace( VariableSpace space ) {
      this.space = space;
    }

    @Override public boolean hasBuckets() {
      return false;
    }

    @Override public TestFileConnection cloneDetails() {
      TestFileConnection copy = new TestFileConnection();
      copy.setName( name );
      copy.setRootPath( getRootPath() );
      return copy;
    }
  }

  /**
   * Supplies a local connection to the production PVFS resolver without requiring an enterprise VFS plugin.
   */
  private static class TestFileProvider extends BaseVFSConnectionProvider<TestFileConnection> {
    private TestFileConnection connection;

    @Override public String getName() {
      return "Test file connection";
    }

    @Override public String getKey() {
      return "file";
    }

    @Override public Class<TestFileConnection> getClassType() {
      return TestFileConnection.class;
    }

    @Override public boolean isStorageManaged() {
      return false;
    }

    @Override public List<TestFileConnection> getConnectionDetails( ConnectionManager manager ) {
      return Collections.singletonList( connection );
    }

    @Override public List<VFSRoot> getLocations( TestFileConnection details ) {
      return Collections.emptyList();
    }

    @Override public String getProtocol( TestFileConnection details ) {
      return "file";
    }

    @Override public boolean test( TestFileConnection details ) {
      return true;
    }
  }
}
