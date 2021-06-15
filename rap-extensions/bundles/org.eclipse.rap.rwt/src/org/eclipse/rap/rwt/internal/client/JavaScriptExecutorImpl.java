/*******************************************************************************
 * Copyright (c) 2009, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.client;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.JavaScriptExecutor;
import org.eclipse.rap.rwt.internal.remote.ConnectionImpl;
import org.eclipse.rap.rwt.remote.RemoteObject;


public final class JavaScriptExecutorImpl implements JavaScriptExecutor {

  private static final String REMOTE_ID = "rwt.client.JavaScriptExecutor";
  private final RemoteObject remoteObject;

  public JavaScriptExecutorImpl() {
    ConnectionImpl connection = ( ConnectionImpl )RWT.getUISession().getConnection();
    remoteObject = connection.createServiceObject( REMOTE_ID );
  }

  @Override
  public void execute( String code ) {
    remoteObject.call( "execute", new JsonObject().add( "content", code.trim() ) );
  }

}
