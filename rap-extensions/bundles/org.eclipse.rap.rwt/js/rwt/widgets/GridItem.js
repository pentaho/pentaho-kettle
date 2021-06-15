/*******************************************************************************
 * Copyright (c) 2010, 2017 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.GridItem", {

  extend : rwt.qx.Target,
  include : rwt.widgets.util.HtmlAttributesMixin,

  construct : function( parent, index, placeholder ) {
    // Dispose is only needed to remove items from the tree and widget manager.
    // Since it holds no references to the dom, it suffices to dispose tree.
    this._autoDispose = false;
    this.base( arguments );
    this._parent = parent;
    this._level = -1;
    this._height = null;
    this._children = [];
    this._indexCache = {};
    this._visibleChildrenCount = 0;
    this._expandedItems = {};
    this._customHeightItems = {};
    if( placeholder ) {
      this._texts = [ "..." ];
    } else {
      this._cached = true;
    }
    if( this._parent != null ) {
      this._level = this._parent.getLevel() + 1;
      this._parent._add( this, index );
    }
    this._expanded = this.isRootItem();
    this.addEventListener( "update", this._onUpdate, this );
    if( this.isRootItem() ) {
      this._rootItem = this;
      this._height = 16;
    } else {
      this._rootItem = parent.getRootItem();
    }
  },

  destruct : function() {
    if( this._parent != null && !this._parent.isDisposed() ) {
      this._parent._remove( this );
    }
    this._parent = null;
    this._height = null;
    this._children = null;
    this._indexCache = null;
    this._expandedItems = null;
    this._customHeightItems = null;
    delete this._texts;
    delete this._images;
    delete this._font;
    delete this._cellFonts;
    delete this._foreground;
    delete this._cellForegrounds;
    delete this._background;
    delete this._cellBackgrounds;
    delete this._cellChecked;
    delete this._cellGrayed;
    delete this._cellCheckable;
    this._rootItem = null;
    delete this._columnSpans;
  },

  statics : {

    createItem : function( parent, index ) {
      var parentItem = this._getItem( parent );
      var result;
      if( parentItem.isChildCreated( index ) && !parentItem.isChildCached( index ) ) {
        result = parentItem.getChild( index );
        result.markCached();
      } else {
        result = new rwt.widgets.GridItem( parentItem, index, false );
      }
      return result;
    },

    _getItem : function( treeOrItem ) {
      var result;
      if( treeOrItem instanceof rwt.widgets.Grid ) {
        result = treeOrItem.getRootItem();
      } else {
        result = treeOrItem;
      }
      return result;
    }

  },

  members : {

    setItemCount : function( value ) {
      var msg = this._children.length > value ? "remove" : "add";
      this._children.length = value;
      this._update( msg );
    },

    setIndex : function( value ) {
      var siblings = this._parent._children;
      if( siblings.indexOf( this ) !== value ) {
        var target = siblings[ value ];
        siblings[ value ] = this;
        if( target && !target.isCached() ) {
          target.dispose();
        }
      }
    },

    clear : function() {
      // TODO [tb] : children?
      delete this._cached;
      delete this._checked;
      delete this._grayed;
      this._texts = [ "..." ];
      delete this._images;
      delete this._background;
      delete this._foreground;
      delete this._font;
      delete this._cellBackgrounds;
      delete this._cellForegrounds;
      delete this._cellFonts;
      delete this._columnSpans;
      delete this._variant;
    },

    isCached : function() {
      return this._cached || false;
    },

    markCached : function() {
      this._cached = true;
      delete this._texts;
    },

    setTexts : function( texts ) {
      this._texts = texts;
      this._update( "content" );
    },

    getText : function( column ) {
      return ( this._texts ? this._texts[ column ] : undefined ) || "";
    },

    hasText : function( column ) {
      return !!( this._texts ? this._texts[ column ] : undefined );
    },

    setFont : function( font ) {
      this._font = font;
      this._update( "content" );
    },

    getCellFont : function( column ) {
      var result = this._cellFonts ? this._cellFonts[ column ] : null;
      return typeof result === "string" && result !== "" ? result : this._getFont();
    },

    _getFont : function() {
      return this._font || null;
    },

    setCellFonts : function( fonts ) {
      this._cellFonts = fonts;
      this._update( "content" );
    },

    setForeground : function( color ) {
      this._foreground = color;
      this._update( "content" );
    },

    getCellForeground : function( column ) {
      var result = this._cellForegrounds ? this._cellForegrounds[ column ] : null;
      return typeof result === "string" ? result : this._getForeground();
    },

    _getForeground : function() {
      return this._foreground || null;
    },

    setCellForegrounds : function( colors ) {
      this._cellForegrounds = colors;
      this._update( "content" );
    },

    setBackground : function( color ) {
      this._background = color;
      this._update( "content" );
    },

    getCellBackground : function( column ) {
      var result = this._cellBackgrounds ? this._cellBackgrounds[ column ] : null;
      return typeof result === "string" ? result : null;
    },

    getBackground : function() {
      return this._background || null;
    },

    setCellBackgrounds : function( colors ) {
      this._cellBackgrounds = colors;
      this._update( "content" );
    },

    setColumnSpans : function( spans ) {
      this._columnSpans = spans;
      this._update( "content" );
    },

    getColumnSpan : function( column ) {
      return ( this._columnSpans ? this._columnSpans[ column ] : undefined ) || 0;
    },

    setImages : function( images ) {
      this._images = images;
      this._update( "content" );
    },

    getImage : function( column ) {
      var result = this._images ? this._images[ column ] : null;
      return result || null;
    },

    setChecked : function( value ) {
      this._checked = value;
      this._update( "check" );
    },

    isChecked : function() {
      return this._checked || false;
    },

    setGrayed : function( value ) {
      this._grayed = value;
      this._update( "check" );
    },

    isGrayed : function() {
      return this._grayed || false;
    },

    setCellChecked : function( value ) {
      this._cellChecked = value;
      this._update( "check" );
    },

    toggleCellChecked : function( cell ) {
      if( !this._cellChecked ) {
        this._cellChecked = [];
      }
      this._cellChecked[ cell ] = !this._cellChecked[ cell ];
      this._update( "check" );
    },

    getCellChecked : function() {
      return this._cellChecked || [];
    },

    isCellChecked : function( column ) {
      return this._cellChecked ? this._cellChecked[ column ] : false;
    },

    setCellGrayed : function( value ) {
      this._cellGrayed = value;
      this._update( "check" );
    },

    isCellGrayed : function( column ) {
      return this._cellGrayed ? this._cellGrayed[ column ] : false;
    },

    setCellCheckable : function( value ) {
      this._cellCheckable = value;
      this._update( "check" );
    },

    isCellCheckable : function( column ) {
      if( !this._cellCheckable || this._cellCheckable[ column ] === undefined ) {
        return true;
      }
      return this._cellCheckable[ column ];
    },

    setVariant : function( variant ) {
      this._variant = variant;
    },

    getVariant : function() {
      if( this._variant ) {
        return this._variant;
      }
      if( this._rootItem && this._rootItem !== this ) {
        return this._rootItem.getVariant();
      }
      return null;
    },

    setDefaultHeight : function( value ) {
      if( !this.isRootItem() ) {
        throw new Error( "Can only set default item height on root item" );
      }
      this._height = value;
    },

    setHeight : function( value, rendering ) {
      if( this.isRootItem() ) {
        throw new Error( "Can not set item height on root item" );
      }
      if( this._height === value ) {
        return;
      }
      this._height = value;
      if( value !== null ) {
        this._parent._addToCustomHeightItems( this );
      } else {
        this._removeFromCustomHeightItems( this );
      }
      this._update( "height", null, rendering );
    },

    getHeight : function() {
      return this._height;
    },

    getDefaultHeight : function() {
      var result;
      if( this.isRootItem() ) {
        result = this._height;
      } else {
        result = this.getRootItem().getDefaultHeight();
      }
      return result;
    },

    //////////////////////////
    // relationship management

    isRootItem : function() {
      return this._level < 0;
    },

    getRootItem : function() {
      return this._rootItem;
    },

    getLevel : function() {
      return this._level;
    },

    getParent : function() {
      return this._parent;
    },

    setExpanded : function( value ) {
      if( this._expanded != value ) {
        this._expanded = value;
        this._update( value ? "expanded" : "collapsed" );
        if( value ) {
          this._parent._addToExpandedItems( this );
        } else {
          this._parent._removeFromExpandedItems( this );
        }
      }
    },

    isExpanded : function() {
      return this._expanded;
    },

    isDisplayable : function() {
      var result = false;
      if( this.isRootItem() || this._parent.isRootItem() ) {
        result = true;
      } else {
        result = this._parent.isExpanded() && this._parent.isDisplayable();
      }
      return result;
    },

    hasChildren : function() {
      return this._children.length > 0;
    },

    getChildrenLength : function() {
      return this._children.length;
    },

    isChildCreated : function( index ) {
      return this._children[ index ] !== undefined;
    },

    isChildCached : function( index ) {
      return this._children[ index ].isCached();
    },

    getCachedChildren : function() {
      var result = [];
      for( var i = 0; i < this._children.length; i++ ) {
        if( this.isChildCreated( i ) && this.isChildCached( i ) ) {
          result.push( this._children[ i ] );
        }
      }
      return result;
    },

    getUncachedChildren : function() {
      var result = [];
      for( var i = 0; i < this._children.length; i++ ) {
        if( this.isChildCreated( i ) && !this.isChildCached( i ) ) {
          result.push( this._children[ i ] );
        }
      }
      return result;
    },

    getOffsetHeight : function() {
      var result = this.getOwnHeight();
      if( this.isExpanded() && this.hasChildren() ) {
        var lastChild = this.getLastChild();
        result += this._getChildOffset( lastChild );
        result += lastChild.getOffsetHeight();
      }
      return result;
    },

    hasCustomHeight : function() {
      return this._height !== null;
    },

    getOwnHeight : function() {
      var result = 0;
      if( !this.isRootItem() ) {
        result = this._height !== null ? this._height : this.getDefaultHeight();
      }
      return result;
    },

    getVisibleChildrenCount : function() { // TODO [tb] : rather "itemCount"
      if( this._visibleChildrenCount == null ) {
        this._computeVisibleChildrenCount();
      }
      return this._visibleChildrenCount;
    },

    getChild : function( index ) {
      var result;
      // Note: Check for index range first to avoid weird behavior in IE8 - bug 429741
      if( index >= 0 && index < this._children.length ) {
        result = this._children[ index ];
        if( !result ) {
          result = new rwt.widgets.GridItem( this, index, true );
        }
      }
      return result;
    },

    getLastChild : function() {
      return this.getChild( this._children.length - 1 );
    },

    indexOf : function( item ) {
      var hash = item.toHashCode();
      if( this._indexCache[ hash ] === undefined ) {
        this._indexCache[ hash ] = this._children.indexOf( item );
      }
      return this._indexCache[ hash ];
    },

    /**
     * Returns true if the given item is one of the parents of this item (recursive).
     */
    isChildOf : function( parent ) {
      var result = this._parent === parent;
      if( !result && !this._parent.isRootItem() ) {
        result = this._parent.isChildOf( parent );
      }
      return result;
    },

    /**
     * Returns the item that at the given vertical offset in pixel. The offset starts below
     * this item. The returned items occupies an area that includes the position defined by the
     * given offset. Its exact offset may by different. Negative offset is now allowed.
     */
    findItemByOffset : function( targetOffset ) {
      // TODO [tb] : cache results
      var itemHeight = this.getDefaultHeight();
      var waypoints = this._getDifferingHeightIndicies();
      if( waypoints[ 0 ] === 0 ) {
        waypoints.shift();
      }
      var currentOffset = 0;
      var currentIndex = 0;
      var result = null;
      var finished = false;
      if( targetOffset < 0 || this.getChildrenLength() === 0 ) {
        finished = true;
      }
      while( !finished ) {
        var currentItem = this.getChild( currentIndex );
        var currentItemHeight = currentItem.getOffsetHeight();
        var nextIndex = waypoints.shift();
        var nextOffset =   currentOffset
                         + currentItemHeight
                         + ( nextIndex - currentIndex - 1 ) * itemHeight;
        if( targetOffset < currentOffset + currentItemHeight ) {
          // case: target in current item
          if( targetOffset < currentOffset + currentItem.getOwnHeight() ) {
            result = currentItem;
          } else {
            var localOffset = targetOffset - currentOffset - currentItem.getOwnHeight();
            result = currentItem.findItemByOffset( localOffset );
          }
          finished = true;
        } else if( nextIndex === undefined || nextOffset > targetOffset ) {
          // case: target after current item, before next waypoint (or no more waypoint)
          var offsetDiff = targetOffset - currentOffset - currentItemHeight;
          var targetIndex = currentIndex + 1 + Math.floor( offsetDiff / itemHeight );
          result = this.getChild( targetIndex );
          finished = true;
        } else {
         // case: target in or after next waypoint
          currentIndex = nextIndex;
          currentOffset = nextOffset;
        }
      }
      return result;
    },

    /**
     * Finds the item at the given index, counting all visible children.
     */
    findItemByFlatIndex : function( index ) {
      var expanded = this._getExpandedIndicies();
      var localIndex = index;
      var result = null;
      var success = false;
      while( !success && localIndex >= 0) {
        var expandedIndex = expanded.shift();
        if( expandedIndex === undefined || expandedIndex >= localIndex ) {
          result = this.getChild( localIndex );
          if( result ) {
            this._indexCache[ result.toHashCode() ] = localIndex;
          }
          success = true;
        } else {
          var childrenCount = this.getChild( expandedIndex ).getVisibleChildrenCount();
          var offset = localIndex - expandedIndex; // Items between current item and target item
          if( offset <= childrenCount ) {
            result = this.getChild( expandedIndex ).findItemByFlatIndex( offset - 1 );
            success = true;
            if( result == null ) {
              throw new Error( "getItemByFlatIndex failed" );
            }
          } else {
            localIndex -= childrenCount;
          }
        }
      }
      return result;
    },

    /**
     * Returns the offset of this items top to the top of the entire visible item tree in pixel.
     * Root item is excluded, as it is not displayed.
     */
    getOffset : function() {
      var result = 0;
      if( !this._parent.isRootItem() ) {
        result += this._parent.getOffset() + this._parent.getOwnHeight();
      }
      result += this._parent._getChildOffset( this );
      return result;
    },

    /**
     * Returns the offset of a child in pixel relative to this item (its parent),
     * excluding the height of child and parent themselves (just the distance between)
     */
    _getChildOffset : function( item ) {
      var localIndex = this.indexOf( item );
      var result = localIndex * this.getDefaultHeight();
      var indicies = this._getDifferingHeightIndicies();
      while( indicies.length > 0 && localIndex > indicies[ 0 ] ) {
        var index = indicies.shift();
        result -= this.getDefaultHeight();
        result += this._children[ index ].getOffsetHeight();
      }
      return result;
    },

    /**
     * Gets the index relative to the root-item, counting all visible items inbetween.
     */
    getFlatIndex : function() {
      var localIndex = this._parent.indexOf( this );
      var result = localIndex;
      var expanded = this._parent._getExpandedIndicies();
      while( expanded.length > 0 && localIndex > expanded[ 0 ] ) {
        var expandedIndex = expanded.shift();
        result += this._parent._children[ expandedIndex ].getVisibleChildrenCount();
      }
      if( !this._parent.isRootItem() ) {
        result += this._parent.getFlatIndex() + 1;
      }
      return result;
    },

    hasPreviousSibling : function() {
      var index = this._parent.indexOf( this ) - 1 ;
      return index >= 0;
    },

    hasNextSibling : function() {
      var index = this._parent.indexOf( this ) + 1 ;
      return index < this._parent.getChildrenLength();
    },

    getPreviousSibling : function() {
      var index = this._parent.indexOf( this ) - 1 ;
      return this._parent.getChild( index );
    },

    getNextSibling : function() {
      var index = this._parent.indexOf( this ) + 1 ;
      var item = this._parent.getChild( index );
      this._parent._indexCache[ item.toHashCode() ] = index;
      return item;
    },

    /**
     * Returns the next visible item, which my be the first child,
     * the next sibling or the next sibling of the parent.
     */
    getNextItem : function( skipChildren ) {
      var result = null;
      if( !skipChildren && this.hasChildren() && this.isExpanded() ) {
        result = this.getChild( 0 );
      } else if( this.hasNextSibling() ) {
        result = this.getNextSibling();
      } else if( this.getLevel() > 0 ) {
        result = this._parent.getNextItem( true );
      }
      return result;
    },

    /**
     * Returns the previous visible item, which my be the previous sibling,
     * the previous siblings last child, or the parent.
     */
    getPreviousItem : function() {
      var result = null;
      if( this.hasPreviousSibling() ) {
        result = this.getPreviousSibling();
        while( result.hasChildren() && result.isExpanded() ) {
          result = result.getLastChild();
        }
      } else if( this.getLevel() > 0 ) {
        result = this._parent;
      }
      return result;
    },

    /////////////////////////
    // API for other TreeItem

    _add : function( item, index ) {
      if( this._children[ index ] ) {
        this._children.splice( index, 0, item );
        this._children.pop();
        this._update( "add", item );
      } else {
        this._children[ index ] = item;
      }
    },

    _remove : function( item ) {
      if( item.isExpanded() ) {
        delete this._expandedItems[ item.toHashCode() ];
      }
      if( item.hasCustomHeight() ) {
        delete this._customHeightItems[ item.toHashCode() ];
      }
      var index = this._children.indexOf( item );
      if( index !== -1 ) {
        this._children.splice( index, 1 );
        this._children.push( undefined );
      }
      this._update( "remove", item );
    },

    _addToExpandedItems : function( item ) {
      this._expandedItems[ item.toHashCode() ] = item;
    },

    _removeFromExpandedItems : function( item ) {
      delete this._expandedItems[ item.toHashCode() ];
    },

    _addToCustomHeightItems : function( item ) {
      this._customHeightItems[ item.toHashCode() ] = item;
    },

    _removeFromCustomHeightItems : function( item ) {
      delete this._customHeightItems[ item.toHashCode() ];
    },

    //////////////////////////////
    // support for event-bubbling:

    getEnabled : function() {
      return true;
    },

    _update : function( msg, related, rendering ) {
      var event = {
        "msg" : msg,
        "related" : related,
        "rendering" : rendering,
        "target" : this
      };
      this.dispatchSimpleEvent( "update", event, true );
      delete event.target;
      delete event.related;
      delete event.msg;
    },

    _onUpdate : function( event ) {
      if( event.msg !== "content" && event.msg !== "check" ) {
        this._visibleChildrenCount = null;
        this._indexCache = {};
      }
    },

    /////////
    // Helper

    _computeVisibleChildrenCount : function() {
      // NOTE: Caching this value speeds up creating and scrolling the tree considerably
      var result = 0;
      if( this.isExpanded() || this.isRootItem() ) {
       result = this._children.length;
        for( var i = 0; i < this._children.length; i++ ) {
          if( this.isChildCreated( i ) ) {
            result += this.getChild( i ).getVisibleChildrenCount();
          }
        }
      }
      this._visibleChildrenCount = result;
    },

    _getDifferingHeightIndicies : function() {
      var result = [];
      for( var key in this._expandedItems ) {
        result.push( this.indexOf( this._expandedItems[ key ] ) );
      }
      for( var key in this._customHeightItems ) {
        if( !this._expandedItems[ key ] ) { // prevent duplicates
          result.push( this.indexOf( this._customHeightItems[ key ] ) );
        }
      }
      // TODO [tb] : result could be cached
      return result.sort( function( a, b ){ return a - b; } );
    },

    _getExpandedIndicies : function() {
      var result = [];
      for( var key in this._expandedItems ) {
        result.push( this.indexOf( this._expandedItems[ key ] ) );
      }
      // TODO [tb] : result could be cached
      return result.sort( function( a, b ){ return a - b; } );
    },

    toString : function() {
      return "TreeItem " + ( this._texts ? this._texts.join() : "" );
    }

  }

} );
