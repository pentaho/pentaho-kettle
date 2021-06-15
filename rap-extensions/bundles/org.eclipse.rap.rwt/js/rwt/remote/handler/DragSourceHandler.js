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

rwt.remote.HandlerRegistry.add( "rwt.widgets.DragSource", {

  factory : function( properties ) {
    var control = rwt.remote.ObjectRegistry.getObject( properties.control );
    var result = new rwt.widgets.DragSource( control, properties.style );
    rwt.remote.HandlerUtil.addDestroyableChild( control, result );
    return result;
  },

  destructor : function( source ) {
    rwt.remote.HandlerUtil.removeDestroyableChild( source.control, source );
    source.dispose();
  },

  properties : [ "transfer" ],

  events : [ "DragStart", "DragEnd" ],

  methods : [ "cancel" ],

  methodHandler : {
    "cancel" : function() {
      rwt.remote.DNDSupport.getInstance().cancel();
    }
  }

} );
