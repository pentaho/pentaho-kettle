/*!
 * Copyright 2018 Hitachi Vantara. All rights reserved.
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
