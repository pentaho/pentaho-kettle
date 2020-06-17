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
  "../../lang/SortedList",
  "../../lang/ArgumentRequiredError",
  "../../lang/ArgumentInvalidError",
  "../../util/object",
  "../../util/fun",
  "../../util/text"
], function(SortedList, ArgumentRequiredError, ArgumentInvalidError, O, F, textUtil) {

  var nextModuleIndex = 1;

  return function(core) {

    /**
     * @classDesc The `MetaService` class is the implementation of the
     * [IMetaService]{@link pentaho.module.IMetaService} interface.
     *
     * @memberOf pentaho._core.module
     * @class
     * @implements {pentaho.module.IMetaService}
     *
     * @description Constructs a module metadata service instance.
     * @constructor
     */
    function MetaService() {

      /**
       * The map of modules' metadata by identifier and alias.
       *
       * @type {Object.<string, pentaho.module.IMeta>}
       * @private
       */
      this.__moduleMap = Object.create(null);
    }

    MetaService.prototype = /** @lends pentaho._core.module.MetaService# */{

      /**
       * Gets a module's metadata given its identifier or alias.
       *
       * @param {string} idOrAlias - The identifier or alias of the module.
       * @return {pentaho.module.IMeta} The module metadata or `null`.
       * @private
       */
      __get: function(idOrAlias) {
        return O.getOwn(this.__moduleMap, idOrAlias, null);
      },

      configure: function(moduleSpecMap) {

        if(moduleSpecMap == null) {
          return this;
        }

        var me = this;

        var preparedModuleSpecMap = {};
        var preparedModuleIdList = [];

        O.eachOwn(moduleSpecMap, prepareModule, this);

        preparedModuleIdList.forEach(function(id) {
          moduleResolver(id);
        });

        return this;

        // ---

        // 1. Perform basic validation and index by id and alias.
        function prepareModule(moduleSpec, id) {
          if(!id) {
            throw new ArgumentInvalidError(
              "moduleSpecMap",
              "Module map contains an empty module identifier.");
          }

          if(!moduleSpec) {
            throw new ArgumentInvalidError(
              "moduleSpecMap",
              "Module with id '" + id + "' provides no specification.");
          }

          // ---

          var module = this.__get(id);
          if(module !== null) {
            module.__configure(moduleSpec);
          } else {

            if(O.hasOwn(preparedModuleSpecMap, id)) {
              throw new ArgumentInvalidError(
                "moduleSpecMap",
                "A module with the id or alias '" + id + "' is already being defined.");
            }

            moduleSpec = Object.create(moduleSpec);
            moduleSpec.id = id;
            moduleSpec.index = nextModuleIndex++;

            preparedModuleIdList.push(id);

            // Index by id and by alias, if any.
            preparedModuleSpecMap[id] = moduleSpec;

            var alias = textUtil.nonEmptyString(moduleSpec.alias);
            if(alias !== null && alias !== id) {
              if(this.__get(alias) !== null || O.hasOwn(preparedModuleSpecMap, alias)) {
                throw new ArgumentInvalidError(
                  "moduleSpecMap",
                  "Module with id '" + id + "' specifies an alias which is already taken: '" + alias + "'.");
              }

              preparedModuleSpecMap[alias] = moduleSpec;
            }
          }
        }

        function moduleResolver(idOrAlias, expectedKind) {

          var module = me.__get(idOrAlias);
          if(module === null) {
            var preparedModuleSpec = O.getOwn(preparedModuleSpecMap, idOrAlias, null);
            if(preparedModuleSpec === null) {
              throw new ArgumentInvalidError(
                "moduleSpecMap",
                "A module with id '" + idOrAlias + "' is not defined.");
            }

            module = createModule(preparedModuleSpec);
          }

          if(expectedKind && module.kind !== expectedKind) {
            throw new ArgumentInvalidError(
              "moduleSpecMap",
              "Expected module '" + module.id + "' to be " + (expectedKind === "type" ? "a " : "an ") + expectedKind);
          }

          return module;
        }

        function createModule(preparedModuleSpec) {

          var module;
          if(("ancestor" in preparedModuleSpec) || ("base" in preparedModuleSpec)) {
            // A Type module.
            module = new core.TypeModuleMeta(preparedModuleSpec.id, preparedModuleSpec, moduleResolver);
          } else {
            // An Instance module.
            module = new core.InstanceModuleMeta(preparedModuleSpec.id, preparedModuleSpec, moduleResolver);
          }

          me.__registerModule(module);

          // Register annotation rules.
          if(preparedModuleSpec.annotations) {

          }

          return module;
        }
      },

      /**
       * Registers a module.
       *
       * @param {pentaho.module.IMeta} module - The module.
       * @private
       */
      __registerModule: function(module) {

        this.__moduleMap[module.id] = module;

        if(module.alias !== null) {
          this.__moduleMap[module.alias] = module;
        }
      },

      get: function(idOrAlias, keyArgs) {

        var module = this.__get(idOrAlias);

        if(module === null) {
          if(O.getOwn(keyArgs, "createIfUndefined", false)) {
            // `resolver` is not used when there is no type.
            module = new core.InstanceModuleMeta(idOrAlias, {}, null);

            this.__registerModule(module);

          } else if(O.getOwn(keyArgs, "assertDefined", false)) {
            throw new ArgumentInvalidError(
              "idOrAlias",
              "A module with id or alias '" + idOrAlias + "' is not defined.");
          }
        }

        return module;
      },

      getId: function(idOrAlias) {
        var module = this.get(idOrAlias);
        return module && module.id;
      },

      getInstancesOf: function(typeIdOrAlias) {

        var instances = __createModulesList();

        var type = __getTypeModule(this, typeIdOrAlias, "typeIdOrAlias");
        if(type !== null) {
          __collectInstanceModules(instances, type);
        }

        return instances;
      },

      getInstanceOf: function(typeIdOrAlias) {

        var instances = this.getInstancesOf(typeIdOrAlias);

        return instances.length > 0 ? instances[0] : null;
      },

      getSubtypesOf: function(baseTypeIdOrAlias) {

        var subtypes = __createModulesList();

        var type = __getTypeModule(this, baseTypeIdOrAlias, "baseTypeIdOrAlias");
        if(type !== null) {
          __collectSubtypeModules(subtypes, type);
        }

        return subtypes;
      },

      getSubtypeOf: function(baseTypeIdOrAlias) {

        var subtypes = this.getSubtypesOf(baseTypeIdOrAlias);

        return subtypes.length > 0 ? subtypes[0] : null;
      }
    };

    return MetaService;

    /**
     * Creates an empty sorted list for holding modules' metadata.
     *
     * @return {pentaho.lang.SortedList} The sorted list.
     */
    function __createModulesList() {
      return new SortedList({comparer: __moduleComparer});
    }

    /**
     * Collects all instance modules which are of type `baseType` or of any of its descendant types.
     *
     * @param {pentaho.lang.SortedList} allInstances - The sorted list of instance modules' metadata.
     * @param {pentaho.module.ITypeMeta} baseType - The base type module.
     */
    function __collectInstanceModules(allInstances, baseType) {

      __eachSubtypeModule(baseType, function(subType) {
        var instances = subType.instances;
        if(instances.length > 0) {
          allInstances.addMany(instances);
        }
      });
    }

    /**
     * Collects all modules which descendant types of a given base type, excluding the base type itself.
     *
     * @param {pentaho.lang.SortedList} allSubtypes - The sorted list of subtypes modules' metadata.
     * @param {pentaho.module.ITypeMeta} baseType - The base type module.
     */
    function __collectSubtypeModules(allSubtypes, baseType) {

      __eachSubtypeModule(baseType, function(subType) {
        if(subType !== baseType) {
          allSubtypes.add(subType);
        }
      });
    }

    /**
     * Traverses the type hierarchy which is rooted on `baseType` and calls `funPre` in pre-order.
     *
     * @param {pentaho.module.ITypeMeta} baseType - The base type module.
     * @param {function(pentaho.module.ITypeMeta)} funPre - The function to call on each type module.
     */
    function __eachSubtypeModule(baseType, funPre) {

      funPre(baseType);

      var subtypes = baseType.subtypes;
      var i = subtypes.length;
      while(i--) {
        __eachSubtypeModule(subtypes[i], funPre);
      }
    }

    /**
     * Gets a type module given its identifier or alias.
     *
     * @param {pentaho.module.IMetaService} metaService - The module metadata service.
     * @param {nonEmptyString} idOrAlias - The identifier or alias of the module.
     * @param {nonEmptyString} argName - The name of the argument.
     *
     * @return {pentaho.module.ITypeMeta} The desired type module.
     *
     * @throws {pentaho.lang.ArgumentInvalidError} When the module is defined but is not a type module.
     */
    function __getTypeModule(metaService, idOrAlias, argName) {

      var module = metaService.get(idOrAlias);

      if(module !== null && module.kind !== "type") {
        throw new ArgumentInvalidError(
          argName,
          "Expected module with id or alias '" + idOrAlias + "' to be a type module.");
      }

      return module;
    }

    /**
     * The module comparer function.
     *
     * Implements the global order of modules: ranking descending + index ascending.
     *
     * @param {pentaho.module.ITypeMeta} moduleA - The first module.
     * @param {pentaho.module.ITypeMeta} moduleB - The second module.
     *
     * @return {number} The comparison result.
     */
    function __moduleComparer(moduleA, moduleB) {
      return F.compare(moduleB.ranking, moduleA.ranking) ||
             F.compare(moduleA.__index, moduleB.__index);
    }
  };
});
