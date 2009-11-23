package org.pentaho.di.ui.spoon;

import java.net.URL;
import java.util.List;

import org.pentaho.ui.xul.impl.XulEventHandler;

public interface SpoonPlugin {
  public List<URL> getOverlays();
  public List<? extends XulEventHandler> getEventHandlers();
}
