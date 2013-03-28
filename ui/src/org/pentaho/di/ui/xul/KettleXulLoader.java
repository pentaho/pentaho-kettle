package org.pentaho.di.ui.xul;

import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.swt.SwtXulLoader;

public class KettleXulLoader extends SwtXulLoader {

  public KettleXulLoader() throws XulException {
    super();
    
    parser.handlers.remove("DIALOG");
    parser.registerHandler("DIALOG", org.pentaho.di.ui.xul.KettleDialog.class.getName());
  }
}
