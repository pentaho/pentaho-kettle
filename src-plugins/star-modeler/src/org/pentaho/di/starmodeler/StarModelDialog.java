/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.starmodeler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.gui.GCInterface;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.spoon.SWTDirectGC;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalRelationship;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.concept.types.TableType;
import org.pentaho.pms.schema.concept.DefaultPropertyID;

/**
 * Allows you to edit the Job settings. Just pass a JobInfo object.
 * 
 * @author Matt Casters
 * @since 02-jul-2003
 */
public class StarModelDialog extends Dialog {
  private static Class<?>  PKG = StarModelDialog.class; // translator

  private CTabFolder       wTabFolder;
  
  private CTabItem         wModelTab, wDimensionsTab; // , wRelationshipsTab;

  private PropsUI          props;

  private Text             wModelName;

  private Text             wModelDescription;

  private Button           wOK, wCancel;

  private LogicalModel     logicalModel;
  private Shell            shell;

  private SelectionAdapter lsDef;

  // fields tab
  private TableView        wTablesList;

  private int              middle;
  private int              margin;

  private String           locale;

  private Canvas canvas;

  private CTabItem wFactTab;

  private Text wFactTableName;

  private Text wFactTableDescription;

  private Text wPhysicalFactName;

  private LogicalTable factTable;

  private TableView wFactAttributes;

  private ColumnInfo[] factColumns;

  private List<LogicalRelationship> logicalRelationships;

  public StarModelDialog(Shell parent, LogicalModel logicalModel, String locale) {
    super(parent, SWT.DIALOG_TRIM);
    this.logicalModel = logicalModel;
    this.props = PropsUI.getInstance();
    this.locale = locale;
    
    List<LogicalTable> factTables = ConceptUtil.findLogicalTables(logicalModel, TableType.FACT);
    if (factTables.isEmpty()) {
      this.factTable = new LogicalTable();
      this.factTable.setId(UUID.randomUUID().toString());
      this.factTable.setProperty(DefaultPropertyID.TABLE_TYPE.getId(), TableType.FACT);
      logicalModel.addLogicalTable(this.factTable);
    } else {
      this.factTable = factTables.get(0);
    }    
  }

  public LogicalModel open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
    props.setLook(shell);
    shell.setImage((Image) GUIResource.getInstance().getImageLogoSmall());

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout(formLayout);
    shell.setText(BaseMessages.getString(PKG, "StarModelDialog.ShellText"));

    middle = props.getMiddlePct();
    margin = Const.MARGIN;

    wTabFolder = new CTabFolder(shell, SWT.BORDER);
    props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);

    addModelTab();
    addDimensionsTab();
    addFactTab();

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment(0, 0);
    fdTabFolder.top = new FormAttachment(0, 0);
    fdTabFolder.right = new FormAttachment(100, 0);
    fdTabFolder.bottom = new FormAttachment(100, -50);
    wTabFolder.setLayoutData(fdTabFolder);

    // THE BUTTONS
    wOK = new Button(shell, SWT.PUSH);
    wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
    wCancel = new Button(shell, SWT.PUSH);
    wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

    BaseStepDialog.positionBottomButtons(shell, new Button[] { wOK, wCancel }, Const.MARGIN, null);
    wOK.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        ok();
      }
    });
    wCancel.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event e) {
        cancel();
      }
    });

    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected(SelectionEvent e) {
        ok();
      }
    };

    wModelName.addSelectionListener(lsDef);
    wModelDescription.addSelectionListener(lsDef);

    // Detect X or ALT-F4 or something that kills this window...
    shell.addShellListener(new ShellAdapter() {
      public void shellClosed(ShellEvent e) {
        cancel();
      }
    });

    wTabFolder.setSelection(0);
    
    wTabFolder.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        getRelationshipsFromFact();
        canvas.redraw();
      }
    });
    
    getData();
    BaseStepDialog.setSize(shell);

    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    return logicalModel;
  }

  private void addModelTab() {
    wModelTab = new CTabItem(wTabFolder, SWT.NONE);
    wModelTab.setText(BaseMessages.getString(PKG, "StarModelDialog.ModelTab.Label")); //$NON-NLS-1$

    Composite wModelComp = new Composite(wTabFolder, SWT.NONE);
    props.setLook(wModelComp);

    FormLayout transLayout = new FormLayout();
    transLayout.marginWidth = Const.MARGIN;
    transLayout.marginHeight = Const.MARGIN;
    wModelComp.setLayout(transLayout);

    // Model name:
    //
    Label wlModelName = new Label(wModelComp, SWT.RIGHT);
    wlModelName.setText(BaseMessages.getString(PKG, "StarModelDialog.ModelName.Label"));
    props.setLook(wlModelName);
    FormData fdlJobname = new FormData();
    fdlJobname.left = new FormAttachment(0, 0);
    fdlJobname.right = new FormAttachment(middle, -margin);
    fdlJobname.top = new FormAttachment(0, margin);
    wlModelName.setLayoutData(fdlJobname);
    wModelName = new Text(wModelComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wModelName);
    FormData fdJobname = new FormData();
    fdJobname.left = new FormAttachment(middle, 0);
    fdJobname.top = new FormAttachment(0, margin);
    fdJobname.right = new FormAttachment(100, 0);
    wModelName.setLayoutData(fdJobname);
    Control lastControl = wModelName;

    // Model description
    //
    Label wlModelDescription = new Label(wModelComp, SWT.RIGHT);
    wlModelDescription.setText(BaseMessages.getString(PKG, "StarModelDialog.ModelDescription.Label"));
    props.setLook(wlModelDescription);
    FormData fdlJobFilename = new FormData();
    fdlJobFilename.left = new FormAttachment(0, 0);
    fdlJobFilename.right = new FormAttachment(middle, -margin);
    fdlJobFilename.top = new FormAttachment(lastControl, margin);
    wlModelDescription.setLayoutData(fdlJobFilename);
    wModelDescription = new Text(wModelComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
    props.setLook(wModelDescription);
    FormData fdJobFilename = new FormData();
    fdJobFilename.left = new FormAttachment(middle, 0);
    fdJobFilename.top = new FormAttachment(lastControl, margin);
    fdJobFilename.right = new FormAttachment(100, 0);
    wModelDescription.setLayoutData(fdJobFilename);
    lastControl = wModelDescription;
    
    canvas = new Canvas(wModelComp, SWT.BORDER);
    FormData fdCanvas = new FormData();
    fdCanvas.left = new FormAttachment(0,0);
    fdCanvas.right = new FormAttachment(100, 0);
    fdCanvas.top = new FormAttachment(lastControl, 3*margin);
    fdCanvas.bottom = new FormAttachment(100, -margin);
    canvas.setLayoutData(fdCanvas);
    canvas.addPaintListener(new PaintListener() {
      @Override
      public void paintControl(PaintEvent paintEvent) {
        drawLogicalModel(logicalModel, canvas, paintEvent);
      }
    });
    

    FormData fdModelComp = new FormData();
    fdModelComp.left = new FormAttachment(0, 0);
    fdModelComp.top = new FormAttachment(0, 0);
    fdModelComp.right = new FormAttachment(100, 0);
    fdModelComp.bottom = new FormAttachment(100, 0);

    wModelComp.setLayoutData(fdModelComp);
    wModelTab.setControl(wModelComp);
  }

  private void addDimensionsTab() {
    wDimensionsTab = new CTabItem(wTabFolder, SWT.NONE);
    wDimensionsTab.setText(BaseMessages.getString(PKG, "StarModelDialog.DimensionsTab.Label")); //$NON-NLS-1$

    FormLayout dimensionsLayout = new FormLayout();
    dimensionsLayout.marginWidth = Const.MARGIN;
    dimensionsLayout.marginHeight = Const.MARGIN;

    Composite wDimensionsComp = new Composite(wTabFolder, SWT.NONE);
    props.setLook(wDimensionsComp);
    wDimensionsComp.setLayout(dimensionsLayout);

    Label wlTables = new Label(wDimensionsComp, SWT.RIGHT);
    wlTables.setText(BaseMessages.getString(PKG, "StarModelDialog.Tables.Label")); //$NON-NLS-1$
    props.setLook(wlTables);
    FormData fdlAttributes = new FormData();
    fdlAttributes.left = new FormAttachment(0, 0);
    fdlAttributes.top = new FormAttachment(0, 0);
    wlTables.setLayoutData(fdlAttributes);
    
    // A few buttons to edit the list
    // 
    Button newTableButton = new Button(wDimensionsComp, SWT.PUSH);
    newTableButton.setText(BaseMessages.getString(PKG, "StarModelDialog.Button.NewTable"));
    newTableButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) { 
        if (newTable(shell, logicalModel)) {
          refreshTablesList();
        }
      }
    });
    
    Button copyTableButton = new Button(wDimensionsComp, SWT.PUSH);
    copyTableButton.setText(BaseMessages.getString(PKG, "StarModelDialog.Button.CopyTable"));
    copyTableButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) { 
        if (wTablesList.getSelectionIndex()<0) return;
        TableItem item = wTablesList.table.getSelection()[0];
        String tableName = item.getText(1);

        if (copyTable(shell, logicalModel, tableName)) {
          refreshTablesList();
        }
      }
    });
    
    Button editTableButton = new Button(wDimensionsComp, SWT.PUSH);
    editTableButton.setText(BaseMessages.getString(PKG, "StarModelDialog.Button.EditTable"));
    editTableButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) { 
        if (wTablesList.getSelectionIndex()<0) return;
        TableItem item = wTablesList.table.getSelection()[0];
        String tableName = item.getText(1);
        if (editTable(tableName)) {
          refreshTablesList();
        }
      }
    });

    Button delTableButton = new Button(wDimensionsComp, SWT.PUSH);
    delTableButton.setText(BaseMessages.getString(PKG, "StarModelDialog.Button.DeleteTable"));
    delTableButton.addSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent event) {
        if (wTablesList.getSelectionIndex()<0) return;
        TableItem item = wTablesList.table.getSelection()[0];
        String tableName = item.getText(1);
        if (deleteTable(tableName)) {
          refreshTablesList();
        }
      }
    });
    BaseStepDialog.positionBottomButtons(wDimensionsComp, new Button[] { newTableButton, copyTableButton, editTableButton, delTableButton, }, margin, null);

    final int FieldsRows = logicalModel.getLogicalTables().size();

    List<DatabaseMeta> sharedDatabases = new ArrayList<DatabaseMeta>();
    try {
      SharedObjects sharedObjects = new SharedObjects();
      for (SharedObjectInterface sharedObject : sharedObjects.getObjectsMap().values()) {
        if (sharedObject instanceof DatabaseMeta) {
          sharedDatabases.add((DatabaseMeta) sharedObject);
        }
      }
    } catch (Exception e) {
      LogChannel.GENERAL.logError("Unable to load shared objects", e);
    }

    // The dimensions and fact of the model
    //
    ColumnInfo[] colinf = new ColumnInfo[] { 
        new ColumnInfo(BaseMessages.getString(PKG, "StarModelDialog.ColumnInfo.Name.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(BaseMessages.getString(PKG, "StarModelDialog.ColumnInfo.Description.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
        new ColumnInfo(BaseMessages.getString(PKG, "StarModelDialog.ColumnInfo.TableType.Label"), ColumnInfo.COLUMN_TYPE_TEXT, false, true), //$NON-NLS-1$
    };

    wTablesList = new TableView(new Variables(), wDimensionsComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, FieldsRows, null, props);
    FormData fdTablesList = new FormData();
    fdTablesList.left = new FormAttachment(0, 0);
    fdTablesList.top = new FormAttachment(wlTables, margin);
    fdTablesList.right = new FormAttachment(100, 0);
    fdTablesList.bottom = new FormAttachment(newTableButton, -margin);
    wTablesList.setLayoutData(fdTablesList);
    wTablesList.setReadonly(true);
    wTablesList.table.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
        if (wTablesList.getSelectionIndex()<0) return;
        TableItem item = wTablesList.table.getSelection()[0];
        String tableName = item.getText(1);
        if (editTable(tableName)) {
          refreshTablesList();
          // refreshRelationshipsList();
        }
      }
    });
    FormData fdTablesComp = new FormData();
    fdTablesComp.left = new FormAttachment(0, 0);
    fdTablesComp.top = new FormAttachment(0, 0);
    fdTablesComp.right = new FormAttachment(100, 0);
    fdTablesComp.bottom = new FormAttachment(100, 0);
    wDimensionsComp.setLayoutData(fdTablesComp);

    wDimensionsComp.layout();
    wDimensionsTab.setControl(wDimensionsComp);
  }
  
  private void addFactTab()
  {
      wFactTab=new CTabItem(wTabFolder, SWT.NONE);
      wFactTab.setText(BaseMessages.getString(PKG, "StarModelDialog.FactTab.Label")); //$NON-NLS-1$

      FormLayout factLayout = new FormLayout ();
      factLayout.marginWidth  = Const.MARGIN;
      factLayout.marginHeight = Const.MARGIN;
      
      Composite wFactComp = new Composite(wTabFolder, SWT.NONE);
      props.setLook(wFactComp);
      wFactComp.setLayout(factLayout);
      
      Button wAddDimensionKeys = new Button(wFactComp, SWT.PUSH);
      wAddDimensionKeys.setText(BaseMessages.getString(PKG, "StarModelDialog.AddDimensionKeys.Label"));
      BaseStepDialog.positionBottomButtons(wFactComp, new Button[] { wAddDimensionKeys, }, margin, null);
      wAddDimensionKeys.addSelectionListener(new SelectionAdapter() {  public void widgetSelected(SelectionEvent e) { addDimensionKeys(); }});

      // Table  name:
      //
      Label wlFactTableName = new Label(wFactComp, SWT.RIGHT);
      wlFactTableName.setText(BaseMessages.getString(PKG, "StarModelDialog.FactTableName.Label"));
      props.setLook(wlFactTableName);
      FormData fdlFactTableName = new FormData();
      fdlFactTableName.left = new FormAttachment(0, 0);
      fdlFactTableName.right= new FormAttachment(middle, -margin);
      fdlFactTableName.top  = new FormAttachment(0, margin);
      wlFactTableName.setLayoutData(fdlFactTableName);
      wFactTableName=new Text(wFactComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
      props.setLook(wFactTableName);
      FormData fdFactTableName = new FormData();
      fdFactTableName.left = new FormAttachment(middle, 0);
      fdFactTableName.top  = new FormAttachment(0, margin);
      fdFactTableName.right= new FormAttachment(100, 0);
      wFactTableName.setLayoutData(fdFactTableName);
      Control lastControl = wFactTableName;

      // Table description
      //
      Label wlFactTableDescription = new Label(wFactComp, SWT.RIGHT);
      wlFactTableDescription.setText(BaseMessages.getString(PKG, "StarModelDialog.FactTableDescription.Label"));
      props.setLook(wlFactTableDescription);
      FormData fdlFactTableDescription = new FormData();
      fdlFactTableDescription.left = new FormAttachment(0, 0);
      fdlFactTableDescription.right= new FormAttachment(middle, -margin);
      fdlFactTableDescription.top  = new FormAttachment(lastControl, margin);
      wlFactTableDescription.setLayoutData(fdlFactTableDescription);
      wFactTableDescription=new Text(wFactComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
      props.setLook(wFactTableDescription);
      FormData fdFactTableDescription = new FormData();
      fdFactTableDescription.left = new FormAttachment(middle, 0);
      fdFactTableDescription.top  = new FormAttachment(lastControl, margin);
      fdFactTableDescription.right= new FormAttachment(100, 0);
      wFactTableDescription.setLayoutData(fdFactTableDescription);
      lastControl = wFactTableDescription;
      
      // Table description
      //
      Label wlPhysicalFactName = new Label(wFactComp, SWT.RIGHT);
      wlPhysicalFactName.setText(BaseMessages.getString(PKG, "StarModelDialog.PhysicalFactName.Label"));
      props.setLook(wlPhysicalFactName);
      FormData fdlPhysicalName = new FormData();
      fdlPhysicalName.left = new FormAttachment(0, 0);
      fdlPhysicalName.right= new FormAttachment(middle, -margin);
      fdlPhysicalName.top  = new FormAttachment(lastControl, margin);
      wlPhysicalFactName.setLayoutData(fdlPhysicalName);
      wPhysicalFactName=new Text(wFactComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
      props.setLook(wPhysicalFactName);
      FormData fdPhysicalFactName = new FormData();
      fdPhysicalFactName.left = new FormAttachment(middle, 0);
      fdPhysicalFactName.top  = new FormAttachment(lastControl, margin);
      fdPhysicalFactName.right= new FormAttachment(100, 0);
      wPhysicalFactName.setLayoutData(fdPhysicalFactName);
      lastControl = wPhysicalFactName;
      
      Label wlAtrributes = new Label(wFactComp, SWT.RIGHT);
      wlAtrributes.setText(BaseMessages.getString(PKG, "DimensionTableDialog.Attributes.Label")); //$NON-NLS-1$
      props.setLook(wlAtrributes);
      FormData fdlAttributes = new FormData();
      fdlAttributes.left = new FormAttachment(0, 0);
      fdlAttributes.top  = new FormAttachment(lastControl, margin);
      wlAtrributes.setLayoutData(fdlAttributes);
      
      final int FieldsRows=factTable.getLogicalColumns().size();
      
      List<DatabaseMeta> sharedDatabases= SharedDatabaseUtil.loadSharedDatabases();
      String[] databaseNames = SharedDatabaseUtil.getSortedDatabaseNames(sharedDatabases);
      
      // data types
      //
      String[] dataTypes = new String[DataType.values().length];
      for (int i=0;i<dataTypes.length;i++) {
        dataTypes[i] = DataType.values()[i].name();
      }

      // field types
      //
      String[] attributeTypes = new String[AttributeType.values().length];
      for (int i=0;i<AttributeType.values().length;i++) {
        attributeTypes[i] = AttributeType.values()[i].name();
      }
      
      String[] dimensionNames = getDimensionTableNames();
      
      //  name, description, field type, physical column name, data type, length, precision, source db, source table, source column, conversion remarks 
      //
      factColumns=new ColumnInfo[] {
          new ColumnInfo(BaseMessages.getString(PKG, "StarModelDialog.ColumnInfo.Name.Label"), ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
          new ColumnInfo(BaseMessages.getString(PKG, "StarModelDialog.ColumnInfo.Description.Label"), ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
          new ColumnInfo(BaseMessages.getString(PKG, "StarModelDialog.ColumnInfo.FieldType.Label"), ColumnInfo.COLUMN_TYPE_CCOMBO, attributeTypes), //$NON-NLS-1$
          new ColumnInfo(BaseMessages.getString(PKG, "StarModelDialog.ColumnInfo.PhysicalName.Label"), ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
          new ColumnInfo(BaseMessages.getString(PKG, "StarModelDialog.ColumnInfo.DataType.Label"), ColumnInfo.COLUMN_TYPE_CCOMBO, dataTypes), //$NON-NLS-1$
          new ColumnInfo(BaseMessages.getString(PKG, "StarModelDialog.ColumnInfo.Dimension.Label"), ColumnInfo.COLUMN_TYPE_CCOMBO, dimensionNames), //$NON-NLS-1$
          new ColumnInfo(BaseMessages.getString(PKG, "StarModelDialog.ColumnInfo.DataLength.Label"), ColumnInfo.COLUMN_TYPE_TEXT,   true), //$NON-NLS-1$
          new ColumnInfo(BaseMessages.getString(PKG, "StarModelDialog.ColumnInfo.DataPrecision.Label"), ColumnInfo.COLUMN_TYPE_TEXT,   true), //$NON-NLS-1$
          new ColumnInfo(BaseMessages.getString(PKG, "StarModelDialog.ColumnInfo.SourceDatabase.Label"), ColumnInfo.COLUMN_TYPE_CCOMBO, databaseNames), //$NON-NLS-1$
          new ColumnInfo(BaseMessages.getString(PKG, "StarModelDialog.ColumnInfo.SourceTable.Label"), ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
          new ColumnInfo(BaseMessages.getString(PKG, "StarModelDialog.ColumnInfo.SourceColumn.Label"), ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
          new ColumnInfo(BaseMessages.getString(PKG, "StarModelDialog.ColumnInfo.ConversionLogicRemarks.Label"), ColumnInfo.COLUMN_TYPE_TEXT,   false), //$NON-NLS-1$
      };
      
      wFactAttributes=new TableView(new Variables(), wFactComp, 
                            SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, 
                            factColumns, 
                            FieldsRows,  
                            null,
                            props
                            );
     
      FormData fdFactAttributes = new FormData();
      fdFactAttributes.left  = new FormAttachment(0, 0);
      fdFactAttributes.top   = new FormAttachment(wlAtrributes, margin);
      fdFactAttributes.right = new FormAttachment(100, 0);
      fdFactAttributes.bottom= new FormAttachment(wAddDimensionKeys, -margin*2);
      wFactAttributes.setLayoutData(fdFactAttributes);

      FormData fdFactComp = new FormData();
      fdFactComp.left  = new FormAttachment(0, 0);
      fdFactComp.top   = new FormAttachment(0, 0);
      fdFactComp.right = new FormAttachment(100, 0);
      fdFactComp.bottom= new FormAttachment(100, 0);
      wFactComp.setLayoutData(fdFactComp);
      
      wFactComp.layout();
      wFactTab.setControl(wFactComp);
  } 

  
  protected void getRelationshipsFromFact() {
    logicalRelationships = new ArrayList<LogicalRelationship>();
    getFactColumns();
    for (LogicalColumn column : factTable.getLogicalColumns()) {
      String dimensionName = ConceptUtil.getString(column, DefaultIDs.LOGICAL_COLUMN_DIMENSION_NAME);
      if (!Const.isEmpty(dimensionName)) {
        LogicalTable dimensionTable = ConceptUtil.findDimensionWithName(logicalModel, dimensionName, locale);
        if (dimensionTable!=null) {
          LogicalColumn tk = ConceptUtil.findLogicalColumn(dimensionTable, AttributeType.TECHNICAL_KEY);
          if (tk==null) {
            tk = ConceptUtil.findLogicalColumn(dimensionTable, AttributeType.SMART_TECHNICAL_KEY);
          }
          if (tk!=null) {
            LogicalTable fromTable = factTable;
            LogicalColumn fromColumn = column;
            LogicalTable toTable = dimensionTable;
            LogicalColumn toColumn = tk;
            LogicalRelationship relationship = new LogicalRelationship(fromTable, toTable, fromColumn, toColumn);
            logicalRelationships.add(relationship);
          }
        }
      }
    }
    
  }

  private String[] getDimensionTableNames() {
    List<LogicalTable> dimensionTables = ConceptUtil.findLogicalTables(logicalModel, TableType.DIMENSION);
    String[] dimensionNames = new String[dimensionTables.size()];
    for (int i=0;i<dimensionNames.length;i++) {
      dimensionNames[i] = Const.NVL(ConceptUtil.getName(dimensionTables.get(i), locale), "");
    }
    return dimensionNames;
  }

  protected void addDimensionKeys() {
    // Import the technical keys from all the dimensions in the fact table...
    //
    List<LogicalColumn> keyColumns = new ArrayList<LogicalColumn>();
    List<LogicalTable> dimensionTables = ConceptUtil.findLogicalTables(logicalModel, TableType.DIMENSION);
    for (LogicalTable dimensionTable : dimensionTables) {
      // Find the technical or smart key
      //
      keyColumns.addAll( ConceptUtil.findLogicalColumns(dimensionTable, AttributeType.SMART_TECHNICAL_KEY) );
      keyColumns.addAll( ConceptUtil.findLogicalColumns(dimensionTable, AttributeType.TECHNICAL_KEY) );
    }

    for (LogicalColumn keyColumn : keyColumns) {
      LogicalColumn column = new LogicalColumn();
      String dimensionName = ConceptUtil.getName(keyColumn.getLogicalTable(), locale);
      
      column.setName(new LocalizedString(locale, dimensionName+" TK"));
      column.setDescription(new LocalizedString(locale, ConceptUtil.getDescription(keyColumn, locale)));
      column.setProperty(DefaultIDs.LOGICAL_COLUMN_PHYSICAL_COLUMN_NAME, dimensionName.toLowerCase().replace(' ', '_')+"_tk");
      column.setProperty(DefaultIDs.LOGICAL_COLUMN_ATTRIBUTE_TYPE, AttributeType.TECHNICAL_KEY.name());
      column.setDataType(keyColumn.getDataType());
      column.setProperty(DefaultIDs.LOGICAL_COLUMN_DIMENSION_NAME, dimensionName);
      column.setProperty(DefaultIDs.LOGICAL_COLUMN_LENGTH, ConceptUtil.getString(keyColumn, DefaultIDs.LOGICAL_COLUMN_LENGTH));
      column.setProperty(DefaultIDs.LOGICAL_COLUMN_LENGTH, ConceptUtil.getString(keyColumn, DefaultIDs.LOGICAL_COLUMN_LENGTH));
      column.setProperty(DefaultIDs.LOGICAL_COLUMN_CONVERSION_REMARKS, "Key to dimension '"+dimensionName+"'");
      addLogicalColumnToFactAttributesList(column);
    }
    
    wFactAttributes.removeEmptyRows();
    wFactAttributes.setRowNums();
    wFactAttributes.optWidth(true);
    
    getRelationshipsFromFact();
  }

  protected void drawLogicalModel(LogicalModel logicalModel, Canvas canvas, PaintEvent paintEvent) {
    getRelationshipsFromFact();
    Rectangle rect = canvas.getBounds();
    GCInterface gc = new SWTDirectGC(paintEvent.gc, new org.pentaho.di.core.gui.Point(rect.width, rect.height), 32);
    StarModelPainter painter = new StarModelPainter(gc, logicalModel, logicalRelationships, locale);
    painter.draw();
  }
  
  protected boolean deleteTable(String tableName) {
    LogicalTable logicalTable = findLogicalTable(tableName);
    if (logicalTable!=null) {
      // TODO : show warning
      //
      logicalModel.getLogicalTables().remove(logicalTable);
      return true;
    }
    return false;
  }
  
  private LogicalTable findLogicalTable(String tableName) {
    for (LogicalTable logicalTable : logicalModel.getLogicalTables()) {
      if (logicalTable.getName(locale).equalsIgnoreCase(tableName)) return logicalTable;
    }
    return null;
  }

  protected boolean editTable(String tableName) {
    LogicalTable logicalTable = findLogicalTable(tableName);
    if (logicalTable!=null) {
      DimensionTableDialog dialog = new DimensionTableDialog(shell, logicalTable, locale);
      if (dialog.open()!=null) {
        return true;
      }
    }
    return false;
  }

  protected void refreshTablesList() {
    wTablesList.clearAll();
    
    for (LogicalTable logicalTable : logicalModel.getLogicalTables()) {
      TableType tableType = (TableType) logicalTable.getProperty(DefaultPropertyID.TABLE_TYPE.getId());
      if (tableType==TableType.DIMENSION) {
        TableItem item = new TableItem(wTablesList.table, SWT.NONE);
        item.setText(1, Const.NVL(ConceptUtil.getName(logicalTable, locale), ""));
        item.setText(2, Const.NVL(ConceptUtil.getDescription(logicalTable, locale), ""));
        String typeDescription = tableType==null ? "" : tableType.name();
        if (tableType==TableType.DIMENSION) {
          DimensionType dimType = ConceptUtil.getDimensionType(logicalTable);
          if (dimType!=DimensionType.OTHER) { 
            typeDescription+=" - "+dimType.name();
          }
        }
        item.setText(3, typeDescription );
      }
    }
    wTablesList.removeEmptyRows();
    wTablesList.setRowNums();
    wTablesList.optWidth(true);
    
    String[] dimensionNames = getDimensionTableNames();
    factColumns[5].setComboValues(dimensionNames);    
  }
  
  protected void refreshFactAttributesList() {
    wFactAttributes.clearAll();
    
    for (LogicalColumn column : factTable.getLogicalColumns()) {
      addLogicalColumnToFactAttributesList(column);
    }
    wFactAttributes.removeEmptyRows();
    wFactAttributes.setRowNums();
    wFactAttributes.optWidth(true);
  }
   
  private void addLogicalColumnToFactAttributesList(LogicalColumn column) {
    TableItem item = new TableItem(wFactAttributes.table, SWT.NONE);
    
    //  name, description, physical column name, data type, length, precision, source db, source table, source column, conversion remarks 
    //
    int col=1;
    item.setText(col++, Const.NVL(ConceptUtil.getName(column,locale), ""));
    item.setText(col++, Const.NVL(ConceptUtil.getDescription(column, locale), ""));
    item.setText(col++, ConceptUtil.getAttributeType(column).name());
    item.setText(col++, Const.NVL((String)column.getProperty(DefaultIDs.LOGICAL_COLUMN_PHYSICAL_COLUMN_NAME), ""));
    DataType dataType = (DataType) column.getProperty(DefaultPropertyID.DATA_TYPE.getId());
    item.setText(col++, dataType==null ? "" : dataType.name() );
    item.setText(col++, Const.NVL(ConceptUtil.getString(column, DefaultIDs.LOGICAL_COLUMN_DIMENSION_NAME), ""));
    item.setText(col++, Const.NVL(ConceptUtil.getString(column, DefaultIDs.LOGICAL_COLUMN_LENGTH), ""));
    item.setText(col++, Const.NVL(ConceptUtil.getString(column, DefaultIDs.LOGICAL_COLUMN_PRECISION), ""));
    item.setText(col++, Const.NVL(ConceptUtil.getString(column, DefaultIDs.LOGICAL_COLUMN_SOURCE_DB), ""));
    item.setText(col++, Const.NVL(ConceptUtil.getString(column, DefaultIDs.LOGICAL_COLUMN_SOURCE_TABLE), ""));
    item.setText(col++, Const.NVL(ConceptUtil.getString(column, DefaultIDs.LOGICAL_COLUMN_SOURCE_COLUMN), ""));
    item.setText(col++, Const.NVL(ConceptUtil.getString(column, DefaultIDs.LOGICAL_COLUMN_CONVERSION_REMARKS), ""));
  }
    
  protected boolean newTable(Shell shell, LogicalModel logicalModel) {
    LogicalTable logicalTable = new LogicalTable(logicalModel, null);
    logicalTable.setId(UUID.randomUUID().toString());
    logicalTable.setName(new LocalizedString(locale, "New table"));
    logicalTable.setDescription(new LocalizedString(locale, "New table description"));
    
    DimensionTableDialog dialog = new DimensionTableDialog(shell, logicalTable, locale);
    if (dialog.open()!=null) {
      logicalModel.addLogicalTable(logicalTable);
      return true;
    }
    return false;
  }
  
  protected boolean copyTable(Shell shell, LogicalModel logicalModel, String tableName) {
    
    LogicalTable originalTable = findLogicalTable(tableName);
    if (originalTable!=null) {
      // Copy
      //
      LogicalTable logicalTable = new LogicalTable();
      logicalTable.setId(UUID.randomUUID().toString());
      logicalTable.setName(new LocalizedString(locale, ConceptUtil.getName(originalTable, locale)+" (Copy)"));
      logicalTable.setDescription(new LocalizedString(locale, ConceptUtil.getDescription(originalTable, locale)+" (Copy)"));
      logicalTable.setProperty(DefaultIDs.LOGICAL_TABLE_PHYSICAL_TABLE_NAME, originalTable.getProperty(DefaultIDs.LOGICAL_TABLE_PHYSICAL_TABLE_NAME));
      logicalTable.setProperty(DefaultPropertyID.TABLE_TYPE.getId(), originalTable.getProperty(DefaultPropertyID.TABLE_TYPE.getId()));
      for (LogicalColumn column : originalTable.getLogicalColumns()) {
        logicalTable.getLogicalColumns().add((LogicalColumn) column.clone());
      }
      
      DimensionTableDialog dialog = new DimensionTableDialog(shell, logicalTable, locale);
      if (dialog.open()!=null) {
        logicalModel.addLogicalTable(logicalTable);
        return true;
      }
    }
    return false;
  }
  
  public void dispose() {
    WindowProperty winprop = new WindowProperty(shell);
    props.setScreen(winprop);
    shell.dispose();
  }

  /**
   * Copy information from the meta-data input to the dialog fields.
   */
  public void getData() {
    wModelName.setText(Const.NVL(logicalModel.getName(locale), ""));
    wModelDescription.setText(Const.NVL(logicalModel.getDescription(locale), ""));

    wFactTableName.setText(Const.NVL(factTable.getName(locale), ""));
    wFactTableDescription.setText(Const.NVL(factTable.getDescription(locale), ""));
    String phFactTable = ConceptUtil.getString(factTable, DefaultIDs.LOGICAL_TABLE_PHYSICAL_TABLE_NAME);
    wPhysicalFactName.setText(Const.NVL(phFactTable, ""));

    String factName = ConceptUtil.getName(factTable, locale);
    System.out.println("Fact name = "+factName+" has "+factTable.getLogicalColumns().size()+" columns");

    refreshTablesList();

    System.out.println("Fact name = "+factName+" has "+factTable.getLogicalColumns().size()+" columns");

    refreshFactAttributesList();
    
    System.out.println("Fact name = "+factName+" has "+factTable.getLogicalColumns().size()+" columns");

  }

  private void cancel() {
    props.setScreen(new WindowProperty(shell));
    logicalModel = null;
    dispose();
  }

  private void ok() {
    
    if (Const.isEmpty(wModelName.getText())) {
      MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
      box.setText(BaseMessages.getString(PKG, "StarModelDialog.ErrorModelHasNoName.Title"));
      box.setMessage(BaseMessages.getString(PKG, "StarModelDialog.ErrorModelHasNoName.Message"));
      box.open();
      return;
    }

    logicalModel.setName(new LocalizedString(locale, wModelName.getText()));
    logicalModel.setDescription(new LocalizedString(locale, wModelDescription.getText()));

    factTable.setName(new LocalizedString(locale, wFactTableName.getText()));
    factTable.setDescription(new LocalizedString(locale, wFactTableDescription.getText()));
    factTable.setProperty(DefaultIDs.LOGICAL_TABLE_PHYSICAL_TABLE_NAME, wPhysicalFactName.getText());

    String factName = ConceptUtil.getName(factTable, locale);
    System.out.println("Fact name = "+factName+" has "+factTable.getLogicalColumns().size()+" columns");

    // Add the relationships informational
    //
    getRelationshipsFromFact();
    logicalModel.getLogicalRelationships().clear();
    logicalModel.getLogicalRelationships().addAll(logicalRelationships);
    
    // System.out.println("Fact name = "+factName+" has "+factTable.getLogicalColumns().size()+" columns");

    // If the fact table is not yet in the table list, add it.
    // If it is, replace it.
    //
    int factIndex = ConceptUtil.indexOfFactTable(logicalModel);
    if (factIndex<0) {
      logicalModel.getLogicalTables().add(factTable);
    }

    System.out.println("Fact name = "+factName+" has "+factTable.getLogicalColumns().size()+" columns");

    dispose();
  }

  private void getFactColumns() {
    factTable.getLogicalColumns().clear();
    int nr = wFactAttributes.nrNonEmpty();
    for (int i=0;i<nr;i++) {
      TableItem item = wFactAttributes.getNonEmpty(i);
      LogicalColumn logicalColumn = new LogicalColumn();
      
      int col=1;
      logicalColumn.setId(UUID.randomUUID().toString());
      logicalColumn.setName(new LocalizedString(locale, item.getText(col++)));
      logicalColumn.setDescription(new LocalizedString(locale, item.getText(col++)));
      String fieldTypeString = item.getText(col++);
      logicalColumn.setProperty(DefaultIDs.LOGICAL_COLUMN_ATTRIBUTE_TYPE, AttributeType.getAttributeType(fieldTypeString).name());
      logicalColumn.setProperty(DefaultIDs.LOGICAL_COLUMN_PHYSICAL_COLUMN_NAME, item.getText(col++));
      logicalColumn.setDataType(ConceptUtil.getDataType(item.getText(col++)));
      logicalColumn.setProperty(DefaultIDs.LOGICAL_COLUMN_DIMENSION_NAME, item.getText(col++));
      logicalColumn.setProperty(DefaultIDs.LOGICAL_COLUMN_LENGTH, item.getText(col++));
      logicalColumn.setProperty(DefaultIDs.LOGICAL_COLUMN_PRECISION, item.getText(col++));
      logicalColumn.setProperty(DefaultIDs.LOGICAL_COLUMN_SOURCE_DB, item.getText(col++));
      logicalColumn.setProperty(DefaultIDs.LOGICAL_COLUMN_SOURCE_TABLE, item.getText(col++));
      logicalColumn.setProperty(DefaultIDs.LOGICAL_COLUMN_SOURCE_COLUMN, item.getText(col++));
      logicalColumn.setProperty(DefaultIDs.LOGICAL_COLUMN_CONVERSION_REMARKS, item.getText(col++));
      
      logicalColumn.setLogicalTable(factTable);
      factTable.getLogicalColumns().add(logicalColumn);
    }
  }
  
}