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

  "combo" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.border = tv.getCssBorder( "Combo", "border" );
      result.backgroundColor = tv.getCssColor( "Combo", "background-color" );
      result.backgroundGradient = tv.getCssGradient( "Combo", "background-image" );
      result.textColor = tv.getCssColor( "Combo", "color" );
      result.font = tv.getCssFont( "Combo", "font" );
      result.shadow = tv.getCssShadow( "Combo", "box-shadow" );
      return result;
    }
  },

  "combo-list" : {
    style : function() {
      return {};
    }
  },

  "combo-list-popup" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        border : tv.getCssBorder( "Combo-List", "border" ),
        shadow : tv.getCssShadow( "Combo-List", "box-shadow" )
      };
    }
  },

  "combo-list-row" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        foreground : tv.getCssColor( "Combo-List-Item", "color" ),
        background : tv.getCssColor( "Combo-List-Item", "background-color" ),
        backgroundImage : tv.getCssImage( "Combo-List-Item", "background-image" ),
        backgroundGradient : tv.getCssGradient( "Combo-List-Item", "background-image" ),
        textDecoration : tv.getCssIdentifier( "Combo-List-Item", "text-decoration" ),
        textShadow : tv.getCssShadow( "Combo-List-Item", "text-shadow" )
      };
    }
  },

  "combo-list-row-overlay" : {
    style : function() {
      return {
        foreground : "undefined",
        background : "undefined",
        backgroundImage : null,
        backgroundGradient : null
      };
    }
  },

  "combo-list-cell" : {
    style : function( states ) {
       var tv = new rwt.theme.ThemeValues( states );
       return {
         padding : tv.getCssBoxDimensions( "Combo-List-Item", "padding" )
       };
    }
  },

  "combo-field" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.font = tv.getCssFont( "Combo", "font" );
      // [if] Do not apply top/bottom paddings on the client
      var cssPadding = tv.getCssBoxDimensions( "Combo-Field", "padding" );
      result.paddingRight = cssPadding[ 1 ];
      result.paddingLeft = cssPadding[ 3 ];
      result.textColor = tv.getCssColor( "Combo", "color" );
      result.textShadow = tv.getCssShadow( "Combo", "text-shadow" );
      return result;
    }
  },

  "combo-button" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.border = tv.getCssBorder( "Combo-Button", "border" );
      result.width = tv.getCssDimension( "Combo-Button", "width" );
      result.icon = tv.getCssSizedImage( "Combo-Button-Icon", "background-image" );
      if( result.icon === rwt.theme.ThemeValues.NONE_IMAGE_SIZED ) {
        result.icon = tv.getCssSizedImage( "Combo-Button", "background-image" );
      } else {
        result.backgroundImage = tv.getCssImage( "Combo-Button", "background-image" );
      }
      result.backgroundGradient = tv.getCssGradient( "Combo-Button", "background-image" );
      // TODO [rst] rather use button.bgcolor?
      result.backgroundColor = tv.getCssColor( "Combo-Button", "background-color" );
      result.cursor = tv.getCssCursor( "Combo-Button", "cursor" );
      return result;
    }
  }

// END TEMPLATE //
};
