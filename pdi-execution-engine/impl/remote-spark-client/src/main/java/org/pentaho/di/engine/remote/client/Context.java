package org.pentaho.di.engine.remote.client;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import org.pentaho.di.engine.api.ExecutionContext;
import org.pentaho.di.engine.api.ExecutionResult;
import org.pentaho.di.engine.api.model.LogicalModelElement;
import org.pentaho.di.engine.api.model.Transformation;
import org.pentaho.di.engine.api.remote.ExecutionRequest;
import org.pentaho.di.engine.api.remote.Notification;
import org.pentaho.di.engine.api.reporting.ReportingEvent;
import org.pentaho.di.engine.api.reporting.Topic;
import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by hudak on 1/25/17.
 */
class Context implements ExecutionContext, ExecutionRequest {
  private final SparkClientEngine engine;
  private final Transformation transformation;
  private final ImmutableList<LogicalModelElement> reportingSources;
  private final ConcurrentMap<Topic, Observable<? extends Serializable>> subscriptions = Maps.newConcurrentMap();
  private final Subject<RemoteEvent> eventSubject = PublishSubject.<RemoteEvent>create().toSerialized();
  private final AtomicReference<String> service = new AtomicReference<>();

  Context( SparkClientEngine engine, Transformation transformation ) {
    this.engine = engine;
    this.transformation = transformation;

    ImmutableList.Builder<LogicalModelElement> builder = ImmutableList.builder();
    builder.add( transformation );
    builder.addAll( transformation.getOperations() );
    builder.addAll( transformation.getHops() );
    reportingSources = builder.build();
  }

  @Override public Map<String, Object> getParameters() {
    return ImmutableMap.of();
  }

  @Override public Map<String, Object> getEnvironment() {
    return ImmutableMap.of();
  }

  @Override public Transformation getTransformation() {
    return transformation;
  }

  @Override public Map<String, Set<Class<? extends Serializable>>> getReportingTopics() {
    HashMultimap<String, Class<? extends Serializable>> topics = subscriptions.keySet().stream().collect(
      HashMultimap::create,
      ( accumulator, topic ) -> accumulator.put( topic.getSourceId(), topic.getEventType() ),
      HashMultimap::putAll
    );

    return Multimaps.asMap( topics );
  }

  @Override public boolean update( Notification notification ) {
    Notification.Type type = notification.getType();
    String serviceId = notification.getServiceId();

    // Ignore notifications from other execution services
    switch( type ) {
      case CLAIM:
        if ( !service.compareAndSet( null, serviceId ) ) {
          return false;
        }
        break;
      default:
        if ( !serviceId.equals( service.get() ) ) {
          return false;
        }
        break;
    }

    // Publish the update
    boolean accepted = update( transformation.getId(), notification );

    // Complete all observables if a CLOSE notification
    switch( type ) {
      case CLOSE:
        eventSubject.onComplete();
        break;
    }

    return accepted;
  }

  @Override public boolean update( String sourceId, Serializable value ) {
    if ( service.get() != null && eventSubject.hasObservers() ) {
      eventSubject.onNext( new RemoteEvent( sourceId, value ) );
      return true;
    } else {
      return false;
    }
  }

  @Override public CompletableFuture<ExecutionResult> execute() {
    return engine.execute( this );
  }

  @Override
  public <S extends LogicalModelElement, D extends Serializable> Publisher<ReportingEvent<S, D>> eventStream(
    S source, Class<D> type ) {
    return subscriptions.computeIfAbsent( new Topic( source, type ), this::topicObservable )
      .cast( type ).map( data -> LocalReportingEvent.create( source, data ) )
      .toFlowable( BackpressureStrategy.LATEST );
  }

  @Override public Collection<LogicalModelElement> getReportingSources() {
    return reportingSources;
  }

  private Observable<? extends Serializable> topicObservable( Topic topic ) {
    return eventSubject
      .filter( event -> topic.getSourceId().equals( event.getSourceId() ) )
      .map( RemoteEvent::getValue )
      .ofType( topic.getEventType() );
  }

  private static class LocalReportingEvent<S extends LogicalModelElement, D extends Serializable>
    implements ReportingEvent<S, D> {
    private final S source;
    private final D data;

    static <S extends LogicalModelElement, D extends Serializable> ReportingEvent<S, D> create( S source, D data ) {
      return new LocalReportingEvent<>( source, data );
    }

    LocalReportingEvent( S source, D data ) {
      this.source = source;
      this.data = data;
    }

    @Override public S getSource() {
      return source;
    }

    @Override public D getData() {
      return data;
    }
  }

  private static class RemoteEvent {
    private final String sourceId;
    private final Serializable value;

    RemoteEvent( String sourceId, Serializable value ) {
      this.sourceId = sourceId;
      this.value = value;
    }

    String getSourceId() {
      return sourceId;
    }

    Serializable getValue() {
      return value;
    }
  }
}
