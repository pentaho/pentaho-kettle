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
  "../util/object",
  "../lang/ArgumentRequiredError"
], function(O, ArgumentRequiredError) {

  "use strict";

  /**
   * @name pentaho.i18n.MessageBundle
   * @class
   * @amd pentaho/i18n/MessageBundle
   *
   * @classdesc The `MessageBundle` class is a container of localized messages.
   *
   * Each localized message is accessible by a string key.
   * Messages can have property tags in them,
   * using the pattern: </br> `"{0}"`, `"{1}"`, ... **or** `"{keyword0}"`, `"{keyword1}"`, ...
   *
   * @description Creates a message bundle given a messages dictionary.
   * @param {?Object.<string, string>} [source] A messages dictionary.
   */
  function MessageBundle(source) {
    /**
     * The source messages dictionary.
     * @type Object.<string, string>
     * @readonly
     */
    this.source = (source && typeof source === "object") ? source : {};
    this.__structured = null;
  }

  /**
   * Indicates if the bundle contains a message with the given key.
   * @alias has
   * @memberOf pentaho.i18n.MessageBundle#
   * @param {string} key The key of the message.
   * @return {boolean} `true` if yes, `false` if no.
   */
  MessageBundle.prototype.has = function(key) {
    return O.getOwn(this.source, key) != null;
  };

  /**
   * Gets a localized, formatted message, given its key.
   *
   * @alias get
   * @memberOf pentaho.i18n.MessageBundle#
   *
   * @example
   * define(["pentaho/i18n/MessageBundle"], function(MessageBundle) {
   *    var bundle = new MessageBundle({key1: "value1", key2: "value2_{0}", key3: "value3_{keyword}");
   *
   *    bundle.get("key1");                        //returns "value1"
   *    bundle.get("keyNA", "default");            //returns "default"
   *    bundle.get("key2", ["scope"]);             //returns "value2_first"
   *    bundle.get("key2", {"keyword": "scope2"}); //returns "value3_scope2"
   *    bundle.get("keyNA", ["scope"], "default"); //returns "default"
   *    bundle.get("key2", [], "default");         //returns "value2_[?]"
   * });
   *
   * @param {string} key The message key.
   * @param {Array|Object|function} [scope] A scope array, object or function.
   *   This parameter can be specified _nully_ or totally omitted.
   *
   * @param {string} [missingMsg] The text to return when
   *    a message with the specified key is not defined.
   *    When `undefined`, the missing message is the specified key.
   *    When `null` (and three arguments were specified), the missing message is `null`.
   *
   * @return {string} A formatted message.
   */
  MessageBundle.prototype.get = function(key, scope, missingMsg) {
    if(arguments.length === 2 && typeof scope === "string") {
      missingMsg = scope;
      scope = null;
    }

    var text = O.getOwn(this.source, key);
    if(text == null) return missingMsg === undefined ? key : missingMsg;

    return this.format(text, scope);
  };

  /**
   * Gets the hierarchical object representation of the message bundle,
   * formed from splitting dotted message keys.
   *
   * @example
   * define(["pentaho/i18n/MessageBundle"], function(MessageBundle) {
   *    var bundle = new MessageBundle({
   *        'key1.folder1' : 'value1',
   *        'key1.folder2' : 'value2',
   *        'key2.folder1' : 'value3
   *    }
   *
   *    var obj = bundle.structured;
   *    //obj: {
   *    //   key1: {
   *    //       folder1: value1,
   *    //       folder2: value2
   *    //   },
   *    //   key2: {
   *    //       folder1: value3
   *    //   }
   *    //}
   * });
   *
   * @type {object}
   * @readonly
   */
  Object.defineProperty(MessageBundle.prototype, "structured", {
    get: function() {
      if(!this.__structured) {
        this.__structured = propertiesToObject(this.source);
      }

      return this.__structured;
    }
  });

  /**
   * Formats a string by
   * replacing the property tags it contains by
   * their corresponding values in a scope.
   *
   * Property tags have the format `"{property}"`,
   * where _property_ can be a number or a word that does not contain the special
   * `"{"` and `"}"` characters.
   *
   * To represent a literal brace character, place two consecutive brace characters,
   * `"{{"` or `"}}"`.
   *
   * When a property tag results in a _nully_ value (like when `scope` is not specified),
   * it is replaced by the special marker `"[?]"`.
   *
   * @example
   * MessageBundle.format("text");                    //returns "text"
   * MessageBundle.format("text_{0}");                //returns "text_[?]"
   * MessageBundle.format("text_{0}", ["v1"]);        //returns "text_v1"
   * MessageBundle.format("text_{k}", {"k": "v2"});   //returns "text_v2"
   * MessageBundle.format("text_{v3}", function(p) {  //returns "text_v3"
   *    return p;
   * });
   *
   * @alias format
   * @memberOf pentaho.i18n.MessageBundle
   * @param {string} text The text to format.
   * @param {Array|Object|function} [scope] A scope array, object or function.
   *
   * @return {string} The formatted string.
   */
  MessageBundle.format = function(text, scope) {
    // This is to prevent errors in a wrong message bundle id not being caught!
    if(text == null) throw new ArgumentRequiredError("text");
    var scopeFun;
    if(scope == null)
      scopeFun = function(prop) { return null; };
    else if(typeof scope === "function")
      scopeFun = scope;
    else
      scopeFun = function(prop) { return O.getOwn(scope, prop); };

    return String(text).replace(/(^|[^{])\{([^{}]+)\}/g, function($0, before, prop) {
      var value = scopeFun(prop);
      return before + (value == null ? "[?]" : value.toString());
    });
  };

  /**
   * @alias format
   * @memberOf pentaho.i18n.MessageBundle#
   *
   * @see pentaho.i18n.MessageBundle.format
   */
  MessageBundle.prototype.format = MessageBundle.format;

  return MessageBundle;

  /**
   * The message bundle's object representation.
   *
   * @example
   * var properties = {
   *    'key1.folder1' : 'value1',
   *    'key1.folder2' : 'value2',
   *    'key2.folder1' : 'value3
   * }
   *
   * var obj = propertiesToObject(properties);
   * //obj: {
   * //   key1: {
   * //       folder1: value1,
   * //       folder2: value2
   * //   },
   * //   key2: {
   * //       folder1: value3
   * //   }
   * //}
   *
   * @param {?object} source  A messages dictionary.
   * @return {object} Message bundle object representation.
   */
  function propertiesToObject(source) {
    var output = {};
    O.eachOwn(source, buildPath, output);
    return output;
  }

  /**
   * Auxiliary function for {@link propertiesToObject}
   *
   * @this this.structured
   * @param {string} value  Dictionary message.
   * @param {string} key    Dictionary message's key.
   */
  function buildPath(value, key) {
    var path = key.split(".");
    var obj = this;

    for(var i = 0, ic = path.length; i !== ic; ++i) {
      var p = path[i];

      if(i < ic - 1) {
        if(!O.hasOwn(obj, p)) {
          obj[p] = {};
        }

        obj = obj[p];
      } else {
        obj[p] = value;
      }
    }
  }
});
