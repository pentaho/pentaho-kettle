package org.pentaho.di.engine.kettlenative.impl;

import org.pentaho.di.base.BaseHopMeta;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.engine.api.IHop;
import org.pentaho.di.engine.api.IOperation;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Hop implements IHop {

  private final IOperation fromOp;
  private final IOperation toOp;

  private final BaseHopMeta hopMeta;

  public Hop( TransHopMeta hopMeta, TransMeta transMeta, Map<StepMeta, IOperation> operations,
              Map<TransHopMeta, IHop> hops ) {
    this.hopMeta = hopMeta;

    fromOp = operations.computeIfAbsent( hopMeta.getFromStep(), thisStepMeta -> Operation.convert( hopMeta.getFromStep(), transMeta, operations, hops ));
    toOp = operations.computeIfAbsent( hopMeta.getToStep(), thisStepMeta -> Operation.convert( hopMeta.getToStep(), transMeta, operations, hops ));
  }

  @Override public IOperation getFrom() {
    return fromOp;
  }

  @Override public IOperation getTo() {
    return toOp;
  }

  public static List<IHop> convertTo( TransMeta transMeta, StepMeta stepMeta, Map<StepMeta, IOperation> operations,
                                      Map<TransHopMeta, IHop> hops ) {
    List<TransHopMeta> toHopMetas = getTransHopMetas( transMeta, hop -> stepMeta.equals( hop.getToStep() ) );
    return getIHops( transMeta, operations, hops, toHopMetas );

  }

  public static List<IHop> convertFrom( TransMeta transMeta, StepMeta stepMeta, Map<StepMeta, IOperation> operations,
                                      Map<TransHopMeta, IHop> hops ) {
    List<TransHopMeta> toHopMetas = getTransHopMetas( transMeta, hop -> stepMeta.equals( hop.getFromStep() ) );
    return getIHops( transMeta, operations, hops, toHopMetas );

  }

  private static List<IHop> getIHops( TransMeta transMeta, Map<StepMeta, IOperation> operations,
                                      Map<TransHopMeta, IHop> hops, List<TransHopMeta> toHopMetas ) {
    return toHopMetas.stream()
      .map( hopMeta -> hops.computeIfAbsent(
        hopMeta, thisHopMeta -> new Hop( thisHopMeta, transMeta, operations, hops ) ) )
      .collect( Collectors.toList());
  }

  private static List<TransHopMeta> getTransHopMetas( TransMeta transMeta,
                                                      Predicate<? super TransHopMeta> predicate )
  {
    return IntStream.range( 0, transMeta.nrTransHops() )
        .mapToObj( transMeta::getTransHop )
        .filter( predicate )
        .collect( Collectors.toList());
  }
}
