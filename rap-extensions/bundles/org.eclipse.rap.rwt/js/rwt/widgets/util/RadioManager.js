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

/**
 * Each instance manages vItems set of radio options: qx.ui.form.RadioButton, qx.ui.toolbar.RadioButton, ...
 */
rwt.qx.Class.define("rwt.widgets.util.RadioManager",
{
  extend : rwt.qx.Target,




  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

  construct : function(vName, vMembers)
  {
    // we don't need the manager data structures
    this.base(arguments);

    // create item array
    this._items = [];

    // apply name property
    this.setName(vName != null ? vName : rwt.widgets.util.RadioManager.AUTO_NAME_PREFIX + this.toHashCode());

    if (vMembers != null)
    {
      // add() iterates over arguments, but vMembers is an array
      this.add.apply(this, vMembers);
    }
  },




  /*
  *****************************************************************************
     STATICS
  *****************************************************************************
  */

  statics : {
    AUTO_NAME_PREFIX : "qx-radio-"
  },




  /*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

  properties :
  {
    selected :
    {
      nullable : true,
      apply : "_applySelected",
      event : "changeSelected",
      check : "rwt.qx.Object"
    },

    name :
    {
      check : "String",
      nullable : true,
      apply : "_applyName"
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
      UTILITIES
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @return {var} TODOC
     */
    getItems : function() {
      return this._items;
    },


    /**
     * TODOC
     *
     * @type member
     * @return {var} TODOC
     */
    getEnabledItems : function()
    {
      var b = [];

      for (var i=0, a=this._items, l=a.length; i<l; i++)
      {
        if (a[i].getEnabled()) {
          b.push(a[i]);
        }
      }

      return b;
    },


    /**
     * TODOC
     *
     * @type member
     * @param vItem {var} TODOC
     * @param vChecked {var} TODOC
     * @return {void}
     */
    handleItemChecked : function(vItem, vChecked)
    {
      if (vChecked) {
        this.setSelected(vItem);
      } else if (this.getSelected() == vItem) {
        this.setSelected(null);
      }
    },




    /*
    ---------------------------------------------------------------------------
      REGISTRY
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @param varargs {var} TODOC
     * @return {void}
     */
    add : function()
    {
      var vItems = arguments;
      var vLength = vItems.length;
      var vItem;

      for (var i=0; i<vLength; i++)
      {
        vItem = vItems[i];

        if (rwt.util.Arrays.contains(this._items, vItem)) {
          return;
        }

        // Push RadioButton to array
        this._items.push(vItem);

        // Inform radio button about new manager
        vItem.setManager(this);

        // Need to update internal value?
        if (vItem.getChecked()) {
          this.setSelected(vItem);
        }

        // Apply Make name the same
        vItem.setName(this.getName());
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @param vItem {var} TODOC
     * @return {void}
     */
    remove : function(vItem)
    {
      // Remove RadioButton from array
      rwt.util.Arrays.remove(this._items, vItem);

      // Inform radio button about new manager
      vItem.setManager(null);

      // if the radio was checked, set internal selection to null
      if (vItem.getChecked()) {
        this.setSelected(null);
      }
    },




    /*
    ---------------------------------------------------------------------------
      APPLY ROUTINES
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applySelected : function(value, old)
    {
      if (old) {
        old.setChecked(false);
      }

      if (value) {
        value.setChecked(true);
      }
    },


    /**
     * TODOC
     *
     * @type member
     * @param value {var} Current value
     * @param old {var} Previous value
     */
    _applyName : function(value)
    {
      for (var i=0, vItems=this._items, vLength=vItems.length; i<vLength; i++) {
        vItems[i].setName(value);
      }
    },




    /*
    ---------------------------------------------------------------------------
      SELECTION
    ---------------------------------------------------------------------------
    */

    /**
     * TODOC
     *
     * @type member
     * @param vItem {var} TODOC
     * @return {void}
     */
    selectNext : function(vItem)
    {
      var vIndex = this._items.indexOf(vItem);

      if (vIndex == -1) {
        return;
      }

      var i = 0;
      var vLength = this._items.length;

      // Find next enabled item
      vIndex = (vIndex + 1) % vLength;

      while (i < vLength && !this._items[vIndex].getEnabled())
      {
        vIndex = (vIndex + 1) % vLength;
        i++;
      }

      this._selectByIndex(vIndex);
    },


    /**
     * TODOC
     *
     * @type member
     * @param vItem {var} TODOC
     * @return {void}
     */
    selectPrevious : function(vItem)
    {
      var vIndex = this._items.indexOf(vItem);

      if (vIndex == -1) {
        return;
      }

      var i = 0;
      var vLength = this._items.length;

      // Find previous enabled item
      vIndex = (vIndex - 1 + vLength) % vLength;

      while (i < vLength && !this._items[vIndex].getEnabled())
      {
        vIndex = (vIndex - 1 + vLength) % vLength;
        i++;
      }

      this._selectByIndex(vIndex);
    },


    /**
     * TODOC
     *
     * @type member
     * @param vIndex {var} TODOC
     * @return {void}
     */
    _selectByIndex : function(vIndex)
    {
      if (this._items[vIndex].getEnabled())
      {
        this.setSelected(this._items[vIndex]);
        this._items[vIndex].setFocused(true);
      }
    }
  },




  /*
  *****************************************************************************
     DESTRUCTOR
  *****************************************************************************
  */

  destruct : function() {
    this._disposeObjectDeep("_items", 1);
  }
});
