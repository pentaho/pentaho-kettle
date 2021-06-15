/*******************************************************************************
 * Copyright (c) 2004, 2015 1&1 Internet AG, Germany, http://www.1und1.de,
 *                          and EclipseSource
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Remote Application Platform
 ******************************************************************************/

( function( $ ) {

rwt.qx.Class.define( "rwt.widgets.base.Scrollable", {

  extend : rwt.widgets.base.Parent,

  construct : function( clientArea ) {
    this.base( arguments );
    this._ignoreScrollTo = [ -1, -1 ];
    this._clientArea = clientArea;
    this._horzScrollBar = new rwt.widgets.base.ScrollBar( true );
    this._vertScrollBar = new rwt.widgets.base.ScrollBar( false );
    this.$spacer = $( "<div>" ).css( {
      "position" : "absolute",
      "visibility" : "hidden",
      "width" : "1px",
      "height" : "1px"
    } );
    this._blockScrolling = false;
    this._internalChangeFlag = false;
    this.add( this._clientArea );
    this.add( this._horzScrollBar );
    this.add( this._vertScrollBar );
    rwt.widgets.util.ScrollBarsActivator.install( this );
    this._configureScrollBars();
    this._configureClientArea();
    this.__onscroll = rwt.util.Functions.bind( this._onscroll, this );
  },

  destruct : function() {
    var el = this._clientArea._getTargetNode();
    if( el ) {
      el.removeEventListener( "scroll", this.__onscroll, false );
      delete this.__onscroll;
    }
    this._clientArea = null;
    this._horzScrollBar = null;
    this._vertScrollBar = null;
    this.$spacer = null;
  },

  statics : {
    _nativeWidth : null,

    getNativeScrollBarWidth : function() {
      if( this._nativeWidth === null ) {
        var dummy = document.createElement( "div" );
        dummy.style.width = "100px";
        dummy.style.height = "100px";
        dummy.style.overflow = "scroll";
        dummy.style.visibility = "hidden";
        document.body.appendChild( dummy );
        this._nativeWidth = dummy.offsetWidth - dummy.clientWidth;
        document.body.removeChild( dummy );
      }
      return this._nativeWidth;
    },

    adjustScrollLeft : function( widget, scrollLeft ) {
      var Client = rwt.client.Client;
      var horzScrollBar = widget.getHorizontalBar();
      if( widget.getDirection() === "rtl" ) {
        if( Client.isGecko() ) {
          return - scrollLeft;
        } else if( horzScrollBar && ( Client.isWebkit() || Client.isBlink() ) ) {
          return horzScrollBar.getMaximum() - horzScrollBar.getThumb() - scrollLeft;
        }
      }
      return scrollLeft;
    }

  },

  members : {

    /////////
    // Public

    setScrollBarsVisible : function( horizontal, vertical ) {
      this._horzScrollBar.setDisplay( horizontal );
      this._vertScrollBar.setDisplay( vertical );
      this._clientArea.setStyleProperty( "overflowX", horizontal ? "scroll" : "hidden" );
      this._clientArea.setStyleProperty( "overflowY", vertical ? "scroll" : "hidden" );
      // Note: [if] Client area does not change its dimensions after show/hide scrollbars anymore.
      // To hide the native scrollbars schedule clientArea._layoutPost manually.
      this._clientArea.addToQueue( "layout" );
      this._syncSpacer();
      this._layoutX();
      this._layoutY();
    },

    setHBarSelection : function( value ) {
      this._internalChangeFlag = true;
      this._horzScrollBar.setValue( value );
      this._internalChangeFlag = false;
    },

    setVBarSelection : function( value ) {
      this._internalChangeFlag = true;
      this._vertScrollBar.setValue( value );
      this._internalChangeFlag = false;
    },

    setBlockScrolling : function( value ) {
      this._blockScrolling = value;
    },

    getVerticalBar : function() {
      return this._vertScrollBar;
    },

    getHorizontalBar : function() {
      return this._horzScrollBar;
    },

    isVerticalBarVisible : function() {
      return this._vertScrollBar.getDisplay();
    },

    isHorizontalBarVisible : function() {
      return this._horzScrollBar.getDisplay();
    },

    getHorizontalBarHeight : function() {
      return this._horzScrollBar.getDisplay() ? this._horzScrollBar.getHeight() : 0;
    },

    getVerticalBarWidth : function() {
      return this._vertScrollBar.getDisplay() ? this._vertScrollBar.getWidth() : 0;
    },

    /////////
    // Layout

    _configureClientArea : function() {
      this._clientArea.setStyleProperty( "overflowX", "scroll" );
      this._clientArea.setStyleProperty( "overflowY", "scroll" );
      this._clientArea.addEventListener( "create", this._onClientCreate, this );
      this._clientArea.addEventListener( "appear", this._onClientAppear, this );
      // TOOD [tb] : Do this with an eventlistner after fixing Bug 327023
      this._clientArea._layoutPost = rwt.util.Functions.bind( this._onClientLayout, this );
    },

    _configureScrollBars : function() {
      var dragBlocker = function( event ) { event.stopPropagation(); };
      this._horzScrollBar.setZIndex( 1e8 );
      this._horzScrollBar.addEventListener( "dragstart", dragBlocker );
      this._horzScrollBar.addEventListener( "changeValue", this._onHorzScrollBarChangeValue, this );
      this._horzScrollBar.addEventListener( "changeMaximum", this._syncSpacer, this );
      this._vertScrollBar.setZIndex( 1e8 );
      this._vertScrollBar.addEventListener( "dragstart", dragBlocker );
      this._vertScrollBar.addEventListener( "changeValue", this._onVertScrollBarChangeValue, this );
      this._vertScrollBar.addEventListener( "changeMaximum", this._syncSpacer, this );
    },

    _applyDirection : function( value ) {
      this.base( arguments, value );
      this.getLayoutImpl().setMirror( value === "rtl" );
      this._horzScrollBar.setDirection( value );
      this._syncSpacer();
      if( this._isCreated ) {
        this._syncClientArea( true, false );
      }
    },

    _layoutX : function() {
      var clientWidth = this.getWidth() - this.getFrameWidth();
      this._clientArea.setWidth( clientWidth );
      this._clientArea.setLeft( 0 );
      this._vertScrollBar.setLeft( clientWidth - this._vertScrollBar.getWidth() );
      this._horzScrollBar.setLeft( 0 );
      this._horzScrollBar.setWidth( clientWidth - this.getVerticalBarWidth() );
    },

    _layoutY : function() {
      var clientHeight = this.getHeight() - this.getFrameHeight();
      this._clientArea.setTop( 0 );
      this._clientArea.setHeight( clientHeight );
      this._horzScrollBar.setTop( clientHeight - this._horzScrollBar.getHeight() );
      this._vertScrollBar.setTop( 0 );
      this._vertScrollBar.setHeight( clientHeight - this.getHorizontalBarHeight() );
    },

    _onClientCreate : function() {
      this._clientArea.prepareEnhancedBorder();
      this._clientArea.setContainerOverflow( false );
      var node = this._clientArea._getTargetNode();
      node.addEventListener( "scroll", this.__onscroll, false );
      this.$spacer.appendTo( node );
      $( node ).prop( "rwtScrollable", this );
      rwt.html.Scroll.disableScrolling( this._clientArea.getElement() );
    },

    _onClientLayout : ( function() {
      if( rwt.client.Client.isTrident() && rwt.client.Client.getMajor() === 9 ) {
        return function() {
          // NOTE [tb] : there is a bug in IE9 where the scrollbar is substracted from the inner
          //             size of an element, not added. Therefore add the barWidth twice.
          var barWidth = rwt.widgets.base.Scrollable.getNativeScrollBarWidth();
          var node = this._clientArea._getTargetNode();
          var el = this._clientArea.getElement();
          var width = parseInt( el.style.width, 10 );
          var height = parseInt( el.style.height, 10 );
          if( this._vertScrollBar.getDisplay() ) {
            width += ( 2 * barWidth );
          }
          if( this._horzScrollBar.getDisplay() ) {
            height += ( 2 * barWidth );
          }
          node.style.width = width + "px";
          node.style.height = height + "px";
        };
      } else {
        return function() {
          var barWidth = rwt.widgets.base.Scrollable.getNativeScrollBarWidth();
          var node = this._clientArea._getTargetNode();
          var el = this._clientArea.getElement();
          var width = parseInt( el.style.width, 10 );
          var height = parseInt( el.style.height, 10 );
          if( this._vertScrollBar.getDisplay() ) {
            width += barWidth;
          }
          if( this._horzScrollBar.getDisplay() ) {
            height += barWidth;
          }
          node.style.width = width + "px";
          node.style.height = height + "px";
        };
      }
    }() ),

    ////////////
    // Scrolling

    _onHorzScrollBarChangeValue : function() {
      if( this._isCreated ) {
        this._syncClientArea( true, false );
      }
      if( !this._internalChangeFlag ) {
        this.dispatchSimpleEvent( "userScroll", true );
      }
    },

    _onVertScrollBarChangeValue : function() {
      if( this._isCreated ) {
        this._syncClientArea( false, true );
      }
      if( !this._internalChangeFlag ) {
        this.dispatchSimpleEvent( "userScroll", false );
      }
    },

    _onClientAppear : function() {
      this._internalChangeFlag = true;
      this._syncClientArea( true, true );
      this._internalChangeFlag = false;
    },

    _onscroll : function( evt ) {
      try {
        var positionChanged =    this._ignoreScrollTo[ 0 ] !== this._clientArea.getScrollLeft()
                              || this._ignoreScrollTo[ 1 ] !== this._clientArea.getScrollTop();
        if( !this._internalChangeFlag && positionChanged ) {
          this._ignoreScrollTo = [ -1, -1 ];
          rwt.event.EventHandlerUtil.stopDomEvent( evt );
          var blockH = this._blockScrolling || !this._horzScrollBar.getDisplay();
          var blockV = this._blockScrolling || !this._vertScrollBar.getDisplay();
          this._internalChangeFlag = true;
          this._syncClientArea( blockH, blockV );
          this._internalChangeFlag = false;
          this._syncScrollBars();
          if( !this._blockScrolling ) {
            this.dispatchSimpleEvent( "scroll" );
          }
        }
      } catch( ex ) {
        rwt.runtime.ErrorHandler.processJavaScriptError( ex );
      }
    },

    _syncClientArea : function( horz, vert ) {
      if( horz && this._horzScrollBar != null ) {
        var scrollX = this._adjustScrollLeft( this._horzScrollBar.getValue() );
        if( this._clientArea.getScrollLeft() !== scrollX ) {
          this._clientArea.setScrollLeft( scrollX );
        }
        var newScrollLeft = this._clientArea.getScrollLeft();
        this._ignoreScrollTo[ 0 ] = newScrollLeft;
        if( newScrollLeft !== scrollX ) {
          this.addToQueue( "hSync" );
        }
      }
      if( vert && this._vertScrollBar != null ) {
        var scrollY = this._vertScrollBar.getValue();
        if( this._clientArea.getScrollTop() !== scrollY ) {
          this._clientArea.setScrollTop( scrollY );
        }
        var newScrollTop = this._clientArea.getScrollTop();
        this._ignoreScrollTo[ 1 ] = newScrollTop;
        if( newScrollTop !== scrollY ) {
          this.addToQueue( "vSync" );
        }
      }
    },

    _layoutPost : function( changes ) {
      this.base( arguments, changes );
      if( changes.hSync || changes.vSync ) {
        // delay because this is still before the client area might get bigger in the display flush
        rwt.client.Timer.once( function() {
          this._internalChangeFlag = true;
          this._syncClientArea( changes.hSync, changes.vSync );
          this._internalChangeFlag = false;
        }, this, 0 );
      }
    },

    _syncScrollBars : function() {
      var scrollX = this._adjustScrollLeft( this._clientArea.getScrollLeft() );
      this._horzScrollBar.setValue( scrollX );
      var scrollY = this._clientArea.getScrollTop();
      this._vertScrollBar.setValue( scrollY );
    },

    _syncSpacer : function() {
      var isRTL = this.getDirection() === "rtl";
      var posX = this._horzScrollBar.getMaximum() + this.getVerticalBarWidth();
      this.$spacer.css( {
        "top" : this._vertScrollBar.getMaximum() + this.getHorizontalBarHeight(),
        "left" : isRTL ? "" : posX,
        "right" : isRTL ? posX : ""
      } );
    },

    _adjustScrollLeft : function( scrollLeft ) {
      return rwt.widgets.base.Scrollable.adjustScrollLeft( this, scrollLeft );
    }

  }

} );

}( rwt.util._RWTQuery ) );
