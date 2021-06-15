/*******************************************************************************
 * Copyright (c) 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.protocol;

import static org.eclipse.rap.rwt.internal.protocol.OperationReader.readOperation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.util.ParamCheck;


public class Message implements Serializable {

  private static final String HEAD = "head";
  private static final String OPERATIONS = "operations";

  private final JsonObject head;
  private final List<Operation> operations;

  protected Message( JsonObject head, List<Operation> operations ) {
    ParamCheck.notNull( head, "head" );
    ParamCheck.notNull( operations, "operations" );
    this.head = head;
    this.operations = operations;
  }

  protected Message( JsonObject json ) {
    ParamCheck.notNull( json, "json" );
    head = readHead( json );
    operations = readOperations( json );
  }

  public JsonObject getHead() {
    return head;
  }

  public List<Operation> getOperations() {
    return operations;
  }

  public JsonObject toJson() {
    JsonArray operationsArray = new JsonArray();
    for( Operation operation : operations ) {
      operationsArray.add( operation.toJson() );
    }
    return new JsonObject().add( HEAD, head ).add( OPERATIONS, operationsArray );
  }

  @Override
  public String toString() {
    return toJson().toString();
  }

  private static JsonObject readHead( JsonObject message ) {
    try {
      return message.get( HEAD ).asObject();
    } catch( Exception exception ) {
      throw new IllegalArgumentException( "Failed to read head from JSON message", exception );
    }
  }

  private static List<Operation> readOperations( JsonObject message ) {
    try {
      return processOperations( message.get( OPERATIONS ).asArray() );
    } catch( Exception exception ) {
      throw new IllegalArgumentException( "Failed to read operations from JSON message", exception );
    }
  }

  private static List<Operation> processOperations( JsonArray operationsArray ) {
    List<Operation> operations = new ArrayList<>( operationsArray.size() );
    for( JsonValue operation : operationsArray ) {
      operations.add( readOperation( operation.asArray() ) );
    }
    return operations;
  }

}
