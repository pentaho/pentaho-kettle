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

  // ECMA-262-7.0 / ISO-8601 format

  // 0 - full string
  // 1 - year
  // 2 -   [month]
  // 3 -     [day]
  // 4 - [time]
  // 5 - [timezone]
  var RE_ECMA_DATE = /^(\d{4})(?:-(\d{2})(?:-(\d{2}))?)?(?:T([\d:\.]+))?([Z+\-][\d:]*)?$/;

  // 0 - full string
  // 1 -  hours
  // 2 -    [minutes]
  // 3 -       [seconds]
  // 4 -          [milliseconds]
  var RE_ECMA_TIME = /^(\d{2}):(\d{2})(?::(\d{2})(?:\.(\d{3}))?)?$/;

  // 0 - full string
  // 1 - Z
  // or
  // 2 - +/-
  // 3 - hour
  // 4 - minutes
  var RE_ECMA_TIMEZONE = /^(?:(Z)|(?:([+\-])(\d{2}):(\d{2})))$/;

  /**
   * The `date` namespace contains utility functions for working with dates.
   *
   * @namespace
   * @memberOf pentaho.util
   * @amd pentaho/util/date
   * @private
   */
  var date = {
    /**
     * Parses a date string according to the simplified ISO-8601 format,
     * as defined by ECMA-262, version 7.
     *
     * For more information on the format,
     * see
     * {@link http://www.ecma-international.org/ecma-262/7.0/#sec-date-time-string-format}.
     *
     * When and if ever the implementations of `Date.parse` of supported browsers work as specified,
     * most of the code in this function can delegate to it.
     *
     * @param {number|string|Date} s The text to parse; or, the number of milliseconds; or, a date instance.
     * @return {?Date} A date instance or `null`, when invalid.
     */
    parseDateEcma262v7: function(s) {
      if(s == null) return null;
      switch(typeof s) {
        case "number": return new Date(s);
        case "object": return (s instanceof Date) ? s : null;
        case "string": break;
        default: return null;
      }

      // ECMA-262-7.0 / ISO-8601 format
      var m = RE_ECMA_DATE.exec(s);
      if(!m) return null;

      // 0 - full string
      // 1 - year
      // 2 -   [month]
      // 3 -     [day]
      // 4 - [time]
      // 5 - [timezone]
      var date = [Number(m[1]), Number(m[2] || "1") - 1, Number(m[3] || "1")];
      var time;

      // Parse time.
      if(m[4]) {
        time = parseTime(m[4]);

        // Invalid time string?
        if(!time) return null;
      } else {
        time = [0, 0, 0, 0];
      }

      // Date & time components
      var comps = date.concat(time);

      // Is Timezone specified?
      var utcOffset;
      if(m[5]) {
        utcOffset = parseTimeZone(m[5]);

        // Invalid Timezone string?
        if(!utcOffset) return null;

        // Account for UTC offset.
        comps[3] -= utcOffset[0];
        comps[4] -= utcOffset[1];
      } else {
        // Is date only? It is UTC.
        // Is date and time? It is local time (utcOffset === undefined).
        if(!m[4]) utcOffset = [0, 0];

        // else d = new Date(Number(m[1]), Number(m[2] || "1") - 1, Number(m[3] || "1"));
      }

      var d = utcOffset
        ? new Date(Date.UTC(comps[0], comps[1], comps[2], comps[3], comps[4], comps[5], comps[6]))
        : new Date(comps[0], comps[1], comps[2], comps[3], comps[4], comps[5], comps[6]);

      return isNaN(d.getTime()) ? null : d;
    }
  };

  function parseTimeZone(zone) {
    // 0 - full string
    // 1 - Z
    // or
    // 2 - +/-
    // 3 - hour
    // 4 - minutes
    var m = RE_ECMA_TIMEZONE.exec(zone);
    if(!m) return null;

    var offset = [0, 0];
    if(!m[1]) {
      var sign = m[2] === "+" ? 1 : -1;
      if(m[3]) offset[0] = sign * Number(m[3]);
      if(m[4]) offset[1] = sign * Number(m[4]);
    }

    return offset;
  }

  function parseTime(time) {
    // 0 - full string
    // 1 -  hours
    // 2 -    [minutes]
    // 3 -       [seconds]
    // 4 -          [milliseconds]
    var m = RE_ECMA_TIME.exec(time);
    if(!m) return null;

    time = [Number(m[1]), Number(m[2]), 0, 0];
    if(m[3]) {
      time[2] = Number(m[3]);
      if(m[4]) {
        time[3] = Number(m[4]);
      }
    }

    return time;
  }

  return date;
});
