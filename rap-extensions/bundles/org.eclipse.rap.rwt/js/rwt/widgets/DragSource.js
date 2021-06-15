/*******************************************************************************
 * Copyright (c) 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

namespace( "rwt.widgets" );

rwt.widgets.DragSource = function( control, operations ) {
  this.control = control;
  this.actions = rwt.remote.DNDSupport.getInstance()._operationsToActions( operations );
  this.dataTypes = [];
  rwt.remote.DNDSupport.getInstance().registerDragSource( this );
};

rwt.widgets.DragSource.prototype = {

  dispose : function() {
    rwt.remote.DNDSupport.getInstance().deregisterDragSource( this );
  },

  setTransfer : function( transferTypes ) {
    rwt.remote.DNDSupport.getInstance().setDragSourceTransferTypes( this.control, transferTypes );
  }

};
