/*******************************************************************************
 * Copyright (c) 2011, 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.util.WidgetRenderAdapter", {

  extend : rwt.qx.Target,

  construct : function( widget ) {
    // Widget is responsible for the dispose:
    this._autoDispose = false;
    this.base( arguments );
    this._widget = widget;
    var key = this.classname;
    if( widget._adapters[ key ] != null ) {
      throw new Error( "Never create WidgetRenderAdapter directly!" );
    }
    widget._adapters[ key ] = this;
  },

  destruct : function() {
    // NOTE: disposing the adapter before the widget is not used or tested.
    this._widget = null;
  },

  events: {
    "visibility" : "rwt.event.DataEvent",
    "height" : "rwt.event.DataEvent",
    "top" : "rwt.event.DataEvent",
    "left" : "rwt.event.DataEvent",
    "opacity" : "rwt.event.DataEvent",
    "backgroundColor" : "rwt.event.DataEvent",
    "backgroundGradient" : "rwt.event.DataEvent"
  },

  members : {

    addRenderListener : function( type, listener, context ) {
      var rendererName = this._renderFunctionNames[ type ];
      if( !this.hasEventListeners( type ) ) {
        var that = this;
        this._widget[ rendererName ] = function() {
          var render = that.dispatchSimpleEvent( type, arguments, false );
          if( render ) {
            this.constructor.prototype[ rendererName ].apply( this, arguments );
          }
        };
      }
      this.addEventListener( type, listener, context );
    },

    removeRenderListener : function( type, listener, context ) {
      this.removeEventListener( type, listener, context );
      if( !this.hasEventListeners( type ) ) {
        var rendererName = this._renderFunctionNames[ type ];
        delete this._widget[ rendererName ];
      }
    },

    forceRender : function( type, value ) {
      this.getOriginalRenderer( type ).call( this._widget, value );
    },

    getOriginalRenderer : function( type ) {
      var rendererName = this._renderFunctionNames[ type ];
      var proto = this._widget.constructor.prototype;
      return proto[ rendererName ];
    },

    // TODO [tb]: AnimationRenderer#getValueFromWidget would also fit here

    _renderFunctionNames :  {
      "visibility" : "_applyVisibility",
      "height" : "_renderRuntimeHeight",
      "top" : "_renderRuntimeTop",
      "left" : "_renderRuntimeLeft",
      "opacity" : "_applyOpacity",
      "backgroundColor" : "_styleBackgroundColor",
      "backgroundGradient" : "_applyBackgroundGradient"
    }

  }

} );
