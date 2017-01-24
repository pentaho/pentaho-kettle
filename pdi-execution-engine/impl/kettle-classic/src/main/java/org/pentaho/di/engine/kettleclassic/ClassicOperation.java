package org.pentaho.di.engine.kettleclassic;

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.IHop;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.IOperationVisitor;
import org.pentaho.di.engine.api.reporting.Status;
import org.pentaho.di.engine.api.reporting.ILogicalModelElement;
import org.pentaho.di.engine.api.reporting.IMaterializedModelElement;
import org.pentaho.di.engine.api.reporting.IReportingEvent;
import org.pentaho.di.engine.api.reporting.Metrics;
import org.pentaho.di.engine.api.reporting.MetricsEvent;
import org.pentaho.di.engine.api.reporting.StatusEvent;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepListener;
import org.pentaho.di.trans.step.StepMeta;
import org.reactivestreams.Publisher;
import rx.RxReactiveStreams;
import rx.subjects.PublishSubject;

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
public class ClassicOperation implements IOperation, IMaterializedModelElement {
  private IExecutionContext context;
  private IOperation logicalOperation;
  private ClassicTransformation transformation;


  private PublishSubject<MetricsEvent<IOperation>> metricsPublisher = PublishSubject.create();
  private PublishSubject<StatusEvent<IOperation>> statusPublisher = PublishSubject.create();
  private Map<Class<? extends Serializable>, PublishSubject<? extends IReportingEvent>> eventPublisherMap = new HashMap<>();
  private ExecutorService metricsExecutorService;

  {
    eventPublisherMap.put( Metrics.class, metricsPublisher );
    eventPublisherMap.put( Status.class, statusPublisher );
  }

  public ClassicOperation( IExecutionContext context, IOperation logicalOperation ) {
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

  @Override public List<IOperation> getFrom() {
    return null;
  }

  @Override public List<IOperation> getTo() {
    return null;
  }

  @Override public List<IHop> getHopsIn() {
    return null;
  }

  @Override public List<IHop> getHopsOut() {
    return null;
  }

  @Override public Map<String, Object> getConfig() {
    return logicalOperation.getConfig();
  }

  @Override public <T> T accept( IOperationVisitor<T> visitor ) {
    return null;
  }

  public void setTransformation( ClassicTransformation transformation ) {
    this.transformation = transformation;
  }

  @Override public ILogicalModelElement getLogicalElement() {
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

      });
    }

    if( metricsPublisher.hasObservers() ) {
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

  private void sendMetricsEvent( List<StepInterface> stepInterfaces) {
    stepInterfaces.stream()
      .forEach( stepInterface ->
        metricsExecutorService.execute( () -> metricsPublisher.onNext( createMetricsEvent( stepInterface ) ) ) );
  }
  private MetricsEvent<IOperation> createMetricsEvent( StepInterface stepInterfaces ) {

    return new MetricsEvent<>( logicalOperation, new ClassicKettleMetrics.Builder()
      .withDropped( stepInterfaces.getLinesRejected() )
      .withRead( stepInterfaces.getLinesRead() )
      .withWritten( stepInterfaces.getLinesWritten() )
      .withIn( stepInterfaces.getLinesInput() )
      .withOut( stepInterfaces.getLinesOutput() )
      .withUpdated( stepInterfaces.getLinesUpdated() )
      .withRejected( stepInterfaces.getLinesRejected() )
      .withErrors( stepInterfaces.getErrors() )
      .withInflight( stepInterfaces.getLinesRead() - stepInterfaces.getProcessed() )
      .build() );
  }

  @Override
  public <D extends Serializable> List<Publisher<? extends IReportingEvent>> getPublisher(
    Class<D> type ) {
    return eventPublisherMap.entrySet().stream().filter( e -> type.isAssignableFrom( e.getKey() ) )
      .flatMap( e -> Stream.of(e.getValue()) ).map( RxReactiveStreams::toPublisher  ).collect( toList() );

  }

  @Override public List<Serializable> getEventTypes() {
    return Collections.unmodifiableList( eventPublisherMap.keySet().stream().collect( Collectors.toList() ) );
  }
}
