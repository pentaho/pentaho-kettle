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

package org.pentaho.di.ui.repository.pur.repositoryexplorer.abs.controller;

import org.pentaho.ui.xul.XulEventSourceAdapter;

public class PermissionPropertyModel extends XulEventSourceAdapter implements java.io.Serializable {

  private static final long serialVersionUID = -1190983562966490855L; /* EESOURCE: UPDATE SERIALVERUID */
  private static final String createPermissionProperty = "createPermissionGranted"; //$NON-NLS-1$
  private static final String readPermissionProperty = "readPermissionGranted"; //$NON-NLS-1$

  private boolean createPermissionGranted = false;

  private boolean readPermissionGranted = false;

  public void setCreatePermissionGranted( boolean createPermissionGranted ) {
    this.createPermissionGranted = createPermissionGranted;
    this.firePropertyChange( createPermissionProperty, null, createPermissionGranted );
  }

  public boolean isCreatePermissionGranted() {
    return createPermissionGranted;
  }

  public void setReadPermissionGranted( boolean readPermissionGranted ) {
    this.readPermissionGranted = readPermissionGranted;
    this.firePropertyChange( readPermissionProperty, null, readPermissionGranted );
  }

  public boolean isReadPermissionGranted() {
    return readPermissionGranted;
  }
}
