/*******************************************************************************
 * Copyright (c) 2011, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.remote.HandlerRegistry.add( "rwt.widgets.ExternalBrowser", {

  factory : function() {
    return rwt.widgets.ExternalBrowser.getInstance();
  },

  destructor : rwt.util.Functions.returnTrue,

  methods : [
    "open",
    "close"
  ],

  methodHandler : {
    "open" : function( widget, args ) {
      var styleMap = rwt.remote.HandlerUtil.createStyleMap( args.style );
      var features = "dependent=1,scrollbars=1,resizable=1,";
      var status = "status=".concat( styleMap.STATUS ? 1 : 0, "," );
      var location = "location=".concat( styleMap.LOCATION_BAR ? 1 : 0, "," );
      var toolbar = "toolbar=".concat( styleMap.NAVIGATION_BAR ? 1 : 0, "," );
      var menubar = "menubar=".concat( styleMap.NAVIGATION_BAR ? 1 : 0 );
      features = features.concat( status, location, toolbar, menubar );
      widget.open( args.id, args.url, features );
    },
    "close" : function( widget, args ) {
      widget.close( args.id );
    }
  }

} );
