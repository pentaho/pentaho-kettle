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
define([
  "module",
  "./debug/impl/Manager",
  "./debug/Levels",
  "./util/domWindow"
], function(module, Manager, DebugLevels, domWindow) {

  "use strict";

  var spec = module.config() || {};

  // URL debugLevel has precedence
  var level = __urlDebugLevel();
  if(level != null) spec.level = level;

  /**
   * The `pentaho.debug.manager` singleton provides access to the main debugging manager of
   * the JavaScript Pentaho Platform.
   *
   * The debugging levels can be configured through AMD as shown in the following example:
   *
   * ```js
   * require.config({
   *   config: {
   *     "pentaho/debug": {
   *       // Default debugging level
   *       "level": "warn",
   *
   *       // Per AMD module
   *       "modules": {
   *         "pentaho/lang/Base": "debug",
   *         "pentaho/type/complex": 3  // <=> "info"
   *       }
   *     }
   *   }
   * });
   * ```
   *
   * @name manager
   * @memberOf pentaho.debug
   * @type pentaho.debug.IManager
   * @amd pentaho/debug
   */

  var mgr = new Manager();
  mgr.configure(spec);
  return mgr;

  // Check URL for "debug" and "debugLevel"
  function __urlDebugLevel() {
    if(domWindow) {

      var urlIfHasDebug = function(win) {
        var url;
        return /\bdebug=true\b/.test((url = win.location.href)) ? url : null;
      };

      try {
        var url = urlIfHasDebug(domWindow) || (domWindow !== domWindow.top ? urlIfHasDebug(domWindow.top) : null);
        if(url) {
          var m = /\bdebugLevel=(\w+)\b/.exec(url);
          return DebugLevels.parse(m && m[1]);
        }
      } catch(e) { /* XSS or bad window object */ }
    }
  }
});
