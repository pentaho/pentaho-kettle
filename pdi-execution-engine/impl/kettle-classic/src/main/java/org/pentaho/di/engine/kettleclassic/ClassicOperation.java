package org.pentaho.di.engine.kettleclassic;

import io.reactivex.BackpressureStrategy;
import io.reactivex.subjects.PublishSubject;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.engine.api.ExecutionContext;
import org.pentaho.di.engine.api.model.Hop;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.reporting.Status;
import org.pentaho.di.engine.api.model.LogicalModelElement;
import org.pentaho.di.engine.api.model.MaterializedModelElement;
import org.pentaho.di.engine.api.reporting.ReportingEvent;
import org.pentaho.di.engine.api.reporting.Metrics;
import org.pentaho.di.engine.api.reporting.MetricsEvent;
import org.pentaho.di.engine.api.reporting.StatusEvent;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepListener;
import org.pentaho.di.trans.step.StepMeta;
import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by nbaker on 1/6/17.
 */
public class ClassicOperation implements Operation, MaterializedModelElement {
  private ExecutionContext context;
  private Operation logicalOperation;
  private ClassicTransformation transformation;


  private PublishSubject<MetricsEvent<Operation>> metricsPublisher = PublishSubject.create();
  private PublishSubject<StatusEvent<Operation>> statusPublisher = PublishSubject.create();
  private Map<Class<? extends Serializable>, PublishSubject<? extends ReportingEvent>> eventPublisherMap =
    new HashMap<>();
  private ExecutorService metricsExecutorService;

  {
    eventPublisherMap.put( Metrics.class, metricsPublisher );
    eventPublisherMap.put( Status.class, statusPublisher );
  }

  public ClassicOperation( ExecutionContext context, Operation logicalOperation ) {
    this.context = context;
    this.logicalOperation = logicalOperation;
  }

  @Override public String getId() {
    return logicalOperation.getId();
  }

  @Override public Optional<Object> getConfig( String key ) {
    return null;
  }

  @Override public <T> Optional<T> getConfig( String key, Class<T> type ) {
    return null;
  }

  @Override public List<Operation> getFrom() {
    return null;
  }

  @Override public List<Operation> getTo() {
    return null;
  }

  @Override public List<Hop> getHopsIn() {
    return null;
  }

  @Override public List<Hop> getHopsOut() {
    return null;
  }

  @Override public Map<String, Object> getConfig() {
    return logicalOperation.getConfig();
  }

  public void setTransformation( ClassicTransformation transformation ) {
    this.transformation = transformation;
  }

  @Override public LogicalModelElement getLogicalElement() {
    return logicalOperation;
  }

  @Override public void init() {
    List<StepInterface> stepInterfaces = transformation.getTrans().findStepInterfaces( logicalOperation.getId() );

    // Status events, if multiple copies need to wait for all before sending event out
    if ( statusPublisher.hasObservers() ) {
      final PhasedEventPublisher runningEventPublisher = new PhasedEventPublisher( stepInterfaces.size(),
        () -> statusPublisher.onNext( new StatusEvent<>( logicalOperation, Status.RUNNING ) ) );

      final PhasedEventPublisher stoppedEventPublisher = new PhasedEventPublisher( stepInterfaces.size(),
        () -> statusPublisher.onNext( new StatusEvent<>( logicalOperation, Status.STOPPED ) ) );

      stepInterfaces.forEach( stepInterface -> {

        stepInterface.addStepListener( new StepListener() {
          @Override public void stepActive( Trans trans, StepMeta stepMeta, StepInterface stepInterface ) {
            runningEventPublisher.decriment();
          }

          @Override public void stepFinished( Trans trans, StepMeta stepMeta, StepInterface stepInterface ) {
            stoppedEventPublisher.decriment();
          }
        } );

      } );
    }

    if ( metricsPublisher.hasObservers() ) {
      metricsExecutorService = Executors.newSingleThreadExecutor();

      stepInterfaces.forEach( stepInterface -> {
        stepInterface.addRowListener( new RowListener() {
          @Override public void rowReadEvent( RowMetaInterface rowMetaInterface, Object[] objects )
            throws KettleStepException {
            sendMetricsEvent( stepInterfaces );
          }

          @Override public void rowWrittenEvent( RowMetaInterface rowMetaInterface, Object[] objects )
            throws KettleStepException {
            sendMetricsEvent( stepInterfaces );
          }

          @Override public void errorRowWrittenEvent( RowMetaInterface rowMetaInterface, Object[] objects )
            throws KettleStepException {
            sendMetricsEvent( stepInterfaces );
          }
        } );
      } );
    }

  }

  private void sendMetricsEvent( List<StepInterface> stepInterfaces ) {
    metricsExecutorService.execute( () -> metricsPublisher.onNext( createMetricsEvent( stepInterfaces ) ) );
  }

  private MetricsEvent<Operation> createMetricsEvent( List<StepInterface> stepInterfaces ) {

    Metrics metrics = new Metrics(
      stepInterfaces.stream().mapToLong( StepInterface::getLinesRead ).sum(),
      stepInterfaces.stream().mapToLong( StepInterface::getLinesWritten ).sum(),
      stepInterfaces.stream().mapToLong( StepInterface::getLinesRejected ).sum(),
      stepInterfaces.stream().mapToLong( value -> value.getLinesRead() - value.getProcessed() ).sum()
    );
    return new MetricsEvent<>( logicalOperation, metrics );
  }

  @Override
  public <D extends Serializable> List<Publisher<? extends ReportingEvent>> getPublisher( Class<D> type ) {
    return eventPublisherMap.entrySet().stream()
      .filter( e -> type.isAssignableFrom( e.getKey() ) )
      .map( entry -> entry.getValue().toFlowable( BackpressureStrategy.BUFFER ) )
      .collect( toList() );
  }

  @Override public List<Serializable> getEventTypes() {
    return Collections.unmodifiableList( eventPublisherMap.keySet().stream().collect( Collectors.toList() ) );
  }
}
