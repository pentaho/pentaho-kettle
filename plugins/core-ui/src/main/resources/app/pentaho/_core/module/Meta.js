/*!
 * Copyright 2018 - 2019 Hitachi Vantara. All rights reserved.
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
  "../../lang/Base",
  "../../debug",
  "../../debug/Levels",
  "../../util/object",
  "../../util/requireJS",
  "../../util/logger",
  "../../util/promise",
  "../../util/text",
  "../../util/fun",
  "../../util/spec",
  "../../util/arg",
  "../../module/util",
  "../../lang/ArgumentRequiredError",
  "../../lang/OperationInvalidError"
], function(module, Base, debugMgr, DebugLevels, O, requireJSUtil, logger, promiseUtil, textUtil, F,
            specUtil, argUtil, moduleUtil, ArgumentRequiredError, OperationInvalidError) {

  "use strict";

  /**
   * Module is created, yet not loaded or prepared.
   * @type {number}
   * @default 0
   */
  var STATE_INIT = 0;

  /**
   * Module is prepared.
   *
   * Configuration and asynchronous annotations are loaded.
   *
   * @type {number}
   * @default 1
   */
  var STATE_PREPARED = 1;

  /**
   * Module is loaded.
   *
   * Module is prepared and its value has been loaded.
   *
   * @type {number}
   * @default 2
   */
  var STATE_LOADED = 2;

  /**
   * Module errored during preparation or loading.
   *
   * @type {number}
   * @default -1
   */
  var STATE_ERROR = -1;

  var LOCAL_NOT_CREATED_ANNOTATION_RESULT = Object.freeze({value: null, error: null});

  return function(core) {

    return Base.extend("pentaho._core.module.Meta", /** @lends pentaho._core.module.Meta# */{

      /**
       * @classDesc The `Meta` class implements the `module.IMeta` interface.
       *
       * @alias Meta
       * @memberOf pentaho._core.module
       * @class
       * @implements {pentaho.module.IMeta}
       * @abstract
       *
       * @description Constructs the metadata of a module.
       * @constructor
       * @param {nonEmptyString} id - The identifier of the module.
       * @param {pentaho.module.spec.IMeta} spec - The specification of the metadata of the module.
       * @param {pentaho._core.module.Resolver} [resolver] - The module resolver function.
       */
      constructor: function(id, spec, resolver) {

        this.id = id;

        /**
         * The index of definition order.
         *
         * Used to build a global order of modules (ranking descending + index ascending).
         *
         * @type {number}
         * @readOnly
         * @private
         * @internal
         */
        this.__index = spec.index || 0;

        this.alias = textUtil.nonEmptyString(spec.alias);
        if(this.alias === id) {
          this.alias = null;
        }

        this.ranking = 0;

        /**
         * The state of the module.
         *
         * @type {number}
         * @private
         */
        this.__state = STATE_INIT;

        /**
         * The value of the module, if it loaded successfully;
         * the preparation of loading error, if it failed loading;
         * `undefined`, otherwise.
         *
         * @type {*|Error|undefined}
         * @private
         */
        this.__result = undefined;

        /**
         * An object holding promises during the preparation and/or loading phases.
         *
         * @type {?({prepare: ?Promise, value: ?Promise})}
         * @private
         */
        this.__promisesControl = null;

        /**
         * Gets the configuration of the module.
         *
         * @memberOf pentaho.module.Meta#
         * @type {?object}
         * @private
         */
        this.__config = null;
        this.__configSpec = spec.config || null;

        /**
         * The store of annotations.
         *
         * Annotation id to an annotation holder.
         *
         * @type {?Object.<string, ({result: {value: ?pentaho.module.Annotation, error: ?Error}, promise: ?Promise})>}
         *
         * @private
         */
        this.__annotationsStore = null;

        this.__configure(spec);

        var value = spec.value;

        this.isVirtual = !!spec.isVirtual || value !== undefined;
        if(this.isVirtual) {
          this.__defineRequireJSValue(value);
        }
      },

      /**
       * Configures the module metadata.
       *
       * Currently, only the `ranking` and `annotations` options are supported.
       *
       * @param {pentaho.module.spec.IMeta} configSpec - The configuration specification.
       * @private
       * @internal
       */
      __configure: function(configSpec) {

        if("ranking" in configSpec) {
          this.ranking = +configSpec.ranking || 0;
        }

        // Module Definition Annotations are converted to global annotation rules.
        var annotations = configSpec.annotations;
        if(annotations != null && core.configService !== null) {

          Object.keys(annotations).forEach(function(annotationId) {
            core.configService.addRule({
              priority: -Infinity,
              select: {
                module: this.id,
                annotation: annotationId
              },
              apply: annotations[annotationId]
            });
          }, this);
        }
      },

      /** @inheritDoc */
      resolveId: function(moduleId) {
        return moduleUtil.resolveModuleId(moduleId, this.id);
      },

      isSubtypeOf: function(baseIdOrAlias) {
        return false;
      },

      isInstanceOf: function(typeIdOrAlias) {
        return false;
      },

      // region State
      /** @inheritDoc */
      get isPrepared() {
        return this.__state >= STATE_PREPARED;
      },

      /** @inheritDoc */
      get isLoaded() {
        return this.__state >= STATE_LOADED;
      },

      /** @inheritDoc */
      get isRejected() {
        return this.__state === STATE_ERROR;
      },
      // endregion

      // region Preparation
      /** @inheritDoc */
      prepareAsync: function() {
        if(this.isPrepared) {
          return Promise.resolve();
        }

        if(this.isRejected) {
          return Promise.reject(this.__result);
        }

        // Load config.
        // Load all async annotations.
        // Load all sync annotation configs.
        var promisesControl = this.__getPromisesControl();
        if(promisesControl.prepare === null) {
          promisesControl.prepare = this._prepareCoreAsync();
        }

        return promisesControl.prepare;
      },

      /**
       * Actually prepares a module.
       *
       * @return {Promise} A promise for the completion of the module's preparation.
       * @private
       * @internal
       */
      _prepareCoreAsync: function() {

        var promises = [];

        if(core.configService !== null) {
          // 1. Configuration
          // RuleSet module and RuleSets themselves are initialized before the config service is created.
          promises.push(core.configService.selectAsync(this.id).then(this.__setConfig.bind(this)));

          // 2. Local Annotations
          // Come from configService as well...
          var annotationsIds = this.getAnnotationsIds();
          if(annotationsIds !== null) {
            annotationsIds.forEach(function(annotationId) {
              promises.push(this.getAnnotationAsync(annotationId));
            }, this);
          }
        }

        return Promise.all(promises).then(this.__onPrepareResolved.bind(this), this.__onLoadRejected.bind(this));
      },

      /**
       * Marks a module as prepared.
       *
       * @private
       */
      __onPrepareResolved: function() {

        this.__state = STATE_PREPARED;

        // Release memory.
        if(this.__promisesControl !== null) {
          this.__promisesControl.prepare = null;
        }
      },
      // endregion

      // region Value
      /**
       * Registers the specified value of this module with the AMD module system.
       *
       * @param {(*|(function(pentaho.module.IMeta) : *))} value - The value or value factory function.
       * Possibly undefined.
       *
       * @private
       */
      __defineRequireJSValue: function(value) {
        requireJSUtil.define(this.id, ["pentaho/module!_"], F.is(value) ? value : F.constant(value));
      },

      /** @inheritDoc */
      get value() {
        if(this.isRejected) {
          throw this.__result;
        }

        // Will be undefined if not yet loaded.
        return this.__result;
      },

      /** @inheritDoc */
      get error() {
        return this.isRejected ? this.__result : null;
      },

      /** @inheritDoc */
      loadAsync: function() {
        if(this.isLoaded) {
          return Promise.resolve(this.__result);
        }

        if(this.isRejected) {
          return Promise.reject(this.__result);
        }

        // Promise preserves value or error!
        var promisesControl = this.__getPromisesControl();

        if(promisesControl.value === null) {

          promisesControl.value = Promise.all([requireJSUtil.promise(this.id), this.prepareAsync()])
            .then(
              function(results) { return this.__onLoadResolved(results[0]); }.bind(this),
              this.__onLoadRejected.bind(this));
        }

        return promisesControl.value;
      },

      /**
       * Gets the promises control. Creates the object if it isn't created yet.
       *
       * @return {({prepare: ?Promise, value: ?Promise})} The promises control.
       * @private
       */
      __getPromisesControl: function() {
        return this.__promisesControl || (this.__promisesControl = {prepare: null, value: null});
      },

      /**
       * Marks the module as loaded, given its value.
       *
       * @param {*} value - The module's value.
       * @return {*} The module's value.
       * @private
       */
      __onLoadResolved: function(value) {

        this.__state = STATE_LOADED;
        this.__result = value;

        // Release memory.
        this.__promisesControl = null;

        if(debugMgr.testLevel(DebugLevels.info, module)) {
          logger.info("Loaded module '" + this.id + "'.");
        }

        return value;
      },

      /**
       * Marks the module as rejected, given a preparation or loading error.
       *
       * @param {*} error - The module's preparation or loading error.
       * @return {Promise.<Error>} A rejected promise with an error.
       * @private
       */
      __onLoadRejected: function(error) {

        // May already have been rejected in the preparation catch and is now passing through the load catch.
        if(this.__state !== STATE_ERROR) {

          if(typeof error === "string") {
            error = new Error(error);
          }

          this.__state = STATE_ERROR;
          this.__result = error;

          // Release memory.
          this.__promisesControl = null;

          if(debugMgr.testLevel(DebugLevels.error, module)) {
            logger.error("Failed loading module '" + this.id + "'. Error: " + error.message);
          }
        }

        return Promise.reject(this.__result);
      },
      // endregion

      // region Configuration
      /** @inheritDoc */
      get config() {
        return this.__config;
      },

      /**
       * Sets the configuration of the module.
       *
       * @param {?object} config - The module's configuration or `null`.
       * @private
       */
      __setConfig: function(config) {
        this.__config = config;
      },
      // endregion

      // region Annotations
      /** @inheritDoc */
      hasAnnotation: function(Annotation, keyArgs) {

        var annotationId = getAnnotationId(Annotation);

        return this._hasAnnotationCore(annotationId, keyArgs);
      },

      /**
       * Determines if this module is annotated with an annotation of a given type.
       *
       * Note that an annotation of the given type may be associated with this module and,
       * still, not have been loaded yet!
       * You may want to use this method to avoid a call to
       * {@link pentaho.module.IMeta#getAnnotationAsync} which would resolve to `null`.
       *
       * @name hasAnnotation
       * @memberOf pentaho.module.IMeta#
       * @param {string} annotationId - The annotation identifier.
       * @param {object} [keyArgs] - The keyword arguments object.
       * @param {boolean} [keyArgs.inherit=false] - Indicates that if an annotation of the specified type is not present
       * in this module and this is a type module,
       * an annotation of the specified type and from ancestor modules may be considered.
       *
       * @return {boolean} `true` if an annotation of the given type is associated with this module; `false`, otherwise.
       * @private
       * @internal
       */
      _hasAnnotationCore: function(annotationId, keyArgs) {

        var holder = this._getAnnotationHolder(annotationId);
        if(holder !== null) {
          return true;
        }

        // RuleSet module and RuleSets themselves are initialized before the config service is created.
        return !this.isPrepared &&
          core.configService !== null &&
          core.configService.hasAnnotation(this.id, annotationId);
      },

      _getAnnotationHolder: function(annotationId) {
        return O.getOwn(this.__annotationsStore, annotationId, null);
      },

      /** @inheritDoc */
      getAnnotationsIds: function(keyArgs) {

        if(this.isPrepared) {
          return this.__annotationsStore === null ? null : Object.keys(this.__annotationsStore);
        }

        // Assuming that all annotations come from the config. service.
        // RuleSet module and RuleSets themselves are initialized before the config service is created.
        if(core.configService !== null) {
          return core.configService.getAnnotationsIds(this.id);
        }

        return null;
      },

      /** @inheritDoc */
      getAnnotation: function(Annotation, keyArgs) {

        var annotationId = getAnnotationId(Annotation);

        var annotationResult = this._getAnnotationResult(annotationId, keyArgs);

        var isMissing = annotationResult == null || annotationResult === LOCAL_NOT_CREATED_ANNOTATION_RESULT;
        if(isMissing) {
          if(argUtil.optional(keyArgs, "assertResult")) {
            throw createErrorAnnotationNotAvailable(this.id, annotationId);
          }

          return null;
        }

        if(annotationResult.error !== null) {
          throw annotationResult.error;
        }

        return annotationResult.value;
      },

      /** @inheritDoc */
      getAnnotationAsync: function(Annotation, keyArgs) {
        if(Annotation == null) {
          return Promise.reject(new ArgumentRequiredError("Annotation"));
        }

        var annotationId;
        if(typeof Annotation === "string") {
          annotationId = Annotation;
          Annotation = null;
        } else {
          annotationId = Annotation.id;
          if(annotationId == null) {
            return Promise.reject(new ArgumentRequiredError("Annotation.id"));
          }
        }

        // Obtain Annotation class if not given.
        var annotationClassPromise = Annotation === null
          ? core.moduleMetaService.get(annotationId, {createIfUndefined: true}).loadAsync()
          : Promise.resolve(Annotation);

        var me = this;

        return annotationClassPromise
          .then(function(Annotation) {
            return me._getAnnotationCoreAsync(Annotation, annotationId, keyArgs);
          })
          .then(function(annotation) {
            if(annotation === null && argUtil.optional(keyArgs, "assertResult")) {
              return Promise.reject(createErrorAnnotationNotAvailable(me.id, annotationId));
            }

            return annotation;
          });
      },

      /**
       * Really gets an annotation, asynchronously.
       *
       * @param {Class.<pentaho.module.Annotation>} Annotation - The constructor of the annotation.
       * @param {string} annotationId - The annotation identifier.
       * @param {?object} keyArgs - The keyword arguments object.
       * @param {boolean} [keyArgs.inherit=false] - Indicates that when this is a type module and
       * the annotation is not present locally, it can be inherited from an ancestor module.
       *
       * @return {?Promise.<pentaho.module.Annotation>} A promise for the annotation,
       * if the result is settled; `null` if no local annotation exists.
       *
       * @private
       * @internal
       */
      _getAnnotationCoreAsync: function(Annotation, annotationId, keyArgs) {

        // Already loading/loaded?
        var holder = this._getAnnotationHolder(annotationId);
        if(holder !== null) {
          // assert holder.promise
          return holder.promise;
        }

        // If the module is prepared, all locally defined annotations are already created,
        //  so the above holder would have not been null. Surely does not exist locally, but may still be inherited.

        // Is the module not prepared and there is a configuration for the annotation?
        if(!this.isPrepared && core.configService !== null && core.configService.hasAnnotation(this.id, annotationId)) {

          var annotationPromise = core.configService.selectAsync(this.id, annotationId)
            .then(function(annotationSpec) {
              return Annotation.createAsync(this, annotationSpec);
            }.bind(this))
            .then(function(annotation) {
              holder.result = {value: annotation, error: null};
              return annotation;
            }, function(error) {
              holder.result = {value: null, error: error};
              return Promise.reject(error);
            });

          (this.__annotationsStore || (this.__annotationsStore = Object.create(null)))[annotationId] = holder = {
            result: null,
            promise: annotationPromise
          };

          return annotationPromise;
        }

        // Not settled. May inherit
        return null;
      },

      /**
       * Gets an available annotation result.
       *
       * @param {string} annotationId - The annotation identifier.
       * @param {?object} keyArgs - The keyword arguments object.
       * @param {boolean} [keyArgs.inherit=false] - Indicates that when this is a type module and
       * the annotation is not present locally, it can be inherited from an ancestor module.
       *
       * @return {?{value: pentaho.module.Annotation, error: ?Error}} The annotation result or `null`.
       *
       * @private
       * @internal
       */
      _getAnnotationResult: function(annotationId, keyArgs) {

        var holder = this._getAnnotationHolder(annotationId);
        if(holder !== null) {
          // May be on the way.
          return holder.result || LOCAL_NOT_CREATED_ANNOTATION_RESULT;
        }

        if(!this.isPrepared &&
           core.configService !== null &&
           core.configService.hasAnnotation(this.id, annotationId)) {

          // There is a local annotation. But its not created.
          // Return a settled, yet null, result to prevent inheritance.
          return LOCAL_NOT_CREATED_ANNOTATION_RESULT;
        }

        return null;
      }
    });

    // region Annotation helpers
    /**
     * Creates an error for when an annotation is not present or otherwise currently available in a module.
     *
     * @param {string} moduleId - The module identifier.
     * @param {string} annotationId - The annotation identifier.
     *
     * @return {pentaho.lang.OperationInvalidError} The "not present" error.
     */
    function createErrorAnnotationNotAvailable(moduleId, annotationId) {
      return new OperationInvalidError(
        "The annotation '" + annotationId + "' is not available in module '" + moduleId + "'.");
    }

    /**
     * Gets the identifier of an annotation, given its type or its identifier.
     *
     * @param {Class.<pentaho.module.Annotation>|string} Annotation - The annotation class or identifier.
     *
     * @return {string} The annotation identifier.
     *
     * @throws {pentaho.lang.ArgumentRequiredError} When the `Annotation` argument is _nully_.
     * @throws {pentaho.lang.ArgumentRequiredError} When the `Annotation` argument does not have an `id` property.
     */
    function getAnnotationId(Annotation) {
      if(Annotation == null) {
        throw new ArgumentRequiredError("Annotation");
      }

      if(typeof Annotation === "string") {
        return Annotation;
      }

      var annotationId = Annotation.id;
      if(annotationId == null) {
        throw new ArgumentRequiredError("Annotation.id");
      }

      return annotationId;
    }
    // endregion
  };
});
