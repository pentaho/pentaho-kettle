/*******************************************************************************
 * Copyright (c) 2004, 2014 1&1 Internet AG, Germany, http://www.1und1.de,
 *                          EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for the Eclipse Remote Application Platform
 ******************************************************************************/

rwt.qx.Class.define( "rwt.theme.AppearanceManager", {

  extend : rwt.util.ObjectManager,

  statics : {

    getInstance : function() {
      return rwt.runtime.Singletons.get( rwt.theme.AppearanceManager );
    }

  },

  construct : function() {
    this.base( arguments );
    this.__cache = {};
    this.__stateMap = {};
    this.__stateMapLength = 1;
  },

  members : {

    setCurrentTheme : function( appearance ) {
      this._currentTheme = appearance;
      this.syncAppearanceTheme();
    },

    getCurrentTheme : function() {
      return this._currentTheme;
    },

    syncAppearanceTheme : function() {
      if( !this._currentTheme ) {
        return;
      }
      if( this._currentTheme ) {
        this.__cache[this._currentTheme.name] = {};
      }
      if( rwt.runtime.System.getInstance().getUiReady() ) {
        rwt.widgets.base.ClientDocument.getInstance()._recursiveAppearanceThemeUpdate( this._currentTheme );
      }
    },

    styleFrom : function( id, states ) {
      var theme = this.getCurrentTheme();
      if( !theme ) {
        return;
      }
      return this.styleFromTheme( theme, id, states );
    },

    styleFromTheme : function( theme, id, states ) {
      var entry = theme.appearances[id];
      if( !entry ) {
        if( rwt.util.Variant.isSet( "qx.debug", "on" ) ) {
          throw new Error( "Missing appearance entry: " + id );
        }
        return null;
      }

      // Fast fallback to super entry
      if( !entry.style ) {
        if( entry.include ) {
          return this.styleFromTheme( theme, entry.include, states );
        } else {
          return null;
        }
      }

      // Creating cache-able ID
      var map = this.__stateMap;
      var helper = [id];
      for( var state in states ) {
        if( !map[state] ) {
          map[state] = this.__stateMapLength++;
        }
        helper[map[state]] = true;
      }
      var unique = helper.join();
      // Using cache if available
      var cache = this.__cache[theme.name];
      if( cache && cache[unique] !== undefined ) {
        return cache[unique];
      }

      var result;

      // Otherwise "compile" the appearance
      // If a include or base is defined, too, we need to merge the entries
      if( entry.include || entry.base ) {
        // This process tries to insert the original data first, and
        // append the new data later, to higher priorise the local
        // data above the included/inherited data. This is especially needed
        // for property groups or properties which includences other
        // properties when modified.
        var local = entry.style(states);

        // Gather included data
        var incl;
        if( entry.include ) {
          incl = this.styleFromTheme(theme, entry.include, states);
        }

        // Create new map
        result = {};

        // Copy base data, but exclude overwritten local and included stuff
        if( entry.base ) {
          var base = this.styleFromTheme( entry.base, id, states );
          if( entry.include ) {
            for( var key in base ) {
              if( incl[key] === undefined && local[key] === undefined ) {
                result[key] = base[key];
              }
            }
          } else {
            for( var key in base ) {
              if( local[key] === undefined ) {
                result[key] = base[key];
              }
            }
          }
        }

        // Copy include data, but exclude overwritten local stuff
        if( entry.include ) {
          for( var key in incl ) {
            if( local[key] === undefined ) {
              result[key] = incl[key];
            }
          }
        }
        // Append local data
        for( var key in local ) {
          result[key] = local[key];
        }
      } else {
        result = entry.style( states );
      }
      // Cache new entry and return
      if( cache ) {
        cache[unique] = result || null;
      }
      return result || null;
    }
  },

  destruct : function() {
    this._disposeFields( "__cache", "__stateMap" );
  }

} );
