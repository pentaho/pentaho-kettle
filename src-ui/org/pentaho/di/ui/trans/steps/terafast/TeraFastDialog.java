/*
 * $Header: TeraFastDialog.java
 * $Revision:
 * $Date: 06.05.2009 23:59:03
 *
 * ==========================================================
 * COPYRIGHTS
 * ==========================================================
 * Copyright (c) 2009 Aschauer EDV.  All rights reserved. 
 * This software was developed by Aschauer EDV and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Terafast 
 * PDI Plugin. The Initial Developer is Aschauer EDV.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/

package org.pentaho.di.ui.trans.steps.terafast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SourceToTargetMapping;
import org.pentaho.di.core.database.TeradataDatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.KeyValue;
import org.pentaho.di.core.util.PluginProperty;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.terafast.TeraFastMeta;
import org.pentaho.di.ui.core.SimpleFileSelection;
import org.pentaho.di.ui.core.dialog.EnterMappingDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.PluginWidgetFactory;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.step.TableItemInsertListener;

/**
 * TODO BaseStepDialog should replaced by something like AbstractStepDialog (BaseStepDialog is ... - asc042, 13.05.2009)
 * 
 * @author <a href="mailto:michael.gugerell@aschauer-edv.at">Michael Gugerell(asc145)</a>
 * 
 */
public class TeraFastDialog extends BaseStepDialog implements StepDialogInterface {

	private static Class<?> PKG = TeraFastMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static final int FORM_ATTACHMENT_OFFSET = 100;

    private static final int FORM_ATTACHMENT_FACTOR = -15;

    private TeraFastMeta meta;

    private CCombo wConnection;

    private Label wlConnection;

    private Button wbnConnection, wbeConnection;

    private Label wlTable;

    private TextVar wTable;

    private Label wlFastLoadPath;

    private TextVar wFastLoadPath;

    private Button wbFastLoadPath;

    private Label wlControlFile;

    private TextVar wControlFile;

    private Button wbControlFile;

    private Label wlDataFile;

    private TextVar wDataFile;

    private Button wbDataFile;

    private Label wlLogFile;

    private Button wbLogFile;

    private TextVar wLogFile;

    private Label wlErrLimit;

    private TextVar wErrLimit;

    private Label wlSessions;

    private TextVar wSessions;

    private Label wlUseControlFile;

    private Button wUseControlFile;
    
    private Label wlVariableSubstitution;
    
    private Button wVariableSubstitution;

    private Label wlTruncateTable;

    private Button wbTruncateTable;

    private Link wAscLink;

    private Label wlReturn;

    private TableView wReturn;

    private Button wGetLU;

    private FormData fdGetLU;

    private Listener lsGetLU;

    private Button wDoMapping;

    private FormData fdDoMapping;
    
    private Button wAbout;

    private ColumnInfo[] ciReturn;

    private Map<String, Integer> inputFields = new HashMap<String, Integer>();

    /**
     * List of ColumnInfo that should have the field names of the selected database table.
     */
    private List<ColumnInfo> tableFieldColumns = new ArrayList<ColumnInfo>();

    /**
     * Constructor.
     * 
     * @param parent
     *            parent shell.
     * @param baseStepMeta
     *            step meta
     * @param transMeta
     *            transaction meta
     * @param stepname
     *            name of step.
     */
    public TeraFastDialog(final Shell parent, final Object baseStepMeta, final TransMeta transMeta,
            final String stepname) {
        super(parent, (BaseStepMeta)baseStepMeta, transMeta, stepname);
        this.meta = (TeraFastMeta) baseStepMeta;
    }

    /**
     * @param property
     *            property.
     * @param textVar
     *            text varibale.
     */
    public static void setTextIfPropertyValue(final PluginProperty property, final TextVar textVar) {
        if (property.evaluate()) {
            textVar.setText(((KeyValue<?>) property).stringValue());
        }
    }

    /**
     * @param property
     *            property.
     * @param combo
     *            text variable.
     */
    public static void setTextIfPropertyValue(final PluginProperty property, final CCombo combo) {
        if (property.evaluate()) {
            combo.setText(((KeyValue<?>) property).stringValue());
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.pentaho.di.trans.step.StepDialogInterface#open()
     */
    public String open() {
        this.changed = this.meta.hasChanged();

        final Shell parent = getParent();
        final Display display = parent.getDisplay();

        this.shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
        this.props.setLook(this.shell);
        setShellImage(this.shell, this.meta);

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        this.shell.setLayout(formLayout);
        this.shell.setText(BaseMessages.getString(PKG, "TeraFastDialog.Shell.Title"));

        buildUi();
        assignChangeListener();
        listeners();

        // 
        // Search the fields in the background
        //

        final Runnable runnable = new Runnable() {
            public void run() {
                final StepMeta stepMetaSearchFields = TeraFastDialog.this.transMeta
                        .findStep(TeraFastDialog.this.stepname);
                if (stepMetaSearchFields == null) {
                    return;
                }
                try {
                    final RowMetaInterface row = TeraFastDialog.this.transMeta.getPrevStepFields(stepMetaSearchFields);

                    // Remember these fields...
                    for (int i = 0; i < row.size(); i++) {
                        TeraFastDialog.this.inputFields.put(row.getValueMeta(i).getName(), Integer.valueOf(i));
                    }

                    setComboBoxes();
                } catch (KettleException e) {
                    TeraFastDialog.this.logError( BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
                }
            }
        };
        new Thread(runnable).start();
        //        
        // // Set the shell size, based upon previous time...
        setSize();

        getData();
        this.meta.setChanged(this.changed);
        disableInputs();

        this.shell.open();
        while (!this.shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return this.stepname;
    }

    /**
     * ...
     */
    protected void setComboBoxes() {
        // Something was changed in the row.
        //
        final Map<String, Integer> fields = new HashMap<String, Integer>();

        // Add the currentMeta fields...
        fields.putAll(this.inputFields);

        Set<String> keySet = fields.keySet();
        List<String> entries = new ArrayList<String>(keySet);

        String[] fieldNames = entries.toArray(new String[entries.size()]);
        Const.sortStrings(fieldNames);
        // return fields
        this.ciReturn[1].setComboValues(fieldNames);
    }

    /**
     * Set data values in dialog.
     */
    public void getData() {
        this.wStepname.selectAll();
        setTextIfPropertyValue(this.meta.getFastloadPath(), this.wFastLoadPath);
        setTextIfPropertyValue(this.meta.getControlFile(), this.wControlFile);
        setTextIfPropertyValue(this.meta.getDataFile(), this.wDataFile);
        setTextIfPropertyValue(this.meta.getLogFile(), this.wLogFile);
        setTextIfPropertyValue(this.meta.getTargetTable(), this.wTable);
        setTextIfPropertyValue(this.meta.getErrorLimit(), this.wErrLimit);
        setTextIfPropertyValue(this.meta.getSessions(), this.wSessions);
        setTextIfPropertyValue(this.meta.getConnectionName(), this.wConnection);
        this.wbTruncateTable.setSelection(this.meta.getTruncateTable().getValue());
        this.wUseControlFile.setSelection(this.meta.getUseControlFile().getValue());
        this.wVariableSubstitution.setSelection(this.meta.getVariableSubstitution().getValue());

        if (this.meta.getTableFieldList().getValue().size() == this.meta.getStreamFieldList().getValue().size()) {
            for (int i = 0; i < this.meta.getTableFieldList().getValue().size(); i++) {
                TableItem item = this.wReturn.table.getItem(i);
                item.setText(1, this.meta.getTableFieldList().getValue().get(i));
                item.setText(2, this.meta.getStreamFieldList().getValue().get(i));
            }
        }
        // DatabaseMeta dbMeta = this.transMeta.findDatabase(this.meta.getDbConnection().getValue());
        if (this.meta.getDbMeta() != null) {
            this.wConnection.setText(this.meta.getConnectionName().getValue());
        } else {
            if (this.transMeta.nrDatabases() == 1) {
                this.wConnection.setText(this.transMeta.getDatabase(0).getName());
            }
        }
        setTableFieldCombo();
    }

    /**
     * Configure listeners.
     */
    private void listeners() {
        this.lsCancel = new Listener() {
            public void handleEvent(final Event event) {
                cancel();
            }
        };
        this.lsOK = new Listener() {
            public void handleEvent(final Event event) {
                ok();
            }
        };

        this.wAbout.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event arg0) {
                new TeraFastAboutDialog(TeraFastDialog.this.shell).open();
            }
        });
        
        this.wCancel.addListener(SWT.Selection, this.lsCancel);
        this.wOK.addListener(SWT.Selection, this.lsOK);

        this.lsDef = new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(final SelectionEvent event) {
                ok();
            }
        };

        this.lsGetLU = new Listener() {
            public void handleEvent(final Event event) {
                getUpdate();
            }
        };

        this.wGetLU.addListener(SWT.Selection, this.lsGetLU);
        this.wStepname.addSelectionListener(this.lsDef);
        final String allFileTypes = BaseMessages.getString(PKG, "TeraFastDialog.Filetype.All");
        this.wbControlFile.addSelectionListener(new SimpleFileSelection(this.shell, this.wControlFile, allFileTypes));
        this.wbDataFile.addSelectionListener(new SimpleFileSelection(this.shell, this.wDataFile, allFileTypes));
        this.wbFastLoadPath.addSelectionListener(new SimpleFileSelection(this.shell, this.wFastLoadPath, allFileTypes));
        this.wbLogFile.addSelectionListener(new SimpleFileSelection(this.shell, this.wLogFile, allFileTypes));

        this.wDoMapping.addListener(SWT.Selection, new Listener() {
            public void handleEvent(final Event event) {
                generateMappings();
            }
        });

        this.wAscLink.addListener(SWT.Selection, new Listener() {
            public void handleEvent(final Event event) {
                Program.launch(event.text);
            }
        });

        // Detect X or ALT-F4 or something that kills this window...
        this.shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(final ShellEvent event) {
                cancel();
            }
        });
    }

    /**
     * Reads in the fields from the previous steps and from the ONE next step and opens an EnterMappingDialog with this
     * information. After the user did the mapping, those information is put into the Select/Rename table.
     */
    public void generateMappings() {

        // Determine the source and target fields...
        //
        RowMetaInterface sourceFields;
        RowMetaInterface targetFields;

        try {
            sourceFields = this.transMeta.getPrevStepFields(this.stepMeta);
        } catch (KettleException e) {
            new ErrorDialog(this.shell, BaseMessages.getString(PKG, "TeraFastDialog.DoMapping.UnableToFindSourceFields.Title"),
                    BaseMessages.getString(PKG, "TeraFastDialog.DoMapping.UnableToFindSourceFields.Message"), e);
            return;
        }
        // refresh fields
        this.meta.getTargetTable().setValue(this.wTable.getText());
        try {
            targetFields = this.meta.getRequiredFields(this.transMeta);
        } catch (KettleException e) {
            new ErrorDialog(this.shell, BaseMessages.getString(PKG, "TeraFastDialog.DoMapping.UnableToFindTargetFields.Title"),
                    BaseMessages.getString(PKG, "TeraFastDialog.DoMapping.UnableToFindTargetFields.Message"), e);
            return;
        }

        String[] inputNames = new String[sourceFields.size()];
        for (int i = 0; i < sourceFields.size(); i++) {
            ValueMetaInterface value = sourceFields.getValueMeta(i);
            inputNames[i] = value.getName() + EnterMappingDialog.STRING_ORIGIN_SEPARATOR + value.getOrigin() + ")";
        }

        // Create the existing mapping list...
        //
        List<SourceToTargetMapping> mappings = new ArrayList<SourceToTargetMapping>();
        StringBuffer missingSourceFields = new StringBuffer();
        StringBuffer missingTargetFields = new StringBuffer();

        int nrFields = this.wReturn.nrNonEmpty();
        for (int i = 0; i < nrFields; i++) {
            TableItem item = this.wReturn.getNonEmpty(i);
            String source = item.getText(2);
            String target = item.getText(1);

            int sourceIndex = sourceFields.indexOfValue(source);
            if (sourceIndex < 0) {
                missingSourceFields.append(Const.CR + "   " + source + " --> " + target);
            }
            int targetIndex = targetFields.indexOfValue(target);
            if (targetIndex < 0) {
                missingTargetFields.append(Const.CR + "   " + source + " --> " + target);
            }
            if (sourceIndex < 0 || targetIndex < 0) {
                continue;
            }

            SourceToTargetMapping mapping = new SourceToTargetMapping(sourceIndex, targetIndex);
            mappings.add(mapping);
        }

        // show a confirm dialog if some missing field was found
        //
        if (missingSourceFields.length() > 0 || missingTargetFields.length() > 0) {

            String message = "";
            if (missingSourceFields.length() > 0) {
                message += BaseMessages.getString(PKG, "TeraFastDialog.DoMapping.SomeSourceFieldsNotFound", missingSourceFields
                        .toString())
                        + Const.CR;
            }
            if (missingTargetFields.length() > 0) {
                message += BaseMessages.getString(PKG, "TeraFastDialog.DoMapping.SomeTargetFieldsNotFound", missingSourceFields
                        .toString())
                        + Const.CR;
            }
            message += Const.CR;
            message += BaseMessages.getString(PKG, "TeraFastDialog.DoMapping.SomeFieldsNotFoundContinue") + Const.CR;
            // MessageDialog.setDefaultImage(GUIResource.getInstance().getImageSpoon());
            // boolean goOn = MessageDialog.openConfirm(this.shell,
            // BaseMessages.getString(PKG, "TeraFastDialog.DoMapping.SomeFieldsNotFoundTitle"), message);
            // if (!goOn) {
            // return;
            // }
        }
        EnterMappingDialog d = new EnterMappingDialog(TeraFastDialog.this.shell, sourceFields.getFieldNames(),
                targetFields.getFieldNames(), mappings);
        mappings = d.open();

        // mappings == null if the user pressed cancel
        //
        if (mappings != null) {
            // Clear and re-populate!
            //
            this.wReturn.table.removeAll();
            this.wReturn.table.setItemCount(mappings.size());
            for (int i = 0; i < mappings.size(); i++) {
                SourceToTargetMapping mapping = mappings.get(i);
                TableItem item = this.wReturn.table.getItem(i);
                item.setText(2, sourceFields.getValueMeta(mapping.getSourcePosition()).getName());
                item.setText(1, targetFields.getValueMeta(mapping.getTargetPosition()).getName());
            }
            this.wReturn.setRowNums();
            this.wReturn.optWidth(true);
        }
    }

    /**
     * ...
     */
    public void getUpdate() {
        try {
            final RowMetaInterface row = this.transMeta.getPrevStepFields(this.stepname);
            if (row != null) {
                TableItemInsertListener listener = new TableItemInsertListener() {
                    public boolean tableItemInserted(final TableItem tableItem, final ValueMetaInterface value) {
                        // possible to check format of input fields
                        return true;
                    }
                };
                BaseStepDialog.getFieldsFromPrevious(row, this.wReturn, 1, new int[] {1, 2}, new int[] {}, -1, -1,
                        listener);
            }
        } catch (KettleException ke) {
            new ErrorDialog(this.shell, BaseMessages.getString(PKG, "TeraFastDialog.FailedToGetFields.DialogTitle"), 
            		BaseMessages.getString(PKG, "TeraFastDialog.FailedToGetFields.DialogMessage"), ke);
        }
    }

    /**
     * Dialog is closed.
     */
    public void cancel() {
        this.stepname = null;
        this.meta.setChanged(this.changed);
        dispose();
    }

    /**
     * Ok clicked.
     */
    public void ok() {
        this.stepname = this.wStepname.getText(); // return value
        this.meta.getUseControlFile().setValue(this.wUseControlFile.getSelection());
        this.meta.getVariableSubstitution().setValue(this.wVariableSubstitution.getSelection());
        this.meta.getControlFile().setValue(this.wControlFile.getText());
        this.meta.getFastloadPath().setValue(this.wFastLoadPath.getText());
        this.meta.getDataFile().setValue(this.wDataFile.getText());
        this.meta.getLogFile().setValue(this.wLogFile.getText());
        this.meta.getErrorLimit().setValue(Const.toInt(this.wErrLimit.getText(), TeraFastMeta.DEFAULT_ERROR_LIMIT));
        this.meta.getSessions().setValue(Const.toInt(this.wSessions.getText(), TeraFastMeta.DEFAULT_SESSIONS));
        this.meta.getTargetTable().setValue(this.wTable.getText());
        this.meta.getConnectionName().setValue(this.wConnection.getText());
        this.meta.getTruncateTable().setValue(this.wbTruncateTable.getSelection() && this.wbTruncateTable.getEnabled());
        this.meta.setDbMeta(this.transMeta.findDatabase(this.wConnection.getText()));

        this.meta.getTableFieldList().getValue().clear();
        this.meta.getStreamFieldList().getValue().clear();
        int nrfields = this.wReturn.nrNonEmpty();
        for (int i = 0; i < nrfields; i++) {
            TableItem item = this.wReturn.getNonEmpty(i);
            this.meta.getTableFieldList().getValue().add(item.getText(1));
            this.meta.getStreamFieldList().getValue().add(item.getText(2));
        }

        dispose();
    }

    /**
     * Build UI.
     */
    protected void buildUi() {
        final PluginWidgetFactory factory = new PluginWidgetFactory(this.shell, this.transMeta);
        factory.setMiddle(this.props.getMiddlePct());
        this.buildStepNameLine(factory);
        this.buildUseControlFileLine(factory);
        this.buildControlFileLine(factory);
        this.buildVariableSubstitutionLine(factory);
        this.buildFastloadLine(factory);
        this.buildLogFileLine(factory);
        // connection line
        this.wbnConnection = new Button(this.shell, SWT.PUSH);
        this.wbeConnection = new Button(this.shell, SWT.PUSH);
        this.wlConnection = new Label(this.shell, SWT.RIGHT);
        this.wConnection = addConnectionLine(this.shell, this.wLogFile, factory.getMiddle(), factory.getMargin(), this.wlConnection, this.wbnConnection, this.wbeConnection, TeradataDatabaseMeta.class);
        this.buildTableLine(factory);
        this.buildTruncateTableLine(factory);
        this.buildDataFileLine(factory);
        this.buildSessionsLine(factory);
        this.buildErrorLimitLine(factory);
        this.buildFieldTable(factory);
        this.buildAscLink(factory);

        this.wOK = factory.createPushButton(BaseMessages.getString(PKG, "System.Button.OK"));
        this.wCancel = factory.createPushButton(BaseMessages.getString(PKG, "System.Button.Cancel"));
        this.wAbout = factory.createPushButton(BaseMessages.getString(PKG, "TeraFastDialog.About.Button"));
        setButtonPositions(new Button[] {this.wOK, this.wCancel, this.wAbout}, factory.getMargin(), this.wAscLink);
    }

    /**
     * @param factory
     *            factory to use.
     */
    protected void buildControlFileLine(final PluginWidgetFactory factory) {
        final Control topControl = this.wUseControlFile;

        this.wlControlFile = factory.createRightLabel(BaseMessages.getString(PKG, "TeraFastDialog.ControlFile.Label"));
        this.props.setLook(this.wlControlFile);
        this.wlControlFile.setLayoutData(factory.createLabelLayoutData(topControl));

        this.wbControlFile = factory.createPushButton(BaseMessages.getString(PKG, "TeraFastDialog.Browse.Button"));
        this.props.setLook(this.wbControlFile);
        FormData formData = factory.createControlLayoutData(topControl);
        formData.left = null;
        this.wbControlFile.setLayoutData(formData);

        this.wControlFile = factory.createSingleTextVarLeft();
        this.props.setLook(this.wControlFile);
        formData = factory.createControlLayoutData(topControl);
        formData.right = new FormAttachment(this.wbControlFile, -factory.getMargin());
        this.wControlFile.setLayoutData(formData);
    }

    /**
     * @param factory
     *            factory to use.
     */
    protected void buildFastloadLine(final PluginWidgetFactory factory) {
        final Control topControl = this.wVariableSubstitution;

        this.wlFastLoadPath = factory.createRightLabel(BaseMessages.getString(PKG, "TeraFastDialog.FastloadPath.Label"));
        this.props.setLook(this.wlFastLoadPath);
        this.wlFastLoadPath.setLayoutData(factory.createLabelLayoutData(topControl));

        this.wbFastLoadPath = factory.createPushButton(BaseMessages.getString(PKG, "TeraFastDialog.Browse.Button"));
        this.props.setLook(this.wbFastLoadPath);
        FormData formData = factory.createControlLayoutData(topControl);
        formData.left = null;
        this.wbFastLoadPath.setLayoutData(formData);

        this.wFastLoadPath = factory.createSingleTextVarLeft();
        this.props.setLook(this.wFastLoadPath);
        formData = factory.createControlLayoutData(topControl);
        formData.right = new FormAttachment(this.wbFastLoadPath, -factory.getMargin());
        this.wFastLoadPath.setLayoutData(formData);
    }

    /**
     * @param factory
     *            factory to use.
     */
    protected void buildUseControlFileLine(final PluginWidgetFactory factory) {
        final Control topControl = this.wStepname;

        this.wlUseControlFile = factory.createRightLabel(BaseMessages.getString(PKG, "TeraFastDialog.UseControlFile.Label"));
        this.props.setLook(this.wlUseControlFile);
        this.wlUseControlFile.setLayoutData(factory.createLabelLayoutData(topControl));

        this.wUseControlFile = new Button(this.shell, SWT.CHECK);
        this.props.setLook(this.wUseControlFile);
        this.wUseControlFile.setLayoutData(factory.createControlLayoutData(topControl));

        this.wUseControlFile.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                disableInputs();
            }
        });
    }

    /**
     * @param factory
     *            factory to use.
     */
    protected void buildVariableSubstitutionLine(final PluginWidgetFactory factory) {
        final Control topControl = this.wControlFile;

        this.wlVariableSubstitution = factory.createRightLabel(BaseMessages.getString(PKG, "TeraFastDialog.VariableSubstitution.Label"));
        this.props.setLook(this.wlVariableSubstitution);
        this.wlVariableSubstitution.setLayoutData(factory.createLabelLayoutData(topControl));

        this.wVariableSubstitution = new Button(this.shell, SWT.CHECK);
        this.props.setLook(this.wVariableSubstitution);
        this.wVariableSubstitution.setLayoutData(factory.createControlLayoutData(topControl));
    }
    
    /**
     * @param factory
     *            factory to use.
     */
    protected void buildLogFileLine(final PluginWidgetFactory factory) {
        final Control topControl = this.wFastLoadPath;

        this.wlLogFile = factory.createRightLabel(BaseMessages.getString(PKG, "TeraFastDialog.LogFile.Label"));
        this.props.setLook(this.wlLogFile);
        this.wlLogFile.setLayoutData(factory.createLabelLayoutData(topControl));

        this.wbLogFile = factory.createPushButton(BaseMessages.getString(PKG, "TeraFastDialog.Browse.Button"));
        this.props.setLook(this.wbLogFile);
        FormData formData = factory.createControlLayoutData(topControl);
        formData.left = null;
        this.wbLogFile.setLayoutData(formData);

        this.wLogFile = factory.createSingleTextVarLeft();
        this.props.setLook(this.wLogFile);
        formData = factory.createControlLayoutData(topControl);
        formData.right = new FormAttachment(this.wbLogFile, -factory.getMargin());
        this.wLogFile.setLayoutData(formData);
    }

    /**
     * Build step name line.
     * 
     * @param factory
     *            factory to use.
     */
    protected void buildStepNameLine(final PluginWidgetFactory factory) {
        this.wlStepname = factory.createRightLabel(BaseMessages.getString(PKG, "TeraFastDialog.StepName.Label"));
        this.props.setLook(this.wlStepname);
        this.fdlStepname = factory.createLabelLayoutData(null);
        this.wlStepname.setLayoutData(this.fdlStepname);

        this.wStepname = factory.createSingleTextLeft(this.stepname);
        this.props.setLook(this.wStepname);
        this.fdStepname = factory.createControlLayoutData(null);
        this.wStepname.setLayoutData(this.fdStepname);
    }

    /**
     * @param factory
     *            factory to use.
     */
    protected void buildTableLine(final PluginWidgetFactory factory) {
        final Control topControl = this.wConnection;

        this.wlTable = factory.createRightLabel(BaseMessages.getString(PKG, "TeraFastDialog.TargetTable.Label"));
        this.props.setLook(this.wlTable);
        this.wlTable.setLayoutData(factory.createLabelLayoutData(topControl));

        this.wTable = factory.createSingleTextVarLeft();
        this.props.setLook(this.wTable);
        this.wTable.setLayoutData(factory.createControlLayoutData(topControl));

        this.wTable.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(final FocusEvent event) {
                setTableFieldCombo();
            }
        });
    }

    /**
     * @param factory
     *            factory to use.
     */
    protected void buildTruncateTableLine(final PluginWidgetFactory factory) {
        final Control topControl = this.wTable;

        this.wlTruncateTable = factory.createRightLabel(BaseMessages.getString(PKG, "TeraFastDialog.TruncateTable.Label"));
        this.props.setLook(this.wlTruncateTable);
        this.wlTruncateTable.setLayoutData(factory.createLabelLayoutData(topControl));

        this.wbTruncateTable = new Button(this.shell, SWT.CHECK);
        this.props.setLook(this.wbTruncateTable);
        this.wbTruncateTable.setLayoutData(factory.createControlLayoutData(topControl));
    }

    /**
     * @param factory
     *            factory to use.
     */
    protected void buildDataFileLine(final PluginWidgetFactory factory) {
        final Control topControl = this.wbTruncateTable;

        this.wlDataFile = factory.createRightLabel(BaseMessages.getString(PKG, "TeraFastDialog.DataFile.Label"));
        this.props.setLook(this.wlDataFile);
        this.wlDataFile.setLayoutData(factory.createLabelLayoutData(topControl));

        this.wbDataFile = factory.createPushButton(BaseMessages.getString(PKG, "TeraFastDialog.Browse.Button"));
        this.props.setLook(this.wbDataFile);
        FormData formData = factory.createControlLayoutData(topControl);
        formData.left = null;
        this.wbDataFile.setLayoutData(formData);

        this.wDataFile = factory.createSingleTextVarLeft();
        this.props.setLook(this.wDataFile);
        formData = factory.createControlLayoutData(topControl);
        formData.right = new FormAttachment(this.wbDataFile, -factory.getMargin());
        this.wDataFile.setLayoutData(formData);
    }

    /**
     * @param factory
     *            factory to use.
     */
    protected void buildSessionsLine(final PluginWidgetFactory factory) {
        final Control topControl = this.wDataFile;

        this.wlSessions = factory.createRightLabel(BaseMessages.getString(PKG, "TeraFastDialog.Sessions.Label"));
        this.props.setLook(this.wlSessions);
        this.wlSessions.setLayoutData(factory.createLabelLayoutData(topControl));

        this.wSessions = factory.createSingleTextVarLeft();
        this.props.setLook(this.wSessions);
        this.wSessions.setLayoutData(factory.createControlLayoutData(topControl));
    }

    /**
     * @param factory
     *            factory to use.
     */
    protected void buildErrorLimitLine(final PluginWidgetFactory factory) {
        final Control topControl = this.wSessions;

        this.wlErrLimit = factory.createRightLabel(BaseMessages.getString(PKG, "TeraFastDialog.ErrLimit.Label"));
        this.props.setLook(this.wlErrLimit);
        this.wlErrLimit.setLayoutData(factory.createLabelLayoutData(topControl));

        this.wErrLimit = factory.createSingleTextVarLeft();
        this.props.setLook(this.wErrLimit);
        this.wErrLimit.setLayoutData(factory.createControlLayoutData(topControl));
    }

    /**
     * @param factory
     *            factory to use.
     */
    protected void buildAscLink(final PluginWidgetFactory factory) {
        final Control topControl = this.wReturn;

        this.wAscLink = new Link(this.shell, SWT.NONE);
        this.wAscLink.setText(BaseMessages.getString(PKG, "TeraFastDialog.Provided.Info"));
        FormData formData = factory.createLabelLayoutData(topControl);
        formData.right = null;
        this.wAscLink.setLayoutData(formData);
    }

    /**
     * @param factory
     *            factory to use.
     */
    protected void buildFieldTable(final PluginWidgetFactory factory) {
        final Control topControl = this.wErrLimit;

        this.wlReturn = factory.createLabel(SWT.NONE, BaseMessages.getString(PKG, "TeraFastDialog.Fields.Label"));
        this.props.setLook(this.wlReturn);
        this.wlReturn.setLayoutData(factory.createLabelLayoutData(topControl));

        final int upInsCols = 2;
        final int upInsRows;
        if (this.meta.getTableFieldList().isEmpty()) {
            upInsRows = 1;
        } else {
            upInsRows = this.meta.getTableFieldList().size();
        }

        this.ciReturn = new ColumnInfo[upInsCols];
        this.ciReturn[0] = new ColumnInfo(BaseMessages.getString(PKG, "TeraFastDialog.ColumnInfo.TableField"),
                ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {""}, false);
        this.ciReturn[1] = new ColumnInfo(BaseMessages.getString(PKG, "TeraFastDialog.ColumnInfo.StreamField"),
                ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] {""}, false);
        this.tableFieldColumns.add(this.ciReturn[0]);
        this.wReturn = new TableView(this.transMeta, this.shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI
                | SWT.V_SCROLL | SWT.H_SCROLL, this.ciReturn, upInsRows, null, this.props);

        this.wGetLU = factory.createPushButton(BaseMessages.getString(PKG, "TeraFastDialog.GetFields.Label"));
        this.fdGetLU = new FormData();
        this.fdGetLU.top = new FormAttachment(this.wlReturn, factory.getMargin());
        this.fdGetLU.right = new FormAttachment(FORM_ATTACHMENT_OFFSET, 0);
        this.wGetLU.setLayoutData(this.fdGetLU);

        this.wDoMapping = factory.createPushButton(BaseMessages.getString(PKG, "TeraFastDialog.EditMapping.Label"));
        this.fdDoMapping = new FormData();
        this.fdDoMapping.top = new FormAttachment(this.wGetLU, factory.getMargin());
        this.fdDoMapping.right = new FormAttachment(FORM_ATTACHMENT_OFFSET, 0);
        this.wDoMapping.setLayoutData(this.fdDoMapping);

        FormData formData = new FormData();
        formData.left = new FormAttachment(0, 0);
        formData.top = new FormAttachment(this.wlReturn, factory.getMargin());
        formData.right = new FormAttachment(this.wGetLU, -factory.getMargin());
        formData.bottom = new FormAttachment(FORM_ATTACHMENT_OFFSET, FORM_ATTACHMENT_FACTOR * factory.getMargin());
        this.wReturn.setLayoutData(formData);
    }

    /**
     * ...
     */
    protected void assignChangeListener() {
        final ModifyListener lsMod = new ModifyListener() {
            public void modifyText(final ModifyEvent event) {
                getMeta().setChanged();
            }
        };
        final SelectionAdapter lsSel = new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                getMeta().setChanged();
            }
        };

        this.wStepname.addModifyListener(lsMod);
        this.wControlFile.addModifyListener(lsMod);
        this.wFastLoadPath.addModifyListener(lsMod);
        this.wLogFile.addModifyListener(lsMod);
        this.wConnection.addModifyListener(lsMod);
        this.wTable.addModifyListener(lsMod);
        this.wDataFile.addModifyListener(lsMod);
        this.wSessions.addModifyListener(lsMod);
        this.wErrLimit.addModifyListener(lsMod);
        this.wbTruncateTable.addSelectionListener(lsSel);
        this.wUseControlFile.addSelectionListener(lsSel);
        this.wVariableSubstitution.addSelectionListener(lsSel);
        this.wReturn.addModifyListener(lsMod);
    }

    /**
     * Disable inputs.
     */
    public void disableInputs() {
        boolean useControlFile = this.wUseControlFile.getSelection();
        this.wbControlFile.setEnabled(useControlFile);
        this.wControlFile.setEnabled(useControlFile);
        this.wDataFile.setEnabled(!useControlFile);
        this.wbDataFile.setEnabled(!useControlFile);
        this.wSessions.setEnabled(!useControlFile);
        this.wErrLimit.setEnabled(!useControlFile);
        this.wReturn.setEnabled(!useControlFile);
        this.wGetLU.setEnabled(!useControlFile);
        this.wDoMapping.setEnabled(!useControlFile);
        this.wTable.setEnabled(!useControlFile);
        this.wbTruncateTable.setEnabled(!useControlFile);
        this.wConnection.setEnabled(!useControlFile);
        this.wbeConnection.setEnabled(!useControlFile);
        this.wbnConnection.setEnabled(!useControlFile);
        this.wVariableSubstitution.setEnabled(useControlFile);
    }

    /**
     * ...
     */
    public void setTableFieldCombo() {
        clearColInfo();
        new FieldLoader(this).start();
    }

    /**
     * Clear.
     */
    private void clearColInfo() {
        for (int i = 0, n = this.tableFieldColumns.size(); i < n; i++) {
            final ColumnInfo colInfo = this.tableFieldColumns.get(i);
            colInfo.setComboValues(new String[] {});
        }
    }

    /**
     * @return the meta
     */
    public TeraFastMeta getMeta() {
        return this.meta;
    }

    /**
     * @author <a href="mailto:thomas.hoedl@aschauer-edv.at">Thomas Hoedl(asc042)</a>
     * 
     */
    private static final class FieldLoader extends Thread {

        private TeraFastDialog dialog;

        /**
         * Constructor.
         * 
         * @param dialog
         *            dialog to set.
         */
        public FieldLoader(final TeraFastDialog dialog) {
            this.dialog = dialog;
        }

        /**
         * {@inheritDoc}
         * 
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            try {
                final RowMetaInterface rowMeta = this.dialog.meta.getRequiredFields(this.dialog.transMeta);
                if (rowMeta==null) return;
                
                final String[] fieldNames = rowMeta.getFieldNames();
                if (fieldNames == null) {
                    return;
                }
                for (int i = 0; i < this.dialog.tableFieldColumns.size(); i++) {
                    final ColumnInfo colInfo = this.dialog.tableFieldColumns.get(i);
                    if (this.dialog.shell.isDisposed()) {
                        return;
                    }
                    this.dialog.shell.getDisplay().asyncExec(new Runnable() {
                        public void run() {
                            if (FieldLoader.this.dialog.shell.isDisposed()) {
                                return;
                            }
                            colInfo.setComboValues(fieldNames);
                        }
                    });
                }
            } catch (KettleException e) {
                this.dialog.logError(this.toString(), "Error while reading fields", e);
                // ignore any errors here. drop downs will not be
                // filled, but no problem for the user
            }
        }
    }
}
