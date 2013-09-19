package org.pentaho.di.ui.trans.dialog;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.TransMeta;

public interface TransDialogPluginInterface {
  public void addTab(TransMeta transMeta, Shell shell, CTabFolder tabFolder);
  public void getData(TransMeta transMeta) throws KettleException;
  public void ok(TransMeta transMeta) throws KettleException;
  public CTabItem getTab();
}
