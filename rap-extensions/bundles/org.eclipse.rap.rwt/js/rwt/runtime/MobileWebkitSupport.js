/*******************************************************************************
 * Copyright (c) 2010, 2016 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - ongoing development
 *    Austin Riddle (Texas Center for Applied Technology) - draggable types
 ******************************************************************************/

/*global alert:false, console:false */

namespace( "rwt.runtime" );


rwt.runtime.MobileWebkitSupport = {

  // These represent widget types and (optionally) defined appearances that are used to determine
  // if the widget is draggable.  If appearances are defined for a type, then one of the
  // appearances must match to allow the widget to be draggable.
  _draggableTypes : {
    "rwt.widgets.Shell" : null,
    "rwt.widgets.Sash"  : null,
    "rwt.widgets.Scale" : [ "scale-thumb" ],
    "rwt.widgets.Slider" : [ "slider-thumb" ],
    "rwt.widgets.base.ScrollBar" : null,
    "rwt.widgets.ScrolledComposite" : [ "scrollbar-thumb" ],
    "rwt.widgets.base.BasicButton" : [ "scrollbar-thumb" ],
    "rwt.widgets.base.Parent" : [ "coolitem-handle" ],
    "rwt.widgets.List" : [ "scrollbar-thumb" ],
    "rwt.widgets.Grid" : [ "tree-column", "label", "image", "scrollbar-thumb" ]
  },

  _lastMouseOverTarget : null,
  _lastMouseClickTarget : null,
  _lastMouseClickTime : null,
  _mouseEnabled : true,
  _fullscreen : window.navigator.standalone,
  _touchListener : null,
  _gestureListener : null,
  _touchSession : null,
  _allowNativeScroll : false,

  _allowedMouseEvents : {
    "INPUT" : {
      "mousedown" : true,
      "mouseup" : true
    },
    "TEXTAREA" : {
      "mousedown" : true,
      "mouseup" : true
    },
    "SELECT" : {
      "mousedown" : true,
      "mouseup" : true
    },
    "*" : {
      "mousewheel" : true
    }
  },

  init : function() {
    if( rwt.client.Client.supportsTouch() ) {
      this._configureToolTip();
      this._hideTabHighlight();
      this._bindListeners();
      this._registerListeners();
      this._registerFilter();
      // scrolling is currently very buggy in android, deactivated:
      this.setTouchScrolling( !rwt.client.Client.isAndroidBrowser() );
    }
  },

  // API for registration of custom-widgets for touch handling
  addDraggableType : function( type ) {
    // protect already registered types
    var exists = type in this._draggableTypes;
    if( !exists ) {
      this._draggableTypes[ type ] = null;
    }
  },

  // Experimental API for custom-widget
  setTouchListener : function( func, context ) {
    this._touchListener = [ func, context ];
  },

  // Experimental API for custom-widget
  setGestureListener : function( func, context ) {
    this._gestureListener = [ func, context ];
  },

  setTouchScrolling : function( value ) {
    this._allowNativeScroll = value;
  },

  _isZoomed : function() {
    var vertical = window.orientation % 180 === 0;
    var width = vertical ? screen.width : screen.height;
    return window.innerWidth !== width;
  },

  _configureToolTip : function() {
    var toolTip = rwt.widgets.base.WidgetToolTip.getInstance();
    toolTip.setMousePointerOffsetX( -35 );
    toolTip.setMousePointerOffsetY( -100 );
    var manager = rwt.widgets.util.ToolTipManager.getInstance();
    manager.handleMouseEvent = function( event ) {
      var type = event.getType();
      if( type === "mousedown" ) {
        this._handleMouseOver( event );
      } else if( type === "mouseup" ) {
        this.setCurrentToolTipTarget( null );
      }
    };
  },

  _hideTabHighlight : function() {
    rwt.html.StyleSheet.createElement( " * { -webkit-tap-highlight-color: rgba(0,0,0,0); }" );
  },

  _bindListeners : function() {
     this.__onTouchEvent = rwt.util.Functions.bind( this._onTouchEvent, this );
     this.__onGestureEvent = rwt.util.Functions.bind( this._onGestureEvent, this );
     this.__onOrientationEvent = rwt.util.Functions.bind( this._onOrientationEvent, this );
  },

  _registerListeners : function() {
    var target = document.body;
    if( rwt.client.Client.isGecko() ) {
      target.addEventListener( "touchstart", this.__onTouchEvent, false );
      target.addEventListener( "touchmove", this.__onTouchEvent, false );
      target.addEventListener( "touchend", this.__onTouchEvent, false );
      target.addEventListener( "touchcancel", this.__onTouchEvent, false );
      target.addEventListener( "deviceorientation", this.__onOrientationEvent, false );
    } else {
      // older iOs versions didn't recognize touch listener registered by AddEventListener:
      target.ontouchstart = this.__onTouchEvent;
      target.ontouchmove = this.__onTouchEvent;
      target.ontouchend = this.__onTouchEvent;
      target.ontouchcancel = this.__onTouchEvent;
      // on new iOS versions on the "ongestureXYZ" setter no longer works
      target.addEventListener( "gesturestart", this.__onGestureEvent );
      target.addEventListener( "gesturechange", this.__onGestureEvent );
      target.addEventListener( "gestureend", this.__onGestureEvent );
      target.onorientationchange = this.__onOrientationEvent;
    }
  },

  _removeListeners : function() {
    var target = document.body;
    if( rwt.client.Client.isGecko() ) {
      target.removeEventListener( "touchstart", this.__onTouchEvent, false );
      target.removeEventListener( "touchmove", this.__onTouchEvent, false );
      target.removeEventListener( "touchend", this.__onTouchEvent, false );
      target.removeEventListener( "touchcancel", this.__onTouchEvent, false );
      target.removeEventListener( "deviceorientation", this.__onOrientationEvent, false );
    } else {
      target.ontouchstart = null;
      target.ontouchmove = null;
      target.ontouchend = null;
      target.ontouchcancel = null;
      target.removeEventListener( "gesturestart", this.__onGestureEvent );
      target.removeEventListener( "gesturechange", this.__onGestureEvent );
      target.removeEventListener( "gestureend", this.__onGestureEvent );
      target.onorientationchange = null;
    }
  },

  _registerFilter : function() {
    rwt.event.EventHandler.setMouseDomEventFilter( this._filterMouseEvents, this );
  },

  _filterMouseEvents : function( event ) {
    var allowedMap = this._allowedMouseEvents;
    var result = typeof event.originalEvent === "object"; // faked event?
    if( !result ) {
      result = allowedMap[ "*" ][ event.type ] === true;
    }
    if( !result && typeof allowedMap[ event.target.tagName ] === "object" ) {
      result = allowedMap[ event.target.tagName ][ event.type ] === true;
    }
    if( !result ) {
      event.preventDefault();
      event.returnValue = false;
    }
    return result;
  },

  _onTouchEvent : function( domEvent ) {
    try {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        var type = domEvent.type;
        if( this._mouseEnabled ) {
          switch( type ) {
          case "touchstart":
            this._handleTouchStart( domEvent );
            break;
          case "touchend":
            this._handleTouchEnd( domEvent );
            break;
          case "touchmove":
            this._handleTouchMove( domEvent );
            break;
          }
        } else {
          if( this._touchListener !== null ) {
            this._touchListener[ 0 ].call( this._touchListener[ 1 ], domEvent );
          }
        }
      } else {
        domEvent.preventDefault();
      }
    } catch( ex ) {
      // problem: touch events emulate mouse events. When an error occurs in the emulation
      // layer, it would be ignored. However, if the ErrorHandler is called here, it will be
      // called twice if the error occurs within the mouse event handling. Therefore only
      // alert is used for now:
      alert( "Error in touch event handling:" + ex );
      if( typeof console === "object" ) {
        console.log( ex );
        if( ex.stack ) {
          console.log( ex.stack );
        }
      }
    }
  },

  _getTouch : function( domEvent ) {
    var touch = domEvent.touches.item( 0 );
    if( touch === null ) {
      // Should happen at touchend (behavior seems unpredictable)
      touch = domEvent.changedTouches.item( 0 );
    }
    return touch;
  },

  _handleTouchStart : function( domEvent ) {
    var touch = this._getTouch( domEvent );
    var target = domEvent.target;
    var widgetTarget = rwt.event.EventHandlerUtil.getOriginalTargetObject( target );
    var pos = [ touch.clientX, touch.clientY ];
    this._touchSession = {
     "type" : this._getSessionType( widgetTarget ),
     "initialTarget" : target,
     "widgetTarget" : widgetTarget,
     "initialPosition" : pos
    };
    if(    !this._touchSession.type.scroll
        && !this._touchSession.type.outerScroll
        && !this._touchSession.type.focus
        && !this._allowTouch( target ) )
    {
      domEvent.preventDefault();
    }
    this._moveMouseTo( target, domEvent );
    if( this._touchSession.type.click ) {
      this._fireMouseEvent( "mousedown", target, domEvent, pos );
    }
    if( this._touchSession.type.virtualScroll ) {
      this._initVirtualScroll( widgetTarget );
    }
  },

  _handleTouchMove : function( domEvent ) {
    if( this._touchSession !== null ) {
      var touch = this._getTouch( domEvent );
      var pos = [ touch.clientX, touch.clientY ];
      if( this._touchSession.type.virtualScroll ) {
        this._handleVirtualScroll( pos );
      }
      if( !this._touchSession.type.scroll ) {
        domEvent.preventDefault();
      }
      if( this._touchSession.type.drag ) {
        domEvent.preventDefault();
        var target = domEvent.target;
        this._fireMouseEvent( "mousemove", target, domEvent, pos );
      } else {
        var oldPos = this._touchSession.initialPosition;
        // TODO [tb] : offset too big for good use with touch-scrolling
        if(    Math.abs( oldPos[ 0 ] - pos[ 0 ] ) >= 15
            || Math.abs( oldPos[ 1 ] - pos[ 1 ] ) >= 15 )
        {
          this._cancelMouseSession( domEvent );
        }
      }
    }
  },

  _handleTouchEnd : function( domEvent ) {
    if( !this._allowTouch( domEvent.target ) ) {
      domEvent.preventDefault();
    }
    var touch = this._getTouch( domEvent );
    var pos = [ touch.clientX, touch.clientY ];
    var target = domEvent.target;
    if( this._touchSession !== null ) {
      var type = this._touchSession.type;
      if( type.delayedClick ) {
        this._fireMouseEvent( "mousedown", target, domEvent, pos );
      }
      if( type.click || type.delayedClick ) {
        this._fireMouseEvent( "mouseup", target, domEvent, pos );
      }
      if( this._touchSession.type.virtualScroll ) {
        this._finishVirtualScroll();
      }
      if( ( type.click || type.delayedClick ) && this._touchSession.initialTarget === target ) {
        this._fireMouseEvent( "click", target, domEvent, pos );
        this._touchSession = null;
        if( this._isDoubleClick( domEvent ) ) {
          this._lastMouseClickTarget = null;
          this._lastMouseClickTime = null;
          this._fireMouseEvent( "dblclick", target, domEvent, pos );
        } else {
          this._lastMouseClickTarget = target;
          this._lastMouseClickTime = ( new Date() ).getTime();
        }
      }
    }
  },

  _getSessionType : function( widgetTarget ) {
    var result = {};
    if( this._isSelectableWidget( widgetTarget ) || this._isFocusable( widgetTarget ) ) {
      result.delayedClick = true;
    } else {
      result.click = true;
    }
    if( this._isDraggableWidget( widgetTarget ) ) {
      result.drag = true;
    } else if( this._isGridRowContainer( widgetTarget ) ) {
      result.virtualScroll = true;
      var hasOuterScrollable
        = this._isScrollableWidget( widgetTarget ) || this._isClientDocumentScrollingEnabled();
      result.outerScroll = this._allowNativeScroll && hasOuterScrollable;
    } else if( this._allowNativeScroll && this._isScrollableWidget( widgetTarget ) ) {
      result.scroll = true;
    } else if( this._isFocusable( widgetTarget ) ) {
      result.focus = true;
    } else if( this._allowNativeScroll && this._isClientDocumentScrollingEnabled() ) {
      result.scroll = true;
    }
    return result;
  },

  ////////////////////
  // virtual scrolling

  _initVirtualScroll : function( widget ) {
    var scrollable;
    if( this._isGridRowContainer( widget ) ) {
      scrollable = widget.getParent();
    } else {
      scrollable = this._findScrollable( widget );
    }
    var scrollBarV = scrollable._vertScrollBar;
    var scrollBarH = scrollable._horzScrollBar;
    this._touchSession.scrollBarV = scrollBarV;
    this._touchSession.initScrollY = this._getScrollYOffset( scrollBarV );
    this._touchSession.scrollBarH = scrollBarH;
    this._touchSession.initScrollX = scrollBarH.getValue();
  },

  _handleVirtualScroll : function( pos ) {
    var oldPos = this._touchSession.initialPosition;
    var offsetX = oldPos[ 0 ] - pos[ 0 ];
    var offsetY = oldPos[ 1 ] - pos[ 1 ];
    var newX = this._touchSession.initScrollX + offsetX;
    var newY = this._touchSession.initScrollY + offsetY;
    var max = this._touchSession.scrollBarV.getMaximum() - this._touchSession.scrollBarV.getThumb();
    var adaptedNewY = this._adaptScrollYOffset( newY );
    var nudged = newY < 0 || adaptedNewY > max;
    if( this._touchSession.type.outerScroll && nudged ) {
      var outer = this._findScrollable( this._touchSession.widgetTarget );
      if( outer == null ) {
        delete this._touchSession.type.virtualScroll;
        this._touchSession.type.scroll = true;
      } else {
        var outerValue = outer._vertScrollBar.getValue();
        var outerMax = outer._vertScrollBar.getMaximum() - outer._vertScrollBar.getThumb();
        if( ( newY < 0 && outerValue > 0 ) || ( adaptedNewY > max && outerValue < outerMax ) ) {
          delete this._touchSession.type.virtualScroll;
          this._touchSession.type.scroll = true;
        }
      }
    }
    this._touchSession.scrollBarH.setValue( newX );
    this._touchSession.scrollBarV.setValue( adaptedNewY );
  },

  _adaptScrollYOffset : function( scrollY ) {
    if( this._isGridRowContainer( this._touchSession.widgetTarget ) ) {
      var grid = this._touchSession.widgetTarget.getParent();
      var item = grid.getRootItem().findItemByOffset( scrollY );
      return item ? item.getFlatIndex() : 0;
    }
    return scrollY ;
  },

  _getScrollYOffset : function( scrollBar ) {
    if( this._isGridRowContainer( this._touchSession.widgetTarget ) ) {
      var grid = this._touchSession.widgetTarget.getParent();
      var topItem = grid._getTopItem();
      return topItem ? topItem.getOffset() : 0;
    }
    return scrollBar.getValue();
  },

  _finishVirtualScroll : function() {
    // set ideal value to actual value (prevents scroll on resize when on max position)
    var barV = this._touchSession.scrollBarV;
    barV.setValue( barV.getValue() );
  },

  /////////
  // Helper

  _allowTouch : function( target ) {
    return target.tagName === "SELECT";
  },

  _isFocusable : function( widgetTarget ) {
    return widgetTarget instanceof rwt.widgets.base.BasicText;
  },

  _isScrollableWidget : function( widget ) {
    return this._findScrollable( widget ) !== null;
  },

  _isGridRowContainer : function( widgetTarget ) {
    return widgetTarget instanceof rwt.widgets.base.GridRowContainer;
  },

  _isSelectableWidget : function( widgetTarget ) {
    var result = false;
    if( widgetTarget instanceof rwt.widgets.ListItem || this._isGridRowContainer( widgetTarget ) ) {
      result = true;
    }
    return result;
  },

  _isClientDocumentScrollingEnabled : function() {
    var overflow = rwt.widgets.base.ClientDocument.getInstance().getOverflow();
    return overflow && overflow !== "hidden";
  },

  _findScrollable : function( widget ) {
    var result = null;
    var currentWidget = widget;
    do {
      if( currentWidget instanceof rwt.widgets.base.Scrollable ) {
        result = currentWidget;
      } else if( currentWidget instanceof rwt.widgets.base.ClientDocument ) {
        currentWidget = null;
      } else {
        currentWidget = currentWidget.getParent();
      }
    } while( currentWidget && !result );
    return result;
  },

  _isDraggableWidget : function ( widgetTarget ) {
    var widgetManager = rwt.remote.WidgetManager.getInstance();
    // We find the nearest control because matching based on widgetTarget can produce too
    // generalized cases.
    var widget = widgetManager.findControl( widgetTarget );
    var draggable = false;
    if( widget == null ) {
      widget = widgetTarget;
    }
    if( widget != null && widget.classname in this._draggableTypes ) {
      var appearances = this._draggableTypes[ widget.classname ];
      if( appearances == null ) {
        draggable = true;
      } else {
        for( var i = 0; i < appearances.length && !draggable; i++ ) {
          if( widgetTarget.getAppearance() === appearances[ i ] ) {
            draggable = true;
          }
        }
      }
    }
    return draggable;
  },

  _isDoubleClick : function( domEvent ) {
    var target = domEvent.target;
    var result = false;
    if( this._lastMouseClickTarget === target ) {
      var diff = ( ( new Date() ).getTime() ) - this._lastMouseClickTime;
      result = diff < rwt.remote.EventUtil.DOUBLE_CLICK_TIME;
    }
    return result;
  },

  _onGestureEvent : function( domEvent ) {
    domEvent.preventDefault();
    var type = domEvent.type;
    if( this._gestureListener !== null ) {
      this._gestureListener[ 0 ].call( this._gestureListener[ 1 ], domEvent );
    }
    switch( type ) {
      case "gesturestart":
        this._disableMouse( domEvent );
      break;
      case "gestureend":
        this._enableMouse( domEvent );
      break;
    }
  },

  _onOrientationEvent : function() {
    // Nothing to do yet
  },

  ////////////////
  // emulate mouse

  _disableMouse : function( domEvent ) {
    // Note: Safari already does somthing similar to this (a touchevent
    // that executes JavaScript will prevent further touch/gesture events),
    // but no in all cases, e.g. on a touchstart with two touches.
    this._cancelMouseSession( domEvent );
    this._mouseEnabled = false;
  },

  _cancelMouseSession : function( domEvent ) {
    var dummy = this._getDummyTarget();
    this._moveMouseTo( dummy, domEvent );
    if( this._touchSession !== null ) {
      this._fireMouseEvent( "mouseup", dummy, domEvent, [ 0, 0 ] );
      delete this._touchSession.type.click;
      delete this._touchSession.type.delayedClick;
    }
  },

  // The target used to release the virtual mouse without consequences
  _getDummyTarget : function() {
    return rwt.widgets.base.ClientDocument.getInstance()._getTargetNode();
  },

  _enableMouse : function() {
    this._mouseEnabled = true;
  },

  _moveMouseTo : function( target, domEvent ) {
    var oldTarget = this._lastMouseOverTarget;
    if( oldTarget !== target ) {
      var pos = [ 0, 0 ];
      if( oldTarget !== null ) {
        this._fireMouseEvent( "mouseout", oldTarget, domEvent, pos );
      }
      this._lastMouseOverTarget = target;
      this._fireMouseEvent( "mouseover", target, domEvent, pos );
    }
  },

  _fireMouseEvent : function( type, target, originalEvent, coordiantes ) {
    var event = document.createEvent( "MouseEvent" );
    event.initMouseEvent( type,
                          true, // bubbles
                          true, //cancelable
                          window, //view
                          0, // detail
                          coordiantes[ 0 ], //screenX
                          coordiantes[ 1 ], //screenY
                          coordiantes[ 0 ], //clientX
                          coordiantes[ 1 ], //clientY
                          false, //ctrlKey
                          false, //altKey
                          false, //shiftKey
                          false, //metaKey
                          rwt.event.MouseEvent.buttons.left,
                          null );
    event.originalEvent = originalEvent;
    target.dispatchEvent( event );
  },

  _postMouseEvent : function( type ) {
    if( type === "mouseup" ) {
      rwt.widgets.util.ToolTipManager.getInstance().setCurrentToolTipTarget( null );
    }
  }

};
