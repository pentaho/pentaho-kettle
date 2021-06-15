/*******************************************************************************
 * Copyright (c) 2007, 2017 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.GridColumn", {

  extend : rwt.qx.Target,
  include : rwt.widgets.util.HtmlAttributesMixin,

  construct : function( grid, isGroup ) {
    this.base( arguments );
    this._grid = grid;
    this._isGroup = isGroup ? true : false;
    this._index = 0;
    this._resizable = isGroup ? false : true;
    this._moveable = false;
    this._alignment = "left";
    this._wordWrap = false;
    this._headerWordWrap = false;
    this._group = null;
    this._left = 0;
    this._height = 0;
    this._visibility = true;
    this._expanded = true;
    this._width = 0;
    this._toolTip = null;
    this._customVariant = null;
    this._text = "";
    this._font = null;
    this._image = null;
    this._footerText = "";
    this._footerFont = null;
    this._footerImage = null;
    this._footerSpan = 1;
    this._sortDirection = null;
    this._showChevron = false;
    this._check = false;
    this._grid.addColumn( this );
  },

  destruct : function() {
    this._grid.removeColumn( this );
    this.dispatchSimpleEvent( "dispose", { target : this } );
  },

  members : {

    setLeft : function( value ) {
      if( rwt.remote.EventUtil.getSuspended() ) {
        this._left = value;
        this._update();
      } else {
        this._sendMove( value );
      }
    },

    getLeft : function() {
      return this._left;
    },

    setWidth : function( value ) {
      if( rwt.remote.EventUtil.getSuspended() ) {
        this._width = value;
        this._update();
      } else {
        this._sendResize( value );
      }
    },

    getWidth : function() {
      return this._width;
    },

    setHeight : function( value ) {
      this._height = value;
      this._update();
    },

    getHeight : function() {
      return this._height;
    },

    setVisibility : function( value ) {
      this._visibility = value;
      this._update();
    },

    getVisibility : function() {
      return this._visibility;
    },

    setExpanded : function( value ) {
      this._expanded = value;
      this._update();
    },

    isExpanded : function() {
      return this._expanded;
    },

    setGroup : function( value ) {
      this._group = value;
    },

    getGroup : function() {
      return this._group;
    },

    setToolTipText : function( value ) {
      this._toolTip = value;
      this._update();
    },

    getToolTipText : function() {
      return this._toolTip;
    },

    setCustomVariant : function( value ) {
      this._customVariant = value;
      this._update();
    },

    getCustomVariant : function() {
      if( this._customVariant ) {
        return this._customVariant;
      }
      return this._grid.getRenderConfig().variant;
    },

    setText : function( value ) {
      this._text = value;
      this._update();
    },

    getText : function() {
      return this._text;
    },

    setImage : function( value ) {
      this._image = value;
      this._update();
    },

    getImage : function( ) {
      return this._image;
    },

    setFont : function( value ) {
      this._font = value ? rwt.html.Font.fromArray( value ) : null;
      this._update();
    },

    getFont : function() {
      return this._font;
    },

    setFooterText : function( value ) {
      this._footerText = value;
      this._update();
    },

    getFooterText : function() {
      return this._footerText;
    },

    setFooterImage : function( value ) {
      this._footerImage = value;
      this._update();
    },

    getFooterImage : function( ) {
      return this._footerImage;
    },

    setFooterFont : function( value ) {
      this._footerFont = value ? rwt.html.Font.fromArray( value ) : null;
      this._update();
    },

    getFooterFont : function() {
      return this._footerFont;
    },

    setFooterSpan : function( span ) {
      this._footerSpan = span;
      this._update();
    },

    getFooterSpan : function() {
      return this._footerSpan;
    },

    getFooterWidth : function() {
      var result = this._width;
      if( this._footerSpan > 1 ) {
        var columnOrder = this._grid.getColumnOrder();
        var start = columnOrder.indexOf( this );
        for( var i = start + 1; i < start + this._footerSpan; i++ ) {
          if( columnOrder[ i ] ) {
            result += columnOrder[ i ].getWidth();
          }
        }
      }
      return result;
    },

    setIndex : function( value ) {
      this._grid.setAlignment( value, this._alignment );
      this._grid.setWordWrap( value, this._wordWrap );
      this._index = value;
      this._update();
    },

    getIndex : function() {
      return this._index;
    },

    setSortDirection : function( value ) {
      this._sortDirection = value;
      this._update();
    },

    getSortDirection : function() {
      return this._sortDirection;
    },

    setResizable : function( value ) {
      this._resizable = value;
    },

    getResizeable : function() {
      return this._resizable;
    },

    setMoveable : function( value ) {
      this._moveable = value;
    },

    getMoveable : function() {
      return this._moveable;
    },

    setCheck : function( value ) {
      this._grid.setCellCheck( this._index, value );
      this._check = value;
    },

    getCheck : function() {
      return this._check;
    },

    handleSelectionEvent : function( event ) {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        if( this._isGroup && event.chevron ) {
          var remoteObject = rwt.remote.Connection.getInstance().getRemoteObject( this );
          remoteObject.set( "expanded", !this._expanded );
          remoteObject.notify( this._expanded ? "Collapse" : "Expand" );
        } else {
          rwt.remote.EventUtil.notifySelected( this );
        }
      }
    },

    setAlignment : function( value ) {
      this._grid.setAlignment( this._index, value );
      this._alignment = value;
      this._update();
    },

    getAlignment : function() {
      return this._alignment;
    },

    setWordWrap : function( value ) {
      this._grid.setWordWrap( this._index, value );
      this._wordWrap = value;
      this._update();
    },

    getWordWrap : function() {
      return this._wordWrap;
    },

    setHeaderWordWrap : function( value ) {
      this._headerWordWrap = value;
      this._update();
    },

    getHeaderWordWrap : function() {
      return this._headerWordWrap;
    },

    setFixed : function( value ) {
      this._fixed = value;
      this._update();
    },

    isFixed : function() {
      return this._fixed;
    },

    isGroup : function() {
      return this._isGroup;
    },

    setShowChevron : function( value ) {
      this._showChevron = value;
    },

    getShowChevron : function() {
      return this._showChevron;
    },

    _update : function() {
      this.dispatchSimpleEvent( "update" );
    },

    _sendResize : function( width ) {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        var serverColumn = rwt.remote.Connection.getInstance().getRemoteObject( this );
        serverColumn.call( "resize", {
          "width" : width
        } );
      }
    },

    _sendMove : function( left ) {
      if( !rwt.remote.EventUtil.getSuspended() ) {
        var serverColumn = rwt.remote.Connection.getInstance().getRemoteObject( this );
        serverColumn.call( "move", {
          "left" : left
        } );
      }
    }

  }
} );
