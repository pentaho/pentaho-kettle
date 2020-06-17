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
  "pentaho/lang/Base",
  "pentaho/lang/ArgumentRequiredError",
  "pentaho/shim/es6-promise"
], function(module, Base, ArgumentRequiredError) {

  var reFullAnnotationId = /Annotation$/;

  return Base.extend(module.id, /** @lends pentaho.module.Annotation# */{
    /**
     * @classDesc Annotations can be associated with modules to extend their configuration space
     * for use by related third-parties.
     *
     * One annotation of each type can be associated with a module in one of two ways:
     * 1. When declaring a module,
     *    by using the [annotations]{@link pentaho.module.spec.IMeta#annotations} property,
     *    to associate any number of annotations, unconditionally.
     * 2. When configuring a module,
     *    by using the [annotation]{@link pentaho.config.spec.IRuleSelector#annotation} property,
     *    a new annotation can be associated with a module or
     *    an existing annotation can be configured.
     *    This can be done conditionally, depending on the current environment.
     *
     * By convention, the name of annotation classes ends with `Annotation`.
     * However, this suffix must *not* be used when associating an annotation with a module,
     * through either of the above methods -
     * the annotation type's [shortId]{@link pentaho.module.Annotation.shortId} must be used.
     *
     * When a module is prepared, all of its annotations are created (if not already created).
     *
     * Annotations are allowed to depend on other annotations.
     * When being created, through [Annotation.createAsync]{@link pentaho.module.Annotation.createAsync},
     * other annotations can be obtained using
     * [IMeta#getAnnotationAsync]{@link pentaho.module.IMeta#getAnnotationAsync},
     * as long as no cycles arise.
     *
     * Annotations are immutable.
     *
     * @alias Annotation
     * @memberOf pentaho.module
     * @class
     * @abstract
     *
     * @description Creates an annotation associated with a given module.
     * @constructor
     * @param {pentaho.module.IMeta} forModule - The annotated module.
     *
     * @see pentaho.module.IMeta#hasAnnotation
     * @see pentaho.module.IMeta#getAnnotationsIds
     * @see pentaho.module.IMeta#getAnnotation
     * @see pentaho.module.IMeta#getAnnotationAsync
     */
    constructor: function(forModule) {
      if(forModule == null) {
        throw new ArgumentRequiredError("forModule");
      }

      this.__forModule = forModule;
    },

    /**
     * Gets the annotated module.
     *
     * @type {pentaho.module.IMeta}
     * @readOnly
     */
    get forModule() {
      return this.__forModule;
    }
  }, /** @lends pentaho.module.Annotation */{
    /**
     * Gets the type of annotation.
     *
     * @type {string}
     * @readOnly
     */
    get id() {
      return module.id;
    },

    /**
     * Rewrites an annotation identifier so that it surely has the `Annotation` suffix.
     *
     * @param {string} annotationId - The annotation type identifier.
     * @return {string} The rewritten annotation type identifier.
     */
    toFullId: function(annotationId) {
      return reFullAnnotationId.test(annotationId) ? annotationId : (annotationId + "Annotation");
    },

    /**
     * Creates an annotation, given the annotated module and the annotation specification.
     *
     * @param {pentaho.module.IMeta} forModule - The annotated module.
     * @param {object} annotSpec - The annotation specification.
     * @return {Promise.<pentaho.module.Annotation>} A promise that resolves to the created annotation.
     */
    createAsync: function(forModule, annotSpec) {
      return Promise.resolve(new this(forModule, annotSpec));
    }
  });
});
