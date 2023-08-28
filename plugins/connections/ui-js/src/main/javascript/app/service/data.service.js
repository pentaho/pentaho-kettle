/*!
 * Copyright 2021 Hitachi Vantara. All rights reserved.
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
      name: "dataService",
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
      var baseUrl = "";
      if ( IS_RUNNING_ON_WEBSPOON_MODE ) {
        baseUrl = "/spoon/osgi/cxf/connection";
      } else {
        baseUrl = "/cxf/connection";
      }
      return {
        getTypes: getTypes,
        getFields: getFields,
        createConnection: createConnection,
        getConnection: getConnection,
        help: help,
        testConnection: testConnection,
        exists: exists
      };

      function getTypes() {
        return helperService.httpGet([baseUrl, "types"].join("/"));
      }

      function getFields(type) {
        return helperService.httpGet([baseUrl, "connection", type].join("/"));
      }

      function createConnection(connection, name) {
        return helperService.httpPut([baseUrl, "connection"].join("/") + "?name=" + name, connection);
      }

      function testConnection(connection) {
        return helperService.httpPost([baseUrl, "test"].join("/"), connection);
      }

      function getConnection(name) {
        return helperService.httpGet([baseUrl, "connection"].join("/") + "?name=" + name);
      }

      function exists( name ) {
        return helperService.httpGet([baseUrl, "connection", "exists"].join("/") + "?name=" + name);
      }

      function help() {
        return helperService.httpGet([baseUrl, "help"].join("/"));
      }
    }
  });
