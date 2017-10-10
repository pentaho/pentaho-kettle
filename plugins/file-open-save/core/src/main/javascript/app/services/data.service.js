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
 * @module services/data.service
 * @property {String} name The name of the module.
 */
define(
    [],
    function() {
      "use strict";

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
          getActiveFileName: getActiveFileName,
          getRecentFiles: getRecentFiles,
          updateRecentFiles: updateRecentFiles,
          getRecentSearches: getRecentSearches,
          storeRecentSearch: storeRecentSearch,
          openRecent: openRecent,
          openFile: openFile,
          saveFile: saveFile,
          rename: rename,
          create: create,
          remove: remove
        };

        /**
         * Gets the directory tree for the currently connected repository
         *
         * @return {Promise} - a promise resolved once data is returned
         */
        function getDirectoryTree(filter) {
          return _httpGet([baseUrl, "loadDirectoryTree", filter].join("/"));
        }

        /**
         * Load files for a specific directory
         *
         * @param {String} id - The object id for a directory
         * @return {Promise} - a promise resolved once data is returned
         */
        function getFiles(id) {
          return _httpGet([baseUrl, "loadFiles", encodeURIComponent(id)].join("/"));
        }

        /**
         * Gets the Active File Name Opened in Spoon
         * @return {Promise} - a promise resolved once data is returned
         */
        function getActiveFileName() {
          return _httpGet([baseUrl, "getActiveFileName"].join("/"));
        }

        /**
         * Load a file
         *
         * @param {String} id - The object id for a file
         * @param {String} type - The file type (job/transformation)
         * @return {Promise} - a promise resolved once data is returned
         */
        function openFile(id, type) {
          return _httpGet([baseUrl, "loadFile", encodeURIComponent(id), type].join("/"));
        }

        /**
         * Save a file
         *
         * @param {String} path - The path to which to save file
         * @param {String} name - The file name
         * @return {Promise} - a promise resolved once data is returned
         */
        function saveFile(path, name) {
          return _httpGet([baseUrl, "saveFile", encodeURIComponent(path), name].join("/"));
        }

        /**
         * Returns the recent files for the connected repository
         *
         * @return {Promise} - a promise resolved once data is returned
         */
        function getRecentFiles() {
          return _httpGet([baseUrl, "recentFiles"].join("/"));
        }

        /**
         * Updates the recent files with new file path
         * @param {String} oldPath - file path to be changed
         * @param {String} newPath - new file path
         *
         * @return {Promise} - a promise resolved once data is returned
         */
        function updateRecentFiles(oldPath, newPath) {
          return _httpGet([baseUrl, "updateRecentFiles", encodeURIComponent(oldPath),
            encodeURIComponent(newPath)].join("/"));
        }

        /**
         * Returns the 5 recent searches performed
         *
         * @return {Promise} - a promise resolved once data is returned
         */
        function getRecentSearches() {
          return _httpGet([baseUrl, "recentSearches"].join("/"));
        }

        /**
         * Stores the most recent search performed
         *
         * @param {String} recentSearch - The most recent search to be stored
         * @return {Promise} - a promise resolved once data is returned
         */
        function storeRecentSearch(recentSearch) {
          return _httpGet([baseUrl, "storeRecentSearch",
            encodeURIComponent(recentSearch) + "?ts=" + new Date().getTime()].join("/"));
        }

        /**
         * Renames a repository object
         *
         * @param {String} id - The repository object id
         * @param {String} path - The file path
         * @param {String} newName - The new name
         * @param {String} type - The object type
         * @param {String} oldName - The old name
         * @return {Promise} - a promise resolved once data is returned
         */
        function rename(id, path, newName, type, oldName) {
          return _httpPost([baseUrl, "rename", encodeURIComponent(id),
            encodeURIComponent(path), newName, type, oldName].join("/"));
        }

        /**
         * Create a new folder
         *
         * @param {String} parent - The parent folder
         * @param {String} name - The new folder name
         * @return {Promise} - a promise resolved once data is returned
         */
        function create(parent, name) {
          return _httpPost([baseUrl, "create", encodeURIComponent(parent), name].join("/"), null);
        }

        /**
         * Remove a repository object
         *
         * @param {String} id - The repository object id
         * @param {String} name - The file/folder name
         * @param {String} path - The file/folder path
         * @param {String} type - The object type
         * @return {Promise} - a promise resolved once data is returned
         */
        function remove(id, name, path, type) {
          return _httpDelete([baseUrl, "remove",
            encodeURIComponent(id), name, encodeURIComponent(path), type].join("/"));
        }

        /**
         * Open a recent file
         *
         * @param {String} repo - The name of the repository
         * @param {String} id - The repository object id
         * @return {Promise} - a promise resolved once data is returned
         */
        function openRecent(repo, id) {
          return _httpGet([baseUrl, "loadRecent", repo, id].join("/"));
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
            url: _cacheBust(url),
            headers: {
              Accept: "application/json"
            }
          };
          if (data !== null) {
            options.data = data;
          }
          return $http(options);
        }

        /**
         * Eliminates cache issues
         * @param {String} url - url string
         * @return {*} - url
         * @private
         */
        function _cacheBust(url) {
          var value = Math.round(new Date().getTime() / 1000) + Math.random();
          if (url.indexOf("?") !== -1) {
            url += "&v=" + value;
          } else {
            url += "?v=" + value;
          }
          return url;
        }
      }
    });
