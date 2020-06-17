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
  "require",
  "module",
  "pentaho/config/ExternalAnnotation",
  "pentaho/module/util",
  "pentaho/i18n",
  "pentaho/shim/es6-promise"
], function(localRequire, module, ExternalConfigAnnotation, moduleUtil, i18nService) {

  var LoadConfigAnnotation = ExternalConfigAnnotation.extend(module.id, /** @lends pentaho.i18n.LoadConfigAnnotation# */{
    /**
     * @classDesc The `LoadConfigAnnotation` loads a module's i18n message bundle as
     * a source of external module configuration.
     *
     * The i18n configuration information is loaded with a lower priority than
     * that of the internal configuration.
     *
     * @name LoadConfigAnnotation
     * @memberOf pentaho.i18n
     * @class
     * @extend pentaho.config.ExternalAnnotation
     * @private
     *
     * @description Creates a _load i18n configuration_ annotation associated with a given module.
     * @constructor
     * @param {pentaho.module.IMeta} forModule - The annotated module.
     * @param {*} config - The associated configuration.
     */
  }, /** @lends pentaho.i18n.LoadConfigAnnotation */{
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
     * This property has the value `-10`.
     *
     * @type {number}
     * @readOnly
     * @default -10
     * @override
     */
    get priority() {
      return -10;
    },

    /**
     * Creates an asynchronous annotation, given the annotated module and the annotation specification.
     *
     * @param {pentaho.module.IMeta} forModule - The annotated module.
     * @param {object} annotSpec - The annotation specification.
     * @return {Promise.<pentaho.i18n.LoadConfigAnnotation>} A promise that resolves to the created annotation.
     * @override
     */
    createAsync: function(forModule, annotSpec) {

      return new Promise(function(resolve, reject) {

        var leafId = moduleUtil.getLeafIdOf(forModule.id);
        var bundleId = forModule.resolveId("./i18n/" + leafId);
        var config = {};
        var onLoad = function(bundle) {
          resolve(bundle.structured);
        };

        onLoad.error = reject;

        i18nService.load(bundleId, localRequire, onLoad, config);
      });
    }
  });

  return LoadConfigAnnotation;
});
