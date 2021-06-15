/*******************************************************************************
 * Copyright (c) 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.qx.Mixin.define( "rwt.widgets.util.HtmlAttributesMixin", {

  destruct : function() {
    delete this._htmlAttributes;
  },

  members : {

    applyObjectId : function( id ) {
      if( rwt.widgets.base.Widget._renderHtmlIds ) {
        this.setHtmlAttribute( "id", id );
      }
    },

    clearHtmlAttributes : function() {
      if( !this._htmlAttributes ) {
        return;
      }
      if( this._element ) {
        for( var attribute in this._htmlAttributes ) {
          this._element.removeAttribute( attribute );
        }
      }
      this._htmlAttributes = {};
    },

    setHtmlAttributes : function( map ) {
      for( var key in map ) {
        this.setHtmlAttribute( key, map[ key ] );
      }
    },

    setHtmlAttribute : function( attribute, value ) {
      if( !this._htmlAttributes && !value ) {
        return;
      } else if( !this._htmlAttributes ) {
        this._htmlAttributes = {};
      }
      if( value === null ) {
        delete this._htmlAttributes[ attribute ];
        if( this._element ) {
          this._element.removeAttribute( attribute );
        }
      } else {
        this._htmlAttributes[ attribute ] = value;
        if( this._element ) {
          this._element.setAttribute( attribute, value );
        }
      }
      if( this._update ) {
        this._update();
      }
    },

    getHtmlAttribute : function( attribute ) { // not used in RWT, but in tests and add-ons
      return this.getHtmlAttributes()[ attribute ] || "";
    },

    getHtmlAttributes : function() {
      return this._htmlAttributes || {}; // NOT a save copy!
    }

  }

} );
