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
 * Basic node creation and type detection
 */
rwt.qx.Class.define("rwt.html.Nodes",
{
  /*
  *****************************************************************************
     STATICS
  *****************************************************************************
  */

  statics :
  {
    /*
    ---------------------------------------------------------------------------
      NODE TYPES
    ---------------------------------------------------------------------------
    */

    /**
     * {Map} Node type:
     *
     * * ELEMENT
     * * ATTRIBUTE
     * * TEXT
     * * CDATA_SECTION
     * * ENTITY_REFERENCE
     * * ENTITY
     * * PROCESSING_INSTRUCTION
     * * COMMENT
     * * DOCUMENT
     * * DOCUMENT_TYPE
     * * DOCUMENT_FRAGMENT
     * * NOTATION
     */
    ELEMENT                : 1,
    ATTRIBUTE              : 2,
    TEXT                   : 3,
    CDATA_SECTION          : 4,
    ENTITY_REFERENCE       : 5,
    ENTITY                 : 6,
    PROCESSING_INSTRUCTION : 7,
    COMMENT                : 8,
    DOCUMENT               : 9,
    DOCUMENT_TYPE          : 10,
    DOCUMENT_FRAGMENT      : 11,
    NOTATION               : 12,



    /**
     * Whether the given node is a DOM document node
     *
     * @type static
     * @param node {Node} the node which should be tested
     * @return {Boolean} true when the node is a document
     */
    isDocument : function(node) {
      return !!(node && node.nodeType === rwt.html.Nodes.DOCUMENT);
    },


    /*
    ---------------------------------------------------------------------------
      DOCUMENT ACCESS
    ---------------------------------------------------------------------------
    */

    /**
     * Returns the owner document of the given node
     *
     * @type static
     * @param node {Node} the node which should be tested
     * @return {Document | null} The document of the given DOM node
     */
    getDocument : function(node)
    {
      if (this.isDocument(node)) {
        return node;
      }

      return node.ownerDocument || node.document || null;
    },


    /**
     * Returns the DOM2 <code>defaultView</code> (window).
     *
     * @type static
     * @signature function(node)
     * @param node {Node} node to inspect
     * @return {Window} the <code>defaultView</code> of the given node
     */
    getWindow : function( node ) {
      return this.getDocument(node).defaultView;
    }

  }
});
