package org.pentaho.di.engine.remote.client;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.remote.ExecutionRequest;
import org.pentaho.di.engine.api.reporting.Metrics;
import org.pentaho.di.engine.api.reporting.MetricsEvent;
import org.pentaho.di.engine.model.Transformation;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.same;

/**
 * Created by hudak on 1/31/17.
 */
@RunWith( MockitoJUnitRunner.class )
public class SparkClientEngineTest {
  private static final String REQUEST_SERVICE_NAME = ExecutionRequest.class.getName();

  @Mock private BundleContext bundleContext;
  @Mock private Context context;
  private Transformation transformation;
  private SparkClientEngine engine;

  @Before
  public void setUp() throws Exception {
    engine = new SparkClientEngine( bundleContext );
    transformation = new Transformation( UUID.randomUUID().toString() );

    when( context.getTransformation() ).thenReturn( transformation );
  }

  @Test
  public void prepare() throws Exception {
    assertThat( engine.prepare( transformation ), instanceOf( Context.class ) );
  }

  @Test
  public void execute() throws Exception {
    Operation[] operations = IntStream.range( 0, 5 )
      .mapToObj( id -> transformation.createOperation( "operation " + id ) )
      .toArray( Operation[]::new );

    when( context.eventStream( isA( Operation.class ), eq( Metrics.class ) ) ).then( invocation -> {
      Operation operation = (Operation) invocation.getArguments()[ 0 ];
      return Flowable.intervalRange( 1, 20, 100, 100, TimeUnit.MILLISECONDS )
        .map( i -> new Metrics( i, 0, 0, 0 ) )
        .map( metrics -> new MetricsEvent<>( operation, metrics ) );
    } );

    ServiceRegistration serviceRegistration = mock( ServiceRegistration.class );
    when( bundleContext.registerService( eq( REQUEST_SERVICE_NAME ), same( context ), any() ) )
      .thenReturn( serviceRegistration );

    Map<Operation, Metrics> report = engine.execute( context ).get().getDataEventReport();

    assertThat( report.keySet(), containsInAnyOrder( operations ) );
    assertThat( report.values(), everyItem( hasProperty( "in", equalTo( 20L ) ) ) );
    verify( serviceRegistration ).unregister();
  }

}