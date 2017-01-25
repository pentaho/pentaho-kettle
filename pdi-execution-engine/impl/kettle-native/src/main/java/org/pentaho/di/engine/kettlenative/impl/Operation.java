package org.pentaho.di.engine.kettlenative.impl;

import com.google.common.collect.ImmutableMap;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.model.IHop;
import org.pentaho.di.engine.api.model.IOperation;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Operation implements IOperation {

  private final String id;
  private final String config;

  private final List<IHop> hopsIn;
  private final List<IHop> hopsOut;

  private Operation(
    StepMeta stepMeta, TransMeta transMeta, Map<TransHopMeta, IHop> hops, Map<StepMeta, IOperation> operations ) {
    operations.put( stepMeta, this );
    id = stepMeta.getName();
    try {
      config = stepMeta.getXML();
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }
    hopsIn = Hop.convertTo( transMeta, stepMeta, operations, hops );
    hopsOut = Hop.convertFrom( transMeta, stepMeta, operations, hops );
  }

  public static List<IOperation> convert( TransMeta transMeta ) {
    List<StepMeta> metas = transMeta.getSteps();
    Map<TransHopMeta, IHop> hops = new HashMap<>();
    Map<StepMeta, IOperation> operations = new HashMap<>();
    return metas.stream()
      .map( meta -> convert( meta, transMeta, operations, hops ) )
      .collect( Collectors.toList() );
  }

  public static IOperation convert( StepMeta stepMeta, TransMeta transMeta, Map<StepMeta, IOperation> convertedOps,
                                    Map<TransHopMeta, IHop> hops ) {
    return convertedOps
      .computeIfAbsent( stepMeta, meta -> new Operation( meta, transMeta, hops, convertedOps ) );
  }

  @Override public String getId() {
    return id;
  }

  @Override public List<IOperation> getFrom() {
    return getHopsIn().stream().map( out -> out.getFrom() ).collect( Collectors.toList() );
  }

  @Override public List<IOperation> getTo() {
    return getHopsOut().stream().map( out -> out.getTo() ).collect( Collectors.toList() );
  }

  @Override public List<IHop> getHopsIn() {
    return hopsIn;
  }

  @Override public List<IHop> getHopsOut() {
    return hopsOut;
  }

  @Override public Map<String, Object> getConfig() {
    return ImmutableMap.of( "StepMeta.xml", config );
  }

}
