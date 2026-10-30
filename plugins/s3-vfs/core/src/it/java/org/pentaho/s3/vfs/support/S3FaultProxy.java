package org.pentaho.s3.vfs.support;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Forwards requests to S3Mock unless the supplied policy injects an S3 error response. */
public final class S3FaultProxy implements AutoCloseable {

  public interface Fault {
    Optional<Failure> reject( HttpExchange exchange );

    default void forwarded( HttpExchange exchange, int status ) {
    }
  }

  public record Failure( int status, String code, String operation ) {
    private void respond( HttpExchange exchange ) throws IOException {
      byte[] error = ( "<Error><Code>" + code + "</Code><Message>Injected " + operation
        + " failure</Message><RequestId>" + operation + "-failure</RequestId></Error>" )
        .getBytes( StandardCharsets.UTF_8 );
      exchange.getResponseHeaders().set( "Content-Type", "application/xml" );
      exchange.sendResponseHeaders( status, error.length );
      exchange.getResponseBody().write( error );
    }
  }

  private static final Set<String> TRANSPORT_HEADERS = Set.of(
    "host", "connection", "content-length", "transfer-encoding", "expect", "upgrade", "http2-settings" );

  private final URI upstream;
  private final Fault fault;
  private final HttpServer server;
  private final ExecutorService executor = Executors.newCachedThreadPool();
  private final HttpClient client = HttpClient.newBuilder()
    .executor( executor ).version( HttpClient.Version.HTTP_1_1 )
    .connectTimeout( Duration.ofSeconds( 10 ) ).build();

  public S3FaultProxy( String endpoint, Fault fault ) throws IOException {
    this.fault = fault;
    upstream = URI.create( endpoint );
    server = HttpServer.create( new InetSocketAddress( "127.0.0.1", 0 ), 0 );
    server.setExecutor( executor );
    server.createContext( "/", this::handle );
    server.start();
  }

  public String endpoint() {
    return "http://127.0.0.1:" + server.getAddress().getPort();
  }

  private void handle( HttpExchange exchange ) throws IOException {
    try ( exchange ) {
      // Drain rejected requests too: inject an S3 error, not a broken connection.
      byte[] body = exchange.getRequestBody().readAllBytes();
      Optional<Failure> failure = fault.reject( exchange );
      if ( failure.isPresent() ) {
        failure.get().respond( exchange );
      } else {
        forward( exchange, body );
      }
    }
  }

  private void forward( HttpExchange exchange, byte[] body ) throws IOException {
    HttpRequest.Builder request = HttpRequest.newBuilder( upstream.resolve( exchange.getRequestURI() ) )
      .timeout( Duration.ofSeconds( 30 ) )
      .method( exchange.getRequestMethod(), HttpRequest.BodyPublishers.ofByteArray( body ) );
    exchange.getRequestHeaders().forEach( ( name, values ) -> {
      if ( !TRANSPORT_HEADERS.contains( name.toLowerCase( Locale.ROOT ) ) ) {
        values.forEach( value -> request.header( name, value ) );
      }
    } );
    try {
      HttpResponse<InputStream> response = client.send( request.build(), HttpResponse.BodyHandlers.ofInputStream() );
      try ( InputStream in = response.body() ) {
        response.headers().map().forEach( ( name, values ) -> {
          if ( !TRANSPORT_HEADERS.contains( name.toLowerCase( Locale.ROOT ) ) ) {
            exchange.getResponseHeaders().put( name, values );
          }
        } );
        if ( "HEAD".equals( exchange.getRequestMethod() ) || response.statusCode() == 204 ) {
          response.headers().firstValue( "content-length" )
            .ifPresent( length -> exchange.getResponseHeaders().set( "Content-Length", length ) );
          exchange.sendResponseHeaders( response.statusCode(), -1 );
        } else {
          exchange.sendResponseHeaders( response.statusCode(), 0 );
          in.transferTo( exchange.getResponseBody() );
        }
        fault.forwarded( exchange, response.statusCode() );
      }
    } catch ( InterruptedException e ) {
      Thread.currentThread().interrupt();
      throw new IOException( "Interrupted while forwarding to S3Mock", e );
    }
  }

  @Override
  public void close() {
    server.stop( 0 );
    executor.shutdownNow();
  }
}