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
  "../../lang/ArgumentRequiredError"
], function(ArgumentRequiredError) {

  "use strict";

  return function(core) {

    return core.ModuleMeta.extend("pentaho._core.module.InstanceMeta", /** @lends pentaho._core.module.InstanceMeta# */{

      get kind() {
        return "instance";
      },

      /**
       * @classDesc The `InstanceMeta` class implements the `module.IInstanceMeta` interface.
       *
       * @alias InstanceMeta
       * @memberOf pentaho._core.module
       * @class
       * @extends pentaho._core.module.Meta
       * @implements {pentaho.module.IInstanceMeta}
       *
       * @description Constructs the metadata of an instance module.
       * @constructor
       * @param {nonEmptyString} id - The identifier of the instance module.
       * @param {pentaho.module.spec.IInstanceMeta} spec - The specification of the metadata of the instance module.
       * @param {pentaho._core.module.Resolver} resolver - The module resolver function.
       */
      constructor: function(id, spec, resolver) {

        this.base(id, spec, resolver);

        var type = spec.type || null;

        this.type = type && resolver(type, "type");
        if(type) {
          this.type.__addInstance(this);
        }
      },

      /** @inheritDoc  */
      isInstanceOf: function(typeIdOrAlias) {
        if(!typeIdOrAlias) {
          throw new ArgumentRequiredError("typeIdOrAlias");
        }

        return this.type !== null && this.type.isSubtypeOf(typeIdOrAlias);
      },

      /** @override */
      _prepareCoreAsync: function() {

        if(this.type !== null) {
          return this.type.prepareAsync().then(this.base.bind(this));
        }

        return this.base();
      }
    });
  };
});
