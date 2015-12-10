/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
