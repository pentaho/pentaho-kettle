/*******************************************************************************
 * Copyright (c) 2004, 2015 1&1 Internet AG, Germany, http://www.1und1.de,
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
 * Contains methods to control and query the element's overflow properties.
 */
rwt.qx.Class.define("rwt.html.Overflow",
{
  /*
  *****************************************************************************
     STATICS
  *****************************************************************************
  */

  statics :
  {

    // Mozilla notes (http://developer.mozilla.org/en/docs/Mozilla_CSS_Extensions):
    // -moz-scrollbars-horizontal: Indicates that horizontal scrollbars should
    //    always appear and vertical scrollbars should never appear.
    // -moz-scrollbars-vertical: Indicates that vertical scrollbars should
    //    always appear and horizontal scrollbars should never appear.
    // -moz-scrollbars-none: Indicates that no scrollbars should appear but
    //    the element should be scrollable from script. (This is the same as
    //    hidden, and has been since Mozilla 1.6alpha.)
    //
    // Also a lot of interesting bugs:
    // * https://bugzilla.mozilla.org/show_bug.cgi?id=42676
    // * https://bugzilla.mozilla.org/show_bug.cgi?id=47710
    // * https://bugzilla.mozilla.org/show_bug.cgi?id=235524

    /**
     * Returns the computed value of the horizontal overflow
     *
     * @type static
     * @signature function(element, mode)
     * @param element {Element} DOM element to query
     * @param mode {Number} Choose one of the modes {@link rwt.html.Style#COMPUTED_MODE},
     *   {@link rwt.html.Style#CASCADED_MODE}, {@link rwt.html.Style#LOCAL_MODE}.
     *   The computed mode is the default one.
     * @return {String} computed overflow value
     */
    getX : rwt.util.Variant.select("qx.client",
    {
      // gecko support differs
      "gecko" : rwt.client.Client.getVersion() < 1.8 ?

      // older geckos do not support overflowX
      // it's also more safe to translate hidden to -moz-scrollbars-none
      // because of issues in older geckos
      function( element ) {
        var overflow = rwt.html.Style.getStyleProperty( element, "overflow" );

        if (overflow === "-moz-scrollbars-none") {
          overflow = "hidden";
        }

        return overflow;
      } :

      // gecko >= 1.8 supports overflowX, too
      function( element ) {
        return rwt.html.Style.getStyleProperty( element, "overflowX" );
      },

      // use native overflowX property
      "default" : function( element ) {
        return rwt.html.Style.getStyleProperty( element, "overflowX" );
      }
    }),




    /**
     * Returns the computed value of the vertical overflow
     *
     * @type static
     * @signature function(element, mode)
     * @param element {Element} DOM element to query
     * @param mode {Number} Choose one of the modes {@link rwt.html.Style#COMPUTED_MODE},
     *   {@link rwt.html.Style#CASCADED_MODE}, {@link rwt.html.Style#LOCAL_MODE}.
     *   The computed mode is the default one.
     * @return {String} computed overflow value
     */
    getY : rwt.util.Variant.select("qx.client",
    {
      // gecko support differs
      "gecko" : rwt.client.Client.getVersion() < 1.8 ?

      // older geckos do not support overflowY
      // it's also more safe to translate hidden to -moz-scrollbars-none
      // because of issues in older geckos
      function(element, mode)
      {
        var overflow = rwt.html.Style.getStyleProperty(element, "overflow", mode, false);

        if (overflow === "-moz-scrollbars-none") {
          overflow = "hidden";
        }

        return overflow;
      } :

      // gecko >= 1.8 supports overflowY, too
      function( element ) {
        return rwt.html.Style.getStyleProperty(element, "overflowY" );
      },

      // use native overflowY property
      "default" : function( element ) {
        return rwt.html.Style.getStyleProperty(element, "overflowY" );
      }
    })

  }
});
