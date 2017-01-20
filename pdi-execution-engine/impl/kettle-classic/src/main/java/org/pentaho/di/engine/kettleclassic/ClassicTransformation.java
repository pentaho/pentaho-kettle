package org.pentaho.di.engine.kettleclassic;

import com.google.common.collect.ImmutableMap;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.IHop;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.api.Status;
import org.pentaho.di.engine.api.reporting.ILogicalModelElement;
import org.pentaho.di.engine.api.reporting.IMaterializedModelElement;
import org.pentaho.di.engine.api.reporting.IReportingEvent;
import org.pentaho.di.engine.api.reporting.StatusEvent;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransListener;
import org.pentaho.di.trans.TransMeta;
import org.reactivestreams.Publisher;
import rx.RxReactiveStreams;
import rx.subjects.PublishSubject;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by nbaker on 1/6/17.
 */
public class ClassicTransformation implements ITransformation, IMaterializedModelElement {
  private List<ClassicOperation> operations;
  private Trans trans;
  private ClassicKettleExecutionContext executionContext;
  private TransMeta transMeta;
  private ITransformation logicalTransformation;

  private PublishSubject<StatusEvent<ITransformation>> statusPublisher = PublishSubject.create();
  private Map<Serializable, PublishSubject<? extends IReportingEvent>> eventPublisherMap = new HashMap<>();

  {
    eventPublisherMap.put( Status.class, statusPublisher );
  }


  public ClassicTransformation( IExecutionContext executionContext, ITransformation logicalTransformation ) {
    this.executionContext = (ClassicKettleExecutionContext) executionContext;
    this.logicalTransformation = logicalTransformation;
  }

  @Override public List<IOperation> getOperations() {
    return operations.stream().collect( Collectors.toList() );
  }

  @Override public List<IOperation> getSourceOperations() {
    return null;
  }

  @Override public List<IOperation> getSinkOperations() {
    return null;
  }

  @Override public List<IHop> getHops() {
    return Collections.emptyList();
  }

  @Override public String getId() {
    return logicalTransformation.getId();
  }

  public void setOperations( List<ClassicOperation> operations ) {
    this.operations = operations;
    this.operations.forEach( o -> o.setTransformation( ClassicTransformation.this ) );
  }

  public void setTrans( Trans trans ) {
    this.trans = trans;
  }

  @Override public void init() {
    // only attach listener is someone is subscribed.

    if ( statusPublisher.hasObservers() ) {
      trans.addTransListener( new TransListener() {
        @Override public void transStarted( Trans trans ) throws KettleException {
          statusPublisher.onNext( new StatusEvent<>( logicalTransformation, Status.RUNNING ) );
        }

        @Override public void transActive( Trans trans ) {
          statusPublisher.onNext( new StatusEvent<>( logicalTransformation, Status.RUNNING ) );
        }

        @Override public void transFinished( Trans trans ) throws KettleException {
          statusPublisher.onNext( new StatusEvent<>( logicalTransformation, Status.FINISHED ) );
        }
      } );
    }

    // propigate to operations
    operations.forEach( IMaterializedModelElement::init );

  }

  public Trans getTrans() {
    return trans;
  }

  public TransMeta getTransMeta() {
    return executionContext.getTransMeta();
  }

  @Override public <D extends Serializable> Optional<Publisher> getPublisher( Class<D> type ) {
    return Optional.ofNullable( eventPublisherMap.get( type ) ).map( RxReactiveStreams::toPublisher );
  }

  @Override public List<Serializable> getEventTypes() {
    return Collections.unmodifiableList( eventPublisherMap.keySet().stream().collect( Collectors.toList() ) );
  }

  @Override public Map<String, Object> getConfig() {
    return ImmutableMap.of();
  }

  @Override public ILogicalModelElement getLogicalElement() {
    return logicalTransformation;
  }
}
