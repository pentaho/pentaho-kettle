/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
