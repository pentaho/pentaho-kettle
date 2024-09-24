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
