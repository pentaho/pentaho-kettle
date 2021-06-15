/*******************************************************************************
 * Copyright (c) 2013, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.internal.client;

import java.util.Locale;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.internal.RWTMessages;
import org.eclipse.rap.rwt.internal.remote.ConnectionImpl;
import org.eclipse.rap.rwt.remote.RemoteObject;


public class WebClientMessages implements ClientMessages {

  private static final String REMOTE_ID = "rwt.client.ClientMessages";
  private static final String PROP_MESSAGES = "messages";
  private static final String[] MESSAGE_IDS = {
    RWTMessages.SERVER_ERROR,
    RWTMessages.SERVER_ERROR_DESCRIPTION,
    RWTMessages.CONNECTION_ERROR,
    RWTMessages.CONNECTION_ERROR_DESCRIPTION,
    RWTMessages.SESSION_TIMEOUT,
    RWTMessages.SESSION_TIMEOUT_DESCRIPTION,
    RWTMessages.CLIENT_ERROR,
    RWTMessages.CLIENT_ERROR_DESCRIPTION,
    RWTMessages.RETRY,
    RWTMessages.RESTART,
    RWTMessages.DETAILS
  };

  private RemoteObject remoteObject;
  private String[] messages;

  public WebClientMessages() {
    ConnectionImpl connection = ( ConnectionImpl )RWT.getUISession().getConnection();
    remoteObject = connection.createServiceObject( REMOTE_ID );
    messages = new String[ MESSAGE_IDS.length ];
  }

  @Override
  public void update( Locale locale ) {
    JsonObject messagesObject = new JsonObject();
    for( int i = 0; i < MESSAGE_IDS.length; i++ ) {
      String newMessage = getMessage( MESSAGE_IDS[ i ], locale );
      if( !newMessage.equals( messages[ i ] ) ) {
        messages[ i ] = newMessage;
        messagesObject.add( MESSAGE_IDS[ i ], newMessage );
      }
    }
    if( !messagesObject.isEmpty() ) {
      remoteObject.set( PROP_MESSAGES, messagesObject );
    }
  }

  String getMessage( String messageId, Locale locale ) {
    return RWTMessages.getMessage( messageId, locale );
  }

}
