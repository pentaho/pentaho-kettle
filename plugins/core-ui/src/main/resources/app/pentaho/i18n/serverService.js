/*!
 * Copyright 2010 - 2018 Hitachi Vantara. All rights reserved.
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
 * The localization service that resolves message bundles using the Pentaho `/i18n` service.
 *
 * @name serverService
 * @memberOf pentaho.i18n
 * @type {pentaho.i18n.IService}
 * @amd pentaho/i18n
 */
define([
  "../environment",
  "./MessageBundle",
  "../util/url",
  "json"
], function(environment, MessageBundle, url) {

  "use strict";

  return /** @type pentaho.i18n.IService */ {
    load: function(bundlePath, localRequire, onLoad, config) {

      if(config.isBuild) {
        // Indicate that the optimizer should not wait for this resource and complete optimization.
        // This resource will be resolved dynamically during run time in the web browser.
        onLoad();
      } else {
        var bundleInfo = __getBundleInfo(localRequire, bundlePath);
        var serverUrl = environment.server.root;

        // Taking into account embedded scenarios when the host
        // is not the Pentaho Server / PDI
        var bundleUrl = "json!" + serverUrl +
          "i18n?plugin=" + bundleInfo.pluginId + "&name=" + bundleInfo.name;

        localRequire([bundleUrl], function(bundle) {

          onLoad(new MessageBundle(bundle));
        }, onLoad.error);
      }
    },
    normalize: function(name, normalize) {
      return normalize(__getBundleId(name));
    },

    __getBundleInfo: __getBundleInfo
  };

  /**
   * Normalizes the given bundle module identifier.
   *
   * @param {string} bundlePath - The specified bundle path argument.
   * @return {string} The normalized bundle identifier.
   */
  function __getBundleId(bundlePath) {
    var bundleMid;
    if(!bundlePath) {
      // E.g. bundlePath="pentaho/i18n!"
      // Use the default location and bundle.
      bundleMid = "./i18n/messages";
    } else if(bundlePath[0] === "/") {
      // E.g. bundlePath="pentaho/i18n!/pentaho/common/nls/messages"
      // The path is, directly, an absolute module id of a message bundle (without the /).
      bundleMid = bundlePath.substr(1);
      if(!bundleMid) throw new Error("[pentaho/messages!] Bundle path argument cannot be a single '/'.");
    } else if(bundlePath[0] !== "." && bundlePath.indexOf("/") < 0) {

      // the name of a bundle in the default "./i18n" sub-module
      bundleMid = "./i18n/" + bundlePath;
    } else {
      // "pentaho/i18n!./nls/information"
      // The path is, directly, a relative module id of a message bundle
      // Or the path has already been resolved by RequireJS.
      bundleMid = bundlePath;
    }

    return bundleMid;
  }

  /**
   * Gets a bundle info object with the plugin identifier and bundle name,
   * for a given bundle module identifier.
   *
   * @param {function} localRequire - The require-js function.
   * @param {string} bundlePath - The specified bundle path argument.
   *
   * @return {object} A bundle info object.
   *
   * @throws {Error} If the specified module identifier cannot be resolved
   *   to a plugin identifier and bundle name.
   */
  function __getBundleInfo(localRequire, bundlePath) {
    // e.g.:
    // bundlePath: pentaho/common/nls/messages
    // bundleMid:  pentaho/common/nls/messages
    // absBundleUrl: /pentaho/content/common-ui/resources/web/dojo/pentaho/common/nls/messages
    // basePath: /pentaho/
    // pluginId: common-ui
    // bundleName: resources/web/dojo/pentaho/common/nls/messages

    var bundleMid = __getBundleId(bundlePath);
    var bundleUrlPath = __getBundleUrlPath(localRequire, bundleMid);

    // Split the url into pluginId and bundleName
    // "pluginId/...bundleName..."
    var separatorIndex = bundleUrlPath.indexOf("/");

    // Catch invalid bundle url paths and throw when
    // the bundleUrlPath 1) starts or 2) ends with a forward slash (/)
    var isValidBundleUrlPath = separatorIndex > 0 || separatorIndex < bundleUrlPath.length - 1;
    if(!isValidBundleUrlPath) {
      throw new Error("[pentaho/messages!] Bundle path argument is invalid: '" + bundlePath + "'.");
    }

    return {
      pluginId: bundleUrlPath.substr(0, separatorIndex),
      name: bundleUrlPath.substr(separatorIndex + 1)
    };
  }

  function __getBundleUrlPath(localRequire, bundleMid) {
    var SERVER_ROOT_PATH = environment.server.root.pathname;
    var CONTENT_PATH = "content/";
    var PLUGIN_PATH = "/plugin/";
    var CGG_URL_SCHEME = "res:";

    var bundleUrl = url.create(localRequire.toUrl(bundleMid));
    var bundleUrlPath = bundleUrl.pathname;
    var bundleUrlScheme = bundleUrl.protocol;

    var startWithServerRoot = !bundleUrlPath.indexOf(SERVER_ROOT_PATH);
    if(startWithServerRoot) {
      bundleUrlPath = bundleUrlPath.substring(SERVER_ROOT_PATH.length);
    }

    var startsWithContent = !bundleUrlPath.indexOf(CONTENT_PATH);
    if(startsWithContent) {
      bundleUrlPath = bundleUrlPath.substr(CONTENT_PATH.length);
    }

    // In CGG, these type of URLs arise:
    // bundleUrl: "res:../../common-ui/resources/web/pentaho/type/i18n/types"
    // or
    // bundleUrl: "/plugin/common-ui/resources/web/pentaho/type/i18n/types"
    var startsWithCggUrlScheme = !bundleUrlPath.indexOf(CGG_URL_SCHEME);
    if(startsWithCggUrlScheme) {
      bundleUrlPath = bundleUrlPath.substr(CGG_URL_SCHEME.length);
    }

    var relativeBundleRegx = /^[./]*(.*)$/;

    var isCggResBundleRequest = startsWithCggUrlScheme || bundleUrlScheme === CGG_URL_SCHEME;
    var startsWithPlugin = !bundleUrlPath.indexOf(PLUGIN_PATH);

    if(isCggResBundleRequest && relativeBundleRegx.test(bundleUrlPath)) {
      var match = relativeBundleRegx.exec(bundleUrlPath);
      bundleUrlPath = match[1];
    } else if(startsWithPlugin) {
      bundleUrlPath = bundleUrlPath.substr(PLUGIN_PATH.length);
    }

    return bundleUrlPath;
  }

});
