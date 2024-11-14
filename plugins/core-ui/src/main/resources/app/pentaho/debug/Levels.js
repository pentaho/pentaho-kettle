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

define([
  "../util/object"
], function(O) {

  "use strict";

  /* eslint no-use-before-define: 0 */

  /*
   * The LevelsProto / Levels separation is so that the parse method is not an own property of Levels -
   * the enum values are its only own properties.
   */

  var LevelsProto = {
    // Unfortunately, there's no way to document a method on a JsDoc enum... :-(
    parse: function DebugLevels_parse(level, defaultLevel) {
      if(level != null && level !== "") {
        var l = Math.floor(+level);
        if(!isNaN(l)) {
          return Math.max(Levels.none, l);
        } else if((l = O.getOwn(Levels, String(level).toLowerCase())) != null) {
          return l;
        }
      }
      return defaultLevel != null ? defaultLevel : Levels.debug;
    }
  };

  /**
   * The `Levels` enum is the class of names for well known _debugging levels_.
   *
   * The enum also exposes a `parse` method.
   *
   * @memberOf pentaho.debug
   * @enum {number}
   * @readonly
   */
  var Levels = {
    /**
     * The `none` debugging level represents the absence of information or not wanting to receive any.
     * @default
     */
    none: 0,

    /**
     * The `error` debugging level represents error events.
     * @default
     */
    error: 1,

    /**
     * The `exception` is an alias for the [error]{@link pentaho.debug.Levels#error} level.
     * @default
     */
    exception: 1,

    /**
     * The `warn` debugging level represents events that could be a problem or not.
     * @default
     */
    warn: 2,

    /**
     * The `info` debugging level represents general information.
     * @default
     */
    info: 3,

    /**
     * The `debug` debugging level represents information that is relevant to actually _debug_ an application.
     * @default
     */
    debug: 4,

    /**
     * The `log` debugging level is an alias for the [debug]{@link pentaho.debug.Levels#debug} level.
     * @default
     */
    log: 4,

    /**
     * The `trace` debugging level represents information with the same character as
     * the [debug]{@link pentaho.debug.Levels#debug} level, but more detailed.
     *
     * @default
     */
    trace: 5,

    /**
     * The `all` debugging level represents _all_ information.
     * @default
     */
    all: Infinity
  };

  return (Levels = Object.freeze(O.assignOwn(Object.create(LevelsProto), Levels)));
});
