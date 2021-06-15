/*******************************************************************************
 * Copyright (c) 2012, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

(function(){

var Connection = rwt.remote.Connection;

/**
 * @private
 * @class Instances of RemoteObject represent the server-side counterpart of a client object
 * and are used to write operations into the next protocol message.
 * @description The constructor is not public. Instances can
 * be obtained from {@link rap.getRemoteObject}.
 * @exports rwt.remote.RemoteObject as RemoteObject
 * @since 2.0
 * @param {}
 *
 */
rwt.remote.RemoteObject = function( id ) {
  this._ = {
    "id" : id,
    "listen" : {}
  };
};

rwt.remote.RemoteObject.prototype = {

  /**
   * @description Sets the specified property of the remote object to the given value.
   * Calling this method multiple times for the same property will overwrite the previous value,
   * the message will not become longer. This method does not cause the message to be sent
   * immediately. Instead it will be sent the next time a "notify" or "call" operation is
   * written to the message.
   * @param {string} property The name of the property.
   * @param {var} value The value of the property.
   */
  set : function( key, value ) {
    Connection.getInstance().getMessageWriter().appendSet( this._.id, key, value );
  },

  /**
   * @description Notifies the remote object that an event of the given type occurred.
   * Notifications can only be sent for types that the server is currently listening for
   * (see {@link rap.registerTypeHandler}, <b>handler.events</b>). If this is not the
   * case, no "notify" operation is written into the message and no request will be sent.
   * Otherwise the message will be sent to the server within a few milliseconds. One message
   * may contain several "notify" operations, if they are added consecutively.
   * @param {string} event The type of the event that occured.
   * @param {Object|null} [properties] This object may contain any number of additional
   * properties/fields associated with the event. It may also be null or omitted.
   */
  notify : function( event, properties ) {
    var suppressSend = arguments[ 2 ];
   // TODO [tb]: suppressSend (or something similar) should be public API
    var actualProps = properties ? properties : {};
    if( this.isListening( event ) ) {
      var connection = Connection.getInstance();
      connection.getMessageWriter().appendNotify( this._.id, event, actualProps );
      if( suppressSend !== true ) {
        if( typeof suppressSend === "number" ) {
          connection.sendDelayed( suppressSend );
        } else {
          connection.send();
        }
      }
    }
  },

  /**
   * @description Instructs the remote object to call the given method.
   * Calling this method will write a "call" operation into the message, which will to be sent to
   * the server within a few milliseconds. One message
   * may contain several "call" operations, if they are added consecutively.
   * @param {string} method The name of the method.
   * @param {Object|null} [properties] This object may contain any number of additional
   * properties/fields associated with the call. It may also be null or omitted.
   */
  call : function( method, properties ) {
    var actualProps = properties ? properties : {};
    Connection.getInstance().getMessageWriter().appendCall( this._.id, method, actualProps );
    Connection.getInstance().send();
  },

  isListening : function( type ) {
    return this._.listen[ type ] === true;
  }

};

}());
