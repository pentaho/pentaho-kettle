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

/**
 * The Services Service
 *
 * The Services Service, a collection of endpoints used by the application
 *
 * @module services/services.service
 * @property {String} name The name of the module.
 */
define(
    [
      "pentaho/module/instancesOf!IPenFileService"
    ],
    function (fileServices) {
      "use strict";

      var factoryArray = [];
      fileServices.map(function(item) {
        factoryArray.push(item.name);
      });
      factoryArray.push(factory);
      var module = {
        name: "servicesService",
        factory: factoryArray
      };

      return module;

      /**
       * The fileService factory
       *
       * @return {Object} The fileService api
       */
      function factory() {
        var services = [];
        for (var i = 0; i < arguments.length; i++) {
          services.push(arguments[i]);
        }
        services.sort(function(a, b) {
          return a.order > b.order ? 1 : -1;
        });

        return {
          get: get,
          getByPath: getByPath
        };

        function getByPath(path, tree) {
          var currentValue = -1;
          var currentService = null;
          for (var i = 0; i < services.length; i++) {
            if (services[i].matchPath) {
              var matchValue = services[i].matchPath(path);
              if (matchValue > 0 && matchValue > currentValue && (tree && _inTree(services[i].root, tree))) {
                currentService = services[i];
                currentValue = matchValue;
              } else if (matchValue > 0 && matchValue > currentValue && !tree) {
                currentService = services[i];
                currentValue = matchValue;
              }
            }
          }
          return currentService;
        }

        function _inTree(root, tree) {
          for (var i = 0; i < tree.length; i++) {
            if (tree[i].name === root) {
              return true;
            }
          }
          return false;
        }

        function get(name) {
          for (var i = 0; i < services.length; i++) {
            if (services[i].provider.toLowerCase() === name.toLowerCase()) {
              return services[i];
            }
          }
          return null;
        }
      }
    });
