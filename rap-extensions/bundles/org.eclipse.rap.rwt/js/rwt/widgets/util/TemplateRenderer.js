/*******************************************************************************
 * Copyright (c) 2010, 2014 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

namespace( "rwt.widgets.util" );

(function(){

// TODO : move relevant Template.js code here
rwt.widgets.util.TemplateRenderer = function( template, parentElement, zIndexOffset ) {
  this._template = template;
  this._container = template._createContainer( {
    "element" : parentElement,
    "zIndexOffset" : zIndexOffset
  } );
};

rwt.widgets.util.TemplateRenderer.prototype = {

  renderItem : function( item ) {
    this._template._render( {
      "container" : this._container,
      "item" : item,
      "bounds" : this.targetBounds,
      "enabled" : this.targetIsEnabled,
      "markupEnabled" : this.markupEnabled,
      "seeable" : this.targetIsSeeable
    } );
  },

  isCellSelectable : function( cellElement ) {
    var cell = this._template._getCellByElement( this._container, cellElement );
    return cell !== -1 && this._template.isCellSelectable( cell );
  },

  getCellName : function( cellElement ) {
    var cell = this._template._getCellByElement( this._container, cellElement );
    return cell === -1 ? null : this._template.getCellName( cell );
  }

};

}());

