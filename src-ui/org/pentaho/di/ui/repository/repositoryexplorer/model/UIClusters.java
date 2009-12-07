package org.pentaho.di.ui.repository.repositoryexplorer.model;

import java.util.List;

import org.pentaho.ui.xul.util.AbstractModelList;

public class UIClusters extends AbstractModelList<UICluster> {

  public UIClusters() {
  }

  public UIClusters(List<UICluster> clusters) {
    super(clusters);
  }

  @Override
  protected void fireCollectionChanged() {
    this.changeSupport.firePropertyChange("children", null, this.getChildren()); //$NON-NLS-1$
  }

}
