/*******************************************************************************
 * Copyright (c) 2012, 2014 EclipseSource and others.
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

 "file-upload" : {
    include : "atom",

    style : function( states ) {
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      result.font = tv.getCssFont( "FileUpload", "font" );
      result.textColor = tv.getCssColor( "FileUpload", "color" );
      result.backgroundColor = tv.getCssColor( "FileUpload", "background-color" );
      result.backgroundImage = tv.getCssImage( "FileUpload", "background-image" );
      result.backgroundRepeat = tv.getCssIdentifier( "FileUpload", "background-repeat" );
      result.backgroundPosition = tv.getCssIdentifier( "FileUpload", "background-position" );
      result.backgroundGradient = tv.getCssGradient( "FileUpload", "background-image" );
      result.border = tv.getCssBorder( "FileUpload", "border" );
      result.spacing = tv.getCssDimension( "FileUpload", "spacing" );
      result.padding = tv.getCssBoxDimensions( "FileUpload", "padding" );
      result.cursor = tv.getCssCursor( "FileUpload", "cursor" );
      result.opacity = tv.getCssFloat( "FileUpload", "opacity" );
      result.textShadow = tv.getCssShadow( "FileUpload", "text-shadow" );
      result.animation = tv.getCssAnimation( "FileUpload", "animation" );
      return result;
    }
  }

// END TEMPLATE //
};
