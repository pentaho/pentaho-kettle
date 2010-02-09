package org.pentaho.di.repository;

public enum ActionPermission {
  CREATE_CONTENT("Create Content"), //$NON-NLS-1$
  READ_CONTENT("Read Content"), //$NON-NLS-1$
  ADMINISTER_SECURITY("Administer Security"); //$NON-NLS-1$
  private final String userFriendlyName;
  
  private ActionPermission(String userFriendlyName) {
    this.userFriendlyName = userFriendlyName;
  }

  public String getUserFriendlyName() {
    return userFriendlyName;
  }
}
