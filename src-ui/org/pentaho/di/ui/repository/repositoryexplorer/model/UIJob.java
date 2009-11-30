package org.pentaho.di.ui.repository.repositoryexplorer.model;

import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryContent;

public class UIJob extends UIRepositoryContent {

  public UIJob() {
  }

  public UIJob(RepositoryContent rc, UIRepositoryDirectory parent, Repository rep) {
    super(rc, parent, rep);
  }

  @Override
  public String getImage() {
    return "images/job.png";
  }

  @Override
  public void setName(String name) throws Exception {
    super.setName(name);
    rep.renameJob(this.getObjectId(), getRepositoryDirectory(), name);
    uiParent.fireCollectionChanged();
  }

}
