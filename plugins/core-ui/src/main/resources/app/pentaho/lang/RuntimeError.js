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
