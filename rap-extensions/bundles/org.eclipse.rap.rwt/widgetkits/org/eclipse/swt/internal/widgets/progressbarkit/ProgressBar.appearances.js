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

  "progressbar" : {
    style : function( states ) {
      var tv = new rwt.theme.ThemeValues( states );
      var result = {};
      result.border = tv.getCssBorder( "ProgressBar", "border" );
      result.backgroundColor = tv.getCssColor( "ProgressBar", "background-color" );
      result.backgroundImage = tv.getCssImage( "ProgressBar", "background-image" );
      result.backgroundGradient = tv.getCssGradient( "ProgressBar", "background-image" );
      result.indicatorColor = tv.getCssColor( "ProgressBar-Indicator", "background-color" );
      result.indicatorImage = tv.getCssImage( "ProgressBar-Indicator", "background-image" );
      result.indicatorGradient = tv.getCssGradient( "ProgressBar-Indicator", "background-image" );
      result.indicatorOpacity = tv.getCssFloat( "ProgressBar-Indicator", "opacity" );
      return result;
    }
  },

  "scrollbar-blocker" : {
    style : function() {
      return {
        backgroundColor : "black",
        opacity : 0.2
      };
    }
  }

// END TEMPLATE //
};
