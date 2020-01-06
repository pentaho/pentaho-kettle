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
  "./Core",
  "pentaho/environment"
], function(Core, environment) {

  var corePromise = null;

  return {
    load: function(name, require, onLoad, config) {

      if(config.isBuild) {
        // Don't create when building.
        onLoad({moduleMetaService: {}, moduleService: {}});
      } else {
        if(corePromise === null) {
          corePromise = Core.createAsync(environment);
        }

        corePromise.then(onLoad, onLoad.error);
      }
    }
  };
});
