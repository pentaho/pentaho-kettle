/*******************************************************************************
 * Copyright (c) 2004, 2014 1&1 Internet AG, Germany, http://www.1und1.de,
 *                          EclipseSource, and others.
 *
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
 * @appearance tab-item
 * @state checked Set by {@link #checked}
 * @state over
 */
rwt.qx.Class.define( "rwt.widgets.TabItem", {

  extend : rwt.widgets.base.MultiCellWidget,

  include : rwt.widgets.util.OverStateMixin,

  construct : function() {
    this.base( arguments, [ "image", "label" ] );
    this.initChecked();
    this.initTabIndex();
    this._rawText = null;
    this._mnemonicIndex = null;
    this.addEventListener( "mousedown", this._onmousedown );
    this.addEventListener( "keydown", this._onkeydown );
    this.addEventListener( "keypress", this._onkeypress );
  },

  properties : {

    appearance : {
      refine : true,
      init : "tab-item"
    },

    tabIndex : {
      refine : true,
      init : 1
    },

    /** If this tab is the currently selected/active one */
    checked : {
      check :"Boolean",
      init : false,
      apply : "_applyChecked",
      event : "changeChecked"
    },

    /** The attached page of this tab */
    page : {
      check : "rwt.widgets.base.TabFolderPage",
      apply : "_applyPage",
      nullable : true
    },

    /**
     * The assigned rwt.widgets.util.RadioManager which handles the switching between registered
     * buttons
     */
    manager : {
      check  : "rwt.widgets.util.RadioManager",
      nullable : true,
      apply : "_applyManager"
    },

    /**
     * The name of the radio group. All the radio elements in a group (registered by the same
     * manager) have the same name (and could have a different value).
     */
    name : {
      check : "String",
      apply : "_applyName"
    }

  },

  members : {

    setText : function( value ) {
      this._rawText = value;
      this._mnemonicIndex = null;
      this._applyText( false );
    },

    setImage : function( value ) {
      if( value === null ) {
        this.setCellContent( 0, null );
        this.setCellDimension( 0, 0, 0 );
      } else {
        this.setCellContent( 0, value[ 0 ] );
        this.setCellDimension( 0, value[ 1 ], value[ 2 ] );
      }
    },

    setMnemonicIndex : function( value ) {
      this._mnemonicIndex = value;
      var mnemonicHandler = rwt.widgets.util.MnemonicHandler.getInstance();
      if( ( typeof value === "number" ) && ( value >= 0 ) ) {
        mnemonicHandler.add( this, this._onMnemonic );
      } else {
        mnemonicHandler.remove( this );
      }
    },

    getMnemonicIndex : function() {
      return this._mnemonicIndex;
    },

    computeBadgePosition : function( size ) {
      return [
        ( size[ 1 ] * -1 ) + this.getPaddingTop() + 1,
        Math.round( this.getPaddingRight() / 2 ),
        "auto",
        "auto"
      ];
    },

    _applyText : function( mnemonic ) {
      if( this._rawText ) {
        var mnemonicIndex = mnemonic ? this._mnemonicIndex : undefined;
        var text = rwt.util.Encoding.escapeText( this._rawText, mnemonicIndex );
        this.setCellContent( 1, text );
      } else {
        this.setCellContent( 1, null );
      }
    },

    _onMnemonic : function( event ) {
      switch( event.type ) {
        case "show":
          this._applyText( true );
        break;
        case "hide":
          this._applyText( false );
        break;
        case "trigger":
          var charCode = this._rawText.toUpperCase().charCodeAt( this._mnemonicIndex );
          if( event.charCode === charCode ) {
            this.setChecked( true );
            event.success = true;
          }
        break;
      }
    },

    _onkeydown : function( event ) {
      var identifier = event.getKeyIdentifier();
      if( identifier === "Enter" || identifier === "Space" ) {
        // there is no toggeling, just make it checked
        this.setChecked( true );
      }
    },

    _onkeypress : function( event ) {
      switch( event.getKeyIdentifier() ) {
        case "Left":
          var vPrev = this.getPreviousActiveSibling();
          if ( vPrev && vPrev !== this ) {
            // we want to enable the outline border, because
            // the user used the keyboard for activation
            delete rwt.widgets.util.FocusHandler.mouseFocus;
            // focus previous tab
            vPrev.setFocused( true );
            // and naturally make it also checked
            vPrev.setChecked( true );
          }
        break;
        case "Right":
          var vNext = this.getNextActiveSibling();
          if( vNext && vNext !== this ) {
            // we want to enable the outline border, because
            // the user used the keyboard for activation
            delete rwt.widgets.util.FocusHandler.mouseFocus;
            // focus next tab
            vNext.setFocused( true );
            // and naturally make it also checked
            vNext.setChecked( true );
          }
        break;
      }
    },

    _renderAppearance : function() {
      if( this.getView() ) {
        this.toggleState( "firstChild", this.isFirstVisibleChild() );
        this.toggleState( "lastChild", this.isLastVisibleChild() );
        this.toggleState( "alignLeft", this.getView().getAlignTabsToLeft() );
        this.toggleState( "alignRight", !this.getView().getAlignTabsToLeft() );
        this.toggleState( "barTop", this.getView().getPlaceBarOnTop() );
        this.toggleState( "barBottom", !this.getView().getPlaceBarOnTop() );
      }
      this.base( arguments );
    },

    getView : function() {
      var parent = this.getParent();
      return parent ? parent.getParent() : null;
    },

    _applyManager : function( value, old ) {
      if( old ) {
        old.remove( this );
      }
      if( value ) {
        value.add( this );
      }
    },

    _applyParent : function( value, old ) {
      this.base( arguments, value, old );
      if ( old ) {
        old.getManager().remove( this );
      }
      if( value ) {
        value.getManager().add( this );
      }
    },

    _applyPage : function( value, old ) {
      if( old ) {
        old.setButton( null );
      }
      if( value ) {
        value.setButton( this );
        if( this.getChecked() ) {
          value.show();
        } else {
          value.hide();
        }
      }
    },

    _applyChecked : function( value ) {
      if( this._hasParent ) {
        var vManager = this.getManager();
        if( vManager ) {
          vManager.handleItemChecked( this, value );
        }
      }
      this.toggleState( "checked", value );
      var vPage = this.getPage();
      if( vPage ) {
        if( this.getChecked() ) {
          vPage.show();
        } else {
          vPage.hide();
        }
      }
      this.setZIndex( value ? 1 : 0 );
      this.addToQueue( "updateBadgePosition" );
    },

    _applyName : function( value ) {
      if( this.getManager() ) {
        this.getManager().setName( value );
      }
    },

    _onmousedown : function() {
      this.setChecked( true );
    }

  },

  destruct : function() {
    this.setMnemonicIndex( null );
  }

} );
