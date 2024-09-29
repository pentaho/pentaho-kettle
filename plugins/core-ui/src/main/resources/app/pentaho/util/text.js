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

  /**
   * The `text` namespace contains utility functions for
   * formatting strings that arise in API design.
   *
   * @namespace
   * @memberOf pentaho.util
   * @amd pentaho/util/text
   * @private
   */
  var text = {

    /**
     * Obtains a non-empty string representation of a given value.
     *
     * If `value` is {@link Nully}, `null` is returned.
     * Otherwise, `value` is given to the {@link String} function.
     * If the result is the empty string, `null` is returned.
     * Otherwise the string value is returned.
     *
     * @param {*} value - The value to convert to a non-empty string.
     * @return {nonEmptyString} A non-empty string or `null`.
     */
    nonEmptyString: function(value) {
      return value == null ? null : (String(value) || null);
    },

    /**
     * Ensures the first letter is upper case.
     *
     * @param {string} s The string to format.
     * @return {string} The formatted string.
     */
    firstUpperCase: function(s) {
      if(s) {
        var c = s.charAt(0);
        var cU = c.toUpperCase();
        if(c !== cU) s = cU + s.substr(1);
      }
      return s;
    },

    /**
     * Converts a camel-case string into title/label appropriate string.
     *
     * @param {string} name The string to convert to a title.
     * @return {string} The title/label appropriate string.
     */
    titleFromName: function(name) {
      if(name) {
        return text.firstUpperCase(name).replace(/([a-z\d])([A-Z])/g, "$1 $2");
      }

      return name;
    },

    /**
     * Converts a string into a snake-like string.
     *
     * Slashes, periods, white space, `@` and underscores are converted to an hyphen character, `-`.
     * Camel-case strings are split and joined by an hyphen character as well.
     * Consecutive resulting hyphens are converted to a single hyphen.
     *
     * If an hyphen would result in the first character, then an underscore is used instead.
     *
     * @param {string} name - The string to convert to snake-case.
     * @return {string} The snake-case string.
     */
    toSnakeCase: function(name) {
      if(name) {
        name = name
            .replace(/([a-z\d])([A-Z])/g, "$1-$2")
            .replace(/[\/\\_\s\.@]+/g, "-")
            .replace(/-+/g, "-");

        // Replace a leading - with an _.
        if(name.charAt(0) === "-") {
          name = "_" + name.substr(1);
        }
      }

      return name;
    },

    /**
     * Appends a sentence to another,
     * making sure that the appended sentence ends with a period or is, otherwise,
     * terminated by a punctuation character.
     *
     * @param {string} text The initial sentence.
     * @param {?string} [sentence] A sentence to append to `text`, that can not be properly terminated.
     * @return {string} A new, terminated sentence.
     */
    andSentence: function(text, sentence) {
      return text + (sentence ? (" " + this.withPeriod(sentence)) : "");
    },

    /**
     * Ensures a sentence is terminated with a period or another punctuation character,
     * like `;`, `?` or `!`.
     *
     * @param {string} sentence A possibly unterminated sentence.
     * @return {string} A new, terminated sentence.
     */
    withPeriod: function(sentence) {
      return sentence && !/[.;!?]/.test(sentence[sentence.length - 1]) ? (sentence + ".") : sentence;
    }
  };

  return text;
});
