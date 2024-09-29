/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

define([
  "./metaService",
  "./util",
  "../shim/es6-promise"
], function(moduleMetaService, moduleUtil) {

  "use strict";

  // "$_$_1", "$_$_2", ...
  var NORMALIZE_PREFIX = "$_$_";
  var NORMALIZE_REGEX = /^\$_\$_/;
  var normalizeCounter = 0;

  var __keyArgsCreateIfUndefined = {createIfUndefined: true};

  /**
   * The `pentaho/module!` module is an AMD/RequireJS loader plugin that
   * obtains the [metadata object]{@link pentaho.module.IMeta} of a module given its identifier.
   * The configuration of the module is also loaded.
   *
   * If the requested module is not defined (through {@link pentaho.modules}),
   * it is defined as an [instance module]{@link pentaho.module.IInstanceMeta}
   * having a `null` [type]{@link pentaho.module.IInstanceMeta#type}.
   *
   * **AMD Plugin Usage**: `"pentaho/module!{moduleId}"`
   *
   * 1. `{moduleId}` — The identifier of the desired module. To refer to the requesting module,
   *    use the special value `_`.
   *
   * @example
   *
   * // Obtain the self module.
   * define(["pentaho/module!_"], function(module) {
   *
   *   if(module.config) {
   *     // Do something.
   *   }
   * });
   *
   * @name metaOf
   * @memberOf pentaho.module
   * @type {IAmdLoaderPlugin}
   * @amd pentaho/module
   *
   * @see pentaho.module.IMeta
   * @see pentaho.module.metaService
   * @see pentaho.module.IMetaService#get
   */

  return {
    load: function(name, requesterRequire, onLoad, config) {
      if(config.isBuild) {
        // Don't resolve when building.
        onLoad();
      } else {

        if(name === "_" || NORMALIZE_REGEX.test(name)) {
          name = null;
        }

        var requesterId = moduleUtil.getId(requesterRequire);
        var moduleId = name
          ? moduleUtil.absolutizeIdRelativeToSibling(name, requesterId)
          : requesterId;

        var moduleMeta = moduleMetaService.get(moduleId, __keyArgsCreateIfUndefined);

        moduleMeta.prepareAsync()
          .then(function() {
            onLoad(moduleMeta);
          }, onLoad.error);
      }
    },
    normalize: function(name, normalize) {

      // This resolves a name which is relative to the parent module.
      // Still, no way to know the actual requesting module, just the parent module's id (`normalize(".")`).
      if(name && name !== "_") {
        return normalize(name);
      }

      // Unfortunately, RequireJS does not give us access to the requesting module's id at this phase.
      // The load method is only called for each distinct return value of the `normalize` method.
      // Returning an always distinct value ensures it is called once for each use...
      // It's also important to return a value that would never be a real module id.
      return NORMALIZE_PREFIX + (++normalizeCounter);
    }
  };
});
