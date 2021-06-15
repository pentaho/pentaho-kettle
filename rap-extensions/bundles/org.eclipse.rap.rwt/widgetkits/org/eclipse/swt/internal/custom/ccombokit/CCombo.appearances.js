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

  "ccombo" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.border = tv.getCssBorder( "CCombo", "border" );
      result.backgroundColor = tv.getCssColor( "CCombo", "background-color" );
      result.backgroundGradient = tv.getCssGradient( "CCombo", "background-image" );
      result.textColor = tv.getCssColor( "CCombo", "color" );
      result.font = tv.getCssFont( "CCombo", "font" );
      result.shadow = tv.getCssShadow( "CCombo", "box-shadow" );
      return result;
    }
  },

  "ccombo-list" : {
    style : function() {
      return {};
    }
  },

  "ccombo-list-popup" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        border : tv.getCssBorder( "CCombo-List", "border" ),
        shadow : tv.getCssShadow( "CCombo-List", "box-shadow" )
      };
    }
  },

  "ccombo-list-row" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        foreground : tv.getCssColor( "CCombo-List-Item", "color" ),
        background : tv.getCssColor( "CCombo-List-Item", "background-color" ),
        backgroundImage : tv.getCssImage( "CCombo-List-Item", "background-image" ),
        backgroundGradient : tv.getCssGradient( "CCombo-List-Item", "background-image" ),
        textDecoration : tv.getCssIdentifier( "CCombo-List-Item", "text-decoration" ),
        textShadow : tv.getCssShadow( "CCombo-List-Item", "text-shadow" )
      };
    }
  },

  "ccombo-list-row-overlay" : {
    style : function() {
      return {
        foreground : "undefined",
        background : "undefined",
        backgroundImage : null,
        backgroundGradient : null
      };
    }
  },

  "ccombo-list-cell" : {
    style : function( states ) {
       var tv = new rwt.theme.ThemeValues( states );
       return {
         padding : tv.getCssBoxDimensions( "CCombo-List-Item", "padding" )
       };
    }
  },

  "ccombo-field" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.font = tv.getCssFont( "CCombo", "font" );
      // [if] Do not apply top/bottom paddings on the client
      var cssPadding = tv.getCssBoxDimensions( "CCombo-Field", "padding" );
      result.paddingRight = cssPadding[ 1 ];
      result.paddingLeft = cssPadding[ 3 ];
      result.textColor = tv.getCssColor( "CCombo", "color" );
      result.textShadow = tv.getCssShadow( "CCombo", "text-shadow" );
      return result;
    }
  },

  "ccombo-button" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.border = tv.getCssBorder( "CCombo-Button", "border" );
      result.width = tv.getCssDimension( "CCombo-Button", "width" );
      result.icon = tv.getCssSizedImage( "CCombo-Button-Icon", "background-image" );
      if( result.icon === rwt.theme.ThemeValues.NONE_IMAGE_SIZED ) {
        result.icon = tv.getCssSizedImage( "CCombo-Button", "background-image" );
      } else {
        result.backgroundImage = tv.getCssImage( "CCombo-Button", "background-image" );
      }
      result.backgroundGradient = tv.getCssGradient( "CCombo-Button", "background-image" );
      // TODO [rst] rather use button.bgcolor?
      result.backgroundColor = tv.getCssColor( "CCombo-Button", "background-color" );
      result.cursor = tv.getCssCursor( "CCombo-Button", "cursor" );
      return result;
    }
  }

// END TEMPLATE //
};
