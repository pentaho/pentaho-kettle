package org.pentaho.di.repository;

import java.util.EnumSet;

public interface IActionPermission {

  public void addActionPermission(ActionPermission actionPermission);
  public void removeActionPermission(ActionPermission actionPermission);
  public void setActionPermissions(EnumSet<ActionPermission> actionPermissions);
  public EnumSet<ActionPermission> getActionPermissions();
}
