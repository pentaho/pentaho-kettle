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

  "clabel" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.textColor = tv.getCssColor( "CLabel", "color" );
      result.backgroundColor = tv.getCssColor( "CLabel", "background-color" );
      result.font = tv.getCssFont( "CLabel", "font" );
      if( states.rwt_SHADOW_IN ) {
        result.border = tv.getCssNamedBorder( "thinInset" );
      } else if( states.rwt_SHADOW_OUT ) {
        result.border = tv.getCssNamedBorder( "thinOutset" );
      } else {
        result.border = tv.getCssBorder( "CLabel", "border" );
      }
      result.backgroundImage = tv.getCssImage( "CLabel", "background-image" );
      result.backgroundRepeat = tv.getCssIdentifier( "CLabel", "background-repeat" );
      result.backgroundPosition = tv.getCssIdentifier( "CLabel", "background-position" );
      result.backgroundGradient = tv.getCssGradient( "CLabel", "background-image" );
      result.cursor = tv.getCssCursor( "CLabel", "cursor" );
      result.padding = tv.getCssBoxDimensions( "CLabel", "padding" );
      result.spacing = tv.getCssDimension( "CLabel", "spacing" );
      result.opacity = tv.getCssFloat( "CLabel", "opacity" );
      result.textShadow = tv.getCssShadow( "CLabel", "text-shadow" );
      return result;
    }
  }

// END TEMPLATE //
};
