package org.pentaho.di.ui.spoon;

import java.io.IOException;

import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.ui.xul.XulSettingsManager;

public class XulSpoonSettingsManager implements XulSettingsManager {
  private static XulSpoonSettingsManager instance = new XulSpoonSettingsManager();
  
  public String getSetting(String prop) {
    return PropsUI.getInstance().getCustomParameter(prop, null);
  }

  public void save() throws IOException {
    PropsUI.getInstance().saveProps();

  }

  public void storeSetting(String prop, String val) {
    PropsUI.getInstance().setCustomParameter(prop, val);

  }
  
  public static XulSpoonSettingsManager getInstance(){
    return instance;
  }
}