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
          services[arguments[i].provider] = arguments[i];
        }

        return {
          get: get
        };

        function get(name) {
          return services[name.toLowerCase()];
        }
      }
    });
