/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

define([
  "../lang/ArgumentRequiredError",
  "../lang/ArgumentInvalidError",
  "../lang/ArgumentInvalidTypeError",
  "../lang/ArgumentRangeError",
  "../lang/OperationInvalidError",
  "../lang/NotImplementedError"
], function(ArgumentRequiredError, ArgumentInvalidError, ArgumentInvalidTypeError,
            ArgumentRangeError, OperationInvalidError, NotImplementedError) {

  "use strict";

  /**
   * The `error` namespace contains factory functions for
   * creating `Error` instances for common error conditions
   * that arise in API design.
   *
   * @name error
   * @memberOf pentaho.util
   * @namespace
   * @amd pentaho/util/error
   * @private
   */
  return /** @lends pentaho.util.error */ {
    /**
     * Creates an error for a case where a required
     * function argument was not specified or was specified _nully_ or empty.
     *
     * It is up to the caller to actually `throw` the returned error object.
     * This makes flow control be clearly visible at the call site.
     *
     * @param {string} name The name of the argument.
     * @param {?string} [text] Optional text further explaining the reason why the argument is required.
     * Can be useful when "being required" is a dynamic rule.
     *
     * @return {pentaho.lang.ArgumentRequiredError} The created `Error` object.
     */
    argRequired: function(name, text) {
      return new ArgumentRequiredError(name, text);
    },

    /**
     * Creates an error object for a case where a function argument
     * has been specified, albeit with an invalid value.
     *
     * @param {string} name The name of the argument.
     * @param {string} reason Text that explains the reason why the argument is considered invalid.
     * @return {ArgumentInvalidError} The created error object.
     */
    argInvalid: function(name, reason) {
      return new ArgumentInvalidError(name, reason);
    },

    /**
     * Creates an error object for a case where a function argument
     * has been specified, albeit with a value of an unsupported type,
     * according to the documented contract.
     *
     * It is up to the caller to actually `throw` the returned `TypeError` object.
     * This makes flow control be clearly visible at the call site.
     *
     * @param {string} name The name of the argument.
     * @param {string|string[]} expectedType The name or names of the expected types.
     * @param {string} [actualType] The name of the received type, when known.
     * @return {ArgumentInvalidTypeError} The created error object.
     */
    argInvalidType: function(name, expectedType, actualType) {
      return new ArgumentInvalidTypeError(name, expectedType, actualType);
    },

    /**
     * Creates an error object for a case where a function argument
     * was specified with a value of one of the expected types,
     * albeit not within the expected range.
     *
     * It is up to the caller to actually `throw` the returned `RangeError` object.
     * This makes flow control be clearly visible at the call site.
     *
     * @param {string} name The name of the argument.§1§
     * @return {pentaho.lang.ArgumentRangeError} The created error object.
     */
    argRange: function(name) {
      return new ArgumentRangeError(name);
    },

    /**
     * Creates an `Error` object for a case where performing an operation is considered invalid.
     *
     * It is up to the caller to actually `throw` the returned `Error` object.
     * This makes flow control be clearly visible at the call site.
     *
     * @param {string} reason Text that explains the reason why performing the operation is considered invalid.
     * @return {pentaho.lang.OperationInvalidError} The created `Error` object.
     */
    operInvalid: function(reason) {
      return new OperationInvalidError(reason);
    },

    /**
     * Creates an `Error` object for a case where an _abstract_ method has not been implemented/overridden
     * and is being called.
     *
     * @param {?string} [text] Complementary text.
     * @return {pentaho.lang.NotImplementedError} The created `Error` object.
     */
    notImplemented: function(text) {
      return new NotImplementedError(text);
    }
  };
});
