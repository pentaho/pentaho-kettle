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
  "./Base"
], function(Base) {

  "use strict";

  return Base.Error.extend("pentaho.lang.ArgumentError", /** @lends pentaho.lang.ArgumentError# */{
    /**
     * @classDesc The `ArgumentError` class is the base class of error objects associated with a function argument.
     *
     * The {@link pentaho.lang.ArgumentError#argument} property contains the name of the associated argument.
     *
     * @name ArgumentError
     * @memberOf pentaho.lang
     * @class
     * @extends pentaho.lang.Base.Error
     * @amd pentaho/lang/ArgumentError
     *
     * @description Creates an argument error object.
     * @constructor
     * @param {string} name The name of the argument.
     * @param {string} message The error message.
     */
    constructor: function(name, message) {

      this.base(message);

      /**
       * The name of the associated argument.
       * @type {string}
       * @readonly
       */
      this.argument = name;
    },

    /**
     * The name of the type of error.
     *
     * @type {string}
     * @readonly
     * @default "ArgumentError"
     */
    name: "ArgumentError"
  });
});
