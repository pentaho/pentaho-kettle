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
  "./ArgumentError"
], function(ArgumentError) {

  "use strict";

  return ArgumentError.extend("pentaho.lang.ArgumentRangeError", /** @lends pentaho.lang.ArgumentRangeError# */{
    /**
     * @classDesc The `ArgumentRangeError` class is the class of errors for a function's argument
     * that was specified with a value of one of the expected types,
     * albeit not within the expected range.
     *
     * The name of the argument can be that of a nested property.
     * For example: `"keyArgs.description"`.
     *
     * @example
     *
     * define(["pentaho/lang/ArgumentRangeError"], function(ArgumentRangeError) {
     *
     *   function insertAt(element, index) {
     *
     *     if(index < 0 || index > this.length) {
     *       throw new ArgumentRangeError("index");
     *     }
     *
     *     // Safe to insert at index
     *     this._elements.splice(index, 0, element);
     *   }
     *
     *   // ...
     * });
     *
     * @name ArgumentRangeError
     * @memberOf pentaho.lang
     * @class
     * @extends pentaho.lang.ArgumentError
     * @amd pentaho/lang/ArgumentRangeError
     *
     * @description Creates an out of range argument error object.
     * @constructor
     * @param {string} name The name of the argument.
     */
    constructor: function(name) {
      this.base(name, "Argument " + name + " is out of range.");
    },

    /**
     * The name of the type of error.
     *
     * @type {string}
     * @readonly
     * @default "ArgumentRangeError"
     */
    name: "ArgumentRangeError"
  });
});
