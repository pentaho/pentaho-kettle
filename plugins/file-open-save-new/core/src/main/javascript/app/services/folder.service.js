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
      "./services.service",
      "./file.service",
      "../components/utils"
    ],
    function (servicesService, fileService, utils) {
      "use strict";

      var factoryArray = [servicesService.name, fileService.name, "$q", factory];
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
      function factory(ss, fileService, $q) {
        return {
          folder: null,
          getPath: getPath,
          selectFolder: selectFolder,
          openFolder: openFolder,
          selectFolderByPath: selectFolderByPath,
          findFolderByPath: findFolderByPath,
          getFilesByPath: getFilesByPath,
          getBreadcrumbPath: getBreadcrumbPath,
          deleteFolder: deleteFolder,
          addFolder: addFolder,
          createFolder: createFolder,
          resolvePath: resolvePath,
          upDirectory: upDirectory,
          openPath: openPath,
          refreshFolder: refreshFolder
        };

        function refreshFolder(tree) {
          var self = this;
          return $q(function(resolve, reject) {
            if (self.folder) {
              self.folder.loaded = false;
              if (self.folder.provider) {
                self.selectFolder(self.folder, undefined, false).then(resolve);
              } else {
                self.openPath(tree, self.folder.path, {useCache: false}).then(resolve);
              }
            }
          });
        }

        // TODO: rename so that it's clear this is a file path not a tree path
        function openPath(tree, path, props) {
          var self = this;
          return $q(function(resolve, reject) {
            path = utils.cleanPath(path);
            self.resolvePath(path, props).then(function (path) {
              self.selectFolderByPath(tree, path).then(function() {
                resolve();
              });
            }).catch(function (path) {
              self.folder = {path: path};
              fileService.get({
                path: path
              }).then(function (file) {
                var filename = null;
                if (file.type === "file") {
                  filename = utils.getFilename(path);
                  path = utils.getParentPath(path);
                }
                self.folder = {path: path};
                var useCache = props ? props.useCache : false;
                self.getFilesByPath(path, useCache).then(function (files) {
                  self.folder.loaded = true;
                  self.folder.children = files.data;
                  self.fileLoading = false;
                  fileService.files = [];
                  _selectFile(filename);
                  resolve();
                });
              });
            });
          });
        }

        function _selectFile(filename) {
          if (filename && self.folder) {
            for (var i = 0; i < self.folder.children.length; i++) {
              if (filename === self.folder.children[i].name) {
                fileService.files = [self.folder.children[i]];
              }
            }
          }
        }

        // Select folder by path in the tree
        function selectFolderByPath(tree, path, props) {
          var self = this;
          return $q(function(resolve, reject) {
            self.findFolderByPath(tree, path, props).then(function (selectedFile) {
              fileService.files = [];
              if (selectedFile.type === "file") {
                self.selectFolderByPath(tree, path.substr(0, path.lastIndexOf("/"))).then(function() {
                  fileService.files = [selectedFile];
                  resolve();
                });
              } else {
                self.selectFolder(selectedFile).then(resolve);
              }
            });
          });
        }

        function upDirectory(tree) {
          if (!this.folder) {
            return;
          }
          var path = this.getPath(this.folder);
          path = utils.getParentPath(path);
          fileService.files = [];
          var self = this;
          return $q(function(resolve, reject) {
            if (self.folder.root) {
              self.selectFolderByPath(tree, path).then(resolve);
            } else {
              self.openPath(tree, path).then(resolve);
            }
          });
        }

        function resolvePath(path, properties) {
          var service;
          if (properties && properties.provider) {
            service = ss.get(properties.provider);
          } else {
            service = ss.getByPath(path);
          }
          if (service) {
            return service.resolvePath(path, properties);
          }
          return null;
        }

        function getPath(folder) {
          var service = folder.provider ? ss.get(folder.provider) : null;
          if (!service) {
            service = ss.getByPath(folder.path);
          }
          return service.getPath(folder);
        }

        function getBreadcrumbPath(file) {
          var service = file.provider ? ss.get(file.provider) : null;
          if (!service) {
            service = ss.getByPath(file.path);
          }
          if (!service) {
            return {
              prefix: null,
              fileType: "folder",
              uri: file.name,
              path: file.name
            };
          }

          return service.getBreadcrumbPath(file);
        }

        function getFilesByPath(path, useCache) {
          var service = ss.getByPath(path);
          return $q(function(resolve, reject) {
            service.getFilesByPath(path, useCache).then(function (files) {
              resolve(files);
            });
          });
        }

        function openFolder(folder) {
          return this.selectFolder(folder);
        }

        function selectFolder(folder, filters, useCache) {
          var self = this;
          return $q(function(resolve, reject) {
            if (folder !== self.folder || self.folder.loaded === false) {
              self.folder = folder;
              self.folder.open = true;
              if (folder.provider !== "recents") {
                _selectFolder(folder, filters, useCache).then(function () {
                  resolve(self.folder);
                });
              } else {
                resolve(self.folder);
              }
            } else {
              resolve(self.folder);
            }
          });
        }

        /**
         * Sets variables showRecents, folder, and selectedFolder according to the contents of parameter
         *
         * @param {Object} folder - folder object
         * @param {String} filters - file filters
         * @param {Boolean} useCache - Clear the cache on the server
         */
        function _selectFolder(folder, filters, useCache) {
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

        function _findRoot(tree, name) {
          for (var i = 0; i < tree.length; i++) {
            if (tree[i].name === name) {
              return tree[i];
            }
          }
        }

        function findFolderByPath(tree, path) {
          return $q(function(resolve, reject) {
            var parts = path.split("/");
            var root = _findRoot(tree, parts.shift());
            if (parts.length === 0) {
              resolve(root);
              return;
            }
            root.open = true;
            _findFolder(root, parts).then(function(folder) {
              resolve(folder);
            });
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

        /**
         * Returns the name of the selected folder
         *
         * @return {String} - "Search results in "<The name of the selected folder>", truncating with ellipsis accordingly
         */
        function getSelectedFolderName() {
          var retVal = i18n.get("file-open-save-plugin.app.search-results-in.label");
          if (vm.selectedFolder === "") {
            retVal += "\"" + vm.currentRepo;
          } else {
            retVal += "\"" + vm.selectedFolder;
          }
          if ($state.is("open") && utils.getTextWidth(retVal) > 435) {
            retVal = utils.truncateString(retVal, 426) + "...";
          } else if ($state.is("save") && utils.getTextWidth(retVal) > 395) {
            retVal = utils.truncateString(retVal, 386) + "...";
          }
          return retVal + "\"";
        }
      }
    });
