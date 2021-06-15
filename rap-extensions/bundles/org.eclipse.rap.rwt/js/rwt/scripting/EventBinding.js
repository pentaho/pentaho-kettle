/*******************************************************************************
 * Copyright (c) 2012, 2018 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

(function(){

rwt.define( "rwt.scripting", {} );

var WRAPPER_REGISTRY = "rwt.scripting.EventBinding.WrapperRegistry";

// TODO : better name?
// TODO [rst] Define directly using rwt.define, remove surrounding function scope
rwt.scripting.EventBinding = {

  addListener : function( widget, eventType, targetFunction ) {
    this._checkEventType( eventType );
    var wrapperList = this._getWrapperList( widget, eventType, targetFunction );
    var nativeType = this._getNativeEventType( widget, eventType );
    var nativeSource = this._getNativeEventSource( widget, eventType );
    var wrappedListener = this._wrapListener( widget, eventType, targetFunction );
    nativeSource.addEventListener( nativeType, wrappedListener, window );
    wrapperList.push( wrappedListener );
  },

  removeListener : function( widget, eventType, targetFunction ) {
    var wrapperList = this._getWrapperList( widget, eventType, targetFunction );
    var nativeType = this._getNativeEventType( widget, eventType );
    var nativeSource = this._getNativeEventSource( widget, eventType );
    var wrappedListener = wrapperList.pop();
    if( wrappedListener ) {
      nativeSource.removeEventListener( nativeType, wrappedListener, window );
    }
  },

  _wrapListener : function( widget, eventType, targetFunction ) {
    return function( nativeEvent ) {
      try {
        var eventProxy = new rwt.scripting.EventProxy( SWT[ eventType ], widget, nativeEvent );
        var wrappedEventProxy = rwt.scripting.EventProxy.wrapAsProto( eventProxy );
        targetFunction( wrappedEventProxy );
        rwt.scripting.EventProxy.postProcessEvent( eventProxy, wrappedEventProxy, nativeEvent );
        rwt.scripting.EventProxy.disposeEventProxy( eventProxy );
      } catch( ex ) {
        var msg = "Error in scripting event type ";
        throw new Error( msg + eventType + ": " + ( ex.message ? ex.message : ex ) );
      }
    };
  },

  _getWrapperList : function( widget, eventType, targetFunction ) {
    var wrapperRegistry = widget.getUserData( WRAPPER_REGISTRY );
    if( !wrapperRegistry ) {
      wrapperRegistry = {};
      widget.setUserData( WRAPPER_REGISTRY, wrapperRegistry );
    }
    var key = eventType + ":" + rwt.qx.Object.toHashCode( targetFunction );
    if( wrapperRegistry[ key ] == null ) {
      wrapperRegistry[ key ] = [];
    }
    return wrapperRegistry[ key ];
  },

  _getNativeEventSource : function( source, eventType ) {
    var result = source;
    switch( source.classname + ":" + eventType ) {
      case "rwt.widgets.List:Selection":
        result = source.getManager();
      break;
      case "rwt.widgets.Spinner:Modify":
        result = source._textfield;
      break;
      case "rwt.widgets.Combo:Modify":
      case "rwt.widgets.Combo:Verify":
        result = source._field;
      break;
    }
    return result;
  },

  _getNativeEventType : function( source, eventType ) {
    var map = this._eventTypeMapping;
    var result = "";
    if( map[ source.classname ] && map[ source.classname ][ eventType ] ) {
      result = map[ source.classname ][ eventType ];
    } else {
      result = map[ "*" ][ eventType ];
    }
    return result;
  },

  _checkEventType : function( type ) {
    if( typeof SWT[ type ] === "string" ) {
      return;
    }
    throw new Error( "Unkown event type " + type );
  },

  _eventTypeMapping : {
    "*" : {
      /**
       * @event
       * @description Sent when a key is pressed
       * @name Control#KeyDown
       * @param {Event} event
       * @see SWT.KeyDown
       */
      "KeyDown" : "keypress",
      /**
       * @event
       * @description Sent when a key is released
       * @name Control#KeyUp
       * @param {Event} event
       * @see SWT.KeyUp
       */
      "KeyUp" : "keyup",
      /**
       * @event
       * @description Sent when a mouse button is pressed
       * @name Control#MouseDown
       * @param {Event} event
       * @see SWT.MouseDown
       */
      "MouseDown" : "mousedown",
      /**
       * @event
       * @description Sent when a mouse button is released
       * @name Control#MouseUp
       * @param {Event} event
       * @see SWT.MouseUp
       */
      "MouseUp" : "mouseup",
      /**
       * @event
       * @description Sent when the mouse pointer is moved
       * @name Control#MouseMove
       * @param {Event} event
       * @see SWT.MouseMove
       */
      "MouseMove" : "mousemove",
      /**
       * @event
       * @description Sent when the mouse pointer enters the widget
       * @name Control#MouseEnter
       * @param {Event} event
       * @see SWT.MouseEnter
       */
      "MouseEnter" : "mouseover",
      /**
       * @event
       * @description Sent when the mouse pointer exits the widget
       * @name Control#MouseExit
       * @param {Event} event
       * @see SWT.MouseExit
       */
      "MouseExit" : "mouseout",
      /**
       * @event
       * @description Sent when the mouse wheel is moved
       * @name Control#MouseWheel
       * @param {Event} event
       * @see SWT.MouseWheel
       */
      "MouseWheel" : "mousewheel",
      /**
       * @event
       * @description Sent when a mouse button is clicked twice
       * @name Control#MouseDoubleClick
       * @param {Event} event
       * @see SWT.MouseDoubleClick
       */
      "MouseDoubleClick" : "dblclick",
      /**
       * @event
       * @description Sent when the widget is painted
       * @name Control#Paint
       * @param {Event} event
       * @see SWT.Paint
       */
      "Paint" : "paint",
      /**
       * @event
       * @description Sent when the widget gains focus
       * @name Control#FocusIn
       * @param {Event} event
       * @see SWT.FocusIn
       */
      "FocusIn" : "focus",
      /**
       * @event
       * @description Sent when the widget looses focus
       * @name Control#FocusOut
       * @param {Event} event
       * @see SWT.FocusOut
       */
      "FocusOut" : "blur",
      /**
       * @event
       * @description Sent when the widget appears
       * @name Control#Show
       * @param {Event} event
       * @see SWT.Show
       */
      "Show" : "appear",
      /**
       * @event
       * @description Sent when the widget is hidden
       * @name Control#Hide
       * @param {Event} event
       * @see SWT.Hide
       */
      "Hide" : "disappear",
      /**
       * @event
       * @description Sent when the widget is disposed. The event is fired before widget is
       * detached from the widget hierarchy and removed from DOM
       * @name Control#Dispose
       * @param {Event} event
       * @see SWT.Dispose
       */
      "Dispose" : "beforeDispose"
    },
    "rwt.widgets.Scale" : {
      /**
       * @event
       * @description Sent when the widget selection is changed
       * @name Scale#Selection
       * @param {Event} event
       * @see SWT.Selection
       */
      "Selection" : "selectionChanged"
    },
    "rwt.widgets.Slider" : {
      /**
       * @event
       * @description Sent when the widget selection is changed
       * @name Slider#Selection
       * @param {Event} event
       * @see SWT.Selection
       */
      "Selection" : "selectionChanged"
    },
    "rwt.widgets.Composite" : {
      /**
       * @event
       * @description Sent when the widget selection changes size
       * @name Composite#Resize
       * @param {Event} event
       * @see SWT.Resize
       */
      "Resize" : "clientAreaChanged"
    },
    "rwt.widgets.Button" : {
      /**
       * @event
       * @description Sent when the widget is selected
       * @name Button#Selection
       * @param {Event} event
       * @see SWT.Selection
       */
      "Selection" : "execute"
    },
    "rwt.widgets.Spinner" : {
      /**
       * @event
       * @description Sent when the widget selection is changed
       * @name Spinner#Selection
       * @param {Event} event
       * @see SWT.Selection
       */
      "Selection" : "change",
      /**
       * @event
       * @description Sent when the widget text is changed
       * @name Spinner#Modify
       * @param {Event} event
       * @see SWT.Modify
       */
      "Modify" : "changeValue"
    },
    "rwt.widgets.Text" : {
      /**
       * @event
       * @description Sent before the widget text is changed
       * @name Text#Verify
       * @param {Event} event
       * @see SWT.Verify
       */
      "Verify" : "input", // TODO [tb] : does currently not react on programatic changes
      /**
       * @event
       * @description Sent when the widget text is changed
       * @name Text#Modify
       * @param {Event} event
       * @see SWT.Modify
       */
      "Modify" : "changeValue"
    },
    "rwt.widgets.Combo" : {
      /**
       * @event
       * @description Sent before the widget text is changed
       * @name Combo#Verify
       * @param {Event} event
       * @see SWT.Verify
       */
      "Verify" : "input", // TODO [tb] : does currently not react on programatic changes
      /**
       * @event
       * @description Sent when the widget text is changed
       * @name Combo#Modify
       * @param {Event} event
       * @see SWT.Modify
       */
      "Modify" : "changeValue"
    }
  }

};

}());
