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

rwt.remote.HandlerRegistry.add( "rwt.widgets.DropTarget", {

  factory : function( properties ) {
    var control = rwt.remote.ObjectRegistry.getObject( properties.control );
    var result = new rwt.widgets.DropTarget( control, properties.style );
    rwt.remote.HandlerUtil.addDestroyableChild( control, result );
    return result;
  },

  destructor : function( target ) {
    rwt.remote.HandlerUtil.removeDestroyableChild( target.control, target );
    target.dispose();
  },

  properties : [ "transfer", "fileDropEnabled" ],

  events : [ "DragEnter", "DragOver", "DragLeave", "DragOperationChanged", "DropAccept" ],

  methods : [ "changeFeedback", "changeDetail", "changeDataType" ],

  methodHandler : {
    "changeFeedback" : function( target, properties ) {
      target.changeFeedback( properties.feedback, properties.flags );
    },
    "changeDetail" : function( target, properties ) {
      target.changeDetail( properties.detail );
    },
    "changeDataType" : function( target, properties ) {
      target.changeDataType( properties.dataType );
    }
  }

} );
