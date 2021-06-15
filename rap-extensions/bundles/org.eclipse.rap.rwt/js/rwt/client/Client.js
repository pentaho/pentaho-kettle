/*******************************************************************************
 * Copyright: 2004, 2020 1&1 Internet AG, Germany, http://www.1und1.de,
 *                       and EclipseSource
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Remote Application Platform
 ******************************************************************************/

namespace( "rwt.client" );

/**
 * @class Basic client detection implementation.
 * @description Version names follow the wikipedia scheme: major.minor[.revision[.build]] at
 * http://en.wikipedia.org/wiki/Software_version
 * Object can be obtained from {@link rap.getClient}.
 * @public
 * @since 3.2
 * @exports rwt.client.Client as Client
 */
rwt.client.Client = {

  __init : function() {
    this._engineName = "unknown";
    this._browserName = "unknown";
    this._engineVersion = 0;
    this._engineVersionMajor = 0;
    this._engineVersionMinor = 0;
    this._engineVersionRevision = 0;
    this._engineVersionBuild = 0;
    this._browserPlatform = "other";
    this._defaultLocale = "en";
    // NOTE: Order is important!
    this._initKonqueror();
    this._initTrident();
    this._initBlink();
    this._initWebkit();
    this._initGecko();
    this._initBoxSizing();
    this._initLocale();
    this._initPlatform();
  },

  /**
   * @description Returns the name of the browser engine - "trident", "gecko", "webkit" or "blink".
   * If the browser engine name can't be detected this function returns "unknown".
   */
  getEngine : function() {
    return this._engineName;
  },

  /**
   * @description Returns the name of the browser, "chrome" or "firefox" for example.
   * If the browser name can't be detected this function returns "unknown".
   */
  getBrowser : function() {
    return this._browserName;
  },

  /**
   * @description Returns the browser engine version.
   */
  getVersion : function() {
    return this._engineVersion;
  },

  /**
   * @description Returns the major number of browser engine version.
   */
  getMajor : function() {
    return this._engineVersionMajor;
  },

  /**
   * @description Returns the minor number of browser engine version.
   */
  getMinor : function() {
    return this._engineVersionMinor;
  },

  /**
   * @description Returns the revision number of browser engine version.
   */
  getRevision : function() {
    return this._engineVersionRevision;
  },

  /**
   * @description Returns the build number of browser engine version.
   */
  getBuild : function() {
    return this._engineVersionBuild;
  },

  /**
   * @description Returns true is browser engine is "trident" (IE/Edge), false otherwise.
   */
  isTrident : function() {
    return this._engineName === "trident";
  },

  /**
   * @description Returns true is browser engine is "gecko" (Firefox), false otherwise.
   */
  isGecko : function() {
    return this._engineName === "gecko";
  },

  /**
   * @description Returns true is browser engine is "blink" (Chrome/Opera), false otherwise.
   */
  isBlink : function() {
    return this._engineName === "blink";
  },

  /**
   * @description Returns true is browser engine is "webkit" (Safari), false otherwise.
   */
  isWebkit : function() {
    return this._engineName === "webkit";
  },

  /**
   * @description Returns the client timezone.
   */
  getTimezoneOffset : function() {
    return ( new Date() ).getTimezoneOffset();
  },

  /**
   * @description Returns the browser locale as defined in http://www.ietf.org/rfc/bcp/bcp47.txt.
   */
  getLocale : function() {
    return this._browserLocale;
  },

  /**
   * @description Returns the language part from the browser locale.
   */
  getLanguage : function() {
    return this.getLocale().split( "-" )[ 0 ];
  },

  /**
   * @description Returns the region part from the browser locale.
   */
  getTerritory : function() {
    var parts = this.getLocale().split( "-" );
    return parts[ 2 ] || parts[ 1 ] || "";
  },

  /**
   * @description Returns the browser default locale.
   */
  getDefaultLocale : function() {
    return this._defaultLocale;
  },

  /**
   * @description Returns true if the current browser locale is the default one, false otherwise.
   */
  usesDefaultLocale : function() {
    return this._browserLocale === this._defaultLocale;
  },

  getEngineBoxSizingAttributes : function() {
    return this._engineBoxSizingAttributes;
  },

  /**
   * @description Returns client platform like Windows, Android for example.
   */
  getPlatform : function() {
    return this._browserPlatform;
  },

  /**
   * @description Returns true if the browser is mobile Safari, false otherwise.
   */
  isMobileSafari : function() {
    return this.getPlatform() === "ios" && this.getBrowser() === "safari";
  },

  /**
   * @description Returns true if the browser is mobile Chrome, false otherwise.
   */
  isMobileChrome : function() {
    return this.getPlatform() === "android" && this.getBrowser() === "chrome";
  },

  /**
   * @description Returns true if the browser is default Android browser, false otherwise.
   */
  isAndroidBrowser : function() {
    return this.getPlatform() === "android" && this.getBrowser() === "android";
  },

  /**
   * @description Returns true if the browser is mobile Firefox, false otherwise.
   */
  isMobileFirefox : function() {
    return this.getPlatform() === "android" && this.getBrowser() === "firefox";
  },

  /**
   * @description Returns true if the browser supports file drop, false otherwise.
   */
  supportsFileDrop : function() {
    return !!window.FormData;
  },

  /**
   * @description Returns true if the browser supports touch events, false otherwise.
   */
  supportsTouch : function() {
    return    this.isMobileSafari()
           || this.isAndroidBrowser()
           || this.isMobileChrome()
           || this.isMobileFirefox();
  },

  /**
   * @description Returns true if the browser supports CSS3, false otherwise.
   *
   * NOTE: This returns true if the browser sufficiently implements border-radius,
   * drop-shadow and linear-gradient.
   */
  supportsCss3 : function() {
    var engine = rwt.client.Client.getEngine();
    var version = rwt.client.Client.getVersion();
    return    engine === "blink"
           || engine === "webkit" && version >= 522
           || engine === "gecko" && version >= 2 // firefox 4+
           || engine === "trident" && version >= 9;
  },

  // NOTE [tb] : only works in IE right now.
  // Inspired by https://github.com/yonran/detect-zoom
  isZoomed : function() {
    var result = false;
    if( this._engineName === "trident" ) {
      if( this._engineVersionMajor >= 8 ) {
        result = ( screen.deviceXDPI / screen.logicalXDPI ) !== 1;
      } else {
        try {
          var rect = document.body.getBoundingClientRect();
          var zoom = ( rect.right - rect.left ) / document.body.offsetWidth;
          result = zoom !== 1;
        } catch( ex ) { // only happens in tests due to different bootstrap
          result = false;
        }
      }
    }
    return result;
  },

  /**
   * @description Returns application base path.
   */
  getBasePath : function() {
    if( !this._basePath ) {
      this._basePath = this._computeBasePath( document.location.href );
    }
    return this._basePath;
  },

  //////////
  // Helper

  _computeBasePath : function( url ) {
    var result = url;
    if( result.indexOf( "?" ) !== -1 ) {
      result = result.slice( 0, result.indexOf( "?" ) );
    }
    if( result.indexOf( "#" ) !== -1 ) {
      result = result.slice( 0, result.indexOf( "#" ) );
    }
    return result.slice( 0, result.lastIndexOf( "/" ) + 1 );
  },

  _initKonqueror : function() {
    if( !this._isBrowserDetected() ) {
      var vendor = navigator.vendor;
      var isKonqueror =    typeof vendor === "string" && vendor === "KDE"
                        && /KHTML\/([0-9\-\.]*)/.test( navigator.userAgent );
      if( isKonqueror ) {
        this._engineName = "webkit";
        this._browserName = "konqueror";
        // Howto translate KDE Version to Webkit Version? Currently emulate Safari 3.0.x for all versions.
        // this._engineVersion = RegExp.$1;
        this._parseVersion( "420" );
      }
    }
  },

  _initBlink : function() {
    if( !this._isBrowserDetected() ) {
      var userAgent = navigator.userAgent;
      // Some Blink browsers like Opera Mobile or Maxthon lack the window.chrome object.
      // A v8 feature check is necessary to cover all current Blink engine browsers as of Dec 2014.
      var isBlink = window.chrome || ( window.Intl && window.Intl.v8BreakIterator );
      if( isBlink ) {
        this._engineName = "blink";
        if( userAgent.indexOf( "OPR" ) !== -1 ) {
          this._browserName = "opera";
          /OPR\/([^ ]+)/.test( userAgent );
          this._parseVersion( RegExp.$1 );
        } else if( userAgent.indexOf( "Chrome" ) !== -1 ) {
          this._browserName = "chrome";
          /Chrome\/([^ ]+)/.test( userAgent );
          this._parseVersion( RegExp.$1 );
        } else {
          this._browserName = "other blink";
        }
      }
    }
  },

  _initWebkit : function() {
    if( !this._isBrowserDetected() ) {
      var userAgent = navigator.userAgent;
      var isWebkit =    userAgent.indexOf( "AppleWebKit" ) != -1
                     && /AppleWebKit\/([^ ]+)/.test( userAgent );
      if( isWebkit ) {
        this._engineName = "webkit";
        var version = RegExp.$1;
        var invalidCharacter = /[^\.0-9]/.exec( version );
        if( invalidCharacter ) {
          version = version.slice( 0, invalidCharacter.index );
        }
        this._parseVersion( version );
        if( userAgent.indexOf( "Safari" ) != -1 ) {
          if( userAgent.indexOf( "Android" ) != -1 ) {
            this._browserName = "android";
          } else {
            this._browserName = "safari";
          }
        } else if( userAgent.indexOf( "OmniWeb" ) != -1 ) {
          this._browserName = "omniweb";
        } else if( userAgent.indexOf( "Shiira" ) != -1 ) {
          this._browserName = "shiira";
        } else if( userAgent.indexOf( "NetNewsWire" ) != -1 ) {
          this._browserName = "netnewswire";
        } else if( userAgent.indexOf( "RealPlayer" ) != -1 ) {
          this._browserName = "realplayer";
        } else if( userAgent.indexOf( "Mobile" ) != -1 ) {
          // iPad reports this in fullscreen mode
          this._browserName = "safari";
        } else {
          this._browserName = "other webkit";
        }
      }
    }
  },

  _initGecko : function() {
    if( !this._isBrowserDetected() ) {
      var userAgent = navigator.userAgent;
      var isGecko =    userAgent.indexOf( "like Gecko" ) === -1
                    && userAgent.indexOf( "Gecko/" ) !== -1
                    && /rv\:([^\);]+)(\)|;)/.test( userAgent );
      if( isGecko ) {
        // http://www.mozilla.org/docs/dom/domref/dom_window_ref13.html
        this._engineName = "gecko";
        this._parseVersion( RegExp.$1 );
        if( userAgent.indexOf( "Firefox" ) != -1) {
          this._browserName = "firefox";
        } else if ( userAgent.indexOf( "Camino" ) != -1) {
          this._browserName = "camino";
        } else if ( userAgent.indexOf( "Galeon" ) != -1) {
          this._browserName = "galeon";
        } else {
          this._browserName = "other gecko";
        }
      }
    }
  },

  _initTrident : function() {
    if( !this._isBrowserDetected() ) {
      var userAgent = navigator.userAgent;
      if( /MSIE\s+([^\);]+)(\)|;)/.test( userAgent ) ) {
        this._parseVersion( RegExp.$1 );
        this._engineName = "trident";
        this._browserName = "explorer";
      } else if( userAgent.indexOf( "Trident" ) != -1 && /rv\:([^\);]+)(\)|;)/.test( userAgent ) ) {
        this._parseVersion( RegExp.$1 );
        this._engineName = "trident";
        this._browserName = "explorer";
      } else if( /Edge\/([^ ]+)/.test( userAgent ) ) {
        this._parseVersion( RegExp.$1 );
        this._engineName = "trident";
        this._browserName = "edge";
      }
    }
  },

  _isBrowserDetected : function() {
    return this._engineName !== "unknown";
  },

  _parseVersion : function( versionStr ) {
    if( typeof versionStr === "string" ) {
      var versionArr = versionStr.split( "." );
      this._engineVersion = parseFloat( versionStr );
      this._engineVersionMajor = parseInt( versionArr[ 0 ] || 0, 10 );
      this._engineVersionMinor = parseFloat( versionArr[ 1 ] || 0 );
      this._engineVersionRevision = parseFloat( versionArr[ 2 ] || 0 );
      this._engineVersionBuild = parseInt( versionArr[ 3 ] || 0, 10 );
    }
  },

  _initBoxSizing : function() {
    var vEngineBoxSizingAttr = [];
    switch( this._engineName ) {
      case "gecko":
        vEngineBoxSizingAttr.push( "-moz-box-sizing" );
      break;
      case "webkit":
      case "blink":
        vEngineBoxSizingAttr.push( "-khtml-box-sizing" );
        vEngineBoxSizingAttr.push( "-webkit-box-sizing" );
      break;
      default:
        vEngineBoxSizingAttr.push( "box-sizing" );
    }
    this._engineBoxSizingAttributes = vEngineBoxSizingAttr;
  },

  _initLocale : function() {
    var language = navigator.userLanguage || navigator.language;
    this._browserLocale = language.replace( "_", "-" );
  },

  _initPlatform : function() {
    var platformStr = navigator.platform;
    if(    platformStr.indexOf( "Windows" ) != -1
        || platformStr.indexOf( "Win32" ) != -1
        || platformStr.indexOf( "Win64" ) != -1 )
    {
      this._browserPlatform = "win";
    } else if(    platformStr.indexOf( "Macintosh" ) != -1
               || platformStr.indexOf( "MacPPC" ) != -1
               || platformStr.indexOf( "MacIntel" ) != -1 )
    {
      this._browserPlatform = "mac";
    } else if(   platformStr.indexOf( "X11" ) != -1
              || platformStr.indexOf( "Linux" ) != -1
              || platformStr.indexOf( "BSD" ) != -1 )
    {
      if( navigator.userAgent.indexOf( "Android" ) != -1 ) {
        this._browserPlatform = "android";
      } else {
        this._browserPlatform = "unix";
      }
    } else if(    platformStr.indexOf( "iPhone" ) != -1
               || platformStr.indexOf( "iPod" ) != -1
               || platformStr.indexOf( "iPad" ) != -1 )
    {
      this._browserPlatform = "ios";
    } else {
      this._browserPlatform = "other";
    }
  }

};

rwt.client.Client.__init();
rwt.util.Variant.define( "qx.client", rwt.client.Client.getEngine() );
