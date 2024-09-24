/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.collect.ImmutableList;
import org.pentaho.di.engine.api.model.Hop;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by hudak on 1/17/17.
 */
public class Operation extends Configurable implements org.pentaho.di.engine.api.model.Operation {

  private static final long serialVersionUID = -1975677580227607466L;
  private final String id;
  private final String key;
  private final Transformation transformation;

  public Operation( String id, Transformation transformation ) {
    this.id = id;
    this.key = id + UUID.randomUUID();
    this.transformation = transformation;
  }

  @Override public String getId() {
    return id;
  }

  @Override
  public String getKey() {
    return key;
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
