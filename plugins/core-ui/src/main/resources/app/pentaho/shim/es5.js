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
 *
 */

/* ES5 polyfills */

/* eslint no-extend-native: 0 */

// Add trim function to support IE8
if(typeof String.prototype.trim !== "function") {
  String.prototype.trim = function() {
    return this.replace(/^\s+|\s+$/g, "");
  };
}

// Add indexOf function to support IE8
if(!Array.prototype.indexOf) {
  Array.prototype.indexOf = function(searchElement /*, fromIndex */) {

    "use strict";

    if(this == null) {
      throw new TypeError();
    }

    var n, k, t = Object(this),
        len = t.length >>> 0;

    if(len === 0) {
      return -1;
    }

    n = 0;
    if(arguments.length > 1) {
      n = Number(arguments[1]);
      if(n != n) { // shortcut for verifying if it's NaN
        n = 0;
      } else if(n != 0 && n != Infinity && n != -Infinity) {
        n = (n > 0 || -1) * Math.floor(Math.abs(n));
      }
    }

    if(n >= len) {
      return -1;
    }

    for(k = n >= 0 ? n : Math.max(len - Math.abs(n), 0); k < len; k++) {
      if(k in t && t[k] === searchElement) {
        return k;
      }
    }

    return -1;
  };
}

if(!Array.isArray) {
  Array.isArray = function(arg) {
    return Object.prototype.toString.call(arg) === "[object Array]";
  };
}
