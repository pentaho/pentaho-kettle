/*!
 * Copyright 2020-2021 Hitachi Vantara. All rights reserved.
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
      "./provider.service",
      "./file.service",
      "./message.service",
      "./providers/fileutil",
      "../components/utils",
      "pentaho/i18n-osgi!file-open-save-new.messages"
    ],
    function (providerService, fileService, messageService, fileUtils, utils, i18n) {
      "use strict";

      var factoryArray = [providerService.name, fileService.name, messageService.name, "$q", factory];
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
      function factory(providerService, fileService, messageService, $q) {
        return {
          folder: null,
          tree: null,
          selectFolder: selectFolder,
          selectFolderByPath: selectFolderByPath,
          findFolderByPath: findFolderByPath,
          getBreadcrumbPath: getBreadcrumbPath,
          deleteFolder: deleteFolder,
          addFolder: addFolder,
          createFolder: createFolder,
          resolvePath: resolvePath,
          upDirectory: upDirectory,
          openPath: openPath,
          refreshFolder: refreshFolder,
          loadFile: loadFile,
          getDuplicate: getDuplicate
        };

        /**
         * Select a folder by object
         *
         * @param folder
         * @param filters
         * @param useCache
         * @returns {*}
         */
        function selectFolder(folder, filters, useCache) {
          messageService.set("file", null);
          var self = this;
          return $q(function (resolve, reject) {
            if (folder !== self.folder || self.folder.loaded === false) {
              self.folder = folder;
              self.folder.open = true;
              _selectFolder(folder, filters, useCache).then(function () {
                resolve(self.folder);
              }, function (err) {
                reject(err);
              });
            } else {
              resolve(self.folder);
            }
          });
        }

        /**
         * Select a folder by the path in the tree
         *
         * @param path
         * @param props
         * @returns {*}
         */
        function selectFolderByPath(path, props) {
          var self = this;
          return $q(function (resolve, reject) {
            fileService.files = [];
            self.findFolderByPath(path).then(function (folder) {
              self.selectFolder(folder).then(resolve);
            }, function (folderFile) {
              if (!_isErrorMessage(folderFile)) {
                fileService.files = [folderFile.file];
                self.selectFolder(folderFile.folder).then(reject());
              }
              else {
                var message = folderFile;
                reject(message);
              }
            });
          });
        }

        /**
         * Reload the tree from the source
         *
         * @returns {*}
         */
        function refreshFolder() {
          var self = this;
          return $q(function (resolve, reject) {
            if (self.folder) {
              self.folder.loaded = false;
              if (self.folder.provider) {
                self.selectFolder(self.folder, undefined, false).then(resolve);
              } else {
                self.openPath(self.folder.path, {useCache: false}).then(resolve);
              }
            }
          });
        }

        /**
         * Load a file based on a file path and not a tree path
         *
         * @param tree
         * @param path
         * @param props
         * @returns {*}
         */
        function openPath(path, props) {
          messageService.set("file", null);
          var self = this;
          return $q(function (resolve, reject) {
            path = fileUtils.cleanPath(path);
            var serviceResolve = self.resolvePath(path, props);
            if (serviceResolve !== null) {
              serviceResolve.then(function (path) {
                self.selectFolderByPath(path).then(function () {
                  resolve();
                }, function(error) {
                  reject(error);
                });
              }, function (path) {
                self.loadFile(path, props, resolve, reject);
              });
            } else {
              reject();
            }
          });
        }

        /**
         * Load a file by its path and select it
         *
         * @param path
         * @param props
         * @param resolve
         */
        function loadFile(path, props, resolve, reject) {
          var self = this;
          self.folder = {path: path};
          fileService.get({path: path}).then(function (file) {
            var filename = null;
            if (file.type === "file") {
              filename = fileUtils.getFilename(path);
              path = fileUtils.getParentPath(path);
              self.folder = {path: path};
            } else {
              self.folder = file;
            }
            var useCache = props ? props.useCache : false;
            _getFilesByPath(path, useCache).then(function (files) {
              self.folder.loaded = true;
              self.folder.children = files.data;
              self.fileLoading = false;
              fileService.files = [];
              _selectFile(filename, self);
              resolve();
            }, function (error) {
              reject(error);
            });
          }, function () {
            reject({
              title: i18n.get('file-open-save-plugin.vfs.unable-to-connect.title'),
              message: i18n.get('file-open-save-plugin.vfs.unable-to-connect.message')
            });
          });
        }

        /**
         * Select a file by the filename
         *
         * @param filename
         * @param self
         * @private
         */
        function _selectFile(filename, self) {
          if (filename && self.folder) {
            for (var i = 0; i < self.folder.children.length; i++) {
              if (filename === self.folder.children[i].name) {
                fileService.files = [self.folder.children[i]];
              }
            }
          }
        }

        /**
         * Find a folder object by the tree path
         *
         * @param path
         * @returns {*}
         */
        function findFolderByPath(path) {
          var self = this;
          return $q(function (resolve, reject) {
            var parts = path.split("/");
            var root = _findRoot(self.tree, parts.shift());
            if (parts.length === 0) {
              resolve(root);
              return;
            }
            root.open = true;
            // Check if the root directory is / and set that directory to root if it is
            if (root.children.length === 1 && root.children[0].name === '/') {
              root = root.children[0];
            }
            root.open = true;
            _findFolder(root, parts).then(function (folder) {
              resolve(folder);
            }, function (folderFile) {
              reject(folderFile);
            });
          });
        }

        /**
         * Get root object in tree by name
         *
         * @param tree
         * @param name
         * @returns {*}
         * @private
         */
        function _findRoot(tree, name) {
          for (var i = 0; i < tree.length; i++) {
            if (tree[i].name === name) {
              return tree[i];
            }
          }
        }

        /**
         * Iterate through path parts and look for the matching folder
         *
         * @param node folder object
         * @param parts path parts
         * @private
         */
        function _findFolder(node, parts) {
          return $q(function (resolve, reject) {
            _doFind(node, parts.shift()).then(function (file) {
              if (file === null) {
                resolve(node);
              } else if (file.type === "file") {
                reject({folder: node, file: file});
              } else {
                if (parts.length > 0) {
                  resolve(_findFolder(file, parts));
                } else {
                  resolve(file);
                }
              }
            }, function(error) {
                reject(error);
            });
          });
        }

        /**
         * Find child folder for node in tree and update status of node
         *
         * @param node
         * @param name
         * @returns {*}
         * @private
         */
        function _doFind(node, name) {
          return $q(function (resolve, reject) {
            var folder = null;
            for (var i = 0; i < node.children.length; i++) {
              var parts = node.children[i].name.trim().split("/");
              var foundPart = false;
              if ( IS_RUNNING_ON_WEBSPOON_MODE ) {
                for (var p = 0; p < parts.length; p++) {
                  if (parts[p].trim() === name.trim()) {
                    foundPart = true;
                  }
                }
              }
              if (node.children[i].name.trim() === name.trim() || ( IS_RUNNING_ON_WEBSPOON_MODE && foundPart === true ) ) {
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
                _populateFolder(node, name, resolve, reject);
              }
            }
          });
        }

        /**
         * If folder is not already loaded, load it from the server
         *
         * @param node
         * @param name
         * @param resolve
         * @private
         */
        function _populateFolder(node, name, resolve, reject) {
          node.loading = true;
          providerService.get(node.provider).createFolder(node, name).then(function (children) {
            if (children.length === 0) {
              resolve(null);
            } else {
              node.children = children;
              node.loading = false;
              resolve(_doFind(node, name));
            }
          }, function (error) {
            node.loading = false;
            reject(error);
          });
        }

        /**
         * Delete a folder by object
         *
         * @param folder
         * @returns {*}
         */
        function deleteFolder(folder) {
          var self = this;
          return $q(function (resolve, reject) {
            self.findFolderByPath(folder).then(function (parent) {
              providerService.get(folder.provider).deleteFiles([folder]).then(function () {
                var index = parent.children.indexOf(folder);
                parent.children.splice(index, 1);
                resolve(parent);
              }, function (response) {
                reject(response);
              });
            });
          });
        }

        /**
         * Navigate up one directory in the tree
         */
        function upDirectory() {
          if (!this.folder) {
            return;
          }
          var path = getPath(this.folder);
          path = fileUtils.getParentPath(path);
          fileService.files = [];
          var self = this;
          return $q(function (resolve) {
            if (self.folder.root) {
              self.selectFolderByPath(path).then(resolve);
            } else {
              self.openPath(path).then(resolve);
            }
          });
        }

        /**
         * Resolve a path from plugin
         *
         * @param path The path to resolve
         * @param properties The properties that may include the provider
         */
        function resolvePath(path, properties) {
          var service;
          if (properties && properties.provider) {
            service = providerService.get(properties.provider);
          } else {
            service = providerService.getByPath(path, this.tree);
          }
          if (service) {
            return service.resolvePath(path, properties);
          }
          return null;
        }

        /**
         * Get tree path folder folder
         *
         * @param folder The folder from which to get path
         * @returns {*} Folder path
         */
        function getPath(folder) {
          var service = folder.provider ? providerService.get(folder.provider) : null;
          if (!service) {
            service = providerService.getByPath(folder.path);
          }
          return service.getPath(folder);
        }

        /**
         * Get bread crumb path
         *
         * @param file
         * @returns {*} - Object representing path
         */
        function getBreadcrumbPath(file) {
          var service = (file && file.provider) ? providerService.get(file.provider) : null;
          if (!service) {
            service = providerService.getByPath(file.path);
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

        /**
         * Get files for an arbitrary path
         *
         * @param path
         * @param useCache
         * @returns {*}
         * @private
         */
        function _getFilesByPath(path, useCache) {
          var service = providerService.getByPath(path);
          return $q(function (resolve, reject) {
            service.getFilesByPath(path, useCache).then(function (files) {
              resolve(files);
            }, function (err) {
              reject({
                title: i18n.get('file-open-save-plugin.vfs.unable-to-connect.title'),
                message: i18n.get('file-open-save-plugin.vfs.unable-to-connect.message')
              })
            });
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
          return $q(function (resolve, reject) {
            var service = providerService.get(folder.provider);
            if (service && service.selectFolder) {
              folder.loading = true;
              service.selectFolder(folder, filters, useCache).then(function () {
                folder.loading = false;
                resolve();
              }, function (err) {
                folder.loading = false;
                reject(err);
              });
            } else {
              resolve();
            }
          });
        }

        /**
         * Creates a folder in the folder
         *
         * @param folder - The folder in which to create a path
         * @returns {*}
         */
        function createFolder(folder) {
          return $q(function (resolve, reject) {
            providerService.get(folder.provider).addFolder(folder).then(function (response) {
              var result = response.data;
              if (result.status === "SUCCESS") {
                folder.new = false;
                folder.date = result.data.date;
                resolve(result);
              } else {
                reject(result);
              }
            }, function (response) {
              reject(response.status);
            });
          });
        }

        function addFolder(parentFolder) {
          var folder = {};
          var name = _getFolderName(parentFolder);
          folder.parent = parentFolder.path;
          folder.path = fileUtils.concatPath(fileUtils.cleanPath(parentFolder.path), name);
          folder.name = name;
          folder.new = true;
          folder.autoEdit = true;
          folder.type = "folder";
          folder.children = [];
          folder.provider = parentFolder.provider;
          folder.canEdit = true;
          folder.canAddChildren = true;
          folder.root = parentFolder.root;
          var provider = providerService.get(parentFolder.provider);
          if (provider.setFolderParams) {
            provider.setFolderParams(parentFolder, folder);
          }
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
          } else if (vm.isSaveState() && utils.getTextWidth(retVal) > 395) {
            retVal = utils.truncateString(retVal, 386) + "...";
          }
          return retVal + "\"";
        }

        /**
         * Checks to see if the user has entered a file to save the same as a file already in current directory
         * NOTE: does not check for hidden files. That is done in the checkForSecurityOrDupeIssues rest call
         * @return {boolean} - true if duplicate, false otherwise
         * @private
         */
        function getDuplicate(name) {
          if (this.folder && this.folder.children) {
            for (var i = 0; i < this.folder.children.length; i++) {
              if (name === this.folder.children[i].name) {
                return this.folder.children[i];
              }
            }
          }
          return null;
        }

        /**
         * Determine if reason returned from a promise is an error message object.
         * @param message reason from promise
         * @returns {boolean} true if error message object, false otherwise
         * @private
         */
        function _isErrorMessage(reason) {
           return reason && reason.hasOwnProperty("title") && reason.hasOwnProperty("message");
        }
      }
    });
