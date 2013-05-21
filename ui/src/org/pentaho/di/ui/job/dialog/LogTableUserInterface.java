package org.pentaho.di.ui.job.dialog;

import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.core.logging.LogTableInterface;

public interface LogTableUserInterface {
  
  public void retrieveLogTableOptions(LogTableInterface logTable);
  
  public void showLogTableOptions(Composite composite, LogTableInterface logTable);
}
