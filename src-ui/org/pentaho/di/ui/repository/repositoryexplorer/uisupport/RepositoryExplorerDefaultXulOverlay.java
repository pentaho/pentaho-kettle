package org.pentaho.di.ui.repository.repositoryexplorer.uisupport;

import org.pentaho.ui.xul.impl.DefaultXulOverlay;

public class RepositoryExplorerDefaultXulOverlay extends DefaultXulOverlay{
  private Class<?> packageClass;
  public RepositoryExplorerDefaultXulOverlay(String overlayUri) {
    super(overlayUri);
  }
  public RepositoryExplorerDefaultXulOverlay(String overlayUri, Class<?> packageClass) {
    super(overlayUri);
    this.packageClass = packageClass;
  }  
  public void setPackageClass(Class<?> packageClass) {
    this.packageClass = packageClass;
  }
  public Class<?> getPackageClass() {
    return packageClass;
  }

}
