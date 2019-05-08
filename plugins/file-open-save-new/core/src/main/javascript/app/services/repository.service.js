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
 * The Repository Service
 *
 * The Repository Service, a collection of endpoints used by the application
 *
 * @module services/data.service
 * @property {String} name The name of the module.
 */
define(
    [
      "../components/utils",
      "./data.service"
    ],
    function (utils) {
      "use strict";

      var factoryArray = ["helperService", "dataService", "$http", "$q", factory];
      var module = {
        name: "repositoryService",
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
      function factory(helperService, dt, $http, $q) {
        var baseUrl = "/cxf/browser-new";
        return {
          provider: "repository",
          selectFolder: selectFolder,
          getPath: getPath,
          createFolder: createFolder,
          findRootNode: findRootNode,
          parsePath: parsePath,
          addFolder: addFolder,
          deleteFiles: deleteFiles,
          renameFile: renameFile,
          isCopy: isCopy,
          open: open,
          save: save
        };

        /**
         *
         * @param tree
         * @param folder
         * @param subtree
         * @returns {*}
         */
        function findRootNode(tree, folder, subtree) {
          for (var i = 0; i < tree.length; i++) {
            if (tree[i].provider.toLowerCase() === folder.provider.toLowerCase()) {
              return tree[i];
            }
          }
          return null;
        }

        /**
         *
         * @param path
         * @param folder
         * @returns {*}
         */
        function parsePath(path, folder) {
          var newPath = path.replace(folder.root, "");
          if (newPath.indexOf("/") === 0) {
            newPath = newPath.substr(1, newPath.length);
          }
          return !newPath ? null : newPath.split("/");
        }

        function selectFolder(folder, filters, useCache) {
          return $q(function (resolve, reject) {
            if (folder.path && !folder.loaded) {
              getFiles(folder, filters, useCache).then(function (response) {
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
          return $q(function (resolve, reject) {
            getFiles(node).then(function (response) {
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
        function getFiles(folder, filters, useCache) {
          var parameters = {
            "filters": filters,
            "useCache": useCache
          };
          return helperService.httpPost([baseUrl, "getFiles"].join("/") + utils.buildParameters(parameters), folder);
        }

        function deleteFiles(files) {
          return helperService.httpPost([baseUrl, "delete"].join("/"), files);
        }

        function addFolder(folder) {
          return $q(function (resolve, reject) {
            return helperService.httpPut([baseUrl, "add"].join("/"), folder).then(function (response) {
              folder.objectId = response.data.data;
              resolve(response);
            });
          });
        }

        function renameFile(file, newPath) {
          return $q(function (resolve, reject) {
            return helperService.httpPost([baseUrl, "rename"].join("/") + "?newPath=" + newPath, file).then(function (response) {
              file.objectId = response.data.data;
              if (file.type === "transformation") {
                file.path = newPath + ".ktr";
              }
              if (file.type === "job") {
                file.path = newPath + ".kjb";
              }
              resolve(response);
            });
          });
        }

        function isCopy(from, to) {
          return from.provider !== to.provider;
        }

        function open(file) {
          select(file.objectId, file.name, file.path, file.parent,null, file.provider, file.type);
        }

        function save(filename, folder, currentFilename, override) {
          console.log(filename, folder, currentFilename, override);
          return $q(function(resolve, reject) {
            dt.checkForSecurityOrDupeIssues(folder.path, filename, currentFilename, override ? override : false).then(function(response) {
              if (response.status === 200) {
                select(null, filename, null, folder.path, null, folder.provider, null);
                resolve();
              } else {
                reject();
              }
            }, function() {
              reject();
            });
          });
        }

        function _getName(path) {
          var newPath = path.substr(path.lastIndexOf("/") + 1, path.length);
          return newPath.substr(0, newPath.lastIndexOf("."));
        }
      }
    });
