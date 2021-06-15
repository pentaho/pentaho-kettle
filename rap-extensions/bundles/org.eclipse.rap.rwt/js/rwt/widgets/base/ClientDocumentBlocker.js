/*******************************************************************************
 * Copyright (c) 2004, 2013 1&1 Internet AG, Germany, http://www.1und1.de,
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

/**
 * rwt.widgets.base.ClientDocumentBlocker blocks the inputs from the user.
 * This will be used internally to allow better modal dialogs for example.
 *
 * @appearance blocker
 */
rwt.qx.Class.define("rwt.widgets.base.ClientDocumentBlocker",
{
  extend : rwt.widgets.base.Terminator,
  include : rwt.animation.VisibilityAnimationMixin,




  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  construct : function()
  {
    this.base(arguments);

    this.initTop();
    this.initLeft();

    this.initWidth();
    this.initHeight();

    this.initZIndex();
  },




  /*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

  properties :
  {
    appearance :
    {
      refine : true,
      init : "client-document-blocker"
    },

    zIndex :
    {
      refine : true,
      init : 1e8
    },

    top :
    {
      refine : true,
      init : 0
    },

    left :
    {
      refine : true,
      init : 0
    },

    width :
    {
      refine : true,
      init : "100%"
    },

    height :
    {
      refine : true,
      init : "100%"
    },

    display :
    {
      refine : true,
      init : false
    }
  },




  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members :
  {
    // We must omit that the focus root is changed to the client document
    // when processing a mouse down event on this widget.
    getFocusRoot : function() {
      return null;
    }
  }
});
