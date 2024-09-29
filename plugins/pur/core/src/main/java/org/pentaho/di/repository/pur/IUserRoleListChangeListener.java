/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.repository.pur;

public interface IUserRoleListChangeListener {

  /**
   * Event listener interface for user role list change events.
   */

  /**
   * Fired when the user role list change
   * 
   */
  void onChange();
}
