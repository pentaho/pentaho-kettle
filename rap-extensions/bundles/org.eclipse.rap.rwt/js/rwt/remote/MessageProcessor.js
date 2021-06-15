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

(function() {

namespace( "rwt.remote" );

var paused = false;
var pendingMessage = null;

rwt.remote.MessageProcessor = {

  processMessage : function( messageObject, callback, startOffset ) {
    var offset = 0;
    if( typeof startOffset !== "undefined" ) {
      offset = startOffset;
    }
    if( offset === 0 ) {
      rap._.notify("receive", messageObject );
      this.processHead( messageObject.head );
    }
    var operations = messageObject.operations || [];
    while( offset < operations.length ) {
      this.processOperationArray( operations[ offset ] );
      offset++;
      if( paused ) {
        this._suspendMessageProcessing( messageObject, callback, offset );
        return;
      }
    }
    rap._.notify("process", messageObject );
    if( callback ) {
      callback();
    }
  },

  processHead : function( head ) {
    var connection = rwt.remote.Connection.getInstance();
    if( head.url ) {
      connection.setUrl( head.url );
    }
    if( head.cid ) {
      connection.setConnectionId( head.cid );
    }
    if( head.redirect ) {
      rwt.widgets.Display.getCurrent().setExitConfirmation( null );
      document.location = head.redirect;
    }
  },

  processOperationArray : function( operation ) {
    var action = operation[ 0 ];
    try {
      switch( action ) {
        case "create":
          this._processCreate( operation[ 1 ], operation[ 2 ], operation[ 3 ] );
        break;
        case "set":
          this._processSet( operation[ 1 ], operation[ 2 ] );
        break;
        case "listen":
          this._processListen( operation[ 1 ], operation[ 2 ] );
        break;
        case "call":
          this._processCall( operation[ 1 ], operation[ 2 ], operation[ 3 ] );
        break;
        case "destroy":
          this._processDestroy( operation[ 1 ] );
        break;
      }
    } catch( ex ) {
      this._processError( ex, operation );
    }
  },

  processOperation : function( operation ) {
    switch( operation.action ) {
      case "create":
        this._processCreate( operation.target, operation.type, operation.properties );
      break;
      case "set":
        this._processSet( operation.target, operation.properties );
      break;
      case "destroy":
        this._processDestroy( operation.target );
      break;
      case "call":
        this._processCall( operation.target, operation.method, operation.properties );
      break;
      case "listen":
        this._processListen( operation.target, operation.properties );
      break;
    }
  },

  pauseExecution : function() {
    paused = true;
  },

  isPaused : function() {
    return paused;
  },

  continueExecution : function() {
    paused = false;
    var resumingMessage = pendingMessage;
    pendingMessage = null;
    this.processMessage.apply( this, resumingMessage );
  },

  ////////////
  // Internals

  _processCreate : function( targetId, type, properties ) {
    var handler = rwt.remote.HandlerRegistry.getHandler( type );
    if( handler.service === true ) {
      throw new Error( "Objects of type " + type + " can not be created" );
    }
    var targetObject = handler.factory( properties );
    this._addTarget( targetObject, targetId, handler );
    this._processSetImpl( targetObject, handler, properties );
  },

  _processDestroy : function( targetId ) {
    var objectEntry = rwt.remote.ObjectRegistry.getEntry( targetId );
    if( objectEntry ) {
      var handler = objectEntry.handler;
      var targetObject = objectEntry.object;
      var children =   handler.getDestroyableChildren
                     ? handler.getDestroyableChildren( targetObject )
                     : null;
      if( handler.destructor instanceof Function ) {
        handler.destructor( targetObject );
      } else if( typeof handler.destructor === "string" ) {
        var destructorName = handler.destructor;
        targetObject[ destructorName ]();
      }
      rwt.remote.ObjectRegistry.remove( targetId );
      rwt.remote.RemoteObjectFactory.remove( targetId );
      for( var i = 0; children != null && i < children.length; i++ ) {
        if( children[ i ] ) {
          this._processDestroy( rwt.remote.ObjectRegistry.getId( children[ i ] ) );
        }
      }
    }
  },

  _processSet : function( targetId, properties ) {
    var objectEntry = rwt.remote.ObjectRegistry.getEntry( targetId );
    this._processSetImpl( objectEntry.object, objectEntry.handler, properties );
  },

  _processSetImpl : function( targetObject, handler, properties ) {
    if( handler.isGeneric && !rwt.util.Objects.isEmpty( properties ) ) {
      targetObject.set( properties, { "nosync" : true } );
    } else if( properties && handler.properties instanceof Array ) {
      for( var i = 0; i < handler.properties.length; i++ ) {
        var property = handler.properties [ i ];
        var value = properties[ property ];
        if( value !== undefined ) {
          if( handler.propertyHandler && handler.propertyHandler[ property ] ) {
            handler.propertyHandler[ property ].call( window, targetObject, value );
          } else {
            var setterName = this._getSetterName( property );
            targetObject[ setterName ]( value );
          }
        }
      }
    }
  },

  _processCall : function( targetId, method, properties ) {
    var objectEntry = rwt.remote.ObjectRegistry.getEntry( targetId );
    var handler = objectEntry.handler;
    var targetObject = objectEntry.object;
    if( handler.isGeneric ) {
      targetObject[ method ]( properties );
    } else if( handler.methods instanceof Array && handler.methods.indexOf( method ) !== -1 ) {
      if( handler.methodHandler && handler.methodHandler[ method ] ) {
        handler.methodHandler[ method ]( targetObject, properties );
      } else {
        targetObject[ method ]( properties );
      }
    }
  },

  _processListen : function( targetId, properties ) {
    var objectEntry = rwt.remote.ObjectRegistry.getEntry( targetId );
    var handler = objectEntry.handler;
    var targetObject = objectEntry.object;
    if( handler.events instanceof Array || handler.isGeneric ) {
      var remoteObject = rwt.remote.RemoteObjectFactory.getRemoteObject( targetObject );
      var events = handler.isGeneric ? rwt.util.Objects.getKeys( properties ) : handler.events;
      for( var i = 0; i < events.length; i++ ) {
        var type = events[ i ];
        if( properties[ type ] === true ) {
          remoteObject._.listen[ type ] = true;
        } if( properties[ type ] === false ) {
          remoteObject._.listen[ type ] = false;
        }
      }
    }
    if( handler.listeners instanceof Array ) {
      for( var i = 0; i < handler.listeners.length; i++ ) {
        var type = handler.listeners[ i ];
        if( properties[ type ] === true ) {
          this._addListener( handler, targetObject, type );
        } if( properties[ type ] === false ) {
          this._removeListener( handler, targetObject, type );
        }
      }
    }
  },

  ////////////
  // Internals

  _processError : function( error, operation ) {
    var errorstr;
    if( error ) {
      errorstr = error.message ? error.message : error.toString();
    } else {
      errorstr = "No Error given!";
    }
    var msg = "Operation \"" + operation[ 0 ] + "\"";
    msg += " on target \"" +  operation[ 1 ] + "\"";
    var objectEntry = rwt.remote.ObjectRegistry.getEntry( operation[ 1 ] );
    var target = objectEntry ? objectEntry.object : null;
    msg += " of type \"" +  ( target && target.classname ? target.classname : target ) + "\"";
    msg += " failed:";
    msg += "\n" + errorstr +"\n";
    msg += "Properties: \n" + this._getPropertiesString( operation );
    throw new Error( msg );
  },

  _getPropertiesString : function( operation ) {
    var result = "";
    var properties;
    switch( operation[ 0 ] ) {
      case "set":
      case "listen":
        properties = operation[ 2 ];
      break;
      case "create":
      case "call":
        properties = operation[ 3 ];
      break;
      default:
        properties = {};
      break;
    }
    for( var key in properties ) {
      result += key + " = " + properties[ key ] + "\n";
    }
    return result;
  },

  _addTarget : function( target, targetId, handler ) {
    if( target instanceof rwt.widgets.base.Widget ) {
      // TODO [tb] : remove WidgetManager and then this if
      var widgetManager = rwt.remote.WidgetManager.getInstance();
      widgetManager.add( target, targetId, false, handler ); // uses ObjectManager internally
    } else {
      rwt.remote.ObjectRegistry.add( targetId, target, handler );
    }
  },

  _addListener : function( handler, targetObject, eventType ) {
    if( handler.listenerHandler &&  handler.listenerHandler[ eventType ] ) {
      handler.listenerHandler[ eventType ]( targetObject, true );
    } else {
      var setterName = this._getListenerSetterName( eventType );
      targetObject[ setterName ]( true );
    }
  },

  _removeListener : function( handler, targetObject, eventType ) {
    if( handler.listenerHandler &&  handler.listenerHandler[ eventType ] ) {
      handler.listenerHandler[ eventType ]( targetObject, false );
    } else {
      var setterName = this._getListenerSetterName( eventType );
      targetObject[ setterName ]( false );
    }
  },

  _getSetterName : function( property ) {
    return "set" + rwt.util.Strings.toFirstUp( property );
  },

  _getListenerSetterName : function( eventType ) {
    return "setHas" + rwt.util.Strings.toFirstUp( eventType ) + "Listener";
  },

  _suspendMessageProcessing : function( message, callback, offset ) {
    if( pendingMessage != null ) {
      throw new Error( "A message is already suspended" );
    }
    pendingMessage = [ message, callback, offset ];
  }


};

}() );
