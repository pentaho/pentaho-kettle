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

rwt.qx.Class.define( "rwt.widgets.ToolBar", {

  extend : rwt.widgets.base.Parent,

  construct : function() {
    this.base( arguments );
    this._hoverItem = null;
    this.setAppearance( "toolbar" );
    this.setOverflow( "hidden" );
    this.initTabIndex();
    this.addEventListener( "focus", this._onFocus );
    this.addEventListener( "blur", this._onBlur );
    this.addEventListener( "mouseover", this._onMouseOver );
    this.addEventListener( "keypress", this._onKeyPress );
    this.addEventListener( "keydown", this._onKeyDown );
    this.addEventListener( "keyup", this._onKeyUp );
  },

  members : {

    _applyDirection : function( value ) {
      this.base( arguments, value );
      this.getLayoutImpl().setMirror( value === "rtl" );
      this.forEachChild( function() {
        this.setDirection( value );
      } );
    },

    _isRelevantEvent : function( event ) {
      var target = event.getTarget();
      return this._isToolItem( target ) || target === this;
    },

    _isToolItem : function( item ) {
      return item instanceof rwt.widgets.ToolItem && item.getParent() === this;
    },

    _onMouseOver : function( event ) {
      var target = event.getTarget();
      if( this._hoverItem != null && this._hoverItem != target ) {
        this._hoverItem = null;
      }
      if( this._isToolItem( target ) ) {
        this._hoverItem = target;
      }
    },

    _onFocus : function() {
      if( this._hoverItem == null ) {
        var child = this.getFirstChild();
        while( child != null && !this._isToolItem( child ) ) {
          child = child.getNextSibling();
        }
        this._hoverItem = child;
      }
      if( this._hoverItem != null ) {
        this._hoverItem.addState( "over" );
      }
    },

    _onBlur : function() {
      if( this._hoverItem != null ) {
        this._hoverItem.removeState( "over" );
      }
    },

    _onKeyPress : function( event ) {
      if( this._isRelevantEvent( event ) ) {
        switch( event.getKeyIdentifier() ) {
          case "Left":
            this._hoverNext( true );
          break;
          case "Right":
            this._hoverNext( false );
          break;
        }
      }
    },

    _onKeyDown : function( event ) {
      if( this._hoverItem != null && this._isRelevantEvent( event ) ) {
        this._hoverItem._onKeyDown( event );
      }
    },

    _onKeyUp : function( event ) {
      if( this._hoverItem != null && this._isRelevantEvent( event ) ) {
        this._hoverItem._onKeyUp( event );
      }
    },

    _hoverNext : function( backwards ) {
      if( this._hoverItem != null ) {
        var oldHoverItem = this._hoverItem;
        this._hoverItem.removeState( "over" );
        do {
          if( backwards ) {
            this._hoverItem = this._hoverItem.getPreviousSibling();
            if( this._hoverItem == null ) {
              this._hoverItem = this.getLastChild();
            }
          } else {
            this._hoverItem = this._hoverItem.getNextSibling();
            if( this._hoverItem == null ) {
              this._hoverItem = this.getFirstChild();
            }
          }
        } while( !( this._isToolItem( this._hoverItem ) && this._hoverItem.isEnabled() )
                 && this._hoverItem !== oldHoverItem );
        this._hoverItem.addState( "over" );
      }
    }

  }

} );
