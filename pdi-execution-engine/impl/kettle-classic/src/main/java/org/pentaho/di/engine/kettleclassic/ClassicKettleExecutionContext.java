package org.pentaho.di.engine.kettleclassic;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import io.reactivex.Flowable;
import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.IExecutionResult;
import org.pentaho.di.engine.api.model.ILogicalModelElement;
import org.pentaho.di.engine.api.model.IMaterializedModelElement;
import org.pentaho.di.engine.api.model.IModelElement;
import org.pentaho.di.engine.api.model.ITransformation;
import org.pentaho.di.engine.api.reporting.IReportingEvent;
import org.pentaho.di.engine.api.reporting.Topic;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.reactivestreams.Publisher;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.pentaho.di.engine.kettleclassic.ClassicUtils.TRANS_META_CONF_KEY;

/**
 * Created by nbaker on 1/5/17.
 */
public class ClassicKettleExecutionContext implements IExecutionContext {
  private Map<String, Object> parameters = new HashMap<String, Object>();
  private Map<String, Object> environment = new HashMap<String, Object>();
  private TransExecutionConfiguration executionConfiguration = new TransExecutionConfiguration();
  private String[] arguments;
  private IMetaStore metaStore;
  private Repository repository;

  private Cache<Topic, Publisher> publishers = CacheBuilder.newBuilder().build();

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

      return publishers.get( new Topic( source, type ),
        () -> Flowable.merge( logical2MaterializedMap.get( source ).getPublisher( type ) )
      );

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


}
