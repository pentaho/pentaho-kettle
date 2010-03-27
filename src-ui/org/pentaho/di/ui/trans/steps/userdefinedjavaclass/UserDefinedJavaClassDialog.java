/***** BEGIN LICENSE BLOCK *****
The contents of this package are subject to the GNU Lesser Public License
 (the "License"); you may not use this file except in compliance with
the License. You may obtain a copy of the License at
http://www.gnu.org/licenses/lgpl-2.1.txt

Software distributed under the License is distributed on an "AS IS" basis,
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
for the specific language governing rights and limitations under the
License.

The Original Code is Pentaho Data Integration

The Initial Developer of the Original Code is
Pentaho Corporation
Portions created by the Initial Developer are Copyright (C) 2010
the Initial Developer. All Rights Reserved.

Contributor(s):
Matt Casters mcaster@pentaho.com
Daniel Einspanjer deinspanjer@mozilla.com

 ***** END LICENSE BLOCK *****/

package org.pentaho.di.ui.trans.steps.userdefinedjavaclass;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;
import org.pentaho.di.trans.steps.userdefinedjavaclass.FieldHelper;
import org.pentaho.di.trans.steps.userdefinedjavaclass.StepDefinition;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UsageParameter;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassDef;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassMeta;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassDef.ClassType;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassMeta.FieldInfo;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.trans.steps.userdefinedjavaclass.UserDefinedJavaClassCodeSnippits.Category;
import org.pentaho.di.ui.trans.steps.userdefinedjavaclass.UserDefinedJavaClassCodeSnippits.Snippit;

public class UserDefinedJavaClassDialog extends BaseStepDialog implements StepDialogInterface {
	private static Class<?>	PKG					= UserDefinedJavaClassMeta.class;	// for
	// i18n
	// purposes,
	// needed by
	// Translator2!!
	// $NON-NLS-1$

	private ModifyListener	lsMod;
	private SashForm		wSash;

	private TableView		wFields;

	private Label			wlPosition;

	private Button			wClearResultFields;

	private Text			wlHelpLabel;

	private Button			wTest;
	private Listener		lsTest;

	private Tree			wTree;
	private TreeItem		wTreeClassesItem;
	private TreeItem		wTreeClassesitem;
	private Listener		lsTree;

	private Image			imageActiveScript	= null;
	private Image			imageInactiveScript	= null;
	private Image			imageInputFields	= null;
	private Image			imageOutputFields	= null;
	private Image			imageArrowOrange	= null;
	private Image			imageArrowGreen		= null;
	private Image			imageUnderGreen		= null;

	private CTabFolder		folder;
	private Menu			cMenu;
	private Menu			tMenu;

	// Suport for Rename Tree
	private TreeItem[]		lastItem;
	private TreeEditor		editor;

	private CTabFolder		wTabFolder;

	private enum TabActions {
		DELETE_ITEM, ADD_ITEM, RENAME_ITEM, SET_ACTIVE_ITEM
	}

	private enum TabAddActions {
		ADD_COPY, ADD_BLANK, ADD_DEFAULT
	}

	private String								strActiveScript;

	private UserDefinedJavaClassMeta			input;
	private UserDefinedJavaClassCodeSnippits	snippitsHelper;
	private UserDefinedJavaClassHighlight		lineStyler	= new UserDefinedJavaClassHighlight();

	private TreeItem							iteminput;

	private TreeItem							itemoutput;

	private static GUIResource					guiresource	= GUIResource.getInstance();

	private TreeItem							itemWaitFieldsIn, itemWaitFieldsOut;

	private RowMetaInterface					inputRowMeta;
	private RowMetaInterface					outputRowMeta;

	private RowGeneratorMeta					genMeta;

	private CTabItem							fieldsTab;

	private int									middle;

	private int									margin;

	private CTabItem							infoTab;
	private TableView							wInfoSteps;

	private String[]							prevStepNames;

	private String[]							nextStepNames;

	private CTabItem							targetTab;
	private TableView							wTargetSteps;

	private CTabItem							parametersTab;
	private TableView							wParameters;

	public UserDefinedJavaClassDialog(Shell parent, Object in, TransMeta transMeta, String sname) {

		super(parent, (BaseStepMeta) in, transMeta, sname);
		input = (UserDefinedJavaClassMeta) in;
		genMeta = null;
		try {
			// ImageLoader xl = new ImageLoader();
			imageUnderGreen = guiresource.getImage("ui/images/underGreen.png");
			imageArrowGreen = guiresource.getImage("ui/images/arrowGreen.png");
			imageArrowOrange = guiresource.getImage("ui/images/arrowOrange.png");
			imageInputFields = guiresource.getImage("ui/images/inSmall.png");
			imageOutputFields = guiresource.getImage("ui/images/outSmall.png");
			imageActiveScript = guiresource.getImage("ui/images/faScript.png");
			imageInactiveScript = guiresource.getImage("ui/images/fScript.png");
		} catch (Exception e) {
			imageActiveScript = new Image(parent.getDisplay(), 16, 16);
			imageInactiveScript = new Image(parent.getDisplay(), 16, 16);
			imageInputFields = new Image(parent.getDisplay(), 16, 16);
			imageOutputFields = new Image(parent.getDisplay(), 16, 16);
			imageArrowOrange = new Image(parent.getDisplay(), 16, 16);
			imageArrowGreen = new Image(parent.getDisplay(), 16, 16);
			imageUnderGreen = new Image(parent.getDisplay(), 16, 16);
		}

		try {
			snippitsHelper = UserDefinedJavaClassCodeSnippits.getSnippitsHelper();
		} catch (Exception e) {
			new ErrorDialog(shell, "Unexpected error", "There was an unexpected error reading the code snippits file",
							e);
		}

	}

	public String open() {

		Shell parent = getParent();
		Display display = parent.getDisplay();

		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
		props.setLook(shell);
		setShellImage(shell, input);

		lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				input.setChanged();
			}
		};
		changed = input.hasChanged();

		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		shell.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.Shell.Title")); //$NON-NLS-1$

		middle = props.getMiddlePct();
		margin = Const.MARGIN;

		// Filename line
		wlStepname = new Label(shell, SWT.RIGHT);
		wlStepname.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.Stepname.Label")); //$NON-NLS-1$
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

		wSash = new SashForm(shell, SWT.VERTICAL);

		// Top sash form
		//
		Composite wTop = new Composite(wSash, SWT.NONE);
		props.setLook(wTop);

		FormLayout topLayout = new FormLayout();
		topLayout.marginWidth = Const.FORM_MARGIN;
		topLayout.marginHeight = Const.FORM_MARGIN;
		wTop.setLayout(topLayout);

		// Script line
		Label wlScriptFunctions = new Label(wTop, SWT.NONE);
		wlScriptFunctions.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.ClassesAndSnippits.Label")); //$NON-NLS-1$
		props.setLook(wlScriptFunctions);
		FormData fdlScriptFunctions = new FormData();
		fdlScriptFunctions.left = new FormAttachment(0, 0);
		fdlScriptFunctions.top = new FormAttachment(0, 0);
		wlScriptFunctions.setLayoutData(fdlScriptFunctions);

		// Tree View Test
		wTree = new Tree(wTop, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		props.setLook(wTree);
		FormData fdlTree = new FormData();
		fdlTree.left = new FormAttachment(0, 0);
		fdlTree.top = new FormAttachment(wlScriptFunctions, margin);
		fdlTree.right = new FormAttachment(20, 0);
		fdlTree.bottom = new FormAttachment(100, -margin);
		wTree.setLayoutData(fdlTree);

		// Script line
		Label wlScript = new Label(wTop, SWT.NONE);
		wlScript.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.Class.Label")); //$NON-NLS-1$
		props.setLook(wlScript);
		FormData fdlScript = new FormData();
		fdlScript.left = new FormAttachment(wTree, margin);
		fdlScript.top = new FormAttachment(0, 0);
		wlScript.setLayoutData(fdlScript);

		folder = new CTabFolder(wTop, SWT.BORDER | SWT.RESIZE);
		folder.setSimple(false);
		folder.setUnselectedImageVisible(true);
		folder.setUnselectedCloseVisible(true);
		FormData fdScript = new FormData();
		fdScript.left = new FormAttachment(wTree, margin);
		fdScript.top = new FormAttachment(wlScript, margin);
		fdScript.right = new FormAttachment(100, -5);
		fdScript.bottom = new FormAttachment(100, -50);
		folder.setLayoutData(fdScript);

		wlPosition = new Label(wTop, SWT.NONE);
		wlPosition.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.Position.Label")); //$NON-NLS-1$
		props.setLook(wlPosition);
		FormData fdlPosition = new FormData();
		fdlPosition.left = new FormAttachment(wTree, margin);
		fdlPosition.right = new FormAttachment(30, 0);
		fdlPosition.top = new FormAttachment(folder, margin);
		wlPosition.setLayoutData(fdlPosition);

		wlHelpLabel = new Text(wTop, SWT.V_SCROLL | SWT.LEFT);
		wlHelpLabel.setEditable(false);
		wlHelpLabel.setText("Hallo");
		props.setLook(wlHelpLabel);
		FormData fdHelpLabel = new FormData();
		fdHelpLabel.left = new FormAttachment(wlPosition, margin);
		fdHelpLabel.top = new FormAttachment(folder, margin);
		fdHelpLabel.right = new FormAttachment(100, -5);
		fdHelpLabel.bottom = new FormAttachment(100, 0);
		wlHelpLabel.setLayoutData(fdHelpLabel);
		wlHelpLabel.setVisible(false);

		FormData fdTop = new FormData();
		fdTop.left = new FormAttachment(0, 0);
		fdTop.top = new FormAttachment(0, 0);
		fdTop.right = new FormAttachment(100, 0);
		fdTop.bottom = new FormAttachment(100, 0);
		wTop.setLayoutData(fdTop);

		// 
		// Add a tab folder for the parameters and various input and output
		// streams
		//
		wTabFolder = new CTabFolder(wSash, SWT.BORDER);
		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
		wTabFolder.setSimple(false);
		wTabFolder.setUnselectedCloseVisible(false);

		FormData fdTabFolder = new FormData();
		fdTabFolder.left = new FormAttachment(0, 0);
		fdTabFolder.right = new FormAttachment(100, 0);
		fdTabFolder.top = new FormAttachment(0, 0);
		fdTabFolder.bottom = new FormAttachment(100, -75);
		wTabFolder.setLayoutData(fdTabFolder);

		// The Fields tab...
		//
		addFieldsTab();

		// The parameters
		//
		addParametersTab();

		prevStepNames = transMeta.getPrevStepNames(stepMeta);
		nextStepNames = transMeta.getNextStepNames(stepMeta);

		// OK, add another tab for the input settings...
		//
		addInfoTab();
		addTargetTab();

		// Select the fields tab...
		//
		wTabFolder.setSelection(fieldsTab);

		FormData fdSash = new FormData();
		fdSash.left = new FormAttachment(0, 0);
		fdSash.top = new FormAttachment(wStepname, 0);
		fdSash.right = new FormAttachment(100, 0);
		fdSash.bottom = new FormAttachment(100, -50);
		wSash.setLayoutData(fdSash);

		wSash.setWeights(new int[] { 75, 25 });

		wOK = new Button(shell, SWT.PUSH);
		wOK.setText(BaseMessages.getString(PKG, "System.Button.OK")); //$NON-NLS-1$
		wTest = new Button(shell, SWT.PUSH);
		wTest.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.TestClass.Button")); //$NON-NLS-1$
		wCancel = new Button(shell, SWT.PUSH);
		wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel")); //$NON-NLS-1$

		setButtonPositions(new Button[] { wOK, wCancel, wTest }, margin, null);

		lsCancel = new Listener() {
			public void handleEvent(Event e) {
				cancel();
			}
		};
		lsTest = new Listener() {
			public void handleEvent(Event e) {
				test();
			}
		};
		lsOK = new Listener() {
			public void handleEvent(Event e) {
				ok();
			}
		};
		lsTree = new Listener() {
			public void handleEvent(Event e) {
				treeDblClick(e);
			}
		};

		wCancel.addListener(SWT.Selection, lsCancel);
		wTest.addListener(SWT.Selection, lsTest);
		wOK.addListener(SWT.Selection, lsOK);
		wTree.addListener(SWT.MouseDoubleClick, lsTree);

		lsDef = new SelectionAdapter() {
			public void widgetDefaultSelected(SelectionEvent e) {
				ok();
			}
		};
		wStepname.addSelectionListener(lsDef);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				if (!cancel()) {
					e.doit = false;
				}
			}
		});

		folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void close(CTabFolderEvent event) {
				CTabItem cItem = (CTabItem) event.item;
				event.doit = false;
				if (cItem != null && folder.getItemCount() > 1) {
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.NO | SWT.YES);
					messageBox.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.DeleteItem.Label"));
					messageBox.setMessage(BaseMessages.getString(PKG,
									"UserDefinedJavaClassDialog.ConfirmDeleteItem.Label", cItem.getText()));
					switch (messageBox.open()) {
						case SWT.YES:
							modifyScriptTree(cItem, TabActions.DELETE_ITEM);
							event.doit = true;
							break;
					}
				}
			}
		});

		cMenu = new Menu(shell, SWT.POP_UP);
		buildingFolderMenu();
		tMenu = new Menu(shell, SWT.POP_UP);
		buildingTreeMenu();

		// Adding the Default Transform Class Item to the Tree
		wTreeClassesItem = new TreeItem(wTree, SWT.NULL);
		wTreeClassesItem.setImage(guiresource.getImageBol());
		wTreeClassesItem.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.Classes.Label"));

		// Set the shell size, based upon previous time...
		setSize();
		getData();

		// Adding the Rest (Functions, InputItems, etc.) to the Tree
		buildSnippitsTree();

		// Input Fields
		iteminput = new TreeItem(wTree, SWT.NULL);
		iteminput.setImage(imageInputFields);
		iteminput.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.InputFields.Label"));
		iteminput.setData("Field Helpers");
		// Output Fields
		itemoutput = new TreeItem(wTree, SWT.NULL);
		itemoutput.setImage(imageOutputFields);
		itemoutput.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.OutputFields.Label"));
		itemoutput.setData("Field Helpers");

		// Display waiting message for input
		itemWaitFieldsIn = new TreeItem(iteminput, SWT.NULL);
		itemWaitFieldsIn.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.GettingFields.Label"));
		itemWaitFieldsIn.setForeground(guiresource.getColorDirectory());
		iteminput.setExpanded(true);

		// Display waiting message for output
		itemWaitFieldsOut = new TreeItem(itemoutput, SWT.NULL);
		itemWaitFieldsOut.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.GettingFields.Label"));
		itemWaitFieldsOut.setForeground(guiresource.getColorDirectory());
		itemoutput.setExpanded(true);

		// 
		// Search the fields in the background
		//

		final Runnable runnable = new Runnable() {
			public void run() {
				StepMeta stepMeta = transMeta.findStep(stepname);
				if (stepMeta != null) {
					try {
						inputRowMeta = transMeta.getPrevStepFields(stepMeta);
						outputRowMeta = transMeta.getThisStepFields(stepMeta, null, inputRowMeta.clone());
						setInputOutputFields();
					} catch (KettleException e) {
						log.logError(toString(), BaseMessages.getString(PKG, "System.Dialog.GetFieldsFailed.Message"));
					}
				}
			}
		};
		new Thread(runnable).start();

		addRenameToTreeScriptItems();
		input.setChanged(changed);

		// Create the drag source on the tree
		DragSource ds = new DragSource(wTree, DND.DROP_MOVE);
		ds.setTransfer(new Transfer[] { TextTransfer.getInstance() });
		ds.addDragListener(new DragSourceAdapter() {

			public void dragStart(DragSourceEvent event) {
				boolean doit = false;
				TreeItem item = wTree.getSelection()[0];

				// Allow dragging snippits and field helpers
				if (item != null && item.getParentItem() != null) {
					if ("Snippits Category".equals(item.getParentItem().getData())
									&& !"Snippits Category".equals(item.getData())) {
						doit = true;
					} else if ("Field Helpers".equals(item.getParentItem().getData())) {
						doit = true;
					} else if (item.getParentItem().getParentItem() != null
									&& "Field Helpers".equals(item.getParentItem().getParentItem().getData())) {
						doit = true;
					}
				}
				event.doit = doit;
			}

			public void dragSetData(DragSourceEvent event) {
				// Set the data to be the first selected item's data
				event.data = wTree.getSelection()[0].getData();
			}
		});

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		return stepname;
	}

	private void addFieldsTab() {
		fieldsTab = new CTabItem(wTabFolder, SWT.NONE);
		fieldsTab.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.Tabs.Fields.Title"));
		fieldsTab.setToolTipText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.Tabs.Fields.TooltipText"));

		Composite wBottom = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wBottom);
		fieldsTab.setControl(wBottom);
		FormLayout bottomLayout = new FormLayout();
		bottomLayout.marginWidth = Const.FORM_MARGIN;
		bottomLayout.marginHeight = Const.FORM_MARGIN;
		wBottom.setLayout(bottomLayout);

		Label wlFields = new Label(wBottom, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.Fields.Label")); //$NON-NLS-1$
		props.setLook(wlFields);
		FormData fdlFields = new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top = new FormAttachment(0, 0);
		wlFields.setLayoutData(fdlFields);

		wClearResultFields = new Button(wBottom, SWT.CHECK);
		wClearResultFields.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.ClearResultFields.Label")); //$NON-NLS-1$
		props.setLook(wClearResultFields);
		FormData fdClearResultFields = new FormData();
		fdClearResultFields.right = new FormAttachment(100, 0);
		fdClearResultFields.top = new FormAttachment(0, 0);
		wClearResultFields.setLayoutData(fdClearResultFields);

		final int fieldsRows = input.getFieldInfo().size();

		ColumnInfo[] colinf = new ColumnInfo[] {
						new ColumnInfo(
										BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.ColumnInfo.Filename"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
						new ColumnInfo(
										BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.ColumnInfo.Type"), ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes()), //$NON-NLS-1$
						new ColumnInfo(
										BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.ColumnInfo.Length"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
						new ColumnInfo(
										BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.ColumnInfo.Precision"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
		};

		wFields = new TableView(transMeta, wBottom, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, fieldsRows,
						lsMod, props);

		FormData fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, 0);
		wFields.setLayoutData(fdFields);

		FormData fdBottom = new FormData();
		fdBottom.left = new FormAttachment(0, 0);
		fdBottom.top = new FormAttachment(0, 0);
		fdBottom.right = new FormAttachment(100, 0);
		fdBottom.bottom = new FormAttachment(100, 0);
		wBottom.setLayoutData(fdBottom);

	}

	private void addInfoTab() {
		infoTab = new CTabItem(wTabFolder, SWT.NONE);
		infoTab.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.Tabs.Info.Title"));
		infoTab.setToolTipText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.Tabs.Info.TooltipText"));

		Composite wBottom = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wBottom);
		infoTab.setControl(wBottom);
		FormLayout bottomLayout = new FormLayout();
		bottomLayout.marginWidth = Const.FORM_MARGIN;
		bottomLayout.marginHeight = Const.FORM_MARGIN;
		wBottom.setLayout(bottomLayout);

		Label wlFields = new Label(wBottom, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.InfoSteps.Label")); //$NON-NLS-1$
		props.setLook(wlFields);
		FormData fdlFields = new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top = new FormAttachment(0, 0);
		wlFields.setLayoutData(fdlFields);

		final int nrRows = input.getInfoStepDefinitions().size();
		ColumnInfo[] colinf = new ColumnInfo[] {
						new ColumnInfo(
										BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.ColumnInfo.StepTag"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
						new ColumnInfo(
										BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.ColumnInfo.StepName"), ColumnInfo.COLUMN_TYPE_CCOMBO, prevStepNames), //$NON-NLS-1$
						new ColumnInfo(
										BaseMessages.getString(PKG,
														"UserDefinedJavaClassDialog.ColumnInfo.StepDescription"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
		};

		wInfoSteps = new TableView(transMeta, wBottom, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, nrRows,
						lsMod, props);

		FormData fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, 0);
		wInfoSteps.setLayoutData(fdFields);

		FormData fdBottom = new FormData();
		fdBottom.left = new FormAttachment(0, 0);
		fdBottom.top = new FormAttachment(0, 0);
		fdBottom.right = new FormAttachment(100, 0);
		fdBottom.bottom = new FormAttachment(100, 0);
		wBottom.setLayoutData(fdBottom);
	}

	private void addTargetTab() {
		targetTab = new CTabItem(wTabFolder, SWT.NONE);
		targetTab.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.Tabs.Target.Title"));
		targetTab.setToolTipText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.Tabs.Target.TooltipText"));

		Composite wBottom = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wBottom);
		targetTab.setControl(wBottom);
		FormLayout bottomLayout = new FormLayout();
		bottomLayout.marginWidth = Const.FORM_MARGIN;
		bottomLayout.marginHeight = Const.FORM_MARGIN;
		wBottom.setLayout(bottomLayout);

		Label wlFields = new Label(wBottom, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.TargetSteps.Label")); //$NON-NLS-1$
		props.setLook(wlFields);
		FormData fdlFields = new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top = new FormAttachment(0, 0);
		wlFields.setLayoutData(fdlFields);

		final int nrRows = input.getInfoStepDefinitions().size();
		ColumnInfo[] colinf = new ColumnInfo[] {
						new ColumnInfo(
										BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.ColumnInfo.StepTag"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
						new ColumnInfo(
										BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.ColumnInfo.StepName"), ColumnInfo.COLUMN_TYPE_CCOMBO, nextStepNames), //$NON-NLS-1$
						new ColumnInfo(
										BaseMessages.getString(PKG,
														"UserDefinedJavaClassDialog.ColumnInfo.StepDescription"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
		};

		wTargetSteps = new TableView(transMeta, wBottom, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, nrRows,
						lsMod, props);

		FormData fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, 0);
		wTargetSteps.setLayoutData(fdFields);

		FormData fdBottom = new FormData();
		fdBottom.left = new FormAttachment(0, 0);
		fdBottom.top = new FormAttachment(0, 0);
		fdBottom.right = new FormAttachment(100, 0);
		fdBottom.bottom = new FormAttachment(100, 0);
		wBottom.setLayoutData(fdBottom);
	}

	private void addParametersTab() {
		parametersTab = new CTabItem(wTabFolder, SWT.NONE);
		parametersTab.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.Tabs.Parameters.Title"));
		parametersTab.setToolTipText(BaseMessages.getString(PKG,
						"UserDefinedJavaClassDialog.Tabs.Parameters.TooltipText"));

		Composite wBottom = new Composite(wTabFolder, SWT.NONE);
		props.setLook(wBottom);
		parametersTab.setControl(wBottom);
		FormLayout bottomLayout = new FormLayout();
		bottomLayout.marginWidth = Const.FORM_MARGIN;
		bottomLayout.marginHeight = Const.FORM_MARGIN;
		wBottom.setLayout(bottomLayout);

		Label wlFields = new Label(wBottom, SWT.NONE);
		wlFields.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.Parameters.Label")); //$NON-NLS-1$
		props.setLook(wlFields);
		FormData fdlFields = new FormData();
		fdlFields.left = new FormAttachment(0, 0);
		fdlFields.top = new FormAttachment(0, 0);
		wlFields.setLayoutData(fdlFields);

		final int nrRows = input.getUsageParameters().size();
		ColumnInfo[] colinf = new ColumnInfo[] {
						new ColumnInfo(
										BaseMessages.getString(PKG,
														"UserDefinedJavaClassDialog.ColumnInfo.ParameterTag"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
						new ColumnInfo(
										BaseMessages.getString(PKG,
														"UserDefinedJavaClassDialog.ColumnInfo.ParameterValue"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
						new ColumnInfo(
										BaseMessages.getString(PKG,
														"UserDefinedJavaClassDialog.ColumnInfo.ParameterDescription"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
		};
		colinf[1].setUsingVariables(true);

		wParameters = new TableView(transMeta, wBottom, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, nrRows,
						lsMod, props);

		FormData fdFields = new FormData();
		fdFields.left = new FormAttachment(0, 0);
		fdFields.top = new FormAttachment(wlFields, margin);
		fdFields.right = new FormAttachment(100, 0);
		fdFields.bottom = new FormAttachment(100, 0);
		wParameters.setLayoutData(fdFields);

		FormData fdBottom = new FormData();
		fdBottom.left = new FormAttachment(0, 0);
		fdBottom.top = new FormAttachment(0, 0);
		fdBottom.right = new FormAttachment(100, 0);
		fdBottom.bottom = new FormAttachment(100, 0);
		wBottom.setLayoutData(fdBottom);
	}

	private void setActiveCtab(String strName) {
		if (strName.length() == 0) {
			folder.setSelection(0);
		} else
			folder.setSelection(getCTabPosition(strName));
	}

	private void addCtab(String tabName, String tabCode, TabAddActions tabType) {
		CTabItem item = new CTabItem(folder, SWT.CLOSE);

		switch (tabType) {
			case ADD_DEFAULT:
				item.setText(tabName);
				break;
			default:
				item.setText(getNextName(tabName));
				break;
		}
		StyledTextComp wScript = new StyledTextComp(item.getParent(), SWT.MULTI | SWT.LEFT | SWT.H_SCROLL
						| SWT.V_SCROLL, item.getText());
		if ((tabCode != null) && tabCode.length() > 0)
			wScript.setText(tabCode);
		else
			wScript.setText(snippitsHelper.getDefaultCode());
		item.setImage(imageInactiveScript);
		props.setLook(wScript, Props.WIDGET_STYLE_FIXED);

		wScript.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				setPosition();
			}

			public void keyReleased(KeyEvent e) {
				setPosition();
			}
		});
		wScript.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				setPosition();
			}

			public void focusLost(FocusEvent e) {
				setPosition();
			}
		});
		wScript.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				setPosition();
			}

			public void mouseDown(MouseEvent e) {
				setPosition();
			}

			public void mouseUp(MouseEvent e) {
				setPosition();
			}
		});

		wScript.addModifyListener(lsMod);

		// Text Higlighting
		lineStyler = new UserDefinedJavaClassHighlight();
		wScript.addLineStyleListener(lineStyler);
		item.setControl(wScript);

		// Adding new Item to Tree
		modifyScriptTree(item, TabActions.ADD_ITEM);
	}

	private void modifyScriptTree(CTabItem ctabitem, TabActions action) {

		switch (action) {
			case DELETE_ITEM:
				TreeItem dItem = getTreeItemByName(ctabitem.getText());
				if (dItem != null) {
					dItem.dispose();
					input.setChanged();
				}
				break;
			case ADD_ITEM:
				TreeItem item = new TreeItem(wTreeClassesItem, SWT.NULL);
				item.setImage(imageActiveScript);
				item.setText(ctabitem.getText());
				input.setChanged();
				break;

			case RENAME_ITEM:
				input.setChanged();
				break;
			case SET_ACTIVE_ITEM:
				input.setChanged();
				break;
		}
	}

	private TreeItem getTreeItemByName(String strTabName) {
		TreeItem[] tItems = wTreeClassesItem.getItems();
		for (int i = 0; i < tItems.length; i++) {
			if (tItems[i].getText().equals(strTabName)) return tItems[i];
		}
		return null;
	}

	private int getCTabPosition(String strTabName) {
		CTabItem[] cItems = folder.getItems();
		for (int i = 0; i < cItems.length; i++) {
			if (cItems[i].getText().equals(strTabName)) return i;
		}
		return -1;
	}

	private CTabItem getCTabItemByName(String strTabName) {
		CTabItem[] cItems = folder.getItems();
		for (int i = 0; i < cItems.length; i++) {
			if (cItems[i].getText().equals(strTabName)) return cItems[i];
		}
		return null;
	}

	private void modifyCTabItem(TreeItem tItem, TabActions iModType, String strOption) {

		switch (iModType) {
			case DELETE_ITEM:
				CTabItem dItem = folder.getItem(getCTabPosition(tItem.getText()));
				if (dItem != null) {
					dItem.dispose();
					input.setChanged();
				}
				break;

			case RENAME_ITEM:
				CTabItem rItem = folder.getItem(getCTabPosition(tItem.getText()));
				if (rItem != null) {
					rItem.setText(strOption);
					input.setChanged();
					if (rItem.getImage().equals(imageActiveScript)) strActiveScript = strOption;
				}
				break;
			case SET_ACTIVE_ITEM:
				CTabItem aItem = folder.getItem(getCTabPosition(tItem.getText()));
				if (aItem != null) {
					input.setChanged();
					strActiveScript = tItem.getText();
					for (int i = 0; i < folder.getItemCount(); i++) {
						if (folder.getItem(i).equals(aItem))
							aItem.setImage(imageActiveScript);
						else
							folder.getItem(i).setImage(imageInactiveScript);
					}
				}
				break;
		}

	}

	private StyledTextComp getStyledTextComp() {
		CTabItem item = folder.getSelection();
		if (item.getControl().isDisposed())
			return null;
		else
			return (StyledTextComp) item.getControl();
	}

	private StyledTextComp getStyledTextComp(CTabItem item) {
		return (StyledTextComp) item.getControl();
	}

	private String getNextName(String strActualName) {
		String strRC = "";
		if (strActualName.length() == 0) {
			strActualName = "ExtraClass";
		}

		int i = 0;
		strRC = strActualName + "_" + i;
		while (getCTabItemByName(strRC) != null) {
			i++;
			strRC = strActualName + "_" + i;
		}
		return strRC;
	}

	public void setPosition() {

		StyledTextComp wScript = getStyledTextComp();
		String scr = wScript.getText();
		int linenr = wScript.getLineAtOffset(wScript.getCaretOffset()) + 1;
		int posnr = wScript.getCaretOffset();

		// Go back from position to last CR: how many positions?
		int colnr = 0;
		while (posnr > 0 && scr.charAt(posnr - 1) != '\n' && scr.charAt(posnr - 1) != '\r') {
			posnr--;
			colnr++;
		}
		wlPosition
						.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.Position.Label2") + linenr + ", " + colnr); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData() {
		int i = 0;
		for (FieldInfo fi : input.getFieldInfo()) {
			TableItem item = wFields.table.getItem(i);
			i++;
			item.setText(1, fi.name);
			item.setText(2, ValueMeta.getTypeDesc(fi.type));
			if (fi.length >= 0) item.setText(3, "" + fi.length); //$NON-NLS-1$
			if (fi.precision >= 0) item.setText(4, "" + fi.precision); //$NON-NLS-1$
		}

		List<UserDefinedJavaClassDef> definitions = input.getDefinitions();
		if (definitions.size() == 0) {
	        try {
	        	definitions = new ArrayList<UserDefinedJavaClassDef>();
	        	// Note the tab name isn't i18n because it is a Java Class name and i18n characters might make it choke.
				definitions.add(new UserDefinedJavaClassDef(UserDefinedJavaClassDef.ClassType.TRANSFORM_CLASS, "Processor",
				        UserDefinedJavaClassCodeSnippits.getSnippitsHelper().getDefaultCode()));
				input.replaceDefinitions(definitions);
			} catch (KettleXMLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (UserDefinedJavaClassDef def : definitions) {
			if (def.isTransformClass()) strActiveScript = def.getClassName();
			addCtab(def.getClassName(), def.getSource(), TabAddActions.ADD_DEFAULT);
		}

		setActiveCtab(strActiveScript);
		refresh();

		wClearResultFields.setSelection(input.isClearingResultFields());

		wFields.setRowNums();
		wFields.optWidth(true);
		int rowNr = 0;
		for (StepDefinition stepDefinition : input.getInfoStepDefinitions()) {
			TableItem item = wInfoSteps.table.getItem(rowNr++);
			int colNr = 1;
			item.setText(colNr++, Const.NVL(stepDefinition.tag, ""));
			item.setText(colNr++, stepDefinition.stepMeta != null ? stepDefinition.stepMeta.getName() : "");
			item.setText(colNr++, Const.NVL(stepDefinition.description, ""));
		}
		wInfoSteps.setRowNums();
		wInfoSteps.optWidth(true);

		rowNr = 0;
		for (StepDefinition stepDefinition : input.getTargetStepDefinitions()) {
			TableItem item = wTargetSteps.table.getItem(rowNr++);
			int colNr = 1;
			item.setText(colNr++, Const.NVL(stepDefinition.tag, ""));
			item.setText(colNr++, stepDefinition.stepMeta != null ? stepDefinition.stepMeta.getName() : "");
			item.setText(colNr++, Const.NVL(stepDefinition.description, ""));
		}
		wTargetSteps.setRowNums();
		wTargetSteps.optWidth(true);

		rowNr = 0;
		for (UsageParameter usageParameter : input.getUsageParameters()) {
			TableItem item = wParameters.table.getItem(rowNr++);
			int colNr = 1;
			item.setText(colNr++, Const.NVL(usageParameter.tag, ""));
			item.setText(colNr++, Const.NVL(usageParameter.value, ""));
			item.setText(colNr++, Const.NVL(usageParameter.description, ""));
		}
		wParameters.setRowNums();
		wParameters.optWidth(true);

		wStepname.selectAll();
	}

	private void refresh() {
		for (int i = 0; i < folder.getItemCount(); i++) {
			CTabItem item = folder.getItem(i);
			if (item.getText().equals(strActiveScript))
				item.setImage(imageActiveScript);
			else
				item.setImage(imageInactiveScript);
		}
	}

	private boolean cancel() {
		if (input.hasChanged()) {
			MessageBox box = new MessageBox(shell, SWT.YES | SWT.NO | SWT.APPLICATION_MODAL);
			box.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.WarningDialogChanged.Title"));
			box.setMessage(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.WarningDialogChanged.Message",
							Const.CR));
			int answer = box.open();

			if (answer == SWT.NO) {
				return false;
			}
		}
		stepname = null;
		input.setChanged(changed);
		dispose();
		return true;
	}

	private void getInfo(UserDefinedJavaClassMeta meta) {
		int nrfields = wFields.nrNonEmpty();
		List<FieldInfo> newFields = new ArrayList<FieldInfo>(nrfields);
		for (int i = 0; i < nrfields; i++) {
			TableItem item = wFields.getNonEmpty(i);
			newFields.add(new FieldInfo(item.getText(1), ValueMeta.getType(item.getText(2)), Const.toInt(item
							.getText(3), -1), Const.toInt(item.getText(4), -1)));
		}
		meta.replaceFields(newFields);

		CTabItem[] cTabs = folder.getItems();
		if (cTabs.length > 0) {
			List<UserDefinedJavaClassDef> definitions = new ArrayList<UserDefinedJavaClassDef>(cTabs.length);
			for (int i = 0; i < cTabs.length; i++) {
				UserDefinedJavaClassDef def = new UserDefinedJavaClassDef(ClassType.NORMAL_CLASS, cTabs[i].getText(),
								getStyledTextComp(cTabs[i]).getText());
				if (cTabs[i].getImage().equals(imageActiveScript)) def.setClassType(ClassType.TRANSFORM_CLASS);
				definitions.add(def);
			}
			meta.replaceDefinitions(definitions);
		}
		meta.setClearingResultFields(wClearResultFields.getSelection());

		int nrInfos = wInfoSteps.nrNonEmpty();
		meta.getInfoStepDefinitions().clear();
		for (int i = 0; i < nrInfos; i++) {
			TableItem item = wInfoSteps.getNonEmpty(i);
			StepDefinition stepDefinition = new StepDefinition();
			int colNr = 1;
			stepDefinition.tag = item.getText(colNr++);
			stepDefinition.stepMeta = transMeta.findStep(item.getText(colNr++));
			stepDefinition.description = item.getText(colNr++);
			meta.getInfoStepDefinitions().add(stepDefinition);
		}

		int nrTargets = wTargetSteps.nrNonEmpty();
		meta.getTargetStepDefinitions().clear();
		for (int i = 0; i < nrTargets; i++) {
			TableItem item = wTargetSteps.getNonEmpty(i);
			StepDefinition stepDefinition = new StepDefinition();
			int colNr = 1;
			stepDefinition.tag = item.getText(colNr++);
			stepDefinition.stepMeta = transMeta.findStep(item.getText(colNr++));
			stepDefinition.description = item.getText(colNr++);
			meta.getTargetStepDefinitions().add(stepDefinition);
		}

		int nrParameters = wParameters.nrNonEmpty();
		meta.getUsageParameters().clear();
		for (int i = 0; i < nrParameters; i++) {
			TableItem item = wParameters.getNonEmpty(i);
			UsageParameter usageParameter = new UsageParameter();
			int colNr = 1;
			usageParameter.tag = item.getText(colNr++);
			usageParameter.value = item.getText(colNr++);
			usageParameter.description = item.getText(colNr++);
			meta.getUsageParameters().add(usageParameter);
		}
	}

	private void ok() {
		if (Const.isEmpty(wStepname.getText())) return;

		stepname = wStepname.getText(); // return value

		boolean bInputOK = false;

		// Check if Active Script has set, otherwise Ask
		if (getCTabItemByName(strActiveScript) == null) {
			MessageBox mb = new MessageBox(shell, SWT.OK | SWT.CANCEL | SWT.ICON_ERROR);
			mb.setMessage(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.NoTransformClassSet"));
			mb.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.ERROR.Label")); //$NON-NLS-1$
			switch (mb.open()) {
				case SWT.OK:
					strActiveScript = folder.getItem(0).getText();
					refresh();
					bInputOK = true;
					break;
				case SWT.CANCEL:
					bInputOK = false;
					break;
			}
		} else {
			bInputOK = true;
		}

		if (bInputOK) {
			getInfo(input);
			dispose();
		}
	}

	private boolean test() {
		PluginRegistry registry = PluginRegistry.getInstance();
		String scriptStepName = wStepname.getText();

		// Create a step with the information in this dialog
		UserDefinedJavaClassMeta scriptMeta = new UserDefinedJavaClassMeta();
		getInfo(scriptMeta);

		try {
			// First, before we get into the trial run, just see if the classes
			// all compile.
			scriptMeta.cookClasses();
			if (scriptMeta.cookErrors.size() == 1) {
				Exception e = scriptMeta.cookErrors.get(0);
				new ErrorDialog(shell, "Error during class compilation", e.toString(), e); //$NON-NLS-1$ //$NON-NLS-2$
				return false;
			} else if (scriptMeta.cookErrors.size() > 1) {
				Exception e = scriptMeta.cookErrors.get(0);
				new ErrorDialog(
								shell,
								"Errors during class compilation", String.format("Multiple errors during class compilation. First error:\n%s", e.toString()), e); //$NON-NLS-1$ //$NON-NLS-2$
				return false;
			}

			// What fields are coming into the step?
			RowMetaInterface rowMeta = transMeta.getPrevStepFields(stepname).clone();
			if (rowMeta != null) {
				// Create a new RowGenerator step to generate rows for the test
				// data...
				// Only create a new instance the first time to help the user.
				// Otherwise he/she has to key in the same test data all the
				// time
				if (genMeta == null) {
					genMeta = new RowGeneratorMeta();
					genMeta.setRowLimit("10");
					genMeta.allocate(rowMeta.size());
					for (int i = 0; i < rowMeta.size(); i++) {
						ValueMetaInterface valueMeta = rowMeta.getValueMeta(i);
						if (valueMeta.isStorageBinaryString()) {
							valueMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_NORMAL);
						}
						genMeta.getFieldName()[i] = valueMeta.getName();
						genMeta.getFieldType()[i] = valueMeta.getTypeDesc();
						genMeta.getFieldLength()[i] = valueMeta.getLength();
						genMeta.getFieldPrecision()[i] = valueMeta.getPrecision();
						genMeta.getCurrency()[i] = valueMeta.getCurrencySymbol();
						genMeta.getDecimal()[i] = valueMeta.getDecimalSymbol();
						genMeta.getGroup()[i] = valueMeta.getGroupingSymbol();

						String string = null;
						switch (valueMeta.getType()) {
							case ValueMetaInterface.TYPE_DATE:
								genMeta.getFieldFormat()[i] = "yyyy/MM/dd HH:mm:ss";
								valueMeta.setConversionMask(genMeta.getFieldFormat()[i]);
								string = valueMeta.getString(new Date());
								break;
							case ValueMetaInterface.TYPE_STRING:
								string = "test value test value"; //$NON-NLS-1$
								break;
							case ValueMetaInterface.TYPE_INTEGER:
								genMeta.getFieldFormat()[i] = " #;-#";
								valueMeta.setConversionMask(genMeta.getFieldFormat()[i]);
								string = valueMeta.getString(Long.valueOf(0L));
								break;
							case ValueMetaInterface.TYPE_NUMBER:
								genMeta.getFieldFormat()[i] = " #.#;-#.#";
								valueMeta.setConversionMask(genMeta.getFieldFormat()[i]);
								string = valueMeta.getString(Double.valueOf(0.0D));
								break;
							case ValueMetaInterface.TYPE_BIGNUMBER:
								genMeta.getFieldFormat()[i] = " #.#;-#.#";
								valueMeta.setConversionMask(genMeta.getFieldFormat()[i]);
								string = valueMeta.getString(BigDecimal.ZERO);
								break;
							case ValueMetaInterface.TYPE_BOOLEAN:
								string = valueMeta.getString(Boolean.TRUE);
								break;
							case ValueMetaInterface.TYPE_BINARY:
								string = valueMeta.getString(new byte[] { 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, });
								break;
							default:
								break;
						}

						genMeta.getValue()[i] = string;
					}
				}
				StepMeta genStep = new StepMeta(registry.getPluginId(StepPluginType.class, genMeta), "## TEST DATA ##",
								genMeta);
				genStep.setLocation(50, 50);

				StepMeta scriptStep = new StepMeta(registry.getPluginId(StepPluginType.class, scriptMeta), Const.NVL(
								scriptStepName, "## SCRIPT ##"), scriptMeta);
				scriptStepName = scriptStep.getName();
				scriptStep.setLocation(150, 50);

				// Create a hop between both steps...
				//
				TransHopMeta hop = new TransHopMeta(genStep, scriptStep);

				// Generate a new test transformation...
				//
				TransMeta transMeta = new TransMeta();
				transMeta.setName(wStepname.getText() + " - PREVIEW"); // $NON-NLS-1$
				transMeta.addStep(genStep);
				transMeta.addStep(scriptStep);
				transMeta.addTransHop(hop);

				// OK, now we ask the user to edit this dialog...
				//
				if (Spoon.getInstance().editStep(transMeta, genStep) != null) {
					// Now run this transformation and grab the results...
					//
					TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, transMeta,
									new String[] { scriptStepName, }, new int[] { Const
													.toInt(genMeta.getRowLimit(), 10), });
					progressDialog.open();

					Trans trans = progressDialog.getTrans();
					String loggingText = progressDialog.getLoggingText();

					if (!progressDialog.isCancelled()) {
						if (trans.getResult() != null && trans.getResult().getNrErrors() > 0) {
							EnterTextDialog etd = new EnterTextDialog(shell, BaseMessages
											.getString("System.Dialog.PreviewError.Title"), BaseMessages
											.getString("System.Dialog.PreviewError.Message"), loggingText, true);
							etd.setReadOnly();
							etd.open();
						}
					}

					RowMetaInterface previewRowsMeta = progressDialog.getPreviewRowsMeta(wStepname.getText());
					List<Object[]> previewRows = progressDialog.getPreviewRows(wStepname.getText());

					if (previewRowsMeta != null && previewRows != null && previewRows.size() > 0) {
						PreviewRowsDialog prd = new PreviewRowsDialog(shell, transMeta, SWT.NONE, wStepname.getText(),
										previewRowsMeta, previewRows, loggingText);
						prd.open();
					}
				}

				return true;
			} else {
				throw new KettleException(BaseMessages.getString(PKG,
								"UserDefinedJavaClassDialog.Exception.CouldNotGetFields")); //$NON-NLS-1$
			}
		} catch (Exception e) {
			new ErrorDialog(
							shell,
							BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.TestFailed.DialogTitle"), BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.TestFailed.DialogMessage"), e); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}

	}

	public String toString() {
		return this.getClass().getName();
	}

	private void buildSnippitsTree() {

		TreeItem item = new TreeItem(wTree, SWT.NULL);
		item.setImage(guiresource.getImageBol());
		item.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.Snippits.Label"));

		Map<Category, TreeItem> categoryTreeItems = new EnumMap<Category, TreeItem>(Category.class);
		for (Category cat : Category.values()) {
			TreeItem itemGroup = new TreeItem(item, SWT.NULL);
			itemGroup.setImage(imageUnderGreen);
			itemGroup.setText(cat.getDescription());
			itemGroup.setData("Snippits Category");
			categoryTreeItems.put(cat, itemGroup);
		}

		Collection<Snippit> snippits = snippitsHelper.getSnippits();
		for (Snippit snippit : snippits) {
			TreeItem itemGroup = categoryTreeItems.get(snippit.category);
			TreeItem itemSnippit = new TreeItem(itemGroup, SWT.NULL);
			itemSnippit.setText(snippit.name);
			itemSnippit.setImage(imageArrowGreen);
			itemSnippit.setData(snippit.code);
		}
	}

	public boolean TreeItemExist(TreeItem itemToCheck, String strItemName) {
		boolean bRC = false;
		if (itemToCheck.getItemCount() > 0) {
			TreeItem[] items = itemToCheck.getItems();
			for (int i = 0; i < items.length; i++) {
				if (items[i].getText().equals(strItemName)) return true;
			}
		}
		return bRC;
	}

	private void setInputOutputFields() {
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				iteminput.removeAll();
				itemoutput.removeAll();

				if (inputRowMeta != null) {
					for (int i = 0; i < inputRowMeta.size(); i++) {
						ValueMetaInterface v = inputRowMeta.getValueMeta(i);
						String itemName = v.getName();
						String itemData = FieldHelper.getAccessor(true, itemName);
						TreeItem itemField = new TreeItem(iteminput, SWT.NULL);
						itemField.setImage(imageArrowOrange);
						itemField.setText(itemName);
						itemField.setData(itemData);
						TreeItem itemFieldGet = new TreeItem(itemField, SWT.NULL);
						itemFieldGet.setText(String.format("get%s()",v.getTypeDesc()));
						itemFieldGet.setData(FieldHelper.getGetSignature(itemData, v));
						TreeItem itemFieldSet = new TreeItem(itemField, SWT.NULL);
						itemFieldSet.setText("setValue()");
						itemFieldSet.setData(itemData+".setValue(r, value);");
					}
				}
				if (outputRowMeta != null) {
					for (int i = 0; i < outputRowMeta.size(); i++) {
						ValueMetaInterface v = outputRowMeta.getValueMeta(i);
						String itemName = v.getName();
						String itemData = FieldHelper.getAccessor(false, itemName);
						TreeItem itemField = new TreeItem(itemoutput, SWT.NULL);
						itemField.setImage(imageArrowOrange);
						itemField.setText(itemName);
						itemField.setData(itemData);
						TreeItem itemFieldGet = new TreeItem(itemField, SWT.NULL);
						itemFieldGet.setText(String.format("get%s()",v.getTypeDesc()));
						itemFieldGet.setData(FieldHelper.getGetSignature(itemData, v));
						TreeItem itemFieldSet = new TreeItem(itemField, SWT.NULL);
						itemFieldSet.setText("setValue()");
						itemFieldSet.setData(itemData+".setValue(r, value);");
					}
				}
			}
		});
	}

	// Adds the Current item to the current Position
	private void treeDblClick(Event event) {
		StyledTextComp wScript = getStyledTextComp();
		Point point = new Point(event.x, event.y);
		TreeItem item = wTree.getItem(point);

		// Qualification where the Click comes from
		if (item != null && item.getParentItem() != null) {
			if (item.getParentItem().equals(wTreeClassesItem)) {
				setActiveCtab(item.getText());
			} else if (!item.getData().equals("Snippit")) {
				int iStart = wScript.getCaretOffset();
				int selCount = wScript.getSelectionCount(); // this selection
				// will be replaced
				// by wScript.insert
				iStart = iStart - selCount; // when a selection is already there
				// we need to subtract the position
				if (iStart < 0) iStart = 0; // just safety
				String strInsert = (String) item.getData();
				wScript.insert(strInsert);
				wScript.setSelection(iStart, iStart + strInsert.length());
			}
		}
	}

	private void buildingFolderMenu() {
		// styledTextPopupmenu = new Menu(, SWT.POP_UP);
		MenuItem addNewItem = new MenuItem(cMenu, SWT.PUSH);
		addNewItem.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.AddNewTab"));
		addNewItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				addCtab("", "", TabAddActions.ADD_BLANK);
			}
		});

		MenuItem copyItem = new MenuItem(cMenu, SWT.PUSH);
		copyItem.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.AddCopy"));
		copyItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				CTabItem item = folder.getSelection();
				StyledTextComp st = (StyledTextComp) item.getControl();
				addCtab(item.getText(), st.getText(), TabAddActions.ADD_COPY);
			}
		});
		new MenuItem(cMenu, SWT.SEPARATOR);

		MenuItem setActiveScriptItem = new MenuItem(cMenu, SWT.PUSH);
		setActiveScriptItem.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.SetTransformClass"));
		setActiveScriptItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				CTabItem item = folder.getSelection();
				for (int i = 0; i < folder.getItemCount(); i++) {
					if (folder.getItem(i).equals(item)) {
						if (item.getImage().equals(imageActiveScript)) strActiveScript = "";
						item.setImage(imageActiveScript);
						strActiveScript = item.getText();
					} else if (folder.getItem(i).getImage().equals(imageActiveScript)) {
						folder.getItem(i).setImage(imageInactiveScript);
					}
				}
				modifyScriptTree(item, TabActions.SET_ACTIVE_ITEM);
			}
		});

		new MenuItem(cMenu, SWT.SEPARATOR);
		MenuItem setRemoveScriptItem = new MenuItem(cMenu, SWT.PUSH);
		setRemoveScriptItem.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.RemoveClassType"));
		setRemoveScriptItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				CTabItem item = folder.getSelection();
				input.setChanged(true);
				if (item.getImage().equals(imageActiveScript)) strActiveScript = "";
				item.setImage(imageInactiveScript);
			}
		});

		folder.setMenu(cMenu);
	}

	private void buildingTreeMenu() {
		// styledTextPopupmenu = new Menu(, SWT.POP_UP);
		MenuItem addDeleteItem = new MenuItem(tMenu, SWT.PUSH);
		addDeleteItem.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.Delete.Label"));
		addDeleteItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				if (wTree.getSelectionCount() <= 0) return;

				TreeItem tItem = wTree.getSelection()[0];
				if (tItem != null) {
					MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.NO | SWT.YES);
					messageBox.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.DeleteItem.Label"));
					messageBox.setMessage(BaseMessages.getString(PKG,
									"UserDefinedJavaClassDialog.ConfirmDeleteItem.Label", tItem.getText()));
					switch (messageBox.open()) {
						case SWT.YES:
							modifyCTabItem(tItem, TabActions.DELETE_ITEM, "");
							tItem.dispose();
							input.setChanged();
							break;
					}
				}
			}
		});

		MenuItem renItem = new MenuItem(tMenu, SWT.PUSH);
		renItem.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.Rename.Label"));
		renItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				renameFunction(wTree.getSelection()[0]);
			}
		});

		new MenuItem(tMenu, SWT.SEPARATOR);
		MenuItem helpItem = new MenuItem(tMenu, SWT.PUSH);
		helpItem.setText(BaseMessages.getString(PKG, "UserDefinedJavaClassDialog.Sample.Label"));
		helpItem.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				String snippitFullName = wTree.getSelection()[0].getText();
				String sampleTabName = snippitFullName.substring(0, snippitFullName.indexOf('(')).replace(' ', '_')
								+ "_Sample";

				if (getCTabPosition(sampleTabName) == -1)
					addCtab(sampleTabName, snippitsHelper.getSample(snippitFullName), TabAddActions.ADD_DEFAULT);

				if (getCTabPosition(sampleTabName) != -1) setActiveCtab(sampleTabName);
			}
		});

		wTree.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				if (wTree.getSelectionCount() <= 0) return;

				TreeItem tItem = wTree.getSelection()[0];
				if (tItem != null) {
					TreeItem pItem = tItem.getParentItem();

					if (pItem != null && pItem.equals(wTreeClassesItem)) {
						if (folder.getItemCount() > 1)
							tMenu.getItem(0).setEnabled(true);
						else
							tMenu.getItem(0).setEnabled(false);
						tMenu.getItem(1).setEnabled(true);
						tMenu.getItem(3).setEnabled(false);
					} else if (tItem.equals(wTreeClassesitem)) {
						tMenu.getItem(0).setEnabled(false);
						tMenu.getItem(1).setEnabled(false);
						tMenu.getItem(3).setEnabled(false);
					} else if (tItem.getText() != null && tItem.getText().equals("Snippit")) {
						tMenu.getItem(0).setEnabled(false);
						tMenu.getItem(1).setEnabled(false);
						tMenu.getItem(3).setEnabled(true);
					} else {
						tMenu.getItem(0).setEnabled(false);
						tMenu.getItem(1).setEnabled(false);
						tMenu.getItem(3).setEnabled(false);
					}
				}
			}
		});
		wTree.setMenu(tMenu);
	}

	private void addRenameToTreeScriptItems() {
		lastItem = new TreeItem[1];
		editor = new TreeEditor(wTree);
		wTree.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				final TreeItem item = (TreeItem) event.item;
				renameFunction(item);
			}
		});
	}

	// This function is for a Windows Like renaming inside the tree
	private void renameFunction(TreeItem tItem) {
		final TreeItem item = tItem;
		if (item.getParentItem() != null && item.getParentItem().equals(wTreeClassesItem)) {
			if (item != null && item == lastItem[0]) {
				boolean isCarbon = SWT.getPlatform().equals("carbon");
				final Composite composite = new Composite(wTree, SWT.NONE);
				if (!isCarbon) composite.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_BLACK));
				final Text text = new Text(composite, SWT.NONE);
				final int inset = isCarbon ? 0 : 1;
				composite.addListener(SWT.Resize, new Listener() {
					public void handleEvent(Event e) {
						Rectangle rect = composite.getClientArea();
						text.setBounds(rect.x + inset, rect.y + inset, rect.width - inset * 2, rect.height - inset * 2);
					}
				});
				Listener textListener = new Listener() {
					public void handleEvent(final Event e) {
						switch (e.type) {
							case SWT.FocusOut:
								if (text.getText().length() > 0) {
									// Check if the field_name Exists
									if (getCTabItemByName(text.getText()) == null) {
										modifyCTabItem(item, TabActions.RENAME_ITEM, text.getText());
										item.setText(cleanClassName(text.getText()));
									}
								}
								composite.dispose();
								break;
							case SWT.Verify:
								String newText = text.getText();
								String leftText = newText.substring(0, e.start);
								String rightText = newText.substring(e.end, newText.length());
								GC gc = new GC(text);
								Point size = gc.textExtent(leftText + e.text + rightText);
								gc.dispose();
								size = text.computeSize(size.x, SWT.DEFAULT);
								editor.horizontalAlignment = SWT.LEFT;
								Rectangle itemRect = item.getBounds(),
								rect = wTree.getClientArea();
								editor.minimumWidth = Math.max(size.x, itemRect.width) + inset * 2;
								int left = itemRect.x,
								right = rect.x + rect.width;
								editor.minimumWidth = Math.min(editor.minimumWidth, right - left);
								editor.minimumHeight = size.y + inset * 2;
								editor.layout();
								break;
							case SWT.Traverse:
								switch (e.detail) {
									case SWT.TRAVERSE_RETURN:
										if (text.getText().length() > 0) {
											// Check if the field_name Exists
											if (getCTabItemByName(text.getText()) == null) {
												modifyCTabItem(item, TabActions.RENAME_ITEM, text.getText());
												item.setText(cleanClassName(text.getText()));
											}
										}
									case SWT.TRAVERSE_ESCAPE:
										composite.dispose();
										e.doit = false;
								}
								break;
						}
					}
				};
				text.addListener(SWT.FocusOut, textListener);
				text.addListener(SWT.Traverse, textListener);
				text.addListener(SWT.Verify, textListener);
				editor.setEditor(composite, item);
				text.setText(item.getText());
				text.selectAll();
				text.setFocus();

			}
		}
		lastItem[0] = item;
	}

	private String cleanClassName(String unsafeName) {
		return unsafeName.replaceAll("(?:^[^\\p{Alpha}])|[^\\w]", "");
	}
}
