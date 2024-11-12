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
  "../_core/main!"
], function(core) {

  "use strict";

  /**
   * The main modules metadata service of the **Pentaho Modules API**.
   *
   * If instead of the metadata of modules, the modules themselves are needed,
   * use {@link pentaho.module.service} directly,
   * or one of the following AMD loader plugins:
   * * `pentaho/module!`
   * * `pentaho/module/instanceOf!`
   * * `pentaho/module/instancesOf!`
   * * `pentaho/module/typeOf!`
   * * `pentaho/module/typesOf!`
   *
   * The modules metadata service is first initialized with the AMD configuration of the `pentaho.modules` module.
   * Secondly,
   * the environmental configuration of `pentaho/modules` also contributes with additional modules' metadata.
   *
   * @name metaService
   * @memberOf pentaho.module
   */
  return core.moduleMetaService;
});
