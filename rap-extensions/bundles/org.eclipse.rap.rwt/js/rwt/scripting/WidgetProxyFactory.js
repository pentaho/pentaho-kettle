/*******************************************************************************
 * Copyright (c) 2012, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

(function(){

var ObjectRegistry = rwt.remote.ObjectRegistry;
var Synchronizer = rwt.scripting.Synchronizer;
var HandlerUtil = rwt.remote.HandlerUtil;
var WidgetUtil = rwt.widgets.util.WidgetUtil;

rwt.define( "rwt.scripting", {} );

/**
 * @private
 * @class RWT Scripting analog to org.eclipse.swt.widgets.Widget. All widgets given by
 * {@link rap.getObject} are instances of this type, even if their specific subtype is not
 * documented.
 * @name Widget
 * @description The constructor is not public.
 * @since 2.2
 */

/*jshint nonew:false */
// TODO [rst] Define directly using rwt.define, remove surrounding function scope
rwt.scripting.WidgetProxyFactory = {

  _wrapperMap : {},
  _ALIAS : {
    "visibility" : "setVisible",
    "toolTip" : "setToolTipText"
  },

  getWidgetProxy : function( obj ) {
    var result = null;
    if( obj instanceof Object ) {
      var hash = rwt.qx.Object.toHashCode( obj );
      if( this._wrapperMap[ hash ] == null ) {
        if( obj instanceof rwt.widgets.Composite ) {
          result = new rwt.scripting.CompositeProxy( obj );
        } else {
          result = {};
        }
        this._wrapperMap[ hash ] = result;
        this._initWrapper( obj, result );
      }
      result = this._wrapperMap[ hash ];
    }
    return result;
  },

  _initWrapper : function( originalWidget, wrapper ) {
    if( WidgetUtil.isWidget( originalWidget ) ) {
      new Synchronizer( originalWidget );
    }
    this._attachSetter( wrapper, originalWidget );
    this._attachMethods( wrapper, originalWidget );
    this._attach$el( wrapper, originalWidget );
    if( originalWidget instanceof rwt.qx.Object ) {
      originalWidget.addEventListener( "dispose", function() {
        rwt.scripting.WidgetProxyFactory._disposeWidgetProxy( originalWidget );
      } );
    }
  },

  _disposeWidgetProxy : function( widget ) {
    var hash = rwt.qx.Object.toHashCode( widget );
    var proxy = this._wrapperMap[ hash ];
    if( proxy ) {
      var userData = widget.getUserData( HandlerUtil.SERVER_DATA );
      rwt.scripting.WidgetProxyFactory._disposeObject( proxy );
      rwt.scripting.WidgetProxyFactory._disposeObject( userData );
      delete this._wrapperMap[ hash ];
    }
  },

  _disposeObject : function( object ) {
    for( var key in object ) {
      if( object.hasOwnProperty( key ) ) {
        object[ key ] = null;
      }
    }
  },

  _attachSetter : function( proxy, source ) {
    var id = ObjectRegistry.getId( source );
    var handler = id ? ObjectRegistry.getEntry( id ).handler : null;
    if( handler ) {
      var properties = handler.properties || [];
      for( var i = 0; i < properties.length; i++ ) {
        var property = properties[ i ];
        var setterName = this._ALIAS[ property ] || "set" + rwt.util.Strings.toFirstUp( property );
        proxy[ setterName ] = this._createSetter( id, property, source );
      }
    }
  },

  _attachMethods : function( proxy, source ) {
    var id = ObjectRegistry.getId( source );
    var handler = id ? ObjectRegistry.getEntry( id ).handler : null;
    if( WidgetUtil.isWidget( source ) ) {
      bindMethods(source, proxy, HandlerUtil._widgetScriptingMethods);
    }
    if( handler ) {
      bindMethods(source, proxy, handler.scriptingMethods || {});
    }
  },

  /**
   * @name $el
   * @memberOf Widget#
   * @description Instance of {@link $} wrapping the widgets HTML element.
   */
  /**
   * @name $input
   * @memberOf Text#
   * @description Instance of {@link $} wrapping the widgets HTML <code>input</code> element.
   */
  _attach$el : function( proxy, source ) {
    if( WidgetUtil.isWidget( source ) ) {
      proxy.$el = new rwt.util.RWTQuery( source, true );
      if( source.classname === "rwt.widgets.Text" ) {
        proxy.$input = new rwt.util.RWTQuery( source.getInputElement(), true );
      }
    }
  },

  _createSetter : function( id, property, widget ) {
    var setProperty = this._setProperty;
    return function( value ) {
      Synchronizer.enable( widget );
      setProperty( id, property, value );
      Synchronizer.disable( widget );
    };
  },

  _setProperty : function( id, property, value ) {
    var props = {};
    props[ property ] = value;
    rwt.remote.MessageProcessor.processOperation( {
      "target" : id,
      "action" : "set",
      "properties" : props
    } );
  }

};

function bindMethods( source, target, methods ) {
  for( var name in methods ) {
    target[ name ] = rwt.util.Functions.bind( methods[ name ], source );
  }
}

}());
