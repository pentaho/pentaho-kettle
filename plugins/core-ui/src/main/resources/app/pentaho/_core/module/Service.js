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
define(function() {

  var FAILED_LOAD_MODULE_VALUE = {};

  /**
   * @classDesc The `Service` class is the implementation of the [IService]{@link pentaho.module.IService} interface.
   *
   * @memberOf pentaho._core.module
   * @class
   * @implements {pentaho.module.IService}
   *
   * @description Constructs a module service instance.
   * @constructor
   * @param {pentaho.module.IMetaService} metaService - The modules' metadata service.
   */
  function Service(metaService) {
    this.__metaService = metaService;
  }

  Service.prototype = /** @lends pentaho._core.module.Service# */{

    getInstancesOfAsync: function(typeIdOrAlias) {
      try {
        var instances = this.__metaService.getInstancesOf(typeIdOrAlias);
        return __loadModulesAsync(instances);
      } catch(ex) {
        return Promise.reject(ex);
      }
    },

    getInstanceOfAsync: function(typeIdOrAlias) {
      try {
        var instance = this.__metaService.getInstanceOf(typeIdOrAlias);

        return instance !== null ? instance.loadAsync() : Promise.resolve(undefined);
      } catch(ex) {
        return Promise.reject(ex);
      }
    },

    getSubtypesOfAsync: function(baseTypeIdOrAlias) {
      try {
        var subtypes = this.__metaService.getSubtypesOf(baseTypeIdOrAlias);

        return __loadModulesAsync(subtypes);
      } catch(ex) {
        return Promise.reject(ex);
      }
    },

    getSubtypeOfAsync: function(baseTypeIdOrAlias) {
      try {
        var subtype = this.__metaService.getSubtypeOf(baseTypeIdOrAlias);

        return subtype !== null ? subtype.loadAsync() : Promise.resolve(undefined);
      } catch(ex) {
        return Promise.reject(ex);
      }
    }
  };

  return Service;

  /**
   * Loads a series of modules given their metadata. Returns an array of values
   * for the successfully loaded modules.
   *
   * @param {Array.<pentaho.module.IMeta>} moduleMetas - The metadata of modules to load.
   *
   * @return {Promise.<Array>} A promise for the array of successfully loaded modules' values.
   */
  function __loadModulesAsync(moduleMetas) {
    if(moduleMetas.length > 0) {
      var valuePromises = moduleMetas.map(function(moduleMeta) {
        return moduleMeta.loadAsync()
          // eslint-disable-next-line dot-notation,no-unexpected-multiline
          ["catch"](function() {
            return FAILED_LOAD_MODULE_VALUE;
          });
      });

      return Promise.all(valuePromises)
        .then(function(values) {
          return values.filter(__filterSuccessfullyLoadedModule);
        });
    }

    return Promise.resolve([]);
  }

  /**
   * Indicates if a value is not the failed load value.
   *
   * @param {*} value - A value to test.
   *
   * @return {boolean} `true` if the value is not a failed load value; `false`, otherwise.
   */
  function __filterSuccessfullyLoadedModule(value) {
    return value !== FAILED_LOAD_MODULE_VALUE;
  }
});
