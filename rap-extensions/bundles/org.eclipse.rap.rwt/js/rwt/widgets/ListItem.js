/*******************************************************************************
 *  Copyright: 2004, 2015 1&1 Internet AG, Germany, http://www.1und1.de,
 *                        and EclipseSource
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Remote Application Platform
 ******************************************************************************/

rwt.qx.Class.define("rwt.widgets.ListItem", {

  extend : rwt.widgets.base.MultiCellWidget,

  construct : function() {
    this.base( arguments, [ "label" ] );
    this.setHorizontalChildrenAlign( "left" );
    // Fix for Bug 396835 - [List][Combo] Lists can scroll over maximum in IE7/8
    this.setContainerOverflow( false );
  },

  properties : {

    appearance : {
      refine : true,
      init : "list-item"
    },

    width : {
      refine : true,
      init : null
    },

    allowStretchX : {
      refine : true,
      init : true
    }

  },

  members : {

    setLabel : function( value ) {
      this.setCellContent( 0, value );
    },

    getLabel : function() {
      return this.getCellContent( 0 );
    },

    matchesString : function( value ) {
      var content;
      var el = this.getCellNode( 0 );
      if( el ) {
        content = el.innerText || el.textContent;
      } else {
        content = this.getLabel();
      }
      var input = ( typeof value === "string" ) ? value.toLowerCase() : "";
      content = ( typeof content === "string" ) ? content.toLowerCase() : "";
      return input !== "" && content.indexOf( input ) === 0;
    },

    // overwritten:
    getCellHeight : function() {
      return this.getHeight() - this.getPaddingTop();
    },

    // overwritten:
    getCellWidth : function() {
      return this.getWidth() - this.getPaddingLeft() - this.getPaddingRight();
    },

    _applyDirection : function( value ) {
      this.base( arguments, value );
      this.setHorizontalChildrenAlign( value === "rtl" ? "right" : "left" );
    }

  }

} );
