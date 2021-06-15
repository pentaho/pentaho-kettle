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

rwt.remote.HandlerRegistry.add( "rwt.client.BrowserNavigation", {

  factory : function() {
    return rwt.client.BrowserNavigation.getInstance();
  },

  service : true,

  listeners : [
    "Navigation"
  ],

  methods : [
    "addToHistory"
  ],

  methodHandler : {
    "addToHistory" : function( object, value ) {
      object.addToHistory( value.state, value.title );
    }
  }

} );
