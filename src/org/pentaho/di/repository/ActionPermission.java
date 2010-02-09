package org.pentaho.di.repository;

public enum ActionPermission {
  CREATE_CONTENT("SecurityTab.ActionPermission.CreateContent"), //$NON-NLS-1$
  READ_CONTENT("SecurityTab.ActionPermission.ReadContent"), //$NON-NLS-1$
  ADMINISTER_SECURITY("SecurityTab.ActionPermission.AdministerSecurity"); //$NON-NLS-1$
  private final String description;
  
  private ActionPermission(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}
