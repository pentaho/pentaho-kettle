package org.pentaho.di.ui.core.runconfiguration.impl.pentaho;

import org.pentaho.di.core.runconfiguration.api.RunConfiguration;
import org.pentaho.di.core.runconfiguration.impl.pentaho.DefaultRunConfiguration;
import org.pentaho.di.ui.core.runconfiguration.api.RunConfigurationUI;
import org.pentaho.di.ui.core.runconfiguration.api.RunConfigurationUIFactory;

public class DefaultRunConfigurationUIFactory implements RunConfigurationUIFactory {
  @Override public RunConfigurationUI generateUI( RunConfiguration runConfiguration ) {
    return new DefaultRunConfigurationUI( (DefaultRunConfiguration) runConfiguration );
  }

  public String getType() {
    //TODO this is... kind of bad? Maybe we register by class?
    //return new DefaultRunConfiguration().getType();
    return DefaultRunConfiguration.class.getName();
  }
}
