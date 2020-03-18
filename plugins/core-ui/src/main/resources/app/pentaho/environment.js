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
  "module",
  "./environment/impl/Environment"
], function(module, Environment) {

  "use strict";

  // This singleton instance module does not use an intermediary interface to be resolved,
  // as it may be used in a bootstrapping phase.

  /**
   * The _main_ environment of the JavaScript Pentaho Platform.
   *
   * This instance is initialized with the environment specification
   * which is the value of this module's AMD configuration.
   *
   * @name pentaho.environment.main
   * @type pentaho.environment.IEnvironment
   * @amd pentaho/environment
   * @see pentaho.environment.spec.IEnvironment
   */
  return new Environment(module.config());
});
