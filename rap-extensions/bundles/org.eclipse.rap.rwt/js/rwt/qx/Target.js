/*******************************************************************************
 * Copyright (c) 2004, 2014 1&1 Internet AG, Germany, http://www.1und1.de,
 *                          EclipseSource, and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Remote Application Platform
 ******************************************************************************/


/**
 * This is the main constructor for all objects that need to be connected to rwt.event.Event objects.
 *
 * In objects created with this constructor, you find functions to addEventListener or
 * removeEventListener to or from the created object. Each event to connect to has a type in
 * form of an identification string. This type could be the name of a regular dom event like "click"
 * or something self-defined like "ready".
 */
rwt.qx.Class.define( "rwt.qx.Target", {

  extend : rwt.qx.Object,

  construct : function() {
    this.base( arguments );
  },

  members : {

    /**
     * Add event listener to an object.
     *
     * @param type {String} name of the event type
     * @param func {Function} event callback function
     * @param obj {Object ? window} reference to the 'this' variable inside the callback
     */
    addEventListener : function( type, func, obj ) {
      if( this.getDisposed() ) {
        return;
      }

      // If this is the first event of given type, we need to create a subobject
      // that contains all the actions that will be assigned to this type
      if( this.__listeners === undefined ) {
        this.__listeners = {};
      }

      if( this.__listeners[ type ] === undefined ) {
        this.__listeners[ type ] = {};
      }

      // Create a special key string to allow identification of each bound action
      var key = "event" + rwt.qx.Object.toHashCode( func ) +
        ( obj ? "$" + rwt.qx.Object.toHashCode( obj ) : "" );

      // Finally set up the listeners object
      this.__listeners[ type ][ key ] = {
        handler : func,
        object  : obj
      };
    },

    /**
     * Remove event listener from object
     *
     * @param type {String} name of the event type
     * @param func {Function} event callback function
     * @param obj {Object ? window} reference to the 'this' variable inside the callback
     */
    removeEventListener : function( type, func, obj ) {
      if( this.getDisposed() ) {
        return;
      }

      var listeners = this.__listeners;

      if( !listeners || listeners[ type ] === undefined ) {
        return;
      }

      if( typeof func !== "function" ) {
        throw new Error( "rwt.qx.Target: removeEventListener(" + type + "): '" + func
                         + "' is not a function!" );
      }

      // Create a special key string to allow identification of each bound action
      var key = "event" + rwt.qx.Object.toHashCode( func ) +
        ( obj ? "$" + rwt.qx.Object.toHashCode( obj ) : "" );

      // Delete object entry for this action
      delete this.__listeners[ type ][ key ];
    },

    // ------------------------------------------------------------------------
    // EVENT CONNECTION UTILITIES
    // ------------------------------------------------------------------------

    /**
     * Check if there are one or more listeners for an event type.
     *
     * @param type {String} name of the event type
     */
    hasEventListeners : function( type ) {
      return this.__listeners &&
        this.__listeners[ type ] !== undefined &&
        !rwt.util.Objects.isEmpty( this.__listeners[ type ] );
    },


    /**
     * Checks if the event is registered. If so it creates an event object and dispatches it.
     *
     * @param type {String} name of the event type
     */
    createDispatchEvent : function( type ) {
      if( this.hasEventListeners( type ) ) {
        this.dispatchEvent( new rwt.event.Event( type ), true );
      }
    },

    /**
     * Checks if the event is registered. If so it creates an event object and dispatches it.
     *
     * @param type {String} name of the event type
     * @param data {Object} user defined data attached to the event object
     */
    createDispatchDataEvent : function( type, data ) {
      if( this.hasEventListeners( type ) ) {
        this.dispatchEvent( new rwt.event.DataEvent( type, data ), true );
      }
    },

    /**
     * Checks if the event is registered. If so it creates an event object and dispatches it.
     *
     * @param type {String} name of the event type
     * @param value {Object} property value attached to the event object
     * @param old {Object} old property value attached to the event object
     */
    createDispatchChangeEvent : function( type, value, old ) {
      if( this.hasEventListeners( type ) ) {
        this.dispatchEvent( new rwt.event.ChangeEvent( type, value, old ), true );
      }
    },

    // ------------------------------------------------------------------------
    // EVENT DISPATCH
    // ------------------------------------------------------------------------

    /**
     * Dispatch an event
     *
     * @param evt {rwt.event.Event} event to dispatch
     * @param dispose {Boolean} whether the event object should be disposed after all event handlers run.
     * @return {Boolean} whether the event default was prevented or not. Returns true, when the event was NOT prevented.
     */
    dispatchEvent : function( evt, dispose ) {
      // Ignore event if eventTarget is disposed
      if( this.getDisposed() ) {
        return;
      }
      if( evt.getTarget() == null ) {
        evt.setTarget(this);
      }
      if( evt.getCurrentTarget() == null ) {
        evt.setCurrentTarget( this );
      }
      // Dispatch Event
      this._dispatchEvent( evt, dispose );
      // Read default prevented
      var defaultPrevented = evt.getDefaultPrevented();
      // enable dispose for event?
      if( dispose ) {
        evt.dispose();
      }
      return !defaultPrevented;
    },

    dispatchSimpleEvent : function( type, data, bubbles ) {
      var listeners = this.__listeners;
      var propagate = bubbles === true;
      var result = true;
      if( listeners ) {
        var typeListeners = listeners[ type ];
        if( typeListeners ) {
          var func;
          var obj;
          for( var hashCode in typeListeners ) {
            // Shortcuts for handler and object
            func = typeListeners[ hashCode ].handler;
            obj = typeListeners[ hashCode ].object || this;
            result = func.call( obj, data ) && result !== false;
            if( result === false ) {
              propagate = false;
            }
          }
        }
      }
      if( propagate && typeof( this.getParent ) === "function" ) {
        var parent = this.getParent();
        if( parent && !parent.getDisposed() && parent.getEnabled() ) {
          parent.dispatchSimpleEvent( type, data, bubbles );
        }
      }
      return result !== false;
    },

    /**
     * Internal event dispatch method
     *
     * @param evt {rwt.event.Event} event to dispatch
     */
    _dispatchEvent : function( evt ) {
      var listeners = this.__listeners;
      if( listeners && this._allowDispatch( evt ) ) {
        // Setup current target
        evt.setCurrentTarget( this );
        // Shortcut for listener data
        var typeListeners = listeners[ evt.getType() ];
        if( typeListeners ) {
          var func, obj;
          // Handle all events for the specified type
          for( var vHashCode in typeListeners ) {
            // Shortcuts for handler and object
            func = typeListeners[ vHashCode ].handler;
            obj = typeListeners[ vHashCode ].object || this;
            // Call object function
            func.call( obj, evt );
          }
        }
      }

      // Bubble event to parents
      // TODO: Move this to Parent or Widget?
      if( evt.getBubbles() && !evt.getPropagationStopped() && typeof( this.getParent ) == "function" )
      {
        var parent = this.getParent();
        if( parent && !parent.getDisposed() ) {
          parent._dispatchEvent( evt );
        }
      }
    },

    _allowDispatch : function( event ) {
      var result = true;
      if( this.getEnabled && event instanceof rwt.event.DomEvent ) {
        result = this.getEnabled();
      }
      return result;
    }

  },

  destruct : function() {
    this._disposeObjectDeep( "__listeners", 2 );
  }

} );
