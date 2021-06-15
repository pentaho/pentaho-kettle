/*******************************************************************************
 * Copyright (c) 2002, 2017 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.CTabItem", {

  extend : rwt.widgets.base.MultiCellWidget,

  construct : function( parent, canClose ) {
    this.base( arguments, [ "image", "label", "image" ] );
    this._parent = parent;
    // TODO [rst] change when a proper state inheritance concept exists
    if( parent.hasState( "rwt_BORDER" ) ) {
      this.addState( "rwt_BORDER" );
    }
    this.setAppearance( "ctab-item" );
    this.setVerticalChildrenAlign( rwt.widgets.util.Layout.ALIGN_MIDDLE );
    this.setHorizontalChildrenAlign( rwt.widgets.util.Layout.ALIGN_LEFT );
    this.setOverflow( "hidden" );
    this.setTabIndex( null );
    this._selected = false;
    this._showClose = false;
    this._rawText = null;
    this._mnemonicIndex = null;
    this._canClose = canClose;
    this.updateForeground();
    this.updateBackground();
    this.updateBackgroundImage();
    this.updateBackgroundGradient();
    this.setTabPosition( parent.getTabPosition() );
    this.setDirection( parent.getDirection() );
    this.updateCloseButton( false );
    this.addEventListener( "elementOver", this._onElementOver, this );
    this.addEventListener( "elementOut", this._onElementOut, this );
    this.addEventListener( "click", this._onClick, this );
    this.addEventListener( "dblclick", this._onDblClick, this );
    this.addEventListener( "changeParent", this._onChangeParent, this );
    this.addEventListener( "changeLeft", this._onChangeLeft, this );
  },

  destruct : function() {
    this.setMnemonicIndex( null );
  },

  statics : {

    IMG_CLOSE : rwt.remote.Connection.RESOURCE_PATH + "widget/rap/ctabfolder/close.gif",
    IMG_CLOSE_HOVER : rwt.remote.Connection.RESOURCE_PATH + "widget/rap/ctabfolder/close_hover.gif"

  },

  members : {

    setText : function( value ) {
      this._rawText = value;
      this._mnemonicIndex = null;
      this._applyText( false );
      this.dispatchSimpleEvent( "changeText" );
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

    _applyText : function( mnemonic ) {
      if( this._rawText ) {
        var mnemonicIndex = mnemonic ? this._mnemonicIndex : undefined;
        var text = rwt.util.Encoding.escapeText( this._rawText, mnemonicIndex );
        this.setCellContent( 1, text );
      } else {
        this.setCellContent( 1, null );
      }
    },

    setTabPosition : function( tabPosition ) {
      this.toggleState( "barTop", tabPosition === "top" );
    },

    setSelected : function( selected ) {
      if( this._selected !== selected ) {
        this._selected = selected;
        this.toggleState( "selected", selected );
        this._updateNextSelected();
        this.updateForeground();
        this.updateBackground();
        this.updateBackgroundImage();
        this.updateBackgroundGradient();
        this.updateCloseButton( false );
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
            this._parent._notifySelection( this );
            event.success = true;
          }
        break;
      }
    },

    _updateNextSelected : function() {
      var prevItem = null;
      var children = this._parent.getChildren();
      for( var i = 0; i < children.length; i++ ) {
        if( children[ i ].classname === "rwt.widgets.CTabItem" ) {
          children[ i ].removeState( "nextSelected" );
          if( prevItem != null && children[ i ].isSelected() ) {
            prevItem.addState( "nextSelected" );
          }
          prevItem = children[ i ];
        }
      }
    },

    isSelected : function() {
      return this._selected;
    },

    setShowClose : function( value ) {
      this._showClose = value;
      this.updateCloseButton( false );
    },

    computeBadgePosition : function() {
      return [ 0, 0, "auto", "auto" ];
    },

    updateForeground : function() {
      var color = this.isSelected()
                ? this._parent.getSelectionForeground()
                : this._parent.getTextColor();
      if( color != null ) {
        this.setTextColor( color );
      } else {
        this.resetTextColor();
      }
    },

    updateBackground : function() {
      var color = this.isSelected() ? this._parent.getSelectionBackground() : null;
      if( color != null ) {
        this.setBackgroundColor( color );
      } else {
        this.resetBackgroundColor();
      }
    },

    updateBackgroundImage : function() {
      var image = this.isSelected() ? this._parent.getSelectionBackgroundImage() : null;
      if( image != null ) {
        this.setUserData( "backgroundImageSize", image.slice( 1 ) );
        this.setBackgroundImage( image[ 0 ] );
      } else {
        this.resetBackgroundImage();
      }
    },

    updateBackgroundGradient : function() {
      var gradient = this.isSelected() ? this._parent.getSelectionBackgroundGradient() : null;
      if( gradient != null ) {
        this.setBackgroundGradient( gradient );
      } else {
        this.resetBackgroundGradient();
      }
    },

    _onElementOver : function( event ) {
      this.addState( "over" );
      this.updateCloseButton( this._isCloseButtonTarget( event ) );
    },

    _onElementOut : function( event ) {
      if( event.getDomTarget() === this.getElement() ) {
        this.removeState( "over" );
      }
      this.updateCloseButton( false );
    },

    updateCloseButton : function( over ) {
      var visible = false;
      if( this._canClose || this._showClose ) {
        var unselectedVisible = this._parent.getUnselectedCloseVisible() && this.hasState( "over" );
        visible = this.isSelected() || unselectedVisible;
      }
      if( visible ) {
        var image = over ? rwt.widgets.CTabItem.IMG_CLOSE_HOVER : rwt.widgets.CTabItem.IMG_CLOSE;
        this.setCellContent( 2, image );
        this.setCellDimension( 2, 16, 16 );
      } else {
        this.setCellContent( 2, null );
        this.setCellDimension( 2, 0, 0 );
      }
    },

    _onClick : function( event ) {
      if( this._isCloseButtonTarget( event ) ) {
        rwt.remote.Connection.getInstance().getRemoteObject( this._parent ).notify( "Folder", {
          "detail" : "close",
          "item" : rwt.remote.ObjectRegistry.getId( this )
        } );
      } else {
        this._parent._notifySelection( this );
      }
    },

    _onDblClick : function( event ) {
      if( !this._isCloseButtonTarget( event ) ) {
        this._parent._notifyDefaultSelection( this );
      }
    },

    _isCloseButtonTarget : function( event ) {
      return event.getDomTarget() === this.getCellNode( 2 );
    },

    _onChangeParent : function() {
      if( !this._parent._isInGlobalDisposeQueue ) {
        this._updateNextSelected();
      }
    },

    _onChangeLeft : function() {
      this.toggleState( "firstItem", this.getLeft() === 0 );
    }

  }

} );
