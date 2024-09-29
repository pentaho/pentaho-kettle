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

  /**
   * @classDesc The `UserError` class is the base class of error objects associated with
   * the logic of a given operation.
   *
   * @name UserError
   * @memberOf pentaho.lang
   * @class
   * @extends pentaho.lang.Base.Error
   *
   * @description Creates a user error object.
   * @constructor
   * @param {string} message The error message.
   */

  return Base.Error.extend("pentaho.lang.UserError", /** @lends pentaho.lang.UserError# */{
    /**
     * The name of the type of error.
     *
     * @type {string}
     * @readonly
     * @default "UserError"
     */
    get name() {
      return "UserError";
    }
  });
});
