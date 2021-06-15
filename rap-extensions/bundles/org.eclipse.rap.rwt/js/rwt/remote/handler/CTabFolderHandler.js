/*******************************************************************************
 * Copyright (c) 2011, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.remote.HandlerRegistry.add( "rwt.widgets.CTabFolder", {

  factory : function( properties ) {
    var result = new rwt.widgets.CTabFolder();
    rwt.remote.HandlerUtil.addStatesForStyles( result, properties.style );
    result.setUserData( "isControl", true );
    rwt.widgets.CTabFolder.setToolTipTexts.apply( result, properties.toolTipTexts );
    rwt.remote.HandlerUtil.setParent( result, properties.parent );
    return result;
  },

  destructor : rwt.remote.HandlerUtil.getControlDestructor(),

  getDestroyableChildren : rwt.remote.HandlerUtil.getDestroyableChildrenFinder(),

  properties : rwt.remote.HandlerUtil.extendControlProperties( [
    "tabPosition",
    "tabHeight",
    "minMaxState",
    "minimizeBounds",
    "minimizeVisible",
    "maximizeBounds",
    "maximizeVisible",
    "chevronBounds",
    "chevronVisible",
    "unselectedCloseVisible",
    "selection",
    "selectionBackground",
    "selectionForeground",
    "selectionBackgroundImage",
    "selectionBackgroundGradient",
    "borderVisible"
  ] ),

  propertyHandler : rwt.remote.HandlerUtil.extendControlPropertyHandler( {
    "minimizeBounds" : function( widget, value ) {
      widget.setMinButtonBounds.apply( widget, value );
    },
    "minimizeVisible" : function( widget, value ) {
      if( value ) {
        widget.showMinButton();
      } else {
        widget.hideMinButton();
      }
    },
    "maximizeBounds" : function( widget, value ) {
      widget.setMaxButtonBounds.apply( widget, value );
    },
    "maximizeVisible" : function( widget, value ) {
      if( value ) {
        widget.showMaxButton();
      } else {
        widget.hideMaxButton();
      }
    },
    "chevronBounds" : function( widget, value ) {
      widget.setChevronBounds.apply( widget, value );
    },
    "chevronVisible" : function( widget, value ) {
      if( value ) {
        widget.showChevron();
      } else {
        widget.hideChevron();
      }
    },
    "selection" : function( widget, value ) {
      widget.deselectAll();
      rwt.remote.HandlerUtil.callWithTarget( value, function( item ) {
        if( item != null ) {
          item.setSelected( true );
        }
      } );
    },
    "selectionBackground" : function( widget, value ) {
      if( value === null ) {
        widget.setSelectionBackground( null );
      } else {
        widget.setSelectionBackground( rwt.util.Colors.rgbToRgbString( value ) );
      }
    },
    "selectionForeground" : function( widget, value ) {
      if( value === null ) {
        widget.setSelectionForeground( null );
      } else {
        widget.setSelectionForeground( rwt.util.Colors.rgbToRgbString( value ) );
      }
    },
    "selectionBackgroundGradient" : function( widget, value ) {
      var gradient = null;
      if( value ) {
        var colors = value[ 0 ];
        var percents = value[ 1 ];
        var vertical = value[ 2 ];
        gradient = [];
        for( var i = 0; i < colors.length; i++ ) {
          gradient[ i ] = [ percents[ i ] / 100, rwt.util.Colors.rgbToRgbString( colors[ i ] ) ];
        }
        gradient.horizontal = !vertical;
      }
      widget.setSelectionBackgroundGradient( gradient );
    }
  } ),

  events : [ "Folder", "Selection", "DefaultSelection" ],

  listeners : rwt.remote.HandlerUtil.extendControlListeners( [] ),

  listenerHandler : rwt.remote.HandlerUtil.extendControlListenerHandler( {} )

} );
