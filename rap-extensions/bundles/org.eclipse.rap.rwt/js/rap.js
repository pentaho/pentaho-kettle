/*******************************************************************************
 * Copyright (c) 2012, 2017 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

(function(){

/**
 * Contains methods required for custom widget development and RWT Scripting.
 *
 * @public
 * @since 2.0
 * @namespace Holds all global public methods of the RAP WebClient.
 */
rap = {

  /**
   * @description Registers a RAP protocol type handler for a specific type of client objects.
   * The handler is used by the protocol message processor to process operations that target
   * any client object of this type. Example:
   *
   * @example
   * rap.registerTypeHandler( "project.MyWidget", {
   *   factory : function( properties ) {
   *     return new MyWidget();
   *   },
   *   properties : [ "propA", "propB" ]
   * } );
   *
   * @param {string} type
   *
   * @param {Object} handler The handler object.
   *
   * @param {Function} handler.factory Called for create operations.
   * Is given a properties object as the first argument, which may contain any number for
   * properties/fields set by the server. Has to return a "client object" representing an instance
   * of the type of this handler. <em>Required for all type handler</em>.
   *
   * @param {string} handler.destructor Name of the method that is called for destroy operations.
   * If the string is given, the client object <em>has</em> to implement a method with the given
   * name. <em>Optional</em>
   *
   * @param {string[]} handler.properties List of properties supported by this type.
   * The order in the list controls the order in which the properties are applied by the message
   * processor. The client object <em>has</em> to implement a setter for each supported property.
   * For example, if the property is "bounds", <code>setBounds</code> will be called on the client
   * object. Properties given by the server that are not in this list will be ignored. (One
   * exception is the factory, which gets an object with <i>all</i> properties set by the server
   * at that time.)  If the property changes on the client, {@link RemoteObject#set} can be
   * used to synchronize the value with the server.
   * <em>Optional.</em>
   *
   * @param {string[]} handler.methods List of methods supported by this type.
   * The order in the list is meaningless, "call" operations are processed in the order in which
   * they are given by the server. The client object has to implement a method of the same name.
   * One argument will be given, which is a properties object with any number of properties/fields.
   * A "call" operation given by the server for a method that is not in this list will be ignored.
   * <em>Optional.</em>
   *
   * @param {string[]} handler.events List of event types supported by this type.
   * The server may instruct the client object with "listen" operations to start or stop sending
   * notifications when events of he given event type occur. Notifications may and can only be
   * sent for types that are given in this list and are listened to by the server. See also
   * {@link RemoteObject#notify}.
   * <em>Optional.</em>
   */
  registerTypeHandler : function( type, handler ) {
    handler.isPublic = true;
    rwt.remote.HandlerRegistry.add( type, handler );
  },

  /**
   * Returns the client object associated with the given id as returned
   * by <code>org.eclipse.rap.rwt.widget.WidgetUtil.getId</code> and
   * <code>org.eclipse.rap.rwt.remote.RemoteObject.getId</code>.
   * If there is no object registered for the given id, <code>null</code> is returned.
   * For custom widgets, that is the result of the type handler factory.
   * For SWT/RWT widgets (any Control or Item) a wrapper (instance of {@link Widget}) is returned that approximates
   * a subset of the API of its Java analog. Getting objects of other types is unsupported.
   * @see Widget
   * @see rap.registerTypeHandler
   * @param {string} id The protocol id for a client object.
   * @returns {Object} The client object associated with the id.
   */
  getObject : function( id ) {
    var entry = rwt.remote.ObjectRegistry.getEntry( id );
    var result;
    if( entry && entry.handler.isPublic ) {
      result = entry.object;
    } else if( entry ) {
      result = rwt.scripting.WidgetProxyFactory.getWidgetProxy( entry.object );
    }
    return result;
  },

  /**
   * @description Returns an instance of {@link RemoteObject} for the given client object.
   * A client object is any object that was created by an type handler factory method.
   * Multiple calls for the same object will return the same RemoteObject
   * instance.
   * @see rap.registerTypeHandler
   * @param {Object} object The client object.
   * @returns {RemoteObject}
   */
  getRemoteObject : function( object ) {
    return rwt.remote.Connection.getInstance().getRemoteObject( object );
  },

  /**
   * @description Returns an instance of Client object used for basic client detection.
   * @returns {Client}
   * @since 3.2
   */
  getClient : function() {
    return rwt.client.Client;
  },

  /**
   * @description Register the function as a listener of the given type. Registering unknown
   * types throws an Error.
   * @param {string} type The type of the event (e.g. "send").
   * @param {Function} listener The callback function. It is executed in global context.
   */
   on : function( type, handler ) {
     if( this._.events[ type ] ) {
       if( this._.events[ type ].indexOf( handler ) === -1 ) {
         this._.events[ type ].push( handler );
       }
     } else {
       throw new Error( "Unknown type " + type );
     }
   },

  /**
   * @description De-register the function as a listener of the given type.
   * @param {string} type The type of the event
   * @param {Function} listener The callback function
   */
   off : function( type, handler ) {
     if( this._.events[ type ] ) {
       var index = this._.events[ type ].indexOf( handler );
       if( index !== -1 ) {
         rwt.util.Arrays.removeAt( this._.events[ type ], index );
       }
     }
   },

   _ : {
    events : {
      /**
       * @event
       * @description Sent right before a message is send to the server.
       * @name rap#send
       */
      "send" : [],
      /**
       * @event
       * @description Sent after a message has been processed.
       * @name rap#render
       */
      "render" : [],
      "receive" : [],
      "process" : []
    },
    notify : function( type ) {
      var listener = this.events[ type ].concat();
      for( var i = 0; i < listener.length; i++ ) {
        listener[ i ].apply( window, Array.prototype.slice.call( arguments, 1 ) );
      }
    }
  }

};

}());
