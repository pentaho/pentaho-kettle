/*******************************************************************************
 * Copyright (c) 2007, 2012 David Perez Carmona, EclipseSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    1&1 Internet AG and others - original API and implementation
 *    EclipseSource - adaptation for RAP
 ******************************************************************************/

/**
 * A popup that can be resized.
 */
rwt.qx.Class.define("rwt.widgets.base.ResizablePopup",
{
  extend   : rwt.widgets.base.Popup,
  include  : rwt.widgets.util.MResizable,

  construct : function()
  {
    this.base(arguments);

    this.initMinWidth();
    this.initMinHeight();
    this.initWidth();
    this.initHeight();
  },




  /*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

  properties:
  {
    appearance :
    {
      refine : true,
      init : "resizer"
    },

    minWidth :
    {
      refine : true,
      init : "auto"
    },

    minHeight :
    {
      refine : true,
      init : "auto"
    },

    width :
    {
      refine : true,
      init : "auto"
    },

    height :
    {
      refine : true,
      init : "auto"
    }
  },






  members:
  {

    _changeWidth: function(value) {
      this.setWidth(value);
    },

    _changeHeight: function(value) {
      this.setHeight(value);
    },

    /**
     * @return {Widget}
     */
    _getResizeParent: function() {
      return this.getParent();
    },

    /**
     * @return {Widget}
     */
    _getMinSizeReference: function() {
      return this;
    }
  }
});
