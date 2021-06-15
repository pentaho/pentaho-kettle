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

  "tree" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        backgroundColor : tv.getCssColor( "Tree", "background-color" ),
        textColor : tv.getCssColor( "Tree", "color" ),
        font : tv.getCssFont( "Tree", "font" ),
        border : tv.getCssBorder( "Tree", "border" )
      };
    }
  },

  "tree-row" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.background = tv.getCssColor( "TreeItem", "background-color" );
      result.backgroundImage = tv.getCssImage( "TreeItem", "background-image" );
      result.backgroundGradient = tv.getCssGradient( "TreeItem", "background-image" );
      result.foreground = tv.getCssColor( "TreeItem", "color" );
      result.textDecoration = tv.getCssIdentifier( "TreeItem", "text-decoration" );
      result.textShadow = tv.getCssShadow( "TreeItem", "text-shadow" );
      result.textOverflow = tv.getCssIdentifier( "TreeItem", "text-overflow" );
      return result;
    }
  },

  "tree-row-overlay" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.background = tv.getCssColor( "Tree-RowOverlay", "background-color" );
      result.backgroundAlpha = tv.getCssAlpha( "Tree-RowOverlay", "background-color" );
      result.backgroundImage = tv.getCssImage( "Tree-RowOverlay", "background-image" );
      result.backgroundGradient = tv.getCssGradient( "Tree-RowOverlay", "background-image" );
      result.foreground = tv.getCssColor( "Tree-RowOverlay", "color" );
      return result;
    }
  },

  "tree-row-check-box" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        backgroundImage : tv.getCssImage( "Tree-Checkbox", "background-image" )
      };
    }
  },

  "tree-row-indent" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        backgroundImage : tv.getCssImage( "Tree-Indent", "background-image" )
      };
    }
  },

  "tree-column" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.cursor = "default";
      result.spacing = 2;
      result.textColor = tv.getCssColor( "TreeColumn", "color" );
      result.font = tv.getCssFont( "TreeColumn", "font" );
      if( states.footer ) {
        //result.backgroundColor = "#efefef"; // this would make it "merged" with scrollbars
        result.backgroundColor = "#dddddd";
        result.backgroundImage = null;
        result.backgroundGradient = null;
      } else {
        result.backgroundColor = tv.getCssColor( "TreeColumn", "background-color" );
        result.backgroundImage = tv.getCssImage( "TreeColumn", "background-image" );
        result.backgroundGradient = tv.getCssGradient( "TreeColumn", "background-image" );
      }
      result.opacity = states.moving ? 0.85 : 1.0;
      result.padding = tv.getCssBoxDimensions( "TreeColumn", "padding" );
      var borderColors = [ null, null, null, null ];
      var borderWidths = [ 0, 0, 0, 0 ];
      var borderStyles = [ "solid", "solid", "solid", "solid" ];
      if( !states.dummy && !states.footer ) {
        var gridLineStates = rwt.util.Objects.copy( states );
        gridLineStates[ "vertical" ] = true;
        gridLineStates[ "header" ] = true;
        var tvGrid = new rwt.theme.ThemeValues( gridLineStates );
        var gridColor = tvGrid.getCssColor( "Tree-GridLine", "color" );
        gridColor = gridColor == "undefined" ? "transparent" : gridColor;
        borderColors[ states.rwt_RIGHT_TO_LEFT ? 3 : 1 ] = gridColor;
        borderWidths[ states.rwt_RIGHT_TO_LEFT ? 3 : 1 ] = 1;
        if( states.moving ) {
          borderColors[ states.rwt_RIGHT_TO_LEFT ? 1 : 3 ] = gridColor;
          borderWidths[ states.rwt_RIGHT_TO_LEFT ? 1 : 3 ] = 1;
        }
      }
      var borderBottom = tv.getCssBorder( "TreeColumn", "border-bottom" );
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
      result.textShadow = tv.getCssShadow( "TreeColumn", "text-shadow" );
      result.textOverflow = tv.getCssIdentifier( "TreeColumn", "text-overflow" );
      return result;
    }
  },

  "tree-column-sort-indicator" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.backgroundImage = tv.getCssSizedImage( "TreeColumn-SortIndicator", "background-image" );
      return result;
    }
  },

  "tree-column-chevron" : {
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

  "tree-cell" : {
    style : function( states ) {
       var tv = new rwt.theme.ThemeValues( states );
       var result = {};
       result.spacing = tv.getCssDimension( "Tree-Cell", "spacing" );
       result.padding = tv.getCssBoxDimensions( "Tree-Cell", "padding" );
       return result;
    }
  }

// END TEMPLATE //
};
