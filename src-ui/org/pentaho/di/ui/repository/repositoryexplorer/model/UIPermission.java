package org.pentaho.di.ui.repository.repositoryexplorer.model;

public enum UIPermission {
  READ("UIPermission.READ_DESC"), //$NON-NLS-1$ 
  CREATE("UIPermission.CREATE_DESC"), //$NON-NLS-1$
  UPDATE("UIPermission.UPDATE_DESC"), //$NON-NLS-1$
  MODIFY_PERMISSION("UIPermission.MODIFY_PERMISSION_DESC"), //$NON-NLS-1$
  DELETE("UIPermission.DELETE_DESC"); //$NON-NLS-1$

  private String description;

  private UIPermission(String description) {
    this.description = description;
  }
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

}
