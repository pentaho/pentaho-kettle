/*!
 * HITACHI VANTARA PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2018 Hitachi Vantara. All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Hitachi Vantara and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Hitachi Vantara and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Hitachi Vantara is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Hitachi Vantara,
 * explicitly covering such access.
 */

/**
 * The Json Input Get Fields Tree component.
 *
 * This provides the component for the Tree screen.
 * @module components/tree/tree.component
 * @property {String} name The name of the Angular component.
 * @property {Object} options The JSON object containing the configurations for this component.
 **/
define([
  "text!./tree.html",
  "css!./tree.css"
], function(template) {
  "use strict";

  var options = {
    bindings: {
      content: '<',
      paths: '<',
      onSelection: '&'
    },
    controllerAs: "vm",
    template: template,
    controller: treeController
  };

  treeController.$inject = ["$scope"];

  /**
   * The Tree Controller
   */
  function treeController($scope) {
    var vm = this;
    vm.toggle = toggle;
    vm.hasChevron = hasChevron;
    vm.checked = [];
    vm.checkboxSelect = checkboxSelect;
    vm.$onChanges = function(changes) {
      if (vm.content) {
        _setParent(null, vm.content);
      }
      if (vm.content && vm.paths) {
        var paths = vm.paths.split(",");
        for (var i = 0; i < paths.length; i++) {
          _selectByPath(paths[i], vm.content);
        }
      }
    };

    $scope.$on("clearSelection", function(e, data) {
      if (vm.content.children) {
        _clearChildren(vm.content);
      }
    });

    function _setParent(parent, node) {
      node.open = true;
      node.parent = parent;
      if (node.children) {
        for (var i = 0; i < node.children.length; i++) {
          var child = node.children[i];
          _setParent(node, child);
        }
      }
    }

    function _clearChildren(node) {
      node.checked = false;
      if (node.children) {
        for (var i = 0; i < node.children.length; i++) {
          var child = node.children[i];
          child.checked = false;
          _clearChildren(child);
        }
      }
    }

    function _getPaths() {
      var paths = [];
      for (var i = 0; i < vm.checked.length; i++) {
        if (!_hasCheckedChildren(vm.checked[i])) {
          var node = vm.checked[i];
          var data = _generatePath(vm.checked[i]);
          var key = node.key;
          if (key === null) {
            if (node.parent) {
              key = node.parent.key;
            } else {
              key = "root";
            }
          }
          paths.push(key + ":" + data + ":" + node.type);
        }
      }
      return paths;
    }

    function _generatePath(node) {
      var path = _getNodePath(node);
      var parent = node.parent;
      while (parent) {
        if (parent.checked) {
          path = _getNodePath(parent) + path;
        }
        parent = parent.parent;
      }
      return "$" + path;
    }

    function _getNodePath(node) {
      var key = node.key;
      if (key === null) {
        if (node.parent && node.parent.type === "Array") {
          if (node.parent.checked) {
            key = "[*]";
          } else {
            key = "..[*]";
          }
        } else {
          key = "";
        }
      } else {
        if (node.parent && node.parent.checked) {
          key = "." + key;
        } else {
          key = ".." + key;
        }
      }
      return key;
    }

    function _hasCheckedChildren(node) {
      if (node.children) {
        for (var i = 0; i < node.children.length; i++) {
          var child = node.children[i];
          if (child.checked) {
            return true;
          }
          if (child.children) {
            if (_hasCheckedChildren(child)) {
              return true;
            }
          }
        }
      }
      return false;
    }

    $scope.$on("ok", function(e, data) {
      var paths = _getPaths();
      try {
        window.ok(paths);
      } catch (err) {
        console.log(paths);
      }
    });

    function hasChevron(node) {
      return node.children && node.children.length > 0;
    }

    /**
     * Toggle a collection open/closed
     *
     * @param value
     */
    function toggle(node) {
      if (!node.open) {
        node.open = true;
      } else {
        node.open = false;
      }
    }

    function checkboxSelect(node) {
      _handleChecked(node);
      if (node.key === null) {
        if (node.checked) {
          _setChecked(node, true);
        } else {
          _setChecked(node, false);
        }
      }
    }

    function _handleChecked(node) {
      if (node.checked) {
        var index = vm.checked.indexOf(node);
        if (index === -1) {
          vm.checked.push(node);
        }
      } else {
        var index = vm.checked.indexOf(node);
        if (index !== -1) {
          vm.checked.splice(index, 1);
        }
      }
      vm.onSelection({count: vm.checked.length});
    }

    function _setChecked(node, checked) {
      if (node.children) {
        for (var i = 0; i < node.children.length; i++) {
          node.children[i].checked = checked;
          checkboxSelect(node.children[i]);
        }
      }
    }

    function _checkNode(node) {
      node.checked = true;
      var index = vm.checked.indexOf(node);
      if (index === -1) {
        vm.checked.push(node);
      }
      vm.onSelection({count: vm.checked.length});
    }

    function _selectByPath(path, node) {
      var expressions = _getExpressions(path);
      var expression = expressions.shift();
      while (expression) {
        if (expression === "$") {
          expression = expressions.shift();
          if (expression === ".") {
            _checkNode(node);
          }
        } else if (expression === ".") {
          expression = expressions.shift();
          node = _findChild(node, expression);
          if (node) {
            _checkNode(node);
          }
        } else if (expression === "..") {
          expression = expressions.shift();
          if (expression === "[*]") {

          }
          node = _findAny(node, expression);
          if (node) {
            _checkNode(node);
          }
        } else if (expression === "[*]" && expressions.length > 0) {
          node = node.children[0];
          _checkNode(node);
          expression = expressions.shift();
        } else {
          expression = expressions.shift();
        }
      }
    }

    function _getExpressions(path) {
      return path.match(/(\w+|\[[\s\S]*?]|\$|\.\.|\.)/g);
    }

    function _findAny(node, value) {
      if (node.children) {
        for (var i = 0; i < node.children.length; i++) {
          if (node.children[i].key === value) {
            return node.children[i];
          }
          if (node.children[i].children) {
            var found = _findAny(node.children[i], value);
            if (found) {
              return found;
            }
          }
        }
      }
      return null;
    }

    function _findChild(node, value) {
      if (node && node.children) {
        for (var i = 0; i < node.children.length; i++) {
          if (node.children[i].key === value) {
            return node.children[i];
          }
        }
      }
      return null;
    }
  }

  return {
    name: "tree",
    options: options
  };
});
