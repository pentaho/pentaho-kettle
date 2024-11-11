/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.repository.pur;

import java.util.ArrayList;

public class UserRoleListChangeListenerCollection extends ArrayList<IUserRoleListChangeListener> implements
    java.io.Serializable {

  private static final long serialVersionUID = -7723158765985622583L; /* EESOURCE: UPDATE SERIALVERUID */

  /**
   * Fires a user role list change event to all listeners.
   * 
   */
  public void fireOnChange() {
    for ( IUserRoleListChangeListener listener : this ) {
      listener.onChange();
    }
  }
}
