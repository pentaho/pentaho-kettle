package org.pentaho.di.engine.kettlenative.impl;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.engine.api.IExecutableOperationFactory;
import org.pentaho.di.engine.api.IOperationFactoryRegistry;
import org.pentaho.di.engine.kettlenative.impl.factories.KettleExecOperationFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class OperationFactoryRegistry implements IOperationFactoryRegistry {


  private final Map<String, IExecutableOperationFactory> factoryMap;
  private final IExecutableOperationFactory fallbackFactory;

  public OperationFactoryRegistry( Map<String, IExecutableOperationFactory> factoryMap,
                                   IExecutableOperationFactory fallback ) {
    this.factoryMap = Collections.unmodifiableMap( factoryMap );
    this.fallbackFactory = fallback;
  }

  @VisibleForTesting
  protected OperationFactoryRegistry() {
    this( Collections.emptyMap(), new KettleExecOperationFactory() );
  }

  @Override public IExecutableOperationFactory getFactory( String operationId ) {
    return Optional.of( factoryMap.get( operationId ) )
      .orElse( fallbackFactory );
  }

}
