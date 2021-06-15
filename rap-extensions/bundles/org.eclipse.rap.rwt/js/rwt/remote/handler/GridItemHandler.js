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

rwt.remote.HandlerRegistry.add( "rwt.widgets.GridItem", {

  factory : function( properties ) {
    var result;
    rwt.remote.HandlerUtil.callWithTarget( properties.parent, function( parent ) {
      result = rwt.widgets.GridItem.createItem( parent, properties.index );
    } );
    return result;
  },

  destructor : function( item ) {
    var destroyItems = item.getUncachedChildren();
    for( var i = 0; i < destroyItems.length; i++ ) {
      destroyItems[ i ].dispose();
    }
    item.dispose();
  },

  getDestroyableChildren : function( widget ) {
    return widget.getCachedChildren();
  },

  properties : [
    "itemCount",
    "texts",
    "images",
    "background",
    "foreground",
    "font",
    "cellBackgrounds",
    "cellForegrounds",
    "cellFonts",
    "expanded",
    "checked",
    "grayed",
    "cellChecked",
    "cellGrayed",
    "cellCheckable",
    "customVariant",
    "height",
    "index",
    "columnSpans",
    "data"
  ],

  propertyHandler : {
    "data" : rwt.remote.HandlerUtil.getControlPropertyHandler( "data" ),
    "background" : function( widget, value ) {
      var background = value == null ? null : rwt.util.Colors.rgbToRgbString( value );
      widget.setBackground( background );
    },
    "foreground" : function( widget, value ) {
      var foreground = value == null ? null : rwt.util.Colors.rgbToRgbString( value );
      widget.setForeground( foreground );
    },
    "font" : function( widget, value ) {
      var font = value == null ? null : rwt.html.Font.fromArray( value ).toCss();
      widget.setFont( font );
    },
    "cellBackgrounds" : function( widget, value ) {
      var backgrounds = [];
      if( value ) {
        for( var i = 0; i < value.length; i++ ) {
          var background = value[ i ] === null ? null : rwt.util.Colors.rgbToRgbString( value[ i ] );
          backgrounds[ i ] = background;
        }
      }
      widget.setCellBackgrounds( backgrounds );
    },
    "cellForegrounds" : function( widget, value ) {
      var foregrounds = [];
      if( value ) {
        for( var i = 0; i < value.length; i++ ) {
          var foreground = value[ i ] === null ? null : rwt.util.Colors.rgbToRgbString( value[ i ] );
          foregrounds[ i ] = foreground;
        }
      }
      widget.setCellForegrounds( foregrounds );
    },
    "cellFonts" : function( widget, value ) {
      var fonts = [];
      if( value ) {
        for( var i = 0; i < value.length; i++ ) {
          var font = value[ i ] === null ? "" : rwt.html.Font.fromArray( value[ i ] ).toCss();
          fonts[ i ] = font;
        }
      }
      widget.setCellFonts( fonts );
    },
    "customVariant" : function( widget, value ) {
      widget.setVariant( value );
    }
  },

  methods : [ "clear" ]

} );
