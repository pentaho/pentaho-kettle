/*******************************************************************************
 * Copyright (c) 2004, 2014 1&1 Internet AG, Germany, http://www.1und1.de,
 *                          EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Remote Application Platform
 ******************************************************************************/

/**
 * @appearance tab-view-page
 */
rwt.qx.Class.define( "rwt.widgets.base.TabFolderPage", {

  extend : rwt.widgets.base.Parent,

  construct : function( vButton ) {
    this.base( arguments );
    if( vButton !== undefined ) {
      this.setButton( vButton );
    }
    this.initTop();
    this.initRight();
    this.initBottom();
    this.initLeft();
  },

  properties : {

    top : {
      refine : true,
      init : 0
    },

    right : {
      refine : true,
      init : 0
    },

    bottom : {
      refine : true,
      init : 0
    },

    left : {
      refine : true,
      init : 0
    },

    /**
     * Make element displayed (if switched to true the widget will be created, if needed, too).
     *  Instead of rwt.widgets.base.Widget, the default is false here.
     */
    display : {
      refine: true,
      init : false
    },

    /** The attached tab of this page. */
    button : {
      check : "rwt.widgets.TabItem",
      apply : "_applyButton"
    }

  },

  members : {

    _applyButton : function( value, old ) {
      if( old ) {
        old.setPage( null );
      }
      if( value ) {
        value.setPage( this );
      }
    }

  }

} );
