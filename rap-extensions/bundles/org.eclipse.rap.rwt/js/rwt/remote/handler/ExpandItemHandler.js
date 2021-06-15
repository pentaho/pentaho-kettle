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

rwt.remote.HandlerRegistry.add( "rwt.widgets.ExpandItem", {

  factory : function( properties ) {
    var result;
    rwt.remote.HandlerUtil.callWithTarget( properties.parent, function( parent ) {
      result = new rwt.widgets.ExpandItem( parent );
      parent.addWidget( result );
      rwt.remote.HandlerUtil.addDestroyableChild( parent, result );
      result.setUserData( "protocolParent", parent );
    } );
    return result;
  },

  destructor : rwt.remote.HandlerUtil.getWidgetDestructor(),

  properties : [
    "customVariant",
    "bounds",
    "text",
    "image",
    "expanded",
    "headerHeight",
    "data"
  ],

  propertyHandler : {
    "data" : rwt.remote.HandlerUtil.getControlPropertyHandler( "data" ),
    "bounds" : function( widget, value ) {
      widget.setLeft( value[ 0 ] );
      widget.setTop( value[ 1 ] );
      widget.setWidth( value[ 2 ] );
      widget.setHeight( value[ 3 ] );
    }
  }

} );
