/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * Copyright (c) 2010 DynamoBI Corporation.  All rights reserved.
 * This software was developed by DynamoBI Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Farrago 
 * Streaming Loader.  The Initial Developer is DynamoBI Corporation.
 * 
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.di.ui.trans.steps.luciddbstreamingloader;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
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
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.SourceToTargetMapping;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.luciddbstreamingloader.LucidDBStreamingLoaderMeta;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.database.dialog.SQLEditor;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.steps.tableinput.SQLValuesHighlight;

/**
 * Description: Dialog class for the Farrago Streaming Loader step.
 * 
 * @author Ray Zhang
 * @since Jan-05-2010
 * 
 */
public class LucidDBStreamingLoaderDialog extends BaseStepDialog implements StepDialogInterface {

    private static Class<?> PKG = LucidDBStreamingLoaderMeta.class;

    private CCombo wConnection;

    private Label wlSchema;

    private TextVar wSchema;

    private FormData fdlSchema, fdSchema;

    private Label wlTable;

    private Button wbTable;

    private TextVar wTable;

    private FormData fdlTable, fdbTable, fdTable;

    private Label wlHost;

    private TextVar wHost;

    private FormData fdlHost, fdHost;

    private Label wlPort;

    private TextVar wPort;

    private FormData fdlPort, fdPort;

    private Label wlOperation;

    private CCombo wOperation;

    private FormData fdlOperation, fdOperation;

    private CTabFolder wTabFolder;

    private FormData fdTabFolder;

    private CTabItem wKeysTab;

    private CTabItem wFieldsTab;

    private CTabItem wCustomTab;

    private Composite wKeysComp;

    private Composite wFieldsComp;

    private Composite wCustomComp;

    private FormData fdKeysComp;

    private FormData fdFieldsComp;

    private FormData fdCustomComp;

    private Label wlKeysTb;

    private TableView wKeysTb;

    private FormData fdlKeysTb, fdKeysTb;

    private Label wlFieldsTb;

    private TableView wFieldsTb;

    private FormData fdlFieldsTb, fdFieldsTb;

    private Label wlCustomTb;

    private StyledTextComp wCustomTb;

    private FormData fdlCustomTb, fdCustomTb;

    private Button wGetFieldsForKeys;

    private FormData fdGetFieldsForKeys;

    private Button wDoMappingForKeys;

    private FormData fdDoMappingForKeys;

    private Button wGetFieldsForFields;

    private FormData fdGetFieldsForFields;

    private Button wDoMappingForFields;

    private FormData fdDoMappingForFields;

    public LucidDBStreamingLoaderMeta input;

    private Label lAutoCreateTable;

    private FormData fdlAutoCreateTable, fdbAutoCreateTable;

    public LucidDBStreamingLoaderDialog(
        Shell parent,
        Object in,
        TransMeta transMeta,
        String sname)
    {
        super(parent, (BaseStepMeta) in, transMeta, sname);
        input = (LucidDBStreamingLoaderMeta) in;
    }

    public String open()
    {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX
            | SWT.MIN);
        props.setLook(shell);
        setShellImage(shell, input);

        ModifyListener lsMod = new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                input.setChanged();
            }
        };
        changed = input.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.Shell.Title"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Stepname line
        wlStepname = new Label(shell, SWT.RIGHT);
        wlStepname.setText(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.Stepname.Label"));
        props.setLook(wlStepname);
        fdlStepname = new FormData();
        fdlStepname.left = new FormAttachment(0, 0);
        fdlStepname.right = new FormAttachment(middle, -margin);
        fdlStepname.top = new FormAttachment(0, margin);
        wlStepname.setLayoutData(fdlStepname);
        wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wStepname.setText(stepname);
        props.setLook(wStepname);
        wStepname.addModifyListener(lsMod);
        fdStepname = new FormData();
        fdStepname.left = new FormAttachment(middle, 0);
        fdStepname.top = new FormAttachment(0, margin);
        fdStepname.right = new FormAttachment(100, 0);
        wStepname.setLayoutData(fdStepname);

        // Connection line
        wConnection = addConnectionLine(shell, wStepname, middle, margin);
        if (input.getDatabaseMeta() == null && transMeta.nrDatabases() == 1)
            wConnection.select(0);
        wConnection.addModifyListener(lsMod);

        // Schema line...
        wlSchema = new Label(shell, SWT.RIGHT);
        wlSchema.setText(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.TargetSchema.Label"));
        props.setLook(wlSchema);
        fdlSchema = new FormData();
        fdlSchema.left = new FormAttachment(0, 0);
        fdlSchema.right = new FormAttachment(middle, -margin);
        fdlSchema.top = new FormAttachment(wConnection, margin * 2);
        wlSchema.setLayoutData(fdlSchema);

        wSchema = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
            | SWT.BORDER);
        props.setLook(wSchema);
        wSchema.addModifyListener(lsMod);
        fdSchema = new FormData();
        fdSchema.left = new FormAttachment(middle, 0);
        fdSchema.top = new FormAttachment(wConnection, margin * 2);
        fdSchema.right = new FormAttachment(100, 0);
        wSchema.setLayoutData(fdSchema);

        // Table line...
        wlTable = new Label(shell, SWT.RIGHT);
        wlTable.setText(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.TargetTable.Label"));
        props.setLook(wlTable);
        fdlTable = new FormData();
        fdlTable.left = new FormAttachment(0, 0);
        fdlTable.right = new FormAttachment(middle, -margin);
        fdlTable.top = new FormAttachment(wSchema, margin);
        wlTable.setLayoutData(fdlTable);

        wbTable = new Button(shell, SWT.PUSH | SWT.CENTER);
        props.setLook(wbTable);
        wbTable.setText(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.Browse.Button"));
        fdbTable = new FormData();
        fdbTable.right = new FormAttachment(100, 0);
        fdbTable.top = new FormAttachment(wSchema, margin);
        wbTable.setLayoutData(fdbTable);
        wTable = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
            | SWT.BORDER);
        props.setLook(wTable);
        wTable.addModifyListener(lsMod);
        fdTable = new FormData();
        fdTable.left = new FormAttachment(middle, 0);
        fdTable.top = new FormAttachment(wSchema, margin);
        fdTable.right = new FormAttachment(wbTable, -margin);
        wTable.setLayoutData(fdTable);

        // Auto create table check.
        lAutoCreateTable = new Label(shell, SWT.RIGHT);
        props.setLook(lAutoCreateTable);
        fdlAutoCreateTable = new FormData();
        fdlAutoCreateTable.left = new FormAttachment(0, 0);
        fdlAutoCreateTable.top = new FormAttachment(wTable, margin);
        fdlAutoCreateTable.right = new FormAttachment(middle, -margin);
        lAutoCreateTable.setLayoutData(fdlAutoCreateTable);

        fdbAutoCreateTable = new FormData();
        fdbAutoCreateTable.left = new FormAttachment(lAutoCreateTable, 10);
        fdbAutoCreateTable.top = new FormAttachment(wTable, margin);

        // Host
        wlHost = new Label(shell, SWT.RIGHT);
        wlHost.setText(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.Host.Label"));
        props.setLook(wlHost);
        fdlHost = new FormData();
        fdlHost.left = new FormAttachment(0, 0);
        fdlHost.top = new FormAttachment(lAutoCreateTable, margin);
        fdlHost.right = new FormAttachment(middle, -margin);
        wlHost.setLayoutData(fdlHost);
        wHost = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
            | SWT.BORDER);
        props.setLook(wHost);
        wHost.addModifyListener(lsMod);
        fdHost = new FormData();
        fdHost.left = new FormAttachment(middle, 0);
        fdHost.top = new FormAttachment(lAutoCreateTable, margin);
        fdHost.right = new FormAttachment(100, 0);
        wHost.setLayoutData(fdHost);

        // Port line
        wlPort = new Label(shell, SWT.RIGHT);
        wlPort.setText(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.Port.Label"));
        props.setLook(wlPort);
        fdlPort = new FormData();
        fdlPort.left = new FormAttachment(0, 0);
        fdlPort.top = new FormAttachment(wHost, margin);
        fdlPort.right = new FormAttachment(middle, -margin);
        wlPort.setLayoutData(fdlPort);
        wPort = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT
            | SWT.BORDER);
        props.setLook(wPort);
        wPort.addModifyListener(lsMod);
        fdPort = new FormData();
        fdPort.left = new FormAttachment(middle, 0);
        fdPort.top = new FormAttachment(wHost, margin);
        fdPort.right = new FormAttachment(100, 0);
        wPort.setLayoutData(fdPort);

        // Operation line:

        wlOperation = new Label(shell, SWT.RIGHT);
        wlOperation.setText(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.Operation.Label"));
        props.setLook(wlOperation);
        fdlOperation = new FormData();
        fdlOperation.left = new FormAttachment(0, 0);
        fdlOperation.top = new FormAttachment(wPort, margin);
        fdlOperation.right = new FormAttachment(middle, -margin);
        wlOperation.setLayoutData(fdlOperation);
        wOperation = new CCombo(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(wOperation);
        wOperation.addModifyListener(lsMod);
        fdOperation = new FormData();
        fdOperation.top = new FormAttachment(wPort, margin);
        fdOperation.left = new FormAttachment(middle, 0);
        fdOperation.right = new FormAttachment(100, 0);
        wOperation.setLayoutData(fdOperation);

        final String[] operations = new String[] {
            BaseMessages.getString(
                PKG,
                "LucidDBStreamingLoaderDialog.Operation.CCombo.Item1"),
            BaseMessages.getString(
                PKG,
                "LucidDBStreamingLoaderDialog.Operation.CCombo.Item2"),
            BaseMessages.getString(
                PKG,
                "LucidDBStreamingLoaderDialog.Operation.CCombo.Item3"),
            BaseMessages.getString(
                PKG,
                "LucidDBStreamingLoaderDialog.Operation.CCombo.Item4")

        };

        wOperation.setItems(operations);

        wOperation.addSelectionListener(new SelectionListener()
        {

            public void widgetDefaultSelected(SelectionEvent event)
            {

                widgetSelected(event);

            }

            public void widgetSelected(SelectionEvent event)
            {

                CCombo mycc = (CCombo) event.widget;
                // MERGE
                if (operations[0].equals(mycc.getItem(mycc.getSelectionIndex())))
                {
                    wKeysTb.table.removeAll();
                    wKeysTb.table.setItemCount(1);
                    wKeysTb.setRowNums();
                    wFieldsTb.table.removeAll();
                    wFieldsTb.table.setItemCount(1);
                    wFieldsTb.setRowNums();
                    wCustomTb.setText("");

                    wTabFolder.setSelection(wKeysTab);
                    wKeysTab.getControl().setEnabled(true);
                    wFieldsTab.getControl().setEnabled(true);
                    wCustomTab.getControl().setEnabled(false);
                    wFieldsTb.table.getColumn(3).setWidth(80);
                    // INSERT
                } else if (operations[1].equals(mycc.getItem(mycc.getSelectionIndex())))
                {
                    wKeysTb.table.removeAll();
                    wKeysTb.table.setItemCount(1);
                    wKeysTb.setRowNums();
                    wFieldsTb.table.removeAll();
                    wFieldsTb.table.setItemCount(1);
                    wFieldsTb.setRowNums();
                    wCustomTb.setText("");

                    wTabFolder.setSelection(wFieldsTab);
                    wKeysTab.getControl().setEnabled(false);
                    wFieldsTab.getControl().setEnabled(true);
                    wCustomTab.getControl().setEnabled(false);
                    wFieldsTb.table.getColumn(3).setWidth(0);
                    // UPDATE
                } else if (operations[2].equals(mycc.getItem(mycc.getSelectionIndex())))
                {
                    wKeysTb.table.removeAll();
                    wKeysTb.table.setItemCount(1);
                    wKeysTb.setRowNums();
                    wFieldsTb.table.removeAll();
                    wFieldsTb.table.setItemCount(1);
                    wFieldsTb.setRowNums();
                    wCustomTb.setText("");

                    wTabFolder.setSelection(wKeysTab);
                    wKeysTab.getControl().setEnabled(true);
                    wFieldsTab.getControl().setEnabled(true);
                    wCustomTab.getControl().setEnabled(false);
                    // grey out update field in Field TabelView
                    wFieldsTb.table.getColumn(3).setWidth(80);
                    // CUSTOM
                } else if (operations[3].equals(mycc.getItem(mycc.getSelectionIndex())))
                {
                    wKeysTb.table.removeAll();
                    wKeysTb.table.setItemCount(1);
                    wKeysTb.setRowNums();
                    wFieldsTb.table.removeAll();
                    wFieldsTb.table.setItemCount(1);
                    wFieldsTb.setRowNums();
                    wCustomTb.setText("");

                    wTabFolder.setSelection(wKeysTab);
                    wKeysTab.getControl().setEnabled(true);
                    wFieldsTab.getControl().setEnabled(true);
                    wCustomTab.getControl().setEnabled(true);
                    wFieldsTb.table.getColumn(3).setWidth(80);
                }

            }

        }

        );

        // TabFolder
        wTabFolder = new CTabFolder(shell, SWT.BORDER);
        props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
        wTabFolder.setSimple(false);

        fdTabFolder = new FormData();
        fdTabFolder.left = new FormAttachment(0, 0);
        fdTabFolder.top = new FormAttachment(wlOperation, margin);
        fdTabFolder.right = new FormAttachment(100, 0);
        fdTabFolder.bottom = new FormAttachment(100, -50);
        wTabFolder.setLayoutData(fdTabFolder);

        wKeysTab = new CTabItem(wTabFolder, SWT.NONE);
        wKeysTab.setText(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.KeyTab.TabTitle"));

        FormLayout fieldsLayout = new FormLayout();
        fieldsLayout.marginWidth = Const.FORM_MARGIN;
        fieldsLayout.marginHeight = Const.FORM_MARGIN;

        // TabItem: Keys
        wKeysComp = new Composite(wTabFolder, SWT.NONE);
        wKeysComp.setLayout(fieldsLayout);
        props.setLook(wKeysComp);

        wlKeysTb = new Label(wKeysComp, SWT.LEFT);
        wlKeysTb.setText(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.KeyTab.Label"));
        fdlKeysTb = new FormData();

        fdlKeysTb.left = new FormAttachment(0, 0);
        fdlKeysTb.top = new FormAttachment(0, 0);
        fdlKeysTb.right = new FormAttachment(100, 0);

        wlKeysTb.setLayoutData(fdlKeysTb);

        ColumnInfo[] colinf = new ColumnInfo[] {
            new ColumnInfo(
                BaseMessages.getString(
                    PKG,
                    "LucidDBStreamingLoaderDialog.Key.Column1"),
                ColumnInfo.COLUMN_TYPE_TEXT,
                false),
            new ColumnInfo(
                BaseMessages.getString(
                    PKG,
                    "LucidDBStreamingLoaderDialog.Key.Column2"),
                ColumnInfo.COLUMN_TYPE_TEXT,
                false), };
        wKeysTb = new TableView(transMeta, wKeysComp, SWT.FULL_SELECTION
            | SWT.MULTI, colinf, 0, lsMod, props);

        fdKeysTb = new FormData();
        fdKeysTb.left = new FormAttachment(0, 0);
        fdKeysTb.top = new FormAttachment(wlKeysTb, margin);
        fdKeysTb.right = new FormAttachment(100, 0);
        fdKeysTb.bottom = new FormAttachment(95, -margin);
        wKeysTb.setLayoutData(fdKeysTb);

        fdKeysComp = new FormData();
        fdKeysComp.left = new FormAttachment(0, 0);
        fdKeysComp.top = new FormAttachment(0, 0);
        fdKeysComp.right = new FormAttachment(100, 0);
        fdKeysComp.bottom = new FormAttachment(100, 0);
        wKeysComp.setLayoutData(fdKeysComp);

        wGetFieldsForKeys = new Button(wKeysComp, SWT.PUSH);
        wGetFieldsForKeys.setText(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.GetFields.Label"));
        fdGetFieldsForKeys = new FormData();
        fdGetFieldsForKeys.top = new FormAttachment(wKeysTb, margin);
        fdGetFieldsForKeys.left = new FormAttachment(0, margin);
        wGetFieldsForKeys.setLayoutData(fdGetFieldsForKeys);

        wGetFieldsForKeys.addListener(SWT.Selection, new Listener()
        {

            public void handleEvent(Event event)
            {

                getFields(wKeysTab.getText());

            }

        });

        wDoMappingForKeys = new Button(wKeysComp, SWT.PUSH);
        wDoMappingForKeys.setText(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.EditMapping.Label"));
        fdDoMappingForKeys = new FormData();
        fdDoMappingForKeys.top = new FormAttachment(wKeysTb, margin);
        fdDoMappingForKeys.left = new FormAttachment(wGetFieldsForKeys, margin);
        wDoMappingForKeys.setLayoutData(fdDoMappingForKeys);

        wDoMappingForKeys.addListener(SWT.Selection, new Listener()
        {

            public void handleEvent(Event event)
            {

                generateMappings(wKeysTab.getText());

            }

        });

        wKeysComp.layout();
        wKeysTab.setControl(wKeysComp);

        // TabItem: Fields
        wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
        wFieldsTab.setText(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.FieldsTab.TabTitle"));

        wFieldsComp = new Composite(wTabFolder, SWT.NONE);
        wFieldsComp.setLayout(fieldsLayout);
        props.setLook(wFieldsComp);

        wlFieldsTb = new Label(wFieldsComp, SWT.LEFT);
        wlFieldsTb.setText(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.FieldTab.Label"));

        fdlFieldsTb = new FormData();
        fdlFieldsTb.left = new FormAttachment(0, 0);
        fdlFieldsTb.top = new FormAttachment(0, 0);
        fdlFieldsTb.right = new FormAttachment(100, 0);
        wlFieldsTb.setLayoutData(fdlFieldsTb);

        ColumnInfo[] colinf1 = new ColumnInfo[] {
            new ColumnInfo(
                BaseMessages.getString(
                    PKG,
                    "LucidDBStreamingLoaderDialog.Field.Column1"),
                ColumnInfo.COLUMN_TYPE_TEXT,
                false),
            new ColumnInfo(
                BaseMessages.getString(
                    PKG,
                    "LucidDBStreamingLoaderDialog.Field.Column2"),
                ColumnInfo.COLUMN_TYPE_TEXT,
                false),
            new ColumnInfo(
                BaseMessages.getString(
                    PKG,
                    "LucidDBStreamingLoaderDialog.Field.Column3"),
                ColumnInfo.COLUMN_TYPE_CCOMBO,
                new String[] { "Y", "N", },
                true)

        };

        wFieldsTb = new TableView(transMeta, wFieldsComp, SWT.FULL_SELECTION
            | SWT.MULTI, colinf1, 0, lsMod, props);

        fdFieldsTb = new FormData();
        fdFieldsTb.left = new FormAttachment(0, 0);
        fdFieldsTb.top = new FormAttachment(wlFieldsTb, margin);
        fdFieldsTb.right = new FormAttachment(100, 0);
        fdFieldsTb.bottom = new FormAttachment(95, -margin);
        wFieldsTb.setLayoutData(fdFieldsTb);

        fdFieldsComp = new FormData();
        fdFieldsComp.left = new FormAttachment(0, 0);
        fdFieldsComp.top = new FormAttachment(0, 0);
        fdFieldsComp.right = new FormAttachment(100, 0);
        fdFieldsComp.bottom = new FormAttachment(100, 0);
        wFieldsComp.setLayoutData(fdFieldsComp);

        wGetFieldsForFields = new Button(wFieldsComp, SWT.PUSH);
        wGetFieldsForFields.setText(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.GetFields.Label"));
        fdGetFieldsForFields = new FormData();
        fdGetFieldsForFields.top = new FormAttachment(wFieldsTb, margin);
        fdGetFieldsForFields.left = new FormAttachment(0, margin);
        wGetFieldsForFields.setLayoutData(fdGetFieldsForFields);

        wGetFieldsForFields.addListener(SWT.Selection, new Listener()
        {

            public void handleEvent(Event event)
            {

                getFields(wFieldsTab.getText());

            }

        });

        wDoMappingForFields = new Button(wFieldsComp, SWT.PUSH);
        wDoMappingForFields.setText(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.EditMapping.Label"));
        fdDoMappingForFields = new FormData();
        fdDoMappingForFields.top = new FormAttachment(wFieldsTb, margin);
        fdDoMappingForFields.left = new FormAttachment(
            wGetFieldsForFields,
            margin);
        wDoMappingForFields.setLayoutData(fdDoMappingForFields);

        wDoMappingForFields.addListener(SWT.Selection, new Listener()
        {

            public void handleEvent(Event event)
            {

                generateMappings(wFieldsTab.getText());

            }

        });

        wFieldsComp.layout();
        wFieldsTab.setControl(wFieldsComp);

        // TabItem: Custom disable Custom tab
        wCustomTab = new CTabItem(wTabFolder, SWT.NONE);
        wCustomTab.setText(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.CustomTab.TabTitle"));

        wCustomComp = new Composite(wTabFolder, SWT.NONE);
        wCustomComp.setLayout(fieldsLayout);
        props.setLook(wCustomComp);

        wlCustomTb = new Label(wCustomComp, SWT.LEFT);
        wlCustomTb.setText(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.CustomTab.Label"));

        fdlCustomTb = new FormData();
        fdlCustomTb.left = new FormAttachment(0, 0);
        fdlCustomTb.top = new FormAttachment(0, 0);
        fdlCustomTb.right = new FormAttachment(100, 0);
        wlCustomTb.setLayoutData(fdlCustomTb);

        wCustomTb = new StyledTextComp(transMeta, wCustomComp, SWT.MULTI | SWT.LEFT
            | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "");
        props.setLook(wCustomTb, Props.WIDGET_STYLE_FIXED);
        wCustomTb.addModifyListener(lsMod);

        fdCustomTb = new FormData();
        fdCustomTb.left = new FormAttachment(0, 0);
        fdCustomTb.top = new FormAttachment(wlCustomTb, margin);
        fdCustomTb.right = new FormAttachment(100, -2*margin);
        fdCustomTb.bottom = new FormAttachment(100, -margin);
        wCustomTb.setLayoutData(fdCustomTb);

        // Text Higlighting
        SQLValuesHighlight lineStyler = new SQLValuesHighlight();
        wCustomTb.addLineStyleListener(lineStyler);

        fdCustomComp = new FormData();
        fdCustomComp.left = new FormAttachment(0, 0);
        fdCustomComp.top = new FormAttachment(0, 0);
        fdCustomComp.right = new FormAttachment(100, 0);
        fdCustomComp.bottom = new FormAttachment(100, 0);
        wCustomComp.setLayoutData(fdCustomComp);

        wCustomComp.layout();
        wCustomTab.setControl(wCustomComp);

        // THE BUTTONS
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString("System.Button.OK"));
       
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString("System.Button.Cancel"));
        
        wSQL = new Button(shell, SWT.PUSH);
        wSQL.setText(BaseMessages.getString("System.Button.SQL"));

        setButtonPositions(new Button[] { wOK, wSQL, wCancel, }, margin, null);
        
        // Add listeners
        lsOK = new Listener()
        {
            public void handleEvent(Event e)
            {
                ok();
            }
        };

        lsSQL = new Listener()
        {
            public void handleEvent(Event e)
            {
                create();
            }
        };
        lsCancel = new Listener()
        {
            public void handleEvent(Event e)
            {
                cancel();
            }
        };

        wOK.addListener(SWT.Selection, lsOK);
        wSQL.addListener(SWT.Selection, lsSQL);
        wCancel.addListener(SWT.Selection, lsCancel);
        lsDef = new SelectionAdapter()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                ok();
            }
        };

        wStepname.addSelectionListener(lsDef);
        wSchema.addSelectionListener(lsDef);
        wTable.addSelectionListener(lsDef);
        wPort.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter()
        {
            public void shellClosed(ShellEvent e)
            {
                cancel();
            }
        });

        wbTable.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                getTableName();
            }
        });

        // Set the shell size, based upon previous time...
        setSize();

        getData();
        input.setChanged(changed);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return stepname;
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */
    public void getData()
    {

        logDebug(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.Log.GettingKeyInfo"));
        if (input.getDatabaseMeta() != null)
            wConnection.setText(input.getDatabaseMeta().getName());
        else {
            if (transMeta.nrDatabases() == 1) {
                wConnection.setText(transMeta.getDatabase(0).getName());
            }
        }
        if (input.getSchemaName() != null)
            wSchema.setText(input.getSchemaName());
        if (input.getTableName() != null)
            wTable.setText(input.getTableName());
        if (input.getHost() != null)
            wHost.setText("" + input.getHost());
        if (input.getPort() != null)
            wPort.setText("" + input.getPort());
        if (input.getOperation() != null) {

            wOperation.select(wOperation.indexOf(input.getOperation()));
            if (BaseMessages.getString(
                PKG,
                "LucidDBStreamingLoaderDialog.Operation.CCombo.Item2").equals(
                input.getOperation()))
            {

                wTabFolder.setSelection(1);
            } else {

                wTabFolder.setSelection(0);
            }
        }

        if (input.getCustom_sql() != null)
            wCustomTb.setText(input.getCustom_sql());

        if (input.getFieldTableForKeys() != null) {

            int nrKeyMapping = input.getFieldTableForKeys().length;
            wKeysTb.table.setItemCount(nrKeyMapping);
            for (int i = 0; i < nrKeyMapping; i++) {
                TableItem item = wKeysTb.table.getItem(i);
                if (input.getFieldTableForKeys()[i] != null)
                    item.setText(1, input.getFieldTableForKeys()[i]);
                if (input.getFieldStreamForKeys()[i] != null)
                    item.setText(2, input.getFieldStreamForKeys()[i]);
            }
        }

        wKeysTb.setRowNums();
        wKeysTb.optWidth(true);

        if (input.getFieldTableForFields() != null) {

            int nrFieldMapping = input.getFieldTableForFields().length;

            wFieldsTb.table.setItemCount(nrFieldMapping);
            for (int i = 0; i < nrFieldMapping; i++) {

                TableItem item = wFieldsTb.table.getItem(i);
                if (input.getFieldTableForFields()[i] != null)
                    item.setText(1, input.getFieldTableForFields()[i]);
                if (input.getFieldStreamForFields()[i] != null)
                    item.setText(2, input.getFieldStreamForFields()[i]);
                if (input.getInsOrUptFlag() != null) {

                    item.setText(3, input.getInsOrUptFlag()[i] ? "Y" : "N");
                } else {

                    item.setText(3, "N");
                }

            }

        }

        wFieldsTb.setRowNums();
        wFieldsTb.optWidth(true);

        if (input.getTabIsEnable().length != 0) {

            wKeysTab.getControl().setEnabled(input.getTabIsEnable()[0]);
            wFieldsTab.getControl().setEnabled(input.getTabIsEnable()[1]);
            wCustomTab.getControl().setEnabled(input.getTabIsEnable()[2]);

        }

        int fieldWidth = wFieldsTb.table.getColumn(3).getWidth();
        if (input.getOperation() != null
            && "INSERT".equalsIgnoreCase(wOperation.getItem(wOperation.getSelectionIndex())))
        {

            wFieldsTb.table.getColumn(3).setWidth(0);

        } else {

            wFieldsTb.table.getColumn(3).setWidth(fieldWidth);
        }

        wStepname.selectAll();

    }

    /**
     * Reads in the fields from the previous steps and from the ONE next step
     * and opens an EnterMappingDialog with this information. After the user did
     * the mapping, those information is put into the Select/Rename table.
     */
    private void generateMappings(String tabName)
    {

        TableView myTb;

        boolean flag = false;

        if (tabName.equals(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.KeyTab.TabTitle")))
        {

            myTb = wKeysTb;

        } else if (tabName.equals(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.FieldsTab.TabTitle")))
        {

            myTb = wFieldsTb;
            // Hidden Update Field when select operation INSERT
            if (BaseMessages.getString(
                PKG,
                "LucidDBStreamingLoaderDialog.Operation.CCombo.Item2")
                .equalsIgnoreCase(
                    wOperation.getItem(wOperation.getSelectionIndex())))
            {

                flag = true;
            }
        } else {

            return;

        }
        // Determine the source and target fields...
        //
        RowMetaInterface sourceFields;
        RowMetaInterface targetFields;

        try {
            sourceFields = transMeta.getPrevStepFields(stepMeta);
        } catch (KettleException e) {
            new ErrorDialog(
                shell,
                BaseMessages.getString(
                    PKG,
                    "LucidDBStreamingLoaderDialog.DoMapping.UnableToFindSourceFields.Title"),
                BaseMessages.getString(
                    PKG,
                    "LucidDBStreamingLoaderDialog.DoMapping.UnableToFindSourceFields.Message"),
                e);
            return;
        }
        // refresh data
        input.setDatabaseMeta(transMeta.findDatabase(wConnection.getText()));
        input.setTableName(transMeta.environmentSubstitute(wTable.getText()));
        StepMetaInterface stepMetaInterface = stepMeta.getStepMetaInterface();
        try {
            targetFields = stepMetaInterface.getRequiredFields(transMeta);
        } catch (KettleException e) {
            new ErrorDialog(
                shell,
                BaseMessages.getString(
                    PKG,
                    "LucidDBStreamingLoaderDialog.DoMapping.UnableToFindTargetFields.Title"),
                BaseMessages.getString(
                    PKG,
                    "LucidDBStreamingLoaderDialog.DoMapping.UnableToFindTargetFields.Message"),
                e);
            return;
        }

        String[] inputNames = new String[sourceFields.size()];
        for (int i = 0; i < sourceFields.size(); i++) {
            ValueMetaInterface value = sourceFields.getValueMeta(i);
            inputNames[i] = value.getName()
                + EnterMappingDialog.STRING_ORIGIN_SEPARATOR
                + value.getOrigin() + ")";
        }

        // Create the existing mapping list...
        //
        List<SourceToTargetMapping> mappings = new ArrayList<SourceToTargetMapping>();
        StringBuffer missingSourceFields = new StringBuffer();
        StringBuffer missingTargetFields = new StringBuffer();

        // show a confirm dialog if some missing field was found
        //
        if (missingSourceFields.length() > 0
            || missingTargetFields.length() > 0)
        {

            String message = "";
            if (missingSourceFields.length() > 0) {
                message += BaseMessages.getString(
                    PKG,
                    "LucidDBStreamingLoaderDialog.DoMapping.SomeSourceFieldsNotFound",
                    missingSourceFields.toString())
                    + Const.CR;
            }
            if (missingTargetFields.length() > 0) {
                message += BaseMessages.getString(
                    PKG,
                    "LucidDBStreamingLoaderDialog.DoMapping.SomeTargetFieldsNotFound",
                    missingSourceFields.toString())
                    + Const.CR;
            }
            message += Const.CR;
            message += BaseMessages.getString(
                PKG,
                "LucidDBStreamingLoaderDialog.DoMapping.SomeFieldsNotFoundContinue")
                + Const.CR;
            MessageDialog.setDefaultImage(GUIResource.getInstance()
                .getImageSpoon());
            boolean goOn = MessageDialog.openConfirm(
                shell,
                BaseMessages.getString(
                    PKG,
                    "LucidDBStreamingLoaderDialog.DoMapping.SomeFieldsNotFoundTitle"),
                message);
            if (!goOn) {
                return;
            }
        }
        EnterMappingDialog d = new EnterMappingDialog(
            LucidDBStreamingLoaderDialog.this.shell,
            sourceFields.getFieldNames(),
            targetFields.getFieldNames(),
            mappings);
        mappings = d.open();

        // mappings == null if the user pressed cancel
        //
        if (mappings != null) {
            // Clear and re-populate!
            //
            myTb.table.removeAll();
            myTb.table.setItemCount(mappings.size());
            for (int i = 0; i < mappings.size(); i++) {
                SourceToTargetMapping mapping = (SourceToTargetMapping) mappings.get(i);
                TableItem item = myTb.table.getItem(i);
                item.setText(2, sourceFields.getValueMeta(
                    mapping.getSourcePosition()).getName());
                item.setText(1, targetFields.getValueMeta(
                    mapping.getTargetPosition()).getName());
            }
            myTb.setRowNums();
            myTb.optWidth(true);

            // Hidden Update Field when select INSERT.
            int width = myTb.table.getColumn(3).getWidth();

            if (flag) {

                myTb.table.getColumn(3).setWidth(0);

            } else {
                myTb.table.getColumn(3).setWidth(width);

            }

        }

    }

    private void cancel()
    {
        stepname = null;
        input.setChanged(changed);
        dispose();
    }

    private void getInfo(LucidDBStreamingLoaderMeta inf)
    {
        inf.setDatabaseMeta(transMeta.findDatabase(wConnection.getText()));
        inf.setSchemaName(wSchema.getText());
        inf.setTableName(wTable.getText());

        inf.setHost(wHost.getText());
        inf.setPort(wPort.getText());
        inf.setOperation(wOperation.getItem(wOperation.getSelectionIndex()));

        int nrKeyMapping = wKeysTb.nrNonEmpty();
        int nrFieldMappping = wFieldsTb.nrNonEmpty();
        inf.allocate(nrKeyMapping, nrFieldMappping, 3);
        for (int i = 0; i < nrKeyMapping; i++) {
            TableItem item = wKeysTb.getNonEmpty(i);
            inf.getFieldTableForKeys()[i] = item.getText(1);
            inf.getFieldStreamForKeys()[i] = item.getText(2);

        }

        for (int i = 0; i < nrFieldMappping; i++) {
            TableItem item = wFieldsTb.getNonEmpty(i);
            inf.getFieldTableForFields()[i] = item.getText(1);
            inf.getFieldStreamForFields()[i] = item.getText(2);
            inf.getInsOrUptFlag()[i] = "Y".equalsIgnoreCase(item.getText(3));
        }
        inf.getTabIsEnable()[0] = wKeysTab.getControl().getEnabled();
        inf.getTabIsEnable()[1] = wFieldsTab.getControl().getEnabled();
        inf.getTabIsEnable()[2] = wCustomTab.getControl().getEnabled();
        if (BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.Operation.CCombo.Item4").equals(
            inf.getOperation()))
        {
            inf.setCustom_sql(wCustomTb.getText());

        }

        stepname = wStepname.getText(); // return value
    }

    /**
     * Description: When click button called get Field, return all fields in
     * table
     * 
     * @param tabName
     */
    private void getFields(String tabName)
    {

        TableView myTb;

        boolean flag = false; // disable update field when select insert
        // operation.

        if (tabName.equals(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.KeyTab.TabTitle")))
        {

            myTb = wKeysTb;

        } else if (tabName.equals(BaseMessages.getString(
            PKG,
            "LucidDBStreamingLoaderDialog.FieldsTab.TabTitle")))
        {

            myTb = wFieldsTb;

            if (BaseMessages.getString(
                PKG,
                "LucidDBStreamingLoaderDialog.Operation.CCombo.Item2")
                .equalsIgnoreCase(
                    wOperation.getItem(wOperation.getSelectionIndex())))
            {
                flag = true;
            }

        } else {
            return;
        }

        RowMetaInterface streamMeta;

        try {

            streamMeta = transMeta.getPrevStepFields(stepMeta);
            String[] fieldNamesOfStream = streamMeta.getFieldNames();
            input.setSchemaName(wSchema.getText());
            input.setTableName(wTable.getText());
            input.setDatabaseMeta(transMeta.findDatabase(wConnection.getText()));
            StepMetaInterface stepMetaInterface = stepMeta.getStepMetaInterface();
            RowMetaInterface tblMeta = null;
            String[] fieldsNamesOfTbl = null;
            try {
                tblMeta = stepMetaInterface.getRequiredFields(transMeta);
                fieldsNamesOfTbl = tblMeta.getFieldNames();
            } catch (KettleException ke) {

            }
            int count = 0;
            if (fieldsNamesOfTbl == null) {
                count = fieldNamesOfStream.length;
                myTb.table.setItemCount(count);
                for (int i = 0; i < count; i++) {
                    TableItem item = myTb.table.getItem(i);
                    item.setText(1, fieldNamesOfStream[i]);
                    item.setText(2, fieldNamesOfStream[i]);
                }
            } else {
                count = ((fieldNamesOfStream.length >= fieldsNamesOfTbl.length) ? fieldNamesOfStream.length
                    : fieldsNamesOfTbl.length);
                myTb.table.setItemCount(count);
                for (int i = 0; i < count; i++) {
                    TableItem item = myTb.table.getItem(i);
                    if (i >= (fieldsNamesOfTbl.length)) {
                        // item.setText(1, "");
                    } else {
                        if (fieldsNamesOfTbl[i] != null) {
                            item.setText(1, fieldsNamesOfTbl[i]);
                        }
                    }
                    if (i >= fieldNamesOfStream.length) {
                        // item.setText(2, "");
                    } else {
                        if (fieldNamesOfStream[i] != null) {
                            item.setText(2, fieldNamesOfStream[i]);
                        }
                    }
                }
            }
            myTb.setRowNums();
            myTb.optWidth(true);
            if (flag) {
                myTb.table.getColumn(3).setWidth(0);
                System.out.println(myTb.table.getColumn(3).getWidth());
            }
        } catch (KettleStepException e) {
        }

    }

    private void ok()
    {
        if (Const.isEmpty(wStepname.getText()))
            return;

        // Get the information for the dialog into the input structure.
        getInfo(input);



        if (input.getDatabaseMeta() == null) {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
            mb.setMessage(BaseMessages.getString(
                PKG,
                "LucidDBStreamingLoaderDialog.InvalidConnection.DialogMessage"));
            mb.setText(BaseMessages.getString(
                PKG,
                "LucidDBStreamingLoaderDialog.InvalidConnection.DialogTitle"));
            mb.open();
        }

        dispose();
    }

    private void getTableName()
    {
        DatabaseMeta inf = null;
        // New class: SelectTableDialog
        int connr = wConnection.getSelectionIndex();
        if (connr >= 0)
            inf = transMeta.getDatabase(connr);

        if (inf != null) {
            logDebug(BaseMessages.getString(
                PKG,
                "LucidDBStreamingLoaderDialog.Log.LookingAtConnection")
                + inf.toString());

            DatabaseExplorerDialog std = new DatabaseExplorerDialog(
                shell,
                SWT.NONE,
                inf,
                transMeta.getDatabases());
            std.setSelectedSchemaAndTable(wSchema.getText(), wTable.getText());
            if (std.open()) {
                wSchema.setText(Const.NVL(std.getSchemaName(), ""));
                wTable.setText(Const.NVL(std.getTableName(), ""));
            }
        } else {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
            mb.setMessage(BaseMessages.getString(
                PKG,
                "LucidDBStreamingLoaderDialog.InvalidConnection.DialogMessage"));
            mb.setText(BaseMessages.getString(
                PKG,
                "LucidDBStreamingLoaderDialog.InvalidConnection.DialogTitle"));
            mb.open();
        }
    }

    // Generate code for create table...
    // Conversions done by Database
    private void create()
    {
      try
      {
        LucidDBStreamingLoaderMeta info = new LucidDBStreamingLoaderMeta();
        getInfo(info);

        String name = stepname; // new name might not yet be linked to other steps!
        StepMeta stepMeta = new StepMeta(BaseMessages.getString(PKG, "LucidDBStreamingLoaderDialog.StepMeta.Title"), name, info); //$NON-NLS-1$
        RowMetaInterface prev = transMeta.getPrevStepFields(stepname);

        SQLStatement sql = info.getSQLStatements(transMeta, stepMeta, prev);
        if (!sql.hasError())
        {
          if (sql.hasSQL())
          {
            SQLEditor sqledit = new SQLEditor(transMeta, shell, SWT.NONE, info.getDatabaseMeta(), transMeta.getDbCache(),
                sql.getSQL());
            sqledit.open();
          }
          else
          {
            MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_INFORMATION);
            mb.setMessage(BaseMessages.getString(PKG, "LucidDBStreamingLoaderDialog.NoSQLNeeds.DialogMessage")); //$NON-NLS-1$
            mb.setText(BaseMessages.getString(PKG, "LucidDBStreamingLoaderDialog.NoSQLNeeds.DialogTitle")); //$NON-NLS-1$
            mb.open();
          }
        }
        else
        {
          MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
          mb.setMessage(sql.getError());
          mb.setText(BaseMessages.getString(PKG, "LucidDBStreamingLoaderDialog.SQLError.DialogTitle")); //$NON-NLS-1$
          mb.open();
        }
      }
      catch (KettleException ke)
      {
        new ErrorDialog(shell, BaseMessages.getString(PKG, "LucidDBStreamingLoaderDialog.CouldNotBuildSQL.DialogTitle"), //$NON-NLS-1$
            BaseMessages.getString(PKG, "LucidDBStreamingLoaderDialog.CouldNotBuildSQL.DialogMessage"), ke); //$NON-NLS-1$
      }

    }
}
