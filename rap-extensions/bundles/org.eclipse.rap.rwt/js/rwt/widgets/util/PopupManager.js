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

/** This singleton is used to manager multiple instances of popups and their state. */
rwt.qx.Class.define( "rwt.widgets.util.PopupManager", {

  extend : rwt.util.ObjectManager,

  statics : {

    getInstance : function() {
      return rwt.runtime.Singletons.get( rwt.widgets.util.PopupManager );
    }

  },

  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members :
  {
    /*
    ---------------------------------------------------------------------------
      METHODS
    ---------------------------------------------------------------------------
    */

    /**
     * Updates all registered popups
     *
     * @type member
     * @param vTarget {rwt.widgets.base.Popup} current widget
     * @return {void}
     */
    update : function(vTarget)
    {
      // be sure that target is correctly set (needed for contains() later)
      if (!(vTarget instanceof rwt.widgets.base.Widget)) {
        vTarget = null;
      }

      var vPopup, vHashCode;
      var vAll = this.getAll();

      for (vHashCode in vAll)
      {
        vPopup = vAll[vHashCode];

        if (!vPopup.getAutoHide() || vTarget == vPopup || vPopup.contains(vTarget)) {
          continue;
        }

        vPopup.hide();
      }
    }
  }
});
