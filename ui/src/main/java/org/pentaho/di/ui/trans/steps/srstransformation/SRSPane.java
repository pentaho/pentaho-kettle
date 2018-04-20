package org.pentaho.di.ui.trans.steps.srstransformation;

// TODO: i18n

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.geospatial.SRS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.steps.setsrs.SRSList;
import org.pentaho.di.trans.steps.srstransformation.SRSTransformation;
import org.pentaho.di.trans.steps.srstransformation.SRSTransformationMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * In this pane the user can select a SRS from a list or he can define
 * a new one with a WKT string.
 * 
 * @author phobus, sgoldinger
 * @since 12-nov-2008
 */
public class SRSPane extends Composite {

	private static Class<?> PKG = SRSTransformation.class;

	// Constants
	public static final String IGNORE_LOOK_AND_FEEL = "ignorelookandfeel";
	
	// Properties
	private SRSTransformationMeta meta;
	private SRS selectedSRS;
	private final SRSList treeData;
	private int status;
	SRSTreeView thread;
	
	// Controls
	private SashForm wSRSSashForm;
	private Composite wSRSExistingPane, wSRSNewFromWKTPane;
	private Button wbSRSExisting, wbSRSNewFromWKT, wCheck, wDetail, wbClose, wbSearch;
	private Text wSRS, wWKT, wSearch;
	private Listener lsCheck, lsDetail, lsClose;
	private Color green, red, white, markYellow;
	private Display display = Display.getDefault();
	private String wkt;
	public Tree tree;
	private boolean ascending = true;
	private Map<String, String> allSRS;
	private Map<String, SRS> dataMap;
	
	/**
	 * Create a new {@link SRSPane} composite.
	 * 
	 * @param parent The parent {@link Composite}.
	 * @param treeData The {@link SRSList} that delivers the data for the tree.
	 * @param text The text of the group field.
	 * @param selection Preselected item in tree.
	 * @param meta The input.
	 */
	public SRSPane(Composite parent, SRSList treeData, String text, SRS selection, SRSTransformationMeta meta, int status) {
		super(parent, SWT.NONE);
		this.selectedSRS = selection;
		this.treeData = treeData;
		this.meta = meta;
		setLayout(new FillLayout());
		createControls(text);
		setStatus(status);
		
		// GUI hack
		if (status == SRSTransformationMeta.STATUS_WKT)
			wbSRSNewFromWKT.setSelection(true);
		else
			wbSRSExisting.setSelection(true);
	}
	
	/**
	 * Create all controls for this pane.
	 * 
	 * @param text The text which is displayed on top of the group.
	 */
	private void createControls(String text) {
		white = new org.eclipse.swt.graphics.Color(display, 255, 255, 255);
		// Group for transformation settings
		Group wgTransSettings = new Group(this, SWT.SHADOW_ETCHED_IN);
		wgTransSettings.setText(text);
		GridLayoutFactory.fillDefaults().numColumns(4).generateLayout(wgTransSettings);

		// Radio buttons
		wbSRSExisting = new Button(wgTransSettings, SWT.RADIO);
		wbSRSExisting.setText(BaseMessages.getString(PKG,"SRSTransformationDialog.Existing.Label")); //$NON-NLS-1$
		wbSRSNewFromWKT = new Button(wgTransSettings, SWT.RADIO);
		wbSRSNewFromWKT.setText(BaseMessages.getString(PKG,"SRSTransformationDialog.WKT.Label")); //$NON-NLS-1$
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.BEGINNING;
		wbSRSNewFromWKT.setLayoutData(gridData);

		//Search button
		wbSearch = new Button(wgTransSettings, SWT.RIGHT);
		wbSearch.setText(BaseMessages.getString(PKG,"SRSTransformationDialog.Search.Label"));
		GridData gridDataButton = new GridData();
		gridDataButton.grabExcessHorizontalSpace = true;
		gridDataButton.horizontalAlignment = GridData.END;
		wbSearch.setLayoutData(gridDataButton);

		//search text
		wSearch = new Text(wgTransSettings, SWT.LEFT | SWT.BORDER);
		GridData gridDataText = new GridData();
		gridDataText.grabExcessHorizontalSpace = true;
		gridDataText.horizontalAlignment = GridData.FILL;
	    wSearch.setLayoutData(gridDataText);

		wSRS = new Text(wgTransSettings, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(4, 1).applyTo(wSRS);

		// Sash
		wSRSSashForm = new SashForm(wgTransSettings, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, true).span(4, 1).applyTo(wSRSSashForm);

		// Existing pane
		wSRSExistingPane = new Composite(wSRSSashForm, SWT.BORDER);
		GridLayoutFactory.fillDefaults().generateLayout(wSRSExistingPane);

		// New from WKT pane
		wSRSNewFromWKTPane = new Composite(wSRSSashForm, SWT.NONE);
		GridLayoutFactory.fillDefaults().generateLayout(wSRSNewFromWKTPane);

		wSRSSashForm.setWeights(new int[]{1, 1});
		wSRSSashForm.setMaximizedControl(wSRSExistingPane);

		// The WKT area
		wWKT = new Text(wSRSNewFromWKTPane, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
		wWKT.setText(BaseMessages.getString(PKG,"SRSTransformationDialog.WKT.Text"));
		GridDataFactory.fillDefaults().grab(true, true).span(4, 1).applyTo(wWKT);

		// Listeners
		wbSRSExisting.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }
			public void widgetSelected(SelectionEvent e) {
				setStatus(SRSTransformationMeta.STATUS_EXISTING);
			}
		});
		wbSRSNewFromWKT.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }
			public void widgetSelected(SelectionEvent e) {
				setStatus(SRSTransformationMeta.STATUS_WKT);
			}
		});
		wbSearch.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }
			public void widgetSelected(SelectionEvent e) {
				 searchSRSMap();
			}
		});

		// Selection list of all possible SRS
		createSRSTree(wSRSExistingPane);

		// Buttons
		wCheck = new Button(wSRSNewFromWKTPane, SWT.PUSH);
		wCheck.setText(BaseMessages.getString(PKG,"SRSTransformationDialog.WKT.Check"));

		wDetail = new Button(wSRSExistingPane, SWT.PUSH);
		wDetail.setText(BaseMessages.getString(PKG,"SRSTransformationDialog.Details.Label"));

		//Add Button Listener
		lsCheck = new Listener() { public void handleEvent(Event e) { checkWKT();}};
		wCheck.addListener(	SWT.Selection, lsCheck);

		lsDetail = new Listener() {
			public void handleEvent(Event e) {
				if (selectedSRS == null || selectedSRS.equals(SRS.UNKNOWN))
					wSRS.setText(BaseMessages.getString(PKG,"SRSTransformationDialog.NoSRSSelected"));
				else
					showSrsDetail();
			}
		};
		wDetail.addListener( SWT.Selection, lsDetail);
	}

	public void buildNewItem(Map.Entry<String, String> entry){
		String[] values = {entry.getKey(), entry.getValue()};
    	TreeItem item = new TreeItem(tree, SWT.NONE);
    	item.setText(values);
	}

	public void searchSRSMap(){
		tree.removeAll();
		String text = wSearch.getText().toLowerCase();
		if(Const.isEmpty(text)){
			for (Map.Entry<String, String> entry : allSRS.entrySet()) {
				buildNewItem(entry);
			}
		}else{
			for (Map.Entry<String, String> entry : allSRS.entrySet()) {
			    if(entry.getKey().toLowerCase().indexOf(text) != -1 || entry.getValue().toLowerCase().indexOf(text) != -1)
			    	buildNewItem(entry);
			}
		}
	}

	/**
	 * @return True, if the text area contains a valid WKT string.
	 */
	public boolean checkWKT() {
		wkt = wWKT.getText();

		red = new org.eclipse.swt.graphics.Color(display, 254, 107, 107);
		green = new org.eclipse.swt.graphics.Color(display, 172, 235, 137);

		try {
			selectedSRS = new SRS(wkt);
			wSRS.setText(selectedSRS.description);
			wSRS.setBackground(green);
			meta.setChanged();
			return true;

		} catch (Exception e) {
			wSRS.setBackground(red);
			wSRS.setText(BaseMessages.getString(PKG,"SRSTransformationDialog.InvalidWKT"));
			// Dummy SRS in case of exception
			selectedSRS = SRS.UNKNOWN;
			tree.deselectAll();
			return false;
		}
	}

	/**
	 * Shows a dialog with the WKT representation of a selected
	 * spatial reference system.
	 */
	public void showSrsDetail() {
		final Shell detailShell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		detailShell.setText(BaseMessages.getString(PKG,"SRSTransformationDialog.WKTDetails"));
		detailShell.setSize (550, 400);

		FormLayout formLayout = new FormLayout();
		detailShell.setLayout(formLayout);

		StyledText wDetailWkt = new StyledText(detailShell, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		wDetailWkt.setEditable(false);
		try {
			wDetailWkt.setText (selectedSRS.getCRS().toWKT());
		} catch (Exception e) {
			wDetailWkt.setText(BaseMessages.getString(PKG,"SRSTransformationDialog.UnsupportedSRS"));
		}

		markYellow = new org.eclipse.swt.graphics.Color(display, 255, 241, 173);
		int markAuth = wDetailWkt.getText().lastIndexOf("AUTHORITY");
		int markSpher = wDetailWkt.getText().indexOf("SPHEROID");
		int markDatum = wDetailWkt.getText().indexOf("DATUM");
		
		if (markAuth != -1) {
			StyleRange markAuthStyle = new StyleRange();
			markAuthStyle.start = markAuth;
			markAuthStyle.length = 9;
			markAuthStyle.background = markYellow;
			wDetailWkt.setStyleRange(markAuthStyle);
		}
		
		if (markSpher != -1) {
			StyleRange markSpherStyle = new StyleRange();
			markSpherStyle.start = markSpher;
			markSpherStyle.length = 8;
			markSpherStyle.background = markYellow;
			wDetailWkt.setStyleRange(markSpherStyle);
		}
		
		if (markDatum != -1) {
			StyleRange markDatumStyle = new StyleRange();
			markDatumStyle.start = markDatum;
			markDatumStyle.length = 5;
			markDatumStyle.background = markYellow;
			wDetailWkt.setStyleRange(markDatumStyle);
		}

		
		wbClose = new Button(detailShell, SWT.PUSH);
		wbClose.setText (BaseMessages.getString(PKG,"SRSTransformationDialog.Close"));
		lsClose = new Listener() { public void handleEvent(Event e) {detailShell.close();}};
		wbClose.addListener( SWT.Selection, lsClose);
		
		FormData formDataWbClose = new FormData();
	    formDataWbClose.left = new FormAttachment(0, 10);
	    formDataWbClose.bottom = new FormAttachment(100, -10);
	    wbClose.setLayoutData(formDataWbClose);
	    
	    FormData formDataWkt = new FormData();
	    formDataWkt.top = new FormAttachment(0, 10);
	    formDataWkt.left = new FormAttachment(0, 10);
	    formDataWkt.right = new FormAttachment(100, -10);
	    formDataWkt.bottom = new FormAttachment(wbClose, -10);
	    wDetailWkt.setLayoutData(formDataWkt);
		
		detailShell.open();
	}
	
	public int getStatus(){
		return this.status;
	}
	
	public void setTableEnabled(boolean isAuto){
		tree.setEnabled(isAuto);
		wSearch.setEnabled(isAuto);
		wbSearch.setEnabled(isAuto);
	}
	
	public void setStatus(int newStatus) {
		wSRS.setText(selectedSRS.description);
		wSRS.setBackground(white);
		
		switch(newStatus) {
		case SRSTransformationMeta.STATUS_AUTO:
			wSRSSashForm.setMaximizedControl(wSRSExistingPane);
			thread.markSelection(selectedSRS.description);
			wSRS.setEditable(false);
			wSRS.setEnabled(false);
			wbSRSExisting.setEnabled(false);
			wbSRSNewFromWKT.setEnabled(false);
			wbSRSExisting.setSelection(true);
			wbSRSNewFromWKT.setSelection(false);
			break;
		case SRSTransformationMeta.STATUS_EXISTING:
			wSRSSashForm.setMaximizedControl(wSRSExistingPane);
			thread.markSelection(selectedSRS.description);
			wSRS.setEditable(true);
			wSRS.setEnabled(true);
			wSearch.setEditable(true);
			wSearch.setEnabled(true);
			wbSearch.setEnabled(true);
			wbSRSExisting.setEnabled(true);
			wbSRSNewFromWKT.setEnabled(true);
			break;
		case SRSTransformationMeta.STATUS_WKT:
			wSRSSashForm.setMaximizedControl(wSRSNewFromWKTPane);
			wSRS.setEditable(false);
			wSRS.setEnabled(true);
			wSearch.setEditable(false);
			wSearch.setEnabled(false);
			wbSearch.setEnabled(false);
			wbSRSExisting.setEnabled(true);
			wbSRSNewFromWKT.setEnabled(true);
			
			if (this.status != SRSTransformationMeta.STATUS_WKT) {
				try {
					if (!selectedSRS.equals(SRS.UNKNOWN))
						wWKT.setText((selectedSRS.getCRS()).toWKT());
				} catch (KettleException ke) {
					new ErrorDialog(this.getShell(), BaseMessages.getString(PKG,"SRSTransformationDialog.Error"),  BaseMessages.getString(PKG,"SRSTransformationDialog.CouldNotCreateWKTError"), ke);
				}
			}
			
			if (this.status == SRSTransformationMeta.STATUS_WKT)
				checkWKT();
			break;
		}
		
		this.status = newStatus;
	}

	public SRS getSRS() {
		return selectedSRS;
	}
	
	public void setSRS(SRS srs) {
		this.selectedSRS = srs;
	}

	/**
	 * Creates a tree containing two root-nodes, containing favorite SRS
	 * and a list of all available SRS.
	 * 
	 * @param parent The parent composite.
	 */
	private void createSRSTree(final Composite parent) {
		tree = new Tree(parent, SWT.FULL_SELECTION | SWT.SINGLE);
		tree.setLinesVisible(true);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));
		tree.setHeaderVisible(true);

		TreeColumn tc1 = new TreeColumn(tree, SWT.NONE);
		tc1.setWidth(300);
		tc1.setText(BaseMessages.getString(PKG,"SRSTransformationDialog.SpatialRefSystem"));
		
		TreeColumn tc2 = new TreeColumn(tree, SWT.NONE);
		tc2.setWidth(100);
		tc2.setText(BaseMessages.getString(PKG,"SRSTransformationDialog.Code"));
		tc2.setResizable(true);
		tc2.setMoveable(false);

	    Listener sortListener = new Listener() {
	        public void handleEvent(Event e) {
	        	TreeColumn column = (TreeColumn) e.widget;
	        	ascending = !ascending;

	        	Map<String, String> srsMap = new HashMap<String, String>(tree.getItemCount());
	        	
	    		for (TreeItem item : tree.getItems()){
	    			srsMap.put(item.getText(0), item.getText(1));
	    		}
	    		
	        	tree.removeAll();	 
	        	
	        	Map<String, String> sortedSRS;
	        	
	        	class KeyComparatorAsc implements Comparator<String> {
	        		public int compare(String i1,String i2){
	        			return i1.compareToIgnoreCase(i2);
	        		}
	        	}
	        	
	        	class KeyComparatorDesc implements Comparator<String> {
	        		public int compare(String i1,String i2){
	        			return i2.compareToIgnoreCase(i1);
	        		}
	        	}
	        	
	        	class ValueComparatorAsc implements Comparator<String> {
	        		Map<String, String> base;
	        		public ValueComparatorAsc(Map<String, String>  base) {
	        		    this.base = base;
	        		}
	        		public int compare(String i1,String i2){
	        			return base.get(i1).compareToIgnoreCase(base.get(i2));
	        		}
	        	}
	        	
	        	class ValueComparatorDesc implements Comparator<String> {
	        		Map<String, String> base;
	        		public ValueComparatorDesc(Map<String, String>  base) {
	        		    this.base = base;
	        		}
	        		public int compare(String i1,String i2){
	        			return base.get(i2).compareToIgnoreCase(base.get(i1));
	        		}
	        	}
	        	
	        	if(tree.indexOf(column) == 0) //sorting keys
	        		sortedSRS = ascending ?  new TreeMap<String, String>(new KeyComparatorAsc()) : new TreeMap<String, String>(new KeyComparatorDesc());
	        	else //sorting keys
	        		sortedSRS = ascending ?  new TreeMap<String, String>(new ValueComparatorAsc(srsMap)) : new TreeMap<String, String>(new ValueComparatorDesc(srsMap));		        				        	
	        	
	        	sortedSRS.putAll(srsMap);
	        	
				for (Map.Entry<String, String> entry : sortedSRS.entrySet()) {		    	
				    buildNewItem(entry);				    
				}
				
	        	tree.setSortColumn(column);
	        	tree.setSortDirection(ascending? SWT.UP: SWT.DOWN);
	        }
	    };
	        
	    tc1.addListener(SWT.Selection, sortListener);
	    tc2.addListener(SWT.Selection, sortListener);
		
	    tree.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { makeSelection(e); }
			public void widgetSelected(SelectionEvent e) { makeSelection(e); }
			private void makeSelection(SelectionEvent event) {
				TreeItem node = (TreeItem) event.item;
				if(!node.isDisposed()) { 
					SRS entry = dataMap.get(node.getText(0));
					if (entry != null) {
						selectedSRS = entry;
						wSRS.setText(entry.description);
						meta.setChanged();
					}
				}
			}
		});
		
		thread = new SRSTreeView(treeData, tree, selectedSRS.description);
		
		allSRS = thread.getSRSMap();
		dataMap = thread.getDataMap();
	}
}
