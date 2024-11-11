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
