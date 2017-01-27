package org.pentaho.di.engine.kettlenative.impl;

import com.google.common.collect.ImmutableMap;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.model.Hop;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.trans.TransMeta;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Transformation implements org.pentaho.di.engine.api.model.Transformation {

  private final List<Operation> operations;
  private final TransMeta transMeta;
  protected Function<TransMeta, List<Operation>> opConverter = org.pentaho.di.engine.kettlenative.impl.Operation::convert;

  private Transformation( TransMeta transMeta ) {
    this.transMeta = transMeta;
    operations = opConverter.apply( transMeta );
  }

  public static org.pentaho.di.engine.api.model.Transformation convert( TransMeta transMeta ) {
    return new Transformation( transMeta );
  }

  @Override public List<Operation> getOperations() {
    return operations;
  }

  public List<Operation> getSourceOperations() {
    return operations.stream()
      .filter( op -> op.getFrom().isEmpty() )
      .collect( Collectors.toList() );
  }

  public List<Operation> getSinkOperations() {
    return operations.stream()
      .filter( op -> op.getTo().isEmpty() )
      .collect( Collectors.toList() );
  }

  @Override public List<Hop> getHops() {
    return null;
  }

  @Override public Map<String, Object> getConfig() {
    try {
      return ImmutableMap.of( "TransMeta.xml", transMeta.getXML() );
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }
  }

  @Override public String getId() {
    return transMeta.getName();
  }
}
