/*******************************************************************************
 * Copyright (c) 2012, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.internal.protocol.Operation.CallOperation;
import org.eclipse.rap.rwt.internal.protocol.Operation.NotifyOperation;
import org.eclipse.rap.rwt.internal.protocol.Operation.SetOperation;


public class ClientMessage extends RequestMessage {

  private Map<String, List<Operation>> index;

  public ClientMessage( JsonObject json ) {
    super( json );
    createOperationsIndex();
  }

  public ClientMessage( Message message ) {
    super( message.getHead(), message.getOperations() );
    createOperationsIndex();
  }

  private void createOperationsIndex() {
    index = new HashMap<>();
    for( Operation operation : getOperations() ) {
      String target = operation.getTarget();
      List<Operation> targetOperations = index.get( target );
      if( targetOperations == null ) {
        targetOperations = new ArrayList<>();
      }
      targetOperations.add( operation );
      index.put( target, targetOperations );
    }
  }

  public List<Operation> getAllOperationsFor( String target ) {
    List<Operation> operations = index.get( target );
    if( operations == null ) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList( operations );
  }

  public List<CallOperation> getAllCallOperationsFor( String target, String methodName ) {
    List<CallOperation> result = new ArrayList<>();
    List<Operation> selected = target == null ? getOperations() : index.get( target );
    if( selected != null ) {
      for( Operation operation : selected ) {
        if( operation instanceof CallOperation ) {
          CallOperation currentOperation = ( CallOperation )operation;
          if( methodName == null || currentOperation.getMethodName().equals( methodName ) ) {
            result.add( currentOperation );
          }
        }
      }
    }
    return result;
  }

  public SetOperation getLastSetOperationFor( String target, String property ) {
    SetOperation result = null;
    List<Operation> selected = target == null ? getOperations() : index.get( target );
    if( selected != null ) {
      for( Operation operation : selected ) {
        if( operation instanceof SetOperation ) {
          SetOperation setOperation = ( SetOperation )operation;
          if( property == null || setOperation.getProperties().get( property ) != null ) {
            result = setOperation;
          }
        }
      }
    }
    return result;
  }

  public NotifyOperation getLastNotifyOperationFor( String target, String eventName ) {
    NotifyOperation result = null;
    List<Operation> selected = target == null ? getOperations() : index.get( target );
    if( selected != null ) {
      for( Operation operation : selected ) {
        if( operation instanceof NotifyOperation ) {
          NotifyOperation currentOperation = ( NotifyOperation )operation;
          if( eventName == null || currentOperation.getEventName().equals( eventName ) ) {
            result = currentOperation;
          }
        }
      }
    }
    return result;
  }

}
