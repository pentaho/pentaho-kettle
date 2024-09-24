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
