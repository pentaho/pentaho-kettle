/*!
 * Copyright 2017 Pentaho Corporation. All rights reserved.
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
 * @module extension-plugins/explorer/services/data.service
 * @property {String} name The name of the module.
 */
define(
    [],
    function() {
      'use strict';

      var factoryArray = ["$http", factory];
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
      function factory($http) {
        var baseUrl = "/cxf/browser";
        return {
          getDirectoryTree: getDirectoryTree,
          getFiles: getFiles,
          getRecentFiles: getRecentFiles,
          openFile: openFile,
          rename: rename,
          create: create,
          remove: remove
        };

        function getDirectoryTree() {
          return _httpGet([baseUrl, "loadDirectoryTree"].join("/"));
        }

        function getFiles(id) {
          return _httpGet([baseUrl, "loadFiles", encodeURIComponent(id)].join("/"));
        }

        function openFile(id, type) {
          return _httpGet([baseUrl, "loadFile", encodeURIComponent(id), type].join("/"));
        }

        function getRecentFiles() {
          return _httpGet([baseUrl, "recentFiles"].join("/"));
        }

        function rename(id, name, path, type) {
          return _httpPost([baseUrl, "rename", encodeURIComponent(id), encodeURIComponent(path), name, type].join("/"));
        }

        function create(parent, name) {
          return _httpPost([baseUrl, "create", encodeURIComponent(parent), name].join("/"), null);
        }

        function remove(id, type) {
          return _httpDelete([baseUrl, "remove", encodeURIComponent(id), type].join("/"));
        }

        /**
         * Asks the server to execute a query.
         *
         * @param {String} uid - The unique identifier of the datasource
         * @param {String} mode - The mode with which to create the query
         * @param {Object} data - The data (fields) to create the query
         * @return {Promise} - a promise to be resolved as soon as we get confirmation from the server.
         */
        function executeQuery(uid, mode, data) {
          return _httpPost([baseUrl, uid, mode, "execute-query"].join("/"), data);
        }

        /**
         * Fetches data from a query.
         *
         * @param {String} uid - The unique identifier of the datasource
         * @param {String} mode - The mode of the query
         * @param {String} quid - The unique identifier of the query
         * @return {Promise} - a promise to be resolved when the data reaches the client.
         */
        function getData(uid, mode, quid) {
          return _httpGet([baseUrl, uid, mode, "queries", quid].join("/"));
        }

        /**
         * Deletes a query from the server.
         *
         * @param {String} uid - The unique identifier of the datasource
         * @param {String} mode - The mode of the query
         * @param {String} quid - The unique identifier of the query
         * @return {Promise} - a promise to be resolved as soon as we get confirmation from the server.
         */
        function deleteQuery(uid, mode, quid) {
          return _httpDelete([baseUrl, uid, mode, "queries", quid].join("/"));
        }

        /**
         * Wraps the http angular service to provide sensible defaults
         *
         * @param {String} url - the url
         * @return {Promise} - a promise to be resolved as soon as we get confirmation from the server.
         * @private
         */
        function _httpGet(url) {
          return _wrapHttp("GET", url);
        }

        /**
         * Wraps the http angular service to provide sensible defaults
         *
         * @param {String} url - the url
         * @param {String} data - the post data
         * @return {Promise} - a promise to be resolved as soon as we get confirmation from the server.
         * @private
         */
        function _httpPost(url, data) {
          return _wrapHttp("POST", url, data);
        }

        /**
         * Wraps the http angular service to provide sensible defaults
         *
         * @param {String} url - the url
         * @return {Promise} - a promise to be resolved as soon as we get confirmation from the server.
         * @private
         */
        function _httpDelete(url) {
          return _wrapHttp("DELETE", url);
        }

        /**
         * Wraps the http angular service to provide sensible defaults
         *
         * @param {String} method - the http method to use
         * @param {String} url - the url
         * @param {String} data - the data to send to the server
         * @return {Promise} - a promise to be resolved as soon as we get confirmation from the server.
         * @private
         */
        function _wrapHttp(method, url, data) {
          var options = {
            method: method,
            url: url,
            headers: {
              Accept: "application/json"
            }
          };
          if (data != null) {
            options.data = data;
          }
          return $http(options);
        }
      }
    });
