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
  "./ArgumentError",
  "../util/text"
], function(ArgumentError, textUtil) {

  "use strict";

  return ArgumentError.extend("pentaho.lang.ArgumentInvalidError", /** @lends pentaho.lang.ArgumentInvalidError# */{
    /**
     * @classDesc  The `ArgumentInvalidError` class is the class of errors
     * for a function's argument that has been specified,
     * although with an invalid value.
     *
     * The name of the argument can be that of a nested property.
     * For example: `"keyArgs.description"`.
     *
     * An argument's value can be considered **invalid** because:
     * * It is not of one of the supported (and documented) types.
     *   Use instead: {@link pentaho.lang.ArgumentInvalidTypeError}.
     * * The specific value is not supported, or is out of range.
     *   Use instead: {@link pentaho.lang.ArgumentRangeError}
     * * The value is not in an acceptable state.
     * * The value refers to something which does not exist (like a dictionary _key_ which is undefined)
     *
     * You should use this error if none of the other more specific
     * invalid argument errors applies.
     *
     * @name ArgumentInvalidError
     * @memberOf pentaho.lang
     * @class
     * @extends pentaho.lang.ArgumentError
     * @amd pentaho/lang/ArgumentInvalidError
     *
     * @description Creates an invalid argument error object.
     * @constructor
     * @param {string} name The name of the argument.
     * @param {string} reason Text that explains the reason why the argument is considered invalid.
     * Can be useful when "being required" is a dynamic rule.
     */
    constructor: function(name, reason) {
      this.base(name, textUtil.andSentence("Argument " + name + " is invalid.", reason));
    },

    /**
     * The name of the type of error.
     *
     * @type {string}
     * @readonly
     * @default "ArgumentInvalidError"
     */
    name: "ArgumentInvalidError"
  });
});
