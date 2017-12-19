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
  "./ArgumentError",
  "../util/text"
], function(ArgumentError, textUtil) {

  "use strict";

  return ArgumentError.extend("pentaho.lang.ArgumentRequiredError", /** @lends pentaho.lang.ArgumentRequiredError# */{
    /**
     * @classDesc The `ArgumentRequiredError` class is the class of errors for a function's argument
     * that was required but was not specified,
     * or was specified but with a [_nully_]{@link Nully} or empty value.
     *
     * The name of the argument can be that of a nested property.
     * For example: `"keyArgs.description"`.
     *
     * An argument being "required" may mean that an argument must be:
     * * Specified: `if(arguments.length < 1) ...`
     * * [_Truthy_]{@link https://developer.mozilla.org/en/docs/Glossary/Truthy}: `if(!value) ...`
     * * Not [_nully_]{@link Nully}: `if(value == null) ...`
     * * Not [_nully_]{@link Nully} or an empty string: `if(value == null || value === "") ...`
     *
     * @example
     *
     * define(["pentaho/lang/ArgumentRequiredError"], function(ArgumentRequiredError) {
     *
     *   function connect(channel) {
     *
     *     if(channel && channel.isOpened) {
     *       throw new ArgumentRequiredError("channel", "Channel not free to use.");
     *     }
     *
     *     var handle = channel.open();
     *     // ...
     *   }
     *
     *   // ...
     * });
     *
     * @name ArgumentRequiredError
     * @memberOf pentaho.lang
     * @class
     * @extends pentaho.lang.ArgumentError
     * @amd pentaho/lang/ArgumentRequiredError
     *
     * @description Creates a required argument error object.
     * @constructor
     * @param {string} name The name of the argument.
     * @param {?string} [text] Optional text further explaining the reason why the argument is required.
     * Can be useful when "being required" is a dynamic rule.
     */
    constructor: function(name, text) {
      this.base(name, textUtil.andSentence("Argument " + name + " is required.", text));
    },

    /**
     * The name of the type of error.
     *
     * @type {string}
     * @readonly
     * @default "ArgumentRequiredError"
     */
    name: "ArgumentRequiredError"
  });
});
