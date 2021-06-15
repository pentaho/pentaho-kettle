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

rwt.remote.HandlerRegistry.add( "rwt.widgets.ControlDecorator", {

  factory : function( properties ) {
    var result = new rwt.widgets.ControlDecorator();
    rwt.remote.HandlerUtil.addStatesForStyles( result, properties.style );
    rwt.remote.HandlerUtil.setParent( result, properties.parent );
    // [if] ControlDecorator is destroyded by the protocol and must not be set as destroyable child.
    // See bug 407397
    rwt.remote.HandlerUtil.callWithTarget( properties.parent, function( parent ) {
      rwt.remote.HandlerUtil.removeDestroyableChild( parent, result );
      result.setUserData( "protocolParent", null );
    } );
    return result;
  },

  destructor : rwt.remote.HandlerUtil.getWidgetDestructor(),

  properties : [
    "bounds",
    "markupEnabled",
    "text",
    "image",
    "visible",
    "showHover"
  ],

  propertyHandler : {
    "bounds" : function( widget, value ) {
      widget.setLeft( value[ 0 ] );
      widget.setTop( value[ 1 ] );
      widget.setWidth( value[ 2 ] );
      widget.setHeight( value[ 3 ] );
    },
    "image" : function( widget, value ) {
      if( value === null ) {
        widget.setSource( null );
      } else {
        widget.setSource( value[ 0 ] );
      }
    },
    "visible" : function( widget, value ) {
      widget.setVisibility( value );
    }
  },

  listeners : [ "Selection", "DefaultSelection" ]

} );
