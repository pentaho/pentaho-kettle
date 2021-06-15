/*******************************************************************************
 * Copyright (c) 2007, 2015 Innoopract Informationssysteme GmbH and others.
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

  "toolbar" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        font : tv.getCssFont( "ToolBar", "font" ),
        overflow : "hidden",
        border : tv.getCssBorder( "ToolBar", "border" ),
        textColor : tv.getCssColor( "ToolBar", "color" ),
        backgroundColor : tv.getCssColor( "ToolBar", "background-color" ),
        backgroundGradient : tv.getCssGradient( "ToolBar", "background-image" ),
        backgroundImage : tv.getCssImage( "ToolBar", "background-image" ),
        opacity : tv.getCssFloat( "ToolBar", "opacity" )
      };
    }
  },

  "toolbar-separator" : {
    style : function() {
      return {};
    }
  },

  "toolbar-separator-line" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = null;
      if( states.vertical ) {
        result = {
          left : 2,
          height : 2,
          right : 2,
          border : tv.getCssNamedBorder( "verticalDivider" )
        };
      } else {
        result = {
          top : 2,
          width : 2,
          bottom : 2,
          border : tv.getCssNamedBorder( "horizontalDivider" )
        };
      }
      return result;
    }
  },

  "toolbar-button" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {
        cursor : "default",
        overflow : "hidden",
        width : "auto",
        verticalChildrenAlign : "middle"
      };
      result.spacing = tv.getCssDimension( "ToolItem", "spacing" );
      result.animation = tv.getCssAnimation( "ToolItem", "animation" );
      var textColor = tv.getCssColor( "ToolItem", "color" );
      result.textColor = textColor === "undefined" ? "inherit" : textColor;
      result.textShadow = tv.getCssShadow( "ToolItem", "text-shadow" );
      result.backgroundColor = tv.getCssColor( "ToolItem", "background-color" );
      result.opacity = tv.getCssFloat( "ToolItem", "opacity" );
      result.backgroundImage = tv.getCssImage( "ToolItem", "background-image" );
      result.backgroundGradient = tv.getCssGradient( "ToolItem", "background-image" );
      result.border = tv.getCssBorder( "ToolItem", "border" );
      result.padding = tv.getCssBoxDimensions( "ToolItem", "padding" );
      result.vertical = !states.rwt_RIGHT;
      result.horizontalChildrenAlign = ( states.rwt_VERTICAL && states.rwt_RIGHT ) ? "left" : "center";
      if( states.dropDown ) {
        result.dropDownArrow = tv.getCssSizedImage( "ToolItem-DropDownIcon", "background-image" );
        result.separatorBorder = tv.getCssBorder( "ToolItem-DropDownIcon", "border" );
      } else {
        result.dropDownArrow = null;
        result.separatorBorder = null;
      }
      return result;
    }
  }

// END TEMPLATE //
};
