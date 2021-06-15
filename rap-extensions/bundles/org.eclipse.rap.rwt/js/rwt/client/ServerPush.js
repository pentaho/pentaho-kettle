/*******************************************************************************
 * Copyright (c) 2011, 2017 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

namespace( "rwt.client" );

rwt.client.ServerPush = function() {
  this._retryCount = 0;
  this._active = false;
  this._running = false;
};

rwt.client.ServerPush.getInstance = function() {
  return rwt.runtime.Singletons.get( rwt.client.ServerPush );
};

rwt.client.ServerPush.prototype = {

  setActive : function( active ) {
    this._active = active;
  },

  sendServerPushRequest : function() {
    if( this._active && !this._running ) {
      this._running = true;
      this._createRequest().send();
    }
  },

  _createRequest : function() {
    var connection = rwt.remote.Connection.getInstance();
    var request = new rwt.remote.Request( connection.getUrl(), "GET", "application/javascript" );
    request.setSuccessHandler( this._handleSuccess, this );
    request.setErrorHandler( this._handleError, this );
    request.setData( "servicehandler=org.eclipse.rap.serverpush&cid=" + connection.getConnectionId() );
    return request;
  },

  _handleSuccess : function() {
    this._running = false;
    this._retryCount = 0;
    this._sendUIRequest();
  },

  _sendUIRequest : function() {
    rwt.remote.Connection.getInstance().sendImmediate( true );
  },

  _handleError : function( event ) {
    this._running = false;
    if( rwt.remote.Connection.getInstance()._isConnectionError( event.status ) ) {
      if( this._retryCount < 3 ) {
        var delay = 1000 * this._retryCount++;
        rwt.client.Timer.once( this.sendServerPushRequest, this, delay );
      } else {
        this._handleConnectionError();
      }
    } else {
      this._handleServerError( event );
    }
  },

  _handleConnectionError : function() {
    rwt.remote.Connection.getInstance().sendImmediate( true );
  },

  _handleServerError : function( event ) {
    var text = event.responseText;
    if( text && text.length > 0 ) {
      rwt.runtime.ErrorHandler.showErrorBox( "server error", true, text );
    } else {
      rwt.runtime.ErrorHandler.showErrorBox( "request failed" );
    }
  }

};
