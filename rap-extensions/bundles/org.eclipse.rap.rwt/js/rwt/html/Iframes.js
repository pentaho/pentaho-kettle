/*******************************************************************************
 * Copyright (c) 2004, 2013 1&1 Internet AG, Germany, http://www.1und1.de,
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
 * Cross browser abstractions to work with iframes.
 */
rwt.qx.Class.define("rwt.html.Iframes",
{
  /*
  *****************************************************************************
     STATICS
  *****************************************************************************
  */

  statics :
  {
    /**
     * Get the DOM window object of an iframe.
     *
     * @type static
     * @param vIframe {Element} DOM element of the iframe.
     * @return {DOMWindow} The DOM window object of the iframe.
     * @signature function(vIframe)
     */
    getWindow : function( vIframe ) {
      try {
        var vDoc = rwt.html.Iframes.getDocument( vIframe );
        return vDoc ? vDoc.defaultView : null;
      } catch( ex ) {
        return null;
      }
    },

    /**
     * Get the DOM document object of an iframe.
     *
     * @type static
     * @param vIframe {Element} DOM element of the iframe.
     * @return {DOMDocument} The DOM document object of the iframe.
     * @signature function(vIframe)
     */
    getDocument : function( vIframe ) {
      try {
        return vIframe.contentDocument;
      } catch( ex ) {
        return null;
      }
    },

    /**
     * Get the HTML body element of the iframe.
     *
     * @type static
     * @param vIframe {Element} DOM element of the iframe.
     * @return {Element} The DOM node of the <code>body</code> element of the iframe.
     */
    getBody : function(vIframe)
    {
      var vDoc = rwt.html.Iframes.getDocument(vIframe);
      return vDoc ? vDoc.getElementsByTagName("body")[0] : null;
    }
  }
});
