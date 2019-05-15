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
 * The Local Service
 *
 * The Local Service, a collection of endpoints used by the application
 *
 * @module services/data.service
 * @property {String} name The name of the module.
 */
define(
    [],
    function(fileService) {
      "use strict";

      var factoryArray = ["helperService", "$http", "$q", factory];
      var module = {
        name: "localService",
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
      function factory(helperService, $http, $q) {
        var baseUrl = "/cxf/browser-new";
        return {
          provider: "local",
          selectFolder: selectFolder,
          getPath: getPath,
          addFolder: addFolder,
          createFolder: createFolder,
          findRootNode: findRootNode,
          parsePath: parsePath,
          deleteFiles: deleteFiles,
          renameFile: renameFile,
          isCopy: isCopy,
          open: open,
          save: save
        };

        function findRootNode(tree, folder, path) {
          for (var i = 0; i < tree.length; i++) {
            if (tree[i].provider.toLowerCase() === folder.provider.toLowerCase()) {
              return tree[i];
            }
          }
          return null;
        }

        function parsePath(path, folder) {
          var newPath = path.replace(folder.root, "");
          if (newPath.indexOf("/") === 0) {
            newPath = newPath.substr(1, newPath.length);
          }
          return !newPath ? null : newPath.split("/");
        }

        function selectFolder(folder, filters) {
          return $q(function(resolve, reject) {
            if (folder.path && !folder.loaded) {
              getFiles(folder, filters).then(function(response) {
                folder.children = response.data;
                folder.loaded = true;
                for (var i = 0; i < folder.children.length; i++) {
                  folder.children[i].provider = folder.provider;
                }
                resolve();
              });
            } else {
              resolve();
            }
          });
        }

        function getPath(folder) {
          return folder.path;
        }

        function createFolder(node, name) {
          return $q(function(resolve, reject) {
            getFiles(node).then(function(response) {
              node.loaded = true;
              resolve(response.data);
            });
          });
        }

        /**
         * Gets the directory tree for the currently connected repository
         *
         * @return {Promise} - a promise resolved once data is returned
         */
        function getFiles(folder, filters) {
          return helperService.httpPost([baseUrl, "getFiles"].join("/") + (filters?"?filters="+filters:""), folder);
        }

        function deleteFiles(files) {
          return helperService.httpPost([baseUrl, "delete"].join("/"), files);
        }

        function addFolder(folder) {
          return helperService.httpPut([baseUrl, "add"].join("/"), folder);
        }

        function renameFile(file, newPath) {
          return $q(function(resolve, reject) {
            helperService.httpPost([baseUrl, "rename"].join("/") + "?newPath="+newPath, file).then(function(response) {
              file.path = newPath;
            });
          });
        }

        function isCopy(from, to) {
          return from.provider !== to.provider;
        }

        function open(file) {
          select(null, file.name, file.path, file.parent, file.connection, file.provider, null);
        }

        function save(filename, folder, currentFilename, override) {
          select(null, filename, null, folder.path, null, folder.provider, null);
          return $q.resolve();
        }
      }
    });
