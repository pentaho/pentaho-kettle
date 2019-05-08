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
 * The VFS Service
 *
 * The VFS Service, a collection of endpoints used by the application
 *
 * @module services/vfs.service
 * @property {String} name The name of the module.
 */
define(
    [
      "../components/utils"
    ],
    function (utils) {
      "use strict";

      var factoryArray = ["helperService", "$http", "$q", factory];
      var module = {
        name: "vfsService",
        factory: factoryArray
      };

      return module;

      /**
       * The dataService factory
       *
       * @param {Object} helperService
       * @param {Object} $http - The $http angular helper service
       * @param {Object} $q
       *
       * @return {Object} The dataService api
       */
      function factory(helperService, $http, $q) {
        var baseUrl = "/cxf/browser-new";
        return {
          provider: "vfs",
          selectFolder: selectFolder,
          getPath: getPath,
          createFolder: createFolder,
          addFolder: addFolder,
          findRootNode: findRootNode,
          parsePath: parsePath,
          deleteFiles: deleteFiles,
          renameFile: renameFile,
          isCopy: isCopy,
          open: open,
          save: save
        };

        function findRootNode(tree, folder, path) {
          var protocol = getProtocol(path);
          path = _stripProtocol(path);
          for (var i = 0; i < tree.length; i++) {
            if (tree[i].provider.toLowerCase() === folder.provider.toLowerCase()) {
              var node = tree[i];
              node.open = true;
              if (!node.root) {
                node = _findConnection(node, folder.connection);
              }
              if (!node) {
                return null;
              }
              node.protocol = protocol;
              return node;
            }
          }
          return null;
        }

        function parsePath(path, folder) {
          var newPath = _stripProtocol(path);
          if (newPath.indexOf(folder.root) === 0) {
            newPath = newPath.replace(folder.root, "");
          }
          if (newPath.indexOf("/") === 0) {
            newPath = newPath.substr(1, newPath.length);
          }
          if (newPath.indexOf(folder.connection) === 0) {
            newPath = newPath.replace(folder.connection, "");
          }
          if (newPath.indexOf("/") === 0) {
            newPath = newPath.substr(1, newPath.length);
          }
          return !newPath ? null : newPath.split("/");
        }

        function _findConnection(node, connection) {
          if (!connection) {
            return node;
          }
          for (var i = 0; i < node.children.length; i++) {
            if (node.children[i].name.toLowerCase() === connection.toLowerCase()) {
              node.children[i].open = true;
              return node.children[i];
            }
          }
        }

        function selectFolder(folder, filters, useCache) {
          return $q(function (resolve, reject) {
            if ((folder.path && !folder.loaded) || (!folder.path && !folder.loaded && folder.connection)) {
              getFiles(folder, filters, useCache).then(function (response) {
                folder.children = response.data;
                folder.loaded = true;
                for (var i = 0; i < folder.children.length; i++) {
                  folder.children[i].connection = folder.connection;
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
          if (!folder.path) {
            return folder.name;
          }
          return (folder.connection ? folder.connection + "/" : "") + folder.path.replace(/^[a-z]+:\/\//, "");
        }

        function _stripProtocol(path) {
          return path.replace(/^[a-z]+:\/\//, "/");
        }

        function getProtocol(path) {
          var match = path.match(/[\w]+:\/\//);
          if (match) {
            return match[0];
          }
          return null;
        }

        /**
         *
         * @param node
         * @param name
         * @returns {*}
         */
        function createFolder(node, name) {
          return $q(function (resolve, reject) {
            if ((node.path && !node.loaded) || (!node.path && !node.loaded && node.connection)) {
              getFiles(node).then(function (response) {
                node.loaded = true;
                resolve(response.data);
              });
            } else {
              reject();
            }
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

        function addFolder(folder) {
          return helperService.httpPut([baseUrl, "add"].join("/"), folder);
        }

        function deleteFiles(files) {
          return helperService.httpPost([baseUrl, "delete"].join("/"), files);
        }

        function renameFile(file, newPath) {
          return $q(function (resolve, reject) {
            helperService.httpPost([baseUrl, "rename"].join("/") + "?newPath=" + newPath, file).then(function (response) {
              file.path = newPath;
            });
          });
        }

        function isCopy(from, to) {
          return from.provider !== to.provider || from.connection !== to.connection;
        }

        function open(file) {
          select(null, file.name, file.path, file.parent, file.connection, file.provider, null);
        }

        function save(filename, folder, currentFilename, override) {
          select(null, filename, null, folder.path, folder.connection, folder.provider, null);
          return $q.resolve();
        }
      }
    });
