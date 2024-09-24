/*!
 * Copyright 2018 - 2019 Hitachi Vantara. All rights reserved.
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
