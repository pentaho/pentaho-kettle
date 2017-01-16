package org.pentaho.di.engine.kettleclassic;

import com.google.common.collect.ImmutableList;
import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.engine.api.reporting.IReportingEvent;
import org.pentaho.di.engine.api.reporting.IReportingEventSource;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.metastore.api.IMetaStore;
import org.reactivestreams.Publisher;
import rx.Observable;
import rx.RxReactiveStreams;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by nbaker on 1/5/17.
 */
public class ClassicKettleExecutionContext implements IExecutionContext {
  private Map<String, Object> parameters = new HashMap<String, Object>();
  private Map<String, Object> environment = new HashMap<String, Object>();
  private final ClassicKettleEngine engine;
  private Scheduler scheduler = Schedulers.io();
  private ITransformation transformation;
  private TransExecutionConfiguration executionConfiguration = new TransExecutionConfiguration();
  private String[] arguments;
  private IMetaStore metaStore;
  private Repository repository;

  public ClassicKettleExecutionContext( ClassicKettleEngine engine, ITransformation trans ) {
    this.engine = engine;
    this.transformation = trans;
  }

  @Override
  public <S extends IReportingEventSource, D extends Serializable>
  Publisher<IReportingEvent<S, D>> eventStream( S source, Class<D> type ) {
    return RxReactiveStreams.toPublisher( Observable.empty() );
  }

  @Override public Collection<IReportingEventSource> getReportingSources() {
    return ImmutableList.of();
  }

  @Override public CompletableFuture<IExecutionResult> execute() {
    return engine.execute( (ClassicKettleExecutionContext) this );
  }

  @Override public Map<String, Object> getParameters() {
    return parameters;
  }

  @Override public Map<String, Object> getEnvironment() {
    return environment;
  }

  @Override public ITransformation getTransformation() {
    return transformation;
  }

  public void setTransformation( ITransformation transformation ) {
    this.transformation = transformation;
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

  @Override public String[] getArguments() {
    return new String[ 0 ];
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
}
