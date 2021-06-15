/*******************************************************************************
 * Copyright (c) 2007, 2014 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

namespace( "rwt.theme.BorderDefinitions" );


rwt.theme.BorderDefinitions = {

  getDefinition : function( name ) {
    return this._definitions[ name ];
  },

  _definitions : {

    ///////////////////////////
    // Names used in ThemeStore

    "inset" : {
      color : [ "shadow", "highlight", "highlight", "shadow" ],
      innerColor : [ "darkshadow", "lightshadow", "lightshadow", "darkshadow" ],
      width : 2
    },

    "thinInset" : {
      color : [ "shadow", "highlight", "highlight", "shadow" ],
      width : 1
    },

    "outset" : {
      color : [ "lightshadow", "darkshadow", "darkshadow", "lightshadow" ],
      innerColor : [ "highlight", "shadow", "shadow", "highlight" ],
      width : 2
    },

    "thinOutset" : {
      color : [ "highlight", "shadow", "shadow", "highlight" ],
      width : 1
    },

    "groove" : {
      color : [ "shadow", "highlight", "highlight", "shadow" ],
      innerColor : [ "highlight", "shadow", "shadow", "highlight" ],
      width : 2
    },

    "ridge" : {
      color : [ "highlight", "shadow", "shadow", "highlight" ],
      innerColor : [ "shadow", "highlight", "highlight", "shadow" ],
      width : 2
    },

    ////////////////////////////////////
    // Names also used in AppearanceBase

    "shadow" : {
      width : 1,
      color :  [ "shadow", "shadow", "shadow", "shadow" ]
    },

    "verticalDivider" : {
      width : [ 1, 0, 1, 0 ],
      color : [ "shadow", null, "highlight", null ]
    },

    "horizontalDivider" : {
      width : [ 0, 1, 0, 1 ],
      color : [ null, "shadow", null, "highlight" ]
    },

    "separator.shadowin.horizontal.border" : {
      width : [ 1, 0, 1, 0 ],
      color : [ "lightshadow", null, "highlight" ]
    },

    "separator.shadowin.vertical.border" : {
      width : [ 0, 1, 0, 1 ],
      color : [ null, "highlight", null, "lightshadow" ]
    },

    "separator.shadowout.horizontal.border" : {
      width : [ 1, 0, 1, 0 ],
      color : [ "highlight", null, "shadow", null ]
    },

    "separator.shadowout.vertical.border" : {
      width : [ 0, 1, 0, 1 ],
      color : [ null, "shadow", null, "highlight" ]
    }

  }

};
