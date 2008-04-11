package org.pentaho.di.ui.core.database.dialog;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.ui.database.event.DataHandler;

public class DataOverrideHandler extends DataHandler {
  
  private java.util.List<DatabaseMeta> databases;
  
  public DataOverrideHandler(){
  }

  public void explore() {
    
    Shell parent = (Shell)document.getRootElement().getManagedObject();
    DatabaseMeta dbinfo = new DatabaseMeta();
    getInfo(dbinfo);

    try {
      if (dbinfo.getAccessType() != DatabaseMeta.TYPE_ACCESS_PLUGIN) {
        DatabaseExplorerDialog ded = new DatabaseExplorerDialog(parent, SWT.NONE, dbinfo, databases, true);
        ded.open();
      } else {
        MessageBox mb = new MessageBox(parent, SWT.OK | SWT.ICON_INFORMATION);
        mb.setText(Messages.getString("DatabaseDialog.ExplorerNotImplemented.Title")); //$NON-NLS-1$
        mb.setMessage(Messages.getString("DatabaseDialog.ExplorerNotImplemented.Message")); //$NON-NLS-1$
        mb.open();
      }
    } catch (Exception e) {
      new ErrorDialog(
          parent,
          Messages.getString("DatabaseDialog.ErrorParameters.title"), Messages.getString("DatabaseDialog.ErrorParameters.description"), e); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  public void showFeatureList() {

    Shell parent = (Shell)document.getRootElement().getManagedObject();
    DatabaseMeta dbinfo = new DatabaseMeta();
    getInfo(dbinfo);

    try {
      java.util.List<RowMetaAndData> buffer = dbinfo.getFeatureSummary();
      if (buffer.size() > 0) {
        RowMetaInterface rowMeta = buffer.get(0).getRowMeta();
        java.util.List<Object[]> rowData = new ArrayList<Object[]>();
        for (RowMetaAndData row : buffer)
          rowData.add(row.getData());

        PreviewRowsDialog prd = new PreviewRowsDialog(parent, dbinfo, SWT.NONE, null, rowMeta, rowData); //$NON-NLS-1$
        prd
            .setTitleMessage(
                Messages.getString("DatabaseDialog.FeatureList.title"), Messages.getString("DatabaseDialog.FeatureList.title")); //$NON-NLS-1$ //$NON-NLS-2$
        prd.open();
      }
    } catch (Exception e) {
      new ErrorDialog(
          parent,
          Messages.getString("DatabaseDialog.FeatureListError.title"), Messages.getString("DatabaseDialog.FeatureListError.description"), e); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  public java.util.List<DatabaseMeta> getDatabases() {
    return databases;
  }

  public void setDatabases(java.util.List<DatabaseMeta> databases) {
    this.databases = databases;
  }

}
