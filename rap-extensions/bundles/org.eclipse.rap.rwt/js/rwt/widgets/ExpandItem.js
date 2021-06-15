/*******************************************************************************
 * Copyright (c) 2008, 2016 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.ExpandItem", {

  extend : rwt.widgets.base.Parent,

  construct : function( parent ) {
    this.base( arguments );
    this._expandBar = parent;
    this._expanded = false;
    this._header = new rwt.widgets.base.MultiCellWidget( [ "image", "label", "image" ] );
    this._header.setAppearance( "expand-item-header" );
    this._header.setFlexibleCell( 1 );
    this._header.expandFlexCell( true );
    this._header.setTextOverflow( "ellipsis" );
    this._header.setHeight( 24 );
    this._header.addEventListener( "click", this._onClick, this );
    this._header.addEventListener( "mouseover", this._onMouseOver, this );
    this._header.addEventListener( "mouseout", this._onMouseOut, this );
    this.add( this._header );
    // Set the appearance after _header is created
    this.setAppearance( "expand-item" );
    this.setDirection( parent.getDirection() );
  },

  destruct : function() {
    this._disposeObjects( "_header" );
  },

  properties : {

    chevronIcon : {
      themeable : true,
      apply : "_applyChevronIcon"
    }

  },

  members : {

    _getSubWidgets : function() {
      return [ this._header ];
    },

    _applyChevronIcon : function( value ) {
      if( value ) {
        this._header.setCellContent( 2, value[ 0 ] );
        this._header.setCellDimension( 2, value[ 1 ], value[ 2 ] );
      } else {
        this._header.setCellContent( 2, null );
      }
    },

    _applyDirection : function( value ) {
      this.base( arguments, value );
      this._header.setDirection( value );
      this._header.setHorizontalChildrenAlign( value === "rtl" ? "right" : "left" );
    },

    setExpanded : function( expanded ) {
      this._expanded = expanded;
      this.toggleState( "expanded", expanded );
      this._header.toggleState( "expanded", expanded );
    },

    getExpanded : function() {
      return this._expanded;
    },

    setImage : function( image ) {
      if( image ) {
        this._header.setCellContent( 0, image[ 0 ] );
        this._header.setCellDimension( 0, image[ 1 ], image[ 2 ] );
      } else {
        this._header.setCellContent( 0, null );
      }
    },

    setText : function( value ) {
      var text = value;
      var EncodingUtil = rwt.util.Encoding;
      if( !this._expandBar.isMarkupEnabled() ) {
        text = EncodingUtil.escapeText( text, false );
        text = EncodingUtil.replaceNewLines( text, "<br/>" );
      }
      this._header.setCellContent( 1, text );
    },

    setHeaderHeight : function( headerHeight ) {
      this._header.setHeight( headerHeight );
    },

    _onClick : function() {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        this.setExpanded( !this._expanded );
        var connection = rwt.remote.Connection.getInstance();
        connection.getRemoteObject( this ).set( "expanded", this._expanded );
        var eventName = this._expanded ? "Expand" : "Collapse";
        var itemId = rwt.remote.ObjectRegistry.getId( this );
        connection.getRemoteObject( this._expandBar ).notify( eventName, { "item" : itemId } );
      }
    },

    _onMouseOver : function() {
      this.addState( "over" );
    },

    _onMouseOut : function() {
      this.removeState( "over" );
    }

  }

} );
