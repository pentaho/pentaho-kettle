package org.pentaho.di.engine.model;

import com.google.common.collect.ImmutableList;
import org.pentaho.di.engine.api.model.Hop;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by hudak on 1/17/17.
 */
public class Operation extends Configurable implements org.pentaho.di.engine.api.model.Operation {

  private final String id;
  private final Transformation transformation;

  public Operation( String id, Transformation transformation ) {
    this.id = id;
    this.transformation = transformation;
  }

  @Override public String getId() {
    return id;
  }

  private Stream<Hop> getHopsWhere( Function<Hop, org.pentaho.di.engine.api.model.Operation> hopFn ) {
    return transformation.getHops().stream().filter( hop -> this.equals( hopFn.apply( hop ) ) );
  }

  @Override public List<org.pentaho.di.engine.api.model.Operation> getFrom() {
    ImmutableList.Builder<org.pentaho.di.engine.api.model.Operation> builder = ImmutableList.builder();
    getHopsWhere( Hop::getTo ).map( Hop::getFrom ).forEach( builder::add );
    return builder.build();
  }

  @Override public List<org.pentaho.di.engine.api.model.Operation> getTo() {
    ImmutableList.Builder<org.pentaho.di.engine.api.model.Operation> builder = ImmutableList.builder();
    getHopsWhere( Hop::getFrom ).map( Hop::getTo ).forEach( builder::add );
    return builder.build();
  }

  @Override public List<Hop> getHopsIn() {
    ImmutableList.Builder<Hop> builder = ImmutableList.builder();
    getHopsWhere( Hop::getTo ).forEach( builder::add );
    return builder.build();
  }

  @Override public List<Hop> getHopsOut() {
    ImmutableList.Builder<Hop> builder = ImmutableList.builder();
    getHopsWhere( Hop::getFrom ).forEach( builder::add );
    return builder.build();
  }

  @Override public String toString() {
    return "Operation{id='" + id + "'}";
  }

  public org.pentaho.di.engine.model.Hop createHopTo( org.pentaho.di.engine.api.model.Operation to ) {
    return transformation.createHop( this, to );
  }

  public org.pentaho.di.engine.model.Hop createHopTo( org.pentaho.di.engine.api.model.Operation to, String type ) {
    return transformation.createHop( this, to, type );
  }

}
