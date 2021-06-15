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

  "spinner" : {
    style : function( states ) {
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      result.font = tv.getCssFont( "Spinner", "font" );
      result.textColor = tv.getCssColor( "Spinner", "color" );
      result.backgroundColor = tv.getCssColor( "Spinner", "background-color" );
      result.border = tv.getCssBorder( "Spinner", "border" );
      result.backgroundGradient = tv.getCssGradient( "Spinner", "background-image" );
      result.shadow = tv.getCssShadow( "Spinner", "box-shadow" );
      return result;
    }
  },

  "spinner-text-field" : {
    style : function( states ) {
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      // [if] Do not apply top/bottom paddings on the client
      var cssPadding = tv.getCssBoxDimensions( "Spinner-Field", "padding" );
      result.paddingRight = cssPadding[ 1 ];
      result.paddingLeft = cssPadding[ 3 ];
      result.textColor = tv.getCssColor( "Spinner", "color" );
      result.textShadow = tv.getCssShadow( "Spinner", "text-shadow" );
      return result;
    }
  },

  "spinner-button-up" : {
    style : function( states ) {
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      result.border = tv.getCssBorder( "Spinner-UpButton", "border" );
      result.width = tv.getCssDimension( "Spinner-UpButton", "width" );
      result.icon = tv.getCssSizedImage( "Spinner-UpButton-Icon", "background-image" );
      if( result.icon === rwt.theme.ThemeValues.NONE_IMAGE ) {
        result.icon = tv.getCssSizedImage( "Spinner-UpButton", "background-image" );
      } else {
        result.backgroundImage = tv.getCssImage( "Spinner-UpButton", "background-image" );
      }
      result.backgroundGradient = tv.getCssGradient( "Spinner-UpButton", "background-image" );
      result.backgroundColor = tv.getCssColor( "Spinner-UpButton", "background-color" );
      result.cursor = tv.getCssCursor( "Spinner-UpButton", "cursor" );
      return result;
    }
  },

  "spinner-button-down" : {
    style : function( states ) {
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      result.border = tv.getCssBorder( "Spinner-DownButton", "border" );
      result.width = tv.getCssDimension( "Spinner-DownButton", "width" );
      result.icon = tv.getCssSizedImage( "Spinner-DownButton-Icon", "background-image" );
      if( result.icon === rwt.theme.ThemeValues.NONE_IMAGE ) {
        result.icon = tv.getCssSizedImage( "Spinner-DownButton", "background-image" );
      } else {
        result.backgroundImage = tv.getCssImage( "Spinner-DownButton", "background-image" );
      }
      result.backgroundGradient = tv.getCssGradient( "Spinner-DownButton", "background-image" );
      result.backgroundColor = tv.getCssColor( "Spinner-DownButton", "background-color" );
      result.cursor = tv.getCssCursor( "Spinner-DownButton", "cursor" );
      return result;
    }
  }

// END TEMPLATE //
};
