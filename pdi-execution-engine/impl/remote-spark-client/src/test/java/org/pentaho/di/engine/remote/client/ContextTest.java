package org.pentaho.di.engine.remote.client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.engine.api.model.Hop;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.remote.Notification;
import org.pentaho.di.engine.api.reporting.Metrics;
import org.pentaho.di.engine.model.Transformation;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

@RunWith( MockitoJUnitRunner.class )
public class ContextTest {
  @Mock private SparkClientEngine engine;
  @Mock private Consumer<Notification> notificationConsumer;
  @Mock private Consumer<Metrics> metricsConsumer;
  @Mock private Runnable runnable;
  private Transformation transformation;
  private Context context;

  @Before
  public void setUp() throws Exception {
    transformation = new Transformation( "Context Test Transformation" );

    IntStream.range( 0, 10 ).mapToObj( i -> "Operation " + i ).forEach( transformation::createOperation );

    List<Operation> operations = transformation.getOperations();
    for ( Iterator<Operation> from = operations.iterator(), to = operations.stream().skip( 1 ).iterator();
          from.hasNext() && to.hasNext(); ) {
      transformation.createHop( from.next(), to.next() );
    }

    context = new Context( engine, transformation );
  }

  @Test
  public void reportingSources() throws Exception {
    assertThat( context.getReportingSources(), hasItem( transformation ) );

    Operation[] operations = transformation.getOperations().stream().toArray( Operation[]::new );
    assertThat( context.getReportingSources(), hasItems( operations ) );

    Hop[] hops = transformation.getHops().stream().toArray( Hop[]::new );
    assertThat( context.getReportingSources(), hasItems( hops ) );
  }

  @Test
  public void stateNotifications() throws Exception {
    context.subscribe( transformation, Notification.class, notificationConsumer, runnable );

    // Verify claim is idempotent
    assertThat( serviceNotifications( Notification.Type.CLAIM, 15 ).filter( context::update ).toArray(), arrayWithSize( 1 ) );
    verify( notificationConsumer ).accept( argThat( hasProperty( "type", equalTo( Notification.Type.CLAIM ) ) ) );

    // Verify closing stream, ignore distractions from other services
    assertThat( serviceNotifications( Notification.Type.CLOSE, 15 ).filter( context::update ).toArray(), arrayWithSize( 1 ) );
    verify( notificationConsumer ).accept( argThat( hasProperty( "type", equalTo( Notification.Type.CLOSE ) ) ) );
    verify( runnable ).run();

    verifyNoMoreInteractions( notificationConsumer );
  }

  private Stream<Notification> serviceNotifications( Notification.Type type, int num ) {
    return IntStream.range( 0, num ).mapToObj( i -> new Notification( "service " + i, type ) ).parallel();
  }

  @Test
  public void eventSubscription() throws Exception {
    Operation operation = transformation.getOperations().get( 3 );
    context.subscribe( operation, Metrics.class, metricsConsumer, runnable );

    serviceNotifications( Notification.Type.CLAIM, 1 ).forEach( context::update );
    LongStream.range( 0, 10 )
      .mapToObj( i -> new Metrics( i, 0, 0, 0 ) )
      .forEach( metrics -> context.update( operation.getId(), metrics ) );
    serviceNotifications( Notification.Type.CLOSE, 1 ).forEach( context::update );

    verify( metricsConsumer, times( 10 ) ).accept( any() );
    verify( runnable ).run();

    context.update( operation.getId(), Metrics.empty() );
    verifyNoMoreInteractions( metricsConsumer );
  }
}