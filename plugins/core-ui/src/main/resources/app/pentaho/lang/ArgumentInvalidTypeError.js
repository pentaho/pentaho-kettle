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

  return ArgumentError.extend("pentaho.lang.ArgumentInvalidTypeError",
    /** @lends pentaho.lang.ArgumentInvalidTypeError# */{
      /**
       * @classDesc  The `ArgumentInvalidTypeError` class is the class of errors
       * for a function's argument that has been specified,
       * although with a value of an unsupported type, according to the documented contract.
       *
       * The name of the argument can be that of a nested property.
       * For example: `"keyArgs.description"`.
       *
       * Types can be:
       * * One of the possible results of the `typeof` operator.
       *   For example: `"number"`, `"string"`, `"boolean"`, `"function"`.
       * * The name of global classes/constructors,
       *   that would be testable by use of the `instanceof` operator or
       *   by accessing the `constructor` property.
       *   For example: `"Array"`, `"Object"`, or `"HTMLElement"`.
       * * The identifier of an AMD module that returns a constructor or factory.
       *   For example: `"pentaho/type/Complex"`.
       *
       * @example
       *
       * define(["pentaho/lang/ArgumentInvalidTypeError"], function(ArgumentInvalidTypeError) {
       *
       *   function createInstance(type, args) {
       *     var TypeCtor;
       *
       *     switch(typeof type) {
       *       case "string":
       *         TypeCtor = window[type];
       *         break;
       *
       *       case "function":
       *         TypeCtor = type;
       *         break;
       *
       *       default:
       *         throw new ArgumentInvalidTypeError("type", ["string", "function"], typeof type);
       *     }
       *
       *     // ...
       *   }
       *
       *   // ...
       * });
       *
       * @name ArgumentInvalidTypeError
       * @memberOf pentaho.lang
       * @class
       * @extends pentaho.lang.ArgumentError
       * @amd pentaho/lang/ArgumentInvalidTypeError
       *
       * @description Creates an invalid argument type error object.
       * @constructor
       * @param {string} name The name of the argument.
       * @param {string|string[]} expectedType The name or names of the expected types.
       * @param {string} [actualType] The name of the received type, when known.
       */
      constructor: function(name, expectedType, actualType) {
        var typesMsg = "Expected type to be ";

        if(!Array.isArray(expectedType)) expectedType = [expectedType];

        if(expectedType.length > 1) {
          var expectedTypesClone = expectedType.slice();
          var lastExpectedType = expectedTypesClone.pop();
          typesMsg += "one of " + expectedTypesClone.join(", ") + " or " + lastExpectedType;
        } else {
          // If should have at least one entry...
          typesMsg += expectedType[0];
        }

        typesMsg += actualType ? (", but got " + actualType + ".") : ".";

        this.base(name, textUtil.andSentence("Argument " + name + " is invalid.", typesMsg));

        /**
         * The name of the received type, when known.
         * @type {?nonEmptyString}
         * @readonly
         */
        this.actualType = actualType || null;

        /**
         * The names of the expected types.
         * @type {nonEmptyString[]}
         * @readonly
         */
        this.expectedTypes = expectedType;
      },

      /**
       * The name of the type of error.
       *
       * @type {string}
       * @readonly
       * @default "ArgumentInvalidTypeError"
       */
      name: "ArgumentInvalidTypeError"
    });
});
