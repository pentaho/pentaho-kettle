/*******************************************************************************
 * Copyright (c) 2002, 2019 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

namespace( "rwt.remote" );

(function() {

var self;

rwt.remote.EventUtil = {

  _suspended : false,

  setSuspended : function( value ) {
    self._suspended = value;
  },

  getSuspended : function() {
    return self._suspended;
  },

  DOUBLE_CLICK_TIME : 500,

  _capturingWidget : null,
  _lastMouseDown : {
    widget : null,
    button : "",
    x : -1,
    y : -1,
    mouseUpCount : 0
  },
  _shiftKey : false,
  _ctrlKey : false,
  _altKey : false,
  _metaKey : false,
  _button : rwt.event.MouseEvent.C_BUTTON_NONE,

  eventTimestamp : function() {
    var init = rwt.runtime.System.getInstance();
    return ( new Date().getTime() - init.getStartupTime() ) % 0x7fffffff;
  },

  widgetDefaultSelected : function( event ) {
    self.notifyDefaultSelected( event.getTarget() );
  },

  widgetSelected : function( event ) {
    var left = event.getTarget().getLeft();
    var top = event.getTarget().getTop();
    var width = event.getTarget().getWidth();
    var height = event.getTarget().getHeight();
    self.notifySelected( event.getTarget(), left, top, width, height );
  },

  notifySelected : function( target ) {
    if( !self.getSuspended() && !target.isDisposed() ) {
      var connection = rwt.remote.Connection.getInstance();
      var properties = self._createSelectionProperties.apply( this, arguments );
      connection.getRemoteObject( target ).notify( "Selection", properties );
    }
  },

  notifyDefaultSelected : function( target ) {
    if( !self.getSuspended() && !target.isDisposed() ) {
      var connection = rwt.remote.Connection.getInstance();
      var properties = self._createSelectionProperties.apply( this, arguments );
      connection.getRemoteObject( target ).notify( "DefaultSelection", properties );
    }
  },

  _createSelectionProperties : function() {
    var properties;
    if( arguments.length === 2 ) {
      properties = arguments[ 1 ];
    } else {
      properties = {
        "x" : arguments[ 1 ],
        "y" : arguments[ 2 ],
        "width" : arguments[ 3 ],
        "height" : arguments[ 4 ],
        "detail" : arguments[ 5 ]
      };
    }
    self.addButtonToProperties( properties );
    self.addModifierToProperties( properties );
    return properties;
  },

  addButtonToProperties : function( properties, event ) {
    var button = event ? event.getButton() : self._button;
    switch( button ) {
      case rwt.event.MouseEvent.C_BUTTON_LEFT:
        properties.button = 1;
        break;
      case rwt.event.MouseEvent.C_BUTTON_MIDDLE:
        properties.button = 2;
        break;
      case rwt.event.MouseEvent.C_BUTTON_RIGHT:
        properties.button = 3;
        break;
      default:
        properties.button = 0;
        break;
    }
  },

  addModifierToProperties : function( properties, event ) {
    var isMac = rwt.client.Client.getPlatform() === "mac";
    var commandKey = isMac && ( event ? event.metaKey : self._metaKey ) === true;
    properties.shiftKey = event ? event.shiftKey : self._shiftKey;
    properties.ctrlKey = ( event ? event.ctrlKey : self._ctrlKey ) || commandKey;
    properties.altKey = event ? event.altKey : self._altKey;
  },

  focusGained : function( event ) {
    if( !self.getSuspended() ) {
      var remoteObject = rwt.remote.Connection.getInstance().getRemoteObject( event.getTarget() );
      remoteObject.notify( "FocusIn" );
    }
  },

  focusLost : function( event ) {
    if( !self.getSuspended() ) {
      var remoteObject = rwt.remote.Connection.getInstance().getRemoteObject( event.getTarget() );
      remoteObject.notify( "FocusOut" );
    }
  },

  ///////////////////////
  // Mouse event handling

  mouseDown : function( event ) {
    if( !self.getSuspended() && self._isRelevantMouseEvent( this, event ) ) {
      // disabled capturing as it interferes with Combo capturing
      // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=262171
      // from now on, redirect mouse event to this widget
      // this.setCapture( true );
      self._capturingWidget = this;
      // Collect request parameters and send
      self._notifyMouseListeners( this, event, "MouseDown" );
    }
  },

  mouseUp : function( event ) {
    if( !self.getSuspended() && self._isRelevantMouseEvent( this, event ) ) {
      // disabled capturing as it interferes with Combo capturing
      // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=262171
      // release mouse event capturing
      // this.setCapture( false );
      self._capturingWidget = null;
      // Add mouse-up request parameter
      self._notifyMouseListeners( this, event, "MouseUp" );
    }
  },

  mouseDoubleClick : function( event ) {
    if( !self.getSuspended() && self._isRelevantMouseEvent( this, event ) ) {
      // disabled capturing as it interferes with Combo capturing
      // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=262171
      // from now on, redirect mouse event to this widget
      // this.setCapture( true );
      self._capturingWidget = this;
      // Add parameters for double-click event
      if( self._isDoubleClick( this, event ) ) {
        self._clearLastMouseDown();
        self._notifyMouseListeners( this, event, "MouseDoubleClick" );
      } else {
        // Store relevant data of current event to detect double-clicks
        var lastMouseDown = self._lastMouseDown;
        lastMouseDown.widget = this;
        lastMouseDown.button = event.getButton();
        lastMouseDown.x = event.getPageX();
        lastMouseDown.y = event.getPageY();
        lastMouseDown.mouseUpCount = 0;
        rwt.client.Timer.once( self._clearLastMouseDown, this, self.DOUBLE_CLICK_TIME );
      }
    }
  },

  mouseUpCounter : function( event ) {
    if( !self.getSuspended() && self._isRelevantMouseEvent( this, event ) ) {
      // disabled capturing as it interferes with Combo capturing
      // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=262171
      // release mouse event capturing
      // this.setCapture( false );
      self._capturingWidget = null;
      // increase number of mouse-up events since last stored mouse down
      self._lastMouseDown.mouseUpCount += 1;
    }
  },

  /**
   * Determines whether the event is relevant (i.e. should be sent) for the
   * given widget.
   * @param widget - the listening widget
   * @param event - the mouse event
   */
  _isRelevantMouseEvent : function( widget, event ) {
    var result = true;
    if( widget !== self._capturingWidget && widget !== event.getOriginalTarget() ) {
      // find parent control and ensure that it is the same as the widget-
      // parameter. Otherwise the mouse event is ignored.
      var widgetManager = rwt.remote.WidgetManager.getInstance();
      var target = event.getOriginalTarget();
      var control = widgetManager.findEnabledControl( target );
      result = widget === control;
    }
    return result;
  },

  _clearLastMouseDown : function() {
    var lastMouseDown = self._lastMouseDown;
    lastMouseDown.widget = null;
    lastMouseDown.button = "";
    lastMouseDown.mouseUpCount = 0;
    lastMouseDown.x = -1;
    lastMouseDown.y = -1;
  },

  _isDoubleClick : function( widget, event ) {
    var lastMouseDown = self._lastMouseDown;
    return    lastMouseDown.mouseUpCount === 1
           && lastMouseDown.widget === widget
           && lastMouseDown.button === rwt.event.MouseEvent.C_BUTTON_LEFT
           && lastMouseDown.button === event.getButton()
           && self._isCloseTo( lastMouseDown.x,
                               lastMouseDown.y,
                               event.getPageX(),
                               event.getPageY() );
  },

  _isCloseTo : function( lastX, lastY, x, y ) {
    return x >= lastX - 5 && x <= lastX + 5 && y >= lastY - 5 && y <= lastY + 5;
  },

  _notifyMouseListeners : function( widget, event, eventType ) {
    var properties = {
      "x" : event.getPageX(),
      "y" : event.getPageY(),
      "time" : self.eventTimestamp()
    };
    self.addButtonToProperties( properties, event );
    self.addModifierToProperties( properties );
    rwt.remote.Connection.getInstance().getRemoteObject( widget ).notify( eventType, properties );
  },

  helpRequested : function( event ) {
    if( event.getKeyIdentifier() === "F1" ) {
      // stop further handling and default handling by the browser
      event.stopPropagation();
      event.preventDefault();
      // send help request to server
      var widget = event.getTarget();
      var widgetManager = rwt.remote.WidgetManager.getInstance();
      var id = widgetManager.findIdByWidget( widget );
      if( id === null ) {
        // find parent control for the widget that received the event in case
        // it wasn't the control itself that received the event
        widget = widgetManager.findControl( widget );
        id = widgetManager.findIdByWidget( widget );
      }
      if( id != null ) {
        var remoteObject = rwt.remote.Connection.getInstance().getRemoteObject( widget );
        remoteObject.notify( "Help" );
      }
    }
  },

  menuDetectedByKey : function( event ) {
    if( event.getKeyIdentifier() === "Apps" ) {
      // stop further handling and default handling by the browser
      event.stopPropagation();
      event.preventDefault();
      var x = rwt.event.MouseEvent.getPageX();
      var y = rwt.event.MouseEvent.getPageY();
      self.sendMenuDetected( event.getTarget(), x, y );
    }
  },

  menuDetectedByMouse : function( event ) {
    if( event.getButton() === rwt.event.MouseEvent.C_BUTTON_RIGHT ) {
      // stop further handling and default handling by the browser
      event.stopPropagation();
      event.preventDefault();
      var x = event.getPageX();
      var y = event.getPageY();
      self.sendMenuDetected( event.getTarget(), x, y );
    }
  },

  sendMenuDetected : function( widget, x, y ) {
    if( !self.getSuspended() ) {
      // send menu detect request to server
      var widgetManager = rwt.remote.WidgetManager.getInstance();
      // find parent control for the widget that received the event in case
      // it wasn't the control itself that received the event
      while( widget != null && !widgetManager.isControl( widget ) ) {
        widget = widget.getParent ? widget.getParent() : null;
      }
      var id = widgetManager.findIdByWidget( widget );
      if( id != null ) {
        var remoteObject = rwt.remote.Connection.getInstance().getRemoteObject( widget );
        remoteObject.notify( "MenuDetect", { "x" : x, "y" : y } );
      }
    }
  }

};

self = rwt.remote.EventUtil;

}() );
