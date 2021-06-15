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

  "text-field" : {
    style : function( states ) {
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      result.font = tv.getCssFont( "Text", "font" );
      result.textColor = tv.getCssColor( "Text", "color" );
      result.backgroundColor = tv.getCssColor( "Text", "background-color" );
      result.backgroundImage = tv.getCssImage( "Text", "background-image" );
      result.backgroundRepeat = tv.getCssIdentifier( "Text", "background-repeat" );
      result.backgroundPosition = tv.getCssIdentifier( "Text", "background-position" );
      result.backgroundGradient = tv.getCssGradient( "Text", "background-image" );
      result.border = tv.getCssBorder( "Text", "border" );
      // [if] Do not apply top/bottom paddings on the client
      var cssPadding = tv.getCssBoxDimensions( "Text", "padding" );
      result.paddingRight = cssPadding[ 1 ];
      result.paddingLeft = cssPadding[ 3 ];
      result.textShadow = tv.getCssShadow( "Text", "text-shadow" );
      result.shadow = tv.getCssShadow( "Text", "box-shadow" );
      return result;
    }
  },

  "text-field-icon" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var selector = states.search ? "Text-Search-Icon" : "Text-Cancel-Icon";
      return {
        icon : tv.getCssSizedImage( selector, "background-image" ),
        spacing : tv.getCssDimension( selector, "spacing" )
      };
    }
  },

  "text-field-message" : {
    style : function( states ) {
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      result.textColor = tv.getCssColor( "Text-Message", "color" );
      // [if] Do not apply top/bottom paddings on the client
      var cssPadding = tv.getCssBoxDimensions( "Text", "padding" );
      result.paddingRight = cssPadding[ 1 ];
      result.paddingLeft = cssPadding[ 3 ];
      result.textShadow = tv.getCssShadow( "Text-Message", "text-shadow" );
      return result;
    }
  },

  "text-area" : {
    include : "text-field",
    style : function( states ) {
      var result = {};
      if( states.rwt_RIGHT_TO_LEFT ) {
        result.padding = [ 0, 3, 0, 0 ];
      } else {
        result.padding = [ 0, 0, 0, 3 ];
      }
      return result;
    }
  }

// END TEMPLATE //
};
