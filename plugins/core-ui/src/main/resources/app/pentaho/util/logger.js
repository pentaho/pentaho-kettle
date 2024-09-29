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

  /**
   * The `logger` namespace contains functions used to log messages in the console.
   *
   * @name logger
   * @namespace
   * @memberOf pentaho.util
   * @amd pentaho/util/logger
   * @private
   */
  var logger = /** @lends pentaho.util.logger */{

    /**
     *  Property enumerating the various log levels.
     *
     *  @type {string[]}
     */
    logLevels: ["debug", "log", "info", "warn", "error"],

    /**
     *  Current log level. Assign a new value to this property to change the log level.
     *
     *  @type {string}
     */
    logLevel: "log",

    /**
     * Logs a message at debug level.
     *
     * @param {string} m - Message to log.
     */
    debug: function(m) {
      __log(m, "debug");
    },

    /**
     * Logs a message at log level.
     *
     * @param {string} m - Message to log.
     */
    log: function(m) {
      __log(m, "log");
    },

    /**
     * Logs a message at info level.
     *
     * @param {string} m - Message to log.
     */
    info: function(m) {
      __log(m, "info");
    },

    /**
     * Logs a message at warn level.
     *
     * @param {string} m - Message to log.
     */
    warn: function(m) {
      __log(m, "warn");
    },

    /**
     * Logs a message at error level.
     *
     * @param {string} m - Message to log.
     */
    error: function(m) {
      __log(m, "error");
    }
  };

  function __log(m, type, css) {
    type = type || "info";

    // if(logger.logLevels.indexOf(type) < logger.logLevels.indexOf(logger.logLevel)) return;

    if(typeof console !== "undefined") {
      if(!console[type]) type = "log";

      if(css) {
        try {
          console[type]("%c" + m, css);
          return;
        } catch(e) {
          // styling is not supported
        }
      }
      console[type](m);
    }
  }

  return logger;
});
