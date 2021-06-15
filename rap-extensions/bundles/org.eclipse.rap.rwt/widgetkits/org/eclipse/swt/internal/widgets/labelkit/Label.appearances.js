/*******************************************************************************
 * Copyright (c) 2007, 2016 Innoopract Informationssysteme GmbH and others.
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

  "label-wrapper" : {
    style : function( states ) {
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      result.font = tv.getCssFont( "Label", "font" );
      var decoration = tv.getCssIdentifier( "Label", "text-decoration" );
      if( decoration != null && decoration != "none" ) {
        var decoratedFont = new rwt.html.Font();
        decoratedFont.setSize( result.font.getSize() );
        decoratedFont.setFamily( result.font.getFamily() );
        decoratedFont.setBold( result.font.getBold() );
        decoratedFont.setItalic( result.font.getItalic() );
        decoratedFont.setDecoration( decoration );
        result.font = decoratedFont;
      }
      result.textColor = tv.getCssColor( "Label", "color" );
      result.backgroundColor = tv.getCssColor( "Label", "background-color" );
      result.backgroundImage = tv.getCssImage( "Label", "background-image" );
      result.backgroundRepeat = tv.getCssIdentifier( "Label", "background-repeat" );
      result.backgroundPosition = tv.getCssIdentifier( "Label", "background-position" );
      result.backgroundGradient = tv.getCssGradient( "Label", "background-image" );
      result.border = tv.getCssBorder( "Label", "border" );
      result.cursor = tv.getCssCursor( "Label", "cursor" );
      result.opacity = tv.getCssFloat( "Label", "opacity" );
      result.padding = tv.getCssBoxDimensions( "Label", "padding" );
      result.textShadow = tv.getCssShadow( "Label", "text-shadow" );
      return result;
    }
  },

  "separator-line" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      if( states.rwt_VERTICAL ) {
        result.width = tv.getCssDimension( "Label-SeparatorLine", "width" );
      } else {
        result.height = tv.getCssDimension( "Label-SeparatorLine", "width" );
      }
      result.border = tv.getCssBorder( "Label-SeparatorLine", "border" );
      result.backgroundColor = tv.getCssColor( "Label-SeparatorLine", "background-color" );
      result.backgroundImage = tv.getCssImage( "Label-SeparatorLine", "background-image" );
      result.backgroundGradient = tv.getCssGradient( "Label-SeparatorLine", "background-image" );
      return result;
    }
  },

  "separator" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        backgroundColor : tv.getCssColor( "Label", "background-color" ),
        backgroundImage : tv.getCssImage( "Label", "background-image" ),
        backgroundGradient : tv.getCssGradient( "Label", "background-image" ),
        border : tv.getCssBorder( "Label", "border" ),
        cursor : tv.getCssCursor( "Label", "cursor" ),
        opacity : tv.getCssFloat( "Label", "opacity" )
      };
    }
  }

// END TEMPLATE //
};
