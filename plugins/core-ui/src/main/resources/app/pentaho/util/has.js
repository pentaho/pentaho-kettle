/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

define(function() {
  "use strict";

  /* eslint no-proto: 0 */

  var O_hasOwn = Object.prototype.hasOwnProperty;

  var hasUrl = false;
  if(typeof URL !== "undefined") {
    try {
      var u = new URL("b", "http://a");
      u.pathname = "c%20d";
      hasUrl = u.href === "http://a/c%20d";
    } catch(e) {
    }
  }

  var capabilities = {
    "Object.setPrototypeOf": Object.setPrototypeOf != null,
    "Object.prototype.__proto__": {}.__proto__ != null,
    "URL": hasUrl
  };

  return has;

  function has(capability) {
    return O_hasOwn.call(capabilities, capability) && !!capabilities[capability];
  }
});
