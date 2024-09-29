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


package org.pentaho.di.ui.repository.repositoryexplorer.uisupport;

import org.pentaho.ui.xul.impl.DefaultXulOverlay;

public class RepositoryExplorerDefaultXulOverlay extends DefaultXulOverlay {
  private Class<?> packageClass;

  public RepositoryExplorerDefaultXulOverlay( String overlayUri ) {
    super( overlayUri );
  }

  public RepositoryExplorerDefaultXulOverlay( String overlayUri, Class<?> packageClass ) {
    super( overlayUri );
    this.packageClass = packageClass;
  }

  public void setPackageClass( Class<?> packageClass ) {
    this.packageClass = packageClass;
  }

  public Class<?> getPackageClass() {
    return packageClass;
  }

}
