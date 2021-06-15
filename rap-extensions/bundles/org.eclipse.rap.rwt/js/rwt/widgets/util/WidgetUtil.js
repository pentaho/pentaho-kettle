/*******************************************************************************
 * Copyright (c) 2007, 2015 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

/**
 * This class contains static helper functions for widgets.
 */
rwt.qx.Class.define( "rwt.widgets.util.WidgetUtil", {

  statics : {

    getControl : function( widget ) {
      var widgetManager = rwt.remote.WidgetManager.getInstance();
      var result = widget;
      while( result != null && !widgetManager.isControl( result ) ) {
        result = result.getParent ? result.getParent() : null;
      }
      return result;
    },

    getShell : function( widget ) {
      var result = widget;
      while( result != null && result.classname !== "rwt.widgets.Shell" ) {
        result = result.getParent();
      }
      return result;
    },

    getChildIds : function( widget ) {
      return widget.getUserData( "rwt_Children" );
    },

    isVisible : function( widget ) {
      var result = true;
      var current = widget;
      while( current && result ) {
        result = current.getVisibility();
        current = current.getParent();
      }
      return result;
    },

    getGC : function( widget ) {
      var gc = widget.getUserData( "rwt.widgets.GC" );
      if( gc == null ) {
        gc = new rwt.widgets.GC( widget );
        widget.setUserData( "rwt.widgets.GC", gc );
      }
      return gc;
    },

    callWithElement : function( widget, callback ) {
      if( widget.getElement() ) {
        callback( widget.getElement() );
      } else {
        var listener = function listener(){
          callback( widget.getElement() );
          widget.removeEventListener( "create", listener );
        };
        widget.addEventListener( "create", listener );
      }
    },

    isWidget : function( target ) {
      return    ( ( target.classname || "" ).indexOf( "rwt.widgets" ) === 0 )
        || ( target instanceof rwt.widgets.base.Widget );
    },

    /**
     * Can be used simulate mouseEvents on the qooxdoo event-layer.
     * Manager and handler that are usually notified by
     * rwt.event.EventHandler will not receive the event.
     */
    _fakeMouseEvent : function( originalTarget, type ) {
      if( !( originalTarget instanceof rwt.widgets.base.Widget ) ) {
        originalTarget = rwt.event.EventHandlerUtil.getTargetObject( originalTarget );
        originalTarget = originalTarget || rwt.widgets.base.ClientDocument.getInstance();
      }
      if( originalTarget.getEnabled() ) {
        var domTarget = originalTarget._getTargetNode();
        var EventHandlerUtil = rwt.event.EventHandlerUtil;
        var target = EventHandlerUtil.getTargetObject( null, originalTarget, true );
        var domEvent = {
          "type" : type,
          "target" : domTarget,
          "button" : 0,
          "wheelData" : 0,
          "detail" : 0,
          "pageX" : 0,
          "pageY" : 0,
          "clientX" : 0,
          "clientY" : 0,
          "screenX" : 0,
          "screenY" : 0,
          "shiftKey" : false,
          "ctrlKey" : false,
          "altKey" : false,
          "metaKey" : false,
          "preventDefault" : function(){}
        };
        var event = new rwt.event.MouseEvent( type,
                                              domEvent,
                                              domTarget,
                                              target,
                                              originalTarget,
                                              null );
        target.dispatchEvent( event );
      }
    }

  }
});
