/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.spoon.trans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.i18n.GlobalMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransAdapter;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowAdapter;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.XulSpoonSettingsManager;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegate;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulLoader;
import org.pentaho.ui.xul.containers.XulToolbar;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.SwtXulLoader;

public class TransPreviewDelegate extends SpoonDelegate implements XulEventHandler {
  private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by
                                             // Translator2!! $NON-NLS-1$

  private static final String XUL_FILE_TRANS_PREVIEW_TOOLBAR = "ui/trans-preview-toolbar.xul";

  private TransGraph transGraph;

  private CTabItem transPreviewTab;

  private XulToolbar toolbar;
  private Composite transPreviewComposite;
  
  protected Map<StepMeta, RowMetaInterface> previewMetaMap;
  protected Map<StepMeta, List<Object[]>> previewDataMap;
  protected Map<StepMeta, StringBuffer> previewLogMap;
  private Composite previewComposite;
  
  private Text logText;
  private TableView tableView; 
  
  private boolean active;
  
  private StepMeta selectedStep;
  
  /**
   * @param spoon
   * @param transGraph
   */
  public TransPreviewDelegate(Spoon spoon, TransGraph transGraph) {
    super(spoon);
    this.transGraph = transGraph;
    
    previewMetaMap = new HashMap<StepMeta, RowMetaInterface>();
    previewDataMap = new HashMap<StepMeta, List<Object[]>>();
    previewLogMap = new HashMap<StepMeta, StringBuffer>();
    
    active = true;
  }

  public void showPreviewView() {

    if (transPreviewTab == null || transPreviewTab.isDisposed()) {
      addTransPreview();
    } else {
      transPreviewTab.dispose();

      transGraph.checkEmptyExtraView();
    }
  }

  /**
   * Add a grid with the execution metrics per step in a table view
   * 
   */
  public void addTransPreview() {

    // First, see if we need to add the extra view...
    //
    if (transGraph.extraViewComposite == null || transGraph.extraViewComposite.isDisposed()) {
      transGraph.addExtraView();
    } else {
      if (transPreviewTab != null && !transPreviewTab.isDisposed()) {
        // just set this one active and get out...
        //
        transGraph.extraViewTabFolder.setSelection(transPreviewTab);
        return;
      }
    }

    transPreviewTab = new CTabItem(transGraph.extraViewTabFolder, SWT.NONE);
    transPreviewTab.setImage(GUIResource.getInstance().getImageTable());
    transPreviewTab.setText(BaseMessages.getString(PKG, "Spoon.TransGraph.PreviewTab.Name"));

    transPreviewComposite = new Composite(transGraph.extraViewTabFolder, SWT.NONE);
    transPreviewComposite.setLayout(new FormLayout());

    addToolBar();

    Control toolbarControl = (Control) toolbar.getManagedObject();

    toolbarControl.setLayoutData(new FormData());
    FormData fd = new FormData();
    fd.left = new FormAttachment(0, 0); // First one in the left top corner
    fd.top = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    toolbarControl.setLayoutData(fd);

    toolbarControl.setParent(transPreviewComposite);

    previewComposite = new Composite(transPreviewComposite, SWT.NONE);
    previewComposite.setLayout(new FillLayout());
    FormData fdPreview = new FormData();
    fdPreview.left = new FormAttachment(0, 0);
    fdPreview.right = new FormAttachment(100, 0);
    fdPreview.top = new FormAttachment((Control)toolbar.getManagedObject(), 0);
    fdPreview.bottom = new FormAttachment(100, 0);
    previewComposite.setLayoutData(fdPreview);
    
    
    transPreviewTab.setControl(transPreviewComposite);

    transGraph.extraViewTabFolder.setSelection(transPreviewTab);
  }

  private void addToolBar() {

    try {
      XulLoader loader = new SwtXulLoader();
      loader.setSettingsManager(XulSpoonSettingsManager.getInstance());
      ResourceBundle bundle = GlobalMessages.getBundle("org/pentaho/di/ui/spoon/messages/messages");
      XulDomContainer xulDomContainer = loader.loadXul(XUL_FILE_TRANS_PREVIEW_TOOLBAR, bundle);
      xulDomContainer.addEventHandler(this);
      toolbar = (XulToolbar) xulDomContainer.getDocumentRoot().getElementById("nav-toolbar");

      ToolBar swtToolBar = (ToolBar) toolbar.getManagedObject();
      swtToolBar.layout(true, true);
    } catch (Throwable t) {
      log.logError(toString(), Const.getStackTracker(t));
      new ErrorDialog(transPreviewComposite.getShell(), BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingXULFile.Title"),
          BaseMessages.getString(PKG, "Spoon.Exception.ErrorReadingXULFile.Message", XUL_FILE_TRANS_PREVIEW_TOOLBAR), new Exception(t));
    }
  }

  /**
   * This refresh is driven by outside influenced using listeners and so on.
   */
  public synchronized void refreshView() {
    
    if (previewComposite==null || previewComposite.isDisposed()) {
      return;
    }
    
    // Which step do we preview...
    //
    StepMeta stepMeta = selectedStep; // copy to prevent race conditions and so on.
    if (stepMeta==null) {
      return;
    }
    
    // Do we have a log for this selected step?
    // This means the preview work is still running or it error-ed out.
    //
    StringBuffer logText = previewLogMap.get(stepMeta);
    if (logText!=null && logText.length()>0) {
      showLogText(stepMeta, logText.toString());
      return;
    } 
    
    // If the preview work is done we have row meta-data and data for each step.
    //
    RowMetaInterface rowMeta = previewMetaMap.get(stepMeta);
    if (rowMeta!=null) {
      List<Object[]> rowData = previewDataMap.get(stepMeta);
      
      try {
        showPreviewGrid(transGraph.getManagedObject(), stepMeta, rowMeta, rowData);
      } catch(Exception e) {
        logText.append( Const.getStackTracker(e) );
        showLogText(stepMeta, logText.toString());
      }
    }
  }

  protected void showPreviewGrid(TransMeta transMeta, StepMeta stepMeta, RowMetaInterface rowMeta, List<Object[]> rowsData) throws KettleException {
    clearPreviewComposite();
    
    ColumnInfo[] columnInfo = new ColumnInfo[rowMeta.size()];
    for (int i=0;i<columnInfo.length;i++) {
      ValueMetaInterface valueMeta = rowMeta.getValueMeta(i);
      columnInfo[i] = new ColumnInfo(valueMeta.getName(), ColumnInfo.COLUMN_TYPE_TEXT, false, true);
      columnInfo[i].setValueMeta(valueMeta);
    }
    
    tableView = new TableView(transMeta, previewComposite, SWT.NONE, columnInfo, rowsData.size(), null, PropsUI.getInstance());
    
    // Put data on it...
    //
    for (int rowNr=0;rowNr<rowsData.size();rowNr++) {
      Object[] rowData = rowsData.get(rowNr);
      TableItem item;
      if (rowNr<tableView.table.getItemCount()) {
        item = tableView.table.getItem(rowNr);
      } else {
        item = new TableItem(tableView.table, SWT.NONE);
      }
      for (int colNr=0;colNr<rowMeta.size();colNr++) {
        String string;
        try {
          string = rowMeta.getString(rowData, colNr);
        } catch(Exception e) {
          string = "Conversion error: "+e.getMessage(); 
        }
        if (string == null) {
          item.setText(colNr+1, "<null>");
          item.setForeground(colNr+1, GUIResource.getInstance().getColorBlue());
        } else {
          item.setText(colNr+1, string);
        }
        
      }
    }
    tableView.setRowNums();
    tableView.setShowingConversionErrorsInline(true);
    tableView.optWidth(true);
    
    previewComposite.layout(true, true);
  }

  protected void showLogText(StepMeta stepMeta, String loggingText) {
    clearPreviewComposite();
    
    logText = new Text(previewComposite, SWT.MULTI);
    logText.setText(loggingText);
    
    previewComposite.layout(true, true);    
  }

  private void clearPreviewComposite() {
    // First clear out the preview composite, then put in a text field showing the log text
    //
    //
    for (Control control : previewComposite.getChildren()) {
      control.dispose();
    }
  }

  public CTabItem getTransGridTab() {
    return transPreviewTab;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getData()
   */
  public Object getData() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getName()
   */
  public String getName() {
    return "transgrid";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.ui.xul.impl.XulEventHandler#getXulDomContainer()
   */
  public XulDomContainer getXulDomContainer() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setData(java.lang.Object)
   */
  public void setData(Object data) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.ui.xul.impl.XulEventHandler#setName(java.lang.String)
   */
  public void setName(String name) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.pentaho.ui.xul.impl.XulEventHandler#setXulDomContainer(org.pentaho.
   * ui.xul.XulDomContainer)
   */
  public void setXulDomContainer(XulDomContainer xulDomContainer) {
    // TODO Auto-generated method stub

  }

  /**
   * @return the active
   */
  public boolean isActive() {
    return active;
  }

  /**
   * @param active the active to set
   */
  public void setActive(boolean active) {
    this.active = active;
  }

  public void capturePreviewData(final Trans trans, List<StepMeta> stepMetas) {
    final StringBuffer loggingText = new StringBuffer();
    try {
      final TransMeta transMeta = trans.getTransMeta();
    
      for (StepMeta stepMeta : stepMetas) {
        
        final RowMetaInterface rowMeta = transMeta.getStepFields(stepMeta).clone();
        previewMetaMap.put(stepMeta, rowMeta);
        final List<Object[]> rowsData = new ArrayList<Object[]>();
        previewDataMap.put(stepMeta, rowsData);
        previewLogMap.put(stepMeta, loggingText);
        
        StepInterface step = trans.findRunThread(stepMeta.getName());
        
        if (step!=null) {
          step.addRowListener(new RowAdapter() {
            
            @Override
            public void rowWrittenEvent(RowMetaInterface rowMeta, Object[] row) throws KettleStepException {
              if (rowsData.size()<PropsUI.getInstance().getDefaultPreviewSize()) {
                try {
                  rowsData.add(rowMeta.cloneRow(row));
                } catch(Exception e) {
                  throw new KettleStepException("Unable to clone row for metadata : "+rowMeta, e);
                }
              }
            }
          });
        }
      }
    } catch(Exception e) {
      loggingText.append(Const.getStackTracker(e));
    }

    // In case there were errors during preview...
    //
    trans.addTransListener(new TransAdapter() { @Override
      public void transFinished(Trans trans) throws KettleException {
        // Copy over the data from the previewDelegate...
        //
        if (trans.getErrors()!=0) {
          // capture logging and store it...
          //
          for (StepMetaDataCombi combi : trans.getSteps()) {
            if (combi.copy==0) {
              StringBuffer logBuffer = CentralLogStore.getAppender().getBuffer(combi.step.getLogChannel().getLogChannelId(), false);
              previewLogMap.put(combi.stepMeta, logBuffer);
            }
          }
        }
      };
    });
  }

  public void addPreviewData(StepMeta stepMeta, RowMetaInterface rowMeta, List<Object[]> rowsData, StringBuffer buffer) {
    previewLogMap.put(stepMeta, buffer);
    previewMetaMap.put(stepMeta, rowMeta);
    previewDataMap.put(stepMeta, rowsData);
  }

  /**
   * @return the selectedStep
   */
  public StepMeta getSelectedStep() {
    return selectedStep;
  }

  /**
   * @param selectedStep the selectedStep to set
   */
  public void setSelectedStep(StepMeta selectedStep) {
    this.selectedStep = selectedStep;
  }
}
