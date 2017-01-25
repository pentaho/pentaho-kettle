package org.pentaho.di.engine.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.pentaho.di.engine.api.model.IHop;
import org.pentaho.di.engine.api.model.IOperation;
import org.pentaho.di.engine.api.model.ITransformation;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by hudak on 1/17/17.
 */
public class Transformation extends Configurable implements ITransformation{
  private final String id;
  private final LinkedList<IOperation> operations = new LinkedList<>();
  private final LinkedList<IHop> hops = new LinkedList<>();

  public Transformation( String id ) {
    this.id = id;
  }

  @Override public String getId() {
    return id;
  }


  @Override public List<IOperation> getOperations() {
    return ImmutableList.copyOf( operations );
  }

  private ImmutableList<IOperation> filterOperations( Predicate<IOperation> filter ) {
    ImmutableList.Builder<IOperation> builder = ImmutableList.builder();
    operations.stream().filter( filter ).forEach( builder::add );
    return builder.build();
  }

  @Override public List<IHop> getHops() {
    return hops;
  }

  @Override public String toString() {
    return "Transformation{id='" + id + "'}";
  }

  public Operation createOperation( String id ) {
    Operation operation = new Operation( id, this );
    this.operations.add( operation );
    return operation;
  }

  public Hop createHop( IOperation from, IOperation to ) {
    return createHop( from, to, IHop.TYPE_NORMAL );
  }

  public Hop createHop( IOperation from, IOperation to, String type ) {
    Preconditions.checkArgument( operations.contains( from ), "!operations.contains(from)" );
    Preconditions.checkArgument( operations.contains( to ), "!operations.contains(to)" );
    Preconditions.checkArgument( from != to, "from == to" );
    Hop hop = new Hop( from, to, type );

    Preconditions.checkState( hops.stream().noneMatch( it -> it.getFrom() == from && it.getTo() == to ),
      "Hop from %s to %s already exists", from, to );

    hops.add( hop );

    return hop;
  }

}
