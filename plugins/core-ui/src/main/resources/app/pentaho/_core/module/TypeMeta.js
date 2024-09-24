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
  "../../lang/ArgumentRequiredError",
  "../../lang/ArgumentInvalidError",
  "../../util/object",
  "../../util/arg"
], function(ArgumentRequiredError, ArgumentInvalidError, O, argUtil) {

  "use strict";

  var keyArgsAssertDefined = {assertDefined: true};

  return function(core) {

    var TypeMeta = core.ModuleMeta.extend("pentaho._core.module.TypeMeta", /** @lends pentaho._core.module.TypeMeta# */{

      /**
       * @classDesc The `TypeMeta` class implements the `ITypeMeta` interface.
       *
       * @alias TypeMeta
       * @memberOf pentaho._core.module
       * @class
       * @extends pentaho._core.module.Meta
       * @implements {pentaho.module.ITypeMeta}
       *
       * @description Constructs the metadata of a type module.
       *
       * @constructor
       * @param {nonEmptyString} id - The identifier of the type module.
       * @param {pentaho.module.spec.ITypeMeta} spec - The specification of the metadata of the type module.
       * @param {pentaho._core.module.Resolver} resolver - The module resolver function.
       */
      constructor: function(id, spec, resolver) {

        this.base(id, spec, resolver);

        var ancestorId = spec.ancestor || spec.base || null;

        var ancestor = ancestorId !== null ? resolver(ancestorId, "type") : null;

        this.ancestor = ancestor;

        if(ancestor !== null) {
          ancestor.__addSubtype(this);
        }
      },

      get kind() {
        return "type";
      },

      // region Subtypes
      /**
       * The array of subtype modules.
       *
       * @type {Array.<pentaho.module.ITypeMeta>}
       * @private
       */
      __subtypes: Object.freeze([]),

      get subtypes() {
        return this.__subtypes;
      },

      /**
       * Adds a given subtype module.
       *
       * @param {pentaho.module.ITypeMeta} subtype - The subtype module.
       * @private
       * @internal
       */
      __addSubtype: function(subtype) {
        var subtypes = O.getOwn(this, "__subtypes", null);
        if(subtypes === null) {
          this.__subtypes = subtypes = [];
        }

        subtypes.push(subtype);
      },
      // endregion

      // region Instances
      /**
       * The array of instance modules.
       *
       * @type {Array.<pentaho.module.IInstanceMeta>}
       * @private
       */
      __instances: Object.freeze([]),

      get instances() {
        return this.__instances;
      },

      /**
       * Adds a given instance module.
       *
       * @param {pentaho.module.InstanceMeta} instance - The instance module.
       * @private
       * @internal
       */
      __addInstance: function(instance) {
        var instances = O.getOwn(this, "__instances", null);
        if(instances === null) {
          this.__instances = instances = [];
        }

        instances.push(instance);
      },
      // endregion

      /** @inheritDoc  */
      isSubtypeOf: function(baseIdOrAlias) {
        if(!baseIdOrAlias) {
          throw new ArgumentRequiredError("baseIdOrAlias");
        }

        var baseModule = typeof baseIdOrAlias === "string"
          ? core.moduleMetaService.get(baseIdOrAlias, keyArgsAssertDefined)
          : baseIdOrAlias;

        if(!(baseModule instanceof TypeMeta)) {
          throw new ArgumentInvalidError("baseIdOrAlias", "Must be or identify a type module.");
        }

        var subModule = this;
        do {
          if(baseModule === subModule) {
            return true;
          }

          subModule = subModule.ancestor;
        } while(subModule !== null);

        return false;
      },

      /** @override */
      _prepareCoreAsync: function() {

        if(this.ancestor !== null) {
          return this.ancestor.prepareAsync().then(this.base.bind(this));
        }

        return this.base();
      },

      /** @override */
      _hasAnnotationCore: function(annotationId, keyArgs) {

        var hasAnnotation = this.base(annotationId, keyArgs);
        if(!hasAnnotation && this.ancestor !== null && argUtil.optional(keyArgs, "inherit")) {
          hasAnnotation = this.ancestor._hasAnnotationCore(annotationId, keyArgs);
        }

        return hasAnnotation;
      },

      getAnnotationsIds: function(keyArgs) {

        var annotationIds = this.base(keyArgs);

        if(this.ancestor !== null && argUtil.optional(keyArgs, "inherit")) {
          var baseAnnotationIds = this.ancestor.getAnnotationsIds(keyArgs);
          if(baseAnnotationIds !== null) {
            if(annotationIds === null) {
              annotationIds = baseAnnotationIds;
            } else {
              // Merge the two lists.
              annotationIds = mergeStringArrays(annotationIds, baseAnnotationIds);
            }
          }
        }

        return annotationIds;
      },

      /** @override */
      _getAnnotationResult: function(annotationId, keyArgs) {

        var annotationResult = this.base(annotationId, keyArgs);

        if(annotationResult === null && this.ancestor !== null && argUtil.optional(keyArgs, "inherit")) {
          // Don't store result.
          annotationResult = this.ancestor._getAnnotationResult(annotationId, keyArgs);
        }

        return annotationResult;
      },

      /** @override */
      _getAnnotationCoreAsync: function(Annotation, annotationId, keyArgs) {

        var annotationPromise = this.base(Annotation, annotationId, keyArgs);

        if(annotationPromise === null && this.ancestor !== null && argUtil.optional(keyArgs, "inherit")) {
          // Don't store result.
          annotationPromise = this.ancestor._getAnnotationCoreAsync(Annotation, annotationId, keyArgs);
        }

        return annotationPromise;
      }
    });

    return TypeMeta;
  };

  // Assumes that as and bs don't have duplicates themselves.
  // Returns a copy if necessary.
  function mergeStringArrays(as, bs) {
    // Index `as`.
    var asMap = Object.create(null);
    var i = as.length;
    while(i--) {
      asMap[as[i]] = true;
    }

    var rs = null;
    var L = bs.length;
    i = -1;
    while(++i < L) {
      var b = bs[i];
      if(!O.hasOwn(asMap, b)) {
        if(rs === null) {
          rs = as.slice();
        }

        rs.push(b);
      }
    }

    return rs || as;
  }
});
