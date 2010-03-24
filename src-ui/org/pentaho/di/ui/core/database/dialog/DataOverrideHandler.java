package org.pentaho.di.ui.core.database.dialog;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.database.dialog.tags.ExtTextbox;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.ui.database.event.DataHandler;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulTree;

public class DataOverrideHandler extends DataHandler {
  boolean cancelPressed = false;
 
  @Override
  public void onCancel() {
    super.onCancel();
    this.cancelPressed = true;
  }

  @Override
  public void onOK() {
    super.onOK();
    this.cancelPressed = false;
  }

  
  @Override
  public Object getData() {
    if(!cancelPressed) {
      return super.getData();
    } else {
      return null;
    }
  }


  private static Class<?> PKG = DataOverrideHandler.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private java.util.List<DatabaseMeta> databases;
  
  public DataOverrideHandler(){
  }

  public void explore() {
    
    Shell parent = getShell();
    
    DatabaseMeta dbinfo = new DatabaseMeta();
    getInfo(dbinfo);

    try {
      if (dbinfo.getAccessType() != DatabaseMeta.TYPE_ACCESS_PLUGIN) {
        DatabaseExplorerDialog ded = new DatabaseExplorerDialog(parent, SWT.NONE, dbinfo, databases, true);
        ded.open();
      } else {
        MessageBox mb = new MessageBox(parent, SWT.OK | SWT.ICON_INFORMATION);
        mb.setText(BaseMessages.getString(PKG, "DatabaseDialog.ExplorerNotImplemented.Title")); //$NON-NLS-1$
        mb.setMessage(BaseMessages.getString(PKG, "DatabaseDialog.ExplorerNotImplemented.Message")); //$NON-NLS-1$
        mb.open();
      }
    } catch (Exception e) {
      new ErrorDialog( parent, BaseMessages.getString(PKG, "DatabaseDialog.ErrorParameters.title"), BaseMessages.getString(PKG, "DatabaseDialog.ErrorParameters.description"), e); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  private Shell getShell(){
    Object obj = document.getRootElement().getManagedObject();
    Shell parent;
    if(obj instanceof Shell){
      parent = (Shell) obj;
    } else {
      parent = ((Composite) obj).getShell();
    }
    if( parent == null){
      throw new IllegalStateException("Could not get Shell reference from Xul Dialog Tree.");
    }
    return parent;
  }
  
  public void showFeatureList() {

    Shell parent = getShell();
    
    
    DatabaseMeta dbinfo = new DatabaseMeta();
    getInfo(dbinfo);

    try {
      java.util.List<RowMetaAndData> buffer = dbinfo.getFeatureSummary();
      if (buffer.size() > 0) {
        RowMetaInterface rowMeta = buffer.get(0).getRowMeta();
        java.util.List<Object[]> rowData = new ArrayList<Object[]>();
        for (RowMetaAndData row : buffer)
          rowData.add(row.getData());

        PreviewRowsDialog prd = new PreviewRowsDialog(parent, dbinfo, SWT.NONE, null, rowMeta, rowData); 
        prd.setTitleMessage(BaseMessages.getString(PKG, "DatabaseDialog.FeatureList.title"), BaseMessages.getString(PKG, "DatabaseDialog.FeatureList.title")); //$NON-NLS-1$ //$NON-NLS-2$
        prd.open();
      }
    } catch (Exception e) {
      new ErrorDialog( parent, BaseMessages.getString(PKG, "DatabaseDialog.FeatureListError.title"), BaseMessages.getString(PKG, "DatabaseDialog.FeatureListError.description"), e); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }
  
  @Override
  protected void getControls() {
    
    super.getControls();
    
    XulTextbox[] boxes = new XulTextbox[] {hostNameBox, databaseNameBox, portNumberBox, userNameBox, passwordBox,
        customDriverClassBox, customUrlBox, dataTablespaceBox, indexTablespaceBox, poolSizeBox, maxPoolSizeBox,
        languageBox, systemNumberBox, clientBox};

    for (int i = 0; i < boxes.length; i++) {
      XulTextbox xulTextbox = boxes[i];
      if ((xulTextbox != null) && (xulTextbox instanceof ExtTextbox)){
        ExtTextbox ext = (ExtTextbox)xulTextbox;
        ext.setVariableSpace(databaseMeta);
      }
    }
    
    XulTree[] trees = new XulTree[] {poolParameterTree, clusterParameterTree, optionsParameterTree};

    for (int i = 0; i < trees.length; i++) {
      XulTree xulTree = trees[i];
      if (xulTree != null){
        xulTree.setData(databaseMeta);
      }
    }
  }
  
  public java.util.List<DatabaseMeta> getDatabases() {
    return databases;
  }

  public void setDatabases(java.util.List<DatabaseMeta> databases) {
    this.databases = databases;
  }

}
