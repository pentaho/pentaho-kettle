/*!
 * Copyright 2010 - 2017 Hitachi Vantara. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
