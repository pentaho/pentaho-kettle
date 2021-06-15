/*******************************************************************************
 * Copyright (c) 2011, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.remote.HandlerRegistry.add( "rwt.widgets.Browser", {

  factory : function( properties ) {
    var result = new rwt.widgets.Browser();
    rwt.remote.HandlerUtil.addStatesForStyles( result, properties.style );
    result.setUserData( "isControl", true );
    rwt.remote.HandlerUtil.setParent( result, properties.parent );
    return result;
  },

  destructor : rwt.remote.HandlerUtil.getControlDestructor(),

  getDestroyableChildren : rwt.remote.HandlerUtil.getDestroyableChildrenFinder(),

  properties : rwt.remote.HandlerUtil.extendControlProperties( [
    "url",
    "functionResult"
  ] ),

  propertyHandler : rwt.remote.HandlerUtil.extendControlPropertyHandler( {
    "url" : function( widget, value ) {
      widget.setSource( value );
      if( widget.isCreated() ) {
        setTimeout( function() { // Delay syncSource, see Bug 474141
          widget.syncSource();
        }, 0 );
      }
    },
    "functionResult" : function( widget, value ) {
      widget.setFunctionResult( value[ 0 ], value[ 1 ], value[ 2 ] );
    }
  } ),

  events : [ "Progress" ],

  listeners : rwt.remote.HandlerUtil.extendControlListeners( [] ),

  listenerHandler : rwt.remote.HandlerUtil.extendControlListenerHandler( {} ),

  methods : [
    "evaluate",
    "destroyFunctions",
    "createFunctions"
  ],

  methodHandler : {
    "evaluate" : function( widget, properties ) {
      widget.execute( properties.script );
    },
    "createFunctions" : function( widget, properties ) {
      var functions = properties.functions;
      for( var i = 0; i < functions.length; i++ ) {
        widget.createFunction( functions[ i ] );
      }
    },
    "destroyFunctions" : function( widget, properties ) {
      var functions = properties.functions;
      for( var i = 0; i < functions.length; i++ ) {
        widget.destroyFunction( functions[ i ] );
      }
    }
  }

} );
