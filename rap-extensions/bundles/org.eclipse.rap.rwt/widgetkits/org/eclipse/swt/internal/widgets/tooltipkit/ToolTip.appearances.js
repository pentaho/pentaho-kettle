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

  "tool-tip" : {
    include : "popup",

    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.width = tv.getCssDimension( "ToolTip", "width" );
      result.height = tv.getCssDimension( "ToolTip", "height" );
      result.minWidth = 36;
      result.minHeight = 36;
      result.cursor = tv.getCssCursor( "ToolTip", "cursor" );
      result.font = tv.getCssFont( "ToolTip", "font" );
      result.textColor = tv.getCssColor( "ToolTip", "color" );
      result.padding = tv.getCssBoxDimensions( "ToolTip", "padding" );
      result.border = tv.getCssBorder( "ToolTip", "border" );
      result.backgroundColor = tv.getCssColor( "ToolTip", "background-color" );
      result.backgroundImage = tv.getCssImage( "ToolTip", "background-image" );
      result.backgroundGradient = tv.getCssGradient( "ToolTip", "background-image" );
      result.animation = tv.getCssAnimation( "ToolTip", "animation" );
      result.opacity = tv.getCssFloat( "ToolTip", "opacity" );
      result.shadow = tv.getCssShadow( "ToolTip", "box-shadow" );
      return result;
    }
  },

  "tool-tip-image" : {
    include: "image",
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        source : tv.getCssImage( "ToolTip-Image", "background-image" )
      };
    }
  },

  "tool-tip-text" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        font : tv.getCssFont( "ToolTip-Text", "font" ),
        textColor : tv.getCssColor( "ToolTip-Text", "color" ),
        textShadow : tv.getCssShadow( "ToolTip-Text", "text-shadow" )
      };
    }
  },

  "tool-tip-message" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      return {
        font : tv.getCssFont( "ToolTip-Message", "font" ),
        textColor : tv.getCssColor( "ToolTip-Message", "color" ),
        textShadow : tv.getCssShadow( "ToolTip-Message", "text-shadow" )
      };
    }
  }

// END TEMPLATE //
};
