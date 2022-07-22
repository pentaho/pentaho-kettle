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
    ['./fileutil'],
    function(fileutil) {
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
          order: 1,
          root: "Local",
          getBreadcrumbPath: getBreadcrumbPath,
          selectFolder: selectFolder,
          matchPath: matchPath,
          addFolder: addFolder,
          createFolder: createFolder,
          deleteFiles: deleteFiles,
          renameFile: renameFile,
          isCopy: isCopy,
          open: open,
          save: save,
          resolvePath: resolvePath,
          getPath: getPath
        };

        function getPath(file) {
          return file.root ? _getTreePath(file) : file.path;
        }

        function resolvePath(path, properties) {
          var self = this;
          var varRoot = this.root;
          return $q(function (resolve, reject) {
            if (path && path.indexOf("file://") === 0) {
              path = path.replace("file://", "");
            }
            if (fileutil.isWindows(path)) {
              path = fileutil.convertWindowsPath(path);
            }
            resolve(varRoot + path);
          });
        }

        function matchPath(path) {
          if (path && path.indexOf("file://") === 0) {
            return 1;
          }
          var isUnix = fileutil.isUnix(path);
          var isWindows = fileutil.isWindows(path);
          return (isUnix || isWindows) ? 1 : 0;
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

        function getBreadcrumbPath(file) {
          return {
            type: "local",
            fileType: file.type ? file.type : "folder",
            prefix: null,
            uri: _getFilePath(file),
            path: _getTreePath(file)
          };
        }


        function _getTreePath(folder) {
          if (!folder.path) {
            return folder.root ? folder.root + "/" + folder.name : folder.name;
          }
          var path = fileutil.isWindows(folder.path) ? fileutil.convertWindowsPath(folder.path) : folder.path;
          return folder.root + path;
        }

        function _getFilePath(file) {
          return file.path ? file.path : null;
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
          select(JSON.stringify({
            name: file.name,
            path: file.path,
            parent: file.parent,
            connection: file.connection,
            provider: file.provider
          }));
        }

        function save(filename, folder) {
          select(JSON.stringify({
            name: filename,
            path: folder.path,
            parent: folder.parent,
            provider: folder.provider
          }));

          return $q.resolve();
        }
      }
    });
