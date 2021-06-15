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

/** Event object for property changes. */
rwt.qx.Class.define("rwt.event.ChangeEvent",
{
  extend : rwt.event.Event,




  /*
  *****************************************************************************
     CONSTRUCTOR
  *****************************************************************************
  */

 /**
  * @param type {String} the type name of the event
  * @param value {var} additional value which should be passed to the event listener
  * @param old {var} additional old value which should be passed to the event listener
  */
  construct : function(type, value, old)
  {
    this.base(arguments, type);

    this.setValue(value);
    this.setOldValue(old);
  },




  /*
  *****************************************************************************
     PROPERTIES
  *****************************************************************************
  */

  properties :
  {
    value : { _fast : true },
    oldValue : { _fast : true }
  },





  /*
  *****************************************************************************
     MEMBERS
  *****************************************************************************
  */

  members :
  {
    /**
     * Returns the event data
     *
     * @deprecated use {@link #getValue} instead
     */
    getData : function() {
      return this.getValue();
    }
  },





  /*
  *****************************************************************************
     DESTRUCTOR
  *****************************************************************************
  */

  destruct : function() {
    this._disposeFields("_valueValue", "_valueOldValue");
  }
});
