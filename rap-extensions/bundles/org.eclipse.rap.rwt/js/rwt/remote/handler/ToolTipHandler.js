/*******************************************************************************
 * Copyright (c) 2011, 2017 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.remote.HandlerRegistry.add( "rwt.widgets.ToolTip", {

  factory : function( properties ) {
    var styleMap = rwt.remote.HandlerUtil.createStyleMap( properties.style );
    var style = null;
    if( styleMap.ICON_ERROR ) {
      style = "error";
    } else if( styleMap.ICON_WARNING ) {
      style = "warning";
    } else if( styleMap.ICON_INFORMATION ) {
      style = "information";
    }
    var result = new rwt.widgets.ToolTip( style );
    result.setMarkupEnabled( properties.markupEnabled === true );
    rwt.remote.HandlerUtil.addStatesForStyles( result, properties.style );
    rwt.remote.HandlerUtil.callWithTarget( properties.parent, function( parent ) {
      rwt.remote.HandlerUtil.addDestroyableChild( parent, result );
      result.setUserData( "protocolParent", parent );
    } );
    return result;
  },

  destructor : rwt.remote.HandlerUtil.getWidgetDestructor(),

  properties : [
    "customVariant",
    "roundedBorder",
    "backgroundGradient",
    "autoHide",
    "text",
    "message",
    "location",
    // Visible must be set after all other properties
    "visible"
  ],

  propertyHandler : {
    "roundedBorder" : rwt.remote.HandlerUtil.getRoundedBorderHandler(),
    "backgroundGradient" : rwt.remote.HandlerUtil.getBackgroundGradientHandler(),
    "autoHide" : function( widget, value ) {
      widget.setHideAfterTimeout( value );
    },
    "location" : function( widget, value ) {
      widget.setLocation( value[ 0 ], value[ 1 ] );
    }
  },

  events : [ "Selection" ]

} );
