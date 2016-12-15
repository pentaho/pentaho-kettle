package org.pentaho.di.engine.kettlenative.impl;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.IHop;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.engine.api.ITransformation;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Transformation implements ITransformation {

  private final List<IOperation> operations;
  private final TransMeta transMeta;
  protected Function<TransMeta, List<IOperation>> opConverter = Operation::convert;

  private Transformation( TransMeta transMeta ) {
    this.transMeta = transMeta;
    operations =  opConverter.apply( transMeta );
  }

  public static ITransformation convert( TransMeta transMeta ) {
    return new Transformation( transMeta );
  }

  @Override public List<IOperation> getOperations() {
    return operations;
  }

  @Override public List<IOperation> getSourceOperations() {
    return operations.stream()
      .filter( op -> op.getFrom().isEmpty() )
      .collect( Collectors.toList() );
  }

  @Override public List<IOperation> getSinkOperations() {
    return operations.stream()
      .filter( op -> op.getTo().isEmpty() )
      .collect( Collectors.toList() );
  }

  @Override public List<IHop> getHops() {
    return null;
  }

  @Override public String getConfig() {
    try {
      return transMeta.getXML();
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }
  }

  @Override public String getId() {
    return transMeta.getName();
  }
}
