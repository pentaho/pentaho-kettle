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
 * The Select Service
 *
 * The services for creating and editing database connections
 *
 * @module select.service
 * @property {String} name The name of the module.
 */
define(
    [],
    function() {
      "use strict";

      var factoryArray = ["helperService", factory];
      var module = {
        name: "selectService",
        factory: factoryArray
      };

      return module;

      /**
       * The dataService factory
       *
       * @param {Object} $http - The $http angular helper service
       *
       * @return {Object} The dataService api
       */
      function factory(helperService) {
        var baseUrl = "/cxf/repositories";
        return {
          getDatabases: getDatabases,
          createNewConnection: createNewConnection,
          editConnection: editConnection,
          deleteConnection: deleteConnection
        };

        /**
         * Get the databases associated with spoon
         *
         * @param repository
         * @returns {Promise}
         */
        function getDatabases(repository) {
          return helperService.httpGet([baseUrl, "databases"].join("/"), repository);
        }

        /**
         * Opens the create new database connection window
         *
         * @returns {Promise}
         */
        function createNewConnection() {
          return helperService.httpGet([baseUrl, "connection", "create"].join("/"));
        }

        /**
         * Opens the edit database connection window
         *
         * @param database
         * @returns {Promise}
         */
        function editConnection(database) {
          return helperService.httpPost([baseUrl, "connection", "edit"].join("/"), database);
        }

        /**
         * Delete a database connection
         *
         * @param database
         * @returns {Promise}
         */
        function deleteConnection(database) {
          return helperService.httpPost([baseUrl, "connection", "delete"].join("/"), database);
        }
      }
    });
