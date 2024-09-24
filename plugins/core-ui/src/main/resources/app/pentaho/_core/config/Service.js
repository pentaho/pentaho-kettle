/*!
 * Copyright 2010 - 2019 Hitachi Vantara. All rights reserved.
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
  "../../lang/SortedList",
  "../../lang/ArgumentRequiredError",
  "../../util/spec",
  "../../util/object",
  "../../util/requireJS",
  "../../util/fun",
  "../../module/util",
  "../../module/Annotation"
], function(module, Base, SortedList, ArgumentRequiredError, specUtil, O, requireJSUtil, F, moduleUtil, Annotation) {

  "use strict";

  /**
   * List of names of environment variables that are handled "generically" when sorting rules.
   * More specific first.
   *
   * @type {string[]}
   * @see pentaho.environment.IEnvironment
   * @see __ruleComparer
   * @see __ruleFilterer
   */
  var __selectCriteria = [
    "user", // TODO: is now user.id and will not have effect as is
    "theme",
    "locale",
    "application"
  ];

  var CRITERIA_COUNT = __selectCriteria.length;

  return function(core) {

    /**
     * The ordinal value of the next rule that is registered.
     *
     * This is used as the fallback rule order.
     * Ensures sorting algorithm stability, because insertion order would be lost during a re-sort.
     *
     * @type {number}
     *
     * @see pentaho.config.IService#addRule
     */
    var __ruleCounter = 0;

    var ConfigurationService = Base.extend(module.id, /** @lends pentaho._core.config.Service# */{

      /**
       * @classDesc The `Service` class is an in-memory implementation of
       * the {@link pentaho.config.IService} interface.
       *
       * @alias Service
       * @memberOf pentaho._core.config
       * @class
       * @extends pentaho.lang.Base
       * @implements {pentaho.config.IService}
       *
       * @description Creates a configuration service instance for a given environment.
       *
       * @param {?pentaho.environment.IEnvironment} [environment] - The environment used to select configuration rules.
       * @param {function(string) : Promise.<?({priority: number, config: object})>} [selectExternalAsync]
       * - An asynchronous callback function for obtaining the external configuration of a module given its identifier.
       */
      constructor: function(environment, selectExternalAsync) {

        /**
         * The environment used to select configuration rules.
         * @type {pentaho.environment.IEnvironment}
         * @readOnly
         */
        this.__environment = environment || {};

        /**
         * A map connecting a module and annotation identifier to the applicable configuration rules,
         * ordered from least to most specific.
         *
         * @type {Object.<string, Array.<pentaho.config.spec.IRule>>}
         * @private
         *
         * @see __buildRuleKey
         */
        this.__ruleStore = Object.create(null);

        /**
         * A map connecting a module identifier to an object whose keys are the identifiers of existing annotations.
         *
         * @type {Object.<string, Object.<string, boolean>>}
         * @private
         */
        this.__moduleAnnots = Object.create(null);

        /**
         * A function which, given module identifier, returns
         * a promise for an array, possibly null, of external configurations, including priorities.
         *
         * @type {?(function(string, string) : Promise.<?({priority: number, config: object})>)}
         * @private
         */
        this.__selectExternalAsync = selectExternalAsync || null;
      },

      /**
       * Adds a configuration rule set.
       *
       * @param {?pentaho.config.spec.IRuleSet} ruleSet - A configuration rule set to add.
       */
      add: function(ruleSet) {

        if(ruleSet && ruleSet.rules) {

          var contextId = ruleSet.contextId || null;

          ruleSet.rules.forEach(function(rule) {
            this.addRule(rule, contextId);
          }, this);
        }
      },

      /**
       * Adds a configuration rule.
       *
       * The insertion order is used as the fallback rule order.
       * For more information on the specificity of rules,
       * see [config.spec.IRuleSet]{@link pentaho.config.spec.IRuleSet}.
       *
       * Note that the specified rule object may be slightly modified to serve
       * the service's internal needs.
       *
       * @param {pentaho.config.spec.IRule} rule - The configuration rule to add.
       * @param {?string} [contextId] - The module identifier to which rule `modules` and `deps`
       * are relative to. Also, this module determines any applicable AMD/RequireJS mappings.
       *
       * @throw {pentaho.lang.OperationInvalidError} When `rule` has relative dependencies and `contextId`
       * is not specified.
       */
      addRule: function(rule, contextId) {

        // Assuming the Service takes ownership of the rules,
        // so mutating it directly is ok.
        rule._ordinal = __ruleCounter++;

        var select = rule.select || {};

        var moduleIds = select.module;
        if(!moduleIds) {
          throw new ArgumentRequiredError("rule.select.module");
        }

        var applicationId = select.application;
        if(applicationId) {
          if(Array.isArray(applicationId)) {
            select.application = applicationId.map(function(appId) {
              return resolveId(appId, contextId);
            });
          } else {
            select.application = resolveId(applicationId, contextId);
          }
        }

        if(this.__applySelector(select)) {

          var annotationId = select.annotation || null;
          if(annotationId !== null) {
            annotationId = resolveAnnotationId(annotationId, contextId);
          }

          if(!Array.isArray(moduleIds)) {
            moduleIds = [moduleIds];
          }

          var depIds = rule.deps;
          if(depIds) {
            // Again, assuming the Service takes ownership of the rules,
            // so mutating it directly is ok.
            depIds.forEach(function(depId, index) {
              depIds[index] = resolveId(depId, contextId);
            });
          }

          moduleIds.forEach(function(moduleId) {
            if(!moduleId) {
              throw new ArgumentRequiredError("rule.select.module");
            }

            moduleId = resolveId(moduleId, contextId);

            this.__addRule(moduleId, annotationId, rule);
          }, this);
        }
      },

      /**
       * Adds one rule to the rule store,
       * associated with a module and, optionally, an annotation,
       * given their identifiers.
       *
       * @param {string} moduleId - The module identifier.
       * @param {?string} annotationId - The annotation identifier.
       * @param {pentaho.config.spec.IRule} rule - The configuration rule to add.
       *
       * @private
       */
      __addRule: function(moduleId, annotationId, rule) {

        var ruleKey = __buildRuleKey(moduleId, annotationId);

        var list = this.__ruleStore[ruleKey];
        if(!list) {
          this.__ruleStore[ruleKey] = list = new SortedList({comparer: __ruleComparer});
        }

        list.push(rule);

        if(annotationId) {
          (this.__moduleAnnots[moduleId] || (this.__moduleAnnots[moduleId] = Object.create(null)))[annotationId] = true;
        }
      },

      /**
       * Determines if a given rule selector selects the current environment.
       *
       * @param {pentaho.config.spec.IRuleSelector} select - A selector.
       *
       * @return {boolean} `true` if the selector selects the current environment; `false`, otherwise.
       * @private
       */
      __applySelector: function(select) {
        // Doing it backwards because `application` is the most common criteria...
        var i = CRITERIA_COUNT;
        var env = this.__environment;

        while(i--) {
          var key = __selectCriteria[i];

          var possibleValues = select[key];
          if(possibleValues != null) {

            var criteriaValue = env[key];
            if(criteriaValue !== undefined) {

              if(Array.isArray(possibleValues)
                ? possibleValues.indexOf(criteriaValue) === -1
                : possibleValues !== criteriaValue) {
                return false;
              }
            }
          }
        }

        return true;
      },

      /** @inheritDoc */
      selectAsync: function(moduleId, annotationId) {

        var internalConfigsPromise = this.__selectInternalAsync(moduleId, annotationId);

        // Annotations do not support external configurations,
        // as the existence of configs needs to be known a priori (getAnnotationIds and hasAnnotation).
        var selectExternalAsync = this.__selectExternalAsync;
        if(annotationId || selectExternalAsync === null) {
          return internalConfigsPromise.then(__mergeConfigs);
        }

        var externalPrioritizedConfigsPromise = Promise.resolve(selectExternalAsync(moduleId));

        return Promise.all([internalConfigsPromise, externalPrioritizedConfigsPromise])
          .then(function(results) {

            var internalConfigs = results[0];
            var externalPrioritizedConfigs = results[1];

            if(externalPrioritizedConfigs == null) {
              return __mergeConfigs(internalConfigs);
            }

            if(internalConfigs === null) {
              return __sortAndMergePrioritizedConfigs(externalPrioritizedConfigs);
            }

            // Internal and external have to be merged together, or specUtil.merge does not
            // give the same result.

            var internalPrioritizedConfigs = internalConfigs.map(function(config) {
              return {priority: 0, config: config};
            });

            externalPrioritizedConfigs.push.apply(externalPrioritizedConfigs, internalPrioritizedConfigs);

            return __sortAndMergePrioritizedConfigs(externalPrioritizedConfigs);
          });
      },

      /** @inheritDoc */
      getAnnotationsIds: function(moduleId) {
        var annotIds = this.__moduleAnnots[moduleId];
        return annotIds ? Object.keys(annotIds) : null;
      },

      /** @inheritDoc */
      hasAnnotation: function(moduleId, annotationId) {
        var annotIds = this.__moduleAnnots[moduleId];
        return annotIds ? !!annotIds[annotationId] : null;
      },

      /**
       * Selects, asynchronously, the internal configuration of a module, or a module annotation,
       * given their identifiers.
       *
       * @param {string} moduleId - The identifier of the module.
       * @param {?string} annotationId - The identifier of the module annotation.
       * @return {?Promise.<object[]>} A promise for the applicable configuration objects, ordered by priority;
       * `null`, if there are no applicable configuration rules.
       * @private
       */
      __selectInternalAsync: function(moduleId, annotationId) {

        var ruleKey = __buildRuleKey(moduleId, annotationId);

        var rules = O.getOwn(this.__ruleStore, ruleKey, null);
        if(rules === null) {
          return Promise.resolve(null);
        }

        // Collect the dependencies of all rules and
        // load them all in parallel.
        var depPromisesList = null;
        var depIndexesById = null;

        var processDependency = function(depId) {

          var depIndex = O.getOwn(depIndexesById, depId, null);
          if(depIndex === null) {
            depIndex = depPromisesList.length;
            depIndexesById[depId] = depIndex;
            depPromisesList.push(__loadDependency(depId));
          }

          return depIndex;
        };

        var createRuleConfigFactory = function(rule) {

          var isFun = F.is(rule.apply);
          var depIndexes = isFun ? [] : null;

          // Process rule dependencies.
          if(rule.deps) {

            if(depPromisesList === null) {
              depPromisesList = [];
              depIndexesById = Object.create(null);
            }

            rule.deps.forEach(function(depId) {
              var depIndex = processDependency(depId);
              if(isFun) {
                depIndexes.push(depIndex);
              }
            });
          }

          return isFun
            ? __wrapRuleConfigFactory(rule.apply, depIndexes)
            : F.constant(rule.apply);
        };

        // Collect all configs and start loading any dependencies.
        var configFactories = rules.map(createRuleConfigFactory);

        return Promise.all(depPromisesList || [])
          .then(function(depValues) {
            return configFactories.map(function(configFactory) {
              return configFactory(depValues);
            });
          });
      }
    });

    function __loadDependency(id) {
      var module = core.moduleMetaService.get(id);
      return module !== null ? module.loadAsync() : requireJSUtil.promise(id);
    }

    function resolveId(idOrAlias, contextId) {

      var id = null;

      if(idOrAlias) {
        // Not relative?
        if(idOrAlias[0] !== ".") {
          id = core.moduleMetaService.getId(idOrAlias);
        }

        if(id === null) {
          id = moduleUtil.resolveModuleId(idOrAlias, contextId);
        }
      }

      return id;
    }

    function resolveAnnotationId(idOrAlias, contextId) {

      var id = null;

      if(idOrAlias) {
        // Not relative?
        if(idOrAlias[0] !== ".") {
          id = core.moduleMetaService.getId(idOrAlias);
        }

        if(id === null) {
          id = moduleUtil.resolveModuleId(Annotation.toFullId(idOrAlias), contextId);
        }
      }

      return id;
    }

    return ConfigurationService;
  };

  function __buildRuleKey(moduleId, annotationId) {
    return moduleId + (annotationId ? ("|" + annotationId) : "");
  }

  function __mergeConfigs(configs) {
    return configs && configs.reduce(function(result, config) {
      return specUtil.merge(result, config);
    }, {});
  }

  function __sortAndMergePrioritizedConfigs(prioritizedConfigs) {
    // Sort and merge.
    // Ensure stable sort.
    prioritizedConfigs.forEach(function(prioritizedConfig, index) {
      prioritizedConfig.ordinal = index;
    });

    prioritizedConfigs.sort(__prioritizedConfigComparer);

    return prioritizedConfigs.reduce(function(result, prioritizedConfig) {
      return specUtil.merge(result, prioritizedConfig.config);
    }, {});
  }

  function __wrapRuleConfigFactory(factory, depIndexes) {

    return function ruleConfigFactoryCaller(allDepValues) {

      // Collect this rule's dependencies.
      var depValues = depIndexes.map(function(depIndex) {
        return allDepValues[depIndex];
      });

      // Call the configuration factory.
      return factory.apply(null, depValues);
    };
  }

  // region compare and select
  /**
   * Compares two type configuration rules according to specificity.
   *
   * @param {pentaho.config.spec.IRule} r1 - The first type configuration rule.
   * @param {pentaho.config.spec.IRule} r2 - The second type configuration rule.
   *
   * @return {number} `-1`, if `r1` is more specific than `r2`,
   * `1`, if `r2` is more specific than `r1`,
   * and `0` if they have the same specificity.
   */
  function __ruleComparer(r1, r2) {
    var priority1 = r1.priority || 0;
    var priority2 = r2.priority || 0;

    if(priority1 !== priority2) {
      return priority1 > priority2 ? 1 : -1;
    }

    var s1 = r1.select || {};
    var s2 = r2.select || {};

    for(var i = 0, ic = __selectCriteria.length; i !== ic; ++i) {
      var key = __selectCriteria[i];

      var isDefined1 = s1[key] != null;
      var isDefined2 = s2[key] != null;

      if(isDefined1 !== isDefined2) {
        return isDefined1 ? 1 : -1;
      }
    }

    return r1._ordinal > r2._ordinal ? 1 : -1;
  }

  function __prioritizedConfigComparer(pc1, pc2) {
    var priority1 = pc1.priority || 0;
    var priority2 = pc2.priority || 0;

    if(priority1 !== priority2) {
      return priority1 > priority2 ? 1 : -1;
    }

    return pc1.ordinal > pc2.ordinal ? 1 : -1;
  }
  // endregion
});
