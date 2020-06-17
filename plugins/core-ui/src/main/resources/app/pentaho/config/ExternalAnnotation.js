/*!
 * Copyright 2019 Hitachi Vantara. All rights reserved.
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
  "../module/Annotation"
], function(module, Annotation) {

  return Annotation.extend(module.id, /** @lends pentaho.config.ExternalAnnotation# */{
    /**
     * @classDesc The external configuration annotation allows external sources to contribute
     * to the configuration of a module.
     *
     * The [priority]{@link pentaho.config.ExternalAnnotation.priority} property
     * allows specifying the order with which configuration sources are merged with the overall
     * _internal_ configuration,
     * which is considered to have priority `0`.
     * Configurations are merged by using the {@link pentaho.util.spec.merge} method.
     *
     * @alias ExternalAnnotation
     * @memberOf pentaho.config
     * @class
     * @extend pentaho.module.Annotation
     * @abstract
     *
     * @private
     *
     * @description Creates an external configuration annotation associated with a given module.
     * @constructor
     * @param {pentaho.module.IMeta} forModule - The annotated module.
     * @param {*} config - The associated configuration.
     */
    constructor: function(forModule, config) {

      this.base(forModule);

      this._config = config;
    },

    /**
     * Gets the external configuration.
     *
     * @type {*}
     * @readOnly
     */
    get config() {
      return this._config;
    }
  }, /** @lends pentaho.config.ExternalAnnotation */{
    /**
     * Gets the type of annotation.
     *
     * @type {string}
     * @readOnly
     * @override
     */
    get id() {
      return module.id;
    },

    /**
     * Gets the priority of the configuration.
     *
     * The internal configuration has priority 0.
     * while this property defaults to `-1`.
     *
     * @type {number}
     * @readOnly
     * @default -1
     */
    get priority() {
      return -1;
    }
  });
});
