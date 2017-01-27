package org.pentaho.di.engine.kettlenative.impl;

import com.google.common.collect.ImmutableMap;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.engine.api.model.Hop;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Operation implements org.pentaho.di.engine.api.model.Operation {

  private final String id;
  private final String config;

  private final List<Hop> hopsIn;
  private final List<Hop> hopsOut;

  private Operation(
    StepMeta stepMeta, TransMeta transMeta, Map<TransHopMeta, Hop> hops, Map<StepMeta, org.pentaho.di.engine.api.model.Operation> operations ) {
    operations.put( stepMeta, this );
    id = stepMeta.getName();
    try {
      config = stepMeta.getXML();
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }
    hopsIn = org.pentaho.di.engine.kettlenative.impl.Hop.convertTo( transMeta, stepMeta, operations, hops );
    hopsOut = org.pentaho.di.engine.kettlenative.impl.Hop.convertFrom( transMeta, stepMeta, operations, hops );
  }

  public static List<org.pentaho.di.engine.api.model.Operation> convert( TransMeta transMeta ) {
    List<StepMeta> metas = transMeta.getSteps();
    Map<TransHopMeta, Hop> hops = new HashMap<>();
    Map<StepMeta, org.pentaho.di.engine.api.model.Operation> operations = new HashMap<>();
    return metas.stream()
      .map( meta -> convert( meta, transMeta, operations, hops ) )
      .collect( Collectors.toList() );
  }

  public static org.pentaho.di.engine.api.model.Operation convert( StepMeta stepMeta, TransMeta transMeta, Map<StepMeta, org.pentaho.di.engine.api.model.Operation> convertedOps,
                                                                   Map<TransHopMeta, Hop> hops ) {
    return convertedOps
      .computeIfAbsent( stepMeta, meta -> new Operation( meta, transMeta, hops, convertedOps ) );
  }

  @Override public String getId() {
    return id;
  }

  @Override public List<org.pentaho.di.engine.api.model.Operation> getFrom() {
    return getHopsIn().stream().map( out -> out.getFrom() ).collect( Collectors.toList() );
  }

  @Override public List<org.pentaho.di.engine.api.model.Operation> getTo() {
    return getHopsOut().stream().map( out -> out.getTo() ).collect( Collectors.toList() );
  }

  @Override public List<Hop> getHopsIn() {
    return hopsIn;
  }

  @Override public List<Hop> getHopsOut() {
    return hopsOut;
  }

  @Override public Map<String, Object> getConfig() {
    return ImmutableMap.of( "StepMeta.xml", config );
  }

}
