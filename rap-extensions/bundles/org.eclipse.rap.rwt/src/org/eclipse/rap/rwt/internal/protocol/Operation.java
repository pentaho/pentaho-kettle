/*******************************************************************************
 * Copyright (c) 2011, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.protocol;

import java.io.Serializable;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;


public abstract class Operation implements Serializable {

  private final String action;
  private final String target;

  Operation( String target, String action ) {
    this.target = target;
    this.action = action;
  }

  public String getTarget() {
    return target;
  }

  public JsonArray toJson() {
    return new JsonArray().add( action ).add( target );
  }

  public static class CreateOperation extends Operation {

    private final JsonObject properties;
    private final String type;

    CreateOperation( String target, String type ) {
      this( target, type, new JsonObject() );
    }

    CreateOperation( String target, String type, JsonObject properties ) {
      super( target, "create" );
      this.type = type;
      this.properties = properties;
    }

    public String getType() {
      return type;
    }

    public JsonObject getProperties() {
      return properties;
    }

    @Override
    public JsonArray toJson() {
      return super.toJson().add( type ).add( properties );
    }

    void putProperty( String key, JsonValue value ) {
      properties.set( key, value );
    }

  }

  public static class DestroyOperation extends Operation {

    DestroyOperation( String target ) {
      super( target, "destroy" );
    }

  }

  public static class SetOperation extends Operation {

    private final JsonObject properties;

    SetOperation( String target ) {
      this( target, new JsonObject() );
    }

    SetOperation( String target, JsonObject properties ) {
      super( target, "set" );
      this.properties = properties;
    }

    public JsonObject getProperties() {
      return properties;
    }

    @Override
    public JsonArray toJson() {
      return super.toJson().add( properties );
    }

    void putProperty( String key, JsonValue value ) {
      properties.set( key, value );
    }

  }

  public static class CallOperation extends Operation {

    private final String method;
    private final JsonObject parameters;

    CallOperation( String target, String method, JsonObject parameters ) {
      super( target, "call" );
      this.method = method;
      this.parameters = parameters != null ? parameters : new JsonObject();
    }

    public String getMethodName() {
      return method;
    }

    public JsonObject getParameters() {
      return parameters;
    }

    @Override
    public JsonArray toJson() {
      return super.toJson().add( method ).add( parameters );
    }

  }

  public static class ListenOperation extends Operation {

    private final JsonObject properties;

    ListenOperation( String target ) {
      this( target, new JsonObject() );
    }

    ListenOperation( String target, JsonObject properties ) {
      super( target, "listen" );
      this.properties = properties;
    }

    public JsonObject getProperties() {
      return properties;
    }

    @Override
    public JsonArray toJson() {
      return super.toJson().add( properties );
    }

    void putListener( String event, boolean listening ) {
      properties.set( event, JsonValue.valueOf( listening ) );
    }

  }

  public static class NotifyOperation extends Operation {

    private final JsonObject properties;
    private final String event;

    NotifyOperation( String target, String event ) {
      this( target, event, new JsonObject() );
    }

    NotifyOperation( String target, String event, JsonObject properties ) {
      super( target, "notify" );
      this.event = event;
      this.properties = properties;
    }

    public String getEventName() {
      return event;
    }

    public JsonObject getProperties() {
      return properties;
    }

    @Override
    public JsonArray toJson() {
      return super.toJson().add( event ).add( properties );
    }

    void putProperty( String key, JsonValue value ) {
      properties.set( key, value );
    }

  }

}
