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
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.hadoop.HadoopSpoonPlugin;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputMeta;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
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
  
  private CTabFolder m_wTabFolder;
  private CTabItem m_wSourceTab;
  private CTabItem m_wVarsTab;
  
  /** various UI bits and pieces for the dialog */
  private Label m_stepnameLabel;
  private Text m_stepnameText;
  
  private Button m_sourceInFileBut;
  private Button m_sourceInFieldBut;
  
  private TextVar m_avroFilenameText;
  private Button m_avroFileBrowse;
  private TextVar m_schemaFilenameText;
  private Button m_schemaFileBrowse;
  
  private CCombo m_avroFieldNameText;
  
  private Button m_jsonEncodedBut;
  
  private Button m_getFields;
  private TableView m_fieldsView;
  
  
  private Button m_getLookupFieldsBut;
  private TableView m_lookupView;
  
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
    
    m_wTabFolder = new CTabFolder(shell, SWT.BORDER);
    props.setLook(m_wTabFolder, Props.WIDGET_STYLE_TAB);
    m_wTabFolder.setSimple(false);
    
    // start of the source tab
    m_wSourceTab = new CTabItem(m_wTabFolder, SWT.NONE);
    m_wSourceTab.setText(BaseMessages.getString(PKG, "AvroInputDialog.SourceTab.Title"));
    
    Composite wSourceComp = new Composite(m_wTabFolder, SWT.NONE);
    props.setLook(wSourceComp);
    
    FormLayout sourceLayout = new FormLayout();
    sourceLayout.marginWidth = 3;
    sourceLayout.marginHeight = 3;
    wSourceComp.setLayout(sourceLayout);
    
    
    // etched in group to hold first three widgets
    Group sourceGroup = new Group(wSourceComp, SWT.SHADOW_ETCHED_IN);
    FormLayout sourceGLayout = new FormLayout();
    sourceGLayout.marginWidth = 3;
    sourceGLayout.marginHeight = 3;
    sourceGroup.setLayout(sourceGLayout);
    props.setLook(sourceGroup);
    
    // source in file line
    Label fileSourceL = new Label(sourceGroup, SWT.RIGHT);
    props.setLook(fileSourceL);
    fileSourceL.setText(BaseMessages.getString(PKG, 
        "AvroInputDialog.FileSource.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    //fd.top = new FormAttachment(m_avroFilenameText, margin);
    fd.top = new FormAttachment(0, margin);
    fd.right = new FormAttachment(middle, -margin);
    fileSourceL.setLayoutData(fd);
    
    m_sourceInFileBut = new Button(sourceGroup, SWT.CHECK);
    props.setLook(m_sourceInFileBut);
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(0, margin);
    m_sourceInFileBut.setLayoutData(fd);
    
    m_sourceInFileBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        m_currentMeta.setChanged();
        checkWidgets();
      }
    });
    
    // source in field line
    Label fieldSourceL = new Label(sourceGroup, SWT.RIGHT);
    props.setLook(fieldSourceL);
    fieldSourceL.setText(BaseMessages.getString(PKG, 
        "AvroInputDialog.FieldSource.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    //fd.top = new FormAttachment(m_avroFilenameText, margin);
    fd.top = new FormAttachment(m_sourceInFileBut, margin);
    fd.right = new FormAttachment(middle, -margin);
    fieldSourceL.setLayoutData(fd);
    
    m_sourceInFieldBut = new Button(sourceGroup, SWT.CHECK);
    props.setLook(m_sourceInFieldBut);
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_sourceInFileBut, margin);
    m_sourceInFieldBut.setLayoutData(fd);
    
    m_sourceInFieldBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        m_currentMeta.setChanged();
        checkWidgets();
      }
    });
    
    
    
    // schema filename line
    Label schemaL = new Label(sourceGroup, SWT.RIGHT);
    props.setLook(schemaL);
    schemaL.setText(BaseMessages.getString(PKG, 
        "AvroInputDialog.SchemaFilename.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    //fd.top = new FormAttachment(m_avroFilenameText, margin);
    fd.top = new FormAttachment(m_sourceInFieldBut, margin);
    fd.right = new FormAttachment(middle, -margin);
    schemaL.setLayoutData(fd);
    schemaL.setToolTipText(BaseMessages.getString(PKG, "AvroInputDialog.SchemaFilename.TipText"));
    
    m_schemaFileBrowse = new Button(sourceGroup, SWT.PUSH | SWT.CENTER);
    props.setLook(m_schemaFileBrowse);
    m_schemaFileBrowse.setText(BaseMessages.getString(PKG, "AvroInputDialog.Button.FileBrowse"));
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(m_sourceInFieldBut, 0);
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
    
    m_schemaFilenameText = new TextVar(transMeta, sourceGroup, SWT.SIMPLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_schemaFilenameText);
    m_schemaFilenameText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_currentMeta.setChanged();
        m_avroFilenameText.setToolTipText(transMeta.environmentSubstitute(m_schemaFilenameText.getText()));
      }
    });
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_sourceInFieldBut, margin);
    fd.right = new FormAttachment(m_schemaFileBrowse, -margin);
    m_schemaFilenameText.setLayoutData(fd);
    
    
    FormData sourceGD = new FormData();
    sourceGD.top = new FormAttachment(0, margin);
    sourceGD.right = new FormAttachment(100, -margin);
    sourceGD.left = new FormAttachment(0, margin);
    sourceGroup.setLayoutData(sourceGD);
    
    
    
    // filename line
    Label filenameL = new Label(wSourceComp, SWT.RIGHT);
    props.setLook(filenameL);
    filenameL.setText(BaseMessages.getString(PKG, 
        "AvroInputDialog.Filename.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(sourceGroup, margin);
    fd.right = new FormAttachment(middle, -margin);
    filenameL.setLayoutData(fd);
    
    m_avroFileBrowse = new Button(wSourceComp, SWT.PUSH | SWT.CENTER);
    props.setLook(m_avroFileBrowse);
    m_avroFileBrowse.setText(BaseMessages.getString(PKG, "AvroInputDialog.Button.FileBrowse"));
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.top = new FormAttachment(sourceGroup, 0);
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
    
    m_avroFilenameText = new TextVar(transMeta, wSourceComp, SWT.SIMPLE | SWT.LEFT | SWT.BORDER);
    props.setLook(m_avroFilenameText);
    m_avroFilenameText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_currentMeta.setChanged();
        m_avroFilenameText.setToolTipText(transMeta.environmentSubstitute(m_avroFilenameText.getText()));
      }
    });
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(sourceGroup, margin);
    fd.right = new FormAttachment(m_avroFileBrowse, -margin);
    m_avroFilenameText.setLayoutData(fd);
    

    
    Label avroFieldNameL = new Label(wSourceComp, SWT.RIGHT);
    props.setLook(avroFieldNameL);
    avroFieldNameL.setText("Avro field to decode from");
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_avroFilenameText, margin);
    fd.right = new FormAttachment(middle, -margin);
    avroFieldNameL.setLayoutData(fd);
    
//    m_avroFieldNameText = new TextVar(transMeta, wSourceComp, SWT.SIMPLE | SWT.LEFT | SWT.BORDER);
    m_avroFieldNameText = new CCombo(wSourceComp, SWT.BORDER);
    props.setLook(m_avroFieldNameText);
    m_avroFieldNameText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        m_currentMeta.setChanged();
        m_avroFieldNameText.setToolTipText(transMeta.environmentSubstitute(m_avroFieldNameText.getText()));
      }
    });
    fd = new FormData();
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_avroFilenameText, margin);
    fd.right = new FormAttachment(100, 0);
    m_avroFieldNameText.setLayoutData(fd);
    populateFieldsCombo();
    
    // json encoded check box
    Label jsonL = new Label(wSourceComp, SWT.RIGHT);
    props.setLook(jsonL);
    jsonL.setText(BaseMessages.getString(PKG, 
        "AvroInputDialog.JsonEncoded.Label"));
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_avroFieldNameText, margin);
    fd.right = new FormAttachment(middle, -margin);
    jsonL.setLayoutData(fd);
    jsonL.setToolTipText(BaseMessages.getString(PKG, "AvroInputDialog.JsonEncoded.TipText"));
    
    m_jsonEncodedBut = new Button(wSourceComp, SWT.CHECK);
    props.setLook(m_jsonEncodedBut);
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_avroFieldNameText, margin);
    m_jsonEncodedBut.setLayoutData(fd);
    m_jsonEncodedBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        m_currentMeta.setChanged();
      }
    });
    
    // get fields button
    m_getFields = new Button(wSourceComp, SWT.PUSH);
    m_getFields.setText(BaseMessages.getString(PKG, "AvroInputDialog.Button.GetFields"));
    props.setLook(m_getFields);
    fd = new FormData();
    //fd.top = new FormAttachment(m_fieldsView, margin);
    fd.right = new FormAttachment(100, 0);
//    fd.bottom = new FormAttachment(wOK, -margin * 2);
    fd.bottom = new FormAttachment(100, 0);
    m_getFields.setLayoutData(fd);
    m_getFields.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        // populate table from schema
        getFields();
      }
    });
    
    wPreview = new Button(wSourceComp, SWT.PUSH | SWT.CENTER);
    wPreview.setText(BaseMessages.getString(PKG, "System.Button.Preview"));
    props.setLook(wPreview);
    fd = new FormData();
    //fd.top = new FormAttachment(m_fieldsView, margin);
    fd.right = new FormAttachment(m_getFields, margin);
    //fd.bottom = new FormAttachment(wOK, -margin * 2);
    fd.bottom = new FormAttachment(100, 0);
    wPreview.setLayoutData(fd);
    wPreview.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        preview();
      }
    });
           
    
    
    // fields stuff
    final ColumnInfo[] colinf = new ColumnInfo[] {
        new ColumnInfo(BaseMessages.getString(PKG, "AvroInputDialog.Fields.FIELD_NAME"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(BaseMessages.getString(PKG, "AvroInputDialog.Fields.FIELD_PATH"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(BaseMessages.getString(PKG, "AvroInputDialog.Fields.FIELD_TYPE"), ColumnInfo.COLUMN_TYPE_CCOMBO, false),
        new ColumnInfo(BaseMessages.getString(PKG, "AvroInputDialog.Fields.FIELD_INDEXED"), ColumnInfo.COLUMN_TYPE_TEXT, false),
    };
    
    colinf[2].setComboValues(ValueMeta.getTypes());
    
    m_fieldsView = new TableView(transMeta, wSourceComp,
        SWT.FULL_SELECTION | SWT.MULTI,
        colinf,
        1,
        lsMod,
        props);
    
    fd = new FormData();
    fd.top = new FormAttachment(m_jsonEncodedBut, margin * 2);
    fd.bottom = new FormAttachment(m_getFields, -margin * 2);
    fd.left = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    m_fieldsView.setLayoutData(fd);    
                
    
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    fd.bottom = new FormAttachment(100, 0);
    wSourceComp.setLayoutData(fd);    
    wSourceComp.layout();
    m_wSourceTab.setControl(wSourceComp);
    
    
    // -- start of the variables tab
    m_wVarsTab = new CTabItem(m_wTabFolder, SWT.NONE);
    m_wVarsTab.setText(BaseMessages.getString(PKG, "AvroInputDialog.VarsTab.Title"));
    Composite wVarsComp = new Composite(m_wTabFolder, SWT.NONE);
    props.setLook(wVarsComp);
    
    
    FormLayout varsLayout = new FormLayout();
    varsLayout.marginWidth = 3;
    varsLayout.marginHeight = 3;
    wVarsComp.setLayout(varsLayout);
    
    // lookup fields (variables) tab
    final ColumnInfo[] colinf2 = new ColumnInfo[] {
        new ColumnInfo(BaseMessages.getString(PKG, "AvroInputDialog.Fields.LOOKUP_NAME"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(BaseMessages.getString(PKG, "AvroInputDialog.Fields.LOOKUP_VARIABLE"), ColumnInfo.COLUMN_TYPE_TEXT, false),
        new ColumnInfo(BaseMessages.getString(PKG, "AvroInputDialog.Fields.LOOKUP_DEFAULT_VALUE"), ColumnInfo.COLUMN_TYPE_TEXT, false),
    };    
    
    // get lookup fields but
    m_getLookupFieldsBut = new Button(wVarsComp, SWT.PUSH | SWT.CENTER);
    props.setLook(m_getLookupFieldsBut);
    m_getLookupFieldsBut.setText(BaseMessages.getString(PKG, "AvroInputDialog.Button.GetLookupFields"));
    fd = new FormData();
    fd.right = new FormAttachment(100, 0);
    fd.bottom = new FormAttachment(100, -margin*2);
    //fd.top = new FormAttachment(0, 0);
    // fd.left = new FormAttachment(0, margin);
    m_getLookupFieldsBut.setLayoutData(fd);
    
    m_getLookupFieldsBut.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        // get incoming field names
        getIncomingFields();
      }
    });
    
    m_lookupView = new TableView(transMeta, wVarsComp,
        SWT.FULL_SELECTION | SWT.MULTI,
        colinf2,
        1,
        lsMod,
        props);
    fd = new FormData();
    fd.top   = new FormAttachment(0, margin*2);
    fd.bottom= new FormAttachment(m_getLookupFieldsBut, -margin*2);
    fd.left  = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    m_lookupView.setLayoutData(fd);
    
    
    
    
/*    Button tempBut = new Button(wVarsComp,  SWT.PUSH);
    tempBut.setText("Here is a button");
    props.setLook(tempBut);
    fd = new  FormData();
    fd.right = new FormAttachment(100, 0);
    fd.left = new FormAttachment(middle, 0);
    fd.top = new FormAttachment(m_lookupView, margin);
    tempBut.setLayoutData(fd); */
    
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(0, 0);
    fd.right = new FormAttachment(100, 0);
    fd.bottom = new FormAttachment(100, 0);
    wVarsComp.setLayoutData(fd);
    
    wVarsComp.layout();
    m_wVarsTab.setControl(wVarsComp);
    
    
    
    
    
    fd = new FormData();
    fd.left = new FormAttachment(0, 0);
    fd.top = new FormAttachment(m_stepnameText, margin);
    fd.right = new FormAttachment(100, 0);
    fd.bottom = new FormAttachment(100, -50);
    m_wTabFolder.setLayoutData(fd);
    
    
    
    // Buttons inherited from BaseStepDialog
    wOK = new Button(shell, SWT.PUSH);
    wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    
    wCancel=new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
            
    setButtonPositions(new Button[] { wOK, wCancel }, 
                       margin, m_wTabFolder);
    
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
    
    m_wTabFolder.setSelection(0);
    
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
    avroMeta.setAvroIsJsonEncoded(m_jsonEncodedBut.getSelection());
    avroMeta.setAvroInField(m_sourceInFieldBut.getSelection());
    avroMeta.setAvroFieldName(m_avroFieldNameText.getText());
    if (!Const.isEmpty(m_avroFieldNameText.getText())) {
      avroMeta.setAvroInField(true);
    } else {
      avroMeta.setAvroInField(false);
    }
    
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
    
    numNonEmpty = m_lookupView.nrNonEmpty();
    if (numNonEmpty > 0) {
      List<AvroInputMeta.LookupField> varFields = 
        new ArrayList<AvroInputMeta.LookupField>();
      
      for (int i = 0; i < numNonEmpty; i++) {
        TableItem item = m_lookupView.getNonEmpty(i);
        AvroInputMeta.LookupField newField = new AvroInputMeta.LookupField();
        boolean add = false;
        
        newField.m_fieldName = item.getText(1).trim();
        if (!Const.isEmpty(item.getText(2))) {
          newField.m_variableName = item.getText(2).trim();
          add = true;
          if (!Const.isEmpty(item.getText(3))) {
            newField.m_defaultValue = item.getText(3).trim();
          }
        }
        
        if (add) {
          varFields.add(newField);
        }
      }
      avroMeta.setLookupFields(varFields);
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
  
  protected void setVariableTableFields(List<AvroInputMeta.LookupField> fields) {
    m_lookupView.clearAll();
    
    for (AvroInputMeta.LookupField f : fields) {
      TableItem item = new TableItem(m_lookupView.table, SWT.NONE);
      
      if (!Const.isEmpty(f.m_fieldName)) {
        item.setText(1, f.m_fieldName);
      }
      
      if (!Const.isEmpty(f.m_variableName)) {
        item.setText(2, f.m_variableName);
      }
      
      if (!Const.isEmpty(f.m_defaultValue)) {
        item.setText(3, f.m_defaultValue);
      }
    }
    
    m_lookupView.removeEmptyRows();
    m_lookupView.setRowNums();
    m_lookupView.optWidth(true);
  }
  
  protected void getData() {
    if (!Const.isEmpty(m_currentMeta.getFilename())) {
      m_avroFilenameText.setText(m_currentMeta.getFilename());
    }
    
    if (!Const.isEmpty(m_currentMeta.getSchemaFilename())) {
      m_schemaFilenameText.setText(m_currentMeta.getSchemaFilename());
    }
    
    if (!Const.isEmpty(m_currentMeta.getAvroFieldName())) {
      m_avroFieldNameText.setText(m_currentMeta.getAvroFieldName());
    }
    
    m_jsonEncodedBut.setSelection(m_currentMeta.getAvroIsJsonEncoded());
    m_sourceInFieldBut.setSelection(m_currentMeta.getAvroInField());
    
    // fields
    if (m_currentMeta.getAvroFields() != null && 
        m_currentMeta.getAvroFields().size() > 0) {
      setTableFields(m_currentMeta.getAvroFields());
    }
    
    if (m_currentMeta.getLookupFields() != null &&
        m_currentMeta.getLookupFields().size() > 0) {
      setVariableTableFields(m_currentMeta.getLookupFields());
    }
    
    checkWidgets();
  }
  
  private void checkWidgets() {
    boolean sifile = m_sourceInFileBut.getSelection();
//    if (m_sourceInFileBut.getSelection()) {
      m_sourceInFieldBut.setEnabled(!sifile);
      
      if (sifile) {
        m_sourceInFieldBut.setSelection(!sifile);
      }
      m_avroFieldNameText.setEnabled(!sifile);
      
      m_avroFilenameText.setEnabled(sifile);
//    }
    
      boolean sifield = m_sourceInFieldBut.getSelection();
//    if (m_sourceInFieldBut.getSelection()) {
      if (sifield) {
        m_sourceInFileBut.setSelection(!sifield);
      }
      m_sourceInFileBut.setEnabled(!sifield);
      m_avroFilenameText.setEnabled(!sifield);
      
      m_avroFieldNameText.setEnabled(sifield);
  //  }
      
      wPreview.setEnabled(m_sourceInFileBut.getSelection());
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
  
  private void getIncomingFields() {
    try {
      RowMetaInterface r = transMeta.getPrevStepFields(stepname);
      if (r != null) {
        BaseStepDialog.getFieldsFromPrevious(r, m_lookupView, 1, new int[] {1}, null, -1, -1, null);
      }
    } catch (KettleException e) {
      new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Title"), 
          BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"), e);
    }
  }
  
  private void populateFieldsCombo() {
    StepMeta stepMeta = transMeta.findStep(stepname);
    
    if (stepMeta != null) {
      try {
        RowMetaInterface rowMeta = transMeta.getPrevStepFields(stepMeta);
        if (rowMeta != null && rowMeta.size() > 0) {
          m_avroFieldNameText.removeAll();
          for (int i = 0; i < rowMeta.size(); i++) {
            ValueMetaInterface vm = rowMeta.getValueMeta(i);
            String fieldName = vm.getName();
            m_avroFieldNameText.add(fieldName);
          }
        }
      } catch (KettleException ex) {
        // TODO
      }
    }    
  }
}
