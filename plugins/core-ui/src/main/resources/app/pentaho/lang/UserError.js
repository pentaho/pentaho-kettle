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
