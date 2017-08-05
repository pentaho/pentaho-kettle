define([
  'text!./loading.html',
  'css!./loading.css'
], function(template) {
  'use strict';

  var options = {
    bindings: {
      title: '<',
      message: '<'
    },
    controllerAs: "vm",
    template: template,
    controller: loadingController
  };

  function loadingController() {
    var vm = this;
  }

  return {
    name: "loading",
    options: options
  };

});
