/*!
 * Copyright 2019-2021 Hitachi Vantara. All rights reserved.
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
//TODO: Remove all the unnecessary methods
define(
    [],
    function() {
      "use strict";

      var factoryArray = ["$http", "$q", factory];
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
      function factory($http, $q) {
        var baseUrl = "/cxf/browser-new";
        var httpRequestCancellers = [];
          return {
          getDirectoryTree: getDirectoryTree,
          getFiles: getFiles,
          getFolders: getFolders,
          getFilesAndFolders: getFilesAndFolders,
          getActiveFileName: getActiveFileName,
          getRecentFiles: getRecentFiles,
          updateRecentFiles: updateRecentFiles,
          getRecentSearches: getRecentSearches,
          getCurrentRepo: getCurrentRepo,
          storeRecentSearch: storeRecentSearch,
          openRecent: openRecent,
          openFile: openFile,
          saveFile: saveFile,
          checkForSecurityOrDupeIssues: checkForSecurityOrDupeIssues,
          rename: rename,
          create: create,
          remove: remove,
          search: search,
          cancelSearch: cancelSearch
        };

        /**
         * Gets the directory tree for the currently connected repository
         *
         * @return {Promise} - a promise resolved once data is returned
         */
        function getDirectoryTree(filter, connectionTypes) {
          var method = "GET";
          var url = [baseUrl, "loadDirectoryTree", filter].join("/");

          var options = {
            method: method,
            url: ( ( IS_RUNNING_ON_WEBSPOON_MODE ) ? CONTEXT_PATH : "" ) + _cacheBust(url),
            headers: {
              Accept: "application/json"
            },
            params: {connectionTypes: connectionTypes}
          };
          return $http(options);

        }

        /**
         * Load files for a specific directory
         *
         * @param {String} path - The path for a directory
         * @return {Promise} - a promise resolved once data is returned
         */
        function getFiles(path) {
          return _httpGet([baseUrl, "loadFiles", encodeURIComponent(path)].join("/"));
        }

        /**
         * Load files and folders for a specific directory
         *
         * @param {String} path - The path for a directory
         * @return {Promise} - a promise resolved once data is returned
         */
        function getFilesAndFolders(path) {
          return _httpGet([baseUrl, "loadFilesAndFolders", encodeURIComponent(path)].join("/"));
        }

        function cancelSearch() {
          if (httpRequestCancellers.length > 0) {
            for (var i = 0; i < httpRequestCancellers.length; i++) {
              httpRequestCancellers[i].resolve();
            }
          }
          httpRequestCancellers = [];
        }

        function search(path, value) {
          return _httpGet([baseUrl, "search", encodeURIComponent(path), encodeURIComponent(value)].join("/"));
        }

        /**
         * Load subfolders for a specific directory
         *
         * @param {String} path - The path for a directory
         * @return {Promise} - a promise resolved once data is returned
         */
        function getFolders(path) {
          var httpRequestCanceller = $q.defer();
          httpRequestCancellers.push(httpRequestCanceller);
          return _httpGet([baseUrl, "loadFolders", encodeURIComponent(path)].join("/"), httpRequestCanceller.promise);
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
         * @param {String} fileName - Possible dupe file name...needed to verify possible dupe on back end
         * @param {String} override - true to override, false to not
         * @return {Promise} - a promise resolved once data is returned
         */
        function saveFile(path, name, fileName, override) {
          if (fileName === null) {
            return _httpGet([baseUrl, "saveFile", encodeURIComponent(path), encodeURIComponent(name), override].join("/"));
          }
          return _httpGet([baseUrl, "saveFile", encodeURIComponent(path), encodeURIComponent(name), encodeURIComponent(fileName), override].join("/"));
        }

        /**
         * Check for security issues or hidden duplicate files before saving
         *
         * @param {String} path - The path to which to save file
         * @param {String} name - The file name
         * @param {String} fileName - Possible dupe file name...needed to verify possible dupe on back end
         * @param {String} override - true to override, false to not
         * @return {Promise} - a promise resolved once data is returned
         */
        function checkForSecurityOrDupeIssues(path, name, fileName, override) {
          if (fileName === null || fileName === "") {
            return _httpGet([baseUrl, "checkForSecurityOrDupeIssues",
              encodeURIComponent(path), encodeURIComponent(name), override].join("/"));
          }
          return _httpGet([baseUrl, "checkForSecurityOrDupeIssues",
            encodeURIComponent(path), encodeURIComponent(name), encodeURIComponent(fileName), override].join("/"));
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

        /*
         * Returns the name of the current repo
         *
         * @return {Promise} - a promise resolved once data is returned
         */
        function getCurrentRepo() {
          return _httpGet([baseUrl, "currentRepo"].join("/"));
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
            encodeURIComponent(path), encodeURIComponent(newName), type, oldName].join("/"));
        }

        /**
         * Create a new folder
         *
         * @param {String} parent - The parent folder
         * @param {String} name - The new folder name
         * @return {Promise} - a promise resolved once data is returned
         */
        function create(parent, name) {
          return _httpPost([baseUrl, "create", encodeURIComponent(parent), encodeURIComponent(name)].join("/"), null);
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
            encodeURIComponent(id), encodeURIComponent(name), encodeURIComponent(path), type].join("/"));
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
        function _httpGet(url, timeout) {
          return _wrapHttp("GET", url, null, timeout);
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
         * @params {Function} timeout - a timeout promise
         * @return {Promise} - a promise to be resolved as soon as we get confirmation from the server.
         * @private
         */
        function _wrapHttp(method, url, data, timeout) {
          var options = {
            method: method,
            url: ( ( IS_RUNNING_ON_WEBSPOON_MODE ) ? CONTEXT_PATH : "" ) + _cacheBust(url),
            headers: {
              Accept: "application/json"
            },
            timeout: timeout
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
          if ( IS_RUNNING_ON_WEBSPOON_MODE ) {
            var cid = getConnectionId();
            url += "&cid=" + cid;
          }
          return url;
        }
      }
    });
