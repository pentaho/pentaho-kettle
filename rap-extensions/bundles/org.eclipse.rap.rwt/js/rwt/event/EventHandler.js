/*******************************************************************************
 * Copyright (c) 2004, 2020 1&1 Internet AG, Germany, http://www.1und1.de,
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

namespace( "rwt.event" );


rwt.event.EventHandler = {

  _filter : {},
  _allowContextMenu : rwt.util.Functions.returnFalse,
  _captureWidget : null,
  _focusRoot : null,
  // state storage:
  _focused : false,
  _blockKeyEvents : false,

  ///////////////////
  // Public functions

  init : function() {
    var functionUtil = rwt.util.Functions;
    this.__onmouseevent = functionUtil.bind( this._onmouseevent, this );
    this.__ondragevent = functionUtil.bind( this._ondragevent, this );
    this.__onselectevent = functionUtil.bind( this._onselectevent, this );
    this.__onwindowblur = functionUtil.bind( this._onwindowblur, this );
    this.__onwindowfocus = functionUtil.bind( this._onwindowfocus, this );
    this.__onwindowresize = functionUtil.bind( this._onwindowresize, this );
    this.__onKeyEvent = rwt.util.Functions.bind( this._onKeyEvent, this );
    rwt.event.EventHandlerUtil.applyBrowserFixes();
  },

  cleanUp : function() {
    delete this.__onmouseevent;
    delete this.__ondragevent;
    delete this.__onselectevent;
    delete this.__onwindowblur;
    delete this.__onwindowfocus;
    delete this.__onwindowresize;
    delete this.__onKeyEvent;
    delete this._lastMouseDownDomTarget;
    delete this._lastMouseDownDispatchTarget;
    rwt.event.EventHandlerUtil.cleanUp();
  },

  attachEvents : function() {
    this.attachEventTypes( this._mouseEventTypes, this.__onmouseevent );
    this.attachEventTypes( this._dragEventTypes, this.__ondragevent );
    this.attachEventTypes( this._keyEventTypes, this.__onKeyEvent );
    window.addEventListener( "blur", this.__onwindowblur, false );
    window.addEventListener( "focus", this.__onwindowfocus, false  );
    window.addEventListener( "resize", this.__onwindowresize, false  );
    document.body.onselect = this.__onselectevent;
    document.onselectstart = this.__onselectevent;
    document.onselectionchange = this.__onselectevent;
  },

  detachEvents : function() {
    this.detachEventTypes( this._mouseEventTypes, this.__onmouseevent);
    this.detachEventTypes( this._dragEventTypes, this.__ondragevent);
    this.detachEventTypes( this._keyEventTypes, this.__onKeyEvent );
    window.removeEventListener( "blur", this.__onwindowblur, false );
    window.removeEventListener( "focus", this.__onwindowfocus, false );
    window.removeEventListener( "resize", this.__onwindowresize, false );
    document.body.onselect = null;
    document.onselectstart = null;
    document.onselectionchange = null;
  },

  setCaptureWidget : function( widget ) {
    if( this._captureWidget !== widget ) {
      if( this._captureWidget !== null ) {
        this._captureWidget.setCapture( false );
      }
      this._captureWidget = widget;
      if( widget != null ) {
        widget.setCapture( true );
      }
    }
  },

  getCaptureWidget : function() {
    return this._captureWidget && this._captureWidget.getEnabled() ? this._captureWidget : null;
  },

  setFocusRoot : function( widget ) {
    if( widget !== this._focusRoot ) {
      if( this._focusRoot !== null ) {
        this._focusRoot.setFocusedChild( null );
      }
      this._focusRoot = widget;
      if( widget !== null && widget.getFocusedChild() === null ) {
        widget.setFocusedChild( widget );
      }
    }
  },

  getFocusRoot : function() {
    return this._focusRoot;
  },

  /**
   * Sets a callback-function to decide if the native context-
   * menu is displayed. It will be called on DOM-events of the type
   * "contextmenu". The target-Widget of the event will be given as
   * the first argument, the dom-target as the second.
   * It must return a boolean. Null is not allowed.
   *
   */
  setAllowContextMenu : function( func ) {
    this._allowContextMenu = func;
  },

  setBlockKeyEvents : function( value ) {
    this._blockKeyEvents = value;
  },

  getBlockKeyEvents : function() {
    return this._blockKeyEvents;
  },

  getMenuManager : function() {
    return rwt.widgets.util.MenuManager.getInstance();
  },

  setMouseEventFilter : function( filter, context ) {
    if( filter ) {
      this._filter[ "mouseevent" ] = [ filter, context ];
    } else {
      delete this._filter[ "mouseevent" ];
    }
  },

  getMouseEventFilter : function() {
    return this._filter[ "mouseevent" ];
  },

  setMouseDomEventFilter : function( filter, context ) {
    this._filter[ "dommouseevent" ] = [ filter, context ];
  },

  setKeyEventFilter : function( filter, context ) {
    // TODO [tb] : Unify behavior and API for EventFilter, only use event
    // create API for order of filter
    this._filter[ "keyevent" ] = [ filter, context ];
  },

  setKeyDomEventFilter : function( filter, context ) {
    this._filter[ "domkeyevent" ] = [ filter, context ];
  },

  //////////////
  // KEY EVENTS:

  _onKeyEvent : function( event ) {
    try {
      var EventHandlerUtil = rwt.event.EventHandlerUtil;
      var keyCode = EventHandlerUtil.getKeyCode( event );
      var charCode = EventHandlerUtil.getCharCode( event );
      if( this._blockKeyEvents ) {
        if( EventHandlerUtil.shouldBlock( event.type, keyCode, charCode, event ) ) {
          EventHandlerUtil.stopDomEvent( event );
        }
        return;
      }
      if( typeof this._filter[ "domkeyevent" ] !== "undefined" ) {
        var context = this._filter[ "domkeyevent" ][ 1 ];
        var func = this._filter[ "domkeyevent" ][ 0 ];
        func.call( context, event.type, keyCode, charCode, event );
      }
      var pseudoTypes = EventHandlerUtil.getEventPseudoTypes( event, keyCode, charCode );
      var keyUpCodes =  EventHandlerUtil.mustRestoreKeyup( keyCode, pseudoTypes );
      for( var i = 0; i < keyUpCodes.length; i++ ) {
        this._onkeyevent_post( event, "keyup", keyUpCodes[ i ], 0 );
        EventHandlerUtil.clearStuckKey( keyUpCodes[ i ] );
      }
      for( var i = 0; i < pseudoTypes.length; i++ ) {
        this._onkeyevent_post( event, pseudoTypes[ i ], keyCode, charCode );
      }
      if( EventHandlerUtil.mustRestoreKeypress( event, pseudoTypes ) ) {
        this._onkeyevent_post( event, "keypress", keyCode, charCode );
      }
      EventHandlerUtil.saveData( event, keyCode, charCode );
    } catch( ex ) {
      rwt.runtime.ErrorHandler.processJavaScriptError( ex );
    }
  },

  _onkeyevent_post : function( vDomEvent, vType, vKeyCode, vCharCode ) {
    var process = true;
    if( typeof this._filter[ "keyevent" ] !== "undefined" ) {
      var context = this._filter[ "keyevent" ][ 1 ];
      var func = this._filter[ "keyevent" ][ 0 ];
      process = func.call( context, vType, vKeyCode, vCharCode, vDomEvent );
    }
    if( process ) {
      this._processKeyEvent( vDomEvent, vType, vKeyCode, vCharCode );
    }
  },

  _processKeyEvent : function( vDomEvent, vType, vKeyCode, vCharCode ) {
    var EventHandlerUtil = rwt.event.EventHandlerUtil;
    var keyIdentifier;
    if( !isNaN( vKeyCode ) && vKeyCode !== 0 ) {
      keyIdentifier = EventHandlerUtil.keyCodeToIdentifier( vKeyCode );
    } else {
      keyIdentifier = EventHandlerUtil.charCodeToIdentifier( vCharCode );
    }
    var vDomTarget = EventHandlerUtil.getDomTarget( vDomEvent );
    var vTarget = this._getKeyEventTarget();
    if( this.getCaptureWidget() ) {
      EventHandlerUtil.stopDomEvent( vDomEvent );
    }
    var vKeyEventObject = new rwt.event.KeyEvent( vType,
                                                      vDomEvent,
                                                      vDomTarget,
                                                      vTarget,
                                                      null,
                                                      vKeyCode,
                                                      vCharCode,
                                                      keyIdentifier );
    if( vTarget != null && vTarget.getEnabled() ) {
      switch( keyIdentifier ) {
        case "Escape":
        case "Tab":
          this.getMenuManager().update(vTarget, vType);
        break;
      }
      if( vDomEvent.ctrlKey && keyIdentifier == "A" ) {
        switch( vDomTarget.tagName.toLowerCase() ) {
          case "input":
          case "textarea":
          case "iframe":
            // selection allowed
          break;
          default:
           EventHandlerUtil.stopDomEvent(vDomEvent);
          break;
        }
      }
      vTarget.dispatchEvent( vKeyEventObject );
      if( rwt.qx.Class.isDefined("rwt.event.DragAndDropHandler") ) {
        rwt.event.DragAndDropHandler.getInstance().handleKeyEvent( vKeyEventObject );
      }
      rwt.widgets.util.ToolTipManager.getInstance().handleKeyEvent( vKeyEventObject );
    }
    vKeyEventObject.dispose();
  },

  ///////////////
  // MOUSE EVENTS

  _onmouseevent : function( event ) {
    try {
      var process = true;
      if( typeof this._filter[ "dommouseevent" ] !== "undefined" ) {
        var context = this._filter[ "dommouseevent" ][ 1 ];
        process = this._filter[ "dommouseevent" ][ 0 ].call( context, event );
      }
      if( process ) {
        this._processMouseEvent( event );
      }
    } catch( ex ) {
      rwt.runtime.ErrorHandler.processJavaScriptError( ex );
    }
  },

  // TODO [tb] : refactor to work like _onKeyEvent
  _processMouseEvent : function( vDomEvent ) {
    var EventHandlerUtil = rwt.event.EventHandlerUtil;
    var vDomTarget = EventHandlerUtil.getDomTarget( vDomEvent );
    var vType = vDomEvent.type;
    switch(vType) {
      case "wheel":
        vType = "mousewheel";
      break;
      case "click":
      case "dblclick":
        // ignore click or dblclick events with other then the left mouse button
        if( vDomEvent.which !== 1 ) {
          return;
        }
    }
    this._onmouseevent_post( vDomEvent, vType, vDomTarget );
  },

  _onmouseevent_post : function( vDomEvent, vType, vDomTarget ) {
    var eventConsumed = false;
    var EventHandlerUtil = rwt.event.EventHandlerUtil;
    var vCaptureTarget = this.getCaptureWidget();
    var vOriginalTarget = EventHandlerUtil.getOriginalTargetObject( vDomTarget );
    var vTarget = EventHandlerUtil.getTargetObject( null, vOriginalTarget, true );
    if( !vTarget ) {
      return;
    }
    var vDispatchTarget = vCaptureTarget ? vCaptureTarget : vTarget;
    var vFixClick = this._onmouseevent_click_fix( vDomTarget, vType, vDispatchTarget );
    if( vType == "contextmenu" ) {
      if( this._allowContextMenu( vOriginalTarget, vDomTarget ) ) {
        eventConsumed = true;
      } else {
        EventHandlerUtil.stopDomEvent( vDomEvent );
      }
    }
    if( vType == "mousedown" ) {
      EventHandlerUtil.handleFocusedChild( vDispatchTarget );
    }
    // handle related target object
    if( vType == "mouseover" || vType == "mouseout" ) {
      var vRelatedTarget = EventHandlerUtil.getRelatedTargetObjectFromEvent( vDomEvent );
      var elementEventType = vType == "mouseover" ? "elementOver" : "elementOut";
      this._fireElementHoverEvents( elementEventType,
                                    vDomEvent,
                                    vDomTarget,
                                    vTarget,
                                    vOriginalTarget,
                                    vRelatedTarget,
                                    vDispatchTarget );
      // Ignore events where the related target and
      // the real target are equal - from our sight
      if( vRelatedTarget == vTarget ) {
        return;
      }
    }
    var vEventObject = new rwt.event.MouseEvent( vType,
                                                     vDomEvent,
                                                     vDomTarget,
                                                     vTarget,
                                                     vOriginalTarget,
                                                     vRelatedTarget );
    // Store last Event in MouseEvent Constructor. Needed for Tooltips, ...
    if( vType !== "contextmenu" ) {
      rwt.event.MouseEvent.storeEventState( vEventObject );
    }
    if( !eventConsumed && !( vType === "mousewheel" && !vEventObject.getWheelDelta() ) ) {
      this._dispatchMouseEvent( vDispatchTarget, vEventObject );
      if( vDispatchTarget.getEnabled() ) {
        this._onmouseevent_special_post( vType,
                                         vTarget,
                                         vOriginalTarget,
                                         vDispatchTarget,
                                         vEventObject,
                                         vDomEvent );
      }
    } else if( vType == "mouseover" ) {
      var toolTipManager = rwt.widgets.util.ToolTipManager.getInstance();
      toolTipManager.handleMouseEvent( vEventObject );
    }
    vEventObject.dispose();
    rwt.widgets.base.Widget.flushGlobalQueues();
    // Fix Click (Gecko Bug, see above)
    if( vFixClick ) {
      this._onmouseevent_post( vDomEvent, "click", this._lastMouseDownDomTarget );
      this._lastMouseDownDomTarget = null;
      this._lastMouseDownDispatchTarget = null;
    }
  },

  _fireElementHoverEvents : function( type,
                                      domEvent,
                                      domTarget,
                                      target,
                                      originalTarget,
                                      relatedTarget,
                                      dispatchTarget )
  {
    if( dispatchTarget.getEnabled() ) {
      var eventObject = new rwt.event.MouseEvent( type,
                                                  domEvent,
                                                  domTarget,
                                                  target,
                                                  originalTarget,
                                                  relatedTarget );
      this._dispatchMouseEvent( dispatchTarget, eventObject );
    }
  },

  _dispatchMouseEvent : function( target, event ) {
    var process = true;
    if( typeof this._filter[ "mouseevent" ] !== "undefined" ) {
      var context = this._filter[ "mouseevent" ][ 1 ];
      process = this._filter[ "mouseevent" ][ 0 ].call( context, event );
    }
    if( process ) {
      target.dispatchEvent( event );
    }
  },

  _onmouseevent_special_post : function( vType,
                                         vTarget,
                                         vOriginalTarget,
                                         vDispatchTarget,
                                         vEventObject )
  {
    switch( vType ) {
      case "mousedown":
        rwt.widgets.util.PopupManager.getInstance().update( vTarget );
        this.getMenuManager().update( vTarget, vType );
        rwt.widgets.util.IframeManager.getInstance().handleMouseDown( vEventObject );
      break;
      case "mouseup":
        // Mouseup event should always hide, independed of target,
        //  so don't send a target
        this.getMenuManager().update( vTarget, vType );
        if( rwt.qx.Class.isDefined("rwt.widgets.util.IframeManager" ) ) {
          rwt.widgets.util.IframeManager.getInstance().handleMouseUp( vEventObject );
        }
      break;
    }
    rwt.widgets.util.ToolTipManager.getInstance().handleMouseEvent( vEventObject );
    this._ignoreWindowBlur = vType === "mousedown";
    if( rwt.qx.Class.isDefined( "rwt.event.DragAndDropHandler" ) && vTarget ) {
      rwt.event.DragAndDropHandler.getInstance().handleMouseEvent( vEventObject );
    }
  },

  _ondragevent : function( event ) {
    try {
      if( !event ) {
        event = window.event;
      }
      if( event.type === "dragstart" || event.type === "dragover" || event.type === "drop" ) {
        rwt.event.EventHandlerUtil.stopDomEvent( event );
      }
      if( ( event.type === "dragover" || event.type === "dragenter" ) && event.dataTransfer ) {
        event.dataTransfer.dropEffect = "none";  // has no effect in IE
      }
    } catch( ex ) {
      rwt.runtime.ErrorHandler.processJavaScriptError( ex );
    }
  },

  ////////////////
  // SELECT EVENTS

  _onselectevent : function( event ) {
    try {
      var EventHandlerUtil = rwt.event.EventHandlerUtil;
      var target = EventHandlerUtil.getOriginalTargetObjectFromEvent( event );
      while( target ) {
        if( target.getSelectable() != null ) {
          if( !target.getSelectable() ) {
            EventHandlerUtil.stopDomEvent( event );
          }
          break;
        }
        target = target.getParent();
      }
    } catch( ex ) {
      rwt.runtime.ErrorHandler.processJavaScriptError( ex );
    }
  },

  _onwindowblur : function( e ) {
    try {
      if( !this._focused || this._ignoreWindowBlur || e.originalTarget != window ) {
        return;
      }
      this._focused = false;
      this.setCaptureWidget( null );
      if( rwt.qx.Class.isDefined( "rwt.widgets.util.PopupManager" ) ) {
        rwt.widgets.util.PopupManager.getInstance().update();
      }
      this.getMenuManager().update();
      if( rwt.qx.Class.isDefined( "rwt.event.DragAndDropHandler" ) ) {
        rwt.event.DragAndDropHandler.getInstance().globalCancelDrag();
      }
      rwt.widgets.base.ClientDocument.getInstance().createDispatchEvent( "windowblur" );
    } catch( ex ) {
      rwt.runtime.ErrorHandler.processJavaScriptError( ex );
    }
  },

  _onwindowfocus : function() {
    try {
      if( this._focused ) {
        return;
      }
      this._focused = true;
      rwt.widgets.base.ClientDocument.getInstance().createDispatchEvent( "windowfocus" );
    } catch( ex ) {
      rwt.runtime.ErrorHandler.processJavaScriptError( ex );
    }
  },

  _onwindowresize : function() {
    try {
      var clientDocument = rwt.widgets.base.ClientDocument.getInstance();
      // Catch redundant resize events, fired for example by iPad:
      var oldWidth = clientDocument.getInnerWidth();
      var oldHeight = clientDocument.getInnerHeight();
      var width = clientDocument._computeInnerWidth();
      var height = clientDocument._computeInnerHeight();
      if( width !== oldWidth || height !== oldHeight ) {
        rwt.widgets.base.ClientDocument.getInstance().createDispatchEvent( "windowresize" );
      }
    } catch( ex ) {
      rwt.runtime.ErrorHandler.processJavaScriptError( ex );
    }
  },

  ///////////////
  // Helper-maps:

  _mouseEventTypes : [
    "mouseover",
    "mousemove",
    "mouseout",
    "mousedown",
    "mouseup",
    "click",
    "dblclick",
    "contextmenu",
    "onwheel" in document ? "wheel" : "mousewheel"
  ],

  _keyEventTypes : [ "keydown", "keypress", "keyup" ],

  _dragEventTypes : [ "dragstart", "drop", "dragover", "dragleave", "dragenter" ],

  ////////////////////
  // Helper-functions:

  _getKeyEventTarget : function() {
    var vFocusRoot = this.getFocusRoot();
    return this.getCaptureWidget() || ( vFocusRoot == null ? null : vFocusRoot.getActiveChild() );
  },

  attachEventTypes : function( vEventTypes, vFunctionPointer ) {
    try {
      // Gecko is a bit buggy to handle key events on document if
      // not previously focused. Internet Explorer has problems to use
      // 'window', so there we use the 'body' element
      var el = rwt.client.Client.isGecko() ? window : document.body;
      for( var i=0, l=vEventTypes.length; i<l; i++ ) {
        el.addEventListener( vEventTypes[ i ], vFunctionPointer, { passive: false } );
      }
    }
    catch( ex ) {
      throw new Error( "EventHandler: Failed to attach window event types: " + vEventTypes + ": " + ex );
    }
  },

  detachEventTypes : function( vEventTypes, vFunctionPointer ) {
    try {
      var el = rwt.client.Client.isGecko() ? window : document.body;
      for(var i=0, l=vEventTypes.length; i<l; i++) {
        el.removeEventListener( vEventTypes[ i ], vFunctionPointer, { passive: false } );
      }
    } catch( ex ) {
      throw new Error( "EventHandler: Failed to detach window event types: " + vEventTypes + ": " + ex );
    }
  },

  /**
   * Fixes browser quirks with 'click' detection
   *
   * Firefox: The DOM-targets are different. The click event only fires,
   * if the target of the mousedown is the same than with the mouseup.
   * If the content moved away, the click isn't fired.
   */
  _onmouseevent_click_fix : rwt.util.Variant.select( "qx.client", {
    "gecko" : function( vDomTarget, vType, vDispatchTarget ) {
      var vReturn = false;
      switch( vType ) {
        case "mousedown":
          this._lastMouseDownDomTarget = vDomTarget;
          this._lastMouseDownDispatchTarget = vDispatchTarget;
        break;
        case "mouseup":
          if(    this._lastMouseDownDispatchTarget === vDispatchTarget
              && vDomTarget !== this._lastMouseDownDomTarget) {
            vReturn = true;
          } else {
            this._lastMouseDownDomTarget = null;
            this._lastMouseDownDispatchTarget = null;
          }
        break;
      }
      return vReturn;
    },

    "default" : function() {
      return false;
    }
  } )

};
