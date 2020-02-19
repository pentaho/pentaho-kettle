/*!
 * Copyright 2020 Hitachi Vantara. All rights reserved.
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
      "./fileutil",
      "../../components/utils",
      "pentaho/i18n-osgi!file-open-save-new.messages"
    ],
    function (fileUtils, utils, i18n) {
      "use strict";

      var factoryArray = ["helperService", "$http", "$q", "$location", factory];
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
      function factory(helperService, $http, $q, $location) {
        var baseUrl = "/cxf/browser-new";
        return {
          provider: "vfs",
          order: 2,
          root: "VFS Connections",
          matchPath: matchPath,
          selectFolder: selectFolder,
          getBreadcrumbPath: getBreadcrumbPath,
          getPath: getPath,
          getFilesByPath: getFilesByPath,
          createFolder: createFolder,
          addFolder: addFolder,
          setFolderParams: setFolderParams,
          deleteFiles: deleteFiles,
          renameFile: renameFile,
          isCopy: isCopy,
          open: open,
          save: save,
          resolvePath: resolvePath
        };

        function resolvePath(path, properties) {
          var self = this;
          return $q(function (resolve, reject) {
            if (path.indexOf("pvfs://") === 0) {
              resolve(self.root + "/" + path.replace("pvfs://", ''));
            } else if (properties && properties.connection) {
              resolve(self.root + "/" + properties.connection + "/" + path.replace(/^[\w]+:\/\//, ''));
            } else {
              reject(path);
            }
          });
        }

        function getPath(file) {
          return file.root ? _getTreePath(file) : file.path;
        }

        function matchPath(path) {
          return (path && path.match(/^[\w]+:\/\//) != null) ? .5 : 0;
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
              }, function(err) {
                reject({
                  title: i18n.get('file-open-save-plugin.vfs.unable-to-connect.title'),
                  message: i18n.get('file-open-save-plugin.vfs.unable-to-connect.message')
                });
              });
            } else {
              resolve();
            }
          });
        }

        function getBreadcrumbPath(file) {
          return {
            type: "vfs",
            fileType: file.type ? file.type : "folder",
            prefix: _getFilePrefix(file),
            uri: _getFilePath(file),
            path: _getTreePath(file)
          };
        }

        function _getFilePrefix(file) {
          if (file.root) {
            return null;
          }
          var prefix = null;
          if (file.path) {
            prefix = file.path.match(/^[\w]+:\/\//)[0];
            var matches = file.path.match(/\/\/(.*@)/);
            if (matches) {
              prefix += matches[1];
            }
          }
          return prefix;
        }

        function _getTreePath(folder) {
          if (!folder.parent) {
            return folder.root ? folder.root + "/" + folder.name : folder.name;
          }
          var path = _justThePath(folder.connectionPath ? folder.connectionPath : folder.path);
          if (folder.connection) {
            return folder.root + "/" + path;
          }
          return path;
        }

        /**
         *  Returns the pvfs or vfs schema based file path based on the useSchema parameter
         */
        function _getFilePath(file) {
          if ($location.search().useSchema && $location.search().useSchema.toLowerCase() === "true") {
            return file.path ? file.path : null;
          }
          return file.connectionPath ? file.connectionPath : file.path;
        }

        /**
         *  Returns the pvfs or vfs schema based parent path based on the useSchema parameter
         */
        function _getFileParentPath(file) {
          if ($location.search().useSchema && $location.search().useSchema.toLowerCase() === "true") {
            return file.parent ? file.parent : null;
          }
          return file.connectionParentPath ? file.connectionParentPath : null;
        }

        function _justThePath(url) {
          return fileUtils.cleanPath(url.replace(/^[\w]+:\/\//, "").replace(/.*@/, ""));
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
              }, function(err) {
                reject({
                  title: i18n.get('file-open-save-plugin.vfs.unable-to-connect.title'),
                  message: i18n.get('file-open-save-plugin.vfs.unable-to-connect.message')
                });
              });
            } else {
              reject();
            }
          });
        }

        function getFilesByPath(path, useCache) {
          return getFiles({path: path, provider: "vfs"}, undefined, useCache);
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
          return $q(function (resolve, reject) {
            helperService.httpPut([baseUrl, "add"].join("/"), folder).then(function (response) {
              var newFolder = response.data.data;
              folder.connectionPath = newFolder.connectionPath;
              folder.connectionParentPath = newFolder.connectionParentPath;
              resolve(response);
            }, function() {
              reject();
            });
          });
        }

        function setFolderParams(parentFolder, folder) {
          if (parentFolder.connectionPath) {
            folder.connection = parentFolder.connection;
            folder.connectionParentPath = parentFolder.connectionPath;
            folder.connectionPath = parentFolder.connectionPath + (parentFolder.connectionPath.charAt(parentFolder.connectionPath.length - 1) === "/" ? "" : "/") + folder.name;
          }
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
          select(JSON.stringify({
            name: file.name,
            path: _getFilePath(file),
            parent: _getFileParentPath(file),
            connection: file.connection,
            provider: file.provider
          }));
        }

        function save(filename, folder, currentFilename, override) {
          select(JSON.stringify({
            name: filename,
            path: _getFilePath(folder),
            parent: _getFilePath(folder),
            connection: folder.connection,
            provider: folder.provider
          }));

          return $q.resolve();
        }
      }
    });
