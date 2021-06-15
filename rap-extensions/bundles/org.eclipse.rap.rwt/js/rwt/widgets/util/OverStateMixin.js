/*******************************************************************************
 * Copyright (c) 2014, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.qx.Mixin.define( "rwt.widgets.util.OverStateMixin", {

  construct : function() {
    this.addEventListener( "mouseover", this._onMouseOver, this );
    this.addEventListener( "mouseout", this._onMouseOut, this );
  },

  members : {

    _onMouseOver : function() {
      this.addState( "over" );
    },

    _onMouseOut : function() {
      this.removeState( "over" );
    }

  }

} );
