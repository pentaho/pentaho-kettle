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

/**
 * The Data Service
 *
 * The Data Service, a collection of endpoints used by the application
 *
 * @module services/data.service
 * @property {String} name The name of the module.
 */
define(
    [],
    function() {
      "use strict";

      var factoryArray = ["helperService", factory];
      var module = {
        name: "loadingService",
        factory: factoryArray
      };

      return module;

      /**
       * The dataService factory
       *
       * @param {Object} helperService - The $http angular helper service
       *
       * @return {Object} The loadingService api
       */
      function factory(helperService) {
        var baseUrl = "/cxf/repositories";
        return {
          addRepository: addRepository,
          updateRepository: updateRepository
        };

        /**
         * Add a repository
         *
         * @param repository
         * @returns {Promise}
         */
        function addRepository(repository) {
          return helperService.httpPost([baseUrl, "add"].join("/"), repository);
        }

        /**
         * Update an existing repository
         *
         * @param repository
         * @returns {Promise}
         */
        function updateRepository(repository) {
          return helperService.httpPost([baseUrl, "update"].join("/"), repository);
        }
      }
    });
