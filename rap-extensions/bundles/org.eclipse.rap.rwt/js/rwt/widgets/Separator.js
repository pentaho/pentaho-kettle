/*******************************************************************************
 * Copyright (c) 2002, 2014 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

/**
 * This class represents SWT Labels with style SWT.SEPARATOR
 */
rwt.qx.Class.define( "rwt.widgets.Separator", {
  extend : rwt.widgets.base.BoxLayout,

  construct : function() {
    this.base( arguments );
    // the actual separator line
    this._line = new rwt.widgets.base.Parent();
    this._line.setAnonymous( true );
    this._line.setAppearance( "separator-line" );
    this.add( this._line );
  },

  properties : {

    appearance : {
      refine : true,
      init : "separator"
    }

  },

  destruct : function() {
    this._line.dispose();
    this._line = null;
  },

  members : {

    _getSubWidgets : function() {
      return [ this._line ];
    },

    setLineStyle : function( style ) {
      this._line.addState( style );
    },

    setLineOrientation : function( value ) {
      if( value == "vertical" ) {
        this.setHorizontalChildrenAlign( "center" );
        this._line.setHeight( "100%" );
      } else {
        this.setVerticalChildrenAlign( "middle" );
        this._line.setWidth( "100%" );
      }
      this._line.toggleState( "rwt_VERTICAL", value == "vertical" );
    }

  }

} );
