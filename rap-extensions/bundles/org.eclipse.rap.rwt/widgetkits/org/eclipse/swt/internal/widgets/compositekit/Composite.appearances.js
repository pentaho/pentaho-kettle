/*******************************************************************************
 * Copyright (c) 2007, 2013 Innoopract Informationssysteme GmbH and others.
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

  "composite" : {
    style : function( states ) {
      var result = {};
      var tv = new rwt.theme.ThemeValues( states );
      result.backgroundColor = tv.getCssColor( "Composite", "background-color" );
      result.backgroundImage = tv.getCssImage( "Composite", "background-image" );
      result.backgroundRepeat = tv.getCssIdentifier( "Composite", "background-repeat" );
      result.backgroundPosition = tv.getCssIdentifier( "Composite", "background-position" );
      result.backgroundGradient = tv.getCssGradient( "Composite", "background-image" );
      result.border = tv.getCssBorder( "Composite", "border" );
      result.opacity = tv.getCssFloat( "Composite", "opacity" );
      result.shadow = tv.getCssShadow( "Composite", "box-shadow" );
      result.animation = tv.getCssAnimation( "Composite", "animation" );
      return result;
    }
  }

// END TEMPLATE //
};
