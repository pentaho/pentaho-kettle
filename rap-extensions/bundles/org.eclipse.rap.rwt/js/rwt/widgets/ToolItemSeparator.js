/*******************************************************************************
 * Copyright (c) 2009, 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.ToolItemSeparator", {
  extend : rwt.widgets.base.Parent,

  construct : function( flat, vertical ) {
    this.base( arguments );
    this._line = null;
    this._control = null;
    this._vertical = vertical;
    if( flat ) {
      this._line = new rwt.widgets.base.Terminator();
      if( vertical ) {
        this._line.addState( "vertical" );
      }
      this._line.setAppearance( "toolbar-separator-line" );
      this.add( this._line );
    }
    this.setStyleProperty( "fontSize", "0px" );
    this.setStyleProperty( "lineHeight", "0px" );
  },

  properties : {

    appearance : {
      refine : true,
      init : "toolbar-separator"
    }

  },

  members : {

    _applyWidth : function( newValue, oldValue ) {
      this.base( arguments, newValue, oldValue );
      if( this._line && !this._vertical ) {
        var lineWidth = this._line.getWidth();
        var center = newValue * 0.5;
        var lineLeft = Math.floor( center - ( lineWidth * 0.5 ) );
        this._line.setLeft( lineLeft );
      }
    },

    _applyHeight : function( newValue, oldValue ) {
      this.base( arguments, newValue, oldValue );
      if( this._line && this._vertical ) {
        var lineHeight = this._line.getHeight();
        var center = newValue * 0.5;
        var lineTop = Math.floor( center - ( lineHeight * 0.5 ) );
        this._line.setTop( lineTop );
      }
    },

    setLineVisible : function( value ) {
      if( this._line ) {
        this._line.setVisibility( value );
      }
    }

  }

} );
