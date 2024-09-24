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

  return Base.Error.extend("pentaho.lang.OperationInvalidError", /** @lends pentaho.lang.OperationInvalidError# */{
    /**
     * @classDesc The `OperationInvalidError` class is the class of errors
     * that signals that performing an operation is considered invalid.
     *
     * Performing an operation can be considered **invalid** when:
     * * The object in which it is executed is not in a state that allows the operation to be performed.
     *   For exampel: it is _locked_, _busy_ or _disposed_.
     * * It cannot be performed on a certain type of object.
     *
     * @example
     *
     * define(["pentaho/lang/OperationInvalid"], function(OperationInvalid) {
     *
     *   function Cell(value) {
     *     this._value = value;
     *     this._locked = false;
     *   }
     *
     *   Cell.prototype = {
     *     lock: function() {
     *       this._locked = true;
     *     },
     *
     *     get value() {
     *       return this._value;
     *     },
     *
     *     set value(v) {
     *       if(this._locked) {
     *         throw new OperationInvalid("Cell is locked.");
     *       }
     *
     *       this._value = v;
     *     }
     *   };
     *
     *   // ...
     * });
     *
     * @name OperationInvalidError
     * @memberOf pentaho.lang
     * @class
     * @extends pentaho.lang.Base.Error
     * @amd pentaho/lang/OperationInvalidError
     *
     * @description Creates an invalid operation error object.
     * @constructor
     * @param {string} reason Text that explains the reason why performing the operation is considered invalid.
     */
    constructor: function(reason) {
      this.base(textUtil.andSentence("Operation invalid.", reason));
    },

    /**
     * The name of the type of error.
     *
     * @type {string}
     * @readonly
     * @default "OperationInvalidError"
     */
    name: "OperationInvalidError"
  });
});
