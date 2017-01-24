package org.pentaho.di.engine.kettleclassic;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.api.reporting.ILogicalModelElement;
import org.pentaho.di.engine.api.reporting.IMaterializedModelElement;
import org.pentaho.di.engine.api.reporting.IModelElement;
import org.pentaho.di.engine.api.reporting.IReportingEvent;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.reactivestreams.Publisher;
import rx.Observable;
import rx.RxReactiveStreams;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.pentaho.di.engine.kettleclassic.ClassicUtils.TRANS_META_CONF_KEY;

/**
 * Created by nbaker on 1/5/17.
 */
public class ClassicKettleExecutionContext implements IExecutionContext {
  private Map<String, Object> parameters = new HashMap<String, Object>();
  private Map<String, Object> environment = new HashMap<String, Object>();
  private Scheduler scheduler = Schedulers.io();
  private TransExecutionConfiguration executionConfiguration = new TransExecutionConfiguration();
  private String[] arguments;
  private IMetaStore metaStore;
  private Repository repository;

  private Cache<PublisherKey, Publisher> publishers = CacheBuilder.newBuilder().build();

  private Map<ILogicalModelElement, IMaterializedModelElement> logical2MaterializedMap = new HashMap<>();

  private final ITransformation logicalTrans;
  private final ClassicKettleEngine engine;
  private final TransMeta transMeta;
  private final ClassicTransformation materializedTrans;

  public TransMeta getTransMeta() {
    return transMeta;
  }

  public ClassicTransformation getMaterializedTransformation() {
    return materializedTrans;
  }


  public ClassicKettleExecutionContext( ClassicKettleEngine engine, ITransformation trans ) {
    this.engine = engine;
    this.logicalTrans = trans;

    // Materialize Trans and populate Logical -> Materialized map
    materializedTrans = ClassicUtils.materialize( this, logicalTrans );
    transMeta = trans.getConfig( TRANS_META_CONF_KEY, TransMeta.class )
      .orElseThrow( () -> new RuntimeException( "TransMeta is required in config for ClassicKettleExecutionContext" ) );

    List<IModelElement> modelElements = new ArrayList<>();
    modelElements.add( materializedTrans );
    modelElements.addAll( materializedTrans.getOperations() );
    modelElements.addAll( materializedTrans.getHops() );

    logical2MaterializedMap
      .putAll( modelElements.stream().map( IMaterializedModelElement.class::cast ).collect( Collectors.toMap(
        IMaterializedModelElement::getLogicalElement, Function.identity() ) ) );

  }

  @Override
  @SuppressWarnings( "unchecked" )
  public <S extends ILogicalModelElement, D extends Serializable>
  Publisher<IReportingEvent<S, D>> eventStream( S source, Class<D> type ) {
    try {
      // Cache as a member cannot use these method type parameters. Having to ignore types

      return publishers.get( new PublisherKey( source, type ),
        () -> {
          IMaterializedModelElement iMaterializedModelElement1 = logical2MaterializedMap.get( source );
          List<Publisher<? extends IReportingEvent>> publishers = iMaterializedModelElement1.getPublisher( type );
          Stream<Observable<? extends IReportingEvent>> observableStream = publishers.stream().map( RxReactiveStreams::toObservable );
          List<Observable<? extends IReportingEvent>> collect = observableStream.collect( toList() );
          Observable<IReportingEvent> concat = Observable.merge( collect );
          return RxReactiveStreams.toPublisher( concat );
        });

    } catch ( ExecutionException e ) {
      throw new RuntimeException( e );
    }
  }

  @Override public Collection<ILogicalModelElement> getReportingSources() {
    return ImmutableList.of();
  }

  @Override public CompletableFuture<IExecutionResult> execute() {
    return engine.execute( this );
  }

  @Override public Map<String, Object> getParameters() {
    return parameters;
  }

  @Override public Map<String, Object> getEnvironment() {
    return environment;
  }

  @Override public ITransformation getTransformation() {
    return logicalTrans;
  }


  public void setExecutionConfiguration( TransExecutionConfiguration executionConfiguration ) {
    this.executionConfiguration = executionConfiguration;
  }

  public TransExecutionConfiguration getExecutionConfiguration() {
    return executionConfiguration;
  }

  public void setMetaStore( IMetaStore metaStore ) {
    this.metaStore = metaStore;
  }

  public void setRepository( Repository repository ) {
    this.repository = repository;
  }

  public void setArguments( String[] arguments ) {
    this.arguments = arguments;
  }

  public IMetaStore getMetaStore() {
    return metaStore;
  }

  public Repository getRepository() {
    return repository;
  }

  public void setScheduler( Scheduler scheduler ) {
    this.scheduler = scheduler;
  }

  public Scheduler getScheduler() {
    return scheduler;
  }


  private static class PublisherKey {
    private final ILogicalModelElement source;
    private final Class<? extends Serializable> eventType;

    public PublisherKey( ILogicalModelElement source, Class<? extends Serializable> eventType ) {
      this.source = source;
      this.eventType = eventType;
    }

    @Override public int hashCode() {
      int result = source.hashCode();
      result = 31 * result + eventType.hashCode();
      return result;
    }

  }
}
