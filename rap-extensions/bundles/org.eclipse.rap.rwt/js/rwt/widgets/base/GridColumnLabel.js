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

rwt.qx.Class.define( "rwt.widgets.base.GridColumnLabel", {

  extend : rwt.widgets.base.MultiCellWidget,

  construct : function( baseAppearance ) {
    this.base( arguments, [ "image", "label", "image" ] );
    this._resizeStartX = 0;
    this._inResize = false;
    this._wasResizeOrMoveEvent = false;
    this._feedbackVisible = false;
    this._inMove = false;
    this._hoverEffect = false;
    this._initialLeft = 0;
    this._chevron = null;
    this.setAppearance( baseAppearance + "-column" );
    this._resizeCursor = null;
    this.setHorizontalChildrenAlign( "left" );
    this.setOverflow( "hidden" );
    this.setFlexibleCell( 1 );
    this.expandFlexCell( true );
    this.addEventListener( "elementOver", this._onElementOver, this );
    this.addEventListener( "elementOut", this._onElementOut, this );
    this.addEventListener( "mouseover", this._onMouseOver, this );
    this.addEventListener( "mousemove", this._onMouseMove, this );
    this.addEventListener( "mouseout", this._onMouseOut, this );
    this.addEventListener( "mousedown", this._onMouseDown, this );
    this.addEventListener( "mouseup", this._onMouseUp, this );
    this.addEventListener( "click", this._onClick, this );
  },

  members : {

    setLeft : function( value ) { // TODO : overwriting generated setter can cause stack overflows
      this.base( arguments, value );
      this._hideDragFeedback( true );
    },

    setText : function( value ) {
      var EncodingUtil = rwt.util.Encoding;
      var text = EncodingUtil.escapeText( value, false );
      text = EncodingUtil.replaceNewLines( text, "<br/>" );
      this.setCellContent( 1, text );
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

    setHoverEffect : function( value ) {
      this._hoverEffect = value;
    },

    setResizeCursor : function( value ) {
      this._resizeCursor = value;
    },

    setSortIndicator : function( value ) {
      if( value ) {
        var manager = rwt.theme.AppearanceManager.getInstance();
        var states = {};
        states[ value ] = true;
        if( this._customVariant !== null ) {
          states[ this._customVariant ] = true;
        }
        var styleMap = manager.styleFrom( this.getAppearance() + "-sort-indicator", states );
        var image = styleMap.backgroundImage;
        this.setCellContent( 2, image[ 0 ] );
        this.setCellDimension( 2, image[ 1 ], image[ 2 ] );
      } else {
        this.setCellContent( 2, null );
        this.setCellDimension( 2, 0, 0 );
      }
    },

    setChevron : function( value ) {
      this._chevron = value;
      this._updateChevronImage( false );
    },

    _onElementOver : function( event ) {
      if( this._chevron && event.getDomTarget() === this.getCellNode( 2 ) ) {
        this._updateChevronImage( true );
      }
    },

    _onElementOut : function( event ) {
      if( this._chevron && event.getDomTarget() === this.getCellNode( 2 ) ) {
        this._updateChevronImage( false );
      }
    },

    _updateChevronImage : function( hover ) {
      if( this._chevron ) {
        var manager = rwt.theme.AppearanceManager.getInstance();
        var states = {};
        states[ this._chevron ] = true;
        if( hover ) {
          states[ "mouseover" ] = true;
        }
        var styleMap = manager.styleFrom( this.getAppearance() + "-chevron", states );
        var image = styleMap.backgroundImage;
        this.setCellContent( 2, image[ 0 ] );
        this.setCellDimension( 2, image[ 1 ], image[ 2 ] );
      } else {
        this.setCellContent( 2, null );
        this.setCellDimension( 2, 0, 0 );
      }
    },

    _onMouseOver : function() {
      if( this._hoverEffect && !this._inMove && !this._inResize ) {
        this.addState( "mouseover" );
      }
    },

    _onMouseDown : function( evt ) {
      if( !this._inMove && !this._inResize && evt.getButton() === "left" ) {
        if( this._isResizeLocation( evt.getPageX() ) && this._allowResize() ) {
          this._inResize = true;
          var position = this.getLeft() + this.getWidth();
          this.dispatchSimpleEvent( "showResizeLine", { "position" : position }, true );
          this._resizeStartX = evt.getPageX();
          this.setCapture( true );
          evt.stopPropagation();
          evt.preventDefault();
          rwt.widgets.util.WidgetUtil._fakeMouseEvent( this, "mouseout" );
        } else if( this._allowMove() ) {
          this._inMove = true;
          this.setCapture( true );
          this._initialLeft = evt.getPageX();
          evt.stopPropagation();
          evt.preventDefault();
          rwt.widgets.util.WidgetUtil._fakeMouseEvent( this, "mouseout" );
        }
      }
    },

    _onMouseMove : function( evt ) {
      if( this._inResize ) {
        var position = this.getLeft() + this._getResizeWidth( evt.getPageX() );
        // min column width is 5 px
        if( position < this.getLeft() + 5 ) {
          position = this.getLeft() + 5;
        }
        this.dispatchSimpleEvent( "showResizeLine", { "position" : position }, true );
      } else if( this._inMove ) {
        this.addState( "mouseover" );
        var left = this.getLeft() + this._getMouseOffset( evt );
        this.dispatchSimpleEvent( "showDragFeedback", { "target" : this, "position" : left } );
        this._feedbackVisible = true;
      } else {
        if( this._isResizeLocation( evt.getPageX() ) ) {
          this.getTopLevelWidget().setGlobalCursor( this._resizeCursor );
        } else {
          this.getTopLevelWidget().setGlobalCursor( null );
        }
      }
      evt.stopPropagation();
      evt.preventDefault();
    },

    _onMouseUp : function( evt ) {
      var widgetUtil = rwt.widgets.util.WidgetUtil;
      if( this._inResize ) {
        this.dispatchSimpleEvent( "hideResizeLine", null, true ); // bubbles: handled by grid
        this.getTopLevelWidget().setGlobalCursor( null );
        this.setCapture( false );
        var newWidth = this._getResizeWidth( evt.getPageX() );
        this.dispatchSimpleEvent( "resizeEnd", {
          "target" : this,
          "width" : newWidth
        } );
        this._inResize = false;
        this._wasResizeOrMoveEvent = true;
        evt.stopPropagation();
        evt.preventDefault();
        widgetUtil._fakeMouseEvent( evt.getTarget(), "mouseover" );
      } else if( this._inMove ) {
        this._inMove = false;
        this.setCapture( false );
        this.removeState( "mouseover" );
        if( Math.abs( evt.getPageX() - this._initialLeft ) > 1 ) {
          this._wasResizeOrMoveEvent = true;
          // Fix for bugzilla 306842
          var left = this.getLeft() + this._getMouseOffset( evt );
          this.dispatchSimpleEvent( "moveEnd", { "target" : this, "position" : left } );
        } else {
          this._hideDragFeedback( false );
        }
        evt.stopPropagation();
        evt.preventDefault();
        widgetUtil._fakeMouseEvent( evt.getTarget(), "mouseover" );
      }
    },

    _getMouseOffset : function( mouseEvent ) {
      if( this.getDirection() === "rtl" ) {
        return this._initialLeft - mouseEvent.getPageX();
      }
      return mouseEvent.getPageX() - this._initialLeft;
    },

    _onClick : function( evt ) {
       // Don't send selection event when the onClick was caused by resizing
      if( !this._wasResizeOrMoveEvent ) {
        var data = { "target" : this };
        if( this._chevron ) {
          data.chevron = evt.getDomTarget() === this.getCellNode( 2 );
          if( data.chevron ) {
            this.setChevron( "loading" );
          }
        }
        this.dispatchSimpleEvent( "selected", data );
      }
      this._wasResizeOrMoveEvent = false;
    },

    _onMouseOut : function( evt ) {
      if( !this._inMove ) {
        this.removeState( "mouseover" );
      }
      if( !this._inResize ) {
        this.getTopLevelWidget().setGlobalCursor( null );
        evt.stopPropagation();
        evt.preventDefault();
      }
    },

    _allowResize : function() {
      return this.dispatchSimpleEvent( "resizeStart", { "target" : this } );
    },

    _allowMove : function() {
      return this.dispatchSimpleEvent( "moveStart", { "target" : this } );
    },

    _hideDragFeedback : function( snap ) {
      if( this._feedbackVisible ) {
        this.dispatchSimpleEvent( "hideDragFeedback", {
          "target" : this,
          "snap" : snap
        } );
        this._feedbackVisible = false;
      }
    },

    /** Returns whether the given pageX is within the right 5 pixels of this
     * column */
    _isResizeLocation : function( pageX ) {
      var columnEdge = rwt.html.Location.getLeft( this.getElement() );
      if( this.getDirection() === "rtl" ) {
        return pageX <= columnEdge + 5 && pageX >= columnEdge;
      }
      columnEdge += this.getWidth();
      return pageX >= columnEdge - 5 && pageX <= columnEdge;
    },

    /** Returns the width of the column that is currently being resized */
    _getResizeWidth : function( pageX ) {
      var delta = this._resizeStartX - pageX;
      if( this.getDirection() === "rtl" ) {
        return this.getWidth() + delta;
      }
      return this.getWidth() - delta;
    }

  }
} );
