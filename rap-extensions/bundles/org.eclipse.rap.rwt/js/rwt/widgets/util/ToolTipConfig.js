/*******************************************************************************
 * Copyright (c) 2011, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

(function(){

namespace( "rwt.client" );

  rwt.widgets.util.ToolTipConfig = {

    getConfig : function( widget ) {
      if( widget.getParent() instanceof rwt.widgets.CoolBar ) {
        return this._getClickableConfig( widget );
      }
      switch( widget.classname ) {
        case "rwt.widgets.ControlDecorator":
          return this._quickConfig;
        case "rwt.widgets.Label":
          return this._getLabelConfig( widget );
        case "rwt.widgets.Button":
        case "rwt.widgets.TabItem":
        case "rwt.widgets.CTabItem":
          return this._getClickableConfig( widget );
        case "rwt.widgets.Text":
        case "rwt.widgets.Spinner":
        case "rwt.widgets.Combo":
        case "rwt.widgets.DateTimeDate":
        case "rwt.widgets.DateTimeTime":
          return this._fieldConfig;
        case "rwt.widgets.ToolItem":
          return this._getClickableConfig( widget );
        case "rwt.widgets.ProgressBar":
          return rwt.util.Objects.mergeWith( { "overlap" : 3 }, this._getBarConfig( widget ) );
        case "rwt.widgets.Scale":
        case "rwt.widgets.Slider":
          return rwt.util.Objects.mergeWith( { "overlap" : -2 }, this._getBarConfig( widget ) );
        case "rwt.widgets.base.GridRowContainer":
          return this._rowConfig;
        default:
          return this._defaultConfig;
      }

    },

    _getBarConfig : function( widget ) {
      if( widget.hasState( "rwt_VERTICAL" ) ) {
        return this._verticalBarConfig;
      } else {
        return this._horizontalBarConfig;
      }
    },

    _getClickableConfig : function( widget ) {
      if( widget.hasState( "rwt_VERTICAL" ) ) {
        return this._verticalClickableConfig;
      } else {
        var appearance = widget.getAppearance();
        var result = this._horizontalClickableConfig;
        if( appearance === "check-box" || appearance === "radio-button" ) {
          return rwt.util.Objects.mergeWith( { "overlap" : -1 }, result );
        } else {
          return result;
        }
      }
    },

    _getLabelConfig : function( widget ) {
      if( !widget._rawText ) {
        return this._quickConfig;
      } else {
        return this._fieldConfig;
      }
    },

    _defaultConfig : {
      "position" : "mouse",
      "appearOn" : "rest",
      "disappearOn" : "move",
      "appearDelay" : 1000,
      "disappearDelay" : 200,
      "autoHide" : true
    },

    _horizontalClickableConfig : {
      "position" : "horizontal-center",
      "appearOn" : "enter",
      "disappearOn" : "exit",
      "appearDelay" : 200,
      "disappearDelay" : 100,
      "autoHide" : true
    },

    _verticalClickableConfig : {
      "position" : "vertical-center",
      "appearOn" : "enter",
      "disappearOn" : "exit",
      "appearDelay" : 200,
      "disappearDelay" : 100,
      "autoHide" : true
    },

    _horizontalBarConfig : {
      "position" : "horizontal-center",
      "appearOn" : "enter",
      "disappearOn" : "exit",
      "appearDelay" : 200,
      "disappearDelay" : 100,
      "autoHide" : false
    },

    _verticalBarConfig : {
      "position" : "vertical-center",
      "appearOn" : "enter",
      "disappearOn" : "exit",
      "appearDelay" : 200,
      "disappearDelay" : 100,
      "autoHide" : false
    },

    _fieldConfig : {
      "position" : "align-left",
      "appearOn" : "rest",
      "disappearOn" : "exit",
      "appearDelay" : 500,
      "disappearDelay" : 200,
      "autoHide" : true
    },

    _quickConfig : {
      "position" : "horizontal-center",
      "appearOn" : "enter",
      "disappearOn" : "exit",
      "appearDelay" : 20,
      "disappearDelay" : 50,
      "autoHide" : false,
      "overlap" : -1
    },

    _rowConfig : {
      "position" : "align-left",
      "appearOn" : "rest",
      "disappearOn" : "exitTargetBounds",
      "appearDelay" : 700,
      "disappearDelay" : 100,
      "autoHide" : true
    }


  };

}());
