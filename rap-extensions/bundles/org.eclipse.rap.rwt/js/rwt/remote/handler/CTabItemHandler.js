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

rwt.remote.HandlerRegistry.add( "rwt.widgets.CTabItem", {

  factory : function( properties ) {
    var result;
    rwt.remote.HandlerUtil.callWithTarget( properties.parent, function( parent ) {
      result = new rwt.widgets.CTabItem( parent, parent.hasState( "rwt_CLOSE" ) );
      parent.addAt( result, properties.index );
      rwt.remote.HandlerUtil.addStatesForStyles( result, properties.style );
      rwt.remote.HandlerUtil.addDestroyableChild( parent, result );
      result.setUserData( "protocolParent", parent );
    } );
    return result;
  },

  destructor : rwt.remote.HandlerUtil.getWidgetDestructor(),

  properties : [
    "bounds",
    "font",
    "text",
    "mnemonicIndex",
    "image",
    "toolTipMarkupEnabled",
    "toolTip",
    "customVariant",
    "showing",
    "showClose",
    "badge",
    "data"
  ],

  propertyHandler : {
    "data" : rwt.remote.HandlerUtil.getControlPropertyHandler( "data" ),
    "bounds" : function( widget, value ) {
      var bounds = value;
      if( widget.getParent().getTabPosition() === "bottom" ) {
        bounds[ 1 ] -= 1;
      }
      bounds[ 3 ] += 1;
      widget.setLeft( bounds[ 0 ] );
      widget.setTop( bounds[ 1 ] );
      widget.setWidth( bounds[ 2 ] );
      widget.setHeight( bounds[ 3 ] );
    },
    "font" : rwt.remote.HandlerUtil.getControlPropertyHandler( "font" ),
    "toolTipMarkupEnabled" : rwt.remote.HandlerUtil.getControlPropertyHandler( "toolTipMarkupEnabled" ),
    "toolTip" : rwt.remote.HandlerUtil.getControlPropertyHandler( "toolTip" ),
    "showing" : function( widget, value ) {
      widget.setVisibility( value );
    },
    "badge" : function( widget, value ) {
      rwt.widgets.util.Badges.setBadge( widget, value );
    }
  }

} );
