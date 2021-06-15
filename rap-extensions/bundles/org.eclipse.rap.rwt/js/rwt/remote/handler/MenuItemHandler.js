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

rwt.remote.HandlerRegistry.add( "rwt.widgets.MenuItem", {

  factory : function( properties ) {
    var result;
    rwt.remote.HandlerUtil.callWithTarget( properties.parent, function( menu ) {
      var styleMap = rwt.remote.HandlerUtil.createStyleMap( properties.style );
      var onMenuBar = menu.hasState( "rwt_BAR" );
      var menuItemType = "push";
      if( styleMap.CASCADE ) {
        menuItemType = "cascade";
      } else if( styleMap.CHECK && !onMenuBar ) {
        menuItemType = "check";
      } else if( styleMap.RADIO && !onMenuBar ) {
        menuItemType = "radio";
      }
      if( styleMap.SEPARATOR ) {
        result = new rwt.widgets.MenuItemSeparator();
      } else {
        result = new rwt.widgets.MenuItem( menuItemType );
        result.setNoRadioGroup( menu.hasState( "rwt_NO_RADIO_GROUP" ) );
        if( onMenuBar ) {
          result.addState( "onMenuBar" );
        }
      }
      menu.addMenuItemAt( result, properties.index );
      rwt.remote.HandlerUtil.addDestroyableChild( menu, result );
      result.setUserData( "protocolParent", menu );
    } );
    rwt.remote.HandlerUtil.addStatesForStyles( result, properties.style );
    return result;
  },

  destructor : rwt.remote.HandlerUtil.getWidgetDestructor(),

  properties : [
    "menu",
    "enabled",
    "text",
    "mnemonicIndex",
    "image",
    "selection",
    "customVariant",
    "data"
  ],

  propertyHandler : {
    "data" : rwt.remote.HandlerUtil.getControlPropertyHandler( "data" ),
    "menu" : function( widget, value ) {
      if( !widget.hasState( "rwt_SEPARATOR" ) ) {
        rwt.remote.HandlerUtil.callWithTarget( value, function( menu ) {
          widget.setMenu( menu );
        } );
      }
    },
    "text" : function( widget, value ) {
      if( !widget.hasState( "rwt_SEPARATOR" ) ) {
        var text = value;
        // Strip accelerator text
        var index = text.indexOf( "\t" );
        var accelerator = null;
        if( index != -1 ) {
          accelerator = text.substring( index + 1 );
          text = text.substring( 0, index );
        }
        widget.setText( text );
        widget.setAccelerator( accelerator );
      }
    },
    "image" : function( widget, value ) {
      if( !widget.hasState( "rwt_SEPARATOR" ) ) {
        if( value === null ) {
          widget.setImage( value );
        } else {
          widget.setImage.apply( widget, value );
        }
      }
    },
    "selection" : function( widget, value ) {
      if( !widget.hasState( "rwt_SEPARATOR" ) ) {
        widget.setSelection( value );
      }
    }
  },

  events : [ "Selection" ],

  listeners : [ "Help" ],

  listenerHandler : {
    "Help" : rwt.remote.HandlerUtil.getControlListenerHandler( "Help" )
  }

} );
