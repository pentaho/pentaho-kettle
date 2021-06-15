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

/**
 * The Label widget displays plain text or HTML text.
 *
 * Most complex qooxdoo widgets use instances of Label to display text.
 * The label supports auto sizing and internationalization.
 *
 * @appearance label
 */
rwt.qx.Class.define( "rwt.widgets.base.Label", {

  extend : rwt.widgets.base.Terminator,

  construct : function( text ) {
    this.base( arguments );
    if( text != null ) {
      this.setText( text );
    }
    this.initWidth();
    this.initHeight();
    this.initSelectable();
    this.initCursor();
    this.initWrap();
  },

  properties : {

    appearance : {
      refine : true,
      init : "label"
    },

    width : {
      refine : true,
      init : "auto"
    },

    height : {
      refine : true,
      init : "auto"
    },

    allowStretchX : {
      refine : true,
      init : false
    },

    allowStretchY : {
      refine : true,
      init : false
    },

    selectable : {
      refine : true,
      init : false
    },

    /**
     * The text of the label. How the text is interpreted depends on the value of the
     * property {@link #mode}.
     */
    text : {
      apply : "_applyText",
      init : "",
      dispose : true,
      check : "Label"
    },

    /**
     * Whether the text should be automatically wrapped into the next line
     */
    wrap : {
      check : "Boolean",
      init : false,
      nullable : true,
      apply : "_applyWrap"
    },

    /**
     * The alignment of the text inside the box
     */
    textAlign : {
      check : [ "left", "center", "right", "justify" ],
      nullable : true,
      themeable : true,
      apply : "_applyTextAlign"
    },

    /**
     * Whether an ellipsis symbol should be rendered if there is not enough room for the full text.
     *
     * Please note: If enabled this conflicts with a custom overflow setting.
     */
    textOverflow : {
      check : "Boolean",
      init : true
    },

    /**
     * Set how the label text should be interpreted
     *
     * <ul>
     *   <li><code>text</code> will set the text verbatim. Leading and trailing white space will be reserved.</li>
     *   <li><code>html</code> will interpret the label text as html.</li>
     *   <li><code>auto</code> will try to guess whether the text represents an HTML string or plain text.
     *       This is how older qooxdoo versions treated the text.
     *   </li>
     * <ul>
     */
    mode : {
      check : [ "html", "text", "auto" ],
      init : "auto"
    }

  },

  members : {

    _content : "",

    _applyTextAlign : function( value ) {
      if( value === null ) {
        this.removeStyleProperty( "textAlign" );
      } else {
        this.setStyleProperty( "textAlign", value );
      }
    },

    _applyFont : function( value ) {
      this._styleFont( value );
    },

    _styleFont : function( font ) {
      this._invalidatePreferredInnerDimensions();
      if( font ) {
        font.render( this );
      } else {
        rwt.html.Font.reset( this );
      }
    },

    _applyWrap : function( value ) {
      if( value == null ) {
        this.removeStyleProperty( "whiteSpace" );
      } else {
        this.setStyleProperty( "whiteSpace", value ? "normal" : "nowrap" );
      }
    },

    _applyText : function() {
      this._syncText( this.getText() );
    },

    _syncText : function( text ) {
      this._content = text;
      if( this._isCreated ) {
        this._renderContent();
      }
    },

    /**
     * Computes the needed dimension for the current text.
     */
    _computeObjectNeededDimensions : function() {
      var fontProps = this._styleProperties;
      var calc = rwt.widgets.util.FontSizeCalculation;
      var dimensions = calc.computeTextDimensions( this._content, fontProps );
      this._cachedPreferredInnerWidth = dimensions[ 0 ];
      this._cachedPreferredInnerHeight = dimensions[ 1 ];
    },

    _computePreferredInnerWidth : function() {
      this._computeObjectNeededDimensions();
      return this._cachedPreferredInnerWidth;
    },

    _computePreferredInnerHeight : function() {
      this._computeObjectNeededDimensions();
      return this._cachedPreferredInnerHeight;
    },

    _postApply : function() {
      var html = this._content;
      var element = this._getTargetNode();
      if( html == null ) {
        element.innerHTML = "";
      } else {
        var style = element.style;
        if( !this.getWrap() ) {
          if( this.getInnerWidth() < this.getPreferredInnerWidth() ) {
            style.overflow = "hidden";
          } else {
            style.overflow = "";
          }
        }
        element.innerHTML = html;
      }
    }

  }

} );
