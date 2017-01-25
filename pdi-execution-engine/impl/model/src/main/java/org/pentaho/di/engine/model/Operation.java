package org.pentaho.di.engine.model;

import com.google.common.collect.ImmutableList;
import org.pentaho.di.engine.api.model.IHop;
import org.pentaho.di.engine.api.model.IOperation;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by hudak on 1/17/17.
 */
public class Operation extends Configurable implements IOperation {

  private final String id;
  private final Transformation transformation;

  public Operation( String id, Transformation transformation ) {
    this.id = id;
    this.transformation = transformation;
  }

  @Override public String getId() {
    return id;
  }

  private Stream<IHop> getHopsWhere( Function<IHop, IOperation> hopFn ) {
    return transformation.getHops().stream().filter( hop -> this.equals( hopFn.apply( hop ) ) );
  }

  @Override public List<IOperation> getFrom() {
    ImmutableList.Builder<IOperation> builder = ImmutableList.builder();
    getHopsWhere( IHop::getTo ).map( IHop::getFrom ).forEach( builder::add );
    return builder.build();
  }

  @Override public List<IOperation> getTo() {
    ImmutableList.Builder<IOperation> builder = ImmutableList.builder();
    getHopsWhere( IHop::getFrom ).map( IHop::getTo ).forEach( builder::add );
    return builder.build();
  }

  @Override public List<IHop> getHopsIn() {
    ImmutableList.Builder<IHop> builder = ImmutableList.builder();
    getHopsWhere( IHop::getTo ).forEach( builder::add );
    return builder.build();
  }

  @Override public List<IHop> getHopsOut() {
    ImmutableList.Builder<IHop> builder = ImmutableList.builder();
    getHopsWhere( IHop::getFrom ).forEach( builder::add );
    return builder.build();
  }

  @Override public String toString() {
    return "Operation{id='" + id + "'}";
  }

  public Hop createHopTo( IOperation to ) {
    return transformation.createHop( this, to );
  }

  public Hop createHopTo( IOperation to, String type ) {
    return transformation.createHop( this, to, type );
  }

}
