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
  "module",
  "./UserError"
], function(module, UserError) {

  "use strict";

  /**
   * @classDesc The `RuntimeError` class represents a runtime error that is, nonetheless, handled and
   * whose message can be shown to the user.
   *
   * @name RuntimeError
   * @memberOf pentaho.lang
   * @class
   * @extends pentaho.lang.UserError
   *
   * @description Creates a runtime error object.
   * @constructor
   * @param {string} message The error message.
   */

  return UserError.extend(module.id, /** @lends pentaho.lang.RuntimeError# */{
    /**
     * The name of the type of error.
     *
     * @type {string}
     * @readonly
     * @default "pentaho.lang.RuntimeError"
     */
    get name() {
      return this.constructor.displayName;
    }
  });
});
