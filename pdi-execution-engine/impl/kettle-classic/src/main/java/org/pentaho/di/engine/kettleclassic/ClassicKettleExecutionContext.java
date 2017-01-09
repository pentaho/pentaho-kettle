package org.pentaho.di.engine.kettleclassic;

import org.pentaho.di.engine.api.IExecutionContext;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.metastore.api.IMetaStore;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nbaker on 1/5/17.
 */
public class ClassicKettleExecutionContext implements IExecutionContext {
  private Map<String, Object> parameters = new HashMap<String, Object>();
  private Map<String, Object> environment = new HashMap<String, Object>();
  private ITransformation transformation;
  private TransExecutionConfiguration executionConfiguration;
  private String[] arguments;
  private IMetaStore metaStore;
  private Repository repository;

  public ClassicKettleExecutionContext( ITransformation trans ) {
    this.transformation = trans;
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
}
