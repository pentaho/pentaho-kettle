package org.pentaho.s3.vfs.support;

import com.sun.net.httpserver.HttpExchange;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** Rejects one upload part once, or every part persistently, and tracks retries and cleanup. */
public final class UploadPartFault implements S3FaultProxy.Fault {

  private final boolean persistent;
  private final AtomicBoolean firstFailure = new AtomicBoolean( true );
  private final ConcurrentHashMap<String, AtomicInteger> failedAttempts = new ConcurrentHashMap<>();
  private final AtomicInteger successfulRetries = new AtomicInteger();
  private final CountDownLatch abortCompleted = new CountDownLatch( 1 );

  public UploadPartFault( boolean persistent ) {
    this.persistent = persistent;
  }

  @Override
  public Optional<S3FaultProxy.Failure> reject( HttpExchange exchange ) {
    if ( isUploadPart( exchange ) && ( persistent || firstFailure.compareAndSet( true, false ) ) ) {
      failedAttempts.computeIfAbsent( exchange.getRequestURI().toString(), ignored -> new AtomicInteger() )
        .incrementAndGet();
      return Optional.of( new S3FaultProxy.Failure( 503, "SlowDown", "part" ) );
    }
    return Optional.empty();
  }

  @Override
  public void forwarded( HttpExchange exchange, int status ) {
    if ( isUploadPart( exchange ) && failedAttempts.containsKey( exchange.getRequestURI().toString() )
      && status == 200 ) {
      successfulRetries.incrementAndGet();
    }
    String query = exchange.getRequestURI().getRawQuery();
    if ( "DELETE".equals( exchange.getRequestMethod() ) && query != null && query.contains( "uploadId=" )
      && status / 100 == 2 ) {
      abortCompleted.countDown();
    }
  }

  public int failures() {
    return failedAttempts.values().stream().mapToInt( AtomicInteger::get ).sum();
  }

  public int successfulRetries() {
    return successfulRetries.get();
  }

  public int mostFailuresForOnePart() {
    return failedAttempts.values().stream().mapToInt( AtomicInteger::get ).max().orElse( 0 );
  }

  public boolean awaitAbort() throws InterruptedException {
    return abortCompleted.await( 10, TimeUnit.SECONDS );
  }

  private boolean isUploadPart( HttpExchange exchange ) {
    String query = exchange.getRequestURI().getRawQuery();
    return "PUT".equals( exchange.getRequestMethod() ) && query != null
      && query.contains( "partNumber=" ) && query.contains( "uploadId=" );
  }
}