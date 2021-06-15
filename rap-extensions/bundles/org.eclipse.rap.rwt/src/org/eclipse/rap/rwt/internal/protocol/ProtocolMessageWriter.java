/*******************************************************************************
* Copyright (c) 2010, 2015 EclipseSource and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    EclipseSource - initial API and implementation
*******************************************************************************/
package org.eclipse.rap.rwt.internal.protocol;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.Operation.CallOperation;
import org.eclipse.rap.rwt.internal.protocol.Operation.CreateOperation;
import org.eclipse.rap.rwt.internal.protocol.Operation.DestroyOperation;
import org.eclipse.rap.rwt.internal.protocol.Operation.ListenOperation;
import org.eclipse.rap.rwt.internal.protocol.Operation.SetOperation;


public class ProtocolMessageWriter {

  private final JsonObject head;
  private final List<Operation> operations;
  private Operation pendingOperation;
  private boolean alreadyCreated;

  public ProtocolMessageWriter() {
    head = new JsonObject();
    operations = new ArrayList<>();
  }

  public void appendHead( String property, int value ) {
    appendHead( property, JsonValue.valueOf( value ) );
  }

  public void appendHead( String property, String value ) {
    appendHead( property, JsonValue.valueOf( value ) );
  }

  public void appendHead( String property, JsonValue value ) {
    ensureMessagePending();
    head.add( property, value );
  }

  public void appendCreate( String target, String type ) {
    prepareOperation( new CreateOperation( target, type ) );
  }

  public void appendSet( String target, String property, int value ) {
    appendSet( target, property, JsonValue.valueOf( value ) );
  }

  public void appendSet( String target, String property, double value ) {
    appendSet( target, property, JsonValue.valueOf( value ) );
  }

  public void appendSet( String target, String property, boolean value ) {
    appendSet( target, property, JsonValue.valueOf( value ) );
  }

  public void appendSet( String target, String property, String value ) {
    appendSet( target, property, JsonValue.valueOf( value ) );
  }

  public void appendSet( String target, String property, JsonValue value ) {
    CreateOperation createOperation = findPendingOperation( target, CreateOperation.class );
    if( createOperation != null ) {
      createOperation.putProperty( property, value );
    } else {
      SetOperation setOperation = findPendingOperation( target, SetOperation.class );
      if( setOperation == null ) {
        setOperation = new Operation.SetOperation( target );
        prepareOperation( setOperation );
      }
      setOperation.putProperty( property, value );
    }
  }

  public void appendListen( String target, String eventType, boolean listen ) {
    ListenOperation operation = findPendingOperation( target, ListenOperation.class );
    if( operation == null ) {
      operation = new ListenOperation( target );
      prepareOperation( operation );
    }
    operation.putListener( eventType, listen );
  }

  public void appendCall( String target, String methodName, JsonObject parameters ) {
    prepareOperation( new CallOperation( target, methodName, parameters ) );
  }

  public void appendDestroy( String target ) {
    prepareOperation( new DestroyOperation( target ) );
  }

  private void prepareOperation( Operation operation ) {
    ensureMessagePending();
    appendPendingOperation();
    pendingOperation = operation;
  }

  public ResponseMessage createMessage() {
    ensureMessagePending();
    alreadyCreated = true;
    return createMessageObject();
  }

  private void ensureMessagePending() {
    if( alreadyCreated ) {
      throw new IllegalStateException( "Message already created" );
    }
  }

  private ResponseMessage createMessageObject() {
    appendPendingOperation();
    return new ResponseMessage( head, operations );
  }

  @SuppressWarnings( "unchecked" )
  private <T extends Operation> T findPendingOperation( String target, Class<T> type ) {
    boolean matches =    pendingOperation != null
                      && pendingOperation.getClass().equals( type )
                      && pendingOperation.getTarget().equals( target );
    return matches ? (T) pendingOperation : null;
  }

  private void appendPendingOperation() {
    if( pendingOperation != null ) {
      operations.add( pendingOperation );
    }
  }

}
