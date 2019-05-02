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
 * The Folder Service
 *
 * The Folder Service, a collection of services used by the application
 *
 * @module services/folder.service
 * @property {String} name The name of the module.
 */
define(
    [
      "./services.service"
    ],
    function (servicesService) {
      "use strict";

      var factoryArray = [servicesService.name, "$q", factory];
      var module = {
        name: "folderService",
        factory: factoryArray
      };

      return module;

      /**
       * The fileService factory
       *
       * @return {Object} The fileService api
       */
      function factory(ss, $q) {
        return {
          selectFolder: selectFolder,
          openFolder: openFolder,
          findFolderByPath: findFolderByPath,
          getPath: getPath,
          deleteFolder: deleteFolder,
          addFolder: addFolder,
          createFolder: createFolder
        };

        function getPath(folder) {
          if (!folder.root) {
            return folder.name;
          }
          var service = ss.get(folder.provider);
          if (!service) {
            return "";
          }
          return service.getPath(folder);
        }

        function openFolder(folder) {
          return selectFolder(folder);
        }

        /**
         * Sets variables showRecents, folder, and selectedFolder according to the contents of parameter
         *
         * @param {Object} folder - folder object
         * @param {String} filters - file filters
         * @param {Boolean} useCache - Clear the cache on the server
         */
        function selectFolder(folder, filters, useCache) {
          return $q(function(resolve, reject) {
            var service = ss.get(folder.provider);
            if (service) {
              folder.loading = true;
              service.selectFolder(folder, filters, useCache).then(function() {
                folder.loading = false;
                resolve();
              }, function () {
                resolve();
              });
            } else {
              resolve();
            }
          });
        }

        function findFolderByPath(tree, f, path) {
          return $q(function(resolve, reject) {
            if (!f.provider) {
              return;
            }
            var service = ss.get(f.provider);
            if (!service) {
              return;
            }
            var node = service.findRootNode(tree, f, path);
            console.log("Root Node: ", node);
            if (node === null) {
              reject();
            } else {
              node.open = true;
              if (node === f) {
                resolve(node);
              } else if (node) {
                var parts = service.parsePath(path, f);
                if (parts) {
                  _findFolder(node, parts).then(function(folder) {
                    resolve(folder);
                  });
                } else {
                  resolve(node);
                }
              } else {
                reject();
              }
            }
          });
        }

        function _findFolder(node, parts) {
          return $q(function(resolve, reject) {
            _doFind(node, parts.shift()).then(function(folder) {
              if (parts.length > 0) {
                resolve(_findFolder(folder, parts));
              } else {
                resolve(folder);
              }
            });
          });
        }

        function _doFind(node, name) {
          return $q(function(resolve, reject) {
            var folder = null;
            for (var i = 0; i < node.children.length; i++) {
              if (node.children[i].name === name) {
                folder = node.children[i];
                folder.open = true;
                folder.loading = false;
              }
            }
            if (folder) {
              resolve(folder);
            } else {
              if (node.children.length > 0) {
                resolve(node);
              } else {
                node.loading = true;
                ss.get(node.provider).createFolder(node, name).then(function(children) {
                  node.children = children;
                  node.loading = false;
                  resolve(_doFind(node, name));
                }, function() {
                  resolve(null);
                });
              }
            }
          });
        }

        function deleteFolder(tree, folder) {
          return $q(function(resolve, reject) {
            var parentFolder = findFolderByPath(tree, folder, _getParent(folder.path)).then(function(parent) {
              ss.get(folder.provider).deleteFiles([folder]).then(function(response) {
                var index = parent.children.indexOf(folder);
                parent.children.splice(index, 1);
                resolve(parent);
              }, function(response) {
                reject(response);
              });
            });
          });
        }

        function _getParent(path) {
          return path.substr(0, path.lastIndexOf("/"));
        }

        function createFolder(folder) {
          return $q(function(resolve, reject) {
            ss.get(folder.provider).addFolder(folder).then(function(response) {
              var result = response.data;
              if (result.status === "SUCCESS") {
                folder.new = false;
                folder.date = response.data.date;
                resolve(result);
              } else {
                reject(result);
              }
            }, function(response) {
              reject(response.status);
            });
          });
        }

        function addFolder(parentFolder) {
          var folder = {};
          var name = _getFolderName(parentFolder);
          folder.parent = parentFolder.path;
          folder.path = parentFolder.path + (parentFolder.path.charAt(parentFolder.path.length - 1) === "/" ? "" : "/") + name;
          folder.name = name;
          folder.new = true;
          folder.autoEdit = true;
          folder.type = "folder";
          folder.children = [];
          folder.provider = parentFolder.provider;
          folder.connection = parentFolder.connection; //TODO: Needs to be abstracted out
          folder.canEdit = true;
          folder.canAddChildren = true;
          folder.root = parentFolder.root;
          parentFolder.children.splice(0, 0, folder);
        }

        /**
         * Sets the default folder name to "New Folder" plus an incrementing integer
         * each time there is a folder called "New Folder <i+1>"
         *
         * @return {string} - Name of the folder
         * @private
         */
        function _getFolderName(parentFolder) {
          var name = "New Folder";
          var index = 0;
          var check = name;
          var search = true;
          while (search) {
            var found = false;
            for (var i = 0; i < parentFolder.children.length; i++) {
              if (parentFolder.children[i].name === check) {
                found = true;
                break;
              }
            }
            if (found) {
              index++;
              check = name + " " + index;
            } else {
              search = false;
            }
          }
          return check;
        }

        function updateDirectories(folder, oldPath, newPath) {
          folder.path = newPath;
          for (var i = 0; i < folder.children.length; i++) {
            _updateDirectories(folder.children[i], oldPath, newPath);
          }
          for (var k = 0; k < vm.recentFiles.length; k++) {
            if ((vm.recentFiles[k].path + "/").lastIndexOf(oldPath + "/", 0) === 0) {
              dt.updateRecentFiles(oldPath, newPath).then(function () {
                dt.getRecentFiles().then(_populateRecentFiles);
              });
              break;
            }
          }
        }

        /**
         * Update all child folder paths on parent rename
         *
         * @param {Object} child - Folder Object
         * @param {String} oldPath - String path of old directory path
         * @param {String} newPath - String path of new directory path
         * @private
         */
        function _updateDirectories(child, oldPath, newPath) {
          child.parent = child.parent.replace(oldPath, newPath);
          child.path = child.path.replace(oldPath, newPath);
          for (var i = 0; i < child.children.length; i++) {
            _updateDirectories(child.children[i], oldPath, newPath);
          }
        }
      }
    });
