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
  "../_core/main!"
], function(core) {

  "use strict";

  /**
   * The main modules service of the **Pentaho Modules API**.
   *
   * To obtain the values of modules as a dependency of an AMD/RequireJS module,
   * use one of the following AMD loader plugins instead:
   * * `pentaho/module!`
   * * `pentaho/module/instanceOf!`
   * * `pentaho/module/instancesOf!`
   * * `pentaho/module/typeOf!`
   * * `pentaho/module/typesOf!`
   *
   * @name service
   * @memberOf pentaho.module
   *
   * @see pentaho.module.metaService
   */
  return core.moduleService;
});
