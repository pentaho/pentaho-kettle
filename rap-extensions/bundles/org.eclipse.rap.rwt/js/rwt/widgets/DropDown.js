/*******************************************************************************
 * Copyright (c) 2013, 2017 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

(function() {

  var TAB = String.fromCharCode( 9 );

  var eventTypes = {
    Selection : SWT.Selection,
    DefaultSelection : SWT.DefaultSelection,
    Show : SWT.Show,
    Hide : SWT.Hide
  };

  var forwardedKeys = {
    Enter : true,
    Up : true,
    Down : true,
    PageUp : true,
    PageDown : true,
    Escape : true
  };

  namespace( "rwt.widgets" );

  /**
   * @class Instances of DropDown represent the server-side counterpart of a DropDown widget
   */
  rwt.widgets.DropDown = function( args ) {
    this._ = {};
    this._.hScroll = !!args.hScroll;
    this._.hideTimer = new rwt.client.Timer( 0 );
    this._.hideTimer.addEventListener( "interval", checkFocus, this );
    this._.parent = args.parent;
    this._.appearance = args.appearance;
    this._.customVariant = null;
    this._.styleMap = null;
    this._.popup = createPopup( args.appearance ); // TODO: create on demand
    this._.grid = createGrid( this._.popup, !!args.markupEnabled, args.appearance );
    inheritParentStyling.call( this );
    this._.visibleItemCount = 5;
    this._.items = [];
    this._.columns = null;
    this._.inMouseSelection = false;
    this._.visibility = false;
    this._.minWidth = 0;
    this._.selectionWrapping = true;
    this._.events = createEventsMap();
    addParentListeners.call( this );
    addGridListeners.call( this );
    this._.popup.addEventListener( "appear", onAppear, this );
    this._.parentFocusRoot = args.parent.getFocusRoot();
    this._.parentFocusRoot.addEventListener( "changeFocusedChild", onFocusChange, this );
  };

  rwt.widgets.DropDown.prototype = {

    classname : "rwt.widgets.DropDown",

    setItems : function( items ) {
      this.setSelectionIndex( -1 );
      delete this._.maxTextWidth;
      this._.items = rwt.util.Arrays.copy( items );
      renderGridItems.call( this );
      if( this._.grid.isSeeable() ) {
        renderLayout.call( this );
      }
      if( this._.visibility && items.length > 0 ) {
        this.show();
      } else if( this._.visibility && items.length === 0 ) {
        this._.popup.hide();
      }
    },

    getItems : function() {
      return rwt.util.Arrays.copy( this._.items );
    },

    getItemCount : function() {
      return this._.grid.getRootItem().getChildrenLength();
    },

    /**
     * Not intended to be called by ClientScripting
     */
    setVisibleItemCount : function( itemCount ) {
      this._.visibleItemCount = itemCount;
      if( this._.grid.isSeeable() ) {
        renderLayout.call( this );
      }
      // TODO: hide dropdown completely if no items are visible
    },

    getVisibleItemCount : function() {
      return this._.visibleItemCount;
    },

    setSelectionIndex : function( index ) {
      if( index < -1 || index >= this.getItemCount() || isNaN( index ) ) {
        throw new Error( "Can not select item: Index " + index + " not valid" );
      }
      if( this.getSelectionIndex() === index ) {
        return;
      }
      this._.grid.deselectAll();
      if( index > -1 ) {
        var item = this._.grid.getRootItem().getChild( index );
        this._.grid.selectItem( item );
        this._.grid.setFocusItem( item );
        this._.grid.scrollItemIntoView( item );
      } else {
        this._.grid.setFocusItem( null );
        this._.grid.setTopItemIndex( 0 );
      }
      // Not called for selection changes by API/Server:
      this._.grid.dispatchSimpleEvent( "selectionChanged", { "type" : "selection" } );
    },

    getSelectionIndex : function() {
      var selection = this._.grid.getSelection();
      var result = -1;
      if( selection[ 0 ] ) {
        result = this._.grid.getRootItem().indexOf( selection[ 0 ] );
      }
      return result;
    },

    setVisible : function( value ) {
      if( value ) {
        this.show();
      } else {
        this.hide();
      }
    },

    getVisible : function() {
      return this._.visibility;
    },

    setMinWidth : function( value ) {
      this._.minWidth = value;
      if( this._.grid.isSeeable() ) {
        renderLayout.call( this );
      }
    },

    setSelectionWrapping : function( value ) {
      this._.selectionWrapping = value;
    },

    setCustomVariant : function( value ) {
      this._.customVariant = value;
      this._.styleMap = null;
      this._.popup.setCustomVariant( value );
      this._.grid.setCustomVariant( value );
    },

    setDirection : function( value ) {
      this._.grid.setDirection( value );
    },

    show : function() {
      checkDisposed( this );
      if( !this._.visibility ) {
        this._.visibility = true;
        addMouseWheelEventFilter.call( this );
        fireEvent.call( this, "Show" );
      }
      if( this._.items.length > 0 && this._.parent.isSeeable() && !this._.popup.isSeeable() ) {
        renderLayout.call( this );
        setPopUpVisible.call( this, true );
      }
    },

    hide : function() {
      checkDisposed( this );
      if( this._.visibility ) {
        this._.visibility = false;
        removeMouseWheelEventFilter.call( this );
        fireEvent.call( this, "Hide" );
      }
      setPopUpVisible.call( this, false );
    },

    setData : function( key, value ) {
      if( !this._.widgetData ) {
        this._.widgetData = {};
      }
      if( arguments.length === 1 && key instanceof Object ) {
        rwt.util.Objects.mergeWith( this._.widgetData, key );
      } else {
        this._.widgetData[ key ] = value;
      }
    },

    getData : function( key ) {
      if( !this._.widgetData ) {
        return null;
      }
      var data = this._.widgetData[ key ];
      return data === undefined ? null : data;
    },

    addListener : function( type, listener ) {
      if( this._.events[ type ] ) {
        if( this._.events[ type ].indexOf( listener ) === -1 ) {
          this._.events[ type ].push( listener );
        }
      } else {
        throw new Error( "Unkown type " + type );
      }
    },

    removeListener : function( type, listener ) {
      if( this._ && this._.events[ type ] ) {
        var index = this._.events[ type ].indexOf( listener );
        rwt.util.Arrays.removeAt( this._.events[ type ], index );
      }
    },

    /**
     * Experimental!
     */
    setColumns : function( columns ) {
      this._.columns = columns;
      this._.grid.setColumnCount( columns.length );
      this._.grid.setColumnOrder = function() {};
      var config = this._.grid.getRenderConfig();
      config.cellOrder = new Array( columns.length );
      for( var i = 0; i < columns.length; i++ ) {
        config.cellOrder[ i ] = i;
      }
      renderGridItems.call( this );
      if( this._.grid.isSeeable() ) {
        renderLayout.call( this );
      }
    },

    /**
     * Not intended to be called by ClientScripting
     */
    destroy : function() {
      if( !this.isDisposed() ) {
        var parentFocusRoot = this._.parentFocusRoot;
        if( parentFocusRoot && !parentFocusRoot.isDisposed() ) {
          parentFocusRoot.removeEventListener( "changeFocusedChild", onFocusChange, this );
        }
        if( !this._.grid.isDisposed() ) {
          this._.grid.getRootItem().setItemCount( 0 );
        }
        if( !this._.parent.isDisposed() ) {
          this._.parent.removeEventListener( "appear", onParentVisibilityChange, this );
          this._.parent.removeEventListener( "disappear", onParentVisibilityChange, this );
          this._.parent.removeEventListener( "flush", onParentFlush, this );
          this._.parent.removeEventListener( "keydown", onParentKeyDownEvent, this );
          this._.parent.removeEventListener( "keypress", onParentKeyPressEvent, this );
          this._.parent.removeEventListener( "changeFont", inheritParentStyling, this );
          this._.parent.removeEventListener( "changeTextColor", inheritParentStyling, this );
          this._.parent.removeEventListener( "changeBackgroundColor", inheritParentStyling, this );
          this._.parent.removeEventListener( "changeCursor", inheritParentStyling, this );
        }
        removeMouseWheelEventFilter.call( this );
        this._.popup.destroy();
        this._.hideTimer.dispose();
        if( this._.widgetData ) {
          for( var key in this._.widgetData ) {
            this._.widgetData[ key ] = null;
          }
        }
        for( var key in this._ ) {
          this._[ key ] = null;
        }
        this._ = null;
      }
    },

    isDisposed : function() {
      return this._ === null;
    },

    toString : function() {
      return "DropDown";
    },

    applyObjectId : function( id ) {
      this._.id = id;
      this._.popup.applyObjectId( id );
    }

  };

  ////////////
  // "statics"

  rwt.widgets.DropDown.searchItems = function( items, query, limit ) {
    var resultIndicies = [];
    var filter = function( item, index ) {
      if( query.test( item ) ) {
        resultIndicies.push( index );
        return true;
      } else {
        return false;
      }
    };
    var resultLimit = typeof limit === "number" ? limit : 0;
    var resultItems = filterArray( items, filter, resultLimit );
    return {
      "items" : resultItems,
      "indicies" : resultIndicies,
      "query" : query,
      "limit" : resultLimit
    };
  };

  rwt.widgets.DropDown.createQuery = function( str, caseSensitive, ignorePosition ) {
    var escapedStr = rwt.widgets.DropDown.escapeRegExp( str );
    return new RegExp( ( ignorePosition ? "" : "^" ) + escapedStr, caseSensitive ? "" : "i" );
  };

  rwt.widgets.DropDown.escapeRegExp = function( str ) {
    return str.replace( /[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&" );
  };

  ////////////
  // Internals

  function addParentListeners() {
    this._.parent.addEventListener( "appear", onParentVisibilityChange, this );
    this._.parent.addEventListener( "disappear", onParentVisibilityChange, this );
    this._.parent.addEventListener( "flush", onParentFlush, this );
    this._.parent.addEventListener( "keydown", onParentKeyDownEvent, this );
    this._.parent.addEventListener( "keypress", onParentKeyPressEvent, this );
    this._.parent.addEventListener( "changeFont", inheritParentStyling, this );
    this._.parent.addEventListener( "changeTextColor", inheritParentStyling, this );
    this._.parent.addEventListener( "changeBackgroundColor", inheritParentStyling, this );
    this._.parent.addEventListener( "changeCursor", inheritParentStyling, this );
  }

  function addGridListeners() {
    this._.grid.addEventListener( "create", onCreate, this );
    this._.grid.addEventListener( "selectionChanged", onSelection, this );
    this._.grid.addEventListener( "keypress", onKeyEvent, this );
    this._.grid.addEventListener( "mousedown", onMouseDown, this );
    this._.grid.addEventListener( "mouseup", onMouseUp, this );
    if ( this._.hScroll && !this._.columns ) {
      this._.grid.getRowContainer().addEventListener( "rowRendered", onRowRendered, this );
    }
  }

  function setPopUpVisible( visible ) {
    if( visible ) {
      this._.popup.show();
    } else {
      this._.popup.setVisibility( false ); // makes it disappear immediately
      this._.popup.setDisplay( false ); // forces the popup to appear after all parents are layouted
    }
  }

  function renderLayout() {
    var font = this._.grid.getFont();
    // NOTE: Guessing the lineheight to be 1.3
    var padding = getStyleMap.call( this ).padding;
    var itemHeight = Math.floor( font.getSize() * 1.3 ) + padding[ 0 ] + padding[ 2 ];
    var visibleItems = Math.min( this._.visibleItemCount, this.getItemCount() );
    var gridWidth = calcGridWidth.apply( this );
    var gridHeight = calcGridHeight.apply( this, [ visibleItems, itemHeight ] );
    var itemWidth = computeItemWidth.apply( this, [ gridWidth, padding ] );
    var frameWidth = getStyleMap.call( this ).border.getWidthLeft() * 2;
    this._.popup.setWidth( gridWidth + frameWidth );
    this._.popup.setHeight( gridHeight + frameWidth );
    renderPosition.call( this );
    this._.grid.setDimension( gridWidth, gridHeight );
    renderItemMetrics.apply( this, [ itemHeight, itemWidth, padding ] );
    updateScrollBars.apply( this, [ gridWidth, itemWidth ] );
  }

  function renderPosition() {
    this._.popup.positionRelativeTo( this._.parent, 0, this._.parent.getHeight() );
    var viewportHeight = rwt.html.Viewport.getHeight();
    var scrollTop = rwt.html.Viewport.getScrollTop();
    if( this._.popup.getTop() + this._.popup.getHeight() > viewportHeight + scrollTop ) {
      this._.popup.positionRelativeTo( this._.parent, 0, -1 * this._.popup.getHeight() );
    }
  }

  function calcGridWidth() {
    var frameWidth = getStyleMap.call( this ).border.getWidthLeft() * 2;
    var result = this._.parent.getWidth() - frameWidth;
    if( this._.minWidth > 0 ) {
      var padding = getStyleMap.call( this ).padding;
      var scrollbarWidth = 0;
      if( this._.visibleItemCount < this.getItemCount() ) {
        scrollbarWidth = this._.grid.getVerticalBar().getWidth();
      }
      var preferredWidth = this._.minWidth + padding[ 1 ] + padding[ 3 ] + scrollbarWidth;
      result = Math.max( result, preferredWidth );
    }
    if( this._.columns ) {
      var columnsSum = 0;
      for( var i = 0; i < this._.columns.length; i++ ) {
        columnsSum += this._.columns[ i ];
      }
      if( columnsSum > result ) {
        result = columnsSum;
      }
    }
    return result;
  }

  function calcGridHeight( visibleItems, itemHeight ) {
    var result = visibleItems * itemHeight;
    if ( this._.hScroll ) {
      result += this._.grid.getHorizontalBar().getHeight();
    }
    return result;
  }

  function renderItemMetrics( itemHeight, itemWidth, padding ) {
    this._.grid.setItemHeight( itemHeight );
    if( this._.columns != null ) {
      var left = 0;
      for( var i = 0; i < this._.columns.length; i++ ) {
        var column = this._.columns[ i ];
        this._.grid.setItemMetrics(
          i,  // column
          left, // left
          column, // width
          0, // imageLeft
          0, // imageWidth
          left + padding[ 3 ], // textLeft
          column - padding[ 1 ] - padding[ 3 ], // textWidth
          0, // checkLeft
          0 // checkWith
        );
        left += column;
      }
    } else {
      this._.grid.setItemMetrics(
        0,  // column
        0, // left
        itemWidth, // width
        0, // imageLeft
        0, // imageWidth
        padding[ 3 ], // textLeft
        itemWidth - padding[ 1 ] - padding[ 3 ], // textWidth
        0, // checkLeft
        0 // checkWith
      );
    }
  }

  function computeItemWidth( gridWidth, padding ) {
    var neededWidth = this._.hScroll ? ( this._.maxTextWidth || 0 ) + padding[ 1 ] + padding[ 3 ] : 0;
    return Math.max( neededWidth, gridWidth );
  }

  function renderGridItems() {
    var rootItem = this._.grid.getRootItem();
    var items = this._.items;
    rootItem.setItemCount( 0 );
    rootItem.setItemCount( items.length );
    for( var i = 0; i < items.length; i++ ) {
      var gridItem = new rwt.widgets.GridItem( rootItem, i, false );
      gridItem.applyObjectId( this._.id + "-listitem-" + i );
      if( this._.columns ) {
        gridItem.setTexts( items[ i ].split( TAB ) );
      } else {
        gridItem.setTexts( [ items[ i ] ] );
      }
    }
  }

  function onParentVisibilityChange() {
    if( this._.visibility ) {
      if( this._.parent.isSeeable() ) {
        this.show(); // makes popup visible if items are present and handles layout
      } else {
        setPopUpVisible.call( this, false );
      }
    }
  }

  function onParentKeyDownEvent( event ) {
    // NOTE: This prevents the underlying Shell from closing. Shell is listening for keydown.
    var key = event.getKeyIdentifier();
    if( this._.visibility && ( key === "Enter" || key === "Escape" ) ) {
      event.stopPropagation();
    }
  }

  function onParentKeyPressEvent( event ) {
    var key = event.getKeyIdentifier();
    if( this._.visibility && forwardedKeys[ key ] && !event.isAltPressed() ) {
      event.preventDefault();
      if( this._.selectionWrapping ) {
        selectWithWrapping.call( this, event );
      } else {
        selectWithoutWrapping.call( this, event );
      }
    }
  }

  function selectWithWrapping( event ) {
    var key = event.getKeyIdentifier();
    if( key === "Down" && this.getSelectionIndex() === -1 && this.getItemCount() > 0 ) {
      this.setSelectionIndex( 0 );
    } else if( key === "Up" && this.getSelectionIndex() === 0 ) {
      this.setSelectionIndex( -1 );
    } else if( key === "Down" && this.getSelectionIndex() === this.getItemCount() - 1 ) {
      this.setSelectionIndex( -1 );
    } else if( key === "Up" && this.getSelectionIndex() === -1 && this.getItemCount() > 0 ) {
      this.setSelectionIndex( this.getItemCount() - 1 );
    } else {
      this._.grid.dispatchEvent( event );
    }
  }

  function selectWithoutWrapping( event ) {
    var key = event.getKeyIdentifier();
    var allowSelection = this.getSelectionIndex() === -1 && this.getItemCount() > 0;
    if( ( key === "Down" || key === "PageDown" ) && allowSelection ) {
      this.setSelectionIndex( 0 );
    } else if( ( key === "Up" || key === "PageUp" ) && allowSelection ) {
      this.setSelectionIndex( this.getItemCount() - 1 );
    } else {
      this._.grid.dispatchEvent( event );
    }
  }

  function onParentFlush( event ) {
    var changes = event.getData();
    var layouted = changes.top || changes.left || changes.width || changes.height;
    if( layouted && this._.parent.isInDom() && this._.visibility ) {
      renderLayout.call( this );
    }
  }

  function onKeyEvent( event ) {
    switch( event.getKeyIdentifier() ) {
      case "Enter":
        rwt.client.Timer.once( function() {
          // NOTE : This async call ensures that the key events is processed before the
          //        DefaultSelection event. A better solution would be to do this for all forwarded
          //        key events, but this would be complicated since the event is disposed by the
          //        time dispatch would be called on the grid.
          fireEvent.call( this, "DefaultSelection" );
        }, this, 0 );
      break;
      case "Escape":
        this.hide();
      break;
    }
  }

  function onSelection( event ) {
    if( event.type === "selection" ) {
      fireEvent.call( this, "Selection" );
    }
  }

  function onMouseDown( event ) {
    if( event.getOriginalTarget() instanceof rwt.widgets.base.GridRowContainer ) {
      this._.inMouseSelection = true;
    }
  }

  function onMouseUp( event ) {
    if(    this._.inMouseSelection
        && event.getOriginalTarget() instanceof rwt.widgets.base.GridRowContainer )
    {
      this._.inMouseSelection = false;
      fireEvent.call( this, "DefaultSelection" );
    }
  }

  function onCreate() {
    var selectedItem = this._.grid.getSelection()[ 0 ];
    if( selectedItem ) {
      this._.grid.scrollItemIntoView( selectedItem );
    }
  }

  function onAppear() {
    // NOTE: widget absolute position can change without changing it's relative postion, therefore:
    renderPosition.call( this );
  }

  function onFocusChange() {
    // NOTE : There is no secure way to get the newly focused widget at this point because
    //        it may have another focus root. Therefore we use this timeout and check afterwards:
    this._.hideTimer.start();
  }

  function onRowRendered( row ) {
    var textWidth = row._computeVisualTextWidth( 0 );
    if ( textWidth > ( this._.maxTextWidth || 0 ) ) {
      this._.maxTextWidth = textWidth;
      renderLayout.call( this );
    }
  }

  function fireEvent( type ) {
    var event = {
      "text" : "",
      "index" : -1
    };
    if( type === "Selection" || type === "DefaultSelection" ) {
      var selection = this._.grid.getSelection();
      if( selection.length > 0 ) {
        event.index = this.getSelectionIndex();
        event.text = this._.items[ event.index ];
      }
      notify.apply( this, [ type, event ] );
      if( !rwt.remote.EventUtil.getSuspended() ) { // TODO [tb] : ClientScripting must reset flag
        if( type === "DefaultSelection" && selection.length > 0 ) {
          this.hide();
        }
      }
    } else {
      notify.apply( this, [ type, event ] );
    }
  }

  function checkFocus() {
    this._.hideTimer.stop();
    if( !hasFocus( this._.parent ) && this._.visibility ) {
      this.hide();
    }
  }

  function updateScrollBars( gridWidth, itemWidth ) {
    var vScrollable = this._.visibleItemCount < this.getItemCount();
    var hScrollable = this._.hScroll && gridWidth < itemWidth;
    this._.grid.setScrollBarsVisible( hScrollable, vScrollable );
  }

  function notify( type, event ) {
    var listeners = this._.events[ type ];
    var eventProxy = rwt.util.Objects.mergeWith( {
      "widget" : this,
      "type" : eventTypes[ type ]
    }, event );
    for( var i = 0; i < listeners.length; i++ ) {
      listeners[ i ]( eventProxy );
    }
  }

  function createPopup( appearance ) {
    var result = new rwt.widgets.base.Popup();
    result.addToDocument();
    result.setBackgroundColor( "#ffffff" );
    result.setDisplay( false );
    result.setRestrictToPageOnOpen( false );
    result.setAutoHide( false );
    result.setAppearance( appearance + "-popup" );
    return result;
  }

  function createGrid( parent, markupEnabled, appearance ) {
    var result = new rwt.widgets.Grid( {
      "fullSelection" : true,
      "appearance" : appearance,
      "markupEnabled" : markupEnabled
    } );
    result.setLocation( 0, 0 );
    result.setParent( parent );
    result.setTreeColumn( -1 ); // TODO [tb] : should be default?
    result.setScrollBarsVisible( false, false );
    result.getRenderConfig().focused = true;
    result.addEventListener( "changeFocused", function() {
      result.getRenderConfig().focused = true;
    } );
    return result;
  }

  function inheritParentStyling() {
    this._.grid.setFont( this._.parent.getFont() );
    this._.grid.setTextColor( this._.parent.getTextColor() );
    this._.grid.setBackgroundColor( this._.parent.getBackgroundColor() );
    // [if] "default" fallback is needed to suppress ibeam cursor shown in Chrome and IE, when no
    // cursor (null) is set - bug 434311
    this._.grid.setCursor( this._.parent.getCursor() || "default" );
  }

  function checkDisposed( dropdown ) {
    if( dropdown.isDisposed() ) {
      throw new Error( "DropDown is disposed" );
    }
  }

  function createEventsMap() {
    var result = {};
    for( var key in eventTypes ) {
      result[ key ] = [];
    }
    return result;
  }

  function hasFocus( control ) {
    var root = control.getFocusRoot();
    if( root ) {
      return    control.getFocused()
             || ( control.contains && control.contains( root.getFocusedChild() ) );
    }
    return false;
  }

  function filterArray( arr, func, limit ) {
    var result = [];
    if( typeof arr.filter === "function" && limit === 0 ) {
      result = arr.filter( func );
    } else {
      for( var i = 0; i < arr.length; i++ ) {
        if( func( arr[ i ], i ) ) {
          result.push( arr[ i ] );
          if( limit !== 0 && result.length === limit ) {
            break;
          }
        }
      }
    }
    return result;
  }

  function getStyleMap() {
    if( this._.styleMap == null ) {
      var manager = rwt.theme.AppearanceManager.getInstance();
      var states = {};
      if( this._.customVariant ) {
        states[ this._.customVariant ] = true;
      }
      this._.styleMap = {
        "border" : manager.styleFrom( this._.appearance + "-popup", states ).border,
        "padding" : manager.styleFrom( this._.appearance + "-cell", states ).padding
      };
    }
    return this._.styleMap;
  }

  function addMouseWheelEventFilter() {
    rwt.event.EventHandler.setMouseEventFilter( filterMouseEvent, this );
  }

  function removeMouseWheelEventFilter() {
    var currentFilter = rwt.event.EventHandler.getMouseEventFilter();
    if( currentFilter && currentFilter[ 0 ] === filterMouseEvent && currentFilter[ 1 ] === this ) {
      rwt.event.EventHandler.setMouseEventFilter( null );
    }
  }

  function filterMouseEvent( event ) {
    if( event.getType() === "mousedown" ) {
      var target = event.getTarget();
      if(    target !== this._.popup && !this._.popup.contains( target )
          && target !== this._.parent && !this._.parent.contains( target ) )
      {
        this.hide();
      }
    } else if( event.getType() === "mousewheel" ) {
      event.preventDefault();
      this._.grid.getRowContainer().dispatchEvent( event );
      return false;
    }
    return true;
  }

}() );
