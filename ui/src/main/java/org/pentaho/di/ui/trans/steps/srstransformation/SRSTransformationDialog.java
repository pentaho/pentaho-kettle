/**
 * 
 */
package org.pentaho.di.ui.trans.steps.srstransformation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.geospatial.SRS;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.setsrs.SRSList;
import org.pentaho.di.trans.steps.srstransformation.SRSTransformation;
import org.pentaho.di.trans.steps.srstransformation.SRSTransformationMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import java.util.TreeSet;

/**
 * Dialog for the {@link SRSTransformation} step.
 * 
 * @author phobus, sgoldinger
 * @since 29-oct-2008
 */
public class SRSTransformationDialog extends BaseStepDialog implements StepDialogInterface {

	private static Class<?> PKG = SRSTransformation.class;

	// Constants
	private static final int LABEL_OFFSET = 5;

	// Other properties
	private SRSTransformationMeta input;
	private SRS sourceSRS, targetSRS, backupSourceSRS, backupTargetSRS;
	private String fieldname, backupFieldname;
	private int sourceGUIStatus, targetGUIStatus, backupSourceGUIStatus, backupTargetGUIStatus;
	
	// Controls
	private SRSPane wSourceSRSPane, wTargetSRSPane;
	private SashForm wSashForm;
	
	
	public SRSTransformationDialog(Shell parent, Object baseStepMeta, TransMeta transMeta, String stepname) {
		super(parent, (BaseStepMeta) baseStepMeta, transMeta, stepname);
		input = (SRSTransformationMeta) baseStepMeta;
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
		layout.marginWidth  = Const.FORM_MARGIN * 2;
		layout.marginHeight = Const.FORM_MARGIN * 2;
		
		shell.setLayout(layout);
		shell.setText(BaseMessages.getString(PKG,"SRSTransformationDialog.Shell.Title")); //$NON-NLS-1$
		
		////////////////////////////////////////////////////////////////////////
		// Adding UI components and listeners
		////////////////////////////////////////////////////////////////////////
		
		// Buttons
		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG,"System.Button.OK")); //$NON-NLS-1$
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG,"System.Button.Cancel")); //$NON-NLS-1$
		setButtonPositions(new Button[] { wOK, wCancel }, 0, null);
		
		// Stepname
		wlStepname = new Label(shell, SWT.LEFT);
		wlStepname.setText(BaseMessages.getString(PKG,"System.Label.StepName")); //$NON-NLS-1$
		fdlStepname = new FormData();
		fdlStepname.left = new FormAttachment(0, 0);
		fdlStepname.right= new FormAttachment(25, 0);
		fdlStepname.top = new FormAttachment(0, LABEL_OFFSET);
		wlStepname.setLayoutData(fdlStepname);
		
		wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		wStepname.setText(stepname);
		fdStepname = new FormData();
		fdStepname.left = new FormAttachment(wlStepname, 0);
		fdStepname.top = new FormAttachment(0, 0);
		fdStepname.right = new FormAttachment(100, 0);
		wStepname.setLayoutData(fdStepname);
		
		// Field
		Label wlField = new Label(shell, SWT.LEFT);
		wlField.setText(BaseMessages.getString(PKG,"SRSTransformationDialog.FieldToTransform.Label"));
		FormData fdlField = new FormData();
		fdlField.left = new FormAttachment(0, 0);
		fdlField.top = new FormAttachment(wStepname, Const.MARGIN*2);
		fdlField.right = new FormAttachment(25, 0);
		wlField.setLayoutData(fdlField);
		
		final CCombo wField = new CCombo(shell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
		FormData fdField = new FormData();
		fdField.left = new FormAttachment(wlField, Const.MARGIN);
		fdField.top = new FormAttachment(wStepname, 0);
		wField.setLayoutData(fdField);
		fillGeometryFieldsList(wField);
		
		Label wlFieldInfo = new Label(shell, SWT.LEFT);
		wlFieldInfo.setText(BaseMessages.getString(PKG,"SRSTransformationDialog.OnlyGeomAreShown.Label"));
		FormData fdlFieldInfo = new FormData();
		fdlFieldInfo.left = new FormAttachment(wField, Const.MARGIN);
		fdlFieldInfo.top = new FormAttachment(wStepname, Const.MARGIN*2);
		fdlFieldInfo.right = new FormAttachment(100, 0);
		wlFieldInfo.setLayoutData(fdlFieldInfo);
		
		// Auto-detect source SRS
		final Button wbAutoDetect = new Button(shell, SWT.CHECK);
		wbAutoDetect.setText(BaseMessages.getString(PKG,"SRSTransformationDialog.Auto.Label")); //$NON-NLS-1$
		FormData fdAutoDetect = new FormData();
		fdAutoDetect.left = new FormAttachment(0, 0);
		fdAutoDetect.top = new FormAttachment(wField, Const.MARGIN);
		fdAutoDetect.right = new FormAttachment(100, 0);
		wbAutoDetect.setLayoutData(fdAutoDetect);
		if (sourceGUIStatus == SRSTransformationMeta.STATUS_AUTO)
			wbAutoDetect.setSelection(true);
		
		Composite wMainPane = new Composite(shell, SWT.NONE);
		FormData fdMainPane = new FormData();
		fdMainPane.left = new FormAttachment(0, 0);
		fdMainPane.right = new FormAttachment(100, 0);
		fdMainPane.top = new FormAttachment(wbAutoDetect, Const.MARGIN*3);
		fdMainPane.bottom = new FormAttachment(wOK, 0);
		wMainPane.setLayoutData(fdMainPane);
		wMainPane.setLayout(new FillLayout());
		
		// Source- and Target-Pane in a SashForm
		wSashForm = new SashForm(wMainPane, SWT.HORIZONTAL);
		SRSList treeData = new SRSList();	// Thread that collects the tree data for all SRS
		wSourceSRSPane = new SRSPane(wSashForm, treeData, BaseMessages.getString(PKG,"SRSTransformationDialog.SourceSRS.Label"), sourceSRS, input, sourceGUIStatus); //$NON-NLS-1$
		wTargetSRSPane = new SRSPane(wSashForm, treeData,  BaseMessages.getString(PKG,"SRSTransformationDialog.TargetSRS.Label"), targetSRS, input, targetGUIStatus); //$NON-NLS-1$
		wSashForm.setWeights(new int[]{1, 1});
		
		// Add listeners to the controls
		lsCancel = new Listener() { public void handleEvent(Event e) { cancel(); } };
		lsOK	 = new Listener() { public void handleEvent(Event e) { ok(); } };
		lsDef 	 = new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		SelectionListener lsField = new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }
			public void widgetSelected(SelectionEvent e) {
				fieldname = wField.getText();
				if (sourceGUIStatus == SRSTransformationMeta.STATUS_AUTO)
					setStatus(SRSTransformationMeta.STATUS_AUTO, targetGUIStatus);
				input.setChanged();
			}
		};
		SelectionListener lsAuto = new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }
			public void widgetSelected(SelectionEvent e){
				boolean auto = wbAutoDetect.getSelection();
				if (auto)
					setStatus(SRSTransformationMeta.STATUS_AUTO, wTargetSRSPane.getStatus());
				else
					setStatus(SRSTransformationMeta.STATUS_EXISTING, wTargetSRSPane.getStatus());
			}
		};
		ModifyListener lsMod = new ModifyListener() { public void modifyText(ModifyEvent e) { input.setChanged();}};
		wCancel.addListener(SWT.Selection, lsCancel);
		wOK.addListener(	SWT.Selection, lsOK);
		wStepname.addModifyListener(lsMod);
		wStepname.addSelectionListener(lsDef);
		wField.addSelectionListener(lsField);
		wbAutoDetect.addSelectionListener(lsAuto);
		
		setStatus(sourceGUIStatus, targetGUIStatus);
		
		////////////////////////////////////////////////////////////////////////
		// Open the dialog and show it
		////////////////////////////////////////////////////////////////////////
		
		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); }});
		
		input.setChanged(backupChanged);
		
		// Set the shell size, based upon previous time and provide min. bounds
		applyLookAndFeel(shell);
		setSize(shell, 450, 350, false);
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
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
			new ErrorDialog(shell,"Error", "Could not find the fields", e);
		}
		
		String[] fieldNames			= inputfields.getFieldNames();
		String[] fieldNamesAndTypes = inputfields.getFieldNamesAndTypes(0);
		TreeSet<String> geomFields  = new TreeSet<String>();
		for (int i=0; i < fieldNames.length; i++) {
			if (fieldNamesAndTypes[i].toLowerCase().contains("geometry")) {
				geomFields.add(fieldNames[i]);
			}
		}
		combo.setItems(geomFields.toArray(new String[]{}));
		
		// set the default selection from loaded repo/xml
		int existingSelection = combo.indexOf(fieldname);
		if (existingSelection > -1)
			combo.select(existingSelection);
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */ 
	private void loadMetadata(SRSTransformationMeta meta) {
		backupSourceGUIStatus = sourceGUIStatus = meta.getSourceGUIStatus();
		backupTargetGUIStatus = targetGUIStatus = meta.getTargetGUIStatus();
		backupFieldname = fieldname = meta.getFieldName();
		try {
			sourceSRS = sourceGUIStatus == SRSTransformationMeta.STATUS_AUTO
					  ? autodetectSourceSRS() 
					  : meta.getSourceSRS(transMeta.getPrevStepFields(stepname));
		} catch (KettleStepException e) {
			sourceSRS = SRS.UNKNOWN;
			new ErrorDialog(shell, BaseMessages.getString(PKG,"SRSTransformationDialog.Error"), BaseMessages.getString(PKG,"SRSTransformationDialog.CouldNotRestoreSourceSRS"), e);
		}
		targetSRS = meta.getTargetSRS();
		
		backupChanged = meta.hasChanged();
		backupSourceSRS = sourceSRS;
		backupTargetSRS = targetSRS;
	}
	
	/**
	 * Copy information from the dialog fields to the meta-data.
	 * 
	 * @param meta The {@link SRSTransformationMeta} object to write.
	 * @throws KettleStepException
	 */
	private void storeMetadata(SRSTransformationMeta meta) throws KettleStepException {
		if (meta.hasChanged()) {
			meta.setFieldName(fieldname);
			meta.setSourceGUIStatus(wSourceSRSPane.getStatus());
			meta.setTargetGUIStatus(wTargetSRSPane.getStatus());
			
			SRS src = sourceGUIStatus == SRSTransformationMeta.STATUS_AUTO
						? autodetectSourceSRS() 
						: wSourceSRSPane.getSRS();
			meta.setSourceSRS(src);
			meta.setTargetSRS(wTargetSRSPane.getSRS());
			stepname = wStepname.getText();	// return value for open()
			meta.setChanged();
		}
	}
	
	private void backupMetadata(SRSTransformationMeta meta) {
		stepname = null;
		fieldname = backupFieldname;
		sourceSRS = backupSourceSRS;
		targetSRS = backupTargetSRS;
		sourceGUIStatus = backupSourceGUIStatus;
		targetGUIStatus = backupTargetGUIStatus;
		meta.setChanged(backupChanged);
	}
	
	private void cancel() {
		try {
			waitForTreeInitialization();
			backupMetadata(input);
		} catch (KettleStepException e) {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
			mb.setMessage(e.toString());
			mb.setText(BaseMessages.getString(PKG,"System.Warning")); //$NON-NLS-1$
			mb.open();
		}
		dispose(); // close in any case
	}
	
	private void ok() {
		try {
			waitForTreeInitialization();
			boolean checkTarget = true, checkSource = true;
			if (wSourceSRSPane.getStatus() == SRSTransformationMeta.STATUS_WKT)
				checkSource = wSourceSRSPane.checkWKT();
			if (wTargetSRSPane.getStatus() == SRSTransformationMeta.STATUS_WKT)
				checkTarget = wTargetSRSPane.checkWKT();
			if (!checkSource || !checkTarget)
				return;
			if (fieldname.equals(""))
				throw new KettleStepException(BaseMessages.getString(PKG,"SRSTransformationDialog.FieldNameMustBeProvided"));
			storeMetadata(input);
		} catch (KettleStepException e) {
			new ErrorDialog(shell, BaseMessages.getString(PKG,"System.Warning"), "SRS Transformation step error", e); //$NON-NLS-1$ //$NON-NLS-2$
		}
		dispose(); // close in any case
	}
	
	/**
	 * Waits for the {@link SRSPane}-source and {@link SRSPane}-target to end.
	 * @throws KettleStepException
	 */
	private void waitForTreeInitialization() throws KettleStepException {
		try {
			wSourceSRSPane.thread.join();
			wTargetSRSPane.thread.join();
		} catch (InterruptedException e) {
			throw new KettleStepException("A SRS multi-threading problem occured!"); // this should never happen
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
			if (c.getData(SRSPane.IGNORE_LOOK_AND_FEEL) == null || !((Boolean) c.getData("ignorelookandfeel")))
				props.setLook(c);			
			if (c instanceof Composite)
				applyLookAndFeel((Composite)c);
		}
	}
	
	/**
	 * Stes the status for the GUI.
	 * 
	 * @param newSourceStatus Status for the source-SRS pane.
	 * @param newTargetStatus Status for the target-SRS pane.
	 */
	private void setStatus(int newSourceStatus, int newTargetStatus) {
		if (newSourceStatus == SRSTransformationMeta.STATUS_AUTO){
			wSourceSRSPane.setSRS(autodetectSourceSRS());
			wSourceSRSPane.setTableEnabled(false);
		}else
			wSourceSRSPane.setTableEnabled(true);
		wSourceSRSPane.setStatus(newSourceStatus);
		wTargetSRSPane.setStatus(newTargetStatus);
		sourceGUIStatus = newSourceStatus;
		targetGUIStatus = newTargetStatus;
	}

	/**
	 * Automatically detects the SRS from the metadata changed by a previous step.
	 * 
	 * @return The {@link SRS} from a previous step.
	 */
	private SRS autodetectSourceSRS() {
		SRS resultMeta = SRS.UNKNOWN;
		try {
			RowMetaInterface inputfields = transMeta.getPrevStepFields(stepname);
			// Find the ValueMeta of the field
			int idx = inputfields.indexOfValue(fieldname);
			if (idx >= 0) {
				// This is the value we need to get the SRS from
				ValueMetaInterface vmi = inputfields.getValueMeta(idx);
				resultMeta = vmi.getGeometrySRS();
			}
		} catch (KettleException ke) { }
		return resultMeta;
	}
}
