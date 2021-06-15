/*******************************************************************************
 * Copyright (c) 2011, 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.remote.HandlerRegistry.add( "rwt.widgets.ToolItem", {

  factory : function( properties ) {
    var styleMap = rwt.remote.HandlerUtil.createStyleMap( properties.style );
    var type = "separator";
    if( styleMap.PUSH ) {
      type = "push";
    } else if( styleMap.CHECK ) {
      type = "check";
    } else if( styleMap.RADIO ) {
      type = "radio";
    } else if( styleMap.DROP_DOWN ) {
      type = "dropDown";
    }
    var result;
    rwt.remote.HandlerUtil.callWithTarget( properties.parent, function( toolbar ) {
      var vertical = toolbar.hasState( "rwt_VERTICAL" );
      if( type === "separator" ) {
        result = new rwt.widgets.ToolItemSeparator( toolbar.hasState( "rwt_FLAT" ), vertical );
      } else {
        result = new rwt.widgets.ToolItem( type, vertical );
        result.setNoRadioGroup( toolbar.hasState( "rwt_NO_RADIO_GROUP" ) );
      }
      toolbar.addAt( result, properties.index );
      rwt.remote.HandlerUtil.addDestroyableChild( toolbar, result );
      result.setUserData( "protocolParent", toolbar );
    } );
    rwt.remote.HandlerUtil.addStatesForStyles( result, properties.style );
    return result;
  },

  destructor : rwt.remote.HandlerUtil.getWidgetDestructor(),

  properties : [
    "bounds",
    "visible",
    "enabled",
    "customVariant",
    "toolTipMarkupEnabled",
    "toolTip",
    "text",
    "mnemonicIndex",
    "image",
    "hotImage",
    "control",
    "selection",
    "badge",
    "data"
  ],

  propertyHandler : {
    "data" : rwt.remote.HandlerUtil.getControlPropertyHandler( "data" ),
    "bounds" : rwt.remote.HandlerUtil.getControlPropertyHandler( "bounds" ),
    "visible" : function( widget, value ) {
      widget.setVisibility( value );
    },
    "toolTipMarkupEnabled" : rwt.remote.HandlerUtil.getControlPropertyHandler( "toolTipMarkupEnabled" ),
    "toolTip" : rwt.remote.HandlerUtil.getControlPropertyHandler( "toolTip" ),
    "image" : function( widget, value ) {
      if( value === null ) {
        widget.setImage( null );
      } else {
        widget.setImage( value[ 0 ], value[ 1 ], value[ 2 ] );
      }
    },
    "hotImage" : function( widget, value ) {
      if( value === null ) {
        widget.setHotImage( null );
      } else {
        widget.setHotImage( value[ 0 ], value[ 1 ], value[ 2 ] );
      }
    },
    "control" : function( widget, value ) {
      widget.setLineVisible( value === null );
    },
    "badge" : function( widget, value ) {
      rwt.widgets.util.Badges.setBadge( widget, value );
    }
  },

  events : [ "Selection" ]

} );
