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
