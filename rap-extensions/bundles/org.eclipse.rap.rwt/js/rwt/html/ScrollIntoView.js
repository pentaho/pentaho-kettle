/*******************************************************************************
 * Copyright (c) 2004, 2014 1&1 Internet AG, Germany, http://www.1und1.de,
 *                          EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Remote Application Platform
 ******************************************************************************/

// Internet Explorer has invented scrollIntoView, but does not behave the same like in Mozilla (which would be better)
// Mozilla has a native well working method scrollIntoView
// Safari does not support scrollIntoView (but it can be found in Webkit since May 2005)
// Opera does not support scrollIntoView

namespace( "rwt.html" );

/**
 * Functions to scroll DOM elements into the visible area of the parent element
 */
rwt.html.ScrollIntoView = {

  /**
   * Scroll the parent DOM element so that the element's so that the x coordinate is inside
   * the visible area of the parent.
   *
   * @type static
   * @param vElement {Element} DOM node to be scrolled into view
   * @param vAlignLeft {Boolean?false} whether the element should be left aligned
   * @return {Boolean} Whether the element could be scrolled into the view
   */
  scrollX : function( vElement, vAlignLeft ) {
    var vHasScroll;
    var vParent = vElement.parentNode;
    var vOffset = vElement.offsetLeft;
    var vWidth = vElement.offsetWidth;
    while( vParent ) {
      switch( rwt.html.Style.getStyleProperty( vParent, "overflow" ) ) {
        case "scroll":
        case "auto":
        case "-moz-scrollbars-horizontal":
          vHasScroll = true;
          break;
        default:
          switch( rwt.html.Style.getStyleProperty( vParent, "overflowX" ) ) {
            case "scroll":
            case "auto":
              vHasScroll = true;
              break;
            default:
              vHasScroll = false;
          }
      }
      if( vHasScroll ) {
        var vParentWidth = vParent.clientWidth;
        var vParentScrollLeft = vParent.scrollLeft;
        var scrollable = vParent.rwtScrollable;
        var scrollBarWidth = scrollable ? scrollable.getVerticalBarWidth() : 0;
        if( vAlignLeft ) {
          vParent.scrollLeft = vOffset;
        } else if( vAlignLeft === false ) {
          vParent.scrollLeft = vOffset + vWidth - vParentWidth + scrollBarWidth;
        } else if( vWidth > vParentWidth || vOffset < vParentScrollLeft ) {
          vParent.scrollLeft = vOffset;
        } else if( vOffset + vWidth >= vParentScrollLeft + vParentWidth ) {
          vParent.scrollLeft = vOffset + vWidth - vParentWidth + scrollBarWidth;
        }
        vOffset = vParent.offsetLeft;
        vWidth = vParent.offsetWidth;
      } else {
        vOffset += vParent.offsetLeft;
      }
      if( vParent.tagName.toLowerCase() == "body" ) {
        break;
      }
      vParent = vParent.offsetParent;
    }
    return true;
  },

  /**
   * Scroll the parent DOM element so that the element's so that the y coordinate is inside
   * the visible area of the parent.
   *
   * @type static
   * @param vElement {Element} DOM node to be scrolled into view
   * @param vAlignTop {Boolean?false} whether the element should be top aligned
   * @return {Boolean} Whether the element could be scrolled into the view
   */
  scrollY : function( vElement, vAlignTop ) {
    var vHasScroll;
    var vParent = vElement.parentNode;
    var vOffset = vElement.offsetTop;
    var vHeight = vElement.offsetHeight;
    while( vParent ) {
      switch( rwt.html.Style.getStyleProperty( vParent, "overflow" ) ) {
        case "scroll":
        case "auto":
        case "-moz-scrollbars-vertical":
          vHasScroll = true;
          break;
        default:
          switch( rwt.html.Style.getStyleProperty( vParent, "overflowY" ) ) {
            case "scroll":
            case "auto":
              vHasScroll = true;
              break;
            default:
              vHasScroll = false;
          }
      }
      if( vHasScroll ) {
        var vParentHeight = vParent.clientHeight;
        var vParentScrollTop = vParent.scrollTop;
        var scrollable = vParent.rwtScrollable;
        var scrollBarHeight = scrollable ? scrollable.getHorizontalBarHeight() : 0;
        if( vAlignTop ) {
          vParent.scrollTop = vOffset;
        } else if( vAlignTop === false ) {
          vParent.scrollTop = vOffset + vHeight - vParentHeight + scrollBarHeight;
        } else if( vHeight > vParentHeight || vOffset < vParentScrollTop ) {
          vParent.scrollTop = vOffset;
        } else if( vOffset + vHeight >= vParentScrollTop + vParentHeight ) {
          vParent.scrollTop = vOffset + vHeight - vParentHeight + scrollBarHeight;
        }
        vOffset = vParent.offsetTop;
        vHeight = vParent.offsetHeight;
      } else {
        vOffset += vParent.offsetTop;
      }
      if( vParent.tagName.toLowerCase() == "body" ) {
        break;
      }
      vParent = vParent.offsetParent;
    }
    return true;
  }

};
