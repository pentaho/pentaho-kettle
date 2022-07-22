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
 * The Database Repository Service
 *
 * A collection of services for the database repository
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
        name: "detailsService",
        factory: factoryArray
      };

      return module;

      /**
       * The dataService factory
       *
       * @param {Object} helperService - The $http angular helper service
       *
       * @return {Object} The detailsService api
       */
      function factory(helperService) {
        var baseUrl = "/cxf/repositories";
        return {
          checkDuplicate: checkDuplicate
        };

        /**
         * Checks to see if a repository with the same name already exists
         *
         * @param {Object} repository - a repository object
         * @returns {Promise} - a promise to be resolve when the server returns
         */
        function checkDuplicate(repository) {
          return helperService.httpPost([baseUrl, "duplicate"].join("/"), repository);
        }
      }
    });
