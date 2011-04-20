package org.pentaho.ui.database;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.swt.SwtXulLoader;

public class DatabaseConnectionDialog {

  public static final String DIALOG_DEFINITION_FILE = "org/pentaho/ui/database/databasedialog.xul"; //$NON-NLS-1$

  private Map<String, String> extendedClasses = new HashMap<String, String>();

  public DatabaseConnectionDialog() {
  }

  public void registerClass(String key, String className) {
    extendedClasses.put(key, className);
  }

  public XulDomContainer getSwtInstance(Shell shell) throws XulException {

    XulDomContainer container = null;
    SwtXulLoader loader = new SwtXulLoader();

    Iterable<String> keyIterable = extendedClasses.keySet();
    for (Object key : keyIterable) {
      loader.register((String) key, extendedClasses.get(key));
    }
    loader.setOuterContext(shell);
    container = loader.loadXul(DIALOG_DEFINITION_FILE, Messages.getBundle());
    container.initialize();
    return container;
  }

}
