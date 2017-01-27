package org.pentaho.di.engine.kettleclassic;

import com.google.common.collect.ImmutableMap;
import io.reactivex.BackpressureStrategy;
import io.reactivex.subjects.PublishSubject;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.ExecutionContext;
import org.pentaho.di.engine.api.model.Hop;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.api.reporting.Status;
import org.pentaho.di.engine.api.model.LogicalModelElement;
import org.pentaho.di.engine.api.model.MaterializedModelElement;
import org.pentaho.di.engine.api.reporting.ReportingEvent;
import org.pentaho.di.engine.api.reporting.StatusEvent;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransListener;
import org.pentaho.di.trans.TransMeta;
import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Created by nbaker on 1/6/17.
 */
public class ClassicTransformation implements Transformation, MaterializedModelElement {
  private List<ClassicOperation> operations;
  private Trans trans;
  private ClassicKettleExecutionContext executionContext;
  private TransMeta transMeta;
  private Transformation logicalTransformation;

  private PublishSubject<StatusEvent<Transformation>> statusPublisher = PublishSubject.create();
  private Map<Class<? extends Serializable>, PublishSubject<? extends ReportingEvent>> eventPublisherMap =
    new HashMap<>();

  {
    eventPublisherMap.put( Status.class, statusPublisher );
  }


  public ClassicTransformation( ExecutionContext executionContext, Transformation logicalTransformation ) {
    this.executionContext = (ClassicKettleExecutionContext) executionContext;
    this.logicalTransformation = logicalTransformation;
  }

  @Override public List<Operation> getOperations() {
    return operations.stream().collect( toList() );
  }

  @Override public List<Hop> getHops() {
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

    // propagate to operations
    operations.forEach( MaterializedModelElement::init );
  }

  public Trans getTrans() {
    return trans;
  }

  public TransMeta getTransMeta() {
    return executionContext.getTransMeta();
  }

  @Override
  public <D extends Serializable> List<Publisher<? extends ReportingEvent>> getPublisher(
    Class<D> type ) {
    return eventPublisherMap.entrySet().stream()
      .filter( e -> type.isAssignableFrom( e.getKey() ) )
      .map( entry -> entry.getValue().toFlowable( BackpressureStrategy.BUFFER ) )
      .collect( toList() );

  }

  @Override public List<Serializable> getEventTypes() {
    return Collections.unmodifiableList( eventPublisherMap.keySet().stream().collect( toList() ) );
  }

  @Override public Map<String, Object> getConfig() {
    return ImmutableMap.of();
  }

  @Override public LogicalModelElement getLogicalElement() {
    return logicalTransformation;
  }
}
