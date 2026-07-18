/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


define([
  "../_core/main!"
], function(core) {

  "use strict";

  /**
   * The _main_ configuration service of the JavaScript Pentaho Platform.
   *
   * @alias service
   * @memberOf pentaho.config
   * @type {pentaho.config.IService}
   * @readOnly
   */
  return core.configService;
});
