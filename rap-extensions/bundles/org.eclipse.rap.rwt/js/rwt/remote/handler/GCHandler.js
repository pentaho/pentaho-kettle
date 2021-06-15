/*******************************************************************************
 * Copyright (c) 2011, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.remote.HandlerRegistry.add( "rwt.widgets.GC", {

  factory : function( properties ) {
    var parent = rwt.remote.ObjectRegistry.getObject( properties.parent );
    var result = rwt.widgets.util.WidgetUtil.getGC( parent );
    rwt.remote.HandlerUtil.addDestroyableChild( parent, result );
    return result;
  },

  destructor : function( gc ) {
    rwt.remote.HandlerUtil.removeDestroyableChild( gc._control, gc );
    gc.dispose();
  },

  methods : [ "init", "draw" ],

  methodHandler : {
    "init" : function( gc, properties ) {
      gc.init(
        properties.x,
        properties.y,
        properties.width,
        properties.height,
        properties.font,
        properties.fillStyle,
        properties.strokeStyle
      );
    },
    "draw" : function( gc, properties ) {
      gc.draw( properties.operations );
    }
  }

} );
