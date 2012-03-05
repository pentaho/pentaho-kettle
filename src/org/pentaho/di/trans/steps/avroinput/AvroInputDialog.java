/*******************************************************************************
 *
 * Pentaho Big Data
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

package org.pentaho.di.trans.steps.avroinput;

import java.util.ArrayList;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.commons.vfs.FileObject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.hadoop.HadoopSpoonPlugin;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.vfs.ui.VfsFileChooserDialog;

/**
 * Dialog for the Avro input step.
 * 
 * @author Mark Hall (mhall{[at]}pentaho{[dot]}com)
 * @version $Revision$
 */
public class AvroInputDialog extends BaseStepDialog implements
    StepDialogInterface {
  
  private static final Class<?> PKG = AvroInputMeta.class;
  
  private AvroInputMeta m_currentMeta;
  private AvroInputMeta m_originalMeta;
  
  /** various UI bits and pieces for the dialog */
  private Label m_stepnameLabel;
  private Text m_stepnameText;
  
  private TextVar m_avroFilenameText;
  private Button m_avroFileBrowse;
  private TextVar m_schemaFilenameText;
  private Button m_schemaFileBrowse;
  
  private Button m_getFields;
  private TableView m_fieldsView;
  
  public AvroInputDialog(Shell parent, Object in,
      TransMeta tr, String name) {
    
    super(parent, (BaseStepMeta)in, tr, name);
    m_currentMeta = (AvroInputMeta)in;
    m_originalMeta = (AvroInputMeta)m_currentMeta.clone();
  }

  public String open() {    
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = 
      new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);

    props.setLook(shell);
    setShellImage(shell, m_currentMeta);

    // used to listen to a text field (m_wStepname)
    ModifyListener lsMod = new ModifyListener() {
        public void modifyText(ModifyEvent e) {
          m_currentMeta.setChanged();
        }
      };

    changed = m_currentMeta.hasChanged();

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "AvroInputDialog.Shell.Title"));

    int middle = props.getMiddlePct();
    int margin = Const.MARGIN;
    
    // Stepname line
    m_stepnameLabel = new Label(shell, SWT.RIGHT);
    m_stepnameLabel.
      setText(BaseMessages.getString(PKG, "AvroInputDialog.StepName.Label"));
    props.setLook(m_stepnameLabel);

    FormData fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(middle, -margin);
    fd.top = new FormAttachment(0, margin);
    m_stepnameLabel.setLayoutData(fd);
    m_stepnameText = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    m_stepnameText.setText(stepname);
    props.setLook(m_stepnameText);
    m_stepnameText.addModifyListener(lsMod);
    
    // format the text field
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(0, margin);
    fd.right = new FormAttachment(100, 0);
    m_stepnameText.setLayoutData(fd);
    
    // filename line
    Label filenameL = new Label(shell, SWT.RIGHT);
    props.setLook(filenameL);
    filenameL.setText(BaseMessages.getString(PKG, 
        "AvroInputDialog.Filename.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_stepnameText, margin);
    fd.right = new FormAttachment(middle, -margin);
    filenameL.setLayoutData(fd);
    
    m_avroFileBrowse = new Button(shell, SWT.PUSH | SWT.CENTER);
    props.setLook(m_avroFileBrowse);
    m_avroFileBrowse.setText(BaseMessages.getString(PKG, "AvroInputDialog.Button.FileBrowse"));
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_stepnameText, margin);
    m_avroFileBrowse.setLayoutData(fd);
    
    // add listener to pop up VFS browse dialog
    m_avroFileBrowse.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        try {
          String[] fileFilters = new String[] {"*"};
          String[] fileFilterNames = new String[] {BaseMessages.getString(TextFileInputMeta.class, "System.FileType.AllFiles")};
          
          // get current file
          FileObject rootFile = null;
          FileObject initialFile = null;
          FileObject defaultInitialFile = null;
          
          if (m_avroFilenameText.getText() != null) {
            String fname = transMeta.environmentSubstitute(m_avroFilenameText.getText());
            
            if (!Const.isEmpty(fname)) {
              initialFile = KettleVFS.getFileObject(fname);
              rootFile = initialFile.getFileSystem().getRoot();
            } else {
              defaultInitialFile = KettleVFS.getFileObject(Spoon.getInstance().getLastFileOpened());
            }
          } else {
            defaultInitialFile = KettleVFS.getFileObject("file:///c:/");
          }
          
          if (rootFile == null) {
            rootFile = defaultInitialFile.getFileSystem().getRoot();
          }
          
          VfsFileChooserDialog fileChooserDialog = 
            Spoon.getInstance().getVfsFileChooserDialog(rootFile, initialFile);
          fileChooserDialog.defaultInitialFile = defaultInitialFile;
          FileObject selectedFile = fileChooserDialog.open(shell, null, 
              HadoopSpoonPlugin.HDFS_SCHEME,true, null, fileFilters, fileFilterNames,
              VfsFileChooserDialog.VFS_DIALOG_OPEN_FILE);
          
          if (selectedFile != null) {
            m_avroFilenameText.setText(selectedFile.getURL().toString());
          }
        } catch (Exception ex) {
          log.logError(BaseMessages.getString(PKG, "AvroInputDialog.Error.KettleFileException"), ex);
          MessageDialog.openError(shell, stepname, BaseMessages.getString(PKG, 
              "AvroInputDialog.Error.KettleFileException"));
        }
      }
    });
    
    m_avroFilenameText = new TextVar(transMeta, shell, SWT.SIMPLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_avroFilenameText);
    m_avroFilenameText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_currentMeta.setChanged();
        m_avroFilenameText.setToolTipText(transMeta.environmentSubstitute(m_avroFilenameText.getText()));
      }
    });
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_stepnameText, margin);
    fd.right = new FormAttachment(m_avroFileBrowse, -margin);
    m_avroFilenameText.setLayoutData(fd);
    
    // schema filename line
    Label schemaL = new Label(shell, SWT.RIGHT);
    props.setLook(schemaL);
    schemaL.setText(BaseMessages.getString(PKG, 
        "AvroInputDialog.SchemaFilename.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_avroFilenameText, margin);
    fd.right = new FormAttachment(middle, -margin);
    schemaL.setLayoutData(fd);
    schemaL.setToolTipText(BaseMessages.getString(PKG, "AvroInputDialog.SchemaFilename.TipText"));
    
    m_schemaFileBrowse = new Button(shell, SWT.PUSH | SWT.CENTER);
    props.setLook(m_schemaFileBrowse);
    m_schemaFileBrowse.setText(BaseMessages.getString(PKG, "AvroInputDialog.Button.FileBrowse"));
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_avroFilenameText, margin);
    m_schemaFileBrowse.setLayoutData(fd);
    
    // add listener to pop up VFS browse dialog
    m_schemaFileBrowse.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        try {
          String[] fileFilters = new String[] {"*"};
          String[] fileFilterNames = new String[] {BaseMessages.getString(TextFileInputMeta.class, "System.FileType.AllFiles")};
          
          // get current file
          FileObject rootFile = null;
          FileObject initialFile = null;
          FileObject defaultInitialFile = null;
          
          if (m_schemaFilenameText.getText() != null) {
            String fname = transMeta.environmentSubstitute(m_schemaFilenameText.getText());
            
            if (!Const.isEmpty(fname)) {
              initialFile = KettleVFS.getFileObject(fname);
              rootFile = initialFile.getFileSystem().getRoot();
            } else {
              defaultInitialFile = KettleVFS.getFileObject(Spoon.getInstance().getLastFileOpened());
            }
          } else {
            defaultInitialFile = KettleVFS.getFileObject("file:///c:/");
          }
          
          if (rootFile == null) {
            rootFile = defaultInitialFile.getFileSystem().getRoot();
          }
          
          VfsFileChooserDialog fileChooserDialog = 
            Spoon.getInstance().getVfsFileChooserDialog(rootFile, initialFile);
          fileChooserDialog.defaultInitialFile = defaultInitialFile;
          FileObject selectedFile = fileChooserDialog.open(shell, null, 
              HadoopSpoonPlugin.HDFS_SCHEME,true, null, fileFilters, fileFilterNames,
              VfsFileChooserDialog.VFS_DIALOG_OPEN_FILE);
          
          if (selectedFile != null) {
            m_schemaFilenameText.setText(selectedFile.getURL().toString());
          }
        } catch (Exception ex) {
          log.logError(BaseMessages.getString(PKG, "AvroInputDialog.Error.KettleFileException"), ex);
          MessageDialog.openError(shell, stepname, BaseMessages.getString(PKG, 
            "AvroInputDialog.Error.KettleFileException"));
        }
      }
    });
    
    m_schemaFilenameText = new TextVar(transMeta, shell, SWT.SIMPLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_schemaFilenameText);
    m_schemaFilenameText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_currentMeta.setChanged();
        m_avroFilenameText.setToolTipText(transMeta.environmentSubstitute(m_schemaFilenameText.getText()));
      }
    });
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_avroFilenameText, margin);
    fd.right = new FormAttachment(m_schemaFileBrowse, -margin);
    m_schemaFilenameText.setLayoutData(fd);
    
    // Buttons inherited from BaseStepDialog
    wOK = new Button(shell, SWT.PUSH);
    wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    
    wCancel=new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
            
    setButtonPositions(new Button[] { wOK, wCancel }, 
                       margin, m_fieldsView);
    
    
    // get fields button
    m_getFields = new Button(shell, SWT.PUSH);
    m_getFields.setText(BaseMessages.getString(PKG, "AvroInputDialog.Button.GetFields"));
    props.setLook(m_getFields);
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.bottom = new FormAttachment(wOK, -margin * 2);
    m_getFields.setLayoutData(fd);
    m_getFields.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        // populate table from schema
        getFields();
      }
    });
    
    wPreview = new Button(shell, SWT.PUSH);
    wPreview.setText(BaseMessages.getString(PKG, "System.Button.Preview"));
    props.setLook(wPreview);
    fd = new FormData();
    fd.right = new FormAttachment(m_getFields, margin);
    fd.bottom = new FormAttachment(wOK, -margin * 2);
    wPreview.setLayoutData(fd);
    wPreview.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        preview();
      }
    });
    
    // fields stuff
    ColumnInfo[] colinf = new ColumnInfo[] {
        new ColumnInfo(BaseMessages.getString(PKG, "AvroInputDialog.Fields.FIELD_NAME"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(BaseMessages.getString(PKG, "AvroInputDialog.Fields.FIELD_PATH"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(BaseMessages.getString(PKG, "AvroInputDialog.Fields.FIELD_TYPE"), ColumnInfo.COLUMN_TYPE_CCOMBO, false),
        new ColumnInfo(BaseMessages.getString(PKG, "AvroInputDialog.Fields.FIELD_INDEXED"), ColumnInfo.COLUMN_TYPE_TEXT, false),
    };
    
    colinf[2].setComboValues(ValueMeta.getTypes());
    
    m_fieldsView = new TableView(transMeta, shell,
        SWT.FULL_SELECTION | SWT.MULTI,
        colinf,
        1,
        lsMod,
        props);
    
    fd = new FormData();
    fd.top = new FormAttachment(m_schemaFilenameText, margin * 2);
    fd.bottom = new FormAttachment(m_getFields, -margin * 2);
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    m_fieldsView.setLayoutData(fd);
    
    
    // Add listeners
    lsCancel = new Listener() {
      public void handleEvent(Event e) {
        cancel();
      }
    };

    lsOK = new Listener() {
      public void handleEvent(Event e) {
        ok();
      }
    };

    wCancel.addListener(SWT.Selection, lsCancel);
    wOK.addListener(SWT.Selection, lsOK);

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected(SelectionEvent e) {
        ok();
      }
    };
    
    m_stepnameText.addSelectionListener(lsDef);
    
    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener(new ShellAdapter() {
        public void shellClosed(ShellEvent e) {
          cancel();
        }
      });
    
    setSize();
    
    getData();
    
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    
    return stepname;    
  }
  
  protected void cancel() {
    stepname = null;
    m_currentMeta.setChanged(changed);
    
    dispose();
  }
  
  protected void ok() {
    if (Const.isEmpty(m_stepnameText.getText())) {
      return;
    }
    
    stepname = m_stepnameText.getText();
    
    setMeta(m_currentMeta);    
    
    if (!m_originalMeta.equals(m_currentMeta)) {
      m_currentMeta.setChanged();
      changed = m_currentMeta.hasChanged();
    }
    
    dispose();
  }
  
  protected void setMeta(AvroInputMeta avroMeta) {
    avroMeta.setFilename(m_avroFilenameText.getText());
    avroMeta.setSchemaFilename(m_schemaFilenameText.getText());
    
    int numNonEmpty = m_fieldsView.nrNonEmpty();
    if (numNonEmpty > 0) {
      List<AvroInputMeta.AvroField> outputFields = 
        new ArrayList<AvroInputMeta.AvroField>();
      
      for (int i = 0; i < numNonEmpty; i++) {
        TableItem item = m_fieldsView.getNonEmpty(i);
        AvroInputMeta.AvroField newField = new AvroInputMeta.AvroField();
        newField.m_fieldName = item.getText(1).trim();
        newField.m_fieldPath = item.getText(2).trim();
        newField.m_kettleType = item.getText(3).trim();
        
        if (!Const.isEmpty(item.getText(4))) {
          newField.m_indexedVals = AvroInputMeta.indexedValsList(item.getText(4).trim());
        }
        
        outputFields.add(newField);
      }
      avroMeta.setAvroFields(outputFields);
    }
  }
  
  protected void getFields() {
    if (!Const.isEmpty(m_schemaFilenameText.getText())) {
      // this schema overrides any that might be in a container file
      String sName = m_schemaFilenameText.getText();
      sName = transMeta.environmentSubstitute(sName);
      try {
        Schema s = AvroInputData.loadSchema(sName);
        List<AvroInputMeta.AvroField> schemaFields = 
          AvroInputData.getLeafFields(s);
        
        setTableFields(schemaFields);
        
      } catch (Exception ex) {        
        log.logError(BaseMessages.getString(PKG, "AvroInputDialog.Error.KettleFileException" 
            + " " + sName), ex);
        MessageDialog.openError(shell, stepname, BaseMessages.getString(PKG, 
            "AvroInputDialog.Error.KettleFileException" + " " + sName));
      }
    } else {
      String avroFileName = m_avroFilenameText.getText();
      avroFileName = transMeta.environmentSubstitute(avroFileName);
      try {
        Schema s = AvroInputData.loadSchemaFromContainer(avroFileName);
        List<AvroInputMeta.AvroField> schemaFields = 
          AvroInputData.getLeafFields(s);
        
        setTableFields(schemaFields);
      } catch (Exception ex) {
        log.logError(BaseMessages.getString(PKG, "AvroInput.Error.UnableToLoadSchemaFromContainerFile"), ex);
        MessageDialog.openError(shell, stepname, BaseMessages.getString(PKG, 
            "AvroInput.Error.UnableToLoadSchemaFromContainerFile", avroFileName));
      }
    }
  }
  
  protected void setTableFields(List<AvroInputMeta.AvroField> fields) {
    m_fieldsView.clearAll();
    for (AvroInputMeta.AvroField f : fields) {
      TableItem item = new TableItem(m_fieldsView.table, SWT.NONE);
      
      if (!Const.isEmpty(f.m_fieldName)) {
        item.setText(1, f.m_fieldName);
      }
      
      if (!Const.isEmpty(f.m_fieldPath)) {
        item.setText(2, f.m_fieldPath);
      }
      
      if (!Const.isEmpty(f.m_kettleType)) {
        item.setText(3, f.m_kettleType);
      }
      
      if (f.m_indexedVals != null && f.m_indexedVals.size() > 0) {
        item.setText(4, AvroInputMeta.indexedValsList(f.m_indexedVals));
      }
    }
    
    m_fieldsView.removeEmptyRows();
    m_fieldsView.setRowNums();
    m_fieldsView.optWidth(true);
  }
  
  protected void getData() {
    if (!Const.isEmpty(m_currentMeta.getFilename())) {
      m_avroFilenameText.setText(m_currentMeta.getFilename());
    }
    
    if (!Const.isEmpty(m_currentMeta.getSchemaFilename())) {
      m_schemaFilenameText.setText(m_currentMeta.getSchemaFilename());
    }
    
    // fields
    if (m_currentMeta.getAvroFields() != null && 
        m_currentMeta.getAvroFields().size() > 0) {
      setTableFields(m_currentMeta.getAvroFields());
    }
  }
  
  private void preview() {
    AvroInputMeta tempMeta = new AvroInputMeta();
    setMeta(tempMeta);

    TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, tempMeta, m_stepnameText.getText());
    transMeta.getVariable("Internal.Transformation.Filename.Directory");
    previewMeta.getVariable("Internal.Transformation.Filename.Directory");

    EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(), BaseMessages.getString(PKG, "CsvInputDialog.PreviewSize.DialogTitle"), BaseMessages.getString(PKG, "AvroInputDialog.PreviewSize.DialogMessage"));
    int previewSize = numberDialog.open();

    if (previewSize > 0) {
      TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { m_stepnameText.getText() }, new int[] { previewSize } );
      progressDialog.open();

      Trans trans = progressDialog.getTrans();
      String loggingText = progressDialog.getLoggingText();

      if (!progressDialog.isCancelled()) {
        if (trans.getResult()!=null && trans.getResult().getNrErrors()>0) {
          EnterTextDialog etd = new EnterTextDialog(shell, BaseMessages.getString(PKG, "System.Dialog.PreviewError.Title"),  
              BaseMessages.getString(PKG, "System.Dialog.PreviewError.Message"), loggingText, true );
          etd.setReadOnly();
          etd.open();
        }
      }

      PreviewRowsDialog prd = new PreviewRowsDialog(shell, transMeta, SWT.NONE, m_stepnameText.getText(), 
          progressDialog.getPreviewRowsMeta(m_stepnameText.getText()), 
          progressDialog.getPreviewRows(m_stepnameText.getText()), loggingText);
      prd.open();
    }
  }
}
