/*******************************************************************************
 * Copyright (c) 2013, 2017 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.scripting;

import java.io.Serializable;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.util.ParamCheck;
import org.eclipse.rap.rwt.remote.RemoteObject;


public class ClientFunction implements Serializable {

  private static final String REMOTE_TYPE = "rwt.scripting.Function";

  private final RemoteObject remoteObject;

  public ClientFunction( String scriptCode ) {
    ParamCheck.notNull( scriptCode, "scriptCode" );
    remoteObject = RWT.getUISession().getConnection().createRemoteObject( REMOTE_TYPE );
    remoteObject.set( "name", "handleEvent" );
    remoteObject.set( "scriptCode", scriptCode );
  }

  String getRemoteId() {
    return remoteObject.getId();
  }

}
