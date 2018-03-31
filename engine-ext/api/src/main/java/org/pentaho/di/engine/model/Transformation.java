/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */
package org.pentaho.di.engine.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.pentaho.di.engine.api.model.Hop;
import org.pentaho.di.engine.api.model.Operation;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by hudak on 1/17/17.
 */
public class Transformation extends Configurable implements org.pentaho.di.engine.api.model.Transformation {
  private static final long serialVersionUID = -4909410400954814057L;
  private final String id;
  private final LinkedList<Operation> operations = new LinkedList<>();
  private final LinkedList<Hop> hops = new LinkedList<>();

  public Transformation( String id ) {
    this.id = id;
  }

  @Override public String getId() {
    return id;
  }


  @Override public List<Operation> getOperations() {
    return ImmutableList.copyOf( operations );
  }

  private ImmutableList<Operation> filterOperations( Predicate<Operation> filter ) {
    ImmutableList.Builder<Operation> builder = ImmutableList.builder();
    operations.stream().filter( filter ).forEach( builder::add );
    return builder.build();
  }

  @Override public List<Hop> getHops() {
    return hops;
  }

  @Override public String toString() {
    return "Transformation{id='" + id + "'}";
  }

  public org.pentaho.di.engine.model.Operation createOperation( String id ) {
    org.pentaho.di.engine.model.Operation operation = new org.pentaho.di.engine.model.Operation( id, this );
    this.operations.add( operation );
    return operation;
  }

  public org.pentaho.di.engine.model.Hop createHop( Operation from, Operation to ) {
    return createHop( from, to, Hop.TYPE_NORMAL );
  }

  public org.pentaho.di.engine.model.Hop createHop( Operation from, Operation to, String type ) {
    Preconditions.checkArgument( operations.contains( from ), "!operations.contains(from)" );
    Preconditions.checkArgument( operations.contains( to ), "!operations.contains(to)" );
    Preconditions.checkArgument( from != to, "from == to" );
    org.pentaho.di.engine.model.Hop hop = new org.pentaho.di.engine.model.Hop( from, to, type );

    Preconditions.checkState( hops.stream().noneMatch( it -> it.getFrom() == from && it.getTo() == to ),
      "Hop from %s to %s already exists", from, to );

    hops.add( hop );

    return hop;
  }

}
