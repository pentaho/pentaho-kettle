package org.pentaho.s3.vfs.client;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import software.amazon.awssdk.http.HttpExecuteRequest;
import software.amazon.awssdk.http.HttpExecuteResponse;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.http.async.AsyncExecuteRequest;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpResponseHandler;
import software.amazon.awssdk.http.async.SdkHttpContentPublisher;

/**
 * A plain GET issued through an SDK HTTP client, which is the smallest request that still goes
 * through the client's TLS configuration. Whatever the client was built with decides whether the
 * body comes back or the handshake fails.
 */
final class SdkHttpProbe {

  private SdkHttpProbe() {
  }

  static String get( SdkHttpClient client, URI uri ) throws Exception {
    HttpExecuteResponse response = client
      .prepareRequest( HttpExecuteRequest.builder().request( request( uri ) ).build() )
      .call();

    try ( InputStream body = response.responseBody().orElseThrow() ) {
      return new String( body.readAllBytes(), StandardCharsets.UTF_8 );
    }
  }

  static String get( SdkAsyncHttpClient client, URI uri ) throws Exception {
    CollectingHandler handler = new CollectingHandler();
    try {
      client.execute( AsyncExecuteRequest.builder()
        .request( request( uri ) )
        .requestContentPublisher( new EmptyBody() )
        .responseHandler( handler )
        .build() ).join();
      return handler.body.join();
    } catch ( CompletionException e ) {
      throw e.getCause() instanceof Exception cause ? cause : e;
    }
  }

  private static SdkHttpRequest request( URI uri ) {
    return SdkHttpRequest.builder().method( SdkHttpMethod.GET ).uri( uri ).build();
  }

  /** Keeps the response body around until the stream says there is no more of it. */
  private static final class CollectingHandler implements SdkAsyncHttpResponseHandler {

    private final CompletableFuture<String> body = new CompletableFuture<>();
    private final ByteArrayOutputStream collected = new ByteArrayOutputStream();

    @Override
    public void onHeaders( SdkHttpResponse headers ) {
      // the status is not what these tests are about
    }

    @Override
    public void onStream( Publisher<ByteBuffer> stream ) {
      stream.subscribe( new Subscriber<ByteBuffer>() {
        @Override
        public void onSubscribe( Subscription subscription ) {
          subscription.request( Long.MAX_VALUE );
        }

        @Override
        public void onNext( ByteBuffer chunk ) {
          byte[] bytes = new byte[ chunk.remaining() ];
          chunk.get( bytes );
          collected.writeBytes( bytes );
        }

        @Override
        public void onError( Throwable error ) {
          body.completeExceptionally( error );
        }

        @Override
        public void onComplete() {
          body.complete( collected.toString( StandardCharsets.UTF_8 ) );
        }
      } );
    }

    @Override
    public void onError( Throwable error ) {
      body.completeExceptionally( error );
    }
  }

  /** A GET carries nothing, but the client still asks for a publisher. */
  private static final class EmptyBody implements SdkHttpContentPublisher {

    @Override
    public Optional<Long> contentLength() {
      return Optional.of( 0L );
    }

    @Override
    public void subscribe( Subscriber<? super ByteBuffer> subscriber ) {
      subscriber.onSubscribe( new Subscription() {
        @Override
        public void request( long n ) {
          // nothing to hand over
        }

        @Override
        public void cancel() {
          // nothing to stop
        }
      } );
      subscriber.onComplete();
    }
  }
}
