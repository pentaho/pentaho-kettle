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

      var factoryArray = [factory];
      var module = {
        name: "searchService",
        factory: factoryArray
      };

      var searchPaths = [];
      var maxSearching = 2;
      var currentSearches = [];
      var searchInterval;
      var checkInterval;
      var start;

      return module;

      function search(folder, results, value) {
        for (var i = 0; i < folder.children.length; i++) {
          var name = folder.children[i].name.toLowerCase();
          if (name.indexOf(value.toLowerCase()) !== -1) {
            results.push(folder.children[i]);
          }
        }
      }

      /**
       * Calls a filter for either recent files or files/folders in current folder
       */
      function doSearch(searchValue) {
        if (searchValue) {
          _initSearch();
          start = (new Date()).getTime();
          if (vm.showRecents === true) {
            selectFolder(vm.tree.children[0], function () {
              $timeout(function () {
                _doSearch(searchValue);
              })
            });
          } else {
            _doSearch(searchValue);
          }
        } else {
          _clearSearch();
          _setSearchState();
        }
      }

      function _setSearchState() {
        if (isPentahoRepo() && vm.searchString === "" && vm.selectedFolder === "/") {
          vm.showRecents = true;
          vm.folder = {name: "Recents", path: "Recents"};
          vm.selectedFolder = vm.folder.name;
          _resetFileAreaMessage();
        } else if (vm.searchString === "") {
          _resetFileAreaMessage();
        }
      }

      function _initSearch() {
        vm.searchResults = [];
        searchPaths = [];
        currentSearches = [];
      }

      function _completeSearch() {
        clearInterval(searchInterval);
        clearInterval(checkInterval);
        $timeout(function () {
          vm.searching = false;
        });
      }

      /**
       * Resets the search string and runs search against that string (which returns normal dir structure).
       */
      function _clearSearch() {
        dt.cancelSearch();
        _completeSearch();
        vm.searchResults = [];
        vm.searchString = "";
        _resetFileAreaMessage();
      }

      function _doSearch(searchValue) {
        vm.searching = true;
        vm.showMessage = true;
        vm.searchString = searchValue;
        _setFileAreaMessage();
        _loadSearchPaths(vm.folder, searchValue);
        searchInterval = setInterval(function () {
          if (currentSearches.length <= maxSearching) {
            var path = searchPaths.pop();
            if (path) {
              _loadSearchResults(path, searchValue);
            }
          }
        });
        checkInterval = setInterval(function () {
          if (currentSearches.length === 0) {
            _completeSearch();
          }
        }, 1000);
      }

      function _loadSearchPaths(folder, searchValue) {
        dt.search(folder.path, searchValue).then(function (response) {
          vm.searchResults = vm.searchResults.concat(response.data);
          if (vm.searchResults.length > 0) {
            vm.showMessage = false;
          }
        });
        for (var i = 0; i < folder.children.length; i++) {
          if (folder.children[i].type === "folder") {
            searchPaths.push(folder.children[i].path);
          }
        }
      }

      function _loadSearchResults(path, searchValue) {
        currentSearches.push(path);
        dt.getFolders(path).then(function (response) {
          _loadSearchPaths(response.data, searchValue);
          var index = currentSearches.indexOf(path);
          currentSearches.splice(index, 1);
        }, function () {
          var index = currentSearches.indexOf(path);
          currentSearches.splice(index, 1);
        });
      }

      /**
       * Recursively searches for the value in elements and any of its children
       *
       * @param {Object} elements - Object with files and folders
       * @param {String} value - String used to search within elements
       * @private
       */
      function _filter(elements, value) {
        if (elements) {
          for (var i = 0; i < elements.length; i++) {
            var name = elements[i].name.toLowerCase();
            var inResult = name.indexOf(value.toLowerCase()) !== -1;
            vm.showMessage = inResult ? false : vm.showMessage;
            elements[i].inResult = inResult;
            if (elements[i].children.length > 0) {
              _filter(elements[i].children, value);
            }
          }
        }
      }

      /**
       * The dataService factory
       *
       * @param {Object} $http - The $http angular helper service
       *
       * @return {Object} The dataService api
       */
      function factory() {
        return {
          search: search
        };


      }
    });
