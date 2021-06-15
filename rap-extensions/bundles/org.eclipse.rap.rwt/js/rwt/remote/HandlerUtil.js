/*******************************************************************************
 * Copyright (c) 2011, 2018 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

namespace( "rwt.remote" );

rwt.remote.HandlerUtil = {

  SERVER_DATA : "org.eclipse.swt.widgets.Widget#data",

  _controlDestructor : function( widget ) {
    rwt.remote.HandlerUtil._widgetDestructor( widget );
  },

  _childrenFinder : function( widget ) {
    return rwt.remote.HandlerUtil.getDestroyableChildren( widget );
  },

  _widgetDestructor : function( widget ) {
    var parent = widget.getUserData( "protocolParent" );
    if( parent ) {
      rwt.remote.HandlerUtil.removeDestroyableChild( parent, widget );
    }
    widget.setToolTipText( null );
    widget.destroy();
  },

  _controlProperties : [
    "parent",
    "children",
    "tabIndex",
    "toolTipMarkupEnabled",
    /**
     * @name setToolTipText
     * @methodOf Control#
     * @description Sets the receiver's tool tip text to the argument, which
     * may be null indicating that no tool tip text should be shown.
     * @param {string|null} toolTipText the new tool tip text (or null)
     */
    "toolTip",
    /**
     * @name setVisible
     * @methodOf Control#
     * @description Marks the receiver as visible if the argument is <code>true</code>,
     * and marks it invisible otherwise.
     * <p>
     * If one of the receiver's ancestors is not visible or some
     * other condition makes the receiver not visible, marking
     * it visible may not actually cause it to be displayed.
     * </p>
     * <p>
     * <b>NOTE:</b> If there is a <code>Show</code> or <code>Hide</code> Java listener attached
     * to this widget, it may be notified at a later point in time. <code>ClientListener</code>
     * are notified right away.
     * </p>
     * @param {boolean} visible the new visibility state
     */
    "visibility",
    /**
     * @name setEnabled
     * @methodOf Control#
     * @description Enables the receiver if the argument is <code>true</code>,
     * and disables it otherwise. A disabled control is typically
     * not selectable from the user interface and draws with an
     * inactive or "grayed" look.
     *
     * @param {boolean} enabled the new enabled state
     */
    "enabled",
    /**
     * @name setForeground
     * @methodOf Control#
     * @description Sets the receiver's foreground color to the color specified
     * by the argument, or to the default system color for the control
     * if the argument is null.
     * @param {int[]|null} color the new color as array [ red, green, blue ] or null
     */
    "foreground",
    /**
     * @name setBackground
     * @methodOf Control#
     * @description Sets the receiver's background color to the color specified
     * by the argument, or to the default system color for the control
     * if the argument is null.
     * @param {int[]|null} color the new color as array [ red, green, blue ] or null
     */
    "background",
    "backgroundImage",
    /**
     * @name setCursor
     * @methodOf Control#
     * Sets the receiver's cursor to the cursor specified by the
     * argument, or to the default cursor for that kind of control
     * if the argument is null.
     * <p>
     * When the mouse pointer passes over a control its appearance
     * is changed to match the control's cursor.
     * </p>
     * <p>
     * All possible values are available as constants on the {@link SWT} object.
     * </p>
     * @param {string|null} cursor the new cursor (or null)
     */
    "cursor",
    "customVariant",
    "bounds",
    "font",
    "menu",
    "activeKeys",
    "cancelKeys",
    "data",
    "direction"
  ],

  _controlPropertyHandler : {
    "parent" : function( widget, value ) {
      var oldParent = widget.getParent();
      var oldParentId = oldParent == null ? null : rwt.remote.ObjectRegistry.getId( oldParent );
      if( oldParentId !== value ) {
        widget.setUserData( "scrolledComposite", null );
        widget.setUserData( "tabFolder", null );
        if( oldParent != null ) {
          rwt.remote.HandlerUtil.removeDestroyableChild( oldParent, widget );
        }
        rwt.remote.HandlerUtil.setParent( widget, value );
      }
    },
    "data" : function( target, value ) {
      var map = rwt.remote.HandlerUtil.getServerData( target );
      rwt.util.Objects.mergeWith( map, value );
      target.dispatchSimpleEvent( "dataChanged" );
    },
    "children" : function( widget, value ) {
      if( value !== null ) {
        var childrenCount = value.length;
        var applyZIndex = function( child ) {
          var index = value.indexOf( rwt.remote.ObjectRegistry.getId( child ) );
          child.setZIndex( childrenCount - index );
        };
        for( var i = 0; i < childrenCount; i++ ) {
          rwt.remote.HandlerUtil.callWithTarget( value[ i ], applyZIndex );
        }
      }
      widget.setUserData( "rwt_Children", value );
    },
    "foreground" : function( widget, value ) {
      if( value === null ) {
        widget.resetTextColor();
      } else {
        widget.setTextColor( rwt.util.Colors.rgbToRgbString( value ) );
      }
    },
    "background" : function( widget, value ) {
      if( value === null ) {
        widget.resetBackgroundColor();
        if( widget.__user$backgroundGradient == null ) {
          widget.resetBackgroundGradient();
        }
      } else {
        if( widget.__user$backgroundGradient == null ) {
          widget.setBackgroundGradient( null );
        }
        var color = value[ 3 ] === 0 ? "transparent" : rwt.util.Colors.rgbaToRgbaString( value );
        widget.setBackgroundColor( color );
      }
    },
    "backgroundImage" : function( widget, value ) {
      if( value === null ) {
        widget.resetBackgroundImage();
        widget.setUserData( "backgroundImageSize", null );
      } else {
        widget.setUserData( "backgroundImageSize", value.slice( 1 ) );
        widget.setBackgroundImage( value[ 0 ] );
      }
    },
    "cursor" : function( widget, value ) {
      if( value === null ) {
        widget.resetCursor();
      } else {
        widget.setCursor( value );
      }
    },
    "bounds" : function( widget, value ) {
      var bounds = value;
      if( widget.getUserData( "tabFolder" ) !== null ) {
        bounds[ 0 ] = 0;
        bounds[ 1 ] = 0;
      }
      if( widget.getUserData( "scrolledComposite" ) === null ) {
        widget.setLeft( bounds[ 0 ] );
        widget.setTop( bounds[ 1 ] );
      }
      widget.setWidth( bounds[ 2 ] );
      widget.setHeight( bounds[ 3 ] );
    },
    "toolTipMarkupEnabled" : function( widget, value ) {
      widget.setUserData( "toolTipMarkupEnabled", value );
    },
    "toolTip" : function( widget, value ) {
      rwt.widgets.base.WidgetToolTip.setToolTipText( widget, value );
    },
    "font" : function( widget, fontData ) {
      if( widget.setFont ) { // test if font property is supported - why wouldn't it? [tb]
        if( fontData === null ) {
          widget.resetFont();
        } else {
          var font = rwt.html.Font.fromArray( fontData );
          widget.setFont( font );
        }
      }
    },
    "menu" : function( widget, value ) {
      rwt.remote.HandlerUtil.callWithTarget( value, function( menu ) {
        widget.setContextMenu( menu );
        var detectByKey = rwt.widgets.Menu.menuDetectedByKey;
        var detectByMouse = rwt.widgets.Menu.menuDetectedByMouse;
        if( menu == null ) {
          widget.removeEventListener( "keydown", detectByKey );
          widget.removeEventListener( "mouseup", detectByMouse );
        } else {
          widget.addEventListener( "keydown", detectByKey );
          widget.addEventListener( "mouseup", detectByMouse );
        }
      } );
    },
    "activeKeys" : function( widget, value ) {
      var map = rwt.util.Objects.fromArray( value );
      widget.setUserData( "activeKeys", map );
    },
    "cancelKeys" : function( widget, value ) {
      var map = rwt.util.Objects.fromArray( value );
      widget.setUserData( "cancelKeys", map );
    }
  },

  _controlListeners : [
    "FocusIn",
    "FocusOut",
    "MouseDown",
    "MouseUp",
    "MouseDoubleClick",
    "KeyDown",
    "Traverse",
    "MenuDetect",
    "Help",
    "Activate",
    "Deactivate"
  ],

  _controlListenerHandler : {
    "KeyDown" : function( widget, value ) {
      widget.setUserData( "keyListener", value ? true : null );
    },
    "Traverse" : function( widget, value ) {
      widget.setUserData( "traverseListener", value ? true : null );
    },
    "FocusIn" : function( widget, value ) {
      var context = rwt.remote.EventUtil;
      var focusGained = rwt.remote.EventUtil.focusGained;
      if( value ) {
        widget.addEventListener( "focus", focusGained, context );
      } else {
        widget.removeEventListener( "focus", focusGained, context );
      }
    },
    "FocusOut" : function( widget, value ) {
      var context = rwt.remote.EventUtil;
      var focusLost = rwt.remote.EventUtil.focusLost;
      if( value ) {
        widget.addEventListener( "blur", focusLost, context );
      } else {
        widget.removeEventListener( "blur", focusLost, context );
      }
    },
    "MouseDown" : function( widget, value ) {
      var context;
      var mouseDown = rwt.remote.EventUtil.mouseDown;
      if( value ) {
        widget.addEventListener( "mousedown", mouseDown, context );
      } else {
        widget.removeEventListener( "mousedown", mouseDown, context );
      }
    },
    "MouseUp" : function( widget, value ) {
      var context;
      var mouseUp = rwt.remote.EventUtil.mouseUp;
      if( value ) {
        widget.addEventListener( "mouseup", mouseUp, context );
      } else {
        widget.removeEventListener( "mouseup", mouseUp, context );
      }
    },
    "MouseDoubleClick" : function( widget, value ) {
      var context;
      var mouseDoubleClick = rwt.remote.EventUtil.mouseDoubleClick;
      var mouseUpCounter = rwt.remote.EventUtil.mouseUpCounter;
      if( value ) {
        widget.addEventListener( "mousedown", mouseDoubleClick, context );
        widget.addEventListener( "mouseup", mouseUpCounter, context );
      } else {
        widget.removeEventListener( "mousedown", mouseDoubleClick, context );
        widget.removeEventListener( "mouseup", mouseUpCounter, context );
      }
    },
    "MenuDetect" : function( widget, value ) {
      var context;
      var detectByKey = rwt.remote.EventUtil.menuDetectedByKey;
      var detectByMouse = rwt.remote.EventUtil.menuDetectedByMouse;
      if( value ) {
        widget.addEventListener( "keydown", detectByKey, context );
        widget.addEventListener( "mouseup", detectByMouse, context );
      } else {
        widget.removeEventListener( "keydown", detectByKey, context );
        widget.removeEventListener( "mouseup", detectByMouse, context );
      }
    },
    "Help" : function( widget, value ) {
      var context;
      var helpRequested = rwt.remote.EventUtil.helpRequested;
      if( value ) {
        widget.addEventListener( "keydown", helpRequested, context );
      } else {
        widget.removeEventListener( "keydown", helpRequested, context );
      }
    },
    "Activate" : function( widget, value ) {
      widget.setUserData( "activateListener", value ? true : null );
    },
    "Deactivate" : function( widget, value ) {
      widget.setUserData( "deactivateListener", value ? true : null );
    }
  },

  _specialHandler : {
    "backgroundGradient" : function( widget, value ) {
      var gradient = null;
      if( value ) {
        var colors = value[ 0 ];
        var percents = value[ 1 ];
        var vertical = value[ 2 ];
        gradient = [];
        for( var i = 0; i < colors.length; i++ ) {
          gradient[ i ] = [ percents[ i ] / 100, rwt.util.Colors.rgbToRgbString( colors[ i ] ) ];
        }
        gradient.horizontal = !vertical;
      }
      widget.setBackgroundGradient( gradient );
    },
    "roundedBorder" : function( widget, value ) {
      if( value ) {
        var width = value[ 0 ];
        var color = rwt.util.Colors.rgbToRgbString( value[ 1 ] );
        var radii = value.slice( -4 );
        var border = new rwt.html.Border( width, "solid", color, radii );
        widget.setBorder( border );
      } else {
        widget.resetBorder();
      }
    }
  },

  _listenerMethodHandler : {
    "addListener": function( widget, properties ) {
      rwt.remote.HandlerUtil.callWithTarget( properties.listenerId, function( targetFunction ) {
        rwt.scripting.EventBinding.addListener( widget, properties.eventType, targetFunction );
      } );
    },
    "removeListener": function( widget, properties ) {
      rwt.remote.HandlerUtil.callWithTarget( properties.listenerId, function( targetFunction ) {
        rwt.scripting.EventBinding.removeListener( widget, properties.eventType, targetFunction );
      } );
    }
  },

  /**
   * @private
   * @class RWT Scripting analog to org.eclipse.swt.widgets.Control. All controls given by
   * {@link rap.getObject} are instances of this type, even if their specific subtype is not
   * documented.
   * @name Control
   * @extends Widget
   * @description The constructor is not public.
   * @since 2.2
   */
  _controlScriptingMethods : /** @lends Control.prototype */ {

    /**
     * @description  Forces the receiver to have the <em>keyboard focus</em>, causing
     * all keyboard events to be delivered to it.
     * @return {boolean} <code>true</code> if the control got focus, and <code>false</code> if it was unable to.
     */
    forceFocus : function() {
      var result = false;
      if( this.getEnabled() && rwt.widgets.util.WidgetUtil.isVisible( this ) ) {
        var id = rwt.remote.ObjectRegistry.getId( this );
        rwt.widgets.Display.getCurrent().setFocusControl( id );
        result = true;
      }
      return result;
    },

    /**
     * @description Returns the receiver's background color.
     * @return {int[]} the background color as array [ red, green, blue ]
     */
    getBackground : function() {
      return rwt.util.Colors.stringToRgb( this.getBackgroundColor() );
    },

    /**
     * @description Returns the receiver's foreground color.
     * @return {int[]} the foreground color as array [ red, green, blue ]
     */
    getForeground : function() {
      return rwt.util.Colors.stringToRgb( this.getTextColor() );
    },

    /**
    * @description Returns the receiver's tool tip text, or null if it has
    * not been set.
    * @return {string|null} the receiver's tool tip text
    */
    getToolTipText : function() {
      return this.getToolTipText();
    },

    /**
    * @description Returns <code>true</code> if the receiver is visible, and
    * <code>false</code> otherwise.
    * <p>
    * If one of the receiver's ancestors is not visible or some
    * other condition makes the receiver not visible, this method
    * may still indicate that it is considered visible even though
    * it may not actually be showing.
    * </p>
    * @return {boolean} the receiver's visibility state
    */
    getVisible : function() {
      return this.getVisibility();
    },

    /**
    * @description Returns <code>true</code> if the receiver is enabled, and
    * <code>false</code> otherwise. A disabled control is typically
    * not selectable from the user interface and draws with an
    * inactive or "grayed" look.
    * @return {boolean} the receiver's enabled state
    */
    getEnabled : function() {
     return this.getEnabled();
    },

    /**
    * @description Returns the receiver's cursor, or null if it has not been set.
    * <p>
    * When the mouse pointer passes over a control its appearance
    * is changed to match the control's cursor.
    * </p>
    * <p>
    * All possible values are available as constants on the {@link SWT} object.
    * </p>
    * @return {string|null} the receiver's cursor or <code>null</code>
    */
    getCursor : function() {
      return this.__user$cursor || null;
    }

  },

  _widgetScriptingMethods : /** @lends Widget.prototype */ {

    /**
     * @name addListener
     * @methodOf Control#
     * @description Register the function as a listener of the given type
     * @param {string} type The type of the event (e.g. SWT.Resize).
     * @param {Function} listener The callback function. It is executed in global context.
     */
    addListener : function( type, listener ) {
      rwt.scripting.EventBinding.addListener( this, type, listener );
    },

    /**
     * @name removeListener
     * @methodOf Control#
     * @description De-register the function as a listener of the given type
     * @param {string} type The type of the event (e.g. SWT.Resize).
     * @param {Function} listener The callback function
     */
    removeListener : function( type, listener ) {
      rwt.scripting.EventBinding.removeListener( this, type, listener );
    },

    /**
     * @description Sets the application defined property of the receiver
     * with the specified name to the given value.
     * <p>
     *   The java widget is not affected by this method, but can itself set this object's
     *   properties if the name was registered with WidgetUtil.registerDataKeys.
     * </p>
     * @param {string} property the name of the property
     * @param {*} value the new value for the property
     * @see Widget#getData
     */
    setData : function( property, value ) {
      if( arguments.length !== 2 ) {
        var msg =  "Wrong number of arguments in setData: Expected 2, found " + arguments.length;
        throw new Error( msg );
      }
      var data = rwt.remote.HandlerUtil.getServerData( this );
      data[ property ] = value;
      this.dispatchSimpleEvent( "dataChanged" );
    },

    /**
     * @description  Returns the application defined property of the receiver
     * with the specified name, or null if it has not been set.
     * <p>
     *   The java widget properties can be accessed if the
     *   property name was registered with WidgetUtil.registerDataKeys.
     * </p>
     * @param {string} property the name of the property
     * @return {*} the value
     * @see Widget#setData
     */
    getData : function( property ) {
      if( arguments.length !== 1 ) {
        var msg =  "Wrong number of arguments in getData: Expected 1, found " + arguments.length;
        throw new Error( msg );
      }
      var result = null;
      var data = rwt.remote.HandlerUtil.getServerData( this );
      if( typeof data[ property ] !== "undefined" ) {
        result = data[ property ];
      }
      return result;
    }

  },

  ////////////////////
  // lists and handler

  getWidgetDestructor : function() {
    return this._widgetDestructor;
  },

  getControlDestructor : function() {
    return this._controlDestructor;
  },

  getDestroyableChildrenFinder : function() {
    return this._childrenFinder;
  },

  extendControlProperties : function( list ) {
    return list.concat( this._controlProperties );
  },

  extendControlPropertyHandler : function( handler ) {
    return rwt.util.Objects.mergeWith( handler, this._controlPropertyHandler, false );
  },

  extendListenerMethodHandler : function( handler ) {
    return rwt.util.Objects.mergeWith( handler, this._listenerMethodHandler, false );
  },

  extendControlListeners : function( list ) {
    return list.concat( this._controlListeners );
  },

  extendControlListenerHandler : function( handler ) {
    return rwt.util.Objects.mergeWith( handler, this._controlListenerHandler, false );
  },

  extendControlScriptingMethods : function( methods ) {
    return rwt.util.Objects.mergeWith( methods, this._controlScriptingMethods, false );
  },

  getBackgroundGradientHandler : function() {
    return this._specialHandler.backgroundGradient;
  },

  getRoundedBorderHandler : function() {
    return this._specialHandler.roundedBorder;
  },

  getControlPropertyHandler : function( property ) {
    return this._controlPropertyHandler[ property ];
  },

  getControlListenerHandler : function( handler ) {
    return this._controlListenerHandler[ handler ];
  },

  /////////////////////
  // Helper for handler

  addStatesForStyles : function( targetObject, styleArray ) {
    if( styleArray ) {
      for( var i = 0; i < styleArray.length; i++ ) {
        targetObject.addState( "rwt_" + styleArray[ i ] );
      }
    }
    targetObject._renderAppearance();
    delete targetObject._isInGlobalStateQueue;
  },

  createStyleMap : function( styleArray ) {
    var result = {};
    if( styleArray ) {
      for( var i = 0; i < styleArray.length; i++ ) {
        result[ styleArray[ i ] ] = true;
      }
    }
    return result;
  },

  setParent : function( widget, parentId ) {
    var impl = this._setParentImplementation;
    this.callWithTarget( parentId, function( parent ) {
      impl( widget, parent );
    } );
  },

  _setParentImplementation : function( widget, parent ) {
    // TODO [rh] there seems to be a difference between add and setParent
    //      when using add sizes and clipping are treated differently
    // parent.add( widget );
    if( parent instanceof rwt.widgets.ScrolledComposite ) {
      // [if] do nothing, parent is set in ScrolledComposite#setContent which is called from the
      // server-side - see bug 349161
      widget.setUserData( "scrolledComposite", parent ); // Needed by "bounds" handler
    } else if ( parent instanceof rwt.widgets.TabFolder ) {
      widget.setUserData( "tabFolder", parent ); // Needed by "bounds" handler
    } else if( parent instanceof rwt.widgets.ExpandBar ) {
      parent.addWidget( widget );
    } else {
      widget.setParent( parent );
    }
    rwt.remote.HandlerUtil.addDestroyableChild( parent, widget );
    widget.setUserData( "protocolParent", parent );
  },

  callWithTarget : function( id, fun ) {
    if( id == null ) {
      fun( null );
    } else {
      var target = rwt.remote.ObjectRegistry.getObject( id );
      if( target ) {
        fun( target );
      } else {
        rwt.remote.ObjectRegistry.addRegistrationCallback( id, fun );
      }
    }
  },

  callWithTargets : function( ids, func ) {
    var result = [];
    result.resolved = 0;
    for( var i = 0; i < ids.length; i++ ) {
      this.callWithTarget( ids[ i ], this._createPartCallback( result, ids, i, func ) );
    }
  },

  _createPartCallback : function( result, ids, index, func ) {
    return function( target ) {
      result[ index ] = target;
      result.resolved++;
      if( result.resolved === ids.length ) {
        delete result.resolved;
        func( result );
      }
    };
  },

  filterUnregisteredObjects : function( list ) {
    var ObjectRegistry = rwt.remote.ObjectRegistry;
    var result = [];
    for( var i = 0; i < list.length; i++ ) {
      if( ObjectRegistry.getId( list[ i ] ) ) {
        result.push( list[ i ] );
      }
    }
    return result;
  },

  // TODO : Can we use "children" property in most cases instead??
  addDestroyableChild : function( parent, child ) {
    var list = parent.getUserData( "destroyableChildren" );
    if( list == null ) {
      list = {};
      parent.setUserData( "destroyableChildren", list );
    }
    list[ rwt.qx.Object.toHashCode( child ) ] = child;
  },

  removeDestroyableChild : function( parent, child ) {
    var list = parent.getUserData( "destroyableChildren" );
    if( list != null ) {
      delete list[ rwt.qx.Object.toHashCode( child ) ];
    }
  },

  getDestroyableChildren : function( parent ) {
    var list = parent.getUserData( "destroyableChildren" );
    if( list == null ) {
      list = {};
    }
    var result = [];
    for( var key in list ) {
      result.push( list[ key ] );
    }
    return result;
  },

  getServerData : function( target ) {
    var result = target.getUserData( rwt.remote.HandlerUtil.SERVER_DATA );
    if( result == null ) {
      result = {};
      target.setUserData( rwt.remote.HandlerUtil.SERVER_DATA, result );
    }
    return result;
  }

};
