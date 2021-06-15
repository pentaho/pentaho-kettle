/*******************************************************************************
 * Copyright (c) 2009, 2016 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.Composite", {

  extend : rwt.widgets.base.Parent,

  include : [ rwt.animation.VisibilityAnimationMixin, rwt.widgets.util.OverStateMixin ],

  construct : function() {
    this.base( arguments );
    this.setAppearance( "composite" );
    this.setOverflow( "hidden" );
    this.setHideFocus( true );
    this.setEnableElementFocus( false );
    // Disable scrolling (see bug 345903)
    rwt.widgets.base.Widget.disableScrolling( this );
    this._clientArea = [ 0, 0, 0, 0 ];
  },

  destruct : function() {
    this._clientArea = null;
  },

  members : {

    setClientArea : function( clientArea ) {
      this._clientArea = clientArea;
      this.dispatchSimpleEvent( "clientAreaChanged" );
    },

    getClientArea : function() {
      return this._clientArea.concat();
    },

    _applyDirection : function( value ) {
      this.base( arguments, value );
      this.getLayoutImpl().setMirror( value === "rtl" );
    }

  }

} );
