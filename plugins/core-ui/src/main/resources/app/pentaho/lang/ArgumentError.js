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
