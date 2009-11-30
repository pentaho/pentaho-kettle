package org.pentaho.di.ui.repository.repositoryexplorer.model;

import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryContent;

public class UITransformation extends UIRepositoryContent {

  public UITransformation() {
  }

  public UITransformation(RepositoryContent rc, UIRepositoryDirectory parent, Repository rep) {
    super(rc, parent, rep);
  }

  @Override
  public String getImage() {
    return "images/transformation.png";
  }

  @Override
  public void setName(String name) throws Exception {
    super.setName(name);
    rep.renameTransformation(this.getObjectId(), getRepositoryDirectory(), name);
    uiParent.fireCollectionChanged();
  }

}
