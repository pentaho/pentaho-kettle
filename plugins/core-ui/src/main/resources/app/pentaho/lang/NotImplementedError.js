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
  "./Base",
  "../util/text"
], function(Base, textUtil) {

  "use strict";

  return Base.Error.extend("pentaho.lang.NotImplementedError", /** @lends pentaho.lang.NotImplementedError# */{
    /**
     * @classDesc The `NotImplementedError` class is the class of errors
     * that signals that a method that is either
     * abstract and has not been overridden, or
     * is not abstract but has not been implemented,
     * and is being called.
     *
     * @name NotImplementedError
     * @memberOf pentaho.lang
     * @class
     * @extends pentaho.lang.Base.Error
     * @amd pentaho/lang/NotImplementedError
     *
     * @description Creates a not implemented error object.
     * @constructor
     * @param {?string} [text] Complementary text.
     */
    constructor: function(text) {
      this.base(textUtil.andSentence("Not Implemented.", text));
    },

    /**
     * The name of the type of error.
     *
     * @type {string}
     * @readonly
     * @default "NotImplementedError"
     */
    name: "NotImplementedError"
  });
});
