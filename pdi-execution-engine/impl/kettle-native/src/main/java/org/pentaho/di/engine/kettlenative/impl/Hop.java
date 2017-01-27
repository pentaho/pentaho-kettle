package org.pentaho.di.engine.kettlenative.impl;

import org.pentaho.di.base.BaseHopMeta;
import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Hop implements org.pentaho.di.engine.api.model.Hop {

  private final Operation fromOp;
  private final Operation toOp;

  private final transient BaseHopMeta hopMeta;

  public Hop( TransHopMeta hopMeta, TransMeta transMeta, Map<StepMeta, Operation> operations,
              Map<TransHopMeta, org.pentaho.di.engine.api.model.Hop> hops ) {
    this.hopMeta = hopMeta;

    fromOp = operations.computeIfAbsent( hopMeta.getFromStep(),
      thisStepMeta -> org.pentaho.di.engine.kettlenative.impl.Operation
        .convert( hopMeta.getFromStep(), transMeta, operations, hops ) );
    toOp = operations.computeIfAbsent( hopMeta.getToStep(),
      thisStepMeta -> org.pentaho.di.engine.kettlenative.impl.Operation
        .convert( hopMeta.getToStep(), transMeta, operations, hops ) );
  }

  @Override public Operation getFrom() {
    return fromOp;
  }

  @Override public Operation getTo() {
    return toOp;
  }

  public static List<org.pentaho.di.engine.api.model.Hop> convertTo( TransMeta transMeta, StepMeta stepMeta, Map<StepMeta, Operation> operations,
                                                                     Map<TransHopMeta, org.pentaho.di.engine.api.model.Hop> hops ) {
    List<TransHopMeta> toHopMetas = getTransHopMetas( transMeta, hop -> stepMeta.equals( hop.getToStep() ) );
    return getIHops( transMeta, operations, hops, toHopMetas );

  }

  public static List<org.pentaho.di.engine.api.model.Hop> convertFrom( TransMeta transMeta, StepMeta stepMeta, Map<StepMeta, Operation> operations,
                                                                       Map<TransHopMeta, org.pentaho.di.engine.api.model.Hop> hops ) {
    List<TransHopMeta> toHopMetas = getTransHopMetas( transMeta, hop -> stepMeta.equals( hop.getFromStep() ) );
    return getIHops( transMeta, operations, hops, toHopMetas );

  }

  private static List<org.pentaho.di.engine.api.model.Hop> getIHops( TransMeta transMeta, Map<StepMeta, Operation> operations,
                                                                     Map<TransHopMeta, org.pentaho.di.engine.api.model.Hop> hops, List<TransHopMeta> toHopMetas ) {
    return toHopMetas.stream()
      .map( hopMeta -> hops.computeIfAbsent(
        hopMeta, thisHopMeta -> new Hop( thisHopMeta, transMeta, operations, hops ) ) )
      .collect( Collectors.toList() );
  }

  private static List<TransHopMeta> getTransHopMetas( TransMeta transMeta,
                                                      Predicate<? super TransHopMeta> predicate ) {
    return IntStream.range( 0, transMeta.nrTransHops() )
      .mapToObj( transMeta::getTransHop )
      .filter( predicate )
      .collect( Collectors.toList() );
  }
}
