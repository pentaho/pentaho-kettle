/*******************************************************************************
 * Copyright (c) 2007, 2014 Innoopract Informationssysteme GmbH and others.
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

  "button" : {
    include : "atom",

    style : function( states ) {
      // [tb] exists for compatibility with the original qooxdoo button
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      result.font = tv.getCssFont( "Button", "font" );
      var decoration = tv.getCssIdentifier( "Button", "text-decoration" );
      if( decoration != null && decoration !== "none" ) {
        var decoratedFont = new rwt.html.Font();
        decoratedFont.setSize( result.font.getSize() );
        decoratedFont.setFamily( result.font.getFamily() );
        decoratedFont.setBold( result.font.getBold() );
        decoratedFont.setItalic( result.font.getItalic() );
        decoratedFont.setDecoration( decoration );
        result.font = decoratedFont;
      }
      result.textColor = tv.getCssColor( "Button", "color" );
      result.backgroundColor = tv.getCssColor( "Button", "background-color" );
      result.backgroundImage = tv.getCssImage( "Button", "background-image" );
      result.backgroundRepeat = tv.getCssIdentifier( "Button", "background-repeat" );
      result.backgroundPosition = tv.getCssIdentifier( "Button", "background-position" );
      result.backgroundGradient = tv.getCssGradient( "Button", "background-image" );
      result.border = tv.getCssBorder( "Button", "border" );
      result.spacing = tv.getCssDimension( "Button", "spacing" );
      result.padding = tv.getCssBoxDimensions( "Button", "padding" );
      result.cursor = tv.getCssCursor( "Button", "cursor" );
      result.opacity = tv.getCssFloat( "Button", "opacity" );
      result.textShadow = tv.getCssShadow( "Button", "text-shadow" );
      result.shadow = tv.getCssShadow( "Button", "box-shadow" );
      return result;
    }
  },

  "push-button" : {
    include : "button",

    style : function( states ) {
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      result.animation = tv.getCssAnimation( "Button", "animation" );
      if( states.rwt_ARROW ) {
        result.icon = tv.getCssSizedImage( "Button-ArrowIcon", "background-image" );
      }
      return result;
    }
  },

  // ------------------------------------------------------------------------
  // CheckBox

  "check-box" : {
    include : "button",

    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        selectionIndicator : tv.getCssSizedImage( "Button-CheckIcon", "background-image" )
      };
    }
  },


  // ------------------------------------------------------------------------
  // RadioButton

  "radio-button" : {
    include : "button",

    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        selectionIndicator : tv.getCssSizedImage( "Button-RadioIcon", "background-image" )
      };
    }
  }

// END TEMPLATE //
};
