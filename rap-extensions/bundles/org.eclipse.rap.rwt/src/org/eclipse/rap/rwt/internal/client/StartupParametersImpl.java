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
package org.eclipse.rap.rwt.internal.client;

import static org.eclipse.rap.rwt.internal.util.ParamCheck.notNullOrEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.json.JsonObject.Member;
import org.eclipse.rap.json.JsonValue;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.StartupParameters;
import org.eclipse.rap.rwt.internal.remote.ConnectionImpl;
import org.eclipse.rap.rwt.remote.AbstractOperationHandler;
import org.eclipse.rap.rwt.remote.RemoteObject;


public class StartupParametersImpl implements StartupParameters {

  private Map<String, List<String>> parameters;

  public StartupParametersImpl() {
    ConnectionImpl connection = ( ConnectionImpl )RWT.getUISession().getConnection();
    RemoteObject remoteObject = connection.createServiceObject( "rwt.client.StartupParameters" );
    remoteObject.setHandler( new StartupParametersOperationHandler() );
    parameters = Collections.emptyMap();
  }

  @Override
  public Collection<String> getParameterNames() {
    return Collections.unmodifiableSet( parameters.keySet() );
  }

  @Override
  public String getParameter( String name ) {
    notNullOrEmpty( name, "name" );
    List<String> values = parameters.get( name );
    return values == null ? null : values.get( 0 );
  }

  @Override
  public List<String> getParameterValues( String name ) {
    notNullOrEmpty( name, "name" );
    List<String> values = parameters.get( name );
    return values == null ? null : Collections.unmodifiableList( values );
  }

  final class StartupParametersOperationHandler extends AbstractOperationHandler {

    @Override
    public void handleSet( JsonObject properties ) {
      JsonValue params = properties.get( "parameters" );
      if( params != null ) {
        parameters = new HashMap<>();
        for( Member member : params.asObject() ) {
          List<String> stringValues = new ArrayList<>();
          for( JsonValue value : member.getValue().asArray() ) {
            stringValues.add( value.asString() );
          }
          parameters.put( member.getName(), stringValues );
        }
      }
    }

  }

}
