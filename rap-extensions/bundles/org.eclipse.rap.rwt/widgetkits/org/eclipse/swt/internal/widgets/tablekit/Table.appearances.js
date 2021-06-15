/*******************************************************************************
 * Copyright (c) 2007, 2017 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
/*jshint unused:false */
var appearances = {
// BEGIN TEMPLATE //

  "table" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        textColor : tv.getCssColor( "Table", "color" ),
        font : tv.getCssFont( "Table", "font" ),
        border : tv.getCssBorder( "Table", "border" ),
        backgroundColor : tv.getCssColor( "Table", "background-color" ),
        backgroundImage : tv.getCssImage( "Table", "background-image" ),
        backgroundGradient : tv.getCssGradient( "Table", "background-image" )
      };
    }
  },

  "table-column" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {
        cursor : "default",
        spacing : 2,
        opacity : states.moving ? 0.85 : 1.0
      };
      result.padding = tv.getCssBoxDimensions( "TableColumn", "padding" );
      result.textColor = tv.getCssColor( "TableColumn", "color" );
      result.font = tv.getCssFont( "TableColumn", "font" );
      result.backgroundColor = tv.getCssColor( "TableColumn", "background-color" );
      result.backgroundImage = tv.getCssImage( "TableColumn", "background-image" );
      result.backgroundGradient = tv.getCssGradient( "TableColumn", "background-image" );
      var borderColors = [ null, null, null, null ];
      var borderWidths = [ 0, 0, 0, 0 ];
      var borderStyles = [ "solid", "solid", "solid", "solid" ];
      if( !states.dummy ) {
        var gridLineStates = rwt.util.Objects.copy( states );
        gridLineStates[ "vertical" ] = true;
        gridLineStates[ "header" ] = true;
        var tvGrid = new rwt.theme.ThemeValues( gridLineStates );
        var gridColor = tvGrid.getCssColor( "Table-GridLine", "color" );
        gridColor = gridColor == "undefined" ? "transparent" : gridColor;
        borderColors[ states.rwt_RIGHT_TO_LEFT ? 3 : 1 ] = gridColor;
        borderWidths[ states.rwt_RIGHT_TO_LEFT ? 3 : 1 ] = 1;
        if( states.moving ) {
          borderColors[ states.rwt_RIGHT_TO_LEFT ? 1 : 3 ] = gridColor;
          borderWidths[ states.rwt_RIGHT_TO_LEFT ? 1 : 3 ] = 1;
        }
      }
      var borderBottom = tv.getCssBorder( "TableColumn", "border-bottom" );
      borderWidths[ 2 ] = borderBottom.getWidthBottom();
      borderStyles[ 2 ] = borderBottom.getStyleBottom();
      borderColors[ 2 ] = borderBottom.getColorBottom();
      result.border = new rwt.html.Border( borderWidths, borderStyles, borderColors );
      result.textShadow = tv.getCssShadow( "TableColumn", "text-shadow" );
      result.textOverflow = tv.getCssIdentifier( "TableColumn", "text-overflow" );
      return result;
    }
  },

  "table-column-resizer" : {
    style : function() {
      return {
        width : 3,
        opacity : 0.3,
        backgroundColor : "black"
      };
    }
  },

  "table-column-sort-indicator" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.backgroundImage = tv.getCssSizedImage( "TableColumn-SortIndicator", "background-image" );
      return result;
    }
  },

  "table-row" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.background = tv.getCssColor( "TableItem", "background-color" );
      result.backgroundImage = tv.getCssImage( "TableItem", "background-image" );
      result.backgroundGradient = tv.getCssGradient( "TableItem", "background-image" );
      result.foreground = tv.getCssColor( "TableItem", "color" );
      result.textDecoration = tv.getCssIdentifier( "TableItem", "text-decoration" );
      result.textShadow = tv.getCssShadow( "TableItem", "text-shadow" );
      result.textOverflow = tv.getCssIdentifier( "TableItem", "text-overflow" );
      return result;
    }
  },

  "table-row-overlay" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.background = tv.getCssColor( "Table-RowOverlay", "background-color" );
      result.backgroundAlpha = tv.getCssAlpha( "Table-RowOverlay", "background-color" );
      result.backgroundImage = tv.getCssImage( "Table-RowOverlay", "background-image" );
      result.backgroundGradient = tv.getCssGradient( "Table-RowOverlay", "background-image" );
      result.foreground = tv.getCssColor( "Table-RowOverlay", "color" );
      return result;
    }
  },

  "table-row-check-box" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        backgroundImage : tv.getCssImage( "Table-Checkbox", "background-image" )
      };
    }
  },

  "table-gridline-vertical" : {
    style : function() {
      var verticalState = { "vertical" : true };
      var tv = new rwt.theme.ThemeValues( verticalState );
      var gridColor = tv.getCssColor( "Table-GridLine", "color" );
      gridColor = gridColor == "undefined" ? "transparent" : gridColor;
      var result = {};
      result.border = new rwt.html.Border( [ 0, 0, 0, 1 ], "solid", gridColor );
      return result;
    }
  },

  "table-cell" : {
    style : function( states ) {
       var tv = new rwt.theme.ThemeValues( states );
       var result = {};
       result.spacing = tv.getCssDimension( "Table-Cell", "spacing" );
       result.padding = tv.getCssBoxDimensions( "Table-Cell", "padding" );
       return result;
    }
  }

// END TEMPLATE //
};
