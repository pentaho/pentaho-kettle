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

rwt.remote.HandlerRegistry.add( "rwt.widgets.TabItem", {

  factory : function( properties ) {
    var result = rwt.widgets.util.TabUtil.createTabItem( properties.id,
                                                         properties.parent,
                                                         properties.index );

    rwt.remote.HandlerUtil.callWithTarget( properties.parent, function( parent ) {
      rwt.remote.HandlerUtil.addDestroyableChild( parent, result );
      result.setUserData( "protocolParent", parent );
    } );
    return result;
  },

  destructor : function( widget ) {
    var control = widget.getUserData( "control" );
    if( control ) {
      if( !control._isInGlobalDisposeQueue ) {
        control.setParent( null );
      }
      widget.setUserData( "control", null );
    }
    rwt.widgets.util.TabUtil.releaseTabItem( widget );
    var parent = widget.getUserData( "protocolParent" );
    if( parent ) {
      rwt.remote.HandlerUtil.removeDestroyableChild( parent, widget );
    }
  },

  properties : [
    "text",
    "mnemonicIndex",
    "image",
    "control",
    "toolTipMarkupEnabled",
    "toolTip",
    "customVariant",
    "badge",
    "data"
  ],

  propertyHandler : {
    "data" : rwt.remote.HandlerUtil.getControlPropertyHandler( "data" ),
    "control" : function( widget, value ) {
      if( value !== null ) {
        rwt.remote.HandlerUtil.callWithTarget( value, function( control ) {
          var widgetManager = rwt.remote.WidgetManager.getInstance();
          var id = widgetManager.findIdByWidget( widget ) + "pg";
          rwt.remote.HandlerUtil.callWithTarget( id, function( parent ) {
            control.setParent( parent );
            widget.setUserData( "control", control );
          } );
        } );
      }
    },
    "toolTipMarkupEnabled" : rwt.remote.HandlerUtil.getControlPropertyHandler( "toolTipMarkupEnabled" ),
    "toolTip" : rwt.remote.HandlerUtil.getControlPropertyHandler( "toolTip" ),
    "badge" : function( widget, value ) {
      rwt.widgets.util.Badges.setBadge( widget, value );
    }
  }

} );
