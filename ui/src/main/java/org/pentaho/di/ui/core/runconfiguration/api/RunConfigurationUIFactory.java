package org.pentaho.di.ui.core.runconfiguration.api;

import org.pentaho.di.core.runconfiguration.api.RunConfiguration;

public interface RunConfigurationUIFactory {
  RunConfigurationUI generateUI( RunConfiguration runConfiguration );
  String getType();
}
