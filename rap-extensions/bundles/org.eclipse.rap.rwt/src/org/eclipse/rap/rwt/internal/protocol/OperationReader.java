/*******************************************************************************
 * Copyright (c) 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.protocol;

import org.eclipse.rap.json.JsonArray;
import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.internal.protocol.Operation.CallOperation;
import org.eclipse.rap.rwt.internal.protocol.Operation.CreateOperation;
import org.eclipse.rap.rwt.internal.protocol.Operation.DestroyOperation;
import org.eclipse.rap.rwt.internal.protocol.Operation.ListenOperation;
import org.eclipse.rap.rwt.internal.protocol.Operation.NotifyOperation;
import org.eclipse.rap.rwt.internal.protocol.Operation.SetOperation;
import org.eclipse.rap.rwt.internal.util.ParamCheck;


public class OperationReader {

  public static Operation readOperation( JsonValue json ) {
    ParamCheck.notNull( json, "json" );
    try {
      return readOperation( json.asArray() );
    } catch( Exception exception ) {
      throw new IllegalArgumentException( "Could not read operation: " + json, exception );
    }
  }

  private static Operation readOperation( JsonArray json ) {
    String action = json.get( 0 ).asString();
    String target = json.get( 1 ).asString();
    if( action.equals( "create" ) ) {
      return readCreateOperation( json, target );
    }
    if( action.equals( "destroy" ) ) {
      return readDestroyOperation( json, target );
    }
    if( action.equals( "set" ) ) {
      return readSetOperation( json, target );
    }
    if( action.equals( "call" ) ) {
      return readCallOperation( json, target );
    }
    if( action.equals( "listen" ) ) {
      return readListenOperation( json, target );
    }
    if( action.equals( "notify" ) ) {
      return readNotifyOperation( json, target );
    }
    throw new IllegalArgumentException( "Unknown operation type: " + action );
  }

  private static Operation readCreateOperation( JsonArray json, String target ) {
    String type = json.get( 2 ).asString();
    JsonObject properties = json.get( 3 ).asObject();
    return new CreateOperation( target, type, properties );
  }

  private static Operation readDestroyOperation( JsonArray json, String target ) {
    return new DestroyOperation( target );
  }

  private static Operation readSetOperation( JsonArray json, String target ) {
    JsonObject properties = json.get( 2 ).asObject();
    return new SetOperation( target, properties );
  }

  private static Operation readCallOperation( JsonArray json, String target ) {
    String method = json.get( 2 ).asString();
    JsonObject parameters = json.get( 3 ).asObject();
    return new CallOperation( target, method, parameters );
  }

  private static Operation readListenOperation( JsonArray json, String target ) {
    JsonObject properties = json.get( 2 ).asObject();
    return new ListenOperation( target, properties );
  }

  private static Operation readNotifyOperation( JsonArray json, String target ) {
    String event = json.get( 2 ).asString();
    JsonObject properties = json.get( 3 ).asObject();
    return new NotifyOperation( target, event, properties );
  }

}
