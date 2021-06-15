/*******************************************************************************
 * Copyright (c) 2015 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
/*jshint unused:false */
var appearances = {
// BEGIN TEMPLATE //

  "grid" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        backgroundColor : tv.getCssColor( "Grid", "background-color" ),
        textColor : tv.getCssColor( "Grid", "color" ),
        font : tv.getCssFont( "Grid", "font" ),
        border : tv.getCssBorder( "Grid", "border" )
      };
    }
  },

  "grid-row" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.background = tv.getCssColor( "GridItem", "background-color" );
      result.backgroundImage = tv.getCssImage( "GridItem", "background-image" );
      result.backgroundGradient = tv.getCssGradient( "GridItem", "background-image" );
      result.foreground = tv.getCssColor( "GridItem", "color" );
      result.textDecoration = tv.getCssIdentifier( "GridItem", "text-decoration" );
      result.textShadow = tv.getCssShadow( "GridItem", "text-shadow" );
      result.textOverflow = tv.getCssIdentifier( "GridItem", "text-overflow" );
      return result;
    }
  },

  "grid-row-overlay" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.background = tv.getCssColor( "Grid-RowOverlay", "background-color" );
      result.backgroundAlpha = tv.getCssAlpha( "Grid-RowOverlay", "background-color" );
      result.backgroundImage = tv.getCssImage( "Grid-RowOverlay", "background-image" );
      result.backgroundGradient = tv.getCssGradient( "Grid-RowOverlay", "background-image" );
      result.foreground = tv.getCssColor( "Grid-RowOverlay", "color" );
      return result;
    }
  },

  "grid-row-check-box" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        backgroundImage : tv.getCssImage( "Grid-Checkbox", "background-image" )
      };
    }
  },

  "grid-row-indent" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        backgroundImage : tv.getCssImage( "Grid-Indent", "background-image" )
      };
    }
  },

  "grid-column" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.cursor = "default";
      result.spacing = 2;
      result.textColor = tv.getCssColor( "GridColumn", "color" );
      result.font = tv.getCssFont( "GridColumn", "font" );
      if( states.footer ) {
        //result.backgroundColor = "#efefef"; // this would make it "merged" with scrollbars
        result.backgroundColor = "#dddddd";
        result.backgroundImage = null;
        result.backgroundGradient = null;
      } else {
        result.backgroundColor = tv.getCssColor( "GridColumn", "background-color" );
        result.backgroundImage = tv.getCssImage( "GridColumn", "background-image" );
        result.backgroundGradient = tv.getCssGradient( "GridColumn", "background-image" );
      }
      result.opacity = states.moving ? 0.85 : 1.0;
      result.padding = tv.getCssBoxDimensions( "GridColumn", "padding" );
      var borderColors = [ null, null, null, null ];
      var borderWidths = [ 0, 0, 0, 0 ];
      var borderStyles = [ "solid", "solid", "solid", "solid" ];
      if( !states.dummy && !states.footer ) {
        var gridLineStates = { "vertical" : true, "header" : true };
        var tvGrid = new rwt.theme.ThemeValues( gridLineStates );
        var gridColor = tvGrid.getCssColor( "Grid-GridLine", "color" );
        gridColor = gridColor == "undefined" ? "transparent" : gridColor;
        borderColors[ states.rwt_RIGHT_TO_LEFT ? 3 : 1 ] = gridColor;
        borderWidths[ states.rwt_RIGHT_TO_LEFT ? 3 : 1 ] = 1;
        if( states.moving ) {
          borderColors[ states.rwt_RIGHT_TO_LEFT ? 1 : 3 ] = gridColor;
          borderWidths[ states.rwt_RIGHT_TO_LEFT ? 1 : 3 ] = 1;
        }
      }
      var borderBottom = tv.getCssBorder( "GridColumn", "border-bottom" );
      if( states.footer ) {
        borderWidths[ 0 ] = borderBottom.getWidthBottom();
        borderStyles[ 0 ] = "solid";
        borderColors[ 0 ] = "#000000";
      } else {
        borderWidths[ 2 ] = borderBottom.getWidthBottom();
        borderStyles[ 2 ] = borderBottom.getStyleBottom();
        borderColors[ 2 ] = borderBottom.getColorBottom();
      }
      result.border = new rwt.html.Border( borderWidths, borderStyles, borderColors );
      result.textShadow = tv.getCssShadow( "GridColumn", "text-shadow" );
      result.textOverflow = tv.getCssIdentifier( "GridColumn", "text-overflow" );
      return result;
    }
  },

  "grid-column-sort-indicator" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.backgroundImage = tv.getCssSizedImage( "GridColumn-SortIndicator", "background-image" );
      return result;
    }
  },

  "grid-column-chevron" : {
    style : function( states ) {
      var result = {};
      var path = rwt.remote.Connection.RESOURCE_PATH + "widget/rap/";
      if( states.loading ) {
        result.backgroundImage = [ path + "tree/loading.gif", 16, 16 ];
      } else {
        var source = path + "arrows/chevron-";
        source += states.expanded ? "left" : "right";
        source += states.mouseover ? "-hover" : "";
        source += ".png";
        result.backgroundImage = [ source, 10, 7 ];
      }
      return result;
    }
  },

  "grid-cell" : {
    style : function( states ) {
       var tv = new rwt.theme.ThemeValues( states );
       var result = {};
       result.spacing = tv.getCssDimension( "Grid-Cell", "spacing" );
       result.padding = tv.getCssBoxDimensions( "Grid-Cell", "padding" );
       return result;
    }
  }

// END TEMPLATE //
};
