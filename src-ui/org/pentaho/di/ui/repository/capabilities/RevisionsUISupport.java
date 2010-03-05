package org.pentaho.di.ui.repository.capabilities;

import org.pentaho.ui.xul.impl.DefaultXulOverlay;

public class RevisionsUISupport extends AbstractRepositoryExplorerUISupport{


  @Override
  protected void setup() {
    overlays.add(new DefaultXulOverlay("org/pentaho/di/ui/repository/repositoryexplorer/xul/version-layout-overlay.xul")); //$NON-NLS-1$
  }
}
