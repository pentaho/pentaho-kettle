package org.pentaho.di.engine.kettlenative.impl;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.IOperationVisitor;
import org.pentaho.di.trans.step.StepMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Operation implements IOperation {

  private final String id;
  private final String config;
  private final List<IOperation> from;
  private final List<IOperation> to;

  private Operation( StepMeta meta, Map<StepMeta, IOperation> operations ) {
    operations.put( meta, this );
    id = meta.getName();
    try {
      config = meta.getXML();
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }
    from = getOpsFromSteps( meta.getParentTransMeta().findPreviousSteps( meta ), operations );
    to =   getOpsFromSteps( meta.getParentTransMeta().findNextSteps( meta ), operations );
  }

  public static List<IOperation> convert( List<StepMeta> metas ) {
    Map<StepMeta, IOperation> operations = new HashMap<>();
    return metas.stream()
      .map( meta -> convert( meta,  operations ) )
      .collect( Collectors.toList() );
  }

  public static IOperation convert( StepMeta meta, Map<StepMeta, IOperation> convertedOps ) {
    if ( convertedOps.containsKey( meta ) ) {
      return convertedOps.get( meta );
    } else {
      return new Operation( meta, convertedOps );
    }
  }

  @Override public String getId() {
    return id;
  }

  @Override public List<IOperation> getFrom() {
    return from;
  }

  @Override public List<IOperation> getTo() {
    return to;
  }

  @Override public String getConfig() {
    return config;
  }

  @Override public <T> T accept( IOperationVisitor<T> visitor ) {
    return null;
  }

  private List<IOperation> getOpsFromSteps( List<StepMeta> steps, Map<StepMeta, IOperation> operations ) {
    return steps.stream()
      .map( meta -> convert( meta, operations ) )
      .collect( Collectors.toList() );
  }

}
