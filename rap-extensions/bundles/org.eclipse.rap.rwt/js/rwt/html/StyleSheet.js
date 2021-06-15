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

rwt.qx.Class.define( "rwt.html.StyleSheet", {

  statics : {

    /**
     * Creates a new Stylesheet node and append it to the document.
     */
    createElement : document.createStyleSheet ?
      // ie4+
      function( vCssText ) {
        var vSheet = document.createStyleSheet();
        if( vCssText ) {
          vSheet.cssText = vCssText;
        }
        return vSheet;
      } :
      // other
      function( vCssText ) {
        var vElement = document.createElement("style");
        vElement.type = "text/css";
        // Safari 2.0 doesn't like empty stylesheets
        vElement.appendChild(document.createTextNode(vCssText || "body {}"));
        document.getElementsByTagName("head")[0].appendChild(vElement);
        if( vElement.sheet ) {
          return vElement.sheet;
        } else {
          // Safari 2.0 doesn't support element.sheet so we neet a workaround
          var styles = document.styleSheets;
          for( var i=styles.length-1; i>=0; i-- ) {
            if( styles[i].ownerNode == vElement ) {
              return styles[i];
            }
          }
        }
        throw "Error: Could not get a reference to the sheet object";
      },

    /**
     * Inserts a new CSS rule into a given Stylesheet.
     */
    addRule : document.createStyleSheet ?
      // ie4+
      function( vSheet, vSelector, vStyle ) {
        vSheet.addRule( vSelector, vStyle );
      } :
      // other
      function( vSheet, vSelector, vStyle ) {
        vSheet.insertRule(vSelector + "{" + vStyle + "}", vSheet.cssRules.length);
      },

    /**
     * Removes a CSS rule from a stylesheet.
     */
    removeRule : document.createStyleSheet ?
      // ie4+
      function( vSheet, vSelector ) {
        var vRules = vSheet.rules;
        var vLength = vRules.length;
        for( var i = vLength - 1 ; i >= 0; i-- ) {
          if( vRules[i].selectorText == vSelector ) {
            vSheet.removeRule( i );
          }
        }
      } :
      // other
      function( vSheet, vSelector ) {
        var vRules = vSheet.cssRules;
        var vLength = vRules.length;
        for( var i = vLength - 1; i >= 0; i-- ) {
          if( vRules[i].selectorText == vSelector ) {
            vSheet.deleteRule( i );
          }
        }
      },

    /**
     * Removes all CSS rules from a stylesheet.
     */
    removeAllRules : document.createStyleSheet ?
      // ie4+
      function( vSheet ) {
        var vRules = vSheet.rules;
        var vLength = vRules.length;
        for( var i = vLength-1; i >= 0; i-- ) {
          vSheet.removeRule( i );
        }
      } :
      // other
      function( vSheet ) {
        var vRules = vSheet.cssRules;
        var vLength = vRules.length;
        for( var i = vLength - 1; i >= 0; i-- ) {
          vSheet.deleteRule( i );
        }
      }

  }

});
