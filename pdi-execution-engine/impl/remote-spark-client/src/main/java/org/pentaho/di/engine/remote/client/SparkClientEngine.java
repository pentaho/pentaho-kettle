package org.pentaho.di.engine.remote.client;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.pentaho.di.engine.api.Engine;
import org.pentaho.di.engine.api.ExecutionContext;
import org.pentaho.di.engine.api.ExecutionResult;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.api.remote.ExecutionRequest;
import org.pentaho.di.engine.api.reporting.Metrics;
import org.pentaho.di.engine.api.reporting.ReportingEvent;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by hudak on 1/25/17.
 */
public class SparkClientEngine implements Engine {
  private final BundleContext bundleContext;

  public SparkClientEngine( BundleContext bundleContext ) {
    this.bundleContext = bundleContext;
  }


  @Override public ExecutionContext prepare( Transformation trans ) {
    return new Context( this, trans );
  }

  CompletableFuture<ExecutionResult> execute( Context context ) {
    Single<Map<Operation, Metrics>> report = Flowable.fromIterable( context.getTransformation().getOperations() )
      .flatMap( op -> context.eventStream( op, Metrics.class ) )
      .toMap( ReportingEvent::getSource, ReportingEvent::getData );

    Hashtable serviceProperties = new Hashtable();
    ServiceRegistration registration =
      bundleContext.registerService( ExecutionRequest.class.getName(), context, serviceProperties );

    CompletableFuture<ExecutionResult> result = new CompletableFuture<>();
    report
      .map( Result::new )
      .doFinally( registration::unregister )
      .subscribe( result::complete, result::completeExceptionally );
    return result;
  }

  private static class Result implements ExecutionResult {
    private final Map<Operation, Metrics> report;

    Result( Map<Operation, Metrics> report ) {
      this.report = report;
    }

    @Override public Map<Operation, Metrics> getDataEventReport() {
      return report;
    }
  }
}
