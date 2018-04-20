/**
 *
 */
package org.pentaho.di.ui.trans.steps.setsrs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.geospatial.SRS;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.setsrs.SRSList;
import org.pentaho.di.trans.steps.setsrs.SetSRS;
import org.pentaho.di.trans.steps.setsrs.SetSRSMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.io.File;
import java.util.TreeSet;

/**
 * Dialog for the {@link SetSRS} step.
 *
 * @author phobus, sgoldinger, tbadard
 * @since 25-nov-2008
 */
public class SetSRSDialog extends BaseStepDialog implements StepDialogInterface {

    private static Class<?> PKG = SetSRS.class;

    // Constants
    private static final int LABEL_OFFSET = 5;
    // Other properties
    private SetSRSMeta input;
    private int status;
    private String wkt, path, fieldname;
    private boolean checkEPSG;
    private Display display = Display.getDefault();
    private Color green, red;

    // Controls
    private ModifyListener lsMod;
    private static CCombo wcSRID;
    private FormData fdwbSRIDCode, fdwbSRIDFile, fdwbSRIDWKT, fdwSRIDInfo,
            fdwFileDialog, fdwbBrowse, fdwWKT, fdWcSRID, fdwbCheck, fdwSRS;
    private SRS selectedSRS;
    private Text wFileDialog, wWKT, wSRS;
    private Label wSRIDInfo;
    private Button wbSRIDCode, wbSRIDFile, wbSRIDWKT, wbBrowse, wbCheck;
    private Listener lsBrowse, lsCheck;

    public SetSRSDialog(Shell parent, Object baseStepMeta, TransMeta transMeta, String stepname) {
        super(parent, (BaseStepMeta) baseStepMeta, transMeta, stepname);
        input = (SetSRSMeta) baseStepMeta;
        status = input.getActualStatus();
        loadMetadata(input);
    }

    /* (non-Javadoc)
     * @see org.pentaho.di.trans.step.StepDialogInterface#open()
     */
    public String open() {
        ////////////////////////////////////////////////////////////////////////
        // Initialize the dialog with look&feel, layout, etc.
        ////////////////////////////////////////////////////////////////////////
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
        setShellImage(shell, input);

        FormLayout layout = new FormLayout();
        layout.marginWidth = Const.FORM_MARGIN * 2;
        layout.marginHeight = Const.FORM_MARGIN * 2;

        shell.setLayout(layout);
        shell.setText(BaseMessages.getString(PKG, "SetSRSDialog.Shell.Title")); //$NON-NLS-1$

        ////////////////////////////////////////////////////////////////////////
        // Adding UI components and listeners
        ////////////////////////////////////////////////////////////////////////

        // Stepname
        wlStepname = new Label(shell, SWT.LEFT);
        wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName")); //$NON-NLS-1$
        fdlStepname = new FormData();
        fdlStepname.left = new FormAttachment(0, 0);
        fdlStepname.right = new FormAttachment(25, 0);
        fdlStepname.top = new FormAttachment(0, Const.MARGIN + LABEL_OFFSET);
        wlStepname.setLayoutData(fdlStepname);

        wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wStepname.setText(stepname);
        fdStepname = new FormData();
        fdStepname.left = new FormAttachment(wlStepname, 0);
        fdStepname.top = new FormAttachment(0, Const.MARGIN);
        fdStepname.right = new FormAttachment(100, 0);
        wStepname.setLayoutData(fdStepname);


        //Geometry-field
        Label wlField = new Label(shell, SWT.LEFT);
        wlField.setText(BaseMessages.getString(PKG, "SetSRSDialog.SetSRSOnField.Label"));
        FormData fdlField = new FormData();
        fdlField.left = new FormAttachment(0, 0);
        fdlField.top = new FormAttachment(wStepname, Const.MARGIN * 2);
        fdlField.right = new FormAttachment(25, 0);
        wlField.setLayoutData(fdlField);

        final CCombo wField = new CCombo(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
        FormData fdField = new FormData();
        fdField.left = new FormAttachment(wlField, 0);
        fdField.top = new FormAttachment(wStepname, Const.MARGIN);
        fdField.right = new FormAttachment(50, 0);
        wField.setLayoutData(fdField);
        fillGeometryFieldsList(wField);

        Label wlFieldInfo = new Label(shell, SWT.LEFT);
        wlFieldInfo.setText(BaseMessages.getString(PKG, "SetSRSDialog.OnlyGeomAreShown.Label"));
        FormData fdlFieldInfo = new FormData();
        fdlFieldInfo.left = new FormAttachment(wField, Const.MARGIN);
        fdlFieldInfo.top = new FormAttachment(wStepname, Const.MARGIN * 2);
        fdlFieldInfo.right = new FormAttachment(100, 0);
        wlFieldInfo.setLayoutData(fdlFieldInfo);

        wbSRIDCode = new Button(shell, SWT.RADIO);
        wbSRIDCode.setText(BaseMessages.getString(PKG, "SetSRSDialog.EPSGCode.Label"));
        fdwbSRIDCode = new FormData();
        fdwbSRIDCode.left = new FormAttachment(0, 0);
        fdwbSRIDCode.top = new FormAttachment(wField, 2 * Const.MARGIN + LABEL_OFFSET);
        fdwbSRIDCode.right = new FormAttachment(25, 0);
        wbSRIDCode.setLayoutData(fdwbSRIDCode);

        //ComboField with EPSG Code List
        wcSRID = new CCombo(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
//		wcSRID.setTextLimit(9);
        wcSRID.setItems(SRSList.getAllSRSCodes());
        wcSRID.add("", 0);
        wcSRID.setEnabled(false);

        fdWcSRID = new FormData();
        fdWcSRID.left = new FormAttachment(wbSRIDCode, 0);
        fdWcSRID.top = new FormAttachment(wField, 2 * Const.MARGIN);
        fdWcSRID.right = new FormAttachment(50, 0);
        wcSRID.setLayoutData(fdWcSRID);

        // Label for SRID Description
        wSRIDInfo = new Label(shell, SWT.NONE);
        fdwSRIDInfo = new FormData();
        fdwSRIDInfo.left = new FormAttachment(wbSRIDCode, 0);
        fdwSRIDInfo.top = new FormAttachment(wcSRID, 2 * Const.MARGIN);
        fdwSRIDInfo.right = new FormAttachment(100, 0);
        wSRIDInfo.setLayoutData(fdwSRIDInfo);

        if (status == SetSRSMeta.STATUS_EPSGCODE) {
            wbSRIDCode.setSelection(true);
            wcSRID.setEnabled(true);
            wSRIDInfo.setEnabled(true);
            setSRIDDescription(selectedSRS.srid);
            // Selection of saved selectedSRS
            if (selectedSRS.description.equals("Custom")) {
                wcSRID.setText(selectedSRS.srid);
            } else {
                wcSRID.select(wcSRID.indexOf(selectedSRS.srid));
            }
        }

        wbSRIDFile = new Button(shell, SWT.RADIO);
        wbSRIDFile.setText(BaseMessages.getString(PKG, "SetSRSDialog.SelectSRSFromFile.Label"));
        fdwbSRIDFile = new FormData();
        fdwbSRIDFile.left = new FormAttachment(0, 0);
        fdwbSRIDFile.right = new FormAttachment(25, 0);
        fdwbSRIDFile.top = new FormAttachment(wSRIDInfo, 2 * Const.MARGIN + LABEL_OFFSET);
        wbSRIDFile.setLayoutData(fdwbSRIDFile);

        // Textfields for FileDialog
        wFileDialog = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wFileDialog.setEnabled(false);
        fdwFileDialog = new FormData();
        fdwFileDialog.left = new FormAttachment(wbSRIDFile, 0);
        fdwFileDialog.top = new FormAttachment(wSRIDInfo, 2 * Const.MARGIN);
        fdwFileDialog.right = new FormAttachment(90, 0);
        wFileDialog.setLayoutData(fdwFileDialog);

        wbBrowse = new Button(shell, SWT.PUSH);
        wbBrowse.setText("...");
        wbBrowse.setEnabled(false);
        fdwbBrowse = new FormData();
        fdwbBrowse.left = new FormAttachment(wFileDialog, Const.MARGIN);
        fdwbBrowse.top = new FormAttachment(wSRIDInfo, 2 * Const.MARGIN);
        fdwbBrowse.right = new FormAttachment(100, 0);
        wbBrowse.setLayoutData(fdwbBrowse);

        if (status == SetSRSMeta.STATUS_FILE) {
            wbSRIDFile.setSelection(true);
            wFileDialog.setEnabled(true);
            wbBrowse.setEnabled(true);
            wFileDialog.setText(selectedSRS.description);
        }

        wbSRIDWKT = new Button(shell, SWT.RADIO);
        wbSRIDWKT.setText(BaseMessages.getString(PKG, "SetSRSDialog.WKTDescription.Label"));
        fdwbSRIDWKT = new FormData();
        fdwbSRIDWKT.left = new FormAttachment(0, 0);
        fdwbSRIDWKT.right = new FormAttachment(25, 0);
        fdwbSRIDWKT.top = new FormAttachment(wFileDialog, 2 * Const.MARGIN + LABEL_OFFSET);
        wbSRIDWKT.setLayoutData(fdwbSRIDWKT);

        // Textfields for WKTString
        wSRS = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wSRS.setEditable(false);
        wSRS.setEnabled(false);
        fdwSRS = new FormData();
        fdwSRS.left = new FormAttachment(wbSRIDWKT, 0);
        fdwSRS.top = new FormAttachment(wFileDialog, 2 * Const.MARGIN);
        fdwSRS.right = new FormAttachment(100, 0);
        wSRS.setLayoutData(fdwSRS);

        // Ok, Cancel buttons
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$
        setButtonPositions(new Button[]{wOK, wCancel}, Const.MARGIN, null);

        wbCheck = new Button(shell, SWT.PUSH);
        wbCheck.setText(BaseMessages.getString(PKG, "SetSRSDialog.CheckWKT.Label"));
        wbCheck.setEnabled(false);
        fdwbCheck = new FormData();
        fdwbCheck.left = new FormAttachment(wbSRIDWKT, 0);
        fdwbCheck.bottom = new FormAttachment(wOK, Const.MARGIN);
        wbCheck.setLayoutData(fdwbCheck);

        wWKT = new Text(shell, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
        wWKT.setText(BaseMessages.getString(PKG, "SetSRSDialog.InsertWKTHere.Label"));
        wWKT.setEnabled(false);
        fdwWKT = new FormData();
        fdwWKT.left = new FormAttachment(wbSRIDWKT, 0);
        fdwWKT.right = new FormAttachment(100, 0);
        fdwWKT.top = new FormAttachment(wSRS, Const.MARGIN);
        fdwWKT.bottom = new FormAttachment(wbCheck, -Const.MARGIN);
        wWKT.setLayoutData(fdwWKT);

        if (status == SetSRSMeta.STATUS_WKT) {
            wbSRIDWKT.setSelection(true);
            wSRS.setEnabled(true);
            wWKT.setEnabled(true);
            wbCheck.setEnabled(true);
            try {
                wWKT.setText((selectedSRS.getCRS()).toWKT());
            } catch (Exception e) {
                checkWKT();
            }
            checkWKT();
        }

        // Add listeners to the controls
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
        lsBrowse = new Listener() {
            public void handleEvent(Event e) {
                openFileDialog();
            }
        };
        lsCheck = new Listener() {
            public void handleEvent(Event e) {
                checkWKT();
            }
        };
        lsDef = new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                ok();
            }
        };
        lsMod = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                input.setChanged();
            }
        };
        SelectionListener lsField = new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }

            public void widgetSelected(SelectionEvent e) {
                fieldname = wField.getText();
                input.setChanged();
            }
        };

        wField.addSelectionListener(lsField);
        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);
        wbBrowse.addListener(SWT.Selection, lsBrowse);
        wbCheck.addListener(SWT.Selection, lsCheck);
        wStepname.addModifyListener(lsMod);
        wStepname.addSelectionListener(lsDef);

        wcSRID.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                String srid = ((CCombo) e.widget).getText();

                if (validateSRID(srid)) {
                    createComboSelectionSRS(srid);
                    setSRIDDescription(srid);
                    checkEPSG = true;
                } else {
                    wSRIDInfo.setText(BaseMessages.getString(PKG, "SetSRSDialog.Error.InvalidSRS"));
                    checkEPSG = false;
                }


                input.setChanged();
            }

        });

        wbSRIDCode.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            public void widgetSelected(SelectionEvent e) {
                setWbSRIDCodeEnabled();
                input.setChanged();
            }
        });

        wbSRIDFile.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            public void widgetSelected(SelectionEvent e) {
                setWbSRIDFileEnabled();
                input.setChanged();
            }
        });

        wbSRIDWKT.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            public void widgetSelected(SelectionEvent e) {
                setWbSRIDWKTEnabled();
                input.setChanged();
            }
        });

        ////////////////////////////////////////////////////////////////////////
        // Open the dialog and show it
        ////////////////////////////////////////////////////////////////////////

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter() {
            public void shellClosed(ShellEvent e) {
                cancel();
            }
        });

        input.setChanged(backupChanged);

        // Set the shell size, based upon previous time and provide min. bounds
        applyLookAndFeel(shell);
        setSize(shell, 350, 300, false);
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return stepname;
    }

    private boolean validateSRID(String srid) {
        int tempSrid = 0;
        boolean digitMatch = srid.matches("-[0-9]+|[0-9]*");
        if (digitMatch) {
            if (srid != "") {
                try {
                    tempSrid = Integer.parseInt(srid);
                } catch (Exception NumberFormatException) {
                    return false;
                }
            }
        }
        if (digitMatch && tempSrid <= Integer.MAX_VALUE && tempSrid >= Integer.MIN_VALUE) {
            return true;
        }
        return false;
    }


    private void setSRIDDescription(String srid) {
        if (selectedSRS.srid == "") {
            wSRIDInfo.setText(BaseMessages.getString(PKG, "SetSRSDialog.Error.NoSRSSelected"));
        } else {
            wSRIDInfo.setText(selectedSRS.authority + ": " + selectedSRS.description);
        }
    }

    /**
     * Fills the combo-box with the available geometry-fields that allow
     * a spatial reference system transformation.
     *
     * @param combo The combo-box to fill.
     */
    private void fillGeometryFieldsList(CCombo combo) {
        RowMetaInterface inputfields = null;
        try {
            inputfields = transMeta.getPrevStepFields(stepname);
        } catch (KettleException e) {
            inputfields = new RowMeta();
            new ErrorDialog(shell, "Error", "Could not find the fields", e);
        }

        String[] fieldNames = inputfields.getFieldNames();
        String[] fieldNamesAndTypes = inputfields.getFieldNamesAndTypes(0);
        TreeSet<String> geomFields = new TreeSet<String>();
        for (int i = 0; i < fieldNames.length; i++) {
            if (fieldNamesAndTypes[i].toLowerCase().contains("geometry")) {
                geomFields.add(fieldNames[i]);
            }
        }
        combo.setItems(geomFields.toArray(new String[]{}));

        // set the default selection from loaded repo/xml
        int existingSelection = combo.indexOf(fieldname);
        if (existingSelection > -1) {
            combo.select(existingSelection);
        }
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */
    private void loadMetadata(SetSRSMeta meta) {
        selectedSRS = meta.getSelectedSRS();
        status = meta.getActualStatus();
        path = meta.getActualPath();
        fieldname = meta.getFieldName();
    }

    /**
     * Copy information from the dialog fields to the meta-data.
     *
     * @param meta The {@link SRSTransformationMeta} object to write.
     * @throws KettleStepException
     */
    private void storeMetadata(SetSRSMeta meta) throws KettleStepException {
        if (meta.hasChanged()) {
            meta.setSelectedSRSMeta(selectedSRS);
            meta.setActualStatus(status);
            meta.setActualPath(path);
            meta.setFieldName(fieldname);
            stepname = wStepname.getText();    // return value for open()
            meta.setChanged();
        }
    }


    private void cancel() {
        dispose(); // close in any case, no storage of SRS
    }

    private void ok() {
        try {
            if (fieldname.equals("")) {
                throw new KettleStepException(BaseMessages.getString(PKG, "SetSRSDialog.Error.FieldMustBeProvided"));
            }
            if (status == SetSRSMeta.STATUS_EPSGCODE) {
                String srid = wcSRID.getText();
                if (validateSRID(srid)) {
                    createComboSelectionSRS(srid);
                    storeMetadata(input);
                    dispose();
                } else {
                    throw new KettleStepException(BaseMessages.getString(PKG, "SetSRSDialog.Error.EpsgCodeMustBeProvided"));
                }
            } else if (status == SetSRSMeta.STATUS_FILE) {
                createSRSFromSelectedFile(path);
                storeMetadata(input);
                dispose();
            } else {
                if (checkWKT()) {
                    storeMetadata(input);
                    dispose();
                } else {
                    checkWKT();
                    return;
                }
            }
        } catch (KettleStepException e) {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Warning"), "Set SRS step error", e);
            //dispose();
        }
    }

    /**
     * Creates the SRS from the selected SRID
     *
     * @param srid The srid from the selected Entry in Combo
     */
    private void createComboSelectionSRS(String srid) {
        String authority_epsg = Citations.getIdentifier(Citations.EPSG);
        CRSAuthorityFactory factory = ReferencingFactoryFinder.getCRSAuthorityFactory(authority_epsg, null);
        try {
            if (srid == "") {
                selectedSRS = SRS.UNKNOWN;
            } else {
                selectedSRS = new SRS(authority_epsg, srid, factory.getDescriptionText(srid).toString());
            }
        } catch (Exception ex) {
            selectedSRS = new SRS("GeoKettle", srid, "Custom");
        }
    }

    private void openFileDialog() {
        FileDialog fileDialog = new FileDialog(shell, SWT.NULL);
        fileDialog.setText("Select File for SRID");
        path = fileDialog.open();
        createSRSFromSelectedFile(path);
    }

    /**
     * Creates the SRS from the description in the file
     *
     * @param path Path of the file with SRS description
     */
    private void createSRSFromSelectedFile(String path) {
        if (path != null) {
            File file = new File(path);
            selectedSRS = new SRS(file);
            wFileDialog.setText(selectedSRS.description);
            input.setChanged();
        }
    }

    /**
     * Checks if the WKT-String is valid
     *
     * @return true if the tested WKT is valid
     */

    private boolean checkWKT() {
        wkt = wWKT.getText();

        red = new org.eclipse.swt.graphics.Color(display, 254, 107, 107);
        green = new org.eclipse.swt.graphics.Color(display, 172, 235, 137);

        try {
            selectedSRS = new SRS(wkt);
            wSRS.setText(selectedSRS.description);
            wSRS.setBackground(green);
            input.setChanged();
            return true;

        } catch (Exception e) {
            wSRS.setBackground(red);
            wSRS.setText("Invalid WKT");
            //Dummy SRS in case of exception
            selectedSRS = SRS.UNKNOWN;
            return false;
        }

    }


    private void setWbSRIDCodeEnabled() {
        if (status != SetSRSMeta.STATUS_EPSGCODE) {
            status = SetSRSMeta.STATUS_EPSGCODE;
            wcSRID.setEnabled(true);
            wSRIDInfo.setEnabled(true);
            wSRS.setEnabled(false);
            wWKT.setEnabled(false);
            wbCheck.setEnabled(false);
            wbBrowse.setEnabled(false);
            wFileDialog.setEnabled(false);
        } else {
            selectedSRS = SRS.UNKNOWN;
        }

    }

    private void setWbSRIDFileEnabled() {
        if (status != SetSRSMeta.STATUS_FILE) {
            status = SetSRSMeta.STATUS_FILE;
            wcSRID.setEnabled(false);
            wSRIDInfo.setEnabled(false);
            wSRS.setEnabled(false);
            wWKT.setEnabled(false);
            wbCheck.setEnabled(false);
            wbBrowse.setEnabled(true);
            wFileDialog.setEnabled(true);
        } else {
            selectedSRS = SRS.UNKNOWN;
        }
    }

    private void setWbSRIDWKTEnabled() {
        if (status != SetSRSMeta.STATUS_WKT) {
            status = SetSRSMeta.STATUS_WKT;
            wcSRID.setEnabled(false);
            wSRIDInfo.setEnabled(false);
            wSRS.setEnabled(true);
            wWKT.setEnabled(true);
            wbCheck.setEnabled(true);
            wbBrowse.setEnabled(false);
            wFileDialog.setEnabled(false);
        } else {
            selectedSRS = SRS.UNKNOWN;
        }
    }

    /**
     * Applies the look-and-feel to all {@link Composite}s and {@link Control}s
     * recursively.
     *
     * @param parent The parent {@link Composite}.
     */
    private void applyLookAndFeel(Composite parent) {
        props.setLook(parent);
        for (Control c : parent.getChildren()) {
            props.setLook(c);
            if (c instanceof Composite)
                applyLookAndFeel((Composite) c);
        }
    }
}
