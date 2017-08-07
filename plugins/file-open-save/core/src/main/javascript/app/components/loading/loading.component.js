define([
  'text!./loading.html',
  "pentaho/i18n-osgi!file-open-save.messages",
  'css!./loading.css'
], function(template, i18n) {
  'use strict';

  var options = {
    bindings: {
    },
    controllerAs: "vm",
    template: template,
    controller: loadingController
  };

  /**
   * The Loading Controller
   */
  function loadingController() {
    var vm = this;
    vm.$onInit = onInit;

    /**
     * The $onInit hook of components lifecycle which is called on each controller
     * after all the controllers on an element have been constructed and had their
     * bindings initialized. We use this hook to put initialization code for our controller.
     */
    function onInit() {
      vm.loadingTitle = i18n.get("file-open-save-plugin.loading.title");
      vm.loadingMessage = i18n.get("file-open-save-plugin.loading.message");
    }
  }

  return {
    name: "loading",
    options: options
  };
});
