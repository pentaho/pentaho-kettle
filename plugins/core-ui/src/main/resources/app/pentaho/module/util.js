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
  "../util/requireJS",
  "../util/object",
  "../lang/OperationInvalidError",
  "../lang/ArgumentRequiredError"
], function(requireJSUtil, O, OperationInvalidError, ArgumentRequiredError) {

  "use strict";

  var moduleIdsMapByContextId = requireJSUtil.config().map || {};

  /**
   * Parses a versioned module id like:
   *
   * * `pentaho/visual/models/Bar` into
   *
   *     `[undefined, undefined, "pentaho/visual/models/Bar"]`
   *
   * * `pentaho-core@1.2.20-SNAPSHOT/pentaho/visual/models/Bar` into
   *
   *     `["pentaho-core", "1.2.20-SNAPSHOT", "pentaho/visual/models/Bar"]`
   *
   * * `@hitachivantara/pentaho-core@1.2.20-SNAPSHOT/pentaho/visual/models/Bar` into
   *
   *     `["@hitachivantara/pentaho-core", "1.2.20-SNAPSHOT", "pentaho/visual/models/Bar"]`
   *
   *
   * @type RegExp
   * @private
   */
  var reVersionedModuleId = /^(?:((?:@[^/@]+\/)?[^/@]+)@([^/@]+)\/)?(.*)$/;

  /**
   * The `util` namespace contains utility function for working with modules.
   *
   * @namespace
   * @memberOf pentaho.module
   * @amd pentaho/module/util
   */
  var util = {
    getBaseIdOf: function(id) {
      return id && id.replace(/.[^/]+$/, "");
    },

    getLeafIdOf: function(id) {
      var match = id && /[^/]+$/.exec(id);
      return match !== null ? match[0] : null;
    },

    getId: function(localRequire) {
      // A global `require` function has no "module", nor id...
      if(localRequire.undef) {
        return null;
      }

      return localRequire("module").id;
    },

    absolutizeIdRelativeToSibling: function(id, siblingId) {
      return this.absolutizeId(id, this.getBaseIdOf(siblingId));
    },

    absolutizeId: function(id, baseId) {
      if(id && /^\./.test(id) && !/\.js$/.test(id)) {
        var baseIds = baseId ? baseId.split("/") : [];
        var ids = id.split("/");
        var needsBase = false;

        while(ids.length) {
          var segment = ids[0];
          if(segment === ".") {
            ids.shift();
            needsBase = true;
          } else if(segment === "..") {
            if(!baseIds.pop()) {
              throw new OperationInvalidError("Invalid path: '" + id + "'.");
            }
            ids.shift();
            needsBase = true;
          } else {
            break;
          }
        }

        if(needsBase) {
          baseId = baseIds.join("/");
          id = ids.join("/");

          return (baseId && id) ? (baseId + "/" + id) : (baseId || id);
        }
      }

      return id;
    },

    /**
     * Resolves a module identifier as if it were a dependency of another module.
     *
     * Resolving makes `moduleId` absolute, relative to `dependentId`.
     *
     * Afterwards, any applicable RequireJS contextual mapping configuration is applied.
     *
     * @param {string} moduleId - The identifier of the module to be resolved.
     * @param {?string} dependentId - The module that depends on `moduleId`.
     * @return {string} The identifier of the resolved module.
     */
    resolveModuleId: function(moduleId, dependentId) {

      if(moduleId.indexOf("!") !== -1) {
        var parts = moduleId.split("!", 2);
        return util.resolveModuleId(parts[0], dependentId) + "!" + util.resolveModuleId(parts[1], dependentId);
      }

      var absModuleId = util.absolutizeIdRelativeToSibling(moduleId, dependentId);

      // Get the module id map applicable to forId.
      var dependentMap = getModuleIdMap(dependentId);

      // "Look up" moduleId in dependentMap, if any.
      return dependentMap !== null ? mapModuleId(dependentMap, absModuleId) : absModuleId;
    },

    /**
     * Parses a module identifier produced by the Web Package system.
     *
     * It is assumed to have a structure similar to:
     *
     * * `@hitachivantara/pentaho-core@1.2.20/pentaho/visual/models/Bar`
     *
     * The package name and version part is optional and when not present,
     * the whole `package` property will be returned null.
     *
     * @param {string} id - The module identifier.
     * @return {({package: ?({name: string, version}), name: string})} The parsed module information.
     *
     * @throws {pentaho.lang.ArgumentInvalidError} When the module identifier has an invalid syntax.
     *
     * @private
     */
    parseId: function(id) {
      if(id == null) {
        throw new ArgumentRequiredError("id");
      }

      var match = reVersionedModuleId.exec(id);
      // assert match !== null
      return {
        package: match[1] ? {name: match[1], version: match[2]} : null,
        name: match[3]
      };
    }
  };

  return util;

  /**
   * Gets a module map used to resolve the dependencies of a given module.
   *
   * @param {?string|undefined} forId - The identifier of the module for which the module map is desired.
   *
   * @return {Object.<string, string>} A map of module identifier to module identifier.
   */
  function getModuleIdMap(forId) {

    var moduleIdsMap;

    if(forId == null) {
      // When no forId, use the global map, if any.
      moduleIdsMap = moduleIdsMapByContextId["*"] || null;
    } else {
      // Is there an exact match map?
      moduleIdsMap = O.getOwn(moduleIdsMapByContextId, forId);
      if(moduleIdsMap === undefined) {

        // Find a map applicable to any of the "ancestor" modules,
        // ending on the global/* map.

        var closestAncestorModuleId = "";

        for(var candidateBaseModuleId in moduleIdsMapByContextId) {
          // moduleId startsWith candidateBaseModuleId and this is a longer match.
          if(candidateBaseModuleId !== "*" &&
             forId.indexOf(candidateBaseModuleId) === 0 &&
             candidateBaseModuleId.length > closestAncestorModuleId.length) {

            closestAncestorModuleId = candidateBaseModuleId;
          }
        }

        if(closestAncestorModuleId.length === 0) {
          closestAncestorModuleId = "*";
        }

        moduleIdsMap = moduleIdsMapByContextId[closestAncestorModuleId] || null; // * may not be defined.
      }
    }

    return moduleIdsMap;
  }

  /**
   * Maps a given module identifier using a module map.
   *
   * @param {Object.<string, string>} moduleIdsMap - A map of module identifier to module identifier.
   * @param {string} moduleId - The module identifier to map.
   *
   * @return {string} The mapped module identifier.
   */
  function mapModuleId(moduleIdsMap, moduleId) {

    var baseModuleId = moduleId;

    // Is there not an exact match?
    if(!O.hasOwn(moduleIdsMap, moduleId)) {

      // Find the closest ancestor module.
      var closestAncestorModuleId = "";

      for(var candidateBaseModuleId in moduleIdsMap) {
        // moduleId startsWith candidateBaseModuleId and this is a longer match.
        if(moduleId.indexOf(candidateBaseModuleId) === 0 &&
           candidateBaseModuleId.length > closestAncestorModuleId.length) {

          closestAncestorModuleId = candidateBaseModuleId;
        }
      }

      if(closestAncestorModuleId.length === 0) {
        return moduleId;
      }

      baseModuleId = closestAncestorModuleId;
    }

    var moduleIdLeaf = moduleId.substring(baseModuleId.length);
    if(moduleIdLeaf.length > 0 && moduleIdLeaf.indexOf("/") !== 0) {
      // false positive, we just caught a substring (probably some old mapping that included an hardcoded version)
      return moduleId;
    }

    var mappedBaseModuleId = moduleIdsMap[baseModuleId];

    return mappedBaseModuleId + moduleIdLeaf;
  }
});
